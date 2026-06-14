import { BookOpen, Plus, Search, Sparkles } from "lucide-react";

export function KnowledgeBasePanel() {
  return (
    <section>
      <div className="mb-6 flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="section-kicker">Knowledge workspace</p>
          <h2 className="section-title">组织你的面试资料</h2>
          <p className="section-description">
            知识库列表将在下一步接入 GET /api/kb/list。
          </p>
        </div>
        <button type="button" className="primary-button">
          <Plus size={16} />
          新建知识库
        </button>
      </div>

      <div className="mb-5 flex items-center gap-2 rounded-lg border border-line bg-white px-3 py-2.5 shadow-soft">
        <Search size={17} className="text-muted" />
        <input
          className="min-w-0 flex-1 border-0 bg-transparent text-sm text-ink outline-none placeholder:text-slate-400"
          placeholder="搜索知识库"
          aria-label="搜索知识库"
        />
      </div>

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
        <article className="surface p-5">
          <div className="mb-5 flex items-start justify-between gap-4">
            <div className="grid size-10 place-items-center rounded-lg bg-brand-soft text-brand">
              <BookOpen size={19} />
            </div>
            <span className="status-badge bg-emerald-50 text-emerald-700">
              示例
            </span>
          </div>
          <h3 className="font-semibold text-ink">Java 核心知识</h3>
          <p className="mt-2 min-h-12 text-sm leading-6 text-muted">
            用于展示知识库卡片结构，接入接口后替换为真实资料。
          </p>
          <div className="mt-5 flex items-center justify-between border-t border-line pt-4 text-xs text-muted">
            <span>文档数 --</span>
            <span>最近更新 --</span>
          </div>
        </article>

        <button
          type="button"
          className="min-h-52 rounded-lg border border-dashed border-slate-300 bg-white/50 p-5 text-left transition hover:border-brand hover:bg-brand-soft/40"
        >
          <Sparkles size={20} className="text-brand" />
          <p className="mt-4 font-semibold text-ink">创建第一个真实知识库</p>
          <p className="mt-2 text-sm leading-6 text-muted">
            表单结构已预留，下一阶段连接创建接口。
          </p>
        </button>
      </div>
    </section>
  );
}
