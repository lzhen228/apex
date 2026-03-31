import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios';
import type { Result } from '@/types';

const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
});

// Request interceptor — attach Bearer token
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => Promise.reject(error)
);

// Response interceptor — handle HTTP + business errors
request.interceptors.response.use(
  (response) => {
    // For blob responses (export), pass through
    if (response.config.responseType === 'blob') return response;

    const data = response.data as Result;
    // Business error (code != 0)
    if (data && typeof data.code === 'number' && data.code !== 0) {
      console.warn('Business error:', data.code, data.message);
      // Don't show toast here — let callers handle it
      return Promise.reject(new Error(data.message || '请求失败'));
    }
    return response;
  },
  (error: AxiosError<Result>) => {
    const { response } = error;
    if (response) {
      const status = response.status;
      if (status === 401) {
        localStorage.removeItem('token');
        window.location.href = '/login';
      }
      return Promise.reject(new Error(response.data?.message || `HTTP ${status}`));
    }
    return Promise.reject(error);
  }
);

export default request;
