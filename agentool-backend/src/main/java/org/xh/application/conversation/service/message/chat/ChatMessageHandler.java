package org.xh.application.conversation.service.message.chat;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xh.application.conversation.service.message.AbstractMessageHandler;
import org.xh.application.conversation.service.message.builtin.BuiltInToolRegistry;
import org.xh.application.conversation.service.ChatSessionManager;
import org.xh.domain.conversation.service.MessageDomainService;
import org.xh.domain.conversation.service.SessionDomainService;
import org.xh.domain.llm.service.HighAvailabilityDomainService;
import org.xh.domain.llm.service.LLMDomainService;
import org.xh.domain.user.service.UserSettingsDomainService;
import org.xh.infrastructure.llm.LLMServiceFactory;

/** 标准消息处理器 */
@Component(value = "chatMessageHandler")
public class ChatMessageHandler extends AbstractMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageHandler.class);

    public ChatMessageHandler(LLMServiceFactory llmServiceFactory, MessageDomainService messageDomainService,
            HighAvailabilityDomainService highAvailabilityDomainService, SessionDomainService sessionDomainService,
            UserSettingsDomainService userSettingsDomainService, LLMDomainService llmDomainService,
            BuiltInToolRegistry builtInToolRegistry, ChatSessionManager chatSessionManager) {
        super(llmServiceFactory, messageDomainService, highAvailabilityDomainService, sessionDomainService,
                userSettingsDomainService, llmDomainService, builtInToolRegistry, chatSessionManager);
    }
}
