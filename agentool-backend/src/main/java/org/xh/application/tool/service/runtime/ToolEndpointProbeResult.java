package org.xh.application.tool.service.runtime;

import org.xh.domain.tool.model.config.ToolDefinition;

import java.util.List;

/** 工具端点探测结果 */
public class ToolEndpointProbeResult {

    private String mcpServerName;

    private List<ToolDefinition> toolDefinitions;

    public ToolEndpointProbeResult() {
    }

    public ToolEndpointProbeResult(String mcpServerName, List<ToolDefinition> toolDefinitions) {
        this.mcpServerName = mcpServerName;
        this.toolDefinitions = toolDefinitions;
    }

    public String getMcpServerName() {
        return mcpServerName;
    }

    public void setMcpServerName(String mcpServerName) {
        this.mcpServerName = mcpServerName;
    }

    public List<ToolDefinition> getToolDefinitions() {
        return toolDefinitions;
    }

    public void setToolDefinitions(List<ToolDefinition> toolDefinitions) {
        this.toolDefinitions = toolDefinitions;
    }
}
