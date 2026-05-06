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
     * 保存视频观看进度
     */
    @PostMapping("/video/progress")
    public Result<String> saveVideoProgress(@RequestBody VideoWatchProgress progress, HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) return Result.error(401, "未登录");
        
        Long userId = (Long) userIdAttr;
        progress.setUserId(userId);
        progress.setLastWatchTime(LocalDateTime.now());
        
        System.out.println("保存视频进度 - userId: " + userId + ", resourceId: " + progress.getResourceId() + 
                          ", currentTime: " + progress.getCurrentTime() + ", totalDuration: " + progress.getTotalDuration());
        
        // 计算观看百分比
        if (progress.getTotalDuration() > 0) {
            BigDecimal percentage = BigDecimal.valueOf(progress.getCurrentTime())
                    .divide(BigDecimal.valueOf(progress.getTotalDuration()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            progress.setWatchPercentage(percentage);
        }
        
        videoWatchProgressMapper.insertOrUpdate(progress);
        System.out.println("视频进度保存成功");
        
        // 检查观看时长，每10分钟获得1积分
        if (progress.getCurrentTime() != null && progress.getCurrentTime() > 0) {
            int watchMinutes = progress.getCurrentTime() / 60; // 转换为分钟
            int earnedPoints = watchMinutes / 10; // 每10分钟1积分
            
            if (earnedPoints > 0) {
                System.out.println("🎬 观看时长: " + watchMinutes + "分钟，可获得积分: " + earnedPoints);
                
                // 检查该视频是否已经发放过积分
                boolean alreadyAwarded = checkIfVideoPointsAlreadyAwarded(userId, progress.getResourceId());
                if (!alreadyAwarded) {
                    System.out.println("✅ 该视频未发放过积分，开始发放观看视频积分");
                    // 发放积分
                    awardVideoWatchingPoints(userId, earnedPoints);
                } else {
                    System.out.println("⚠️ 该视频已经发放过积分，跳过");
                }
            } else {
                System.out.println("📊 观看时长: " + watchMinutes + "分钟，未达到10分钟，不发放积分");
            }
        } else {
            System.out.println("📊 观看时长数据无效，不发放积分");
        }
        
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
        System.out.println("获取视频进度 - userId: " + userId + ", resourceId: " + resourceId);
        
        VideoWatchProgress progress = videoWatchProgressMapper.selectByUserIdAndResourceId(userId, resourceId);
        System.out.println("查询到的进度数据: " + progress);
        
        return Result.success(progress);
    }

    /**
     * 检查是否已经发放过视频观看积分
     */
    private boolean checkIfVideoPointsAlreadyAwarded(Long userId, Long resourceId) {
        // 查询积分记录，检查今天是否已经发放过观看视频积分
        List<Points> existingPoints = pointsMapper.selectByUserId(userId);
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        
        for (Points point : existingPoints) {
            if (point.getDescription().contains("观看学习视频") && 
                point.getCreatedAt().isAfter(today)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 发放观看视频积分
     */
    private void awardVideoWatchingPoints(Long userId, int points) {
        System.out.println("🎁 开始发放观看视频积分 - 用户: " + userId + ", 积分: " + points);
        
        try {
            // 获取用户信息
            Member member = memberMapper.selectById(userId);
            if (member == null) {
                System.out.println("❌ 用户不存在，无法发放积分: " + userId);
                return;
            }

            System.out.println("✅ 用户信息: " + member.getUsername() + ", 支部ID: " + member.getBranchId());

            // 创建积分记录
            Points pointsRecord = new Points();
            pointsRecord.setUserId(userId);
            pointsRecord.setBranchId(member.getBranchId());
            pointsRecord.setPoints(points); // 根据观看时长计算的积分
            pointsRecord.setDescription("观看学习视频");
            pointsRecord.setCreatedAt(LocalDateTime.now());
            
            // 插入积分记录
            int insertResult = pointsMapper.insert(pointsRecord);
            if (insertResult > 0) {
                System.out.println("✅ 观看视频积分发放成功: " + insertResult + ", 积分ID: " + pointsRecord.getId());
                
                // 验证插入结果
                List<Points> userPoints = pointsMapper.selectByUserId(userId);
                System.out.println("🔍 用户积分记录数量: " + userPoints.size());
            } else {
                System.out.println("❌ 观看视频积分发放失败");
            }
        } catch (Exception e) {
            System.out.println("❌ 发放观看视频积分失败: " + e.getMessage());
            e.printStackTrace();
        }
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
