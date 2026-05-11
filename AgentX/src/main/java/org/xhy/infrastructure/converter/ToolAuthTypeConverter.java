package org.xhy.infrastructure.converter;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.xhy.domain.tool.constant.ToolAuthType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** 工具鉴权类型转换器 */
@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(ToolAuthType.class)
public class ToolAuthTypeConverter extends BaseTypeHandler<ToolAuthType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ToolAuthType parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public ToolAuthType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return ToolAuthType.fromCode(rs.getString(columnName));
    }

    @Override
    public ToolAuthType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return ToolAuthType.fromCode(rs.getString(columnIndex));
    }

    @Override
    public ToolAuthType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return ToolAuthType.fromCode(cs.getString(columnIndex));
    }
}
