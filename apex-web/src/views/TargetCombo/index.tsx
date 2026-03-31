import { useState, useRef, useCallback, useEffect } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { message } from 'antd';
import { getDiseaseTree, getCompetitionMatrix, getCellDrugs, exportCompetition } from '@/services/competition';
import { getFilterPresets, createFilterPreset } from '@/services/filterPreset';
import type {
  DiseaseNode, DiseaseItem, MatrixRow, MatrixColumn,
  CellDrug, CompetitionMatrixRequest,
} from '@/types';
import { ALL_PHASES, phaseToCardClass, scoreToClass, scoreToColor } from '@/types';

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

interface DiseaseSummaryItem {
  key: string;
  label: string;
  type: 'group' | 'leaf' | 'all';
  id?: number;
}

function getVisibleDiseaseGroups(nodes: DiseaseNode[], keyword: string): DiseaseNode[] {
  const search = keyword.trim().toLowerCase();
  if (!search) return nodes;

  return nodes.flatMap(group => {
    const children = group.children ?? [];
    const groupMatched = group.label.toLowerCase().includes(search);
    if (groupMatched) {
      return [{ ...group, children }];
    }

    const matchedChildren = children.filter(child => child.label.toLowerCase().includes(search));
    if (matchedChildren.length === 0) {
      return [];
    }

    return [{ ...group, children: matchedChildren }];
  });
}

function getSelectedDiseaseSummary(tree: DiseaseNode[], selected: number[], allDiseaseIds: number[]): DiseaseSummaryItem[] {
  if (selected.length === 0) {
    return [];
  }
  if (selected.length === allDiseaseIds.length) {
    return [{ key: 'all', label: '全部疾病', type: 'all' }];
  }

  const selectedSet = new Set(selected);
  const items: DiseaseSummaryItem[] = [];

  for (const group of tree) {
    const children = group.children ?? [];
    const childIds = children.map(child => child.id);
    const allChildrenSelected = childIds.length > 0 && childIds.every(id => selectedSet.has(id));

    if (allChildrenSelected) {
      items.push({ key: `group-${group.id}`, label: group.label, type: 'group', id: group.id });
      continue;
    }

    for (const child of children) {
      if (selectedSet.has(child.id)) {
        items.push({ key: `leaf-${child.id}`, label: child.label, type: 'leaf', id: child.id });
      }
    }
  }

  return items;
}

function normalizeDiseaseIds(ids: number[]): number[] {
  return Array.from(new Set(ids));
}

/* ─── Phase badge config ────────────────────────────────────── */
const PHASE_BADGE_CONFIG = [
  { phase: '批准上市', cls: 'pb-approved', label: '4.0 批准上市' },
  { phase: '申请上市', cls: 'pb-bla', label: '3.5 申请上市' },
  { phase: 'III期临床', cls: 'pb-p3', label: '3.0 III期' },
  { phase: 'II/III期临床', cls: 'pb-p2p3', label: '2.5 II/III期' },
  { phase: 'II期临床', cls: 'pb-p2', label: '2.0 II期' },
  { phase: 'I/II期临床', cls: 'pb-p1p2', label: '1.5 I/II期' },
  { phase: 'I期临床', cls: 'pb-p1', label: '1.0 I期' },
  { phase: '申报临床', cls: 'pb-ind', label: '0.5 申报临床' },
  { phase: '临床前', cls: 'pb-prec', label: '0.1 临床前' },
] as const;

const TOOLTIP_VIEWPORT_PADDING = 16;
const TOOLTIP_GAP = 14;

/* ─── Disease multi-select dropdown ────────────────────────── */
interface DiseaseSelectProps {
  tree: DiseaseNode[];
  selected: number[];
  loading?: boolean;
  onChange: (ids: number[]) => void;
}

function DiseaseSelect({ tree, selected, loading = false, onChange }: DiseaseSelectProps) {
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState('');

  const all = flattenDiseases(tree);
  const allDiseaseIds = all.map(item => item.id);
  const selectedSet = new Set(selected);
  const visibleGroups = getVisibleDiseaseGroups(tree, search);
  const summaryItems = getSelectedDiseaseSummary(tree, selected, allDiseaseIds);
  const displayItems = summaryItems.slice(0, 2);
  const extra = summaryItems.length - displayItems.length;

  const applySelection = (ids: number[]) => {
    onChange(normalizeDiseaseIds(ids));
  };

  const toggleDisease = (id: number) => {
    applySelection(selectedSet.has(id) ? selected.filter(item => item !== id) : [...selected, id]);
  };

  const toggleGroup = (groupId: number) => {
    const sourceGroup = tree.find(group => group.id === groupId);
    if (!sourceGroup) return;

    const childIds = (sourceGroup.children ?? []).map(child => child.id);
    const allChildrenSelected = childIds.length > 0 && childIds.every(id => selectedSet.has(id));
    if (allChildrenSelected) {
      applySelection(selected.filter(id => !childIds.includes(id)));
      return;
    }
    applySelection([...selected, ...childIds]);
  };

  const removeSummaryItem = (item: DiseaseSummaryItem) => {
    if (item.type === 'group' && item.id != null) {
      const sourceGroup = tree.find(group => group.id === item.id);
      if (!sourceGroup) return;
      const childIds = new Set((sourceGroup.children ?? []).map(child => child.id));
      applySelection(selected.filter(id => !childIds.has(id)));
      return;
    }
    if (item.type === 'leaf' && item.id != null) {
      applySelection(selected.filter(id => id !== item.id));
    }
  };

  useEffect(() => {
    if (loading) {
      setOpen(false);
    }
  }, [loading]);

  return (
    <div style={{ position: 'relative' }}>
      {open && <div className="apex-dropdown-overlay" onClick={() => setOpen(false)} />}
      <div className={`apex-tags-input${loading ? ' is-loading' : ''}`} onClick={() => !loading && setOpen(v => !v)}>
        {loading && <span style={{ color: 'var(--text-muted)', fontSize: 12 }}>疾病数据加载中…</span>}
        {!loading && summaryItems.length === 0 && <span style={{ color: 'var(--text-muted)', fontSize: 12 }}>请选择疾病…</span>}
        {!loading && displayItems.map(item => (
          <span key={item.key} className="apex-tag">
            <span>{item.label.length > 16 ? item.label.slice(0, 14) + '…' : item.label}</span>
            {item.type !== 'all' && (
              <span className="tag-x" onClick={e => { e.stopPropagation(); removeSummaryItem(item); }}>×</span>
            )}
          </span>
        ))}
        {!loading && extra > 0 && <span className="apex-tag-more">+{extra}</span>}
        {loading ? (
          <span className="apex-inline-spinner" aria-hidden="true" style={{ marginLeft: 'auto' }} />
        ) : (
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="var(--text-muted)" strokeWidth="2" style={{ marginLeft: 'auto', flexShrink: 0 }}>
            <path d="M6 9l6 6 6-6" />
          </svg>
        )}
      </div>

      {open && !loading && (
        <div className="apex-dropdown" style={{ minWidth: 340 }}>
          <div className="apex-dropdown-search">
            <input placeholder="搜索治疗领域或疾病…" value={search} onChange={e => setSearch(e.target.value)} autoFocus />
          </div>
          <div className="apex-dropdown-list">
            {visibleGroups.map(group => {
              const sourceGroup = tree.find(item => item.id === group.id) ?? group;
              const fullChildren = sourceGroup.children ?? [];
              const visibleChildren = group.children ?? [];
              const childIds = fullChildren.map(child => child.id);
              const allChildrenSelected = childIds.length > 0 && childIds.every(id => selectedSet.has(id));
              const partiallySelected = !allChildrenSelected && childIds.some(id => selectedSet.has(id));

              return (
                <div key={group.id}>
                  <div
                    className={`apex-dropdown-item${allChildrenSelected ? ' selected' : ''}${partiallySelected ? ' partial' : ''}`}
                    onClick={() => toggleGroup(group.id)}
                  >
                    <span className="check" />
                    <span>{group.label}</span>
                    <span style={{ marginLeft: 'auto', fontSize: 10, color: 'var(--text-muted)' }}>{fullChildren.length} 个疾病</span>
                  </div>
                  {visibleChildren.map(d => (
                    <div key={d.id} className={`apex-dropdown-item${selectedSet.has(d.id) ? ' selected' : ''}`}
                      style={{ paddingLeft: 34 }}
                      onClick={() => toggleDisease(d.id)}>
                      <span className="check" />
                      <span>{d.label}</span>
                    </div>
                  ))}
                </div>
              );
            })}
            {visibleGroups.length === 0 && (
              <div className="apex-dropdown-item" style={{ cursor: 'default' }}>
                <span style={{ color: 'var(--text-muted)' }}>未找到匹配的治疗领域或疾病</span>
              </div>
            )}
          </div>
          <div className="apex-dropdown-footer">
            <span className="count">已选 {selected.length} / {allDiseaseIds.length}</span>
            <div style={{ display: 'flex', gap: 12 }}>
              <button onClick={() => applySelection(allDiseaseIds)}>全选</button>
              <button onClick={() => applySelection([])}>清空</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

/* ─── Tooltip ───────────────────────────────────────────────── */
interface TooltipState {
  anchorLeft: number;
  anchorRight: number;
  anchorTop: number;
  anchorBottom: number;
  target: string;
  pairTarget: string;
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
  const [showSaveModal, setShowSaveModal] = useState(false);

  // Matrix data
  const [columns, setColumns] = useState<MatrixColumn[]>([]);
  const [rows, setRows] = useState<MatrixRow[]>([]);
  const [queried, setQueried] = useState(false);

  // Tooltip
  const [tooltip, setTooltip] = useState<TooltipState | null>(null);
  const [tooltipExpanded, setTooltipExpanded] = useState(false);
  const [tooltipStyle, setTooltipStyle] = useState<{ left: number; top: number } | null>(null);
  const tooltipTimerRef = useRef<ReturnType<typeof setTimeout>>();
  const tooltipRef = useRef<HTMLDivElement>(null);
  const initializedDefaultSelectionRef = useRef(false);
  const autoQueriedRef = useRef(false);

  const clearTooltipTimer = () => {
    clearTimeout(tooltipTimerRef.current);
  };

  const scheduleTooltipHide = () => {
    clearTooltipTimer();
    tooltipTimerRef.current = setTimeout(() => {
      setTooltip(null);
      setTooltipExpanded(false);
    }, 120);
  };

  // Disease tree
  const { data: treeData, isLoading: treeLoading, isFetching: treeFetching } = useQuery({
    queryKey: ['disease-tree'],
    queryFn: async () => {
      const res = await getDiseaseTree();
      return res.code === 0 ? (res.data as DiseaseNode[]) : [];
    },
    staleTime: Infinity,
  });
  const tree: DiseaseNode[] = treeData ?? [];
  const diseaseSelectLoading = treeLoading || treeFetching;

  // Presets
  const { refetch: refetchPresets } = useQuery({
    queryKey: ['presets-competition'],
    queryFn: async () => {
      const res = await getFilterPresets('competition');
      return res.code === 0 ? res.data : [];
    },
  });

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

  useEffect(() => {
    if (initializedDefaultSelectionRef.current || tree.length === 0) {
      return;
    }

    const firstGroup = tree[0];
    const firstGroupDiseaseIds = (firstGroup.children ?? []).map(child => child.id);
    if (firstGroupDiseaseIds.length === 0) {
      initializedDefaultSelectionRef.current = true;
      return;
    }

    setSelectedDiseases(firstGroupDiseaseIds);
    initializedDefaultSelectionRef.current = true;
  }, [tree]);

  useEffect(() => {
    if (!initializedDefaultSelectionRef.current || autoQueriedRef.current || selectedDiseases.length === 0) {
      return;
    }

    autoQueriedRef.current = true;
    matrixMutation.mutate({
      diseaseIds: selectedDiseases,
      phases: selectedPhases,
      hideNoComboTargets: hideNoCombo,
    });
  }, [hideNoCombo, matrixMutation, selectedDiseases, selectedPhases]);

  const handleReset = () => {
    setSelectedDiseases([]);
    setSelectedPhases([...ALL_PHASES]);
    setHideNoCombo(false);
    setColumns([]);
    setRows([]);
    setQueried(false);
  };

  const togglePhase = (phase: string) => {
    setSelectedPhases(prev =>
      prev.includes(phase) ? prev.filter(p => p !== phase) : [...prev, phase]
    );
  };

  const toggleAllPhases = () => {
    setSelectedPhases(prev => prev.length === ALL_PHASES.length ? [] : [...ALL_PHASES]);
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

    clearTooltipTimer();
    tooltipTimerRef.current = setTimeout(async () => {
      setTooltipExpanded(false);
      setTooltipStyle(null);
      setTooltip({
        anchorLeft: rect.left,
        anchorRight: rect.right,
        anchorTop: rect.top,
        anchorBottom: rect.bottom,
        target,
        pairTarget: col.target,
        drugs: [],
        loading: true,
      });
      try {
        const res = await getCellDrugs({ target, pairTarget: col.target, diseaseIds: selectedDiseases, phases: selectedPhases });
        if (res.code === 0) {
          setTooltip(prev => prev ? { ...prev, drugs: res.data.drugs, loading: false } : null);
        }
      } catch {
        setTooltip(prev => prev ? { ...prev, loading: false } : null);
      }
    }, 200);
  }, [selectedDiseases, selectedPhases]);

  const handleCellLeave = useCallback(() => {
    scheduleTooltipHide();
  }, []);

  useEffect(() => {
    if (!tooltip) {
      setTooltipStyle(null);
      return;
    }

    const updateTooltipStyle = () => {
      const node = tooltipRef.current;
      if (!node) return;

      const tooltipWidth = node.offsetWidth;
      const tooltipHeight = node.offsetHeight;
      const viewportWidth = window.innerWidth;
      const viewportHeight = window.innerHeight;
      const roomOnRight = viewportWidth - tooltip.anchorRight - TOOLTIP_VIEWPORT_PADDING;
      const roomOnLeft = tooltip.anchorLeft - TOOLTIP_VIEWPORT_PADDING;

      let left = roomOnRight >= tooltipWidth || roomOnRight >= roomOnLeft
        ? Math.min(tooltip.anchorRight + TOOLTIP_GAP, viewportWidth - tooltipWidth - TOOLTIP_VIEWPORT_PADDING)
        : Math.max(TOOLTIP_VIEWPORT_PADDING, tooltip.anchorLeft - tooltipWidth - TOOLTIP_GAP);

      let top = tooltip.anchorTop;
      if (top + tooltipHeight > viewportHeight - TOOLTIP_VIEWPORT_PADDING) {
        const alignedBottom = tooltip.anchorBottom - tooltipHeight;
        top = alignedBottom >= TOOLTIP_VIEWPORT_PADDING
          ? alignedBottom
          : Math.max(TOOLTIP_VIEWPORT_PADDING, viewportHeight - tooltipHeight - TOOLTIP_VIEWPORT_PADDING);
      }

      left = Math.max(TOOLTIP_VIEWPORT_PADDING, Math.min(left, viewportWidth - tooltipWidth - TOOLTIP_VIEWPORT_PADDING));
      top = Math.max(TOOLTIP_VIEWPORT_PADDING, Math.min(top, viewportHeight - tooltipHeight - TOOLTIP_VIEWPORT_PADDING));

      setTooltipStyle(prev => (prev?.left === left && prev?.top === top ? prev : { left, top }));
    };

    const frameId = window.requestAnimationFrame(updateTooltipStyle);
    window.addEventListener('resize', updateTooltipStyle);

    return () => {
      window.cancelAnimationFrame(frameId);
      window.removeEventListener('resize', updateTooltipStyle);
    };
  }, [tooltip, tooltipExpanded]);

  const handleExport = async () => {
    if (!queried) { message.warning('请先查询'); return; }
    try {
      const blob = await exportCompetition({ diseaseIds: selectedDiseases, phases: selectedPhases, hideNoComboTargets: hideNoCombo });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `competition_matrix_${new Date().toISOString().slice(0, 10)}.xlsx`;
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
      {/* <div className="apex-preset-selector">
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
      </div> */}

      {/* Filter bar */}
      <div className="apex-filter-bar">
        <div className="apex-filter-group">
          <span className="apex-filter-label">疾病：</span>
          <DiseaseSelect tree={tree} selected={selectedDiseases} loading={diseaseSelectLoading} onChange={setSelectedDiseases} />
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
        {/* <button className="apex-btn apex-btn-secondary" onClick={() => setShowSaveModal(true)}>
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M19 21H5a2 2 0 01-2-2V5a2 2 0 012-2h11l5 5v11a2 2 0 01-2 2z" />
            <polyline points="17 21 17 13 7 13 7 21" />
          </svg>
          保 存
        </button> */}
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
                    <th key={col.target} title={col.target}>
                      {col.target.length > 12 ? col.target.slice(0, 10) + '…' : col.target}
                    </th>
                  ))}
                </tr>
                <tr>
                  <th className="th-corner" style={{ fontSize: 11 }}>Highest<br />Phase</th>
                  <th className="th-hp-col" />
                  {columns.map(col => (
                    <th key={col.target}>
                      {col.maxScore > 0 && (
                        <span className="hp-badge" style={{ background: scoreToColor(col.maxScore) }}>
                          {col.maxScore.toFixed(1)}
                        </span>
                      )}
                    </th>
                  ))}
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
                      const cell = row.cells.find(c => c.target === col.target);
                      const score = cell?.score ?? 0;
                      return (
                        <td key={col.target}>
                          {score > 0 && (
                            <span
                              className={`score-cell ${scoreToClass(score)}`}
                              onMouseEnter={e => handleCellEnter(e, row.target, col, cell?.drugCount ?? 0)}
                              onMouseLeave={handleCellLeave}
                              title={`${row.target} × ${col.target}: ${cell?.phaseName ?? ''}`}
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
      {tooltip && (
        <div
          ref={tooltipRef}
          className="apex-tooltip apex-tooltip-combo"
          style={{
            left: tooltipStyle?.left ?? TOOLTIP_VIEWPORT_PADDING,
            top: tooltipStyle?.top ?? TOOLTIP_VIEWPORT_PADDING,
            visibility: tooltipStyle ? 'visible' : 'hidden',
          }}
          onMouseEnter={clearTooltipTimer}
          onMouseLeave={scheduleTooltipHide}
        >
          <div className="tt-header">
            <div>
              <div className="tt-title">{tooltip.target} + {tooltip.pairTarget}</div>
              <div className="tt-subtitle">靶点组合药品明细</div>
            </div>
            <span className="tt-count">{tooltip.loading ? '加载中' : `${tooltip.drugs.length} 个药品`}</span>
          </div>
          {tooltip.loading && <div className="tt-state">加载中…</div>}
          {!tooltip.loading && tooltip.drugs.length === 0 && (
            <div className="tt-state">暂无药品数据</div>
          )}
          {!tooltip.loading && (tooltipExpanded ? tooltip.drugs : tooltip.drugs.slice(0, 3)).map((drug, i) => (
            <div key={`${drug.drugNameEn}-${drug.nctId ?? i}`} className={`tt-drug-card ${phaseToCardClass(drug.highestPhase)}`}>
              <div className="tt-drug-top">
                <div className="tt-drug-name">药品信息</div>
                <span className="tt-phase-pill">{drug.highestPhase || '未知阶段'}</span>
              </div>
              <div className="tt-row">
                <span className="tt-label">药品英文名</span>
                <span className="tt-value">{drug.drugNameEn || '—'}</span>
              </div>
              <div className="tt-row">
                <span className="tt-label">原研机构</span>
                <span className="tt-value">{drug.originator || '—'}</span>
              </div>
              <div className="tt-row">
                <span className="tt-label">所有研究机构</span>
                <span className="tt-value">{drug.researchInstitute || '—'}</span>
              </div>
              <div className="tt-row">
                <span className="tt-label">最高阶段日期</span>
                <span className="tt-value">{drug.highestPhaseDate || '—'}</span>
              </div>
              <div className="tt-row">
                <span className="tt-label">nctId</span>
                <span className="tt-value tt-mono">{drug.nctId || '—'}</span>
              </div>
            </div>
          ))}
          {!tooltip.loading && tooltip.drugs.length > 3 && (
            <button
              type="button"
              className="tt-more tt-more-btn"
              onClick={() => setTooltipExpanded(value => !value)}
            >
              已显示 {tooltipExpanded ? tooltip.drugs.length : 3} / {tooltip.drugs.length}，点击{tooltipExpanded ? '收起' : '查看更多'}
            </button>
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
