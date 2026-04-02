import { useEffect, useMemo, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/stores/authStore';
import { logout as apiLogout } from '@/services/auth';
import { isFeishuClient } from '@/utils/clientEnv';
import request from '@/utils/request';

const PAGE_LABELS: Record<string, string> = {
  '/target-combo': '靶点组合竞争格局',
  '/target-progress': '靶点研发进展格局',
};

export default function Header() {
  const location = useLocation();
  const navigate = useNavigate();
  const { userInfo, logout } = useAuthStore();
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);
  const closeTimerRef = useRef<number | null>(null);
  const feishuClient = useMemo(() => isFeishuClient(), []);
  const menuEnabled = !feishuClient;

  const pageLabel = PAGE_LABELS[location.pathname] ?? '';
  const initials = userInfo?.displayName
    ? userInfo.displayName.slice(-2).toUpperCase()
    : userInfo?.username?.slice(0, 2).toUpperCase() ?? 'HB';

  const [updatedAt, setUpdatedAt] = useState<string | null>(null);
  useEffect(() => {
    request.get<{ code: number; data: { updatedAt: string } }>('/system/last-sync')
      .then(res => { if (res.data?.code === 0) setUpdatedAt(res.data.data.updatedAt); })
      .catch(() => { });
  }, []);

  useEffect(() => {
    if (!menuOpen) {
      return;
    }

    const handlePointerDown = (event: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setMenuOpen(false);
      }
    };

    document.addEventListener('mousedown', handlePointerDown);
    return () => document.removeEventListener('mousedown', handlePointerDown);
  }, [menuOpen]);

  useEffect(() => {
    return () => {
      if (closeTimerRef.current !== null) {
        window.clearTimeout(closeTimerRef.current);
      }
    };
  }, []);

  const openMenu = () => {
    if (!menuEnabled) {
      return;
    }

    if (closeTimerRef.current !== null) {
      window.clearTimeout(closeTimerRef.current);
      closeTimerRef.current = null;
    }
    setMenuOpen(true);
  };

  const scheduleCloseMenu = () => {
    if (!menuEnabled) {
      return;
    }

    if (closeTimerRef.current !== null) {
      window.clearTimeout(closeTimerRef.current);
    }
    closeTimerRef.current = window.setTimeout(() => {
      setMenuOpen(false);
      closeTimerRef.current = null;
    }, 180);
  };

  const handleLogout = async () => {
    try { await apiLogout(); } catch { /* ignore */ }
    setMenuOpen(false);
    logout();
    localStorage.removeItem('token');
    navigate('/login');
  };

  return (
    <header className="apex-header">
      <div className="apex-logo">
        <div className="apex-logo-mark">HB</div>
        <span className="apex-logo-text">Apex早期靶点情报分析智能体</span>
      </div>

      {pageLabel && (
        <div className="apex-breadcrumb">
          <span className="sep">/</span>
          <span className="active">{pageLabel}</span>
        </div>
      )}

      <div className="apex-header-right">
        {/* Home icon */}
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
          stroke="var(--text-muted)" strokeWidth="1.8" style={{ cursor: 'pointer' }}
          onClick={() => navigate('/target-combo')}>
          <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z" />
          <polyline points="9 22 9 12 15 12 15 22" />
        </svg>

        {updatedAt && (
          <span className="apex-header-sync-time">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" style={{ flexShrink: 0 }}>
              <ellipse cx="12" cy="12" rx="10" ry="10" />
              <line x1="12" y1="6" x2="12" y2="12" /><line x1="12" y1="12" x2="16" y2="14" />
            </svg>
            数据更新至: {updatedAt}
          </span>
        )}

        <span className="apex-header-user">
          {userInfo?.displayName || userInfo?.username || '用户'}
        </span>

        <div
          ref={menuRef}
          className="apex-user-menu"
          onMouseEnter={menuEnabled ? openMenu : undefined}
          onMouseLeave={menuEnabled ? scheduleCloseMenu : undefined}
        >
          <div
            className="apex-avatar"
            title={menuEnabled ? '用户菜单' : undefined}
            style={{ cursor: menuEnabled ? 'pointer' : 'default' }}
            onClick={() => {
              if (!menuEnabled) {
                return;
              }

              if (menuOpen) {
                scheduleCloseMenu();
                return;
              }
              openMenu();
            }}
          >
            {initials}
          </div>

          {menuEnabled && menuOpen && (
            <div className="apex-user-dropdown" onMouseEnter={openMenu}>
              <button type="button" className="apex-user-dropdown-item" onClick={handleLogout}>
                退出登录
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
