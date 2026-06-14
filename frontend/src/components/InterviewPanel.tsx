import {
  BadgeCheck,
  BrainCircuit,
  ClipboardCheck,
  Sparkles,
} from "lucide-react";

interface InterviewPanelProps {
  mode: "questions" | "evaluation";
}

export function InterviewPanel({ mode }: InterviewPanelProps) {
  const isQuestionMode = mode === "questions";

  return (
    <section>
      <div className="mb-6">
        <p className="section-kicker">
          {isQuestionMode ? "Interview builder" : "Answer review"}
        </p>
        <h2 className="section-title">
          {isQuestionMode ? "生成有依据的面试题" : "让每次回答都有改进方向"}
        </h2>
        <p className="section-description">
          {isQuestionMode
            ? "围绕主题检索知识库，再由 DeepSeek 生成结构化题目。"
            : "结合题目、参考答案与知识库资料进行评分。"}
        </p>
      </div>

      <div className="surface p-5 sm:p-7">
        {isQuestionMode ? (
          <div className="grid gap-5 lg:grid-cols-[1fr_220px]">
            <label className="field-label">
              面试主题
              <input
                className="field-input"
                placeholder="例如：Java IO、并发编程"
              />
            </label>
            <label className="field-label">
              难度
              <select className="field-input">
                <option value="easy">基础</option>
                <option value="medium">进阶</option>
                <option value="hard">深入</option>
              </select>
            </label>
          </div>
        ) : (
          <div className="grid gap-5">
            <label className="field-label">
              面试题
              <input className="field-input" placeholder="输入需要评价的面试题" />
            </label>
            <label className="field-label">
              你的回答
              <textarea
                className="field-input min-h-36 resize-y"
                placeholder="输入你的作答内容"
              />
            </label>
          </div>
        )}

        <div className="mt-6 flex flex-wrap items-center justify-between gap-3 border-t border-line pt-5">
          <div className="flex items-center gap-2 text-xs text-muted">
            {isQuestionMode ? (
              <BrainCircuit size={16} className="text-brand" />
            ) : (
              <ClipboardCheck size={16} className="text-mint" />
            )}
            将结合知识库引用生成结果
          </div>
          <button type="button" className="primary-button">
            {isQuestionMode ? <Sparkles size={16} /> : <BadgeCheck size={16} />}
            {isQuestionMode ? "生成面试题" : "开始评分"}
          </button>
        </div>
      </div>
    </section>
  );
}
