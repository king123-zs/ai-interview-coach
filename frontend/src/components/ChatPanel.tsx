import { useState, type KeyboardEvent } from "react";
import {
  Bot,
  CheckCircle2,
  CornerDownLeft,
  Send,
  Sparkles,
  UserRound,
} from "lucide-react";

interface ChatMessageState {
  id: number;
  role: "assistant" | "user";
  content: string;
}

const initialMessages: ChatMessageState[] = [
  {
    id: 1,
    role: "assistant",
    content:
      "你好，我已经准备好基于你的 Java 知识库回答问题。当前界面使用本地状态演示，下一步可直接接入后端 SSE 流。",
  },
];

export function ChatPanel() {
  const [question, setQuestion] = useState("");
  const [messages, setMessages] = useState(initialMessages);
  const [isStreaming, setIsStreaming] = useState(false);

  const sendQuestion = () => {
    const normalizedQuestion = question.trim();
    if (!normalizedQuestion || isStreaming) {
      return;
    }

    const userMessage: ChatMessageState = {
      id: Date.now(),
      role: "user",
      content: normalizedQuestion,
    };
    setMessages((current) => [...current, userMessage]);
    setQuestion("");
    setIsStreaming(true);

    window.setTimeout(() => {
      setMessages((current) => [
        ...current,
        {
          id: Date.now() + 1,
          role: "assistant",
          content:
            "这是前端骨架的本地模拟回答。接入 streamRagChat 后，这里会随着 DeepSeek 返回的 token 实时更新，并在右侧同步展示引用片段。",
        },
      ]);
      setIsStreaming(false);
    }, 650);
  };

  const handleKeyDown = (event: KeyboardEvent<HTMLTextAreaElement>) => {
    if ((event.ctrlKey || event.metaKey) && event.key === "Enter") {
      event.preventDefault();
      sendQuestion();
    }
  };

  return (
    <section className="flex min-h-[calc(100vh-121px)] flex-col">
      <div className="mb-5 flex flex-wrap items-end justify-between gap-3">
        <div>
          <p className="section-kicker">Grounded conversation</p>
          <h2 className="section-title">基于资料，而不是凭空回答</h2>
        </div>
        <div className="flex items-center gap-2 rounded-lg border border-emerald-200 bg-emerald-50 px-3 py-2 text-xs font-semibold text-emerald-700">
          <CheckCircle2 size={15} />
          SSE · 待接入
        </div>
      </div>

      <div className="surface flex min-h-0 flex-1 flex-col overflow-hidden">
        <div className="border-b border-line bg-white px-5 py-3">
          <div className="flex items-center gap-2 text-sm font-semibold text-ink">
            <Sparkles size={16} className="text-brand" />
            Java 知识库助手
          </div>
        </div>

        <div className="flex-1 space-y-5 overflow-y-auto px-4 py-6 sm:px-6">
          {messages.map((message) => (
            <div
              key={message.id}
              className={`flex gap-3 ${
                message.role === "user" ? "justify-end" : "justify-start"
              }`}
            >
              {message.role === "assistant" && (
                <div className="message-avatar bg-ink text-white">
                  <Bot size={17} />
                </div>
              )}
              <div
                className={`max-w-[82%] rounded-lg px-4 py-3 text-sm leading-7 ${
                  message.role === "user"
                    ? "bg-brand text-white"
                    : "border border-line bg-[#fbfcfe] text-ink"
                }`}
              >
                {message.content}
              </div>
              {message.role === "user" && (
                <div className="message-avatar border border-line bg-white text-muted">
                  <UserRound size={17} />
                </div>
              )}
            </div>
          ))}

          {isStreaming && (
            <div className="flex items-center gap-3 text-sm text-muted">
              <div className="message-avatar bg-ink text-white">
                <Bot size={17} />
              </div>
              <span className="streaming-dots">正在生成回答</span>
            </div>
          )}
        </div>

        <div className="border-t border-line bg-white p-3 sm:p-4">
          <div className="rounded-lg border border-line bg-white p-2 shadow-soft focus-within:border-brand">
            <textarea
              value={question}
              onChange={(event) => setQuestion(event.target.value)}
              onKeyDown={handleKeyDown}
              rows={3}
              className="w-full resize-none border-0 bg-transparent px-2 py-1 text-sm leading-6 text-ink outline-none placeholder:text-slate-400"
              placeholder="输入一个 Java 面试问题..."
              aria-label="问题输入框"
            />
            <div className="flex items-center justify-between gap-3 border-t border-slate-100 px-2 pt-2">
              <span className="flex items-center gap-1.5 text-[11px] text-muted">
                <CornerDownLeft size={13} />
                Ctrl + Enter 发送
              </span>
              <button
                type="button"
                className="primary-button"
                onClick={sendQuestion}
                disabled={!question.trim() || isStreaming}
              >
                <Send size={16} />
                发送
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
