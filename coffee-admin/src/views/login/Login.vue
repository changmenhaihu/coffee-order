<template>
  <div class="login-page">
    <div class="login-left">
      <div class="brand-inner">
        <div class="brand-icon">
          <el-icon :size="64" color="#d4a574"><CoffeeCup /></el-icon>
        </div>
        <h1 class="brand-title">Coffee Order</h1>
        <p class="brand-subtitle">O2O Coffee Delivery Management</p>
        <div class="brand-divider"></div>
        <p class="brand-desc">
          一站式管理您的咖啡门店、商品、订单和骑手。
        </p>
      </div>
    </div>
    <div class="login-right">
      <div class="login-card">
        <h2 class="login-card-title">欢迎回来</h2>
        <p class="login-card-sub">登录管理后台</p>
        <el-form
          ref="formRef"
          :model="loginForm"
          :rules="rules"
          size="large"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="邮箱"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="密码"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
              round
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>
        <div v-if="errorMsg" class="login-error">
          <el-icon><WarningFilled /></el-icon>
          {{ errorMsg }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CoffeeCup, User, Lock, WarningFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()

const loading = ref(false)
const errorMsg = ref('')
const formRef = ref()

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入您的邮箱', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  errorMsg.value = ''

  try {
    await authStore.login(loginForm.username, loginForm.password)
    const role = authStore.userInfo?.role
    if (role === 'ADMIN') {
      ElMessage.success('登录成功')
      router.push('/')
    } else if (role === 'STORE_MANAGER') {
      ElMessage.success('商家登录成功')
      router.push('/store/workbench')
    } else {
      ElMessage.error('该账号无权使用管理后台')
      authStore.clearAuth()
      errorMsg.value = '该账号无权使用管理后台'
    }
  } catch (e: any) {
    errorMsg.value = e?.message || '登录失败，请检查账号信息'
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  display: flex;
  height: 100vh;
  width: 100vw;
}

.login-left {
  width: 45%;
  min-width: 360px;
  background: linear-gradient(160deg, #3e2a1a 0%, #5a3e2b 60%, #6f4e37 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(
      ellipse at 30% 50%,
      rgba(212, 165, 116, 0.1) 0%,
      transparent 60%
    );
  }
}

.brand-inner {
  text-align: center;
  position: relative;
  z-index: 1;
}

.brand-icon {
  margin-bottom: 24px;
}

.brand-title {
  font-size: 36px;
  font-weight: 800;
  color: #ffffff;
  letter-spacing: 2px;
  margin: 0 0 8px 0;
}

.brand-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0 0 28px 0;
  font-weight: 400;
}

.brand-divider {
  width: 60px;
  height: 3px;
  background: #d4a574;
  margin: 0 auto 28px auto;
  border-radius: 2px;
}

.brand-desc {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
  line-height: 1.6;
  max-width: 280px;
  margin: 0 auto;
}

.login-right {
  flex: 1;
  background: #f5f0e8;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  width: 400px;
  max-width: 90vw;
  background: #fff;
  border-radius: var(--radius-lg);
  padding: 48px 40px 40px;
  box-shadow: 0 4px 24px rgba(111, 78, 55, 0.1);
}

.login-card-title {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px 0;
  text-align: center;
}

.login-card-sub {
  font-size: 14px;
  color: #909399;
  margin: 0 0 32px 0;
  text-align: center;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 1px;
}

.login-error {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--color-danger);
  font-size: 13px;
  margin-top: 8px;
  justify-content: center;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}
</style>
