<template>
  <div class="page-container">
    <div class="detail-header">
      <el-button :icon="ArrowLeft" @click="$router.back()">返回</el-button>
      <h2 class="page-heading" style="margin-bottom: 0; margin-left: 12px">订单详情</h2>
    </div>

    <div v-loading="loading">
      <div class="coffee-card detail-card">
        <h3 class="card-title">订单信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单编号">{{ order.orderNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(order.status)" size="small">{{ statusLabel(order.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="门店ID">{{ order.storeId ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="取餐方式">
            {{ order.pickupType === 1 ? '外卖' : '自取' }}
          </el-descriptions-item>
          <el-descriptions-item label="商品小计">¥{{ formatPrice(order.totalPrice) }}</el-descriptions-item>
          <el-descriptions-item label="配送费">¥{{ formatPrice(order.deliveryFee) }}</el-descriptions-item>
          <el-descriptions-item label="实付金额">
            <span style="font-weight: 700; font-size: 16px; color: #6f4e37">
              ¥{{ formatPrice((order.totalPrice ?? 0) + (order.deliveryFee ?? 0)) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(order.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ formatTime(order.payTime) }}</el-descriptions-item>
          <el-descriptions-item label="接单时间">{{ formatTime(order.acceptTime) }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ formatTime(order.completeTime) }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ order.remark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2" v-if="order.addressSnapshot">
            {{ order.addressSnapshot }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getOrderById } from '@/api/order'
import dayjs from 'dayjs'

const route = useRoute()
const loading = ref(false)
const order = ref<any>({})

const statusMap: Record<number, string> = {
  0: '待支付', 1: '制作中', 2: '待取餐', 3: '已完成', 4: '已取消'
}
const statusColors: Record<number, string> = {
  0: 'warning', 1: '', 2: 'info', 3: 'success', 4: 'info'
}

function statusLabel(code: number) { return statusMap[code] || '未知' }
function statusTagType(code: number) { return statusColors[code] || 'info' }
function formatTime(time: string) { return time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '-' }
function formatPrice(val: number) { return (val || 0).toFixed(2) }

async function fetchDetail() {
  loading.value = true
  try {
    const id = route.params.id as string
    const res: any = await getOrderById(Number(id))
    order.value = res?.data || res || {}
  } catch { /* handled */ } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<style lang="scss" scoped>
.detail-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}
.detail-card {
  padding: 20px 24px;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0e8d8;
}
</style>
