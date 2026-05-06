-- 为task_assignments表添加points字段（如果不存在）
USE `smart_party_building`;

-- 检查points字段是否存在，如果不存在则添加
SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'task_assignments' 
    AND COLUMN_NAME = 'points'
);

SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE task_assignments ADD COLUMN points INT DEFAULT 0 COMMENT ''获得的积分'';',
  'SELECT 1');

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 验证字段是否存在
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'task_assignments' 
  AND COLUMN_NAME = 'points';

-- 显示表结构
DESCRIBE task_assignments;
