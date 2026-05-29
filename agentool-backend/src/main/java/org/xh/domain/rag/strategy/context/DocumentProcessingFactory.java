package org.xh.domain.rag.strategy.context;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.xh.domain.rag.model.enums.DocumentProcessingType;
import org.xh.domain.rag.strategy.DocumentProcessingStrategy;

import jakarta.annotation.Resource;

/** @author xh
 * @date 09:39 <br/>
 */
@Service
public class DocumentProcessingFactory {

    @Resource
    private Map<String, DocumentProcessingStrategy> documentProcessingStrategyMap;

    public DocumentProcessingStrategy getDocumentStrategyHandler(String strategy) {
        return documentProcessingStrategyMap.get(DocumentProcessingType.getLabelByValue(strategy));
    }
}
