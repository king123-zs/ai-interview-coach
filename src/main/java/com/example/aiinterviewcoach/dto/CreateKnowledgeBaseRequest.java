package com.example.aiinterviewcoach.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateKnowledgeBaseRequest {

    @NotBlank(message = "name cannot be blank")
    @Size(max = 100, message = "name length cannot exceed 100")
    private String name;

    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
