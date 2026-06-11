package com.example.aiinterviewcoach.service;

import com.example.aiinterviewcoach.dto.VectorSearchRequest;
import com.example.aiinterviewcoach.vo.VectorSearchResultVO;
import java.util.List;

public interface VectorSearchService {

    List<VectorSearchResultVO> search(Long knowledgeBaseId, VectorSearchRequest request);
}
