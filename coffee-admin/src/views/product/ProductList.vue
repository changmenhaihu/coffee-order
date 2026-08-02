<template>
  <div class="page-container">
    <h2 class="page-heading">商品管理</h2>

    <div class="search-bar">
      <el-select v-model="searchParams.storeId" placeholder="选择门店" clearable style="width: 180px" @change="fetchList">
        <el-option v-for="s in storeOptions" :key="s.id" :label="s.name" :value="s.id" />
      </el-select>
      <el-input
        v-model="searchParams.keyword"
        placeholder="搜索关键词..."
        clearable
        style="width: 200px"
        :prefix-icon="Search"
      />
      <el-button type="primary" @click="fetchList">搜索</el-button>
      <div style="flex: 1"></div>
      <el-button type="primary" :icon="Plus" @click="openAdd">新增商品</el-button>
    </div>

    <div class="coffee-card" style="padding: 0">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="图片" width="80" align="center">
          <template #default="{ row }">
            <el-avatar v-if="row.image" :src="row.image" shape="square" :size="48" fit="cover" />
            <div v-else class="cover-placeholder">
              <el-icon :size="20" color="#c0c4cc"><PictureFilled /></el-icon>
            </div>
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
        <el-table-column prop="sales" label="销量" width="80" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" :icon="Edit" @click="openEdit(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑商品' : '新增商品'" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="门店" prop="storeId">
          <el-select v-model="form.storeId" placeholder="选择门店" style="width: 100%" @change="onStoreChange">
            <el-option v-for="s in storeOptions" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%">
            <el-option v-for="c in categoryOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="售价" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="商品描述" />
        </el-form-item>
        <el-form-item label="商品图片">
          <el-input v-model="form.image" placeholder="输入图片链接" />
        </el-form-item>
        <el-form-item label="是否推荐">
          <el-switch v-model="form.isRecommendBool" active-color="#6f4e37" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" style="width: 100%" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, PictureFilled } from '@element-plus/icons-vue'
import { getProductList, createProduct, updateProduct, deleteProduct, getCategories } from '@/api/product'
import { getAllStores } from '@/api/store'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const tableData = ref<any[]>([])
const total = ref(0)
const storeOptions = ref<any[]>([])
const categoryOptions = ref<any[]>([])
const formRef = ref()

const searchParams = reactive({
  storeId: '',
  keyword: '',
  page: 1,
  size: 10
})

const form = reactive({
  storeId: '',
  categoryId: '',
  name: '',
  price: 0,
  description: '',
  image: '',
  isRecommendBool: false,
  sortOrder: 0,
  statusBool: true
})

const rules = {
  storeId: [{ required: true, message: '请选择门店', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }]
}

async function fetchStores() {
  try {
    const res: any = await getAllStores()
    storeOptions.value = res?.records || res?.list || res || []
  } catch {
    // ignore
  }
}

async function onStoreChange(storeId: any) {
  categoryOptions.value = []
  form.categoryId = ''
  if (!storeId) return
  try {
    const res: any = await getCategories(storeId)
    categoryOptions.value = res?.data || res || []
  } catch {
    // ignore
  }
}

async function fetchList() {
  loading.value = true
  try {
    const res: any = await getProductList({
      storeId: searchParams.storeId || undefined,
      keyword: searchParams.keyword || undefined,
      page: searchParams.page,
      size: searchParams.size
    })
    const data = res?.data || res
    tableData.value = data?.records || data?.list || data || []
    total.value = data?.total || 0
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

function formatPrice(val: number) {
  return (val ?? 0).toFixed(2)
}

function resetForm() {
  Object.assign(form, {
    storeId: '',
    categoryId: '',
    name: '',
    price: 0,
    description: '',
    image: '',
    isRecommendBool: false,
    sortOrder: 0,
    statusBool: true
  })
  categoryOptions.value = []
  editId.value = null
}

function openAdd() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

async function openEdit(row: any) {
  isEdit.value = true
  editId.value = row.id
  Object.assign(form, {
    storeId: row.storeId || '',
    categoryId: row.categoryId || '',
    name: row.name || '',
    price: row.price || 0,
    description: row.description || '',
    image: row.image || '',
    isRecommendBool: row.isRecommend === 1,
    sortOrder: row.sortOrder || 0,
    statusBool: row.status === 1
  })
  await onStoreChange(form.storeId)
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const data = {
      storeId: form.storeId,
      categoryId: form.categoryId,
      name: form.name,
      price: form.price,
      description: form.description,
      image: form.image,
      isRecommend: form.isRecommendBool ? 1 : 0,
      sortOrder: form.sortOrder,
      status: form.statusBool ? 1 : 0
    }
    if (isEdit.value && editId.value) {
      await updateProduct(editId.value, data)
      ElMessage.success('商品更新成功')
    } else {
      await createProduct(data)
      ElMessage.success('商品创建成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // handled
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定要删除商品"${row.name}"吗？`, '确认删除', { type: 'warning' })
    await deleteProduct(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch {
    // cancelled
  }
}

onMounted(() => {
  fetchStores()
  fetchList()
})
</script>

<style lang="scss" scoped>
.cover-placeholder {
  width: 48px;
  height: 48px;
  border-radius: 4px;
  background: #f5f0e8;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
