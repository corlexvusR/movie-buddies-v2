/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  darkMode: ["class"],
  theme: {
    extend: {
      colors: {
        // 액센트 컬러
        "accent-primary": "rgb(var(--color-accent-primary) / <alpha-value>)",
        "accent-secondary":
          "rgb(var(--color-accent-secondary) / <alpha-value>)",
        "accent-success": "rgb(var(--color-accent-success) / <alpha-value>)",
        "accent-warning": "rgb(var(--color-accent-warning) / <alpha-value>)",

        // 배경 컬러
        "background-primary":
          "rgb(var(--color-background-primary) / <alpha-value>)",
        "background-secondary":
          "rgb(var(--color-background-secondary) / <alpha-value>)",
        "background-tertiary":
          "rgb(var(--color-background-tertiary) / <alpha-value>)",

        // 텍스트 컬러
        "text-primary": "rgb(var(--color-text-primary) / <alpha-value>)",
        "text-secondary": "rgb(var(--color-text-secondary) / <alpha-value>)",
        "text-muted": "rgb(var(--color-text-muted) / <alpha-value>)",

        // 보더 컬러
        "border-primary": "rgb(var(--color-border-primary) / <alpha-value>)",
        "border-secondary":
          "rgb(var(--color-border-secondary) / <alpha-value>)",
      },
      fontFamily: {
        sans: ["Noto Sans KR", "sans-serif"],
      },
      animation: {
        "fade-in": "fade-in 0.2s ease-out",
        "slide-up": "slide-up 0.2s ease-out",
        "zoom-in-95": "zoom-in 0.2s ease-out",
      },
      keyframes: {
        "fade-in": {
          "0%": { opacity: "0" },
          "100%": { opacity: "1" },
        },
        "slide-up": {
          "0%": { opacity: "0", transform: "translateY(10px)" },
          "100%": { opacity: "1", transform: "translateY(0)" },
        },
        "zoom-in": {
          "0%": { opacity: "0", transform: "scale(0.95)" },
          "100%": { opacity: "1", transform: "scale(1)" },
        },
      },
    },
  },
  plugins: [],
};
