<template>
  <div class="page-container">
    <h2 class="page-heading">菜单管理</h2>

    <div class="search-bar">
      <el-button type="primary" :icon="Plus" @click="openAdd(0)">新增根菜单</el-button>
    </div>

    <div class="coffee-card" style="padding: 0">
      <el-table
        :data="tableData"
        v-loading="loading"
        row-key="id"
        border
        stripe
        :tree-props="{ children: 'children' }"
        default-expand-all
      >
        <el-table-column prop="title" label="菜单标题" min-width="180" />
        <el-table-column prop="name" label="名称" min-width="140" />
        <el-table-column prop="path" label="路径" min-width="160" />
        <el-table-column prop="icon" label="图标" width="100">
          <template #default="{ row }">
            <el-icon v-if="row.icon"><component :is="row.icon" /></el-icon>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="menuTypeTags[row.menuType]" size="small">
              {{ menuTypeLabels[row.menuType] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" :icon="Plus" @click="openAdd(row.id)">
              新增
            </el-button>
            <el-button type="primary" link size="small" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑菜单' : '新增菜单'"
      width="560px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="父级菜单" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="treeData"
            :props="{ label: 'title', value: 'id', children: 'children' }"
            placeholder="选择父级菜单（空为根菜单）"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单标题" prop="title">
          <el-input v-model="form.title" placeholder="显示标题" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="路由名称" />
        </el-form-item>
        <el-form-item label="路径">
          <el-input v-model="form.path" placeholder="路由路径" />
        </el-form-item>
        <el-form-item label="组件">
          <el-input v-model="form.component" placeholder="Vue 组件路径" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="Element Plus 图标名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="form.menuType">
            <el-radio :value="1">目录</el-radio>
            <el-radio :value="2">菜单</el-radio>
            <el-radio :value="3">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="权限标识">
          <el-input v-model="form.perms" placeholder="例如 sys:user:list" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-color="#6f4e37" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getMenuList, createMenu, updateMenu, deleteMenu } from '@/api/system'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const tableData = ref<any[]>([])
const treeData = ref<any[]>([])
const formRef = ref()

const menuTypeLabels: Record<number, string> = { 1: '目录', 2: '菜单', 3: '按钮' }
const menuTypeTags: Record<number, string> = { 1: 'primary', 2: 'success', 3: 'warning' }

const form = reactive({
  parentId: 0 as number | undefined,
  title: '',
  name: '',
  path: '',
  component: '',
  icon: '',
  sortOrder: 0,
  menuType: 2,
  perms: '',
  status: 1
})

const rules = {
  title: [{ required: true, message: '请输入菜单标题', trigger: 'blur' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

async function fetchList() {
  loading.value = true
  try {
    const res: any = await getMenuList()
    const data = res?.data || res || []
    tableData.value = data
    treeData.value = data
  } catch { /* handled */ } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(form, {
    parentId: 0,
    title: '',
    name: '',
    path: '',
    component: '',
    icon: '',
    sortOrder: 0,
    menuType: 2,
    perms: '',
    status: 1
  })
  editId.value = null
}

function openAdd(parentId?: number) {
  isEdit.value = false
  resetForm()
  form.parentId = parentId || 0
  dialogVisible.value = true
}

function openEdit(row: any) {
  isEdit.value = true
  editId.value = row.id
  Object.assign(form, {
    parentId: row.parentId || 0,
    title: row.title || '',
    name: row.name || '',
    path: row.path || '',
    component: row.component || '',
    icon: row.icon || '',
    sortOrder: row.sortOrder || 0,
    menuType: row.menuType ?? 2,
    perms: row.perms || '',
    status: row.status ?? 1
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const data: any = {
      parentId: form.parentId || 0,
      title: form.title,
      name: form.name,
      path: form.path,
      component: form.component,
      icon: form.icon,
      sortOrder: form.sortOrder,
      menuType: form.menuType,
      perms: form.perms,
      status: form.status
    }
    if (isEdit.value && editId.value) {
      await updateMenu(editId.value, data)
      ElMessage.success('菜单更新成功')
    } else {
      await createMenu(data)
      ElMessage.success('菜单创建成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch { /* handled */ } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定要删除菜单"${row.title}"吗？`, '确认删除', { type: 'warning' })
    await deleteMenu(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch { /* cancelled */ }
}

onMounted(() => { fetchList() })
</script>
