package org.xhy.domain.tool.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.domain.tool.constant.ToolAuthType;
import org.xhy.domain.tool.constant.ToolRuntimeType;
import org.xhy.domain.tool.model.config.ToolDefinition;
import org.xhy.infrastructure.converter.MapConverter;
import org.xhy.infrastructure.converter.ToolAuthTypeConverter;
import org.xhy.infrastructure.converter.ToolDefinitionListConverter;
import org.xhy.infrastructure.converter.ToolRuntimeTypeConverter;
import org.xhy.infrastructure.entity.BaseEntity;

import java.util.List;
import java.util.Map;

/** 工具运行配置，描述 AgentX 如何直接调用远程 MCP 或 HTTP API。 */
@TableName(value = "tool_runtime_configs", autoResultMap = true)
public class ToolRuntimeConfigEntity extends BaseEntity {

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("tool_id")
    private String toolId;

    @TableField("tool_version")
    private String toolVersion;

    @TableField(value = "runtime_type", typeHandler = ToolRuntimeTypeConverter.class)
    private ToolRuntimeType runtimeType;

    @TableField("transport_type")
    private String transportType;

    @TableField("endpoint_url")
    private String endpointUrl;

    @TableField("http_method")
    private String httpMethod;

    @TableField(value = "auth_type", typeHandler = ToolAuthTypeConverter.class)
    private ToolAuthType authType = ToolAuthType.NONE;

    @TableField(value = "auth_config_schema", typeHandler = MapConverter.class)
    private Map<String, Object> authConfigSchema;

    @TableField(value = "default_headers", typeHandler = MapConverter.class)
    private Map<String, Object> defaultHeaders;

    @TableField(value = "request_schema", typeHandler = MapConverter.class)
    private Map<String, Object> requestSchema;

    @TableField(value = "response_schema", typeHandler = MapConverter.class)
    private Map<String, Object> responseSchema;

    @TableField(value = "tool_definitions", typeHandler = ToolDefinitionListConverter.class)
    private List<ToolDefinition> toolDefinitions;

    @TableField("mcp_server_name")
    private String mcpServerName;

    @TableField("timeout_ms")
    private Integer timeoutMs = 30000;

    @TableField("enabled")
    private Boolean enabled = true;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToolId() {
        return toolId;
    }

    public void setToolId(String toolId) {
        this.toolId = toolId;
    }

    public String getToolVersion() {
        return toolVersion;
    }

    public void setToolVersion(String toolVersion) {
        this.toolVersion = toolVersion;
    }

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

    public List<ToolDefinition> getToolDefinitions() {
        return toolDefinitions;
    }

    public void setToolDefinitions(List<ToolDefinition> toolDefinitions) {
        this.toolDefinitions = toolDefinitions;
    }

    public String getMcpServerName() {
        return mcpServerName;
    }

    public void setMcpServerName(String mcpServerName) {
        this.mcpServerName = mcpServerName;
    }

    public Integer getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(Integer timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
