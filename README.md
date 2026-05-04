# AgentX

AgentX 是我的本科毕业设计项目，是一个从 0 到 1 设计并实现的智能体应用平台。项目围绕 AI Agent 的创建、运行、工具调用、知识库问答和平台化管理展开，覆盖需求分析、系统设计、数据库建模、后端接口开发、前端页面实现、Docker 本地部署等完整工程环节。

系统提供 Agent 创建、会话聊天、工具调用、知识库问答、长期记忆、模型服务商配置、开放 API、嵌入式组件和后台管理等能力。项目采用前后端分离架构，后端负责业务编排、模型调用、RAG 检索、MCP 工具接入、容器管理、计费与权限控制，前端提供用户工作台、知识库、工具市场、管理后台等操作界面。

该项目适合作为智能体平台、RAG 应用平台、AI 助手系统或毕业设计项目的完整工程参考。

## 项目定位

本项目定位为毕业设计中的完整工程实践，不只是单一聊天 Demo。系统从传统 Java 后端工程能力出发，将大模型调用、Agent 编排、RAG 知识库、MCP 工具接入、会话管理、记忆管理和后台运维管理整合到同一个平台中，用于展示 AI 应用平台在真实业务系统中的设计与实现方式。

项目的主要目标包括：

- 完成一个可运行、可演示、可扩展的 AI Agent 平台。
- 将前端页面、后端服务、数据库、中间件和 Docker 部署整合成完整系统。
- 通过 RAG、MCP、模型服务商管理、执行追踪等模块体现智能体平台的核心技术点。
- 形成能够支撑毕业论文、答辩演示和后续扩展开发的工程基础。

## 核心功能

- 用户认证：登录、注册、重置密码、认证配置与用户管理。
- Agent 管理：创建 Agent、配置提示词、管理版本、工作区和会话。
- 智能对话：支持流式聊天、会话管理、执行追踪和上下文处理。
- 工具市场：支持工具安装、工具调用、MCP 工具接入和容器化运行。
- 知识库 RAG：支持文件上传、知识库管理、文件解析、向量检索和知识库发布。
- 长期记忆：支持用户记忆管理和对话中的长期信息复用。
- 模型服务商：支持服务商、模型、密钥和高可用网关相关配置。
- 开放能力：提供 API Key、Open API 和 Widget 嵌入式聊天组件。
- 计费订单：支持商品、订单、支付、用量记录和规则配置。
- 管理后台：提供用户、Agent、工具、RAG、容器、订单、规则、认证等后台管理能力。

## 技术栈

### 后端

- Java 17
- Spring Boot 3.2.3
- MyBatis-Plus
- PostgreSQL + pgvector
- RabbitMQ
- LangChain4j
- JWT
- Docker API
- x-file-storage / S3 兼容文件存储

### 前端

- Next.js 15
- React 19
- TypeScript
- Tailwind CSS
- Radix UI
- React Hook Form / Zod
- Axios

### 部署与基础设施

- Docker
- Docker Compose
- PostgreSQL
- RabbitMQ Management
- Adminer
- API Gateway

## 项目结构

```text
AgentX/
├── AgentX/                  # 后端 Spring Boot 工程
│   ├── src/main/java/org/xhy
│   │   ├── application      # 应用服务层，编排业务用例
│   │   ├── domain           # 领域层，承载核心业务模型与规则
│   │   ├── infrastructure   # 基础设施层，数据库、外部服务、配置等
│   │   └── interfaces       # 接口层，Controller 与对外 API
│   └── src/main/resources   # 后端配置与 SQL 脚本
├── agentx-frontend-plus/    # 前端 Next.js 工程
│   ├── app                  # 页面路由
│   ├── components           # UI 组件
│   ├── lib                  # API 客户端与业务请求封装
│   └── types                # TypeScript 类型定义
├── deploy/                  # 本地开发 Docker Compose 配置
├── docker/                  # 前后端 Dockerfile
├── docs/sql/                # 数据库初始化脚本
└── production/              # 生产环境部署配置
```

## 本地启动

### 环境要求

- JDK 17+
- Node.js 18+
- Docker Desktop
- Docker Compose
- Maven 3.8+

### 使用 Docker Compose 启动

进入部署目录：

```bash
cd deploy
```

执行启动脚本：

```bash
./start.sh
```

启动后常用访问地址：

```text
前端页面: http://localhost:3000
后端接口: http://localhost:8088
API 网关: http://localhost:8081
数据库管理: http://localhost:8082
RabbitMQ 管理台: http://localhost:15672
```

默认开发账号：

```text
管理员: admin@agentx.ai / admin123
测试用户: test@agentx.ai / test123
```

常用 Docker 命令：

```bash
docker compose ps
docker compose logs -f
docker compose restart
docker compose down
```

## 本地开发

### 后端

```bash
cd AgentX
./mvnw spring-boot:run
```

后端配置文件位于：

```text
AgentX/src/main/resources/application.yml
```

### 前端

```bash
cd agentx-frontend-plus
npm install
npm run dev
```

前端默认运行在：

```text
http://localhost:3000
```

## 主要页面

- `/login`：登录
- `/register`：注册
- `/workspace`：工作区与聊天入口
- `/studio`：Agent 创建与管理
- `/knowledge`：知识库管理
- `/tools-market`：工具市场
- `/tools`：我的工具
- `/traces`：执行追踪
- `/admin`：管理后台
- `/widget/[publicId]`：外部嵌入式聊天组件

## 主要接口模块

后端接口按使用场景划分：

- `interfaces/api/portal`：普通用户端接口
- `interfaces/api/admin`：管理后台接口
- `interfaces/api/external`：外部开放接口
- `interfaces/api/public_api`：公开访问接口，例如 Widget 聊天

典型模块包括：

- 用户与认证
- Agent 与工作区
- 会话与消息
- RAG 知识库
- 工具与容器
- 模型服务商
- API Key
- 计费、订单与支付
- 管理后台

## 项目特点

- 从 0 到 1 完成系统设计与工程实现，覆盖前端、后端、数据库和部署配置。
- 以后端业务架构为核心，采用分层设计组织 Agent、RAG、工具、会话、用户、计费等模块。
- 支持 Docker Compose 一键启动本地开发环境，方便毕业设计演示和功能验证。
- 保留较完整的平台化能力，不局限于单轮问答，而是面向 Agent 应用的创建、运行、管理和发布。

## 适用场景

- AI Agent 平台
- 企业知识库问答系统
- RAG 应用管理平台
- MCP 工具调用平台
- 智能助手毕业设计
- 前后端分离大型项目学习

## 项目说明

本仓库用于个人本科毕业设计、工程实践展示和后续学习扩展。当前项目以本地开发和毕设演示为主要目标，如需用于生产环境，还需要进一步补充安全配置、密钥管理、权限隔离、日志审计、生产部署和运维监控等能力。
