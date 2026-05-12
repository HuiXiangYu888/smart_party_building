package com.example.server.controller;

import com.example.common.result.Result;
import com.example.pojo.entity.Announcement;
import com.example.pojo.entity.PartyBranch;
import com.example.server.mapper.AnnouncementMapper;
import com.example.server.mapper.PartyBranchMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementMapper announcementMapper;
    
    @Autowired
    private PartyBranchMapper partyBranchMapper;

    /**
     * 获取公告列表（管理端 - 真分页）
     */
    @GetMapping
    public Result<java.util.Map<String, Object>> getAllAnnouncements(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortField", defaultValue = "created_at") String sortField,
            @RequestParam(value = "sortOrder", defaultValue = "DESC") String sortOrder) {
        
        // 防注入，验证 sortField 和 sortOrder
        if (!"created_at".equals(sortField) && !"updated_at".equals(sortField)) {
            sortField = "created_at";
        }
        if (!"ASC".equalsIgnoreCase(sortOrder) && !"DESC".equalsIgnoreCase(sortOrder)) {
            sortOrder = "DESC";
        }

        int offset = (page - 1) * size;
        List<Announcement> records;
        int total;
        if (keyword != null && !keyword.trim().isEmpty()) {
            List<Announcement> all = announcementMapper.selectByKeyword(keyword.trim(), sortField, sortOrder);
            total = all.size();
            records = all.subList(Math.min(offset, total), Math.min(offset + size, total));
        } else {
            records = announcementMapper.selectWithPagination(offset, size, sortField, sortOrder);
            total = announcementMapper.selectCount();
        }
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("records", records);
        data.put("total", total);
        return Result.success(data);
    }


    /**
     * 分页获取公告
     */
    @GetMapping("/page")
    public Result<List<Announcement>> getAnnouncementsByPage(@RequestParam(defaultValue = "1") int page,
                                                             @RequestParam(defaultValue = "10") int size,
                                                             @RequestParam(value = "sortField", defaultValue = "created_at") String sortField,
                                                             @RequestParam(value = "sortOrder", defaultValue = "DESC") String sortOrder) {
        int offset = (page - 1) * size;
        List<Announcement> announcements = announcementMapper.selectWithPagination(offset, size, sortField, sortOrder);
        int total = announcementMapper.selectCount();
        
        // 这里可以返回分页信息，简化处理直接返回数据
        return Result.success(announcements);
    }

    /**
     * 根据ID获取公告详情
     */
    @GetMapping("/{id}")
    public Result<Announcement> getAnnouncementById(@PathVariable Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            return Result.error(404, "公告不存在");
        }
        return Result.success(announcement);
    }

    /**
     * 创建公告（管理端）
     */
    @PostMapping
    public Result<String> createAnnouncement(@RequestBody Announcement announcement, HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) {
            return Result.error(401, "未登录");
        }
        
        Long adminId = (Long) userIdAttr;
        announcement.setCreatedBy(adminId);
        announcement.setCreatedAt(LocalDateTime.now());
        
        int result = announcementMapper.insert(announcement);
        if (result > 0) {
            return Result.success("公告创建成功");
        } else {
            return Result.error(500, "公告创建失败");
        }
    }

    /**
     * 更新公告（管理端）
     */
    @PutMapping("/{id}")
    public Result<String> updateAnnouncement(@PathVariable Long id, @RequestBody Announcement announcement, HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) {
            return Result.error(401, "未登录");
        }
        
        Announcement existingAnnouncement = announcementMapper.selectById(id);
        if (existingAnnouncement == null) {
            return Result.error(404, "公告不存在");
        }
        
        announcement.setId(id);
        announcement.setLastModifiedBy((Long) userIdAttr); // 记录修改人
        int result = announcementMapper.update(announcement);
        if (result > 0) {
            return Result.success("公告更新成功");
        } else {
            return Result.error(500, "公告更新失败");
        }
    }

    /**
     * 删除公告（管理端）
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteAnnouncement(@PathVariable Long id, HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) {
            return Result.error(401, "未登录");
        }
        
        Announcement existingAnnouncement = announcementMapper.selectById(id);
        if (existingAnnouncement == null) {
            return Result.error(404, "公告不存在");
        }
        
        int result = announcementMapper.deleteById(id);
        if (result > 0) {
            return Result.success("公告删除成功");
        } else {
            return Result.error(500, "公告删除失败");
        }
    }

    /**
     * 获取用户端公告列表（全部）
     */
    @GetMapping("/user")
    public Result<List<Announcement>> getUserAnnouncements() {
        List<Announcement> announcements = announcementMapper.selectAllWithDetails();
        return Result.success(announcements);
    }

    /**
     * 获取最新公告（用户端首页显示）
     */
    @GetMapping("/latest")
    public Result<List<Announcement>> getLatestAnnouncements(@RequestParam(defaultValue = "5") int limit) {
        List<Announcement> announcements = announcementMapper.selectWithPagination(0, limit, "created_at", "DESC");
        return Result.success(announcements);
    }
}
