import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginPage.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/dashboard'
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '党建数据' }
      },
      {
        path: 'members',
        name: 'MemberManagement',
        component: () => import('@/views/MemberManagement.vue'),
        meta: { title: '成员管理' }
      },
      {
        path: 'tasks',
        name: 'TaskManagement',
        component: () => import('@/views/TaskManagement.vue'),
        meta: { title: '任务管理' }
      },
      {
        path: 'announcements',
        name: 'AnnouncementManagement',
        component: () => import('@/views/AnnouncementManagement.vue'),
        meta: { title: '公告管理' }
      },
      {
        path: 'applications',
        name: 'ApplicationReview',
        component: () => import('@/views/ApplicationReview.vue'),
        meta: { title: '申请审核' }
      },
      {
        path: 'resources',
        name: 'StudyResourceManagement',
        component: () => import('@/views/StudyResourceManagement.vue'),
        meta: { title: '学习资源管理' }
      },
      {
        path: 'progress',
        name: 'StudyProgressManagement',
        component: () => import('@/views/StudyProgressManagement.vue'),
        meta: { title: '学习情况管理' }
      },
      {
        path: 'account',
        name: 'AccountManagement',
        component: () => import('@/views/AccountManagement.vue'),
        meta: { title: '账号管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  // 如果访问登录页面且已登录，重定向到首页
  if (to.path === '/login' && authStore.isLoggedIn) {
    next('/dashboard')
    return
  }
  
  // 如果需要认证但未登录，重定向到登录页
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next('/login')
    return
  }
  
  // 其他情况正常跳转
  next()
})

export default router
