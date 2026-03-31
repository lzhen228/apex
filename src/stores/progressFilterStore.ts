import { create } from 'zustand';

export interface FilterPreset {
  name: string;
  diseaseId: number | null;
  targets: string[];
  phases: string[];
  origins: string[];
  sortBy: string;
  sortOrder: 'ASC' | 'DESC';
}

interface ProgressFilterState {
  diseaseId: number | null;
  targets: string[];
  phases: string[];
  origins: string[];
  sortBy: string;
  sortOrder: 'ASC' | 'DESC';
  setDiseaseId: (id: number | null) => void;
  setTargets: (targets: string[]) => void;
  setPhases: (phases: string[]) => void;
  setOrigins: (origins: string[]) => void;
  setSortBy: (sortBy: string) => void;
  setSortOrder: (order: 'ASC' | 'DESC') => void;
  resetFilters: () => void;
  loadFromPreset: (preset: FilterPreset) => void;
}

// 默认研发阶段列表（从高到低）
const DEFAULT_PHASES = [
  'Approved',
  'BLA',
  'Phase III',
  'Phase II/III',
  'Phase II',
  'Phase I/II',
  'Phase I',
  'IND',
  'PreClinical'
];

export const useProgressFilterStore = create<ProgressFilterState>((set) => ({
  diseaseId: null,
  targets: [],
  phases: DEFAULT_PHASES,
  origins: [],
  sortBy: 'drugCount', // 默认按药物数量排序
  sortOrder: 'DESC',

  setDiseaseId: (id) => set({ diseaseId: id }),
  
  setTargets: (targets) => set({ targets }),
  
  setPhases: (phases) => set({ phases }),
  
  setOrigins: (origins) => set({ origins }),
  
  setSortBy: (sortBy) => set({ sortBy }),
  
  setSortOrder: (order) => set({ sortOrder: order }),
  
  resetFilters: () => set({
    diseaseId: null,
    targets: [],
    phases: DEFAULT_PHASES,
    origins: [],
    sortBy: 'drugCount',
    sortOrder: 'DESC'
  }),
  
  loadFromPreset: (preset) => set({
    diseaseId: preset.diseaseId,
    targets: preset.targets,
    phases: preset.phases,
    origins: preset.origins,
    sortBy: preset.sortBy,
    sortOrder: preset.sortOrder
  })
}));
