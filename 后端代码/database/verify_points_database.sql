-- 积分功能数据库验证和修复脚本
USE `smart_party_building`;

-- 1. 检查表是否存在
SELECT '检查表结构' as step;
SHOW TABLES LIKE 'points';
SHOW TABLES LIKE 'tasks';
SHOW TABLES LIKE 'task_assignments';
SHOW TABLES LIKE 'members';
SHOW TABLES LIKE 'party_branches';

-- 2. 检查表结构
SELECT '检查points表结构' as step;
DESCRIBE points;

SELECT '检查tasks表结构' as step;
DESCRIBE tasks;

SELECT '检查task_assignments表结构' as step;
DESCRIBE task_assignments;

-- 3. 检查测试数据
SELECT '检查测试数据' as step;
SELECT COUNT(*) as members_count FROM members;
SELECT COUNT(*) as branches_count FROM party_branches;
SELECT COUNT(*) as admins_count FROM admins;

-- 4. 查看具体数据
SELECT '查看用户数据' as step;
SELECT id, username, branch_id FROM members;

SELECT '查看支部数据' as step;
SELECT id, name, leader_id FROM party_branches;

SELECT '查看管理员数据' as step;
SELECT id, username, branch_id FROM admins;

-- 5. 检查积分表数据
SELECT '检查积分表数据' as step;
SELECT COUNT(*) as points_count FROM points;
SELECT * FROM points ORDER BY created_at DESC LIMIT 10;

-- 6. 手动插入测试积分记录
SELECT '手动插入测试积分记录' as step;
INSERT INTO points (user_id, branch_id, points, description, created_at) 
VALUES (1, 1, 100, '手动测试积分记录', NOW());

-- 7. 验证插入结果
SELECT '验证插入结果' as step;
SELECT * FROM points WHERE user_id = 1 ORDER BY created_at DESC;

-- 8. 检查外键约束
SELECT '检查外键约束' as step;
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'points' 
  AND REFERENCED_TABLE_NAME IS NOT NULL;

-- 9. 测试积分查询
SELECT '测试积分查询' as step;
SELECT user_id, SUM(points) as total_points 
FROM points 
GROUP BY user_id 
ORDER BY total_points DESC;

-- 10. 测试积分排行查询
SELECT '测试积分排行查询' as step;
SELECT 
    m.id AS userId,
    m.username AS username,
    pb.name AS branchName,
    COALESCE(SUM(p.points), 0) AS totalPoints
FROM members m
LEFT JOIN points p ON p.user_id = m.id
LEFT JOIN party_branches pb ON pb.id = m.branch_id
GROUP BY m.id, m.username, pb.name
ORDER BY totalPoints DESC;
