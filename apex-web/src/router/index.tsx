import { lazy, Suspense } from 'react';
import { createBrowserRouter, Navigate } from 'react-router-dom';
import MainLayout from '@/components/layout/MainLayout';
import LazyErrorBoundary from '@/components/LazyErrorBoundary';

const TargetCombo    = lazy(() => import('@/views/TargetCombo/index'));
const TargetProgress = lazy(() => import('@/views/TargetProgress/index'));
const Login          = lazy(() => import('@/views/Login/index'));
const NotFound       = lazy(() => import('@/views/NotFound/index'));

const Wrap = ({ C }: { C: React.ComponentType }) => (
  <LazyErrorBoundary>
    <Suspense fallback={<div className="apex-loading"><div className="apex-spinner" /></div>}>
      <C />
    </Suspense>
  </LazyErrorBoundary>
);

const router = createBrowserRouter([
  { path: '/login', element: <Wrap C={Login} /> },
  {
    path: '/',
    element: <MainLayout />,
    children: [
      { index: true,            element: <Navigate to="/target-combo" replace /> },
      { path: 'target-combo',   element: <Wrap C={TargetCombo} /> },
      { path: 'target-progress',element: <Wrap C={TargetProgress} /> },
    ],
  },
  { path: '*', element: <Wrap C={NotFound} /> },
]);

export default router;
