# 校园活动管理系统 V1.0（Camus-activity-management-system）

> 本文档是项目的核心上下文文档，供团队成员与 AI 辅助开发使用。
> AI 会话开始前应先阅读本文档；开发过程中如有需求/设计变更，必须同步更新本文档。

- **GitHub 仓库**：<https://github.com/WindborneX/campus-activity-management-system-v>
- **性质**：软件工程实验一「基于工程意图的软件迭代开发」
- **截止时间**：2026-09-13 23:59
- **目标**：在 AI 辅助下，从概括的业务描述出发，完成需求分析 → 工程意图 → 软件设计 → 开发实现 → 软件验证，并用 Git 记录全过程。
- **基本业务方向**（实验给定的出发点）：
  1. 系统支持用户注册和登录；
  2. 学生能够了解和参与校园活动；
  3. 活动组织教师能够发布和管理校园活动。

## 1. 工程意图（开发的最高依据）

- **为什么做 V1.0**：活动组织靠线下通知，学生获取信息分散，需要一个最小可用的线上系统跑通「发布 → 浏览 → 报名」业务闭环。
- **主要解决问题**：教师线上发布活动、学生线上报名，消除信息不对称。
- **本轮范围**：注册登录、活动发布与管理、活动浏览与报名，共 9 个功能点（见 §2）。
- **重要约束**：
  - 不引入管理员等额外角色（仅学生、教师）；
  - 不做评论、签到、通知、搜索等扩展功能；
  - 密码必须加密存储（BCrypt）；
  - 教师仅能操作本人发布的活动；
  - 不以功能数量和代码规模为目标，优先保证业务闭环完整、结构合理。
- **完成依据（验收标准）**：§7 验证场景全部通过，即 V1.0 达标。

## 2. 需求分析

### 2.1 用户与目标

| 角色 | 目标 | 核心诉求 |
|---|---|---|
| 学生 | 发现并参与活动 | 浏览列表 → 查看详情 → 报名 → 查看自己的报名 |
| 教师 | 发布并管理活动 | 创建活动 → 查看报名情况 → 修改/取消活动 |

### 2.2 功能需求（V1.0 共 9 项）

| 编号 | 模块 | 功能 |
|---|---|---|
| F1 | 用户 | 注册（用户名 + 密码 + 角色学生/教师） |
| F2 | 用户 | 登录（返回 JWT）、退出登录 |
| F3 | 活动 | 教师创建活动（标题/描述/地点/起止时间/报名截止/人数上限） |
| F4 | 活动 | 教师管理自己的活动（查看、编辑、取消） |
| F5 | 活动 | 学生浏览活动列表（可按状态筛选） |
| F6 | 活动 | 学生查看活动详情 |
| F7 | 报名 | 学生报名活动 |
| F8 | 报名 | 学生取消报名 |
| F9 | 报名 | 教师查看活动的报名名单 |

### 2.3 业务规则（实现核心，Service 层职责）

| 编号 | 规则 |
|---|---|
| R1 | 未登录用户只能浏览，不能报名/发布 |
| R2 | 重复报名同一活动 → 拒绝 |
| R3 | 报名人数达上限 → 拒绝 |
| R4 | 超过报名截止时间 → 拒绝 |
| R5 | 只有发布者本人可修改/取消活动/查看名单 |
| R6 | 活动已取消（CANCELLED）→ 不可报名 |
| R7 | 密码 BCrypt 加密存储，禁止明文 |

### 2.4 非功能需求

- 接口基于 JWT 无状态鉴权；
- 参数校验（时间先后合法、容量为正整数等）；
- 统一响应结构 `Result<T>` + 全局异常处理；
- 代码仓库不得提交密码、Token、密钥、隐私数据（.gitignore 覆盖 application-local.yml 等）。

## 3. 软件架构

### 3.1 总体架构（前后端分离）

```
Vue3 前端 (Vite + Element Plus + Axios + Vue Router + Pinia)
        │ REST API (JSON, Authorization: Bearer <JWT>)
Spring Boot 3 后端 (单模块)
   Controller → Service(业务规则 R1~R7) → MyBatis-Plus → MySQL 9.6
```

### 3.2 技术选型（已定稿，2026-09-13 确认）

| 层 | 选型 |
|---|---|
| 前端 | Vue 3 + Vite 5 + Element Plus + Axios + Pinia + Vue Router（JavaScript） |
| 后端 | Spring Boot 3.3.5 + Spring Web + Validation + MyBatis-Plus 3.5.7 |
| 安全 | JWT（jjwt 0.12.6）+ HandlerInterceptor 鉴权 + BCrypt（spring-security-crypto），不引入完整 Spring Security |
| 数据库 | MySQL 9.6（mysql-connector-j 版本随 Boot BOM） |
| JDK | 17（Temurin 17.0.19） |

### 3.3 后端模块划分

```
server/src/main/java/com/campus/activity/
├── controller/   AuthController, ActivityController, RegistrationController
├── service/      业务规则 R1~R7 全部在此层实现
├── mapper/       MyBatis-Plus BaseMapper
├── entity/       User, Activity, Registration
├── dto/          LoginRequest, RegisterRequest, ActivityCreateRequest, ...
├── common/       Result<T>, 全局异常处理器, 业务异常
└── config/       JWT 过滤器, Web 配置, CORS
```

### 3.4 API 清单（10 个）

| 方法 | 路径 | 说明 | 相关规则 |
|---|---|---|---|
| POST | /api/auth/register | 注册 | 用户名唯一 |
| POST | /api/auth/login | 登录返回 JWT | |
| GET | /api/activities?status= | 活动列表（公开） | |
| GET | /api/activities/{id} | 活动详情（公开） | |
| POST | /api/activities | 创建活动 | 教师角色 |
| PUT | /api/activities/{id} | 修改活动 | R5 |
| DELETE | /api/activities/{id} | 取消活动 | R5 |
| POST | /api/activities/{id}/signup | 报名 | R1 R2 R3 R4 R6 |
| DELETE | /api/activities/{id}/signup | 取消报名 | R1 仅本人 |
| GET | /api/activities/{id}/registrations | 报名名单 | R5 |

### 3.5 数据库设计（3 张表）

```sql
user            (id, username UNIQUE, password_hash, role[STUDENT|TEACHER], created_at)
activity        (id, teacher_id→user, title, description, location,
                 start_time, end_time, signup_deadline, max_participants,
                 status[ACTIVE|CANCELLED], created_at)
registration    (id, activity_id→activity, student_id→user, registered_at,
                 UNIQUE(activity_id, student_id))
```

## 4. 前端页面规划

| 路由 | 页面 | 访问权限 |
|---|---|---|
| /login | 登录/注册 | 公开 |
| /activities | 活动列表 | 公开（报名需登录） |
| /activities/:id | 活动详情 | 公开（报名需登录） |
| /my-registrations | 我的报名 | 学生 |
| /manage | 活动管理（创建/编辑/取消/看名单） | 教师 |

## 5. Git 规范

- 托管平台：GitHub，仓库地址：<https://github.com/WindborneX/campus-activity-management-system-v>（远程名 `origin`）；
- 从空仓库起步，小步提交；
- 提交信息格式：`type(scope): 说明`，type ∈ init/docs/feat/fix/test；
- 计划提交序列（体现逐步形成过程）：

```
1. init:        建仓库 + .gitignore + README(工程意图摘要)
2. docs:        需求分析与设计文档
3. feat(backend): 建表脚本 + 实体类
4. feat(backend): 注册登录接口(JWT)
5. feat(backend): 活动发布/管理接口
6. feat(backend): 报名接口(含业务规则)
7. feat(frontend): 项目骨架 + 路由
8. feat(frontend): 登录注册页
9. feat(frontend): 活动列表/详情页
10. feat(frontend): 教师管理页 + 报名交互
11. test:       验证场景执行与修复
```

- 安全红线：禁止提交数据库密码、JWT 密钥等敏感信息，敏感配置放本地文件并加入 .gitignore。

## 6. 当前开发状态（随开发进度更新）

- [x] 需求分析与工程意图确定
- [x] 架构与数据库设计
- [x] Git 仓库初始化 + .gitignore + README
- [x] 数据库建表脚本 + 后端骨架
- [ ] 后端：注册登录（JWT）
- [ ] 后端：活动发布/管理
- [ ] 后端：报名（业务规则 R1~R7）
- [ ] 前端：骨架 + 登录注册页
- [ ] 前端：活动列表/详情页
- [ ] 前端：教师管理页 + 报名交互
- [ ] 验证场景执行 + 实验报告

## 7. 验证方案（对应验收标准）

| # | 场景 | 预期 | 对应规则 |
|---|---|---|---|
| V1 | 教师注册→登录→发布容量 2 人的活动 | 成功 | — |
| V2 | 学生 A、B 报名成功；学生 C 报名 | C 被拒 | R3 |
| V3 | A 取消报名后 C 再报名 | 成功 | — |
| V4 | 对已过截止时间的活动报名 | 被拒 | R4 |
| V5 | 学生 Token 调用创建活动接口 | 403 | R1 |
| V6 | 教师 2 取消教师 1 的活动 | 被拒 | R5 |
| V7 | 对已取消活动报名 | 被拒 | R6 |

> 验证不能只判断「程序能启动」，须逐条执行并记录到实验报告。
