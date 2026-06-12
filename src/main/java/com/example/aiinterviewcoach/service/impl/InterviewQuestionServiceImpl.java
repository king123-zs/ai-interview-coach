package com.example.aiinterviewcoach.service.impl;

import com.example.aiinterviewcoach.common.ResultCode;
import com.example.aiinterviewcoach.dto.GenerateInterviewQuestionRequest;
import com.example.aiinterviewcoach.dto.VectorSearchRequest;
import com.example.aiinterviewcoach.exception.BusinessException;
import com.example.aiinterviewcoach.mapper.KnowledgeBaseMapper;
import com.example.aiinterviewcoach.service.DeepSeekChatClient;
import com.example.aiinterviewcoach.service.InterviewQuestionService;
import com.example.aiinterviewcoach.service.VectorSearchService;
import com.example.aiinterviewcoach.vo.GenerateInterviewQuestionResponse;
import com.example.aiinterviewcoach.vo.ReferenceVO;
import com.example.aiinterviewcoach.vo.VectorSearchResultVO;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class InterviewQuestionServiceImpl implements InterviewQuestionService {

    private static final String DEFAULT_DIFFICULTY = "medium";

    private static final int DEFAULT_COUNT = 5;

    private static final int DEFAULT_TOP_K = 5;

    private static final int MIN_VALUE = 1;

    private static final int MAX_VALUE = 20;

    private static final String INSUFFICIENT_CONTEXT_TEXT = "当前知识库资料不足，无法基于资料生成面试题。";

    private static final Set<String> SUPPORTED_DIFFICULTIES = Set.of("easy", "medium", "hard");

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    private final VectorSearchService vectorSearchService;

    private final DeepSeekChatClient deepSeekChatClient;

    public InterviewQuestionServiceImpl(
            KnowledgeBaseMapper knowledgeBaseMapper,
            VectorSearchService vectorSearchService,
            DeepSeekChatClient deepSeekChatClient) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.vectorSearchService = vectorSearchService;
        this.deepSeekChatClient = deepSeekChatClient;
    }

    @Override
    public GenerateInterviewQuestionResponse generate(
            Long knowledgeBaseId,
            GenerateInterviewQuestionRequest request) {
        ensureKnowledgeBaseExists(knowledgeBaseId);

        if (request == null || !StringUtils.hasText(request.getTopic())) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }

        String topic = request.getTopic().trim();
        String difficulty = normalizeDifficulty(request.getDifficulty());
        Integer count = normalizeNumber(request.getCount(), DEFAULT_COUNT);
        Integer topK = normalizeNumber(request.getTopK(), DEFAULT_TOP_K);

        List<VectorSearchResultVO> searchResults = vectorSearchService.search(
                knowledgeBaseId,
                buildVectorSearchRequest(topic, topK)
        );

        if (searchResults.isEmpty()) {
            return buildResponse(topic, difficulty, count, INSUFFICIENT_CONTEXT_TEXT, Collections.emptyList());
        }

        String context = buildContext(searchResults);
        String prompt = buildPrompt(topic, difficulty, count, context);
        String questionsText = deepSeekChatClient.chat(prompt);

        return buildResponse(topic, difficulty, count, questionsText, toReferences(searchResults));
    }

    private void ensureKnowledgeBaseExists(Long knowledgeBaseId) {
        if (knowledgeBaseMapper.selectById(knowledgeBaseId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
    }

    private String normalizeDifficulty(String difficulty) {
        if (!StringUtils.hasText(difficulty)) {
            return DEFAULT_DIFFICULTY;
        }

        String normalizedDifficulty = difficulty.trim().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_DIFFICULTIES.contains(normalizedDifficulty)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "difficulty must be easy, medium, or hard");
        }
        return normalizedDifficulty;
    }

    private Integer normalizeNumber(Integer value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value < MIN_VALUE) {
            return MIN_VALUE;
        }
        if (value > MAX_VALUE) {
            return MAX_VALUE;
        }
        return value;
    }

    private VectorSearchRequest buildVectorSearchRequest(String topic, Integer topK) {
        VectorSearchRequest request = new VectorSearchRequest();
        request.setQuery(topic);
        request.setTopK(topK);
        return request;
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

    private String buildPrompt(String topic, String difficulty, Integer count, String context) {
        return """
                你是一个专业的 Java 后端面试官和面试辅导老师。

                请严格根据【参考资料】围绕【面试主题】生成面试题。
                不要编造参考资料中完全没有的信息。
                如果资料不足，请明确说明资料不足。

                【面试主题】
                %s

                【难度】
                %s

                【题目数量】
                %d

                【参考资料】
                %s

                请按下面格式输出：

                # %s 面试题

                ## 1. 题目
                问题：...
                考察点：...
                参考答案：...
                追问：...

                ## 2. 题目
                问题：...
                考察点：...
                参考答案：...
                追问：...

                要求：
                1. 问题要适合 Java 后端面试。
                2. 参考答案要简洁但完整。
                3. 每道题都要有考察点。
                4. 每道题都给一个追问。
                5. 如果 difficulty = easy，偏基础概念。
                6. 如果 difficulty = medium，加入原理和对比。
                7. 如果 difficulty = hard，加入源码、场景和性能问题。
                """.formatted(topic, difficulty, count, context, topic);
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

    private GenerateInterviewQuestionResponse buildResponse(
            String topic,
            String difficulty,
            Integer count,
            String questionsText,
            List<ReferenceVO> references) {
        GenerateInterviewQuestionResponse response = new GenerateInterviewQuestionResponse();
        response.setTopic(topic);
        response.setDifficulty(difficulty);
        response.setCount(count);
        response.setQuestionsText(questionsText);
        response.setReferences(references);
        return response;
    }
}
