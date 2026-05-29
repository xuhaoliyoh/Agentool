package org.xh.domain.rag.repository;

import org.apache.ibatis.annotations.Mapper;
import org.xh.domain.rag.model.RagVersionEntity;
import org.xh.infrastructure.repository.MyBatisPlusExtRepository;

/** RAG版本仓储接口
 * @author xh
 * @date 2025-07-16 <br/>
 */
@Mapper
public interface RagVersionRepository extends MyBatisPlusExtRepository<RagVersionEntity> {

}