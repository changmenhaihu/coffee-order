<template>
  <div class="dashboard-page">
    <h2 class="page-heading">数据概览</h2>

    <el-row :gutter="20" class="stat-row">
      <el-col :xs="12" :sm="6" v-for="card in statCards" :key="card.label">
        <div class="stat-card">
          <div class="stat-icon" :style="{ backgroundColor: card.bgColor }">
            <el-icon :size="24" :color="card.iconColor">
              <component :is="card.icon" />
            </el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ card.value }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :xs="24" :lg="12">
        <div class="chart-card">
          <h3 class="chart-title">近7天订单</h3>
          <div v-if="loading" class="chart-loading">加载中...</div>
          <div v-else class="bar-chart">
            <div
              v-for="(item, idx) in weekOrders"
              :key="idx"
              class="bar-column"
            >
              <div class="bar-label">{{ item.count }}</div>
              <div class="bar-track">
                <div
                  class="bar-fill"
                  :style="{ height: getBarHeight(item.count) + '%' }"
                ></div>
              </div>
              <div class="bar-day">{{ item.label }}</div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :lg="12">
        <div class="chart-card">
          <h3 class="chart-title">分类销量分布</h3>
          <div v-if="loading" class="chart-loading">加载中...</div>
          <div v-else class="pie-chart-container">
            <div class="simple-pie">
              <svg viewBox="0 0 120 120" width="140" height="140">
                <circle
                  v-for="(seg, idx) in pieSegments"
                  :key="idx"
                  cx="60"
                  cy="60"
                  r="50"
                  fill="transparent"
                  :stroke="seg.color"
                  :stroke-width="20"
                  :stroke-dasharray="`${seg.dashLength} ${100 - seg.dashLength}`"
                  :stroke-dashoffset="seg.dashOffset"
                  stroke-linecap="butt"
                  :transform="'rotate(-90 60 60)'"
                />
              </svg>
            </div>
            <div class="pie-legend">
              <div
                v-for="seg in pieSegments"
                :key="seg.label"
                class="legend-item"
              >
                <span class="legend-dot" :style="{ background: seg.color }"></span>
                <span class="legend-label">{{ seg.label }}</span>
                <span class="legend-pct">{{ seg.pct }}%</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  Document,
  Coin,
  User,
  Bicycle
} from '@element-plus/icons-vue'
import { getDashboardData } from '@/api/dashboard'

const loading = ref(false)
const weekOrders = ref<Array<{ label: string; count: number }>>([])
const pieSegments = ref<Array<{ label: string; pct: number; color: string; dashLength: number; dashOffset: number }>>([])

const statCards = ref([
  { label: '今日订单', value: 0, icon: 'Document', bgColor: '#fef5e7', iconColor: '#e6a23c' },
  { label: '今日营收', value: '¥0', icon: 'Coin', bgColor: '#f0f9eb', iconColor: '#67c23a' },
  { label: '今日新增用户', value: 0, icon: 'User', bgColor: '#f4f0fd', iconColor: '#7c5ce7' },
  { label: '在线骑手', value: 0, icon: 'Bicycle', bgColor: '#e8f4fd', iconColor: '#409eff' }
])

function getBarHeight(count: number): number {
  const max = Math.max(...weekOrders.value.map((o) => o.count), 1)
  return Math.round((count / max) * 100)
}

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getDashboardData()
    const data = res?.data || res || {}
    statCards.value[0].value = data.todayOrders ?? data.orderCount ?? 0
    statCards.value[1].value = '¥' + (data.todayRevenue ?? data.revenue ?? 0)
    statCards.value[2].value = data.newUsers ?? data.userCount ?? 0
    statCards.value[3].value = data.riderOnline ?? data.riderCount ?? 0

    if (data.weekOrders) {
      weekOrders.value = data.weekOrders
    } else {
      weekOrders.value = [
        { label: '周一', count: 12 },
        { label: '周二', count: 18 },
        { label: '周三', count: 15 },
        { label: '周四', count: 22 },
        { label: '周五', count: 25 },
        { label: '周六', count: 30 },
        { label: '周日', count: 20 }
      ]
    }

    if (data.categorySales) {
      const total = data.categorySales.reduce((s: number, c: any) => s + c.value, 0) || 1
      let offset = 0
      const colors = ['#6f4e37', '#e6a23c', '#67c23a', '#409eff', '#7c5ce7', '#f56c6c']
      pieSegments.value = data.categorySales.map((c: any, i: number) => {
        const pct = Math.round((c.value / total) * 100)
        const dashLength = pct
        const dashOffset = -offset
        offset += pct
        return {
          label: c.name,
          pct,
          color: colors[i % colors.length],
          dashLength,
          dashOffset
        }
      })
    } else {
      const mock = [
        { name: '咖啡', value: 45 },
        { name: '茶', value: 25 },
        { name: '糕点', value: 18 },
        { name: '其他', value: 12 }
      ]
      const total = mock.reduce((s, c) => s + c.value, 0)
      let offset = 0
      const colors = ['#6f4e37', '#e6a23c', '#67c23a', '#409eff']
      pieSegments.value = mock.map((c, i) => {
        const pct = Math.round((c.value / total) * 100)
        const dashLength = pct
        const dashOffset = -offset
        offset += pct
        return { label: c.name, pct, color: colors[i], dashLength, dashOffset }
      })
    }
  } catch {
    // use defaults already set
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
.stat-row {
  margin-bottom: 0;
}

.chart-card {
  background: #fff;
  border-radius: var(--radius-md);
  padding: 20px 24px;
  box-shadow: var(--shadow-card);
  min-height: 280px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 20px 0;
}

.chart-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 220px;
  color: #909399;
  font-size: 14px;
}

/* Bar chart */
.bar-chart {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 220px;
  padding: 0 10px;
}

.bar-column {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.bar-label {
  font-size: 12px;
  font-weight: 600;
  color: #606266;
}

.bar-track {
  width: 32px;
  height: 150px;
  background: #f5f0e8;
  border-radius: 6px 6px 0 0;
  display: flex;
  align-items: flex-end;
  overflow: hidden;
}

.bar-fill {
  width: 100%;
  background: linear-gradient(180deg, #6f4e37 0%, #8b5e3c 100%);
  border-radius: 6px 6px 0 0;
  transition: height 0.5s ease;
  min-height: 4px;
}

.bar-day {
  font-size: 11px;
  color: #909399;
}

/* Pie chart */
.pie-chart-container {
  display: flex;
  align-items: center;
  gap: 32px;
  justify-content: center;
}

.simple-pie {
  flex-shrink: 0;
}

.pie-legend {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.legend-label {
  font-size: 13px;
  color: #606266;
  min-width: 50px;
}

.legend-pct {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}
</style>
