<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item to="/dashboard">
      <el-icon><HomeFilled /></el-icon>
    </el-breadcrumb-item>
    <el-breadcrumb-item
      v-for="item in breadcrumbList"
      :key="item.path"
      :to="item.path"
    >
      {{ item.title }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { HomeFilled } from '@element-plus/icons-vue'

const route = useRoute()

const breadcrumbList = computed(() => {
  return route.matched
    .filter((r) => r.meta && r.meta.title)
    .map((r) => ({
      title: r.meta.title as string,
      path: r.path
    }))
})
</script>

<style lang="scss" scoped>
.el-breadcrumb {
  :deep(.el-breadcrumb__inner) {
    color: #909399;
    font-weight: 400;
    &.is-link {
      color: var(--color-primary);
    }
  }
}
</style>
