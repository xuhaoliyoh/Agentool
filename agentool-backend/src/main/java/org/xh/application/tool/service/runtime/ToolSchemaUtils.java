package org.xh.application.tool.service.runtime;

import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import org.xh.domain.tool.model.config.ToolDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 工具 schema 转换工具。 */
public final class ToolSchemaUtils {

    private ToolSchemaUtils() {
    }

    @SuppressWarnings("unchecked")
    public static JsonObjectSchema toJsonObjectSchema(Map<String, Object> schema) {
        JsonObjectSchema.Builder builder = JsonObjectSchema.builder().additionalProperties(true);
        if (schema == null || schema.isEmpty()) {
            return builder.build();
        }

        Object propertiesObject = schema.get("properties");
        if (propertiesObject instanceof Map<?, ?> properties) {
            for (Map.Entry<?, ?> entry : properties.entrySet()) {
                String name = String.valueOf(entry.getKey());
                String type = "string";
                String description = null;
                if (entry.getValue() instanceof Map<?, ?> detail) {
                    Object typeObject = detail.get("type");
                    Object descriptionObject = detail.get("description");
                    if (typeObject != null) {
                        type = String.valueOf(typeObject);
                    }
                    if (descriptionObject != null) {
                        description = String.valueOf(descriptionObject);
                    }
                }

                switch (type) {
                    case "integer" -> builder.addIntegerProperty(name, description);
                    case "number" -> builder.addNumberProperty(name, description);
                    case "boolean" -> builder.addBooleanProperty(name, description);
                    default -> builder.addStringProperty(name, description);
                }
            }
        }

        Object requiredObject = schema.get("required");
        if (requiredObject instanceof List<?> required) {
            List<String> requiredFields = required.stream().map(String::valueOf).toList();
            builder.required(requiredFields);
        } else if (requiredObject instanceof String[] required) {
            builder.required(required);
        }

        return builder.build();
    }

    public static ToolDefinition createHttpToolDefinition(String name, String description, Map<String, Object> schema) {
        ToolDefinition definition = new ToolDefinition();
        definition.setName(name);
        definition.setDescription(description);
        definition.setEnabled(true);
        definition.setParameters(schema);
        return definition;
    }

    public static List<ToolDefinition> one(ToolDefinition definition) {
        List<ToolDefinition> result = new ArrayList<>();
        result.add(definition);
        return result;
    }
}
