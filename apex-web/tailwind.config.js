/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Ant Design 主题色
        primary: '#1890ff',
        success: '#52c41a',
        warning: '#faad14',
        error: '#f5222d',
        info: '#13c2c2',
        
        // 靶点研发阶段颜色（参考 TECH_SPEC.md 附录 B）
        'phase-approved': '#52c41a',
        'phase-3': '#52c41a',
        'phase-2': '#13c2c2',
        'phase-1': '#1890ff',
        'phase-0': '#faad14',
        'phase-preclinical': '#fa8c16',
        'phase-discovery': '#f5222d',
        
        // 自定义品牌色
        'brand-dark': '#001529',
        'brand-light': '#e6f7ff',
      },
    },
  },
  plugins: [],
}
