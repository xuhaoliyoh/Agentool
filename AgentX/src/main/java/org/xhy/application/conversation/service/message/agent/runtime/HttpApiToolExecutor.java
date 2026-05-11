package org.xhy.application.conversation.service.message.agent.runtime;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.service.tool.ToolExecutor;
import org.xhy.domain.tool.model.ToolRuntimeConfigEntity;
import org.xhy.infrastructure.exception.BusinessException;
import org.xhy.infrastructure.utils.JsonUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

/** HTTP API 工具执行器。 */
public class HttpApiToolExecutor implements ToolExecutor {

    private static final int MAX_RESPONSE_LENGTH = 12000;

    private final DirectToolRuntime runtime;
    private final HttpClient httpClient;

    public HttpApiToolExecutor(DirectToolRuntime runtime) {
        this.runtime = runtime;
        ToolRuntimeConfigEntity config = runtime.getRuntimeConfig();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.getTimeoutMs() == null ? 30000 : config.getTimeoutMs()))
                .build();
    }

    @Override
    public String execute(ToolExecutionRequest request, Object memoryId) {
        try {
            ToolRuntimeConfigEntity config = runtime.getRuntimeConfig();
            Map<String, Object> arguments = JsonUtils.parseMap(request.arguments());
            if (arguments == null) {
                arguments = Map.of();
            }

            String method = config.getHttpMethod() == null ? "POST" : config.getHttpMethod().toUpperCase();
            String endpoint = ToolAuthSupport.appendQueryAuth(config.getEndpointUrl(), config, runtime.getCredential());
            if ("GET".equals(method) && !arguments.isEmpty()) {
                endpoint = appendQuery(endpoint, arguments);
            }

            HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(endpoint))
                    .timeout(Duration.ofMillis(config.getTimeoutMs() == null ? 30000 : config.getTimeoutMs()));

            ToolAuthSupport.buildHeaders(config, runtime.getCredential()).forEach(builder::header);
            if (!"GET".equals(method)) {
                builder.header("Content-Type", "application/json");
            }

            if ("GET".equals(method)) {
                builder.GET();
            } else {
                builder.method(method, HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(arguments)));
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            String body = response.body() == null ? "" : response.body();
            if (body.length() > MAX_RESPONSE_LENGTH) {
                body = body.substring(0, MAX_RESPONSE_LENGTH) + "...";
            }
            if (response.statusCode() >= 400) {
                return "HTTP " + response.statusCode() + ": " + body;
            }
            return body;
        } catch (Exception e) {
            throw new BusinessException("调用 HTTP 工具失败: " + runtime.getServerName() + " - " + e.getMessage());
        }
    }

    private String appendQuery(String endpoint, Map<String, Object> arguments) {
        String query = arguments.entrySet().stream().filter(entry -> entry.getValue() != null)
                .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        if (query.isEmpty()) {
            return endpoint;
        }
        return endpoint + (endpoint.contains("?") ? "&" : "?") + query;
    }
}
