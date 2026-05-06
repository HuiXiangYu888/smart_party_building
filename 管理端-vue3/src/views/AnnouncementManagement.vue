<template>
  <div class="announcement-management">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <div class="search-bar">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索公告标题或内容"
          style="width: 300px"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </el-card>
    
    <!-- 操作栏 -->
    <el-card class="action-card">
      <div class="action-bar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          发布公告
        </el-button>
      </div>
    </el-card>
    
    <!-- 公告列表 -->
    <el-card class="table-card">
      <el-table
        :data="announcementList"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="branchName" label="所属支部" width="150" />
        <el-table-column prop="createdByName" label="发布人" width="120" />
        <el-table-column prop="createdAt" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
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
    
    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        
        <el-form-item label="所属支部" prop="branchId">
          <el-select v-model="form.branchId" placeholder="请选择所属支部" style="width: 100%">
            <el-option 
              v-for="branch in branchList" 
              :key="branch.id" 
              :label="branch.name" 
              :value="branch.id" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            placeholder="请输入公告内容"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 查看对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="公告详情"
      width="800px"
    >
      <div v-if="currentAnnouncement" class="announcement-detail">
        <h3>{{ currentAnnouncement.title }}</h3>
        <div class="announcement-meta">
          <span>发布人：{{ currentAnnouncement.createdByName }}</span>
          <span>发布时间：{{ formatTime(currentAnnouncement.createdAt) }}</span>
          <span>所属支部：{{ currentAnnouncement.branchName }}</span>
        </div>
        <div class="announcement-content">
          {{ currentAnnouncement.content }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const currentAnnouncement = ref(null)

const searchForm = reactive({
  keyword: ''
})

const form = reactive({
  id: null,
  title: '',
  branchId: null,
  content: ''
})

const rules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' }
  ],
  branchId: [
    { required: true, message: '请选择所属支部', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' }
  ]
}

const announcementList = ref([])
const branchList = ref([])

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const dialogTitle = ref('发布公告')

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

const handleAdd = () => {
  dialogTitle.value = '发布公告'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑公告'
  Object.assign(form, {
    id: row.id,
    title: row.title,
    branchId: row.branchId,
    content: row.content
  })
  dialogVisible.value = true
}

const handleView = (row) => {
  currentAnnouncement.value = row
  viewDialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该公告吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request({ 
      url: `/announcements/${row.id}`, 
      method: 'DELETE' 
    })
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    if (form.id) {
      // 编辑
      await request({ 
        url: `/announcements/${form.id}`, 
        method: 'PUT', 
        data: form 
      })
      ElMessage.success('编辑成功')
    } else {
      // 新增
      await request({ 
        url: '/announcements', 
        method: 'POST', 
        data: form 
      })
      ElMessage.success('发布成功')
    }
    
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(form.id ? '编辑失败' : '发布失败')
  } finally {
    submitLoading.value = false
  }
}

const handleDialogClose = () => {
  resetForm()
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    title: '',
    branchId: null,
    content: ''
  })
  formRef.value?.clearValidate()
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadData()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadData()
}

// 加载支部列表
const loadBranches = async () => {
  try {
    const res = await request({ 
      url: '/announcements/branches', 
      method: 'GET' 
    })
    console.log('支部列表API响应:', res)
    if (res.code === 200) {
      branchList.value = res.data || []
      console.log('加载到的支部列表:', branchList.value)
    } else {
      console.error('获取支部列表失败:', res.message)
      ElMessage.error('获取支部列表失败: ' + (res.message || '未知错误'))
    }
  } catch (e) {
    console.error('加载支部列表失败:', e)
    ElMessage.error('加载支部列表失败: ' + e.message)
  }
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
      url: '/announcements', 
      method: 'GET',
      params 
    })
    console.log('公告列表API响应:', res)
    if (res.code === 200) {
      announcementList.value = res.data || []
      pagination.total = res.data?.length || 0
      console.log('加载到的公告列表:', announcementList.value)
    } else {
      console.error('获取公告列表失败:', res.message)
      ElMessage.error('获取公告列表失败: ' + (res.message || '未知错误'))
    }
  } catch (e) {
    console.error('加载数据失败:', e)
    ElMessage.error('加载数据失败: ' + e.message)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadBranches()
  loadData()
})
</script>

<style scoped>
.announcement-management {
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

.action-card {
  margin-bottom: 20px;
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
}

.action-bar {
  display: flex;
  gap: 15px;
  align-items: center;
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

.announcement-detail {
  padding: 20px 0;
}

.announcement-detail h3 {
  margin-bottom: 15px;
  color: #303133;
  font-size: 18px;
}

.announcement-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  color: #909399;
  font-size: 14px;
  flex-wrap: wrap;
}

.announcement-content {
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
}

@media (max-width: 768px) {
  .search-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-bar .el-input {
    width: 100% !important;
  }
}
</style>
