package org.xh.domain.llm.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xh.domain.llm.model.ProviderEntity;
import org.xh.infrastructure.repository.MyBatisPlusExtRepository;

/** 服务提供商仓储接口 */
@Mapper
public interface ProviderRepository extends MyBatisPlusExtRepository<ProviderEntity> {

}