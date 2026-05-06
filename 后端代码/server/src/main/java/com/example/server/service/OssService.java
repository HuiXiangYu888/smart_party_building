package com.example.server.service;

import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;

@Service
public class OssService {
    private static final Logger log = LoggerFactory.getLogger(OssService.class);

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.bucket-name}")
    private String bucket;

    @Value("${aliyun.oss.region:}")
    private String region;

    @Value("${aliyun.oss.access-key-id:}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret:}")
    private String accessKeySecret;

    private String resolveRegion() {
        if (region != null && !region.trim().isEmpty()) return region;
        // 从 endpoint 推断 region，例如 https://oss-cn-beijing.aliyuncs.com → cn-beijing
        if (endpoint == null) return null;
        String host = endpoint.replace("https://", "").replace("http://", "");
        // 形如 oss-cn-xxx.aliyuncs.com
        if (host.startsWith("oss-") && host.contains(".aliyuncs.com")) {
            String withoutPrefix = host.substring("oss-".length());
            int dot = withoutPrefix.indexOf('.');
            if (dot > 0) {
                return withoutPrefix.substring(0, dot);
            }
        }
        return null;
    }

    private String getFinalRegion() {
        String r = resolveRegion();
        if (r == null || r.trim().isEmpty()) {
            // 安全兜底，默认北京。如需其他Region，请在配置中显式设置 aliyun.oss.region
            r = "cn-beijing";
        }
        return r;
    }

    private boolean isPlaceholder(String v) {
        if (v == null) return true;
        String s = v.trim().toLowerCase();
        if (s.isEmpty()) return true;
        // 忽略示例占位符
        return s.contains("your_endpoint_here") || s.contains("your_bucket_name_here")
                || s.contains("dev_endpoint_here") || s.contains("dev_bucket_name_here")
                || s.contains("access-key-id_here") || s.contains("access-key-secret_here");
    }

    public String upload(String objectKey, InputStream inputStream) {
        ClientBuilderConfiguration cfg = new ClientBuilderConfiguration();
        cfg.setSignatureVersion(SignVersion.V4);
        com.aliyun.oss.common.auth.CredentialsProvider cp;
        try {
            // 优先环境变量
            cp = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        } catch (com.aliyuncs.exceptions.ClientException e) {
            // 退化为配置中的 AK/SK
            if (accessKeyId != null && !accessKeyId.isEmpty() && accessKeySecret != null && !accessKeySecret.isEmpty()) {
                cp = new DefaultCredentialProvider(accessKeyId, accessKeySecret);
            } else {
                throw new RuntimeException("未找到OSS凭证，请配置环境变量 OSS_ACCESS_KEY_ID/OSS_ACCESS_KEY_SECRET 或在 application.yml 中配置 aliyun.oss.access-key-id/access-key-secret", e);
            }
        }
        // endpoint 兜底：优先应用配置，其次环境变量 OSS_ENDPOINT
        String ep = endpoint;
        if (isPlaceholder(ep)) ep = null;
        if (ep == null || ep.trim().isEmpty()) {
            ep = System.getenv("OSS_ENDPOINT");
        }
        if (ep == null || ep.trim().isEmpty()) {
            // 兜底默认北京
            ep = "https://oss-cn-beijing.aliyuncs.com";
        }
        log.info("OSS resolved endpoint={}, bucket={}, region={}", ep, (bucket == null ? "<null>" : bucket), getFinalRegion());
        String finalRegion = getFinalRegion();
        OSS client = OSSClientBuilder.create()
                .endpoint(ep)
                .credentialsProvider(cp)
                .clientConfiguration(cfg)
                .region(finalRegion)
                .build();
        try {
            if (isPlaceholder(bucket) || bucket == null || bucket.trim().isEmpty()) {
                String envBucket = System.getenv("OSS_BUCKET_NAME");
                if (envBucket == null || envBucket.trim().isEmpty()) {
                    envBucket = System.getenv("OSS_BUCKET");
                }
                if (envBucket == null || envBucket.trim().isEmpty()) {
                    // 最终兜底，按你的示例默认 bucket 名
                    envBucket = "my---demo";
                }
                bucket = envBucket;
            }
            client.putObject(bucket, objectKey, inputStream);
            // 返回可访问URL（按公共读Bucket处理，如为私有需STS签名）
            String host = ep.replace("https://", "").replace("http://", "");
            return "https://" + bucket + "." + host + "/" + objectKey;
        } finally {
            if (client != null) client.shutdown();
        }
    }

    /**
     * 将对象键转换为公网可访问的 URL（假定 Bucket 为公共读）。
     * 若传入的已是 http/https URL，则原样返回。
     */
    public String toPublicUrl(String objectKeyOrUrl) {
        if (objectKeyOrUrl == null || objectKeyOrUrl.trim().isEmpty()) return objectKeyOrUrl;
        String v = objectKeyOrUrl.trim();
        if (v.startsWith("http://") || v.startsWith("https://")) return v;
        String ep = endpoint;
        if (isPlaceholder(ep) || ep == null || ep.trim().isEmpty()) {
            ep = System.getenv("OSS_ENDPOINT");
            if (ep == null || ep.trim().isEmpty()) ep = "https://oss-cn-beijing.aliyuncs.com";
        }
        String b = bucket;
        if (isPlaceholder(b) || b == null || b.trim().isEmpty()) {
            String envBucket = System.getenv("OSS_BUCKET_NAME");
            if (envBucket == null || envBucket.trim().isEmpty()) envBucket = System.getenv("OSS_BUCKET");
            if (envBucket == null || envBucket.trim().isEmpty()) envBucket = "my---demo";
            b = envBucket;
        }
        String host = ep.replace("https://", "").replace("http://", "");
        return "https://" + b + "." + host + "/" + v;
    }

    public void delete(String objectKey) {
        ClientBuilderConfiguration cfg = new ClientBuilderConfiguration();
        cfg.setSignatureVersion(SignVersion.V4);
        com.aliyun.oss.common.auth.CredentialsProvider cp;
        try {
            cp = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        } catch (com.aliyuncs.exceptions.ClientException e) {
            if (accessKeyId != null && !accessKeyId.isEmpty() && accessKeySecret != null && !accessKeySecret.isEmpty()) {
                cp = new DefaultCredentialProvider(accessKeyId, accessKeySecret);
            } else {
                throw new RuntimeException("未找到OSS凭证，请配置环境变量 OSS_ACCESS_KEY_ID/OSS_ACCESS_KEY_SECRET 或在 application.yml 中配置 aliyun.oss.access-key-id/access-key-secret", e);
            }
        }
        String ep = endpoint;
        if (isPlaceholder(ep)) ep = null;
        if (ep == null || ep.trim().isEmpty()) {
            ep = System.getenv("OSS_ENDPOINT");
        }
        if (ep == null || ep.trim().isEmpty()) {
            ep = "https://oss-cn-beijing.aliyuncs.com";
        }
        log.info("OSS resolved endpoint(delete)={}, bucket={}, region={}", ep, (bucket == null ? "<null>" : bucket), getFinalRegion());
        String finalRegion = getFinalRegion();
        OSS client = OSSClientBuilder.create()
                .endpoint(ep)
                .credentialsProvider(cp)
                .clientConfiguration(cfg)
                .region(finalRegion)
                .build();
        try {
            if (isPlaceholder(bucket) || bucket == null || bucket.trim().isEmpty()) {
                String envBucket = System.getenv("OSS_BUCKET_NAME");
                if (envBucket == null || envBucket.trim().isEmpty()) {
                    envBucket = System.getenv("OSS_BUCKET");
                }
                if (envBucket == null || envBucket.trim().isEmpty()) {
                    envBucket = "my---demo";
                }
                bucket = envBucket;
            }
            client.deleteObject(bucket, objectKey);
        } finally {
            if (client != null) client.shutdown();
        }
    }

    public URL generatePresignedUrl(String objectKey, int expireSeconds) {
        ClientBuilderConfiguration cfg = new ClientBuilderConfiguration();
        cfg.setSignatureVersion(SignVersion.V4);
        com.aliyun.oss.common.auth.CredentialsProvider cp;
        try {
            cp = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        } catch (com.aliyuncs.exceptions.ClientException e) {
            if (accessKeyId != null && !accessKeyId.isEmpty() && accessKeySecret != null && !accessKeySecret.isEmpty()) {
                cp = new DefaultCredentialProvider(accessKeyId, accessKeySecret);
            } else {
                throw new RuntimeException("未找到OSS凭证，请配置环境变量或application.yml", e);
            }
        }
        String ep = endpoint;
        if (isPlaceholder(ep)) ep = null;
        if (ep == null || ep.trim().isEmpty()) {
            ep = System.getenv("OSS_ENDPOINT");
        }
        if (ep == null || ep.trim().isEmpty()) {
            ep = "https://oss-cn-beijing.aliyuncs.com";
        }
        String finalRegion = getFinalRegion();
        OSS client = OSSClientBuilder.create()
                .endpoint(ep)
                .credentialsProvider(cp)
                .clientConfiguration(cfg)
                .region(finalRegion)
                .build();
        try {
            if (isPlaceholder(bucket) || bucket == null || bucket.trim().isEmpty()) {
                String envBucket = System.getenv("OSS_BUCKET_NAME");
                if (envBucket == null || envBucket.trim().isEmpty()) envBucket = System.getenv("OSS_BUCKET");
                if (envBucket == null || envBucket.trim().isEmpty()) envBucket = "my---demo";
                bucket = envBucket;
            }
            Date expiration = new Date(System.currentTimeMillis() + expireSeconds * 1000L);
            return client.generatePresignedUrl(bucket, objectKey, expiration);
        } finally {
            if (client != null) client.shutdown();
        }
    }

    public OSS buildClient() {
        ClientBuilderConfiguration cfg = new ClientBuilderConfiguration();
        cfg.setSignatureVersion(SignVersion.V4);
        com.aliyun.oss.common.auth.CredentialsProvider cp;
        try {
            cp = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        } catch (com.aliyuncs.exceptions.ClientException e) {
            if (accessKeyId != null && !accessKeyId.isEmpty() && accessKeySecret != null && !accessKeySecret.isEmpty()) {
                cp = new DefaultCredentialProvider(accessKeyId, accessKeySecret);
            } else {
                throw new RuntimeException("未找到OSS凭证，请配置环境变量或application.yml", e);
            }
        }
        String ep = endpoint;
        if (isPlaceholder(ep)) ep = null;
        if (ep == null || ep.trim().isEmpty()) ep = System.getenv("OSS_ENDPOINT");
        if (ep == null || ep.trim().isEmpty()) ep = "https://oss-cn-beijing.aliyuncs.com";
        String finalRegion = getFinalRegion();
        return OSSClientBuilder.create()
                .endpoint(ep)
                .credentialsProvider(cp)
                .clientConfiguration(cfg)
                .region(finalRegion)
                .build();
    }
}


