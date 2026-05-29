package org.xh.infrastructure.converter;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.xh.domain.tool.constant.ToolRuntimeType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** 工具运行类型转换器 */
@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(ToolRuntimeType.class)
public class ToolRuntimeTypeConverter extends BaseTypeHandler<ToolRuntimeType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ToolRuntimeType parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public ToolRuntimeType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return ToolRuntimeType.fromCode(rs.getString(columnName));
    }

    @Override
    public ToolRuntimeType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return ToolRuntimeType.fromCode(rs.getString(columnIndex));
    }

    @Override
    public ToolRuntimeType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return ToolRuntimeType.fromCode(cs.getString(columnIndex));
    }
}
