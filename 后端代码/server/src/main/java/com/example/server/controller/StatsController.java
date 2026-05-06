package com.example.server.controller;

import com.example.common.result.Result;
import com.example.server.mapper.StatsMapper;
import com.example.server.mapper.AdminMapper;
import com.example.pojo.entity.Admin;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stats")
public class StatsController {

    @Autowired
    private StatsMapper statsMapper;
    
    @Autowired
    private AdminMapper adminMapper;

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview(HttpServletRequest request) {
        Object userType = request.getAttribute("userType");
        if (userType == null) return Result.error(401, "未登录");
        String type = userType.toString();
        if (!"SYSTEM_ADMIN".equals(type) && !"BRANCH_ADMIN".equals(type)) {
            return Result.error(403, "无权限");
        }
        
        Map<String, Object> data = new HashMap<>();
        
        // 支部管理员只能看到所属支部的数据
        if ("BRANCH_ADMIN".equals(type)) {
            Long adminId = (Long) request.getAttribute("userId");
            System.out.println("🔍 支部管理员统计开始 - AdminId: " + adminId);
            
            Admin admin = adminMapper.selectById(adminId);
            System.out.println("🔍 查询到的管理员信息: " + (admin != null ? admin.toString() : "null"));
            
            if (admin != null && admin.getBranchId() != null) {
                Long branchId = admin.getBranchId();
                System.out.println("🔍 管理员关联的支部ID: " + branchId);
                
                // 使用按支部ID统计的方法
                int memberCount = statsMapper.countMembersByBranchId(branchId);
                int pendingApplications = statsMapper.countPendingApplicationsByBranchId(branchId);
                int ongoingActivities = statsMapper.countOngoingActivitiesByBranchId(branchId);
                int pendingAssignments = statsMapper.countPendingAssignmentsByBranchId(branchId);
                
                // 额外验证：直接查询所有成员和该支部成员
                int totalMembers = statsMapper.countMembers();
                System.out.println("🔍 验证数据 - 总成员数: " + totalMembers + ", 支部" + branchId + "成员数: " + memberCount);
                
                System.out.println("🔍 统计结果 - MemberCount: " + memberCount + 
                    ", PendingApplications: " + pendingApplications + 
                    ", OngoingActivities: " + ongoingActivities + 
                    ", PendingAssignments: " + pendingAssignments);
                
                data.put("memberCount", memberCount);
                data.put("pendingApplications", pendingApplications);
                data.put("ongoingActivities", ongoingActivities);
                data.put("pendingAssignments", pendingAssignments);
                
                // 添加调试信息
                System.out.println("✅ 支部管理员统计完成 - AdminId: " + adminId + ", BranchId: " + branchId + 
                    ", MemberCount: " + memberCount + ", PendingApplications: " + pendingApplications);
            } else {
                // 如果支部管理员没有关联支部，返回0
                data.put("memberCount", 0);
                data.put("pendingApplications", 0);
                data.put("ongoingActivities", 0);
                data.put("pendingAssignments", 0);
                System.out.println("❌ 支部管理员没有关联支部 - AdminId: " + adminId + 
                    ", Admin: " + (admin != null ? "存在" : "null") + 
                    ", BranchId: " + (admin != null ? admin.getBranchId() : "null"));
            }
        } else {
            // 超级管理员可以看到所有数据
            int memberCount = statsMapper.countMembers();
            int pendingApplications = statsMapper.countPendingApplications();
            int ongoingActivities = statsMapper.countOngoingActivities();
            int pendingAssignments = statsMapper.countPendingAssignments();
            
            data.put("memberCount", memberCount);
            data.put("pendingApplications", pendingApplications);
            data.put("ongoingActivities", ongoingActivities);
            data.put("pendingAssignments", pendingAssignments);
            
            // 添加调试信息
            System.out.println("超级管理员统计 - MemberCount: " + memberCount + 
                ", PendingApplications: " + pendingApplications + 
                ", OngoingActivities: " + ongoingActivities + 
                ", PendingAssignments: " + pendingAssignments);
        }
        
        return Result.success(data);
    }

    @GetMapping("/branch-study-durations")
    public Result<List<Map<String, Object>>> branchStudyDurations(HttpServletRequest request) {
        Object userType = request.getAttribute("userType");
        if (userType == null) return Result.error(401, "未登录");
        String type = userType.toString();
        if (!"SYSTEM_ADMIN".equals(type) && !"BRANCH_ADMIN".equals(type)) {
            return Result.error(403, "无权限");
        }
        
        List<Map<String, Object>> list = statsMapper.selectBranchStudyDurations();
        
        // 支部管理员只能看到所属支部的学习数据
        if ("BRANCH_ADMIN".equals(type)) {
            Long adminId = (Long) request.getAttribute("userId");
            Admin admin = adminMapper.selectById(adminId);
            if (admin != null && admin.getBranchId() != null) {
                Long branchId = admin.getBranchId();
                list = list.stream().filter(item -> {
                    Object itemBranchId = item.get("branchId");
                    return itemBranchId != null && branchId.equals(Long.valueOf(itemBranchId.toString()));
                }).collect(java.util.stream.Collectors.toList());
            } else {
                list = java.util.Collections.emptyList();
            }
        }
        
        return Result.success(list);
    }
}


