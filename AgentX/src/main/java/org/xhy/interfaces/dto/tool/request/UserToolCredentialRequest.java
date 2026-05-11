package org.xhy.interfaces.dto.tool.request;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

/** 用户工具凭据保存请求 */
public class UserToolCredentialRequest {

    @NotNull(message = "鉴权配置不可为空")
    private Map<String, Object> authValues;

    public Map<String, Object> getAuthValues() {
        return authValues;
    }

    public void setAuthValues(Map<String, Object> authValues) {
        this.authValues = authValues;
    }
}
