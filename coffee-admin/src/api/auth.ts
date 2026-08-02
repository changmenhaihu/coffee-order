import request from './request'

export interface LoginParams {
  username: string
  password: string
}

export function login(data: LoginParams) {
  return request.post('/auth/login', data)
}

export function getUserInfo() {
  return request.get('/auth/info')
}

