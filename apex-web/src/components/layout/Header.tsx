import { useEffect, useMemo, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/stores/authStore';
import { logout as apiLogout } from '@/services/auth';
import { isFeishuClient } from '@/utils/clientEnv';
import request from '@/utils/request';

type ThemeMode = 'dark' | 'light';

const PAGE_LABELS: Record<string, string> = {
  '/target-combo': '靶点组合竞争格局',
  '/target-progress': '靶点研发进展格局',
};

export default function Header() {
  const location = useLocation();
  const navigate = useNavigate();
  const { userInfo, logout } = useAuthStore();
  const [menuOpen, setMenuOpen] = useState(false);
  const [theme, setTheme] = useState<ThemeMode>(() => {
    const savedTheme = localStorage.getItem('apex-theme');
    return savedTheme === 'dark' ? 'dark' : 'light';
  });
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
    document.documentElement.dataset.theme = theme;
    document.documentElement.style.colorScheme = theme;
    localStorage.setItem('apex-theme', theme);
  }, [theme]);

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

  const toggleTheme = () => {
    setTheme(prev => prev === 'dark' ? 'light' : 'dark');
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

        <button
          type="button"
          className="apex-theme-toggle"
          onClick={toggleTheme}
          title={theme === 'dark' ? '切换到浅色主题' : '切换到暗黑主题'}
          aria-label={theme === 'dark' ? '切换到浅色主题' : '切换到暗黑主题'}
        >
          {theme === 'dark' ? (
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
              <circle cx="12" cy="12" r="4" />
              <path d="M12 2v2.5M12 19.5V22M4.93 4.93l1.77 1.77M17.3 17.3l1.77 1.77M2 12h2.5M19.5 12H22M4.93 19.07l1.77-1.77M17.3 6.7l1.77-1.77" />
            </svg>
          ) : (
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
              <path d="M21 12.79A9 9 0 1111.21 3c0 .18-.01.36-.01.54A7.5 7.5 0 0018.46 13.8c.18 0 .36-.01.54-.01z" />
            </svg>
          )}
          <span>{theme === 'dark' ? '浅色' : '暗黑'}</span>
        </button>

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
