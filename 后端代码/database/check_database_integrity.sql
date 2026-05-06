-- 检查数据库表结构和数据完整性
USE `smart_party_building`;

-- 1. 检查points表是否存在
SHOW TABLES LIKE 'points';

-- 2. 检查points表结构
DESCRIBE points;

-- 3. 检查是否有积分记录
SELECT COUNT(*) as points_count FROM points;

-- 4. 查看现有积分记录
SELECT p.id, p.user_id, p.points, p.description, p.created_at, m.username
FROM points p 
LEFT JOIN members m ON m.id = p.user_id 
ORDER BY p.created_at DESC;

-- 5. 检查任务表
SELECT id, title, status, points FROM tasks LIMIT 5;

-- 6. 检查任务分配表
SELECT ta.id, ta.task_id, ta.user_id, ta.status, m.username, t.title
FROM task_assignments ta 
LEFT JOIN members m ON m.id = ta.user_id 
LEFT JOIN tasks t ON t.id = ta.task_id
LIMIT 10;

-- 7. 手动插入测试积分记录
INSERT INTO points (user_id, branch_id, points, description, created_at) 
VALUES (1, 1, 100, '手动测试积分记录', NOW());

-- 8. 验证插入结果
SELECT p.id, p.user_id, p.points, p.description, p.created_at, m.username
FROM points p 
LEFT JOIN members m ON m.id = p.user_id 
WHERE p.user_id = 1
ORDER BY p.created_at DESC;

-- 9. 检查数据库连接
SELECT DATABASE(), USER(), NOW();
