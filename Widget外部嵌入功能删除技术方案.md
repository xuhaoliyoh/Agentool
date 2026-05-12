# Widget 外部嵌入功能删除技术方案

## 1. 背景与目标

本方案用于指导后续 Codex 对 AgentX 中的 Widget 外部嵌入功能进行完整删除。

Widget 外部嵌入功能当前允许用户在 Agent 工作台中创建网站小组件配置，并通过公开页面 `/widget/[publicId]` 或 iframe 代码把 Agent 嵌入第三方网站。外部访客无需登录即可通过公开 Widget 与 Agent 或 RAG 知识库对话。

本次删除目标不是简单隐藏入口，而是从前端用户视角、管理端视角、后端接口、会话处理链路、数据库表结构、配置项和文档说明中整体移除该功能，确保系统不再暴露匿名 Widget 访问能力。

## 2. 删除后的用户视角

### 2.1 普通用户端

删除前，用户可以在 Agent 列表或编辑页中进入“嵌入组件 / 网站小组件”功能，完成以下操作：

- 创建 Widget 配置。
- 配置名称、描述、模型、允许访问域名、每日调用限制。
- 选择 Agent 类型 Widget 或 RAG 类型 Widget。
- 复制 iframe 或悬浮窗嵌入代码。
- 打开 `/widget/{publicId}` 预览公开聊天页面。
- 启用、停用、编辑、删除 Widget。

删除后，普通用户端应表现为：

- Agent 列表中不再出现“嵌入组件”操作入口。
- Agent 编辑页中不再出现“网站小组件 / Widget”标签页或配置面板。
- 不再提供创建、编辑、复制嵌入代码、预览 Widget 的任何按钮。
- 访问旧的前端地址 `/widget/{publicId}` 时应返回 404 页面，而不是公开聊天页。
- 已经复制到外部网站的旧 iframe 地址不再可用。

### 2.2 管理端

当前代码中没有发现独立的后台 Widget 管理页面，但管理端和文档中仍可能把 Widget 作为平台开放能力的一部分描述。

删除后，管理端应表现为：

- 管理后台导航、统计、功能说明中不再出现 Widget、嵌入式组件、网站小组件等表述。
- 如果未来管理端存在 Agent 能力审核或功能列表，不应再把 Widget 作为可管理能力展示。
- 管理端不需要新增替代页面，也不需要保留只读历史记录页面。

## 3. 当前实现边界

### 3.1 前端用户端入口

当前 Widget 前端主要分布在：

| 文件 | 当前职责 | 删除策略 |
| --- | --- | --- |
| `agentx-frontend-plus/app/(main)/studio/page.tsx` | Agent 卡片菜单中提供“嵌入组件”入口，跳转 `/studio/{agentId}/widgets` | 删除菜单项和相关图标引用 |
| `agentx-frontend-plus/app/(main)/studio/[id]/widgets/page.tsx` | 独立 Widget 管理页，包含创建、列表、复制嵌入代码、预览等逻辑 | 删除整个页面目录 |
| `agentx-frontend-plus/app/(main)/studio/edit/[id]/components/AgentWidgetTab.tsx` | Agent 编辑页中的 Widget 配置 Tab 内容 | 删除组件，并移除父组件引用 |
| `agentx-frontend-plus/app/(main)/studio/edit/[id]/components/CreateWidgetDialog.tsx` | 创建 Widget 弹窗 | 删除 |
| `agentx-frontend-plus/app/(main)/studio/edit/[id]/components/UpdateWidgetDialog.tsx` | 编辑 Widget 弹窗 | 删除 |
| `agentx-frontend-plus/app/(main)/studio/edit/[id]/components/WidgetCodeDialog.tsx` | 展示和复制嵌入代码 | 删除 |
| `agentx-frontend-plus/app/widget/layout.tsx` | 公开 Widget 页面布局 | 删除 |
| `agentx-frontend-plus/app/widget/[publicId]/page.tsx` | 公开 Widget 页面入口 | 删除 |
| `agentx-frontend-plus/app/widget/[publicId]/components/WidgetChatInterface.tsx` | 公开 Widget 聊天 UI | 删除 |

### 3.2 前端接口和类型

| 文件 | 当前职责 | 删除策略 |
| --- | --- | --- |
| `agentx-frontend-plus/types/widget.ts` | Widget 类型定义，包括 `AgentWidget`、`CreateWidgetRequest`、`WidgetChatRequest` | 删除 |
| `agentx-frontend-plus/lib/agent-widget-service.ts` | 登录用户侧 Widget CRUD API 封装 | 删除 |
| `agentx-frontend-plus/lib/widget-service.ts` | 另一套 Widget API 封装，包含公开信息获取 | 删除 |
| `agentx-frontend-plus/lib/widget-chat-service.ts` | 公开 Widget 聊天流式请求处理 | 删除 |
| `agentx-frontend-plus/lib/api-config.ts` | `AGENT_WIDGETS`、`CREATE_WIDGET`、`WIDGET_INFO`、`WIDGET_CHAT` 等端点常量 | 删除 Widget 相关端点常量 |
| `agentx-frontend-plus/app/providers.tsx` | 对 `/widget` 路由做特殊处理 | 删除 `isWidgetRoute` 分支 |

### 3.3 后端接口层

| 文件 | 当前职责 | 删除策略 |
| --- | --- | --- |
| `AgentX/src/main/java/org/xhy/interfaces/api/portal/agent/AgentWidgetController.java` | 登录用户侧 Widget CRUD 接口：`/agents/{agentId}/widgets` 和 `/user/widgets` | 删除整个文件 |
| `AgentX/src/main/java/org/xhy/interfaces/api/public_api/WidgetChatController.java` | 公开匿名 Widget 接口：`/widget/{publicId}/info`、`/widget/{publicId}/chat`、`/widget/{publicId}/chat/sync` | 删除整个文件 |
| `AgentX/src/main/java/org/xhy/interfaces/dto/agent/request/CreateWidgetRequest.java` | 创建 Widget 请求 DTO | 删除 |
| `AgentX/src/main/java/org/xhy/interfaces/dto/agent/request/UpdateWidgetRequest.java` | 更新 Widget 请求 DTO | 删除 |
| `AgentX/src/main/java/org/xhy/interfaces/dto/agent/request/WidgetChatRequest.java` | Widget 聊天请求 DTO | 删除 |
| `AgentX/src/main/java/org/xhy/interfaces/dto/agent/request/EmbedChatRequest.java` | 嵌入聊天请求 DTO，若仅 Widget 使用则删除 | 删除前用 `rg "EmbedChatRequest"` 确认无其他用途 |

删除后，以下后端接口不应再存在：

- `POST /agents/{agentId}/widgets`
- `GET /agents/{agentId}/widgets`
- `GET /agents/{agentId}/widgets/{widgetId}`
- `PUT /agents/{agentId}/widgets/{widgetId}`
- `POST /agents/{agentId}/widgets/{widgetId}/status`
- `DELETE /agents/{agentId}/widgets/{widgetId}`
- `GET /user/widgets`
- `GET /widget/{publicId}/info`
- `POST /widget/{publicId}/chat`
- `POST /widget/{publicId}/chat/sync`

### 3.4 后端应用层、领域层和基础设施

| 文件 | 当前职责 | 删除策略 |
| --- | --- | --- |
| `AgentX/src/main/java/org/xhy/application/agent/service/AgentWidgetAppService.java` | Widget 应用服务，负责权限校验、DTO 转换、公开访问信息组装 | 删除 |
| `AgentX/src/main/java/org/xhy/application/agent/assembler/AgentWidgetAssembler.java` | Widget Entity/DTO 转换，并生成 iframe / script 嵌入代码 | 删除 |
| `AgentX/src/main/java/org/xhy/application/agent/dto/AgentWidgetDTO.java` | Widget 返回 DTO | 删除 |
| `AgentX/src/main/java/org/xhy/domain/agent/service/AgentWidgetDomainService.java` | Widget 领域服务，负责 publicId 查询、域名白名单、启停、删除 | 删除 |
| `AgentX/src/main/java/org/xhy/domain/agent/model/AgentWidgetEntity.java` | `agent_widgets` 表实体 | 删除 |
| `AgentX/src/main/java/org/xhy/domain/agent/repository/AgentWidgetRepository.java` | Widget Repository | 删除 |
| `AgentX/src/main/java/org/xhy/domain/agent/constant/WidgetType.java` | `AGENT` / `RAG` Widget 类型枚举 | 删除 |
| `AgentX/src/main/java/org/xhy/infrastructure/converter/WidgetTypeConverter.java` | Widget 类型 MyBatis 转换器 | 删除 |

### 3.5 会话链路中的 Widget 特殊分支

Widget 不只在 `AgentWidget*` 类中存在，还改造了会话处理链路。后续 coding 必须清理这些残留，否则会出现编译失败或概念残留。

| 文件 | 当前 Widget 逻辑 | 删除策略 |
| --- | --- | --- |
| `AgentX/src/main/java/org/xhy/application/conversation/service/ConversationAppService.java` | `widgetChat`、`widgetChatSync`、`prepareWidgetEnvironment`、`createWidgetChatContext`、`setupWidgetContextAndHistory`、`createRagWidgetContext` | 删除这些方法和 `AgentWidgetEntity`、`WidgetChatRequest` 相关 import |
| `AgentX/src/main/java/org/xhy/application/conversation/service/handler/MessageHandlerFactory.java` | `getHandler(AgentEntity agent, AgentWidgetEntity widget)` 根据 Widget 类型选择 `RagMessageHandler` | 删除该重载方法和 `AgentWidgetEntity` import |
| `AgentX/src/main/java/org/xhy/application/conversation/service/handler/context/ChatContext.java` | `publicAccess`、`publicId` 字段及 getter/setter | 如果仅 Widget 使用，删除字段和方法 |

删除后，普通会话、Agent 预览会话、RAG 会话仍保留；只是移除“匿名公开访问上下文”和“基于 Widget 配置的 RAG 分支”。

## 4. 数据库删除方案

### 4.1 当前 Widget 表

当前初始化 SQL 和迁移中存在 `agent_widgets` 表：

- `docs/sql/01_init.sql`
- `AgentX/src/main/resources/db/migration/V20250810001__create_agent_widget_table.sql`

表字段包括：

- `id`
- `agent_id`
- `user_id`
- `public_id`
- `name`
- `description`
- `model_id`
- `provider_id`
- `allowed_domains`
- `daily_limit`
- `enabled`
- `created_at`
- `updated_at`
- `deleted_at`
- `widget_type`
- `knowledge_base_ids`

索引包括：

- `agent_embeds_public_id_key`
- `idx_agent_embeds_agent_id`
- `idx_agent_embeds_user_id`
- `idx_agent_embeds_public_id`
- `idx_agent_embeds_enabled`

### 4.2 新库初始化 SQL

对新初始化数据库，应从 `docs/sql/01_init.sql` 中删除整段 `create table public.agent_widgets`、相关 index 和 comment。

删除后，全量初始化库不再创建 Widget 表。

### 4.3 已有库迁移 SQL

对已经执行过历史迁移的数据库，不建议直接修改已执行过的 Flyway 历史迁移文件，否则可能出现 checksum 不一致。

建议新增迁移文件：

`AgentX/src/main/resources/db/migration/V20260512001__drop_agent_widget_table.sql`

内容建议：

```sql
drop table if exists public.agent_widgets;
```

如果当前项目确认没有任何环境执行过 `V20250810001__create_agent_widget_table.sql`，也可以在删除代码时同时删除该历史迁移文件。但更稳妥的实施方式是：

- 保留历史迁移文件，避免已有库 Flyway 校验失败。
- 新增 drop 迁移，删除已有库中的表。
- 修改 `docs/sql/01_init.sql`，确保新库不再创建该表。

### 4.4 数据处理策略

本次删除不做历史数据迁移。`agent_widgets` 中的历史配置属于被删除功能的数据，随表删除一起清理。

实施前如需备份，可执行：

```sql
create table public.agent_widgets_backup_20260512 as
select * from public.agent_widgets;
```

但正式代码方案中不建议默认保留备份表，避免毕业设计演示库和初始化库残留已删除功能。

## 5. 配置文件删除方案

### 5.1 后端配置

`AgentWidgetAssembler` 当前使用：

```java
@Value("${agentx.frontend.base-url:http://localhost:3000}")
public String frontendBaseUrl;
```

该配置仅用于生成 iframe / script 嵌入代码。删除 `AgentWidgetAssembler` 后，应同步检查并删除以下位置可能存在的配置项：

- `AgentX/src/main/resources/application.yml`
- `.env.example`
- `deploy/.env.local.example`
- `deploy/.env.external.example`
- `deploy/.env.production.example`
- `production/docker-compose.yml`
- `deploy/docker-compose.yml`

当前检索结果中没有发现显式 `agentx.frontend.base-url` 环境变量配置。如果后续 coding 时仍能检索到该 key，应删除。

### 5.2 前端配置

`NEXT_PUBLIC_API_BASE_URL` 是通用 API 地址，不是 Widget 专用配置，不能因为删除 Widget 而删除。

需要删除的是 `agentx-frontend-plus/lib/api-config.ts` 中 Widget 相关端点常量：

- `AGENT_WIDGETS`
- `CREATE_WIDGET`
- `WIDGET_DETAIL`
- `UPDATE_WIDGET`
- `DELETE_WIDGET`
- `TOGGLE_WIDGET_STATUS`
- `USER_WIDGETS`
- `WIDGET_INFO`
- `WIDGET_CHAT`

### 5.3 认证白名单配置

当前 `AgentX/src/main/java/org/xhy/infrastructure/config/WebMvcConfig.java` 中将 `/widget/**` 排除在登录拦截器之外：

```java
"/widget/**", // Widget公开API接口，无需认证
```

删除公开 Widget 接口后，必须移除该白名单。否则即使 Controller 被删，后续新增同路径接口也会默认绕过登录认证，形成安全隐患。

保留 `/v1/**`，因为它属于 Open API，不属于 Widget。

## 6. 文档和产品说明删除方案

需要同步删除或改写以下文档中的 Widget 描述：

| 文件 | 删除内容 |
| --- | --- |
| `README.md` | “嵌入式组件”“Widget 嵌入式聊天组件”“/widget/[publicId]” 等说明 |
| `docs/项目综述笔记.md` | `app/widget`、`Widget 外部嵌入`、`AgentWidgetController`、`WidgetChatController`、`agent_widgets` 等章节和清单 |
| 其他技术方案或论文材料 | 如果仍把 Widget 作为系统能力，需要改为已删除或不再纳入范围 |

注意：如果某些 thesis/论文材料是历史说明，不一定要在本次 coding 中修改；但 README 和项目综述属于当前项目说明，建议同步清理。

## 7. 推荐实施顺序

### 阶段一：前端入口删除

1. 删除 Agent 列表中的“嵌入组件”菜单项。
2. 删除 Agent 编辑页中的 Widget Tab 引用。
3. 删除 Widget 管理页目录：`agentx-frontend-plus/app/(main)/studio/[id]/widgets/`。
4. 删除公开 Widget 页面目录：`agentx-frontend-plus/app/widget/`。
5. 删除 Widget 弹窗组件：`CreateWidgetDialog.tsx`、`UpdateWidgetDialog.tsx`、`WidgetCodeDialog.tsx`、`AgentWidgetTab.tsx`。
6. 删除 Widget API service 和类型文件。
7. 清理 `api-config.ts` 端点常量。
8. 清理 `providers.tsx` 中 `/widget` 特判。

阶段一完成后，前端不应再能跳转到任何 Widget 页面。

### 阶段二：后端接口和领域模型删除

1. 删除 `AgentWidgetController.java`。
2. 删除 `WidgetChatController.java`。
3. 删除 Widget 请求 DTO。
4. 删除 `AgentWidgetAppService.java`。
5. 删除 `AgentWidgetAssembler.java`。
6. 删除 `AgentWidgetDTO.java`。
7. 删除 `AgentWidgetDomainService.java`。
8. 删除 `AgentWidgetEntity.java`。
9. 删除 `AgentWidgetRepository.java`。
10. 删除 `WidgetType.java`。
11. 删除 `WidgetTypeConverter.java`。

阶段二完成后，后端不应再存在 Widget 的 CRUD 或公开匿名访问接口。

### 阶段三：会话链路残留清理

1. 在 `ConversationAppService.java` 中删除 Widget 专用聊天方法和上下文准备方法。
2. 删除 `ConversationAppService.java` 中 `AgentWidgetEntity`、`WidgetChatRequest`、Widget/RAG Widget 相关 import。
3. 在 `MessageHandlerFactory.java` 中删除 `getHandler(AgentEntity, AgentWidgetEntity)` 重载。
4. 在 `ChatContext.java` 中删除 `publicAccess`、`publicId` 字段和 getter/setter。
5. 全局执行 `rg -n "Widget|widget|publicId|public_id|publicAccess|agent_widgets|WidgetType"`，逐项判断是否还有业务残留。

阶段三完成后，普通聊天和 RAG 聊天仍能运行，但不再存在匿名 Widget 模式。

### 阶段四：数据库和配置清理

1. 从 `docs/sql/01_init.sql` 删除 `agent_widgets` 建表、索引和 comment。
2. 新增 drop 迁移：`V20260512001__drop_agent_widget_table.sql`。
3. 根据实际 Flyway 使用情况决定是否保留 `V20250810001__create_agent_widget_table.sql`。
4. 删除 `WebMvcConfig.java` 中 `/widget/**` 认证白名单。
5. 检查 env、compose、application.yml 中是否还有 Widget 或 `agentx.frontend.base-url` 配置残留。

### 阶段五：文档清理和验证

1. 清理 README 中 Widget 能力描述。
2. 清理项目综述笔记中 Widget 章节。
3. 执行后端编译。
4. 执行前端 lint/build 或至少 TypeScript 检查。
5. 通过接口和页面访问验证删除效果。

## 8. 验收标准

### 8.1 代码检索验收

以下命令不应再返回业务代码结果：

```bash
rg -n "AgentWidget|WidgetChat|WidgetType|agent_widgets|publicAccess|isWidgetRoute" AgentX agentx-frontend-plus
```

允许保留的结果只应是本技术方案文档、历史说明文档或明确不参与运行的备份说明。

### 8.2 前端验收

- `/studio` Agent 卡片菜单不再出现“嵌入组件”。
- Agent 编辑页不再出现“网站小组件 / Widget”相关 Tab。
- 访问 `/studio/{agentId}/widgets` 返回 404。
- 访问 `/widget/{publicId}` 返回 404。
- 前端编译不再引用 `types/widget.ts`、`widget-service.ts`、`agent-widget-service.ts`、`widget-chat-service.ts`。

### 8.3 后端验收

以下接口不应再存在：

```text
/agents/{agentId}/widgets
/user/widgets
/widget/{publicId}/info
/widget/{publicId}/chat
/widget/{publicId}/chat/sync
```

直接请求 `/api/widget/test/info` 应返回 404 或统一未找到，而不是进入业务逻辑。

### 8.4 数据库验收

新库初始化后：

```sql
select to_regclass('public.agent_widgets');
```

结果应为 `null`。

已有库执行迁移后：

```sql
select to_regclass('public.agent_widgets');
```

结果也应为 `null`。

### 8.5 安全验收

- `WebMvcConfig.java` 中不再存在 `/widget/**` 免登录配置。
- 系统不再暴露无需登录即可调用模型、工具或 RAG 的 Widget 入口。
- Open API `/v1/**` 不受影响。

## 9. 风险点和处理建议

### 9.1 Flyway 历史迁移风险

如果删除或修改已执行的历史迁移文件，已有数据库可能启动失败。推荐使用新增 drop 迁移删除表，不直接改动已执行迁移。

### 9.2 RAG Widget 与普通 RAG 的边界

`createRagWidgetContext` 是 Widget 专用逻辑，应删除。普通用户侧 RAG 功能仍依赖 `RagChatRequest`、`RagMessageHandler`、`RAGSearchAppService` 等，不应误删。

判断原则：

- 名称包含 Widget、`publicId`、`agent_widgets` 的删除。
- 普通 `/rag/**`、`RagChatRequest`、知识库上传、向量检索保留。

### 9.3 Open API 与 Widget 的边界

Widget 是匿名页面和 iframe 嵌入能力；Open API 是通过 API Key 访问的开放接口。删除 Widget 时不要删除：

- `API Key` 管理。
- `/v1/**` 公开 API。
- API Key 拦截器或认证逻辑。

### 9.4 前端重复 service 风险

当前前端同时存在 `agent-widget-service.ts` 和 `widget-service.ts` 两套 Widget 封装。删除时不能只删其中一个，否则 TypeScript 仍可能通过另一条引用保留残留入口。

### 9.5 旧外部网站嵌入失效

删除后，外部网站中已复制的 iframe/script 会失效。这是本次删除的预期结果，不需要兼容重定向。

如果希望对旧链接返回明确错误，可以保留一个非业务性的 404 页面；不建议保留后端 `/widget/**` Controller 返回“功能已下线”，因为这会继续占用匿名公开路径。

## 10. 后续 Coding 提示词建议

后续让 Codex 实施时，可以直接使用以下约束：

```text
根据《Widget外部嵌入功能删除技术方案.md》实施删除。
只删除 Widget 外部嵌入功能，不删除 Open API、API Key、普通 Agent 聊天、普通 RAG 功能。
优先保证项目可编译，删除后执行 rg 验收命令，并说明仍保留的 Widget 文档残留是否属于非运行文档。
```
