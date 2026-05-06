<template>
  <div class="dashboard">
    <div class="welcome-section">
      <h1>党建数据概览</h1>
      <p>当前用户：{{ userInfo.realName || userInfo.username }}</p>
    </div>
    
    <div class="stats-grid">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ overview.memberCount }}</div>
            <div class="stat-label">成员总数</div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ overview.pendingApplications }}</div>
            <div class="stat-label">待审核申请</div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ overview.ongoingActivities }}</div>
            <div class="stat-label">发布中任务</div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon">
            <el-icon><List /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ overview.pendingAssignments }}</div>
            <div class="stat-label">待完结任务</div>
          </div>
        </div>
      </el-card>
    </div>
    
    <div class="quick-actions">
      <h2>快速操作</h2>
      <div class="action-buttons">
        <el-button type="primary" @click="goToMembers">
          <el-icon><User /></el-icon>
          成员管理
        </el-button>
        <el-button type="success" @click="goToApplications">
          <el-icon><Document /></el-icon>
          申请审核
        </el-button>
        <el-button type="warning" @click="goToAnnouncements">
          <el-icon><Bell /></el-icon>
          公告管理
        </el-button>
        <el-button type="info" @click="goToTasks">
          <el-icon><List /></el-icon>
          任务管理
        </el-button>
      </div>
    </div>
    <div class="branch-durations">
      <h2>{{ isSystemAdmin ? '各支部学习总时长（分钟）' : '本支部学习总时长（分钟）' }}</h2>
      <el-table :data="branchRows" style="width: 100%" size="small">
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

const userInfo = computed(() => authStore.userInfo)

// 检查是否为超级管理员
const isSystemAdmin = computed(() => {
  return userInfo.value?.userType === 'SYSTEM_ADMIN'
})

// 检查是否为支部管理员
const isBranchAdmin = computed(() => {
  return userInfo.value?.userType === 'BRANCH_ADMIN'
})

const overview = ref({ memberCount: 0, pendingApplications: 0, ongoingActivities: 0, pendingAssignments: 0 })
const branchRows = ref([])

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

const goToMembers = () => router.push('/members')
const goToApplications = () => router.push('/applications')
const goToAnnouncements = () => router.push('/announcements')
const goToTasks = () => router.push('/tasks')
onMounted(loadData)
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.welcome-section {
  text-align: center;
  margin-bottom: 40px;
  padding: 40px;
  /* 使用自然的海洋/天空蓝渐变，清新且专业 */
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  border-radius: 12px;
  color: white;
}

.welcome-section h1 {
  font-size: 32px;
  margin-bottom: 10px;
  font-weight: 600;
}

.welcome-section p {
  font-size: 16px;
  opacity: 0.9;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.stat-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  /* 使用 Element Plus 默认的标准蓝色 */
  background: #409EFF;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.quick-actions {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.quick-actions h2 {
  margin-bottom: 20px;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  font-size: 14px;
  border-radius: 8px;
}

.branch-durations {
  margin-top: 30px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .action-buttons .el-button {
    width: 100%;
  }
}
</style>
