import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { message } from 'antd'
import { getPresetList as getPresetListApi, savePreset as savePresetApi, deletePreset } from '@/services/api'
import { useComboFilterStore } from '@/stores/comboFilterStore'
import type { FilterPreset } from '@/types'

// 获取预设列表
export const getPresetList = () => {
  return useQuery({
    queryKey: ['presetList'],
    queryFn: async () => {
      const response = await getPresetListApi()
      return response.presets
    },
    staleTime: 5 * 60 * 1000, // 5 分钟
  })
}

// 保存预设
export const savePreset = async (preset: Omit<FilterPreset, 'id' | 'createdAt' | 'updatedAt'>) => {
  try {
    const result = await savePresetApi({
      name: preset.name,
      diseaseIds: preset.diseaseIds,
      targets: preset.targets,
      phases: preset.phases,
      origins: preset.origins,
      sortBy: preset.sortBy,
    })
    
    // 刷新预设列表
    const queryClient = useQueryClient.getState()
    queryClient.invalidateQueries({ queryKey: ['presetList'] })
    
    return result
  } catch (error) {
    console.error('Save preset failed:', error)
    throw error
  }
}

// 加载预设
export const loadPreset = async (presetId: number) => {
  try {
    const queryClient = useQueryClient.getState()
    const presets = queryClient.getQueryData<FilterPreset[]>(['presetList'])
    
    if (!presets) {
      throw new Error('预设列表未加载')
    }

    const preset = presets.find((p) => p.id === presetId)
    if (!preset) {
      throw new Error('预设不存在')
    }

    // 加载到 Zustand store
    useComboFilterStore.getState().loadFromPreset(preset)

    return preset
  } catch (error) {
    console.error('Load preset failed:', error)
    throw error
  }
}

// 删除预设
export const useDeletePreset = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (id: number) => {
      await deletePreset(id)
    },
    onSuccess: () => {
      message.success('预设删除成功')
      queryClient.invalidateQueries({ queryKey: ['presetList'] })
    },
    onError: (error) => {
      console.error('Delete preset failed:', error)
      message.error('预设删除失败')
    },
  })
}
