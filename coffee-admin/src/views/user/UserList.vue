<template>
  <div class="page-container">
    <h2 class="page-heading">用户管理</h2>

    <div class="search-bar">
      <el-input
        v-model="searchParams.keyword"
        placeholder="搜索用户名 / 昵称..."
        clearable
        style="width: 240px"
        :prefix-icon="Search"
      />
      <el-select v-model="searchParams.role" placeholder="角色" clearable style="width: 160px">
        <el-option label="平台管理员" value="ADMIN" />
        <el-option label="商家" value="STORE_MANAGER" />
        <el-option label="骑手" value="RIDER" />
        <el-option label="用户" value="USER" />
      </el-select>
      <el-button type="primary" @click="fetchList">搜索</el-button>
    </div>

    <div class="coffee-card" style="padding: 0">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column label="余额" width="110" align="right">
          <template #default="{ row }">
            <span style="font-weight: 600; color: #e6a23c">¥{{ formatPrice(row.balance) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="90" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.role === 'ADMIN' ? 'danger' : (row.role === 'STORE_MANAGER' || row.role === 'RIDER') ? 'warning' : 'info'"
              size="small"
            >
              {{ roleLabel(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '冻结' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button type="warning" link size="small" :icon="Coin" @click="openRecharge(row)">充值</el-button>
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

    <el-dialog v-model="dialogVisible" title="编辑用户" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" label-width="100px">
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="昵称" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" placeholder="电话" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="邮箱" />
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

    <el-dialog v-model="rechargeVisible" title="充值" width="380px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="用户">
          <span>{{ rechargeUser?.username || '-' }}</span>
        </el-form-item>
        <el-form-item label="充值金额">
          <el-input-number
            v-model="rechargeAmount"
            :min="1"
            :step="10"
            :precision="2"
            style="width: 100%"
            placeholder="请输入金额"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rechargeVisible = false">取消</el-button>
        <el-button type="primary" :loading="rechargeLoading" @click="handleRecharge">确认充值</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Edit, Coin } from '@element-plus/icons-vue'
import { getUserList, updateUser, rechargeUser as rechargeUserApi } from '@/api/user'
import dayjs from 'dayjs'

function roleLabel(role: string) {
  const map: Record<string, string> = {
    ADMIN: '平台管理员', STORE_MANAGER: '商家', RIDER: '骑手', USER: '用户'
  }
  return map[role] || role || '用户'
}

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const editId = ref<number | null>(null)
const formRef = ref()

const searchParams = reactive({
  keyword: '',
  role: '',
  page: 1,
  size: 10
})

const form = reactive({
  nickname: '',
  phone: '',
  email: '',
  statusBool: true
})

const rechargeVisible = ref(false)
const rechargeLoading = ref(false)
const rechargeUser = ref<any>(null)
const rechargeAmount = ref(0)

function formatTime(time: string) {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '-'
}

function formatPrice(val: number) {
  return (val ?? 0).toFixed(2)
}

async function fetchList() {
  loading.value = true
  try {
    const res: any = await getUserList({
      keyword: searchParams.keyword || undefined,
      role: searchParams.role || undefined,
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

function openEdit(row: any) {
  editId.value = row.id
  Object.assign(form, {
    nickname: row.nickname || '',
    phone: row.phone || '',
    email: row.email || '',
    statusBool: row.status === 1
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  submitLoading.value = true
  try {
    await updateUser(editId.value!, {
      nickname: form.nickname,
      phone: form.phone,
      email: form.email,
      status: form.statusBool ? 1 : 0
    })
    ElMessage.success('更新成功')
    dialogVisible.value = false
    fetchList()
  } catch { /* handled */ } finally {
    submitLoading.value = false
  }
}

function openRecharge(row: any) {
  rechargeUser.value = row
  rechargeAmount.value = 0
  rechargeVisible.value = true
}

async function handleRecharge() {
  if (!rechargeAmount.value || rechargeAmount.value <= 0) {
    ElMessage.warning('请输入有效金额')
    return
  }
  rechargeLoading.value = true
  try {
    await rechargeUserApi(rechargeUser.value.id, {
      amount: rechargeAmount.value
    })
    ElMessage.success('充值成功')
    rechargeVisible.value = false
    fetchList()
  } catch { /* handled */ } finally {
    rechargeLoading.value = false
  }
}

onMounted(() => { fetchList() })
</script>
