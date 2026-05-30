const W = 1280;
const H = 720;

const C = {
  ink: "#172026",
  muted: "#5F6972",
  paper: "#F7F5EF",
  white: "#FFFFFF",
  line: "#D8D3C7",
  blue: "#2456A6",
  teal: "#2B8A83",
  red: "#B2473E",
  gold: "#C9922A",
  green: "#4E8D54",
  slate: "#303B45",
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

function t(slide, ctx, text, x, y, width, height, opts = {}) {
  return ctx.addText(slide, {
    text,
    x,
    y,
    width,
    height,
    fontSize: opts.size || 24,
    color: opts.color || C.ink,
    bold: opts.bold || false,
    typeface: opts.face || font.body,
    align: opts.align || "left",
    valign: opts.valign || "top",
    fill: opts.fill || { color: "transparent" },
    line: { style: "solid", fill: "transparent", width: 0 },
    insets: opts.insets || { left: 0, right: 0, top: 0, bottom: 0 },
  });
}

function title(slide, ctx, main, sub, source) {
  t(slide, ctx, main, 70, 54, 780, 48, { size: 30, bold: true, face: font.title });
  if (sub) t(slide, ctx, sub, 72, 103, 880, 26, { size: 15, color: C.muted });
  ctx.addShape(slide, { x: 70, y: 142, width: 1140, height: 1.2, fill: C.line });
  if (source) t(slide, ctx, source, 72, 674, 930, 22, { size: 12, color: C.muted });
}

function chip(slide, ctx, label, x, y, width, color = C.blue) {
  ctx.addShape(slide, { x, y, width, height: 28, fill: color });
  t(slide, ctx, label, x + 12, y + 5, width - 24, 18, { size: 12, color: C.white, bold: true });
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

function bulletList(slide, ctx, items, x, y, width, opts = {}) {
  const gap = opts.gap || 48;
  const dot = opts.dot || C.blue;
  items.forEach((item, i) => {
    const yy = y + i * gap;
    ctx.addShape(slide, { x, y: yy + 7, width: 8, height: 8, fill: dot });
    t(slide, ctx, item, x + 20, yy, width - 20, gap - 4, { size: opts.size || 19, color: opts.color || C.ink });
  });
}

function arrow(slide, ctx, x1, y1, x2, y2, color = C.muted) {
  if (Math.abs(y2 - y1) < 4) {
    const x = Math.min(x1, x2);
    ctx.addShape(slide, { x, y: y1 - 1, width: Math.abs(x2 - x1), height: 2, fill: color });
    ctx.addShape(slide, { x: x2 - 8, y: y1 - 5, width: 10, height: 10, fill: color, geometry: "triangle" });
  } else {
    const y = Math.min(y1, y2);
    ctx.addShape(slide, { x: x1 - 1, y, width: 2, height: Math.abs(y2 - y1), fill: color });
  }
}

function stat(slide, ctx, value, label, x, y, color) {
  t(slide, ctx, value, x, y, 170, 42, { size: 30, bold: true, color });
  t(slide, ctx, label, x, y + 44, 180, 40, { size: 14, color: C.muted });
}

function sectionNumber(slide, ctx, n) {
  t(slide, ctx, String(n).padStart(2, "0"), 1118, 56, 80, 40, { size: 30, bold: true, color: C.line, align: "right" });
}

const slides = [
  {
    title: "基于大模型的智能体生成工具的设计与实现",
    subtitle: "本科毕业设计答辩 | 软件工程 | 徐昊 20222241361",
    kind: "cover",
  },
  {
    title: "汇报主线",
    subtitle: "按论文结构展开：背景、需求、设计、实现、测试、结论",
    source: "依据论文目录与章节安排",
    kind: "agenda",
    items: ["研究背景与目标", "需求分析与技术基础", "总体设计与数据库设计", "核心模块实现", "系统测试与总结"],
  },
  {
    title: "研究背景：从模型调用走向智能体平台",
    subtitle: "问题不只是“接入一个模型接口”，而是组织稳定的工程闭环",
    source: "依据论文 1.1、1.2",
    kind: "problem",
  },
  {
    title: "研究目标与主要工作",
    subtitle: "围绕智能体生成工具完成需求、设计、实现与测试",
    source: "依据论文摘要、1.3",
    kind: "goals",
  },
  {
    title: "需求分析：面向用户主链路与平台治理",
    subtitle: "功能需求覆盖创建、运行、增强、管理；非功能需求覆盖安全与可扩展",
    source: "依据论文第 3 章",
    kind: "requirements",
  },
  {
    title: "相关技术：围绕智能体运行闭环选型",
    subtitle: "后端工程能力 + 大模型编排 + 向量检索 + 异步处理 + 前端交互",
    source: "依据论文第 2 章",
    kind: "stack",
  },
  {
    title: "总体架构：DDD 分层组织复杂业务",
    subtitle: "接口层接入请求，应用层编排用例，领域层承载规则，基础设施层连接外部能力",
    source: "依据论文 4.2",
    kind: "architecture",
  },
  {
    title: "功能模块设计：智能体生命周期闭环",
    subtitle: "从登录、配置、创建到对话、工具、知识库和后台治理",
    source: "依据论文 4.3",
    kind: "modules",
  },
  {
    title: "数据库设计：业务数据与向量检索共同支撑",
    subtitle: "消息、上下文、工具、知识库和向量数据分别承担不同职责",
    source: "依据论文 4.4",
    kind: "data",
  },
  {
    title: "对话主流程：环境装配、流式响应与状态回写",
    subtitle: "用户请求不是直接发给模型，而是先完成会话、模型、上下文和增强能力装配",
    source: "依据论文 5.2",
    kind: "conversation",
  },
  {
    title: "大模型调用：服务商、模型与用户凭据解耦",
    subtitle: "运行时解析模型配置，避免平台绑定单一模型服务",
    source: "依据论文 5.3",
    kind: "model",
  },
  {
    title: "MCP 工具接入：远程端点探测与直连调用",
    subtitle: "工具能力通过远程 MCP SSE / HTTP API 接入，并纳入发布、审核、安装和调用流程",
    source: "依据论文 5.4",
    kind: "mcp",
  },
  {
    title: "RAG 知识库：异步处理与混合检索",
    subtitle: "上传、解析、切分、向量化、检索和引用返回构成知识增强闭环",
    source: "依据论文 5.5、5.7",
    kind: "rag",
  },
  {
    title: "会话与上下文治理：控制多轮对话规模",
    subtitle: "完整历史与运行时视图分离，必要时进行保留或摘要压缩",
    source: "依据论文 5.6",
    kind: "context",
  },
  {
    title: "后台治理与运行支撑",
    subtitle: "管理后台、异步任务和状态机保证平台能力可管理、可追踪",
    source: "依据论文 5.7",
    kind: "governance",
  },
  {
    title: "系统测试：黑盒场景 + 白盒路径",
    subtitle: "验证核心功能闭环和关键后端分支处理是否符合预期",
    source: "依据论文第 6 章",
    kind: "testing",
  },
  {
    title: "结论与后续工作",
    subtitle: "系统已形成较完整智能体平台闭环，后续继续完善测试、评估和监控",
    source: "依据论文结论",
    kind: "conclusion",
  },
  {
    title: "请各位老师批评指正",
    subtitle: "Q&A",
    kind: "thanks",
  },
];

async function renderCover(slide, ctx, s) {
  ctx.addShape(slide, { x: 0, y: 0, width: W, height: H, fill: C.ink });
  ctx.addShape(slide, { x: 0, y: 0, width: W * 0.58, height: H, fill: "#203244" });
  ctx.addShape(slide, { x: 732, y: 0, width: 548, height: H, fill: C.paper });
  t(slide, ctx, s.title, 78, 118, 610, 122, { size: 38, bold: true, color: C.white, face: font.title });
  t(slide, ctx, "Design and Implementation of An Agent Generation Tool Based on Large Language Models", 82, 258, 560, 48, { size: 17, color: "#D9E2EA" });
  chip(slide, ctx, "本科毕业设计答辩", 82, 342, 164, C.gold);
  t(slide, ctx, "软件学院 · 软件工程\n学生：徐昊   学号：20222241361\n指导教师：梁文新", 82, 438, 470, 96, { size: 20, color: C.white });
  const rail = [
    ["智能体配置", C.blue],
    ["MCP 工具接入", C.teal],
    ["RAG 知识增强", C.green],
    ["上下文治理", C.red],
  ];
  rail.forEach((r, i) => {
    box(slide, ctx, 790, 142 + i * 102, 344, 70, { fill: C.white, line: C.line });
    ctx.addShape(slide, { x: 790, y: 142 + i * 102, width: 9, height: 70, fill: r[1] });
    t(slide, ctx, r[0], 820, 162 + i * 102, 260, 28, { size: 22, bold: true });
  });
}

async function renderAgenda(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  const colors = [C.blue, C.teal, C.gold, C.red, C.green];
  s.items.forEach((item, i) => {
    const y = 190 + i * 78;
    t(slide, ctx, `0${i + 1}`, 98, y - 4, 58, 38, { size: 27, bold: true, color: colors[i] });
    ctx.addShape(slide, { x: 170, y: y + 15, width: 845, height: 2, fill: colors[i] });
    t(slide, ctx, item, 1008, y - 2, 184, 44, { size: 17, bold: true, align: "right" });
  });
}

async function renderProblem(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  const left = ["单轮问答难以覆盖持续任务", "模型参数知识存在时效与可靠性边界", "缺少工具、知识库、权限与会话协同", "多用户场景需要后台治理与状态追踪"];
  const right = ["自然语言作为统一交互入口", "外部工具弥补计算与业务动作不足", "私有知识库提升资料型回答依据", "上下文治理保持多轮对话连续性"];
  chip(slide, ctx, "论文指出的现实问题", 92, 178, 180, C.red);
  chip(slide, ctx, "智能体平台的设计方向", 702, 178, 198, C.teal);
  bulletList(slide, ctx, left, 98, 244, 475, { dot: C.red, size: 20, gap: 58 });
  bulletList(slide, ctx, right, 708, 244, 450, { dot: C.teal, size: 20, gap: 58 });
  t(slide, ctx, "研究重点：把大模型能力稳定组织进业务平台，而不是只完成一次模型接口调用。", 230, 582, 820, 58, { size: 21, bold: true, align: "center", color: C.blue });
}

async function renderGoals(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  const cols = [
    ["需求分析", "明确普通用户与管理员需求，覆盖功能与非功能约束", C.blue],
    ["总体设计", "构建分层架构、功能模块、数据库与核心流程", C.teal],
    ["核心实现", "实现认证、对话、模型、MCP、RAG、上下文等模块", C.red],
    ["系统测试", "通过黑盒与白盒测试验证主要功能和关键分支", C.gold],
  ];
  cols.forEach((c, i) => {
    const x = 86 + i * 288;
    box(slide, ctx, x, 205, 248, 260, { fill: C.white, line: c[2] });
    t(slide, ctx, c[0], x + 24, 235, 190, 34, { size: 24, bold: true, color: c[2] });
    t(slide, ctx, c[1], x + 24, 295, 196, 94, { size: 18, color: C.ink });
    t(slide, ctx, `0${i + 1}`, x + 164, 394, 54, 40, { size: 28, bold: true, color: C.line, align: "right" });
  });
  t(slide, ctx, "目标产物：一个支持智能体创建、运行、工具增强、知识增强和后台治理的完整工程系统。", 118, 552, 1010, 40, { size: 22, bold: true, align: "center" });
}

async function renderRequirements(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  const groups = [
    ["普通用户主链路", ["登录注册与账户维护", "模型服务商与 API Key 配置", "Agent 创建、版本维护与工作区使用", "持续会话、历史消息和流式回复"], C.blue],
    ["增强能力需求", ["远程 MCP / HTTP 工具接入", "知识库上传、处理、检索和问答", "会话上下文保留、裁剪与摘要", "异常提示与权限边界"], C.teal],
    ["平台治理需求", ["用户、Agent、工具、RAG 管理", "发布审核与认证策略配置", "可靠性、安全性、可扩展性", "性能与可用性保障"], C.gold],
  ];
  groups.forEach((g, i) => {
    const x = 75 + i * 385;
    box(slide, ctx, x, 184, 335, 395, { fill: C.white, line: g[2] });
    ctx.addShape(slide, { x, y: 184, width: 335, height: 10, fill: g[2] });
    t(slide, ctx, g[0], x + 22, 220, 260, 34, { size: 22, bold: true, color: g[2] });
    bulletList(slide, ctx, g[1], x + 28, 288, 280, { dot: g[2], size: 17, gap: 56 });
  });
}

async function renderStack(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  const rows = [
    ["后端框架", "Java 17 / Spring Boot 3 / DDD 分层", C.blue],
    ["数据与检索", "MyBatis-Plus / PostgreSQL / pgvector", C.teal],
    ["智能体能力", "LangChain4j / 流式响应 / MCP / RAG", C.red],
    ["运行支撑", "RabbitMQ / 对象存储 / JWT / 邮件验证码", C.gold],
    ["前端交互", "Next.js / React / TypeScript / Tailwind CSS", C.green],
  ];
  rows.forEach((r, i) => {
    const y = 176 + i * 78;
    ctx.addShape(slide, { x: 110, y, width: 9, height: 52, fill: r[2] });
    t(slide, ctx, r[0], 138, y + 4, 150, 28, { size: 20, bold: true, color: r[2] });
    t(slide, ctx, r[1], 330, y + 3, 700, 30, { size: 21 });
    ctx.addShape(slide, { x: 110, y: y + 64, width: 960, height: 1, fill: C.line });
  });
  t(slide, ctx, "选型逻辑：每项技术都对应智能体平台的运行环节，而不是简单堆叠。", 190, 614, 840, 30, { size: 19, color: C.muted, align: "center" });
}

async function renderArchitecture(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  const layers = [
    ["接口层", "Portal / Admin / External API", C.blue],
    ["应用服务层", "业务用例编排：认证、会话、Agent、RAG、工具", C.teal],
    ["领域层", "实体、领域服务、状态与核心规则", C.red],
    ["基础设施层", "数据库、MQ、对象存储、模型、邮件、远程工具", C.gold],
  ];
  layers.forEach((l, i) => {
    const y = 178 + i * 94;
    box(slide, ctx, 128, y, 880, 62, { fill: C.white, line: l[2] });
    ctx.addShape(slide, { x: 128, y, width: 12, height: 62, fill: l[2] });
    t(slide, ctx, l[0], 162, y + 17, 130, 26, { size: 21, bold: true, color: l[2] });
    t(slide, ctx, l[1], 330, y + 17, 610, 26, { size: 19 });
    if (i < layers.length - 1) arrow(slide, ctx, 568, y + 62, 568, y + 92, C.muted);
  });
  t(slide, ctx, "调用方向", 1038, 258, 80, 24, { size: 15, color: C.muted });
  arrow(slide, ctx, 1080, 292, 1080, 486, C.muted);
}

async function renderModules(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  const modules = [
    ["用户认证", C.blue], ["Agent 管理", C.teal], ["工作区会话", C.red], ["模型服务商", C.gold],
    ["工具市场", C.green], ["RAG 知识库", C.blue], ["API Key", C.teal], ["管理后台", C.red],
  ];
  modules.forEach((m, i) => {
    const x = 100 + (i % 4) * 270;
    const y = 190 + Math.floor(i / 4) * 150;
    box(slide, ctx, x, y, 222, 86, { fill: C.white, line: m[1] });
    ctx.addShape(slide, { x, y, width: 222, height: 8, fill: m[1] });
    t(slide, ctx, m[0], x + 22, y + 30, 160, 30, { size: 22, bold: true, color: m[1] });
  });
  t(slide, ctx, "用户链路：登录 -> 配置模型 -> 创建 Agent -> 绑定工具/知识库 -> 工作区持续对话", 150, 528, 930, 34, { size: 22, bold: true, align: "center", color: C.ink });
  t(slide, ctx, "管理员链路：用户、Agent、工具、RAG、服务商、认证配置治理", 260, 584, 710, 30, { size: 18, align: "center", color: C.muted });
}

async function renderData(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  const nodes = [
    ["User", 90, 205, C.blue], ["Agent / Version", 315, 205, C.teal], ["Session / Message", 580, 205, C.red], ["Context", 860, 205, C.gold],
    ["Provider / Model", 190, 420, C.blue], ["Tool / Version", 445, 420, C.teal], ["RAG / File / Unit", 700, 420, C.green], ["Vector Store", 975, 420, C.red],
  ];
  nodes.forEach((n) => {
    box(slide, ctx, n[1], n[2], 178, 72, { fill: C.white, line: n[3] });
    t(slide, ctx, n[0], n[1] + 12, n[2] + 15, 154, 44, { size: 16, bold: true, color: n[3], align: "center" });
  });
  arrow(slide, ctx, 268, 241, 315, 241, C.muted);
  arrow(slide, ctx, 493, 241, 580, 241, C.muted);
  arrow(slide, ctx, 758, 241, 860, 241, C.muted);
  arrow(slide, ctx, 878, 456, 975, 456, C.muted);
  t(slide, ctx, "设计重点：完整历史、运行时上下文、工具配置、知识库片段与向量数据分表承担职责。", 188, 596, 850, 32, { size: 19, bold: true, align: "center" });
}

async function renderFlow(slide, ctx, s, labels, colors, note) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  labels.forEach((label, i) => {
    const x = 86 + i * 175;
    box(slide, ctx, x, 278, 138, 78, { fill: C.white, line: colors[i % colors.length] });
    t(slide, ctx, label, x + 10, 299, 118, 34, { size: 16, bold: true, color: colors[i % colors.length], align: "center" });
    if (i < labels.length - 1) arrow(slide, ctx, x + 138, 317, x + 174, 317, C.muted);
  });
  t(slide, ctx, note, 146, 495, 970, 62, { size: 21, bold: true, align: "center" });
}

async function renderConversation(slide, ctx, s) {
  await renderFlow(slide, ctx, s, ["用户消息", "会话校验", "模型配置", "上下文恢复", "增强装配", "流式生成", "状态回写"], [C.blue, C.teal, C.red], "对话主链路同时处理模型调用、消息持久化、流式传输和上下文维护。");
}

async function renderModel(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  const cols = [["服务商配置", "平台维护服务商、端点和模型信息", C.blue], ["用户凭据", "用户维护自己的 API Key 和偏好配置", C.teal], ["运行解析", "会话运行时组合模型参数并发起调用", C.red]];
  cols.forEach((c, i) => {
    const x = 126 + i * 345;
    box(slide, ctx, x, 222, 275, 210, { fill: C.white, line: c[2] });
    t(slide, ctx, c[0], x + 26, 250, 210, 32, { size: 23, bold: true, color: c[2] });
    t(slide, ctx, c[1], x + 26, 315, 216, 62, { size: 18 });
    if (i < 2) arrow(slide, ctx, x + 275, 326, x + 344, 326, C.muted);
  });
  t(slide, ctx, "设计收益：降低对单一模型服务的绑定，便于后续扩展服务商和模型参数。", 226, 544, 820, 34, { size: 20, bold: true, align: "center" });
}

async function renderMcp(slide, ctx, s) {
  await renderFlow(slide, ctx, s, ["提交配置", "端点探测", "能力解析", "审核发布", "安装绑定", "对话调用"], [C.teal, C.gold, C.red], "工具调用失败时返回清晰错误提示，并保持整轮会话连续性。");
}

async function renderRag(slide, ctx, s) {
  await renderFlow(slide, ctx, s, ["文件上传", "异步解析", "文档切分", "向量写入", "混合检索", "引用回答"], [C.green, C.blue, C.gold], "RAG 链路通过 RabbitMQ 异步处理文件，并用 pgvector 支撑知识片段检索。");
}

async function renderContext(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  box(slide, ctx, 108, 210, 424, 260, { fill: C.white, line: C.blue });
  box(slide, ctx, 748, 210, 424, 260, { fill: C.white, line: C.red });
  t(slide, ctx, "消息表", 150, 248, 160, 34, { size: 25, bold: true, color: C.blue });
  t(slide, ctx, "保存完整历史记录，支持刷新后恢复与审计追踪。", 150, 314, 320, 70, { size: 19 });
  t(slide, ctx, "上下文表", 790, 248, 180, 34, { size: 25, bold: true, color: C.red });
  t(slide, ctx, "维护当前参与模型输入的有效消息或摘要视图。", 790, 314, 330, 70, { size: 19 });
  arrow(slide, ctx, 532, 340, 748, 340, C.muted);
  t(slide, ctx, "Token 过长时：保留近期消息或将早期内容压缩为摘要。", 288, 552, 700, 32, { size: 21, bold: true, align: "center" });
}

async function renderGovernance(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  const groups = [
    ["管理后台", ["用户", "Agent", "工具", "RAG", "服务商", "认证配置"], C.blue],
    ["运行支撑", ["RabbitMQ", "文件状态机", "对象存储", "异常提示", "异步向量化"], C.teal],
  ];
  groups.forEach((g, i) => {
    const x = 118 + i * 560;
    box(slide, ctx, x, 205, 485, 285, { fill: C.white, line: g[2] });
    t(slide, ctx, g[0], x + 28, 238, 220, 34, { size: 25, bold: true, color: g[2] });
    g[1].forEach((item, idx) => {
      chip(slide, ctx, item, x + 30 + (idx % 3) * 140, 310 + Math.floor(idx / 3) * 60, 110, g[2]);
    });
  });
  t(slide, ctx, "重点不是消除外部服务不稳定性，而是做到状态可追踪、失败可反馈、能力可治理。", 196, 578, 880, 32, { size: 20, bold: true, align: "center" });
}

async function renderTesting(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  stat(slide, ctx, "18", "黑盒测试用例：认证、对话、RAG、MCP", 120, 202, C.blue);
  stat(slide, ctx, "5", "白盒路径：登录与对话主流程分支", 380, 202, C.teal);
  stat(slide, ctx, "通过", "主要功能结果与预期一致", 640, 202, C.green);
  box(slide, ctx, 118, 350, 1000, 160, { fill: C.white, line: C.line });
  const rows = [
    ["认证", "注册、重复注册、验证码、登录、重置密码"],
    ["智能对话", "创建 Agent、加入工作区、流式回复、历史恢复、中断生成"],
    ["RAG / MCP", "知识库问答、无权限访问、工具探测、工具增强对话、异常提示"],
  ];
  rows.forEach((r, i) => {
    const y = 378 + i * 42;
    t(slide, ctx, r[0], 150, y, 120, 24, { size: 18, bold: true, color: [C.blue, C.teal, C.red][i] });
    t(slide, ctx, r[1], 310, y, 720, 24, { size: 17 });
  });
}

async function renderConclusion(slide, ctx, s) {
  title(slide, ctx, s.title, s.subtitle, s.source);
  const left = ["完成从需求分析到系统测试的完整工程闭环", "融合用户体系、Agent、MCP、RAG、上下文和后台治理", "体现传统 Web 后端能力与大模型智能体能力结合"];
  const right = ["补充自动化测试覆盖", "量化评估 RAG 检索效果", "完善运行监控、日志审计和生产级安全配置"];
  chip(slide, ctx, "主要结论", 108, 190, 128, C.blue);
  chip(slide, ctx, "后续工作", 718, 190, 128, C.red);
  bulletList(slide, ctx, left, 112, 262, 470, { dot: C.blue, size: 20, gap: 66 });
  bulletList(slide, ctx, right, 722, 262, 420, { dot: C.red, size: 20, gap: 66 });
}

async function renderThanks(slide, ctx, s) {
  ctx.addShape(slide, { x: 0, y: 0, width: W, height: H, fill: C.ink });
  t(slide, ctx, s.title, 120, 210, 760, 62, { size: 42, bold: true, color: C.white, face: font.title });
  t(slide, ctx, s.subtitle, 124, 304, 280, 44, { size: 30, color: C.gold, bold: true });
  t(slide, ctx, "基于大模型的智能体生成工具的设计与实现", 124, 520, 650, 30, { size: 18, color: "#CED6DC" });
  t(slide, ctx, "徐昊 · 软件工程", 124, 558, 280, 26, { size: 16, color: "#CED6DC" });
}

export async function renderSlide(presentation, ctx, idx) {
  const s = slides[idx - 1];
  const slide = presentation.slides.add();
  if (!s) throw new Error(`Missing slide data for ${idx}`);
  if (!["cover", "thanks"].includes(s.kind)) {
    bg(slide, ctx);
    sectionNumber(slide, ctx, idx);
  }
  if (s.kind === "cover") await renderCover(slide, ctx, s);
  else if (s.kind === "agenda") await renderAgenda(slide, ctx, s);
  else if (s.kind === "problem") await renderProblem(slide, ctx, s);
  else if (s.kind === "goals") await renderGoals(slide, ctx, s);
  else if (s.kind === "requirements") await renderRequirements(slide, ctx, s);
  else if (s.kind === "stack") await renderStack(slide, ctx, s);
  else if (s.kind === "architecture") await renderArchitecture(slide, ctx, s);
  else if (s.kind === "modules") await renderModules(slide, ctx, s);
  else if (s.kind === "data") await renderData(slide, ctx, s);
  else if (s.kind === "conversation") await renderConversation(slide, ctx, s);
  else if (s.kind === "model") await renderModel(slide, ctx, s);
  else if (s.kind === "mcp") await renderMcp(slide, ctx, s);
  else if (s.kind === "rag") await renderRag(slide, ctx, s);
  else if (s.kind === "context") await renderContext(slide, ctx, s);
  else if (s.kind === "governance") await renderGovernance(slide, ctx, s);
  else if (s.kind === "testing") await renderTesting(slide, ctx, s);
  else if (s.kind === "conclusion") await renderConclusion(slide, ctx, s);
  else if (s.kind === "thanks") await renderThanks(slide, ctx, s);
  else throw new Error(`Unknown slide kind: ${s.kind}`);
  return slide;
}
