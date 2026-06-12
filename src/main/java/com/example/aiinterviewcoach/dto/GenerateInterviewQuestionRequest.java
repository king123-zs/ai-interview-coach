package com.example.aiinterviewcoach.dto;

import jakarta.validation.constraints.NotBlank;

public class GenerateInterviewQuestionRequest {

    @NotBlank(message = "topic cannot be blank")
    private String topic;

    private String difficulty;

    private Integer count;

    private Integer topK;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }
}
