import {
  BookOpen,
  BrainCircuit,
  FileStack,
  History,
  MessageSquareText,
  NotebookPen,
  PanelLeft,
  Sparkles,
} from "lucide-react";

export type NavigationKey =
  | "knowledge"
  | "documents"
  | "chat"
  | "questions"
  | "evaluation"
  | "history";

interface SidebarProps {
  activeItem: NavigationKey;
  onNavigate: (item: NavigationKey) => void;
}

const navigationItems = [
  { key: "knowledge", label: "知识库", icon: BookOpen },
  { key: "documents", label: "文档管理", icon: FileStack },
  { key: "chat", label: "RAG 问答", icon: MessageSquareText },
  { key: "questions", label: "面试题生成", icon: NotebookPen },
  { key: "evaluation", label: "答案评分", icon: BrainCircuit },
  { key: "history", label: "聊天记录", icon: History },
] satisfies Array<{
  key: NavigationKey;
  label: string;
  icon: typeof BookOpen;
}>;

export function Sidebar({ activeItem, onNavigate }: SidebarProps) {
  return (
    <aside className="sidebar">
      <div className="flex items-center gap-3 px-5 pb-6 pt-5">
        <div className="grid size-10 shrink-0 place-items-center rounded-lg bg-ink text-white shadow-soft">
          <Sparkles size={19} strokeWidth={2.2} />
        </div>
        <div className="min-w-0">
          <p className="font-display text-[16px] font-semibold leading-5 text-ink">
            <span>AI Interview</span>
            <span className="md:block"> Coach</span>
          </p>
          <p className="mt-0.5 font-mono text-[10px] uppercase text-muted">
            Java RAG Workspace
          </p>
        </div>
      </div>

      <nav className="flex gap-1 overflow-x-auto px-3 pb-4 md:block md:space-y-1 md:overflow-visible">
        {navigationItems.map(({ key, label, icon: Icon }) => {
          const isActive = activeItem === key;
          return (
            <button
              key={key}
              type="button"
              className={`nav-item ${isActive ? "nav-item-active" : ""}`}
              onClick={() => onNavigate(key)}
              aria-current={isActive ? "page" : undefined}
            >
              <Icon size={18} strokeWidth={1.9} />
              <span>{label}</span>
            </button>
          );
        })}
      </nav>

      <div className="mt-auto hidden px-4 pb-5 md:block">
        <div className="border-t border-line pt-4">
          <div className="flex items-center gap-2 text-xs text-muted">
            <PanelLeft size={15} />
            <span>MVP 前端骨架</span>
          </div>
        </div>
      </div>
    </aside>
  );
}
