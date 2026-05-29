package org.xh.domain.tool.repository;

import org.apache.ibatis.annotations.Mapper;
import org.xh.domain.tool.model.ToolRuntimeConfigEntity;
import org.xh.infrastructure.repository.MyBatisPlusExtRepository;

/** 工具运行配置仓储 */
@Mapper
public interface ToolRuntimeConfigRepository extends MyBatisPlusExtRepository<ToolRuntimeConfigEntity> {
}
