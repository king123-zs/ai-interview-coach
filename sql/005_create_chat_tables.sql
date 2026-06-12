CREATE TABLE IF NOT EXISTS chat_session (
    id BIGSERIAL PRIMARY KEY,
    knowledge_base_id BIGINT NOT NULL,
    title VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS chat_message (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_chat_session_kb_id
ON chat_session(knowledge_base_id);

CREATE INDEX IF NOT EXISTS idx_chat_message_session_id
ON chat_message(session_id);
