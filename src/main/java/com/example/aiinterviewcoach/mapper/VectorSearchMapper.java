package com.example.aiinterviewcoach.mapper;

import com.example.aiinterviewcoach.vo.VectorSearchResultVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VectorSearchMapper {

    @Select("""
            SELECT
                id,
                chunk_id,
                document_id,
                knowledge_base_id,
                content,
                embedding_model,
                created_at,
                1 - (embedding <=> #{queryEmbedding}::vector) AS score
            FROM document_chunk_embedding
            WHERE knowledge_base_id = #{knowledgeBaseId}
            ORDER BY embedding <=> #{queryEmbedding}::vector
            LIMIT #{topK}
            """)
    List<VectorSearchResultVO> searchByKnowledgeBaseId(
            @Param("knowledgeBaseId") Long knowledgeBaseId,
            @Param("queryEmbedding") String queryEmbedding,
            @Param("topK") Integer topK);
}
