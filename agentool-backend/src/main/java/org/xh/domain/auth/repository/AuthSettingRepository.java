package org.xh.domain.auth.repository;

import org.apache.ibatis.annotations.Mapper;
import org.xh.domain.auth.model.AuthSettingEntity;
import org.xh.infrastructure.repository.MyBatisPlusExtRepository;

/** 认证配置Repository接口 */
@Mapper
public interface AuthSettingRepository extends MyBatisPlusExtRepository<AuthSettingEntity> {
}