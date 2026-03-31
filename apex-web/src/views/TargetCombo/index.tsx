import { useState, useRef, useCallback } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { message } from 'antd';
import { getDiseaseTree, getCompetitionMatrix, getCellDrugs, exportCompetition } from '@/services/competition';
import { getFilterPresets, createFilterPreset } from '@/services/filterPreset';
import type {
  DiseaseNode, DiseaseItem, MatrixRow, MatrixColumn,
  CellDrug, CompetitionMatrixRequest, FilterPreset,
} from '@/types';
import { ALL_PHASES, scoreToClass, scoreToColor } from '@/types';

/* ─── helpers ─────────────────────────────────────────────── */
function flattenDiseases(nodes: DiseaseNode[]): DiseaseItem[] {
  const result: DiseaseItem[] = [];
  for (const ta of nodes) {
    for (const d of ta.children ?? []) {
      result.push({ id: d.id, label: d.label, taId: ta.id, taLabel: ta.label });
    }
  }
  return result;
}

/* ─── Phase badge config ────────────────────────────────────── */
const PHASE_BADGE_CONFIG = [
  { phase: '批准上市', cls: 'pb-approved', label: '4.0 批准上市' },
  { phase: '申请上市', cls: 'pb-bla',      label: '3.5 申请上市' },
  { phase: 'III期临床', cls: 'pb-p3',      label: '3.0 III期' },
  { phase: 'II/III期临床', cls: 'pb-p2p3', label: '2.5 II/III期' },
  { phase: 'II期临床', cls: 'pb-p2',       label: '2.0 II期' },
  { phase: 'I/II期临床', cls: 'pb-p1p2',   label: '1.5 I/II期' },
  { phase: 'I期临床', cls: 'pb-p1',        label: '1.0 I期' },
  { phase: '申报临床', cls: 'pb-ind',       label: '0.5 申报临床' },
  { phase: '临床前', cls: 'pb-prec',        label: '0.1 临床前' },
] as const;

/* ─── Disease multi-select dropdown ────────────────────────── */
interface DiseaseSelectProps {
  tree: DiseaseNode[];
  selected: number[];
  onChange: (ids: number[]) => void;
}

function DiseaseSelect({ tree, selected, onChange }: DiseaseSelectProps) {
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState('');
  const containerRef = useRef<HTMLDivElement>(null);

  const all = flattenDiseases(tree);
  const filtered = search
    ? all.filter(d => d.label.toLowerCase().includes(search.toLowerCase()))
    : all;

  const toggle = (id: number) => {
    onChange(selected.includes(id) ? selected.filter(x => x !== id) : [...selected, id]);
  };

  const selectAll = () => onChange(all.map(d => d.id));
  const clearAll  = () => onChange([]);

  const displayItems = selected.slice(0, 2).map(id => all.find(d => d.id === id)?.label ?? String(id));
  const extra = selected.length - 2;

  return (
    <div ref={containerRef} style={{ position: 'relative' }}>
      {open && <div className="apex-dropdown-overlay" onClick={() => setOpen(false)} />}
      <div className="apex-tags-input" onClick={() => setOpen(v => !v)}>
        {selected.length === 0 && <span style={{ color: 'var(--text-muted)', fontSize: 12 }}>请选择疾病…</span>}
        {displayItems.map((label, i) => (
          <span key={selected[i]} className="apex-tag">
            <span>{label.length > 16 ? label.slice(0, 14) + '…' : label}</span>
            <span className="tag-x" onClick={e => { e.stopPropagation(); toggle(selected[i]); }}>×</span>
          </span>
        ))}
        {extra > 0 && <span className="apex-tag-more">+{extra}</span>}
        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="var(--text-muted)" strokeWidth="2" style={{ marginLeft: 'auto', flexShrink: 0 }}>
          <path d="M6 9l6 6 6-6" />
        </svg>
      </div>

      {open && (
        <div className="apex-dropdown" style={{ minWidth: 340 }}>
          <div className="apex-dropdown-search">
            <input placeholder="搜索疾病…" value={search} onChange={e => setSearch(e.target.value)} autoFocus />
          </div>
          <div className="apex-dropdown-list">
            {search
              ? filtered.map(d => (
                  <div key={d.id} className={`apex-dropdown-item${selected.includes(d.id) ? ' selected' : ''}`}
                    onClick={() => toggle(d.id)}>
                    <span className="check" />
                    <span>{d.label}</span>
                    <span style={{ marginLeft: 'auto', fontSize: 10, color: 'var(--text-muted)' }}>{d.taLabel}</span>
                  </div>
                ))
              : tree.map(ta => (
                  <div key={ta.id}>
                    <div className="apex-dropdown-group-label">{ta.label}</div>
                    {(ta.children ?? []).map(d => (
                      <div key={d.id} className={`apex-dropdown-item${selected.includes(d.id) ? ' selected' : ''}`}
                        onClick={() => toggle(d.id)}>
                        <span className="check" />
                        <span>{d.label}</span>
                      </div>
                    ))}
                  </div>
                ))
            }
          </div>
          <div className="apex-dropdown-footer">
            <span className="count">已选 {selected.length} / {all.length}</span>
            <div style={{ display: 'flex', gap: 12 }}>
              <button onClick={selectAll}>全选</button>
              <button onClick={clearAll}>清空</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

/* ─── Tooltip ───────────────────────────────────────────────── */
interface TooltipState {
  x: number;
  y: number;
  target: string;
  diseaseName: string;
  drugs: CellDrug[];
  loading: boolean;
}

/* ─── Save preset modal ─────────────────────────────────────── */
interface SaveModalProps {
  onSave: (name: string) => void;
  onClose: () => void;
}
function SaveModal({ onSave, onClose }: SaveModalProps) {
  const [name, setName] = useState('');
  return (
    <div className="apex-modal-overlay" onClick={onClose}>
      <div className="apex-modal" onClick={e => e.stopPropagation()}>
        <h3>保存筛选条件</h3>
        <div className="apex-form-item">
          <label className="apex-form-label">预设名称</label>
          <input className="apex-input" value={name} onChange={e => setName(e.target.value)}
            placeholder="如：GI领域查询" autoFocus />
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
export default function TargetCombo() {
  // Filter state
  const [selectedDiseases, setSelectedDiseases] = useState<number[]>([]);
  const [selectedPhases, setSelectedPhases] = useState<string[]>([...ALL_PHASES]);
  const [hideNoCombo, setHideNoCombo] = useState(false);
  const [currentPreset, setCurrentPreset] = useState<string>('系统默认筛选');
  const [showSaveModal, setShowSaveModal] = useState(false);

  // Matrix data
  const [columns, setColumns] = useState<MatrixColumn[]>([]);
  const [rows, setRows] = useState<MatrixRow[]>([]);
  const [queried, setQueried] = useState(false);

  // Tooltip
  const [tooltip, setTooltip] = useState<TooltipState | null>(null);
  const tooltipTimerRef = useRef<ReturnType<typeof setTimeout>>();

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

  // Presets
  const { data: presetsData, refetch: refetchPresets } = useQuery({
    queryKey: ['presets-competition'],
    queryFn: async () => {
      const res = await getFilterPresets('competition');
      return res.code === 0 ? res.data : [];
    },
  });
  const presets: FilterPreset[] = presetsData ?? [];

  // Matrix mutation
  const matrixMutation = useMutation({
    mutationFn: (req: CompetitionMatrixRequest) => getCompetitionMatrix(req),
    onSuccess: (res) => {
      if (res.code === 0 && res.data) {
        setColumns(res.data.columns);
        setRows(res.data.rows);
        setQueried(true);
      } else {
        message.error(res.message || '查询失败');
      }
    },
    onError: () => message.error('查询失败，请重试'),
  });

  // Save preset mutation
  const savePresetMutation = useMutation({
    mutationFn: (name: string) => createFilterPreset({
      name,
      module: 'competition',
      conditions: { diseaseIds: selectedDiseases, phases: selectedPhases, hideNoComboTargets: hideNoCombo },
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
    if (selectedDiseases.length === 0) { message.warning('请至少选择一个疾病'); return; }
    matrixMutation.mutate({ diseaseIds: selectedDiseases, phases: selectedPhases, hideNoComboTargets: hideNoCombo });
  };

  const handleReset = () => {
    setSelectedDiseases([]);
    setSelectedPhases([...ALL_PHASES]);
    setHideNoCombo(false);
    setColumns([]);
    setRows([]);
    setQueried(false);
    setCurrentPreset('系统默认筛选');
  };

  const togglePhase = (phase: string) => {
    setSelectedPhases(prev =>
      prev.includes(phase) ? prev.filter(p => p !== phase) : [...prev, phase]
    );
  };

  const toggleAllPhases = () => {
    setSelectedPhases(prev => prev.length === ALL_PHASES.length ? [] : [...ALL_PHASES]);
  };

  const loadPreset = (preset: FilterPreset) => {
    const c = preset.conditions;
    if (c.diseaseIds) setSelectedDiseases(c.diseaseIds);
    if (c.phases)    setSelectedPhases(c.phases);
    if (typeof c.hideNoComboTargets === 'boolean') setHideNoCombo(c.hideNoComboTargets);
    setCurrentPreset(preset.name);
  };

  // Cell hover — debounced tooltip
  const handleCellEnter = useCallback((
    e: React.MouseEvent,
    target: string,
    col: MatrixColumn,
    drugCount: number
  ) => {
    if (drugCount === 0) return;
    const rect = (e.target as HTMLElement).getBoundingClientRect();
    const x = rect.right + 10;
    const y = rect.top;

    clearTimeout(tooltipTimerRef.current);
    tooltipTimerRef.current = setTimeout(async () => {
      setTooltip({ x, y, target, diseaseName: col.diseaseName, drugs: [], loading: true });
      try {
        const res = await getCellDrugs({ target, diseaseId: col.diseaseId, phases: selectedPhases });
        if (res.code === 0) {
          setTooltip(prev => prev ? { ...prev, drugs: res.data.drugs, loading: false } : null);
        }
      } catch {
        setTooltip(prev => prev ? { ...prev, loading: false } : null);
      }
    }, 200);
  }, [selectedPhases]);

  const handleCellLeave = useCallback(() => {
    clearTimeout(tooltipTimerRef.current);
    setTooltip(null);
  }, []);

  // Tooltip position — flip if near right/bottom edge
  const tooltipStyle = tooltip ? (() => {
    const tw = 300, th = 280;
    let left = tooltip.x;
    let top  = tooltip.y;
    if (left + tw > window.innerWidth - 16)  left = tooltip.x - tw - 50;
    if (top  + th > window.innerHeight - 16) top  = window.innerHeight - th - 16;
    return { left, top };
  })() : null;

  const handleExport = async () => {
    if (!queried) { message.warning('请先查询'); return; }
    try {
      const blob = await exportCompetition({ diseaseIds: selectedDiseases, phases: selectedPhases, hideNoComboTargets: hideNoCombo });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `competition_matrix_${new Date().toISOString().slice(0,10)}.xlsx`;
      a.click();
      URL.revokeObjectURL(url);
    } catch { message.error('导出失败'); }
  };

  return (
    <div>
      {/* Tab bar */}
      <div className="apex-tab-bar">
        <button className="apex-tab-btn active">靶点组合竞争格局</button>
        <button className="apex-tab-btn" disabled>
          多抗组合详细信息查询 <span style={{ fontSize: 10, color: 'var(--accent-amber)' }}>(预留)</span>
        </button>
      </div>

      {/* Preset selector */}
      <div className="apex-preset-selector">
        <span className="apex-preset-label">当前筛选：</span>
        <select
          className="apex-select"
          style={{ minWidth: 160 }}
          value={currentPreset}
          onChange={e => {
            const p = presets.find(x => x.name === e.target.value);
            if (p) loadPreset(p); else setCurrentPreset(e.target.value);
          }}
        >
          <option value="系统默认筛选">系统默认筛选</option>
          {presets.map(p => <option key={p.id} value={p.name}>{p.name}</option>)}
        </select>
      </div>

      {/* Filter bar */}
      <div className="apex-filter-bar">
        <div className="apex-filter-group">
          <span className="apex-filter-label">疾病：</span>
          <DiseaseSelect tree={tree} selected={selectedDiseases} onChange={setSelectedDiseases} />
        </div>

        <div className="apex-toggle-group">
          <span className="apex-toggle-label">隐藏无组合的靶点：</span>
          <div className={`apex-toggle-track${hideNoCombo ? ' on' : ''}`} onClick={() => setHideNoCombo(v => !v)}>
            <div className="apex-toggle-thumb" />
          </div>
        </div>

        <button className="apex-btn apex-btn-primary" onClick={handleQuery} disabled={matrixMutation.isPending}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="2">
            <circle cx="11" cy="11" r="8" /><line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          {matrixMutation.isPending ? '查询中…' : '查 询'}
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

      {/* Phase filter */}
      <div className="apex-section-label">研发阶段筛选</div>
      <div className="apex-phase-bar">
        <span
          className={`apex-phase-badge pb-all${selectedPhases.length === ALL_PHASES.length ? ' selected' : ''}`}
          onClick={toggleAllPhases}
        >
          <span className="dot" />全选
        </span>
        {PHASE_BADGE_CONFIG.map(({ phase, cls, label }) => (
          <span
            key={phase}
            className={`apex-phase-badge ${cls}${selectedPhases.includes(phase) ? ' selected' : ''}`}
            onClick={() => togglePhase(phase)}
          >
            <span className="dot" />{label}
          </span>
        ))}
      </div>

      {/* Matrix section */}
      <div className="apex-section-label">竞争格局矩阵</div>

      {matrixMutation.isPending && (
        <div className="apex-loading"><div className="apex-spinner" /><span>查询中…</span></div>
      )}

      {!matrixMutation.isPending && !queried && (
        <div className="apex-empty">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="var(--border)" strokeWidth="1.5">
            <rect x="3" y="3" width="7" height="7" rx="1" /><rect x="14" y="3" width="7" height="7" rx="1" />
            <rect x="3" y="14" width="7" height="7" rx="1" /><rect x="14" y="14" width="7" height="7" rx="1" />
          </svg>
          <span>请选择疾病并点击查询</span>
        </div>
      )}

      {!matrixMutation.isPending && queried && rows.length === 0 && (
        <div className="apex-empty"><span>暂无数据</span></div>
      )}

      {!matrixMutation.isPending && queried && rows.length > 0 && (
        <div className="matrix-wrap">
          <button className="apex-download-btn" title="导出 Excel" onClick={handleExport}>
            <svg viewBox="0 0 24 24">
              <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4" />
              <polyline points="7 10 12 15 17 10" />
              <line x1="12" y1="15" x2="12" y2="3" />
            </svg>
          </button>

          <div className="matrix-container">
            <table className="matrix-table">
              <thead>
                <tr>
                  <th className="th-corner">Target</th>
                  <th className="th-hp-col">Highest<br />Phase</th>
                  {columns.map(col => (
                    <th key={col.diseaseId} title={col.diseaseName}>
                      {col.diseaseName.length > 12 ? col.diseaseName.slice(0, 10) + '…' : col.diseaseName}
                    </th>
                  ))}
                </tr>
                <tr>
                  <th className="th-corner" style={{ fontSize: 11 }}>Highest<br />Phase</th>
                  <th className="th-hp-col" />
                  {columns.map(col => {
                    const maxScore = Math.max(0, ...rows.map(r => r.cells.find(c => c.diseaseId === col.diseaseId)?.score ?? 0));
                    return (
                      <th key={col.diseaseId}>
                        {maxScore > 0 && (
                          <span className="hp-badge" style={{ background: scoreToColor(maxScore) }}>
                            {maxScore.toFixed(1)}
                          </span>
                        )}
                      </th>
                    );
                  })}
                </tr>
              </thead>
              <tbody>
                {rows.map(row => (
                  <tr key={row.target}>
                    <td className="td-target">{row.target}</td>
                    <td className="td-hp">
                      {row.maxScore > 0 && (
                        <span className="hp-badge" style={{ background: scoreToColor(row.maxScore) }}>
                          {row.maxScore.toFixed(1)}
                        </span>
                      )}
                    </td>
                    {columns.map(col => {
                      const cell = row.cells.find(c => c.diseaseId === col.diseaseId);
                      const score = cell?.score ?? 0;
                      return (
                        <td key={col.diseaseId}>
                          {score > 0 && (
                            <span
                              className={`score-cell ${scoreToClass(score)}`}
                              onMouseEnter={e => handleCellEnter(e, row.target, col, cell?.drugCount ?? 0)}
                              onMouseLeave={handleCellLeave}
                              title={`${row.target} × ${col.diseaseName}: ${cell?.phaseName ?? ''}`}
                            >
                              {score.toFixed(1)}
                            </span>
                          )}
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

      {/* Tooltip */}
      {tooltip && tooltipStyle && (
        <div className="apex-tooltip" style={{ left: tooltipStyle.left, top: tooltipStyle.top }}>
          <div className="tt-title">{tooltip.target}</div>
          <div className="tt-subtitle">{tooltip.diseaseName}</div>
          {tooltip.loading && <div style={{ color: 'var(--text-muted)', fontSize: 12 }}>加载中…</div>}
          {!tooltip.loading && tooltip.drugs.length === 0 && (
            <div style={{ color: 'var(--text-muted)', fontSize: 12 }}>暂无药品数据</div>
          )}
          {!tooltip.loading && tooltip.drugs.slice(0, 3).map((drug, i) => (
            <div key={i} className="tt-drug">
              <div className="tt-row">
                <span className="tt-label">药品名</span>
                <span className="tt-value">{drug.drugNameEn}</span>
              </div>
              <div className="tt-row">
                <span className="tt-label">原研机构</span>
                <span className="tt-value">{drug.originator}</span>
              </div>
              <div className="tt-row">
                <span className="tt-label">最高阶段</span>
                <span className="tt-value">{drug.highestPhase}</span>
              </div>
              {drug.highestPhaseDate && (
                <div className="tt-row">
                  <span className="tt-label">阶段日期</span>
                  <span className="tt-value">{drug.highestPhaseDate}</span>
                </div>
              )}
              {drug.nctId && (
                <div className="tt-row">
                  <span className="tt-label">NCT</span>
                  <span className="tt-value tt-mono">{drug.nctId}</span>
                </div>
              )}
            </div>
          ))}
          {!tooltip.loading && tooltip.drugs.length > 3 && (
            <div className="tt-more">共 {tooltip.drugs.length} 个药品，点击查看更多</div>
          )}
        </div>
      )}

      {/* Save modal */}
      {showSaveModal && (
        <SaveModal
          onSave={name => savePresetMutation.mutate(name)}
          onClose={() => setShowSaveModal(false)}
        />
      )}
    </div>
  );
}
