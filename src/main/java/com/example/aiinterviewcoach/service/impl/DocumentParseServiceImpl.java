package com.example.aiinterviewcoach.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiinterviewcoach.common.ResultCode;
import com.example.aiinterviewcoach.entity.Document;
import com.example.aiinterviewcoach.entity.DocumentChunk;
import com.example.aiinterviewcoach.exception.BusinessException;
import com.example.aiinterviewcoach.mapper.DocumentChunkMapper;
import com.example.aiinterviewcoach.mapper.DocumentMapper;
import com.example.aiinterviewcoach.service.DocumentParseService;
import com.example.aiinterviewcoach.vo.DocumentChunkVO;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DocumentParseServiceImpl implements DocumentParseService {

    private static final int CHUNK_SIZE = 500;

    private static final int OVERLAP = 100;

    private static final String STATUS_PARSED = "PARSED";

    private static final String STATUS_FAILED = "FAILED";

    private final DocumentMapper documentMapper;

    private final DocumentChunkMapper documentChunkMapper;

    public DocumentParseServiceImpl(DocumentMapper documentMapper, DocumentChunkMapper documentChunkMapper) {
        this.documentMapper = documentMapper;
        this.documentChunkMapper = documentChunkMapper;
    }

    @Override
    public List<DocumentChunkVO> parse(Long documentId) {
        Document document = getExistingDocument(documentId);

        try {
            validateSupportedFile(document);
            String content = readDocumentContent(document);
            List<String> chunkContents = splitText(content);

            deleteExistingChunks(documentId);
            List<DocumentChunkVO> chunks = saveChunks(document, chunkContents);
            updateDocumentStatus(document, STATUS_PARSED);
            return chunks;
        } catch (RuntimeException exception) {
            updateDocumentStatus(document, STATUS_FAILED);
            throw exception;
        }
    }

    @Override
    public List<DocumentChunkVO> listChunks(Long documentId) {
        getExistingDocument(documentId);

        LambdaQueryWrapper<DocumentChunk> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocumentChunk::getDocumentId, documentId);
        queryWrapper.orderByAsc(DocumentChunk::getChunkIndex);
        return documentChunkMapper.selectList(queryWrapper)
                .stream()
                .map(this::toVO)
                .toList();
    }

    private Document getExistingDocument(Long documentId) {
        Document document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return document;
    }

    private void validateSupportedFile(Document document) {
        String fileType = getFileType(document);
        if (!"txt".equals(fileType) && !"md".equals(fileType)) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }
    }

    private String getFileType(Document document) {
        if (StringUtils.hasText(document.getFileType())) {
            return document.getFileType().toLowerCase(Locale.ROOT);
        }

        String fileName = document.getFileName();
        if (!StringUtils.hasText(fileName)) {
            return "";
        }

        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String readDocumentContent(Document document) {
        if (!StringUtils.hasText(document.getFilePath())) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        Path filePath = Paths.get(document.getFilePath());
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        try {
            return Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR);
        }
    }

    private List<String> splitText(String content) {
        List<String> chunks = new ArrayList<>();
        String text = content == null ? "" : content;

        if (text.length() <= CHUNK_SIZE) {
            chunks.add(text);
            return chunks;
        }

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + CHUNK_SIZE, text.length());
            chunks.add(text.substring(start, end));
            if (end == text.length()) {
                break;
            }
            start = end - OVERLAP;
        }
        return chunks;
    }

    private void deleteExistingChunks(Long documentId) {
        LambdaQueryWrapper<DocumentChunk> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(DocumentChunk::getDocumentId, documentId);
        documentChunkMapper.delete(deleteWrapper);
    }

    private List<DocumentChunkVO> saveChunks(Document document, List<String> chunkContents) {
        List<DocumentChunkVO> chunks = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < chunkContents.size(); i++) {
            DocumentChunk chunk = new DocumentChunk();
            chunk.setDocumentId(document.getId());
            chunk.setKnowledgeBaseId(document.getKnowledgeBaseId());
            chunk.setChunkIndex(i);
            chunk.setContent(chunkContents.get(i));
            chunk.setCreatedAt(now);

            documentChunkMapper.insert(chunk);
            chunks.add(toVO(chunk));
        }
        return chunks;
    }

    private void updateDocumentStatus(Document document, String status) {
        document.setStatus(status);
        document.setUpdatedAt(LocalDateTime.now());
        documentMapper.updateById(document);
    }

    private DocumentChunkVO toVO(DocumentChunk chunk) {
        DocumentChunkVO vo = new DocumentChunkVO();
        vo.setId(chunk.getId());
        vo.setDocumentId(chunk.getDocumentId());
        vo.setKnowledgeBaseId(chunk.getKnowledgeBaseId());
        vo.setChunkIndex(chunk.getChunkIndex());
        vo.setContent(chunk.getContent());
        vo.setCreatedAt(chunk.getCreatedAt());
        return vo;
    }
}
