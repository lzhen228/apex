import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { Spin, Empty, message } from 'antd';
import { useProgressFilterStore } from '../../stores/progressFilterStore';
import {
  useDiseaseViewData,
  DiseaseViewResponse,
  DrugInfo
} from './usePipelineData';
import FilterBar from './FilterBar';
import PipelineTable from './PipelineTable';
import { exportPipelineData } from './exportPipeline';
import { ReloadOutlined } from '@ant-design/icons';

// 获取疾病名称（用于显示）
const useDiseaseName = (diseaseId: number | null) => {
  return useQuery({
    queryKey: ['disease', 'name', diseaseId],
    queryFn: async () => {
      if (!diseaseId) return null;
      const { data } = await fetch(`/api/v1/diseases/${diseaseId}`).then((res) => res.json());
      return data.data;
    },
    enabled: !!diseaseId,
    staleTime: 30 * 60 * 1000 // 30分钟缓存
  });
};

// 主页面组件
const TargetProgress: React.FC = () => {
  // Store 中的状态
  const {
    diseaseId,
    targets,
    phases,
    origins,
    sortBy,
    sortOrder
  } = useProgressFilterStore();

  // 控制是否显示查询结果
  const [hasQueried, setHasQueried] = React.useState(false);
  const [queryKey, setQueryKey] = React.useState<number>(0);

  // 获取疾病名称
  const { data: diseaseName } = useDiseaseName(diseaseId);

  // 获取管线数据
  const {
    data: pipelineData,
    isLoading: isLoadingData,
    refetch
  } = useDiseaseViewData({
    diseaseId: diseaseId || 0,
    targets: targets.length > 0 ? targets : undefined,
    phases: phases.length > 0 ? phases : undefined,
    origins: origins.length > 0 ? origins : undefined,
    sortBy,
    sortOrder
  });

  // 处理查询
  const handleQuery = () => {
    setHasQueried(true);
    setQueryKey((prev) => prev + 1);
    refetch();
  };

  // 处理导出
  const handleExport = async () => {
    if (!diseaseId || !pipelineData) {
      message.warning('暂无可导出的数据');
      return;
    }

    const exportData = {
      diseaseName: pipelineData.diseaseName,
      phases: pipelineData.phases,
      targetRows: pipelineData.targetRows,
      filterParams: {
        diseaseId,
        targets: targets.length > 0 ? targets : undefined,
        phases: phases.length > 0 ? phases : undefined,
        origins: origins.length > 0 ? origins : undefined,
        sortBy,
        sortOrder
      }
    };

    await exportPipelineData(exportData.filterParams, exportData);
  };

  // 生成 Banner 摘要信息
  const renderBanner = () => {
    if (!hasQueried || !pipelineData) {
      return null;
    }

    const { targetRows, phases, totalDrugs } = pipelineData;

    return (
      <div className="bg-gradient-to-r from-cyan-500 to-blue-500 text-white px-6 py-4 rounded-lg shadow-md">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-6">
            <div>
              <div className="text-xs opacity-80 mb-1">疾病名称</div>
              <div className="text-lg font-semibold">{pipelineData.diseaseName}</div>
            </div>
            <div className="w-px h-10 bg-white opacity-30"></div>
            <div>
              <div className="text-xs opacity-80 mb-1">靶点数量</div>
              <div className="text-2xl font-bold">{targetRows.length}</div>
            </div>
            <div className="w-px h-10 bg-white opacity-30"></div>
            <div>
              <div className="text-xs opacity-80 mb-1">药物总数</div>
              <div className="text-2xl font-bold">{totalDrugs}</div>
            </div>
            <div className="w-px h-10 bg-white opacity-30"></div>
            <div>
              <div className="text-xs opacity-80 mb-1">研发阶段</div>
              <div className="text-sm font-medium">
                {phases.length > 3 ? `${phases.slice(0, 3).join(', ')}...` : phases.join(', ')}
              </div>
            </div>
          </div>
          <div className="text-xs opacity-70">
            更新时间: {pipelineData.updatedAt}
          </div>
        </div>
      </div>
    );
  };

  // Tab 切换
  const [activeTab, setActiveTab] = React.useState<string>('pipeline');

  return (
    <div className="min-h-screen bg-slate-50">
      {/* 页面标题 */}
      <div className="px-6 py-4">
        <h1 className="text-2xl font-bold text-gray-900">靶点研发进展格局</h1>
      </div>

      {/* Banner 区域 */}
      <div className="px-6 mb-6">
        {renderBanner()}
      </div>

      {/* Tab 切换 */}
      <div className="px-6 mb-6">
        <div className="flex items-center gap-2 bg-white rounded-lg p-1 inline-flex shadow-sm">
          <button
            onClick={() => setActiveTab('pipeline')}
            className={`px-4 py-2 rounded-md text-sm font-medium transition-all ${
              activeTab === 'pipeline'
                ? 'bg-blue-50 text-blue-600'
                : 'text-gray-600 hover:bg-gray-50'
            }`}
          >
            管线视图
          </button>
          <button
            disabled
            className="px-4 py-2 rounded-md text-sm font-medium text-gray-400 cursor-not-allowed"
          >
            统计视图（开发中）
          </button>
        </div>
      </div>

      {/* 预设选择器 */}
      <div className="px-6 mb-6">
        <div className="inline-flex items-center gap-2">
          <span className="text-sm text-gray-600">加载预设:</span>
          <select
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 min-w-[160px]"
            onChange={(e) => {
              if (e.target.value) {
                const preset = JSON.parse(e.target.value);
                const { loadFromPreset } = useProgressFilterStore.getState();
                loadFromPreset(preset);
                message.success(`已加载预设: ${preset.name}`);
              }
            }}
          >
            <option value="">选择预设...</option>
            {JSON.parse(localStorage.getItem('filterPresets') || '[]').map(
              (preset: any, index: number) => (
                <option key={index} value={JSON.stringify(preset)}>
                  {preset.name}
                </option>
              )
            )}
          </select>
        </div>
      </div>

      {/* 筛选条件区域 */}
      <div className="px-6 mb-6">
        <FilterBar onQuery={handleQuery} loading={isLoadingData} />
      </div>

      {/* 管线表格区域 */}
      <div className="px-6 pb-8">
        {hasQueried ? (
          <div className="min-h-[400px]">
            {isLoadingData ? (
              <div className="flex items-center justify-center h-[400px]">
                <Spin size="large" tip="加载数据中..." />
              </div>
            ) : pipelineData && pipelineData.targetRows.length > 0 ? (
              <PipelineTable
                data={pipelineData.targetRows}
                loading={isLoadingData}
                diseaseName={pipelineData.diseaseName}
                phases={pipelineData.phases}
                onExport={handleExport}
              />
            ) : (
              <div className="flex items-center justify-center h-[400px]">
                <Empty
                  description="暂无数据，请调整筛选条件后重新查询"
                  image={Empty.PRESENTED_IMAGE_SIMPLE}
                />
              </div>
            )}
          </div>
        ) : (
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-12">
            <Empty
              description={
                <div className="text-center">
                  <p className="text-gray-600 mb-2">请设置筛选条件并点击"查询"按钮</p>
                  <p className="text-gray-400 text-sm">选择疾病后，可查看该疾病下的靶点研发进展格局</p>
                </div>
              }
              image={Empty.PRESENTED_IMAGE_SIMPLE}
            />
          </div>
        )}
      </div>

      {/* 固定的刷新按钮 */}
      <button
        onClick={handleQuery}
        className="fixed bottom-8 right-8 w-12 h-12 bg-blue-600 text-white rounded-full shadow-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-all"
        title="刷新数据"
      >
        <ReloadOutlined className="text-xl" />
      </button>
    </div>
  );
};

export default TargetProgress;
