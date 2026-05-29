package org.xh.domain.rag.repository;

import org.apache.ibatis.annotations.Mapper;
import org.xh.domain.rag.model.UserRagFileEntity;
import org.xh.infrastructure.repository.MyBatisPlusExtRepository;

/** 用户RAG文件快照仓储接口
 * @author xh
 * @date 2025-07-22 <br/>
 */
@Mapper
public interface UserRagFileRepository extends MyBatisPlusExtRepository<UserRagFileEntity> {

}