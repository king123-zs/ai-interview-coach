package com.example.aiinterviewcoach.service;

import com.example.aiinterviewcoach.dto.GenerateInterviewQuestionRequest;
import com.example.aiinterviewcoach.vo.GenerateInterviewQuestionResponse;

public interface InterviewQuestionService {

    GenerateInterviewQuestionResponse generate(Long knowledgeBaseId, GenerateInterviewQuestionRequest request);
}
