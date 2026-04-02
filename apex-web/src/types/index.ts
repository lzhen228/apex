/* ============================================================
   Common
   ============================================================ */
export interface Result<T = any> {
  code: number
  message: string
  data: T
  traceId?: string
  timestamp?: number
}

/* ============================================================
   Auth
   ============================================================ */
export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken?: string
  expiresIn?: number
  tokenType?: string
  user: {
    id: number
    username: string
    displayName: string
  }
}

export interface UserInfo {
  id: number
  username: string
  displayName: string
}

/* ============================================================
   Disease tree
   ============================================================ */
export interface DiseaseNode {
  id: number
  label: string
  children?: DiseaseNode[]
}

export interface DiseaseItem {
  id: number
  label: string
  taId: number
  taLabel: string
}

/* ============================================================
   Phase helpers
   ============================================================ */
export const ALL_PHASES = [
  '批准上市',
  '申请上市',
  'III期临床',
  'II/III期临床',
  'II期临床',
  'I/II期临床',
  'I期临床',
  '申报临床',
  '临床前',
] as const

export type PhaseName = (typeof ALL_PHASES)[number]

export function scoreToClass(score: number): string {
  if (score >= 4.0) return 'score-4'
  if (score >= 3.5) return 'score-3-5'
  if (score >= 3.0) return 'score-3'
  if (score >= 2.5) return 'score-2-5'
  if (score >= 2.0) return 'score-2'
  if (score >= 1.5) return 'score-1-5'
  if (score >= 1.0) return 'score-1'
  if (score >= 0.5) return 'score-0-5'
  return 'score-0-1'
}

export function scoreToColor(score: number): string {
  if (score >= 4.0) return '#ef4444'
  if (score >= 3.5) return '#a855f7'
  if (score >= 3.0) return '#3b82f6'
  if (score >= 2.5) return '#0ea5e9'
  if (score >= 2.0) return '#06b6d4'
  if (score >= 1.5) return '#14b8a6'
  if (score >= 1.0) return '#10b981'
  if (score >= 0.5) return '#f59e0b'
  return '#6b7280'
}

export function phaseToCardClass(phase: string): string {
  const p = phase.toLowerCase()
  if (p.includes('批准') || p.includes('approved') || p.includes('market')) return 'dc-approved'
  if (p.includes('申请') || p.includes('bla') || p.includes('nda')) return 'dc-bla'
  if (p.includes('ii/iii') || p.includes('2/3')) return 'dc-p2p3'
  if (p.includes('iii') || p === 'phase iii' || p === '3') return 'dc-p3'
  if (p.includes('i/ii') || p.includes('1/2')) return 'dc-p1p2'
  if (p.includes('ii期') || p.includes('ii临') || p === 'phase ii' || p === '2') return 'dc-p2'
  if (p.includes('i期') || p.includes('i临') || p === 'phase i' || p === '1') return 'dc-p1'
  if (p.includes('申报') || p.includes('ind')) return 'dc-ind'
  return 'dc-prec'
}

/* ============================================================
   Competition Matrix  POST /api/v1/competition/matrix
   ============================================================ */
export interface CompetitionMatrixRequest {
  diseaseIds: number[]
  phases: string[]
  hideNoComboTargets: boolean
}

export interface MatrixCell {
  target: string
  score: number
  phaseName: string | null
  drugCount: number
}

export interface MatrixRow {
  target: string
  maxScore: number
  sumScore: number
  cells: MatrixCell[]
}

export interface MatrixColumn {
  target: string
  maxScore: number
}

export interface CompetitionMatrixData {
  columns: MatrixColumn[]
  rows: MatrixRow[]
  totalTargets: number
  totalDiseases: number
  updatedAt: string
}

/* ============================================================
   Cell drugs  GET /api/v1/competition/cell-drugs
   ============================================================ */
export interface CellDrug {
  drugNameEn: string
  originator: string
  researchInstitute: string
  highestPhase: string
  highestPhaseDate: string | null
  nctId: string | null
  moa: string | null
}

export interface CellDrugsData {
  target: string
  pairTarget: string
  drugs: CellDrug[]
}

/* ============================================================
   Progress targets  GET /api/v1/progress/targets
   ============================================================ */
export interface TargetItem {
  target: string
  drugCount: number
}

/* ============================================================
   Disease view  POST /api/v1/progress/disease-view
   ============================================================ */
export interface ProgressDrug {
  drugNameEn: string
  originator: string
  researchInstitute: string
  highestPhaseDate: string | null
  nctId: string | null
}

export interface ProgressTargetRow {
  target: string
  phaseDrugs: Record<string, ProgressDrug[]>
}

export interface DiseaseViewData {
  diseaseName: string
  phases: string[]
  targetRows: ProgressTargetRow[]
  totalDrugs: number
  updatedAt: string
}

export interface DiseaseViewRequest {
  diseaseId: number
  targets: string[]
}

/* ============================================================
   Filter presets
   ============================================================ */
export interface FilterPreset {
  id: number
  name: string
  userId: number
  module: 'competition' | 'progress'
  conditions: Record<string, any>
  isDefault: boolean
  createdAt: string
  updatedAt: string
}

export interface FilterPresetRequest {
  name: string
  module: 'competition' | 'progress'
  conditions: Record<string, any>
  isDefault: boolean
}

/* ============================================================
   UI types
   ============================================================ */
export type ViewType = 'target-combo' | 'target-progress'
