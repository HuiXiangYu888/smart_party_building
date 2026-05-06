-- 新闻表创建脚本
-- 在现有数据库基础上添加新闻管理功能

USE `smart_party_building`;

-- 创建新闻表 (news)
CREATE TABLE news (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    title VARCHAR(200) NOT NULL COMMENT '新闻标题',
    content TEXT NOT NULL COMMENT '新闻内容',
    cover_image VARCHAR(500) DEFAULT NULL COMMENT '封面图片URL',
    branch_id BIGINT NOT NULL COMMENT '所属支部',
    created_by BIGINT NOT NULL COMMENT '创建人（指向admins.id）',
    published_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (branch_id) REFERENCES party_branches(id),
    FOREIGN KEY (created_by) REFERENCES admins(id)
) COMMENT '新闻信息表';

-- 创建索引
CREATE INDEX idx_news_published_at ON news(published_at);
CREATE INDEX idx_news_branch_id ON news(branch_id);

-- 如果表已存在且有 view_count 字段，删除该字段
ALTER TABLE news DROP COLUMN IF EXISTS view_count;

-- 插入测试数据
INSERT INTO news (title, content, branch_id, created_by, published_at) VALUES
('深入学习贯彻党的二十大精神', '<p>党的二十大是在全党全国各族人民迈上全面建设社会主义现代化国家新征程、向第二个百年奋斗目标进军的关键时刻召开的一次十分重要的大会。</p><p>大会高举中国特色社会主义伟大旗帜，全面贯彻习近平新时代中国特色社会主义思想，分析了国际国内形势，提出了党的二十大主题，回顾总结了过去5年的工作和新时代10年的伟大变革。</p>', 1, 1, NOW()),
('加强基层党组织建设', '<p>基层党组织是党在社会基层组织中的战斗堡垒，是党的全部工作和战斗力的基础。</p><p>新形势下基层党组织工作开展得怎么样，直接影响到党的凝聚力、影响力、战斗力的充分发挥。我们要以提升组织力为重点，突出政治功能，把企业、农村、机关、学校、科研院所、街道社区、社会组织等基层党组织建设成为宣传党的主张、贯彻党的决定、领导基层治理、团结动员群众、推动改革发展的坚强战斗堡垒。</p>', 1, 1, NOW()),
('推进全面从严治党向纵深发展', '<p>全面从严治党是党的十八大以来党中央作出的重大战略部署，是"四个全面"战略布局的重要组成部分。</p><p>全面从严治党，基础在全面，关键在严，要害在治。我们要坚持思想建党和制度治党同向发力，统筹推进党的各项建设，抓住"关键少数"，坚持"三严三实"，坚持民主集中制，严肃党内政治生活，严明党的纪律，强化党内监督。</p>', 1, 1, NOW());

-- 验证数据
SELECT COUNT(*) as total_news FROM news;
SELECT * FROM news ORDER BY published_at DESC;
