import React from 'react'
import type { MatrixDrug } from '@/types'

interface TooltipCardProps {
  drugs: MatrixDrug[]
  visible: boolean
  position: { x: number; y: number }
  onClose?: () => void
}

const TooltipCard: React.FC<TooltipCardProps> = ({ drugs, visible, position, onClose }) => {
  if (!visible || drugs.length === 0) {
    return null
  }

  return (
    <div
      className="fixed z-50 bg-white rounded-lg shadow-xl border border-border p-3 min-w-[280px] max-h-[400px] overflow-y-auto"
      style={{
        left: `${position.x}px`,
        top: `${position.y}px`,
      }}
    >
      <div className="flex justify-between items-center mb-2">
        <h3 className="font-semibold text-sm text-gray-700">药物列表</h3>
        {onClose && (
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 text-sm"
          >
            ✕
          </button>
        )}
      </div>
      <div className="space-y-2">
        {drugs.slice(0, 5).map((drug, index) => (
          <div
            key={index}
            className="bg-gray-50 rounded p-2 text-xs border border-gray-100"
          >
            <div className="flex justify-between items-start mb-1">
              <span className="font-medium text-gray-900 truncate flex-1">
                {drug.id.nameEn}
              </span>
              {drug.id.name && (
                <span className="text-gray-500 ml-2 truncate">
                  {drug.id.name}
                </span>
              )}
            </div>
            <div className="flex justify-between items-center text-gray-600">
              <span>{drug.global_highest_phase}</span>
              <span className="truncate max-w-[120px]">{drug.originator}</span>
            </div>
          </div>
        ))}
        {drugs.length > 5 && (
          <div className="text-center text-xs text-gray-500">
            还有 {drugs.length - 5} 个药物...
          </div>
        )}
      </div>
    </div>
  )
}

export default TooltipCard
