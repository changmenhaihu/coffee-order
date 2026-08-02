<template>
  <div class="page-container">
    <h2 class="page-heading">订单管理</h2>

    <div class="search-bar">
      <el-select v-model="searchParams.status" placeholder="状态" clearable style="width: 140px">
        <el-option label="待支付" :value="0" />
        <el-option label="制作中" :value="1" />
        <el-option label="待取餐" :value="2" />
        <el-option label="已完成" :value="3" />
        <el-option label="已取消" :value="4" />
      </el-select>
      <el-select v-model="searchParams.type" placeholder="取餐方式" clearable style="width: 140px">
        <el-option label="自取" :value="0" />
        <el-option label="外卖" :value="1" />
      </el-select>
      <el-select v-model="searchParams.storeId" placeholder="门店" clearable style="width: 160px">
        <el-option v-for="s in storeOptions" :key="s.id" :label="s.name" :value="s.id" />
      </el-select>
      <el-input
        v-model="searchParams.orderNo"
        placeholder="订单编号"
        clearable
        style="width: 180px"
        :prefix-icon="Search"
      />
      <el-button type="primary" @click="fetchList">搜索</el-button>
    </div>

    <div class="coffee-card" style="padding: 0">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="orderNo" label="订单编号" min-width="170" />
        <el-table-column prop="storeName" label="门店" min-width="140" />
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.pickupType === 1 ? 'primary' : 'info'" size="small">
              {{ row.pickupType === 1 ? '外卖' : '自取' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="110" align="right">
          <template #default="{ row }">
            <span style="font-weight: 600">¥{{ formatPrice(row.payAmount ?? row.totalPrice) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="goDetail(row.id)">
              详情
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="primary"
              link
              size="small"
              @click="openAssignRider(row)"
            >
              指派骑手
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

    <el-dialog v-model="riderDialogVisible" title="指派骑手" width="500px" destroy-on-close>
      <el-table :data="riderList" v-loading="riderLoading" height="300" @row-click="selectRiderRow">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column label="选择" width="60" align="center">
          <template #default="{ row }">
            <el-radio :model-value="selectedRiderId" :value="row.id" />
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="riderDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="assignLoading" @click="confirmAssign">
          确认指派
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getOrderList, assignRider } from '@/api/order'
import { getAllStores } from '@/api/store'
import { getRiderList } from '@/api/rider'
import dayjs from 'dayjs'

const router = useRouter()
const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const storeOptions = ref<any[]>([])

const searchParams = reactive({
  status: '' as any,
  type: '',
  storeId: '',
  orderNo: '',
  page: 1,
  size: 10
})

const riderDialogVisible = ref(false)
const riderLoading = ref(false)
const assignLoading = ref(false)
const riderList = ref<any[]>([])
const selectedRiderId = ref<number | null>(null)
const assigningOrder = ref<any>(null)

const statusMap: Record<number, string> = {
  0: '待支付', 1: '制作中', 2: '待取餐', 3: '已完成', 4: '已取消'
}

const statusColors: Record<number, string> = {
  0: 'warning', 1: '', 2: 'info', 3: 'success', 4: 'info'
}

function statusLabel(code: number) {
  return statusMap[code] || '未知'
}

function statusTagType(code: number) {
  return statusColors[code] || 'info'
}

function formatTime(time: string) {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '-'
}

function formatPrice(val: number) {
  return (val ?? 0).toFixed(2)
}

function goDetail(id: number) {
  router.push(`/order/detail/${id}`)
}

async function fetchStores() {
  try {
    const res: any = await getAllStores()
    storeOptions.value = res?.records || res?.list || res || []
  } catch { /* ignore */ }
}

async function fetchList() {
  loading.value = true
  try {
    const params: any = {
      page: searchParams.page,
      size: searchParams.size
    }
    if (searchParams.status !== '' && searchParams.status !== null && searchParams.status !== undefined) {
      params.status = searchParams.status
    }
    if (searchParams.type !== '') params.type = searchParams.type
    if (searchParams.storeId) params.storeId = searchParams.storeId
    if (searchParams.orderNo) params.orderNo = searchParams.orderNo

    const res: any = await getOrderList(params)
    const data = res?.data || res
    tableData.value = data?.records || data?.list || data || []
    total.value = data?.total || 0
  } catch { /* handled */ } finally {
    loading.value = false
  }
}

async function openAssignRider(order: any) {
  assigningOrder.value = order
  selectedRiderId.value = null
  riderDialogVisible.value = true
  riderLoading.value = true
  try {
    const res: any = await getRiderList({ page: 1, size: 1000 })
    riderList.value = res?.records || res?.list || []
  } catch { /* ignore */ } finally {
    riderLoading.value = false
  }
}

function selectRiderRow(row: any) {
  selectedRiderId.value = row.id
}

async function confirmAssign() {
  if (!selectedRiderId.value || !assigningOrder.value) {
    ElMessage.warning('请选择骑手')
    return
  }
  assignLoading.value = true
  try {
    await assignRider(assigningOrder.value.id, selectedRiderId.value)
    ElMessage.success('骑手指派成功')
    riderDialogVisible.value = false
    fetchList()
  } catch { /* handled */ } finally {
    assignLoading.value = false
  }
}

onMounted(() => {
  fetchStores()
  fetchList()
})
</script>
