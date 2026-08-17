# OpenSpec 验收场景映射

规格来源：`openspec/changes/build-book-catalog-service/specs/book-catalog/spec.md`。

| Requirement / Scenario | 验证证据 |
|---|---|
| 图书资源表示 / 返回图书资源 | `BookApiTest#exercisesCompleteCrudLifecycleAndOrderedListing`、`BookMapperTest#replacementPreservesIdentifierAndMapsExactPublicFields` |
| 创建图书 / 成功创建 | `BookApiTest#exercisesCompleteCrudLifecycleAndOrderedListing` |
| 创建图书 / 缺少创建必填字段 | `BookApiTest#returnsStandardFieldErrorsForBlankRequiredFieldsAndInvalidIsbn` |
| 创建图书 / 创建时 ISBN 冲突 | `BookApiTest#rejectsNormalizedCreateAndReplacementConflictsWithoutChangingStoredData` |
| 获取图书列表 / 获取非空目录 | `BookApiTest#exercisesCompleteCrudLifecycleAndOrderedListing` |
| 获取图书列表 / 获取空目录 | `BookApiTest#exercisesCompleteCrudLifecycleAndOrderedListing` |
| 查询单本图书 / 查询存在的图书 | `BookApiTest#exercisesCompleteCrudLifecycleAndOrderedListing` |
| 查询单本图书 / 查询不存在的图书 | `BookApiTest#returnsStandardNotFoundForEveryItemMutation` |
| 完整替换图书 / 成功替换 | `BookApiTest#exercisesCompleteCrudLifecycleAndOrderedListing` |
| 完整替换图书 / 替换不存在的图书 | `BookApiTest#returnsStandardNotFoundForEveryItemMutation` |
| 完整替换图书 / 拒绝非法替换 | `BookApiTest#rejectsInvalidReplacementWithoutChangingStoredBook` |
| 完整替换图书 / 替换时 ISBN 冲突 | `BookApiTest#rejectsNormalizedCreateAndReplacementConflictsWithoutChangingStoredData` |
| 删除图书 / 成功删除 | `BookApiTest#exercisesCompleteCrudLifecycleAndOrderedListing` |
| 删除图书 / 删除不存在的图书 | `BookApiTest#returnsStandardNotFoundForEveryItemMutation` |
| ISBN 规范化与校验 / 校验并规范化 ISBN-10 | `Isbn10ValidationStrategyTest`、`IsbnServiceTest#normalizesFormattingAndLowercaseTerminalXBeforeValidation`、Factory 测试 |
| ISBN 规范化与校验 / 校验并规范化 ISBN-13 | `Isbn13ValidationStrategyTest`、`BookApiTest#exercisesCompleteCrudLifecycleAndOrderedListing`、Factory 测试 |
| ISBN 规范化与校验 / 拒绝非法 ISBN | `IsbnServiceTest#rejectsFailedChecksum`、`BookApiTest#returnsStandardFieldErrorsForBlankRequiredFieldsAndInvalidIsbn` |
| ISBN 唯一性 / 不同格式的等价 ISBN 发生冲突 | `BookApiTest#rejectsNormalizedCreateAndReplacementConflictsWithoutChangingStoredData` |
| ISBN 唯一性 / 并发唯一性冲突 | `BookRepositoryTest#databaseRejectsDuplicateIsbn`、`BookServiceTest#translatesDatabaseUniquenessRace` |
| 统一错误表示 / 字段校验错误响应 | `BookApiTest#returnsStandardFieldErrorsForBlankRequiredFieldsAndInvalidIsbn` |
| 统一错误表示 / 非字段错误响应 | `BookApiTest#returnsStandardNotFoundForEveryItemMutation`、冲突测试 |
| 自动化验证 / 达到阈值时验证成功 | 2026-08-17 使用 Java 21.0.2 执行 `./mvnw clean verify`：46 个测试全部通过，JaCoCo 行覆盖率 98.59% (140/142)，门禁通过；详见 Surefire 报告及 `target/site/jacoco/index.html` |
| 自动化验证 / 低于阈值时验证失败 | 手工审查 `pom.xml` 中 `jacoco:check` 的 `LINE/COVEREDRATIO/0.80` 规则；CI/评审可临时提高阈值验证失败后撤销临时修改 |
| 项目文档 / 评审者按照文档操作 | 2026-08-17 按 README 启动打包后的服务，实际验证 POST 201 + `Location`、列表 GET 200、单项 GET 200、PUT 200、DELETE 204，且删除后 GET 返回统一 JSON 404；详见 AI 使用日志 |
| 项目文档 / 评审者审计 AI 使用情况 | 检查 `docs/ai/`、`openspec/`、`.agents/skills/openspec-*` |
| 范围排除 / 检查作业范围 | 手工检查 Controller、依赖项和仓库文件树：无认证、前端、分页、搜索或分布式组件 |

## 最终验证摘要

- 运行时：OpenJDK 21.0.2（macOS aarch64）。
- 构建命令：`./mvnw clean verify`，结果 `BUILD SUCCESS`。
- 自动化测试：46 个，0 失败，0 错误，0 跳过。
- JaCoCo 行覆盖率：98.59% (140/142)，高于 80% 强制阈值。
- 冒烟测试：五个 CRUD 端点全部通过，临时使用内存 H2，停止后无本地数据文件遗留。
