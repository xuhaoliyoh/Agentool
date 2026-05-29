package org.xh.application.conversation.service.message.agent.runtime;

import org.xh.domain.tool.constant.ToolAuthType;
import org.xh.domain.tool.model.ToolRuntimeConfigEntity;
import org.xh.domain.tool.model.UserToolCredentialEntity;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/** 直连工具鉴权配置组装。 */
public final class ToolAuthSupport {

    private ToolAuthSupport() {
    }

    public static Map<String, String> buildHeaders(ToolRuntimeConfigEntity config, UserToolCredentialEntity credential) {
        Map<String, String> headers = new HashMap<>();
        if (config.getDefaultHeaders() != null) {
            config.getDefaultHeaders().forEach((key, value) -> {
                if (key != null && value != null) {
                    headers.put(key, String.valueOf(value));
                }
            });
        }

        ToolAuthType authType = config.getAuthType();
        Map<String, Object> values = credential == null ? Map.of() : credential.getAuthValues();
        if (authType == null || authType == ToolAuthType.NONE || values == null) {
            return headers;
        }

        if (authType == ToolAuthType.BEARER_TOKEN) {
            Object token = values.getOrDefault("token", values.get("apiKey"));
            if (token != null) {
                headers.put("Authorization", "Bearer " + token);
            }
        } else if (authType == ToolAuthType.API_KEY_HEADER) {
            String headerName = getConfigValue(config, "headerName", "X-API-Key");
            Object apiKey = values.getOrDefault("apiKey", values.get("token"));
            if (apiKey != null) {
                headers.put(headerName, String.valueOf(apiKey));
            }
        } else if (authType == ToolAuthType.BASIC) {
            Object username = values.get("username");
            Object password = values.get("password");
            if (username != null && password != null) {
                String raw = username + ":" + password;
                headers.put("Authorization", "Basic " + Base64.getEncoder().encodeToString(raw.getBytes()));
            }
        }
        return headers;
    }

    public static String appendQueryAuth(String endpointUrl, ToolRuntimeConfigEntity config,
            UserToolCredentialEntity credential) {
        if (config.getAuthType() != ToolAuthType.API_KEY_QUERY || credential == null
                || credential.getAuthValues() == null) {
            return endpointUrl;
        }
        Object apiKey = credential.getAuthValues().getOrDefault("apiKey", credential.getAuthValues().get("token"));
        if (apiKey == null) {
            return endpointUrl;
        }
        String queryName = getConfigValue(config, "queryName", "api_key");
        String separator = endpointUrl.contains("?") ? "&" : "?";
        return endpointUrl + separator + URLEncoder.encode(queryName, StandardCharsets.UTF_8) + "="
                + URLEncoder.encode(String.valueOf(apiKey), StandardCharsets.UTF_8);
    }

    private static String getConfigValue(ToolRuntimeConfigEntity config, String key, String defaultValue) {
        if (config.getAuthConfigSchema() != null && config.getAuthConfigSchema().get(key) != null) {
            return String.valueOf(config.getAuthConfigSchema().get(key));
        }
        return defaultValue;
    }
}
