import React from 'react'

interface PhaseFilterBarProps {
  summary?: {
    approved: number
    bla: number
    phase3: number
    phase23: number
    phase2: number
    phase12: number
    phase1: number
    ind: number
    preClinical: number
    total: number
  }
  selectedPhases: string[]
  onPhaseToggle: (phase: string) => void
}

// 阶段定义
const PHASES = [
  { key: 'Approved', label: 'Approved', color: 'bg-phase-approved' },
  { key: 'BLA', label: 'BLA', color: 'bg-phase-bla' },
  { key: 'Phase III', label: 'Phase III', color: 'bg-phase-3' },
  { key: 'Phase II/III', label: 'Phase II/III', color: 'bg-phase-23' },
  { key: 'Phase II', label: 'Phase II', color: 'bg-phase-2' },
  { key: 'Phase I/II', label: 'Phase I/II', color: 'bg-phase-12' },
  { key: 'Phase I', label: 'Phase I', color: 'bg-phase-1' },
  { key: 'IND', label: 'IND', color: 'bg-phase-ind' },
  { key: 'PreClinical', label: 'PreClinical', color: 'bg-phase-preclinical' },
]

const PhaseFilterBar: React.FC<PhaseFilterBarProps> = ({ summary, selectedPhases, onPhaseToggle }) => {
  // 获取药物数量
  const getCount = (phaseKey: string): number => {
    if (!summary) return 0
    const countMap: Record<string, number> = {
      Approved: summary.approved,
      BLA: summary.bla,
      'Phase III': summary.phase3,
      'Phase II/III': summary.phase23,
      'Phase II': summary.phase2,
      'Phase I/II': summary.phase12,
      'Phase I': summary.phase1,
      IND: summary.ind,
      PreClinical: summary.preClinical,
    }
    return countMap[phaseKey] || 0
  }

  return (
    <div className="bg-white p-3 rounded-lg shadow-sm border border-border mb-4">
      <div className="flex gap-2 overflow-x-auto">
        {PHASES.map((phase) => {
          const count = getCount(phase.key)
          const isSelected = selectedPhases.includes(phase.key)

          return (
            <button
              key={phase.key}
              onClick={() => onPhaseToggle(phase.key)}
              className={`
                flex items-center gap-1.5 px-3 py-1.5 rounded-md text-sm font-medium
                transition-all duration-200 whitespace-nowrap
                ${isSelected 
                  ? `${phase.color} text-white shadow-sm` 
                  : 'bg-gray-50 text-gray-600 hover:bg-gray-100'
                }
              `}
              title={`点击${isSelected ? '取消' : '筛选'} ${phase.label}`}
            >
              <div className={`w-2 h-2 rounded-full ${isSelected ? 'bg-white' : phase.color}`} />
              <span>{phase.label}</span>
              <span className={`ml-1 ${isSelected ? 'text-white' : 'text-gray-500'}`}>
                ({count})
              </span>
            </button>
          )
        })}
      </div>
    </div>
  )
}

export default PhaseFilterBar
