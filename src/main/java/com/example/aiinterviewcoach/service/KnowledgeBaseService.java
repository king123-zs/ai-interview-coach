package com.example.aiinterviewcoach.service;

import com.example.aiinterviewcoach.dto.CreateKnowledgeBaseRequest;
import com.example.aiinterviewcoach.dto.UpdateKnowledgeBaseRequest;
import com.example.aiinterviewcoach.vo.KnowledgeBaseVO;
import java.util.List;

public interface KnowledgeBaseService {

    Long create(CreateKnowledgeBaseRequest request);

    List<KnowledgeBaseVO> list();

    KnowledgeBaseVO getById(Long id);

    void update(Long id, UpdateKnowledgeBaseRequest request);

    void delete(Long id);
}
