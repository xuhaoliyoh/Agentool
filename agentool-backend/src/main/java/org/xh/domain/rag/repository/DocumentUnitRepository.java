package org.xh.domain.rag.repository;

import org.apache.ibatis.annotations.Mapper;
import org.xh.domain.rag.model.DocumentUnitEntity;
import org.xh.infrastructure.repository.MyBatisPlusExtRepository;

/** @author xh
 * @date 21:07 <br/>
 */
@Mapper
public interface DocumentUnitRepository extends MyBatisPlusExtRepository<DocumentUnitEntity> {

}
