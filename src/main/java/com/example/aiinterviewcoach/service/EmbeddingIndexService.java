package com.example.aiinterviewcoach.service;

import com.example.aiinterviewcoach.vo.DocumentChunkEmbeddingVO;
import java.util.List;

public interface EmbeddingIndexService {

    List<DocumentChunkEmbeddingVO> indexDocument(Long documentId);

    List<DocumentChunkEmbeddingVO> listEmbeddings(Long documentId);
}
