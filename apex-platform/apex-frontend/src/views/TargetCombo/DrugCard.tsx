import React from 'react'
import type { MatrixDrug } from '@/types'

interface DrugCardProps {
  drug: MatrixDrug
  onClick?: () => void
}

const DrugCard: React.FC<DrugCardProps> = ({ drug, onClick }) => {
  // 根据阶段获取颜色
  const getPhaseColor = (phase: string): string => {
    const colorMap: Record<string, string> = {
      Approved: 'bg-phase-approved',
      BLA: 'bg-phase-bla',
      'Phase III': 'bg-phase-3',
      'Phase II/III': 'bg-phase-23',
      'Phase II': 'bg-phase-2',
      'Phase I/II': 'bg-phase-12',
      'Phase I': 'bg-phase-1',
      IND: 'bg-phase-ind',
      PreClinical: 'bg-phase-preclinical',
    }
    return colorMap[phase] || 'bg-gray-500'
  }

  const phaseColor = getPhaseColor(drug.global_highest_phase)

  return (
    <div
      className="bg-white rounded-lg shadow-sm border border-border p-3 cursor-pointer hover:shadow-md transition-shadow"
      onClick={onClick}
    >
      {/* 药物名称 */}
      <div className="mb-2">
        <h4 className="font-semibold text-sm text-gray-900 truncate">
          {drug.id.nameEn}
        </h4>
        {drug.id.name && (
          <p className="text-xs text-gray-500 truncate mt-0.5">
            {drug.id.name}
          </p>
        )}
      </div>

      {/* 研发阶段 */}
      <div className="flex items-center gap-2 mb-2">
        <div className={`w-2 h-2 rounded-full ${phaseColor}`} />
        <span className="text-xs font-medium text-gray-700">
          {drug.global_highest_phase}
        </span>
      </div>

      {/* 起源公司 */}
      {drug.originator && (
        <div className="mb-1.5">
          <span className="text-xs text-gray-600">
            <span className="font-medium">Originator:</span> {drug.originator}
          </span>
        </div>
      )}

      {/* 作用机制 */}
      {drug.moa && (
        <div className="mb-1.5">
          <p className="text-xs text-gray-600 line-clamp-2">
            <span className="font-medium">MOA:</span> {drug.moa}
          </p>
        </div>
      )}

      {/* 最高临床试验 */}
      {(drug.highest_trial_id || drug.nct_id) && (
        <div className="pt-2 border-t border-border">
          {drug.nct_id && (
            <p className="text-xs text-gray-500 mb-0.5">
              NCT: <span className="font-mono">{drug.nct_id}</span>
            </p>
          )}
          {drug.highest_trial_id && (
            <p className="text-xs text-gray-500">
              Trial: <span className="font-mono">{drug.highest_trial_id}</span>
            </p>
          )}
        </div>
      )}
    </div>
  )
}

export default DrugCard
