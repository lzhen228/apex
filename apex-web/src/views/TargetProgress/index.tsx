import { useState, useRef, useCallback, useEffect } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { message } from 'antd';
import { getDiseaseTree } from '@/services/competition';
import { getDiseaseTargets, getDiseaseView } from '@/services/progress';
import { getFilterPresets, createFilterPreset } from '@/services/filterPreset';
import type {
  DiseaseNode, DiseaseItem, ProgressDrug, ProgressTargetRow,
  DiseaseViewRequest,
} from '@/types';
import { phaseToCardClass } from '@/types';

/* Pipeline phase columns — fixed order */
const PIPELINE_PHASES = [
  '临床前', '申报临床', 'I期临床', 'I/II期临床',
  'II期临床', 'II/III期临床', 'III期临床', '申请上市', '批准上市',
];

const TARGET_GROUP_INDEX = [
  'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
  'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '#',
];

const TOOLTIP_VIEWPORT_PADDING = 16;
const TOOLTIP_GAP = 14;

function flattenDiseases(nodes: DiseaseNode[]): DiseaseItem[] {
  const result: DiseaseItem[] = [];
  for (const ta of nodes) {
    for (const disease of ta.children ?? []) {
      result.push({ id: disease.id, label: disease.label, taId: ta.id, taLabel: ta.label });
    }
  }
  return result;
}

function getVisibleDiseaseGroups(nodes: DiseaseNode[], keyword: string): DiseaseNode[] {
  const search = keyword.trim().toLowerCase();
  if (!search) {
    return nodes;
  }

  return nodes.flatMap(group => {
    const children = (group.children ?? []).filter(child => child.label.toLowerCase().includes(search));
    if (group.label.toLowerCase().includes(search)) {
      return [{ ...group, children: group.children ?? [] }];
    }
    if (children.length === 0) {
      return [];
    }
    return [{ ...group, children }];
  });
}

function getTargetGroupKey(target: string): string {
  const first = target.trim().charAt(0).toUpperCase();
  return /^[A-Z]$/.test(first) ? first : '#';
}

function groupTargets(options: string[], keyword: string) {
  const search = keyword.trim().toLowerCase();
  const filtered = search
    ? options.filter(option => option.toLowerCase().includes(search))
    : options;

  const grouped = new Map<string, string[]>();
  for (const option of filtered) {
    const key = getTargetGroupKey(option);
    const items = grouped.get(key) ?? [];
    items.push(option);
    grouped.set(key, items);
  }

  return TARGET_GROUP_INDEX.flatMap(key => {
    const items = grouped.get(key);
    if (!items?.length) {
      return [];
    }
    return [{
      key,
      items: [...items].sort((left, right) => left.localeCompare(right, 'en', { sensitivity: 'base' })),
    }];
  });
}

/* ─── Disease single-select dropdown ───────────────────────── */
interface DiseaseSelectProps {
  tree: DiseaseNode[];
  selectedId: number | null;
  loading?: boolean;
  onChange: (id: number) => void;
}

function DiseaseSelect({ tree, selectedId, loading = false, onChange }: DiseaseSelectProps) {
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState('');

  const allDiseases = flattenDiseases(tree);
  const selectedDisease = allDiseases.find(item => item.id === selectedId) ?? null;
  const visibleGroups = getVisibleDiseaseGroups(tree, search);

  const handleSelect = (id: number) => {
    onChange(id);
    setOpen(false);
    setSearch('');
  };

  useEffect(() => {
    if (loading) {
      setOpen(false);
    }
  }, [loading]);

  return (
    <div style={{ position: 'relative' }}>
      {open && <div className="apex-dropdown-overlay" onClick={() => setOpen(false)} />}
      <button
        type="button"
        className={`apex-select-trigger${loading ? ' is-loading' : ''}`}
        onClick={() => !loading && setOpen(v => !v)}
        disabled={loading}
      >
        <div className="apex-select-trigger-content">
          <span className="apex-select-trigger-value">{loading ? '疾病数据加载中…' : (selectedDisease?.label ?? '暂无疾病')}</span>
          {!loading && selectedDisease && <span className="apex-select-trigger-meta">{selectedDisease.taLabel}</span>}
        </div>
        {loading ? (
          <span className="apex-inline-spinner" aria-hidden="true" />
        ) : (
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="var(--text-muted)" strokeWidth="2" style={{ flexShrink: 0 }}>
            <path d="M6 9l6 6 6-6" />
          </svg>
        )}
      </button>

      {open && (
        <div className="apex-dropdown" style={{ minWidth: 320 }}>
          <div className="apex-dropdown-search">
            <input
              placeholder="搜索治疗领域或疾病…"
              value={search}
              onChange={e => setSearch(e.target.value)}
              autoFocus
            />
          </div>
          <div className="apex-dropdown-list">
            {visibleGroups.map(group => (
              <div key={group.id}>
                <div className="apex-dropdown-group-label">{group.label}</div>
                {(group.children ?? []).map(disease => (
                  <div
                    key={disease.id}
                    className={`apex-dropdown-item${selectedId === disease.id ? ' selected' : ''}`}
                    style={{ paddingLeft: 18 }}
                    onClick={() => handleSelect(disease.id)}
                  >
                    <span className="check" />
                    <span>{disease.label}</span>
                  </div>
                ))}
              </div>
            ))}
            {visibleGroups.length === 0 && (
              <div className="apex-dropdown-item" style={{ cursor: 'default' }}>
                <span style={{ color: 'var(--text-muted)' }}>未找到匹配的疾病</span>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

/* ─── Target multi-select dropdown ─────────────────────────── */
interface TargetSelectProps {
  options: string[];
  selected: string[];
  loading?: boolean;
  onChange: (targets: string[]) => void;
}

function TargetSelect({ options, selected, loading = false, onChange }: TargetSelectProps) {
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState('');
  const groupRefs = useRef<Record<string, HTMLDivElement | null>>({});
  const groupedTargets = groupTargets(options, search);
  const activeGroupKeys = new Set(groupedTargets.map(group => group.key));

  const toggle = (t: string) => {
    onChange(selected.includes(t) ? selected.filter(x => x !== t) : [...selected, t]);
  };

  const displayItems = selected.slice(0, 3);
  const extra = selected.length - 3;

  const handleJumpToGroup = (groupKey: string) => {
    groupRefs.current[groupKey]?.scrollIntoView({ block: 'start' });
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
        {loading && <span style={{ color: 'var(--text-muted)', fontSize: 12 }}>靶点数据加载中…</span>}
        {!loading && selected.length === 0 && <span style={{ color: 'var(--text-muted)', fontSize: 12 }}>请选择靶点…</span>}
        {!loading && displayItems.map(t => (
          <span key={t} className="apex-tag">
            <span>{t}</span>
            <span className="tag-x" onClick={e => { e.stopPropagation(); toggle(t); }}>×</span>
          </span>
        ))}
        {!loading && extra > 0 && <span className="apex-tag-more">+{extra} more</span>}
        {loading ? (
          <span className="apex-inline-spinner" aria-hidden="true" style={{ marginLeft: 'auto' }} />
        ) : (
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="var(--text-muted)" strokeWidth="2" style={{ marginLeft: 'auto', flexShrink: 0 }}>
            <path d="M6 9l6 6 6-6" />
          </svg>
        )}
      </div>

      {open && (
        <div className="apex-dropdown" style={{ minWidth: 420 }}>
          <div className="apex-dropdown-search">
            <input placeholder="搜索靶点…" value={search} onChange={e => setSearch(e.target.value)} autoFocus />
          </div>
          <div className="apex-target-dropdown-body">
            <div className="apex-dropdown-list apex-target-dropdown-list">
              {groupedTargets.map(group => (
                <div key={group.key}>
                  <div
                    ref={node => { groupRefs.current[group.key] = node; }}
                    className="apex-dropdown-group-label apex-target-group-label"
                  >
                    {group.key}
                  </div>
                  {group.items.map(target => (
                    <div
                      key={target}
                      className={`apex-dropdown-item${selected.includes(target) ? ' selected' : ''}`}
                      onClick={() => toggle(target)}
                    >
                      <span className="check" />
                      <span>{target}</span>
                    </div>
                  ))}
                </div>
              ))}
              {groupedTargets.length === 0 && (
                <div className="apex-dropdown-item" style={{ cursor: 'default' }}>
                  <span style={{ color: 'var(--text-muted)' }}>未找到匹配的靶点</span>
                </div>
              )}
            </div>
            <div className="apex-target-index">
              {TARGET_GROUP_INDEX.map(groupKey => (
                <button
                  key={groupKey}
                  type="button"
                  className={`apex-target-index-item${activeGroupKeys.has(groupKey) ? ' active' : ''}`}
                  onClick={() => activeGroupKeys.has(groupKey) && handleJumpToGroup(groupKey)}
                  disabled={!activeGroupKeys.has(groupKey)}
                >
                  {groupKey}
                </button>
              ))}
            </div>
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
  anchorLeft: number;
  anchorRight: number;
  anchorTop: number;
  anchorBottom: number;
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
  const [showSaveModal, setShowSaveModal] = useState(false);
  const [targetInitToken, setTargetInitToken] = useState(0);

  const [diseaseName, setDiseaseName] = useState('');
  const [pipelinePhases, setPipelinePhases] = useState<string[]>(PIPELINE_PHASES);
  const [targetRows, setTargetRows] = useState<ProgressTargetRow[]>([]);
  const [queried, setQueried] = useState(false);

  const [tooltip, setTooltip] = useState<DrugTooltip | null>(null);
  const [tooltipStyle, setTooltipStyle] = useState<{ left: number; top: number } | null>(null);
  const tooltipTimer = useRef<ReturnType<typeof setTimeout>>();
  const tooltipRef = useRef<HTMLDivElement>(null);

  const clearTooltipTimer = () => {
    clearTimeout(tooltipTimer.current);
  };

  const scheduleTooltipHide = () => {
    clearTooltipTimer();
    tooltipTimer.current = setTimeout(() => {
      setTooltip(null);
      setTooltipStyle(null);
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
  const allDiseases = flattenDiseases(tree);
  const firstDiseaseId = allDiseases[0]?.id ?? null;

  // Targets for selected disease
  const { data: targetsData, isLoading: targetsLoading, isFetching: targetsFetching } = useQuery({
    queryKey: ['progress-targets', selectedDiseaseId],
    queryFn: async () => {
      if (!selectedDiseaseId) return [];
      const res = await getDiseaseTargets(selectedDiseaseId);
      return res.code === 0 ? res.data.map((t: any) => t.target ?? t) : [];
    },
    enabled: !!selectedDiseaseId,
  });
  const availableTargets: string[] = targetsData ?? [];
  const diseaseSelectLoading = treeLoading || treeFetching;
  const targetSelectLoading = (!!selectedDiseaseId && (targetsLoading || targetsFetching)) || diseaseSelectLoading;

  const pendingTargetsRef = useRef<string[] | null>(null);
  const shouldAutoQueryRef = useRef(false);

  const handleDiseaseChange = (id: number, initialTargets: string[] | null = null) => {
    pendingTargetsRef.current = initialTargets;
    shouldAutoQueryRef.current = true;
    setSelectedDiseaseId(id);
    setSelectedTargets([]);
    setQueried(false);
    setTargetRows([]);
    setDiseaseName('');
    setTargetInitToken(token => token + 1);
  };

  useEffect(() => {
    if (!selectedDiseaseId && firstDiseaseId) {
      handleDiseaseChange(firstDiseaseId);
    }
  }, [selectedDiseaseId, firstDiseaseId]);

  useEffect(() => {
    if (!selectedDiseaseId) {
      return;
    }

    if (availableTargets.length === 0) {
      setSelectedTargets([]);
      pendingTargetsRef.current = null;
      return;
    }

    const pendingTargets = pendingTargetsRef.current;
    const nextTargets = pendingTargets === null
      ? [...availableTargets]
      : pendingTargets.filter(target => availableTargets.includes(target));

    setSelectedTargets(nextTargets);
    pendingTargetsRef.current = null;
  }, [selectedDiseaseId, availableTargets, targetInitToken]);

  // Presets
  const { refetch: refetchPresets } = useQuery({
    queryKey: ['presets-progress'],
    queryFn: async () => {
      const res = await getFilterPresets('progress');
      return res.code === 0 ? res.data : [];
    },
  });

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

  useEffect(() => {
    if (!shouldAutoQueryRef.current || !selectedDiseaseId || selectedTargets.length === 0 || viewMutation.isPending) {
      return;
    }

    shouldAutoQueryRef.current = false;
    viewMutation.mutate({ diseaseId: selectedDiseaseId, targets: selectedTargets });
  }, [selectedDiseaseId, selectedTargets, viewMutation]);

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
    pendingTargetsRef.current = null;
    if (firstDiseaseId) {
      handleDiseaseChange(firstDiseaseId);
    } else {
      setSelectedDiseaseId(null);
      setSelectedTargets([]);
      setQueried(false);
      setTargetRows([]);
      setDiseaseName('');
    }
  };

  const handleDrugEnter = useCallback((e: React.MouseEvent, drug: ProgressDrug, target: string, phase: string) => {
    const rect = (e.target as HTMLElement).getBoundingClientRect();
    clearTooltipTimer();
    tooltipTimer.current = setTimeout(() => {
      setTooltipStyle(null);
      setTooltip({
        anchorLeft: rect.left,
        anchorRight: rect.right,
        anchorTop: rect.top,
        anchorBottom: rect.bottom,
        drug,
        target,
        phase,
      });
    }, 150);
  }, []);

  const handleDrugLeave = useCallback(() => {
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
  }, [tooltip]);

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
      {/* <div className="apex-preset-selector">
        <span className="apex-preset-label">当前筛选：</span>
        <select className="apex-select" style={{ minWidth: 160 }} value={currentPreset}
          onChange={e => {
            const p = presets.find(x => x.name === e.target.value);
            if (p) loadPreset(p); else setCurrentPreset(e.target.value);
          }}>
          <option value="系统默认筛选">系统默认筛选</option>
          {presets.map(p => <option key={p.id} value={p.name}>{p.name}</option>)}
        </select>
      </div> */}

      {/* Filter bar */}
      <div className="apex-filter-bar">
        <div className="apex-filter-group">
          <span className="apex-filter-label">疾病：</span>
          <DiseaseSelect tree={tree} selectedId={selectedDiseaseId} loading={diseaseSelectLoading} onChange={handleDiseaseChange} />
        </div>

        <div className="apex-filter-group">
          <span className="apex-filter-label">靶点：</span>
          <TargetSelect options={availableTargets} selected={selectedTargets} loading={targetSelectLoading} onChange={setSelectedTargets} />
        </div>

        <button className="apex-btn apex-btn-primary" onClick={handleQuery} disabled={viewMutation.isPending}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="2">
            <circle cx="11" cy="11" r="8" /><line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          {viewMutation.isPending ? '查询中…' : '查 询'}
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
      {tooltip && (
        <div
          ref={tooltipRef}
          className="apex-tooltip"
          style={{
            left: tooltipStyle?.left ?? TOOLTIP_VIEWPORT_PADDING,
            top: tooltipStyle?.top ?? TOOLTIP_VIEWPORT_PADDING,
            visibility: tooltipStyle ? 'visible' : 'hidden',
          }}
          onMouseEnter={clearTooltipTimer}
          onMouseLeave={scheduleTooltipHide}
        >
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
