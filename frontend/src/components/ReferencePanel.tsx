import { BookOpenCheck, FileText, Quote, ScanSearch } from "lucide-react";

const sampleReferences = [
  {
    id: 1,
    source: "java-io-notes.md",
    score: "0.92",
    content:
      "InputStream 是所有字节输入流的抽象父类，Reader 则面向字符数据并处理字符编码。",
  },
  {
    id: 2,
    source: "interview-handbook.txt",
    score: "0.87",
    content:
      "BufferedInputStream 通过内部缓冲区减少底层读取次数，适合频繁的小块读取场景。",
  },
];

export function ReferencePanel() {
  return (
    <div className="sticky top-[92px]">
      <div className="mb-5 flex items-center justify-between">
        <div>
          <p className="section-kicker">Retrieval context</p>
          <h2 className="mt-1 text-base font-semibold text-ink">引用片段</h2>
        </div>
        <div
          className="grid size-9 place-items-center rounded-lg border border-line bg-white text-brand"
          title="向量检索结果"
        >
          <ScanSearch size={17} />
        </div>
      </div>

      <div className="space-y-3">
        {sampleReferences.map((reference, index) => (
          <article key={reference.id} className="reference-card">
            <div className="mb-3 flex items-center justify-between gap-2">
              <span className="flex min-w-0 items-center gap-2 text-xs font-semibold text-ink">
                <FileText size={14} className="shrink-0 text-coral" />
                <span className="truncate">{reference.source}</span>
              </span>
              <span className="rounded-md bg-emerald-50 px-2 py-1 font-mono text-[10px] font-semibold text-emerald-700">
                {reference.score}
              </span>
            </div>
            <div className="flex gap-2.5">
              <Quote size={15} className="mt-1 shrink-0 text-slate-300" />
              <p className="text-xs leading-6 text-muted">{reference.content}</p>
            </div>
            <p className="mt-3 border-t border-slate-100 pt-2 font-mono text-[10px] text-slate-400">
              CHUNK {String(index + 1).padStart(2, "0")}
            </p>
          </article>
        ))}
      </div>

      <div className="mt-4 flex items-center gap-2 rounded-lg border border-dashed border-slate-300 px-3 py-3 text-xs text-muted">
        <BookOpenCheck size={16} className="text-mint" />
        后续由 references 事件实时更新
      </div>
    </div>
  );
}
