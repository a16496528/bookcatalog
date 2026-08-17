## Purpose

本能力定义一个 RESTful 图书目录服务的外部可观察行为契约，以及可供课程作业评审的自动化质量证据和 AI 辅助开发证据。

## ADDED Requirements

### Requirement: 图书资源表示
服务 SHALL 将图书表示为 JSON 对象，其中包含服务端生成的正整数 `id`、非空字符串 `title`、非空字符串 `author`、规范化字符串 `isbn` 和可为 null 的整数 `publicationYear`。服务 MUST 使用独立的 API 请求与响应表示，不得暴露持久化层专用字段。

#### Scenario: 返回图书资源
- **GIVEN** 已存储一本图书
- **WHEN** 客户端通过 API 查询该图书
- **THEN** 响应仅包含受支持的资源字段 `id`、`title`、`author`、`isbn` 和 `publicationYear`
- **AND** 不暴露任何持久化层内部字段

### Requirement: 创建图书
服务 SHALL 接受包含 `title`、`author`、`isbn` 和可选 `publicationYear` 的 `POST /api/books` 请求，持久化合法的新图书，并返回 HTTP 201、创建后的资源表示，以及指向 `/api/books/{id}` 的 `Location` 响应头。

#### Scenario: 成功创建
- **GIVEN** 尚无图书使用提交值规范化后的 ISBN
- **WHEN** 客户端向 `POST /api/books` 提交合法图书
- **THEN** 服务持久化该图书
- **AND** 返回 HTTP 201、生成的标识符、规范化 ISBN 和新资源的 `Location` 响应头

#### Scenario: 缺少创建必填字段
- **GIVEN** 创建请求中的 `title`、`author` 或 `isbn` 缺失或为空白
- **WHEN** 客户端向 `POST /api/books` 提交请求
- **THEN** 服务使用统一错误结构返回 HTTP 400
- **AND** 不持久化图书

#### Scenario: 创建时 ISBN 冲突
- **GIVEN** 已有图书的规范化 ISBN 与创建请求相同
- **WHEN** 客户端向 `POST /api/books` 提交请求
- **THEN** 服务使用统一错误结构返回 HTTP 409
- **AND** 不创建重复图书

### Requirement: 获取图书列表
服务 SHALL 对 `GET /api/books` 返回 HTTP 200 和包含全部已存储图书的 JSON 数组。初始实现 SHALL 按 `id` 升序返回图书，且不提供分页、筛选或搜索功能。

#### Scenario: 获取非空目录
- **GIVEN** 已存储多本图书
- **WHEN** 客户端请求 `GET /api/books`
- **THEN** 服务返回 HTTP 200
- **AND** 按 `id` 升序返回全部已存储图书

#### Scenario: 获取空目录
- **GIVEN** 尚未存储任何图书
- **WHEN** 客户端请求 `GET /api/books`
- **THEN** 服务返回 HTTP 200 和空 JSON 数组

### Requirement: 查询单本图书
服务 SHALL 通过 `GET /api/books/{id}` 返回匹配的图书；当标识符不存在时返回 HTTP 404。

#### Scenario: 查询存在的图书
- **GIVEN** 请求标识符对应的图书存在
- **WHEN** 客户端请求 `GET /api/books/{id}`
- **THEN** 服务返回 HTTP 200 和该图书的资源表示

#### Scenario: 查询不存在的图书
- **GIVEN** 请求标识符对应的图书不存在
- **WHEN** 客户端请求 `GET /api/books/{id}`
- **THEN** 服务使用统一错误结构返回 HTTP 404

### Requirement: 完整替换图书
服务 SHALL 接受 `PUT /api/books/{id}`，完整替换可变字段 `title`、`author`、`isbn` 和 `publicationYear`，保留路径中的标识符，并返回 HTTP 200 和更新后的资源表示。提交的请求体 MUST 满足与创建操作相同的校验和唯一性规则。

#### Scenario: 成功替换
- **GIVEN** 请求标识符对应的图书存在，且提交的 ISBN 未分配给其他图书
- **WHEN** 客户端向 `PUT /api/books/{id}` 提交合法的完整替换请求
- **THEN** 服务更新全部可变字段
- **AND** 使用原标识符返回 HTTP 200 和更新后的资源表示

#### Scenario: 替换不存在的图书
- **GIVEN** 请求标识符对应的图书不存在
- **WHEN** 客户端向 `PUT /api/books/{id}` 提交合法请求
- **THEN** 服务使用统一错误结构返回 HTTP 404

#### Scenario: 拒绝非法替换
- **GIVEN** 替换请求包含空白必填字段或非法 ISBN
- **WHEN** 客户端向 `PUT /api/books/{id}` 提交请求
- **THEN** 服务使用统一错误结构返回 HTTP 400
- **AND** 保持已存储资源不变

#### Scenario: 替换时 ISBN 冲突
- **GIVEN** 提交值规范化后的 ISBN 属于另一已存储图书
- **WHEN** 客户端向 `PUT /api/books/{id}` 提交替换请求
- **THEN** 服务使用统一错误结构返回 HTTP 409
- **AND** 保持两本已存储图书不变

### Requirement: 删除图书
服务 SHALL 通过 `DELETE /api/books/{id}` 删除存在的图书并返回无响应体的 HTTP 204；当标识符不存在时返回 HTTP 404。

#### Scenario: 成功删除
- **GIVEN** 请求标识符对应的图书存在
- **WHEN** 客户端请求 `DELETE /api/books/{id}`
- **THEN** 服务删除该图书
- **AND** 返回 HTTP 204 且不包含响应体

#### Scenario: 删除不存在的图书
- **GIVEN** 请求标识符对应的图书不存在
- **WHEN** 客户端请求 `DELETE /api/books/{id}`
- **THEN** 服务使用统一错误结构返回 HTTP 404

### Requirement: ISBN 规范化与校验
服务 MUST 在校验或比较 ISBN 前移除空格和连字符，并将末尾小写 `x` 转为大写。服务 SHALL 只接受校验和正确的 ISBN-10 或 ISBN-13，返回并持久化规范化值，并对不支持的长度、非法字符或错误校验和返回 HTTP 400。ISBN-10 与 ISBN-13 校验 MUST 由两个可互换的 Strategy 分别实现，Factory MUST 根据规范化后的 ISBN 长度选择策略。

#### Scenario: 校验并规范化 ISBN-10
- **GIVEN** 客户端提供包含允许的空格或连字符且校验和正确的 ISBN-10
- **WHEN** 创建或替换操作处理该 ISBN
- **THEN** Factory 选择 ISBN-10 Strategy
- **AND** 服务接受并返回规范化后的 10 字符 ISBN

#### Scenario: 校验并规范化 ISBN-13
- **GIVEN** 客户端提供包含允许的空格或连字符且校验和正确的 ISBN-13
- **WHEN** 创建或替换操作处理该 ISBN
- **THEN** Factory 选择 ISBN-13 Strategy
- **AND** 服务接受并返回规范化后的 13 位 ISBN

#### Scenario: 拒绝非法 ISBN
- **GIVEN** 客户端提供长度不受支持、包含非法字符或校验和错误的 ISBN
- **WHEN** 创建或替换操作处理该 ISBN
- **THEN** 服务使用统一错误结构返回 HTTP 400
- **AND** 不执行数据库变更

### Requirement: ISBN 唯一性
服务 MUST 同时在应用层和数据库层对规范化 ISBN 强制实施唯一性约束。持久化阶段发生的唯一性竞争 MUST 转换为与预检查重复值相同的 HTTP 409 契约。

#### Scenario: 不同格式的等价 ISBN 发生冲突
- **GIVEN** 已存储图书的规范化 ISBN 为 `9780132350884`
- **WHEN** 客户端尝试使用 `978-0-13-235088-4` 创建另一图书
- **THEN** 服务使用统一错误结构返回 HTTP 409

#### Scenario: 并发唯一性冲突
- **GIVEN** 两个写请求竞争持久化同一规范化 ISBN
- **WHEN** 数据库通过唯一约束拒绝其中一次写入
- **THEN** 被拒绝的请求使用统一错误结构收到 HTTP 409

### Requirement: 统一错误表示
服务产生的每个 API 错误 SHALL 使用 JSON 表示，并包含 `timestamp`、数值 `status`、简短 `error`、可读 `message`、请求 `path` 和 `fieldErrors` 对象。非字段错误的 `fieldErrors` SHALL 为空对象；请求字段校验错误的 `fieldErrors` SHALL 将非法字段名映射到错误消息。

#### Scenario: 字段校验错误响应
- **GIVEN** 请求包含一个或多个非法字段
- **WHEN** 服务以 HTTP 400 拒绝该请求
- **THEN** JSON 响应包含全部统一错误字段
- **AND** `fieldErrors` 标识所有被拒绝的请求字段

#### Scenario: 非字段错误响应
- **GIVEN** 请求目标资源不存在或 ISBN 与现有数据冲突
- **WHEN** 服务返回 HTTP 404 或 HTTP 409
- **THEN** JSON 响应包含全部统一错误字段
- **AND** `fieldErrors` 为空对象

### Requirement: 自动化验证与覆盖率门禁
项目 MUST 提供使用 JUnit 5 和 Mockito 的业务行为及 ISBN Strategy/Factory 单元测试，并提供基于隔离 H2 数据库的 Spring Boot MockMvc API 测试，覆盖成功、校验失败、资源不存在和冲突场景。Maven `verify` MUST 生成 JaCoCo 报告，并在项目聚合行覆盖率低于 80% 时使构建失败。

#### Scenario: 达到阈值时验证成功
- **GIVEN** 全部自动化测试通过，且聚合行覆盖率不低于 80%
- **WHEN** 评审者从干净检出执行 Maven Wrapper 的 `verify` 生命周期
- **THEN** 构建成功并生成测试报告和 JaCoCo 报告

#### Scenario: 低于阈值时验证失败
- **GIVEN** 项目聚合行覆盖率低于 80%
- **WHEN** Maven 在 `verify` 阶段执行 JaCoCo 检查
- **THEN** 构建因未满足覆盖率规则而失败

### Requirement: 面向人员和 AI 的项目文档
仓库 MUST 包含 README，用于说明前置条件、构建/运行/测试命令、API 请求与响应、架构、持久化选择、错误行为，以及 Strategy 和 Factory 的具体职责。仓库还 MUST 在 `docs/ai/prompts/` 下保存本作业使用的每个完整 AI 提示词，在 AI 使用日志中记录日期、工具、已知模型、用途、结果和人工修改，并保留开发过程中使用的 OpenSpec 规划工件及生成的 agent skill 文件。

#### Scenario: 评审者按照项目文档操作
- **GIVEN** 评审者拥有干净的代码检出和 Java 21
- **WHEN** 评审者按照 README 操作
- **THEN** 评审者能够构建、测试、启动服务并调用全部 CRUD 端点
- **AND** 能够定位两种设计模式的实现与测试

#### Scenario: 评审者审计 AI 使用情况
- **GIVEN** 已提交的代码仓库
- **WHEN** 评审者检查 `docs/ai/`、`openspec/` 和生成的 agent skill 文件
- **THEN** 评审者能够阅读完整提示词，并将每项 AI 辅助活动追溯到用途、结果和声明的人工修改

### Requirement: 范围排除
交付的服务 SHALL 不提供图形用户界面或认证流程，并 SHALL 排除分页、搜索、分布式基础设施以及本变更未要求的其他功能。

#### Scenario: 检查作业范围
- **GIVEN** 已完成的服务
- **WHEN** 评审者检查其公开端点和依赖项
- **THEN** 仅存在指定的无认证图书目录 API，以及框架可能提供的辅助运行端点
- **AND** 不包含前端实现
