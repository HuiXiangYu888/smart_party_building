<template>
  <div class="study-resource-management">
    <!-- 操作栏 -->
    <el-card class="action-card">
      <div class="action-bar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加资源
        </el-button>
        
        
        <el-button type="danger" @click="handleClearAll">
          <el-icon><Delete /></el-icon>
          一键清空
        </el-button>
      </div>
    </el-card>
    
    <!-- 资源列表 -->
    <el-card class="table-card">
      <el-table
        :data="resourceList"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column prop="createdBy" label="上传者" width="140" />
        <el-table-column prop="createdAt" label="上传时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
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
      width="620px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入资源标题" />
        </el-form-item>
        
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择资源类型" style="width: 100%">
            <el-option label="视频" value="VIDEO" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="文件" prop="file">
          <el-upload
            class="upload-demo"
            action="#"
            :auto-upload="false"
            :on-change="handleFileChange"
            :file-list="fileList"
            :limit="1"
            accept=".mp4,.avi,.mov,.wmv,.flv,.webm"
            :before-upload="beforeUpload"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持mp4/avi/mov/wmv/flv/webm等视频格式文件，最大支持2GB
              </div>
            </template>
          </el-upload>
          
          <!-- 上传进度显示 -->
          <div v-if="uploadProgress.show" class="upload-progress">
            <div class="progress-info">
              <span>正在上传: {{ uploadProgress.fileName }}</span>
              <span>{{ uploadProgress.percentage }}%</span>
            </div>
            <el-progress 
              :percentage="uploadProgress.percentage" 
              :status="uploadProgress.status"
              :stroke-width="8"
            />
            <div v-if="uploadProgress.status === 'success'" class="upload-success">
              <el-icon><Check /></el-icon>
              上传成功
            </div>
            <div v-if="uploadProgress.status === 'exception'" class="upload-error">
              <el-icon><Close /></el-icon>
              上传失败: {{ uploadProgress.errorMessage }}
            </div>
          </div>
        </el-form-item>
        
        <el-form-item label="封面图" prop="cover">
          <el-upload
            class="upload-demo"
            action="#"
            :auto-upload="false"
            :on-change="handleCoverChange"
            :file-list="coverList"
            list-type="picture-card"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Check, Close, Delete } from '@element-plus/icons-vue'
import request, { uploadService } from '@/utils/request'

const loading = ref(false)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const fileList = ref([])
const coverList = ref([])

// 上传进度状态
const uploadProgress = reactive({
  show: false,
  percentage: 0,
  fileName: '',
  status: '', // 'success', 'exception', ''
  errorMessage: ''
})

const form = reactive({
  id: null,
  title: '',
  type: '',
  file: null,
  cover: null
})

const rules = {
  title: [
    { required: true, message: '请输入资源标题', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择资源类型', trigger: 'change' }
  ]
}

const resourceList = ref([])

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const dialogTitle = ref('添加资源')

const handleAdd = () => {
  dialogTitle.value = '添加资源'
  resetForm()
  dialogVisible.value = true
}

// 精简操作：仅上传/删除

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该资源吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request({ url: '/study/resources/' + row.id, method: 'DELETE' })
    ElMessage.success('删除成功')
    await loadData()
  } catch {
    // 用户取消
  }
}

const handleFileChange = (file) => {
  form.file = file.raw
  // 重置上传进度
  uploadProgress.show = false
  uploadProgress.percentage = 0
  uploadProgress.status = ''
  uploadProgress.errorMessage = ''
}

const beforeUpload = (file) => {
  // 检查文件大小 (2GB = 2 * 1024 * 1024 * 1024 bytes)
  const maxSize = 2 * 1024 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过2GB')
    return false
  }
  
  // 检查文件类型
  const allowedTypes = ['video/mp4', 'video/avi', 'video/quicktime', 'video/x-ms-wmv', 'video/x-flv', 'video/webm']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('只支持视频格式文件')
    return false
  }
  
  return true
}

const handleCoverChange = (file) => {
  form.cover = file.raw
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    // 显示上传进度
    uploadProgress.show = true
    uploadProgress.fileName = form.file?.name || '未知文件'
    uploadProgress.percentage = 0
    uploadProgress.status = ''
    uploadProgress.errorMessage = ''
    
    const fd = new FormData()
    fd.append('title', form.title)
    fd.append('type', form.type)
    if (form.file) fd.append('file', form.file)
    if (form.cover) fd.append('cover', form.cover)
    
    // 模拟上传进度
    const progressInterval = setInterval(() => {
      if (uploadProgress.percentage < 90) {
        uploadProgress.percentage += Math.random() * 10
      }
    }, 200)
    
    try {
      const response = await uploadService({ 
        url: '/study/resources/upload', 
        method: 'POST', 
        data: fd
      })
      
      clearInterval(progressInterval)
      uploadProgress.percentage = 100
      uploadProgress.status = 'success'
      
      setTimeout(() => {
        ElMessage.success('上传成功')
        dialogVisible.value = false
        loadData()
      }, 1000)
      
    } catch (error) {
      clearInterval(progressInterval)
      uploadProgress.status = 'exception'
      uploadProgress.errorMessage = error.response?.data?.message || error.message || '上传失败'
      ElMessage.error('上传失败: ' + uploadProgress.errorMessage)
    }
    
  } catch (error) {
    console.error('表单验证失败:', error)
    uploadProgress.status = 'exception'
    uploadProgress.errorMessage = '表单验证失败'
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
    type: '',
    file: null,
    cover: null
  })
  fileList.value = []
  coverList.value = []
  formRef.value?.clearValidate()
  
  // 重置上传进度
  uploadProgress.show = false
  uploadProgress.percentage = 0
  uploadProgress.fileName = ''
  uploadProgress.status = ''
  uploadProgress.errorMessage = ''
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadData()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadData()
}


// 清空所有资源
const handleClearAll = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有学习资源吗？此操作不可恢复！', '确认清空', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request({ 
      url: '/study/resources/clear-all', 
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

const loadData = async () => {
  loading.value = true
  try {
    const res = await request({ 
      url: '/study/resources', 
      method: 'GET'
    })
    if (res.code === 200) {
      resourceList.value = res.data || []
      pagination.total = resourceList.value.length
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.study-resource-management {
  padding: 0;
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

/* 上传进度样式 */
.upload-progress {
  margin-top: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 14px;
  color: #666;
}

.upload-success {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
  color: #67c23a;
  font-size: 14px;
}

.upload-error {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
  color: #f56c6c;
  font-size: 14px;
}
</style>
