# Apex 前端项目

和铂医药的药物早期研发靶点情报分析平台前端。

## 技术栈

- React 18.3.1
- TypeScript 5.5.0+
- Ant Design 5.26.2
- React Router 6.30.1
- Tailwind CSS 3.4.1
- Zustand 4.5.5
- Day.js 1.11.13
- Vite 6.0.5
- React Query 5.64.0

## 项目结构

```
apex-frontend/
├── src/
│   ├── components/        # 通用组件
│   │   └── common/
│   │       └── TooltipCard.tsx
│   ├── services/          # API 服务
│   │   └── api.ts
│   ├── stores/            # 状态管理
│   │   └── comboFilterStore.ts
│   ├── types/             # TypeScript 类型定义
│   │   └── index.ts
│   ├── utils/             # 工具函数
│   │   └── api.ts
│   ├── views/             # 页面组件
│   │   └── TargetCombo/   # 靶点组合竞争格局页面
│   │       ├── FilterBar.tsx       # 筛选条件组件
│   │       ├── PhaseFilterBar.tsx  # 阶段筛选条组件
│   │       ├── MatrixTable.tsx     # 矩阵表格组件
│   │       ├── DrugCard.tsx        # 药物卡片组件
│   │       ├── index.tsx           # 主页面组件
│   │       ├── useMatrixData.ts     # 数据获取 Hook
│   │       ├── usePreset.ts         # 预设管理 Hook
│   │       └── exportMatrix.ts     # 导出功能
│   ├── App.tsx
│   ├── main.tsx
│   └── index.css
├── package.json
├── tsconfig.json
├── vite.config.ts
├── tailwind.config.ts
└── postcss.config.ts
```

## 安装依赖

```bash
npm install
```

## 运行开发服务器

```bash
npm run dev
```

前端将运行在 `http://localhost:3000`

## 构建生产版本

```bash
npm run build
```

## 功能特性

### 靶点组合竞争格局页面

- **Banner 区域**：显示当前筛选范围摘要
- **Tab 切换**：矩阵视图
- **预设选择器**：下拉选择已保存的预设
- **筛选条件区域**：
  - 疾病筛选（多选下拉，支持树形结构）
  - 靶点筛选（标签输入，支持添加/删除）
  - 阶段筛选（多选下拉）
  - 起源公司筛选（标签输入，支持添加/删除）
  - 排序方式选择
  - 查询按钮
  - 保存预设按钮
- **阶段筛选条**：
  - 固定显示 9 个研发阶段（Approved 到 PreClinical）
  - 每个阶段显示阶段名称和药物数量
  - 点击阶段可筛选/取消筛选
- **矩阵表格**：
  - 固定列：疾病名称、靶点组合
  - 动态列：9 个研发阶段
  - 每行数据包含疾病名称、靶点组合、各阶段药物数量、综合评分
  - 单元格交互：
    - 悬停显示 Tooltip（该阶段的药物列表）
    - 点击单元格弹出药物列表
  - 列表功能：可配置列显示/隐藏、导出数据、分页
- **药物详情弹窗**：
  - 药物名称（中英文）
  - 研发阶段（带颜色标识）
  - 起源公司
  - 作用机制（MOA）
  - 最高临床试验信息
  - NCT ID
  - 点击查看详情

## API 接口

前端通过 `/api/v1` 路径访问后端 API，主要接口：

- `GET /api/v1/diseases/tree` - 获取疾病树
- `POST /api/v1/competition/matrix` - 获取矩阵数据
- `GET /api/v1/competition/cell-drugs` - 获取单元格药物列表
- `POST /api/v1/competition/export` - 导出矩阵数据
- `GET /api/v1/filter-presets` - 获取筛选预设列表
- `POST /api/v1/filter-presets` - 保存筛选预设
- `DELETE /api/v1/filter-presets/:id` - 删除筛选预设

## 样式主题

- 背景色：#f8fafc
- 表头背景色：#f1f5f9
- 边框色：#e2e8f0

阶段颜色：
- Approved：#10B981（绿色）
- BLA：#06B6D4（青色）
- Phase III：#3B82F6（蓝色）
- Phase II/III：#6366F1（靛蓝色）
- Phase II：#8B5CF6（紫色）
- Phase I/II：#A855F7（浅紫色）
- Phase I：#D946EF（粉紫色）
- IND：#F59E0B（橙色）
- PreClinical：#6B7280（灰色）

## 开发说明

### 状态管理

使用 Zustand 进行状态管理，`comboFilterStore.ts` 存储筛选条件状态。

### 数据获取

使用 React Query 进行数据获取和缓存，`useMatrixData.ts` 提供矩阵数据获取 Hook。

### 预设管理

`usePreset.ts` 提供预设的保存、加载和删除功能。

### 导出功能

`exportMatrix.ts` 提供矩阵数据导出为 Excel 的功能。
