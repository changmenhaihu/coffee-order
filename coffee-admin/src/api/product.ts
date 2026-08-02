import request from './request'

export function getProductList(params: any) {
  return request.get('/admin/products', { params })
}

export function createProduct(data: any) {
  return request.post('/admin/products', data)
}

export function updateProduct(id: number, data: any) {
  return request.put(`/admin/products/${id}`, data)
}

export function deleteProduct(id: number) {
  return request.delete(`/admin/products/${id}`)
}

export function getCategories(storeId: number) {
  return request.get('/product/categories', { params: { storeId } })
}
