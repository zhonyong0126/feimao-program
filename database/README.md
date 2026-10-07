# 数据库迁移说明

- `legacy-axcx.sql`：原租售出行 MySQL 数据库脚本，包含 43 张业务表及原始数据。
- `backups/axcx-before-legacy-import-20260822.sql`：2026-08-22 重新导入前的 `axcx` 完整备份。
- `../feimao-backend/src/main/resources/schema.sql`：当前配送业务的 `fm_*` 表初始化脚本，数据库名及表前缀已与当前实体统一；仅使用 `CREATE TABLE IF NOT EXISTS` 建表，不删除已有数据，也不创建默认账号或演示数据。

2026-08-22 已将 `legacy-axcx.sql` 导入本机 `axcx` 数据库。导入后的抽查结果：

| 项目 | 数量 |
|---|---:|
| 原脚本业务表 | 43 |
| 当前 `ymcx_` 表 | 44（含后续新增的 `ymcx_usertoken`） |
| 用户 | 27 |
| 订单 | 5 |
| 门店 | 20 |
| 服务网点 | 3 |
| 平台车辆 | 34 |

该脚本含 `DROP TABLE IF EXISTS`，再次执行前必须先备份。备份和原始数据包含个人信息，不应提交到公开仓库。

## 增量迁移

- `migrations/V002__legacy_user_auth_fields.sql`：补充 PHP 用户认证接口已经使用、但原始 SQL 未交付的实名认证与员工认证字段；已于 2026-08-23 执行。
- `migrations/V003__legacy_role_sessions.sql`：增加门店/加盟商等角色隔离的兼容会话表，避免沿用旧系统可混用角色令牌的问题；已于 2026-08-23 执行。
- `migrations/V004__legacy_admin_sessions.sql`：增加独立管理员会话表，为后台审核、财务等高权限接口提供登录隔离；已于 2026-08-23 执行。
- `migrations/V005__rename_role_session.sql`：将迁移期会话表正式重命名为 `anxin_role_session`；已于 2026-08-23 执行。
- `migrations/V006__rename_business_role_session.sql`：将业务会话表统一为当前项目命名 `business_role_session`；已于 2026-08-31 执行。
- `migrations/V007__driver_income_idempotency.sql`：为司机订单收入流水增加订单级唯一约束，防止订单完成重试导致重复入账；部署更新时执行一次。
- `migrations/V008__allow_driver_wechat_onboarding.sql`：允许微信首次登录司机在补录手机号前创建待认证账号；部署更新时执行一次。
- `migrations/V009__driver_withdraw_review.sql`：补充司机提现微信批次号、审核备注和审核时间字段；已于 2026-09-06 在本机 `axcx` 数据库执行。

Spring Boot 数据库连接必须包含 `tinyInt1isBit=false`。旧系统把 `TINYINT(1)` 用作多值状态（1、2、3、4等），不能按布尔值读取。
