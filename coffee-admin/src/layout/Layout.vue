<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapsed ? '64px' : '220px'" class="layout-aside">
      <Sidebar :collapsed="isCollapsed" />
    </el-aside>
    <el-container>
      <el-header height="56px" class="layout-header">
        <Header @toggle-sidebar="toggleSidebar" :collapsed="isCollapsed" />
      </el-header>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
const isCollapsed = computed(() => appStore.sidebarCollapsed)

function toggleSidebar() {
  appStore.toggleSidebar()
}
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
}
.layout-aside {
  background-color: var(--bg-sidebar);
  transition: width 0.3s ease;
  overflow: hidden;
}
.layout-header {
  padding: 0 20px;
  display: flex;
  align-items: center;
  background: #fff;
  box-shadow: var(--shadow-header);
  z-index: 10;
}
.layout-main {
  background-color: var(--bg-page);
  padding: 20px;
  overflow-y: auto;
}
</style>
