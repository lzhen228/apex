import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { UserInfo } from '@/types';

interface AuthState {
  token: string | null;
  userInfo: UserInfo | null;
  setToken: (token: string) => void;
  login: (token: string, userInfo: UserInfo) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      userInfo: null,

      setToken: (token) => set({ token }),

      login: (token, userInfo) => set({ token, userInfo }),

      logout: () => set({ token: null, userInfo: null }),
    }),
    {
      name: 'apex-auth',
      partialize: (state) => ({ token: state.token, userInfo: state.userInfo }),
    }
  )
);
