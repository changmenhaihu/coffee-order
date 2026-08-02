<template>
  <div class="error-page">
    <el-icon :size="80" color="#f56c6c"><Lock /></el-icon>
    <h1 class="error-code">403</h1>
    <p class="error-msg">无权限访问</p>
    <p class="error-desc">您没有权限访问此页面。</p>
    <el-button type="primary" @click="$router.push('/')" round>
      返回首页
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { Lock } from '@element-plus/icons-vue'
</script>

<style lang="scss" scoped>
.error-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: var(--bg-page);
}
.error-code {
  font-size: 72px;
  font-weight: 800;
  color: #3e2a1a;
  margin: 16px 0 8px;
}
.error-msg {
  font-size: 20px;
  color: #606266;
  margin: 0 0 8px;
}
.error-desc {
  font-size: 14px;
  color: #909399;
  margin: 0 0 32px;
}
</style>
