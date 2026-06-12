package com.example.aiinterviewcoach.controller;

import com.example.aiinterviewcoach.common.Result;
import com.example.aiinterviewcoach.service.ChatHistoryService;
import com.example.aiinterviewcoach.vo.ChatMessageVO;
import com.example.aiinterviewcoach.vo.ChatSessionVO;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(produces = "application/json;charset=UTF-8")
public class ChatHistoryController {

    private final ChatHistoryService chatHistoryService;

    public ChatHistoryController(ChatHistoryService chatHistoryService) {
        this.chatHistoryService = chatHistoryService;
    }

    @GetMapping("/api/kb/{kbId}/chat/sessions")
    public Result<List<ChatSessionVO>> listSessions(@PathVariable Long kbId) {
        return Result.success(chatHistoryService.listSessions(kbId));
    }

    @GetMapping("/api/chat/sessions/{sessionId}/messages")
    public Result<List<ChatMessageVO>> listMessages(@PathVariable Long sessionId) {
        return Result.success(chatHistoryService.listMessages(sessionId));
    }

    @DeleteMapping("/api/chat/sessions/{sessionId}")
    public Result<Void> deleteSession(@PathVariable Long sessionId) {
        chatHistoryService.deleteSession(sessionId);
        return Result.success();
    }
}
