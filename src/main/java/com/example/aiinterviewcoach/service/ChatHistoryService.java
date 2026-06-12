package com.example.aiinterviewcoach.service;

import com.example.aiinterviewcoach.vo.ChatMessageVO;
import com.example.aiinterviewcoach.vo.ChatSessionVO;
import java.util.List;

public interface ChatHistoryService {

    Long createSession(Long knowledgeBaseId, String firstQuestion);

    void saveMessage(Long sessionId, String role, String content);

    List<ChatSessionVO> listSessions(Long knowledgeBaseId);

    List<ChatMessageVO> listMessages(Long sessionId);

    void deleteSession(Long sessionId);
}
