package com.example.aiinterviewcoach.vo;

import java.util.List;

public class EvaluateAnswerResponse {

    private Integer score;

    private String evaluationText;

    private List<ReferenceVO> references;

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getEvaluationText() {
        return evaluationText;
    }

    public void setEvaluationText(String evaluationText) {
        this.evaluationText = evaluationText;
    }

    public List<ReferenceVO> getReferences() {
        return references;
    }

    public void setReferences(List<ReferenceVO> references) {
        this.references = references;
    }
}
