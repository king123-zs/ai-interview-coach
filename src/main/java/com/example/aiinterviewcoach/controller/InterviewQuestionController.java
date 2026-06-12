package com.example.aiinterviewcoach.controller;

import com.example.aiinterviewcoach.common.Result;
import com.example.aiinterviewcoach.dto.GenerateInterviewQuestionRequest;
import com.example.aiinterviewcoach.service.InterviewQuestionService;
import com.example.aiinterviewcoach.vo.GenerateInterviewQuestionResponse;
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
public class InterviewQuestionController {

    private final InterviewQuestionService interviewQuestionService;

    public InterviewQuestionController(InterviewQuestionService interviewQuestionService) {
        this.interviewQuestionService = interviewQuestionService;
    }

    @PostMapping(
            value = "/{kbId}/interview/questions/generate",
            produces = "application/json;charset=UTF-8"
    )
    public Result<GenerateInterviewQuestionResponse> generate(
            @PathVariable Long kbId,
            @Valid @RequestBody GenerateInterviewQuestionRequest request) {
        return Result.success(interviewQuestionService.generate(kbId, request));
    }
}
