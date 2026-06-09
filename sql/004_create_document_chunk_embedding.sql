CREATE TABLE IF NOT EXISTS document_chunk_embedding (
    id BIGSERIAL PRIMARY KEY,
    chunk_id BIGINT NOT NULL,
    document_id BIGINT NOT NULL,
    knowledge_base_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    embedding VECTOR(1024) NOT NULL,
    embedding_model VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_chunk_embedding_document_id
ON document_chunk_embedding(document_id);

CREATE INDEX IF NOT EXISTS idx_chunk_embedding_kb_id
ON document_chunk_embedding(knowledge_base_id);
