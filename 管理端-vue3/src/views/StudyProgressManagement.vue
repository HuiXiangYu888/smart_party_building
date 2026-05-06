<template>
  <div class="study-progress-management">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <div class="search-bar">
        <el-input
          v-model="searchForm.keyword"
          placeholder="请输入姓名或学号搜索"
          style="width: 300px"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
        
        <el-button type="danger" @click="handleClearAll">
          <el-icon><Delete /></el-icon>
          一键清空
        </el-button>
      </div>
    </el-card>
    
    <!-- 学习进度列表 -->
    <el-card class="table-card">
      <el-table
        :data="progressList"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="memberName" label="姓名" width="120" />
        <el-table-column prop="studentId" label="学号" width="120" />
        <el-table-column prop="totalDuration" label="学习总时长" width="120">
          <template #default="{ row }">
            {{ formatDuration(row.totalDuration) }}
          </template>
        </el-table-column>
        <el-table-column prop="studyCount" label="学习次数" width="100" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看详情</el-button>
            <el-button size="small" type="danger" @click="handleClearUser(row)">清空</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="学习记录详情"
      width="800px"
    >
      <div v-if="currentProgress" class="progress-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ currentProgress.memberName }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentProgress.studentId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="学习总时长">{{ formatDuration(currentProgress.totalDuration) }}</el-descriptions-item>
          <el-descriptions-item label="学习次数">{{ currentProgress.studyCount }}次</el-descriptions-item>
        </el-descriptions>
        
        <div class="detail-section">
          <h4>学习记录列表</h4>
          <el-table :data="studyRecords" style="width: 100%" max-height="400">
            <el-table-column prop="title" label="视频标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="startedAt" label="开始时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.startedAt) }}
              </template>
            </el-table-column>
            <el-table-column prop="endedAt" label="结束时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.endedAt) }}
              </template>
            </el-table-column>
            <el-table-column prop="duration" label="学习时长" width="120">
              <template #default="{ row }">
                {{ formatDuration(row.duration) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
    
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const viewDialogVisible = ref(false)
const currentProgress = ref(null)
const studyRecords = ref([])

const searchForm = reactive({
  keyword: ''
})

const progressList = ref([])

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 格式化时长
const formatDuration = (minutes) => {
  if (!minutes || minutes === 0) return '0分钟'
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours > 0) {
    return `${hours}小时${mins}分钟`
  }
  return `${mins}分钟`
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  try {
    const date = new Date(timeStr)
    return date.toLocaleString('zh-CN')
  } catch {
    return timeStr
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.keyword = ''
  handleSearch()
}

const handleView = async (row) => {
  currentProgress.value = row
  viewDialogVisible.value = true
  
  // 加载该用户的学习记录
  try {
    const res = await request({ 
      url: `/study/records/user/${row.userId}`, 
      method: 'GET' 
    })
    if (res.code === 200) {
      studyRecords.value = res.data || []
    }
  } catch (e) {
    ElMessage.error('加载学习记录失败')
  }
}

const handleClearUser = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要清空 ${row.memberName} 的学习记录吗？`, '确认清空', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request({ 
      url: `/study/records/admin/user/${row.userId}`, 
      method: 'DELETE' 
    })
    ElMessage.success('清空成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('清空失败')
    }
  }
}

const handleClearAll = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有学习记录吗？此操作不可恢复！', '确认清空', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request({ 
      url: '/study/records/admin/all', 
      method: 'DELETE' 
    })
    ElMessage.success('清空成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('清空失败')
    }
  }
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadData()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadData()
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.size
    }
    if (searchForm.keyword) {
      params.keyword = searchForm.keyword
    }
    
    const res = await request({ 
      url: '/study/records/admin/stats', 
      method: 'GET',
      params 
    })
    if (res.code === 200) {
      progressList.value = res.data || []
      pagination.total = res.data?.length || 0
    }
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.study-progress-management {
  padding: 0;
}

.search-card {
  margin-bottom: 20px;
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
}

.search-bar {
  display: flex;
  gap: 15px;
  align-items: center;
  flex-wrap: wrap;
}

.table-card {
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.progress-detail {
  padding: 20px 0;
}

.detail-section {
  margin-top: 20px;
}

.detail-section h4 {
  margin-bottom: 15px;
  color: #303133;
}

@media (max-width: 768px) {
  .search-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-bar .el-input,
  .search-bar .el-select {
    width: 100% !important;
  }
}
</style>
