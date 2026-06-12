package com.example.aiinterviewcoach.dto;

import jakarta.validation.constraints.NotBlank;

public class EvaluateAnswerRequest {

    @NotBlank(message = "question cannot be blank")
    private String question;

    @NotBlank(message = "userAnswer cannot be blank")
    private String userAnswer;

    private String referenceAnswer;

    private Integer topK;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    public String getReferenceAnswer() {
        return referenceAnswer;
    }

    public void setReferenceAnswer(String referenceAnswer) {
        this.referenceAnswer = referenceAnswer;
    }

    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }
}
