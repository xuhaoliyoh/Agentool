const W = 1280;
const H = 720;

const C = {
  ink: "#172026",
  slate: "#303B45",
  muted: "#5F6972",
  paper: "#F7F5EF",
  white: "#FFFFFF",
  line: "#D8D3C7",
  blue: "#2456A6",
  teal: "#2B8A83",
  red: "#B2473E",
  gold: "#C9922A",
  green: "#4E8D54",
  purple: "#6B4CA8",
};

const font = {
  title: "Microsoft YaHei",
  body: "Microsoft YaHei",
  mono: "Aptos Mono",
};

function bg(slide, ctx) {
  ctx.addShape(slide, { x: 0, y: 0, width: W, height: H, fill: C.paper });
  ctx.addShape(slide, { x: 0, y: 0, width: W, height: 12, fill: C.ink });
  ctx.addShape(slide, { x: 0, y: H - 10, width: W, height: 10, fill: C.ink });
}

function text(slide, ctx, value, x, y, width, height, opts = {}) {
  return ctx.addText(slide, {
    text: value,
    x,
    y,
    width,
    height,
    fontSize: opts.size || 20,
    color: opts.color || C.ink,
    bold: Boolean(opts.bold),
    typeface: opts.face || font.body,
    align: opts.align || "left",
    valign: opts.valign || "top",
    fill: opts.fill || { color: "transparent" },
    line: { style: "solid", fill: "transparent", width: 0 },
    insets: opts.insets || { left: 0, right: 0, top: 0, bottom: 0 },
  });
}

function title(slide, ctx, main, sub, source) {
  text(slide, ctx, main, 70, 48, 900, 60, { size: 26, bold: true, face: font.title });
  if (sub) text(slide, ctx, sub, 72, 110, 940, 28, { size: 15, color: C.muted });
  ctx.addShape(slide, { x: 70, y: 150, width: 1140, height: 1.2, fill: C.line });
  if (source) text(slide, ctx, source, 72, 673, 1060, 22, { size: 11, color: C.muted });
}

function slideNo(slide, ctx, n) {
  text(slide, ctx, String(n).padStart(2, "0"), 1120, 54, 80, 36, {
    size: 28,
    bold: true,
    color: C.line,
    align: "right",
  });
}

function box(slide, ctx, x, y, width, height, opts = {}) {
  ctx.addShape(slide, {
    x,
    y,
    width,
    height,
    fill: opts.fill || C.white,
    line: { style: "solid", fill: opts.line || C.line, width: opts.lineWidth || 1 },
  });
}

function chip(slide, ctx, label, x, y, width, color = C.blue) {
  ctx.addShape(slide, { x, y, width, height: 28, fill: color });
  text(slide, ctx, label, x + 11, y + 5, width - 22, 18, {
    size: 12,
    color: C.white,
    bold: true,
  });
}

function bullets(slide, ctx, items, x, y, width, opts = {}) {
  const gap = opts.gap || 44;
  const color = opts.color || C.blue;
  items.forEach((item, i) => {
    const yy = y + i * gap;
    ctx.addShape(slide, { x, y: yy + 7, width: 8, height: 8, fill: color });
    text(slide, ctx, item, x + 18, yy, width - 18, gap - 2, {
      size: opts.size || 17,
      color: opts.textColor || C.ink,
      bold: opts.bold || false,
    });
  });
}

function arrow(slide, ctx, x1, y1, x2, y2, color = C.muted) {
  if (Math.abs(y2 - y1) <= 4) {
    const x = Math.min(x1, x2);
    ctx.addShape(slide, { x, y: y1 - 1, width: Math.abs(x2 - x1), height: 2, fill: color });
    ctx.addShape(slide, { x: x2 - 8, y: y1 - 5, width: 10, height: 10, fill: color, geometry: "triangle" });
  } else {
    const y = Math.min(y1, y2);
    ctx.addShape(slide, { x: x1 - 1, y, width: 2, height: Math.abs(y2 - y1), fill: color });
  }
}

function flow(slide, ctx, labels, x, y, width, colors = [C.blue, C.teal, C.gold, C.red]) {
  const gap = 22;
  const itemW = (width - gap * (labels.length - 1)) / labels.length;
  labels.forEach((label, i) => {
    const xx = x + i * (itemW + gap);
    box(slide, ctx, xx, y, itemW, 74, { fill: C.white, line: colors[i % colors.length] });
    text(slide, ctx, label, xx + 8, y + 16, itemW - 16, 42, {
      size: 14,
      bold: true,
      color: colors[i % colors.length],
      align: "center",
    });
    if (i < labels.length - 1) arrow(slide, ctx, xx + itemW, y + 37, xx + itemW + gap, y + 37, C.muted);
  });
}

function codeBox(slide, ctx, lines, x, y, width, height, color = C.slate) {
  box(slide, ctx, x, y, width, height, { fill: "#FAFAF7", line: color });
  ctx.addShape(slide, { x, y, width: 8, height, fill: color });
  text(slide, ctx, lines.join("\n"), x + 18, y + 14, width - 28, height - 22, {
    size: 12.5,
    color: C.ink,
    face: font.mono,
  });
}

function sourceTag(slide, ctx, path, x, y, width) {
  text(slide, ctx, path, x, y, width, 20, { size: 10.5, color: C.muted, face: font.mono });
}

const slides = [
  {
    kind: "cover",
    title: "基于大模型的智能体生成工具的设计与实现",
    subtitle: "代码链路讲解版答辩 PPT | 徐昊 20222241361",
  },
  {
    kind: "agenda",
    title: "本版汇报重点：从源码解释平台如何运行",
    subtitle: "围绕四条代码主链路展开，不只做功能罗列",
    source: "答辩重点来自论文第 5 章与 agentool 后端源码",
  },
  {
    kind: "layers",
    title: "工程主线：接口层、应用层、领域层、基础设施层分工",
    subtitle: "后续四条链路都按这个调用方向讲解",
    source: "源码包：interfaces / application / domain / infrastructure",
  },
  {
    kind: "loginFlow",
    title: "登录链路：从账号密码到 JWT Token",
    subtitle: "登录接口只负责接收请求，认证开关、密码校验、Token 生成分别下沉到服务层",
    source: "LoginController.java / LoginAppService.java / UserDomainService.java / JwtUtils.java",
  },
  {
    kind: "jwtDetail",
    title: "JWT 生成与校验：subject 保存 userId，过期时间 24 小时",
    subtitle: "系统不在每次请求查登录态，而是通过签名 Token 恢复用户身份",
    source: "JwtUtils.java",
  },
  {
    kind: "authInterceptor",
    title: "鉴权拦截器：请求进入业务前恢复 UserContext",
    subtitle: "拦截器解析 Authorization: Bearer token，校验后写入 ThreadLocal，并在请求结束清理",
    source: "UserAuthInterceptor.java / UserContext.java / WebMvcConfig.java",
  },
  {
    kind: "dialogEntry",
    title: "用户对话入口：Controller 不处理模型细节",
    subtitle: "前端工作区发送消息后，后端进入统一 ConversationAppService.chat",
    source: "PortalAgentSessionController.java / ConversationAppService.java",
  },
  {
    kind: "dialogEnv",
    title: "对话环境装配：一次请求先拼出 ChatContext",
    subtitle: "会话、Agent、工具、模型、服务商、上下文历史都会在调用模型前准备好",
    source: "ConversationAppService.prepareEnvironmentWithModel",
  },
  {
    kind: "context",
    title: "上下文治理：消息历史不是无限拼接",
    subtitle: "ContextEntity 维护 activeMessages；Token 策略决定保留、裁剪或摘要压缩",
    source: "ConversationAppService.applyTokenOverflowStrategy",
  },
  {
    kind: "handler",
    title: "对话模板方法：AbstractMessageHandler 串起整轮生成",
    subtitle: "保存用户消息、构建内存、装配工具、流式输出、保存助手消息都在同一模板流程中完成",
    source: "AbstractMessageHandler.chat / processChat",
  },
  {
    kind: "stream",
    title: "流式响应与持久化：边生成边发，结束后回写上下文",
    subtitle: "SSE 负责前端增量展示；MessageDomainService 负责消息和上下文状态落库",
    source: "SseMessageTransport.java / AbstractMessageHandler.processChat",
  },
  {
    kind: "toolOverview",
    title: "工具调用总览：AgentToolManager 把已安装工具交给 LangChain4j",
    subtitle: "工具不是写死在 Prompt 中，而是被转成 ToolProvider 供模型按需调用",
    source: "AgentMessageHandler.java / AgentToolManager.java",
  },
  {
    kind: "toolRuntime",
    title: "工具运行时解析：serverName -> 用户安装记录 -> 运行配置 -> 凭据",
    subtitle: "DirectToolRuntimeResolver 保证调用的工具属于当前用户并且有运行配置",
    source: "DirectToolRuntimeResolver.java / DirectToolRuntime.java",
  },
  {
    kind: "toolInvoke",
    title: "远程工具调用：HTTP API 与 MCP SSE 两种执行路径",
    subtitle: "HTTP 工具由 HttpApiToolExecutor 请求端点；MCP 工具创建远程 SSE Client",
    source: "HttpApiToolExecutor.java / RemoteMcpToolProviderFactory.java / ToolAuthSupport.java",
  },
  {
    kind: "ragIngest",
    title: "RAG 入库链路：上传后异步 OCR/解析，再自动触发向量化",
    subtitle: "文件处理不是前台同步完成，而是通过 RabbitMQ 和状态机推进",
    source: "RagDocConsumer.java / RagDocStorageConsumer.java",
  },
  {
    kind: "ragSplit",
    title: "RAG 切分逻辑：Markdown 两阶段处理保留原文并生成向量片段",
    subtitle: "第一阶段按结构保存原文 DocumentUnit，第二阶段翻译特殊节点、二次切分并发送向量化任务",
    source: "MarkdownRagDocumentProcessing.java / DocumentVectorizationOrchestrator.java",
  },
  {
    kind: "vector",
    title: "向量化与元数据：向量内容和原文记录分离",
    subtitle: "向量库存储 TextSegment 和 metadata，metadata 反向关联 fileId、datasetId、documentId",
    source: "EmbeddingDomainService.syncStorage / buildMetadata",
  },
  {
    kind: "ragSearch",
    title: "RAG 检索：权限过滤后构建 HybridSearchConfig",
    subtitle: "搜索前验证用户是否安装或创建知识库，再注入嵌入模型与聊天模型配置",
    source: "RAGSearchAppService.ragSearch",
  },
  {
    kind: "hybrid",
    title: "混合检索：向量检索 + 关键词检索 + RRF 融合 + 重排序",
    subtitle: "向量和关键词并行执行，RRF 解决不同检索结果的排序融合问题",
    source: "HybridSearchDomainService.hybridSearch / fusionWithRRF",
  },
  {
    kind: "ragTool",
    title: "RAG 如何进入对话：作为内置工具 knowledge_search",
    subtitle: "Agent 绑定知识库后，RAG 工具会被注册进 AiServices，模型需要资料时主动调用",
    source: "RagBuiltInToolProvider.java / AbstractMessageHandler.buildStreamingAgent",
  },
  {
    kind: "close",
    title: "总结：系统价值体现在四条可运行的工程闭环",
    subtitle: "登录身份闭环、对话状态闭环、RAG 知识闭环、工具调用闭环",
    source: "结论依据：论文第 5、6 章和当前 agentool 源码",
  },
];

async function cover(slide, ctx, s) {
  ctx.addShape(slide, { x: 0, y: 0, width: W, height: H, fill: C.ink });
  ctx.addShape(slide, { x: 0, y: 0, width: 720, height: H, fill: "#203244" });
  ctx.addShape(slide, { x: 720, y: 0, width: 560, height: H, fill: C.paper });
  text(slide, ctx, s.title, 78, 116, 590, 120, { size: 38, bold: true, color: C.white, face: font.title });
  text(slide, ctx, s.subtitle, 82, 262, 560, 36, { size: 18, color: "#D9E2EA" });
  chip(slide, ctx, "本科毕业设计答辩", 82, 340, 164, C.gold);
  text(slide, ctx, "本版重点：核心代码逻辑讲解\n登录 JWT | 用户对话 | RAG 切分检索 | 工具调用", 82, 430, 520, 80, {
    size: 22,
    color: C.white,
    bold: true,
  });
  const items = [
    ["LoginAppService", C.blue],
    ["ConversationAppService", C.teal],
    ["HybridSearchDomainService", C.green],
    ["AgentToolManager", C.red],
  ];
  items.forEach((item, i) => {
    box(slide, ctx, 790, 132 + i * 104, 350, 72, { fill: C.white, line: item[1] });
    ctx.addShape(slide, { x: 790, y: 132 + i * 104, width: 9, height: 72, fill: item[1] });
    text(slide, ctx, item[0], 820, 154 + i * 104, 280, 26, { size: 20, bold: true, color: item[1] });
  });
}

async function agenda(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  const items = [
    ["01", "登录 JWT", "认证开关、密码校验、Token 签发、拦截器恢复用户上下文", C.blue],
    ["02", "用户对话", "会话校验、Agent/模型/工具装配、上下文策略、SSE 流式响应", C.teal],
    ["03", "RAG", "文件异步解析、层次化切分、向量化、混合检索和内置工具", C.green],
    ["04", "工具调用", "用户安装工具、运行配置解析、HTTP API 与远程 MCP SSE 执行", C.red],
  ];
  items.forEach((item, i) => {
    const y = 185 + i * 95;
    text(slide, ctx, item[0], 92, y + 8, 60, 34, { size: 26, bold: true, color: item[3] });
    ctx.addShape(slide, { x: 168, y: y + 25, width: 230, height: 2, fill: item[3] });
    text(slide, ctx, item[1], 430, y + 2, 170, 32, { size: 22, bold: true, color: item[3] });
    text(slide, ctx, item[2], 625, y - 2, 500, 50, { size: 16.5 });
  });
}

async function layers(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  const rows = [
    ["interfaces", "Controller：接收 HTTP 请求，读取 UserContext，返回 Result/SseEmitter", C.blue],
    ["application", "AppService：编排一次业务用例，例如登录、对话、RAG 搜索", C.teal],
    ["domain", "DomainService/Entity：表达业务规则、状态流转和数据合法性", C.red],
    ["infrastructure", "外部能力：JWT、LLM、MQ、对象存储、向量库、MCP/HTTP 工具", C.gold],
  ];
  rows.forEach((r, i) => {
    const y = 184 + i * 88;
    box(slide, ctx, 110, y, 920, 58, { fill: C.white, line: r[2] });
    ctx.addShape(slide, { x: 110, y, width: 10, height: 58, fill: r[2] });
    text(slide, ctx, r[0], 144, y + 16, 180, 26, { size: 21, bold: true, color: r[2], face: font.mono });
    text(slide, ctx, r[1], 360, y + 17, 600, 24, { size: 17 });
    if (i < rows.length - 1) arrow(slide, ctx, 570, y + 58, 570, y + 88, C.muted);
  });
  text(slide, ctx, "答辩时的讲法：每个功能都说清楚“入口在哪、谁编排、规则在哪、外部依赖怎么接”。", 168, 586, 820, 30, {
    size: 19,
    bold: true,
    align: "center",
  });
}

async function loginFlow(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  flow(slide, ctx, ["POST /login", "检查 NORMAL_LOGIN", "查询用户", "BCrypt 校验", "生成 JWT", "返回 token"], 95, 206, 1040);
  codeBox(slide, ctx, [
    "LoginController.login()",
    "  token = loginAppService.login(request)",
    "  return { token }",
    "",
    "LoginAppService.login()",
    "  isFeatureEnabled(NORMAL_LOGIN)",
    "  userDomainService.login(account, password)",
    "  JwtUtils.generateToken(userId)",
  ], 145, 370, 470, 178, C.blue);
  codeBox(slide, ctx, [
    "UserDomainService.login()",
    "  where email = account or phone = account",
    "  PasswordUtils.matches(raw, encoded)",
    "  fail -> BusinessException(\"账号密码错误\")",
  ], 675, 388, 430, 126, C.teal);
}

async function jwtDetail(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  codeBox(slide, ctx, [
    "generateToken(userId)",
    "  subject(userId)",
    "  issuedAt(now)",
    "  expiration(now + 24h)",
    "  signWith(HMAC key)",
    "  compact()",
  ], 105, 200, 360, 184, C.blue);
  codeBox(slide, ctx, [
    "validateToken(token)",
    "  parseSignedClaims(token)",
    "  verifyWith(signingKey)",
    "  expiration.before(now) -> false",
    "  JwtException -> false",
  ], 500, 200, 360, 184, C.red);
  codeBox(slide, ctx, [
    "getUserIdFromToken(token)",
    "  claims = parseSignedClaims(token)",
    "  return claims.getSubject()",
  ], 895, 200, 280, 184, C.teal);
  bullets(slide, ctx, [
    "Token 中不保存完整用户信息，只把 userId 放到 subject，降低载荷复杂度。",
    "过期时间在 JwtUtils 内统一控制，当前实现为 24 小时。",
    "校验失败不会进入业务 Controller，而是在拦截器层返回 401。"
  ], 150, 470, 920, { color: C.gold, size: 18, gap: 48 });
}

async function authInterceptor(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  flow(slide, ctx, ["请求进入", "读取 Authorization", "校验 Bearer", "validateToken", "解析 userId", "写 UserContext", "after 清理"], 76, 206, 1128, [C.blue, C.teal, C.gold, C.red]);
  codeBox(slide, ctx, [
    "preHandle(request)",
    "  authHeader = request.getHeader(AUTHORIZATION)",
    "  token = authHeader.substring(\"Bearer \".length())",
    "  JwtUtils.validateToken(token)",
    "  userId = JwtUtils.getUserIdFromToken(token)",
    "  UserContext.setCurrentUserId(userId)",
    "",
    "afterCompletion()",
    "  UserContext.clear()",
  ], 170, 360, 500, 210, C.teal);
  bullets(slide, ctx, [
    "ThreadLocal 让后续 Controller 可以直接 UserContext.getCurrentUserId()。",
    "WebMvcConfig 排除 /login、/register、/health 等公开接口。",
    "请求结束必须 clear，避免线程复用导致用户身份串扰。"
  ], 735, 380, 390, { color: C.red, size: 17, gap: 55 });
}

async function dialogEntry(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  codeBox(slide, ctx, [
    "PortalAgentSessionController.chat()",
    "  userId = UserContext.getCurrentUserId()",
    "  return conversationAppService.chat(request, userId)",
  ], 110, 205, 430, 118, C.blue);
  flow(slide, ctx, ["Controller", "ConversationAppService", "MessageTransport", "MessageHandler", "SseEmitter"], 140, 390, 980);
  bullets(slide, ctx, [
    "Controller 不读取 Agent、模型、工具等复杂对象，只传递请求和当前用户。",
    "ConversationAppService 是对话用例的编排入口。",
    "最终返回 SseEmitter，前端可以持续接收增量回复。"
  ], 635, 198, 450, { color: C.teal, size: 16.5, gap: 68 });
}

async function dialogEnv(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  const items = [
    ["会话", "sessionDomainService.getSession(sessionId, userId)", C.blue],
    ["Agent", "getAgentWithValidation(agentId, userId)", C.teal],
    ["工具", "getMcpServerNames(agent.toolIds, userId)", C.red],
    ["模型", "getModelForChat(llmModelConfig, modelId, userId)", C.gold],
    ["服务商", "selectBestProvider(model, userId, sessionId, fallbackChain)", C.green],
    ["上下文", "setupContextAndHistory(chatContext, request)", C.purple],
  ];
  items.forEach((item, i) => {
    const x = 92 + (i % 2) * 550;
    const y = 178 + Math.floor(i / 2) * 122;
    box(slide, ctx, x, y, 470, 78, { fill: C.white, line: item[2] });
    text(slide, ctx, item[0], x + 22, y + 15, 90, 24, { size: 20, bold: true, color: item[2] });
    text(slide, ctx, item[1], x + 126, y + 18, 315, 40, { size: 12.5, face: font.mono });
  });
  text(slide, ctx, "ChatContext 是这轮对话的“运行时上下文对象”，后续 MessageHandler 不再反复查这些配置。", 190, 580, 850, 30, {
    size: 18,
    bold: true,
    align: "center",
  });
}

async function context(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  box(slide, ctx, 100, 190, 330, 260, { fill: C.white, line: C.blue });
  box(slide, ctx, 475, 190, 330, 260, { fill: C.white, line: C.teal });
  box(slide, ctx, 850, 190, 330, 260, { fill: C.white, line: C.red });
  text(slide, ctx, "MessageEntity", 128, 224, 260, 30, { size: 22, bold: true, color: C.blue });
  bullets(slide, ctx, ["完整历史消息", "用户/助手角色", "tokenCount 与 bodyTokenCount", "文件 URL"], 130, 285, 240, { color: C.blue, size: 15, gap: 34 });
  text(slide, ctx, "ContextEntity", 503, 224, 260, 30, { size: 22, bold: true, color: C.teal });
  bullets(slide, ctx, ["activeMessages", "summary", "当前运行时视图", "避免每次扫描全部历史"], 505, 285, 240, { color: C.teal, size: 15, gap: 34 });
  text(slide, ctx, "TokenDomainService", 878, 224, 260, 30, { size: 22, bold: true, color: C.red });
  bullets(slide, ctx, ["NONE", "SLIDING_WINDOW", "SUMMARIZE", "处理后回写上下文"], 880, 285, 240, { color: C.red, size: 15, gap: 34 });
  arrow(slide, ctx, 430, 320, 475, 320, C.muted);
  arrow(slide, ctx, 805, 320, 850, 320, C.muted);
  codeBox(slide, ctx, [
    "TokenProcessResult result = tokenDomainService.processMessages(...)",
    "if (result.isProcessed()) {",
    "  contextEntity.setActiveMessages(retainedMessageIds)",
    "  contextEntity.setSummary(summary)",
    "}",
  ], 210, 500, 820, 102, C.gold);
}

async function handler(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  flow(slide, ctx, ["createConnection", "onChatStart", "create messages", "initMemory", "buildHistory", "provideTools", "stream/sync"], 66, 204, 1148);
  codeBox(slide, ctx, [
    "AbstractMessageHandler.chat()",
    "  connection = transport.createConnection(...)",
    "  userEntity = createUserMessage(context)",
    "  llmEntity = createLlmMessage(context)",
    "  memory = initMemory()",
    "  buildHistoryMessage(context, memory)",
    "  toolProvider = provideTools(context)",
    "  processStreamingChat(...) or processSyncChat(...)",
  ], 170, 355, 520, 210, C.blue);
  bullets(slide, ctx, [
    "这是典型模板方法：公共流程固定，具体工具由子类覆盖 provideTools。",
    "AgentMessageHandler 会提供工具；普通 ChatMessageHandler 不额外覆盖。",
    "系统提示词、摘要、历史消息在进入模型前写入 MessageWindowChatMemory。"
  ], 740, 370, 400, { color: C.teal, size: 16.5, gap: 58 });
}

async function stream(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  flow(slide, ctx, ["agent.chat()", "onPartialResponse", "SSE send", "onToolExecuted", "onCompleteResponse", "save context"], 86, 205, 1100, [C.blue, C.teal, C.green, C.gold, C.red]);
  codeBox(slide, ctx, [
    "tokenStream.onPartialResponse(reply -> {",
    "  messageBuilder.append(reply)",
    "  transport.sendMessage(connection, AgentChatResponse.build(reply))",
    "})",
    "",
    "tokenStream.onCompleteResponse(resp -> {",
    "  setMessageTokenCount(...)",
    "  messageDomainService.saveMessageAndUpdateContext(llmEntity, context)",
    "  transport.sendEndMessage(connection, ...)",
    "})",
  ], 122, 360, 620, 220, C.teal);
  bullets(slide, ctx, [
    "用户消息会先保存并更新 activeMessages。",
    "工具调用发生时会额外保存 TOOL_CALL 类型消息。",
    "模型调用结果会上报给高可用服务，用于后续服务商选择。"
  ], 800, 388, 335, { color: C.red, size: 16.5, gap: 58 });
}

async function toolOverview(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  flow(slide, ctx, ["Agent 工具 ID", "用户安装工具", "mcpServerName", "DirectToolRuntime", "ToolProvider", "AiServices"], 88, 208, 1090);
  codeBox(slide, ctx, [
    "AgentMessageHandler.provideTools(context)",
    "  names = agentToolManager.getAvailableTools(context)",
    "  return agentToolManager.createToolProvider(",
    "    names, presetParams, userId",
    "  )",
  ], 160, 380, 450, 150, C.red);
  bullets(slide, ctx, [
    "工具不会直接拼到 Prompt 文本里，而是注册为 LangChain4j ToolProvider。",
    "模型在推理过程中根据 ToolSpecification 决定是否调用。",
    "预设参数通过 presetToolParameters 注入，减少用户重复填写。"
  ], 680, 375, 420, { color: C.teal, size: 17, gap: 55 });
}

async function toolRuntime(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  codeBox(slide, ctx, [
    "resolve(serverName, userId)",
    "  userTool = getUserInstalledToolByServerName(serverName, userId)",
    "  runtimeConfig = findByToolIdAndVersion(toolId, version)",
    "  credential = findByUserToolId(userTool.id, userId)",
    "  return new DirectToolRuntime(serverName, userTool, config, credential)",
  ], 120, 196, 680, 156, C.blue);
  const cols = [
    ["UserToolEntity", "用户是否已安装、当前版本、服务名", C.teal],
    ["ToolRuntimeConfigEntity", "运行类型、端点、方法、schema、鉴权方式", C.red],
    ["UserToolCredentialEntity", "用户自己的 token、apiKey、username/password", C.gold],
  ];
  cols.forEach((c, i) => {
    const x = 115 + i * 370;
    box(slide, ctx, x, 416, 315, 122, { fill: C.white, line: c[2] });
    text(slide, ctx, c[0], x + 18, 444, 260, 22, { size: 17, bold: true, color: c[2], face: font.mono });
    text(slide, ctx, c[1], x + 18, 474, 260, 42, { size: 13.5 });
  });
  text(slide, ctx, "这一步保证：模型只能调用当前用户已安装、且平台保存了运行配置的工具。", 230, 584, 820, 28, {
    size: 18,
    bold: true,
    align: "center",
  });
}

async function toolInvoke(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  box(slide, ctx, 92, 190, 500, 330, { fill: C.white, line: C.blue });
  box(slide, ctx, 688, 190, 500, 330, { fill: C.white, line: C.red });
  text(slide, ctx, "HTTP_API", 130, 220, 180, 28, { size: 23, bold: true, color: C.blue, face: font.mono });
  bullets(slide, ctx, ["ToolSpecification 根据 ToolDefinition 生成", "HttpApiToolExecutor 解析 request.arguments()", "GET 参数拼 query；POST/PUT 写 JSON body", "ToolAuthSupport 写 Header 或 Query 鉴权", "响应过长截断，HTTP 4xx/5xx 返回状态和 body"], 130, 270, 380, { color: C.blue, size: 15.5, gap: 42 });
  text(slide, ctx, "REMOTE_MCP_SSE", 725, 220, 240, 28, { size: 23, bold: true, color: C.red, face: font.mono });
  bullets(slide, ctx, ["RemoteMcpToolProviderFactory 创建 DefaultMcpClient", "HttpMcpTransport 使用 sseUrl 建立远程连接", "当前 MCP SSE 只支持 query 参数鉴权", "McpToolProvider.provideTools 拉取远端工具列表", "执行异常被包装为可读失败消息返回给模型"], 725, 270, 390, { color: C.red, size: 15.5, gap: 42 });
}

async function ragIngest(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  flow(slide, ctx, ["上传文件", "RagDocMessage", "RabbitMQ", "选择策略", "保存 DocumentUnit", "自动向量化"], 82, 205, 1110, [C.green, C.blue, C.gold, C.red]);
  codeBox(slide, ctx, [
    "RagDocConsumer.receiveMessage()",
    "  startFileOcrProcessing(fileId)",
    "  fileExt = getFileExtension(fileId)",
    "  strategy = documentProcessingFactory.getDocumentStrategyHandler(fileExt)",
    "  strategy.handle(docMessage, fileExt)",
    "  completeFileOcrProcessing(fileId)",
    "  autoStartVectorization(fileId, fileEntity)",
  ], 140, 355, 575, 198, C.green);
  bullets(slide, ctx, [
    "文件上传成功不代表已经可检索，后台状态机会继续处理。",
    "RabbitMQ 解耦前台请求和耗时文档解析。",
    "失败时会更新文件处理状态，便于前端提示和后续重试。"
  ], 770, 370, 365, { color: C.red, size: 17, gap: 58 });
}

async function ragSplit(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  flow(slide, ctx, ["AST 解析", "文档树", "层次化分段", "原文入库", "特殊节点翻译", "二次切分", "发送向量任务"], 58, 200, 1160, [C.green, C.teal, C.gold, C.red]);
  codeBox(slide, ctx, [
    "MarkdownRagDocumentProcessing.insertData()",
    "  save raw segment -> DocumentUnitEntity",
    "  isOcr = true; isVector = false",
    "  vectorSegmentProcessor.processDocumentUnits(savedUnits, context)",
    "",
    "DocumentVectorizationOrchestrator.processSingleUnit()",
    "  translateSpecialNodes(originalContent)",
    "  splitter.splitIfNeeded(translatedContent, titleContext)",
    "  triggerVectorization(unit, vectorText, index, context)",
  ], 125, 344, 760, 226, C.teal);
  text(slide, ctx, "关键设计：原文 content 不被覆盖；向量化使用翻译/增强后的 vectorText，通过 MQ 消息传递。", 210, 598, 860, 28, {
    size: 18,
    bold: true,
    align: "center",
    color: C.ink,
  });
}

async function vector(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  codeBox(slide, ctx, [
    "EmbeddingDomainService.syncStorage(message)",
    "  content = message.getContent()",
    "  metadata = { fileId, fileName, documentId, datasetId }",
    "  textSegment = new TextSegment(content, metadata)",
    "  embedding = embeddingModel.embed(textSegment)",
    "  embeddingStore.add(embedding, textSegment)",
    "  update DocumentUnit.isVector = true",
  ], 110, 190, 570, 194, C.green);
  box(slide, ctx, 750, 195, 360, 260, { fill: C.white, line: C.blue });
  text(slide, ctx, "metadata 的作用", 782, 230, 250, 26, { size: 22, bold: true, color: C.blue });
  bullets(slide, ctx, ["按 datasetId 过滤知识库范围", "按 fileId 追踪文件来源", "按 documentId 反查原文片段", "搜索结果可展示引用来源"], 780, 282, 260, { color: C.blue, size: 15, gap: 38 });
  text(slide, ctx, "向量库只负责相似度检索；真正展示给用户的原文片段仍回到 DocumentUnitEntity 查询。", 195, 520, 850, 32, {
    size: 19,
    bold: true,
    align: "center",
  });
}

async function ragSearch(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  flow(slide, ctx, ["RagSearchRequest", "权限过滤", "嵌入模型配置", "聊天模型配置", "HybridSearchConfig", "DTO 返回"], 86, 200, 1100, [C.green, C.blue, C.teal, C.gold, C.red]);
  codeBox(slide, ctx, [
    "RAGSearchAppService.ragSearch(request, userId)",
    "  for datasetId in request.datasetIds:",
    "    isRagInstalledByOriginalId(userId, datasetId)",
    "    or checkDatasetExists(datasetId, userId)",
    "  embeddingConfig = userModelConfigResolver.getUserEmbeddingModelConfig(userId)",
    "  chatModelConfig = getUserChatModelConfig(userId)",
    "  config = HybridSearchConfig.builder(validDatasetIds, question)...build()",
    "  entities = hybridSearchDomainService.hybridSearch(config)",
  ], 140, 350, 810, 196, C.green);
  bullets(slide, ctx, ["权限先于检索", "模型配置按用户隔离", "HyDE 可用时使用聊天模型生成假设文档"], 990, 378, 180, { color: C.red, size: 15, gap: 58 });
}

async function hybrid(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  box(slide, ctx, 82, 190, 520, 300, { fill: C.white, line: C.teal });
  box(slide, ctx, 680, 190, 500, 300, { fill: C.white, line: C.red });
  text(slide, ctx, "并行召回", 120, 225, 180, 28, { size: 24, bold: true, color: C.teal });
  bullets(slide, ctx, ["HyDE 先把问题改写为假设文档", "CompletableFuture 并行执行向量检索和关键词检索", "向量检索失败不影响关键词结果", "关键词检索失败不影响向量结果"], 124, 285, 390, { color: C.teal, size: 16, gap: 44 });
  text(slide, ctx, "RRF 融合与重排序", 720, 225, 260, 28, { size: 24, bold: true, color: C.red });
  codeBox(slide, ctx, [
    "RRF(d) = sum(1 / (k + rank_i(d)))",
    "k = 60",
    "vector rank + keyword rank -> fused rank",
    "enableRerank -> rerank(texts, question)",
  ], 720, 285, 390, 122, C.red);
  text(slide, ctx, "RRF 的价值：不同检索方式的分数不可直接相加，但排名可以融合。", 245, 565, 760, 30, {
    size: 19,
    bold: true,
    align: "center",
  });
}

async function ragTool(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  flow(slide, ctx, ["Agent 绑定知识库", "BuiltInToolRegistry", "defineTools", "knowledge_search", "ragSearch", "格式化片段", "模型回答"], 60, 205, 1160, [C.green, C.blue, C.teal, C.gold, C.red]);
  codeBox(slide, ctx, [
    "RagBuiltInToolProvider.defineTools(agent)",
    "  knowledgeBaseIds = agent.getKnowledgeBaseIds()",
    "  validateKnowledgeBases(ids, agent.userId)",
    "  ToolDefinition name = \"knowledge_search\"",
    "",
    "doExecute()",
    "  query/maxResults/minScore/enableRerank",
    "  ragSearchAppService.ragSearch(searchRequest, agent.userId)",
    "  formatSearchResults(...)",
  ], 135, 350, 660, 210, C.green);
  bullets(slide, ctx, [
    "RAG 在普通 Agent 对话中不是单独页面逻辑，而是内置工具。",
    "模型决定何时调用 knowledge_search，并把片段作为回答依据。",
    "返回内容会带片段序号和来源信息。"
  ], 855, 370, 300, { color: C.red, size: 16, gap: 58 });
}

async function close(slide, ctx, s) {
  ctx.addShape(slide, { x: 0, y: 0, width: W, height: H, fill: C.ink });
  text(slide, ctx, s.title, 96, 105, 910, 52, { size: 34, bold: true, color: C.white, face: font.title });
  text(slide, ctx, s.subtitle, 100, 170, 760, 28, { size: 18, color: "#D9E2EA" });
  const items = [
    ["登录身份闭环", "登录签发 JWT，拦截器验签并恢复 UserContext", C.blue],
    ["对话状态闭环", "ChatContext 装配，SSE 流式输出，消息和上下文落库", C.teal],
    ["RAG 知识闭环", "异步解析、二次切分、向量化、混合检索、内置工具", C.green],
    ["工具调用闭环", "安装工具、解析运行配置、HTTP/MCP 调用、异常可反馈", C.red],
  ];
  items.forEach((item, i) => {
    const y = 260 + i * 78;
    ctx.addShape(slide, { x: 108, y, width: 10, height: 50, fill: item[2] });
    text(slide, ctx, item[0], 136, y + 2, 190, 24, { size: 21, bold: true, color: item[2] });
    text(slide, ctx, item[1], 360, y + 4, 620, 24, { size: 18, color: C.white });
  });
  text(slide, ctx, "请各位老师批评指正", 98, 620, 420, 34, { size: 26, bold: true, color: C.gold });
}

export async function renderSlide(presentation, ctx, idx) {
  const s = slides[idx - 1];
  if (!s) throw new Error(`Missing slide ${idx}`);
  const slide = presentation.slides.add();
  if (!["cover", "close"].includes(s.kind)) {
    bg(slide, ctx);
    slideNo(slide, ctx, idx);
  }
  if (s.kind === "cover") await cover(slide, ctx, s);
  else if (s.kind === "agenda") await agenda(slide, ctx, s);
  else if (s.kind === "layers") await layers(slide, ctx, s);
  else if (s.kind === "loginFlow") await loginFlow(slide, ctx, s);
  else if (s.kind === "jwtDetail") await jwtDetail(slide, ctx, s);
  else if (s.kind === "authInterceptor") await authInterceptor(slide, ctx, s);
  else if (s.kind === "dialogEntry") await dialogEntry(slide, ctx, s);
  else if (s.kind === "dialogEnv") await dialogEnv(slide, ctx, s);
  else if (s.kind === "context") await context(slide, ctx, s);
  else if (s.kind === "handler") await handler(slide, ctx, s);
  else if (s.kind === "stream") await stream(slide, ctx, s);
  else if (s.kind === "toolOverview") await toolOverview(slide, ctx, s);
  else if (s.kind === "toolRuntime") await toolRuntime(slide, ctx, s);
  else if (s.kind === "toolInvoke") await toolInvoke(slide, ctx, s);
  else if (s.kind === "ragIngest") await ragIngest(slide, ctx, s);
  else if (s.kind === "ragSplit") await ragSplit(slide, ctx, s);
  else if (s.kind === "vector") await vector(slide, ctx, s);
  else if (s.kind === "ragSearch") await ragSearch(slide, ctx, s);
  else if (s.kind === "hybrid") await hybrid(slide, ctx, s);
  else if (s.kind === "ragTool") await ragTool(slide, ctx, s);
  else if (s.kind === "close") await close(slide, ctx, s);
  else throw new Error(`Unknown slide kind: ${s.kind}`);
  return slide;
}
