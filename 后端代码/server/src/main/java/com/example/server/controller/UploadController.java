package com.example.server.controller;

import com.example.common.result.Result;
import com.example.server.service.OssService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/upload")
public class UploadController {

    @Autowired
    private OssService ossService;

    /**
     * 上传图片
     */
    @PostMapping(value = "/image", consumes = {"multipart/form-data"})
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file,
                                      HttpServletRequest request) throws Exception {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) {
            return Result.error(401, "未登录");
        }
        
        // 检查文件类型（兼容部分浏览器未设置 contentType 的场景）
        String ct = file.getContentType();
        boolean isImage = (ct != null && ct.startsWith("image/"));
        if (!isImage) {
            String original = file.getOriginalFilename();
            String lower = original == null ? "" : original.toLowerCase();
            isImage = lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".gif") || lower.endsWith(".webp");
        }
        if (!isImage) {
            return Result.error(400, "只能上传图片文件");
        }

        // 检查文件大小 (5MB)
        if (file.getSize() > 5L * 1024 * 1024) {
            return Result.error(400, "图片大小不能超过5MB");
        }
        
        try {
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            // 使用 UUID 生成文件名，避免中文名导致 OSS 链接无法访问，统一放到 uploads 目录下
            String objectKey = "uploads/" + java.util.UUID.randomUUID().toString().replace("-", "") + ext;
            String url = ossService.upload(objectKey, file.getInputStream());
            return Result.success("上传成功", url);
        } catch (Exception e) {
            return Result.error(500, "上传失败: " + e.getMessage());
        }
    }
}
