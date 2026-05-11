package org.xhy.application.tool.service.runtime;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import org.springframework.stereotype.Component;
import org.xhy.domain.tool.constant.ToolRuntimeType;
import org.xhy.domain.tool.model.config.ToolDefinition;
import org.xhy.domain.tool.model.config.ToolSpecificationConverter;
import org.xhy.infrastructure.exception.BusinessException;
import org.xhy.interfaces.dto.tool.request.ToolRuntimeConfigRequest;

import java.net.URI;
import java.time.Duration;
import java.util.List;

/** 远程 MCP endpoint 探测客户端 */
@Component
public class RemoteMcpProbeClient {

    public ToolEndpointProbeResult probe(ToolRuntimeConfigRequest request) {
        if (request.getRuntimeType() != ToolRuntimeType.REMOTE_MCP_SSE) {
            throw new BusinessException("当前只支持远程 MCP SSE 探测");
        }
        validateEndpoint(request.getEndpointUrl());

        HttpMcpTransport transport = new HttpMcpTransport.Builder().sseUrl(request.getEndpointUrl())
                .timeout(Duration.ofMillis(resolveTimeout(request))).logRequests(false).logResponses(false).build();
        McpClient client = new DefaultMcpClient.Builder().transport(transport).build();
        try {
            List<ToolSpecification> specifications = client.listTools();
            List<ToolDefinition> definitions = ToolSpecificationConverter.convert(specifications);
            if (definitions == null || definitions.isEmpty()) {
                throw new BusinessException("远程 MCP 未返回可用工具");
            }

            String serverName = request.getMcpServerName();
            if (serverName == null || serverName.trim().isEmpty()) {
                serverName = definitions.get(0).getName();
            }
            return new ToolEndpointProbeResult(serverName, definitions);
        } catch (Exception e) {
            throw new BusinessException("远程 MCP 工具探测失败: " + e.getMessage(), e);
        } finally {
            try {
                client.close();
            } catch (Exception ignored) {
                // 探测阶段关闭失败不影响已得到的业务结果
            }
        }
    }

    private long resolveTimeout(ToolRuntimeConfigRequest request) {
        Integer timeoutMs = request.getTimeoutMs();
        return timeoutMs == null || timeoutMs <= 0 ? 30000L : timeoutMs.longValue();
    }

    private void validateEndpoint(String endpointUrl) {
        if (endpointUrl == null || endpointUrl.trim().isEmpty()) {
            throw new BusinessException("MCP Endpoint URL 不能为空");
        }
        try {
            URI uri = URI.create(endpointUrl);
            if (!"https".equalsIgnoreCase(uri.getScheme()) && !"http".equalsIgnoreCase(uri.getScheme())) {
                throw new BusinessException("MCP Endpoint URL 仅支持 http/https");
            }
        } catch (IllegalArgumentException e) {
            throw new BusinessException("MCP Endpoint URL 格式不正确");
        }
    }
}
