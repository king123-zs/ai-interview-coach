package com.example.aiinterviewcoach.service;

import com.example.aiinterviewcoach.dto.RagChatRequest;
import com.example.aiinterviewcoach.vo.RagChatResponse;

public interface RagChatService {

    RagChatResponse chat(Long knowledgeBaseId, RagChatRequest request);
}
