package org.xh.application.conversation.service.message.agent.runtime;

import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import org.springframework.stereotype.Service;
import org.xh.domain.tool.constant.ToolAuthType;
import org.xh.domain.tool.model.ToolRuntimeConfigEntity;
import org.xh.infrastructure.exception.BusinessException;

import java.time.Duration;

/** 创建远程 MCP SSE 客户端。 */
@Service
public class RemoteMcpToolProviderFactory {

    public McpClient createClient(DirectToolRuntime runtime) {
        ToolRuntimeConfigEntity config = runtime.getRuntimeConfig();
        if (config.getAuthType() != null && config.getAuthType() != ToolAuthType.NONE
                && config.getAuthType() != ToolAuthType.API_KEY_QUERY) {
            throw new BusinessException("当前 MCP SSE 连接仅支持 query 参数鉴权: " + runtime.getServerName());
        }
        String sseUrl = ToolAuthSupport.appendQueryAuth(config.getEndpointUrl(), config, runtime.getCredential());
        McpTransport transport = new HttpMcpTransport.Builder().sseUrl(sseUrl).logRequests(true).logResponses(true)
                .timeout(Duration.ofMillis(config.getTimeoutMs() == null ? 30000 : config.getTimeoutMs())).build();
        return new DefaultMcpClient.Builder().transport(transport).build();
    }
}
