package com.example.aiinterviewcoach.controller;

import com.example.aiinterviewcoach.common.Result;
import com.example.aiinterviewcoach.service.DocumentParseService;
import com.example.aiinterviewcoach.vo.DocumentChunkVO;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DocumentParseController {

    private final DocumentParseService documentParseService;

    public DocumentParseController(DocumentParseService documentParseService) {
        this.documentParseService = documentParseService;
    }

    @PostMapping("/api/documents/{documentId}/parse")
    public Result<List<DocumentChunkVO>> parse(@PathVariable Long documentId) {
        return Result.success(documentParseService.parse(documentId));
    }

    @GetMapping("/api/documents/{documentId}/chunks")
    public Result<List<DocumentChunkVO>> listChunks(@PathVariable Long documentId) {
        return Result.success(documentParseService.listChunks(documentId));
    }
}
