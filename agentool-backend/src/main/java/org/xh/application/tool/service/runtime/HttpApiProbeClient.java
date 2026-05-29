package org.xh.application.tool.service.runtime;

import org.springframework.stereotype.Component;
import org.xh.domain.tool.model.config.ToolDefinition;
import org.xh.infrastructure.exception.BusinessException;
import org.xh.interfaces.dto.tool.request.ToolRuntimeConfigRequest;

import java.net.URI;
import java.util.Locale;

/** HTTP API 工具配置探测客户端 */
@Component
public class HttpApiProbeClient {

    public ToolEndpointProbeResult probe(ToolRuntimeConfigRequest request, String toolName, String description) {
        validateEndpoint(request.getEndpointUrl());
        String method = request.getHttpMethod() == null ? "POST" : request.getHttpMethod().toUpperCase(Locale.ROOT);
        if (!method.equals("GET") && !method.equals("POST") && !method.equals("PUT") && !method.equals("DELETE")
                && !method.equals("PATCH")) {
            throw new BusinessException("不支持的 HTTP Method: " + method);
        }

        String functionName = request.getMcpServerName();
        if (functionName == null || functionName.trim().isEmpty()) {
            functionName = normalizeToolName(toolName);
        }

        ToolDefinition definition = ToolSchemaUtils.createHttpToolDefinition(functionName, description,
                request.getRequestSchema());
        return new ToolEndpointProbeResult(functionName, ToolSchemaUtils.one(definition));
    }

    private void validateEndpoint(String endpointUrl) {
        if (endpointUrl == null || endpointUrl.trim().isEmpty()) {
            throw new BusinessException("HTTP API URL 不能为空");
        }
        try {
            URI uri = URI.create(endpointUrl);
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                throw new BusinessException("HTTP API 工具第一阶段仅允许 https:// 地址");
            }
            String host = uri.getHost();
            if (host == null || host.equalsIgnoreCase("localhost") || host.startsWith("127.")
                    || host.equals("0.0.0.0")) {
                throw new BusinessException("HTTP API 工具不允许访问本机地址");
            }
        } catch (IllegalArgumentException e) {
            throw new BusinessException("HTTP API URL 格式不正确");
        }
    }

    private String normalizeToolName(String toolName) {
        String normalized = toolName == null ? "http_tool" : toolName.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_]+", "_").replaceAll("_+", "_").replaceAll("^_|_$", "");
        return normalized.isEmpty() ? "http_tool" : normalized;
    }
}
