package com.example.aiinterviewcoach.vo;

import java.util.List;

public class RagChatResponse {

    private String answer;

    private List<ReferenceVO> references;

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<ReferenceVO> getReferences() {
        return references;
    }

    public void setReferences(List<ReferenceVO> references) {
        this.references = references;
    }
}
