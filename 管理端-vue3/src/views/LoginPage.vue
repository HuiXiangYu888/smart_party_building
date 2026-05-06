<template>
  <div class="min-h-screen flex items-center justify-center bg-[var(--color-bg-page)] p-5 transition-colors duration-300"
       style="background-image: radial-gradient(var(--color-border) 1px, transparent 0); background-size: 30px 30px;">
    <div class="w-full max-w-md page-card overflow-hidden">
      <!-- Top accent bar -->
      <div class="h-1 bg-gradient-to-r from-indigo-500 to-violet-500"></div>

      <!-- Header -->
      <div class="text-center pt-10 pb-5 px-10">
        <div class="mb-4">
          <el-icon :size="40" color="var(--color-primary)"><Flag /></el-icon>
        </div>
        <h2 class="text-2xl font-semibold text-[var(--color-text-primary)] mb-2 tracking-wide">智慧党建管理端</h2>
        <p class="text-sm text-[var(--color-text-muted)]">系统资源受限，请使用授权账户登录</p>
      </div>

      <!-- Form -->
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="px-10"
        label-position="top"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username" label="账号">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入管理员账号"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password" label="密码">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入登录密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <div class="flex justify-between items-center mb-5">
          <el-checkbox v-model="rememberMe" label="记住账号" size="small" />
          <el-link type="info" :underline="false" class="!text-xs">遇到问题？</el-link>
        </div>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="w-full !h-11 !text-base !font-semibold !rounded-lg"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '身份验证中...' : '立即登录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <!-- Footer -->
      <div class="text-center py-5 px-10 border-t border-[var(--color-border-light)] mt-2">
        <p class="text-xs text-[var(--color-text-muted)]">Copyright © 2024 智慧党建技术支持团队</p>
        <p class="text-xs text-[var(--color-text-muted)] mt-1">建议使用 Chrome 或 Edge 浏览器</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'

const router = useRouter()
const authStore = useAuthStore()

const loginFormRef = ref()
const loading = ref(false)
const rememberMe = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

// Restore remembered username
onMounted(() => {
  const saved = localStorage.getItem('rememberedUsername')
  if (saved) {
    loginForm.username = saved
    rememberMe.value = true
  }
})

const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    loading.value = true

    const response = await login({ username: loginForm.username, password: loginForm.password })
    const loginVO = response.data || {}
    const token = loginVO.accessToken
    const userInfo = {
      id: loginVO.userId,
      username: loginVO.username,
      userType: loginVO.userType,
      realName: loginVO.realName || loginVO.username
    }

    // Remember username
    if (rememberMe.value) {
      localStorage.setItem('rememberedUsername', loginForm.username)
    } else {
      localStorage.removeItem('rememberedUsername')
    }

    authStore.login(token, userInfo)
    ElMessage.success('登录成功！')
    await router.push('/dashboard')
  } catch (error) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('登录失败，请检查用户名和密码')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
:deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--color-text-secondary);
  margin-bottom: 4px !important;
}
</style>
