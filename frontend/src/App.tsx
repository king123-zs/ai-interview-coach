import { useState } from "react";
import { Clock3, MessageSquareText } from "lucide-react";
import { AppShell } from "./components/AppShell";
import { ChatPanel } from "./components/ChatPanel";
import { DocumentPanel } from "./components/DocumentPanel";
import { InterviewPanel } from "./components/InterviewPanel";
import { KnowledgeBasePanel } from "./components/KnowledgeBasePanel";
import { ReferencePanel } from "./components/ReferencePanel";
import type { NavigationKey } from "./components/Sidebar";

const pageTitles: Record<NavigationKey, string> = {
  knowledge: "知识库",
  documents: "文档管理",
  chat: "RAG 问答",
  questions: "面试题生成",
  evaluation: "答案评分",
  history: "聊天记录",
};

function ChatHistoryPlaceholder() {
  return (
    <section>
      <div className="mb-6">
        <p className="section-kicker">Conversation archive</p>
        <h2 className="section-title">回看每一次练习</h2>
        <p className="section-description">
          会话列表和消息详情将在下一步连接聊天记录接口。
        </p>
      </div>
      <div className="surface flex min-h-72 flex-col items-center justify-center px-6 text-center">
        <div className="grid size-12 place-items-center rounded-lg bg-brand-soft text-brand">
          <MessageSquareText size={22} />
        </div>
        <p className="mt-4 font-semibold text-ink">还没有加载聊天记录</p>
        <p className="mt-2 max-w-sm text-sm leading-6 text-muted">
          接入会话列表后，可在这里按更新时间浏览历史问答。
        </p>
        <div className="mt-4 flex items-center gap-2 text-xs text-slate-400">
          <Clock3 size={14} />
          按最近更新排序
        </div>
      </div>
    </section>
  );
}

function App() {
  const [activeItem, setActiveItem] = useState<NavigationKey>("chat");

  const renderContent = () => {
    switch (activeItem) {
      case "knowledge":
        return <KnowledgeBasePanel />;
      case "documents":
        return <DocumentPanel />;
      case "questions":
        return <InterviewPanel mode="questions" />;
      case "evaluation":
        return <InterviewPanel mode="evaluation" />;
      case "history":
        return <ChatHistoryPlaceholder />;
      case "chat":
      default:
        return <ChatPanel />;
    }
  };

  return (
    <AppShell
      activeItem={activeItem}
      title={pageTitles[activeItem]}
      onNavigate={setActiveItem}
      referencePanel={<ReferencePanel />}
    >
      {renderContent()}
    </AppShell>
  );
}

export default App;
