import { Outlet, Navigate } from 'react-router-dom';
import Header from './Header';
import Sidebar from './Sidebar';
import { useAuthStore } from '@/stores/authStore';

export default function MainLayout() {
  const { token } = useAuthStore();
  if (!token) return <Navigate to="/login" replace />;

  return (
    <>
      <Header />
      <Sidebar />
      <main className="apex-main">
        <Outlet />
      </main>

      {/* AI FAB */}
      <button className="apex-fab" title="AI 助手">
        <svg viewBox="0 0 24 24" fill="none" strokeWidth="1.8">
          <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" />
        </svg>
      </button>
    </>
  );
}
