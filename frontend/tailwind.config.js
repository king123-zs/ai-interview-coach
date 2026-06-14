/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        canvas: "#f5f7fb",
        ink: "#172033",
        muted: "#667085",
        line: "#dde3ec",
        brand: "#4f46e5",
        "brand-soft": "#eef2ff",
        mint: "#0f9f7f",
        coral: "#e46f51",
      },
      boxShadow: {
        panel: "0 12px 32px rgba(36, 48, 72, 0.08)",
        soft: "0 4px 14px rgba(36, 48, 72, 0.06)",
      },
      fontFamily: {
        sans: [
          '"Segoe UI Variable"',
          '"Noto Sans SC"',
          '"Microsoft YaHei"',
          "sans-serif",
        ],
        display: ['Georgia', '"Noto Serif SC"', "serif"],
        mono: ['"Cascadia Code"', '"SFMono-Regular"', "monospace"],
      },
    },
  },
  plugins: [],
};
