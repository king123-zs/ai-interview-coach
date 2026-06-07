package com.example.aiinterviewcoach.controller;

import com.example.aiinterviewcoach.common.Result;
import com.example.aiinterviewcoach.service.DocumentService;
import com.example.aiinterviewcoach.vo.DocumentVO;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/api/kb/{kbId}/documents/upload")
    public Result<DocumentVO> upload(@PathVariable Long kbId, @RequestParam("file") MultipartFile file) {
        return Result.success(documentService.upload(kbId, file));
    }

    @GetMapping("/api/kb/{kbId}/documents")
    public Result<List<DocumentVO>> listByKnowledgeBaseId(@PathVariable Long kbId) {
        return Result.success(documentService.listByKnowledgeBaseId(kbId));
    }

    @GetMapping("/api/documents/{documentId}")
    public Result<DocumentVO> getById(@PathVariable Long documentId) {
        return Result.success(documentService.getById(documentId));
    }

    @DeleteMapping("/api/documents/{documentId}")
    public Result<Void> delete(@PathVariable Long documentId) {
        documentService.delete(documentId);
        return Result.success();
    }
}
