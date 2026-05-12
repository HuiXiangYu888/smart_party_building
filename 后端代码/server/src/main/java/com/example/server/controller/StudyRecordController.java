package com.example.server.controller;

import com.example.common.result.Result;
import com.example.pojo.entity.StudyRecord;
import com.example.pojo.entity.VideoWatchProgress;
import com.example.pojo.entity.Admin;
import com.example.pojo.entity.Member;
import com.example.server.mapper.StudyRecordMapper;
import com.example.server.mapper.VideoWatchProgressMapper;
import com.example.server.mapper.AdminMapper;
import com.example.server.mapper.MemberMapper;
import com.example.server.mapper.PointsMapper;
import com.example.pojo.entity.Points;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/study/records")
public class StudyRecordController {

    @Autowired
    private StudyRecordMapper studyRecordMapper;
    
    @Autowired
    private VideoWatchProgressMapper videoWatchProgressMapper;
    
    @Autowired
    private AdminMapper adminMapper;
    
    @Autowired
    private MemberMapper memberMapper;
    
    @Autowired
    private PointsMapper pointsMapper;

    /**
     * 记录学习时长
     */
    @PostMapping("/log")
    public Result<String> logStudyTime(@RequestBody StudyRecord record, HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) return Result.error(401, "未登录");
        
        Long userId = (Long) userIdAttr;
        record.setUserId(userId);
        record.setStartedAt(LocalDateTime.now());
        record.setEndedAt(LocalDateTime.now().plusMinutes(record.getDuration()));
        
        studyRecordMapper.insert(record);
        return Result.success("学习记录已保存");
    }

    /**
     * 获取用户学习记录
     */
    @GetMapping("/user")
    public Result<List<Map<String, Object>>> getUserStudyRecords(HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) return Result.error(401, "未登录");
        
        Long userId = (Long) userIdAttr;
        List<Map<String, Object>> records = studyRecordMapper.selectByUserIdWithTitle(userId);
        return Result.success(records);
    }

    /**
     * 获取用户总学习时长
     */
    @GetMapping("/user/total-duration")
    public Result<Integer> getUserTotalDuration(HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) return Result.error(401, "未登录");
        
        Long userId = (Long) userIdAttr;
        Integer totalDuration = studyRecordMapper.selectTotalDurationByUserId(userId);
        return Result.success(totalDuration);
    }

    /**
     * 获取指定用户的学习记录（管理端）
     */
    @GetMapping("/user/{userId}")
    public Result<List<Map<String, Object>>> getUserStudyRecordsById(@PathVariable Long userId) {
        List<Map<String, Object>> records = studyRecordMapper.selectByUserIdWithTitle(userId);
        return Result.success(records);
    }

    /**
     * 保存视频观看进度（仅保存进度，不发放积分）
     */
    @PostMapping("/video/progress")
    public Result<String> saveVideoProgress(@RequestBody VideoWatchProgress progress, HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) return Result.error(401, "未登录");
        
        Long userId = (Long) userIdAttr;
        progress.setUserId(userId);
        progress.setLastWatchTime(LocalDateTime.now());
        
        // 计算观看百分比
        if (progress.getTotalDuration() > 0) {
            BigDecimal percentage = BigDecimal.valueOf(progress.getCurrentTime())
                    .divide(BigDecimal.valueOf(progress.getTotalDuration()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            progress.setWatchPercentage(percentage);
        }
        
        videoWatchProgressMapper.insertOrUpdate(progress);
        return Result.success("观看进度已保存");
    }

    /**
     * 获取视频观看进度
     */
    @GetMapping("/video/progress/{resourceId}")
    public Result<VideoWatchProgress> getVideoProgress(@PathVariable Long resourceId, HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) return Result.error(401, "未登录");
        
        Long userId = (Long) userIdAttr;
        VideoWatchProgress progress = videoWatchProgressMapper.selectByUserIdAndResourceId(userId, resourceId);
        return Result.success(progress);
    }

    /**
     * 视频完整观看完成：发放积分 + 记录学习次数
     * 每个视频只能获得一次积分和一次学习次数，重复观看不增加
     */
    @PostMapping("/video/complete")
    public Result<String> completeVideo(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) return Result.error(401, "未登录");

        Long userId = (Long) userIdAttr;
        Long resId = Long.valueOf(body.get("resourceId").toString());

        // 1. 检查该视频是否已经完成过（通过学习记录判断）
        List<StudyRecord> existing = studyRecordMapper.selectByUserIdAndResourceId(userId, resId);
        if (existing != null && !existing.isEmpty()) {
            // 已经看过这个视频了，不重复计分
            return Result.success("该视频已完成观看，不重复计分");
        }

        // 2. 插入一条学习记录（学习次数 +1）
        StudyRecord record = new StudyRecord();
        record.setUserId(userId);
        record.setResourceId(resId);
        record.setDuration(1); // 至少1分钟
        record.setStartedAt(LocalDateTime.now());
        record.setEndedAt(LocalDateTime.now());
        studyRecordMapper.insert(record);

        // 3. 发放1积分
        Member member = memberMapper.selectById(userId);
        if (member != null) {
            Points pointsRecord = new Points();
            pointsRecord.setUserId(userId);
            pointsRecord.setBranchId(member.getBranchId());
            pointsRecord.setPoints(1);
            pointsRecord.setDescription("完整观看视频(资源#" + resId + ")");
            pointsRecord.setCreatedAt(LocalDateTime.now());
            pointsMapper.insert(pointsRecord);
            System.out.println("✅ 用户" + userId + "完整观看视频#" + resId + "，+1积分");
            return Result.success("完成观看，已获得1积分");
        }

        return Result.success("完成观看");
    }

    /**
     * 获取所有用户学习统计（管理端）
     */
    @GetMapping("/admin/stats")
    public Result<List<Map<String, Object>>> getAllUserStudyStats(@RequestParam(value = "keyword", required = false) String keyword, HttpServletRequest request) {
        Object userType = request.getAttribute("userType");
        if (userType == null) return Result.error(401, "未登录");
        String type = userType.toString();
        if (!"SYSTEM_ADMIN".equals(type) && !"BRANCH_ADMIN".equals(type)) {
            return Result.error(403, "无权限");
        }
        
        List<Map<String, Object>> stats;
        if (keyword != null && !keyword.trim().isEmpty()) {
            stats = studyRecordMapper.selectUserStudyStatsByKeyword(keyword.trim());
        } else {
            stats = studyRecordMapper.selectAllUserStudyStats();
        }
        
        // 支部管理员只能查看所属支部成员的学习数据
        if ("BRANCH_ADMIN".equals(type)) {
            Long adminId = (Long) request.getAttribute("userId");
            Admin admin = adminMapper.selectById(adminId);
            if (admin != null && admin.getBranchId() != null) {
                Long branchId = admin.getBranchId();
                stats = stats.stream().filter(stat -> {
                    Object userIdObj = stat.get("userId");
                    if (userIdObj == null) return false;
                    Long userId = Long.valueOf(userIdObj.toString());
                    Member member = memberMapper.selectById(userId);
                    return member != null && branchId.equals(member.getBranchId());
                }).collect(Collectors.toList());
            } else {
                stats = new ArrayList<>();
            }
        }
        
        return Result.success(stats);
    }

    /**
     * 清空用户学习记录（管理端）
     */
    @DeleteMapping("/admin/user/{userId}")
    public Result<String> clearUserStudyRecords(@PathVariable Long userId, HttpServletRequest request) {
        Object userType = request.getAttribute("userType");
        if (userType == null) return Result.error(401, "未登录");
        String type = userType.toString();
        if (!"SYSTEM_ADMIN".equals(type) && !"BRANCH_ADMIN".equals(type)) {
            return Result.error(403, "无权限");
        }
        
        // 支部管理员只能清空所属支部成员的学习记录
        if ("BRANCH_ADMIN".equals(type)) {
            Long adminId = (Long) request.getAttribute("userId");
            Admin admin = adminMapper.selectById(adminId);
            if (admin != null && admin.getBranchId() != null) {
                Member member = memberMapper.selectById(userId);
                if (member == null || !admin.getBranchId().equals(member.getBranchId())) {
                    return Result.error(403, "只能清空所属支部成员的学习记录");
                }
            } else {
                return Result.error(403, "无权限");
            }
        }
        
        studyRecordMapper.deleteByUserId(userId);
        return Result.success("用户学习记录已清空");
    }

    /**
     * 清空所有学习记录（管理端）
     */
    @DeleteMapping("/admin/all")
    public Result<String> clearAllStudyRecords(HttpServletRequest request) {
        Object userType = request.getAttribute("userType");
        if (userType == null) return Result.error(401, "未登录");
        String type = userType.toString();
        if (!"SYSTEM_ADMIN".equals(type)) {
            return Result.error(403, "只有超级管理员可以清空所有学习记录");
        }
        
        studyRecordMapper.deleteAll();
        return Result.success("所有学习记录已清空");
    }
}
