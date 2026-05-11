package org.xhy.application.conversation.service.message.agent.runtime;

import org.springframework.stereotype.Service;
import org.xhy.domain.tool.model.ToolRuntimeConfigEntity;
import org.xhy.domain.tool.model.UserToolCredentialEntity;
import org.xhy.domain.tool.model.UserToolEntity;
import org.xhy.domain.tool.service.ToolDomainService;
import org.xhy.domain.tool.service.ToolRuntimeConfigDomainService;
import org.xhy.domain.tool.service.UserToolCredentialDomainService;
import org.xhy.infrastructure.exception.BusinessException;

/** 将会话中的 mcpServerName 解析为用户已安装工具的直连运行配置。 */
@Service
public class DirectToolRuntimeResolver {

    private final ToolDomainService toolDomainService;
    private final ToolRuntimeConfigDomainService toolRuntimeConfigDomainService;
    private final UserToolCredentialDomainService userToolCredentialDomainService;

    public DirectToolRuntimeResolver(ToolDomainService toolDomainService,
            ToolRuntimeConfigDomainService toolRuntimeConfigDomainService,
            UserToolCredentialDomainService userToolCredentialDomainService) {
        this.toolDomainService = toolDomainService;
        this.toolRuntimeConfigDomainService = toolRuntimeConfigDomainService;
        this.userToolCredentialDomainService = userToolCredentialDomainService;
    }

    public DirectToolRuntime resolve(String serverName, String userId) {
        UserToolEntity userTool = toolDomainService.getUserInstalledToolByServerName(serverName, userId);
        if (userTool == null) {
            throw new BusinessException("未安装工具或工具不可用: " + serverName);
        }

        ToolRuntimeConfigEntity runtimeConfig = toolRuntimeConfigDomainService
                .findByToolIdAndVersion(userTool.getToolId(), userTool.getVersion());
        if (runtimeConfig == null) {
            throw new BusinessException("工具运行配置不存在: " + serverName);
        }

        UserToolCredentialEntity credential = userTool.getId() == null
                ? null
                : userToolCredentialDomainService.findByUserToolId(userTool.getId(), userId);
        return new DirectToolRuntime(serverName, userTool, runtimeConfig, credential);
    }
}
