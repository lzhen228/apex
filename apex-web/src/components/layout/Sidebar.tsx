import { useLocation, useNavigate } from 'react-router-dom';

const NAV_ITEMS = [
  {
    key: '/target-combo',
    label: '靶点组合',
    icon: (
      <svg className="nav-icon" viewBox="0 0 24 24">
        <rect x="3" y="3" width="7" height="7" rx="1.5" />
        <rect x="14" y="3" width="7" height="7" rx="1.5" />
        <rect x="3" y="14" width="7" height="7" rx="1.5" />
        <rect x="14" y="14" width="7" height="7" rx="1.5" />
      </svg>
    ),
  },
  {
    key: '/target-progress',
    label: '靶点进展',
    icon: (
      <svg className="nav-icon" viewBox="0 0 24 24">
        <circle cx="12" cy="12" r="9" />
        <path d="M12 3v9l6 3" />
      </svg>
    ),
  },
];

export default function Sidebar({ open, onNavClick }: { open?: boolean; onNavClick?: () => void }) {
  const location = useLocation();
  const navigate = useNavigate();

  return (
    <nav className={`apex-sidebar${open ? ' open' : ''}`}>
      {NAV_ITEMS.map((item) => (
        <div
          key={item.key}
          className={`apex-nav-item${location.pathname === item.key ? ' active' : ''}`}
          onClick={() => {
            navigate(item.key);
            onNavClick?.();
          }}
        >
          {item.icon}
          <span className="nav-label">{item.label}</span>
        </div>
      ))}
    </nav>
  );
}
