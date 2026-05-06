-- 检查积分功能数据库状态
USE `smart_party_building`;

-- 1. 检查points表结构
DESCRIBE points;

-- 2. 检查是否有积分记录
SELECT COUNT(*) as points_count FROM points;

-- 3. 查看现有积分记录
SELECT p.id, p.user_id, p.points, p.description, p.created_at, m.username
FROM points p 
LEFT JOIN members m ON m.id = p.user_id 
ORDER BY p.created_at DESC;

-- 4. 检查任务表
SELECT id, title, status, points FROM tasks WHERE status != 'ENDED' LIMIT 5;

-- 5. 检查任务分配
SELECT ta.id, ta.task_id, ta.user_id, ta.status, m.username, t.title
FROM task_assignments ta 
LEFT JOIN members m ON m.id = ta.user_id 
LEFT JOIN tasks t ON t.id = ta.task_id
LIMIT 10;

-- 6. 手动插入测试积分记录
INSERT INTO points (user_id, branch_id, points, description, created_at) 
VALUES (1, 1, 100, '手动测试积分记录', NOW());

-- 7. 验证插入结果
SELECT p.id, p.user_id, p.points, p.description, p.created_at, m.username
FROM points p 
LEFT JOIN members m ON m.id = p.user_id 
WHERE p.user_id = 1
ORDER BY p.created_at DESC;
