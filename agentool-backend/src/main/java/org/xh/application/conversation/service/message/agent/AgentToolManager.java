package org.xh.application.conversation.service.message.agent;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.PresetParameter;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProviderResult;
import dev.langchain4j.service.tool.ToolProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xh.application.conversation.service.handler.context.ChatContext;
import org.xh.application.conversation.service.message.agent.runtime.DirectToolRuntime;
import org.xh.application.conversation.service.message.agent.runtime.DirectToolRuntimeResolver;
import org.xh.application.conversation.service.message.agent.runtime.HttpApiToolProviderFactory;
import org.xh.application.conversation.service.message.agent.runtime.RemoteMcpToolProviderFactory;
import org.xh.domain.tool.constant.ToolRuntimeType;
import org.xh.infrastructure.utils.JsonUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Agent工具管理器 负责创建和管理工具提供者 */
@Component
public class AgentToolManager {

    private static final Logger logger = LoggerFactory.getLogger(AgentToolManager.class);

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
                httpToolBuilder.addAll(wrapToolExecutors(httpApiToolProviderFactory.createTools(runtime),
                        runtime.getServerName()));
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
                try {
                    resultBuilder.addAll(wrapToolExecutors(mcpToolProvider.provideTools(request).tools(), "remote-mcp"));
                } catch (Exception e) {
                    logger.warn("获取远程 MCP 工具列表失败: {}", e.getMessage(), e);
                }
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

    private Map<ToolSpecification, ToolExecutor> wrapToolExecutors(Map<ToolSpecification, ToolExecutor> tools,
            String runtimeName) {
        if (tools == null || tools.isEmpty()) {
            return Map.of();
        }
        Map<ToolSpecification, ToolExecutor> wrappedTools = new LinkedHashMap<>();
        tools.forEach((specification, executor) -> wrappedTools.put(specification, (request, memoryId) -> {
            try {
                String result = executor.execute(request, memoryId);
                if (result == null || result.trim().isEmpty()) {
                    return formatToolFailure(runtimeName, request.name(), "工具没有返回有效内容");
                }
                return result;
            } catch (Throwable throwable) {
                logger.warn("工具执行失败: runtime={}, tool={}, error={}", runtimeName, request.name(),
                        throwable.getMessage(), throwable);
                return formatToolFailure(runtimeName, request.name(), throwable.getMessage());
            }
        }));
        return wrappedTools;
    }

    private String formatToolFailure(String runtimeName, String toolName, String reason) {
        String safeReason = reason == null || reason.trim().isEmpty() ? "未知错误" : reason;
        return "工具执行失败：runtime=" + runtimeName + ", tool=" + toolName + ", reason=" + safeReason
                + "。请不要继续重复调用同一个失败工具，直接向用户说明工具暂时不可用，并给出可行的下一步建议。";
    }
}
