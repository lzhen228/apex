import React, { useState, useRef, useCallback } from 'react'
import { Table, Popover, Modal, Button, Dropdown, Checkbox, Spin } from 'antd'
import { DownOutlined, DownloadOutlined } from '@ant-design/icons'
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table'
import type { MatrixCellData, MatrixDrug } from '@/types'
import DrugCard from './DrugCard'
import TooltipCard from '@/components/common/TooltipCard'

interface MatrixTableProps {
  data: MatrixCellData[]
  loading: boolean
  onExport: () => void
}

const MatrixTable: React.FC<MatrixTableProps> = ({ data, loading, onExport }) => {
  // 分页状态
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 20,
    total: data.length,
  })

  // Tooltip 状态
  const [tooltip, setTooltip] = useState<{
    visible: boolean
    drugs: MatrixDrug[]
    position: { x: number; y: number }
  }>({
    visible: false,
    drugs: [],
    position: { x: 0, y: 0 },
  })

  // 药物详情弹窗
  const [drugDetailVisible, setDrugDetailVisible] = useState(false)
  const [selectedDrugs, setSelectedDrugs] = useState<MatrixDrug[]>([])

  // 列可见性状态
  const [visibleColumns, setVisibleColumns] = useState<string[]>([
    'diseaseName',
    'targets',
    'approved',
    'bla',
    'phase3',
    'phase23',
    'phase2',
    'phase12',
    'phase1',
    'ind',
    'preClinical',
    'score',
  ])

  // 阶段配置
  const phaseConfig = [
    { key: 'approved', label: 'Approved', color: 'bg-phase-approved' },
    { key: 'bla', label: 'BLA', color: 'bg-phase-bla' },
    { key: 'phase3', label: 'Phase III', color: 'bg-phase-3' },
    { key: 'phase23', label: 'Phase II/III', color: 'bg-phase-23' },
    { key: 'phase2', label: 'Phase II', color: 'bg-phase-2' },
    { key: 'phase12', label: 'Phase I/II', color: 'bg-phase-12' },
    { key: 'phase1', label: 'Phase I', color: 'bg-phase-1' },
    { key: 'ind', label: 'IND', color: 'bg-phase-ind' },
    { key: 'preClinical', label: 'PreClinical', color: 'bg-phase-preclinical' },
  ]

  // 获取单元格药物列表
  const getDrugsForCell = (row: MatrixCellData, phaseKey: string): MatrixDrug[] => {
    const drugMap: Record<string, MatrixDrug[]> = {
      approved: row.approved,
      bla: row.bla,
      phase3: row.phase3,
      phase23: row.phase23,
      phase2: row.phase2,
      phase12: row.phase12,
      phase1: row.phase1,
      ind: row.ind,
      preClinical: row.preClinical,
    }
    return drugMap[phaseKey] || []
  }

  // 处理单元格悬停
  const handleCellHover = useCallback((e: React.MouseEvent, drugs: MatrixDrug[]) => {
    if (drugs.length === 0) return

    const rect = (e.target as HTMLElement).getBoundingClientRect()
    setTooltip({
      visible: true,
      drugs,
      position: {
        x: rect.right + 10,
        y: rect.top,
      },
    })
  }, [])

  // 处理单元格点击
  const handleCellClick = (drugs: MatrixDrug[]) => {
    if (drugs.length === 0) return
    setSelectedDrugs(drugs)
    setDrugDetailVisible(true)
  }

  // 关闭 Tooltip
  const closeTooltip = () => {
    setTooltip({ ...tooltip, visible: false })
  }

  // 列定义
  const columns: ColumnsType<MatrixCellData> = [
    {
      title: '疾病名称',
      dataIndex: 'diseaseName',
      key: 'diseaseName',
      fixed: 'left',
      width: 180,
      render: (text: string) => (
        <div className="text-sm font-medium text-gray-900 truncate">{text}</div>
      ),
    },
    {
      title: '靶点组合',
      dataIndex: 'targets',
      key: 'targets',
      fixed: 'left',
      width: 200,
      render: (targets: string[]) => (
        <div className="text-xs text-gray-600 line-clamp-2">
          {targets.join(', ')}
        </div>
      ),
    },
    ...phaseConfig.map((phase) => ({
      title: () => (
        <div className="flex items-center gap-1">
          <div className={`w-2 h-2 rounded-full ${phase.color}`} />
          <span className="text-sm">{phase.label}</span>
        </div>
      ),
      dataIndex: phase.key,
      key: phase.key,
      width: 100,
      align: 'center' as const,
      render: (_: any, record: MatrixCellData) => {
        const drugs = getDrugsForCell(record, phase.key)
        if (drugs.length === 0) {
          return <span className="text-gray-300">-</span>
        }
        return (
          <button
            className={`
              px-2 py-1 rounded text-sm font-medium transition-colors
              ${drugs.length > 0 
                ? `${phase.color} text-white hover:opacity-80` 
                : 'text-gray-300'
              }
            `}
            onMouseEnter={(e) => handleCellHover(e, drugs)}
            onMouseLeave={closeTooltip}
            onClick={() => handleCellClick(drugs)}
          >
            {drugs.length}
          </button>
        )
      },
    })),
    {
      title: '综合评分',
      dataIndex: 'score',
      key: 'score',
      width: 100,
      align: 'center' as const,
      render: (score: number) => (
        <span className="text-sm font-semibold text-gray-900">
          {score.toFixed(2)}
        </span>
      ),
    },
  ].filter((col) => visibleColumns.includes(col.key as string))

  // 列显示/隐藏菜单
  const columnMenuItems = [
    ...phaseConfig.map((phase) => ({
      key: phase.key,
      label: (
        <Checkbox
          checked={visibleColumns.includes(phase.key)}
          onChange={(e) => {
            if (e.target.checked) {
              setVisibleColumns([...visibleColumns, phase.key])
            } else {
              setVisibleColumns(visibleColumns.filter((c) => c !== phase.key))
            }
          }}
        >
          {phase.label}
        </Checkbox>
      ),
    })),
    { type: 'divider' as const },
    {
      key: 'score',
      label: (
        <Checkbox
          checked={visibleColumns.includes('score')}
          onChange={(e) => {
            if (e.target.checked) {
              setVisibleColumns([...visibleColumns, 'score'])
            } else {
              setVisibleColumns(visibleColumns.filter((c) => c !== 'score'))
            }
          }}
        >
          综合评分
        </Checkbox>
      ),
    },
  ]

  // 处理分页变化
  const handleTableChange = (newPagination: TablePaginationConfig) => {
    setPagination({
      current: newPagination.current || 1,
      pageSize: newPagination.pageSize || 20,
      total: data.length,
    })
  }

  return (
    <div className="bg-white rounded-lg shadow-sm border border-border">
      {/* 工具栏 */}
      <div className="flex items-center justify-between p-3 border-b border-border">
        <div className="text-sm text-gray-600">
          共 {data.length} 条记录
        </div>
        <div className="flex gap-2">
          <Dropdown menu={{ items: columnMenuItems }} trigger={['click']}>
            <Button size="small" icon={<DownOutlined />}>
              列设置
            </Button>
          </Dropdown>
          <Button
            size="small"
            icon={<DownloadOutlined />}
            onClick={onExport}
          >
            导出
          </Button>
        </div>
      </div>

      {/* 表格 */}
      <Spin spinning={loading}>
        <Table
          columns={columns}
          dataSource={data}
          rowKey={(record, index) => `${record.diseaseName}-${record.targets.join('-')}-${index}`}
          pagination={pagination}
          onChange={handleTableChange}
          scroll={{ x: 'max-content', y: 600 }}
          bordered
          size="small"
        />
      </Spin>

      {/* Tooltip */}
      <TooltipCard
        visible={tooltip.visible}
        drugs={tooltip.drugs}
        position={tooltip.position}
        onClose={closeTooltip}
      />

      {/* 药物详情弹窗 */}
      <Modal
        title="药物详情"
        open={drugDetailVisible}
        onCancel={() => setDrugDetailVisible(false)}
        footer={null}
        width={800}
        className="drug-detail-modal"
      >
        <div className="grid grid-cols-2 gap-4 max-h-[600px] overflow-y-auto">
          {selectedDrugs.map((drug, index) => (
            <DrugCard
              key={index}
              drug={drug}
              onClick={() => {
                // TODO: 导航到药物详情页
                console.log('查看药物详情:', drug)
              }}
            />
          ))}
        </div>
      </Modal>
    </div>
  )
}

export default MatrixTable
