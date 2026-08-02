<template>
  <div class="sidebar-wrapper">
    <div class="sidebar-logo" :class="{ collapsed: collapsed }">
      <el-icon :size="22" color="#d4a574"><CoffeeCup /></el-icon>
      <span v-if="!collapsed" class="logo-text">Coffee Order</span>
    </div>
    <el-menu
      :default-active="activeMenu"
      :collapse="collapsed"
      :collapse-transition="false"
      background-color="#3e2a1a"
      text-color="rgba(255,255,255,0.75)"
      active-text-color="#ffffff"
      router
      class="sidebar-menu"
    >
      <template v-for="item in menuItems" :key="item.path">
        <el-menu-item v-if="!item.children" :index="resolvePath(item.path)">
          <el-icon>
            <component :is="item.meta.icon" />
          </el-icon>
          <template #title>{{ item.meta.title }}</template>
        </el-menu-item>
        <el-sub-menu v-else :index="item.path" :key="item.path">
          <template #title>
            <el-icon>
              <component :is="item.meta.icon" />
            </el-icon>
            <span>{{ item.meta.title }}</span>
          </template>
          <el-menu-item
            v-for="child in item.children"
            :key="child.path"
            :index="resolvePath(child.path)"
          >
            <el-icon>
              <component :is="child.meta.icon" />
            </el-icon>
            <template #title>{{ child.meta.title }}</template>
          </el-menu-item>
        </el-sub-menu>
      </template>
    </el-menu>
    <div class="sidebar-collapse-btn" @click="$emit('toggle-collapse')">
      <el-icon :size="18">
        <DArrowLeft v-if="!collapsed" />
        <DArrowRight v-else />
      </el-icon>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { CoffeeCup, DArrowLeft, DArrowRight } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

defineProps<{
  collapsed: boolean
}>()

defineEmits<{
  (e: 'toggle-collapse'): void
}>()

const route = useRoute()
const authStore = useAuthStore()

interface MenuMeta {
  title: string
  icon: string
}

interface MenuItem {
  path: string
  meta: MenuMeta
  children?: MenuItem[]
}

const adminMenu: MenuItem[] = [
  {
    path: 'dashboard',
    meta: { title: '数据概览', icon: 'DataLine' }
  },
  {
    path: 'store/workbench',
    meta: { title: '商家工作台', icon: 'Shop' }
  },
  {
    path: 'store',
    meta: { title: '门店管理', icon: 'Shop' }
  },
  {
    path: 'product',
    meta: { title: '商品管理', icon: 'Goods' }
  },
  {
    path: 'order',
    meta: { title: '订单管理', icon: 'List' },
    children: [
      {
        path: 'order/list',
        meta: { title: '订单列表', icon: 'List' }
      }
    ]
  },
  {
    path: 'rider',
    meta: { title: '骑手管理', icon: 'Bicycle' }
  },
  {
    path: 'user',
    meta: { title: '用户管理', icon: 'User' }
  },
  {
    path: 'system',
    meta: { title: '系统管理', icon: 'Setting' },
    children: [
      {
        path: 'system/menu',
        meta: { title: '菜单管理', icon: 'Menu' }
      },
      {
        path: 'system/role',
        meta: { title: '角色权限', icon: 'Lock' }
      },
      {
        path: 'system/log',
        meta: { title: '操作日志', icon: 'Document' }
      }
    ]
  }
]

const storeManagerMenu: MenuItem[] = [
  {
    path: 'store/workbench',
    meta: { title: '商家工作台', icon: 'Shop' }
  }
]

const menuItems = computed(() => (authStore.isAdmin ? adminMenu : storeManagerMenu))

const activeMenu = computed(() => {
  const { path } = route
  if (path.startsWith('/order/detail')) return '/order/list'
  return path
})

function resolvePath(p: string) {
  return '/' + p
}
</script>

<style lang="scss" scoped>
.sidebar-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.sidebar-logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  flex-shrink: 0;
  overflow: hidden;
  &.collapsed {
    justify-content: center;
  }
}
.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #ffffff;
  white-space: nowrap;
  letter-spacing: 0.5px;
}
.sidebar-menu {
  flex: 1;
  border-right: none;
  overflow-y: auto;
  overflow-x: hidden;
  .el-menu-item,
  .el-sub-menu__title {
    &:hover {
      background-color: #4a3420 !important;
    }
  }
  .el-menu-item.is-active {
    background-color: rgba(111, 78, 55, 0.4) !important;
    border-left: 3px solid #d4a574;
  }
}
.sidebar-collapse-btn {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.55);
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  transition: color 0.2s;
  flex-shrink: 0;
  &:hover {
    color: #ffffff;
    background-color: #4a3420;
  }
}
</style>
