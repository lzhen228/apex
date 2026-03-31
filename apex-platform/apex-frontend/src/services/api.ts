import api from '@/utils/api'
import type {
  Disease,
  TherapeuticArea,
  MatrixRequest,
  MatrixResponse,
  CellDrugsResponse,
  ExportRequest,
  SavePresetRequest,
  PresetListResponse,
  FilterPreset,
} from '@/types'

// 获取疾病树
export const getDiseaseTree = async (): Promise<Disease[]> => {
  return api.get('/diseases/tree')
}

// 获取矩阵数据
export const getMatrixData = async (request: MatrixRequest): Promise<MatrixResponse> => {
  return api.post('/competition/matrix', request)
}

// 获取单元格药物列表
export const getCellDrugs = async (params: {
  target: string
  diseaseId: number
  phases?: string[]
}): Promise<CellDrugsResponse> => {
  const { target, diseaseId, phases } = params
  const query = new URLSearchParams({
    target,
    diseaseId: String(diseaseId),
  })
  if (phases && phases.length > 0) {
    query.append('phases', phases.join(','))
  }
  return api.get(`/competition/cell-drugs?${query.toString()}`)
}

// 导出矩阵数据
export const exportMatrix = async (request: ExportRequest): Promise<Blob> => {
  const response = await api.post('/competition/export', request, {
    responseType: 'blob',
  })
  return response
}

// 获取筛选预设列表
export const getPresetList = async (): Promise<PresetListResponse> => {
  return api.get('/filter-presets')
}

// 保存筛选预设
export const savePreset = async (request: SavePresetRequest): Promise<FilterPreset> => {
  return api.post('/filter-presets', request)
}

// 删除筛选预设
export const deletePreset = async (id: number): Promise<void> => {
  return api.delete(`/filter-presets/${id}`)
}
