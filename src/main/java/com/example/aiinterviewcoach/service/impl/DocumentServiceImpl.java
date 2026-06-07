package com.example.aiinterviewcoach.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiinterviewcoach.common.ResultCode;
import com.example.aiinterviewcoach.entity.Document;
import com.example.aiinterviewcoach.exception.BusinessException;
import com.example.aiinterviewcoach.mapper.DocumentMapper;
import com.example.aiinterviewcoach.mapper.KnowledgeBaseMapper;
import com.example.aiinterviewcoach.service.DocumentService;
import com.example.aiinterviewcoach.vo.DocumentVO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentServiceImpl implements DocumentService {

    private static final String STATUS_UPLOADED = "UPLOADED";

    private static final String UPLOAD_DIR = "uploads";

    private final DocumentMapper documentMapper;

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    public DocumentServiceImpl(DocumentMapper documentMapper, KnowledgeBaseMapper knowledgeBaseMapper) {
        this.documentMapper = documentMapper;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
    }

    @Override
    public DocumentVO upload(Long kbId, MultipartFile file) {
        ensureKnowledgeBaseExists(kbId);
        validateFile(file);

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileType = getFileType(originalFileName);
        String savedFileName = UUID.randomUUID() + "." + fileType;
        Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        Path targetPath = uploadPath.resolve(savedFileName).normalize();

        try {
            Files.createDirectories(uploadPath);
            file.transferTo(targetPath);
        } catch (IOException exception) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR);
        }

        LocalDateTime now = LocalDateTime.now();
        Document document = new Document();
        document.setKnowledgeBaseId(kbId);
        document.setFileName(originalFileName);
        document.setFileType(fileType);
        document.setFilePath(targetPath.toString());
        document.setStatus(STATUS_UPLOADED);
        document.setCreatedAt(now);
        document.setUpdatedAt(now);

        documentMapper.insert(document);
        return toVO(document);
    }

    @Override
    public List<DocumentVO> listByKnowledgeBaseId(Long kbId) {
        ensureKnowledgeBaseExists(kbId);

        LambdaQueryWrapper<Document> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Document::getKnowledgeBaseId, kbId);
        queryWrapper.orderByDesc(Document::getCreatedAt);
        return documentMapper.selectList(queryWrapper)
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public DocumentVO getById(Long documentId) {
        return toVO(getExistingDocument(documentId));
    }

    @Override
    public void delete(Long documentId) {
        Document document = getExistingDocument(documentId);
        documentMapper.deleteById(documentId);

        if (StringUtils.hasText(document.getFilePath())) {
            try {
                Files.deleteIfExists(Paths.get(document.getFilePath()));
            } catch (IOException exception) {
                throw new BusinessException(ResultCode.INTERNAL_ERROR);
            }
        }
    }

    private void ensureKnowledgeBaseExists(Long kbId) {
        if (knowledgeBaseMapper.selectById(kbId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (!StringUtils.hasText(originalFileName)) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }

        String fileType = getFileType(originalFileName);
        if (!"txt".equals(fileType) && !"md".equals(fileType)) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }
    }

    private String getFileType(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private Document getExistingDocument(Long documentId) {
        Document document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return document;
    }

    private DocumentVO toVO(Document document) {
        DocumentVO vo = new DocumentVO();
        vo.setId(document.getId());
        vo.setKnowledgeBaseId(document.getKnowledgeBaseId());
        vo.setFileName(document.getFileName());
        vo.setFileType(document.getFileType());
        vo.setFilePath(document.getFilePath());
        vo.setStatus(document.getStatus());
        vo.setCreatedAt(document.getCreatedAt());
        vo.setUpdatedAt(document.getUpdatedAt());
        return vo;
    }
}
