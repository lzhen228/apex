# Apex Web - 前端应用

> **版本**: v1.0.0 &nbsp;|&nbsp; **最后更新**: 2026-03-30

---

## 目录

- [模块简介](#模块简介)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [开发指南](#开发指南)
- [构建部署](#构建部署)
- [浏览器兼容性](#浏览器兼容性)

---

## 模块简介

**Apex Web** 是 Apex 早期靶点情报分析智能体的前端应用，基于 React 19 + TypeScript 构建，提供靶点组合竞争格局和研发进展格局的可视化分析界面。

### 核心特性

- **现代化 UI 设计**：深色科技风格，专业数据分析体验
- **高性能表格渲染**：AG Grid 虚拟滚动，支持万级单元格
- **智能缓存策略**：React Query 自动缓存，减少重复请求
- **响应式布局**：适配不同屏幕尺寸
- **类型安全**：TypeScript 全栈类型支持

---

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| React | 19 | UI 框架 |
| TypeScript | 5.x | 类型安全 |
| Vite | 6.3.5 | 构建工具 |
| Ant Design | 5.x | UI 组件库 |
| AG Grid | 31.x | 表格渲染引擎 |
| ECharts | 5.x | 图表库 |
| React Query | 5.x | 服务端状态管理 |
| Zustand | 4.x | 客户端状态管理 |
| Axios | 1.x | HTTP 客户端 |
| clsx / classnames | ^2 | 类名工具 |
| dayjs | ^1 | 日期处理 |

---

## 项目结构

```
apex-web/
├── public/                     # 静态资源
│   ├── favicon.ico
│   └── logo.png
├── src/
│   ├── main.tsx                # 应用入口
│   ├── App.tsx                 # 根组件
│   └── vite-env.d.ts           # Vite 类型声明
├── src/assets/                 # 资源文件
│   ├── images/
│   └── icons/
├── src/components/             # 通用组件
│   ├── common/
│   │   ├── Layout/             # 布局组件
│   │   │   ├── Header.tsx      # 顶部导航
│   │   │   ├── Sidebar.tsx     # 侧边栏
│   │   │   └── index.tsx
│   │   ├── Button/
│   │   ├── Modal/
│   │   └── Table/
│   ├── competition/
│   │   ├── MatrixTable.tsx    # 矩阵表格
│   │   ├── DiseaseTreeSelect.tsx # 疾病树形选择
│   │   ├── PhaseFilter.tsx    # 研发阶段筛选
│   │   └── CellTooltip.tsx    # 格子悬浮提示
│   └── progress/
│       ├── PipelineChart.tsx  # 管线全景图
│       ├── DiseaseSelect.tsx  # 疾病选择
│       └── TargetSelect.tsx   # 靶点选择
├── src/pages/                  # 页面组件
│   ├── Competition/            # 竞争格局页面
│   │   ├── index.tsx
│   │   ├── useCompetitionQuery.ts
│   │   └── components/
│   ├── Progress/               # 研发进展页面
│   │   ├── index.tsx
│   │   ├── useProgressQuery.ts
│   │   └── components/
│   └── Login/                  # 登录页面
│       └── index.tsx
├── src/services/               # API 服务
│   ├── api.ts                  # Axios 实例配置
│   ├── auth.service.ts         # 认证服务
│   ├── competition.service.ts  # 竞争格局服务
│   ├── progress.service.ts     # 研发进展服务
│   └── disease.service.ts      # 疾病服务
├── src/store/                  # 状态管理
│   ├── useAuthStore.ts         # 认证状态
│   ├── useCompetitionStore.ts  # 竞争格局状态
│   └── useProgressStore.ts     # 研发进展状态
├── src/types/                  # 类型定义
│   ├── api.ts                  # API 响应类型
│   ├── competition.ts          # 竞争格局类型
│   ├── progress.ts             # 研发进展类型
│   └── common.ts               # 通用类型
├── src/hooks/                  # 自定义 Hooks
│   ├── useAuth.ts              # 认证 Hook
│   ├── useDebounce.ts          # 防抖 Hook
│   └── useWindowSize.ts        # 窗口大小 Hook
├── src/utils/                  # 工具函数
│   ├── request.ts              # 请求封装
│   ├── format.ts               # 格式化工具
│   └── validation.ts           # 表单验证
├── src/styles/                 # 样式文件
│   ├── global.css              # 全局样式
│   ├── variables.css           # CSS 变量
│   └── themes/                 # 主题样式
│       ├── dark.css
│       └── light.css
├── index.html                  # HTML 模板
├── package.json                # 依赖配置
├── tsconfig.json               # TypeScript 配置
├── vite.config.ts              # Vite 配置
└── .env.dummy                  # 环境变量模板
```

---

## 快速开始

### 环境要求

- Node.js 20+
- npm 10+ 或 pnpm 9+

### 安装依赖

```bash
# 进入前端目录
cd apex-web

# 使用 npm 安装
npm install

# 或使用 pnpm（推荐）
pnpm install
```

### 本地开发

```bash
# 开发模式启动（热更新）
npm run dev

# 或
pnpm dev
```

访问 http://localhost:3000

### 代码检查

```bash
# TypeScript 类型检查
npm run type-check

# ESLint 代码检查
npm run lint

# 自动修复可修复的问题
npm run lint:fix

# Prettier 格式化
npm run format
```

---

## 配置说明

### .env 环境变量

创建 `.env` 文件（参考 `.env.dummy`）：

```bash
# API 基础地址
VITE_API_BASE_URL=http://localhost:8080/api/v1

# 应用标题
VITE_APP_TITLE=Apex 早期靶点情报分析智能体

# 启用开发工具
VITE_DEV_TOOLS=true

# 矩阵表格每页行数
VITE_MATRIX_PAGE_SIZE=50

# 管线图表每页行数
VITE_PIPELINE_PAGE_SIZE=30
```

### .env.production（生产环境）

```bash
VITE_API_BASE_URL=https://ai-harby.harbourbiomed.com/api/v1
VITE_APP_TITLE=Apex 早期靶点情报分析智能体
VITE_DEV_TOOLS=false
VITE_MATRIX_PAGE_SIZE=100
VITE_PIPELINE_PAGE_SIZE=50
```

### Vite 配置 (vite.config.ts)

```typescript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  
  // 路径别名
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
      '@components': path.resolve(__dirname, './src/components'),
      '@pages': path.resolve(__dirname, './src/pages'),
      '@services': path.resolve(__dirname, './src/services'),
      '@store': path.resolve(__dirname, './src/store'),
      '@types': path.resolve(__dirname, './src/types'),
      '@hooks': path.resolve(__dirname, './src/hooks'),
      '@utils': path.resolve(__dirname, './src/utils'),
    },
  },
  
  // 开发服务器配置
  server: {
    port: 3000,
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  
  // 构建配置
  build: {
    outDir: 'dist',
    sourcemap: false,
    minify: 'terser',
    tersrollupOptions: {
      compress: {
        drop_console: true,
      },
    },
    rollupOptions: {
      output: {
        manualChunks: {
          'react-vendor': ['react', 'react-dom'],
          'antd-vendor': ['antd', '@ant-design/icons'],
          'ag-grid-vendor': ['ag-grid-community'],
          'echarts-vendor': ['echarts'],
        },
      },
    },
  },
})
```

### API 代理配置

开发环境通过 Vite Proxy 代理 API 请求，避免 CORS 问题：

```typescript
// vite.config.ts
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      // 重写路径（可选）
      // rewrite: (path) => path.replace(/^\/api/, '/api/v1'),
    },
  },
}
```

---

## 开发指南

### 组件开发规范

#### 函数组件 + Hooks

```typescript
// ✅ 推荐
import { useState, useEffect } from 'react'
import type { MatrixData } from '@/types/competition'

interface MatrixTableProps {
  data: MatrixData
  loading?: boolean
  onCellHover?: (target: string, diseaseId: number) => void
}

export const MatrixTable: React.FC<MatrixTableProps> = ({
  data,
  loading = false,
  onCellHover,
}) => {
  const [selectedCell, setSelectedCell] = useState<{ target: string; diseaseId: number } | null>(null)
  
  useEffect(() => {
    // 组件逻辑
  }, [data])
  
  return (
    <div className="matrix-container">
      {/* 渲染逻辑 */}
    </div>
  )
}
```

#### 组件命名

- 文件名：`PascalCase.tsx`（如 `MatrixTable.tsx`）
- 组件名：`PascalCase`（如 `MatrixTable`）
- 样式文件：与组件同名（如 `MatrixTable.module.css`）

### 状态管理

#### 使用 Zustand 管理客户端状态

```typescript
// store/useCompetitionStore.ts
import { create } from 'zustand'
import type { FilterState } from '@/types/competition'

interface CompetitionStore {
  filters: FilterState
  updateFilters: (filters: Partial<FilterState>) => void
  resetFilters: () => void
}

const initialFilters: FilterState = {
  diseaseIds: [],
  phases: [],
  hideNoComboTargets: false,
}

export const useCompetitionStore = create<CompetitionStore>((set) => ({
  filters: initialFilters,
  updateFilters: (newFilters) => 
    set((state) => ({ 
      filters: { ...state.filters, ...newFilters } 
    })),
  resetFilters: () => set({ filters: initialFilters }),
}))
```

#### 使用 React Query 管理服务端状态

```typescript
// hooks/useCompetitionQuery.ts
import { useQuery, useMutation } from '@tanstack/react-query'
import { competitionService } from '@/services/competition.service'
import { useCompetitionStore } from '@/store/useCompetitionStore'

export const useCompetitionQuery = () => {
  const { filters } = useCompetitionStore()
  
  // 查询矩阵数据
  const query = useQuery({
    queryKey: ['competition', 'matrix', filters],
    queryFn: () => competitionService.queryMatrix(filters),
    staleTime: 5 * 60 * 1000, // 5 分钟
    gcTime: 30 * 60 * 1000,   // 30 分钟
  })
  
  return {
    ...query,
  }
}
```

### API 调用规范

#### 服务层封装

```typescript
// services/competition.service.ts
import request from '@/utils/request'
import type { MatrixQueryRequest, MatrixQueryResponse } from '@/types/competition'

export const competitionService = {
  /**
   * 查询靶点组合竞争格局矩阵
   */
  async queryMatrix(params: MatrixQueryRequest): Promise<MatrixQueryResponse> {
    return request.post('/competition/matrix', params)
  },
  
  /**
   * 获取格子药品详情
   */
  async getCellDrugs(
    target: string, 
    diseaseId: number, 
    phases: string[]
  ): Promise<CellDrugsResponse> {
    return request.get('/competition/cell-drugs', {
      params: { target, diseaseId, phases }
    })
  },
}
```

#### 请求工具封装

```typescript
// utils/request.ts
import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { message } from 'antd'
import { useAuthStore } from '@/store/useAuthStore'

const instance: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
instance.interceptors.request.use(
  (config) => {
    const { token } = useAuthStore.getState()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
instance.interceptors.response.use(
  (response: AxiosResponse) => {
    const { code, message: msg, data } = response.data
    if (code === 0) {
      return data
    } else {
      message.error(msg || '请求失败')
    }
  },
  (error) => {
    if (error.response?.status === 401) {
      // 处理未授权
      useAuthStore.getState().logout()
    }
    message.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default instance
```

---

## 构建部署

### 本地构建

```bash
# 生产构建
npm run build

# 预览构建结果
npm run preview
```

构建产物在 `dist/` 目录下，可直接部署到 Nginx 等静态服务器。

### Docker 镜像构建

```dockerfile
# Dockerfile
FROM node:20-alpine AS builder

WORKDIR /app
COPY package.json pnpm-lock.yaml ./
RUN npm install -g pnpm && pnpm install

COPY . .
RUN pnpm build

FROM nginx:1.25-alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### Nginx 配置示例

```nginx
server {
    listen 80;
    server_name localhost;
    
    root /usr/share/nginx/html;
    index index.html;
    
    # Gzip 压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml;
    
    # SPA 路由支持
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # API 反向代理
    location /api/ {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

---

## 浏览器兼容性

| 浏览器 | 支持版本 | 说明 |
|--------|----------|------|
| Chrome | 90+ | 完全支持 |
| Firefox | 88+ | 完全支持 |
| Safari | 14+ | 完全支持 |
| Edge | 90+ | 完全支持 |
| IE | ❌ | 不支持 |

---

**和铂医药（Harbour BioMed）** © 2026
