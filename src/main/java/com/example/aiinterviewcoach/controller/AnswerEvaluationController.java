package com.example.aiinterviewcoach.controller;

import com.example.aiinterviewcoach.common.Result;
import com.example.aiinterviewcoach.dto.EvaluateAnswerRequest;
import com.example.aiinterviewcoach.service.AnswerEvaluationService;
import com.example.aiinterviewcoach.vo.EvaluateAnswerResponse;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/kb")
public class AnswerEvaluationController {

    private final AnswerEvaluationService answerEvaluationService;

    public AnswerEvaluationController(AnswerEvaluationService answerEvaluationService) {
        this.answerEvaluationService = answerEvaluationService;
    }

    @PostMapping(
            value = "/{kbId}/interview/answers/evaluate",
            produces = "application/json;charset=UTF-8"
    )
    public Result<EvaluateAnswerResponse> evaluate(
            @PathVariable Long kbId,
            @Valid @RequestBody EvaluateAnswerRequest request) {
        return Result.success(answerEvaluationService.evaluate(kbId, request));
    }
}
