package com.example.aiinterviewcoach.service;

import com.example.aiinterviewcoach.common.ResultCode;
import com.example.aiinterviewcoach.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
public class DeepSeekStreamClient {

    private static final String SYSTEM_PROMPT = "你是一个专业的 Java 面试辅导老师。";

    private static final MediaType APPLICATION_JSON_UTF8 =
            new MediaType("application", "json", StandardCharsets.UTF_8);

    private final String apiKey;

    private final String baseUrl;

    private final String chatModel;

    private final RestClient restClient;

    private final ObjectMapper objectMapper;

    public DeepSeekStreamClient(
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

    public void streamChat(String prompt, StreamTokenHandler handler) {
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR.getCode(), "DEEPSEEK_API_KEY is not configured");
        }

        Map<String, Object> requestBody = Map.of(
                "model", chatModel,
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.3,
                "stream", true
        );

        try {
            restClient.post()
                    .uri(baseUrl + "/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .contentType(APPLICATION_JSON_UTF8)
                    .body(requestBody)
                    .exchange((request, response) -> {
                        if (response.getStatusCode().isError()) {
                            throw new BusinessException(
                                    ResultCode.INTERNAL_ERROR.getCode(),
                                    "failed to call DeepSeek streaming API"
                            );
                        }

                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                processLine(line, handler);
                                if ("data: [DONE]".equals(line.trim())) {
                                    break;
                                }
                            }
                        }
                        return null;
                    });
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR.getCode(), "failed to stream DeepSeek chat response");
        }
    }

    private void processLine(String line, StreamTokenHandler handler) {
        if (!line.startsWith("data:")) {
            return;
        }

        String data = line.substring("data:".length()).trim();
        if (!StringUtils.hasText(data) || "[DONE]".equals(data)) {
            return;
        }

        try {
            JsonNode contentNode = objectMapper.readTree(data)
                    .path("choices")
                    .path(0)
                    .path("delta")
                    .path("content");

            if (contentNode.isTextual() && StringUtils.hasLength(contentNode.asText())) {
                handler.onToken(contentNode.asText());
            }
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR.getCode(), "failed to parse DeepSeek stream response");
        }
    }
}
