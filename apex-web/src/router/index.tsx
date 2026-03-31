import { lazy, Suspense } from 'react';
import { createBrowserRouter, Navigate } from 'react-router-dom';
import MainLayout from '@/components/layout/MainLayout';

const TargetCombo    = lazy(() => import('@/views/TargetCombo/index'));
const TargetProgress = lazy(() => import('@/views/TargetProgress/index'));
const Login          = lazy(() => import('@/views/Login/index'));
const NotFound       = lazy(() => import('@/views/NotFound/index'));

const Wrap = ({ C }: { C: React.ComponentType }) => (
  <Suspense fallback={<div className="apex-loading"><div className="apex-spinner" /></div>}>
    <C />
  </Suspense>
);

const router = createBrowserRouter([
  {
    path: '/',
    element: <Navigate to="/target-combo" replace />,
  },
  {
    path: '/',
    element: <MainLayout />,
    children: [
      { path: 'target-combo',    element: <Wrap C={TargetCombo} /> },
      { path: 'target-progress', element: <Wrap C={TargetProgress} /> },
    ],
  },
  { path: '/login', element: <Wrap C={Login} /> },
  { path: '*',      element: <Wrap C={NotFound} /> },
]);

export default router;
