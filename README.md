# Coffee Order — 基于协同过滤与AI大模型的O2O咖啡外卖点单系统

> **项目代号**：`coffee-order`  
> **版本**：`v1.0.0`  
> **文档类型**：AI代码生成指令文档（AI Code Generation Specification）  
> **目标读者**：AI编程助手（Cursor/Copilot/Claude Code等）  
> **最后更新**：2026-07-14

---

## 文档定位

本文档是**面向AI代码生成工具的详细技术规格书**，AI工具阅读后应能**独立生成可编译、可运行的代码**。

---

## 1. 系统架构总览

### 1.1 架构风格

采用**"微服务风格的单体架构"（Modular Monolith）**：
- 当前阶段为单体部署，代码层面按领域模块严格分层
- 每个领域模块的Service层通过接口隔离，未来可无痛拆分为独立微服务
- 数据库层面预留分库分表扩展点

### 1.2 技术栈矩阵

| 层级 | 技术选型 | 版本 | 选型理由 |
|------|---------|------|---------|
| 后端语言 | Java | 21 LTS | 虚拟线程支持高并发 |
| 基础框架 | Spring Boot | 3.2.0 | 原生支持虚拟线程 |
| 安全框架 | Spring Security | 6.2.0 | JWT无状态认证 |
| ORM框架 | MyBatis-Plus | 3.5.4.1 | 代码生成器、分页插件 |
| 数据库 | MySQL | 8.0.44 | JSON类型、窗口函数 |
| 缓存 | Redis | 5.0.14.1 | Geo、ZSet支持 |
| 文档 | Knife4j | 4.5.0 | OpenAPI 3自动生成 |
| 管理前端 | Vue 3 + Vite | 3.4.0 / 5.0.10 | 组合式API |
| 管理UI | Element Plus | 2.5.0 | 企业级组件库 |
| 管理状态 | Pinia | 2.1.7 | 类型安全 |
| 小程序 | 微信原生 + Vant | 基础库>=3.0 | 原生性能最优 |
| 地图 | 腾讯地图SDK | v1.2 | 微信小程序原生支持 |
| AI模型 | DeepSeek API | v4 | 中文理解能力强 |

---

## 2. 编码规范与命名约定

### 2.1 后端规范（Java）

**包结构**：`com.example.coffee.{模块名}.{层级}`

**类命名**：
- Controller: `XxxController`
- Service接口: `XxxService`
- Service实现: `XxxServiceImpl`
- Mapper: `XxxMapper`
- Entity: `Xxx`（对应表名，驼峰命名）
- DTO请求: `XxxReq` / `XxxDTO`
- DTO响应: `XxxResp` / `XxxVO`
- 工具类: `XxxUtil` / `XxxUtils`
- 常量: `XxxConstant` / `XxxConstants`
- 枚举: `XxxEnum`
- 异常: `XxxException`
- 配置类: `XxxConfig` / `XxxConfiguration`

**方法命名**：
- 查询单条: `getById` / `getByXxx`
- 查询列表: `list` / `listByXxx`
- 分页查询: `page` / `pageByXxx`
- 新增: `save` / `add` / `create`
- 修改: `update` / `modify`
- 删除: `remove` / `delete` / `removeById`

**数据库字段约定**：
- 主键: `id` (BIGINT, AUTO_INCREMENT)
- 创建时间: `create_time` (DATETIME, DEFAULT CURRENT_TIMESTAMP)
- 更新时间: `update_time` (DATETIME, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)
- 逻辑删除: `deleted` (TINYINT, DEFAULT 0, 1表示已删除)
- 状态: `status` (TINYINT, 1启用 0禁用)
- 外键: `xxx_id` (BIGINT, 建立索引)

**API路径规范**：
- 基础路径: `/api`
- 模块路径: `/api/{模块}`
- 资源操作: `GET /api/{resource}` (列表), `POST /api/{resource}` (新增)
- 单资源: `GET /api/{resource}/{id}`, `PUT /api/{resource}/{id}`, `DELETE /api/{resource}/{id}`
- 非标准操作: `POST /api/{resource}/{id}/action`

### 2.2 前端规范

**Vue 3 管理端**：
- 组件名: PascalCase
- 组合式函数: `useXxx`
- Store: `useXxxStore`
- API模块: `api/xxx.ts`
- 类型定义: `types/xxx.ts`

**微信小程序**：
- 页面目录: `pages/xxx/`
- 页面文件: `index.wxml`, `index.wxss`, `index.ts`, `index.json`
- 组件目录: `components/xxx/`
- 工具函数: `utils/xxx.ts`


---

## 3. 数据库设计（完整DDL）

> **数据库名**: `coffee_order`  
> **字符集**: `utf8mb4`  
> **排序规则**: `utf8mb4_unicode_ci`  
> **引擎**: `InnoDB`  
> **所有表必须包含**: `id`, `create_time`, `update_time`, `deleted`  
 > **所有外键必须建立索引**

### 3.1 用户与权限模块

#### sys_user（用户表）

```sql
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '登录账号（手机号/邮箱）',
  `password` VARCHAR(100) NOT NULL COMMENT 'BCrypt加密密码',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `balance` DECIMAL(10,2) DEFAULT '0.00' COMMENT '钱包余额',
  `role` ENUM('admin','user','rider') NOT NULL DEFAULT 'user' COMMENT '角色',
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
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
```

#### sys_menu（菜单表）

```sql
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
```

#### sys_role_menu（角色菜单关联表）

```sql
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
```

### 3.2 门店与商品模块

#### store（门店表）

```sql
CREATE TABLE `store` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '门店ID',
  `name` VARCHAR(100) NOT NULL COMMENT '门店名称',
  `address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `lng` DECIMAL(10,7) NOT NULL COMMENT '经度',
  `lat` DECIMAL(10,7) NOT NULL COMMENT '纬度',
  `cover` VARCHAR(255) DEFAULT NULL COMMENT '门店封面图URL',
  `business_hours` VARCHAR(50) DEFAULT NULL COMMENT '营业时间',
  `min_delivery_amount` DECIMAL(10,2) DEFAULT '0.00' COMMENT '起送金额',
  `delivery_fee` DECIMAL(10,2) DEFAULT '0.00' COMMENT '配送费',
  `status` TINYINT NOT NULL DEFAULT '1' COMMENT '状态：1-营业中 0-休息中',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_location` (`lng`, `lat`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门店表';
```

#### product_category（商品分类表）

```sql
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
```

#### product（商品表）

```sql
CREATE TABLE `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `store_id` BIGINT NOT NULL COMMENT '所属门店ID',
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '商品描述',
  `cover` VARCHAR(255) DEFAULT NULL COMMENT '商品封面图URL',
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
```

#### product_spec（商品规格表）

```sql
CREATE TABLE `product_spec` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '规格ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `spec_name` VARCHAR(50) NOT NULL COMMENT '规格名称（如 大杯/中杯/小杯）',
  `price` DECIMAL(10,2) NOT NULL COMMENT '规格价格',
  `stock` INT NOT NULL DEFAULT '0' COMMENT '库存数量',
  `status` TINYINT NOT NULL DEFAULT '1' COMMENT '状态：1-可用 0-不可用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_spec_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品规格表';
```

### 3.3 订单与配送模块

#### orders（订单主表）

```sql
CREATE TABLE `orders` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号（唯一）',
  `user_id` BIGINT NOT NULL COMMENT '下单用户ID',
  `store_id` BIGINT NOT NULL COMMENT '门店ID',
  `rider_id` BIGINT DEFAULT NULL COMMENT '配送骑手ID',
  `type` ENUM('takeaway','delivery') NOT NULL COMMENT '订单类型：takeaway-自取 delivery-外送',
  `status` TINYINT NOT NULL DEFAULT '0' COMMENT '订单状态：0-待支付 1-待接单 2-已接单 3-已取餐 4-配送中 5-已完成 6-已取消',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '商品总价',
  `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
  `delivery_fee` DECIMAL(10,2) DEFAULT '0.00' COMMENT '配送费',
  `discount_amount` DECIMAL(10,2) DEFAULT '0.00' COMMENT '优惠金额',
  `address` JSON DEFAULT NULL COMMENT '收货地址信息（外送时）',
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
  KEY `idx_type_status` (`type`, `status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_pay_time` (`pay_time`),
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_order_store` FOREIGN KEY (`store_id`) REFERENCES `store` (`id`),
  CONSTRAINT `fk_order_rider` FOREIGN KEY (`rider_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单主表';
```

#### order_item（订单商品明细表）

```sql
CREATE TABLE `order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `spec_id` BIGINT NOT NULL COMMENT '规格ID',
  `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称（冗余）',
  `spec_name` VARCHAR(50) NOT NULL COMMENT '规格名称（冗余）',
  `price` DECIMAL(10,2) NOT NULL COMMENT '购买时单价',
  `quantity` INT NOT NULL COMMENT '购买数量',
  `total` DECIMAL(10,2) NOT NULL COMMENT '小计金额',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`),
  CONSTRAINT `fk_item_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `fk_item_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
  CONSTRAINT `fk_item_spec` FOREIGN KEY (`spec_id`) REFERENCES `product_spec` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单商品明细表';
```

#### order_status_log（订单状态流转日志表）

```sql
CREATE TABLE `order_status_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `from_status` TINYINT DEFAULT NULL COMMENT '原状态',
  `to_status` TINYINT NOT NULL COMMENT '目标状态',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `operator_type` TINYINT NOT NULL DEFAULT '0' COMMENT '操作人类型：0-系统 1-用户 2-骑手 3-管理员',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '操作备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_status_log_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单状态流转日志表';
```

#### rider_location（骑手实时位置表）

```sql
CREATE TABLE `rider_location` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '位置记录ID',
  `rider_id` BIGINT NOT NULL COMMENT '骑手ID',
  `order_id` BIGINT DEFAULT NULL COMMENT '当前配送订单ID',
  `lng` DECIMAL(10,7) NOT NULL COMMENT '经度',
  `lat` DECIMAL(10,7) NOT NULL COMMENT '纬度',
  `accuracy` FLOAT DEFAULT NULL COMMENT '定位精度（米）',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '位置更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rider_id` (`rider_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_update_time` (`update_time`),
  CONSTRAINT `fk_location_rider` FOREIGN KEY (`rider_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_location_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='骑手实时位置表';
```

### 3.4 购物车模块

#### cart（购物车表）

```sql
CREATE TABLE `cart` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `store_id` BIGINT NOT NULL COMMENT '门店ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `spec_id` BIGINT NOT NULL COMMENT '规格ID',
  `quantity` INT NOT NULL DEFAULT '1' COMMENT '数量',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_store_product_spec` (`user_id`, `store_id`, `product_id`, `spec_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_store_id` (`store_id`),
  CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_cart_store` FOREIGN KEY (`store_id`) REFERENCES `store` (`id`),
  CONSTRAINT `fk_cart_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
  CONSTRAINT `fk_cart_spec` FOREIGN KEY (`spec_id`) REFERENCES `product_spec` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';
```

### 3.5 推荐与评价模块

#### user_behavior（用户行为表）

```sql
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
```

#### evaluation（评价表）

```sql
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
```

### 3.6 系统配置与日志模块

#### sys_config（系统配置表）

```sql
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
```

#### sys_login_log（登录日志表）

```sql
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
```

#### sys_oper_log（操作日志表）

```sql
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
```

### 3.7 种子数据（初始化SQL）

```sql
-- 插入系统配置
INSERT INTO `sys_config` (`config_key`, `config_value`, `remark`) VALUES
('deepseek.api.key', '', 'DeepSeek API Key'),
('deepseek.api.url', 'https://api.deepseek.com/v1/chat/completions', 'DeepSeek API地址'),
('deepseek.model', 'deepseek-chat', 'DeepSeek模型名称'),
('tencent.map.key', '', '腾讯地图Key'),
('system.name', 'Coffee Order', '系统名称'),
('system.version', '1.0.0', '系统版本');

-- 插入管理员账号（密码：123456，BCrypt加密后）
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `role`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '系统管理员', 'admin', 1);

-- 插入测试骑手账号
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `phone`, `role`, `status`) VALUES
('rider', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '测试骑手', '13800138001', 'rider', 1);

-- 插入测试用户账号
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `phone`, `balance`, `role`, `status`) VALUES
('user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '测试用户', '13800138002', 1000.00, 'user', 1);

-- 插入测试门店
INSERT INTO `store` (`name`, `address`, `phone`, `lng`, `lat`, `cover`, `business_hours`, `min_delivery_amount`, `delivery_fee`, `status`) VALUES
('星巴克咖啡（科技园店）', '深圳市南山区科技园南区A栋1楼', '0755-88888888', 113.9433, 22.5482, 'https://example.com/store1.jpg', '07:00-22:00', 20.00, 3.00, 1),
('瑞幸咖啡（软件园店）', '深圳市南山区软件园B栋大堂', '0755-66666666', 113.9520, 22.5420, 'https://example.com/store2.jpg', '06:30-22:30', 15.00, 2.00, 1);

-- 插入菜单数据（管理后台）
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `title`, `icon`, `order_num`, `menu_type`, `perms`, `status`) VALUES
(0, 'Dashboard', '/dashboard', 'dashboard/index', '数据概览', 'DataLine', 1, 2, 'dashboard:view', 1),
(0, 'Store', '/store', NULL, '门店管理', 'Shop', 2, 1, NULL, 1),
(0, 'Product', '/product', NULL, '商品管理', 'Goods', 3, 1, NULL, 1),
(0, 'Order', '/order', NULL, '订单管理', 'List', 4, 1, NULL, 1),
(0, 'Rider', '/rider', NULL, '骑手管理', 'Bicycle', 5, 1, NULL, 1),
(0, 'User', '/user', NULL, '用户管理', 'User', 6, 1, NULL, 1),
(0, 'System', '/system', NULL, '系统管理', 'Setting', 7, 1, NULL, 1);

-- 角色菜单关联
INSERT INTO `sys_role_menu` (`role`, `menu_id`) VALUES
('admin', 1), ('admin', 2), ('admin', 3), ('admin', 4), ('admin', 5), ('admin', 6), ('admin', 7);
```


---

## 4. 后端接口设计（完整API Spec）

### 4.1 统一响应格式

```json
{
  "code": 200,
  "msg": "success",
  "data": { ... },
  "timestamp": 1720953600000
}
```

**错误码定义**：
| 错误码 | 含义 | 说明 |
|--------|------|------|
| 200 | 成功 | 请求处理成功 |
| 400 | 请求参数错误 | 参数校验失败或格式错误 |
| 401 | 未认证 | Token缺失或过期 |
| 403 | 无权限 | 角色权限不足 |
| 404 | 资源不存在 | 请求的资源未找到 |
| 409 | 业务冲突 | 如库存不足、余额不足 |
| 429 | 请求过于频繁 | 限流触发 |
| 500 | 服务器内部错误 | 系统异常 |

### 4.2 认证模块（/api/auth）

#### 4.2.1 发送邮箱验证码

```
POST /api/auth/captcha
Content-Type: application/json

请求体：
{
  "email": "user@qq.com",
  "type": "login"
}

响应：
{
  "code": 200,
  "msg": "验证码已发送",
  "data": null
}

业务规则：
1. 同一邮箱60秒内只能发送一次验证码
2. 验证码为6位数字，有效期5分钟
3. 验证码存入Redis：key = "captcha:{type}:{email}", TTL = 300秒
4. 使用QQ邮箱SMTP发送，邮件标题："Coffee Order 验证码"
5. 邮件正文模板："您好，您的验证码是：{code}，有效期5分钟。如非本人操作，请忽略此邮件。"

异常场景：
- 邮箱格式错误 -> 400
- 发送过于频繁 -> 429，msg="发送过于频繁，请{remain}秒后重试"
- 邮箱SMTP配置错误 -> 500
```

#### 4.2.2 用户注册

```
POST /api/auth/register
Content-Type: application/json

请求体：
{
  "username": "user@qq.com",
  "password": "123456",
  "nickname": "咖啡爱好者",
  "email": "user@qq.com",
  "captcha": "123456",
  "phone": "13800138000"
}

响应：
{
  "code": 200,
  "msg": "注册成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 7200,
    "user": {
      "id": 1,
      "username": "user@qq.com",
      "nickname": "咖啡爱好者",
      "avatar": null,
      "role": "user",
      "balance": 0.00
    }
  }
}

业务规则：
1. username和email必须一致（当前版本使用邮箱作为账号）
2. 密码使用BCrypt加密存储（强度10）
3. 验证码校验通过后立即删除Redis中的验证码
4. 注册成功后自动登录，返回JWT Token
5. Token有效期2小时，Refresh Token有效期7天
6. 新用户默认角色为user，余额为0

异常场景：
- 用户名已存在 -> 409
- 验证码错误或过期 -> 400
- 密码强度不足 -> 400
```

#### 4.2.3 用户登录

```
POST /api/auth/login
Content-Type: application/json

请求体（方式一：密码登录）：
{
  "username": "user@qq.com",
  "password": "123456"
}

请求体（方式二：验证码登录）：
{
  "username": "user@qq.com",
  "captcha": "123456",
  "loginType": "captcha"
}

响应：
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 7200,
    "user": {
      "id": 1,
      "username": "user@qq.com",
      "nickname": "咖啡爱好者",
      "avatar": null,
      "role": "user",
      "balance": 100.00
    }
  }
}

业务规则：
1. 密码登录：校验BCrypt密码，失败3次后锁定账号3分钟
2. 验证码登录：校验Redis中的验证码
3. 登录成功后更新 last_login_time 和 last_login_ip
4. 记录登录日志到 sys_login_log
5. 返回Token格式：Bearer {token}

异常场景：
- 账号不存在 -> 401
- 密码错误 -> 401，msg="密码错误，还剩{remain}次机会"
- 账号被锁定 -> 403，msg="账号已锁定，请{remain}分钟后重试"
- 账号被禁用 -> 403
```

#### 4.2.4 Token刷新

```
POST /api/auth/refresh
Content-Type: application/json
Authorization: Bearer {refreshToken}

响应：
{
  "code": 200,
  "msg": "刷新成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 7200
  }
}
```

#### 4.2.5 获取当前用户信息

```
GET /api/auth/info
Authorization: Bearer {token}

响应：
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "username": "user@qq.com",
    "nickname": "咖啡爱好者",
    "avatar": "https://example.com/avatar.jpg",
    "phone": "13800138000",
    "email": "user@qq.com",
    "balance": 100.00,
    "role": "user",
    "createTime": "2024-01-01 10:00:00"
  }
}
```

#### 4.2.6 修改密码

```
PUT /api/auth/password
Authorization: Bearer {token}
Content-Type: application/json

请求体：
{
  "oldPassword": "123456",
  "newPassword": "654321",
  "confirmPassword": "654321"
}

业务规则：
1. 新密码不能与旧密码相同
2. 新密码长度6-20字符
3. 需要重新登录（使旧Token失效）
```

### 4.3 门店模块（/api/store）

#### 4.3.1 获取附近门店列表

```
GET /api/store/nearby?lng=113.9433&lat=22.5482&radius=5000&keyword=&page=1&size=10

请求参数：
- lng: 必填，经度
- lat: 必填，纬度
- radius: 可选，搜索半径（米），默认5000，最大20000
- keyword: 可选，门店名称关键词
- page: 可选，页码，默认1
- size: 可选，每页数量，默认10，最大50

响应：
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 15,
    "pages": 2,
    "current": 1,
    "size": 10,
    "records": [
      {
        "id": 1,
        "name": "星巴克咖啡（科技园店）",
        "address": "深圳市南山区科技园南区A栋1楼",
        "phone": "0755-88888888",
        "lng": 113.9433,
        "lat": 22.5482,
        "cover": "https://example.com/store1.jpg",
        "businessHours": "07:00-22:00",
        "minDeliveryAmount": 20.00,
        "deliveryFee": 3.00,
        "status": 1,
        "distance": 520,
        "distanceText": "520m"
      }
    ]
  }
}

业务规则：
1. 使用MySQL空间函数或Haversine公式计算距离
2. 按距离升序排列
3. 只返回status=1的门店
4. 距离计算SQL示例：
   SELECT *, (6371000 * acos(cos(radians(?)) * cos(radians(lat)) * cos(radians(lng) - radians(?)) + sin(radians(?)) * sin(radians(lat)))) AS distance
   FROM store WHERE status = 1 HAVING distance <= ? ORDER BY distance LIMIT ?, ?
5. 结果缓存到Redis，key = "store:nearby:{lng}:{lat}:{radius}:{keyword}:{page}", TTL = 5分钟
```

#### 4.3.2 获取门店详情

```
GET /api/store/{id}

响应：
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "name": "星巴克咖啡（科技园店）",
    "address": "深圳市南山区科技园南区A栋1楼",
    "phone": "0755-88888888",
    "lng": 113.9433,
    "lat": 22.5482,
    "cover": "https://example.com/store1.jpg",
    "businessHours": "07:00-22:00",
    "minDeliveryAmount": 20.00,
    "deliveryFee": 3.00,
    "status": 1,
    "categories": [
      {
        "id": 1,
        "name": "经典咖啡",
        "products": [
          {
            "id": 1,
            "name": "拿铁",
            "description": "浓缩咖啡与牛奶的经典融合",
            "cover": "https://example.com/latte.jpg",
            "sales": 1280,
            "rating": 4.8,
            "specs": [
              {
                "id": 1,
                "specName": "中杯",
                "price": 28.00,
                "stock": 100
              },
              {
                "id": 2,
                "specName": "大杯",
                "price": 32.00,
                "stock": 80
              }
            ]
          }
        ]
      }
    ]
  }
}

业务规则：
1. 返回门店基本信息 + 所有分类及分类下的商品
2. 只返回status=1的分类和商品
3. 商品按sort_order升序排列
4. 结果缓存到Redis，key = "store:detail:{id}", TTL = 10分钟
```

## 4.4 商品模块（/api/product）

#### 4.4.1 获取分类列表

```
GET /api/product/categories?storeId=1

请求参数：
- storeId: 必填，门店ID

响应：
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "storeId": 1,
      "name": "经典咖啡",
      "sortOrder": 1
    }
  ]
}

业务规则：
1. 根据 storeId 查询 status=1 的分类，按 sort_order 升序排列。
2. 结果缓存至 Redis：key = "product:categories:{storeId}", TTL = 30 分钟。
3. 当门店无有效分类时返回空数组。
```

#### 4.4.2 商品列表

```
GET /api/product/list?storeId=1&categoryId=&keyword=&page=1&size=20

请求参数：
- storeId: 必填，门店ID
- categoryId: 可选，分类ID，为空则查询所有分类
- keyword: 可选，商品名称模糊搜索
- page: 当前页码，默认1
- size: 每页数量，默认20，最大50

响应：
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 50,
    "pages": 3,
    "current": 1,
    "size": 20,
    "records": [
      {
        "id": 1,
        "storeId": 1,
        "categoryId": 1,
        "categoryName": "经典咖啡",
        "name": "拿铁",
        "description": "浓缩咖啡与牛奶的经典融合",
        "cover": "https://example.com/latte.jpg",
        "sales": 1280,
        "rating": 4.8,
        "ratingCount": 256,
        "sortOrder": 1,
        "specs": [
          {
            "id": 1,
            "specName": "中杯",
            "price": 28.00,
            "stock": 100,
            "status": 1
          },
          {
            "id": 2,
            "specName": "大杯",
            "price": 32.00,
            "stock": 80,
            "status": 1
          }
        ]
      }
    ]
  }
}

业务规则：
1. 查询 product 表，条件：store_id = ? AND status = 1，可选 category_id 和 name LIKE '%keyword%'。
2. 联表查询 product_spec 获取规格信息（仅 status=1 的规格）。
3. 排序规则：按 sales DESC, sort_order ASC 排列。
4. 分页使用 MyBatis-Plus 的 Page 对象。
5. 结果不缓存（考虑实时库存）。
```

#### 4.4.3 商品详情

```
GET /api/product/{id}

响应：
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "storeId": 1,
    "storeName": "星巴克咖啡（科技园店）",
    "categoryId": 1,
    "categoryName": "经典咖啡",
    "name": "拿铁",
    "description": "浓缩咖啡与牛奶的经典融合",
    "cover": "https://example.com/latte.jpg",
    "sales": 1280,
    "rating": 4.8,
    "ratingCount": 256,
    "sortOrder": 1,
    "specs": [...],  // 同列表中的规格结构
    "recentEvaluations": [  // 最近3条有效评价
      {
        "id": 10,
        "userId": 2,
        "nickname": "用户***2",
        "avatar": null,
        "score": 5,
        "content": "很好喝，推荐！",
        "images": [],
        "createTime": "2024-06-01 10:00:00"
      }
    ]
  }
}

业务规则：
1. 根据 id 查询商品，同时查出所属门店名称、分类名称。
2. 关联查询 evaluation 表，条件：product_id = ? AND status = 1 AND content IS NOT NULL，按 create_time DESC 取3条。
3. 用户昵称做脱敏处理（仅显示首字符和末尾字符，中间用***代替）。
4. 记录用户行为：写入 user_behavior 表，type = 'view'，如果已存在则 count+1。
```

---

## 4.5 购物车模块（/api/cart）

> 所有接口均需携带 `Authorization: Bearer {token}`，用户只能操作自己的购物车。

#### 4.5.1 获取购物车

```
GET /api/cart

响应：
{
  "code": 200,
  "msg": "success",
  "data": {
    "storeId": 1,
    "storeName": "星巴克咖啡（科技园店）",
    "totalCount": 3,
    "totalAmount": 96.00,
    "items": [
      {
        "cartId": 1,
        "productId": 1,
        "productName": "拿铁",
        "cover": "https://...",
        "specId": 1,
        "specName": "中杯",
        "price": 28.00,
        "quantity": 2,
        "subtotal": 56.00,
        "stock": 100
      }
    ]
  }
}

业务规则：
1. 根据当前登录用户 id 查询 cart 表，关联 product 和 product_spec，获取实时价格和库存。
2. 校验门店一致性：购物车内所有商品必须属于同一门店，如果包含不同门店，则清空购物车并返回空（提示用户确认门店）。
3. 金额计算：各条目 subtotal = price * quantity，totalAmount 为所有 subtotal 之和。
4. 当商品下架或规格停用时，标记为不可购买，但仍展示给用户。
```

#### 4.5.2 添加至购物车

```
POST /api/cart
Content-Type: application/json

请求体：
{
  "productId": 1,
  "specId": 1,
  "storeId": 1,
  "quantity": 1
}

响应：
{
  "code": 200,
  "msg": "添加成功",
  "data": null
}

业务规则：
1. 校验商品、规格是否存在且状态为启用。
2. 校验库存：quantity <= stock，否则返回 409，提示"库存不足"。
3. 校验门店一致性：若购物车已有商品，确保新商品 storeId 与已有的一致，否则提示"请先清空购物车再添加其他门店商品"。
4. 存在则更新 quantity（叠加），不存在则新增记录。
5. 记录用户行为：type = 'cart'。
```

#### 4.5.3 修改购物车商品数量

```
PUT /api/cart/{cartId}
Content-Type: application/json

请求体：
{
  "quantity": 3
}

业务规则：
1. 校验 cartId 属于当前用户。
2. 校验库存：新 quantity <= stock，否则返回 409。
3. 如果 quantity <= 0，则视为删除该条目。
```

#### 4.5.4 删除购物车条目

```
DELETE /api/cart/{cartId}
```

#### 4.5.5 清空购物车

```
DELETE /api/cart/clear
```
---

## 4.6 订单模块（/api/order）

#### 4.6.1 创建订单

```
POST /api/order
Content-Type: application/json

请求体：
{
  "storeId": 1,
  "type": "takeaway",
  "remark": "少冰",
  "address": {
    "name": "张三",
    "phone": "13800138000",
    "address": "深圳市南山区科技园A栋10楼",
    "lng": 113.9450,
    "lat": 22.5500
  },
  "couponId": null
}

响应：
{
  "code": 200,
  "msg": "订单创建成功",
  "data": {
    "orderId": 100,
    "orderNo": "CO20240714153012456",
    "totalAmount": 96.00,
    "deliveryFee": 0.00,
    "payAmount": 96.00,
    "expireTime": "2024-07-14 15:35:12"  // 5分钟后
  }
}

业务规则：
1. 订单号规则：`CO` + `yyyyMMddHHmmss` + 3位随机数字，确保唯一性（使用 Redis 分布式锁防重）。
2. 从购物车获取当前用户在该门店的商品，校验购物车非空。
3. 校验商品库存：逐项检查 quantity <= stock，否则返回 409 并列出库存不足商品。
4. 计算金额：商品总价 totalAmount、配送费 deliveryFee（自取为0）、优惠金额 discountAmount、实付金额 payAmount = totalAmount + deliveryFee - discountAmount。
5. 订单状态 status = 0（待支付），记录创建时间，设置支付超时时间（5分钟后）。
6. 执行事务：
   - 创建 orders 记录
   - 批量插入 order_item 明细
   - 更新商品销量 sales 字段
   - 清空购物车
   - 记录行为 user_behavior（type='order', count 对应购买数量）
   - 写入 order_status_log（from_status=null, to_status=0）
7. 支付超时由定时任务自动取消（见第5节）。
```

#### 4.6.2 订单支付（余额支付）

```
POST /api/order/{orderId}/pay
Content-Type: application/json

请求体：
{
  "payPassword": null  // 暂不实现
}

业务规则：
1. 校验订单 status == 0（待支付），否则返回 400。
2. 检查支付是否超时，超时则更新状态为 6（已取消）并返回 409。
3. 判断用户余额是否 >= payAmount，不足返回 409。
4. 执行事务（用乐观锁或悲观锁防并发）：
   - 扣减用户余额：update sys_user set balance = balance - ? where id = ? and balance >= ?
   - 更新订单：status = 1（待接单），pay_time = now()
   - 记录订单状态日志
5. 记录用户行为 score 字段暂时不写（待评价后更新）。
6. 返回支付成功。
```

#### 4.6.3 订单列表

```
GET /api/order/list?status=&type=&page=1&size=10

响应 data.records 包含：
- orderId, orderNo, storeName, type, status, totalAmount, payAmount, createTime
- 订单状态文本 statusText
- 商品简要信息（第一件商品名称 + 规格 + 数量，如超过1件则显示"等N件"）
```

业务规则：
1. 根据当前用户 role 过滤：
   - user：只看自己订单
   - rider：待接单和已指派给自己的订单
   - admin：可查看所有
2. status 参数为空则查询所有状态，支持多个状态逗号分隔。
3. type 为空则查询所有类型。
4. 按 create_time DESC 排序。
5. 结果不缓存。

#### 4.6.4 订单详情

```
GET /api/order/{orderId}

响应 data 包含：
- 订单基本信息（同上）
- items[]：订单明细
- statusTimeline[]：从 order_status_log 组装，每个节点包含 status, statusText, time, remark
- riderLocation（仅配送中状态）：骑手最新位置 lng, lat, updateTime
```

#### 4.6.5 取消订单

```
PUT /api/order/{orderId}/cancel
Content-Type: application/json

请求体：
{
  "reason": "不想喝了"
}

业务规则：
1. 校验订单状态：只有 status = 0（待支付）或 status = 1（待接单）可取消。
2. 执行事务：
   - 更新订单 status = 6，cancel_time = now()，cancel_reason = reason
   - 恢复库存：逐项 update product_spec set stock = stock + item.quantity where id = specId
   - 退回余额：若已支付，update sys_user set balance = balance + payAmount
   - 记录状态日志
```

#### 4.6.6 确认收货

```
PUT /api/order/{orderId}/confirm

业务规则：
1. 仅 status = 4（配送中）或 status = 3（已取餐，自取订单）可确认。
2. 更新 status = 5，complete_time = now()
3. 记录日志
```

---

## 4.7 骑手模块（/api/rider）

所有接口需要 rider 角色权限。

#### 4.7.1 待接单列表

```
GET /api/rider/orders/pending?page=1&size=10

业务规则：
1. 查询 orders 表 status = 1（待接单）的订单。
2. 可按门店筛选（后续版本接入位置），当前返回所有待接单。
3. 结果包含：orderId, orderNo, storeName, storeAddress, createTime, items[0].productName 等简要信息。
```

#### 4.7.2 接单

```
POST /api/rider/orders/{orderId}/accept

业务规则：
1. 使用 Redis 分布式锁（key = "order:accept:{orderId}"）防止多个骑手同时接单。
2. 校验订单 status == 1，如果已被接单（status != 1）返回 409 "订单已被其他骑手抢走"。
3. 更新 orders 表：rider_id = 当前骑手ID, status = 2（已接单）, accept_time = now()。
4. 记录状态日志。
5. 释放锁。
```

#### 4.7.3 上报位置

```
POST /api/rider/location
Content-Type: application/json

请求体：
{
  "orderId": 100,
  "lng": 113.9450,
  "lat": 22.5500,
  "accuracy": 15.5
}

业务规则：
1. 更新 rider_location 表（REPLACE 或 INSERT ON DUPLICATE KEY UPDATE），更新 lng, lat, accuracy, order_id。
2. 同时写入 Redis：key = "rider:location:{riderId}", value = JSON，TTL = 120秒，用于快速查询。
3. orderId 为空表示骑手空闲状态。
```

#### 4.7.4 取餐

```
POST /api/rider/orders/{orderId}/pickup

业务规则：
1. 校验订单状态 == 2（已接单）且 rider_id == 当前骑手。
2. 更新订单 status = 3（已取餐），pickup_time = now()。
```

#### 4.7.5 确认送达

```
POST /api/rider/orders/{orderId}/deliver

业务规则：
1. 校验订单状态 == 3（已取餐）或 == 4（配送中），且 rider_id == 当前骑手。
2. 更新订单 status = 5（已完成），complete_time = now()。
3. 可选：自动触发用户确认收货操作。
```

#### 4.7.6 我的配送任务

```
GET /api/rider/orders/tasks

业务规则：查询当前骑手未完成的订单（status in 2,3,4），按 accept_time 倒序。
```

---

## 4.8 推荐模块（/api/recommend）

#### 4.8.1 获取商品推荐

```
GET /api/recommend?storeId=1&size=10

响应：
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "productId": 5,
      "productName": "冰美式",
      "cover": "...",
      "score": 0.89,
      "reason": "与你相似的用户也喜欢"
    }
  ]
}

业务规则：
1. 首次请求时触发协同过滤计算，结果缓存至 Redis：key = "recommend:user:{userId}:store:{storeId}", TTL = 6小时。
2. 冷启动处理：新用户或无足够行为数据时，返回该门店销量最高的10个商品（reason="门店热销"）。
3. 推荐数量默认10，最大20。
```

---

## 4.9 AI对话模块（/api/ai）

#### 4.9.1 智能点单对话

```
POST /api/ai/chat
Content-Type: application/json

请求体：
{
  "message": "我想喝一杯中杯拿铁，少冰",
  "storeId": 1,
  "history": []  // 可选，历史对话上下文
}

响应：
{
  "code": 200,
  "msg": "success",
  "data": {
    "reply": "好的，已经为您将中杯拿铁（少冰）加入购物车。请问还需要其他吗？",
    "action": {
      "type": "add_cart",
      "params": {
        "productId": 1,
        "specId": 1,
        "quantity": 1,
        "remark": "少冰"
      }
    }
  }
}

业务规则：
1. 请求消息发送至 DeepSeek API，携带预设的 System Prompt（定义咖啡店助手角色、门店菜单信息、意图分类）。
2. 解析 AI 返回的意图和参数，支持的操作：添加购物车、修改数量、清空购物车、下单、查询订单、推荐商品。
3. 如果 AI 调用失败或解析失败，降级为本地规则匹配：使用关键词 + 正则表达式提取意图。
4. 结果缓存同用户同内容的请求不重复请求AI（Redis key: "ai:chat:{md5(message+storeId)}", TTL=1h）。
```

#### 4.9.2 获取推荐话术

```
GET /api/ai/suggestions

响应：返回一组常用的引导语，如"来一杯提神醒脑的冰美式"、"热销推荐"等。
```

---

## 4.10 管理后台模块（/api/admin）

> 所有接口需要 `admin` 角色或对应权限标识。

#### 4.10.1 数据概览

```
GET /api/admin/dashboard

响应：
{
  "code": 200,
  "msg": "success",
  "data": {
    "today": {
      "orderCount": 128,
      "revenue": 3200.50,
      "newUser": 12,
      "riderOnline": 5
    },
    "total": {
      "orderCount": 35820,
      "revenue": 895000.00,
      "userCount": 5200,
      "riderCount": 25
    },
    "chartData": {
      "recentWeekOrder": [  // 近7天每日订单量
        {"date": "2024-07-08", "count": 45},
        ...
      ],
      "categorySales": [  // 各分类销量占比
        {"name": "经典咖啡", "value": 60},
        {"name": "冷萃系列", "value": 25},
        ...
      ]
    }
  }
}

业务规则：
1. 今日数据从 orders 和 sys_user 统计，注意日期范围。
2. 累计数据直接 count 或 sum。
3. 图表数据由前端 ECharts 渲染，这里提供原始数据。
4. 使用 Redis 缓存 dashboard 数据，TTL=1分钟，避免频繁查库。
```

#### 4.10.2 门店管理 CRUD

```
GET    /api/admin/stores?page=1&size=20&name=关键词
POST   /api/admin/stores
PUT    /api/admin/stores/{id}
DELETE /api/admin/stores/{id}
```

#### 4.10.3 商品管理 CRUD

```
GET    /api/admin/products?storeId=&categoryId=&keyword=&page=1&size=20
POST   /api/admin/products  (含规格列表)
PUT    /api/admin/products/{id}
DELETE /api/admin/products/{id}
```

- 商品创建/编辑时需要同时维护 product_spec 表（规格增删改）。

#### 4.10.4 订单管理

```
GET    /api/admin/orders?status=&type=&storeId=&orderNo=&page=1&size=20
GET    /api/admin/orders/{id}
POST   /api/admin/orders/{id}/assign  { "riderId": 5 }  // 手动指派骑手
```

- 手动指派时校验骑手是否存在、订单状态是否可指派（如待接单状态）。

#### 4.10.5 骑手管理

```
GET    /api/admin/riders?keyword=&page=1&size=20
POST   /api/admin/riders  // 新增骑手账号
PUT    /api/admin/riders/{id}
GET    /api/admin/riders/{id}/track  // 实时轨迹（从Redis获取）
```

#### 4.10.6 用户管理

```
GET    /api/admin/users?keyword=&role=&page=1&size=20
PUT    /api/admin/users/{id}  // 编辑昵称、状态等
POST   /api/admin/users/{id}/recharge  { "amount": 100 }  // 管理员充值
```

#### 4.10.7 菜单与权限管理

```
GET    /api/admin/menus  // 树形结构
POST   /api/admin/menus
PUT    /api/admin/menus/{id}
DELETE /api/admin/menus/{id}
GET    /api/admin/roles/{role}/menus
PUT    /api/admin/roles/{role}/menus  { "menuIds": [1,2,3] }
```

#### 4.10.8 操作日志查询

```
GET /api/admin/logs/operation?userId=&businessType=&page=1&size=20
```

#### 4.10.9 AI 一键批量开店

```
POST /api/admin/ai/store/batch
Content-Type: application/json

请求体：
{
  "city": "深圳",
  "district": "南山区",
  "brand": "Coffee Order",
  "count": 3
}

业务规则：
1. 调用 DeepSeek API，让 AI 根据城市区域生成合理门店信息（名称、地址、经纬度范围、电话）。
2. 后端解析 AI 返回的 JSON 数组，批量插入 store 表。
3. 返回创建结果，包含成功/失败列表。
```

---

## 5. 核心算法

### 5.1 User-Based 协同过滤推荐算法

**余弦相似度公式**：

\[
\text{sim}(u,v) = \frac{\sum_{i} (r_{ui} \cdot r_{vi})}{\sqrt{\sum_{i} r_{ui}^2} \sqrt{\sum_{i} r_{vi}^2}}
\]

其中 \( r_{ui} \) 表示用户 u 对商品 i 的兴趣得分（由行为次数加权计算）。

**Java 伪代码**：

```java
public List<ProductDTO> recommend(Long userId, Long storeId, int size) {
    // 1. 获取目标用户的行为向量（score = view*1 + cart*2 + order*3 + favorite*5）
    Map<Long, Double> targetVector = getUserVector(userId);
    if (targetVector.isEmpty()) return getHotProducts(storeId, size); // 冷启动

    // 2. 找出与该用户有过共同行为的其他用户（排除自己）
    List<Long> neighborIds = findNeighborCandidates(userId, targetVector.keySet());
    
    // 3. 计算相似度并取 TopK 近邻
    PriorityQueue<Neighbor> topK = new PriorityQueue<>(K, Comparator.comparingDouble(Neighbor::getSimilarity));
    for (Long neighborId : neighborIds) {
        Map<Long, Double> neighborVector = getUserVector(neighborId);
        double sim = cosineSimilarity(targetVector, neighborVector);
        topK.offer(new Neighbor(neighborId, sim));
        if (topK.size() > K) topK.poll();
    }
    
    // 4. 预测目标用户对未交互商品的兴趣度
    Map<Long, Double> predictions = new HashMap<>();
    Set<Long> interacted = targetVector.keySet();
    for (Neighbor neighbor : topK) {
        Map<Long, Double> neighborVector = getUserVector(neighbor.getUserId());
        for (Map.Entry<Long, Double> entry : neighborVector.entrySet()) {
            Long productId = entry.getKey();
            if (!interacted.contains(productId) && belongsToStore(productId, storeId)) {
                predictions.merge(productId, entry.getValue() * neighbor.getSimilarity(), Double::sum);
            }
        }
    }
    
    // 5. 排序并返回前 size 个商品
    return predictions.entrySet().stream()
        .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
        .limit(size)
        .map(e -> getProductDTO(e.getKey()))
        .collect(Collectors.toList());
}
```

**参数说明**：
- K（近邻数）：默认取 20
- 行为权重：view=1, cart=2, order=3, favorite=5
- 推荐结果数：默认10，最大20

**缓存策略**：结果缓存到 Redis，key = "recommend:user:{userId}:store:{storeId}"，有效期 6 小时。当用户行为更新时（下单/收藏）主动删除对应缓存。

### 5.2 订单状态机

状态枚举与流转规则：

| 状态码 | 状态名称 | 允许的操作 | 下一状态 |
|--------|----------|-----------|----------|
| 0 | 待支付 | 支付, 取消 | 1, 6 |
| 1 | 待接单 | 接单（骑手）, 取消（用户） | 2, 6 |
| 2 | 已接单 | 取餐（骑手）, 取消（骑手申请，暂不实现） | 3 |
| 3 | 已取餐 | 配送中/确认送达(自取), 确认收货(用户) | 4, 5 |
| 4 | 配送中 | 确认送达(骑手), 确认收货(用户) | 5 |
| 5 | 已完成 | 评价 | - |
| 6 | 已取消 | - | - |

状态变更通过 `order_status_log` 记录。骑手取餐后，如果是外送订单状态变为 4（配送中），如果是自取订单状态直接变为 5 或保持 3 等待用户确认。

### 5.3 订单超时自动取消

使用 Spring `@Scheduled` 定时任务，每分钟执行一次：

```java
@Scheduled(cron = "0 * * * * ?")
public void cancelExpiredOrders() {
    // 1. 查询 status = 0 且 create_time + 5分钟 < now() 的订单
    // 2. 批量更新订单状态为 6，cancel_reason = '支付超时自动取消'
    // 3. 恢复库存
    // 4. 记录状态日志
}
```

- 使用 Redis 分布式锁保证集群环境下只有一个节点执行。
- 单次处理订单数限制100条，避免长事务。

---

## 6. 项目目录结构

```
coffee-order/
├── coffee-server/                          # 后端单体服务
│   ├── src/main/java/com/example/coffee/
│   │   ├── CoffeeApplication.java
│   │   ├── config/                         # 配置类
│   │   │   ├── SecurityConfig.java
│   │   │   ├── CorsConfig.java
│   │   │   ├── RedisConfig.java
│   │   │   ├── MyBatisPlusConfig.java
│   │   │   └── DeepSeekConfig.java
│   │   ├── controller/                     # API接口
│   │   │   ├── AuthController.java
│   │   │   ├── StoreController.java
│   │   │   ├── ProductController.java
│   │   │   ├── CartController.java
│   │   │   ├── OrderController.java
│   │   │   ├── RiderController.java
│   │   │   ├── RecommendController.java
│   │   │   ├── AiController.java
│   │   │   └── admin/
│   │   │       ├── DashboardController.java
│   │   │       ├── AdminStoreController.java
│   │   │       ├── AdminProductController.java
│   │   │       ├── AdminOrderController.java
│   │   │       ├── AdminRiderController.java
│   │   │       ├── AdminUserController.java
│   │   │       └── SystemController.java
│   │   ├── service/                        # 业务接口
│   │   │   ├── AuthService.java
│   │   │   ├── StoreService.java
│   │   │   ├── ProductService.java
│   │   │   ├── CartService.java
│   │   │   ├── OrderService.java
│   │   │   ├── RiderService.java
│   │   │   ├── RecommendService.java
│   │   │   ├── AiService.java
│   │   │   ├── UserBehaviorService.java
│   │   │   └── impl/                       # 实现类
│   │   ├── mapper/                         # MyBatis Mapper
│   │   │   ├── SysUserMapper.java
│   │   │   ├── StoreMapper.java
│   │   │   ├── ProductMapper.java
│   │   │   ├── ProductSpecMapper.java
│   │   │   ├── CartMapper.java
│   │   │   ├── OrderMapper.java
│   │   │   ├── OrderItemMapper.java
│   │   │   ├── OrderStatusLogMapper.java
│   │   │   ├── RiderLocationMapper.java
│   │   │   ├── UserBehaviorMapper.java
│   │   │   ├── EvaluationMapper.java
│   │   │   └── ...
│   │   ├── entity/                         # 数据库实体
│   │   │   ├── SysUser.java
│   │   │   ├── Store.java
│   │   │   ├── Product.java
│   │   │   ├── ProductSpec.java
│   │   │   ├── Cart.java
│   │   │   ├── Orders.java
│   │   │   ├── OrderItem.java
│   │   │   ├── OrderStatusLog.java
│   │   │   ├── RiderLocation.java
│   │   │   ├── UserBehavior.java
│   │   │   ├── Evaluation.java
│   │   │   └── ...
│   │   ├── dto/                            # 请求/响应对象
│   │   │   ├── request/
│   │   │   │   ├── LoginReq.java
│   │   │   │   ├── RegisterReq.java
│   │   │   │   ├── CreateOrderReq.java
│   │   │   │   └── ...
│   │   │   └── response/
│   │   │       ├── LoginResp.java
│   │   │       ├── StoreDetailResp.java
│   │   │       └── ...
│   │   ├── vo/                             # 视图对象（用于管理后台展示）
│   │   ├── util/                           # 工具类
│   │   │   ├── JwtUtil.java
│   │   │   ├── RedisUtil.java
│   │   │   ├── GeoUtil.java
│   │   │   ├── CosineSimilarityUtil.java
│   │   │   └── ...
│   │   ├── handler/                        # 全局异常、拦截器等
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── ...
│   │   └── task/                           # 定时任务
│   │       └── OrderCancelTask.java
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── application-dev.yml
│   │   └── mapper/                         # MyBatis XML
│   └── pom.xml
├── coffee-admin/                           # Vue3 管理前端
│   ├── src/
│   │   ├── api/                            # 接口封装
│   │   │   ├── auth.ts
│   │   │   ├── store.ts
│   │   │   ├── order.ts
│   │   │   └── ...
│   │   ├── stores/                         # Pinia 状态
│   │   │   ├── user.ts
│   │   │   └── app.ts
│   │   ├── views/                          # 页面
│   │   │   ├── dashboard/
│   │   │   ├── store/
│   │   │   ├── product/
│   │   │   ├── order/
│   │   │   └── ...
│   │   ├── router/
│   │   └── components/
│   ├── package.json
│   └── vite.config.ts
└── coffee-mp/                              # 微信小程序
    ├── pages/
    │   ├── index/                          # 首页/门店选择
    │   ├── store/                          # 商品点单
    │   ├── cart/                           # 购物车
    │   ├── order/                          # 订单详情
    │   ├── ai/                             # AI对话
    │   └── user/                           # 我的
    ├── components/
    │   ├── product-card/
    │   ├── spec-selector/
    │   └── map-tracker/                   # 地图追踪组件
    ├── utils/
    │   ├── api.ts
    │   ├── util.ts
    │   └── location.ts
    ├── app.json
    ├── app.ts
    └── project.config.json
```

---

## 7. 配置文件

### application.yml

```yaml
spring:
  application:
    name: coffee-order
  profiles:
    active: dev
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8
    default-property-inclusion: non_null
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 20MB

server:
  port: 8080

mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  mapper-locations: classpath:mapper/*.xml
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

jwt:
  secret: CoffeeOrderSecretKey2024MustBeLongEnough
  expiration: 7200          # 2小时
  refresh-expiration: 604800  # 7天

deepseek:
  api-key: ${DEEPSEEK_KEY:your-api-key}
  api-url: https://api.deepseek.com/v4/chat/completions
  model: deepseek-chat
```

### application-dev.yml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/coffee_order?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
  data:
    redis:
      host: localhost
      port: 6379
      password: 
      database: 0
      timeout: 3000ms
      lettuce:
        pool:
          max-active: 16
          max-idle: 8
          min-idle: 2
  mail:
    host: smtp.qq.com
    port: 587
    username: your-email@qq.com
    password: your-auth-code
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true

tencent:
  map:
    key: ${TENCENT_MAP_KEY:your-key}
```

---

## 8. 前端页面设计（关键代码示例）

### 8.1 微信小程序：商品点单页

```html
<!-- pages/store/index.wxml -->
<view class="store-page">
  <!-- 门店信息栏 -->
  <view class="store-header">
    <text class="store-name">{{store.name}}</text>
    <text class="store-address">{{store.address}}</text>
  </view>

  <!-- 分类Tabs -->
  <scroll-view class="category-tabs" scroll-x>
    <view wx:for="{{categories}}" wx:key="id" 
          class="tab-item {{currentCategoryId === item.id ? 'active' : ''}}"
          bindtap="switchCategory" data-id="{{item.id}}">
      {{item.name}}
    </view>
  </scroll-view>

  <!-- 商品列表 -->
  <scroll-view class="product-list" scroll-y>
    <view wx:for="{{products}}" wx:key="id" class="product-card">
      <image src="{{item.cover}}" class="product-img"/>
      <view class="product-info">
        <text class="product-name">{{item.name}}</text>
        <text class="product-desc">{{item.description}}</text>
        <text class="product-sales">月售{{item.sales}}</text>
        <view class="spec-prices">
          <view wx:for="{{item.specs}}" wx:for-item="spec" wx:key="id" class="spec-item">
            <text>{{spec.specName}}: ¥{{spec.price}}</text>
            <button size="mini" bindtap="addToCart" data-product-id="{{item.id}}" data-spec-id="{{spec.id}}">+</button>
          </view>
        </view>
      </view>
    </view>
  </scroll-view>
</view>
```

```typescript
// pages/store/index.ts
Page({
  data: {
    store: {},
    categories: [],
    products: [],
    currentCategoryId: null
  },
  onLoad(options: { storeId: string }) {
    this.fetchStoreDetail(options.storeId);
  },
  async fetchStoreDetail(storeId: string) {
    const resp = await api.get(`/store/${storeId}`);
    if (resp.code === 200) {
      const data = resp.data;
      this.setData({
        store: { id: data.id, name: data.name, address: data.address },
        categories: data.categories.map(c => ({ id: c.id, name: c.name })),
        currentCategoryId: data.categories[0]?.id || null
      });
      this.filterProducts(data.categories);
    }
  },
  filterProducts(categories) {
    const catId = this.data.currentCategoryId;
    const selected = categories.find(c => c.id === catId);
    this.setData({ products: selected ? selected.products : [] });
  },
  switchCategory(e) {
    this.setData({ currentCategoryId: e.currentTarget.dataset.id });
    // 重新请求或从缓存获取该分类商品
    this.loadProductsByCategory(this.data.store.id, e.currentTarget.dataset.id);
  },
  addToCart(e) {
    const { productId, specId } = e.currentTarget.dataset;
    api.post('/cart', { productId, specId, storeId: this.data.store.id, quantity: 1 });
  }
})
```

### 8.2 微信小程序：订单详情（地图追踪）

```html
<!-- pages/order/detail.wxml -->
<view class="order-detail">
  <map 
    id="deliveryMap"
    latitude="{{riderLocation.lat}}"
    longitude="{{riderLocation.lng}}"
    markers="{{markers}}"
    polyline="{{[polyline]}}"
    style="width: 100%; height: 300px;"
  />
  <view class="rider-info">
    <text>骑手 {{riderName}} 正在赶来</text>
    <text>预计送达 {{estimatedTime}}</text>
  </view>
</view>
```

```typescript
// pages/order/detail.ts
Page({
  data: {
    orderId: null,
    riderLocation: { lat: 0, lng: 0 },
    markers: [],
    polyline: { points: [], color: "#0097FF", width: 4 }
  },
  onLoad(options) {
    this.orderId = options.orderId;
    this.fetchOrderDetail();
    this.startPollingRiderLocation();
  },
  async fetchOrderDetail() {
    const resp = await api.get(`/order/${this.orderId}`);
    if (resp.code === 200) {
      const data = resp.data;
      // 设置收货地址、门店位置、骑手位置等标记
      this.setData({
        markers: [
          { id: 1, latitude: data.store.lat, longitude: data.store.lng, iconPath: "/images/store.png" },
          { id: 2, latitude: data.address.lat, longitude: data.address.lng, iconPath: "/images/user.png" },
          { id: 3, latitude: data.riderLocation.lat, longitude: data.riderLocation.lng, iconPath: "/images/rider.png" }
        ]
      });
    }
  },
  startPollingRiderLocation() {
    this.timer = setInterval(async () => {
      const resp = await api.get(`/rider/location?orderId=${this.orderId}`);
      if (resp.code === 200) {
        const { lng, lat } = resp.data;
        this.setData({
          riderLocation: { lng, lat },
          "markers[2].latitude": lat,
          "markers[2].longitude": lng
        });
        // 更新路径
      }
    }, 3000);
  },
  onUnload() {
    clearInterval(this.timer);
  }
})
```

### 8.3 管理后台：订单管理 Vue 组件

```vue
<!-- views/order/index.vue -->
<template>
  <div class="order-container">
    <el-card>
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="订单状态">
          <el-select v-model="queryParams.status" clearable>
            <el-option label="待支付" :value="0" />
            <el-option label="待接单" :value="1" />
            <el-option label="配送中" :value="4" />
            <el-option label="已完成" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单类型">
          <el-select v-model="queryParams.type" clearable>
            <el-option label="自取" value="takeaway" />
            <el-option label="外送" value="delivery" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="orderList" border stripe>
      <el-table-column prop="orderNo" label="订单编号" width="180" />
      <el-table-column prop="storeName" label="门店" />
      <el-table-column label="状态">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ row.statusText }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="金额" />
      <el-table-column prop="createTime" label="下单时间" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button size="small" @click="showDetail(row.id)">详情</el-button>
          <el-button v-if="row.status === 1" size="small" type="primary" @click="assignRider(row)">指派骑手</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="queryParams.page"
      v-model:page-size="queryParams.size"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="handleQuery"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { getOrderList } from '@/api/order'

const queryParams = reactive({ page: 1, size: 10, status: null, type: null })
const orderList = ref([])
const total = ref(0)

const handleQuery = async () => {
  const res = await getOrderList(queryParams)
  orderList.value = res.data.records
  total.value = res.data.total
}

const statusTagType = (status: number) => {
  return status === 0 ? 'warning' : status === 5 ? 'success' : 'info'
}
</script>
```

---

## 9. 安全设计

### 9.1 JWT 认证流程

1. 用户登录成功后，服务端签发 Access Token（有效期2h）和 Refresh Token（有效期7d）。
2. 客户端请求时，在 Header 中添加 `Authorization: Bearer {accessToken}`。
3. 服务端 `JwtAuthenticationFilter` 拦截所有 `/api/**` 请求（排除 `/api/auth/**`），校验 Token 有效性。
4. 过期或解析失败返回 401。
5. 前端拦截器在返回 401 时自动使用 Refresh Token 换取新 Token，若 Refresh Token 也失效则跳转登录页。

### 9.2 方法级权限控制

```java
@RestController
@RequestMapping("/api/admin")
public class AdminOrderController {

    @PreAuthorize("hasRole('admin')") // 仅管理员可访问
    @GetMapping("/orders")
    public Result pageOrders(...) { ... }
}
```

- 权限注解结合 Spring Security 的 `MethodSecurityConfig` 使用。
- 角色字符串与数据库 `role` 字段对应。

### 9.3 密码加密

所有密码存储使用 BCryptPasswordEncoder，强度10。

### 9.4 敏感信息脱敏

- 用户手机号、邮箱在非本人查看时，中间数字替换为 `****`。
- AI 对话日志不记录完整消息内容，仅存储摘要。
- 响应中不输出 password 字段。

---

## 10. 性能优化

### 10.1 Redis 缓存策略

| 缓存对象 | Key 规则 | TTL | 更新策略 |
|----------|---------|-----|---------|
| 门店详情 | `store:detail:{id}` | 10分钟 | 门店信息修改时删除 |
| 商品分类 | `product:categories:{storeId}` | 30分钟 | 分类/商品变更时删除 |
| 附近门店 | `store:nearby:{lng}:{lat}:{radius}:{keyword}:{page}` | 5分钟 | 门店位置/状态变更时模糊删除 |
| 用户推荐 | `recommend:user:{userId}:store:{storeId}` | 6小时 | 用户行为更新时删除 |
| AI 对话 | `ai:chat:{md5(message+storeId)}` | 1小时 | 无需主动删除 |
| 骑手位置 | `rider:location:{riderId}` | 120秒 | 骑手上报时覆盖 |
| 数据概览 | `dashboard:data` | 1分钟 | 定时刷新或查询时懒刷新 |

### 10.2 数据库优化

- 所有外键字段建立索引，如 `store_id`, `user_id`。
- 订单表按 `user_id` 分区（当数据量达到千万级后）。
- 商品列表查询使用覆盖索引：`(status, sales, sort_order, id)`。
- 使用 MyBatis-Plus 的分页插件进行物理分页。

### 10.3 异步处理

- 行为记录（user_behavior）和操作日志写入使用 `@Async` 异步执行，不阻塞主线程。
- 邮件发送验证码使用异步任务线程池。

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.initialize();
        return executor;
    }
}
```

---

## 11. 部署指南

### 11.1 开发环境启动

1. **环境要求**：JDK 21、Maven 3.8+、MySQL 8.0、Redis 5.0+、Node 18+。
2. **初始化数据库**：执行项目中的 SQL 脚本（所有DDL + 种子数据）。
3. **修改配置**：修改 `application-dev.yml` 中的数据库、Redis、邮箱、腾讯地图 API Key、DeepSeek API Key。
4. **启动后端**：
   ```bash
   cd coffee-server
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```
5. **启动管理前端**：
   ```bash
   cd coffee-admin
   npm install
   npm run dev
   ```
6. **小程序**：打开微信开发者工具，导入 `coffee-mp` 目录，修改 `utils/api.ts` 中的 `baseUrl` 为本地后端地址。

### 11.2 生产环境 Nginx 配置示例

```nginx
server {
    listen 80;
    server_name api.coffee-order.com;
    
    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

server {
    listen 80;
    server_name admin.coffee-order.com;
    
    location / {
        root /var/www/coffee-admin/dist;
        try_files $uri $uri/ /index.html;
    }
}
```

---

## 12. 测试策略

### 12.1 单元测试示例

```java
@SpringBootTest
@Transactional  // 测试后回滚数据
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Test
    void testCreateOrder_Success() {
        CreateOrderReq req = new CreateOrderReq();
        req.setStoreId(1L);
        req.setType("takeaway");
        // 模拟已登录用户
        Long userId = 2L;
        // 先添加购物车数据...

        OrderResp resp = orderService.createOrder(req, userId);
        assertNotNull(resp.getOrderId());
        assertNotNull(resp.getOrderNo());
        // 验证订单状态为0
        Orders order = orderMapper.selectById(resp.getOrderId());
        assertEquals(0, order.getStatus());
        // 验证购物车已清空
        List<Cart> carts = cartMapper.selectList(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
        assertTrue(carts.isEmpty());
    }
}
```

### 12.2 接口测试（curl 命令）

```bash
# 1. 登录获取Token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"123456"}'

# 保存返回的token
TOKEN="eyJhbGciOiJIUzI1NiIs..."

# 2. 获取附近门店
curl -X GET "http://localhost:8080/api/store/nearby?lng=113.9433&lat=22.5482&radius=5000" \
  -H "Authorization: Bearer $TOKEN"

# 3. 添加购物车
curl -X POST http://localhost:8080/api/cart \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"specId":1,"storeId":1,"quantity":2}'

# 4. 创建订单
curl -X POST http://localhost:8080/api/order \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"storeId":1,"type":"takeaway"}'
```

---

## 13. FAQ

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| Token 突然失效 | Token 过期或被篡改 | 检查 Token 有效期，调用 refresh 接口刷新 |
| 无法获取附近门店 | 位置权限未开启或坐标未传入 | 确保微信小程序已授权位置，后端 lng/lat 参数正确 |
| 添加购物车提示"门店不一致" | 购物车中已有其他门店商品 | 先调用清空购物车接口，再添加新商品 |
| 支付失败"余额不足" | 用户钱包余额小于订单实付金额 | 提示用户充值或减少商品 |
| AI 对话无响应 | DeepSeek API 不可达或配额耗尽 | 检查 API Key 和网络，系统自动降级为规则匹配 |
| 骑手接单提示"已被抢走" | 并发导致，另一骑手先一步接单 | 正常业务场景，刷新列表获取新订单 |
| 商品不显示 | 商品 status=0 或未关联当前门店 | 检查商品上架状态及所属门店 |
| 图片上传失败 | 文件大小超限或配置错误 | 确认 multipart 配置和存储服务可用性 |

---

## 14. 扩展路线图

### Phase 1（当前 v1.0.0）
- ✅ 用户注册/登录（邮箱+验证码）
- ✅ 门店浏览、商品点单、购物车
- ✅ 余额支付、订单管理
- ✅ 骑手接单、配送追踪
- ✅ 管理后台基础CRUD
- ✅ AI智能点单（DeepSeek）

### Phase 2（v1.5.0 计划）
- 🔲 集成微信支付
- 🔲 优惠券系统
- 🔲 会员等级与积分
- 🔲 商品评价带图上传
- 🔲 骑手智能调度算法
- 🔲 小程序订阅消息（订单状态通知）

### Phase 3（v2.0.0 展望）
- 🔲 多商户平台
- 🔲 实时数据分析大屏
- 🔲 语音点单（AIGC）
- 🔲 无人配送对接
- 🔲 微服务拆分（Spring Cloud）

---

## 15. 附录

### 15.1 pom.xml 核心依赖

```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
        <version>3.5.4.1</version>
    </dependency>

    <!-- MySQL -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
    </dependency>

    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.3</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.3</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.3</version>
        <scope>runtime</scope>
    </dependency>

    <!-- Knife4j -->
    <dependency>
        <groupId>com.github.xiaoymin</groupId>
        <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
        <version>4.5.0</version>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- 腾讯云COS（可选） -->
    <dependency>
        <groupId>com.qcloud</groupId>
        <artifactId>cos_api</artifactId>
        <version>5.6.155</version>
    </dependency>

    <!-- Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 15.2 微信小程序 app.json 配置

```json
{
  "pages": [
    "pages/index/index",
    "pages/store/index",
    "pages/cart/index",
    "pages/order/list",
    "pages/order/detail",
    "pages/ai/index",
    "pages/user/index",
    "pages/user/address",
    "pages/user/wallet"
  ],
  "window": {
    "navigationBarTitleText": "Coffee Order",
    "navigationBarBackgroundColor": "#6f4e37",
    "navigationBarTextStyle": "white"
  },
  "tabBar": {
    "color": "#999",
    "selectedColor": "#6f4e37",
    "list": [
      {
        "pagePath": "pages/index/index",
        "text": "首页",
        "iconPath": "images/tab/home.png",
        "selectedIconPath": "images/tab/home-active.png"
      },
      {
        "pagePath": "pages/ai/index",
        "text": "AI",
        "iconPath": "images/tab/ai.png",
        "selectedIconPath": "images/tab/ai-active.png"
      },
      {
        "pagePath": "pages/cart/index",
        "text": "购物车",
        "iconPath": "images/tab/cart.png",
        "selectedIconPath": "images/tab/cart-active.png"
      },
      {
        "pagePath": "pages/user/index",
        "text": "我的",
        "iconPath": "images/tab/user.png",
        "selectedIconPath": "images/tab/user-active.png"
      }
    ]
  },
  "permission": {
    "scope.userLocation": {
      "desc": "需要获取你的位置信息以配送商品"
    }
  },
  "requiredPrivateInfos": ["getLocation", "chooseLocation"],
  "sitemapLocation": "sitemap.json"
}
```
