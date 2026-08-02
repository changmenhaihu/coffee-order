-- ============================================
-- Coffee Order 数据库迁移脚本 v2
-- 用于已有 v1 结构的数据库升级，兼容 MySQL 8.0+
-- 支持重复执行（幂等）
-- ============================================

USE `coffee_order`;

-- 1. store 表新增字段（若不存在则添加）
ALTER TABLE `store`
  ADD COLUMN IF NOT EXISTS `open_time` TIME DEFAULT NULL COMMENT '开门时间' AFTER `business_hours`,
  ADD COLUMN IF NOT EXISTS `close_time` TIME DEFAULT NULL COMMENT '关门时间' AFTER `open_time`;

-- 2. sys_user 表新增 gender 字段
ALTER TABLE `sys_user`
  ADD COLUMN IF NOT EXISTS `gender` TINYINT(1) DEFAULT '0' COMMENT '性别：0-未知 1-男 2-女' AFTER `email`;

-- 3. 创建用户地址表（若不存在）
CREATE TABLE IF NOT EXISTS `user_address` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `phone` VARCHAR(20) NOT NULL COMMENT '收货人手机号',
  `province` VARCHAR(50) DEFAULT NULL COMMENT '省',
  `city` VARCHAR(50) DEFAULT NULL COMMENT '市',
  `district` VARCHAR(50) DEFAULT NULL COMMENT '区',
  `detail` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `latitude` DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
  `longitude` DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
  `is_default` TINYINT(1) DEFAULT '0' COMMENT '是否默认地址：1-是 0-否',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_address_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收货地址表';

-- 4. 创建反馈表（若不存在）
CREATE TABLE IF NOT EXISTS `feedback` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `content` TEXT NOT NULL COMMENT '反馈内容',
  `images` VARCHAR(1000) DEFAULT NULL COMMENT '图片URL，多个逗号分隔',
  `contact` VARCHAR(100) DEFAULT NULL COMMENT '联系方式',
  `status` TINYINT DEFAULT '0' COMMENT '状态：0-未处理 1-已处理',
  `reply` TEXT DEFAULT NULL COMMENT '管理员回复',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_feedback_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户反馈表';

-- 5. 更新已有门店的营业时间（仅当 open_time 为空时，避免覆盖已有数据）
UPDATE `store` 
SET `open_time` = '07:00:00', `close_time` = '22:00:00' 
WHERE `name` LIKE '%星巴克科技园%' AND `open_time` IS NULL;

UPDATE `store` 
SET `open_time` = '06:30:00', `close_time` = '22:30:00' 
WHERE `name` LIKE '%瑞幸软件园%' AND `open_time` IS NULL;

-- 6. 新增门店（避免重复插入，以名称+地址作为唯一性判断）
INSERT INTO `store` (`name`, `address`, `phone`, `lng`, `lat`, `cover`, `business_hours`, `open_time`, `close_time`, `min_delivery_amount`, `delivery_fee`, `status`)
SELECT '瑞幸咖啡（科兴科学园店）', '深圳市南山区科兴科学园C1栋1楼', '0755-86303333', 113.9470, 22.5510, 
       'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=400', '07:00-21:30', '07:00:00', '21:30:00', 15.00, 2.50, 1
WHERE NOT EXISTS (
  SELECT 1 FROM `store` 
  WHERE `name` = '瑞幸咖啡（科兴科学园店）' AND `address` = '深圳市南山区科兴科学园C1栋1楼'
);

INSERT INTO `store` (`name`, `address`, `phone`, `lng`, `lat`, `cover`, `business_hours`, `open_time`, `close_time`, `min_delivery_amount`, `delivery_fee`, `status`)
SELECT '库迪咖啡（深圳大学店）', '深圳市南山区南海大道3688号深圳大学西门', '0755-86404444', 113.9385, 22.5380,
       'https://images.unsplash.com/photo-1461023058943-07fcbe16d735?w=400', '07:30-21:00', '07:30:00', '21:00:00', 12.00, 2.00, 1
WHERE NOT EXISTS (
  SELECT 1 FROM `store` 
  WHERE `name` = '库迪咖啡（深圳大学店）' AND `address` = '深圳市南山区南海大道3688号深圳大学西门'
);

INSERT INTO `store` (`name`, `address`, `phone`, `lng`, `lat`, `cover`, `business_hours`, `open_time`, `close_time`, `min_delivery_amount`, `delivery_fee`, `status`)
SELECT '星巴克（万象天地店）', '深圳市南山区深南大道9668号万象天地B1层', '0755-86505555', 113.9518, 22.5375,
       'https://images.unsplash.com/photo-1445116572660-236099ec97a0?w=400', '08:00-22:30', '08:00:00', '22:30:00', 25.00, 3.00, 1
WHERE NOT EXISTS (
  SELECT 1 FROM `store` 
  WHERE `name` = '星巴克（万象天地店）' AND `address` = '深圳市南山区深南大道9668号万象天地B1层'
);

INSERT INTO `store` (`name`, `address`, `phone`, `lng`, `lat`, `cover`, `business_hours`, `open_time`, `close_time`, `min_delivery_amount`, `delivery_fee`, `status`)
SELECT 'Manner Coffee（高新园店）', '深圳市南山区高新南一道中国科技开发院1楼', '0755-82605555', 113.9490, 22.5440,
       'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400', '07:00-20:00', '07:00:00', '20:00:00', 20.00, 2.00, 1
WHERE NOT EXISTS (
  SELECT 1 FROM `store` 
  WHERE `name` = 'Manner Coffee（高新园店）' AND `address` = '深圳市南山区高新南一道中国科技开发院1楼'
);