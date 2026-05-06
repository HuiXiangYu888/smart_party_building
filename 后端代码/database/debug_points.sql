-- 手动测试积分发放功能
USE `smart_party_building`;

-- 1. 查看当前用户
SELECT id, username, branch_id FROM members LIMIT 5;

-- 2. 查看当前任务
SELECT id, title, status, points FROM tasks LIMIT 5;

-- 3. 查看任务分配情况
SELECT ta.id, ta.task_id, ta.user_id, ta.status, ta.points, m.username 
FROM task_assignments ta 
LEFT JOIN members m ON m.id = ta.user_id 
LIMIT 10;

-- 4. 查看当前积分记录
SELECT p.id, p.user_id, p.points, p.description, p.created_at, m.username
FROM points p 
LEFT JOIN members m ON m.id = p.user_id 
ORDER BY p.created_at DESC;

-- 5. 手动插入测试积分记录
INSERT INTO points (user_id, branch_id, points, description, created_at) 
VALUES (1, 1, 20, '测试积分发放 taskId:1', NOW());

-- 6. 再次查看积分记录
SELECT p.id, p.user_id, p.points, p.description, p.created_at, m.username
FROM points p 
LEFT JOIN members m ON m.id = p.user_id 
ORDER BY p.created_at DESC;

-- 7. 查看积分排行
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
