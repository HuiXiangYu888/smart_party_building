<template>
  <div class="space-y-6">
    <!-- Welcome -->
    <div class="relative overflow-hidden rounded-xl bg-gradient-to-r from-indigo-600 to-violet-600 p-8 text-white">
      <div class="absolute -top-10 -right-10 w-40 h-40 bg-white/10 rounded-full blur-2xl"></div>
      <h1 class="text-2xl font-bold mb-1">党建数据概览</h1>
      <p class="text-indigo-100 text-sm">当前用户：{{ authStore.displayName }}</p>
    </div>

    <!-- Stats Grid -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <div v-for="(stat, i) in stats" :key="i" class="page-card p-5 flex items-center gap-4">
        <div class="w-12 h-12 rounded-xl flex items-center justify-center text-white text-xl shrink-0" :class="stat.gradient">
          <el-icon><component :is="stat.icon" /></el-icon>
        </div>
        <div>
          <div class="text-2xl font-bold text-[var(--color-text-primary)]">{{ stat.value }}</div>
          <div class="text-sm text-[var(--color-text-secondary)]">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="page-card p-6">
      <h2 class="section-title">快速操作</h2>
      <div class="flex flex-wrap gap-3">
        <el-button type="primary" @click="router.push('/members')">
          <el-icon class="mr-1"><User /></el-icon> 成员管理
        </el-button>
        <el-button type="success" @click="router.push('/applications')">
          <el-icon class="mr-1"><Document /></el-icon> 申请审核
        </el-button>
        <el-button type="warning" @click="router.push('/announcements')">
          <el-icon class="mr-1"><Bell /></el-icon> 公告管理
        </el-button>
        <el-button @click="router.push('/tasks')">
          <el-icon class="mr-1"><List /></el-icon> 任务管理
        </el-button>
      </div>
    </div>

    <!-- Branch Study Durations -->
    <div class="page-card p-6">
      <h2 class="section-title">{{ authStore.isSystemAdmin ? '各支部学习总时长（分钟）' : '本支部学习总时长（分钟）' }}</h2>
      <el-table :data="branchRows" class="w-full" size="small">
        <el-table-column prop="branchName" label="党支部" min-width="220" />
        <el-table-column prop="totalDuration" label="总时长(分钟)" width="180" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import request from '@/utils/request'

const router = useRouter()
const authStore = useAuthStore()

const overview = ref({ memberCount: 0, pendingApplications: 0, ongoingActivities: 0, pendingAssignments: 0 })
const branchRows = ref([])

const stats = computed(() => [
  { label: '成员总数',   value: overview.value.memberCount,        icon: 'User',     gradient: 'stat-gradient-indigo' },
  { label: '待审核申请', value: overview.value.pendingApplications, icon: 'Document', gradient: 'stat-gradient-emerald' },
  { label: '发布中任务', value: overview.value.ongoingActivities,   icon: 'Calendar', gradient: 'stat-gradient-amber' },
  { label: '待完结任务', value: overview.value.pendingAssignments,  icon: 'List',     gradient: 'stat-gradient-sky' },
])

const loadData = async () => {
  try {
    const o = await request.get('/stats/overview')
    if (o.code === 200) overview.value = o.data || overview.value
  } catch {}
  try {
    const b = await request.get('/stats/branch-study-durations')
    if (b.code === 200) branchRows.value = b.data || []
  } catch {}
}

onMounted(loadData)
</script>
