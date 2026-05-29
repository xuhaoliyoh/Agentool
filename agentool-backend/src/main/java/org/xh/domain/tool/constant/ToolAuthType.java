package org.xh.domain.tool.constant;

import org.xh.infrastructure.exception.BusinessException;

/** 工具鉴权类型 */
public enum ToolAuthType {
    NONE,
    BEARER_TOKEN,
    API_KEY_HEADER,
    API_KEY_QUERY,
    BASIC;

    public static ToolAuthType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return NONE;
        }
        for (ToolAuthType type : values()) {
            if (type.name().equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new BusinessException("未知的工具鉴权类型: " + code);
    }
}
