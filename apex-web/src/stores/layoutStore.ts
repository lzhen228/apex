import { create } from 'zustand';
import type { ViewType } from '@/types';

/**
 * 布局状态接口
 */
interface LayoutState {
  currentView: ViewType;
  sidebarCollapsed: boolean;
  mobileDrawerVisible: boolean;
  viewTitle: string;
  
  // Actions
  setCurrentView: (view: ViewType) => void;
  toggleSidebar: () => void;
  setSidebarCollapsed: (collapsed: boolean) => void;
  updateViewTitle: (title: string) => void;
  toggleMobileDrawer: () => void;
  setMobileDrawerVisible: (visible: boolean) => void;
}

/**
 * 布局状态 Store
 * 管理侧边栏折叠状态和当前视图信息
 */
export const useLayoutStore = create<LayoutState>((set) => ({
  currentView: 'target-combo',
  sidebarCollapsed: false,
  mobileDrawerVisible: false,
  viewTitle: '靶点组合竞争格局',
  
  setCurrentView: (view) =>
    set({
      currentView: view,
      mobileDrawerVisible: false,
      viewTitle: view === 'target-combo' ? '靶点组合竞争格局' : '靶点研发进展格局',
    }),
  
  toggleSidebar: () =>
    set((state) => ({
      sidebarCollapsed: !state.sidebarCollapsed,
    })),
  
  setSidebarCollapsed: (collapsed) =>
    set({
      sidebarCollapsed: collapsed,
    }),
  
  updateViewTitle: (title) =>
    set({
      viewTitle: title,
    }),
  
  toggleMobileDrawer: () =>
    set((state) => ({
      mobileDrawerVisible: !state.mobileDrawerVisible,
    })),
  
  setMobileDrawerVisible: (visible) =>
    set({
      mobileDrawerVisible: visible,
    }),
}));
