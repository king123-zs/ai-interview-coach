import type {
  ChatMessage,
  ChatSession,
  Document,
  EvaluateAnswerRequest,
  EvaluateAnswerResponse,
  InterviewQuestionRequest,
  InterviewQuestionResponse,
  KnowledgeBase,
  RagChatRequest,
  RagChatResponse,
  Result,
  VectorSearchRequest,
  VectorSearchResult,
} from "./types";

export const API_BASE_URL = (
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080"
).replace(/\/$/, "");

interface RequestOptions extends Omit<RequestInit, "body"> {
  body?: unknown;
}

async function request<T>(
  path: string,
  options: RequestOptions = {},
): Promise<T> {
  const isFormData = options.body instanceof FormData;
  const headers = new Headers(options.headers);

  headers.set("Accept", "application/json");
  if (!isFormData && options.body !== undefined) {
    headers.set("Content-Type", "application/json; charset=UTF-8");
  }

  let requestBody: BodyInit | undefined;
  if (options.body instanceof FormData) {
    requestBody = options.body;
  } else if (options.body !== undefined) {
    requestBody = JSON.stringify(options.body);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
    body: requestBody,
  });

  const rawBody = await response.text();
  let result: Result<T> | null = null;

  if (rawBody) {
    try {
      result = JSON.parse(rawBody) as Result<T>;
    } catch {
      throw new Error(`服务端返回了无法解析的响应（HTTP ${response.status}）`);
    }
  }

  if (!response.ok) {
    throw new Error(result?.message || `请求失败（HTTP ${response.status}）`);
  }

  if (!result) {
    return undefined as T;
  }

  if (result.code !== 200) {
    throw new Error(result.message || `业务请求失败（code: ${result.code}）`);
  }

  return result.data;
}

export function getKnowledgeBases() {
  return request<KnowledgeBase[]>("/api/kb/list");
}

export function createKnowledgeBase(data: {
  name: string;
  description?: string;
}) {
  return request<number>("/api/kb", {
    method: "POST",
    body: data,
  });
}

export function uploadDocument(kbId: number, file: File) {
  const formData = new FormData();
  formData.append("file", file);

  return request<Document>(`/api/kb/${kbId}/documents/upload`, {
    method: "POST",
    body: formData,
  });
}

export function parseDocument(documentId: number) {
  return request(`/api/documents/${documentId}/parse`, {
    method: "POST",
  });
}

export function indexDocument(documentId: number) {
  return request(`/api/documents/${documentId}/embeddings`, {
    method: "POST",
  });
}

export function searchKnowledgeBase(
  kbId: number,
  data: VectorSearchRequest,
) {
  return request<VectorSearchResult[]>(`/api/kb/${kbId}/search`, {
    method: "POST",
    body: data,
  });
}

export function ragChat(kbId: number, data: RagChatRequest) {
  return request<RagChatResponse>(`/api/kb/${kbId}/chat`, {
    method: "POST",
    body: data,
  });
}

export function generateInterviewQuestions(
  kbId: number,
  data: InterviewQuestionRequest,
) {
  return request<InterviewQuestionResponse>(
    `/api/kb/${kbId}/interview/questions/generate`,
    {
      method: "POST",
      body: data,
    },
  );
}

export function evaluateAnswer(kbId: number, data: EvaluateAnswerRequest) {
  return request<EvaluateAnswerResponse>(
    `/api/kb/${kbId}/interview/answers/evaluate`,
    {
      method: "POST",
      body: data,
    },
  );
}

export function getChatSessions(kbId: number) {
  return request<ChatSession[]>(`/api/kb/${kbId}/chat/sessions`);
}

export function getChatMessages(sessionId: number) {
  return request<ChatMessage[]>(`/api/chat/sessions/${sessionId}/messages`);
}
