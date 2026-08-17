# AI 使用日志

> 时区：Asia/Shanghai。仅记录用户提供的输入、仓库中可见的 OpenSpec skill 和可观察结果；无法访问的服务提供方内部提示不作虚假声明。

| 日期 | 工具与模型 | 用途 | 输入证据 | 结果 | 人工修改 |
|---|---|---|---|---|---|
| 2026-08-17 | Codex（GPT-5；具体部署快照未显示） | 检查 OpenSpec 安装并解释规格驱动流程 | `001-original-assignment.md` | 确认 OpenSpec 1.9.0 可用并给出实施流程 | 无 |
| 2026-08-17 | Codex + OpenSpec 1.9.0 `openspec-propose` skill | 生成图书目录服务的 proposal、spec、design 和 tasks | `002-openspec-proposal.md`、`.agents/skills/openspec-propose/SKILL.md` | 生成 `build-book-catalog-service` 变更并通过严格校验 | 用户要求将正文改为中文 |
| 2026-08-17 | Codex + OpenSpec 1.9.0 `openspec-update-change` skill | 将规划工件正文转换为简体中文并保持结构关键字 | `003-language-and-workflow.md`、`.agents/skills/openspec-update-change/SKILL.md` | 四个规划工件完成中文化并再次通过严格校验 | 无 |
| 2026-08-17 | Codex + OpenSpec 1.9.0 `openspec-apply-change` skill | 按任务清单实现、测试和记录本项目 | `004-openspec-apply.md`、`.agents/skills/openspec-apply-change/SKILL.md` | 完成 Spring Boot CRUD 服务、Strategy/Factory、统一错误、46 个自动化测试及文档；Java 21.0.2 下 `./mvnw clean verify` 成功，JaCoCo 行覆盖率 98.59%；五个 API 冒烟测试通过 | 用户提前确认 OpenSpec 规格使用中文；实施阶段无额外人工代码修改 |
| 2026-08-17 | Codex（GPT-5；具体部署快照未显示） | 仅修改项目名称 | `005-rename-project.md` | 将 Maven artifact/name、Spring application name 和 README 标题改为 `library-service`，不改 Java 包名、API、目录或历史 OpenSpec 归档；Java 21 下 `./mvnw clean verify` 成功，46 个测试通过 | 用户确认使用对话中的示例名称 |
| 2026-08-17 | Codex + GitHub CLI 2.96.0 | 创建并发布 GitHub 仓库 | `006-github-publish.md` | 创建公开仓库 `a16496528/bookcatalog`，将已验证的项目以提交 `f356f5c` 首次推送到 `main`；构建产物、H2 数据、IDE 文件和无关工具文件未发布 | 用户明确授权创建公开仓库 |

## 最终验证记录

- 2026-08-17：核对 `docs/ai/prompts/001` 至 `005`，已覆盖原始作业需求、OpenSpec proposal、中文化/工作流交互、apply 和项目重命名提示词。
- 2026-08-17：使用经 SHA-256 校验的 OpenJDK 21.0.2 执行 `./mvnw clean verify`，46 个测试全部通过，JaCoCo 行覆盖率 98.59% (140/142)。
- 2026-08-17：启动打包后的 JAR，使用临时内存 H2 冒烟测试 POST、列表 GET、单项 GET、PUT 和 DELETE；状态码分别为 201、200、200、200、204，删除后 GET 为统一 JSON 404。
