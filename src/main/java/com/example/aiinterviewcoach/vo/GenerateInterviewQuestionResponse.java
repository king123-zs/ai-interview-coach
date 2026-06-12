package com.example.aiinterviewcoach.vo;

import java.util.List;

public class GenerateInterviewQuestionResponse {

    private String topic;

    private String difficulty;

    private Integer count;

    private String questionsText;

    private List<ReferenceVO> references;

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

    public String getQuestionsText() {
        return questionsText;
    }

    public void setQuestionsText(String questionsText) {
        this.questionsText = questionsText;
    }

    public List<ReferenceVO> getReferences() {
        return references;
    }

    public void setReferences(List<ReferenceVO> references) {
        this.references = references;
    }
}
