package com.example.aiinterviewcoach.service;

import com.example.aiinterviewcoach.common.ResultCode;
import com.example.aiinterviewcoach.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
public class DashScopeEmbeddingClient {

    private final String apiKey;

    private final String baseUrl;

    private final String embeddingModel;

    private final Integer embeddingDimensions;

    private final RestClient restClient;

    private final ObjectMapper objectMapper;

    public DashScopeEmbeddingClient(
            @Value("${dashscope.api-key:}") String apiKey,
            @Value("${dashscope.base-url}") String baseUrl,
            @Value("${dashscope.embedding-model}") String embeddingModel,
            @Value("${dashscope.embedding-dimensions}") Integer embeddingDimensions,
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.embeddingModel = embeddingModel;
        this.embeddingDimensions = embeddingDimensions;
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public List<Double> embed(String input) {
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR.getCode(), "DASHSCOPE_API_KEY is not configured");
        }

        Map<String, Object> requestBody = Map.of(
                "model", embeddingModel,
                "input", input,
                "dimensions", embeddingDimensions,
                "encoding_format", "float"
        );

        try {
            String responseBody = restClient.post()
                    .uri(baseUrl + "/embeddings")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return parseEmbedding(responseBody);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR.getCode(), "failed to call DashScope embeddings API");
        }
    }

    public String getEmbeddingModel() {
        return embeddingModel;
    }

    private List<Double> parseEmbedding(String responseBody) {
        try {
            JsonNode embeddingNode = objectMapper.readTree(responseBody)
                    .path("data")
                    .path(0)
                    .path("embedding");

            if (!embeddingNode.isArray()) {
                throw new BusinessException(ResultCode.INTERNAL_ERROR.getCode(), "invalid DashScope embeddings response");
            }

            List<Double> embedding = new ArrayList<>();
            for (JsonNode valueNode : embeddingNode) {
                embedding.add(valueNode.asDouble());
            }

            if (embedding.size() != embeddingDimensions) {
                throw new BusinessException(ResultCode.INTERNAL_ERROR.getCode(), "invalid embedding dimensions");
            }
            return embedding;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR.getCode(), "failed to parse DashScope embeddings response");
        }
    }
}
