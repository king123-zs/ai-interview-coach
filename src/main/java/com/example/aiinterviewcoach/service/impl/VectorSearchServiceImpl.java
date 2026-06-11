package com.example.aiinterviewcoach.service.impl;

import com.example.aiinterviewcoach.common.ResultCode;
import com.example.aiinterviewcoach.dto.VectorSearchRequest;
import com.example.aiinterviewcoach.exception.BusinessException;
import com.example.aiinterviewcoach.mapper.KnowledgeBaseMapper;
import com.example.aiinterviewcoach.mapper.VectorSearchMapper;
import com.example.aiinterviewcoach.service.DashScopeEmbeddingClient;
import com.example.aiinterviewcoach.service.VectorSearchService;
import com.example.aiinterviewcoach.util.VectorUtils;
import com.example.aiinterviewcoach.vo.VectorSearchResultVO;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class VectorSearchServiceImpl implements VectorSearchService {

    private static final int DEFAULT_TOP_K = 5;

    private static final int MIN_TOP_K = 1;

    private static final int MAX_TOP_K = 20;

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    private final DashScopeEmbeddingClient dashScopeEmbeddingClient;

    private final VectorSearchMapper vectorSearchMapper;

    public VectorSearchServiceImpl(
            KnowledgeBaseMapper knowledgeBaseMapper,
            DashScopeEmbeddingClient dashScopeEmbeddingClient,
            VectorSearchMapper vectorSearchMapper) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.dashScopeEmbeddingClient = dashScopeEmbeddingClient;
        this.vectorSearchMapper = vectorSearchMapper;
    }

    @Override
    public List<VectorSearchResultVO> search(Long knowledgeBaseId, VectorSearchRequest request) {
        ensureKnowledgeBaseExists(knowledgeBaseId);

        if (request == null || !StringUtils.hasText(request.getQuery())) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }

        Integer topK = normalizeTopK(request.getTopK());
        String queryEmbedding = VectorUtils.toPgVectorString(dashScopeEmbeddingClient.embed(request.getQuery()));
        return vectorSearchMapper.searchByKnowledgeBaseId(knowledgeBaseId, queryEmbedding, topK);
    }

    private void ensureKnowledgeBaseExists(Long knowledgeBaseId) {
        if (knowledgeBaseMapper.selectById(knowledgeBaseId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
    }

    private Integer normalizeTopK(Integer topK) {
        if (topK == null) {
            return DEFAULT_TOP_K;
        }
        if (topK < MIN_TOP_K) {
            return MIN_TOP_K;
        }
        if (topK > MAX_TOP_K) {
            return MAX_TOP_K;
        }
        return topK;
    }
}
