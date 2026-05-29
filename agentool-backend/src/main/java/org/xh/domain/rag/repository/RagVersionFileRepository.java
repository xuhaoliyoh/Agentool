package org.xh.domain.rag.repository;

import org.apache.ibatis.annotations.Mapper;
import org.xh.domain.rag.model.RagVersionFileEntity;
import org.xh.infrastructure.repository.MyBatisPlusExtRepository;

/** RAG版本文件仓储接口
 * @author xh
 * @date 2025-07-16 <br/>
 */
@Mapper
public interface RagVersionFileRepository extends MyBatisPlusExtRepository<RagVersionFileEntity> {

}