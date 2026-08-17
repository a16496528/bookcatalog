## Context

这是一个全新的单模块课程项目；动机见 `proposal.md`，行为契约见 `specs/book-catalog/spec.md`。项目必须能够在具有 Java 21 和 Maven 的干净检出环境中运行，规模应便于评审，必须在可执行代码中明确展示两种设计模式，并使 80% 覆盖率结论可复现而不是仅停留在文字说明。

## Goals / Non-Goals

**Goals:**

- 将 HTTP、应用服务、校验、映射和持久化职责分离，使其能够独立测试。
- 通过包结构、运行时装配和针对性测试清晰展示 Strategy 与 Factory 的参与方式。
- 在不依赖外部基础设施的情况下提供确定性的本地运行环境和隔离的 API 测试。
- 通过标准 Maven 生命周期强制执行覆盖率门禁。
- 保存足够的 AI 开发证据，使评审者能够还原规划过程。

**Non-Goals:**

- 不实现领域驱动或分布式服务基础设施、异步消息、认证、部署清单和生产数据库迁移工具。
- 不采用会隐藏课程要求设计决策的通用 CRUD 框架、代码生成器或重度反射映射工具。
- 不实现分页、局部更新、搜索和 API 版本协商。

## Decisions

### 1. 使用单 Maven 模块和常规分层包结构

使用已固定版本、兼容 Java 21 的 Spring Boot 3.x 和 Maven Wrapper。生产代码置于 `com.example.bookcatalog` 下，并划分为 `api`、`application`、`domain`、`isbn` 和 `persistence` 包：

- `api`：Controller、请求/响应 record、错误 record 和全局异常处理器。
- `application`：事务性 `BookService` 和轻量手写 `BookMapper`。
- `domain`：JPA `Book` 实体和领域异常。
- `isbn`：规范化逻辑、校验 Strategy 和 Strategy Factory。
- `persistence`：Spring Data `BookRepository`。

Controller 只负责 HTTP；Service 协调规范化、校验、唯一性、映射和持久化。DTO 尽可能使用 Java record，JPA 实体使用普通类且不承担公开序列化职责。

备选方案：六边形多模块设计能提供更强边界，但对于单一 CRUD 聚合过于繁琐；Controller 直接访问 Repository 虽然代码更短，却难以隔离校验、事务、错误处理和单元测试职责。

### 2. 将 Strategy 与 Factory 实现为一等 ISBN 协作者

定义 `IsbnValidationStrategy` 产品接口，显式声明所支持的规范化长度和校验操作。分别实现 `Isbn10ValidationStrategy` 与 `Isbn13ValidationStrategy` 的校验和算法。定义 `IsbnValidationStrategyFactory` 创建者契约，并实现基于长度的具体 Factory：接收所有已注册 Strategy，构建不可变的“长度到 Strategy”注册表，返回适用 Strategy，或对不受支持的格式抛出校验异常。

`IsbnService` 首先删除空格和连字符并将小写 `x` 转为大写，然后请求 Factory 选择 Strategy、执行校验，最终只返回合法的规范化 ISBN。应用服务依赖 `IsbnService`，因此每次创建和替换都会实际经过两种模式，而不是保留未使用的示例类。

备选方案：在单个校验器中使用 `if/else` 更短，但无法展示可互换算法；由 `BookService` 直接选择具体校验器会令应用逻辑依赖具体产品，也不能体现所要求的 Factory 职责。

### 3. 在应用层和数据库层共同强制 ISBN 唯一性

将规范化 ISBN 存储在非空且具有唯一约束的列中。创建前调用 `existsByIsbn`；替换前调用 `existsByIsbnAndIdNot`。这些检查用于返回清晰的冲突消息。同时将 `DataIntegrityViolationException` 转为领域冲突异常，使并发写入也得到相同的 HTTP 409 响应。

创建和替换操作使用事务。替换时先加载目标，再检查唯一性，从而保证不存在的标识符始终返回 404。路径标识符具有权威性，请求 DTO 不包含 `id`。

备选方案：只做应用层检查存在竞争条件；只依赖数据库约束虽然正确，但正常冲突路径的反馈不够清晰。

### 4. 使用稳定的异常到错误响应转换边界

使用 `@RestControllerAdvice` 将请求绑定/Bean Validation 与非法 ISBN 映射为 400，将图书不存在映射为 404，将 ISBN 冲突映射为 409。统一构造 `ApiError` DTO，包含 ISO-8601 时间戳、状态码、错误名称、消息、路径和按字段名确定排序的错误映射。未预期异常保持 Spring 的 500 状态，但不泄露堆栈信息。

API Controller 返回 `ResponseEntity`：创建操作构造 `Location` URI；替换、查询返回 200；列表返回 200；成功删除返回 204。

备选方案：Spring 默认 Problem 响应代码更少，但不满足作业要求的统一结构；从 Service 抛出 HTTP 专用异常会令业务逻辑耦合 Web 层。

### 5. 使用自包含 H2 Profile

默认本地 Profile 使用基于文件的 H2，使图书在应用重启后仍然存在且无需外部服务。测试 Profile 使用隔离的内存 H2，并在测试运行时重建 Schema。对于该全新课程项目，使用 JPA Schema 生成可以接受；ISBN 唯一约束在实体映射中声明。

备选方案：PostgreSQL 加 Testcontainers 更接近生产环境，但会引入 Docker 和外部镜像可用性要求，不利于从干净检出直接评审。默认使用纯内存数据库更简单，但对一个标明支持持久化的目录服务而言容易造成误解。

### 6. 分离针对性单元测试和 HTTP 级 API 测试

JUnit 5 参数化测试覆盖合法/非法 ISBN-10 与 ISBN-13 校验和、规范化、不支持的格式和 Factory 选择。基于 Mockito 的 `BookServiceTest` 在不启动 Spring 的情况下覆盖全部 CRUD 路径、Mapper 交互、唯一性判断和异常转换。必要的 Repository 测试在数据库边界验证规范化 ISBN 唯一性。

使用 `@SpringBootTest` 与 `@AutoConfigureMockMvc` 的 API 测试执行真实 JSON 序列化、Bean Validation、Controller Advice、Service、JPA 和 H2。每个测试前清空测试数据库。测试断言 201/200/204 的响应体和响应头，以及 400/404/409 的统一错误结构。

备选方案：只使用 `@WebMvcTest` 虽然更快，但会模拟持久化层，不能证明 API 到数据库的契约；只使用全上下文测试则难以快速定位失败，因此仍需配合针对性单元测试。

### 7. 将 JaCoCo 设置为可执行的构建门禁

在测试前绑定 JaCoCo Agent，在 `verify` 阶段生成报告并执行 `jacoco:check`，以 bundle 级 `LINE` 覆盖率 `0.80` 为最低要求。不得排除 Controller、Service、异常处理、ISBN 组件、Entity 或 Mapper；应用启动类同样保留在统计范围内，通过测试真正达到目标，而不是调整排除项。

将 `./mvnw clean verify` 作为唯一推荐验证命令。Surefire 运行全部单元测试和 MockMvc API 测试；JaCoCo HTML/XML 报告保存在 `target/site/jacoco/`。

备选方案：只生成报告而不配置 `check` 无法保证阈值；只检查部分包会使作业的覆盖率声明具有误导性。

### 8. 将文档和 AI 证据作为版本化交付物

README 包含前置条件、Maven Wrapper 命令、本地数据库行为、每个端点的 curl 示例、错误结构、包架构，以及 Strategy 与 Factory 的类级职责和对应测试。`docs/ai/prompts/` 使用编号 Markdown 文件逐字保存每个用户提示；`docs/ai/usage-log.md` 记录日期/时区、工具、界面暴露的模型、用途、输出以及人工修改，或明确声明未作人工修改。

提交 `openspec/` 和生成的 `.agents/skills/openspec-*` 内容。不得声称能够访问隐藏的平台或系统提示；日志要区分用户输入、仓库内可见的 skill 指令和无法取得的服务提供方内部内容。

备选方案：只依赖聊天记录或截图不便比较、搜索和评分，也可能遗漏上下文。

## Risks / Trade-offs

- [Spring Boot 3.x 补丁版本会变化] → 在 `pom.xml` 中固定一个兼容 Java 21 的补丁版本，并记录在 README 中，不使用浮动版本。
- [H2 SQL 行为与生产数据库存在差异] → 保持 JPA 操作可移植，并明确 H2 是为自包含作业选择，而非生产等价环境。
- [预检查唯一性仍存在竞争窗口] → 保留数据库唯一约束，并将其异常映射为 409。
- [只有两个 Strategy 时 Factory 可能显得形式化] → 让所有写操作经过 Factory，并测试两个选择分支和不支持的输入。
- [严格断言错误字段可能使测试脆弱] → 将已记录的错误结构视为有意设计的公开契约，只对必要语义断言消息内容。
- [覆盖率百分比可能鼓励浅层断言] → 将阈值与场景化 API 测试、分支导向单元测试以及 OpenSpec 需求映射结合。
- [AI 证据可能遗漏后续提示] → 在任务清单中多次明确要求捕获提示词，并在最终验证前再次检查日志。

## Migration Plan

当前不存在需要迁移的运行环境或数据。实现顺序为：Maven 骨架与 Profile、领域/持久化层、ISBN 模式组件、应用/API 层、自动化测试与覆盖率门禁，最后完成文档和证据。若实施失败，可回退到版本库上一修订；无需维护外部数据库 Schema 或既有调用方兼容性。
