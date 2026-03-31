import type { Config } from 'tailwindcss'

export default {
  content: [
    './index.html',
    './src/**/*.{js,ts,jsx,tsx}',
  ],
  theme: {
    extend: {
      colors: {
        // 主色调
        background: '#f8fafc',
        'table-header': '#f1f5f9',
        border: '#e2e8f0',
        // 阶段颜色
        'phase-approved': '#10B981',
        'phase-bla': '#06B6D4',
        'phase-3': '#3B82F6',
        'phase-23': '#6366F1',
        'phase-2': '#8B5CF6',
        'phase-12': '#A855F7',
        'phase-1': '#D946EF',
        'phase-ind': '#F59E0B',
        'phase-preclinical': '#6B7280',
      },
    },
  },
  plugins: [],
} satisfies Config
