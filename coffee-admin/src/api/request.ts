import axios, { AxiosError, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

const CODE_SUCCESS = 200
const CODE_UNAUTHORIZED = 401
const CODE_FORBIDDEN = 403

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

let isRefreshing = false
const pendingRequests: Array<(token: string) => void> = []

// 请求拦截器：从 sessionStorage 获取 token 并添加到请求头
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = sessionStorage.getItem('token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (res.code !== CODE_SUCCESS) {
      ElMessage.error(res.msg || 'Request failed')
      if (res.code === CODE_UNAUTHORIZED) {
        handleAuthError()
      }
      return Promise.reject(new Error(res.msg || 'Request failed'))
    }
    return res.data !== undefined ? res.data : res
  },
  async (error: AxiosError) => {
    if (error.response?.status === CODE_UNAUTHORIZED) {
      return handleAuthError(error)
    }
    if (error.response?.status === CODE_FORBIDDEN) {
      window.location.href = '/#/403'
      return Promise.reject(error)
    }
    ElMessage.error(error.message || 'Network error')
    return Promise.reject(error)
  }
)

function handleAuthError(originalError?: AxiosError): Promise<any> {
  const refreshToken = sessionStorage.getItem('refreshToken')
  if (!refreshToken) {
    redirectToLogin()
    return Promise.reject(originalError || new Error('No refresh token'))
  }

  if (!isRefreshing) {
    isRefreshing = true
    return axios.post('/api/auth/refresh', { refreshToken })
      .then((res) => {
        const data = res.data?.data || res.data
        const newToken = data?.token || data?.accessToken
        const newRefreshToken = data?.refreshToken
        if (newToken) {
          sessionStorage.setItem('token', newToken)
          if (newRefreshToken) {
            sessionStorage.setItem('refreshToken', newRefreshToken)
          }
          // 重新计算过期时间（30分钟）
          const EXPIRATION = 30 * 60 * 1000
          sessionStorage.setItem('expiresAt', String(Date.now() + EXPIRATION))
          
          pendingRequests.forEach(cb => cb(newToken))
          pendingRequests.length = 0
          if (originalError?.config) {
            originalError.config.headers.Authorization = `Bearer ${newToken}`
            return request(originalError.config)
          }
        }
        return Promise.resolve(data)
      })
      .catch(() => {
        redirectToLogin()
        pendingRequests.length = 0
        return Promise.reject(new Error('Token refresh failed'))
      })
      .finally(() => {
        isRefreshing = false
      })
  } else {
    return new Promise((resolve) => {
      pendingRequests.push((token: string) => {
        if (originalError?.config) {
          originalError.config.headers.Authorization = `Bearer ${token}`
          resolve(request(originalError.config))
        } else {
          resolve(token)
        }
      })
    })
  }
}

function redirectToLogin() {
  sessionStorage.removeItem('token')
  sessionStorage.removeItem('refreshToken')
  sessionStorage.removeItem('expiresAt')
  window.location.href = '/#/login'
}

export default request