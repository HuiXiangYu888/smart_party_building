<template>
  <div class="space-y-5">
    <!-- Action Bar -->
    <div class="page-card">
      <div class="search-toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon class="mr-1"><Plus /></el-icon> 发布任务
        </el-button>
      </div>
    </div>

    <!-- Table -->
    <div class="page-card">
      <el-table :data="taskList" v-loading="loading" stripe class="w-full">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="任务标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="points" label="积分" width="100" />
        <el-table-column prop="capacity" label="人数上限" width="120" />
        <el-table-column prop="dueDate" label="截止时间" width="180" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ translateStatus(row.status) }}</el-tag>
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

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="任务标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="截止时间" prop="dueDate">
          <el-date-picker v-model="form.dueDate" type="datetime" placeholder="选择截止时间" class="w-full" />
        </el-form-item>
        <el-form-item label="任务描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入任务描述" />
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
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- View Dialog -->
    <el-dialog v-model="viewDialogVisible" title="任务详情" width="800px">
      <div v-if="currentTask" class="space-y-4 py-2">
        <h3 class="text-lg font-semibold text-[var(--color-text-primary)]">{{ currentTask.title }}</h3>
        <div class="flex flex-wrap gap-5 text-sm text-[var(--color-text-secondary)]">
          <span>积分：{{ currentTask.points }}</span>
          <span>截止时间：{{ currentTask.dueDate }}</span>
          <span v-if="currentTask.capacity != null">人数上限：{{ currentTask.capacity }}</span>
        </div>
        <el-tag :type="getStatusType(currentTask.status)">{{ translateStatus(currentTask.status) }}</el-tag>
        <div>
          <h4 class="font-medium text-[var(--color-text-primary)] mb-2">任务描述</h4>
          <p class="leading-relaxed text-[var(--color-text-secondary)] whitespace-pre-wrap">{{ currentTask.description }}</p>
        </div>
        <div>
          <h4 class="font-medium text-[var(--color-text-primary)] mb-2">参与人列表</h4>
          <el-table :data="participants" size="small" class="w-full" v-loading="participantsLoading">
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

const form = reactive({ id: null, title: '', description: '', dueDate: '', status: 'PUBLISHING', points: 0, capacity: null })
const rules = {
  title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }],
  dueDate: [{ required: true, message: '请选择截止时间', trigger: 'change' }],
  description: [{ required: true, message: '请输入任务描述', trigger: 'blur' }]
}

const taskList = ref([])
const pagination = reactive({ current: 1, size: 10, total: 0 })
const dialogTitle = ref('发布任务')

const statusMap = { 'ENDED': 'success', 'PENDING_END': 'warning', 'PUBLISHING': 'info' }
const statusText = { 'PUBLISHING': '发布中', 'PENDING_END': '待完结', 'ENDED': '已完结' }
const getStatusType = (s) => statusMap[s] || 'info'
const translateStatus = (s) => statusText[s] || s || ''

const handleAdd = () => { dialogTitle.value = '发布任务'; resetForm(); dialogVisible.value = true }
const handleEdit = (row) => { dialogTitle.value = '编辑任务'; Object.assign(form, row); dialogVisible.value = true }
const handleView = (row) => { currentTask.value = row; viewDialogVisible.value = true; loadParticipants(row.id) }

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该任务吗？', '提示', { type: 'warning' })
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
    if (form.id) { await taskAPI.update(form.id, form) } else { await taskAPI.create(form) }
    ElMessage.success(form.id ? '编辑成功' : '发布成功')
    dialogVisible.value = false
    loadData()
  } catch {} finally { submitLoading.value = false }
}

const resetForm = () => {
  Object.assign(form, { id: null, title: '', description: '', dueDate: '', status: 'PUBLISHING', points: 0, capacity: null })
  formRef.value?.clearValidate()
}

const loadData = () => {
  loading.value = true
  taskAPI.page({ page: pagination.current, size: pagination.size }).then(res => {
    const { records, total } = res.data
    taskList.value = records
    pagination.total = total
  }).finally(() => { loading.value = false })
}

const loadParticipants = async (taskId) => {
  try { participantsLoading.value = true; const res = await taskAPI.listParticipants(taskId); participants.value = res.data || [] }
  finally { participantsLoading.value = false }
}

onMounted(loadData)
</script>
