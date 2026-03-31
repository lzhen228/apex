import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface CompetitionFilters {
  diseaseIds: number[];
  phases: string[];
  hideNoComboTargets: boolean;
}

interface ProgressFilters {
  diseaseId: number | null;
  targets: string[];
}

interface FilterState {
  competitionFilters: CompetitionFilters;
  progressFilters: ProgressFilters;
  currentPresetName: string | null;
  setCompetitionFilters: (filters: CompetitionFilters) => void;
  setProgressFilters: (filters: ProgressFilters) => void;
  setCurrentPresetName: (name: string | null) => void;
}

export const useFilterStore = create<FilterState>()(
  persist(
    (set) => ({
      competitionFilters: { diseaseIds: [], phases: [], hideNoComboTargets: false },
      progressFilters:    { diseaseId: null, targets: [] },
      currentPresetName:  null,
      setCompetitionFilters: (filters) => set({ competitionFilters: filters }),
      setProgressFilters:    (filters) => set({ progressFilters: filters }),
      setCurrentPresetName:  (name)    => set({ currentPresetName: name }),
    }),
    { name: 'apex-filters', partialize: (s) => ({ competitionFilters: s.competitionFilters, progressFilters: s.progressFilters }) }
  )
);
