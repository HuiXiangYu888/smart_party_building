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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

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

        try {
            Task originalTask = taskMapper.selectById(id);
            if (originalTask == null) return Result.error(404, "任务不存在");

            String originalStatus = originalTask.getStatus();
            String newStatus = task.getStatus();
            log.info("任务状态变更: {} -> {}, taskId={}", originalStatus, newStatus, id);

            task.setId(id);
            taskMapper.update(task);
            handleTaskStatusChange(id, originalStatus, newStatus, originalTask);

            Task db = taskMapper.selectById(id);
            return Result.success(db);
        } catch (Exception e) {
            log.error("更新任务状态失败: taskId={}", id, e);
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

    @PutMapping("/assignments/{assignmentId}/status")
    public Result<String> updateAssignmentStatus(@PathVariable Long assignmentId, @RequestParam String status) {
        taskAssignmentMapper.updateStatus(assignmentId, status);
        return Result.success("状态已更新");
    }

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

    @GetMapping("/{taskId}/evaluations")
    public Result<List<TaskEvaluation>> listEvaluations(@PathVariable Long taskId) {
        List<TaskEvaluation> list = taskEvaluationMapper.selectByTaskId(taskId);
        return Result.success(list);
    }

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
        Task task = taskMapper.selectById(taskId);
        if (task == null) return Result.error(404, "任务不存在");
        if (task.getCapacity() != null) {
            if (task.getCapacity() <= 0) return Result.error(400, "报名人数已满");
            int enrolled = taskAssignmentMapper.countByTaskId(taskId);
            if (enrolled >= task.getCapacity()) return Result.error(400, "报名人数已满");
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
    @Transactional
    public Result<String> endTask(@PathVariable Long taskId, HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) return Result.error(401, "未登录");

        try {
            Task task = taskMapper.selectById(taskId);
            if (task == null) return Result.error(404, "任务不存在");
            if ("ENDED".equals(task.getStatus())) return Result.error(400, "任务已经完结");

            List<TaskAssignment> assignments = taskAssignmentMapper.selectByTaskId(taskId);
            if (assignments.isEmpty()) return Result.error(400, "没有参与者，无法发放积分");

            task.setStatus("ENDED");
            taskMapper.update(task);

            Integer taskPoints = task.getPoints();
            if (taskPoints == null || taskPoints <= 0) {
                return Result.success("任务已完结，但积分为0，未发放积分");
            }

            int awardedCount = awardTaskPoints(taskId, assignments, taskPoints, task.getTitle());
            return Result.success("任务已完结，积分已发放给 " + awardedCount + " 个用户");
        } catch (Exception e) {
            log.error("任务完结失败: taskId={}", taskId, e);
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

        Task task = taskMapper.selectById(taskId);
        if (task == null) return Result.error(404, "任务不存在");

        for (Map.Entry<Long, Integer> entry : userPoints.entrySet()) {
            Long userId = entry.getKey();
            Integer points = entry.getValue();
            try {
                Member member = memberMapper.selectById(userId);
                if (member != null) {
                    Points pointsRecord = new Points();
                    pointsRecord.setUserId(userId);
                    pointsRecord.setBranchId(member.getBranchId());
                    pointsRecord.setPoints(points);
                    pointsRecord.setDescription("管理员分配积分：" + task.getTitle());
                    pointsRecord.setCreatedAt(LocalDateTime.now());
                    pointsMapper.insert(pointsRecord);
                }
            } catch (Exception e) {
                log.warn("为用户 {} 分配积分失败", userId, e);
            }
        }
        return Result.success("积分分配成功");
    }

    /**
     * 清理任务相关积分记录（管理员功能）
     */
    @DeleteMapping("/{taskId}/points")
    @Transactional
    public Result<String> clearTaskPoints(@PathVariable Long taskId, HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) return Result.error(401, "未登录");

        Task task = taskMapper.selectById(taskId);
        if (task == null) return Result.error(404, "任务不存在");

        int deletedCount = pointsMapper.deleteByTaskTitle(task.getTitle());
        log.info("任务积分记录清理: taskId={}, deleted={}", taskId, deletedCount);
        return Result.success("任务积分记录已清理，共删除 " + deletedCount + " 条记录");
    }

    // ================= Private Helpers =================

    private boolean checkIfTaskPointsAlreadyAwarded(Long userId, Long taskId) {
        try {
            List<Points> existingPoints = pointsMapper.selectByUserId(userId);
            for (Points point : existingPoints) {
                if (point.getDescription() != null && point.getDescription().contains("完成任务：")) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.warn("检查任务积分发放状态失败: userId={}, taskId={}", userId, taskId, e);
            return false;
        }
    }

    private void handleTaskStatusChange(Long taskId, String originalStatus, String newStatus, Task task) {
        try {
            List<TaskAssignment> assignments = taskAssignmentMapper.selectByTaskId(taskId);
            if (assignments.isEmpty()) return;

            Integer taskPoints = task.getPoints();
            if (taskPoints == null || taskPoints <= 0) return;

            if (!"ENDED".equals(originalStatus) && "ENDED".equals(newStatus)) {
                awardTaskPoints(taskId, assignments, taskPoints, task.getTitle());
            } else if ("ENDED".equals(originalStatus) && !"ENDED".equals(newStatus)) {
                deductTaskPoints(assignments, taskPoints, task.getTitle());
            }
        } catch (Exception e) {
            log.error("处理任务状态变更积分失败: taskId={}", taskId, e);
        }
    }

    private int awardTaskPoints(Long taskId, List<TaskAssignment> assignments, Integer taskPoints, String taskTitle) {
        int awardedCount = 0;
        for (TaskAssignment assignment : assignments) {
            try {
                if (checkIfTaskPointsAlreadyAwarded(assignment.getUserId(), taskId)) continue;
                Member member = memberMapper.selectById(assignment.getUserId());
                if (member == null) continue;

                Points points = new Points();
                points.setUserId(assignment.getUserId());
                points.setBranchId(member.getBranchId());
                points.setPoints(taskPoints);
                points.setDescription("完成任务：" + taskTitle);
                points.setCreatedAt(LocalDateTime.now());

                int result = pointsMapper.insert(points);
                if (result > 0) awardedCount++;
            } catch (Exception e) {
                log.warn("处理用户 {} 积分时出错", assignment.getUserId(), e);
            }
        }
        log.info("任务积分发放完成: taskId={}, awarded={}", taskId, awardedCount);
        return awardedCount;
    }

    private void deductTaskPoints(List<TaskAssignment> assignments, Integer taskPoints, String taskTitle) {
        pointsMapper.deleteByTaskTitle(taskTitle);
        for (TaskAssignment assignment : assignments) {
            try {
                Member member = memberMapper.selectById(assignment.getUserId());
                if (member != null) {
                    Points deductPoints = new Points();
                    deductPoints.setUserId(assignment.getUserId());
                    deductPoints.setBranchId(member.getBranchId());
                    deductPoints.setPoints(-taskPoints);
                    deductPoints.setDescription("任务状态变更扣减：" + taskTitle);
                    deductPoints.setCreatedAt(LocalDateTime.now());
                    pointsMapper.insert(deductPoints);
                }
            } catch (Exception e) {
                log.warn("处理用户 {} 扣减记录时出错", assignment.getUserId(), e);
            }
        }
    }
}
