import React, { useState } from 'react';
import { Table, Button, Dropdown, Checkbox, message } from 'antd';
import type { ColumnsType, TableProps } from 'antd';
import { DownOutlined, DownloadOutlined } from '@ant-design/icons';
import { TargetRowData } from '../usePipelineData';
import PipelineCard from './PipelineCard';

interface PipelineTableProps {
  data: TargetRowData[];
  loading: boolean;
  diseaseName: string;
  phases: string[];
  onExport?: () => void;
}

// 可配置列选项
interface ColumnOption {
  key: string;
  label: string;
  visible: boolean;
}

// 默认列配置
const DEFAULT_COLUMNS: ColumnOption[] = [
  { key: 'target', label: '靶点名称', visible: true },
  { key: 'drugCount', label: '药物数量', visible: true },
  { key: 'avgPhaseScore', label: '平均分值', visible: true },
  { key: 'drugs', label: '药物管线', visible: true }
];

const PipelineTable: React.FC<PipelineTableProps> = ({
  data,
  loading,
  diseaseName,
  phases,
  onExport
}) => {
  const [expandedRowKeys, setExpandedRowKeys] = useState<React.Key[]>([]);
  const [columnOptions, setColumnOptions] = useState<ColumnOption[]>(DEFAULT_COLUMNS);
  const [showColumnMenu, setShowColumnMenu] = useState(false);

  // 默认展开前3个靶点
  React.useEffect(() => {
    if (data.length > 0 && expandedRowKeys.length === 0) {
      const keysToExpand = data.slice(0, 3).map((_, index) => index);
      setExpandedRowKeys(keysToExpand);
    }
  }, [data]);

  // 表格列定义
  const columns: ColumnsType<TargetRowData> = [
    {
      title: '靶点名称',
      dataIndex: 'target',
      key: 'target',
      width: 200,
      render: (text: string) => (
        <span className="font-semibold text-gray-900">{text}</span>
      )
    },
    {
      title: '药物数量',
      dataIndex: 'drugCount',
      key: 'drugCount',
      width: 100,
      sorter: (a, b) => a.drugCount - b.drugCount,
      render: (count: number) => (
        <span className="inline-flex items-center px-2 py-1 rounded-full bg-blue-50 text-blue-600 text-sm font-medium">
          {count}
        </span>
      )
    },
    {
      title: '平均阶段分值',
      dataIndex: 'avgPhaseScore',
      key: 'avgPhaseScore',
      width: 120,
      sorter: (a, b) => a.avgPhaseScore - b.avgPhaseScore,
      render: (score: number) => (
        <span className="text-gray-700 font-medium">
          {score.toFixed(2)}
        </span>
      )
    },
    {
      title: '药物管线',
      dataIndex: 'drugs',
      key: 'drugs',
      render: (drugs: any[]) => (
        <div className="py-2">
          {drugs.map((drug, index) => (
            <PipelineCard key={`${drug.drugId}-${index}`} drug={drug} />
          ))}
        </div>
      )
    }
  ];

  // 根据可见列配置过滤列
  const visibleColumns = columns.filter((col) => {
    const key = col.key as string;
    const option = columnOptions.find((opt) => opt.key === key);
    return option ? option.visible : true;
  });

  // 处理列可见性切换
  const handleColumnToggle = (key: string, visible: boolean) => {
    setColumnOptions((prev) =>
      prev.map((opt) => (opt.key === key ? { ...opt, visible } : opt))
    );
  };

  // 列配置菜单
  const columnMenu = (
    <div className="p-2 w-40 bg-white rounded-lg shadow-lg">
      <div className="text-xs font-semibold text-gray-500 mb-2 px-2">
        显示列
      </div>
      {columnOptions.map((option) => (
        <div key={option.key} className="px-2 py-1 hover:bg-gray-50 rounded">
          <Checkbox
            checked={option.visible}
            onChange={(e) => handleColumnToggle(option.key, e.target.checked)}
          >
            <span className="text-sm text-gray-700">{option.label}</span>
          </Checkbox>
        </div>
      ))}
    </div>
  );

  // 自定义表格展开/折叠图标
  const expandIcon = ({ expanded, onExpand, record }: any) => (
    <button
      type="button"
      onClick={(e) => onExpand(record, e)}
      className="w-6 h-6 flex items-center justify-center hover:bg-gray-200 rounded transition-colors"
    >
      {expanded ? (
        <DownOutlined rotate={180} className="text-gray-500" />
      ) : (
        <DownOutlined className="text-gray-500" />
      )}
    </button>
  );

  // 表格行样式
  const rowClassName = (record: TargetRowData, index: number) => {
    return 'pipeline-target-row';
  };

  // 处理导出
  const handleExport = () => {
    if (onExport) {
      onExport();
    } else {
      message.info('导出功能开发中...');
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
      {/* 表格头部 */}
      <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200 bg-gray-50">
        <div>
          <h3 className="text-lg font-semibold text-gray-900">
            管线数据 - {diseaseName}
          </h3>
          <div className="text-sm text-gray-500 mt-1">
            共 {data.length} 个靶点，总计 {data.reduce((sum, d) => sum + d.drugCount, 0)} 个药物
          </div>
        </div>
        <div className="flex items-center gap-3">
          {/* 列配置按钮 */}
          <Dropdown
            overlay={columnMenu}
            trigger={['click']}
            open={showColumnMenu}
            onOpenChange={setShowColumnMenu}
          >
            <Button type="text" size="small">
              <span className="text-sm">列配置</span>
              <DownOutlined className="ml-1 text-xs" />
            </Button>
          </Dropdown>

          {/* 导出按钮 */}
          <Button
            type="primary"
            size="small"
            icon={<DownloadOutlined />}
            onClick={handleExport}
          >
            导出
          </Button>
        </div>
      </div>

      {/* 表格内容 */}
      <Table
        columns={visibleColumns}
        dataSource={data}
        loading={loading}
        rowKey={(_, index) => index}
        expandable={{
          expandedRowKeys,
          onExpandedRowsChange: (rows) => {
            setExpandedRowKeys(rows.map((_, index) => index));
          },
          expandIcon
        }}
        rowClassName={rowClassName}
        pagination={{
          pageSize: 20,
          showSizeChanger: true,
          showTotal: (total) => `共 ${total} 个靶点`,
          position: ['bottomCenter']
        }}
        scroll={{ x: true }}
        className="pipeline-table"
        style={{
          '--table-header-bg': '#f1f5f9',
          '--target-row-bg': '#e0f2fe'
        } as any}
      />

      {/* 自定义样式 */}
      <style jsx>{`
        .pipeline-table :global(.pipeline-target-row) {
          background-color: var(--target-row-bg, #e0f2fe);
        }
        .pipeline-table :global(.ant-table-thead > tr > th) {
          background-color: var(--table-header-bg, #f1f5f9);
          font-weight: 600;
        }
        .pipeline-table :global(.ant-table-row-expand-icon) {
          background: transparent;
        }
      `}</style>
    </div>
  );
};

export default PipelineTable;
