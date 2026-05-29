package org.xh.domain.rag.repository;

import org.apache.ibatis.annotations.Mapper;
import org.xh.domain.rag.model.FileDetailEntity;
import org.xh.infrastructure.repository.MyBatisPlusExtRepository;

/** 文件详情仓库接口
 * @author xh*/
@Mapper
public interface FileDetailRepository extends MyBatisPlusExtRepository<FileDetailEntity> {

}