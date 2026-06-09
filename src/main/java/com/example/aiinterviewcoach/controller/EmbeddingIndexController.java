package com.example.aiinterviewcoach.controller;

import com.example.aiinterviewcoach.common.Result;
import com.example.aiinterviewcoach.service.EmbeddingIndexService;
import com.example.aiinterviewcoach.vo.DocumentChunkEmbeddingVO;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmbeddingIndexController {

    private final EmbeddingIndexService embeddingIndexService;

    public EmbeddingIndexController(EmbeddingIndexService embeddingIndexService) {
        this.embeddingIndexService = embeddingIndexService;
    }

    @PostMapping("/api/documents/{documentId}/index")
    public Result<List<DocumentChunkEmbeddingVO>> indexDocument(@PathVariable Long documentId) {
        return Result.success(embeddingIndexService.indexDocument(documentId));
    }

    @GetMapping("/api/documents/{documentId}/embeddings")
    public Result<List<DocumentChunkEmbeddingVO>> listEmbeddings(@PathVariable Long documentId) {
        return Result.success(embeddingIndexService.listEmbeddings(documentId));
    }
}
