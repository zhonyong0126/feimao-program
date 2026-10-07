# 飞猫程序 - 项目启动说明

## 项目结构

```
feimao-program/
├── feimao-backend/          # Spring Boot 后端及 FreeMarker 后台管理系统
├── feimao-driver/           # 司机端小程序
├── feimao-staff/            # 员工端小程序  
├── feimao-user/             # 租售出行用户端
├── database/                # 原数据库脚本、导入说明及导入前备份
├── MIGRATION_AUDIT.md       # 原系统与当前项目的功能差异及迁移清单
└── README.md               # 本文件
```

> 当前 Spring Boot 服务已完成电动车租售业务、后台管理和原业务数据迁移，同时保留现有货运抢单/调度扩展。迁移及验收详情见 `MIGRATION_AUDIT.md`。

后端数据库、JWT、微信、短信和保险凭据均通过环境变量提供。可参考
`feimao-backend/.env.example` 配置，但不要提交填写了真实密钥的文件。后端至少需要设置
`DATABASE_PASSWORD`；生产环境还应设置不少于32个随机字符的 `JWT_SECRET`，并启用
`ADMIN_CAPTCHA_ENABLED=true`。

小程序本地开发默认连接 `http://127.0.0.1:8080`（司机、员工端使用
`localhost:8080`）。正式构建前设置 `VUE_APP_API_BASE_URL` 为已在微信后台登记的 HTTPS
后端域名，例如 `https://api.example.com`，不要包含末尾 `/api`。生产环境未设置时仅使用同域
`/api`，微信小程序发布时必须显式配置该变量及合法请求域名。

## 环境要求

- JDK 1.8
- MySQL 5.7+
- Redis 6.x
- Node.js 16+
- HBuilderX (小程序开发)

## 后端启动步骤

1. 创建 `axcx` 数据库并导入配送表；再按 [database/README.md](database/README.md) 导入租售业务数据及增量脚本：
```bash
mysql -u root -p < feimao-backend/src/main/resources/schema.sql
```

2. 按 `.env.example` 设置环境变量，至少包括：
```bash
export DATABASE_URL='jdbc:mysql://127.0.0.1:3306/axcx?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&tinyInt1isBit=false'
export DATABASE_USERNAME='axcx'
export DATABASE_PASSWORD='你的数据库密码'
export JWT_SECRET='至少32字节的随机密钥'
```

3. 编译打包：
```bash
cd feimao-backend
mvn clean package -DskipTests
```

4. 启动：
```bash
java -jar target/feimao-backend-1.0.0.jar
```

5. 访问API文档：http://localhost:8080/doc.html

## 小程序启动步骤

### 司机端
1. 用 HBuilderX 打开 `feimao-driver` 目录
2. 通过 `VUE_APP_API_BASE_URL` 配置后端地址
3. 运行到微信开发者工具或真机调试

### 员工端
1. 用 HBuilderX 打开 `feimao-staff` 目录
2. 同上配置 `VUE_APP_API_BASE_URL`
3. 运行调试

## 后台管理系统启动

后台管理已集成在 `feimao-backend` 中，不需要单独启动前端项目。后端启动后访问：

http://localhost:8080/admin/login

## 初始账号

生产初始化脚本不会创建默认账号。后台管理员来自迁移后的 `ymcx_admin`；配送员工账号应由管理员创建并设置独立强密码，禁止使用公开默认密码。

## API 接口列表

### 司机端 API
- POST `/api/driver/login/wx` - 微信登录
- GET `/api/driver/info` - 获取司机信息
- GET `/api/driver/orders/grab` - 抢单大厅
- GET `/api/driver/orders/my` - 我的订单
- POST `/api/driver/order/grab/{id}` - 抢单
- POST `/api/driver/order/start/{id}` - 开始运输
- POST `/api/driver/order/complete/{id}` - 完成订单
- GET `/api/driver/stats/today` - 今日统计

### 员工端 API
- POST `/api/staff/login` - 登录
- GET `/api/staff/orders` - 订单列表
- POST `/api/staff/order/dispatch` - 派单
- GET `/api/staff/drivers` - 司机列表

### 后台管理 API
- GET `/api/admin/dashboard` - 首页数据
- GET `/api/admin/orders` - 订单管理
- GET `/api/admin/drivers` - 司机管理
- GET `/api/admin/staffs` - 员工管理
- GET `/api/admin/customers` - 客户管理

## 技术栈

### 后端
- Spring Boot 2.7.18
- MyBatis Plus 3.5.3
- JWT 认证
- Redis 缓存
- Knife4j API文档

### 前端
- FreeMarker（后台管理）
- Vue 2.x / uniapp（小程序）

## 开发团队

- 开发时间：2026年4月
- 版本：v1.0.0
