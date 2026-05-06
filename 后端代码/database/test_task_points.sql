-- 测试任务积分发放功能
USE `smart_party_building`;

-- 1. 查看当前任务状态
SELECT id, title, status, points FROM tasks WHERE id = 1;

-- 2. 查看任务参与者
SELECT ta.id, ta.task_id, ta.user_id, ta.status, ta.points, m.username 
FROM task_assignments ta 
LEFT JOIN members m ON m.id = ta.user_id 
WHERE ta.task_id = 1;

-- 3. 查看用户积分记录
SELECT p.id, p.user_id, p.points, p.description, p.created_at, m.username
FROM points p 
LEFT JOIN members m ON m.id = p.user_id 
ORDER BY p.created_at DESC;

-- 4. 手动完结任务（测试用）
-- UPDATE tasks SET status = 'ENDED' WHERE id = 1;

-- 5. 查看任务完结后的积分记录
-- SELECT p.id, p.user_id, p.points, p.description, p.created_at, m.username
-- FROM points p 
-- LEFT JOIN members m ON m.id = p.user_id 
-- WHERE p.description LIKE '%完成任务%'
-- ORDER BY p.created_at DESC;
