import request from './request'

export function getUserList(params: any) {
  return request.get('/admin/users', { params })
}

export function getUserById(id: number) {
  return request.get(`/admin/users/${id}`)
}

export function updateUser(id: number, data: any) {
  return request.put(`/admin/users/${id}`, data)
}

export function rechargeUser(id: number, data: { amount: number }) {
  return request.post(`/admin/users/${id}/recharge`, data)
}
