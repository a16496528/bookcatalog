# OpenSpec Proposal 提示词

> 记录方式：逐字保存用户输入。

```text
$openspec-propose build-book-catalog-service

为课程作业规划并构建一个“图书目录服务”。

功能要求：
1. 基于 Spring Boot、Java 21、Maven 构建，不需要前端界面。
2. 提供 RESTful 图书 CRUD API：
   - POST /api/books
   - GET /api/books
   - GET /api/books/{id}
   - PUT /api/books/{id}
   - DELETE /api/books/{id}
3. Book 至少包含：
   - id
   - title
   - author
   - isbn
   - publicationYear
4. title、author、isbn 必填；ISBN 唯一；请求错误需要返回统一 JSON 错误结构。
5. 明确定义 201、200、204、400、404、409 等 HTTP 响应。
6. 使用 Spring Data JPA；测试环境使用 H2。
7. 至少明确实现两种 GoF 设计模式：
   - Strategy：分别实现 ISBN-10 和 ISBN-13 校验策略。
   - Factory：根据 ISBN 格式选择对应校验策略。
   不要只在文档中声称使用设计模式，代码结构和测试必须能体现这些模式。
8. 使用 DTO，不直接暴露 JPA Entity。
9. 编写：
   - JUnit 5 和 Mockito 单元测试；
   - Spring Boot MockMvc API 集成测试；
   - 正常、校验失败、不存在、ISBN 冲突等测试场景。
10. 配置 JaCoCo，在 Maven verify 阶段强制要求行覆盖率不低于 80%，低于阈值时构建失败。
11. 创建 README，说明运行、测试、API 示例、架构和设计模式。
12. 保存所有 AI 使用记录：
    - docs/ai/prompts/ 保存完整提示词；
    - docs/ai/usage-log.md 记录日期、AI 工具、模型、用途、结果及人工修改；
    - 提交 OpenSpec 生成的规格和 Codex/OpenSpec 技能文件。
13. 不实现认证、前端或超出作业范围的复杂功能。

请生成完整的 proposal、行为规格、design 和 tasks。先不要写业务代码。
规格中的每条关键需求都必须有 GIVEN/WHEN/THEN 验收场景。
```

