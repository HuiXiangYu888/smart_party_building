<template>
  <div class="login-container">
    <div class="login-box">
      <!-- 装饰性顶部条 -->
      <div class="top-accent"></div>
      
      <div class="login-header">
        <div class="logo-wrapper">
          <el-icon :size="40" color="#409EFF"><Flag /></el-icon>
        </div>
        <h2>智慧党建管理端</h2>
        <p>系统资源受限，请使用授权账户登录</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
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
        
        <div class="form-options">
          <el-checkbox label="记住账号" size="small" />
          <el-link type="info" :underline="false" style="font-size: 12px">遇到问题？</el-link>
        </div>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '身份验证中...' : '立即登录' }}
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <p>Copyright © 2024 智慧党建技术支持团队</p>
        <p>建议使用 Chrome 或 Edge 浏览器以获得最佳体验</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Flag, User, Lock } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'

const router = useRouter()
const authStore = useAuthStore()

// 表单引用
const loginFormRef = ref()

// 加载状态
const loading = ref(false)

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

// 表单验证规则
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

// 处理登录
const handleLogin = async () => {
  try {
    // 表单验证
    await loginFormRef.value.validate()
    
    // 统一使用真实API（避免开发环境模拟导致假登录）
    loading.value = true
    const response = await login({ username: loginForm.username, password: loginForm.password })
    // 后端返回 Result<LoginVO>，结构：{ code, data: { accessToken, refreshToken, userId, username, userType, expiresIn } }
    const loginVO = response.data || {}
    const token = loginVO.accessToken
    const userInfo = {
      id: loginVO.userId,
      username: loginVO.username,
      userType: loginVO.userType,
      // realName 可选，后端暂无则使用 username
      realName: loginVO.realName || loginVO.username
    }
    authStore.login(token, userInfo)
    ElMessage.success('登录成功！')
    await router.push('/dashboard')
    
  } catch (error) {
    console.error('登录失败:', error)
    
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
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  /* 使用 Element Plus 风格的浅灰背景，并加入极淡的底纹感 */
  background-color: #f0f2f5;
  background-image: radial-gradient(#d9d9d9 1px, transparent 0);
  background-size: 30px 30px;
  padding: 20px;
}

.login-box {
  width: 100%;
  max-width: 420px;
  background: #ffffff;
  border-radius: 4px; /* 减小圆角，显得更严谨 */
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 0; /* 内部 padding 由子元素控制 */
  overflow: hidden;
  position: relative;
}

.top-accent {
  height: 4px;
  background-color: #409EFF; /* 清新的天蓝色 */
}

.login-header {
  text-align: center;
  padding: 40px 40px 20px;
}

.logo-wrapper {
  margin-bottom: 16px;
}

.login-header h2 {
  color: #262626; /* 深灰，非纯黑 */
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 12px;
  letter-spacing: 1px;
}

.login-header p {
  color: #8c8c8c;
  font-size: 14px;
  margin: 0;
}

.login-form {
  padding: 0 40px;
}

/* 覆盖 Element Plus 的 label 样式使其更紧凑 */
:deep(.el-form-item__label) {
  font-weight: 500;
  color: #595959;
  margin-bottom: 4px !important;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.login-button {
  width: 100%;
  height: 45px;
  font-size: 16px;
  font-weight: 600;
  background-color: #409EFF; /* 使用 Element Plus 主色蓝 */
  border-color: #409EFF;
  transition: all 0.3s;
}

.login-button:hover {
  background-color: #337ecc;
  border-color: #337ecc;
  box-shadow: 0 4px 10px rgba(64, 158, 255, 0.2);
}

.login-footer {
  text-align: center;
  padding: 20px 40px 30px;
  border-top: 1px solid #f0f0f0;
  margin-top: 10px;
}

.login-footer p {
  color: #bfbfbf;
  font-size: 12px;
  margin: 4px 0;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-box {
    box-shadow: none;
    background: transparent;
  }
  .login-header, .login-form, .login-footer {
    padding: 20px;
  }
}
</style>
