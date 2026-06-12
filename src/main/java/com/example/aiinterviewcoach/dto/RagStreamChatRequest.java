package com.example.aiinterviewcoach.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class RagStreamChatRequest {

    private Long sessionId;

    @NotBlank(message = "question cannot be blank")
    private String question;

    @Min(value = 1, message = "topK must be at least 1")
    @Max(value = 20, message = "topK must be at most 20")
    private Integer topK;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }
}
