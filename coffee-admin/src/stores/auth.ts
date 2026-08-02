import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getUserInfo } from '@/api/auth'

export interface UserInfoResp {
  id: number
  username: string
  nickname: string
  avatar: string
  email: string
  phone: string
  role: string
  storeId: number | null
  balance: number
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(sessionStorage.getItem('token') || '')
  const refreshToken = ref<string>(sessionStorage.getItem('refreshToken') || '')
  const userInfo = ref<UserInfoResp | null>(null)
  const expiresAt = ref<number>(Number(sessionStorage.getItem('expiresAt')) || 0)

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')
  const isStoreManager = computed(() => userInfo.value?.role === 'STORE_MANAGER')
  const username = computed(() => userInfo.value?.username || '')
  const avatar = computed(() => userInfo.value?.avatar || '')

  function isTokenExpired(): boolean {
    if (!expiresAt.value) return false
    return Date.now() > expiresAt.value
  }

  if (token.value && isTokenExpired()) {
    clearAuth()
  }

  async function login(usernameVal: string, password: string) {
    const data: any = await loginApi({ username: usernameVal, password })
    token.value = data.token || data.accessToken
    refreshToken.value = data.refreshToken

    const EXPIRATION = 30 * 60 * 1000
    expiresAt.value = Date.now() + EXPIRATION

    sessionStorage.setItem('token', token.value)
    sessionStorage.setItem('refreshToken', refreshToken.value)
    sessionStorage.setItem('expiresAt', String(expiresAt.value))

    // 登录接口已返回 user 信息（含 role/storeId），直接使用，无需二次请求
    if (data.user) {
      userInfo.value = data.user
    } else {
      await fetchUserInfo()
    }
  }

  async function fetchUserInfo() {
    try {
      const data: any = await getUserInfo()
      userInfo.value = data || null
    } catch {
      // 获取失败时不清除 token，可能是网络问题，由路由守卫处理
    }
  }

  function logout() {
    clearAuth()
  }

  function clearAuth() {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    expiresAt.value = 0
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('refreshToken')
    sessionStorage.removeItem('expiresAt')
  }

  return {
    token,
    refreshToken,
    userInfo,
    expiresAt,
    isLoggedIn,
    isAdmin,
    isStoreManager,
    username,
    avatar,
    login,
    logout,
    fetchUserInfo,
    clearAuth,
    isTokenExpired
  }
})
