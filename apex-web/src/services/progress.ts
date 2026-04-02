import request from '@/utils/request'
import type { Result, DiseaseViewRequest, DiseaseViewData, TargetItem } from '@/types'

export async function getDiseaseTargets(diseaseId: number): Promise<Result<TargetItem[]>> {
  const response = await request.get<Result<TargetItem[]>>('/progress/targets', {
    params: { diseaseId },
  })
  return response.data
}

export async function getDiseaseView(data: DiseaseViewRequest): Promise<Result<DiseaseViewData>> {
  const response = await request.post<Result<DiseaseViewData>>('/progress/disease-view', data)
  return response.data
}

export async function exportProgress(data: DiseaseViewRequest): Promise<Blob> {
  const response = await request.post('/progress/export', data, { responseType: 'blob' })
  return response.data
}
