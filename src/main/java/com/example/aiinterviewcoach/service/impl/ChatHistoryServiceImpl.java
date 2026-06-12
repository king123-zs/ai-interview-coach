package com.example.aiinterviewcoach.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiinterviewcoach.common.ResultCode;
import com.example.aiinterviewcoach.entity.ChatMessage;
import com.example.aiinterviewcoach.entity.ChatSession;
import com.example.aiinterviewcoach.exception.BusinessException;
import com.example.aiinterviewcoach.mapper.ChatMessageMapper;
import com.example.aiinterviewcoach.mapper.ChatSessionMapper;
import com.example.aiinterviewcoach.mapper.KnowledgeBaseMapper;
import com.example.aiinterviewcoach.service.ChatHistoryService;
import com.example.aiinterviewcoach.vo.ChatMessageVO;
import com.example.aiinterviewcoach.vo.ChatSessionVO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ChatHistoryServiceImpl implements ChatHistoryService {

    private static final int MAX_TITLE_LENGTH = 50;

    private static final Set<String> SUPPORTED_ROLES = Set.of("USER", "ASSISTANT");

    private final ChatSessionMapper chatSessionMapper;

    private final ChatMessageMapper chatMessageMapper;

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    public ChatHistoryServiceImpl(
            ChatSessionMapper chatSessionMapper,
            ChatMessageMapper chatMessageMapper,
            KnowledgeBaseMapper knowledgeBaseMapper) {
        this.chatSessionMapper = chatSessionMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
    }

    @Override
    public Long createSession(Long knowledgeBaseId, String firstQuestion) {
        ensureKnowledgeBaseExists(knowledgeBaseId);
        if (!StringUtils.hasText(firstQuestion)) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }

        LocalDateTime now = LocalDateTime.now();
        ChatSession session = new ChatSession();
        session.setKnowledgeBaseId(knowledgeBaseId);
        session.setTitle(buildTitle(firstQuestion));
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        chatSessionMapper.insert(session);
        return session.getId();
    }

    @Override
    @Transactional
    public void saveMessage(Long sessionId, String role, String content) {
        ChatSession session = getExistingSession(sessionId);
        if (!SUPPORTED_ROLES.contains(role) || !StringUtils.hasText(content)) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }

        LocalDateTime now = LocalDateTime.now();
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setCreatedAt(now);
        chatMessageMapper.insert(message);

        session.setUpdatedAt(now);
        chatSessionMapper.updateById(session);
    }

    @Override
    public List<ChatSessionVO> listSessions(Long knowledgeBaseId) {
        LambdaQueryWrapper<ChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatSession::getKnowledgeBaseId, knowledgeBaseId);
        queryWrapper.orderByDesc(ChatSession::getUpdatedAt);
        return chatSessionMapper.selectList(queryWrapper)
                .stream()
                .map(this::toSessionVO)
                .toList();
    }

    @Override
    public List<ChatMessageVO> listMessages(Long sessionId) {
        getExistingSession(sessionId);

        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatMessage::getSessionId, sessionId);
        queryWrapper.orderByAsc(ChatMessage::getCreatedAt);
        queryWrapper.orderByAsc(ChatMessage::getId);
        return chatMessageMapper.selectList(queryWrapper)
                .stream()
                .map(this::toMessageVO)
                .toList();
    }

    @Override
    @Transactional
    public void deleteSession(Long sessionId) {
        getExistingSession(sessionId);

        LambdaQueryWrapper<ChatMessage> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ChatMessage::getSessionId, sessionId);
        chatMessageMapper.delete(deleteWrapper);
        chatSessionMapper.deleteById(sessionId);
    }

    private void ensureKnowledgeBaseExists(Long knowledgeBaseId) {
        if (knowledgeBaseMapper.selectById(knowledgeBaseId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
    }

    private ChatSession getExistingSession(Long sessionId) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return session;
    }

    private String buildTitle(String firstQuestion) {
        String title = firstQuestion.trim();
        if (title.length() <= MAX_TITLE_LENGTH) {
            return title;
        }
        return title.substring(0, MAX_TITLE_LENGTH);
    }

    private ChatSessionVO toSessionVO(ChatSession session) {
        ChatSessionVO vo = new ChatSessionVO();
        vo.setId(session.getId());
        vo.setKnowledgeBaseId(session.getKnowledgeBaseId());
        vo.setTitle(session.getTitle());
        vo.setCreatedAt(session.getCreatedAt());
        vo.setUpdatedAt(session.getUpdatedAt());
        return vo;
    }

    private ChatMessageVO toMessageVO(ChatMessage message) {
        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(message.getId());
        vo.setSessionId(message.getSessionId());
        vo.setRole(message.getRole());
        vo.setContent(message.getContent());
        vo.setCreatedAt(message.getCreatedAt());
        return vo;
    }
}
