import React from 'react';
import { Tooltip } from 'antd';
import { DrugInfo } from '../usePipelineData';

interface PipelineCardProps {
  drug: DrugInfo;
}

// 研发阶段颜色映射
const PHASE_COLORS: Record<string, string> = {
  'Approved': '#ef4444',
  'BLA': '#a855f7',
  'Phase III': '#3b82f6',
  'Phase II/III': '#0ea5e9',
  'Phase II': '#06b6d4',
  'Phase I/II': '#14b8a6',
  'Phase I': '#10b981',
  'IND': '#f59e0b',
  'PreClinical': '#6b7280'
};

// 获取阶段颜色
const getPhaseColor = (phase: string): string => {
  return PHASE_COLORS[phase] || '#6b7280';
};

const PipelineCard: React.FC<PipelineCardProps> = ({ drug }) => {
  const phaseColor = getPhaseColor(drug.highestPhase);

  // Tooltip 内容
  const tooltipContent = (
    <div className="w-80">
      <div className="text-sm font-semibold text-cyan-400 mb-3 border-b border-gray-700 pb-2">
        药物详细信息
      </div>
      <div className="space-y-2 text-xs">
        <div className="flex justify-between">
          <span className="text-gray-500">药物名称（英）</span>
          <span className="text-white font-medium">{drug.drugNameEn}</span>
        </div>
        {drug.drugNameCn && (
          <div className="flex justify-between">
            <span className="text-gray-500">药物名称（中）</span>
            <span className="text-white font-medium">{drug.drugNameCn}</span>
          </div>
        )}
        <div className="flex justify-between">
          <span className="text-gray-500">药物 ID</span>
          <span className="text-gray-300">{drug.drugId}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-gray-500">研发阶段</span>
          <span style={{ color: phaseColor }} className="font-medium">
            {drug.highestPhase}
          </span>
        </div>
        <div className="flex justify-between">
          <span className="text-gray-500">阶段分值</span>
          <span className="text-white">{drug.highestPhaseScore.toFixed(1)}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-gray-500">原研机构</span>
          <span className="text-white text-right max-w-[160px] truncate">
            {drug.originator}
          </span>
        </div>
        {drug.researchInstitute && (
          <div className="flex justify-between">
            <span className="text-gray-500">研究机构</span>
            <span className="text-white text-right max-w-[160px] truncate">
              {drug.researchInstitute}
            </span>
          </div>
        )}
        {drug.moa && (
          <div className="flex justify-between">
            <span className="text-gray-500">作用机制</span>
            <span className="text-white text-right max-w-[160px] truncate">
              {drug.moa}
            </span>
          </div>
        )}
        {drug.highestTrialId && (
          <div className="flex justify-between">
            <span className="text-gray-500">试验 ID</span>
            <span className="text-gray-300">{drug.highestTrialId}</span>
          </div>
        )}
        {drug.nctId && (
          <div className="flex justify-between">
            <span className="text-gray-500">NCT ID</span>
            <span className="text-cyan-400">{drug.nctId}</span>
          </div>
        )}
        {drug.highestPhaseDate && (
          <div className="flex justify-between">
            <span className="text-gray-500">阶段日期</span>
            <span className="text-white">{drug.highestPhaseDate}</span>
          </div>
        )}
      </div>
    </div>
  );

  return (
    <Tooltip title={tooltipContent} placement="rightTop">
      <div
        className="bg-slate-800 border border-slate-700 rounded-lg p-3 mb-2 cursor-pointer hover:bg-slate-700 hover:translate-x-1 hover:shadow-lg transition-all duration-300 border-l-3"
        style={{
          borderLeftColor: phaseColor,
          borderLeftWidth: '3px'
        }}
      >
        {/* 左侧：药物基本信息 */}
        <div className="flex items-start justify-between">
          <div className="flex-1">
            <div className="text-sm font-semibold mb-1" style={{ color: phaseColor }}>
              {drug.drugNameEn}
              {drug.drugNameCn && (
                <span className="ml-2 text-xs text-gray-400">({drug.drugNameCn})</span>
              )}
            </div>
            <div className="text-xs text-gray-500 font-mono mb-1">
              ID: {drug.drugId}
            </div>
          </div>
          
          {/* 右侧：阶段标签 */}
          <div
            className="px-2 py-1 rounded text-xs font-medium"
            style={{
              backgroundColor: `${phaseColor}20`,
              color: phaseColor
            }}
          >
            {drug.highestPhase}
          </div>
        </div>

        {/* 中间：研发信息 */}
        <div className="mt-2 space-y-1">
          {drug.originator && (
            <div className="flex items-center gap-2 text-xs">
              <span className="text-gray-500">原研机构</span>
              <span className="text-gray-300 truncate">{drug.originator}</span>
            </div>
          )}
          {drug.moa && (
            <div className="flex items-center gap-2 text-xs">
              <span className="text-gray-500">作用机制</span>
              <span className="text-gray-300 truncate">{drug.moa}</span>
            </div>
          )}
        </div>

        {/* 右侧：试验信息 */}
        {(drug.highestTrialId || drug.nctId) && (
          <div className="mt-2 pt-2 border-t border-gray-700 space-y-1">
            {drug.highestTrialId && (
              <div className="flex items-center gap-2 text-xs">
                <span className="text-gray-500">试验 ID</span>
                <span className="text-gray-400">{drug.highestTrialId}</span>
              </div>
            )}
            {drug.nctId && (
              <div className="flex items-center gap-2 text-xs">
                <span className="text-gray-500">NCT ID</span>
                <span className="text-cyan-400">{drug.nctId}</span>
              </div>
            )}
          </div>
        )}
      </div>
    </Tooltip>
  );
};

export default PipelineCard;
