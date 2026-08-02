<template>
  <div class="page-container">
    <h2 class="page-heading">商家工作台</h2>
    <p class="page-desc" v-if="authStore.userInfo">
      当前账号：{{ authStore.userInfo.nickname || authStore.userInfo.username }}
      <el-tag v-if="authStore.isAdmin" type="warning" size="small">平台管理员（查看全部门店）</el-tag>
      <el-tag v-else type="success" size="small">本门店</el-tag>
    </p>

    <!-- 订单区 -->
    <div class="coffee-card">
      <div class="section-header">
        <h3 class="card-title">门店订单</h3>
        <el-radio-group v-model="statusFilter" @change="fetchOrders">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="0">待支付</el-radio-button>
          <el-radio-button value="1">制作中</el-radio-button>
          <el-radio-button value="2">待取餐</el-radio-button>
          <el-radio-button value="3">已完成</el-radio-button>
          <el-radio-button value="4">已取消</el-radio-button>
        </el-radio-group>
      </div>

      <el-table :data="orders" v-loading="orderLoading" border stripe empty-text="暂无订单">
        <el-table-column prop="orderNo" label="订单编号" min-width="170" />
        <el-table-column prop="storeName" label="门店" min-width="140" />
        <el-table-column label="取餐方式" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.pickupType === 1 ? 'primary' : 'info'" size="small">
              {{ row.pickupType === 1 ? '外卖' : '自取' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="firstItemDesc" label="商品" min-width="200" />
        <el-table-column label="金额" width="110" align="right">
          <template #default="{ row }">
            <span style="font-weight: 600">¥{{ formatPrice(row.payAmount ?? row.totalPrice) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="primary"
              size="small"
              :loading="actingOrderId === row.orderId"
              @click="handleAccept(row)"
            >
              接单
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="success"
              size="small"
              :loading="actingOrderId === row.orderId"
              @click="handleComplete(row)"
            >
              出餐
            </el-button>
            <span v-if="row.status !== 0 && row.status !== 1" style="color: #c0c4cc">-</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 商品区 -->
    <div class="coffee-card" style="margin-top: 16px">
      <div class="section-header">
        <h3 class="card-title">门店商品</h3>
      </div>
      <el-table :data="products" v-loading="productLoading" border stripe empty-text="暂无商品">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="图片" width="90" align="center">
          <template #default="{ row }">
            <el-avatar :src="row.image || ''" shape="square" :size="48" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="160" />
        <el-table-column label="售价" width="100" align="right">
          <template #default="{ row }">
            <span style="font-weight: 600">¥{{ formatPrice(row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="推荐" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isRecommend === 1 ? 'warning' : 'info'" size="small">
              {{ row.isRecommend === 1 ? '推荐' : '普通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="上下架" width="110" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              :loading="togglingProductId === row.id"
              active-color="#6f4e37"
              @change="(val: boolean) => handleToggleProduct(row, val)"
            />
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getStoreOrders,
  acceptStoreOrder,
  completeStoreOrder,
  getStoreProducts,
  updateStoreProduct
} from '@/api/store'
import { useAuthStore } from '@/stores/auth'
import dayjs from 'dayjs'

const authStore = useAuthStore()

const statusFilter = ref('')
const orders = ref<any[]>([])
const orderLoading = ref(false)
const actingOrderId = ref<number | null>(null)

const products = ref<any[]>([])
const productLoading = ref(false)
const togglingProductId = ref<number | null>(null)

const statusTagTypes: Record<number, string> = {
  0: 'warning', 1: '', 2: 'info', 3: 'success', 4: 'info'
}

function statusTagType(code: number) {
  return statusTagTypes[code] || 'info'
}

function formatPrice(val: number | undefined) {
  if (val == null) return '0.00'
  return Number(val).toFixed(2)
}

function formatTime(time: string) {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '-'
}

async function fetchOrders() {
  orderLoading.value = true
  try {
    const params: any = { page: 1, size: 100 }
    if (statusFilter.value !== '') params.status = statusFilter.value
    const res: any = await getStoreOrders(params)
    const data = res?.data || res
    orders.value = data?.list || []
  } catch {
    /* handled by interceptor */
  } finally {
    orderLoading.value = false
  }
}

async function fetchProducts() {
  productLoading.value = true
  try {
    const res: any = await getStoreProducts()
    products.value = res?.data || res || []
  } catch {
    /* handled by interceptor */
  } finally {
    productLoading.value = false
  }
}

async function handleAccept(row: any) {
  actingOrderId.value = row.orderId
  try {
    await acceptStoreOrder(row.orderId)
    ElMessage.success('已接单，开始制作')
    fetchOrders()
  } catch {
    /* handled */
  } finally {
    actingOrderId.value = null
  }
}

async function handleComplete(row: any) {
  actingOrderId.value = row.orderId
  try {
    await completeStoreOrder(row.orderId)
    ElMessage.success('已出餐，等待取餐')
    fetchOrders()
  } catch {
    /* handled */
  } finally {
    actingOrderId.value = null
  }
}

async function handleToggleProduct(row: any, val: boolean) {
  const target = val ? 1 : 0
  try {
    await ElMessageBox.confirm(
      `确定将商品"${row.name}"${target === 1 ? '上架' : '下架'}吗？`,
      '确认操作',
      { type: 'warning' }
    )
  } catch {
    row.status = target === 1 ? 0 : 1 // 取消则回退显示
    return
  }
  togglingProductId.value = row.id
  try {
    await updateStoreProduct(row.id, { status: target })
    row.status = target
    ElMessage.success(target === 1 ? '已上架' : '已下架')
  } catch {
    row.status = target === 1 ? 0 : 1
  } finally {
    togglingProductId.value = null
  }
}

onMounted(() => {
  fetchOrders()
  fetchProducts()
})
</script>

<style lang="scss" scoped>
.page-desc {
  color: #909399;
  margin: 4px 0 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}
.coffee-card {
  padding: 20px 24px;
}
</style>
