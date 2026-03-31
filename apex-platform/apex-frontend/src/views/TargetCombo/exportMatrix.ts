import { message } from 'antd'
import { exportMatrix as exportMatrixApi } from '@/services/api'
import { useComboFilterStore } from '@/stores/comboFilterStore'
import dayjs from 'dayjs'

export const handleExport = async () => {
  const { diseaseIds, targets, phases, origins, sortBy } = useComboFilterStore.getState()

  try {
    message.loading({ content: '正在导出数据...', key: 'export' })

    const blob = await exportMatrixApi({
      diseaseIds,
      targets,
      phases,
      origins,
      sortBy,
      format: 'excel',
    })

    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `靶点组合竞争格局_${dayjs().format('YYYY-MM-DD_HH-mm-ss')}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    message.success({ content: '导出成功', key: 'export' })
  } catch (error) {
    console.error('Export failed:', error)
    message.error({ content: '导出失败，请重试', key: 'export' })
  }
}
