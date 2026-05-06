-- 积分插入问题调试脚本
USE `smart_party_building`;

-- 1. 检查表结构
SELECT '检查points表结构' as step;
DESCRIBE points;

-- 2. 检查测试数据
SELECT '检查测试数据' as step;
SELECT id, username, branch_id FROM members WHERE id = 1;
SELECT id, name FROM party_branches WHERE id = 1;

-- 3. 检查外键约束
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

-- 4. 手动插入测试数据
SELECT '手动插入测试积分记录' as step;
INSERT INTO points (user_id, branch_id, points, description, created_at) 
VALUES (1, 1, 100, '手动测试积分记录', NOW());

-- 5. 验证插入结果
SELECT '验证插入结果' as step;
SELECT * FROM points WHERE user_id = 1 ORDER BY created_at DESC;

-- 6. 检查是否有重复记录
SELECT '检查重复记录' as step;
SELECT COUNT(*) as duplicate_count 
FROM points 
WHERE description LIKE '%手动测试积分记录%';

-- 7. 清理测试数据
SELECT '清理测试数据' as step;
DELETE FROM points WHERE description LIKE '%手动测试积分记录%';

-- 8. 检查积分表当前状态
SELECT '检查积分表当前状态' as step;
SELECT COUNT(*) as total_points FROM points;
SELECT * FROM points ORDER BY created_at DESC LIMIT 5;
