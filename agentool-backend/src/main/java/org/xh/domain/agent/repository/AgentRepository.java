package org.xh.domain.agent.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xh.domain.agent.model.AgentEntity;
import org.xh.infrastructure.repository.MyBatisPlusExtRepository;

/** Agent仓库接口 */
@Mapper
public interface AgentRepository extends MyBatisPlusExtRepository<AgentEntity> {
}