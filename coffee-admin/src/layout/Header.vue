<template>
  <div class="header-inner">
    <div class="header-left">
      <div class="hamburger" @click="$emit('toggle-sidebar')">
        <el-icon :size="20">
          <Fold v-if="!collapsed" />
          <Expand v-else />
        </el-icon>
      </div>
      <Breadcrumb />
    </div>
    <div class="header-right">
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-dropdown-trigger">
          <el-avatar :size="32" :src="avatar" icon="UserFilled" />
          <span class="username">{{ username || '管理员' }}</span>
          <el-icon :size="14" color="#909399"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon> 个人资料
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon> 退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { Fold, Expand, ArrowDown, User, SwitchButton } from '@element-plus/icons-vue'
import Breadcrumb from './Breadcrumb.vue'

defineProps<{
  collapsed: boolean
}>()

defineEmits<{
  (e: 'toggle-sidebar'): void
}>()

const authStore = useAuthStore()
const router = useRouter()

const username = computed(() => authStore.username)
const avatar = computed(() => authStore.avatar)

function handleCommand(cmd: string) {
  if (cmd === 'logout') {
    authStore.logout()
    router.push('/login')
  }
}
</script>

<style lang="scss" scoped>
.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  height: 100%;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.header-right {
  display: flex;
  align-items: center;
}
.hamburger {
  cursor: pointer;
  color: #606266;
  padding: 4px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  &:hover {
    background-color: #f5f0e8;
    color: var(--color-primary);
  }
}
.user-dropdown-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.2s;
  &:hover {
    background-color: #f5f0e8;
  }
  .username {
    font-size: 14px;
    color: #303133;
  }
}
</style>
