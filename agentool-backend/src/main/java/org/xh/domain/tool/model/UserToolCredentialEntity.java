package org.xh.domain.tool.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.xh.infrastructure.converter.MapConverter;
import org.xh.infrastructure.entity.BaseEntity;

import java.util.Map;

/** 用户安装工具后的私有鉴权配置。 */
@TableName(value = "user_tool_credentials", autoResultMap = true)
public class UserToolCredentialEntity extends BaseEntity {

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("user_tool_id")
    private String userToolId;

    @TableField("user_id")
    private String userId;

    @TableField("tool_id")
    private String toolId;

    @TableField(value = "auth_values", typeHandler = MapConverter.class)
    private Map<String, Object> authValues;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserToolId() {
        return userToolId;
    }

    public void setUserToolId(String userToolId) {
        this.userToolId = userToolId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToolId() {
        return toolId;
    }

    public void setToolId(String toolId) {
        this.toolId = toolId;
    }

    public Map<String, Object> getAuthValues() {
        return authValues;
    }

    public void setAuthValues(Map<String, Object> authValues) {
        this.authValues = authValues;
    }
}
