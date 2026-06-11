package com.example.aiinterviewcoach.controller;

import com.example.aiinterviewcoach.common.Result;
import com.example.aiinterviewcoach.dto.RagChatRequest;
import com.example.aiinterviewcoach.service.RagChatService;
import com.example.aiinterviewcoach.vo.RagChatResponse;
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
public class RagChatController {

    private final RagChatService ragChatService;

    public RagChatController(RagChatService ragChatService) {
        this.ragChatService = ragChatService;
    }

    @PostMapping(value = "/{kbId}/chat", produces = "application/json;charset=UTF-8")
    public Result<RagChatResponse> chat(@PathVariable Long kbId, @Valid @RequestBody RagChatRequest request) {
        return Result.success(ragChatService.chat(kbId, request));
    }
}
