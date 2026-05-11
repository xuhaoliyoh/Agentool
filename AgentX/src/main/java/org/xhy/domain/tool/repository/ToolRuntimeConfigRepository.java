package org.xhy.domain.tool.repository;

import org.apache.ibatis.annotations.Mapper;
import org.xhy.domain.tool.model.ToolRuntimeConfigEntity;
import org.xhy.infrastructure.repository.MyBatisPlusExtRepository;

/** 工具运行配置仓储 */
@Mapper
public interface ToolRuntimeConfigRepository extends MyBatisPlusExtRepository<ToolRuntimeConfigEntity> {
}
