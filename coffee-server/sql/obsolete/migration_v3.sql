-- ============================================
-- Coffee Order 数据库初始化与修复脚本 v3.0
-- 用途：一键初始化/修复数据库，确保核心流程可走通
-- 使用：mysql -u root -p < migration_v3.sql
-- ============================================

CREATE DATABASE IF NOT EXISTS `coffee_order`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `coffee_order`;

-- ============================================
-- 一、用户表
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `password` VARCHAR(100) NOT NULL,
  `nickname` VARCHAR(50) DEFAULT NULL,
  `avatar` VARCHAR(255) DEFAULT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `email` VARCHAR(100) DEFAULT NULL,
  `gender` TINYINT(1) DEFAULT '0',
  `balance` DECIMAL(10,2) DEFAULT '0.00',
  `role` ENUM('admin','user','rider') NOT NULL DEFAULT 'user',
  `status` TINYINT NOT NULL DEFAULT '1',
  `last_login_time` DATETIME DEFAULT NULL,
  `last_login_ip` VARCHAR(50) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 二、门店表（核心）
-- ============================================
CREATE TABLE IF NOT EXISTS `store` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `address` VARCHAR(255) NOT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `lng` DECIMAL(10,7) NOT NULL,
  `lat` DECIMAL(10,7) NOT NULL,
  `cover` VARCHAR(255) DEFAULT NULL,
  `business_hours` VARCHAR(50) DEFAULT NULL,
  `open_time` TIME DEFAULT NULL,
  `close_time` TIME DEFAULT NULL,
  `min_delivery_amount` DECIMAL(10,2) DEFAULT '0.00',
  `delivery_fee` DECIMAL(10,2) DEFAULT '0.00',
  `status` TINYINT NOT NULL DEFAULT '1',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_location` (`lng`, `lat`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 三、商品分类表
-- ============================================
CREATE TABLE IF NOT EXISTS `product_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `store_id` BIGINT NOT NULL,
  `name` VARCHAR(50) NOT NULL,
  `sort_order` INT NOT NULL DEFAULT '0',
  `status` TINYINT NOT NULL DEFAULT '1',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_store_id` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 四、商品表
-- ============================================
CREATE TABLE IF NOT EXISTS `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `store_id` BIGINT NOT NULL,
  `category_id` BIGINT NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `description` VARCHAR(500) DEFAULT NULL,
  `cover` VARCHAR(255) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT '1',
  `sales` INT NOT NULL DEFAULT '0',
  `rating` DECIMAL(2,1) DEFAULT '5.0',
  `rating_count` INT NOT NULL DEFAULT '0',
  `sort_order` INT NOT NULL DEFAULT '0',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status_sales` (`status`, `sales`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 五、商品规格表
-- ============================================
CREATE TABLE IF NOT EXISTS `product_spec` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `product_id` BIGINT NOT NULL,
  `spec_name` VARCHAR(50) NOT NULL,
  `price` DECIMAL(10,2) NOT NULL,
  `stock` INT NOT NULL DEFAULT '0',
  `status` TINYINT NOT NULL DEFAULT '1',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 六、购物车表
-- ============================================
CREATE TABLE IF NOT EXISTS `cart` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `store_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `spec_id` BIGINT NOT NULL,
  `quantity` INT NOT NULL DEFAULT '1',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_store_product_spec` (`user_id`, `store_id`, `product_id`, `spec_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 七、订单表
-- ============================================
CREATE TABLE IF NOT EXISTS `orders` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(32) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `store_id` BIGINT NOT NULL,
  `rider_id` BIGINT DEFAULT NULL,
  `type` ENUM('takeaway','delivery') NOT NULL,
  `status` TINYINT NOT NULL DEFAULT '0',
  `total_amount` DECIMAL(10,2) NOT NULL,
  `pay_amount` DECIMAL(10,2) NOT NULL,
  `delivery_fee` DECIMAL(10,2) DEFAULT '0.00',
  `discount_amount` DECIMAL(10,2) DEFAULT '0.00',
  `address` JSON DEFAULT NULL,
  `remark` VARCHAR(255) DEFAULT NULL,
  `pay_time` DATETIME DEFAULT NULL,
  `accept_time` DATETIME DEFAULT NULL,
  `pickup_time` DATETIME DEFAULT NULL,
  `deliver_time` DATETIME DEFAULT NULL,
  `complete_time` DATETIME DEFAULT NULL,
  `cancel_time` DATETIME DEFAULT NULL,
  `cancel_reason` VARCHAR(255) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 八、订单明细表
-- ============================================
CREATE TABLE IF NOT EXISTS `order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `spec_id` BIGINT NOT NULL,
  `product_name` VARCHAR(100) NOT NULL,
  `spec_name` VARCHAR(50) NOT NULL,
  `price` DECIMAL(10,2) NOT NULL,
  `quantity` INT NOT NULL,
  `total` DECIMAL(10,2) NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 九、订单状态日志表
-- ============================================
CREATE TABLE IF NOT EXISTS `order_status_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `from_status` TINYINT DEFAULT NULL,
  `to_status` TINYINT NOT NULL,
  `operator_id` BIGINT DEFAULT NULL,
  `operator_type` TINYINT NOT NULL DEFAULT '0',
  `remark` VARCHAR(255) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 十、用户行为表（推荐系统）
-- ============================================
CREATE TABLE IF NOT EXISTS `user_behavior` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `store_id` BIGINT NOT NULL,
  `type` VARCHAR(20) NOT NULL COMMENT '行为类型: view/cart/order/favorite',
  `count` INT NOT NULL DEFAULT '1',
  `score` DECIMAL(3,1) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product_type` (`user_id`, `product_id`, `type`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 十一、清空旧数据（仅清空业务数据，保留表结构）
-- ============================================
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `order_status_log`;
TRUNCATE TABLE `order_item`;
TRUNCATE TABLE `orders`;
TRUNCATE TABLE `cart`;
TRUNCATE TABLE `user_behavior`;
TRUNCATE TABLE `product_spec`;
TRUNCATE TABLE `product`;
TRUNCATE TABLE `product_category`;
TRUNCATE TABLE `store`;
TRUNCATE TABLE `sys_user`;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 十二、插入测试用户
-- ============================================
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `phone`, `email`, `balance`, `role`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', '13800000000', 'admin@coffee.com', 0.00, 'admin', 1),
('rider', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '骑手小王', '13800138001', 'rider@coffee.com', 0.00, 'rider', 1),
('user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户', '13800138002', 'user@coffee.com', 10000.00, 'user', 1);

-- ============================================
-- 十三、插入门店（均位于 22.5483, 113.9434 附近 5km 内）
-- ============================================
INSERT INTO `store` (`id`, `name`, `address`, `phone`, `lng`, `lat`, `cover`, `business_hours`, `open_time`, `close_time`, `min_delivery_amount`, `delivery_fee`, `status`) VALUES
(1, '星巴克咖啡（科技园店）', '深圳市南山区科技园南区A栋1楼', '0755-86101111', 113.9433, 22.5482, 'https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=400', '07:00-22:00', '07:00:00', '22:00:00', 25.00, 3.00, 1),
(2, '瑞幸咖啡（软件园店）', '深圳市南山区软件园B栋大堂', '0755-86202222', 113.9520, 22.5420, 'https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=400', '06:30-22:30', '06:30:00', '22:30:00', 15.00, 2.00, 1),
(3, '瑞幸咖啡（科兴科学园店）', '深圳市南山区科兴科学园C1栋1楼', '0755-86303333', 113.9470, 22.5510, 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=400', '07:00-21:30', '07:00:00', '21:30:00', 15.00, 2.50, 1),
(4, '库迪咖啡（深圳大学店）', '深圳市南山区南海大道3688号深圳大学西门', '0755-86404444', 113.9385, 22.5380, 'https://images.unsplash.com/photo-1461023058943-07fcbe16d735?w=400', '07:30-21:00', '07:30:00', '21:00:00', 12.00, 2.00, 1),
(5, '星巴克（万象天地店）', '深圳市南山区深南大道9668号万象天地B1层', '0755-86505555', 113.9518, 22.5375, 'https://images.unsplash.com/photo-1445116572660-236099ec97a0?w=400', '08:00-22:30', '08:00:00', '22:30:00', 25.00, 3.00, 1),
(6, 'Manner Coffee（高新园店）', '深圳市南山区高新南一道中国科技开发院1楼', '0755-82605555', 113.9490, 22.5440, 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400', '07:00-20:00', '07:00:00', '20:00:00', 20.00, 2.00, 1);

-- ============================================
-- 十四、插入商品分类
-- ============================================
INSERT INTO `product_category` (`id`, `store_id`, `name`, `sort_order`, `status`) VALUES
-- 星巴克科技园 (store=1) - 5个分类
(1, 1, '经典咖啡', 1, 1), (2, 1, '冷萃冰咖啡', 2, 1), (3, 1, '星冰乐', 3, 1), (4, 1, '茶饮系列', 4, 1), (5, 1, '轻食甜品', 5, 1),
-- 瑞幸软件园 (store=2) - 5个分类
(6, 2, '大师咖啡', 1, 1), (7, 2, '生椰系列', 2, 1), (8, 2, '小鹿茶', 3, 1), (9, 2, '瑞纳冰', 4, 1), (10, 2, '轻食', 5, 1),
-- 瑞幸科兴 (store=3) - 4个分类
(11, 3, '大师咖啡', 1, 1), (12, 3, '生椰系列', 2, 1), (13, 3, '小鹿茶', 3, 1), (14, 3, '轻食', 4, 1),
-- 库迪深大 (store=4) - 4个分类
(15, 4, '经典咖啡', 1, 1), (16, 4, '生椰系列', 2, 1), (17, 4, '茶饮', 3, 1), (18, 4, '轻食', 4, 1),
-- 星巴克万象天地 (store=5) - 4个分类
(19, 5, '经典咖啡', 1, 1), (20, 5, '冷萃系列', 2, 1), (21, 5, '星冰乐', 3, 1), (22, 5, '轻食甜品', 4, 1),
-- Manner高新园 (store=6) - 4个分类
(23, 6, '意式咖啡', 1, 1), (24, 6, '手冲系列', 2, 1), (25, 6, '特色饮品', 3, 1), (26, 6, '轻食', 4, 1);

-- ============================================
-- 十五、插入商品（每家门店 10+ 商品）
-- ============================================
INSERT INTO `product` (`id`, `store_id`, `category_id`, `name`, `description`, `cover`, `status`, `sales`, `rating`, `rating_count`, `sort_order`) VALUES
-- ===== 星巴克科技园 (store=1) - 12个商品 =====
(1, 1, 1, '拿铁', '浓缩咖啡与丝滑牛乳的经典融合', 'https://images.unsplash.com/photo-1570968915860-54d5c301fa9f?w=400', 1, 3280, 4.8, 856, 1),
(2, 1, 1, '美式咖啡', '纯粹黑咖啡，清爽提神零负担', 'https://images.unsplash.com/photo-1559496417-e7f25cb247f3?w=400', 1, 2960, 4.6, 680, 2),
(3, 1, 1, '卡布奇诺', '意式浓缩加厚奶泡，口感绵密', 'https://images.unsplash.com/photo-1572442386806-1050eaef1c17?w=400', 1, 1780, 4.7, 442, 3),
(4, 1, 1, '焦糖玛奇朵', '香醇焦糖邂逅浓缩咖啡的甜蜜滋味', 'https://images.unsplash.com/photo-1523948855788-f0502cfa49l?w=400', 1, 2120, 4.8, 523, 4),
(5, 1, 2, '冷萃冰咖啡', '14小时冷水慢萃，口感顺滑醇厚', 'https://images.unsplash.com/photo-1517701550927-30cf4ba1dba5?w=400', 1, 1520, 4.9, 398, 1),
(6, 1, 2, '绵云冷萃', '冷萃咖啡搭配轻盈奶盖，层次分明', 'https://images.unsplash.com/photo-1461023058943-07fcbe16d735?w=400', 1, 980, 4.7, 267, 2),
(7, 1, 3, '摩卡星冰乐', '巧克力与咖啡的冰爽碰撞', 'https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=400', 1, 2340, 4.8, 612, 1),
(8, 1, 3, '抹茶星冰乐', '日式抹茶与奶油的清新搭配', 'https://images.unsplash.com/photo-1536256263959-770b48d82b0a?w=400', 1, 1890, 4.6, 478, 2),
(9, 1, 4, '抹茶拿铁', '日式抹茶与牛乳的温柔融合', 'https://images.unsplash.com/photo-1536508817515-0e85b42b2a25?w=400', 1, 1340, 4.5, 367, 1),
(10, 1, 4, '红茶拿铁', '英式红茶邂逅醇香牛乳', 'https://images.unsplash.com/photo-1558857563-b371033873b8?w=400', 1, 890, 4.4, 245, 2),
(11, 1, 5, '提拉米苏', '经典意式提拉米苏，入口即化', 'https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?w=400', 1, 760, 4.7, 189, 1),
(12, 1, 5, '法式牛角包', '酥脆多层，黄油香气浓郁', 'https://images.unsplash.com/photo-1555507036-ab1f4038024a?w=400', 1, 1020, 4.5, 234, 2),

-- ===== 瑞幸软件园 (store=2) - 12个商品 =====
(13, 2, 6, '生椰拿铁', '椰乳与浓缩咖啡的完美搭配，年销3亿杯', 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400', 1, 5100, 4.9, 1420, 1),
(14, 2, 6, '厚乳拿铁', '双倍牛乳，口感醇厚丝滑', 'https://images.unsplash.com/photo-1570968915860-54d5c301fa9f?w=400', 1, 3800, 4.8, 980, 2),
(15, 2, 6, '丝绒拿铁', '超滤牛乳打造丝绒般顺滑口感', 'https://images.unsplash.com/photo-1485808191679-5f86510681a2?w=400', 1, 1600, 4.7, 456, 3),
(16, 2, 6, '标准美式', '经典美式，纯粹咖啡体验', 'https://images.unsplash.com/photo-1559496417-e7f25cb247f3?w=400', 1, 4200, 4.6, 1120, 4),
(17, 2, 7, '生椰丝绒拿铁', '丝绒拿铁与生椰的甜蜜碰撞', 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=400', 1, 2100, 4.8, 620, 1),
(18, 2, 7, '生椰爱摩卡', '生椰+摩卡，浓郁巧克力椰香', 'https://images.unsplash.com/photo-1514432324607-a09d9b4aefda?w=400', 1, 1500, 4.7, 389, 2),
(19, 2, 7, '椰青冰萃', '清爽椰青遇上冷萃咖啡', 'https://images.unsplash.com/photo-1517701550927-30cf4ba1dba5?w=400', 1, 890, 4.5, 234, 3),
(20, 2, 8, '茉莉花香拿铁', '茉莉花茶与咖啡的东方韵味', 'https://images.unsplash.com/photo-1558857563-b371033873b8?w=400', 1, 3200, 4.8, 876, 1),
(21, 2, 8, '葡萄冰萃', '整颗葡萄搭配冰萃咖啡', 'https://images.unsplash.com/photo-1536508817515-0e85b42b2a25?w=400', 1, 1650, 4.6, 420, 2),
(22, 2, 9, '抹茶瑞纳冰', '抹茶冰沙搭配奶油顶', 'https://images.unsplash.com/photo-1536256263959-770b48d82b0a?w=400', 1, 1100, 4.5, 312, 1),
(23, 2, 9, '可可瑞纳冰', '浓郁巧克力冰沙，夏日必备', 'https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=400', 1, 980, 4.6, 278, 2),
(24, 2, 10, '火腿芝士可颂', '酥脆可颂搭配火腿芝士', 'https://images.unsplash.com/photo-1555507036-ab1f4038024a?w=400', 1, 540, 4.4, 156, 1),

-- ===== 瑞幸科兴 (store=3) - 11个商品 =====
(25, 3, 11, '生椰拿铁', '椰乳与浓缩咖啡的完美搭配', 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400', 1, 2300, 4.9, 678, 1),
(26, 3, 11, '厚乳拿铁', '双倍牛乳，口感醇厚丝滑', 'https://images.unsplash.com/photo-1570968915860-54d5c301fa9f?w=400', 1, 1800, 4.8, 512, 2),
(27, 3, 11, '标准美式', '经典美式，纯粹咖啡体验', 'https://images.unsplash.com/photo-1559496417-e7f25cb247f3?w=400', 1, 2100, 4.6, 589, 3),
(28, 3, 11, '澳白', '精萃浓缩与丝滑奶泡的黄金比例', 'https://images.unsplash.com/photo-1485808191679-5f86510681a2?w=400', 1, 1200, 4.7, 345, 4),
(29, 3, 12, '生椰丝绒拿铁', '丝绒拿铁与生椰的甜蜜碰撞', 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=400', 1, 980, 4.8, 289, 1),
(30, 3, 12, '生椰爱摩卡', '生椰+摩卡，浓郁巧克力椰香', 'https://images.unsplash.com/photo-1514432324607-a09d9b4aefda?w=400', 1, 720, 4.7, 198, 2),
(31, 3, 12, '椰青冷萃', '清爽椰青遇上冷萃咖啡', 'https://images.unsplash.com/photo-1517701550927-30cf4ba1dba5?w=400', 1, 450, 4.5, 134, 3),
(32, 3, 13, '茉莉花香拿铁', '茉莉花茶与咖啡的东方韵味', 'https://images.unsplash.com/photo-1558857563-b371033873b8?w=400', 1, 1600, 4.8, 456, 1),
(33, 3, 13, '山茶花拿铁', '山茶花清香融入醇美咖啡', 'https://images.unsplash.com/photo-1536508817515-0e85b42b2a25?w=400', 1, 1100, 4.6, 312, 2),
(34, 3, 14, '火腿芝士可颂', '酥脆可颂搭配火腿芝士', 'https://images.unsplash.com/photo-1555507036-ab1f4038024a?w=400', 1, 320, 4.4, 98, 1),
(35, 3, 14, '巧克力麦芬', '浓郁巧克力，松软可口', 'https://images.unsplash.com/photo-1558401391-7899b40bd42c?w=400', 1, 410, 4.3, 123, 2),

-- ===== 库迪深大 (store=4) - 10个商品 =====
(36, 4, 15, '经典拿铁', '浓缩咖啡与牛乳的黄金比例', 'https://images.unsplash.com/photo-1570968915860-54d5c301fa9f?w=400', 1, 1200, 4.5, 345, 1),
(37, 4, 15, '美式咖啡', '浓郁黑咖，唤醒每一天', 'https://images.unsplash.com/photo-1559496417-e7f25cb247f3?w=400', 1, 980, 4.4, 278, 2),
(38, 4, 15, '卡布奇诺', '经典意式奶咖，绵密细腻', 'https://images.unsplash.com/photo-1572442386806-1050eaef1c17?w=400', 1, 670, 4.3, 189, 3),
(39, 4, 16, '生椰拿铁', '椰子与咖啡的热带碰撞', 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400', 1, 1800, 4.7, 512, 1),
(40, 4, 16, '生椰Dirty', '冰博克与椰乳层层交织', 'https://images.unsplash.com/photo-1514432324607-a09d9b4aefda?w=400', 1, 890, 4.6, 234, 2),
(41, 4, 17, '抹茶拿铁', '日式抹茶搭配丝滑牛乳', 'https://images.unsplash.com/photo-1536256263959-770b48d82b0a?w=400', 1, 560, 4.5, 167, 1),
(42, 4, 17, '茉莉绿茶', '清新茉莉花绿茶', 'https://images.unsplash.com/photo-1558857563-b371033873b8?w=400', 1, 430, 4.4, 123, 2),
(43, 4, 17, '柠檬茶', '鲜切柠檬搭配红茶底', 'https://images.unsplash.com/photo-1536508817515-0e85b42b2a25?w=400', 1, 780, 4.6, 210, 3),
(44, 4, 18, '巧克力曲奇', '酥脆曲奇搭配巧克力豆', 'https://images.unsplash.com/photo-1558401391-7899b40bd42c?w=400', 1, 320, 4.3, 89, 1),
(45, 4, 18, '原味华夫饼', '现烤华夫饼，外酥里嫩', 'https://images.unsplash.com/photo-1555507036-ab1f4038024a?w=400', 1, 290, 4.2, 76, 2),

-- ===== 星巴克万象天地 (store=5) - 10个商品 =====
(46, 5, 19, '拿铁', '经典浓缩与牛乳的平衡艺术', 'https://images.unsplash.com/photo-1570968915860-54d5c301fa9f?w=400', 1, 1800, 4.7, 512, 1),
(47, 5, 19, '美式咖啡', '清爽纯粹的经典黑咖啡', 'https://images.unsplash.com/photo-1559496417-e7f25cb247f3?w=400', 1, 1500, 4.5, 423, 2),
(48, 5, 19, '卡布奇诺', '绵密奶泡与浓缩的完美组合', 'https://images.unsplash.com/photo-1572442386806-1050eaef1c17?w=400', 1, 1100, 4.6, 312, 3),
(49, 5, 19, '澳白', '精萃浓缩的极致体验', 'https://images.unsplash.com/photo-1485808191679-5f86510681a2?w=400', 1, 890, 4.7, 245, 4),
(50, 5, 20, '冷萃冰咖啡', '14小时慢萃，顺滑回甘', 'https://images.unsplash.com/photo-1517701550927-30cf4ba1dba5?w=400', 1, 980, 4.8, 289, 1),
(51, 5, 20, '冷萃浮云', '冷萃配轻盈奶泡，温柔清爽', 'https://images.unsplash.com/photo-1461023058943-07fcbe16d735?w=400', 1, 670, 4.6, 178, 2),
(52, 5, 21, '摩卡星冰乐', '巧克力与咖啡的冰爽体验', 'https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=400', 1, 1450, 4.7, 378, 1),
(53, 5, 21, '香草星冰乐', '香草风味冰沙，清新甜蜜', 'https://images.unsplash.com/photo-1536256263959-770b48d82b0a?w=400', 1, 1230, 4.6, 334, 2),
(54, 5, 22, '纽约芝士蛋糕', '浓郁芝士，丝滑绵密', 'https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?w=400', 1, 560, 4.6, 145, 1),
(55, 5, 22, '蓝莓麦芬', '新鲜蓝莓搭配松软蛋糕', 'https://images.unsplash.com/photo-1558401391-7899b40bd42c?w=400', 1, 430, 4.4, 112, 2),

-- ===== Manner高新园 (store=6) - 11个商品 =====
(56, 6, 23, '拿铁', '自烘拼配豆的精萃呈现', 'https://images.unsplash.com/photo-1570968915860-54d5c301fa9f?w=400', 1, 890, 4.8, 234, 1),
(57, 6, 23, '美式', '浅烘单品豆，果酸明亮', 'https://images.unsplash.com/photo-1559496417-e7f25cb247f3?w=400', 1, 670, 4.6, 178, 2),
(58, 6, 23, 'Dirty', '冰博克与热浓缩的碰撞', 'https://images.unsplash.com/photo-1514432324607-a09d9b4aefda?w=400', 1, 540, 4.7, 145, 3),
(59, 6, 23, '短笛拿铁', '小杯高浓，浓缩爱好者首选', 'https://images.unsplash.com/photo-1485808191679-5f86510681a2?w=400', 1, 380, 4.5, 98, 4),
(60, 6, 24, '埃塞耶加雪菲', '柑橘花香，明亮酸质', 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=400', 1, 320, 4.9, 89, 1),
(61, 6, 24, '哥伦比亚慧兰', '坚果可可调性，平衡顺滑', 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400', 1, 280, 4.7, 76, 2),
(62, 6, 25, '桂花拿铁', '桂花清香与浓缩的美妙结合', 'https://images.unsplash.com/photo-1558857563-b371033873b8?w=400', 1, 520, 4.6, 134, 1),
(63, 6, 25, '燕麦拿铁', 'OATLY燕麦奶搭配浓缩', 'https://images.unsplash.com/photo-1536508817515-0e85b42b2a25?w=400', 1, 610, 4.7, 167, 2),
(64, 6, 25, '抹茶拿铁', '日式抹茶与燕麦奶的清新', 'https://images.unsplash.com/photo-1536256263959-770b48d82b0a?w=400', 1, 450, 4.5, 123, 3),
(65, 6, 26, '海盐卷', '日式海盐面包，外脆里软', 'https://images.unsplash.com/photo-1555507036-ab1f4038024a?w=400', 1, 210, 4.4, 67, 1);

-- ============================================
-- 十六、插入商品规格
-- ============================================
INSERT INTO `product_spec` (`product_id`, `spec_name`, `price`, `stock`, `status`) VALUES
-- 星巴克科技园 (products 1-12)
(1, '中杯', 30.00, 100, 1), (1, '大杯', 35.00, 80, 1), (1, '超大杯', 39.00, 60, 1),
(2, '中杯', 26.00, 120, 1), (2, '大杯', 30.00, 90, 1), (2, '超大杯', 34.00, 70, 1),
(3, '中杯', 32.00, 60, 1), (3, '大杯', 37.00, 50, 1), (3, '超大杯', 41.00, 30, 1),
(4, '中杯', 34.00, 80, 1), (4, '大杯', 39.00, 60, 1), (4, '超大杯', 43.00, 40, 1),
(5, '中杯', 33.00, 50, 1), (5, '大杯', 38.00, 30, 1),
(6, '中杯', 35.00, 40, 1), (6, '大杯', 40.00, 30, 1),
(7, '中杯', 36.00, 80, 1), (7, '大杯', 41.00, 60, 1), (7, '超大杯', 45.00, 40, 1),
(8, '中杯', 36.00, 70, 1), (8, '大杯', 41.00, 50, 1), (8, '超大杯', 45.00, 30, 1),
(9, '中杯', 32.00, 50, 1), (9, '大杯', 37.00, 40, 1),
(10, '中杯', 30.00, 50, 1), (10, '大杯', 35.00, 40, 1),
(11, '单品', 28.00, 30, 1),
(12, '单品', 18.00, 40, 1),

-- 瑞幸软件园 (products 13-24)
(13, '中杯', 19.90, 200, 1), (13, '大杯', 23.90, 150, 1),
(14, '中杯', 22.00, 180, 1), (14, '大杯', 26.00, 130, 1),
(15, '中杯', 21.00, 160, 1), (15, '大杯', 25.00, 120, 1),
(16, '中杯', 16.00, 200, 1), (16, '大杯', 20.00, 160, 1),
(17, '中杯', 23.00, 150, 1), (17, '大杯', 27.00, 110, 1),
(18, '中杯', 22.00, 140, 1), (18, '大杯', 26.00, 100, 1),
(19, '大杯', 25.00, 100, 1),
(20, '中杯', 20.00, 170, 1), (20, '大杯', 24.00, 130, 1),
(21, '大杯', 25.00, 90, 1),
(22, '中杯', 23.00, 100, 1), (22, '大杯', 27.00, 70, 1),
(23, '中杯', 24.00, 80, 1), (23, '大杯', 28.00, 60, 1),
(24, '单品', 16.00, 50, 1),

-- 瑞幸科兴 (products 25-35)
(25, '中杯', 19.90, 180, 1), (25, '大杯', 23.90, 140, 1),
(26, '中杯', 22.00, 160, 1), (26, '大杯', 26.00, 120, 1),
(27, '中杯', 16.00, 200, 1), (27, '大杯', 20.00, 160, 1),
(28, '中杯', 21.00, 140, 1), (28, '大杯', 25.00, 100, 1),
(29, '中杯', 23.00, 130, 1), (29, '大杯', 27.00, 100, 1),
(30, '大杯', 22.00, 120, 1),
(31, '大杯', 25.00, 80, 1),
(32, '中杯', 20.00, 150, 1), (32, '大杯', 24.00, 110, 1),
(33, '大杯', 25.00, 90, 1),
(34, '单品', 16.00, 45, 1),
(35, '单品', 15.00, 40, 1),

-- 库迪深大 (products 36-45)
(36, '中杯', 15.00, 160, 1), (36, '大杯', 19.00, 120, 1),
(37, '中杯', 13.00, 180, 1), (37, '大杯', 17.00, 140, 1),
(38, '中杯', 16.00, 100, 1), (38, '大杯', 20.00, 80, 1),
(39, '中杯', 18.00, 150, 1), (39, '大杯', 22.00, 110, 1),
(40, '大杯', 22.00, 100, 1),
(41, '中杯', 16.00, 90, 1), (41, '大杯', 20.00, 70, 1),
(42, '中杯', 14.00, 100, 1), (42, '大杯', 18.00, 80, 1),
(43, '中杯', 13.00, 120, 1), (43, '大杯', 17.00, 90, 1),
(44, '单品', 12.00, 40, 1),
(45, '单品', 14.00, 35, 1),

-- 星巴克万象天地 (products 46-55)
(46, '中杯', 30.00, 100, 1), (46, '大杯', 35.00, 80, 1), (46, '超大杯', 39.00, 60, 1),
(47, '中杯', 26.00, 120, 1), (47, '大杯', 30.00, 90, 1), (47, '超大杯', 34.00, 70, 1),
(48, '中杯', 32.00, 60, 1), (48, '大杯', 37.00, 50, 1), (48, '超大杯', 41.00, 30, 1),
(49, '中杯', 31.00, 70, 1), (49, '大杯', 36.00, 50, 1), (49, '超大杯', 40.00, 30, 1),
(50, '中杯', 33.00, 50, 1), (50, '大杯', 38.00, 30, 1),
(51, '中杯', 35.00, 40, 1), (51, '大杯', 40.00, 25, 1),
(52, '中杯', 36.00, 70, 1), (52, '大杯', 41.00, 50, 1), (52, '超大杯', 45.00, 30, 1),
(53, '中杯', 36.00, 60, 1), (53, '大杯', 41.00, 40, 1), (53, '超大杯', 45.00, 25, 1),
(54, '单品', 28.00, 25, 1),
(55, '单品', 22.00, 30, 1),

-- Manner高新园 (products 56-65)
(56, '中杯', 20.00, 100, 1), (56, '大杯', 25.00, 70, 1),
(57, '中杯', 15.00, 120, 1), (57, '大杯', 20.00, 90, 1),
(58, '中杯', 22.00, 60, 1),
(59, '中杯', 18.00, 80, 1),
(60, '单品', 28.00, 20, 1),
(61, '单品', 30.00, 15, 1),
(62, '中杯', 22.00, 70, 1), (62, '大杯', 27.00, 50, 1),
(63, '中杯', 23.00, 80, 1), (63, '大杯', 28.00, 60, 1),
(64, '中杯', 20.00, 70, 1), (64, '大杯', 25.00, 50, 1),
(65, '单品', 16.00, 35, 1);

-- ============================================
-- 十七、插入一些用户行为数据（推荐系统种子数据）
-- ============================================
INSERT INTO `user_behavior` (`user_id`, `product_id`, `store_id`, `type`, `count`, `score`) VALUES
(3, 1, 1, 'view', 5, 4.8), (3, 1, 1, 'cart', 2, NULL), (3, 1, 1, 'order', 1, NULL),
(3, 2, 1, 'view', 3, 4.5), (3, 2, 1, 'favorite', 1, NULL),
(3, 13, 2, 'view', 4, 4.9), (3, 13, 2, 'order', 2, NULL),
(3, 14, 2, 'view', 2, 4.7), (3, 14, 2, 'cart', 1, NULL),
(3, 25, 3, 'view', 3, 4.8);

-- ============================================
-- 完成！
-- ============================================
-- 测试用户: user / 13800138002 (密码 BCrypt 加密，原文 123456)
-- 所有门店均在 (22.5483, 113.9434) 附近 5km 内
-- 每家门店 10+ 商品，每个商品 1-3 个规格
-- 推荐系统已有种子数据
-- ============================================
