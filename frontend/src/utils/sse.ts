import { API_BASE_URL } from "../api/client";
import type { RagChatRequest, Reference } from "../api/types";

export interface StreamSession {
  sessionId: number;
}

export interface RagStreamCallbacks {
  onSession?: (session: StreamSession) => void;
  onReferences?: (references: Reference[]) => void;
  onMessage?: (token: string) => void;
  onDone?: () => void;
  onError?: (error: Error) => void;
}

interface ParsedSseEvent {
  event: string;
  data: string;
}

function parseEventBlock(block: string): ParsedSseEvent | null {
  let event = "message";
  const dataLines: string[] = [];

  for (const line of block.split("\n")) {
    if (!line || line.startsWith(":")) {
      continue;
    }
    if (line.startsWith("event:")) {
      event = line.slice(6).trim();
    } else if (line.startsWith("data:")) {
      dataLines.push(line.slice(5).trimStart());
    }
  }

  if (dataLines.length === 0) {
    return null;
  }

  return { event, data: dataLines.join("\n") };
}

function parseJson<T>(data: string): T {
  return JSON.parse(data) as T;
}

function decodeMessage(data: string): string {
  if (data.startsWith('"') && data.endsWith('"')) {
    try {
      return parseJson<string>(data);
    } catch {
      return data;
    }
  }
  return data;
}

function dispatchEvent(
  parsedEvent: ParsedSseEvent,
  callbacks: RagStreamCallbacks,
) {
  const { event, data } = parsedEvent;

  if (data === "[DONE]") {
    callbacks.onDone?.();
    return;
  }

  switch (event) {
    case "session":
      callbacks.onSession?.(parseJson<StreamSession>(data));
      break;
    case "references":
      callbacks.onReferences?.(parseJson<Reference[]>(data));
      break;
    case "message":
      callbacks.onMessage?.(decodeMessage(data));
      break;
    case "done":
      callbacks.onDone?.();
      break;
    case "error": {
      let message = data;
      try {
        const parsed = parseJson<{ message?: string }>(data);
        message = parsed.message || data;
      } catch {
        // Keep the original SSE data as the error message.
      }
      callbacks.onError?.(new Error(message));
      break;
    }
  }
}

export async function streamRagChat(
  kbId: number,
  request: RagChatRequest,
  callbacks: RagStreamCallbacks,
  signal?: AbortSignal,
): Promise<void> {
  try {
    const response = await fetch(
      `${API_BASE_URL}/api/kb/${kbId}/chat/stream`,
      {
        method: "POST",
        headers: {
          Accept: "text/event-stream",
          "Content-Type": "application/json; charset=UTF-8",
        },
        body: JSON.stringify(request),
        signal,
      },
    );

    if (!response.ok) {
      throw new Error(`流式请求失败（HTTP ${response.status}）`);
    }
    if (!response.body) {
      throw new Error("当前浏览器无法读取流式响应");
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder("utf-8");
    let buffer = "";

    while (true) {
      const { done, value } = await reader.read();
      buffer += decoder
        .decode(value, { stream: !done })
        .replace(/\r\n?/g, "\n");

      const blocks = buffer.split("\n\n");
      buffer = blocks.pop() ?? "";

      for (const block of blocks) {
        const parsedEvent = parseEventBlock(block);
        if (parsedEvent) {
          dispatchEvent(parsedEvent, callbacks);
        }
      }

      if (done) {
        break;
      }
    }

    if (buffer.trim()) {
      const parsedEvent = parseEventBlock(buffer);
      if (parsedEvent) {
        dispatchEvent(parsedEvent, callbacks);
      }
    }
  } catch (error) {
    const normalizedError =
      error instanceof Error ? error : new Error("流式请求发生未知错误");
    callbacks.onError?.(normalizedError);
    throw normalizedError;
  }
}
