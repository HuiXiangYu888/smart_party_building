-- 智慧党建系统数据库初始化脚本
-- 设置兼容的 SQL 模式
SET SQL_MODE = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';
SET NAMES utf8mb4;
SET time_zone = '+08:00';

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `smart_party_building` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `smart_party_building`;

-- 删除旧表（从子表到父表，严格按外键依赖顺序）
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS prepare_member_application;
DROP TABLE IF EXISTS party_application;
DROP TABLE IF EXISTS positive_member_application;
DROP TABLE IF EXISTS application_process;
DROP TABLE IF EXISTS task_evaluations;
DROP TABLE IF EXISTS task_assignments;
DROP TABLE IF EXISTS tasks;
DROP TABLE IF EXISTS activity_participants;
DROP TABLE IF EXISTS activities;
DROP TABLE IF EXISTS points;
DROP TABLE IF EXISTS study_records;
DROP TABLE IF EXISTS video_watch_progress;
DROP TABLE IF EXISTS study_resources;
DROP TABLE IF EXISTS announcements;
DROP TABLE IF EXISTS member_update_requests;
DROP TABLE IF EXISTS system_settings;
DROP TABLE IF EXISTS admins;
DROP TABLE IF EXISTS members;
DROP TABLE IF EXISTS party_branches;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. 普通用户表 (members)
CREATE TABLE members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    student_id VARCHAR(20) UNIQUE COMMENT '学号（可选）',
    id_number VARCHAR(18) UNIQUE NOT NULL COMMENT '身份证号',
    password VARCHAR(255) NOT NULL COMMENT '登录密码（MD5加密存储）',
    username VARCHAR(50) NOT NULL COMMENT '姓名',
    mobile VARCHAR(20) NOT NULL COMMENT '手机号',
    branch_id BIGINT NULL COMMENT '所属支部',
    political_status ENUM('共青团员','群众','预备党员','正式党员','其他') NOT NULL DEFAULT '群众' COMMENT '政治面貌',
    join_date DATETIME NULL COMMENT '入党时间（可为空）',
    review_status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING' COMMENT '审核状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT chk_members_mobile_length CHECK (CHAR_LENGTH(mobile) >= 10 AND CHAR_LENGTH(mobile) <= 15),
    CONSTRAINT chk_members_login_method CHECK (student_id IS NOT NULL OR id_number IS NOT NULL)
) COMMENT '普通用户表';

-- 2. 管理员表 (admins)
CREATE TABLE admins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    real_name VARCHAR(50) NULL COMMENT '真实姓名',
    mobile VARCHAR(20) NOT NULL COMMENT '手机号',
    password VARCHAR(255) NOT NULL COMMENT '登录密码（MD5加密存储）',
    admin_type ENUM('BRANCH_ADMIN', 'SYSTEM_ADMIN') NOT NULL COMMENT '管理员类型',
    branch_id BIGINT COMMENT '所属支部ID（仅对BRANCH_ADMIN有效）',
    status ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE' COMMENT '账号状态',
    last_modified_by BIGINT NULL COMMENT '最近一次修改人（admins.id）',
    last_modified_at DATETIME NULL COMMENT '最近一次修改时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT chk_admins_mobile_length CHECK (CHAR_LENGTH(mobile) >= 10 AND CHAR_LENGTH(mobile) <= 15)
) COMMENT '管理员表';

-- 3. 支部表 (party_branches)
CREATE TABLE party_branches (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    name VARCHAR(100) NOT NULL COMMENT '支部名称',
    leader_id BIGINT NOT NULL COMMENT '负责人ID（指向admins.id）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (leader_id) REFERENCES admins(id) ON DELETE CASCADE
) COMMENT '党支部信息表';

-- 4. 为 admins 表添加 branch_id 外键
ALTER TABLE admins
ADD CONSTRAINT fk_admins_branch_id
FOREIGN KEY (branch_id) REFERENCES party_branches(id) ON DELETE SET NULL;

ALTER TABLE admins
ADD CONSTRAINT fk_admins_last_modified_by
FOREIGN KEY (last_modified_by) REFERENCES admins(id) ON DELETE SET NULL;

ALTER TABLE members
ADD CONSTRAINT fk_members_branch_id
FOREIGN KEY (branch_id) REFERENCES party_branches(id) ON DELETE SET NULL;

-- 5. 公告表 (announcements)
CREATE TABLE announcements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    branch_id BIGINT NOT NULL COMMENT '所属支部',
    created_by BIGINT NOT NULL COMMENT '创建人（指向admins.id）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (branch_id) REFERENCES party_branches(id),
    FOREIGN KEY (created_by) REFERENCES admins(id)
) COMMENT '公告信息表';

-- 6. 学习资源表 (study_resources)
CREATE TABLE study_resources (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    title VARCHAR(200) NOT NULL COMMENT '资源标题',
    type ENUM('VIDEO') NOT NULL COMMENT '资源类型（只支持视频）',
    url VARCHAR(500) NOT NULL COMMENT 'OSS存储路径',
    cover_url VARCHAR(500) DEFAULT NULL COMMENT '封面图URL',
    created_by BIGINT NOT NULL COMMENT '上传人（指向admins.id）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (created_by) REFERENCES admins(id)
) COMMENT '学习资源表';

-- 7. 学习记录表 (study_records)
CREATE TABLE study_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID（指向members.id）',
    resource_id BIGINT NOT NULL COMMENT '资源ID',
    duration INT NOT NULL COMMENT '学习时长(分钟)',
    started_at DATETIME NOT NULL COMMENT '开始时间',
    ended_at DATETIME NOT NULL COMMENT '结束时间',
    FOREIGN KEY (user_id) REFERENCES members(id),
    FOREIGN KEY (resource_id) REFERENCES study_resources(id)
) COMMENT '学习记录表';

-- 8. 视频观看进度表 (video_watch_progress)
CREATE TABLE video_watch_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID（指向members.id）',
    resource_id BIGINT NOT NULL COMMENT '资源ID（指向study_resources.id）',
    `current_time` INT NOT NULL DEFAULT 0 COMMENT '当前观看时间(秒)',
    total_duration INT NOT NULL DEFAULT 0 COMMENT '视频总时长(秒)',
    watch_percentage DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '观看进度百分比',
    last_watch_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后观看时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 添加唯一索引：防止同一用户重复记录同一个视频
    UNIQUE KEY uk_user_resource (user_id, resource_id),

    -- 外键约束（确保数据一致性）
    CONSTRAINT fk_video_watch_user
        FOREIGN KEY (user_id) REFERENCES members(id) ON DELETE CASCADE,
    CONSTRAINT fk_video_watch_resource
        FOREIGN KEY (resource_id) REFERENCES study_resources(id) ON DELETE CASCADE
) COMMENT '视频观看进度表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. 积分明细表 (points)
CREATE TABLE points (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID（指向members.id）',
    branch_id BIGINT NOT NULL COMMENT '所属支部',
    points INT NOT NULL COMMENT '积分变化值',
    description VARCHAR(200) NOT NULL COMMENT '积分说明',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES members(id),
    FOREIGN KEY (branch_id) REFERENCES party_branches(id)
) COMMENT '积分明细表';

-- 10. 活动表 (activities)
CREATE TABLE activities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    title VARCHAR(200) NOT NULL COMMENT '活动标题',
    description TEXT NOT NULL COMMENT '活动描述',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    branch_id BIGINT NOT NULL COMMENT '所属支部',
    created_by BIGINT NOT NULL COMMENT '创建人（指向admins.id）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (branch_id) REFERENCES party_branches(id),
    FOREIGN KEY (created_by) REFERENCES admins(id)
) COMMENT '活动信息表';

-- 11. 活动参与记录表 (activity_participants)
CREATE TABLE activity_participants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    user_id BIGINT NOT NULL COMMENT '用户ID（指向members.id）',
    status ENUM('PARTICIPATED', 'NOT_PARTICIPATED') NOT NULL DEFAULT 'NOT_PARTICIPATED' COMMENT '参与状态',
    joined_at DATETIME COMMENT '参与时间',
    FOREIGN KEY (activity_id) REFERENCES activities(id),
    FOREIGN KEY (user_id) REFERENCES members(id)
) COMMENT '活动参与记录表';

-- 12. 任务表 (tasks)
CREATE TABLE tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    title VARCHAR(200) NOT NULL COMMENT '任务标题',
    description TEXT NOT NULL COMMENT '任务描述',
    due_date DATETIME NOT NULL COMMENT '截止时间',
    status ENUM('PUBLISHING','PENDING_END','ENDED') NOT NULL DEFAULT 'PUBLISHING' COMMENT '任务状态：发布中/待完结/已完结',
    points INT NOT NULL DEFAULT 0 COMMENT '完成该任务可获得的积分',
    capacity INT NULL COMMENT '活动人数上限',
    branch_id BIGINT NOT NULL COMMENT '所属支部',
    created_by BIGINT NOT NULL COMMENT '创建人（指向admins.id）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (branch_id) REFERENCES party_branches(id),
    FOREIGN KEY (created_by) REFERENCES admins(id)
) COMMENT '任务表';

-- 14. 任务分配表 (task_assignments)
CREATE TABLE task_assignments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    user_id BIGINT NOT NULL COMMENT '分配用户（指向members.id）',
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED') NOT NULL DEFAULT 'PENDING' COMMENT '任务状态',
    assigned_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
    completed_at DATETIME COMMENT '完成时间',
    UNIQUE KEY uk_task_user (task_id, user_id),
    FOREIGN KEY (task_id) REFERENCES tasks(id),
    FOREIGN KEY (user_id) REFERENCES members(id)
) COMMENT '任务分配表';

-- 15.1 任务评价表 (task_evaluations)
CREATE TABLE task_evaluations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    user_id BIGINT NOT NULL COMMENT '评价人（指向members.id）',
    rating INT NOT NULL DEFAULT 5 COMMENT '评分 1-5',
    comment TEXT NULL COMMENT '评价内容',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES members(id) ON DELETE CASCADE,
    CONSTRAINT chk_task_evaluations_rating CHECK (rating >= 1 AND rating <= 5)
) COMMENT '任务评价表';

-- 15. 审核流程主表 (application_process)
CREATE TABLE application_process (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID（指向members.id）',
    current_stage ENUM('POSITIVE_MEMBER', 'PARTY_APPLICATION', 'PREPARE_MEMBER') NOT NULL COMMENT '当前审核阶段',
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING' COMMENT '审核状态',
    reviewer_id BIGINT NOT NULL COMMENT '当前审核人（指向admins.id）',
    submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    reviewed_at DATETIME COMMENT '审核时间',
    FOREIGN KEY (user_id) REFERENCES members(id),
    FOREIGN KEY (reviewer_id) REFERENCES admins(id)
) COMMENT '审核流程主表';

-- 16. 积极分子审核表
CREATE TABLE positive_member_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    application_details TEXT NOT NULL,
    supporting_documents TEXT NULL COMMENT '相关证明材料（存JSON字符串）',
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    reviewer_id BIGINT NOT NULL,
    reviewed_at DATETIME,
    comments TEXT,
    FOREIGN KEY (user_id) REFERENCES members(id),
    FOREIGN KEY (reviewer_id) REFERENCES admins(id)
) COMMENT '积极分子审核表';

-- 17. 入党审核表
CREATE TABLE party_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    application_details TEXT NOT NULL,
    training_records TEXT NULL COMMENT '培训记录（存JSON字符串）',
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    reviewer_id BIGINT NOT NULL,
    reviewed_at DATETIME,
    comments TEXT,
    FOREIGN KEY (user_id) REFERENCES members(id),
    FOREIGN KEY (reviewer_id) REFERENCES admins(id)
) COMMENT '入党审核表';

-- 18. 预备党员审核表 (prepare_member_application)
CREATE TABLE prepare_member_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID（指向members.id）',
    evaluation_report TEXT NOT NULL COMMENT '考察报告',
    probation_period_start DATETIME NOT NULL COMMENT '预备期开始时间',
    probation_period_end DATETIME NOT NULL COMMENT '预备期结束时间',
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING' COMMENT '审核状态',
    reviewer_id BIGINT NOT NULL COMMENT '审核人（指向admins.id）',
    reviewed_at DATETIME COMMENT '审核时间',
    comments TEXT COMMENT '审核意见',
    FOREIGN KEY (user_id) REFERENCES members(id),
    FOREIGN KEY (reviewer_id) REFERENCES admins(id)
) COMMENT '预备党员审核表';

-- 19. 系统设置表 (system_settings)
CREATE TABLE system_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    setting_key VARCHAR(100) NOT NULL UNIQUE COMMENT '设置键',
    setting_value VARCHAR(500) NOT NULL COMMENT '设置值',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '系统设置表';

-- 20. 成员资料修改申请表 (member_update_requests)
CREATE TABLE member_update_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    member_id BIGINT NOT NULL COMMENT '成员ID（指向members.id）',
    fields_json LONGTEXT NOT NULL COMMENT '修改字段JSON',
    status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    reviewer_id BIGINT NULL COMMENT '审核人（指向admins.id）',
    remark VARCHAR(500) NULL COMMENT '审核意见',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    reviewed_at DATETIME NULL COMMENT '审核时间',
    FOREIGN KEY (member_id) REFERENCES members(id)
) COMMENT '成员资料修改申请表';

-- 插入测试数据
-- 先插入管理员（因为支部表需要引用管理员ID）
INSERT INTO `admins` (`username`, `real_name`, `password`, `mobile`, `admin_type`, `status`) VALUES
('branch_admin', '张书记', 'e10adc3949ba59abbe56e057f20f883e', '13800138001', 'BRANCH_ADMIN', 'ACTIVE'),
('super_admin', '系统管理员', 'e10adc3949ba59abbe56e057f20f883e', '13800138003', 'SYSTEM_ADMIN', 'ACTIVE');

-- 插入支部测试数据
INSERT INTO `party_branches` (`name`, `leader_id`) VALUES
('测试支部', 1),
('测试子支部', 1);

-- 更新管理员的支部ID
UPDATE `admins` SET `branch_id` = 1 WHERE `id` = 1;

-- 插入用户测试数据
INSERT INTO `members` (`student_id`, `id_number`, `password`, `username`, `mobile`, `branch_id`, `political_status`) VALUES
('1', '110101199001011234', 'e10adc3949ba59abbe56e057f20f883e', '测试党员', '13800138000', 1, '共青团员'),
('2', '110101199001011235', 'e10adc3949ba59abbe56e057f20f883e', '测试积极分子', '13800138002', 1, '群众');

-- 默认开启个人信息开放申请开关为关闭
INSERT INTO system_settings (setting_key, setting_value) VALUES ('OPEN_APPLICATION', 'false')
ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value);

-- 数据库迁移脚本：移除学习资源表中的文档和音频类型
-- 执行前请备份数据库

USE `smart_party_building`;

-- 1. 删除现有的非视频类型的学习资源记录
DELETE FROM study_resources WHERE type IN ('DOCUMENT', 'AUDIO');

-- 2. 修改表结构，将type字段的ENUM类型限制为只支持VIDEO
ALTER TABLE study_resources
MODIFY COLUMN type ENUM('VIDEO') NOT NULL COMMENT '资源类型（只支持视频）';

-- 3. 创建视频观看进度表（如果不存在）
CREATE TABLE IF NOT EXISTS video_watch_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID（指向members.id）',
    resource_id BIGINT NOT NULL COMMENT '资源ID',
    `current_time` INT NOT NULL DEFAULT 0 COMMENT '当前观看时间(秒)',
    total_duration INT NOT NULL DEFAULT 0 COMMENT '视频总时长(秒)',
    watch_percentage DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '观看进度百分比',
    last_watch_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后观看时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_resource (user_id, resource_id),
    FOREIGN KEY (user_id) REFERENCES members(id) ON DELETE CASCADE,
    FOREIGN KEY (resource_id) REFERENCES study_resources(id) ON DELETE CASCADE
) COMMENT '视频观看进度表';

-- 4. 验证修改结果
SELECT 'Migration completed successfully' as status;
SELECT COUNT(*) as total_video_resources FROM study_resources WHERE type = 'VIDEO';
SELECT COUNT(*) as total_other_resources FROM study_resources WHERE type != 'VIDEO';
SELECT COUNT(*) as video_progress_table_exists FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_name = 'video_watch_progress';

-- 重新创建视频进度表，确保字段类型正确
-- 重新创建视频进度表，确保字段类型正确
USE smart_party_building;

-- 删除现有表
DROP TABLE IF EXISTS video_watch_progress;

-- 重新创建表，确保 current_time 是 INT 类型
CREATE TABLE video_watch_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID（指向members.id）',
    resource_id BIGINT NOT NULL COMMENT '资源ID',
    `current_time` INT NOT NULL DEFAULT 0 COMMENT '当前观看时间(秒)',
    total_duration INT NOT NULL DEFAULT 0 COMMENT '视频总时长(秒)',
    watch_percentage DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '观看进度百分比',
    last_watch_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后观看时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_resource (user_id, resource_id),
    FOREIGN KEY (user_id) REFERENCES members(id) ON DELETE CASCADE,
    FOREIGN KEY (resource_id) REFERENCES study_resources(id) ON DELETE CASCADE
) COMMENT '视频观看进度表';


-- 验证表结构
DESCRIBE video_watch_progress;

-- 验证数据
SELECT id, user_id, resource_id, `current_time`, total_duration, watch_percentage, last_watch_time
FROM video_watch_progress WHERE user_id = 1 AND resource_id = 7;

-- 兼容低版本MySQL：若不存在再新增列
SET @cap_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tasks' AND COLUMN_NAME = 'capacity'
);
SET @ddl_cap := IF(@cap_exists = 0,
  'ALTER TABLE tasks ADD COLUMN capacity INT NULL COMMENT ''活动人数上限'';',
  'SELECT 1'
);
PREPARE stmt_cap FROM @ddl_cap;
EXECUTE stmt_cap;
DEALLOCATE PREPARE stmt_cap;
