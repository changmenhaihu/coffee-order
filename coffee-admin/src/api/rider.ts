import request from './request'

export function getRiderList(params: any) {
  return request.get('/admin/riders', { params })
}

export function getRiderById(id: number) {
  return request.get(`/admin/riders/${id}`)
}

export function createRider(data: any) {
  return request.post('/admin/riders', data)
}

export function updateRider(id: number, data: any) {
  return request.put(`/admin/riders/${id}`, data)
}

export function deleteRider(id: number) {
  return request.delete(`/admin/riders/${id}`)
}

export function getRiderTrack(id: number) {
  return request.get(`/admin/riders/${id}/track`)
}
