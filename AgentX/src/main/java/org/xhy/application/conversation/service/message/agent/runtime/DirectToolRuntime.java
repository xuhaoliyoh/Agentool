package org.xhy.application.conversation.service.message.agent.runtime;

import org.xhy.domain.tool.model.ToolRuntimeConfigEntity;
import org.xhy.domain.tool.model.UserToolCredentialEntity;
import org.xhy.domain.tool.model.UserToolEntity;

/** 会话执行期的直连工具运行配置 */
public class DirectToolRuntime {

    private final String serverName;
    private final UserToolEntity userTool;
    private final ToolRuntimeConfigEntity runtimeConfig;
    private final UserToolCredentialEntity credential;

    public DirectToolRuntime(String serverName, UserToolEntity userTool, ToolRuntimeConfigEntity runtimeConfig,
            UserToolCredentialEntity credential) {
        this.serverName = serverName;
        this.userTool = userTool;
        this.runtimeConfig = runtimeConfig;
        this.credential = credential;
    }

    public String getServerName() {
        return serverName;
    }

    public UserToolEntity getUserTool() {
        return userTool;
    }

    public ToolRuntimeConfigEntity getRuntimeConfig() {
        return runtimeConfig;
    }

    public UserToolCredentialEntity getCredential() {
        return credential;
    }
}
