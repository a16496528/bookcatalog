## 1. 项目与证据准备

- [x] 1.1 创建 `docs/ai/prompts/`，使用编号 Markdown 文件逐字保存作业要求和本次 OpenSpec proposal 提示词。
- [x] 1.2 创建 `docs/ai/usage-log.md`，包含日期/时区、工具、界面暴露的模型名称、用途、结果和人工修改字段，并记录本次规划过程。
- [x] 1.3 创建 Java 21 Spring Boot Maven 项目和 Maven Wrapper，引入固定版本的 Web、Validation、Data JPA、H2 与测试依赖。
- [x] 1.4 配置基于文件的本地 H2 数据源和隔离的内存 H2 测试 Profile，并设置可预测的 JPA Schema 行为。
- [x] 1.5 配置 JaCoCo Agent、报告和 bundle 级行覆盖率 `0.80` 检查，在 Maven `verify` 阶段执行，且不排除应用代码包。

## 2. 领域与持久化

- [x] 2.1 实现 `Book` JPA 实体，包括生成的 `Long` 标识符、必填 title/author/规范化 ISBN 列、可空出版年份，以及 ISBN 数据库唯一约束。
- [x] 2.2 实现 `BookRepository`，支持按 ID 升序列表，以及创建和替换所需的 ISBN 存在性查询。
- [x] 2.3 实现图书不存在、ISBN 冲突和 ISBN 非法等领域异常，且不依赖 HTTP 层。
- [x] 2.4 增加 JPA Repository 测试，使用 H2 验证 ISBN 唯一约束和 Repository 查询。

## 3. ISBN Strategy 与 Factory 模式

- [x] 3.1 定义 `IsbnValidationStrategy` 产品接口，并实现验证校验和的 ISBN-10 与 ISBN-13 Strategy。
- [x] 3.2 定义 `IsbnValidationStrategyFactory` 创建者契约，并实现基于长度的 Strategy 注册、选择和不支持格式拒绝逻辑。
- [x] 3.3 实现 `IsbnService` 规范化与校验，保证每个接受值都是规范格式，且每次创建/替换都会调用 Factory 选择的 Strategy。
- [x] 3.4 增加参数化单元测试，覆盖合法/非法 ISBN-10 与 ISBN-13、末尾 `X` 规则和格式错误字符。
- [x] 3.5 增加针对性单元测试，证明 Factory 对两种格式的选择、不支持长度的拒绝，以及空格、连字符和末尾小写 `x` 的规范化。

## 4. 应用层

- [x] 4.1 实现经过校验的创建/替换请求 DTO 和图书响应 DTO，不暴露 JPA 实体，也不在请求体中接受 ID。
- [x] 4.2 实现 API DTO 与实体之间的手写 `BookMapper`，并在替换时保留路径标识符。
- [x] 4.3 实现事务性 `BookService` 创建与替换操作，包括 ISBN 规范化、应用层唯一性检查和持久化竞争冲突转换。
- [x] 4.4 实现 Service 列表、单项查询和删除操作，保证 ID 升序和一致的图书不存在行为。
- [x] 4.5 增加 Mockito 单元测试，覆盖每个 Service 成功路径、资源不存在、规范化后重复、数据库唯一性竞争，以及替换被拒后资源保持不变。
- [x] 4.6 增加 Mapper 单元测试，确认公开资源字段和替换语义完全符合规格。

## 5. REST API 与错误契约

- [x] 5.1 实现 `/api/books` 下的 POST、列表 GET、单项 GET、完整 PUT 和 DELETE 端点，并返回规定的 201/200/204 状态。
- [x] 5.2 构造创建响应的 `Location` 头，并保证成功删除不包含响应体。
- [x] 5.3 实现 `ApiError` 和 `@RestControllerAdvice`，映射 Bean Validation/非法 ISBN（400）、图书不存在（404）和 ISBN 冲突（409），并生成确定性的 `fieldErrors`。
- [x] 5.4 保证未预期异常返回不泄露内部信息的统一 500 错误结构，且非法 JSON/请求绑定错误保持统一结构。

## 6. API 测试与质量门禁

- [x] 6.1 增加全上下文 MockMvc/H2 测试，覆盖成功创建、非空与空列表、查询、替换和删除，并断言响应体与 `Location` 头。
- [x] 6.2 增加 MockMvc 测试，覆盖空白必填字段、格式错误或校验和错误的 ISBN，以及完整的 400 错误/字段错误结构。
- [x] 6.3 增加 MockMvc 测试，覆盖查询/替换/删除的 404，以及完整的非字段错误结构。
- [x] 6.4 增加 MockMvc 测试，覆盖创建和替换时规范化 ISBN 冲突、拒绝后数据保持不变，以及 409 错误结构。
- [x] 6.5 运行 `./mvnw clean verify`，修复全部失败，并确认 JaCoCo 报告的聚合行覆盖率不低于 80% 且门禁已启用。

## 7. 文档与最终审计

- [x] 7.1 编写 README，说明 Java 前置条件、Maven Wrapper 构建/测试/运行命令、H2 存储行为，以及每个 CRUD 端点和错误类别的 curl 示例。
- [x] 7.2 记录包架构，指出具体的 Strategy 产品、Factory 创建者/具体 Factory、运行时调用路径及对应测试。
- [x] 7.3 将每个实施与验证提示词逐字补充到 `docs/ai/prompts/`，并完善使用日志中的生成结果和人工修改记录。
- [x] 7.4 确认提交中保留 `openspec/` 和 `.agents/skills/openspec-*`，且未跟踪密钥、机器凭据、构建产物或 H2 数据文件。
- [x] 7.5 从干净工作状态重新运行 `./mvnw clean verify`，检查单元/API/JaCoCo 报告，并使用 README 命令手工冒烟测试全部五个端点。
- [x] 7.6 将 `specs/book-catalog/spec.md` 的每个场景映射到至少一个自动化测试或已记录的手工验证，并在请求归档前记录最终 AI 使用情况。
