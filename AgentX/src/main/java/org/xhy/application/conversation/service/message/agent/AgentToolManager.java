package org.xhy.application.conversation.service.message.agent;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.PresetParameter;
import dev.langchain4j.service.tool.ToolProviderResult;
import dev.langchain4j.service.tool.ToolProvider;
import org.springframework.stereotype.Component;
import org.xhy.application.conversation.service.handler.context.ChatContext;
import org.xhy.application.conversation.service.message.agent.runtime.DirectToolRuntime;
import org.xhy.application.conversation.service.message.agent.runtime.DirectToolRuntimeResolver;
import org.xhy.application.conversation.service.message.agent.runtime.HttpApiToolProviderFactory;
import org.xhy.application.conversation.service.message.agent.runtime.RemoteMcpToolProviderFactory;
import org.xhy.domain.tool.constant.ToolRuntimeType;
import org.xhy.infrastructure.utils.JsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Agent工具管理器 负责创建和管理工具提供者 */
@Component
public class AgentToolManager {

    private final DirectToolRuntimeResolver directToolRuntimeResolver;
    private final RemoteMcpToolProviderFactory remoteMcpToolProviderFactory;
    private final HttpApiToolProviderFactory httpApiToolProviderFactory;

    public AgentToolManager(DirectToolRuntimeResolver directToolRuntimeResolver,
            RemoteMcpToolProviderFactory remoteMcpToolProviderFactory,
            HttpApiToolProviderFactory httpApiToolProviderFactory) {
        this.directToolRuntimeResolver = directToolRuntimeResolver;
        this.remoteMcpToolProviderFactory = remoteMcpToolProviderFactory;
        this.httpApiToolProviderFactory = httpApiToolProviderFactory;
    }

    /** 创建工具提供者（支持全局/用户隔离工具自动识别）
     *
     * @param mcpServerNames 工具服务名列表
     * @param toolPresetParams 工具预设参数
     * @param userId 用户ID（关键参数：用于用户隔离工具）
     * @return 工具提供者实例，如果工具列表为空则返回null */
    public ToolProvider createToolProvider(List<String> mcpServerNames,
            Map<String, Map<String, Map<String, String>>> toolPresetParams, String userId) {
        if (mcpServerNames == null || mcpServerNames.isEmpty()) {
            return null;
        }

        List<McpClient> mcpClients = new ArrayList<>();
        ToolProviderResult.Builder httpToolBuilder = ToolProviderResult.builder();

        for (String mcpServerName : mcpServerNames) {
            DirectToolRuntime runtime = directToolRuntimeResolver.resolve(mcpServerName, userId);
            if (runtime.getRuntimeConfig().getRuntimeType() == ToolRuntimeType.HTTP_API) {
                httpToolBuilder.addAll(httpApiToolProviderFactory.createTools(runtime));
                continue;
            }
            if (runtime.getRuntimeConfig().getRuntimeType() == ToolRuntimeType.REMOTE_MCP_SSE) {
                McpClient mcpClient = remoteMcpToolProviderFactory.createClient(runtime);
                presetToolParameters(mcpClient, mcpServerName, toolPresetParams);
                mcpClients.add(mcpClient);
            }
        }

        ToolProvider mcpToolProvider = mcpClients.isEmpty() ? null : McpToolProvider.builder().mcpClients(mcpClients).build();

        return request -> {
            ToolProviderResult.Builder resultBuilder = ToolProviderResult.builder();
            resultBuilder.addAll(httpToolBuilder.build().tools());
            if (mcpToolProvider != null) {
                resultBuilder.addAll(mcpToolProvider.provideTools(request).tools());
            }
            return resultBuilder.build();
        };
    }

    /** 获取可用的工具列表
     *
     * @return 工具URL列表 */
    public List<String> getAvailableTools(ChatContext chatContext) {
        return chatContext.getMcpServerNames();
    }

    private void presetToolParameters(McpClient mcpClient, String mcpServerName,
            Map<String, Map<String, Map<String, String>>> toolPresetParams) {
        if (toolPresetParams == null || !toolPresetParams.containsKey(mcpServerName)) {
            return;
        }
        List<PresetParameter> presetParameters = new ArrayList<>();
        toolPresetParams.get(mcpServerName).forEach((toolName, params) -> {
            presetParameters.add(new PresetParameter(toolName, JsonUtils.toJsonString(params)));
        });
        mcpClient.presetParameters(presetParameters);
    }
}
