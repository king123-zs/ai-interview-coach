import { CircleDot, GitFork, PanelTop } from "lucide-react";

interface TopBarProps {
  title: string;
}

export function TopBar({ title }: TopBarProps) {
  return (
    <header className="topbar">
      <div className="min-w-0">
        <div className="flex items-center gap-2 text-xs font-medium text-muted">
          <PanelTop size={14} />
          <span>AI 面试辅导工作台</span>
        </div>
        <h1 className="mt-1 truncate font-display text-xl font-semibold text-ink">
          {title}
        </h1>
      </div>

      <div className="flex shrink-0 items-center gap-2">
        <div
          className="flex size-10 items-center justify-center gap-2 rounded-lg border border-amber-200 bg-amber-50 text-xs font-medium text-amber-700 sm:h-10 sm:w-auto sm:px-3"
          title="后续通过健康检查接口更新连接状态"
        >
          <CircleDot size={14} />
          <span className="hidden sm:inline">后端待连接</span>
        </div>
        <a
          href="#"
          onClick={(event) => event.preventDefault()}
          className="icon-button"
          aria-label="GitHub 项目链接待配置"
          title="GitHub 项目链接待配置"
        >
          <GitFork size={18} />
        </a>
      </div>
    </header>
  );
}
