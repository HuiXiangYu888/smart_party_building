import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

export const useAuthStore = defineStore('auth', () => {
  // State
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  // Computed
  const isLoggedIn = computed(() => {
    return !!token.value && !!userInfo.value.id
  })

  const isSystemAdmin = computed(() => {
    return userInfo.value?.userType === 'SYSTEM_ADMIN'
  })

  const isBranchAdmin = computed(() => {
    return userInfo.value?.userType === 'BRANCH_ADMIN'
  })

  const displayName = computed(() => {
    return userInfo.value?.realName || userInfo.value?.username || '-'
  })

  // Actions
  const login = (newToken, newUserInfo) => {
    token.value = newToken
    userInfo.value = newUserInfo
    localStorage.setItem('token', newToken)
    localStorage.setItem('userInfo', JSON.stringify(newUserInfo))
  }

  const logout = () => {
    token.value = ''
    userInfo.value = {}
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
    isSystemAdmin,
    isBranchAdmin,
    displayName,
    login,
    logout,
    updateUserInfo
  }
})
