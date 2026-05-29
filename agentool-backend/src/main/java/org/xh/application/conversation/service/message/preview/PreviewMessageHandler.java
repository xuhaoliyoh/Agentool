package org.xh.application.conversation.service.message.preview;

import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolProvider;
import org.springframework.stereotype.Component;
import org.xh.application.conversation.dto.AgentChatResponse;
import org.xh.application.conversation.service.handler.context.ChatContext;
import org.xh.application.conversation.service.message.AbstractMessageHandler;
import org.xh.application.conversation.service.message.Agent;
import org.xh.application.conversation.service.message.agent.AgentToolManager;
import org.xh.application.conversation.service.message.builtin.BuiltInToolRegistry;
import org.xh.application.conversation.service.ChatSessionManager;
import org.xh.domain.conversation.constant.MessageType;
import org.xh.domain.conversation.model.MessageEntity;
import org.xh.domain.conversation.service.MessageDomainService;
import org.xh.domain.conversation.service.SessionDomainService;
import org.xh.domain.llm.service.HighAvailabilityDomainService;
import org.xh.domain.llm.service.LLMDomainService;
import org.xh.domain.user.service.UserSettingsDomainService;
import org.xh.infrastructure.llm.LLMServiceFactory;
import org.xh.infrastructure.transport.MessageTransport;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

/** 预览消息处理器 专门用于Agent预览功能，不会保存消息到数据库 */
@Component(value = "previewMessageHandler")
public class PreviewMessageHandler extends AbstractMessageHandler {

    private final AgentToolManager agentToolManager;

    public PreviewMessageHandler(LLMServiceFactory llmServiceFactory, MessageDomainService messageDomainService,
            HighAvailabilityDomainService highAvailabilityDomainService, SessionDomainService sessionDomainService,
            UserSettingsDomainService userSettingsDomainService, LLMDomainService llmDomainService,
            BuiltInToolRegistry builtInToolRegistry, ChatSessionManager chatSessionManager,
            AgentToolManager agentToolManager) {
        super(llmServiceFactory, messageDomainService, highAvailabilityDomainService, sessionDomainService,
                userSettingsDomainService, llmDomainService, builtInToolRegistry, chatSessionManager);
        this.agentToolManager = agentToolManager;
    }

    @Override
    protected ToolProvider provideTools(ChatContext chatContext) {
        return agentToolManager.createToolProvider(agentToolManager.getAvailableTools(chatContext),
                chatContext.getAgent().getToolPresetParams(), chatContext.getUserId());
    }

    /** 预览专用的聊天处理逻辑 与正常流程的区别是不保存消息到数据库 */
    @Override
    protected <T> void processChat(Agent agent, T connection, MessageTransport<T> transport, ChatContext chatContext,
            MessageEntity userEntity, MessageEntity llmEntity) {

        AtomicReference<StringBuilder> messageBuilder = new AtomicReference<>(new StringBuilder());

        TokenStream tokenStream = agent.chat(chatContext.getUserMessage());

        tokenStream.onError(throwable -> {
            transport.sendMessage(connection,
                    AgentChatResponse.buildEndMessage(throwable.getMessage(), MessageType.TEXT));
        });

        // 部分响应处理
        tokenStream.onPartialResponse(reply -> {
            messageBuilder.get().append(reply);
            // 删除换行后消息为空字符串
            if (messageBuilder.get().toString().trim().isEmpty()) {
                return;
            }
            transport.sendMessage(connection, AgentChatResponse.build(reply, MessageType.TEXT));
        });

        // 完整响应处理
        tokenStream.onCompleteResponse(chatResponse -> {
            // 发送结束消息
            transport.sendEndMessage(connection, AgentChatResponse.buildEndMessage(MessageType.TEXT));
        });

        // 工具执行处理
        tokenStream.onToolExecuted(toolExecution -> {
            if (messageBuilder.get().length() > 0) {
                transport.sendMessage(connection, AgentChatResponse.buildEndMessage(MessageType.TEXT));
                llmEntity.setContent(messageBuilder.toString());

                messageBuilder.set(new StringBuilder());
            }
            String message = "执行工具：" + toolExecution.request().name();
            MessageEntity toolMessage = createLlmMessage(chatContext);
            toolMessage.setMessageType(MessageType.TOOL_CALL);
            toolMessage.setContent(message);
            transport.sendMessage(connection, AgentChatResponse.buildEndMessage(message, MessageType.TOOL_CALL));
        });

        // 启动流处理
        tokenStream.start();
    }
}
