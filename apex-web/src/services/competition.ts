import request from '@/utils/request';
import type { Result, CompetitionMatrixRequest, CompetitionMatrixData, CellDrugsData } from '@/types';

export async function getDiseaseTree(): Promise<Result> {
  const response = await request.get<Result>('/diseases/tree');
  return response.data;
}

export async function getCompetitionMatrix(data: CompetitionMatrixRequest): Promise<Result<CompetitionMatrixData>> {
  const response = await request.post<Result<CompetitionMatrixData>>('/competition/matrix', data);
  return response.data;
}

export async function getCellDrugs(params: { target: string; diseaseId: number; phases?: string[] }): Promise<Result<CellDrugsData>> {
  const response = await request.get<Result<CellDrugsData>>('/competition/cell-drugs', { params });
  return response.data;
}

export async function exportCompetition(data: CompetitionMatrixRequest): Promise<Blob> {
  const response = await request.post('/competition/export', data, { responseType: 'blob' });
  return response.data;
}
