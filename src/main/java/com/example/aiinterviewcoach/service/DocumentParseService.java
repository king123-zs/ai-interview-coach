package com.example.aiinterviewcoach.service;

import com.example.aiinterviewcoach.vo.DocumentChunkVO;
import java.util.List;

public interface DocumentParseService {

    List<DocumentChunkVO> parse(Long documentId);

    List<DocumentChunkVO> listChunks(Long documentId);
}
