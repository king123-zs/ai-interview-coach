package com.example.aiinterviewcoach.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aiinterviewcoach.entity.DocumentChunkEmbedding;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DocumentChunkEmbeddingMapper extends BaseMapper<DocumentChunkEmbedding> {

    @Insert("""
            INSERT INTO document_chunk_embedding
            (chunk_id, document_id, knowledge_base_id, content, embedding, embedding_model, created_at)
            VALUES
            (#{chunkId}, #{documentId}, #{knowledgeBaseId}, #{content}, #{embedding}::vector, #{embeddingModel}, CURRENT_TIMESTAMP)
            """)
    int insertEmbedding(DocumentChunkEmbedding embedding);

    @Delete("DELETE FROM document_chunk_embedding WHERE document_id = #{documentId}")
    int deleteByDocumentId(Long documentId);

    @Select("""
            SELECT id, chunk_id, document_id, knowledge_base_id, content, embedding_model, created_at
            FROM document_chunk_embedding
            WHERE document_id = #{documentId}
            ORDER BY id ASC
            """)
    List<DocumentChunkEmbedding> listByDocumentId(Long documentId);
}
