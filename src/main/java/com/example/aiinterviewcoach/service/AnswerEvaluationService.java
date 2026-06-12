package com.example.aiinterviewcoach.service;

import com.example.aiinterviewcoach.dto.EvaluateAnswerRequest;
import com.example.aiinterviewcoach.vo.EvaluateAnswerResponse;

public interface AnswerEvaluationService {

    EvaluateAnswerResponse evaluate(Long knowledgeBaseId, EvaluateAnswerRequest request);
}
