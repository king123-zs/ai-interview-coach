package com.example.aiinterviewcoach.vo;

import java.util.List;

public class RagChatResponse {

    private Long sessionId;

    private String answer;

    private List<ReferenceVO> references;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

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
