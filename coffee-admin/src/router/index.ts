import { createRouter, createWebHashHistory, RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { hidden: true }
  },
  {
    path: '/',
    component: () => import('@/layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
        meta: { title: '数据概览', icon: 'DataLine', adminOnly: true }
      },
      {
        path: 'store',
        name: 'StoreList',
        component: () => import('@/views/store/StoreList.vue'),
        meta: { title: '门店管理', icon: 'Shop', adminOnly: true }
      },
      {
        path: 'store/workbench',
        name: 'StoreWorkbench',
        component: () => import('@/views/store/StoreWorkbench.vue'),
        meta: { title: '商家工作台', icon: 'Shop' }
      },
      {
        path: 'product',
        name: 'ProductList',
        component: () => import('@/views/product/ProductList.vue'),
        meta: { title: '商品管理', icon: 'Goods', adminOnly: true }
      },
      {
        path: 'order/list',
        name: 'OrderList',
        component: () => import('@/views/order/OrderList.vue'),
        meta: { title: '订单管理', icon: 'List', adminOnly: true }
      },
      {
        path: 'order/detail/:id',
        name: 'OrderDetail',
        component: () => import('@/views/order/OrderDetail.vue'),
        meta: { title: '订单详情', icon: 'List', hidden: true, adminOnly: true }
      },
      {
        path: 'rider',
        name: 'RiderList',
        component: () => import('@/views/rider/RiderList.vue'),
        meta: { title: '骑手管理', icon: 'Bicycle', adminOnly: true }
      },
      {
        path: 'user',
        name: 'UserList',
        component: () => import('@/views/user/UserList.vue'),
        meta: { title: '用户管理', icon: 'User', adminOnly: true }
      },
      {
        path: 'system/menu',
        name: 'MenuManage',
        component: () => import('@/views/system/MenuManage.vue'),
        meta: { title: '菜单管理', icon: 'Menu', adminOnly: true }
      },
      {
        path: 'system/role',
        name: 'RoleMenu',
        component: () => import('@/views/system/RoleMenu.vue'),
        meta: { title: '角色权限', icon: 'Lock', adminOnly: true }
      },
      {
        path: 'system/log',
        name: 'OperLog',
        component: () => import('@/views/system/OperLog.vue'),
        meta: { title: '操作日志', icon: 'Document', adminOnly: true }
      }
    ]
  },
  {
    path: '/403',
    name: '403',
    component: () => import('@/views/error/403.vue'),
    meta: { hidden: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: '404',
    component: () => import('@/views/error/404.vue'),
    meta: { hidden: true }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach(async (to, _from, next) => {
  if (to.path === '/login') {
    next()
    return
  }

  const authStore = useAuthStore()

  if (!authStore.token) {
    next('/login')
    return
  }

  if (!authStore.userInfo) {
    try {
      await authStore.fetchUserInfo()
    } catch {
      authStore.clearAuth()
      next('/login')
      return
    }
  }

  const role = authStore.userInfo?.role

  // 平台管理员：可访问所有后台页面
  if (role === 'ADMIN') {
    next()
    return
  }

  // 商家：仅可访问商家工作台
  if (role === 'STORE_MANAGER') {
    if (to.path === '/store/workbench') {
      next()
    } else {
      next('/store/workbench')
    }
    return
  }

  // 其他角色（用户/骑手）无权使用后台
  authStore.clearAuth()
  next('/login')
})

export default router
