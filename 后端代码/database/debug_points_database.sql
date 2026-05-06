-- 彻底检查积分功能数据库状态
USE `smart_party_building`;

-- 1. 检查points表是否存在
SHOW TABLES LIKE 'points';

-- 2. 检查points表结构
DESCRIBE points;

-- 3. 检查points表数据
SELECT COUNT(*) as points_count FROM points;
SELECT * FROM points LIMIT 10;

-- 4. 检查用户表
SELECT id, username, branch_id FROM members LIMIT 5;

-- 5. 检查任务表
SELECT id, title, status, points FROM tasks LIMIT 5;

-- 6. 检查任务分配表
SELECT ta.id, ta.task_id, ta.user_id, ta.status, m.username 
FROM task_assignments ta 
LEFT JOIN members m ON m.id = ta.user_id 
LIMIT 10;

-- 7. 手动插入测试积分记录
INSERT INTO points (user_id, branch_id, points, description, created_at) 
VALUES (1, 1, 100, '手动测试积分记录', NOW());

-- 8. 验证插入结果
SELECT * FROM points WHERE user_id = 1;

-- 9. 检查数据库连接和权限
SELECT DATABASE(), USER(), NOW();

-- 10. 检查表创建语句
SHOW CREATE TABLE points;
