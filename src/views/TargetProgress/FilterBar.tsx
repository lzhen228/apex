import React, { useState } from 'react';
import {
  Select,
  Button,
  Tag,
  Input,
  Space,
  message,
  Modal
} from 'antd';
import {
  SearchOutlined,
  FilterOutlined,
  SaveOutlined,
  ReloadOutlined
} from '@ant-design/icons';
import { useProgressFilterStore, FilterPreset } from '../../stores/progressFilterStore';
import { useDiseaseList, useTargetList } from '../usePipelineData';
import TargetListSelector from './TargetListSelector';

const { Option } = Select;

// 研发阶段选项
const PHASE_OPTIONS = [
  { label: '已批准', value: 'Approved' },
  { label: 'BLA', value: 'BLA' },
  { label: '三期', value: 'Phase III' },
  { label: '二期/三期', value: 'Phase II/III' },
  { label: '二期', value: 'Phase II' },
  { label: '一期/二期', value: 'Phase I/II' },
  { label: '一期', value: 'Phase I' },
  { label: 'IND', value: 'IND' },
  { label: '临床前', value: 'PreClinical' }
];

// 排序选项
const SORT_BY_OPTIONS = [
  { label: '靶点名称', value: 'target' },
  { label: '药物数量', value: 'drugCount' },
  { label: '平均分值', value: 'avgPhaseScore' }
];

const SORT_ORDER_OPTIONS = [
  { label: '升序', value: 'ASC' },
  { label: '降序', value: 'DESC' }
];

interface FilterBarProps {
  onQuery: () => void;
  loading?: boolean;
}

const FilterBar: React.FC<FilterBarProps> = ({ onQuery, loading = false }) => {
  // Store 中的状态
  const {
    diseaseId,
    targets,
    phases,
    origins,
    sortBy,
    sortOrder,
    setDiseaseId,
    setTargets,
    setPhases,
    setOrigins,
    setSortBy,
    setSortOrder,
    resetFilters,
    loadFromPreset
  } = useProgressFilterStore();

  // 本地状态
  const [tempOrigin, setTempOrigin] = useState('');
  const [presetModalVisible, setPresetModalVisible] = useState(false);
  const [presetName, setPresetName] = useState('');
  const [selectedPreset, setSelectedPreset] = useState<string | null>(null);

  // 获取疾病列表
  const { data: diseases = [] } = useDiseaseList();

  // 获取靶点列表（根据选中的疾病）
  const { data: targetList = [], isLoading: targetLoading } = useTargetList(diseaseId);

  // 处理疾病选择
  const handleDiseaseChange = (value: number | null) => {
    setDiseaseId(value);
    // 清空靶点选择（因为不同疾病的靶点不同）
    setTargets([]);
  };

  // 处理靶点选择
  const handleTargetsChange = (values: string[]) => {
    setTargets(values);
  };

  // 处理阶段选择
  const handlePhasesChange = (values: string[]) => {
    setPhases(values);
  };

  // 添加起源公司
  const handleAddOrigin = () => {
    if (!tempOrigin.trim()) {
      return;
    }
    if (origins.includes(tempOrigin.trim())) {
      message.warning('该起源公司已存在');
      return;
    }
    setOrigins([...origins, tempOrigin.trim()]);
    setTempOrigin('');
  };

  // 删除起源公司
  const handleRemoveOrigin = (origin: string) => {
    setOrigins(origins.filter((o) => o !== origin));
  };

  // 处理起源公司输入回车
  const handleOriginInputPressEnter = () => {
    handleAddOrigin();
  };

  // 处理排序字段变化
  const handleSortByChange = (value: string) => {
    setSortBy(value);
  };

  // 处理排序方向变化
  const handleSortOrderChange = (value: 'ASC' | 'DESC') => {
    setSortOrder(value);
  };

  // 处理查询
  const handleQuery = () => {
    if (!diseaseId) {
      message.warning('请先选择疾病');
      return;
    }
    onQuery();
  };

  // 重置筛选条件
  const handleReset = () => {
    resetFilters();
    setSelectedPreset(null);
    message.success('已重置筛选条件');
  };

  // 打开保存预设弹窗
  const handleOpenSavePreset = () => {
    if (!diseaseId) {
      message.warning('请先设置筛选条件');
      return;
    }
    setPresetModalVisible(true);
  };

  // 保存预设
  const handleSavePreset = () => {
    if (!presetName.trim()) {
      message.warning('请输入预设名称');
      return;
    }

    const preset: FilterPreset = {
      name: presetName,
      diseaseId,
      targets,
      phases,
      origins,
      sortBy,
      sortOrder
    };

    // 保存到 localStorage
    const existingPresets = JSON.parse(
      localStorage.getItem('filterPresets') || '[]'
    );
    const newPresets = [...existingPresets, preset];
    localStorage.setItem('filterPresets', JSON.stringify(newPresets));

    setPresetModalVisible(false);
    setPresetName('');
    message.success('预设已保存');
  };

  // 加载已保存的预设列表
  const savedPresets = JSON.parse(
    localStorage.getItem('filterPresets') || '[]'
  );

  // 处理加载预设
  const handleLoadPreset = (presetName: string) => {
    const preset = savedPresets.find((p: FilterPreset) => p.name === presetName);
    if (preset) {
      loadFromPreset(preset);
      setSelectedPreset(presetName);
      message.success(`已加载预设：${presetName}`);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <div className="flex items-center gap-2 mb-4">
        <FilterOutlined className="text-blue-600" />
        <h3 className="text-lg font-semibold text-gray-900">筛选条件</h3>
      </div>

      {/* 第一行：疾病、靶点、阶段 */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
        {/* 疾病选择 */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            疾病
            <span className="text-red-500 ml-1">*</span>
          </label>
          <Select
            placeholder="请选择疾病"
            value={diseaseId}
            onChange={handleDiseaseChange}
            allowClear
            showSearch
            filterOption={(input, option) => {
              const label = option?.children as string;
              return label.toLowerCase().includes(input.toLowerCase());
            }}
            className="w-full"
          >
            {diseases.map((disease) => (
              <Option key={disease.id} value={disease.id}>
                {disease.nameCn} ({disease.nameEn})
              </Option>
            ))}
          </Select>
        </div>

        {/* 靶点选择 */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            靶点
          </label>
          <TargetListSelector
            targets={targetList}
            loading={targetLoading}
            value={targets}
            onChange={handleTargetsChange}
            disabled={!diseaseId}
          />
        </div>

        {/* 阶段选择 */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            研发阶段
          </label>
          <Select
            mode="multiple"
            placeholder="请选择研发阶段"
            value={phases}
            onChange={handlePhasesChange}
            options={PHASE_OPTIONS}
            allowClear
            maxTagCount="responsive"
            className="w-full"
          />
        </div>
      </div>

      {/* 第二行：起源公司、排序 */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
        {/* 起源公司 */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            起源公司
          </label>
          <Space direction="vertical" className="w-full">
            <Input
              placeholder="输入起源公司名称"
              value={tempOrigin}
              onChange={(e) => setTempOrigin(e.target.value)}
              onPressEnter={handleOriginInputPressEnter}
              suffix={
                <Button
                  type="text"
                  size="small"
                  icon={<SearchOutlined />}
                  onClick={handleAddOrigin}
                />
              }
            />
            <div className="flex flex-wrap gap-2">
              {origins.map((origin) => (
                <Tag
                  key={origin}
                  closable
                  onClose={() => handleRemoveOrigin(origin)}
                  className="bg-blue-50 text-blue-600 border-blue-200"
                >
                  {origin}
                </Tag>
              ))}
            </div>
          </Space>
        </div>

        {/* 排序字段 */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            排序方式
          </label>
          <Select
            placeholder="请选择排序字段"
            value={sortBy}
            onChange={handleSortByChange}
            options={SORT_BY_OPTIONS}
            className="w-full"
          />
        </div>

        {/* 排序方向 */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            排序方向
          </label>
          <Select
            placeholder="请选择排序方向"
            value={sortOrder}
            onChange={handleSortOrderChange}
            options={SORT_ORDER_OPTIONS}
            className="w-full"
          />
        </div>
      </div>

      {/* 操作按钮 */}
      <div className="flex items-center justify-between pt-4 border-t border-gray-200">
        <Button
          type="default"
          icon={<ReloadOutlined />}
          onClick={handleReset}
        >
          重置
        </Button>

        <div className="flex items-center gap-3">
          <Button
            type="default"
            icon={<SaveOutlined />}
            onClick={handleOpenSavePreset}
          >
            保存预设
          </Button>
          <Button
            type="primary"
            icon={<SearchOutlined />}
            onClick={handleQuery}
            loading={loading}
          >
            查询
          </Button>
        </div>
      </div>

      {/* 保存预设弹窗 */}
      <Modal
        title="保存筛选预设"
        open={presetModalVisible}
        onOk={handleSavePreset}
        onCancel={() => {
          setPresetModalVisible(false);
          setPresetName('');
        }}
      >
        <div className="py-4">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            预设名称
          </label>
          <Input
            placeholder="请输入预设名称"
            value={presetName}
            onChange={(e) => setPresetName(e.target.value)}
            maxLength={50}
          />
        </div>
      </Modal>
    </div>
  );
};

export default FilterBar;
