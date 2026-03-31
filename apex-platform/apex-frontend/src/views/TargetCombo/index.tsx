import React, { useState, useEffect } from 'react'
import { Input, Select, Button, Modal, Form, message, Tabs, Segmented } from 'antd'
import { SearchOutlined, SaveOutlined, DownloadOutlined } from '@ant-design/icons'
import FilterBar from './FilterBar'
import PhaseFilterBar from './PhaseFilterBar'
import MatrixTable from './MatrixTable'
import { useDiseaseTree, useMatrixData } from './useMatrixData'
import { useComboFilterStore } from '@/stores/comboFilterStore'
import { getPresetList, savePreset, loadPreset } from './usePreset'
import { handleExport } from './exportMatrix'

const TargetCombo: React.FC = () => {
  // 状态管理
  const [hasQueried, setHasQueried] = useState(false)
  const [selectedPhases, setSelectedPhases] = useState<string[]>([])
  const [presetModalVisible, setPresetModalVisible] = useState(false)
  const [presetForm] = Form.useForm<{ name: string }>()

  // 数据获取
  const { data: diseaseTree, isLoading: diseaseLoading } = useDiseaseTree()
  const { data: matrixData, isLoading: matrixLoading, refetch: refetchMatrix } = useMatrixData(hasQueried)
  const { presets, isLoading: presetsLoading } = getPresetList()

  // 初始化数据
  useEffect(() => {
    // 加载疾病树后自动查询一次
    if (diseaseTree && !hasQueried) {
      handleQuery()
    }
  }, [diseaseTree])

  // 处理查询
  const handleQuery = () => {
    setHasQueried(true)
    refetchMatrix()
  }

  // 处理阶段筛选
  const handlePhaseToggle = (phase: string) => {
    const newSelectedPhases = selectedPhases.includes(phase)
      ? selectedPhases.filter((p) => p !== phase)
      : [...selectedPhases, phase]

    setSelectedPhases(newSelectedPhases)

    // 更新 Zustand store 中的阶段筛选
    useComboFilterStore.getState().setPhases(newSelectedPhases)

    // 如果已经查询过，则重新查询
    if (hasQueried) {
      refetchMatrix()
    }
  }

  // 处理保存预设
  const handleSavePreset = () => {
    setPresetModalVisible(true)
  }

  // 确认保存预设
  const confirmSavePreset = async () => {
    try {
      const values = await presetForm.validateFields()
      const presetData = useComboFilterStore.getState().toPresetData()

      await savePreset({
        name: values.name,
        ...presetData,
      })

      message.success('预设保存成功')
      setPresetModalVisible(false)
      presetForm.resetFields()

      // 刷新预设列表
      await getPresetList().refetch()
    } catch (error) {
      console.error('Save preset failed:', error)
      message.error('预设保存失败')
    }
  }

  // 处理加载预设
  const handleLoadPreset = async (presetId: number) => {
    try {
      await loadPreset(presetId)
      message.success('预设加载成功')
      setSelectedPhases(useComboFilterStore.getState().phases)
      handleQuery()
    } catch (error) {
      console.error('Load preset failed:', error)
      message.error('预设加载失败')
    }
  }

  // 生成 Banner 摘要
  const generateBannerSummary = () => {
    const { diseaseIds, targets, phases, origins } = useComboFilterStore.getState()

    const parts: string[] = []
    if (diseaseIds.length > 0) {
      parts.push(`${diseaseIds.length} 个疾病`)
    }
    if (targets.length > 0) {
      parts.push(`${targets.length} 个靶点`)
    }
    if (phases.length > 0) {
      parts.push(`${phases.length} 个阶段`)
    }
    if (origins.length > 0) {
      parts.push(`${origins.length} 个公司`)
    }

    if (parts.length === 0) {
      return '全部数据'
    }

    return `已选择：${parts.join('，')}`
  }

  // 预设选项
  const presetOptions = presets?.map((preset) => ({
    label: preset.name,
    value: preset.id,
  })) || []

  return (
    <div className="w-full h-full flex flex-col overflow-hidden">
      {/* 页面标题 */}
      <div className="bg-white px-6 py-4 border-b border-border">
        <h1 className="text-xl font-semibold text-gray-900">靶点组合竞争格局</h1>
      </div>

      <div className="flex-1 overflow-y-auto p-6">
        {/* Banner 区域 */}
        <div className="bg-gradient-to-r from-blue-50 to-indigo-50 p-4 rounded-lg mb-4 border border-blue-100">
          <div className="flex items-center gap-2">
            <div className="w-2 h-2 rounded-full bg-blue-500" />
            <span className="text-sm font-medium text-gray-700">
              {generateBannerSummary()}
            </span>
          </div>
        </div>

        {/* Tab 切换 */}
        <div className="mb-4">
          <Segmented
            options={[{ label: '矩阵视图', value: 'matrix' }]}
            defaultValue="matrix"
            block
          />
        </div>

        {/* 预设选择器 */}
        {presetOptions.length > 0 && (
          <div className="mb-4">
            <Select
              placeholder="选择已保存的预设"
              style={{ width: 200 }}
              options={presetOptions}
              onChange={handleLoadPreset}
              allowClear
            />
          </div>
        )}

        {/* 筛选条件区域 */}
        <FilterBar
          onQuery={handleQuery}
          onSavePreset={handleSavePreset}
        />

        {/* 阶段筛选条 */}
        <PhaseFilterBar
          summary={matrixData?.summary}
          selectedPhases={selectedPhases}
          onPhaseToggle={handlePhaseToggle}
        />

        {/* 矩阵表格区域 */}
        <MatrixTable
          data={matrixData?.data || []}
          loading={matrixLoading}
          onExport={handleExport}
        />
      </div>

      {/* 保存预设弹窗 */}
      <Modal
        title="保存筛选预设"
        open={presetModalVisible}
        onOk={confirmSavePreset}
        onCancel={() => {
          setPresetModalVisible(false)
          presetForm.resetFields()
        }}
        okText="保存"
        cancelText="取消"
      >
        <Form form={presetForm} layout="vertical">
          <Form.Item
            name="name"
            label="预设名称"
            rules={[
              { required: true, message: '请输入预设名称' },
              { max: 50, message: '预设名称最多 50 个字符' },
            ]}
          >
            <Input placeholder="输入预设名称" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default TargetCombo
