# library-service

一个基于 Java 21、Spring Boot 3.3.5 和 Maven 的 RESTful 图书目录微服务。项目没有前端或认证功能，使用 Spring Data JPA 持久化图书，并通过 H2 保持本地运行自包含。

## 技术栈

- Java 21
- Spring Boot 3.3.5
- Spring Web、Jakarta Validation、Spring Data JPA
- H2（本地文件数据库与测试内存数据库）
- JUnit 5、Mockito、MockMvc
- JaCoCo 0.8.12（行覆盖率门禁 80%）
- Maven Wrapper 3.9.10

## 环境准备

确认 Java 版本：

```bash
java -version
```

macOS/Homebrew 用户可以安装并临时启用 JDK 21：

```bash
brew install openjdk@21
export JAVA_HOME="$(brew --prefix openjdk@21)/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

无需单独安装 Maven，仓库包含 Maven Wrapper。

## 构建与测试

```bash
./mvnw clean verify
```

该命令会：

1. 编译 Java 21 项目；
2. 运行 JUnit 5/Mockito 单元测试；
3. 运行 Spring Boot/MockMvc/H2 API 测试；
4. 在 `target/site/jacoco/index.html` 生成 JaCoCo 报告；
5. 当项目聚合行覆盖率低于 80% 时令构建失败。

核心应用包没有被排除在覆盖率计算之外。

## 启动服务

```bash
./mvnw spring-boot:run
```

服务默认监听 `http://localhost:8080`。默认配置使用文件数据库 `./data/bookcatalog`，因此数据可跨应用重启保留；测试使用独立的内存 H2，并在测试结束时销毁。

## API

图书 JSON 字段：

```json
{
  "id": 1,
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "isbn": "9780132350884",
  "publicationYear": 2008
}
```

`title`、`author` 和 `isbn` 必填。ISBN 可以包含空格和连字符；服务会删除这些分隔符并将末尾 `x` 转为 `X`，然后使用 ISBN-10 或 ISBN-13 校验和算法验证并保存规范值。规范化后的 ISBN 必须唯一。

### 创建图书：POST /api/books

```bash
curl -i -X POST http://localhost:8080/api/books \
  -H 'Content-Type: application/json' \
  -d '{
    "title":"Clean Code",
    "author":"Robert C. Martin",
    "isbn":"978-0-13-235088-4",
    "publicationYear":2008
  }'
```

成功返回 HTTP 201、创建后的图书，以及 `Location: /api/books/{id}`。

### 获取全部图书：GET /api/books

```bash
curl -i http://localhost:8080/api/books
```

成功返回 HTTP 200 和按 `id` 升序排列的 JSON 数组；空目录返回 `[]`。

### 查询单本图书：GET /api/books/{id}

```bash
curl -i http://localhost:8080/api/books/1
```

存在时返回 HTTP 200，不存在时返回 HTTP 404。

### 完整替换图书：PUT /api/books/{id}

```bash
curl -i -X PUT http://localhost:8080/api/books/1 \
  -H 'Content-Type: application/json' \
  -d '{
    "title":"Clean Code, Second Edition",
    "author":"Robert C. Martin",
    "isbn":"9780132350884",
    "publicationYear":2025
  }'
```

PUT 是完整替换：请求必须重新提供所有必填字段。成功返回 HTTP 200 并保留路径中的 `id`。

### 删除图书：DELETE /api/books/{id}

```bash
curl -i -X DELETE http://localhost:8080/api/books/1
```

成功返回 HTTP 204 且没有响应体；资源不存在时返回 HTTP 404。

## 错误结构

HTTP 400、404、409 和 500 都使用相同结构：

```json
{
  "timestamp": "2026-08-17T12:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "A book with ISBN 9780132350884 already exists",
  "path": "/api/books",
  "fieldErrors": {}
}
```

- HTTP 400：空白必填字段、非法 JSON、未知请求字段、非法路径参数、非法 ISBN。
- HTTP 404：目标图书不存在。
- HTTP 409：规范化 ISBN 已由另一图书使用，包括数据库唯一约束竞争。
- HTTP 500：未预期错误；响应不会泄露堆栈信息。

字段校验失败时，`fieldErrors` 将字段名映射到错误消息；404、409 和其他非字段错误返回空对象。

## 架构

```text
HTTP request
    ↓
api/BookController + DTO + GlobalExceptionHandler
    ↓
application/BookService + BookMapper
    ├── isbn/IsbnService → Factory → Strategy
    └── persistence/BookRepository → H2
```

- `api`：HTTP 路由、请求/响应 DTO、统一异常响应。
- `application`：事务、CRUD 协调、DTO/Entity 映射。
- `domain`：JPA `Book` 实体和不依赖 HTTP 的领域异常。
- `isbn`：ISBN 规范化及设计模式实现。
- `persistence`：Spring Data JPA Repository。

JPA Entity 不会被 Controller 直接序列化；公开 JSON 由 `BookRequest` 和 `BookResponse` 定义。

## 设计模式

### Strategy

产品接口 `IsbnValidationStrategy` 定义 `supportedLength()` 和 `isValid()`：

- `Isbn10ValidationStrategy` 实现 ISBN-10 校验和算法；
- `Isbn13ValidationStrategy` 实现 ISBN-13 校验和算法。

测试位于 `Isbn10ValidationStrategyTest` 和 `Isbn13ValidationStrategyTest`，覆盖合法值、错误校验和、非法字符、长度和末尾 `X`。

### Factory

创建者契约 `IsbnValidationStrategyFactory` 隔离 Strategy 选择。`LengthBasedIsbnValidationStrategyFactory` 将规范化长度映射到具体 Strategy，不受支持的长度会产生领域校验错误。

运行时调用链为：

```text
BookService.create/replace
  → IsbnService.validateAndNormalize
  → IsbnValidationStrategyFactory.forIsbn
  → Isbn10ValidationStrategy 或 Isbn13ValidationStrategy
```

测试位于 `LengthBasedIsbnValidationStrategyFactoryTest` 和 `IsbnServiceTest`，证明两个选择分支、非法长度、格式规范化及真实调用关系。

## 测试结构

- `BookServiceTest`：Mockito 单元测试，覆盖业务成功与异常分支。
- `BookMapperTest`：DTO/Entity 映射与 ID 保留。
- `BookRepositoryTest`：H2 唯一约束、查询和升序列表。
- `BookApiTest`：全 Spring 上下文的 MockMvc/H2 API 测试。
- `docs/testing/scenario-matrix.md`：OpenSpec 场景到自动化或手工验证的映射。

## AI 与 OpenSpec 证据

- `docs/ai/prompts/`：本作业使用的用户提示词原文。
- `docs/ai/usage-log.md`：日期、工具/模型、用途、结果和人工修改。
- `openspec/`：proposal、行为规格、技术设计和任务历史。
- `.agents/skills/openspec-*`：本项目使用的 OpenSpec/Codex skill 指令。

服务不包含认证、前端、分页、搜索或分布式基础设施。
