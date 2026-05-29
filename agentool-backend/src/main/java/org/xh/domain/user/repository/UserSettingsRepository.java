package org.xh.domain.user.repository;

import org.apache.ibatis.annotations.Mapper;
import org.xh.domain.user.model.UserSettingsEntity;
import org.xh.infrastructure.repository.MyBatisPlusExtRepository;

/** 用户设置仓储接口 */
@Mapper
public interface UserSettingsRepository extends MyBatisPlusExtRepository<UserSettingsEntity> {

}