## Why

课程作业要求实现一个小型、可测试的 Spring Boot 微服务，用于展示 RESTful CRUD、设计模式的有意识运用、可量化的自动化测试覆盖率，以及透明的 AI 辅助开发证据。需要先通过规格明确 API 契约、错误行为、设计模式职责、质量门禁和提交证据，以便在编写代码前完成评审。

## What Changes

- 新增基于 Java 21、Maven 和 Spring Boot 的图书目录服务，不包含用户界面或认证。
- 在 `/api/books` 下提供 RESTful 的创建、列表、单项查询、完整替换和删除操作；使用请求/响应 DTO，不直接暴露持久化实体。
- 通过 Spring Data JPA 持久化图书，强制校验必填元数据和 ISBN 唯一性，并为参数校验失败、资源不存在和数据冲突提供稳定的 JSON 错误契约。
- 使用 Strategy 模式实现 ISBN-10 与 ISBN-13 校验和算法，并使用 Factory 根据规范化后的 ISBN 形式选择对应策略。
- 增加 JUnit 5/Mockito 单元测试，以及使用 H2 的 Spring Boot/MockMvc API 测试，覆盖成功与失败路径。
- 在 Maven `verify` 生命周期中加入不低于 80% 的 JaCoCo 行覆盖率门禁。
- 增加项目文档，说明环境准备、运行方式、API 示例、架构和设计模式的具体实现。
- 保存完整提示词、AI 使用日志、OpenSpec 工件以及生成的 agent skill，作为可评审的作业提交证据。

## Capabilities

### New Capabilities

- `book-catalog`：定义公开 CRUD API、图书校验及唯一性行为、统一错误结构、ISBN 策略选择、自动化验证要求，以及必须提交的项目与 AI 使用文档。

### Modified Capabilities

无。

## Impact

- 在当前仓库根目录新增独立的 Spring Boot 服务及 Maven 构建。
- 引入 Spring Web、Jakarta Validation、Spring Data JPA、H2 测试/运行支持、JUnit 5、Mockito、MockMvc 和 JaCoCo。
- 将 `/api/books` 定义为公开 HTTP 接口；不修改任何现有 API。
- 增加由版本库管理的 OpenSpec 工件、生成的 Codex/OpenSpec skills、README 和 `docs/ai/` 证据。
