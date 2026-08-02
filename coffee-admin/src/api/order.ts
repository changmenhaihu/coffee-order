import request from './request'

export function getOrderList(params: any) {
  return request.get('/admin/orders', { params })
}

export function getOrderById(id: number) {
  return request.get(`/admin/orders/${id}`)
}

export function assignRider(orderId: number, riderId: number) {
  return request.post(`/admin/orders/${orderId}/assign`, { riderId })
}
