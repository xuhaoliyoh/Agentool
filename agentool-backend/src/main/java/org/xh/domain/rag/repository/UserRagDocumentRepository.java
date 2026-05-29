package org.xh.domain.rag.repository;

import org.apache.ibatis.annotations.Mapper;
import org.xh.domain.rag.model.UserRagDocumentEntity;
import org.xh.infrastructure.repository.MyBatisPlusExtRepository;

/** 用户RAG文档快照仓储接口
 * @author xh
 * @date 2025-07-22 <br/>
 */
@Mapper
public interface UserRagDocumentRepository extends MyBatisPlusExtRepository<UserRagDocumentEntity> {

}