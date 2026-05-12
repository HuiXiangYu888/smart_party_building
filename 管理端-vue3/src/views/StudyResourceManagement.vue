<template>
  <div class="space-y-5">
    <!-- Action Bar -->
    <div class="page-card">
      <div class="search-toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon class="mr-1"><Plus /></el-icon> 添加资源
        </el-button>
        <el-button type="danger" @click="handleClearAll">
          <el-icon class="mr-1"><Delete /></el-icon> 一键清空
        </el-button>
      </div>
    </div>

    <!-- Table -->
    <div class="page-card">
      <el-table :data="resourceList" v-loading="loading" stripe class="w-full">
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

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="s => { pagination.size = s; loadData() }"
          @current-change="c => { pagination.current = c; loadData() }"
        />
      </div>
    </div>

    <!-- Upload Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="620px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入资源标题" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择资源类型" class="w-full">
            <el-option label="视频" value="VIDEO" />
          </el-select>
        </el-form-item>
        <el-form-item label="文件" prop="file">
          <el-upload action="#" :auto-upload="false" :on-change="handleFileChange" :file-list="fileList" :limit="1" accept=".mp4,.avi,.mov,.wmv,.flv,.webm" :before-upload="beforeUpload">
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="text-xs text-[var(--color-text-muted)] mt-1">支持mp4/avi/mov等视频格式，最大2GB</div>
            </template>
          </el-upload>

          <!-- Upload Progress -->
          <div v-if="uploadProgress.show" class="mt-4 p-4 rounded-lg bg-[var(--color-border-light)] border border-[var(--color-border)]">
            <div class="flex justify-between items-center mb-2 text-sm text-[var(--color-text-secondary)]">
              <span>正在上传: {{ uploadProgress.fileName }}</span>
              <span>{{ uploadProgress.percentage }}%</span>
            </div>
            <el-progress :percentage="uploadProgress.percentage" :status="uploadProgress.status" :stroke-width="8" />
            <div v-if="uploadProgress.status === 'success'" class="flex items-center gap-2 mt-2 text-sm text-green-600">
              <el-icon><Check /></el-icon> 上传成功
            </div>
            <div v-if="uploadProgress.status === 'exception'" class="flex items-center gap-2 mt-2 text-sm text-red-500">
              <el-icon><Close /></el-icon> 上传失败: {{ uploadProgress.errorMessage }}
            </div>
          </div>
        </el-form-item>
        <el-form-item label="封面图" prop="cover">
          <el-upload 
            action="#" 
            :auto-upload="false" 
            :on-change="handleCoverChange" 
            :on-remove="handleCoverRemove"
            :file-list="coverList" 
            list-type="picture-card"
            :limit="1"
            :class="{ 'hide-upload': coverList.length >= 1 }"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request, { uploadService } from '@/utils/request'

const loading = ref(false)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const fileList = ref([])
const coverList = ref([])
const uploadProgress = reactive({ show: false, percentage: 0, fileName: '', status: '', errorMessage: '' })

const form = reactive({ id: null, title: '', type: '', file: null, cover: null })
const rules = {
  title: [{ required: true, message: '请输入资源标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择资源类型', trigger: 'change' }]
}

const resourceList = ref([])
const pagination = reactive({ current: 1, size: 10, total: 0 })
const dialogTitle = ref('添加资源')

const handleAdd = () => { dialogTitle.value = '添加资源'; resetForm(); dialogVisible.value = true }

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该资源吗？', '提示', { type: 'warning' })
    await request({ url: '/study/resources/' + row.id, method: 'DELETE' })
    ElMessage.success('删除成功')
    await loadData()
  } catch {}
}

const handleFileChange = (file) => {
  form.file = file.raw
  Object.assign(uploadProgress, { show: false, percentage: 0, status: '', errorMessage: '' })
}

const beforeUpload = (file) => {
  if (file.size > 2 * 1024 * 1024 * 1024) { ElMessage.error('文件大小不能超过2GB'); return false }
  const allowedTypes = ['video/mp4', 'video/avi', 'video/quicktime', 'video/x-ms-wmv', 'video/x-flv', 'video/webm']
  if (!allowedTypes.includes(file.type)) { ElMessage.error('只支持视频格式文件'); return false }
  return true
}

const handleCoverChange = (file, list) => { 
  form.cover = file.raw
  coverList.value = list
}
const handleCoverRemove = (file, list) => { 
  form.cover = null
  coverList.value = list
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitLoading.value = true
    Object.assign(uploadProgress, { show: true, fileName: form.file?.name || '未知文件', percentage: 0, status: '', errorMessage: '' })
    const fd = new FormData()
    fd.append('title', form.title)
    fd.append('type', form.type)
    if (form.file) fd.append('file', form.file)
    if (form.cover) fd.append('cover', form.cover)
    const progressInterval = setInterval(() => { if (uploadProgress.percentage < 90) uploadProgress.percentage += Math.random() * 10 }, 200)
    try {
      await uploadService({ url: '/study/resources/upload', method: 'POST', data: fd })
      clearInterval(progressInterval)
      uploadProgress.percentage = 100
      uploadProgress.status = 'success'
      setTimeout(() => { ElMessage.success('上传成功'); dialogVisible.value = false; loadData() }, 1000)
    } catch (error) {
      clearInterval(progressInterval)
      uploadProgress.status = 'exception'
      uploadProgress.errorMessage = error.response?.data?.message || error.message || '上传失败'
    }
  } catch { uploadProgress.status = 'exception'; uploadProgress.errorMessage = '表单验证失败' }
  finally { submitLoading.value = false }
}

const resetForm = () => {
  Object.assign(form, { id: null, title: '', type: '', file: null, cover: null })
  fileList.value = []; coverList.value = []; formRef.value?.clearValidate()
  Object.assign(uploadProgress, { show: false, percentage: 0, fileName: '', status: '', errorMessage: '' })
}

const handleClearAll = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有学习资源吗？此操作不可恢复！', '确认清空', { type: 'warning' })
    await request({ url: '/study/resources/clear-all', method: 'DELETE' })
    ElMessage.success('清空成功'); loadData()
  } catch (e) { if (e !== 'cancel') ElMessage.error('清空失败') }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/study/resources', method: 'GET' })
    if (res.code === 200) { resourceList.value = res.data || []; pagination.total = resourceList.value.length }
  } finally { loading.value = false }
}

onMounted(loadData)
</script>

<style scoped>
:deep(.hide-upload .el-upload--picture-card) {
  display: none !important;
}
</style>
