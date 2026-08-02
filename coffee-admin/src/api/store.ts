import request from './request'

// ---- 平台管理员 ----
export function getStoreList(params: any) {
  return request.get('/admin/stores', { params })
}

export function getAllStores() {
  return request.get('/admin/stores', { params: { page: 1, size: 1000 } })
}

export function createStore(data: any) {
  return request.post('/admin/stores', data)
}

export function updateStore(id: number, data: any) {
  return request.put(`/admin/stores/${id}`, data)
}

export function deleteStore(id: number) {
  return request.delete(`/admin/stores/${id}`)
}

// ---- 商家端（STORE_MANAGER / ADMIN）----
export function getStoreOrders(params: any) {
  return request.get('/store/orders', { params })
}

export function acceptStoreOrder(orderId: number) {
  return request.put(`/store/orders/${orderId}/accept`)
}

export function completeStoreOrder(orderId: number) {
  return request.put(`/store/orders/${orderId}/complete`)
}

export function getStoreProducts(params?: any) {
  return request.get('/store/products', { params })
}

export function updateStoreProduct(productId: number, data: any) {
  return request.put(`/store/products/${productId}`, data)
}
