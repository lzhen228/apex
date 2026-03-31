import request from '@/utils/request';
import type { FilterPreset, FilterPresetRequest, Result } from '@/types';

export async function getFilterPresets(module: 'competition' | 'progress'): Promise<Result<FilterPreset[]>> {
  const response = await request.get<Result<FilterPreset[]>>('/filter-presets', { params: { module } });
  return response.data;
}

export async function createFilterPreset(data: FilterPresetRequest): Promise<Result<FilterPreset>> {
  const response = await request.post<Result<FilterPreset>>('/filter-presets', data);
  return response.data;
}

export async function deleteFilterPreset(id: number): Promise<Result<null>> {
  const response = await request.delete<Result<null>>(`/filter-presets/${id}`);
  return response.data;
}
