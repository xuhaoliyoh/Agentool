package org.xhy.domain.tool.constant;

import org.xhy.infrastructure.exception.BusinessException;

/** 工具运行类型 */
public enum ToolRuntimeType {
    REMOTE_MCP_SSE,
    REMOTE_MCP_STREAMABLE_HTTP,
    HTTP_API;

    public static ToolRuntimeType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        for (ToolRuntimeType type : values()) {
            if (type.name().equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new BusinessException("未知的工具运行类型: " + code);
    }
}
