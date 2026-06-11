package com.example.aiinterviewcoach.dto;

import jakarta.validation.constraints.NotBlank;

public class VectorSearchRequest {

    @NotBlank(message = "query cannot be blank")
    private String query;

    private Integer topK;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }
}
