export interface Result<T> {
  code: number;
  message: string;
  data: T;
}

export interface KnowledgeBase {
  id: number;
  name: string;
  description?: string;
  createdAt: string;
  updatedAt: string;
}

export interface Document {
  id: number;
  knowledgeBaseId: number;
  fileName: string;
  fileType?: string;
  filePath?: string;
  status: "UPLOADED" | "PARSED" | "INDEXED" | "FAILED";
  createdAt: string;
  updatedAt: string;
}

export interface Reference {
  chunkId: number;
  documentId: number;
  knowledgeBaseId: number;
  content: string;
  score: number;
}

export interface RagChatRequest {
  sessionId?: number;
  question: string;
  topK?: number;
}

export interface RagChatResponse {
  sessionId: number;
  answer: string;
  references: Reference[];
}

export interface InterviewQuestionRequest {
  topic: string;
  difficulty?: "easy" | "medium" | "hard";
  count?: number;
  topK?: number;
}

export interface InterviewQuestionResponse {
  topic: string;
  difficulty: string;
  count: number;
  questionsText: string;
  references: Reference[];
}

export interface EvaluateAnswerRequest {
  question: string;
  userAnswer: string;
  referenceAnswer?: string;
  topK?: number;
}

export interface EvaluateAnswerResponse {
  score: number | null;
  evaluationText: string;
  references: Reference[];
}

export interface VectorSearchRequest {
  query: string;
  topK?: number;
}

export interface VectorSearchResult extends Reference {
  id: number;
  embeddingModel: string;
  createdAt: string;
}

export interface ChatSession {
  id: number;
  knowledgeBaseId: number;
  title: string;
  createdAt: string;
  updatedAt: string;
}

export interface ChatMessage {
  id: number;
  sessionId: number;
  role: "USER" | "ASSISTANT";
  content: string;
  createdAt: string;
}
