package org.xh.domain.scheduledtask.repository;

import org.apache.ibatis.annotations.Mapper;
import org.xh.domain.scheduledtask.model.ScheduledTaskEntity;
import org.xh.infrastructure.repository.MyBatisPlusExtRepository;

/** 定时任务仓储接口 */
@Mapper
public interface ScheduledTaskRepository extends MyBatisPlusExtRepository<ScheduledTaskEntity> {
}