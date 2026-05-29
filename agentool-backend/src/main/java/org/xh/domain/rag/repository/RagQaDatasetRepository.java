package org.xh.domain.rag.repository;

import org.apache.ibatis.annotations.Mapper;
import org.xh.domain.rag.model.FileDetailEntity;
import org.xh.domain.rag.model.RagQaDatasetEntity;
import org.xh.infrastructure.repository.MyBatisPlusExtRepository;

/** @author xh
 * @date 17:44 <br/>
 */
@Mapper
public interface RagQaDatasetRepository extends MyBatisPlusExtRepository<RagQaDatasetEntity> {

}
