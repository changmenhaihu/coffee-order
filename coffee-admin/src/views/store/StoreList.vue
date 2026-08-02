<template>
  <div class="page-container">
    <h2 class="page-heading">门店管理</h2>

    <div class="search-bar">
      <el-input
        v-model="searchParams.name"
        placeholder="搜索门店名称..."
        clearable
        style="width: 240px"
        @clear="fetchList"
        :prefix-icon="Search"
      />
      <el-button type="primary" @click="fetchList">搜索</el-button>
      <div style="flex: 1"></div>
      <el-button type="primary" :icon="Plus" @click="openAdd">新增门店</el-button>
    </div>

    <div class="coffee-card" style="padding: 0">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="name" label="门店名称" min-width="160" />
        <el-table-column prop="address" label="地址" min-width="220" show-overflow-tooltip />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(val: boolean) => handleStatusChange(row, val)"
              active-color="#6f4e37"
            />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" :icon="Edit" @click="openEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" link size="small" :icon="Delete" @click="handleDelete(row)">
              删除
            </el-button>
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
      :title="isEdit ? '编辑门店' : '新增门店'"
      width="580px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="130px">
        <el-form-item label="门店名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入门店名称" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="经度" prop="longitude">
          <el-input-number v-model="form.longitude" :precision="6" :step="0.001" style="width: 100%" />
        </el-form-item>
        <el-form-item label="纬度" prop="latitude">
          <el-input-number v-model="form.latitude" :precision="6" :step="0.001" style="width: 100%" />
        </el-form-item>
        <el-form-item label="门店图片">
          <el-input v-model="form.image" placeholder="输入图片链接" />
        </el-form-item>
        <el-form-item label="营业时间">
          <el-input v-model="form.businessHours" placeholder="例如 08:00-22:00" />
        </el-form-item>
        <el-form-item label="起送金额">
          <el-input-number v-model="form.minDeliveryAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="配送费">
          <el-input-number v-model="form.deliveryFee" :min="0" :precision="2" style="width: 100%" />
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
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getStoreList, createStore, updateStore, deleteStore } from '@/api/store'
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
  name: '',
  page: 1,
  size: 10
})

const form = reactive({
  name: '',
  address: '',
  phone: '',
  longitude: 0,
  latitude: 0,
  image: '',
  businessHours: '',
  minDeliveryAmount: 0,
  deliveryFee: 0,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入门店名称', trigger: 'blur' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }],
  longitude: [{ required: true, message: '请输入经度', trigger: 'blur' }],
  latitude: [{ required: true, message: '请输入纬度', trigger: 'blur' }]
}

function formatTime(time: string) {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '-'
}

async function fetchList() {
  loading.value = true
  try {
    const res: any = await getStoreList({
      name: searchParams.name || undefined,
      page: searchParams.page,
      size: searchParams.size
    })
    const data = res?.data || res
    tableData.value = data?.records || data?.list || data || []
    total.value = data?.total || 0
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(form, {
    name: '',
    address: '',
    phone: '',
    longitude: 0,
    latitude: 0,
    image: '',
    businessHours: '',
    minDeliveryAmount: 0,
    deliveryFee: 0,
    status: 1
  })
  editId.value = null
}

function openAdd() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: any) {
  isEdit.value = true
  editId.value = row.id
  Object.assign(form, {
    name: row.name || '',
    address: row.address || '',
    phone: row.phone || '',
    longitude: row.longitude || 0,
    latitude: row.latitude || 0,
    image: row.image || '',
    businessHours: row.businessHours || '',
    minDeliveryAmount: row.minDeliveryAmount || 0,
    deliveryFee: row.deliveryFee || 0,
    status: row.status ?? 1
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value && editId.value) {
      await updateStore(editId.value, { ...form })
      ElMessage.success('门店更新成功')
    } else {
      await createStore({ ...form })
      ElMessage.success('门店创建成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // handled by interceptor
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定要删除门店"${row.name}"吗？此操作不可撤销。`, '确认删除', {
      type: 'warning'
    })
    await deleteStore(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch {
    // cancelled or error
  }
}

async function handleStatusChange(row: any, val: boolean) {
  try {
    await updateStore(row.id, { status: val ? 1 : 0 })
    row.status = val ? 1 : 0
    ElMessage.success('状态更新成功')
  } catch {
    row.status = val ? 0 : 1
  }
}

onMounted(() => {
  fetchList()
})
</script>
