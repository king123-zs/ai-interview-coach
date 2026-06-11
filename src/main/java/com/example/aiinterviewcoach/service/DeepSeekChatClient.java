package com.example.aiinterviewcoach.service;

import com.example.aiinterviewcoach.common.ResultCode;
import com.example.aiinterviewcoach.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
public class DeepSeekChatClient {

    private static final String SYSTEM_PROMPT = "你是一个专业的 Java 面试辅导老师。";

    private static final MediaType APPLICATION_JSON_UTF8 =
            new MediaType("application", "json", StandardCharsets.UTF_8);

    private final String apiKey;

    private final String baseUrl;

    private final String chatModel;

    private final RestClient restClient;

    private final ObjectMapper objectMapper;

    public DeepSeekChatClient(
            @Value("${deepseek.api-key:}") String apiKey,
            @Value("${deepseek.base-url}") String baseUrl,
            @Value("${deepseek.chat-model}") String chatModel,
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.chatModel = chatModel;
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public String chat(String prompt) {
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR.getCode(), "DEEPSEEK_API_KEY is not configured");
        }

        Map<String, Object> requestBody = Map.of(
                "model", chatModel,
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.3
        );

        try {
            byte[] responseBytes = restClient.post()
                    .uri(baseUrl + "/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(APPLICATION_JSON_UTF8)
                    .body(requestBody)
                    .retrieve()
                    .body(byte[].class);

            if (responseBytes == null || responseBytes.length == 0) {
                throw new BusinessException(ResultCode.INTERNAL_ERROR.getCode(), "empty DeepSeek chat response");
            }

            return parseAnswer(new String(responseBytes, StandardCharsets.UTF_8));
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR.getCode(), "failed to call DeepSeek chat API");
        }
    }

    private String parseAnswer(String responseBody) {
        try {
            JsonNode contentNode = objectMapper.readTree(responseBody)
                    .path("choices")
                    .path(0)
                    .path("message")
                    .path("content");

            if (!contentNode.isTextual() || !StringUtils.hasText(contentNode.asText())) {
                throw new BusinessException(ResultCode.INTERNAL_ERROR.getCode(), "invalid DeepSeek chat response");
            }
            return contentNode.asText();
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR.getCode(), "failed to parse DeepSeek chat response");
        }
    }
}
