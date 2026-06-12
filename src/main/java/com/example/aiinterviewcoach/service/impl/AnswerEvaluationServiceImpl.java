package com.example.aiinterviewcoach.service.impl;

import com.example.aiinterviewcoach.common.ResultCode;
import com.example.aiinterviewcoach.dto.EvaluateAnswerRequest;
import com.example.aiinterviewcoach.dto.VectorSearchRequest;
import com.example.aiinterviewcoach.exception.BusinessException;
import com.example.aiinterviewcoach.mapper.KnowledgeBaseMapper;
import com.example.aiinterviewcoach.service.AnswerEvaluationService;
import com.example.aiinterviewcoach.service.DeepSeekChatClient;
import com.example.aiinterviewcoach.service.VectorSearchService;
import com.example.aiinterviewcoach.vo.EvaluateAnswerResponse;
import com.example.aiinterviewcoach.vo.ReferenceVO;
import com.example.aiinterviewcoach.vo.VectorSearchResultVO;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AnswerEvaluationServiceImpl implements AnswerEvaluationService {

    private static final int DEFAULT_TOP_K = 5;

    private static final int MIN_TOP_K = 1;

    private static final int MAX_TOP_K = 20;

    private static final String EMPTY_REFERENCE_ANSWER = "未提供";

    private static final String EMPTY_CONTEXT =
            "当前知识库没有检索到相关资料，请主要根据题目和参考答案评价，并提示资料不足。";

    private static final Pattern SCORE_PATTERN =
            Pattern.compile("总分\\s*[:：]\\s*(\\d{1,3})\\s*/\\s*100");

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    private final VectorSearchService vectorSearchService;

    private final DeepSeekChatClient deepSeekChatClient;

    public AnswerEvaluationServiceImpl(
            KnowledgeBaseMapper knowledgeBaseMapper,
            VectorSearchService vectorSearchService,
            DeepSeekChatClient deepSeekChatClient) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.vectorSearchService = vectorSearchService;
        this.deepSeekChatClient = deepSeekChatClient;
    }

    @Override
    public EvaluateAnswerResponse evaluate(Long knowledgeBaseId, EvaluateAnswerRequest request) {
        ensureKnowledgeBaseExists(knowledgeBaseId);

        if (request == null
                || !StringUtils.hasText(request.getQuestion())
                || !StringUtils.hasText(request.getUserAnswer())) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }

        String question = request.getQuestion().trim();
        String userAnswer = request.getUserAnswer().trim();
        String referenceAnswer = StringUtils.hasText(request.getReferenceAnswer())
                ? request.getReferenceAnswer().trim()
                : EMPTY_REFERENCE_ANSWER;
        Integer topK = normalizeTopK(request.getTopK());

        List<VectorSearchResultVO> searchResults = vectorSearchService.search(
                knowledgeBaseId,
                buildVectorSearchRequest(question, topK)
        );

        String context = searchResults.isEmpty() ? EMPTY_CONTEXT : buildContext(searchResults);
        String prompt = buildPrompt(question, userAnswer, referenceAnswer, context);
        String evaluationText = deepSeekChatClient.chat(prompt);

        return buildResponse(extractScore(evaluationText), evaluationText, toReferences(searchResults));
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

    private VectorSearchRequest buildVectorSearchRequest(String question, Integer topK) {
        VectorSearchRequest request = new VectorSearchRequest();
        request.setQuery(question);
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

    private String buildPrompt(String question, String userAnswer, String referenceAnswer, String context) {
        return """
                你是一个专业的 Java 后端面试官和面试辅导老师。

                请根据【面试题】、【用户回答】、【参考答案】和【知识库资料】对用户回答进行评分。
                如果知识库资料不足，请明确说明资料不足，但仍然可以根据题目和参考答案进行基础评价。
                不要编造不存在的知识点。

                【面试题】
                %s

                【用户回答】
                %s

                【参考答案】
                %s

                【知识库资料】
                %s

                请严格按下面格式输出：

                总分：xx/100

                一、优点
                1. ...
                2. ...

                二、不足
                1. ...
                2. ...

                三、遗漏的关键点
                1. ...
                2. ...

                四、改进版回答
                ...

                五、建议追问
                ...

                评分标准：
                1. 准确性：40分
                2. 完整性：30分
                3. 逻辑表达：20分
                4. 面试表达：10分

                要求：
                1. 评价要具体，不要只说“还可以”。
                2. 指出用户回答中遗漏的关键概念。
                3. 改进版回答要适合面试中直接说出来。
                4. 总分必须是 0 到 100 的整数。
                """.formatted(question, userAnswer, referenceAnswer, context);
    }

    private Integer extractScore(String evaluationText) {
        if (!StringUtils.hasText(evaluationText)) {
            return null;
        }

        Matcher matcher = SCORE_PATTERN.matcher(evaluationText);
        if (!matcher.find()) {
            return null;
        }

        Integer score = Integer.valueOf(matcher.group(1));
        if (score < 0 || score > 100) {
            return null;
        }
        return score;
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

    private EvaluateAnswerResponse buildResponse(
            Integer score,
            String evaluationText,
            List<ReferenceVO> references) {
        EvaluateAnswerResponse response = new EvaluateAnswerResponse();
        response.setScore(score);
        response.setEvaluationText(evaluationText);
        response.setReferences(references);
        return response;
    }
}
