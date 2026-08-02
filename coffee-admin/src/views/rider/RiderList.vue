<template>
  <div class="page-container">
    <h2 class="page-heading">骑手管理</h2>

    <div class="search-bar">
      <el-input
        v-model="searchParams.keyword"
        placeholder="搜索用户名 / 昵称..."
        clearable
        style="width: 260px"
        :prefix-icon="Search"
      />
      <el-button type="primary" @click="fetchList">搜索</el-button>
      <div style="flex: 1"></div>
      <el-button type="primary" :icon="Plus" @click="openAdd">新增骑手</el-button>
    </div>

    <div class="coffee-card" style="padding: 0">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="username" label="用户名" min-width="130" />
        <el-table-column prop="nickname" label="昵称" min-width="130" />
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button type="primary" link size="small" :icon="MapLocation" @click="openTrack(row)">位置追踪</el-button>
            <el-button type="danger" link size="small" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div style="display: flex; justify-content: flex-end; margin-top: 16px">
      <el-pagination
        v-model:current-page="searchParams.page"
        v-model:page-size="searchParams.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @change="fetchList"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑骑手' : '新增骑手'"
      width="480px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" :prop="isEdit ? '' : 'password'">
          <el-input v-model="form.password" type="password" placeholder="密码" show-password />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="昵称" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" placeholder="电话号码" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.statusBool" active-color="#6f4e37" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="trackVisible" title="骑手位置" width="400px">
      <div v-loading="trackLoading">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="骑手">{{ trackData.nickname || trackData.username || '-' }}</el-descriptions-item>
          <el-descriptions-item label="经度">{{ trackData.longitude ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="纬度">{{ trackData.latitude ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatTime(trackData.updateTime) }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="trackVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, MapLocation } from '@element-plus/icons-vue'
import { getRiderList, createRider, updateRider, deleteRider, getRiderTrack } from '@/api/rider'
import dayjs from 'dayjs'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const tableData = ref<any[]>([])
const total = ref(0)
const formRef = ref()

const searchParams = reactive({
  keyword: '',
  page: 1,
  size: 10
})

const form = reactive({
  username: '',
  password: '',
  nickname: '',
  phone: '',
  statusBool: true
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const trackVisible = ref(false)
const trackLoading = ref(false)
const trackData = ref<any>({})

function formatTime(time: string) {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '-'
}

async function fetchList() {
  loading.value = true
  try {
    const res: any = await getRiderList({
      keyword: searchParams.keyword || undefined,
      page: searchParams.page,
      size: searchParams.size
    })
    const data = res?.data || res
    tableData.value = data.records || data.list || data || []
    total.value = data.total || 0
  } catch { /* handled */ } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(form, { username: '', password: '', nickname: '', phone: '', statusBool: true })
  editId.value = null
}

function openAdd() { isEdit.value = false; resetForm(); dialogVisible.value = true }
function openEdit(row: any) {
  isEdit.value = true
  editId.value = row.id
  Object.assign(form, {
    username: row.username || '',
    password: '',
    nickname: row.nickname || '',
    phone: row.phone || '',
    statusBool: row.status === 1
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const data: any = {
      nickname: form.nickname,
      phone: form.phone,
      status: form.statusBool ? 1 : 0
    }
    if (form.password) data.password = form.password
    if (!isEdit.value) data.username = form.username

    if (isEdit.value && editId.value) {
      await updateRider(editId.value, data)
      ElMessage.success('骑手更新成功')
    } else {
      await createRider(data)
      ElMessage.success('骑手创建成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch { /* handled */ } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定要删除骑手"${row.username}"吗？`, '确认删除', { type: 'warning' })
    await deleteRider(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch { /* cancelled */ }
}

async function openTrack(row: any) {
  trackVisible.value = true
  trackData.value = row
  trackLoading.value = true
  try {
    const res: any = await getRiderTrack(row.id)
    trackData.value = (res?.data || res || {})
  } catch { /* ignore */ } finally {
    trackLoading.value = false
  }
}

onMounted(() => { fetchList() })
</script>
