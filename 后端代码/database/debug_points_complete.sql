-- 完整的积分发放调试脚本
USE `smart_party_building`;

-- 1. 检查数据库表结构
DESCRIBE points;
DESCRIBE tasks;
DESCRIBE task_assignments;

-- 2. 查看当前数据
SELECT '=== 用户信息 ===' as info;
SELECT id, username, branch_id FROM members LIMIT 5;

SELECT '=== 任务信息 ===' as info;
SELECT id, title, status, points FROM tasks LIMIT 5;

SELECT '=== 任务分配信息 ===' as info;
SELECT ta.id, ta.task_id, ta.user_id, ta.status, ta.points, m.username 
FROM task_assignments ta 
LEFT JOIN members m ON m.id = ta.user_id 
LIMIT 10;

SELECT '=== 积分记录 ===' as info;
SELECT p.id, p.user_id, p.points, p.description, p.created_at, m.username
FROM points p 
LEFT JOIN members m ON m.id = p.user_id 
ORDER BY p.created_at DESC;

-- 3. 手动插入测试积分记录
SELECT '=== 插入测试积分记录 ===' as info;
INSERT INTO points (user_id, branch_id, points, description, created_at) 
VALUES (1, 1, 100, '手动测试积分', NOW());

-- 4. 验证插入结果
SELECT '=== 验证积分记录 ===' as info;
SELECT p.id, p.user_id, p.points, p.description, p.created_at, m.username
FROM points p 
LEFT JOIN members m ON m.id = p.user_id 
ORDER BY p.created_at DESC;

-- 5. 测试积分查询
SELECT '=== 测试积分查询 ===' as info;
SELECT id,user_id,branch_id,points,description,created_at
FROM points
WHERE user_id = 1
ORDER BY created_at DESC;

-- 6. 测试积分汇总
SELECT '=== 测试积分汇总 ===' as info;
SELECT COALESCE(SUM(points),0) as total_points FROM points WHERE user_id = 1;

-- 7. 测试积分排行
SELECT '=== 测试积分排行 ===' as info;
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
