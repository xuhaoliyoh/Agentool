package org.xh.domain.rag.repository;

import org.apache.ibatis.annotations.Mapper;
import org.xh.domain.rag.model.UserRagEntity;
import org.xh.infrastructure.repository.MyBatisPlusExtRepository;

/** 用户安装的RAG仓储接口
 * @author xh
 * @date 2025-07-16 <br/>
 */
@Mapper
public interface UserRagRepository extends MyBatisPlusExtRepository<UserRagEntity> {

}