import React, { useState, useCallback } from 'react'
import { Input, Tag, Select, Button, Space, Modal, message } from 'antd'
import { PlusOutlined, SaveOutlined, SearchOutlined } from '@ant-design/icons'
import { useComboFilterStore } from '@/stores/comboFilterStore'
import type { Disease } from '@/types'
import { saveDiseaseTree } from './useMatrixData'

interface FilterBarProps {
  onQuery: () => void
  onSavePreset: () => void
}

const FilterBar: React.FC<FilterBarProps> = ({ onQuery, onSavePreset }) => {
  const {
    diseaseIds,
    targets,
    phases,
    origins,
    sortBy,
    setDiseaseIds,
    setTargets,
    setPhases,
    setOrigins,
    setSortBy,
    resetFilters,
  } = useComboFilterStore()

  const { diseaseTree } = saveDiseaseTree()

  // 状态管理
  const [targetInput, setTargetInput] = useState('')
  const [originInput, setOriginInput] = useState('')

  // 选项定义
  const phaseOptions = [
    { label: 'Approved', value: 'Approved' },
    { label: 'BLA', value: 'BLA' },
    { label: 'Phase III', value: 'Phase III' },
    { label: 'Phase II/III', value: 'Phase II/III' },
    { label: 'Phase II', value: 'Phase II' },
    { label: 'Phase I/II', value: 'Phase I/II' },
    { label: 'Phase I', value: 'Phase I' },
    { label: 'IND', value: 'IND' },
    { label: 'PreClinical', value: 'PreClinical' },
  ]

  const sortOptions = [
    { label: '综合评分 (降序)', value: 'score-desc' },
    { label: '综合评分 (升序)', value: 'score-asc' },
    { label: '药物数量 (降序)', value: 'count-desc' },
    { label: '药物数量 (升序)', value: 'count-asc' },
  ]

  // 递归生成疾病树选项
  const generateDiseaseOptions = (diseases: Disease[]): any[] => {
    return diseases.map((disease) => ({
      label: disease.nameEn,
      value: disease.id,
      children: disease.children ? generateDiseaseOptions(disease.children) : undefined,
    }))
  }

  const diseaseOptions = diseaseTree ? generateDiseaseOptions(diseaseTree) : []

  // 处理靶点输入
  const handleTargetAdd = useCallback(() => {
    const trimmed = targetInput.trim()
    if (trimmed && !targets.includes(trimmed)) {
      setTargets([...targets, trimmed])
      setTargetInput('')
    }
  }, [targetInput, targets, setTargets])

  const handleTargetRemove = (target: string) => {
    setTargets(targets.filter((t) => t !== target))
  }

  // 处理起源公司输入
  const handleOriginAdd = useCallback(() => {
    const trimmed = originInput.trim()
    if (trimmed && !origins.includes(trimmed)) {
      setOrigins([...origins, trimmed])
      setOriginInput('')
    }
  }, [originInput, origins, setOrigins])

  const handleOriginRemove = (origin: string) => {
    setOrigins(origins.filter((o) => o !== origin))
  }

  return (
    <div className="bg-white p-4 rounded-lg shadow-sm border border-border mb-4">
      {/* 第一行：疾病筛选和靶点筛选 */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 mb-4">
        {/* 疾病筛选 */}
        <div className="space-y-2">
          <label className="text-sm font-medium text-gray-700 block">疾病</label>
          <Select
            mode="multiple"
            placeholder="选择疾病"
            style={{ width: '100%' }}
            value={diseaseIds}
            onChange={setDiseaseIds}
            treeCheckable
            showCheckedStrategy="SHOW_CHILD"
            options={diseaseOptions}
            maxTagCount="responsive"
          />
        </div>

        {/* 靶点筛选 */}
        <div className="space-y-2">
          <label className="text-sm font-medium text-gray-700 block">靶点</label>
          <div className="border border-gray-300 rounded-md p-2 min-h-[38px] flex flex-wrap gap-1">
            {targets.map((target) => (
              <Tag
                key={target}
                closable
                onClose={() => handleTargetRemove(target)}
                className="mb-1"
              >
                {target}
              </Tag>
            ))}
            <Input
              value={targetInput}
              onChange={(e) => setTargetInput(e.target.value)}
              onPressEnter={handleTargetAdd}
              placeholder="输入靶点名称后回车添加"
              style={{ border: 'none', outline: 'none', width: '150px' }}
              className="flex-1"
            />
          </div>
        </div>
      </div>

      {/* 第二行：阶段筛选和起源公司筛选 */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 mb-4">
        {/* 阶段筛选 */}
        <div className="space-y-2">
          <label className="text-sm font-medium text-gray-700 block">阶段</label>
          <Select
            mode="multiple"
            placeholder="选择研发阶段"
            style={{ width: '100%' }}
            value={phases}
            onChange={setPhases}
            options={phaseOptions}
          />
        </div>

        {/* 起源公司筛选 */}
        <div className="space-y-2">
          <label className="text-sm font-medium text-gray-700 block">起源公司</label>
          <div className="border border-gray-300 rounded-md p-2 min-h-[38px] flex flex-wrap gap-1">
            {origins.map((origin) => (
              <Tag
                key={origin}
                closable
                onClose={() => handleOriginRemove(origin)}
                className="mb-1"
              >
                {origin}
              </Tag>
            ))}
            <Input
              value={originInput}
              onChange={(e) => setOriginInput(e.target.value)}
              onPressEnter={handleOriginAdd}
              placeholder="输入公司名称后回车添加"
              style={{ border: 'none', outline: 'none', width: '150px' }}
              className="flex-1"
            />
          </div>
        </div>
      </div>

      {/* 第三行：排序方式、查询按钮和保存预设按钮 */}
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div className="flex items-center gap-4 flex-wrap">
          {/* 排序方式 */}
          <div className="space-y-2">
            <label className="text-sm font-medium text-gray-700 block">排序</label>
            <Select
              placeholder="选择排序方式"
              style={{ width: 180 }}
              value={sortBy}
              onChange={setSortBy}
              options={sortOptions}
            />
          </div>

          {/* 查询按钮 */}
          <div className="space-y-2">
            <label className="text-sm font-medium text-gray-700 block">&nbsp;</label>
            <Button
              type="primary"
              icon={<SearchOutlined />}
              onClick={onQuery}
              size="middle"
            >
              查询
            </Button>
          </div>

          {/* 保存预设按钮 */}
          <div className="space-y-2">
            <label className="text-sm font-medium text-gray-700 block">&nbsp;</label>
            <Button
              icon={<SaveOutlined />}
              onClick={onSavePreset}
              size="middle"
            >
              保存预设
            </Button>
          </div>

          {/* 重置按钮 */}
          <div className="space-y-2">
            <label className="text-sm font-medium text-gray-700 block">&nbsp;</label>
            <Button onClick={resetFilters} size="middle">
              重置
            </Button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default FilterBar
