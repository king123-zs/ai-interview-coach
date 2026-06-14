import {
  Check,
  FileUp,
  Layers3,
  ScanText,
  UploadCloud,
  Waypoints,
} from "lucide-react";

const pipeline = [
  { label: "上传", icon: UploadCloud, color: "text-brand bg-brand-soft" },
  { label: "解析", icon: ScanText, color: "text-coral bg-orange-50" },
  { label: "切片", icon: Layers3, color: "text-mint bg-emerald-50" },
  { label: "向量化", icon: Waypoints, color: "text-sky-700 bg-sky-50" },
];

export function DocumentPanel() {
  return (
    <section>
      <div className="mb-6">
        <p className="section-kicker">Document pipeline</p>
        <h2 className="section-title">从资料到可检索向量</h2>
        <p className="section-description">
          第一版支持 TXT 与 Markdown，上传操作将在下一步连接真实接口。
        </p>
      </div>

      <div className="surface p-5 sm:p-7">
        <label className="flex min-h-56 cursor-pointer flex-col items-center justify-center rounded-lg border border-dashed border-slate-300 bg-[#fbfcfe] px-6 text-center transition hover:border-brand hover:bg-brand-soft/30">
          <input type="file" accept=".txt,.md" className="sr-only" />
          <div className="grid size-12 place-items-center rounded-lg bg-white text-brand shadow-soft">
            <FileUp size={23} />
          </div>
          <span className="mt-4 font-semibold text-ink">选择或拖入学习资料</span>
          <span className="mt-2 text-sm text-muted">TXT、Markdown · 单文件上传</span>
        </label>
      </div>

      <div className="mt-6">
        <div className="mb-3 flex items-center gap-2 text-sm font-semibold text-ink">
          <Check size={16} className="text-mint" />
          处理流程
        </div>
        <div className="grid gap-3 sm:grid-cols-4">
          {pipeline.map(({ label, icon: Icon, color }, index) => (
            <div
              key={label}
              className="flex items-center gap-3 rounded-lg border border-line bg-white px-4 py-4 shadow-soft"
            >
              <div className={`grid size-9 shrink-0 place-items-center rounded-lg ${color}`}>
                <Icon size={17} />
              </div>
              <div>
                <p className="font-mono text-[10px] text-slate-400">
                  STEP {index + 1}
                </p>
                <p className="mt-0.5 text-sm font-semibold text-ink">{label}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
