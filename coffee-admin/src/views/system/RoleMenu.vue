<template>
  <div class="page-container">
    <h2 class="page-heading">角色菜单分配</h2>

    <div class="coffee-card" style="padding: 20px 24px; margin-bottom: 16px;">
      <el-tabs v-model="activeRole" @tab-change="onRoleChange">
        <el-tab-pane label="平台管理员" name="ADMIN" />
        <el-tab-pane label="商家" name="STORE_MANAGER" />
        <el-tab-pane label="骑手" name="RIDER" />
        <el-tab-pane label="用户" name="USER" />
      </el-tabs>
    </div>

    <div class="coffee-card" style="padding: 20px 24px">
      <div v-loading="loading">
        <el-tree
          ref="treeRef"
          :data="menuTree"
          show-checkbox
          node-key="id"
          :default-checked-keys="checkedKeys"
          :props="{ label: 'title', children: 'children' }"
          default-expand-all
          highlight-current
        />
      </div>
      <div style="margin-top: 20px; display: flex; justify-content: center;">
        <el-button type="primary" :loading="saveLoading" @click="handleSave" :icon="Check">
          保存菜单权限
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import { getMenuList, getRoleMenus, updateRoleMenus } from '@/api/system'

const activeRole = ref('ADMIN')
const loading = ref(false)
const saveLoading = ref(false)
const menuTree = ref<any[]>([])
const checkedKeys = ref<number[]>([])
const treeRef = ref()

async function fetchMenus() {
  loading.value = true
  try {
    const res: any = await getMenuList()
    menuTree.value = (res?.data || res || [])
  } catch { /* handled */ } finally {
    loading.value = false
  }
}

async function fetchRoleMenus() {
  loading.value = true
  try {
    const res: any = await getRoleMenus(activeRole.value)
    const data = res?.data || res
    checkedKeys.value = Array.isArray(data) ? data : (data?.menuIds || [])
  } catch {
    checkedKeys.value = []
  } finally {
    loading.value = false
  }
}

function onRoleChange() {
  fetchRoleMenus()
}

async function handleSave() {
  saveLoading.value = true
  try {
    const keys = treeRef.value?.getCheckedKeys() || []
    const halfKeys = treeRef.value?.getHalfCheckedKeys() || []
    await updateRoleMenus(activeRole.value, [...keys, ...halfKeys])
    ElMessage.success('角色权限保存成功')
  } catch { /* handled */ } finally {
    saveLoading.value = false
  }
}

onMounted(() => {
  fetchMenus()
  fetchRoleMenus()
})
</script>
