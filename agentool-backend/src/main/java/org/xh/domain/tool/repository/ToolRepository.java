package org.xh.domain.tool.repository;

import org.apache.ibatis.annotations.Mapper;
import org.xh.domain.tool.model.ToolEntity;
import org.xh.infrastructure.repository.MyBatisPlusExtRepository;

/** 工具仓储接口 */
@Mapper
public interface ToolRepository extends MyBatisPlusExtRepository<ToolEntity> {
}