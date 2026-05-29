package org.xh.domain.user.repository;

import org.apache.ibatis.annotations.Mapper;
import org.xh.domain.user.model.UserEntity;
import org.xh.infrastructure.repository.MyBatisPlusExtRepository;

/** 模型仓储接口 */
@Mapper
public interface UserRepository extends MyBatisPlusExtRepository<UserEntity> {

}