import request from '@/utils/request'
import type { LoginRequest, LoginResponse, Result } from '@/types'

export async function login(data: LoginRequest): Promise<Result<LoginResponse>> {
  const response = await request.post<Result<LoginResponse>>('/auth/login', data)
  return response.data
}

export async function logout(): Promise<Result<null>> {
  const response = await request.post<Result<null>>('/auth/logout')
  return response.data
}

export async function loginWithFeishuCode(code: string): Promise<Result<LoginResponse>> {
  const response = await request.post<Result<LoginResponse>>('/auth/feishu/login', { code })
  return response.data
}
