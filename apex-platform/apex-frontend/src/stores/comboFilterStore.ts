import { create } from 'zustand'

export interface FilterPreset {
  id: number
  name: string
  diseaseIds: number[]
  targets: string[]
  phases: string[]
  origins: string[]
  sortBy: string
}

interface ComboFilterState {
  diseaseIds: number[]
  targets: string[]
  phases: string[]
  origins: string[]
  sortBy: string
  setDiseaseIds: (ids: number[]) => void
  setTargets: (targets: string[]) => void
  setPhases: (phases: string[]) => void
  setOrigins: (origins: string[]) => void
  setSortBy: (sortBy: string) => void
  resetFilters: () => void
  loadFromPreset: (preset: FilterPreset) => void
  toPresetData: () => Omit<FilterPreset, 'id' | 'name'>
}

export const useComboFilterStore = create<ComboFilterState>((set) => ({
  diseaseIds: [],
  targets: [],
  phases: [],
  origins: [],
  sortBy: 'score-desc',

  setDiseaseIds: (diseaseIds) => set({ diseaseIds }),
  setTargets: (targets) => set({ targets }),
  setPhases: (phases) => set({ phases }),
  setOrigins: (origins) => set({ origins }),
  setSortBy: (sortBy) => set({ sortBy }),

  resetFilters: () =>
    set({
      diseaseIds: [],
      targets: [],
      phases: [],
      origins: [],
      sortBy: 'score-desc',
    }),

  loadFromPreset: (preset) =>
    set({
      diseaseIds: preset.diseaseIds,
      targets: preset.targets,
      phases: preset.phases,
      origins: preset.origins,
      sortBy: preset.sortBy,
    }),

  toPresetData: () => {
    const state = useComboFilterStore.getState()
    return {
      diseaseIds: state.diseaseIds,
      targets: state.targets,
      phases: state.phases,
      origins: state.origins,
      sortBy: state.sortBy,
    }
  },
}))
