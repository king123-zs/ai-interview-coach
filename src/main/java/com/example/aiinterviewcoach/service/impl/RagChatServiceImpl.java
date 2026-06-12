package com.example.aiinterviewcoach.service.impl;

import com.example.aiinterviewcoach.common.ResultCode;
import com.example.aiinterviewcoach.dto.RagChatRequest;
import com.example.aiinterviewcoach.dto.VectorSearchRequest;
import com.example.aiinterviewcoach.exception.BusinessException;
import com.example.aiinterviewcoach.mapper.KnowledgeBaseMapper;
import com.example.aiinterviewcoach.service.ChatHistoryService;
import com.example.aiinterviewcoach.service.DeepSeekChatClient;
import com.example.aiinterviewcoach.service.RagChatService;
import com.example.aiinterviewcoach.service.VectorSearchService;
import com.example.aiinterviewcoach.vo.RagChatResponse;
import com.example.aiinterviewcoach.vo.ReferenceVO;
import com.example.aiinterviewcoach.vo.VectorSearchResultVO;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class RagChatServiceImpl implements RagChatService {

    private static final int DEFAULT_TOP_K = 5;

    private static final String INSUFFICIENT_CONTEXT_ANSWER = "当前知识库资料不足，无法基于资料回答该问题。";

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    private final VectorSearchService vectorSearchService;

    private final DeepSeekChatClient deepSeekChatClient;

    private final ChatHistoryService chatHistoryService;

    public RagChatServiceImpl(
            KnowledgeBaseMapper knowledgeBaseMapper,
            VectorSearchService vectorSearchService,
            DeepSeekChatClient deepSeekChatClient,
            ChatHistoryService chatHistoryService) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.vectorSearchService = vectorSearchService;
        this.deepSeekChatClient = deepSeekChatClient;
        this.chatHistoryService = chatHistoryService;
    }

    @Override
    public RagChatResponse chat(Long knowledgeBaseId, RagChatRequest request) {
        ensureKnowledgeBaseExists(knowledgeBaseId);

        if (request == null || !StringUtils.hasText(request.getQuestion())) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }

        Long sessionId = request.getSessionId() == null
                ? chatHistoryService.createSession(knowledgeBaseId, request.getQuestion())
                : request.getSessionId();
        chatHistoryService.saveMessage(sessionId, "USER", request.getQuestion());

        List<VectorSearchResultVO> searchResults = vectorSearchService.search(
                knowledgeBaseId,
                buildVectorSearchRequest(request)
        );

        if (searchResults.isEmpty()) {
            chatHistoryService.saveMessage(sessionId, "ASSISTANT", INSUFFICIENT_CONTEXT_ANSWER);
            return buildResponse(sessionId, INSUFFICIENT_CONTEXT_ANSWER, Collections.emptyList());
        }

        String context = buildContext(searchResults);
        String prompt = buildPrompt(context, request.getQuestion());
        String answer = deepSeekChatClient.chat(prompt);
        chatHistoryService.saveMessage(sessionId, "ASSISTANT", answer);

        return buildResponse(sessionId, answer, toReferences(searchResults));
    }

    private void ensureKnowledgeBaseExists(Long knowledgeBaseId) {
        if (knowledgeBaseMapper.selectById(knowledgeBaseId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
    }

    private VectorSearchRequest buildVectorSearchRequest(RagChatRequest request) {
        VectorSearchRequest vectorSearchRequest = new VectorSearchRequest();
        vectorSearchRequest.setQuery(request.getQuestion());
        vectorSearchRequest.setTopK(request.getTopK() == null ? DEFAULT_TOP_K : request.getTopK());
        return vectorSearchRequest;
    }

    private String buildContext(List<VectorSearchResultVO> searchResults) {
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < searchResults.size(); i++) {
            VectorSearchResultVO result = searchResults.get(i);
            context.append("资料片段 ").append(i + 1).append("：\n")
                    .append(result.getContent())
                    .append("\n\n");
        }
        return context.toString();
    }

    private String buildPrompt(String context, String question) {
        return """
                你是一个专业的 Java 面试辅导老师。

                请严格根据【参考资料】回答用户问题。
                如果参考资料中没有相关内容，请回答：当前知识库资料不足，无法基于资料回答该问题。
                不要编造参考资料中没有的信息。

                【参考资料】
                %s

                【用户问题】
                %s

                回答要求：
                1. 先给出核心结论。
                2. 再分点解释。
                3. 尽量使用适合面试表达的语言。
                4. 如果资料不足，要明确说明资料不足。
                """.formatted(context, question);
    }

    private List<ReferenceVO> toReferences(List<VectorSearchResultVO> searchResults) {
        return searchResults.stream()
                .map(this::toReference)
                .collect(Collectors.toList());
    }

    private ReferenceVO toReference(VectorSearchResultVO result) {
        ReferenceVO reference = new ReferenceVO();
        reference.setChunkId(result.getChunkId());
        reference.setDocumentId(result.getDocumentId());
        reference.setKnowledgeBaseId(result.getKnowledgeBaseId());
        reference.setContent(result.getContent());
        reference.setScore(result.getScore());
        return reference;
    }

    private RagChatResponse buildResponse(Long sessionId, String answer, List<ReferenceVO> references) {
        RagChatResponse response = new RagChatResponse();
        response.setSessionId(sessionId);
        response.setAnswer(answer);
        response.setReferences(references);
        return response;
    }
}
