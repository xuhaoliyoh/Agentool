# AgentX 远程 MCP / HTTP 工具调用重构技术方案

> 目标：删除原 MCP Gateway / GitHub 发布 / Docker 容器部署链路，把工具运行模型改成“用户提交远程 MCP 或 HTTP API 配置，AgentX 负责审核、登记、安装和直接调用”。
>
> 本文档是下一轮 coding 的执行方案。实现时应以本文档为准，按阶段改造，不再继续维护“平台部署用户工具代码”的旧方案。

## 1. 业务目标

### 1.1 当前问题

当前工具链路是：

```text
用户上传工具
-> GitHub URL 校验
-> REVIEW 审核容器部署
-> 从审核容器获取工具列表
-> 管理员人工审核
-> 审核通过后发布到目标 GitHub 仓库
-> 用户安装工具
-> 对话时启动/复用 USER 用户容器
-> 将工具部署到用户容器的 mcp-gateway
-> LangChain4j 通过 mcp-gateway SSE 调用工具
```

这条链路的问题：

- 依赖 GitHub Token、目标仓库、GitHub push 权限。
- 依赖 Docker socket、动态容器、端口映射、容器网络。
- 依赖 `ghcr.io/lucky-aeon/mcp-gateway` 的部署和转发行为。
- 对远程 SSE MCP 工具存在额外转发复杂度。
- 对本科毕设演示来说，部署链路过重，故障点过多。

### 1.2 新目标

新链路改为：

```text
用户提交工具基础信息
-> 用户填写远程 MCP endpoint 或 HTTP API 配置
-> 后端直接探测 endpoint，获取/生成工具定义
-> 进入人工审核
-> 管理员审核通过
-> 创建者自动安装
-> 用户可上架市场
-> 其他用户安装
-> 对话时后端直接调用远程 MCP 或 HTTP API
```

### 1.3 AgentX 的新定位

AgentX 不负责托管和部署用户工具代码，只负责：

- 工具登记
- 工具连通性检测
- 工具定义解析
- 管理员审核
- 工具市场分发
- 用户安装
- 对话时调用远程工具

一句话定位：

```text
AgentX 是工具调用平台，不是工具托管平台。
```

## 2. 设计边界

### 2.1 保留

- 工具市场页面和 API。
- 用户创建工具。
- 管理员审核工具。
- 工具上架市场。
- 用户安装/卸载工具。
- Agent 绑定工具并在对话中调用。
- trace / tool call 记录链路。
- `tools`、`tool_versions`、`user_tools` 三张主表，第一阶段不整体推倒。

### 2.2 删除

- GitHub URL 校验流程。
- GitHub 下载/发布到目标仓库流程。
- REVIEW 审核容器。
- USER 用户容器。
- MCP Gateway 服务。
- 容器模板管理。
- 后端动态 Docker 管理。
- 前端容器管理页面。
- 前端上传工具里的“GitHub 仓库 URL”和“安装命令”概念。

### 2.3 不做

- 不执行用户上传的任意命令。
- 不运行 `npx`、`uvx`、`python` 等用户提供命令。
- 不为每个用户创建工具容器。
- 不做 MCP Server 托管。
- 不把用户密钥公开到市场配置。

## 3. 新工具类型

新增工具运行类型：

```java
public enum ToolRuntimeType {
    REMOTE_MCP_SSE,
    REMOTE_MCP_STREAMABLE_HTTP,
    HTTP_API
}
```

说明：

- `REMOTE_MCP_SSE`：远程 MCP SSE endpoint。
- `REMOTE_MCP_STREAMABLE_HTTP`：远程 MCP Streamable HTTP endpoint，第一阶段可以先定义枚举但不实现完整调用。
- `HTTP_API`：普通 HTTP API 工具，由 AgentX 按 JSON Schema 包装成 ToolProvider。

新增鉴权类型：

```java
public enum ToolAuthType {
    NONE,
    BEARER_TOKEN,
    API_KEY_HEADER,
    API_KEY_QUERY,
    BASIC
}
```

说明：

- 工具公开配置只保存鉴权方式和参数名。
- 用户自己的密钥保存到 `user_tool_credentials`，不随工具市场公开。

## 4. 后端改造方案

### 4.1 后端删除文件

以下代码和 Gateway / 容器部署强绑定，重构完成后删除：

```text
AgentX/src/main/java/org/xhy/infrastructure/mcp_gateway/MCPGatewayService.java
AgentX/src/main/java/org/xhy/infrastructure/mcp_gateway/MCPGatewayProperties.java

AgentX/src/main/java/org/xhy/application/container/**
AgentX/src/main/java/org/xhy/domain/container/**
AgentX/src/main/java/org/xhy/infrastructure/docker/DockerService.java
AgentX/src/main/java/org/xhy/interfaces/api/admin/AdminContainerController.java
AgentX/src/main/java/org/xhy/interfaces/api/container/ContainerTemplateController.java
AgentX/src/main/java/org/xhy/interfaces/dto/container/**

AgentX/src/main/java/org/xhy/application/tool/service/state/impl/AppGithubUrlValidateProcessor.java
AgentX/src/main/java/org/xhy/application/tool/service/state/impl/AppDeployingProcessor.java
AgentX/src/main/java/org/xhy/application/tool/service/state/impl/AppFetchingToolsProcessor.java
AgentX/src/main/java/org/xhy/application/tool/service/state/impl/AppPublishingProcessor.java
```

以下 GitHub 相关代码，如果确认只用于旧工具发布链路，也删除：

```text
AgentX/src/main/java/org/xhy/infrastructure/github/GitHubService.java
AgentX/src/main/java/org/xhy/infrastructure/github/GitHubUrlParser.java
AgentX/src/main/java/org/xhy/infrastructure/github/GitHubProperties.java
AgentX/src/main/java/org/xhy/domain/tool/model/dto/GitHubRepoInfo.java
```

如果这些 GitHub 类被其他业务复用，先只删除工具状态机中的引用，最后阶段再清理。

### 4.2 后端保留并改造文件

#### 4.2.1 `ToolAppService`

文件：

```text
AgentX/src/main/java/org/xhy/application/tool/service/ToolAppService.java
```

当前问题：

- `uploadTool` 创建工具后提交旧状态机。
- `updateTool` 更新工具后提交旧状态机。
- `marketTool`、`installTool` 可以复用，但需要同步 runtime 配置。

改造要求：

- 注入新的 `ToolReviewAppService` 或 `ToolEndpointProbeService`。
- 创建工具时不再提交旧 `ToolStateStateMachineAppService`。
- 创建工具时执行端点探测：

```text
uploadTool()
-> ToolAssembler.toEntity()
-> toolEndpointProbeService.probe(request.runtimeConfig)
-> toolEntity.toolList = probeResult.toolDefinitions
-> toolEntity.mcpServerName = probeResult.serverName
-> toolEntity.status = MANUAL_REVIEW
-> toolDomainService.createTool()
-> toolRuntimeConfigDomainService.save(toolId, runtimeConfig)
```

- 如果探测失败：
  - 可以直接抛业务异常，不创建工具。
  - 或创建 `FAILED` 工具记录，记录 `rejectReason`。
  - 推荐第一阶段直接抛异常，避免脏数据。

`updateTool()`：

- 如果 runtime 配置变化，需要重新探测。
- 更新后状态回到 `MANUAL_REVIEW`，需要重新审核。
- 已经 `APPROVED` 的工具如果修改 runtime 配置，不能继续保持已审核状态。

`marketTool()`：

- 创建 `ToolVersionEntity` 后，需要同步创建版本 runtime 快照。
- 推荐新增 `tool_runtime_configs.version` 字段，版本上架时复制一份 runtime config，避免后续编辑影响历史版本。

`installTool()`：

- 安装工具时仍然复用 `user_tools`。
- 安装后如果工具需要用户密钥，不在安装接口强制填写，允许后续在工具配置页填写。

#### 4.2.2 `AdminToolAppService`

文件：

```text
AgentX/src/main/java/org/xhy/application/admin/tool/service/AdminToolAppService.java
```

当前问题：

- 人工审核通过时调用 `toolStateStateMachine.manualReviewComplete()`。
- 旧状态机在 `APPROVED` 后会继续触发 GitHub 发布。

改造要求：

- 删除 `ToolStateStateMachineAppService` 注入。
- 审核通过只做：

```text
toolDomainService.manualReviewComplete(tool, true)
toolAppService.autoInstallApprovedTool(toolId)
```

- 审核失败继续复用：

```text
toolDomainService.updateFailedToolStatus(toolId, currentStatus, rejectReason)
```

- 删除或调整全局状态逻辑：
  - `updateToolGlobalStatus` 可以删除。
  - `isGlobal` 在新模型下没有运行意义。
  - 如果前端仍展示“官方工具”，用 `isOffice` 替代，不要再用 `isGlobal`。

#### 4.2.3 `ToolDomainService`

文件：

```text
AgentX/src/main/java/org/xhy/domain/tool/service/ToolDomainService.java
```

改造要求：

- 保留工具 CRUD、状态更新、市场查询能力。
- 删除或废弃 `updateToolGlobalStatus`。
- `createTool` 不再以旧状态机为前提。
- 状态合法流转改为：

```text
WAITING_REVIEW -> ENDPOINT_VALIDATE -> MANUAL_REVIEW -> APPROVED
WAITING_REVIEW -> FAILED
ENDPOINT_VALIDATE -> FAILED
MANUAL_REVIEW -> FAILED
```

实际第一阶段可以简化为：

```text
创建工具时同步探测成功 -> MANUAL_REVIEW
管理员审核通过 -> APPROVED
管理员审核失败 -> FAILED
```

这样可以不引入异步状态机，代码更简单。

#### 4.2.4 `ToolStateStateMachineAppService`

文件：

```text
AgentX/src/main/java/org/xhy/application/tool/service/ToolStateStateMachineAppService.java
```

处理方案：

- 推荐删除整个类。
- 如果为了减少改动暂时保留，则改名或重写为 `ToolReviewAppService`。
- 不再注册任何 Gateway/GitHub/Deploying processor。

推荐新增：

```text
AgentX/src/main/java/org/xhy/application/tool/service/ToolReviewAppService.java
```

职责：

- `submitForReview(ToolEntity tool)`：设置 `MANUAL_REVIEW`。
- `approve(String toolId)`：设置 `APPROVED`。
- `reject(String toolId, String reason)`：设置 `FAILED`。

不再异步执行外部部署任务。

### 4.3 后端新增模块：运行配置

新增包：

```text
AgentX/src/main/java/org/xhy/domain/tool/constant/ToolRuntimeType.java
AgentX/src/main/java/org/xhy/domain/tool/constant/ToolAuthType.java

AgentX/src/main/java/org/xhy/domain/tool/model/ToolRuntimeConfigEntity.java
AgentX/src/main/java/org/xhy/domain/tool/model/UserToolCredentialEntity.java

AgentX/src/main/java/org/xhy/domain/tool/repository/ToolRuntimeConfigRepository.java
AgentX/src/main/java/org/xhy/domain/tool/repository/UserToolCredentialRepository.java

AgentX/src/main/java/org/xhy/infrastructure/repository/ToolRuntimeConfigRepositoryImpl.java
AgentX/src/main/java/org/xhy/infrastructure/repository/UserToolCredentialRepositoryImpl.java

AgentX/src/main/java/org/xhy/domain/tool/service/ToolRuntimeConfigDomainService.java
AgentX/src/main/java/org/xhy/domain/tool/service/UserToolCredentialDomainService.java
```

#### 4.3.1 `ToolRuntimeConfigEntity`

字段：

```java
@TableName(value = "tool_runtime_configs", autoResultMap = true)
public class ToolRuntimeConfigEntity extends BaseEntity {
    private String id;
    private String toolId;
    private String toolVersion;
    private ToolRuntimeType runtimeType;
    private String transportType;
    private String endpointUrl;
    private String httpMethod;
    private ToolAuthType authType;
    private Map<String, Object> authConfigSchema;
    private Map<String, Object> defaultHeaders;
    private Map<String, Object> requestSchema;
    private Map<String, Object> responseSchema;
    private List<ToolDefinition> toolDefinitions;
    private String mcpServerName;
    private Integer timeoutMs;
    private Boolean enabled;
}
```

说明：

- `toolId` 关联 `tools.id`。
- `toolVersion` 为空表示当前草稿/当前工具配置。
- 上架时复制一份带 `toolVersion` 的快照。
- `toolDefinitions` 保存探测到的 MCP tools 或 HTTP API 包装出来的工具定义。
- `authConfigSchema` 只描述需要用户填什么，不保存真实密钥。

#### 4.3.2 `UserToolCredentialEntity`

字段：

```java
@TableName(value = "user_tool_credentials", autoResultMap = true)
public class UserToolCredentialEntity extends BaseEntity {
    private String id;
    private String userToolId;
    private String userId;
    private String toolId;
    private Map<String, Object> authValues;
}
```

说明：

- `authValues` 存用户自己的 token/api key。
- 第一阶段可以明文存在本地数据库，后续如需正式化再接入加密。
- 市场工具版本和公开接口不能返回 `authValues`。

### 4.4 后端新增模块：端点探测

新增包：

```text
AgentX/src/main/java/org/xhy/application/tool/service/runtime/ToolEndpointProbeService.java
AgentX/src/main/java/org/xhy/application/tool/service/runtime/ToolEndpointProbeResult.java
AgentX/src/main/java/org/xhy/application/tool/service/runtime/RemoteMcpProbeClient.java
AgentX/src/main/java/org/xhy/application/tool/service/runtime/HttpApiProbeClient.java
```

#### 4.4.1 `ToolEndpointProbeService`

职责：

- 根据 `runtimeType` 路由到不同探测器。
- 探测成功后返回 `ToolEndpointProbeResult`。
- 探测失败后抛 `BusinessException`。

伪代码：

```java
public ToolEndpointProbeResult probe(ToolRuntimeConfigRequest request) {
    if (request.getRuntimeType() == REMOTE_MCP_SSE) {
        return remoteMcpProbeClient.probeSse(request);
    }
    if (request.getRuntimeType() == HTTP_API) {
        return httpApiProbeClient.probe(request);
    }
    throw new BusinessException("暂不支持的工具运行类型");
}
```

#### 4.4.2 `RemoteMcpProbeClient`

职责：

- 使用 LangChain4j `HttpMcpTransport` 直接连接用户填写的 SSE URL。
- 调用 `client.listTools()`。
- 用现有 `ToolSpecificationConverter.convert()` 转换成 `ToolDefinition`。

复用：

```text
AgentX/src/main/java/org/xhy/domain/tool/model/config/ToolSpecificationConverter.java
AgentX/src/main/java/org/xhy/domain/tool/model/config/ToolDefinition.java
```

不要复用：

```text
MCPGatewayService.listTools()
MCPGatewayService.listToolsFromReviewContainer()
```

因为它们依赖 Gateway URL 拼接。

#### 4.4.3 `HttpApiProbeClient`

职责：

- 校验 URL 合法。
- 校验 method 合法。
- 校验 request schema 是合法 JSON object。
- 将 HTTP API 包装为一个 `ToolDefinition`。

HTTP API 工具定义规则：

```text
toolDefinition.name = request.toolName 或 mcpServerName
toolDefinition.description = 工具描述
toolDefinition.parameters = requestSchema
toolDefinition.enabled = true
```

第一阶段不强制真实请求 HTTP API，避免对有副作用接口产生误调用。

### 4.5 后端新增模块：对话时直接调用

当前旧文件：

```text
AgentX/src/main/java/org/xhy/application/conversation/service/McpUrlProviderService.java
AgentX/src/main/java/org/xhy/application/conversation/service/message/agent/AgentToolManager.java
```

#### 4.5.1 删除 `McpUrlProviderService`

删除原因：

- 它的职责是根据工具类型准备审核容器/用户容器，并构建 Gateway SSE URL。
- 新方案不再存在容器和 Gateway。

替代类：

```text
AgentX/src/main/java/org/xhy/application/conversation/service/tool/DirectToolRuntimeResolver.java
```

职责：

- 根据 `mcpServerName` 和 `userId` 查找已安装工具。
- 找到 `ToolRuntimeConfigEntity`。
- 找到用户凭据 `UserToolCredentialEntity`。
- 返回 `ResolvedToolRuntime`。

`ResolvedToolRuntime` 字段：

```java
public class ResolvedToolRuntime {
    private String toolId;
    private String userToolId;
    private String mcpServerName;
    private ToolRuntimeType runtimeType;
    private String endpointUrl;
    private String httpMethod;
    private ToolAuthType authType;
    private Map<String, Object> authConfigSchema;
    private Map<String, Object> authValues;
    private Map<String, Object> defaultHeaders;
    private Map<String, Object> requestSchema;
    private Integer timeoutMs;
}
```

#### 4.5.2 改造 `AgentToolManager`

文件：

```text
AgentX/src/main/java/org/xhy/application/conversation/service/message/agent/AgentToolManager.java
```

当前逻辑：

```java
String sseUrl = mcpUrlProviderService.getMcpToolUrl(mcpServerName, userId);
McpTransport transport = new HttpMcpTransport.Builder().sseUrl(sseUrl)...
McpClient mcpClient = new DefaultMcpClient.Builder().transport(transport).build();
```

新逻辑：

```text
for each mcpServerName:
  runtime = directToolRuntimeResolver.resolve(mcpServerName, userId)
  if runtime.type is REMOTE_MCP_SSE:
      create McpClient directly with endpointUrl and auth headers/query
  if runtime.type is HTTP_API:
      create HTTP API tool provider / executor
merge all providers
```

建议新增：

```text
AgentX/src/main/java/org/xhy/application/conversation/service/tool/DirectToolProviderFactory.java
AgentX/src/main/java/org/xhy/application/conversation/service/tool/RemoteMcpToolProviderFactory.java
AgentX/src/main/java/org/xhy/application/conversation/service/tool/HttpApiToolProviderFactory.java
AgentX/src/main/java/org/xhy/application/conversation/service/tool/HttpApiToolExecutor.java
```

#### 4.5.3 MCP 鉴权处理

远程 MCP SSE 支持：

- 无鉴权：原 URL 直接连接。
- Query API Key：把用户密钥追加到 URL query。
- Bearer Token：添加 Authorization header。
- Header API Key：添加自定义 header。

如果当前 LangChain4j `HttpMcpTransport` 构造器不支持自定义 header：

- 第一阶段只支持 `NONE` 和 `API_KEY_QUERY`。
- 文档和前端明确提示。
- `BEARER_TOKEN` / `API_KEY_HEADER` 作为第二阶段。

### 4.6 后端接口改造

#### 4.6.1 创建工具接口

当前：

```text
POST /tools
CreateToolRequest:
  name
  icon
  subtitle
  description
  labels
  uploadUrl
  installCommand
  isGlobal
```

改为：

```text
POST /tools
CreateToolRequest:
  name
  icon
  subtitle
  description
  labels
  runtimeConfig
```

新增 DTO：

```text
AgentX/src/main/java/org/xhy/interfaces/dto/tool/request/ToolRuntimeConfigRequest.java
AgentX/src/main/java/org/xhy/interfaces/dto/tool/request/ToolAuthConfigRequest.java
AgentX/src/main/java/org/xhy/interfaces/dto/tool/request/HttpApiToolConfigRequest.java
```

`ToolRuntimeConfigRequest` 字段：

```java
private ToolRuntimeType runtimeType;
private String mcpServerName;
private String endpointUrl;
private String transportType;
private String httpMethod;
private ToolAuthType authType;
private Map<String, Object> authConfigSchema;
private Map<String, Object> defaultHeaders;
private Map<String, Object> requestSchema;
private Map<String, Object> responseSchema;
private Integer timeoutMs;
```

兼容策略：

- 第一阶段可以保留旧字段 `uploadUrl`、`installCommand`，但不再使用。
- 如果前端已全部切新字段，后端 DTO 可直接删除旧字段。

#### 4.6.2 更新工具接口

当前：

```text
PUT /tools/{toolId}
UpdateToolRequest:
  uploadUrl
  installCommand
```

改为：

```text
PUT /tools/{toolId}
UpdateToolRequest:
  runtimeConfig
```

更新 runtime 配置后必须重新探测并进入 `MANUAL_REVIEW`。

#### 4.6.3 测试连接接口

新增：

```text
POST /tools/runtime/probe
```

用途：

- 前端上传工具页点击“测试连接”。
- 返回解析出的工具列表。

请求：

```json
{
  "runtimeType": "REMOTE_MCP_SSE",
  "endpointUrl": "https://example.com/sse",
  "authType": "API_KEY_QUERY",
  "authConfigSchema": {
    "queryName": "api_key"
  }
}
```

响应：

```json
{
  "mcpServerName": "geocode",
  "toolDefinitions": [
    {
      "name": "geo_code",
      "description": "查询地址坐标",
      "parameters": {}
    }
  ]
}
```

#### 4.6.4 用户工具凭据接口

新增：

```text
GET /tools/user/{userToolId}/credentials
PUT /tools/user/{userToolId}/credentials
```

用途：

- 用户安装需要 token 的工具后，填写自己的 token/api key。
- 对话调用时后端从 `user_tool_credentials` 读取密钥。

注意：

- GET 只能返回是否已配置、字段名、脱敏值。
- 不返回明文密钥。

### 4.7 后端删除接口

删除：

```text
GET /admin/containers/**
POST /admin/containers/**
DELETE /admin/containers/**

GET /container-templates/**
POST /container-templates/**
PUT /container-templates/**
DELETE /container-templates/**
```

如果担心前端还引用，可先后端保留空壳但前端隐藏入口。最终清理时删除。

## 5. 数据库改造方案

### 5.1 新增 migration

新增：

```text
AgentX/src/main/resources/db/migration/V2026xxxxxxx__replace_mcp_gateway_with_direct_tool_runtime.sql
```

内容：

```sql
create table if not exists tool_runtime_configs (
    id varchar(36) primary key,
    tool_id varchar(36) not null,
    tool_version varchar(50),
    runtime_type varchar(50) not null,
    transport_type varchar(50),
    endpoint_url text,
    http_method varchar(20),
    auth_type varchar(50) not null default 'NONE',
    auth_config_schema jsonb,
    default_headers jsonb,
    request_schema jsonb,
    response_schema jsonb,
    tool_definitions jsonb,
    mcp_server_name varchar(255),
    timeout_ms integer not null default 30000,
    enabled boolean not null default true,
    created_at timestamp without time zone not null default current_timestamp,
    updated_at timestamp without time zone not null default current_timestamp,
    deleted_at timestamp without time zone
);

create index if not exists idx_tool_runtime_configs_tool_id
    on tool_runtime_configs(tool_id);

create index if not exists idx_tool_runtime_configs_tool_version
    on tool_runtime_configs(tool_id, tool_version);

create table if not exists user_tool_credentials (
    id varchar(36) primary key,
    user_tool_id varchar(36) not null,
    user_id varchar(36) not null,
    tool_id varchar(36) not null,
    auth_values jsonb,
    created_at timestamp without time zone not null default current_timestamp,
    updated_at timestamp without time zone not null default current_timestamp,
    deleted_at timestamp without time zone
);

create unique index if not exists uk_user_tool_credentials_user_tool
    on user_tool_credentials(user_tool_id)
    where deleted_at is null;
```

### 5.2 `tools` 表调整

第一阶段保留旧字段，降低改动风险：

```text
upload_url
install_command
is_global
```

但业务不再写入和读取它们。

建议新增字段：

```sql
alter table tools add column if not exists runtime_type varchar(50);
```

说明：

- `runtime_type` 用于列表快速展示。
- 详细运行配置仍以 `tool_runtime_configs` 为准。

第二阶段再删除旧字段：

```sql
alter table tools drop column upload_url;
alter table tools drop column install_command;
alter table tools drop column is_global;
```

### 5.3 `tool_versions` 表调整

第一阶段建议新增：

```sql
alter table tool_versions add column if not exists runtime_type varchar(50);
```

版本的具体 endpoint/config 不直接放在 `tool_versions` 中，而是在 `tool_runtime_configs` 里通过：

```text
tool_id + tool_version
```

保存快照。

### 5.4 `user_tools` 表调整

第一阶段建议新增：

```sql
alter table user_tools add column if not exists runtime_type varchar(50);
alter table user_tools add column if not exists credential_required boolean not null default false;
alter table user_tools add column if not exists credential_configured boolean not null default false;
```

用途：

- 前端展示“该工具需要配置密钥”。
- 对话前快速判断是否缺少凭据。

### 5.5 删除旧表

最终删除：

```sql
drop table if exists user_containers;
drop table if exists container_templates;
```

执行策略：

- 如果本地数据不重要，可直接在 migration 中 drop。
- 如果需要平滑迁移，先代码不再引用，第二个 migration 再 drop。

## 6. 前端改造方案

### 6.1 用户端工具上传页

文件：

```text
agentx-frontend-plus/app/(main)/tools/upload/page.tsx
```

当前页面字段：

- 工具名称
- 副标题
- 描述
- 标签
- GitHub 仓库 URL
- 安装命令 JSON
- 图标

改为：

- 工具名称
- 副标题
- 描述
- 标签
- 工具调用方式
  - 远程 MCP
  - HTTP API
- 远程 MCP 配置
  - Endpoint URL
  - 协议：SSE / Streamable HTTP
  - MCP Server Name
  - 鉴权方式
  - Query 参数名 / Header 名
- HTTP API 配置
  - URL
  - Method
  - 工具函数名称
  - 请求 JSON Schema
  - 响应说明
  - 鉴权方式
- 测试连接按钮
- 图标

删除文案：

```text
GitHub仓库
GitHub仓库URL
您的GitHub仓库需要包含工具定义文件
安装命令：mcp server 的 stdio 或者 sse 格式
命令将在安装时执行，请确保命令格式正确
```

新增 payload：

```ts
const payload = {
  name,
  subtitle,
  description,
  labels,
  icon,
  runtimeConfig: {
    runtimeType,
    transportType,
    endpointUrl,
    mcpServerName,
    httpMethod,
    authType,
    authConfigSchema,
    defaultHeaders,
    requestSchema,
    responseSchema,
    timeoutMs
  }
}
```

### 6.2 用户端工具编辑页

文件：

```text
agentx-frontend-plus/app/(main)/tools/edit/[id]/page.tsx
```

改造与上传页一致。

注意：

- 如果工具已 `APPROVED`，修改 runtime 配置后需要提示“修改后需要重新审核”。
- 保存后前端应回到工具列表，状态显示 `MANUAL_REVIEW`。

### 6.3 用户端审核状态弹窗

文件：

```text
agentx-frontend-plus/app/(main)/tools/components/dialogs/ReviewStatusDialog.tsx
```

当前步骤：

```ts
WAITING_REVIEW
GITHUB_URL_VALIDATE
DEPLOYING
FETCHING_TOOLS
MANUAL_REVIEW
APPROVED
```

改为：

```ts
WAITING_REVIEW
ENDPOINT_VALIDATE
MANUAL_REVIEW
APPROVED
```

如果第一阶段后端创建时同步探测并直接进入 `MANUAL_REVIEW`，则前端也可以只显示：

```ts
MANUAL_REVIEW
APPROVED
FAILED
```

推荐展示为：

```text
连接检测 -> 人工审核 -> 审核通过
```

### 6.4 用户端工具详情 / 安装逻辑

相关文件：

```text
agentx-frontend-plus/app/(main)/tools/[id]/[version]/page.tsx
agentx-frontend-plus/app/(main)/tools/components/dialogs/InstallToolDialog.tsx
agentx-frontend-plus/app/(main)/tools/components/dialogs/UserToolDetailDialog.tsx
agentx-frontend-plus/app/(main)/tools/components/cards/UserToolCard.tsx
```

改造点：

- 展示工具类型：远程 MCP / HTTP API。
- 如果 `credentialRequired=true` 且 `credentialConfigured=false`，显示“需要配置密钥”。
- 安装后如果需要密钥，弹出配置入口。
- 不展示 GitHub 仓库信息。
- 不展示安装命令。

### 6.5 管理端工具审核页

文件：

```text
agentx-frontend-plus/app/(main)/admin/tools/page.tsx
agentx-frontend-plus/lib/admin-tool-service.ts
```

改造点：

- 工具列表增加：
  - runtimeType
  - endpointUrl 脱敏显示
  - toolList
  - credentialRequired
- 审核通过按钮仍调用：

```text
POST /admin/tools/{toolId}/status?status=APPROVED
```

- 审核失败仍传 reason。
- 删除“全局工具”开关或隐藏。

### 6.6 管理端容器页面删除

删除页面：

```text
agentx-frontend-plus/app/(main)/admin/containers/page.tsx
agentx-frontend-plus/app/(main)/admin/container-templates/page.tsx
agentx-frontend-plus/app/(main)/templates/page.tsx
```

删除服务：

```text
agentx-frontend-plus/lib/admin-container-service.ts
agentx-frontend-plus/lib/container-template-service.ts
```

修改导航：

```text
agentx-frontend-plus/app/(main)/admin/components/AdminSidebar.tsx
```

删除入口：

```text
容器管理
容器模板
```

### 6.7 前端类型调整

文件：

```text
agentx-frontend-plus/types/tool.ts
agentx-frontend-plus/app/(main)/tools/utils/types.ts
```

新增：

```ts
export type ToolRuntimeType =
  | "REMOTE_MCP_SSE"
  | "REMOTE_MCP_STREAMABLE_HTTP"
  | "HTTP_API"

export type ToolAuthType =
  | "NONE"
  | "BEARER_TOKEN"
  | "API_KEY_HEADER"
  | "API_KEY_QUERY"
  | "BASIC"

export interface ToolRuntimeConfig {
  runtimeType: ToolRuntimeType
  transportType?: string
  endpointUrl?: string
  mcpServerName?: string
  httpMethod?: string
  authType: ToolAuthType
  authConfigSchema?: Record<string, any>
  defaultHeaders?: Record<string, any>
  requestSchema?: Record<string, any>
  responseSchema?: Record<string, any>
  timeoutMs?: number
}
```

删除或废弃：

```ts
upload_url
install_command
isGlobal
```

兼容期间可以保留可选字段，但页面不再使用。

## 7. 配置和 Docker 改造

### 7.1 删除 Compose 配置

文件：

```text
deploy/docker-compose.yml
```

删除：

- `mcp-gateway` service。
- `mcp-gateway-data` volume。
- backend 环境变量：
  - `MCP_GATEWAY_BASE_URL`
  - `MCP_GATEWAY_API_KEY`
  - `MCP_GATEWAY_CONNECT_TIMEOUT`
  - `MCP_GATEWAY_READ_TIMEOUT`
  - `AGENTX_CONTAINER_USER_VOLUME_PATH`
  - `AGENTX_CONTAINER_DEFAULT_MCP_IMAGE`
- Docker socket 挂载如果只服务容器工具链路，也删除：
  - `/var/run/docker.sock:/var/run/docker.sock`

注意：

- 删除 Docker socket 挂载前确认其他业务没有依赖 Docker。
- 目前容器管理服务就是旧工具链路的一部分，删除后 backend 不应再需要 Docker socket。

### 7.2 删除 env 配置

文件：

```text
.env
deploy/.env
.env.example
.env.local.example
```

删除：

```text
MCP_GATEWAY_HOST_PORT
MCP_GATEWAY_BASE_URL
MCP_GATEWAY_API_KEY
MCP_GATEWAY_CONNECT_TIMEOUT
MCP_GATEWAY_READ_TIMEOUT
AGENTX_CONTAINER_USER_VOLUME_PATH
AGENTX_CONTAINER_DEFAULT_MCP_IMAGE
GITHUB_USERNAME
GITHUB_TOKEN
GITHUB_REPO_NAME
```

如果 GitHub 登录或其他功能仍使用 GitHub，不能全局删除 GitHub 配置，只删除 `github.target.*` 工具发布配置。

## 8. 代码迁移步骤

### 阶段 1：数据库和 DTO 准备

1. 新增 `ToolRuntimeType`。
2. 新增 `ToolAuthType`。
3. 新增 `ToolRuntimeConfigEntity`。
4. 新增 `UserToolCredentialEntity`。
5. 新增 migration。
6. 新增 `ToolRuntimeConfigRequest`。
7. `CreateToolRequest` / `UpdateToolRequest` 增加 `runtimeConfig`。
8. 保留旧字段但标记废弃，避免一次性编译改动过大。

验收：

- 项目能编译。
- migration 能执行。
- 旧工具列表接口不受影响。

### 阶段 2：替换工具提交审核链路

1. 新增 `ToolEndpointProbeService`。
2. 新增 `RemoteMcpProbeClient`。
3. 新增 `HttpApiProbeClient`。
4. 改 `ToolAppService.uploadTool()`。
5. 改 `ToolAppService.updateTool()`。
6. 改 `AdminToolAppService.updateToolStatus()`。
7. 删除 `ToolStateStateMachineAppService` 对 GitHub/Gateway 的依赖。
8. 删除旧 processor 或先从注册列表中移除。

验收：

- 上传远程 MCP 工具不访问 GitHub。
- 上传 HTTP API 工具不访问 GitHub。
- 上传工具不创建 Docker 容器。
- 工具探测成功后进入 `MANUAL_REVIEW`。
- 管理员审核通过后进入 `APPROVED` 并自动安装给创建者。

### 阶段 3：替换对话调用链路

1. 删除 `McpUrlProviderService`。
2. 新增 `DirectToolRuntimeResolver`。
3. 新增 `DirectToolProviderFactory`。
4. 新增 `RemoteMcpToolProviderFactory`。
5. 新增 `HttpApiToolProviderFactory`。
6. 新增 `HttpApiToolExecutor`。
7. 改 `AgentToolManager`。
8. 保留原 `toolPresetParams` 逻辑，但只对 MCP 工具生效。

验收：

- 对话时不会访问 `mcp-gateway`。
- 对话时不会调用 `ContainerAppService`。
- 远程 MCP 工具能被模型识别并调用。
- HTTP API 工具能被模型识别并调用。

### 阶段 4：前端用户端改造

1. 改工具上传页。
2. 改工具编辑页。
3. 改工具详情页。
4. 改安装弹窗。
5. 改审核状态弹窗。
6. 改类型定义。
7. 增加“测试连接”按钮和接口调用。

验收：

- 用户上传工具页面不再出现 GitHub 仓库和安装命令。
- 用户能填写远程 MCP URL。
- 用户能填写 HTTP API schema。
- 测试连接成功后展示工具列表。
- 提交后进入审核状态。

### 阶段 5：前端管理端改造

1. 改管理端工具列表。
2. 改审核详情展示。
3. 删除/隐藏全局工具开关。
4. 删除容器管理入口。
5. 删除容器模板入口。
6. 删除容器相关 service 文件。

验收：

- 管理端只审核工具配置和工具定义。
- 管理端没有容器管理入口。
- 管理端没有 MCP Gateway 状态入口。

### 阶段 6：清理旧代码和配置

1. 删除容器后端模块。
2. 删除 DockerService。
3. 删除 MCPGatewayService。
4. 删除 GitHub 发布模块。
5. 删除 compose 中 mcp-gateway。
6. 删除 env 中 gateway/github publish 配置。
7. 删除旧数据库表。

验收：

- `rg "MCPGatewayService|ContainerAppService|DockerService|AppPublishingProcessor|AppDeployingProcessor"` 不应再有业务引用。
- `docker ps` 不需要 `agentx-mcp-gateway`。
- 后端启动不需要 Docker socket。

## 9. 关键兼容策略

### 9.1 旧数据

旧工具数据中可能存在：

```text
upload_url
install_command
is_global
status = GITHUB_URL_VALIDATE / DEPLOYING / FETCHING_TOOLS
```

迁移策略：

- 本地开发环境可直接清空旧工具数据。
- 如果保留旧工具：
  - `status in (GITHUB_URL_VALIDATE, DEPLOYING, FETCHING_TOOLS)` 统一改为 `FAILED`。
  - `reject_reason = '旧 MCP Gateway 工具链路已废弃，请重新提交远程 MCP / HTTP 配置'`。

SQL：

```sql
update tools
set status = 'FAILED',
    reject_reason = '旧 MCP Gateway 工具链路已废弃，请重新提交远程 MCP / HTTP 配置',
    updated_at = now()
where status in ('GITHUB_URL_VALIDATE', 'DEPLOYING', 'FETCHING_TOOLS');
```

### 9.2 接口兼容

第一阶段可以继续返回：

```json
{
  "uploadUrl": null,
  "installCommand": null,
  "isGlobal": false
}
```

避免前端旧组件空指针。

前端完成后再删除这些字段。

### 9.3 安全边界

HTTP API 工具调用必须限制：

- 禁止访问内网地址：
  - `127.0.0.1`
  - `localhost`
  - `0.0.0.0`
  - `10.0.0.0/8`
  - `172.16.0.0/12`
  - `192.168.0.0/16`
- 禁止 file 协议。
- 禁止任意 shell 命令。
- 设置超时。
- 限制响应最大长度。

第一阶段如果来不及做完整 SSRF 防护，至少限制协议必须是 `https://`。

## 10. 验收清单

### 10.1 后端验收

- 后端启动成功。
- 上传远程 MCP 工具时，不访问 GitHub。
- 上传远程 MCP 工具时，不创建 Docker 容器。
- 上传 HTTP API 工具时，不创建 Docker 容器。
- 工具进入 `MANUAL_REVIEW`。
- 管理员审核通过后，工具进入 `APPROVED`。
- 创建者自动安装工具。
- Agent 对话能直接调用远程 MCP。
- Agent 对话能调用 HTTP API 工具。
- `docker ps` 不需要 mcp-gateway。

### 10.2 前端验收

- 用户端上传工具页展示“远程 MCP / HTTP API”。
- 用户端不展示 GitHub 仓库字段。
- 用户端不展示安装命令字段。
- 用户端可以测试连接。
- 管理端工具审核页展示工具 endpoint、runtimeType、toolList。
- 管理端没有容器管理入口。
- 审核状态弹窗不再展示部署中、GitHub 校验、获取工具列表。

### 10.3 清理验收

执行：

```bash
rg "MCPGatewayService|mcp-gateway|ContainerAppService|DockerService|AppDeployingProcessor|AppFetchingToolsProcessor|AppPublishingProcessor|GitHub URL|安装命令" AgentX/src/main/java agentx-frontend-plus deploy
```

期望：

- 没有业务引用。
- 只允许在历史文档或迁移说明里出现。

## 11. 推荐最终文件结构

新增后端文件：

```text
AgentX/src/main/java/org/xhy/domain/tool/constant/ToolRuntimeType.java
AgentX/src/main/java/org/xhy/domain/tool/constant/ToolAuthType.java
AgentX/src/main/java/org/xhy/domain/tool/model/ToolRuntimeConfigEntity.java
AgentX/src/main/java/org/xhy/domain/tool/model/UserToolCredentialEntity.java
AgentX/src/main/java/org/xhy/domain/tool/repository/ToolRuntimeConfigRepository.java
AgentX/src/main/java/org/xhy/domain/tool/repository/UserToolCredentialRepository.java
AgentX/src/main/java/org/xhy/domain/tool/service/ToolRuntimeConfigDomainService.java
AgentX/src/main/java/org/xhy/domain/tool/service/UserToolCredentialDomainService.java
AgentX/src/main/java/org/xhy/infrastructure/repository/ToolRuntimeConfigRepositoryImpl.java
AgentX/src/main/java/org/xhy/infrastructure/repository/UserToolCredentialRepositoryImpl.java
AgentX/src/main/java/org/xhy/application/tool/service/runtime/ToolEndpointProbeService.java
AgentX/src/main/java/org/xhy/application/tool/service/runtime/ToolEndpointProbeResult.java
AgentX/src/main/java/org/xhy/application/tool/service/runtime/RemoteMcpProbeClient.java
AgentX/src/main/java/org/xhy/application/tool/service/runtime/HttpApiProbeClient.java
AgentX/src/main/java/org/xhy/application/tool/service/ToolReviewAppService.java
AgentX/src/main/java/org/xhy/application/conversation/service/tool/DirectToolRuntimeResolver.java
AgentX/src/main/java/org/xhy/application/conversation/service/tool/ResolvedToolRuntime.java
AgentX/src/main/java/org/xhy/application/conversation/service/tool/DirectToolProviderFactory.java
AgentX/src/main/java/org/xhy/application/conversation/service/tool/RemoteMcpToolProviderFactory.java
AgentX/src/main/java/org/xhy/application/conversation/service/tool/HttpApiToolProviderFactory.java
AgentX/src/main/java/org/xhy/application/conversation/service/tool/HttpApiToolExecutor.java
AgentX/src/main/java/org/xhy/interfaces/dto/tool/request/ToolRuntimeConfigRequest.java
AgentX/src/main/java/org/xhy/interfaces/dto/tool/request/UserToolCredentialRequest.java
```

新增数据库 migration：

```text
AgentX/src/main/resources/db/migration/V2026xxxxxxx__replace_mcp_gateway_with_direct_tool_runtime.sql
```

改造前端文件：

```text
agentx-frontend-plus/app/(main)/tools/upload/page.tsx
agentx-frontend-plus/app/(main)/tools/edit/[id]/page.tsx
agentx-frontend-plus/app/(main)/tools/components/dialogs/ReviewStatusDialog.tsx
agentx-frontend-plus/app/(main)/tools/components/dialogs/InstallToolDialog.tsx
agentx-frontend-plus/app/(main)/tools/components/dialogs/UserToolDetailDialog.tsx
agentx-frontend-plus/app/(main)/admin/tools/page.tsx
agentx-frontend-plus/app/(main)/admin/components/AdminSidebar.tsx
agentx-frontend-plus/lib/tool-service.ts
agentx-frontend-plus/lib/admin-tool-service.ts
agentx-frontend-plus/types/tool.ts
agentx-frontend-plus/app/(main)/tools/utils/types.ts
```

删除前端文件：

```text
agentx-frontend-plus/app/(main)/admin/containers/page.tsx
agentx-frontend-plus/app/(main)/admin/container-templates/page.tsx
agentx-frontend-plus/app/(main)/templates/page.tsx
agentx-frontend-plus/lib/admin-container-service.ts
agentx-frontend-plus/lib/container-template-service.ts
```

删除后端文件见第 4.1 节。

## 12. 最终判断

这次重构不是在 MCP Gateway 上修 bug，而是替换工具运行模型：

```text
旧：平台部署工具
新：平台调用工具
```

因此 coding 时要避免“保留旧 Gateway，再新增远程调用”的双轨实现。双轨会让状态、页面、配置、数据库都更复杂。正确做法是：

- 工具提交链路替换为 endpoint 探测。
- 对话调用链路替换为 direct runtime resolver。
- 容器链路整体删除。
- GitHub 发布链路整体删除。

这样改完后，AgentX 的 MCP/工具能力仍然完整，但实现难度和运行风险会明显降低。
