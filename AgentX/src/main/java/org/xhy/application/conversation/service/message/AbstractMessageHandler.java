package org.xhy.application.conversation.service.message;

import cn.hutool.core.collection.CollectionUtil;
import org.apache.commons.lang3.StringUtils;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.*;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.xhy.application.conversation.dto.AgentChatResponse;
import org.xhy.application.conversation.service.handler.context.AgentPromptTemplates;
import org.xhy.application.conversation.service.handler.context.ChatContext;
import org.xhy.application.conversation.service.message.Agent;
import org.xhy.application.conversation.service.message.builtin.BuiltInToolRegistry;
import org.xhy.application.conversation.service.ChatSessionManager;
import org.xhy.domain.agent.model.AgentEntity;
import org.xhy.domain.conversation.constant.MessageType;
import org.xhy.domain.conversation.constant.Role;
import org.xhy.domain.conversation.model.ContextEntity;
import org.xhy.domain.conversation.model.MessageEntity;
import org.xhy.domain.conversation.service.MessageDomainService;
import org.xhy.domain.conversation.service.SessionDomainService;
import org.xhy.domain.llm.model.HighAvailabilityResult;
import org.xhy.domain.llm.model.ModelEntity;
import org.xhy.domain.llm.model.ProviderEntity;
import org.xhy.domain.llm.service.HighAvailabilityDomainService;
import org.xhy.domain.llm.service.LLMDomainService;
import org.xhy.domain.user.service.UserSettingsDomainService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.lang.Nullable;
import org.xhy.infrastructure.llm.LLMServiceFactory;
import org.xhy.infrastructure.transport.MessageTransport;
import org.xhy.infrastructure.transport.SseEmitterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public abstract class AbstractMessageHandler {

    /** 日志记录器 */
    private static final Logger logger = LoggerFactory.getLogger(AbstractMessageHandler.class);

    private static final String EMPTY_RESPONSE_FALLBACK = "本次没有生成有效回复。可能是工具返回结果异常或模型连续调用工具后没有整理出最终答案，请稍后重试或换一个更明确的问题。";

    /** 连接超时时间（毫秒） */
    protected static final long CONNECTION_TIMEOUT = 3000000L;

    protected final LLMServiceFactory llmServiceFactory;
    protected final MessageDomainService messageDomainService;
    protected final HighAvailabilityDomainService highAvailabilityDomainService;
    protected final SessionDomainService sessionDomainService;
    protected final UserSettingsDomainService userSettingsDomainService;
    protected final LLMDomainService llmDomainService;
    protected final BuiltInToolRegistry builtInToolRegistry;
    protected final ChatSessionManager chatSessionManager;

    public AbstractMessageHandler(LLMServiceFactory llmServiceFactory, MessageDomainService messageDomainService,
            HighAvailabilityDomainService highAvailabilityDomainService, SessionDomainService sessionDomainService,
            UserSettingsDomainService userSettingsDomainService, LLMDomainService llmDomainService,
            BuiltInToolRegistry builtInToolRegistry, ChatSessionManager chatSessionManager) {
        this.llmServiceFactory = llmServiceFactory;
        this.messageDomainService = messageDomainService;
        this.highAvailabilityDomainService = highAvailabilityDomainService;
        this.sessionDomainService = sessionDomainService;
        this.userSettingsDomainService = userSettingsDomainService;
        this.llmDomainService = llmDomainService;
        this.builtInToolRegistry = builtInToolRegistry;
        this.chatSessionManager = chatSessionManager;
    }

    /** 处理对话的模板方法
     *
     * @param chatContext 对话环境
     * @param transport 消息传输实现
     * @return 连接对象
     * @param <T> 连接类型 */
    public <T> T chat(ChatContext chatContext, MessageTransport<T> transport) {
        // 1. 创建连接
        T connection = transport.createConnection(CONNECTION_TIMEOUT);

        // 2. 调用对话开始钩子
        onChatStart(chatContext);

        // 3. 创建消息实体
        MessageEntity llmMessageEntity = createLlmMessage(chatContext);
        MessageEntity userMessageEntity = createUserMessage(chatContext);

        // 4. 调用用户消息处理完成钩子
        onUserMessageProcessed(chatContext, userMessageEntity);

        // 5. 初始化聊天内存
        MessageWindowChatMemory memory = initMemory();

        // 6. 构建历史消息
        buildHistoryMessage(chatContext, memory);

        // 7. 根据子类决定是否需要工具
        ToolProvider toolProvider = provideTools(chatContext);

        // 8. 根据是否流式选择不同的处理方式
        if (chatContext.isStreaming()) {
            processStreamingChat(chatContext, connection, transport, userMessageEntity, llmMessageEntity, memory,
                    toolProvider);
        } else {
            processSyncChat(chatContext, connection, transport, userMessageEntity, llmMessageEntity, memory,
                    toolProvider);
        }

        return connection;
    }

    /** 追踪钩子方法 - 对话开始时调用 子类可以覆盖此方法实现追踪逻辑
     * 
     * @param chatContext 对话上下文 */
    protected void onChatStart(ChatContext chatContext) {
        // 默认空实现，子类可选择性覆盖
    }

    /** 追踪钩子方法 - 用户消息处理完成时调用
     * 
     * @param chatContext 对话上下文
     * @param userMessage 用户消息实体 */
    protected void onUserMessageProcessed(ChatContext chatContext, MessageEntity userMessage) {
        // 默认空实现，子类可选择性覆盖
    }

    /** 追踪钩子方法 - 对话完成时调用
     * 
     * @param chatContext 对话上下文
     * @param success 是否成功
     * @param errorMessage 错误信息（成功时为null） */
    protected void onChatCompleted(ChatContext chatContext, boolean success, String errorMessage) {
        // 默认空实现，子类可选择性覆盖
    }

    /** 子类可以覆盖这个方法提供工具 */
    protected ToolProvider provideTools(ChatContext chatContext) {
        return null; // 默认不提供工具
    }

    /** 流式聊天处理 */
    protected <T> void processStreamingChat(ChatContext chatContext, T connection, MessageTransport<T> transport,
            MessageEntity userEntity, MessageEntity llmEntity, MessageWindowChatMemory memory,
            ToolProvider toolProvider) {

        // 获取流式LLM客户端
        StreamingChatModel streamingClient = llmServiceFactory.getStreamingClient(chatContext.getProvider(),
                chatContext.getModel());

        // 创建流式Agent
        Agent agent = buildStreamingAgent(streamingClient, memory, toolProvider, chatContext.getAgent());

        // 使用现有的流式处理逻辑
        processChat(agent, connection, transport, chatContext, userEntity, llmEntity);
    }

    /** 同步聊天处理 */
    protected <T> void processSyncChat(ChatContext chatContext, T connection, MessageTransport<T> transport,
            MessageEntity userEntity, MessageEntity llmEntity, MessageWindowChatMemory memory,
            ToolProvider toolProvider) {

        // 1. 获取同步LLM客户端
        ChatModel syncClient = llmServiceFactory.getStrandClient(chatContext.getProvider(), chatContext.getModel());

        // 2. 保存用户消息和摘要
        this.saveMessageAndUpdateContext(chatContext, userEntity);

        // 3. 记录调用开始时间
        long startTime = System.currentTimeMillis();

        try {

            List<ChatMessage> messages = memory.messages();
            messages.add(new UserMessage(chatContext.getUserMessage()));

            // 4. 构建同步Agent并调用
            ChatResponse chatResponse = syncClient.chat(messages);

            // 5. 处理响应 - 设置消息token
            this.setMessageTokenCount(chatContext.getMessageHistory(), userEntity, llmEntity, chatResponse);

            // 6. 保存消息
            messageDomainService.updateMessage(userEntity);
            messageDomainService.saveMessageAndUpdateContext(Collections.singletonList(llmEntity),
                    chatContext.getContextEntity());

            // 7. 发送完整响应
            AgentChatResponse response = new AgentChatResponse(chatResponse.aiMessage().text(), true);
            response.setMessageType(MessageType.TEXT);
            transport.sendEndMessage(connection, response);

            // 8. 上报调用成功结果
            long latency = System.currentTimeMillis() - startTime;
            highAvailabilityDomainService.reportCallResult(chatContext.getInstanceId(), chatContext.getModel().getId(),
                    true, latency, null);

            // 9. 调用对话完成钩子
            onChatCompleted(chatContext, true, null);

        } catch (Exception e) {
            // 直接发送错误消息
            AgentChatResponse errorResponse = AgentChatResponse.buildEndMessage(e.getMessage(), MessageType.TEXT);
            transport.sendMessage(connection, errorResponse);

            long latency = System.currentTimeMillis() - startTime;
            highAvailabilityDomainService.reportCallResult(chatContext.getInstanceId(), chatContext.getModel().getId(),
                    false, latency, e.getMessage());

            onChatCompleted(chatContext, false, e.getMessage());
        }
    }

    /** 保存用户、摘要消息记录和更新活跃消息
     * @param chatContext 对话环境
     * @param userEntity 此次的用户消息 */
    private void saveMessageAndUpdateContext(ChatContext chatContext, MessageEntity userEntity) {
        MessageEntity summary = this.getSummaryFromHistory(chatContext.getMessageHistory());
        ContextEntity contextEntity = chatContext.getContextEntity();
        if (summary != null) {
            // 不重置 created_at 字段
            messageDomainService.saveMessage(Collections.singletonList(summary));
        }
        List<String> activeMessages = chatContext.getMessageHistory().stream().filter(Objects::nonNull)
                .sorted(Comparator.comparing(MessageEntity::getCreatedAt)).map(MessageEntity::getId)
                .collect(Collectors.toList());
        contextEntity.setActiveMessages(activeMessages);
        // 保存用户消息
        messageDomainService.saveMessageAndUpdateContext(Collections.singletonList(userEntity), contextEntity);
    }

    /** 子类实现具体的聊天处理逻辑 */
    protected <T> void processChat(Agent agent, T connection, MessageTransport<T> transport, ChatContext chatContext,
            MessageEntity userEntity, MessageEntity llmEntity) {

        // 保存用户消息和摘要
        this.saveMessageAndUpdateContext(chatContext, userEntity);

        AtomicReference<StringBuilder> messageBuilder = new AtomicReference<>(new StringBuilder());
        TokenStream tokenStream = agent.chat(chatContext.getUserMessage());

        // 记录调用开始时间
        long startTime = System.currentTimeMillis();

        tokenStream.onError(throwable -> {
            // 直接发送错误消息，transport内部处理连接异常
            transport.sendMessage(connection,
                    AgentChatResponse.buildEndMessage(throwable.getMessage(), MessageType.TEXT));

            // 上报调用失败结果
            long latency = System.currentTimeMillis() - startTime;
            highAvailabilityDomainService.reportCallResult(chatContext.getInstanceId(), chatContext.getModel().getId(),
                    false, latency, throwable.getMessage());

            onChatCompleted(chatContext, false, throwable.getMessage());
        });

        // 部分响应处理
        tokenStream.onPartialResponse(reply -> {
            messageBuilder.get().append(reply);
            // 删除换行后消息为空字符串
            if (messageBuilder.get().toString().trim().isEmpty()) {
                return;
            }

            // 直接发送消息，transport内部处理连接异常
            transport.sendMessage(connection, AgentChatResponse.build(reply, MessageType.TEXT));
        });

        // 完整响应处理
        tokenStream.onCompleteResponse(chatResponse -> {

            this.setMessageTokenCount(chatContext.getMessageHistory(), userEntity, llmEntity, chatResponse);
            boolean emptyResponse = StringUtils.isBlank(llmEntity.getContent());
            if (emptyResponse) {
                llmEntity.setContent(EMPTY_RESPONSE_FALLBACK);
            }

            // 按仅用户抽取策略，不记录AI文本

            messageDomainService.updateMessage(userEntity);
            // 保存AI消息
            messageDomainService.saveMessageAndUpdateContext(Collections.singletonList(llmEntity),
                    chatContext.getContextEntity());

            // 发送结束消息
            if (emptyResponse) {
                transport.sendEndMessage(connection,
                        AgentChatResponse.buildEndMessage(EMPTY_RESPONSE_FALLBACK, MessageType.TEXT));
            } else {
                transport.sendEndMessage(connection, AgentChatResponse.buildEndMessage(MessageType.TEXT));
            }

            // 上报调用成功结果
            long latency = System.currentTimeMillis() - startTime;
            highAvailabilityDomainService.reportCallResult(chatContext.getInstanceId(), chatContext.getModel().getId(),
                    true, latency, null);

            // 调用对话完成钩子
            onChatCompleted(chatContext, true, null);

            smartRenameSession(chatContext);
        });

        // 错误处理
        // tokenStream.onError(throwable -> handleError(
        // connection, transport, chatContext,
        // messageBuilder.toString(), llmEntity, throwable));

        // 工具执行处理
        tokenStream.onToolExecuted(toolExecution -> {
            if (!messageBuilder.get().isEmpty()) {
                transport.sendMessage(connection, AgentChatResponse.buildEndMessage(MessageType.TEXT));
                llmEntity.setContent(messageBuilder.get().toString());
                messageDomainService.saveMessageAndUpdateContext(Collections.singletonList(llmEntity),
                        chatContext.getContextEntity());
                messageBuilder.set(new StringBuilder());
            }
            String message = "执行工具：" + toolExecution.request().name();
            MessageEntity toolMessage = createLlmMessage(chatContext);
            toolMessage.setMessageType(MessageType.TOOL_CALL);
            toolMessage.setContent(message);
            messageDomainService.saveMessageAndUpdateContext(Collections.singletonList(toolMessage),
                    chatContext.getContextEntity());

            // 直接发送工具调用消息
            transport.sendMessage(connection, AgentChatResponse.buildEndMessage(message, MessageType.TOOL_CALL));

        });

        // 启动流处理
        tokenStream.start();
    }

    @Nullable
    private MessageEntity getSummaryFromHistory(List<MessageEntity> historyMessages) {
        // List<MessageEntity> list = historyMessages.stream().filter(MessageEntity::isSummaryMessage).toList();
        if (historyMessages.isEmpty()) {
            return null;
        }
        return historyMessages.get(0).isSummaryMessage() ? historyMessages.get(0) : null;
    }

    /** 根据历史消息的本体token算出本次消息的本体token
     * @param historyMessages 历史消息列表
     * @param userEntity 用户请求消息实体
     * @param llmEntity llm回复消息实体
     * @param chatResponse llm响应 */
    private void setMessageTokenCount(List<MessageEntity> historyMessages, MessageEntity userEntity,
            MessageEntity llmEntity, ChatResponse chatResponse) {
        llmEntity.setTokenCount(chatResponse.tokenUsage().outputTokenCount());
        llmEntity.setBodyTokenCount(chatResponse.tokenUsage().outputTokenCount());
        llmEntity.setContent(chatResponse.aiMessage().text());
        int bodyTokenSum = 0;
        if (CollectionUtil.isNotEmpty(historyMessages)) {
            bodyTokenSum = historyMessages.stream().mapToInt(MessageEntity::getBodyTokenCount).sum();
        }
        userEntity.setTokenCount(chatResponse.tokenUsage().inputTokenCount());
        userEntity.setBodyTokenCount(chatResponse.tokenUsage().inputTokenCount() - bodyTokenSum);
    }

    /** 初始化内存 */
    protected MessageWindowChatMemory initMemory() {
        return MessageWindowChatMemory.builder().maxMessages(1000).chatMemoryStore(new InMemoryChatMemoryStore())
                .build();
    }

    /** 构建流式Agent */
    protected Agent buildStreamingAgent(StreamingChatModel model, MessageWindowChatMemory memory,
            ToolProvider toolProvider, AgentEntity agent) {

        // 通过内置工具注册器获取所有适用的内置工具
        Map<ToolSpecification, ToolExecutor> builtInTools = builtInToolRegistry.createToolsForAgent(agent);

        AiServices<Agent> agentService = AiServices.builder(Agent.class).streamingChatModel(model).chatMemory(memory);

        // 添加内置工具（如RAG等）
        if (builtInTools != null && !builtInTools.isEmpty()) {
            agentService.tools(builtInTools);
        }

        // 添加外部工具提供者
        if (toolProvider != null) {
            agentService.toolProvider(toolProvider);
        }

        return agentService.build();
    }

    /** 创建用户消息实体 */
    protected MessageEntity createUserMessage(ChatContext environment) {
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setRole(Role.USER);
        messageEntity.setContent(environment.getUserMessage());
        messageEntity.setSessionId(environment.getSessionId());
        messageEntity.setFileUrls(environment.getFileUrls());
        return messageEntity;
    }

    /** 创建LLM消息实体 */
    protected MessageEntity createLlmMessage(ChatContext environment) {
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setRole(Role.ASSISTANT);
        messageEntity.setSessionId(environment.getSessionId());
        messageEntity.setModel(environment.getModel().getModelId());
        messageEntity.setProvider(environment.getProvider().getId());
        return messageEntity;
    }

    /** 构建历史消息到内存中 */
    protected void buildHistoryMessage(ChatContext chatContext, MessageWindowChatMemory memory) {
        // String summary = chatContext.getContextEntity().getSummary();
        String summary = Optional.ofNullable(this.getSummaryFromHistory(chatContext.getMessageHistory()))
                .map(MessageEntity::getContent).orElse("");
        if (StringUtils.isNotEmpty(summary)) {
            // 添加为AI消息，但明确标识这是摘要
            memory.add(new AiMessage(summary));
        }

        String presetToolPrompt = "";
        // 设置预先工具设置的参数到系统提示词中
        Map<String, Map<String, Map<String, String>>> toolPresetParams = chatContext.getAgent().getToolPresetParams();
        if (toolPresetParams != null) {
            presetToolPrompt = AgentPromptTemplates.generatePresetToolPrompt(toolPresetParams);
        }

        String fullSystemPrompt = chatContext.getAgent().getSystemPrompt() + "\n" + presetToolPrompt;

        memory.add(new SystemMessage(fullSystemPrompt));
        List<MessageEntity> messageHistory = chatContext.getMessageHistory();
        for (MessageEntity messageEntity : messageHistory) {
            // 注意不要重复发送摘要消息
            if (messageEntity.isUserMessage()) {
                List<String> fileUrls = messageEntity.getFileUrls();
                for (String fileUrl : fileUrls) {
                    memory.add(UserMessage.from(ImageContent.from(fileUrl)));
                }
                if (!StringUtils.isEmpty(messageEntity.getContent())) {
                    memory.add(new UserMessage(messageEntity.getContent()));
                }
            } else if (messageEntity.isAIMessage()) {
                memory.add(new AiMessage(messageEntity.getContent()));
            } else if (messageEntity.isSystemMessage()) {
                memory.add(new SystemMessage(messageEntity.getContent()));
            }
        }
    }

    // 智能重命名会话
    protected void smartRenameSession(ChatContext chatContext) {
        Thread thread = new Thread(() -> {
            // 获取会话 id
            String sessionId = chatContext.getSessionId();
            // 是否是首次对话
            boolean isFirstConversation = messageDomainService.isFirstConversation(sessionId);
            // 如果首次对话，则重命名会话
            if (isFirstConversation) {
                // 调用用户默认模型进行智能会话名称
                String userId = chatContext.getUserId();
                String userDefaultModelId = userSettingsDomainService.getUserDefaultModelId(userId);
                ModelEntity model = llmDomainService.getModelById(userDefaultModelId);
                // 4. 获取用户降级配置
                List<String> fallbackChain = userSettingsDomainService.getUserFallbackChain(userId);

                // 5. 获取服务商信息（支持高可用、会话亲和性和降级）
                HighAvailabilityResult result = highAvailabilityDomainService.selectBestProvider(model, userId,
                        sessionId, fallbackChain);
                ProviderEntity provider = result.getProvider();
                ModelEntity selectedModel = result.getModel();
                ChatModel strandClient = llmServiceFactory.getStrandClient(provider, selectedModel);
                ArrayList<ChatMessage> chatMessages = new ArrayList<>();
                chatMessages.add(new SystemMessage(AgentPromptTemplates.getStartConversationPrompt()));
                chatMessages.add(new UserMessage(chatContext.getUserMessage()));
                ChatResponse chat = strandClient.chat(chatMessages);
                String sessionTitle = chat.aiMessage().text();
                sessionDomainService.updateSession(chatContext.getSessionId(), userId, sessionTitle);

            }
        });
        thread.start();
    }

}
