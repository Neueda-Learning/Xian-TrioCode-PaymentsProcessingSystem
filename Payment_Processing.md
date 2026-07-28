# 支付处理系统（Vue3 + SpringBoot）基础业务架构详细设计 V2（精简版）

面向课程项目的最小可用版本：
- 不引入分布式、MQ、微服务
- 错误码采用后端枚举类，不使用错误码字典表
- 重试过程记录在代码日志，不单独建处理尝试记录表
- 保留核心审计表，确保状态轨迹可追溯
- 不考虑跨域配置
- 使用订单号 paymentNo 做幂等，不单独设置幂等键字段

---

## 1. 项目整体架构设计

### 1.1 前后端分层架构

1. 前端层（Vue3）
- 页面：创建支付、支付列表、支付详情、失败详情
- 职责：输入与交互、前端基础校验、状态展示

2. 接口层（SpringBoot Controller）
- 职责：参数接收、参数注解校验、调用服务、返回统一响应

3. 业务层（Service）
- 职责：流程编排、事务控制、幂等处理
- 内部组件 PaymentRuleChecker：金额/账户/币种/业务约束校验，可被多个流程复用
- 内部组件 PaymentStateMachine：状态白名单校验，判断当前状态能否流转到目标状态
- 说明：两个组件均放在 service 包下，不单独分层，由 ServiceImpl 直接调用

4. 数据层（Mapper + MySQL）
- 职责：支付主数据和状态历史持久化

### 1.2 技术栈说明

前端：
1. Vue3
2. Vue Router
3. Pinia
4. Axios
5. Element Plus

后端：
1. Spring Boot 3.x
2. Spring Web
3. Spring Validation
4. MyBatis-Plus（或 MyBatis）
5. Lombok
6. springdoc-openapi
7. MySQL 8.x

### 1.3 整体业务模块划分

1. 支付创建模块
2. 支付查询模块（详情、列表、失败详情）
3. 支付生命周期模块（validate/send/complete/fail）
4. 状态历史审计模块
5. 幂等与错误处理模块

---

## 2. 数据库详细设计

数据库建议：MySQL 8.0，字符集 utf8mb4。  
本版采用 5 张核心表：
1. account
2. account_balance_history
3. currency_dict
4. payment
5. payment_status_history

### 2.1 表结构与字段说明

#### 2.1.1 account（账户表）

字段：
1. id，BIGINT，主键，自增，非空，账户主键
2. account_no，VARCHAR(32)，唯一，非空，账户号
3. name，VARCHAR(30)，非空，姓名
4. balance，DECIMAL，非空，，账户余额
5. status，TINYINT，非空，默认，1=启用 0=禁用
6. created_at，DATETIME(3)，非空，默认当前时间
7. updated_at，DATETIME(3)，非空，默认当前时间，自动更新

索引：
1. uk_account_no(account_no)
2. idx_account_status(status)

#### 2.1.2 account_balance_history（账户余额流水表）

**用途**：记录每笔支付交易前后的账户余额快照，用于对账、审计和余额追溯。

字段：
1. id，BIGINT，主键，自增，非空
2. account_id，BIGINT，非空，账户ID（支付方或收款方）
3. payment_id，BIGINT，非空，关联支付ID
4. operation_type，VARCHAR(16)，非空，操作类型：DEBIT(扣款) / CREDIT(入账)
5. balance_before，DECIMAL，非空，操作前余额
6. balance_after，DECIMAL，非空，操作后余额
7. amount，DECIMAL，非空，交易金额
8. description，VARCHAR(255)，可空，操作描述（如"Payment ABC123"） 
9. created_at，DATETIME(3)，非空，默认当前时间

索引：
1. idx_history_account_payment(account_id, payment_id)
2. idx_history_account_time(account_id, created_at)
3. idx_history_payment_id(payment_id)

#### 2.1.3 currency_dict（币种字典表）

字段：
1. code，CHAR(3)，主键，非空，ISO4217 币种代码
2. code_name, VARCHAR(32)，非空，币种名称
3. country_name， VARCHAR(32)，非空，国家名称
4. enabled，TINYINT，非空，默认 1，是否启用
5. scale，TINYINT，非空，小数位
6. created_at，DATETIME(3)，非空，默认当前时间
7. updated_at，DATETIME(3)，非空，默认当前时间，自动更新

索引：
1. idx_currency_enabled(enabled)

#### 2.1.4 payment（支付主表）

字段：
1. id，BIGINT，主键，自增，非空
2. payment_no，VARCHAR(32)，唯一，非空，订单号（客户端传入，同时作为幂等键）
3. source_account_id，BIGINT，非空，付款账户ID
4. destination_account_id，BIGINT,非空，收款账户ID
5. amount，DECIMAL，非空，支付金额
6. currency，CHAR(3)，非空，支付币种
7. reference，VARCHAR(128)，可空，备注
8. status，VARCHAR(16)，非空，默认 CREATED，支付状态
9. failure_code，VARCHAR(64)，可空，失败业务码
10. failure_message，VARCHAR(255)，可空，失败说明
11. validated_at，DATETIME(3)，可空
12. sent_at，DATETIME(3)，可空
13. completed_at，DATETIME(3)，可空
14. failed_at，DATETIME(3)，可空
15. version，INT，非空，默认 0，乐观锁版本号
16. created_at，DATETIME(3)，非空，默认当前时间
17. updated_at，DATETIME(3)，非空，默认当前时间，自动更新

索引：
1. uk_payment_no(payment_no)
2. idx_payment_status(status)
3. idx_payment_created_at(created_at)
4. idx_payment_status_created(status, created_at)
5. idx_payment_source_account(source_account_id)

#### 2.1.5 payment_status_history（状态历史表）

字段：
1. id，BIGINT，主键，自增，非空
2. payment_id，BIGINT，非空，支付ID
3. from_status，VARCHAR(16)，可空，原状态
4. to_status，VARCHAR(16)，非空，目标状态
5. reference，VARCHAR(255)，可空，备注
6. error_code，VARCHAR(64)，可空，错误码
7. error_message，VARCHAR(255)，可空，错误描述 
8. created_at，DATETIME(3)，非空，默认当前时间

索引：
1. idx_history_payment_time(payment_id, created_at)
2. idx_history_to_status(to_status)

### 2.2 表关系

1. payment.source_account_id -> account.id（多对一）
2. account_balance_history.account_id -> account.id（多对一）
3. account_balance_history.payment_id -> payment.id（多对一）
4. payment_status_history.payment_id -> payment.id（一对多）
5. payment.currency 与 currency_dict.code（逻辑关联）

### 2.3 可直接执行建表语句

```sql

create database payment;
use payment;

CREATE TABLE account (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  account_no VARCHAR(32) NOT NULL COMMENT '账户号',
  name VARCHAR(30) NOT NULL COMMENT '姓名',
  balance DECIMAL NOT NULL DEFAULT 0.00 COMMENT '账户余额',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1启用0禁用',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  UNIQUE KEY uk_account_no (account_no),
  KEY idx_account_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账户表';

CREATE TABLE account_balance_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  account_id BIGINT NOT NULL COMMENT '账户ID',
  payment_id BIGINT NOT NULL COMMENT '支付ID',
  operation_type VARCHAR(16) NOT NULL COMMENT '操作类型: DEBIT扣款/CREDIT入账',
  balance_before DECIMAL NOT NULL COMMENT '操作前余额',
  balance_after DECIMAL NOT NULL COMMENT '操作后余额',
  amount DECIMAL NOT NULL COMMENT '交易金额',
  description VARCHAR(255) DEFAULT NULL COMMENT '操作描述',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  KEY idx_history_account_payment (account_id, payment_id),
  KEY idx_history_account_time (account_id, created_at),
  KEY idx_history_payment_id (payment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账户余额流水表';

CREATE TABLE currency_dict (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  code CHAR(3) NOT NULL COMMENT 'ISO4217币种编码',
  code_name VARCHAR(100) NOT NULL COMMENT '币种名称',
  country_name VARCHAR(100) NOT NULL COMMENT '国家名称'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='币种字典表';

CREATE TABLE payment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  payment_no VARCHAR(32) NOT NULL COMMENT '订单号(客户端传入, 同时作为幂等键)',
  source_account_id BIGINT NOT NULL COMMENT '付款方账户ID',
  destination_account_id BIGINT NOT NULL COMMENT '收款方账户ID',
  amount DECIMAL NOT NULL COMMENT '支付金额',
  currency CHAR(3) NOT NULL COMMENT '支付币种',
  reference VARCHAR(128) DEFAULT NULL COMMENT '备注',
  status VARCHAR(16) NOT NULL DEFAULT 'CREATED' COMMENT '支付状态',
  failure_code VARCHAR(64) DEFAULT NULL COMMENT '失败错误码',
  failure_message VARCHAR(255) DEFAULT NULL COMMENT '失败详情',
  validated_at DATETIME(3) DEFAULT NULL COMMENT '校验通过时间',
  sent_at DATETIME(3) DEFAULT NULL COMMENT '发送时间',
  completed_at DATETIME(3) DEFAULT NULL COMMENT '完成时间',
  failed_at DATETIME(3) DEFAULT NULL COMMENT '失败时间',
  version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  UNIQUE KEY uk_payment_no (payment_no),
  KEY idx_payment_status (status),
  KEY idx_payment_created_at (created_at),
  KEY idx_payment_status_created (status, created_at),
  KEY idx_payment_source_account (source_account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付主表';

CREATE TABLE payment_status_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  payment_id BIGINT NOT NULL COMMENT '支付ID',
  from_status VARCHAR(16) DEFAULT NULL COMMENT '原状态',
  to_status VARCHAR(16) NOT NULL COMMENT '目标状态',
  reference VARCHAR(255) DEFAULT NULL COMMENT '备注',
  error_code VARCHAR(64) DEFAULT NULL COMMENT '错误码',
  error_message VARCHAR(255) DEFAULT NULL COMMENT '错误信息',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  KEY idx_history_payment_time (payment_id, created_at),
  KEY idx_history_to_status (to_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付状态历史表';
```

---

## 3. 业务状态流转设计

### 3.1 状态定义

1. CREATED：已创建，待校验
2. VALIDATED：校验通过，待发送
3. SENT：已发送，待完成
4. COMPLETED：完成（终态）
5. FAILED：失败（终态）

### 3.2 合法流转

1. CREATED -> VALIDATED
2. CREATED -> FAILED
3. VALIDATED -> SENT
4. VALIDATED -> FAILED
5. SENT -> COMPLETED
6. SENT -> FAILED

### 3.3 禁止流转

1. COMPLETED -> 任意状态
2. FAILED -> 任意状态
3. CREATED -> SENT 或 COMPLETED
4. VALIDATED -> COMPLETED
5. SENT -> VALIDATED
6. 任意状态 -> CREATED

### 3.4 状态机实现建议

1. 使用枚举 + 白名单映射实现状态机
2. 在 Service 流转前统一调用 canTransit(from, to)
3. 非法流转抛出 INVALID_STATUS_TRANSITION
4. 合法流转后必须落状态历史表

---

## 4. 后端完整接口设计（RESTful）

统一前缀：/api/v1

### 4.1 统一响应结构

所有接口（含分页与非分页）统一使用 `Result<T>`：

| 字段 | 类型 | 说明 |
|---|---|---|
| code | String | 业务码，如 SUCCESS / VALIDATION_FAILED |
| message | String | 可读描述 |
| data | T | 业务数据，失败时为 null |

分页场景 data 类型为 `PageResult<T>`：

| 字段 | 类型 | 说明 |
|---|---|---|
| records | List\<T\> | 当页数据 |
| total | Long | 总条数 |
| pageNum | int | 当前页码 |
| pageSize | int | 每页条数 |
| totalPages | int | 总页数 |

### 4.2 错误码与 HTTP 状态码对照

| 业务码 | HTTP | 触发场景 |
|---|---|---|
| SUCCESS | 200 | 正常返回 |
| VALIDATION_FAILED | 400 | 参数格式或业务规则校验失败 |
| INVALID_AMOUNT | 400 | 金额为0/负数/超上限/精度错误 |
| INVALID_ACCOUNT | 400 | 账户不存在或已禁用 |
| INVALID_CURRENCY | 400 | 币种不支持 |
| INSUFFICIENT_FUNDS | 400 | 余额不足 |
| INVALID_STATUS_TRANSITION | 400 | 非法状态流转 |
| DUPLICATE_PAYMENT | 409 | 订单号已存在（重复提交） |
| PAYMENT_NOT_FOUND | 404 | 支付记录不存在 |
| PROCESSING_ERROR | 500 | 系统内部异常 |
| NETWORK_ERROR | 503 | 模拟网络通信失败 |

### 4.3 各接口详细说明

---

#### 接口 1：创建支付
- **URL**：`POST /api/v1/payments`
- **用途**：创建支付单，初始状态为 CREATED
- **幂等**：同一 paymentNo 重复请求返回已存在记录，不再新建

**请求体 `PaymentCreateReqDTO`：**

| 参数名 | 类型 | 必传 | 校验规则 | 说明 |
|---|---|---|---|---|
| paymentNo | String | 是 | 非空，长度≤32，唯一 | 订单号（客户端传入，同时作为幂等键） |
| sourceAccountNo | String | 是 | 非空，长度≤32 | 付款方账户号 |
| destinationAccountNo | String | 是 | 非空，长度≤32 | 收款方账户号 |
| amount | BigDecimal | 是 | >0，≤1000000，最多2位小数 | 支付金额 |
| currency | String | 是 | 非空，3位大写字母 | ISO4217 币种，如 USD |
| reference | String | 否 | 长度≤128 | 备注说明 |

**返回 `Result<PaymentDetailVO>`**（字段见接口2返回说明）

---

#### 接口 2：查询支付详情
- **URL**：`GET /api/v1/payments/{paymentId}`
- **用途**：查看单笔支付完整信息和当前状态

**路径参数：**

| 参数名 | 类型 | 必传 | 说明 |
|---|---|---|---|
| paymentId | Long | 是 | 支付主键ID |

**返回 `Result<PaymentDetailVO>`：**

| 字段 | 类型 | 说明 |
|---|---|---|
| paymentId | Long | 支付ID |
| paymentNo | String | 订单号（客户端传入） |
| sourceAccountNo | String | 付款账户号 |
| destinationAccountNo | String | 收款账户号 |
| amount | BigDecimal | 金额 |
| currency | String | 币种 |
| reference | String | 备注 |
| status | String | 当前状态 |
| failureCode | String | 失败业务码（失败时有值） |
| failureMessage | String | 失败描述（失败时有值） |
| validatedAt | String | 校验通过时间 |
| sentAt | String | 发送时间 |
| completedAt | String | 完成时间 |
| failedAt | String | 失败时间 |
| createdAt | String | 创建时间 |
| updatedAt | String | 最后更新时间 |

---

#### 接口 3：分页查询支付列表
- **URL**：`GET /api/v1/payments`
- **用途**：按条件筛选支付记录，支持分页

**查询参数 `PaymentListQueryDTO`：**

| 参数名 | 类型 | 必传 | 说明 |
|---|---|---|---|
| status | String | 否 | 按状态过滤（精确匹配） |
| paymentNo | String | 否 | 订单号模糊搜索 |
| reference | String | 否 | 备注模糊搜索 |
| currency | String | 否 | 按币种过滤 |
| createdFrom | String | 否 | 创建时间起（ISO8601） |
| createdTo | String | 否 | 创建时间止 |
| pageNum | int | 否 | 页码，默认 1 |
| pageSize | int | 否 | 每页条数，默认 10，最大 100 |

**返回 `Result<PageResult<PaymentListItemVO>>`：**

`PaymentListItemVO` 字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| paymentId | Long | 支付ID |
| paymentNo | String | 订单号 |
| sourceAccountNo | String | 付款账户号 |
| destinationAccountNo | String | 收款账户号 |
| amount | BigDecimal | 金额 |
| currency | String | 币种 |
| status | String | 当前状态 |
| createdAt | String | 创建时间 |

---

#### 接口 4：查询状态历史（时间线）
- **URL**：`GET /api/v1/payments/{paymentId}/histories`
- **用途**：查看单笔支付完整状态变更轨迹，用于时间线展示
- **说明**：不分页，按 event_time 升序，返回全量历史记录

**路径参数：**

| 参数名 | 类型 | 必传 | 说明 |
|---|---|---|---|
| paymentId | Long | 是 | 支付主键ID |

**返回 `Result<List<PaymentHistoryVO>>`：**

| 字段 | 类型 | 说明 |
|---|---|---|
| historyId | Long | 历史记录ID |
| fromStatus | String | 原状态（首次创建时为 null） |
| toStatus | String | 目标状态 |
| triggerType | String | 触发类型：API / SYSTEM |
| triggerBy | String | 触发者，默认 SYSTEM |
| reason | String | 原因说明 |
| errorCode | String | 错误码（失败节点有值） |
| errorMessage | String | 错误描述（失败节点有值） |
| eventTime | String | 事件发生时间 |

---

#### 接口 5：执行校验
- **URL**：`POST /api/v1/payments/{paymentId}/validate`
- **用途**：触发 CREATED → VALIDATED，业务校验失败则转 FAILED
- **事务**：主表更新 + 历史记录插入，一个事务

**路径参数：**

| 参数名 | 类型 | 必传 | 说明 |
|---|---|---|---|
| paymentId | Long | 是 | 支付主键ID |

**请求体 `PaymentActionReqDTO`：**

| 参数名 | 类型 | 必传 | 说明 |
|---|---|---|---|
| reason | String | 否 | 操作原因，写入历史表 reason 字段 |

**返回 `Result<PaymentDetailVO>`**（返回流转后最新状态）

---

#### 接口 6：执行发送
- **URL**：`POST /api/v1/payments/{paymentId}/send`
- **用途**：触发 VALIDATED → SENT，模拟发送失败则转 FAILED

**路径参数 / 请求体**：同接口5

**返回 `Result<PaymentDetailVO>`**

---

#### 接口 7：执行完成
- **URL**：`POST /api/v1/payments/{paymentId}/complete`
- **用途**：触发 SENT → COMPLETED，模拟清算失败则转 FAILED

**路径参数 / 请求体**：同接口5

**返回 `Result<PaymentDetailVO>`**

---

#### 接口 8：手工置失败
- **URL**：`POST /api/v1/payments/{paymentId}/fail`
- **用途**：CREATED / VALIDATED / SENT → FAILED，演示或管理场景

**路径参数：**

| 参数名 | 类型 | 必传 | 说明 |
|---|---|---|---|
| paymentId | Long | 是 | 支付主键ID |

**请求体 `PaymentFailReqDTO`：**

| 参数名 | 类型 | 必传 | 说明 |
|---|---|---|---|
| errorCode | String | 是 | 错误码，参考 ErrorCodeEnum |
| errorMessage | String | 是 | 错误描述 |
| reason | String | 否 | 操作原因，写入历史表 |

**返回 `Result<PaymentDetailVO>`**

---

#### 接口 9：查询失败详情
- **URL**：`GET /api/v1/payments/{paymentId}/failure`
- **用途**：专门为失败详情页提供聚合数据

**路径参数：**

| 参数名 | 类型 | 必传 | 说明 |
|---|---|---|---|
| paymentId | Long | 是 | 支付主键ID |

**返回 `Result<PaymentFailureVO>`：**

| 字段 | 类型 | 说明 |
|---|---|---|
| paymentId | Long | 支付ID |
| status | String | 当前状态（应为 FAILED） |
| failureCode | String | 失败业务码 |
| failureMessage | String | 失败详细描述 |
| failedAt | String | 失败时间 |

---

#### 接口 10：查询币种字典
- **URL**：`GET /api/v1/dicts/currencies`
- **用途**：前端创建支付表单下拉项数据源

**查询参数：**

| 参数名 | 类型 | 必传 | 说明 |
|---|---|---|---|
| enabled | int | 否 | 1=启用（默认），0=全部 |

**返回 `Result<List<CurrencyVO>>`：**

| 字段 | 类型 | 说明 |
|---|---|---|
| code | String | 币种代码，如 USD |
| nameCn | String | 中文名称 |
| scale | int | 小数位数 |

---

## 5. 后端代码分层设计

### 5.1 包结构

1. com.example.payment.controller
2. com.example.payment.service（接口定义）
3. com.example.payment.service.impl（ServiceImpl + RuleChecker + StateMachine）
4. com.example.payment.domain.entity
5. com.example.payment.domain.dto
6. com.example.payment.domain.vo
7. com.example.payment.mapper
8. com.example.payment.enums
9. com.example.payment.exception
10. com.example.payment.common
11. com.example.payment.config

说明：PaymentRuleChecker 和 PaymentStateMachine 放在 service.impl 包下，作为 Service 的内部组件（Spring Bean），不独立分层。

### 5.2 校验职责分层（最终定稿）

**第一层：前端（Vue3）**
- 必填、长度、格式的友好提示（不可信，仅提升体验）

**第二层：Controller（接口入参格式校验）**
- 使用 Spring Validation 注解
- @NotBlank：paymentNo、sourceAccountNo、destinationAccountNo、currency
- @Size(max=32)：paymentNo
- @NotNull + @DecimalMin("0.01") + @DecimalMax("1000000")：amount
- @Pattern(regexp="[A-Z]{3}")：currency 格式
- @Size(max=128)：reference
- 校验失败直接返回 HTTP 400，业务码 VALIDATION_FAILED

**第三层：Service 内部（核心业务规则，由 PaymentRuleChecker 承载）**
- 付款账户是否存在且状态为启用
- 目标账户号与源账户号不能相同
- 币种是否在 currency_dict 中且 enabled=1
- 余额是否充足（模拟校验即可）
- 同一个 PaymentRuleChecker 可被 createPayment 和 validatePayment 复用

**第四层：Service 内部（状态流转规则，由 PaymentStateMachine 承载）**
- validate/send/complete/fail 操作前调用 canTransit(fromStatus, toStatus)
- 白名单枚举，非法流转直接抛出 INVALID_STATUS_TRANSITION

**第五层：数据库约束（兜底）**
- payment_no 唯一索引：防重复创建（订单号幂等）
- version 乐观锁：防并发覆盖
- 非空约束、外键约束

### 5.3 关键类清单

**实体 Entity（domain/entity）：**
1. AccountEntity
2. AccountBalanceHistoryEntity
3. CurrencyDictEntity
4. PaymentEntity
5. PaymentStatusHistoryEntity

**请求 DTO（domain/dto）：**
1. PaymentCreateReqDTO
2. PaymentListQueryDTO
3. PaymentActionReqDTO
4. PaymentFailReqDTO

**响应 VO（domain/vo）：**
1. PaymentDetailVO
2. PaymentListItemVO
3. PaymentHistoryVO
4. PaymentFailureVO
5. CurrencyVO

**Controller（controller）：**
1. PaymentController（创建、查询、列表、失败详情）
2. PaymentLifecycleController（validate/send/complete/fail）
3. DictController（币种字典）

**Service 接口（service）：**
1. PaymentService（创建、查询、列表、历史、失败详情）
2. PaymentLifecycleService（validate/send/complete/fail）

**Service 实现及内部组件（service/impl）：**
1. PaymentServiceImpl
2. PaymentLifecycleServiceImpl
3. PaymentRuleChecker（业务规则校验，被 ServiceImpl 调用）
4. PaymentStateMachine（状态流转白名单，被 ServiceImpl 调用）

**Mapper（mapper）：**
1. AccountMapper
2. AccountBalanceHistoryMapper
3. CurrencyDictMapper
4. PaymentMapper
5. PaymentStatusHistoryMapper

**枚举（enums）：**
1. PaymentStatusEnum（CREATED/VALIDATED/SENT/COMPLETED/FAILED）
2. ErrorCodeEnum（替代错误码字典表，含 code/message/httpStatus/retryable）
3. TriggerTypeEnum（API/SYSTEM）

---

### 5.4 数据库事务边界设计

**核心原则：凡是主表状态变化 + 历史表写入，必须在同一事务内完成。当支付完成或失败时，需记录账户余额流水。**  
使用 Spring `@Transactional` 注解标注在 ServiceImpl 方法上。

| 操作 | 事务范围 | 涉及表操作 | 失败回滚效果 |
|---|---|---|---|
| createPayment | 开启事务 | payment（insert）+ payment_status_history（insert） | 不会只写主表而历史丢失 |
| validatePayment | 开启事务 | payment（update status/version/validated_at）+ history（insert） | 不会出现状态已改但历史没写的情况 |
| sendPayment | 开启事务 | payment（update status/version/sent_at）+ history（insert） | 同上 |
| completePayment | 开启事务 | payment（update status/version/completed_at）+ account_balance_history（2条insert：扣款方+入账方）+ history（insert） | 确保支付完成时余额流水完整记录 |
| failPayment | 开启事务 | payment（update status/version/failure_code/failed_at）+ account_balance_history（可选，记录失败原因）+ history（insert） | 同上 |
| 查询接口 | 无需事务 | payment/history/balance_history（只读） | N/A |

**乐观锁配合事务：**
1. 在事务内执行 UPDATE payment SET status=?, version=version+1 WHERE id=? AND version=? AND status=?
2. 影响行数为 0 → 并发冲突，抛出 BizException(PROCESSING_ERROR)，事务回滚
3. 客户端可重新查询当前状态再决定是否重试

**幂等与事务配合：**
1. createPayment 先查 payment_no 是否已存在
2. 已存在→直接返回已有记录，不开启新事务
3. 不存在→开启事务插入
4. 极端并发两请求同时判断不存在→唯一索引拒绝第二次插入→捕获异常再查返回

---

## 6. 通用基础封装设计

### 6.1 统一返回结果类

`Result<T>` 字段：
1. code（String，业务码）
2. message（String，可读描述）
3. data（T，业务数据，失败时为 null）

静态工厂方法：
1. `ok(data)` → code=SUCCESS
2. `fail(ErrorCodeEnum)` → code+message 取自枚举
3. `fail(ErrorCodeEnum, data)` → 失败时附带数据

### 6.2 分页工具类

1. PageQuery（pageNum, pageSize）
2. PageResult<T>（records, total, totalPages 等）

### 6.3 全局异常处理

1. BizException（携带 ErrorCodeEnum）
2. MethodArgumentNotValidException
3. ConstraintViolationException
4. Exception 兜底

### 6.4 常量与配置

1. ApiConstants
2. RetryConstants（MAX_RETRY=3 等）
3. RegexConstants
4. TimeConstants

---

## 7. 开发顺序建议与优化建议

### 7.1 开发顺序

1. 建表 + 初始化币种和测试账户
2. 搭建统一返回、异常、分页、Swagger
3. 完成创建支付 + 幂等
4. 完成查询（详情/列表/历史/失败详情）
5. 完成 validate/send/complete/fail 状态流转
6. 前端页面联调
7. 测试边界场景

### 7.2 设计考量逐项落地（最终确认）

1. 幂等性
- 客户端传 paymentNo（订单号）
- payment.payment_no 唯一索引
- 重复提交返回已存在支付（推荐策略）

2. 重试处理
- 仅对 NETWORK_ERROR 等可重试错误进行重试
- 最大重试次数 3 次（代码常量配置）
- 超过阈值标记为 FAILED
- 重试明细写应用日志，不单建尝试表

3. 状态流转
- 状态机白名单控制合法流转
- 非法流转直接拒绝

4. 故障场景 API
- 统一响应结构
- HTTP 状态码与业务码配套返回

5. 审计轨迹
- 每次状态变更写 payment_status_history
- 记录 from/to、event_time、trigger_type、trigger_by、error_code

### 7.3 可选增强（有余力再做）

1. 实现账户余额流水查询接口（查看某账户的所有交易前后余额）
2. 引入处理尝试记录表（增强排障能力）
3. 引入错误码字典表（支持动态文案/多语言）
4. 引入定时任务自动推进状态（模拟异步处理）

### 7.4 余额流水表的使用场景

**account_balance_history 表的核心作用：**

1. **支付完成时的双边记录（completePayment）**
   ```
   源账户(支付方)：DEBIT（扣款）
     - balance_before: 100.00
     - balance_after: 40.00
     - amount: 60.00
   
   目标账户(收款方)：CREDIT（入账）
     - balance_before: 50.00
     - balance_after: 110.00
     - amount: 60.00
   ```

2. **对账与审计**
   - 通过 payment_id 关联，可追溯每笔支付对应的账户变化
   - 通过 account_id 查询，可看某账户的完整流水历史
   - balance_before 和 balance_after 记录快照，防止原账户表被覆盖后无法追溯

3. **支付失败处理**
   - 支付失败时，不写余额流水（因为实际未发生扣款）
   - 可选：记录失败流水用于诊断，但操作类型标记为"PENDING_FAILED"

4. **查询接口建议**
   - GET /api/v1/accounts/{accountId}/balance-history?pageNum=1&pageSize=20
   - 返回按 event_time 降序排列的余额流水，可视化账户变化轨迹

---

本 V2 文档已按你的反馈精简：
- 保留核心能力与课程要求
- 减少非必要表结构复杂度
- 明确校验职责分层
- 明确幂等、重试、状态机、故障响应、审计轨迹的落地方式
- **新增：account_balance_history 表，完整记录每笔支付交易前后的账户余额快照，支持对账和审计**
