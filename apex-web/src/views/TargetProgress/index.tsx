import { useState, useRef, useCallback } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { message } from 'antd';
import { getDiseaseTree } from '@/services/competition';
import { getDiseaseTargets, getDiseaseView } from '@/services/progress';
import { getFilterPresets, createFilterPreset } from '@/services/filterPreset';
import type {
  DiseaseNode, ProgressDrug, ProgressTargetRow,
  DiseaseViewRequest, FilterPreset,
} from '@/types';
import { phaseToCardClass } from '@/types';

/* Pipeline phase columns — fixed order */
const PIPELINE_PHASES = [
  '临床前', '申报临床', 'I期临床', 'I/II期临床',
  'II期临床', 'II/III期临床', 'III期临床', '申请上市', '批准上市',
];

/* ─── Target multi-select dropdown ─────────────────────────── */
interface TargetSelectProps {
  options: string[];
  selected: string[];
  onChange: (targets: string[]) => void;
}

function TargetSelect({ options, selected, onChange }: TargetSelectProps) {
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState('');

  const filtered = search
    ? options.filter(t => t.toLowerCase().includes(search.toLowerCase()))
    : options;

  const toggle = (t: string) => {
    onChange(selected.includes(t) ? selected.filter(x => x !== t) : [...selected, t]);
  };

  const displayItems = selected.slice(0, 3);
  const extra = selected.length - 3;

  return (
    <div style={{ position: 'relative' }}>
      {open && <div className="apex-dropdown-overlay" onClick={() => setOpen(false)} />}
      <div className="apex-tags-input" onClick={() => setOpen(v => !v)}>
        {selected.length === 0 && <span style={{ color: 'var(--text-muted)', fontSize: 12 }}>请选择靶点…</span>}
        {displayItems.map(t => (
          <span key={t} className="apex-tag">
            <span>{t}</span>
            <span className="tag-x" onClick={e => { e.stopPropagation(); toggle(t); }}>×</span>
          </span>
        ))}
        {extra > 0 && <span className="apex-tag-more">+{extra} more</span>}
        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="var(--text-muted)" strokeWidth="2" style={{ marginLeft: 'auto', flexShrink: 0 }}>
          <path d="M6 9l6 6 6-6" />
        </svg>
      </div>

      {open && (
        <div className="apex-dropdown" style={{ minWidth: 260 }}>
          <div className="apex-dropdown-search">
            <input placeholder="搜索靶点…" value={search} onChange={e => setSearch(e.target.value)} autoFocus />
          </div>
          <div className="apex-dropdown-list">
            {filtered.map(t => (
              <div key={t} className={`apex-dropdown-item${selected.includes(t) ? ' selected' : ''}`}
                onClick={() => toggle(t)}>
                <span className="check" /><span>{t}</span>
              </div>
            ))}
          </div>
          <div className="apex-dropdown-footer">
            <span className="count">已选 {selected.length} / {options.length}</span>
            <div style={{ display: 'flex', gap: 12 }}>
              <button onClick={() => onChange([...options])}>全选</button>
              <button onClick={() => onChange([])}>清空</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

/* ─── Drug card tooltip ─────────────────────────────────────── */
interface DrugTooltip {
  x: number; y: number;
  drug: ProgressDrug;
  target: string;
  phase: string;
}

/* ─── Save preset modal ─────────────────────────────────────── */
function SaveModal({ onSave, onClose }: { onSave: (name: string) => void; onClose: () => void }) {
  const [name, setName] = useState('');
  return (
    <div className="apex-modal-overlay" onClick={onClose}>
      <div className="apex-modal" onClick={e => e.stopPropagation()}>
        <h3>保存筛选条件</h3>
        <div className="apex-form-item">
          <label className="apex-form-label">预设名称</label>
          <input className="apex-input" value={name} onChange={e => setName(e.target.value)} placeholder="如：皮肤科默认视图" autoFocus />
        </div>
        <div className="apex-modal-footer">
          <button className="apex-btn apex-btn-secondary" onClick={onClose}>取消</button>
          <button className="apex-btn apex-btn-primary" onClick={() => name && onSave(name)}>保存</button>
        </div>
      </div>
    </div>
  );
}

/* ─── Main component ────────────────────────────────────────── */
export default function TargetProgress() {
  const [selectedDiseaseId, setSelectedDiseaseId] = useState<number | null>(null);
  const [selectedTargets, setSelectedTargets] = useState<string[]>([]);
  const [currentPreset, setCurrentPreset] = useState<string>('系统默认筛选');
  const [showSaveModal, setShowSaveModal] = useState(false);

  const [diseaseName, setDiseaseName] = useState('');
  const [pipelinePhases, setPipelinePhases] = useState<string[]>(PIPELINE_PHASES);
  const [targetRows, setTargetRows] = useState<ProgressTargetRow[]>([]);
  const [queried, setQueried] = useState(false);

  const [tooltip, setTooltip] = useState<DrugTooltip | null>(null);
  const tooltipTimer = useRef<ReturnType<typeof setTimeout>>();

  // Disease tree
  const { data: treeData } = useQuery({
    queryKey: ['disease-tree'],
    queryFn: async () => {
      const res = await getDiseaseTree();
      return res.code === 0 ? (res.data as DiseaseNode[]) : [];
    },
    staleTime: Infinity,
  });
  const tree: DiseaseNode[] = treeData ?? [];

  // Targets for selected disease
  const { data: targetsData } = useQuery({
    queryKey: ['progress-targets', selectedDiseaseId],
    queryFn: async () => {
      if (!selectedDiseaseId) return [];
      const res = await getDiseaseTargets(selectedDiseaseId);
      return res.code === 0 ? res.data.map((t: any) => t.target ?? t) : [];
    },
    enabled: !!selectedDiseaseId,
  });
  const availableTargets: string[] = targetsData ?? [];

  // Auto-select all targets when disease changes
  const handleDiseaseChange = (id: number) => {
    setSelectedDiseaseId(id);
    setSelectedTargets([]);
    setQueried(false);
    setTargetRows([]);
  };

  // Once targets load, select all
  const prevDiseaseRef = useRef<number | null>(null);
  if (selectedDiseaseId && selectedDiseaseId !== prevDiseaseRef.current && availableTargets.length > 0) {
    prevDiseaseRef.current = selectedDiseaseId;
    setSelectedTargets([...availableTargets]);
  }

  // Presets
  const { data: presetsData, refetch: refetchPresets } = useQuery({
    queryKey: ['presets-progress'],
    queryFn: async () => {
      const res = await getFilterPresets('progress');
      return res.code === 0 ? res.data : [];
    },
  });
  const presets: FilterPreset[] = presetsData ?? [];

  // Disease view mutation
  const viewMutation = useMutation({
    mutationFn: (req: DiseaseViewRequest) => getDiseaseView(req),
    onSuccess: (res) => {
      if (res.code === 0 && res.data) {
        setDiseaseName(res.data.diseaseName);
        setPipelinePhases(res.data.phases?.length ? res.data.phases : PIPELINE_PHASES);
        setTargetRows(res.data.targetRows);
        setQueried(true);
      } else {
        message.error(res.message || '查询失败');
      }
    },
    onError: () => message.error('查询失败，请重试'),
  });

  const savePresetMutation = useMutation({
    mutationFn: (name: string) => createFilterPreset({
      name,
      module: 'progress',
      conditions: { diseaseId: selectedDiseaseId, targets: selectedTargets },
      isDefault: false,
    }),
    onSuccess: (res) => {
      if (res.code === 0) {
        message.success('保存成功');
        setCurrentPreset(res.data.name);
        refetchPresets();
        setShowSaveModal(false);
      }
    },
    onError: () => message.error('保存失败'),
  });

  const handleQuery = () => {
    if (!selectedDiseaseId) { message.warning('请选择疾病'); return; }
    if (selectedTargets.length === 0) { message.warning('请选择至少一个靶点'); return; }
    viewMutation.mutate({ diseaseId: selectedDiseaseId, targets: selectedTargets });
  };

  const handleReset = () => {
    setSelectedDiseaseId(null);
    setSelectedTargets([]);
    setQueried(false);
    setTargetRows([]);
    setDiseaseName('');
    setCurrentPreset('系统默认筛选');
  };

  const loadPreset = (preset: FilterPreset) => {
    const c = preset.conditions;
    if (c.diseaseId) { setSelectedDiseaseId(c.diseaseId); }
    if (c.targets)   { setSelectedTargets(c.targets); }
    setCurrentPreset(preset.name);
  };

  const handleDrugEnter = useCallback((e: React.MouseEvent, drug: ProgressDrug, target: string, phase: string) => {
    const rect = (e.target as HTMLElement).getBoundingClientRect();
    clearTimeout(tooltipTimer.current);
    tooltipTimer.current = setTimeout(() => {
      setTooltip({ x: rect.right + 10, y: rect.top, drug, target, phase });
    }, 150);
  }, []);

  const handleDrugLeave = useCallback(() => {
    clearTimeout(tooltipTimer.current);
    setTooltip(null);
  }, []);

  const tooltipStyle = tooltip ? (() => {
    const tw = 280, th = 220;
    let left = tooltip.x;
    let top  = tooltip.y;
    if (left + tw > window.innerWidth - 16)  left = tooltip.x - tw - 60;
    if (top  + th > window.innerHeight - 16) top  = window.innerHeight - th - 16;
    return { left, top };
  })() : null;

  return (
    <div>
      {/* Tab bar */}
      <div className="apex-tab-bar">
        <button className="apex-tab-btn active">疾病视图</button>
        <button className="apex-tab-btn" disabled>
          靶点视图 <span style={{ fontSize: 10, color: 'var(--accent-amber)' }}>(预留)</span>
        </button>
        <button className="apex-tab-btn" disabled>
          管线历史事件视图 <span style={{ fontSize: 10, color: 'var(--accent-amber)' }}>(预留)</span>
        </button>
      </div>

      {/* Preset */}
      <div className="apex-preset-selector">
        <span className="apex-preset-label">当前筛选：</span>
        <select className="apex-select" style={{ minWidth: 160 }} value={currentPreset}
          onChange={e => {
            const p = presets.find(x => x.name === e.target.value);
            if (p) loadPreset(p); else setCurrentPreset(e.target.value);
          }}>
          <option value="系统默认筛选">系统默认筛选</option>
          {presets.map(p => <option key={p.id} value={p.name}>{p.name}</option>)}
        </select>
      </div>

      {/* Filter bar */}
      <div className="apex-filter-bar">
        <div className="apex-filter-group">
          <span className="apex-filter-label">疾病：</span>
          <select className="apex-select"
            value={selectedDiseaseId ?? ''}
            onChange={e => { if (e.target.value) handleDiseaseChange(Number(e.target.value)); }}>
            <option value="">请选择疾病…</option>
            {tree.map(ta => (
              <optgroup key={ta.id} label={ta.label}>
                {(ta.children ?? []).map(d => (
                  <option key={d.id} value={d.id}>{d.label}</option>
                ))}
              </optgroup>
            ))}
          </select>
        </div>

        <div className="apex-filter-group">
          <span className="apex-filter-label">靶点：</span>
          <TargetSelect options={availableTargets} selected={selectedTargets} onChange={setSelectedTargets} />
        </div>

        <button className="apex-btn apex-btn-primary" onClick={handleQuery} disabled={viewMutation.isPending}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="2">
            <circle cx="11" cy="11" r="8" /><line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          {viewMutation.isPending ? '查询中…' : '查 询'}
        </button>

        <button className="apex-btn apex-btn-secondary" onClick={handleReset}>重 置</button>

        <button className="apex-btn apex-btn-secondary" onClick={() => setShowSaveModal(true)}>
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M19 21H5a2 2 0 01-2-2V5a2 2 0 012-2h11l5 5v11a2 2 0 01-2 2z" />
            <polyline points="17 21 17 13 7 13 7 21" />
          </svg>
          保 存
        </button>
      </div>

      {/* Pipeline */}
      {queried && <div className="apex-section-label">疾病: {diseaseName} — 靶点研发管线进展</div>}

      {viewMutation.isPending && (
        <div className="apex-loading"><div className="apex-spinner" /><span>查询中…</span></div>
      )}

      {!viewMutation.isPending && !queried && (
        <div className="apex-empty">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="var(--border)" strokeWidth="1.5">
            <circle cx="12" cy="12" r="9" /><path d="M12 3v9l6 3" />
          </svg>
          <span>请选择疾病和靶点并点击查询</span>
        </div>
      )}

      {!viewMutation.isPending && queried && targetRows.length === 0 && (
        <div className="apex-empty"><span>暂无数据</span></div>
      )}

      {!viewMutation.isPending && queried && targetRows.length > 0 && (
        <div style={{ position: 'relative' }}>
          <div className="pipeline-container">
            <table className="pipeline-table">
              <thead>
                <tr>
                  <th>靶点</th>
                  {pipelinePhases.map(ph => <th key={ph}>{ph}</th>)}
                </tr>
              </thead>
              <tbody>
                {targetRows.map(row => (
                  <tr key={row.target} className="pipeline-row">
                    <td>{row.target}</td>
                    {pipelinePhases.map(ph => {
                      const drugs = row.phaseDrugs[ph] ?? [];
                      return (
                        <td key={ph}>
                          {drugs.map((drug, i) => (
                            <div
                              key={i}
                              className={`drug-card ${phaseToCardClass(ph)}`}
                              onMouseEnter={e => handleDrugEnter(e, drug, row.target, ph)}
                              onMouseLeave={handleDrugLeave}
                            >
                              <div className="drug-name">{drug.drugNameEn}</div>
                              <div className="drug-org">{drug.originator}</div>
                              <div className="drug-meta">
                                {ph}{drug.highestPhaseDate ? ` · ${drug.highestPhaseDate}` : ''}{drug.nctId ? `\n${drug.nctId}` : ''}
                              </div>
                            </div>
                          ))}
                        </td>
                      );
                    })}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Drug tooltip */}
      {tooltip && tooltipStyle && (
        <div className="apex-tooltip" style={{ left: tooltipStyle.left, top: tooltipStyle.top }}>
          <div className="tt-title">{tooltip.drug.drugNameEn}</div>
          <div className="tt-subtitle">{tooltip.target} · {tooltip.phase}</div>
          <div className="tt-row">
            <span className="tt-label">原研机构</span>
            <span className="tt-value">{tooltip.drug.originator || '—'}</span>
          </div>
          <div className="tt-row">
            <span className="tt-label">研究机构</span>
            <span className="tt-value">{tooltip.drug.researchInstitute || '—'}</span>
          </div>
          {tooltip.drug.highestPhaseDate && (
            <div className="tt-row">
              <span className="tt-label">阶段日期</span>
              <span className="tt-value">{tooltip.drug.highestPhaseDate}</span>
            </div>
          )}
          {tooltip.drug.nctId && (
            <div className="tt-row">
              <span className="tt-label">NCT</span>
              <span className="tt-value tt-mono">{tooltip.drug.nctId}</span>
            </div>
          )}
        </div>
      )}

      {/* Save modal */}
      {showSaveModal && (
        <SaveModal onSave={name => savePresetMutation.mutate(name)} onClose={() => setShowSaveModal(false)} />
      )}
    </div>
  );
}
