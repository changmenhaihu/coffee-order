<template>
  <div class="page-container">
    <h2 class="page-heading">操作日志</h2>

    <div class="search-bar">
      <el-select v-model="searchParams.userId" placeholder="用户" clearable filterable style="width: 180px">
        <el-option
          v-for="u in userOptions"
          :key="u.id"
          :label="u.username"
          :value="u.id"
        />
      </el-select>
      <el-select v-model="searchParams.businessType" placeholder="业务类型" clearable style="width: 160px">
        <el-option label="登录" value="login" />
        <el-option label="订单" value="order" />
        <el-option label="商品" value="product" />
        <el-option label="门店" value="store" />
        <el-option label="用户" value="user" />
        <el-option label="骑手" value="rider" />
        <el-option label="系统" value="system" />
      </el-select>
      <el-button type="primary" @click="fetchList">搜索</el-button>
    </div>

    <div class="coffee-card" style="padding: 0">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="businessType" label="业务类型" width="130" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.businessType || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userId" label="用户ID" width="80" align="center" />
        <el-table-column label="请求路径" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span style="font-size: 12px; color: #909399">{{ row.requestUrl || row.url || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.costTime !== undefined">{{ row.costTime }}ms</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getOperLogList } from '@/api/system'
import { getUserList } from '@/api/user'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const userOptions = ref<any[]>([])

const searchParams = reactive({
  userId: '',
  businessType: '',
  page: 1,
  size: 10
})

function formatTime(time: string) {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '-'
}

async function fetchUsers() {
  try {
    const res: any = await getUserList({ page: 1, size: 9999 })
    const data = res?.data || res
    userOptions.value = data.records || data.list || data || []
  } catch { /* ignore */ }
}

async function fetchList() {
  loading.value = true
  try {
    const res: any = await getOperLogList({
      userId: searchParams.userId || undefined,
      businessType: searchParams.businessType || undefined,
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

onMounted(() => {
  fetchUsers()
  fetchList()
})
</script>
