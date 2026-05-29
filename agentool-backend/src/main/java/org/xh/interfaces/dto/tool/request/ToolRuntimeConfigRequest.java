package org.xh.interfaces.dto.tool.request;

import jakarta.validation.constraints.NotNull;
import org.xh.domain.tool.constant.ToolAuthType;
import org.xh.domain.tool.constant.ToolRuntimeType;

import java.util.Map;

/** 工具运行配置请求 */
public class ToolRuntimeConfigRequest {

    @NotNull(message = "工具运行类型不可为空")
    private ToolRuntimeType runtimeType;

    private String transportType;

    private String endpointUrl;

    private String mcpServerName;

    private String httpMethod;

    private ToolAuthType authType = ToolAuthType.NONE;

    private Map<String, Object> authConfigSchema;

    private Map<String, Object> defaultHeaders;

    private Map<String, Object> requestSchema;

    private Map<String, Object> responseSchema;

    private Integer timeoutMs = 30000;

    public ToolRuntimeType getRuntimeType() {
        return runtimeType;
    }

    public void setRuntimeType(ToolRuntimeType runtimeType) {
        this.runtimeType = runtimeType;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public String getMcpServerName() {
        return mcpServerName;
    }

    public void setMcpServerName(String mcpServerName) {
        this.mcpServerName = mcpServerName;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public ToolAuthType getAuthType() {
        return authType;
    }

    public void setAuthType(ToolAuthType authType) {
        this.authType = authType;
    }

    public Map<String, Object> getAuthConfigSchema() {
        return authConfigSchema;
    }

    public void setAuthConfigSchema(Map<String, Object> authConfigSchema) {
        this.authConfigSchema = authConfigSchema;
    }

    public Map<String, Object> getDefaultHeaders() {
        return defaultHeaders;
    }

    public void setDefaultHeaders(Map<String, Object> defaultHeaders) {
        this.defaultHeaders = defaultHeaders;
    }

    public Map<String, Object> getRequestSchema() {
        return requestSchema;
    }

    public void setRequestSchema(Map<String, Object> requestSchema) {
        this.requestSchema = requestSchema;
    }

    public Map<String, Object> getResponseSchema() {
        return responseSchema;
    }

    public void setResponseSchema(Map<String, Object> responseSchema) {
        this.responseSchema = responseSchema;
    }

    public Integer getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(Integer timeoutMs) {
        this.timeoutMs = timeoutMs;
    }
}
