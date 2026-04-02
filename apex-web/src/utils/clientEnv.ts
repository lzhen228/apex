export function isFeishuClient() {
  if (typeof window === 'undefined') {
    return false
  }

  const userAgent = window.navigator.userAgent.toLowerCase()
  return /feishu|lark/.test(userAgent)
}
