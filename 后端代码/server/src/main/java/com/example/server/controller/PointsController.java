package com.example.server.controller;

import com.example.common.result.Result;
import com.example.pojo.entity.Points;
import com.example.pojo.entity.Member;
import com.example.server.mapper.PointsMapper;
import com.example.server.mapper.MemberMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/points")
public class PointsController {

    @Autowired
    private PointsMapper pointsMapper;
    
    @Autowired
    private MemberMapper memberMapper;

    @GetMapping("/user/list")
    public Result<List<Points>> listUserPoints(HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) return Result.error(401, "未登录");
        Long userId = (Long) userIdAttr;
        List<Points> list = pointsMapper.selectByUserId(userId);
        return Result.success(list);
    }

    @GetMapping("/user/summary")
    public Result<Map<String, Object>> summaryUserPoints(HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) return Result.error(401, "未登录");
        Long userId = (Long) userIdAttr;
        Integer total = pointsMapper.sumByUserId(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("total", total == null ? 0 : total);
        return Result.success(data);
    }

        /**
         * 全员积分排行（从低到高）
         * 返回：username, className, totalPoints
         */
        @GetMapping("/rank")
        public Result<List<Map<String, Object>>> rankAllMembers() {
            List<Map<String, Object>> list = pointsMapper.selectAllMemberPointsRank();
            return Result.success(list);
        }

        /**
         * 测试积分发放（调试用）
         */
        @PostMapping("/test-award")
        public Result<String> testAwardPoints(@RequestParam Long userId, @RequestParam Integer points, 
                                           @RequestParam String description, HttpServletRequest request) {
            try {
                System.out.println("🧪 测试积分发放 - 用户: " + userId + ", 积分: " + points);
                
                // 获取用户信息
                Member member = memberMapper.selectById(userId);
                if (member == null) {
                    System.out.println("❌ 用户不存在: " + userId);
                    return Result.error(404, "用户不存在");
                }
                
                System.out.println("✅ 用户信息: " + member.getUsername() + ", 支部ID: " + member.getBranchId());
                
                // 创建积分记录
                Points pointsRecord = new Points();
                pointsRecord.setUserId(userId);
                pointsRecord.setBranchId(member.getBranchId());
                pointsRecord.setPoints(points);
                pointsRecord.setDescription(description);
                pointsRecord.setCreatedAt(LocalDateTime.now());
                
                System.out.println("📝 准备插入积分记录: " + pointsRecord);
                
                // 插入积分记录
                int insertResult = pointsMapper.insert(pointsRecord);
                System.out.println("📊 插入结果: " + insertResult + ", 积分ID: " + pointsRecord.getId());
                
                if (insertResult > 0) {
                    // 验证插入结果
                    List<Points> userPoints = pointsMapper.selectByUserId(userId);
                    System.out.println("🔍 用户积分记录数量: " + userPoints.size());
                    
                    // 打印所有积分记录
                    for (Points p : userPoints) {
                        System.out.println("📋 积分记录: ID=" + p.getId() + ", 积分=" + p.getPoints() + ", 描述=" + p.getDescription());
                    }
                    
                    return Result.success("测试积分发放成功，积分ID: " + pointsRecord.getId() + ", 总记录数: " + userPoints.size());
                } else {
                    System.out.println("❌ 积分记录插入失败，返回结果: " + insertResult);
                    return Result.error(500, "积分发放失败，返回结果: " + insertResult);
                }
                
            } catch (Exception e) {
                System.out.println("❌ 测试积分发放失败: " + e.getMessage());
                e.printStackTrace();
                return Result.error(500, "测试积分发放失败: " + e.getMessage());
            }
        }
        
        /**
         * 直接测试数据库连接和积分插入
         */
        @PostMapping("/direct-test")
        public Result<String> directTestPoints() {
            try {
                System.out.println("🔧 直接测试数据库连接和积分插入");
                
                // 先检查用户是否存在
                Member testMember = memberMapper.selectById(1L);
                if (testMember == null) {
                    System.out.println("❌ 测试用户不存在，创建测试用户");
                    // 创建测试用户
                    Member newMember = new Member();
                    newMember.setUsername("测试用户");
                    newMember.setBranchId(1L);
                    newMember.setPassword("123456");
                    // newMember.setEmail("test@example.com");
                    // newMember.setPhone("13800138000");
                    newMember.setPoliticalStatus("群众");
                    newMember.setCreatedAt(LocalDateTime.now());
                    
                    // 这里需要添加insert方法到MemberMapper
                    System.out.println("⚠️ 需要先创建测试用户");
                }
                
                // 直接插入测试数据
                Points testPoints = new Points();
                testPoints.setUserId(1L);
                testPoints.setBranchId(1L);
                testPoints.setPoints(999);
                testPoints.setDescription("直接测试积分插入");
                testPoints.setCreatedAt(LocalDateTime.now());
                
                System.out.println("📝 准备直接插入: " + testPoints);
                System.out.println("📝 用户ID: " + testPoints.getUserId());
                System.out.println("📝 支部ID: " + testPoints.getBranchId());
                System.out.println("📝 积分: " + testPoints.getPoints());
                System.out.println("📝 描述: " + testPoints.getDescription());
                System.out.println("📝 时间: " + testPoints.getCreatedAt());
                
                int result = pointsMapper.insert(testPoints);
                System.out.println(" 直接插入结果: " + result + ", ID: " + testPoints.getId());
                
                if (result > 0) {
                    // 查询验证
                    List<Points> allPoints = pointsMapper.selectByUserId(1L);
                    System.out.println(" 查询结果数量: " + allPoints.size());
                    
                    // 打印所有积分记录
                    for (Points p : allPoints) {
                        System.out.println("📋 积分记录: ID=" + p.getId() + ", 积分=" + p.getPoints() + ", 描述=" + p.getDescription());
                    }
                    
                    return Result.success("直接测试成功，插入结果: " + result + ", 查询数量: " + allPoints.size());
                } else {
                    System.out.println(" 插入失败，返回结果: " + result);
                    return Result.error(500, "直接插入失败: " + result);
                }
                
            } catch (Exception e) {
                System.out.println("直接测试失败: " + e.getMessage());
                e.printStackTrace();
                return Result.error(500, "直接测试失败: " + e.getMessage());
            }
        }
        
        /**
         * 检查数据库表结构
         */
        @GetMapping("/check-database")
        public Result<Map<String, Object>> checkDatabase() {
            try {
                System.out.println(" 检查数据库表结构");
                
                Map<String, Object> result = new HashMap<>();
                
                // 检查用户数量
                List<Points> allPoints = pointsMapper.selectByUserId(1L);
                result.put("userPointsCount", allPoints.size());
                
                // 检查积分排行
                List<Map<String, Object>> rankList = pointsMapper.selectAllMemberPointsRank();
                result.put("rankListCount", rankList.size());
                
                // 检查用户积分汇总
                Integer userSum = pointsMapper.sumByUserId(1L);
                result.put("userSum", userSum);
                
                System.out.println("数据库检查结果: " + result);
                
                return Result.success(result);
                
            } catch (Exception e) {
                System.out.println(" 数据库检查失败: " + e.getMessage());
                e.printStackTrace();
                return Result.error(500, "数据库检查失败: " + e.getMessage());
            }
        }
        
        /**
         * 获取任务相关积分记录
         */
        @GetMapping("/task/{taskTitle}")
        public Result<List<Points>> getTaskPoints(@PathVariable String taskTitle) {
            try {
                System.out.println("🔍 查询任务积分记录: " + taskTitle);
                
                // 查询所有积分记录，然后过滤出任务相关的
                List<Points> allPoints = pointsMapper.selectByUserId(1L); // 这里需要改进，查询所有用户的积分记录
                List<Points> taskPoints = new ArrayList<>();
                
                for (Points point : allPoints) {
                    if (point.getDescription().contains("完成任务：")) {
                        taskPoints.add(point);
                    }
                }
                
                System.out.println(" 任务积分记录数量: " + taskPoints.size());
                
                return Result.success(taskPoints);
                
            } catch (Exception e) {
                System.out.println("❌ 查询任务积分记录失败: " + e.getMessage());
                e.printStackTrace();
                return Result.error(500, "查询任务积分记录失败: " + e.getMessage());
            }
        }
        
        /**
         * 清理任务积分记录
         */
        @DeleteMapping("/task/{taskTitle}")
        public Result<String> clearTaskPoints(@PathVariable String taskTitle) {
            try {
                System.out.println("🧹 清理任务积分记录: " + taskTitle);
                
                int deletedCount = pointsMapper.deleteByTaskTitle(taskTitle);
                System.out.println("✅ 任务积分记录清理成功，共删除 " + deletedCount + " 条记录");
                
                return Result.success("任务积分记录已清理，共删除 " + deletedCount + " 条记录");
                
            } catch (Exception e) {
                System.out.println("❌ 清理任务积分记录失败: " + e.getMessage());
                e.printStackTrace();
                return Result.error(500, "清理任务积分记录失败: " + e.getMessage());
            }
        }
}


