# agentool

agentool 是我的本科毕业设计项目，是一个从 0 到 1 设计并实现的智能体应用平台。项目围绕 AI Agent 的创建、运行、工具调用、知识库问答、模型服务商配置和后台管理展开，覆盖需求分析、系统设计、数据库建模、后端接口开发、前端页面实现和 Docker 本地部署等完整工程环节。

这个项目不是一个简单的聊天 Demo，而是一个前后端分离的 AI 应用平台。后端负责用户认证、Agent 编排、模型调用、RAG 检索、远程 MCP/HTTP 工具调用、会话上下文和权限控制；前端负责用户工作台、Agent 工作室、知识库、工具市场、模型配置和管理后台等操作界面。

## 核心功能

- 用户认证：支持登录、注册、密码重置、用户信息维护和后台用户管理。
- Agent 管理：支持创建 Agent、配置提示词、维护版本、发布审核和工作区使用。
- 智能对话：支持会话管理、上下文处理、流式响应和 Agent 工具调用。
- 工具市场：支持工具发布、审核、安装和远程 MCP SSE / HTTP API 工具接入。
- 知识库 RAG：支持文件上传、解析处理、向量检索、知识库发布和知识库问答。
- 模型服务商：支持配置第三方模型服务商、模型信息和用户自己的 API Key。
- 开放接口：支持 API Key 管理和外部 Open API 调用。
- 管理后台：支持用户、Agent、工具、RAG、服务商和认证配置等后台治理能力。

## 技术栈

### 后端

- Java 17
- Spring Boot 3.2.3
- MyBatis-Plus
- PostgreSQL + pgvector
- RabbitMQ
- LangChain4j
- JWT
- x-file-storage / S3 兼容对象存储

### 前端

- Next.js 15
- React 19
- TypeScript
- Tailwind CSS
- Radix UI
- React Hook Form / Zod
- Axios

### 部署与中间件

- Docker
- Docker Compose
- PostgreSQL
- RabbitMQ Management
- MinIO
- Adminer

## 项目结构

```text
agentool/
├── agentool-backend/          # Spring Boot 后端工程
│   ├── src/main/java/org/xh
│   │   ├── application        # 应用服务层，编排业务用例
│   │   ├── domain             # 领域层，承载核心业务模型与规则
│   │   ├── infrastructure     # 基础设施层，连接数据库、模型、文件存储、消息队列等
│   │   └── interfaces         # 接口层，提供用户端、管理端和开放接口
│   └── src/main/resources     # 配置文件、数据库迁移和 SQL 脚本
├── agentool-frontend/         # Next.js 前端工程
│   ├── app                    # 页面路由
│   ├── components             # 业务组件和通用 UI 组件
│   ├── hooks                  # 前端复用状态逻辑
│   ├── lib                    # API 客户端和请求封装
│   └── types                  # TypeScript 类型定义
├── deploy/                    # 本地 Docker Compose 启动配置
├── docker/                    # 前后端 Dockerfile
├── docs/sql/                  # 数据库初始化脚本
└── production/                # 生产部署参考配置
```

## 本地启动

推荐使用 Docker Compose 启动完整开发环境。项目运行依赖 PostgreSQL、RabbitMQ、MinIO、前端、后端以及容器网络中的环境变量配置，直接在宿主机分别启动前后端容易出现配置不一致。

### 环境要求

- Docker Desktop
- Docker Compose
- JDK 17+
- Node.js 18+
- Maven 3.8+

### 启动命令

```bash
./deploy/start.sh
```

启动后常用访问地址：

```text
前端页面: http://localhost:3000
后端接口: http://localhost:8088
数据库管理: http://localhost:8082
RabbitMQ 管理台: http://localhost:15672
MinIO 控制台: http://localhost:9001
```

默认开发账号：

```text
管理员: admin@agentool.ai / admin123
测试用户: test@agentool.ai / test123
```

常用 Docker 命令：

```bash
docker compose -f deploy/docker-compose.yml --env-file deploy/.env ps
docker compose -f deploy/docker-compose.yml --env-file deploy/.env logs -f
docker compose -f deploy/docker-compose.yml --env-file deploy/.env restart
docker compose -f deploy/docker-compose.yml --env-file deploy/.env down
```

## 主要页面

- `/login`：登录页面
- `/register`：注册页面
- `/workspace`：工作区与聊天入口
- `/studio`：Agent 创建与管理
- `/knowledge`：知识库管理
- `/tools-market`：工具市场
- `/tools`：我的工具
- `/settings`：个人设置、模型服务商和 API Key
- `/admin`：管理后台

## 后端模块

后端按使用场景和业务职责分层组织：

- `interfaces/api/portal`：普通用户端接口
- `interfaces/api/admin`：管理后台接口
- `interfaces/api/external`：外部开放接口
- `application`：应用服务层，负责业务用例编排
- `domain`：领域层，负责核心业务规则和实体模型
- `infrastructure`：基础设施层，负责数据库、消息队列、对象存储、模型服务、邮件和工具运行等外部能力

后端配置入口：

```text
agentool-backend/src/main/resources/application.yml
```

## 前端模块

前端使用 Next.js App Router 组织页面：

- `app`：页面路由和布局入口。
- `components`：业务组件、弹窗、卡片、表格和通用 UI。
- `lib`：请求封装、服务模块和 API 配置。
- `hooks`：聊天、表单、上传等复用逻辑。
- `types`：前后端交互数据结构。

## 项目特点

- 从 0 到 1 完成系统设计、编码实现和本地部署，适合作为本科毕业设计工程实践。
- 以后端业务架构为核心，围绕 Agent、RAG、工具调用、模型服务商、会话上下文和后台治理组织代码。
- 支持 Docker Compose 一键启动本地演示环境，便于论文演示、答辩展示和后续功能验证。
- 前端提供用户端和管理端完整界面，不只停留在接口或单页 Demo。
- 工具能力支持远程 MCP / HTTP API 接入，知识库能力支持上传、解析、检索和问答闭环。

## 项目说明

本仓库主要用于个人本科毕业设计、工程实践展示和后续学习扩展。当前项目以本地开发和毕设演示为主要目标，如需用于生产环境，还需要进一步完善生产级密钥管理、权限隔离、日志审计、数据备份、监控告警和部署安全策略。
