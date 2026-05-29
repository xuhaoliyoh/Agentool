package org.xh.application.rag.assembler;

import org.springframework.beans.BeanUtils;
import org.xh.application.rag.dto.FileDetailInfoDTO;
import org.xh.domain.rag.model.FileDetailEntity;

/** 文件详细信息转换器
 * 
 * @author xh*/
public class FileDetailInfoAssembler {

    /** Convert FileDetailEntity to FileDetailInfoDTO */
    public static FileDetailInfoDTO toDTO(FileDetailEntity entity) {
        if (entity == null) {
            return null;
        }
        FileDetailInfoDTO dto = new FileDetailInfoDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setFileId(entity.getId());
        return dto;
    }
}