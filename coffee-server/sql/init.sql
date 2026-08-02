-- ============================================
-- Coffee Order 数据库初始化脚本（v2.0 全新设计）
-- 说明：执行前会删除旧数据库 coffee_order 并重建。
--   字符集 utf8mb4，排序规则 utf8mb4_unicode_ci。
--   测试账号密码统一为 123456（BCrypt）。
-- ============================================

-- 强制本次连接使用 utf8mb4，避免客户端默认字符集（如 GBK）导致中文乱码/插入失败
SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

DROP DATABASE IF EXISTS `coffee_order`;
CREATE DATABASE `coffee_order`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `coffee_order`;

-- ============================================
-- 1. 用户与权限模块
-- ============================================

CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '登录账号（手机号/邮箱）',
  `password` VARCHAR(100) NOT NULL COMMENT 'BCrypt加密密码',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `gender` TINYINT(1) DEFAULT '0' COMMENT '性别：0-未知 1-男 2-女',
  `balance` DECIMAL(10,2) DEFAULT '0.00' COMMENT '钱包余额',
  `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN-平台管理员 STORE_MANAGER-商家 RIDER-骑手 USER-用户',
  `store_id` BIGINT DEFAULT NULL COMMENT '所属门店ID，为空代表平台管理员/用户/骑手',
  `status` TINYINT NOT NULL DEFAULT '1' COMMENT '状态：1-启用 0-禁用',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_role_status` (`role`, `status`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE `sys_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` BIGINT NOT NULL DEFAULT '0' COMMENT '父菜单ID，0为根节点',
  `name` VARCHAR(50) NOT NULL COMMENT '菜单名称（路由名）',
  `path` VARCHAR(100) DEFAULT NULL COMMENT '路由路径',
  `component` VARCHAR(100) DEFAULT NULL COMMENT '组件路径',
  `title` VARCHAR(50) NOT NULL COMMENT '菜单标题（显示名称）',
  `icon` VARCHAR(50) DEFAULT NULL COMMENT '图标类名',
  `order_num` INT NOT NULL DEFAULT '0' COMMENT '排序号',
  `menu_type` TINYINT NOT NULL DEFAULT '1' COMMENT '菜单类型：1-目录 2-菜单 3-按钮',
  `perms` VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
  `status` TINYINT NOT NULL DEFAULT '1' COMMENT '状态：1-启用 0-禁用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status_order` (`status`, `order_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';

CREATE TABLE `sys_role_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role` VARCHAR(20) NOT NULL COMMENT '角色标识',
  `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role`, `menu_id`),
  KEY `idx_role` (`role`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- ============================================
-- 2. 门店与商品模块
-- ============================================

CREATE TABLE `store` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '门店ID',
  `name` VARCHAR(100) NOT NULL COMMENT '门店名称',
  `address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `latitude` DECIMAL(10,7) NOT NULL COMMENT '纬度',
  `longitude` DECIMAL(10,7) NOT NULL COMMENT '经度',
  `image` VARCHAR(255) DEFAULT NULL COMMENT '门店图片URL',
  `business_hours` VARCHAR(50) DEFAULT NULL COMMENT '营业时间描述',
  `open_time` TIME DEFAULT NULL COMMENT '开门时间',
  `close_time` TIME DEFAULT NULL COMMENT '关门时间',
  `min_delivery_amount` DECIMAL(10,2) DEFAULT '0.00' COMMENT '起送金额',
  `delivery_fee` DECIMAL(10,2) DEFAULT '0.00' COMMENT '配送费',
  `status` TINYINT NOT NULL DEFAULT '1' COMMENT '状态：1-营业中 0-休息中',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_location` (`longitude`, `latitude`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门店表';

CREATE TABLE `product_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `store_id` BIGINT NOT NULL COMMENT '所属门店ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `sort_order` INT NOT NULL DEFAULT '0' COMMENT '排序号',
  `status` TINYINT NOT NULL DEFAULT '1' COMMENT '状态：1-启用 0-禁用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_status_sort` (`status`, `sort_order`),
  CONSTRAINT `fk_category_store` FOREIGN KEY (`store_id`) REFERENCES `store` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

CREATE TABLE `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `store_id` BIGINT NOT NULL COMMENT '所属门店ID',
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `image` VARCHAR(255) DEFAULT NULL COMMENT '商品图片URL',
  `price` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '基础售价',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '商品描述',
  `is_recommend` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '是否推荐：1-是 0-否',
  `status` TINYINT NOT NULL DEFAULT '1' COMMENT '状态：1-上架 0-下架',
  `sales` INT NOT NULL DEFAULT '0' COMMENT '累计销量',
  `rating` DECIMAL(2,1) DEFAULT '5.0' COMMENT '平均评分（1.0-5.0）',
  `rating_count` INT NOT NULL DEFAULT '0' COMMENT '评分人数',
  `sort_order` INT NOT NULL DEFAULT '0' COMMENT '排序号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status_sales` (`status`, `sales`),
  KEY `idx_name` (`name`),
  CONSTRAINT `fk_product_store` FOREIGN KEY (`store_id`) REFERENCES `store` (`id`),
  CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `product_category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

CREATE TABLE `product_spec` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '规格ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `spec_name` VARCHAR(50) NOT NULL COMMENT '规格维度（如 温度/糖度/杯型）',
  `spec_value` VARCHAR(50) NOT NULL COMMENT '具体选项值（如 热/冰/正常糖/中杯）',
  `price_adjust` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '价格调整（可为0/正/负）',
  `status` TINYINT NOT NULL DEFAULT '1' COMMENT '状态：1-可用 0-不可用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_spec_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品规格表';

-- ============================================
-- 3. 订单与配送模块
-- ============================================

CREATE TABLE `orders` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号（唯一）',
  `user_id` BIGINT NOT NULL COMMENT '下单用户ID',
  `store_id` BIGINT NOT NULL COMMENT '门店ID',
  `rider_id` BIGINT DEFAULT NULL COMMENT '配送骑手ID',
  `pickup_type` TINYINT NOT NULL DEFAULT '0' COMMENT '取餐方式：0-自取 1-外卖',
  `address_id` BIGINT DEFAULT NULL COMMENT '收货地址ID（外卖时）',
  `status` TINYINT NOT NULL DEFAULT '0' COMMENT '订单状态：0-待支付 1-制作中 2-待取餐 3-已完成 4-已取消',
  `total_price` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '商品总价（含规格）',
  `delivery_fee` DECIMAL(10,2) DEFAULT '0.00' COMMENT '配送费',
  `discount_amount` DECIMAL(10,2) DEFAULT '0.00' COMMENT '优惠金额',
  `address_snapshot` VARCHAR(500) DEFAULT NULL COMMENT '收货地址快照（外卖时冗余）',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '订单备注',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `accept_time` DATETIME DEFAULT NULL COMMENT '接单时间',
  `pickup_time` DATETIME DEFAULT NULL COMMENT '取餐时间',
  `deliver_time` DATETIME DEFAULT NULL COMMENT '送达时间',
  `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_rider_id` (`rider_id`),
  KEY `idx_status` (`status`),
  KEY `idx_pickup_type_status` (`pickup_type`, `status`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_order_store` FOREIGN KEY (`store_id`) REFERENCES `store` (`id`),
  CONSTRAINT `fk_order_rider` FOREIGN KEY (`rider_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单主表';

CREATE TABLE `order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称（冗余）',
  `spec_info` VARCHAR(200) DEFAULT NULL COMMENT '规格拼接（如 冰/少糖/大杯）',
  `quantity` INT NOT NULL COMMENT '购买数量',
  `price` DECIMAL(10,2) NOT NULL COMMENT '购买时单价（含规格调整）',
  `total` DECIMAL(10,2) NOT NULL COMMENT '小计金额',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`),
  CONSTRAINT `fk_item_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `fk_item_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单商品明细表';

CREATE TABLE `order_status_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `from_status` TINYINT DEFAULT NULL COMMENT '原状态',
  `to_status` TINYINT NOT NULL COMMENT '目标状态',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `operator_type` TINYINT NOT NULL DEFAULT '0' COMMENT '操作人类型：0-系统 1-用户 2-骑手 3-商家 4-管理员',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '操作备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_status_log_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单状态流转日志表';

CREATE TABLE `rider_location` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '位置记录ID',
  `rider_id` BIGINT NOT NULL COMMENT '骑手ID',
  `order_id` BIGINT DEFAULT NULL COMMENT '当前配送订单ID',
  `longitude` DECIMAL(10,7) NOT NULL COMMENT '经度',
  `latitude` DECIMAL(10,7) NOT NULL COMMENT '纬度',
  `accuracy` FLOAT DEFAULT NULL COMMENT '定位精度（米）',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '位置更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rider_id` (`rider_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_update_time` (`update_time`),
  CONSTRAINT `fk_location_rider` FOREIGN KEY (`rider_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_location_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='骑手实时位置表';

-- ============================================
-- 4. 购物车模块
-- ============================================

CREATE TABLE `cart` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `store_id` BIGINT NOT NULL COMMENT '门店ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `spec_info` VARCHAR(200) DEFAULT NULL COMMENT '规格拼接（如 冰/少糖/大杯）',
  `price` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '单价（含规格调整）',
  `quantity` INT NOT NULL DEFAULT '1' COMMENT '数量',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_store_product_spec` (`user_id`, `store_id`, `product_id`, `spec_info`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_store_id` (`store_id`),
  CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_cart_store` FOREIGN KEY (`store_id`) REFERENCES `store` (`id`),
  CONSTRAINT `fk_cart_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- ============================================
-- 5. 用户地址 / 反馈模块
-- ============================================

CREATE TABLE `user_address` (
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

CREATE TABLE `feedback` (
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

-- ============================================
-- 6. 推荐与评价模块
-- ============================================

CREATE TABLE `user_behavior` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '行为ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `store_id` BIGINT NOT NULL COMMENT '门店ID',
  `type` ENUM('view','cart','order','favorite') NOT NULL COMMENT '行为类型',
  `count` INT NOT NULL DEFAULT '1' COMMENT '行为次数',
  `score` TINYINT DEFAULT NULL COMMENT '评分（1-5，仅order类型有效）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product_type` (`user_id`, `product_id`, `type`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_type` (`type`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_behavior_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_behavior_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
  CONSTRAINT `fk_behavior_store` FOREIGN KEY (`store_id`) REFERENCES `store` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行为表';

CREATE TABLE `evaluation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `store_id` BIGINT NOT NULL COMMENT '门店ID',
  `product_id` BIGINT DEFAULT NULL COMMENT '商品ID（NULL表示评价整体订单）',
  `score` TINYINT NOT NULL COMMENT '评分（1-5星）',
  `content` VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
  `images` JSON DEFAULT NULL COMMENT '评价图片URL数组',
  `is_anonymous` TINYINT NOT NULL DEFAULT '0' COMMENT '是否匿名：0-否 1-是',
  `reply` VARCHAR(500) DEFAULT NULL COMMENT '商家回复',
  `reply_time` DATETIME DEFAULT NULL COMMENT '回复时间',
  `status` TINYINT NOT NULL DEFAULT '1' COMMENT '状态：1-显示 0-隐藏',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_score` (`score`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_eval_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `fk_eval_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_eval_store` FOREIGN KEY (`store_id`) REFERENCES `store` (`id`),
  CONSTRAINT `fk_eval_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- ============================================
-- 7. 系统配置与日志模块
-- ============================================

CREATE TABLE `sys_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` VARCHAR(50) NOT NULL COMMENT '配置键',
  `config_value` TEXT COMMENT '配置值',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注说明',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

CREATE TABLE `sys_login_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
  `username` VARCHAR(50) DEFAULT NULL COMMENT '登录账号',
  `ip` VARCHAR(50) DEFAULT NULL COMMENT '登录IP',
  `location` VARCHAR(100) DEFAULT NULL COMMENT '登录地点',
  `browser` VARCHAR(50) DEFAULT NULL COMMENT '浏览器',
  `os` VARCHAR(50) DEFAULT NULL COMMENT '操作系统',
  `status` TINYINT NOT NULL DEFAULT '1' COMMENT '状态：1-成功 0-失败',
  `msg` VARCHAR(255) DEFAULT NULL COMMENT '登录消息',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

CREATE TABLE `sys_oper_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `title` VARCHAR(50) DEFAULT NULL COMMENT '操作模块',
  `business_type` TINYINT DEFAULT '0' COMMENT '业务类型：0-其他 1-新增 2-修改 3-删除 4-授权 5-导出',
  `method` VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
  `request_method` VARCHAR(10) DEFAULT NULL COMMENT '请求方式（GET/POST/PUT/DELETE）',
  `operator_type` TINYINT DEFAULT '0' COMMENT '操作人类型：0-其他 1-后台用户 2-移动端用户',
  `user_id` BIGINT DEFAULT NULL COMMENT '操作人员ID',
  `url` VARCHAR(255) DEFAULT NULL COMMENT '请求URL',
  `ip` VARCHAR(50) DEFAULT NULL COMMENT '操作IP',
  `location` VARCHAR(100) DEFAULT NULL COMMENT '操作地点',
  `param` TEXT DEFAULT NULL COMMENT '请求参数',
  `json_result` TEXT DEFAULT NULL COMMENT '返回结果',
  `status` TINYINT NOT NULL DEFAULT '1' COMMENT '状态：1-正常 0-异常',
  `error_msg` TEXT DEFAULT NULL COMMENT '错误消息',
  `cost_time` BIGINT DEFAULT '0' COMMENT '消耗时间（毫秒）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_business_type` (`business_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ============================================
-- 8. 种子数据
-- ============================================

INSERT INTO `sys_config` (`config_key`, `config_value`, `remark`) VALUES
('deepseek.api.key', '', 'DeepSeek API Key'),
('deepseek.api.url', 'https://api.deepseek.com/v1/chat/completions', 'DeepSeek API地址'),
('deepseek.model', 'deepseek-v4-flash', 'DeepSeek模型名称'),
('tencent.map.key', 'USIBZ-OZSO4-BFMUH-KOPL6-G4IDE-45FTF', '腾讯地图Key'),
('system.name', 'Coffee Order', '系统名称'),
('system.version', '2.0.0', '系统版本');

-- ============================================
-- 8.1 测试账号（密码统一 123456，BCrypt）
--   admin  / ADMIN         / 平台管理员
--   store1 / STORE_MANAGER / 1号门店商家
--   rider1 / RIDER         / 测试骑手
--   user1  / USER          / 测试用户
-- ============================================

INSERT INTO `sys_user` (`username`, `password`, `nickname`, `phone`, `email`, `balance`, `role`, `store_id`, `status`) VALUES
('admin',  '$2a$10$NPtS4Mw2PzvtEDE3DoWeYOjXvODtHoGB41gWqHZtnU4M/RvShl2T2', '平台管理员', '13800000001', 'admin@coffee.com', 0.00, 'ADMIN', NULL, 1),
('store1', '$2a$10$vCvY9s67.vHauW9Fl7MOg.kzCPilFtxCnPhmChcM36m6ci4vpgkGa', '科技园店商家', '13800000002', 'store1@coffee.com', 0.00, 'STORE_MANAGER', 1, 1),
('rider1', '$2a$10$9U/sVxk/EVQDfjSSLxsR1euBBqSkq34arCnaHVOo8OuPO7jI9rZzK', '测试骑手', '13800000003', 'rider1@coffee.com', 0.00, 'RIDER', NULL, 1),
('user1',  '$2a$10$dextOXv3WNfi4L6lg/Pu0.q2eXW2ycefX085FJ.sKNtWiGpRd3Dbi', '测试用户', '13800000004', 'user1@coffee.com', 1000.00, 'USER', NULL, 1);

-- ============================================
-- 8.2 门店（3 家，位于 (22.54, 113.94) 附近 5 公里内）
-- ============================================

INSERT INTO `store` (`id`, `name`, `address`, `phone`, `latitude`, `longitude`, `image`, `business_hours`, `open_time`, `close_time`, `min_delivery_amount`, `delivery_fee`, `status`) VALUES
(1, '星巴克咖啡（科技园店）', '深圳市南山区科技园南区A栋1楼', '0755-86101111', 22.5482000, 113.9433000, 'https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=400', '07:00-22:00', '07:00:00', '22:00:00', 25.00, 3.00, 1),
(2, '瑞幸咖啡（软件园店）', '深圳市南山区软件园B栋大堂', '0755-86202222', 22.5420000, 113.9520000, 'https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=400', '06:30-22:30', '06:30:00', '22:30:00', 15.00, 2.00, 1),
(3, '瑞幸咖啡（科兴科学园店）', '深圳市南山区科兴科学园C1栋1楼', '0755-86303333', 22.5510000, 113.9470000, 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=400', '07:00-21:30', '07:00:00', '21:30:00', 15.00, 2.50, 1);

-- ============================================
-- 8.3 商品分类（每家 3 类：经典咖啡/瑞纳冰/轻食）
-- ============================================

INSERT INTO `product_category` (`id`, `store_id`, `name`, `sort_order`, `status`) VALUES
(1, 1, '经典咖啡', 1, 1), (2, 1, '瑞纳冰', 2, 1), (3, 1, '轻食', 3, 1),
(4, 2, '经典咖啡', 1, 1), (5, 2, '瑞纳冰', 2, 1), (6, 2, '轻食', 3, 1),
(7, 3, '经典咖啡', 1, 1), (8, 3, '瑞纳冰', 2, 1), (9, 3, '轻食', 3, 1);

-- ============================================
-- 8.4 商品（每店 9 个，共 27 个；id 固定 1-27）
-- ============================================

INSERT INTO `product` (`id`, `store_id`, `category_id`, `name`, `image`, `price`, `description`, `is_recommend`, `status`, `sales`, `rating`, `rating_count`, `sort_order`) VALUES
-- ==== 星巴克科技园 (store 1) ====
(1, 1, 1, '拿铁',     'https://images.unsplash.com/photo-1570968915860-54d5c301fa9f?w=400', 28.00, '浓缩咖啡与丝滑牛乳的经典融合', 1, 1, 3280, 4.8, 856, 1),
(2, 1, 1, '美式咖啡', 'https://images.unsplash.com/photo-1559496417-e7f25cb247f3?w=400', 22.00, '纯粹黑咖啡，清爽提神零负担', 0, 1, 2960, 4.6, 680, 2),
(3, 1, 1, '卡布奇诺', 'https://images.unsplash.com/photo-1572442386806-1050eaef1c17?w=400', 30.00, '意式浓缩加厚奶泡，口感绵密', 0, 1, 1780, 4.7, 442, 3),
(4, 1, 1, '焦糖玛奇朵', 'https://images.unsplash.com/photo-1523948855788-f0502cfa49l?w=400', 32.00, '香醇焦糖邂逅浓缩咖啡的甜蜜滋味', 1, 1, 2120, 4.8, 523, 4),
(5, 1, 2, '抹茶瑞纳冰', 'https://images.unsplash.com/photo-1536256263959-770b48d82b0a?w=400', 32.00, '抹茶冰沙搭配奶油顶', 1, 1, 1100, 4.5, 312, 1),
(6, 1, 2, '可可瑞纳冰', 'https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=400', 32.00, '浓郁巧克力冰沙，夏日必备', 0, 1, 980, 4.6, 278, 2),
(7, 1, 2, '摩卡瑞纳冰', 'https://images.unsplash.com/photo-1517701550927-30cf4ba1dba5?w=400', 34.00, '巧克力与咖啡的冰爽碰撞', 0, 1, 760, 4.7, 198, 3),
(8, 1, 3, '提拉米苏', 'https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?w=400', 28.00, '经典意式提拉米苏，入口即化', 0, 1, 760, 4.7, 189, 1),
(9, 1, 3, '法式牛角包', 'https://images.unsplash.com/photo-1555507036-ab1f4038024a?w=400', 18.00, '酥脆多层，黄油香气浓郁', 0, 1, 1020, 4.5, 234, 2),
-- ==== 瑞幸软件园 (store 2) ====
(10, 2, 4, '生椰拿铁', 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400', 19.90, '椰乳与浓缩咖啡的完美搭配，年销3亿杯', 1, 1, 5100, 4.9, 1420, 1),
(11, 2, 4, '厚乳拿铁', 'https://images.unsplash.com/photo-1570968915860-54d5c301fa9f?w=400', 22.00, '双倍牛乳，口感醇厚丝滑', 1, 1, 3800, 4.8, 980, 2),
(12, 2, 4, '标准美式', 'https://images.unsplash.com/photo-1559496417-e7f25cb247f3?w=400', 16.00, '经典美式，纯粹咖啡体验', 0, 1, 4200, 4.6, 1120, 3),
(13, 2, 4, '澳白', 'https://images.unsplash.com/photo-1485808191679-5f86510681a2?w=400', 21.00, '精萃浓缩与丝滑奶泡的黄金比例', 0, 1, 1200, 4.7, 345, 4),
(14, 2, 5, '抹茶瑞纳冰', 'https://images.unsplash.com/photo-1536256263959-770b48d82b0a?w=400', 23.00, '抹茶冰沙搭配奶油顶', 1, 1, 1100, 4.5, 312, 1),
(15, 2, 5, '可可瑞纳冰', 'https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=400', 24.00, '浓郁巧克力冰沙，夏日必备', 0, 1, 980, 4.6, 278, 2),
(16, 2, 5, '生椰瑞纳冰', 'https://images.unsplash.com/photo-1517701550927-30cf4ba1dba5?w=400', 25.00, '清爽椰香冰沙，清凉一夏', 0, 1, 720, 4.7, 189, 3),
(17, 2, 6, '火腿芝士可颂', 'https://images.unsplash.com/photo-1555507036-ab1f4038024a?w=400', 16.00, '酥脆可颂搭配火腿芝士', 0, 1, 540, 4.4, 156, 1),
(18, 2, 6, '巧克力麦芬', 'https://images.unsplash.com/photo-1558401391-7899b40bd42c?w=400', 15.00, '浓郁巧克力，松软可口', 0, 1, 410, 4.3, 123, 2),
-- ==== 瑞幸科兴科学园 (store 3) ====
(19, 3, 7, '生椰拿铁', 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400', 19.90, '椰乳与浓缩咖啡的完美搭配', 1, 1, 2300, 4.9, 678, 1),
(20, 3, 7, '丝绒拿铁', 'https://images.unsplash.com/photo-1485808191679-5f86510681a2?w=400', 21.00, '超滤牛乳打造丝绒般顺滑口感', 0, 1, 1600, 4.7, 456, 2),
(21, 3, 7, '标准美式', 'https://images.unsplash.com/photo-1559496417-e7f25cb247f3?w=400', 16.00, '经典美式，纯粹咖啡体验', 0, 1, 2100, 4.6, 589, 3),
(22, 3, 7, '山茶花拿铁', 'https://images.unsplash.com/photo-1536508817515-0e85b42b2a25?w=400', 25.00, '山茶花清香融入醇美咖啡', 0, 1, 1100, 4.6, 312, 4),
(23, 3, 8, '抹茶瑞纳冰', 'https://images.unsplash.com/photo-1536256263959-770b48d82b0a?w=400', 23.00, '抹茶冰沙搭配奶油顶', 1, 1, 890, 4.5, 234, 1),
(24, 3, 8, '生椰瑞纳冰', 'https://images.unsplash.com/photo-1517701550927-30cf4ba1dba5?w=400', 25.00, '清爽椰香冰沙，清凉一夏', 0, 1, 650, 4.7, 178, 2),
(25, 3, 9, '海盐卷', 'https://images.unsplash.com/photo-1555507036-ab1f4038024a?w=400', 14.00, '日式海盐面包，外脆里软', 0, 1, 210, 4.4, 67, 1),
(26, 3, 9, '火腿芝士可颂', 'https://images.unsplash.com/photo-1555507036-ab1f4038024a?w=400', 16.00, '酥脆可颂搭配火腿芝士', 0, 1, 320, 4.4, 98, 2),
(27, 3, 9, '原味华夫饼', 'https://images.unsplash.com/photo-1555507036-ab1f4038024a?w=400', 15.00, '现烤华夫饼，外酥里嫩', 0, 1, 290, 4.2, 76, 3);

-- ============================================
-- 8.5 商品规格（温度/糖度/杯型 三维；饮品通用，轻食使用 温度/糖度/份量）
--   温度：热/冰；糖度：正常糖/少糖/无糖；杯型：中杯/大杯(+3.00)
-- ============================================

INSERT INTO `product_spec` (`product_id`, `spec_name`, `spec_value`, `price_adjust`, `status`) VALUES
-- ==== 饮品规格（商品 1-7, 10-16, 19-24）====
(1,'温度','热',0.00,1),(1,'温度','冰',0.00,1),(1,'糖度','正常糖',0.00,1),(1,'糖度','少糖',0.00,1),(1,'糖度','无糖',0.00,1),(1,'杯型','中杯',0.00,1),(1,'杯型','大杯',3.00,1),
(2,'温度','热',0.00,1),(2,'温度','冰',0.00,1),(2,'糖度','正常糖',0.00,1),(2,'糖度','少糖',0.00,1),(2,'糖度','无糖',0.00,1),(2,'杯型','中杯',0.00,1),(2,'杯型','大杯',3.00,1),
(3,'温度','热',0.00,1),(3,'温度','冰',0.00,1),(3,'糖度','正常糖',0.00,1),(3,'糖度','少糖',0.00,1),(3,'糖度','无糖',0.00,1),(3,'杯型','中杯',0.00,1),(3,'杯型','大杯',3.00,1),
(4,'温度','热',0.00,1),(4,'温度','冰',0.00,1),(4,'糖度','正常糖',0.00,1),(4,'糖度','少糖',0.00,1),(4,'糖度','无糖',0.00,1),(4,'杯型','中杯',0.00,1),(4,'杯型','大杯',3.00,1),
(5,'温度','冰',0.00,1),(5,'糖度','正常糖',0.00,1),(5,'糖度','少糖',0.00,1),(5,'糖度','无糖',0.00,1),(5,'杯型','中杯',0.00,1),(5,'杯型','大杯',3.00,1),
(6,'温度','冰',0.00,1),(6,'糖度','正常糖',0.00,1),(6,'糖度','少糖',0.00,1),(6,'糖度','无糖',0.00,1),(6,'杯型','中杯',0.00,1),(6,'杯型','大杯',3.00,1),
(7,'温度','冰',0.00,1),(7,'糖度','正常糖',0.00,1),(7,'糖度','少糖',0.00,1),(7,'糖度','无糖',0.00,1),(7,'杯型','中杯',0.00,1),(7,'杯型','大杯',3.00,1),
(10,'温度','热',0.00,1),(10,'温度','冰',0.00,1),(10,'糖度','正常糖',0.00,1),(10,'糖度','少糖',0.00,1),(10,'糖度','无糖',0.00,1),(10,'杯型','中杯',0.00,1),(10,'杯型','大杯',3.00,1),
(11,'温度','热',0.00,1),(11,'温度','冰',0.00,1),(11,'糖度','正常糖',0.00,1),(11,'糖度','少糖',0.00,1),(11,'糖度','无糖',0.00,1),(11,'杯型','中杯',0.00,1),(11,'杯型','大杯',3.00,1),
(12,'温度','热',0.00,1),(12,'温度','冰',0.00,1),(12,'糖度','正常糖',0.00,1),(12,'糖度','少糖',0.00,1),(12,'糖度','无糖',0.00,1),(12,'杯型','中杯',0.00,1),(12,'杯型','大杯',3.00,1),
(13,'温度','热',0.00,1),(13,'温度','冰',0.00,1),(13,'糖度','正常糖',0.00,1),(13,'糖度','少糖',0.00,1),(13,'糖度','无糖',0.00,1),(13,'杯型','中杯',0.00,1),(13,'杯型','大杯',3.00,1),
(14,'温度','冰',0.00,1),(14,'糖度','正常糖',0.00,1),(14,'糖度','少糖',0.00,1),(14,'糖度','无糖',0.00,1),(14,'杯型','中杯',0.00,1),(14,'杯型','大杯',3.00,1),
(15,'温度','冰',0.00,1),(15,'糖度','正常糖',0.00,1),(15,'糖度','少糖',0.00,1),(15,'糖度','无糖',0.00,1),(15,'杯型','中杯',0.00,1),(15,'杯型','大杯',3.00,1),
(16,'温度','冰',0.00,1),(16,'糖度','正常糖',0.00,1),(16,'糖度','少糖',0.00,1),(16,'糖度','无糖',0.00,1),(16,'杯型','中杯',0.00,1),(16,'杯型','大杯',3.00,1),
(19,'温度','热',0.00,1),(19,'温度','冰',0.00,1),(19,'糖度','正常糖',0.00,1),(19,'糖度','少糖',0.00,1),(19,'糖度','无糖',0.00,1),(19,'杯型','中杯',0.00,1),(19,'杯型','大杯',3.00,1),
(20,'温度','热',0.00,1),(20,'温度','冰',0.00,1),(20,'糖度','正常糖',0.00,1),(20,'糖度','少糖',0.00,1),(20,'糖度','无糖',0.00,1),(20,'杯型','中杯',0.00,1),(20,'杯型','大杯',3.00,1),
(21,'温度','热',0.00,1),(21,'温度','冰',0.00,1),(21,'糖度','正常糖',0.00,1),(21,'糖度','少糖',0.00,1),(21,'糖度','无糖',0.00,1),(21,'杯型','中杯',0.00,1),(21,'杯型','大杯',3.00,1),
(22,'温度','热',0.00,1),(22,'温度','冰',0.00,1),(22,'糖度','正常糖',0.00,1),(22,'糖度','少糖',0.00,1),(22,'糖度','无糖',0.00,1),(22,'杯型','中杯',0.00,1),(22,'杯型','大杯',3.00,1),
(23,'温度','冰',0.00,1),(23,'糖度','正常糖',0.00,1),(23,'糖度','少糖',0.00,1),(23,'糖度','无糖',0.00,1),(23,'杯型','中杯',0.00,1),(23,'杯型','大杯',3.00,1),
(24,'温度','冰',0.00,1),(24,'糖度','正常糖',0.00,1),(24,'糖度','少糖',0.00,1),(24,'糖度','无糖',0.00,1),(24,'杯型','中杯',0.00,1),(24,'杯型','大杯',3.00,1),
-- ==== 轻食规格（商品 8, 9, 17, 18, 25, 26, 27）====
(8,'温度','常温',0.00,1),(8,'温度','加热',0.00,1),(8,'糖度','无糖',0.00,1),(8,'糖度','含糖',0.00,1),(8,'份量','标准',0.00,1),(8,'份量','加大',3.00,1),
(9,'温度','常温',0.00,1),(9,'温度','加热',0.00,1),(9,'糖度','无糖',0.00,1),(9,'糖度','含糖',0.00,1),(9,'份量','标准',0.00,1),(9,'份量','加大',3.00,1),
(17,'温度','常温',0.00,1),(17,'温度','加热',0.00,1),(17,'糖度','无糖',0.00,1),(17,'糖度','含糖',0.00,1),(17,'份量','标准',0.00,1),(17,'份量','加大',3.00,1),
(18,'温度','常温',0.00,1),(18,'温度','加热',0.00,1),(18,'糖度','无糖',0.00,1),(18,'糖度','含糖',0.00,1),(18,'份量','标准',0.00,1),(18,'份量','加大',3.00,1),
(25,'温度','常温',0.00,1),(25,'温度','加热',0.00,1),(25,'糖度','无糖',0.00,1),(25,'糖度','含糖',0.00,1),(25,'份量','标准',0.00,1),(25,'份量','加大',3.00,1),
(26,'温度','常温',0.00,1),(26,'温度','加热',0.00,1),(26,'糖度','无糖',0.00,1),(26,'糖度','含糖',0.00,1),(26,'份量','标准',0.00,1),(26,'份量','加大',3.00,1),
(27,'温度','常温',0.00,1),(27,'温度','加热',0.00,1),(27,'糖度','无糖',0.00,1),(27,'糖度','含糖',0.00,1),(27,'份量','标准',0.00,1),(27,'份量','加大',3.00,1);

-- ============================================
-- 8.6 菜单与角色菜单
-- ============================================

INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `title`, `icon`, `order_num`, `menu_type`, `perms`, `status`) VALUES
(0, 'Dashboard', '/dashboard', 'dashboard/index', '数据概览', 'DataLine', 1, 2, 'dashboard:view', 1),
(0, 'Store', '/store', NULL, '门店管理', 'Shop', 2, 1, NULL, 1),
(0, 'Product', '/product', NULL, '商品管理', 'Goods', 3, 1, NULL, 1),
(0, 'Order', '/order', NULL, '订单管理', 'List', 4, 1, NULL, 1),
(0, 'Rider', '/rider', NULL, '骑手管理', 'Bicycle', 5, 1, NULL, 1),
(0, 'User', '/user', NULL, '用户管理', 'User', 6, 1, NULL, 1),
(0, 'System', '/system', NULL, '系统管理', 'Setting', 7, 1, NULL, 1);

INSERT INTO `sys_role_menu` (`role`, `menu_id`) VALUES
('ADMIN', 1), ('ADMIN', 2), ('ADMIN', 3), ('ADMIN', 4), ('ADMIN', 5), ('ADMIN', 6), ('ADMIN', 7),
('STORE_MANAGER', 4);
