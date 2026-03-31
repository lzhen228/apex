import { create } from 'zustand';

/**
 * 视图类型
 */
export type ViewType = 'target-combo' | 'target-progress';

/**
 * 菜单项类型
 */
export interface MenuItem {
  key: string;
  icon: React.ReactNode;
  label: string;
;
}

/**
 * 布局状态接口
 */
interface LayoutState {
  currentView: ViewType;
  sidebarCollapsed: boolean;
  viewTitle: string;
  
  // Actions
  setCurrentView: (view: ViewType) => void;
  toggleSidebar: () => void;
  setSidebarCollapsed: (collapsed: boolean) => void;
  updateViewTitle: (title: string) => void;
}

/**
 * 布局状态 Store
 * 管理侧边栏折叠状态和当前视图信息
 */
export const useLayoutStore = create<LayoutState>((set) => ({
  currentView: 'target-combo',
  sidebarCollapsed: false,
  viewTitle: '靶点组合竞争格局',
  
  setCurrentView: (view) =>
    set({
      currentView: view,
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
}));
