package com.example.server.controller;

import com.example.common.result.Result;
import com.example.pojo.entity.Task;
import com.example.pojo.entity.TaskAssignment;
import com.example.pojo.entity.TaskEvaluation;
import com.example.server.mapper.TaskAssignmentMapper;
import com.example.server.mapper.TaskEvaluationMapper;
import com.example.server.mapper.TaskMapper;
import com.example.server.mapper.AdminMapper;
import com.example.pojo.entity.Admin;
import com.example.server.mapper.PointsMapper;
import com.example.pojo.entity.Points;
import com.example.server.mapper.MemberMapper;
import com.example.pojo.entity.Member;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskAssignmentMapper taskAssignmentMapper;

    @Autowired
    private TaskEvaluationMapper taskEvaluationMapper;
    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private PointsMapper pointsMapper;
    @Autowired
    private MemberMapper memberMapper;

    @PostMapping
    public Result<Task> create(@RequestBody Task task, HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) return Result.error(401, "未登录");
        Long adminId = (Long) userIdAttr;
        task.setCreatedBy(adminId);
        // 若未传 branchId，则自动使用当前管理员的 branchId
        if (task.getBranchId() == null) {
            Admin admin = adminMapper.selectById(adminId);
            if (admin == null || admin.getBranchId() == null) {
                return Result.error(400, "当前管理员未绑定支部，无法创建任务");
            }
            task.setBranchId(admin.getBranchId());
        }
        task.setCreatedAt(LocalDateTime.now());
        if (task.getStatus() == null) task.setStatus("PUBLISHING");
        if (task.getPoints() == null) task.setPoints(0);
        taskMapper.insert(task);
        return Result.success(task);
    }

    @PutMapping("/{id}")
    @Transactional
    public Result<Task> update(@PathVariable Long id, @RequestBody Task task, HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) return Result.error(401, "未登录");
        
        System.out.println("=== 更新任务状态: " + id + " ===");
        
        try {
            // 获取原任务信息
            Task originalTask = taskMapper.selectById(id);
            if (originalTask == null) {
                return Result.error(404, "任务不存在");
            }
            
            String originalStatus = originalTask.getStatus();
            String newStatus = task.getStatus();
            
            System.out.println("📋 任务状态变更: " + originalStatus + " -> " + newStatus);
            
            // 更新任务
            task.setId(id);
            int updateResult = taskMapper.update(task);
            System.out.println("✅ 任务更新结果: " + updateResult);
            
            // 处理积分发放和扣减
            handleTaskStatusChange(id, originalStatus, newStatus, originalTask);
            
            Task db = taskMapper.selectById(id);
            return Result.success(db);
            
        } catch (Exception e) {
            System.out.println("❌ 更新任务状态失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error(500, "更新任务状态失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) return Result.error(401, "未登录");
        taskAssignmentMapper.deleteByTaskId(id);
        taskMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @GetMapping("/page")
    public Result<Map<String, Object>> page(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        int offset = (page - 1) * size;
        List<Task> records = taskMapper.selectWithPagination(offset, size);
        int total = taskMapper.selectCount();
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", total);
        return Result.success(data);
    }

    /**
     * 小程序公开分页（无需鉴权）
     */
    @GetMapping("/user/page")
    public Result<Map<String, Object>> userPage(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        int offset = (page - 1) * size;
        List<Task> records = taskMapper.selectWithPagination(offset, size);
        int total = taskMapper.selectCount();
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", total);
        return Result.success(data);
    }

    // 分配成员
    @PostMapping("/{taskId}/assignments")
    public Result<String> assign(@PathVariable Long taskId, @RequestBody List<Long> userIds, HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) return Result.error(401, "未登录");
        for (Long userId : userIds) {
            TaskAssignment a = new TaskAssignment();
            a.setTaskId(taskId);
            a.setUserId(userId);
            a.setStatus("PENDING");
            a.setAssignedAt(LocalDateTime.now());
            taskAssignmentMapper.insert(a);
        }
        return Result.success("分配成功");
    }

    // 更新成员任务状态
    @PutMapping("/assignments/{assignmentId}/status")
    public Result<String> updateAssignmentStatus(@PathVariable Long assignmentId, @RequestParam String status) {
        taskAssignmentMapper.updateStatus(assignmentId, status);
        return Result.success("状态已更新");
    }

    // 提交评价
    @PostMapping("/{taskId}/evaluations")
    public Result<String> evaluate(@PathVariable Long taskId, @RequestBody TaskEvaluation evaluation, HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) return Result.error(401, "未登录");
        Long userId = (Long) userIdAttr;
        evaluation.setTaskId(taskId);
        evaluation.setUserId(userId);
        evaluation.setCreatedAt(LocalDateTime.now());
        taskEvaluationMapper.insert(evaluation);
        return Result.success("评价成功");
    }

    // 查看任务评价
    @GetMapping("/{taskId}/evaluations")
    public Result<List<TaskEvaluation>> listEvaluations(@PathVariable Long taskId) {
        List<TaskEvaluation> list = taskEvaluationMapper.selectByTaskId(taskId);
        return Result.success(list);
    }

    // 参与人列表（管理端使用）
    @GetMapping("/{taskId}/participants")
    public Result<List<Map<String, Object>>> participants(@PathVariable Long taskId, HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) return Result.error(401, "未登录");
        List<Map<String, Object>> list = taskAssignmentMapper.selectParticipantsByTaskId(taskId);
        return Result.success(list);
    }

    // ================= 用户端：自助报名/取消报名/我的任务 =================

    @PostMapping("/user/{taskId}/join")
    public Result<String> userJoin(@PathVariable Long taskId, HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) return Result.error(401, "未登录");
        Long userId = (Long) userIdAttr;
        // 人数上限校验（capacity = 0 表示不可报名）
        Task task = taskMapper.selectById(taskId);
        if (task == null) return Result.error(404, "任务不存在");
        if (task.getCapacity() != null) {
            if (task.getCapacity() <= 0) {
                return Result.error(400, "报名人数已满");
            }
            int enrolled = taskAssignmentMapper.countByTaskId(taskId);
            if (enrolled >= task.getCapacity()) {
                return Result.error(400, "报名人数已满");
            }
        }
        TaskAssignment a = new TaskAssignment();
        a.setTaskId(taskId);
        a.setUserId(userId);
        a.setStatus("PENDING");
        a.setAssignedAt(LocalDateTime.now());
        taskAssignmentMapper.insert(a);
        return Result.success("报名成功");
    }

    @PostMapping("/user/{taskId}/cancel")
    public Result<String> userCancel(@PathVariable Long taskId, HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) return Result.error(401, "未登录");
        Long userId = (Long) userIdAttr;
        // 若任务处于待完结，则不允许取消报名
        Task task = taskMapper.selectById(taskId);
        if (task == null) return Result.error(404, "任务不存在");
        if ("PENDING_END".equals(task.getStatus())) {
            return Result.error(400, "当前任务待完结，已报名用户不可取消");
        }
        int affected = taskAssignmentMapper.deleteByTaskIdAndUserId(taskId, userId);
        if (affected > 0) return Result.success("已取消报名");
        return Result.success("未报名或已取消");
    }

    @GetMapping("/user/my")
    public Result<List<Map<String, Object>>> myTasks(HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) return Result.error(401, "未登录");
        Long userId = (Long) userIdAttr;
        List<Map<String, Object>> list = taskAssignmentMapper.selectTasksByUserId(userId);
        return Result.success(list);
    }

    /**
     * 完结任务并发放积分
     */
    @PutMapping("/{taskId}/end")
    public Result<String> endTask(@PathVariable Long taskId, HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) return Result.error(401, "未登录");
        
        System.out.println("=== 开始完结任务: " + taskId + " ===");
        
        try {
            // 检查任务是否存在
            Task task = taskMapper.selectById(taskId);
            if (task == null) {
                System.out.println("❌ 任务不存在: " + taskId);
                return Result.error(404, "任务不存在");
            }
            
            System.out.println("✅ 任务信息: " + task.getTitle() + ", 状态: " + task.getStatus() + ", 积分: " + task.getPoints());
            
            // 检查任务状态
            if ("ENDED".equals(task.getStatus())) {
                System.out.println("❌ 任务已经完结: " + taskId);
                return Result.error(400, "任务已经完结");
            }
            
            // 获取所有参与任务的用户
            List<TaskAssignment> assignments = taskAssignmentMapper.selectByTaskId(taskId);
            System.out.println("📋 任务参与者数量: " + assignments.size());
            
            if (assignments.isEmpty()) {
                System.out.println("❌ 没有参与者，无法发放积分");
                return Result.error(400, "没有参与者，无法发放积分");
            }
            
            // 更新任务状态为已完结
            task.setStatus("ENDED");
            int updateResult = taskMapper.update(task);
            System.out.println("✅ 任务状态更新结果: " + updateResult);
            
            // 为每个参与用户发放积分
            int awardedCount = 0;
            Integer taskPoints = task.getPoints();
            
            if (taskPoints == null || taskPoints <= 0) {
                System.out.println("⚠️ 任务积分为0或null，跳过积分发放");
                return Result.success("任务已完结，但积分为0，未发放积分");
            }
            
            System.out.println("💰 任务积分: " + taskPoints);
            
            for (TaskAssignment assignment : assignments) {
                System.out.println("👤 处理用户: " + assignment.getUserId());
                
                try {
                    // 检查是否已经发放过积分
                    boolean alreadyAwarded = checkIfTaskPointsAlreadyAwarded(assignment.getUserId(), taskId);
                    if (alreadyAwarded) {
                        System.out.println("⚠️ 用户 " + assignment.getUserId() + " 已经发放过积分，跳过");
                        continue;
                    }
                    
                    // 获取用户信息
                    Member member = memberMapper.selectById(assignment.getUserId());
                    if (member == null) {
                        System.out.println("❌ 用户不存在: " + assignment.getUserId());
                        continue;
                    }
                    
                    System.out.println("✅ 用户信息: " + member.getUsername() + ", 支部ID: " + member.getBranchId());
                    
                    // 创建积分记录
                    Points points = new Points();
                    points.setUserId(assignment.getUserId());
                    points.setBranchId(member.getBranchId());
                    points.setPoints(taskPoints);
                    points.setDescription("完成任务：" + task.getTitle());
                    points.setCreatedAt(LocalDateTime.now());
                    
                    // 插入积分记录
                    int insertResult = pointsMapper.insert(points);
                    if (insertResult > 0) {
                        System.out.println("✅ 积分记录插入成功: " + insertResult + ", 积分ID: " + points.getId());
                        awardedCount++;
                        
                        // 验证插入结果
                        List<Points> userPoints = pointsMapper.selectByUserId(assignment.getUserId());
                        System.out.println("🔍 用户积分记录数量: " + userPoints.size());
                    } else {
                        System.out.println("❌ 积分记录插入失败");
                    }
                    
                } catch (Exception e) {
                    System.out.println("❌ 处理用户 " + assignment.getUserId() + " 积分时出错: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("🎉 任务完结完成，共发放积分给 " + awardedCount + " 个用户");
            return Result.success("任务已完结，积分已发放给 " + awardedCount + " 个用户");
            
        } catch (Exception e) {
            System.out.println("❌ 任务完结过程中发生异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error(500, "任务完结失败: " + e.getMessage());
        }
    }

    /**
     * 为任务分配积分（管理员设置每个参与者的积分）
     */
    @PostMapping("/{taskId}/assign-points")
    public Result<String> assignPoints(@PathVariable Long taskId, @RequestBody Map<Long, Integer> userPoints, HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) return Result.error(401, "未登录");
        
        // 检查任务是否存在
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            return Result.error(404, "任务不存在");
        }
        
        // 为每个用户创建积分记录
        for (Map.Entry<Long, Integer> entry : userPoints.entrySet()) {
            Long userId = entry.getKey();
            Integer points = entry.getValue();
            
            try {
                // 获取用户信息
                Member member = memberMapper.selectById(userId);
                if (member != null) {
                    // 创建积分记录
                    Points pointsRecord = new Points();
                    pointsRecord.setUserId(userId);
                    pointsRecord.setBranchId(member.getBranchId());
                    pointsRecord.setPoints(points);
                    pointsRecord.setDescription("管理员分配积分：" + task.getTitle());
                    pointsRecord.setCreatedAt(LocalDateTime.now());
                    
                    // 插入积分记录
                    pointsMapper.insert(pointsRecord);
                }
            } catch (Exception e) {
                System.out.println("为用户 " + userId + " 分配积分失败: " + e.getMessage());
            }
        }
        
        return Result.success("积分分配成功");
    }

    /**
     * 检查是否已经为任务发放过积分
     */
    private boolean checkIfTaskPointsAlreadyAwarded(Long userId, Long taskId) {
        try {
            System.out.println("检查用户 " + userId + " 任务 " + taskId + " 的积分发放状态");
            
            // 查询积分记录，检查是否已经为这个任务发放过积分
            List<Points> existingPoints = pointsMapper.selectByUserId(userId);
            System.out.println("用户 " + userId + " 现有积分记录数量: " + existingPoints.size());
            
            for (Points point : existingPoints) {
                System.out.println("检查积分记录: " + point.getDescription());
                if (point.getDescription().contains("完成任务：")) {
                    System.out.println("发现重复积分记录: " + point.getDescription());
                    return true;
                }
            }
            
            System.out.println("未发现重复积分记录");
            return false;
        } catch (Exception e) {
            System.out.println("检查任务积分发放状态失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 测试积分发放功能（调试用）
     */
    @PostMapping("/{taskId}/test-award-points")
    @Transactional
    public Result<String> testAwardPoints(@PathVariable Long taskId, HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) return Result.error(401, "未登录");
        
        System.out.println("=== 测试积分发放功能 ===");
        
        try {
            // 检查任务是否存在
            Task task = taskMapper.selectById(taskId);
            if (task == null) {
                return Result.error(404, "任务不存在");
            }
            
            System.out.println("任务信息: " + task.getTitle() + ", 积分: " + task.getPoints());
            
            // 获取所有参与任务的用户
            List<TaskAssignment> assignments = taskAssignmentMapper.selectByTaskId(taskId);
            System.out.println("任务参与者数量: " + assignments.size());
            
            // 手动为第一个用户发放积分（测试用）
            if (!assignments.isEmpty()) {
                TaskAssignment assignment = assignments.get(0);
                Long userId = assignment.getUserId();
                
                System.out.println("为测试用户 " + userId + " 发放积分");
                
                // 获取用户信息
                Member member = memberMapper.selectById(userId);
                if (member != null) {
                    System.out.println("用户信息: " + member.getUsername() + ", 支部ID: " + member.getBranchId());
                    
                    // 创建积分记录
                    Points points = new Points();
                    points.setUserId(userId);
                    points.setBranchId(member.getBranchId());
                    points.setPoints(50); // 测试积分
                    points.setDescription("测试积分发放：" + task.getTitle());
                    points.setCreatedAt(LocalDateTime.now());
                    
                    System.out.println("准备插入积分记录: " + points);
                    
                    // 插入积分记录
                    int insertResult = pointsMapper.insert(points);
                    System.out.println("测试积分记录插入结果: " + insertResult + ", 积分ID: " + points.getId());
                    
                    if (insertResult > 0) {
                        // 验证插入结果
                        List<Points> userPoints = pointsMapper.selectByUserId(userId);
                        System.out.println("用户积分记录数量: " + userPoints.size());
                        
                        // 再次查询验证
                        List<Points> allPoints = pointsMapper.selectByUserId(userId);
                        System.out.println("验证查询结果: " + allPoints.size() + " 条记录");
                        
                        return Result.success("测试积分发放成功，积分ID: " + points.getId() + ", 记录数: " + allPoints.size());
                    } else {
                        return Result.error(500, "积分记录插入失败，返回结果: " + insertResult);
                    }
                } else {
                    return Result.error(404, "用户不存在");
                }
            } else {
                return Result.error(400, "没有参与者");
            }
        } catch (Exception e) {
            System.out.println("测试积分发放异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error(500, "测试积分发放异常: " + e.getMessage());
        }
    }

    /**
     * 处理任务状态变更时的积分发放和扣减
     */
    private void handleTaskStatusChange(Long taskId, String originalStatus, String newStatus, Task task) {
        System.out.println("🔄 处理任务状态变更积分逻辑: " + taskId);
        
        try {
            // 获取任务参与者
            List<TaskAssignment> assignments = taskAssignmentMapper.selectByTaskId(taskId);
            System.out.println("📋 任务参与者数量: " + assignments.size());
            
            if (assignments.isEmpty()) {
                System.out.println("⚠️ 没有参与者，跳过积分处理");
                return;
            }
            
            Integer taskPoints = task.getPoints();
            if (taskPoints == null || taskPoints <= 0) {
                System.out.println("⚠️ 任务积分为0或null，跳过积分处理");
                return;
            }
            
            // 情况1：从未完结状态变为已完结 -> 发放积分
            if (!"ENDED".equals(originalStatus) && "ENDED".equals(newStatus)) {
                System.out.println("🎁 任务完结，开始发放积分");
                awardTaskPoints(taskId, assignments, taskPoints, task.getTitle());
            }
            // 情况2：从已完结状态变为其他状态 -> 扣减积分
            else if ("ENDED".equals(originalStatus) && !"ENDED".equals(newStatus)) {
                System.out.println("💸 任务状态变更，开始扣减积分");
                deductTaskPoints(taskId, assignments, taskPoints, task.getTitle());
            }
            else {
                System.out.println("ℹ️ 状态变更不涉及积分处理");
            }
            
        } catch (Exception e) {
            System.out.println("❌ 处理任务状态变更积分失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 发放任务积分
     */
    private void awardTaskPoints(Long taskId, List<TaskAssignment> assignments, Integer taskPoints, String taskTitle) {
        System.out.println("🎁 开始发放任务积分: " + taskId + ", 积分: " + taskPoints);
        
        int awardedCount = 0;
        for (TaskAssignment assignment : assignments) {
            try {
                System.out.println("👤 处理用户: " + assignment.getUserId());
                
                // 检查是否已经发放过积分
                boolean alreadyAwarded = checkIfTaskPointsAlreadyAwarded(assignment.getUserId(), taskId);
                if (alreadyAwarded) {
                    System.out.println("⚠️ 用户 " + assignment.getUserId() + " 已经发放过积分，跳过");
                    continue;
                }
                
                // 获取用户信息
                Member member = memberMapper.selectById(assignment.getUserId());
                if (member == null) {
                    System.out.println("❌ 用户不存在: " + assignment.getUserId());
                    continue;
                }
                
                    // 创建积分记录
                    Points points = new Points();
                    points.setUserId(assignment.getUserId());
                    points.setBranchId(member.getBranchId());
                    points.setPoints(taskPoints);
                    points.setDescription("完成任务：" + taskTitle);
                    points.setCreatedAt(LocalDateTime.now());
                    
                    System.out.println("📝 准备插入积分记录: " + points);
                    System.out.println("📝 用户ID: " + points.getUserId());
                    System.out.println("📝 支部ID: " + points.getBranchId());
                    System.out.println("📝 积分: " + points.getPoints());
                    System.out.println("📝 描述: " + points.getDescription());
                    
                    // 插入积分记录
                    int insertResult = pointsMapper.insert(points);
                    System.out.println("📊 积分记录插入结果: " + insertResult + ", 积分ID: " + points.getId());
                    
                    if (insertResult > 0) {
                        // 立即验证插入结果
                        List<Points> userPoints = pointsMapper.selectByUserId(assignment.getUserId());
                        System.out.println("🔍 用户积分记录数量: " + userPoints.size());
                        
                        System.out.println("✅ 积分发放成功: 用户=" + assignment.getUserId() + ", 积分=" + taskPoints + ", ID=" + points.getId());
                        awardedCount++;
                    } else {
                        System.out.println("❌ 积分发放失败: 用户=" + assignment.getUserId() + ", 插入结果: " + insertResult);
                    }
                
            } catch (Exception e) {
                System.out.println("❌ 处理用户 " + assignment.getUserId() + " 积分时出错: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("🎉 任务积分发放完成，共发放给 " + awardedCount + " 个用户");
    }

    /**
     * 扣减任务积分（直接删除积分记录）
     */
    private void deductTaskPoints(Long taskId, List<TaskAssignment> assignments, Integer taskPoints, String taskTitle) {
        System.out.println("💸 开始删除任务积分记录: " + taskId);
        
        try {
            // 直接删除该任务的所有积分记录
            int deletedCount = pointsMapper.deleteByTaskTitle(taskTitle);
            System.out.println("✅ 任务积分记录删除成功，共删除 " + deletedCount + " 条记录");
            
            // 为每个用户创建扣减记录（可选，用于记录状态变更）
            for (TaskAssignment assignment : assignments) {
                try {
                    System.out.println("👤 处理用户: " + assignment.getUserId());
                    
                    // 获取用户信息
                    Member member = memberMapper.selectById(assignment.getUserId());
                    if (member != null) {
                        // 创建扣减积分记录（负数）
                        Points deductPoints = new Points();
                        deductPoints.setUserId(assignment.getUserId());
                        deductPoints.setBranchId(member.getBranchId());
                        deductPoints.setPoints(-taskPoints); // 负数表示扣减
                        deductPoints.setDescription("任务状态变更扣减：" + taskTitle);
                        deductPoints.setCreatedAt(LocalDateTime.now());
                        
                        // 插入扣减记录
                        int insertResult = pointsMapper.insert(deductPoints);
                        if (insertResult > 0) {
                            System.out.println("✅ 扣减记录创建成功: 用户=" + assignment.getUserId() + ", 扣减=" + taskPoints);
                        }
                    }
                    
                } catch (Exception e) {
                    System.out.println("❌ 处理用户 " + assignment.getUserId() + " 扣减记录时出错: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
        } catch (Exception e) {
            System.out.println("❌ 删除任务积分记录失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("🎉 任务积分扣减完成");
    }

    /**
     * 清理任务相关积分记录（管理员功能）
     */
    @DeleteMapping("/{taskId}/points")
    @Transactional
    public Result<String> clearTaskPoints(@PathVariable Long taskId, HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) return Result.error(401, "未登录");
        
        System.out.println("🧹 清理任务积分记录: " + taskId);
        
        try {
            // 先获取任务信息
            Task task = taskMapper.selectById(taskId);
            if (task == null) {
                return Result.error(404, "任务不存在");
            }
            
            // 删除该任务的所有积分记录
            int deletedCount = pointsMapper.deleteByTaskTitle(task.getTitle());
            System.out.println("✅ 任务积分记录清理成功，共删除 " + deletedCount + " 条记录");
            
            return Result.success("任务积分记录已清理，共删除 " + deletedCount + " 条记录");
            
        } catch (Exception e) {
            System.out.println("❌ 清理任务积分记录失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error(500, "清理任务积分记录失败: " + e.getMessage());
        }
    }



}


