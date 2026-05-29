package org.xh.application.conversation.service.message.agent.runtime;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutor;
import org.springframework.stereotype.Service;
import org.xh.application.tool.service.runtime.ToolSchemaUtils;
import org.xh.domain.tool.model.ToolRuntimeConfigEntity;
import org.xh.domain.tool.model.config.ToolDefinition;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 将 HTTP API 运行配置转换为 LangChain4j 工具。 */
@Service
public class HttpApiToolProviderFactory {

    public Map<ToolSpecification, ToolExecutor> createTools(DirectToolRuntime runtime) {
        ToolRuntimeConfigEntity config = runtime.getRuntimeConfig();
        List<ToolDefinition> definitions = config.getToolDefinitions();
        if (definitions == null || definitions.isEmpty()) {
            definitions = ToolSchemaUtils
                    .one(ToolSchemaUtils.createHttpToolDefinition(runtime.getServerName(), runtime.getServerName(),
                            config.getRequestSchema()));
        }

        Map<ToolSpecification, ToolExecutor> tools = new LinkedHashMap<>();
        for (ToolDefinition definition : definitions) {
            if (Boolean.FALSE.equals(definition.getEnabled())) {
                continue;
            }
            ToolSpecification specification = ToolSpecification.builder().name(definition.getName())
                    .description(definition.getDescription())
                    .parameters(ToolSchemaUtils.toJsonObjectSchema(definition.getParameters())).build();
            tools.put(specification, new HttpApiToolExecutor(runtime));
        }
        return tools;
    }
}
