package com.example.server.controller;

import com.example.common.result.Result;
import com.example.pojo.entity.StudyResource;
import com.example.server.mapper.StudyResourceMapper;
import com.example.server.service.OssService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/study/resources")
public class StudyResourceController {

    @Autowired
    private StudyResourceMapper studyResourceMapper;
    @Autowired
    private OssService ossService;

    @GetMapping
    public Result<List<StudyResource>> list(@RequestParam(value = "type", required = false) String type) {
        List<StudyResource> list = studyResourceMapper.selectAll(type);
        
        // 补充：为每个资源提供一个可直接下载/预览的短期签名URL（前端更稳）
        for (StudyResource r : list) {
            try {
                String url = r.getUrl();
                int idx = url.indexOf('/', 8);
                String key = (idx > 0 && idx + 1 < url.length()) ? url.substring(idx + 1) : url;
                String signed = ossService.generatePresignedUrl(key, 600).toString();
                r.setUrl(signed);
            } catch (Exception ignored) {}
        }
        return Result.success(list);
    }
    

    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    public Result<StudyResource> upload(@RequestParam("file") MultipartFile file,
                                        @RequestParam(value = "cover", required = false) MultipartFile cover,
                                        @RequestParam("title") String title,
                                        @RequestParam("type") String type,
                                        HttpServletRequest request) throws Exception {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) return Result.error(401, "未登录");
        Long adminId = (Long) userIdAttr;
        
        // 只允许视频类型
        if (!"VIDEO".equals(type)) {
            return Result.error(400, "只支持视频资源上传");
        }
        
        // 检查文件大小
        if (file.getSize() > 2L * 1024 * 1024 * 1024) { // 2GB
            return Result.error(400, "文件大小不能超过2GB");
        }
        
        try {
            String objectKey = "study/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String url = ossService.upload(objectKey, file.getInputStream());
            String coverUrl = null;
            if (cover != null && !cover.isEmpty()) {
                String coverKey = "study/cover/" + System.currentTimeMillis() + "_" + cover.getOriginalFilename();
                coverUrl = ossService.upload(coverKey, cover.getInputStream());
            }
            StudyResource r = new StudyResource();
            r.setTitle(title);
            r.setType(type);
            r.setUrl(url);
            r.setCoverUrl(coverUrl);
            r.setCreatedBy(adminId);
            studyResourceMapper.insert(r);
            return Result.success(r);
        } catch (Exception e) {
            return Result.error(500, "上传失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        StudyResource r = studyResourceMapper.selectById(id);
        if (r == null) return Result.error(404, "不存在");
        // 从URL提取objectKey
        String url = r.getUrl();
        String objectKey = url.substring(url.indexOf('/', 8) + 1);
        ossService.delete(objectKey);
        studyResourceMapper.deleteById(id);
        return Result.success("已删除");
    }

    @GetMapping("/{id}/signed-url")
    public Result<Map<String, String>> signedUrl(@PathVariable Long id,
                                                 @RequestParam(value = "expire", defaultValue = "600") int expireSeconds) {
        StudyResource r = studyResourceMapper.selectById(id);
        if (r == null) return Result.error(404, "不存在");
        String url = r.getUrl();
        // 提取 objectKey：去除 https://bucket.endpoint/
        String key = url;
        int idx = url.indexOf('/', 8);
        if (idx > 0 && idx + 1 < url.length()) {
            key = url.substring(idx + 1);
        }
        String signed = ossService.generatePresignedUrl(key, expireSeconds).toString();
        Map<String, String> data = new HashMap<>();
        data.put("url", signed);
        return Result.success(data);
    }
    
    @DeleteMapping("/clear-all")
    public Result<String> clearAll() {
        try {
            // 删除所有学习资源
            studyResourceMapper.deleteAll();
            return Result.success("所有学习资源已清空");
        } catch (Exception e) {
            return Result.error(500, "清空失败: " + e.getMessage());
        }
    }
}


