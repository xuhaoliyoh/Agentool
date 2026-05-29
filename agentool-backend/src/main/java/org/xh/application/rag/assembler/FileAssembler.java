package org.xh.application.rag.assembler;

import org.xh.domain.rag.model.FileDetailEntity;
import org.xh.interfaces.dto.rag.RagUploadRequest;

/** @author xh
 * @date 11:09 <br/>
 */
public class FileAssembler {

    /** 将CreateAgentRequest转换为AgentEntity */
    public static FileDetailEntity toEntity(RagUploadRequest request) {

        FileDetailEntity fileDetailEntity = new FileDetailEntity();
        fileDetailEntity.setMultipartFile(request.getFile());
        fileDetailEntity.setDataSetId(request.getDataSetId());

        return fileDetailEntity;
    }
}
