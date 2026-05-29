package org.xh.domain.tool.repository;

import org.apache.ibatis.annotations.Mapper;
import org.xh.domain.tool.model.UserToolCredentialEntity;
import org.xh.infrastructure.repository.MyBatisPlusExtRepository;

/** 用户工具凭据仓储 */
@Mapper
public interface UserToolCredentialRepository extends MyBatisPlusExtRepository<UserToolCredentialEntity> {
}
