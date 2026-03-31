import { useQuery } from '@tanstack/react-query'
import { getDiseaseTree, getMatrixData } from '@/services/api'
import { useComboFilterStore } from '@/stores/comboFilterStore'
import type { Disease, MatrixRequest, MatrixResponse } from '@/types'

// 导出疾病树状态供 FilterBar 使用
export let diseaseTreeState: Disease[] | null = null

export const saveDiseaseTree = () => {
  return {
    diseaseTree: diseaseTreeState,
    setDiseaseTree: (data: Disease[] | null) => {
      diseaseTreeState = data
    },
  }
}

// 获取疾病树
export const useDiseaseTree = () => {
  return useQuery({
    queryKey: ['diseaseTree'],
    queryFn: async () => {
      const data = await getDiseaseTree()
      diseaseTreeState = data
      return data
    },
    staleTime: 60 * 60 * 1000, // 1 小时
  })
}

// 获取矩阵数据
export const useMatrixData = (enabled: boolean = true) => {
  const { diseaseIds, targets, phases, origins, sortBy } = useComboFilterStore()

  return useQuery({
    queryKey: ['matrix', { diseaseIds, targets, phases, origins, sortBy }],
    queryFn: async () => {
      const request: MatrixRequest = {
        diseaseIds,
        targets,
        phases,
        origins,
        sortBy,
      }
      return await getMatrixData(request)
    },
    enabled,
    staleTime: 5 * 60 * 1000, // 5 分钟
  })
}
