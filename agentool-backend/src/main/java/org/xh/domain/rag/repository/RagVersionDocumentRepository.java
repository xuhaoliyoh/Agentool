package org.xh.domain.rag.repository;

import org.apache.ibatis.annotations.Mapper;
import org.xh.domain.rag.model.RagVersionDocumentEntity;
import org.xh.infrastructure.repository.MyBatisPlusExtRepository;

/** RAG版本文档单元仓储接口
 * @author xh
 * @date 2025-07-16 <br/>
 */
@Mapper
public interface RagVersionDocumentRepository extends MyBatisPlusExtRepository<RagVersionDocumentEntity> {

}