package com.example.aiinterviewcoach.service;

import com.example.aiinterviewcoach.vo.DocumentVO;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentService {

    DocumentVO upload(Long kbId, MultipartFile file);

    List<DocumentVO> listByKnowledgeBaseId(Long kbId);

    DocumentVO getById(Long documentId);

    void delete(Long documentId);
}
