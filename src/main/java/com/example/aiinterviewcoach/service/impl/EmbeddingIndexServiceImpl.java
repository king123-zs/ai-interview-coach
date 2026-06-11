package com.example.aiinterviewcoach.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiinterviewcoach.common.ResultCode;
import com.example.aiinterviewcoach.entity.Document;
import com.example.aiinterviewcoach.entity.DocumentChunk;
import com.example.aiinterviewcoach.entity.DocumentChunkEmbedding;
import com.example.aiinterviewcoach.exception.BusinessException;
import com.example.aiinterviewcoach.mapper.DocumentChunkEmbeddingMapper;
import com.example.aiinterviewcoach.mapper.DocumentChunkMapper;
import com.example.aiinterviewcoach.mapper.DocumentMapper;
import com.example.aiinterviewcoach.service.DashScopeEmbeddingClient;
import com.example.aiinterviewcoach.service.EmbeddingIndexService;
import com.example.aiinterviewcoach.util.VectorUtils;
import com.example.aiinterviewcoach.vo.DocumentChunkEmbeddingVO;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingIndexServiceImpl implements EmbeddingIndexService {

    private static final String STATUS_INDEXED = "INDEXED";

    private static final String STATUS_FAILED = "FAILED";

    private final DocumentMapper documentMapper;

    private final DocumentChunkMapper documentChunkMapper;

    private final DocumentChunkEmbeddingMapper documentChunkEmbeddingMapper;

    private final DashScopeEmbeddingClient dashScopeEmbeddingClient;

    public EmbeddingIndexServiceImpl(
            DocumentMapper documentMapper,
            DocumentChunkMapper documentChunkMapper,
            DocumentChunkEmbeddingMapper documentChunkEmbeddingMapper,
            DashScopeEmbeddingClient dashScopeEmbeddingClient) {
        this.documentMapper = documentMapper;
        this.documentChunkMapper = documentChunkMapper;
        this.documentChunkEmbeddingMapper = documentChunkEmbeddingMapper;
        this.dashScopeEmbeddingClient = dashScopeEmbeddingClient;
    }

    @Override
    public List<DocumentChunkEmbeddingVO> indexDocument(Long documentId) {
        Document document = getExistingDocument(documentId);
        List<DocumentChunk> chunks = listDocumentChunks(documentId);
        if (chunks.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "please parse document before indexing");
        }

        try {
            documentChunkEmbeddingMapper.deleteByDocumentId(documentId);

            for (DocumentChunk chunk : chunks) {
                List<Double> vector = dashScopeEmbeddingClient.embed(chunk.getContent());

                DocumentChunkEmbedding embedding = new DocumentChunkEmbedding();
                embedding.setChunkId(chunk.getId());
                embedding.setDocumentId(chunk.getDocumentId());
                embedding.setKnowledgeBaseId(chunk.getKnowledgeBaseId());
                embedding.setContent(chunk.getContent());
                embedding.setEmbedding(VectorUtils.toPgVectorString(vector));
                embedding.setEmbeddingModel(dashScopeEmbeddingClient.getEmbeddingModel());

                documentChunkEmbeddingMapper.insertEmbedding(embedding);
            }

            updateDocumentStatus(document, STATUS_INDEXED);
            return listEmbeddings(documentId);
        } catch (RuntimeException exception) {
            updateDocumentStatus(document, STATUS_FAILED);
            if (exception instanceof BusinessException businessException) {
                throw businessException;
            }
            throw new BusinessException(ResultCode.INTERNAL_ERROR.getCode(), "failed to index document embeddings");
        }
    }

    @Override
    public List<DocumentChunkEmbeddingVO> listEmbeddings(Long documentId) {
        getExistingDocument(documentId);
        return documentChunkEmbeddingMapper.listByDocumentId(documentId)
                .stream()
                .map(this::toVO)
                .toList();
    }

    private Document getExistingDocument(Long documentId) {
        Document document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return document;
    }

    private List<DocumentChunk> listDocumentChunks(Long documentId) {
        LambdaQueryWrapper<DocumentChunk> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocumentChunk::getDocumentId, documentId);
        queryWrapper.orderByAsc(DocumentChunk::getChunkIndex);
        return documentChunkMapper.selectList(queryWrapper);
    }

    private void updateDocumentStatus(Document document, String status) {
        document.setStatus(status);
        document.setUpdatedAt(LocalDateTime.now());
        documentMapper.updateById(document);
    }

    private DocumentChunkEmbeddingVO toVO(DocumentChunkEmbedding embedding) {
        DocumentChunkEmbeddingVO vo = new DocumentChunkEmbeddingVO();
        vo.setId(embedding.getId());
        vo.setChunkId(embedding.getChunkId());
        vo.setDocumentId(embedding.getDocumentId());
        vo.setKnowledgeBaseId(embedding.getKnowledgeBaseId());
        vo.setContent(embedding.getContent());
        vo.setEmbeddingModel(embedding.getEmbeddingModel());
        vo.setCreatedAt(embedding.getCreatedAt());
        return vo;
    }
}
