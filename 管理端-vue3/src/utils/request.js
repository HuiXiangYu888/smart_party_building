import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

// 创建axios实例
const service = axios.create({
  baseURL: 'http://127.0.0.1:8000',
  timeout: 10000
})

// 创建专门用于文件上传的axios实例
const uploadService = axios.create({
  baseURL: 'http://127.0.0.1:8000',
  timeout: 300000 // 5分钟超时
})

// 请求拦截器
const requestInterceptor = (config) => {
  const authStore = useAuthStore()
  if (authStore.token) {
    config.headers['Authorization'] = `Bearer ${authStore.token}`
  }
  return config
}

const requestErrorInterceptor = (error) => {
  console.error('请求错误:', error)
  return Promise.reject(error)
}

service.interceptors.request.use(requestInterceptor, requestErrorInterceptor)
uploadService.interceptors.request.use(requestInterceptor, requestErrorInterceptor)

// 响应拦截器
const responseInterceptor = (response) => {
  const res = response.data
  
  // 如果返回的状态码不是200，说明接口有问题，应该提示错误
  if (res.code !== 200) {
    ElMessage.error(res.message || '请求失败')
    
    // 401: 未登录或token过期
    if (res.code === 401) {
      const authStore = useAuthStore()
      authStore.logout()
      window.location.href = '/login'
    }
    
    return Promise.reject(new Error(res.message || '请求失败'))
  } else {
    return res
  }
}

const responseErrorInterceptor = (error) => {
  console.error('响应错误:', error)
  
  if (error.response) {
    const { status, data } = error.response
    
    switch (status) {
      case 401:
        ElMessage.error('登录已过期，请重新登录')
        const authStore = useAuthStore()
        authStore.logout()
        window.location.href = '/login'
        break
      case 403:
        ElMessage.error('没有权限访问')
        break
      case 404:
        ElMessage.error('请求的资源不存在')
        break
      case 500:
        ElMessage.error('服务器内部错误')
        break
      default:
        ElMessage.error(data?.message || '请求失败')
    }
  } else {
    ElMessage.error('网络错误，请检查网络连接')
  }
  
  return Promise.reject(error)
}

service.interceptors.response.use(responseInterceptor, responseErrorInterceptor)
uploadService.interceptors.response.use(responseInterceptor, responseErrorInterceptor)

export default service
export { uploadService }
