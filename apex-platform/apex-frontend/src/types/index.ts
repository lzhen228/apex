// 疾病相关类型
export interface Disease {
  id: number
  nameEn: string
  nameCn: string
  parentId?: number
  children?: Disease[]
}

export interface TherapeuticArea {
  id: number
  nameEn: string
  nameCn: string
  children?: Disease[]
}

// 靶点组合竞争格局相关类型
export interface MatrixDrug {
  id: {
    name: string
    nameEn: string
  }
  global_highest_phase: string
  global_highest_phase_score: number
  originator: string
  moa: string
  highest_trial_id: string
  nct_id: string
  indication_top_global_start_date?: string
  research_institute?: string
}

export interface MatrixRequest {
  diseaseIds?: number[]
  targets?: string[]
  phases?: string[]
  origins?: string[]
  sortBy?: string
}

export interface MatrixCellData {
  diseaseName: string
  targets: string[]
  approved: MatrixDrug[]
  bla: MatrixDrug[]
  phase3: MatrixDrug[]
  phase23: MatrixDrug[]
  phase2: MatrixDrug[]
  phase12: MatrixDrug[]
  phase1: MatrixDrug[]
  ind: MatrixDrug[]
  preClinical: MatrixDrug[]
  totalCount: number
  score: number
}

export interface MatrixResponse {
  data: MatrixCellData[]
  summary: {
    approved: number
    bla: number
    phase3: number
    phase23: number
    phase2: number
    phase12: number
    phase1: number
    ind: number
    preClinical: number
    total: number
  }
}

export interface CellDrugsResponse {
  drugs: MatrixDrug[]
}

export interface ExportRequest {
  diseaseIds?: number[]
  targets?: string[]
  phases?: string[]
  origins?: string[]
  sortBy?: string
  format?: 'excel'
}

// 筛选预设相关类型
export interface FilterPreset {
  id: number
  name: string
  diseaseIds: number[]
  targets: string[]
  phases: string[]
  origins: string[]
  sortBy: string
  createdAt: string
  updatedAt: string
}

export interface SavePresetRequest {
  name: string
  diseaseIds: number[]
  targets: string[]
  phases: string[]
  origins: string[]
  sortBy: string
}

export interface PresetListResponse {
  presets: FilterPreset[]
}
