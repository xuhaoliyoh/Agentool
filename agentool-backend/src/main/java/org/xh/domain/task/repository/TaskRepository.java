package org.xh.domain.task.repository;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.apache.ibatis.annotations.Mapper;
import org.xh.domain.task.model.TaskEntity;
import org.xh.infrastructure.repository.MyBatisPlusExtRepository;

import java.util.List;

/** 任务仓储接口 */
@Mapper
public interface TaskRepository extends MyBatisPlusExtRepository<TaskEntity> {

}