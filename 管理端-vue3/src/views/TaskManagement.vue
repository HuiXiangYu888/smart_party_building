<template>
  <div class="task-management">
    <!-- 操作栏 -->
    <el-card class="action-card">
      <div class="action-bar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          发布任务
        </el-button>
      </div>
    </el-card>
    
    <!-- 任务列表 -->
    <el-card class="table-card">
      <el-table
        :data="taskList"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="任务标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="points" label="积分" width="100" />
        <el-table-column prop="capacity" label="人数上限" width="120" />
        <el-table-column prop="dueDate" label="截止时间" width="180" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ translateStatus(row.status) }}
            </el-tag>
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
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="任务标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入任务标题" />
        </el-form-item>
        
        <el-form-item label="截止时间" prop="dueDate">
          <el-date-picker
            v-model="form.dueDate"
            type="datetime"
            placeholder="选择截止时间"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="任务描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入任务描述"
          />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="PUBLISHING">发布中</el-radio>
            <el-radio label="PENDING_END">待完结</el-radio>
            <el-radio label="ENDED">已完结</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="人数上限" prop="capacity">
          <el-input v-model.number="form.capacity" placeholder="请输入人数上限" type="number" />
        </el-form-item>
        <el-form-item label="积分" prop="points">
          <el-input v-model.number="form.points" placeholder="请输入积分" type="number" />
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
      title="任务详情"
      width="800px"
    >
      <div v-if="currentTask" class="task-detail">
        <h3>{{ currentTask.title }}</h3>
        <div class="task-meta">
          <span>积分：{{ currentTask.points }}</span>
          <span>截止时间：{{ currentTask.dueDate }}</span>
          <span v-if="currentTask.capacity != null">人数上限：{{ currentTask.capacity }}</span>
        </div>
        <div class="task-status">
          <el-tag :type="getStatusType(currentTask.status)">
            {{ translateStatus(currentTask.status) }}
          </el-tag>
        </div>
        <div class="task-description">
          <h4>任务描述</h4>
          <p>{{ currentTask.description }}</p>
        </div>

        <div class="task-participants">
          <h4>参与人列表</h4>
          <el-table :data="participants" size="small" style="width: 100%" v-loading="participantsLoading">
            <el-table-column prop="name" label="姓名" width="140" />
            <el-table-column prop="studentId" label="学号" width="160" />
            <el-table-column prop="mobile" label="电话号码" min-width="160" />
            <el-table-column prop="assigned_at" label="报名时间" min-width="180" />
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { taskAPI } from '@/api/task'

const loading = ref(false)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const currentTask = ref(null)
const participants = ref([])
const participantsLoading = ref(false)

const form = reactive({
  id: null,
  title: '',
  description: '',
  dueDate: '',
  status: 'PUBLISHING',
  points: 0,
  capacity: null
})

const rules = {
  title: [
    { required: true, message: '请输入任务标题', trigger: 'blur' }
  ],
  dueDate: [
    { required: true, message: '请选择截止时间', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入任务描述', trigger: 'blur' }
  ]
}

const taskList = ref([])

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const dialogTitle = ref('发布任务')

const getStatusType = (status) => {
  switch (status) {
    case 'ENDED':
      return 'success'
    case 'PENDING_END':
      return 'warning'
    case 'PUBLISHING':
      return 'info'
    default:
      return 'info'
  }
}

const translateStatus = (status) => {
  switch (status) {
    case 'PUBLISHING':
      return '发布中'
    case 'PENDING_END':
      return '待完结'
    case 'ENDED':
      return '已完结'
    default:
      return status || ''
  }
}

const handleAdd = () => {
  dialogTitle.value = '发布任务'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑任务'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleView = (row) => {
  currentTask.value = row
  viewDialogVisible.value = true
  loadParticipants(row.id)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await taskAPI.remove(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch {}
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitLoading.value = true

    if (form.id) {
      await taskAPI.update(form.id, form)
    } else {
      await taskAPI.create(form)
    }
    ElMessage.success(form.id ? '编辑成功' : '发布成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('表单验证失败:', error)
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
    description: '',
    dueDate: '',
    status: 'PUBLISHING',
    points: 0,
    capacity: null
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

const loadData = () => {
  loading.value = true
  taskAPI.page({ page: pagination.current, size: pagination.size }).then(res => {
    const { records, total } = res.data
    taskList.value = records
    pagination.total = total
  }).finally(() => {
    loading.value = false
  })
}

const loadParticipants = async (taskId) => {
  try {
    participantsLoading.value = true
    const res = await taskAPI.listParticipants(taskId)
    participants.value = res.data || []
  } finally {
    participantsLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.task-management {
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

.task-detail {
  padding: 20px 0;
}

.task-detail h3 {
  margin-bottom: 15px;
  color: #303133;
  font-size: 18px;
}

.task-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  color: #909399;
  font-size: 14px;
  flex-wrap: wrap;
}

.task-status {
  margin-bottom: 20px;
}

.task-description h4 {
  margin-bottom: 10px;
  color: #303133;
}

.task-description p {
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
}
</style>
