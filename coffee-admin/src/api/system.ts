import request from './request'

export function getMenuList() {
  return request.get('/admin/menus')
}

export function createMenu(data: any) {
  return request.post('/admin/menus', data)
}

export function updateMenu(id: number, data: any) {
  return request.put(`/admin/menus/${id}`, data)
}

export function deleteMenu(id: number) {
  return request.delete(`/admin/menus/${id}`)
}

export function getRoleMenus(role: string) {
  return request.get(`/admin/roles/${role}/menus`)
}

export function updateRoleMenus(role: string, menuIds: number[]) {
  return request.put(`/admin/roles/${role}/menus`, menuIds)
}

export function getOperLogList(params: any) {
  return request.get('/admin/logs/operation', { params })
}
