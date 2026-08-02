import request from './request'

export function getDashboardData() {
  return request.get('/admin/dashboard')
}
