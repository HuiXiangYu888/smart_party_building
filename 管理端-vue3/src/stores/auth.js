import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  // 计算属性
  const isLoggedIn = computed(() => {
    return !!token.value && !!userInfo.value.id
  })

  // 方法
  const login = (newToken, newUserInfo) => {
    token.value = newToken
    userInfo.value = newUserInfo
    
    // 保存到localStorage
    localStorage.setItem('token', newToken)
    localStorage.setItem('userInfo', JSON.stringify(newUserInfo))
    
    console.log('登录成功，用户信息：', newUserInfo)
  }

  const logout = () => {
    token.value = ''
    userInfo.value = {}
    
    // 清除localStorage
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    
    ElMessage.success('已退出登录')
  }

  const updateUserInfo = (newUserInfo) => {
    userInfo.value = { ...userInfo.value, ...newUserInfo }
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    login,
    logout,
    updateUserInfo
  }
})
