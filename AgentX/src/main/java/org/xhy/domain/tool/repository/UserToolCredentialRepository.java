package org.xhy.domain.tool.repository;

import org.apache.ibatis.annotations.Mapper;
import org.xhy.domain.tool.model.UserToolCredentialEntity;
import org.xhy.infrastructure.repository.MyBatisPlusExtRepository;

/** 用户工具凭据仓储 */
@Mapper
public interface UserToolCredentialRepository extends MyBatisPlusExtRepository<UserToolCredentialEntity> {
}
