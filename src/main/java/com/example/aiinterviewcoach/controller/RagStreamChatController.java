package com.example.aiinterviewcoach.controller;

import com.example.aiinterviewcoach.dto.RagStreamChatRequest;
import com.example.aiinterviewcoach.service.RagStreamChatService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Validated
@RestController
@RequestMapping("/api/kb")
public class RagStreamChatController {

    private final RagStreamChatService ragStreamChatService;

    public RagStreamChatController(RagStreamChatService ragStreamChatService) {
        this.ragStreamChatService = ragStreamChatService;
    }

    @PostMapping(value = "/{kbId}/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(
            @PathVariable Long kbId,
            @Valid @RequestBody RagStreamChatRequest request) {
        return ragStreamChatService.streamChat(kbId, request);
    }
}
