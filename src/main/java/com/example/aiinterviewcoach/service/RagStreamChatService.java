package com.example.aiinterviewcoach.service;

import com.example.aiinterviewcoach.dto.RagStreamChatRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface RagStreamChatService {

    SseEmitter streamChat(Long knowledgeBaseId, RagStreamChatRequest request);
}
