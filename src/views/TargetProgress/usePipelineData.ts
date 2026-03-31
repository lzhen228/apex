import { useQuery } from '@tanstack/react-query';
import axios from 'axios';

// API 响应类型定义
export interface TargetItem {
  target: string;
  drugCount: number;
}

export interface DrugInfo {
  drugNameEn: string;
  drugNameCn?: string;
  drugId: string;
  originator: string;
  researchInstitute?: string;
  moa?: string;
  highestPhase: string;
  highestPhaseScore: number;
  highestPhaseDate?: string;
  nctId?: string;
  highestTrialId?: string;
  highestTrialPhase?: string;
}

export interface TargetRowData {
  target: string;
  drugCount: number;
  avgPhaseScore: number;
  drugs: DrugInfo[];
}

export interface DiseaseViewResponse {
  diseaseName: string;
  phases: string[];
  targetRows: TargetRowData[];
  totalDrugs: number;
  updatedAt: string;
}

// 获取指定疾病下的所有靶点列表
export const useTargetList = (diseaseId: number | null) => {
  return useQuery({
    queryKey: ['progress', 'targets', diseaseId],
    queryFn: async () => {
      if (!diseaseId) {
        return [];
      }
      const { data } = await axios.get<{ code: number; data: TargetItem[] }>(
        `/api/v1/progress/targets?diseaseId=${diseaseId}`
      );
      return data.data || [];
    },
    enabled: !!diseaseId,
    staleTime: 5 * 60 * 1000, // 5分钟内认为数据是新鲜的
    gcTime: 10 * 60 * 1000 // 10分钟后清理缓存
  });
};

// 获取疾病视图的管线数据
interface DiseaseViewRequest {
  diseaseId: number;
  targets?: string[];
  phases?: string[];
  origins?: string[];
  sortBy?: string;
  sortOrder?: 'ASC' | 'DESC';
}

export const useDiseaseViewData = (params: DiseaseViewRequest) => {
  return useQuery({
    queryKey: ['progress', 'disease-view', params],
    queryFn: async () => {
      const { data } = await axios.post<{ code: number; data: DiseaseViewResponse }>(
        '/api/v1/progress/disease-view',
        params
      );
      return data.data;
    },
    enabled: !!params.diseaseId,
    staleTime: 2 * 60 * 1000, // 2分钟内认为数据是新鲜的
    gcTime: 5 * 60 * 1000 // 5分钟后清理缓存
  });
};

// 获取疾病列表（用于下拉选择）
export interface DiseaseItem {
  id: number;
  nameEn: string;
  nameCn: string;
}

export const useDiseaseList = () => {
  return useQuery({
    queryKey: ['diseases', 'list'],
    queryFn: async () => {
      const { data } = await axios.get<{ code: number; data: DiseaseItem[] }>(
        '/api/v1/diseases/list'
      );
      return data.data || [];
    },
    staleTime: 30 * 60 * 1000 // 30分钟内认为数据是新鲜的
  });
};
