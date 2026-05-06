<template>
  <div class="flex h-screen bg-[var(--color-bg-page)] transition-colors duration-300">
    <!-- Sidebar -->
    <aside
      class="flex flex-col transition-all duration-300 bg-[var(--color-bg-sidebar)]"
      :class="isCollapsed ? 'w-16' : 'w-60'"
    >
      <!-- Logo -->
      <div class="flex items-center justify-center h-14 bg-slate-950/40 text-white text-base font-semibold shrink-0 overflow-hidden">
        <span v-if="!isCollapsed">智慧党建管理端</span>
        <span v-else>党建</span>
      </div>

      <!-- Menu -->
      <el-menu
        :default-active="activeMenu"
        class="!border-none flex-1 overflow-y-auto"
        :collapse="isCollapsed"
        background-color="transparent"
        text-color="#94a3b8"
        active-text-color="#818cf8"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>党建数据</span>
        </el-menu-item>

        <el-menu-item index="/members">
          <el-icon><User /></el-icon>
          <span>成员管理</span>
        </el-menu-item>

        <el-menu-item index="/tasks">
          <el-icon><List /></el-icon>
          <span>任务管理</span>
        </el-menu-item>

        <el-menu-item index="/announcements">
          <el-icon><Bell /></el-icon>
          <span>公告管理</span>
        </el-menu-item>

        <el-menu-item index="/applications">
          <el-icon><Document /></el-icon>
          <span>申请审核</span>
        </el-menu-item>

        <el-menu-item index="/resources">
          <el-icon><Files /></el-icon>
          <span>学习资源管理</span>
        </el-menu-item>

        <el-menu-item index="/progress">
          <el-icon><TrendCharts /></el-icon>
          <span>学习情况管理</span>
        </el-menu-item>

        <el-menu-item v-if="authStore.isSystemAdmin" index="/account">
          <el-icon><Setting /></el-icon>
          <span>账号管理</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <!-- Main -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <!-- Top Nav -->
      <header class="h-14 bg-[var(--color-bg-card)] border-b border-[var(--color-border)] flex items-center justify-between px-5 shrink-0 transition-colors duration-300">
        <div class="flex items-center gap-4">
          <button
            @click="toggleSidebar"
            class="text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)] transition-colors p-1 rounded"
          >
            <el-icon :size="20"><Fold v-if="!isCollapsed" /><Expand v-else /></el-icon>
          </button>
          <span class="text-lg font-semibold text-[var(--color-text-primary)]">{{ currentPageTitle }}</span>
        </div>

        <div class="flex items-center gap-3">
          <!-- Dark Mode Toggle -->
          <button
            @click="toggleDark"
            class="w-9 h-9 flex items-center justify-center rounded-lg text-[var(--color-text-secondary)] hover:bg-[var(--color-border-light)] hover:text-[var(--color-text-primary)] transition-all duration-200"
            :title="isDark ? '切换浅色模式' : '切换深色模式'"
          >
            <el-icon :size="18">
              <Sunny v-if="isDark" />
              <Moon v-else />
            </el-icon>
          </button>

          <!-- User Dropdown -->
          <el-dropdown @command="handleCommand">
            <div class="flex items-center gap-2 cursor-pointer px-3 py-2 rounded-lg hover:bg-[var(--color-border-light)] transition-colors">
              <el-avatar :size="28">
                {{ authStore.displayName.charAt(0) }}
              </el-avatar>
              <span class="text-sm text-[var(--color-text-secondary)]">{{ authStore.displayName }}</span>
              <el-icon class="text-[var(--color-text-muted)]"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- Content -->
      <main class="flex-1 p-5 overflow-y-auto bg-[var(--color-bg-page)] transition-colors duration-300">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// Dark mode from App.vue
const isDark = inject('isDark')
const toggleDark = inject('toggleDark')

// Sidebar
const isCollapsed = ref(false)
const activeMenu = computed(() => route.path)
const currentPageTitle = computed(() => route.meta?.title || '智慧党建管理端')

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

// User dropdown
const handleCommand = async (command) => {
  if (command === 'profile') {
    router.push('/account')
  } else if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      authStore.logout()
      router.push('/login')
    } catch {
      // User cancelled
    }
  }
}
</script>

<style scoped>
/* Sidebar menu item hover */
:deep(.el-menu-item) {
  margin: 2px 8px;
  border-radius: 8px;
  transition: all 0.2s;
}

:deep(.el-menu-item:hover) {
  background-color: var(--color-bg-sidebar-hover) !important;
}

:deep(.el-menu-item.is-active) {
  background-color: rgba(99, 102, 241, 0.15) !important;
}

/* Collapsed menu */
:deep(.el-menu--collapse) {
  width: 64px;
}
</style>
