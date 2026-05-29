package org.xh.application.tool.service.runtime;

import org.springframework.stereotype.Service;
import org.xh.domain.tool.constant.ToolRuntimeType;
import org.xh.infrastructure.exception.BusinessException;
import org.xh.interfaces.dto.tool.request.ToolRuntimeConfigRequest;

/** 工具端点探测服务 */
@Service
public class ToolEndpointProbeService {

    private final RemoteMcpProbeClient remoteMcpProbeClient;
    private final HttpApiProbeClient httpApiProbeClient;

    public ToolEndpointProbeService(RemoteMcpProbeClient remoteMcpProbeClient, HttpApiProbeClient httpApiProbeClient) {
        this.remoteMcpProbeClient = remoteMcpProbeClient;
        this.httpApiProbeClient = httpApiProbeClient;
    }

    public ToolEndpointProbeResult probe(ToolRuntimeConfigRequest request, String toolName, String description) {
        if (request == null || request.getRuntimeType() == null) {
            throw new BusinessException("工具运行配置不可为空");
        }
        if (request.getRuntimeType() == ToolRuntimeType.REMOTE_MCP_SSE) {
            return remoteMcpProbeClient.probe(request);
        }
        if (request.getRuntimeType() == ToolRuntimeType.HTTP_API) {
            return httpApiProbeClient.probe(request, toolName, description);
        }
        throw new BusinessException("暂不支持的工具运行类型: " + request.getRuntimeType());
    }
}
