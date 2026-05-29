package org.xh.domain.tool.repository;

import org.apache.ibatis.annotations.Mapper;
import org.xh.domain.tool.model.ToolVersionEntity;
import org.xh.domain.tool.model.UserToolEntity;
import org.xh.infrastructure.repository.MyBatisPlusExtRepository;

@Mapper
public interface UserToolRepository extends MyBatisPlusExtRepository<UserToolEntity> {
}
