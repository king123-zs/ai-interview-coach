package com.example.aiinterviewcoach.service.impl;

import com.example.aiinterviewcoach.common.ResultCode;
import com.example.aiinterviewcoach.dto.RagStreamChatRequest;
import com.example.aiinterviewcoach.dto.VectorSearchRequest;
import com.example.aiinterviewcoach.exception.BusinessException;
import com.example.aiinterviewcoach.mapper.KnowledgeBaseMapper;
import com.example.aiinterviewcoach.service.ChatHistoryService;
import com.example.aiinterviewcoach.service.DeepSeekStreamClient;
import com.example.aiinterviewcoach.service.RagStreamChatService;
import com.example.aiinterviewcoach.service.VectorSearchService;
import com.example.aiinterviewcoach.vo.ReferenceVO;
import com.example.aiinterviewcoach.vo.VectorSearchResultVO;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class RagStreamChatServiceImpl implements RagStreamChatService {

    private static final long SSE_TIMEOUT_MILLIS = 5 * 60 * 1000L;

    private static final int DEFAULT_TOP_K = 5;

    private static final int MIN_TOP_K = 1;

    private static final int MAX_TOP_K = 20;

    private static final String INSUFFICIENT_CONTEXT_ANSWER = "当前知识库资料不足，无法基于资料回答该问题。";

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    private final VectorSearchService vectorSearchService;

    private final ChatHistoryService chatHistoryService;

    private final DeepSeekStreamClient deepSeekStreamClient;

    private final ExecutorService streamExecutor = Executors.newFixedThreadPool(4);

    public RagStreamChatServiceImpl(
            KnowledgeBaseMapper knowledgeBaseMapper,
            VectorSearchService vectorSearchService,
            ChatHistoryService chatHistoryService,
            DeepSeekStreamClient deepSeekStreamClient) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.vectorSearchService = vectorSearchService;
        this.chatHistoryService = chatHistoryService;
        this.deepSeekStreamClient = deepSeekStreamClient;
    }

    @Override
    public SseEmitter streamChat(Long knowledgeBaseId, RagStreamChatRequest request) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        streamExecutor.execute(() -> executeStreamChat(emitter, knowledgeBaseId, request));
        return emitter;
    }

    private void executeStreamChat(
            SseEmitter emitter,
            Long knowledgeBaseId,
            RagStreamChatRequest request) {
        try {
            ensureKnowledgeBaseExists(knowledgeBaseId);
            validateRequest(request);

            Long sessionId = request.getSessionId() == null
                    ? chatHistoryService.createSession(knowledgeBaseId, request.getQuestion())
                    : request.getSessionId();
            chatHistoryService.saveMessage(sessionId, "USER", request.getQuestion());

            List<VectorSearchResultVO> searchResults = vectorSearchService.search(
                    knowledgeBaseId,
                    buildVectorSearchRequest(request)
            );
            List<ReferenceVO> references = toReferences(searchResults);

            sendEvent(emitter, "session", Map.of("sessionId", sessionId));
            sendEvent(emitter, "references", references);

            if (searchResults.isEmpty()) {
                sendEvent(emitter, "message", INSUFFICIENT_CONTEXT_ANSWER);
                chatHistoryService.saveMessage(sessionId, "ASSISTANT", INSUFFICIENT_CONTEXT_ANSWER);
                completeStream(emitter);
                return;
            }

            String context = buildContext(searchResults);
            String prompt = buildPrompt(context, request.getQuestion());
            StringBuilder fullAnswer = new StringBuilder();

            deepSeekStreamClient.streamChat(prompt, token -> {
                fullAnswer.append(token);
                sendEvent(emitter, "message", token);
            });

            chatHistoryService.saveMessage(sessionId, "ASSISTANT", fullAnswer.toString());
            completeStream(emitter);
        } catch (Exception exception) {
            handleStreamError(emitter, exception);
        }
    }

    private void ensureKnowledgeBaseExists(Long knowledgeBaseId) {
        if (knowledgeBaseMapper.selectById(knowledgeBaseId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
    }

    private void validateRequest(RagStreamChatRequest request) {
        if (request == null || !StringUtils.hasText(request.getQuestion())) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }
    }

    private VectorSearchRequest buildVectorSearchRequest(RagStreamChatRequest request) {
        VectorSearchRequest searchRequest = new VectorSearchRequest();
        searchRequest.setQuery(request.getQuestion());
        searchRequest.setTopK(normalizeTopK(request.getTopK()));
        return searchRequest;
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
        if (searchResults.isEmpty()) {
            return Collections.emptyList();
        }
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

    private void sendEvent(SseEmitter emitter, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
        } catch (IOException exception) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR.getCode(), "failed to send SSE event");
        }
    }

    private void completeStream(SseEmitter emitter) {
        sendEvent(emitter, "done", "[DONE]");
        emitter.complete();
    }

    private void handleStreamError(SseEmitter emitter, Exception exception) {
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(Map.of("message", getErrorMessage(exception))));
        } catch (IOException ignored) {
        }
        emitter.completeWithError(exception);
    }

    private String getErrorMessage(Exception exception) {
        if (StringUtils.hasText(exception.getMessage())) {
            return exception.getMessage();
        }
        return ResultCode.INTERNAL_ERROR.getMessage();
    }

    @PreDestroy
    public void shutdownExecutor() {
        streamExecutor.shutdown();
    }
}
