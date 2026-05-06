<template>
  <div class="space-y-5">
    <!-- Search -->
    <div class="page-card">
      <div class="search-toolbar">
        <el-input v-model="searchForm.keyword" placeholder="请输入姓名/学号搜索" class="!w-72" clearable @keyup.enter="handleSearch">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="searchForm.type" placeholder="申请类型" class="!w-44" clearable>
          <el-option label="入党申请" value="入党申请" />
          <el-option label="积极分子申请" value="积极分子申请" />
          <el-option label="预备党员申请" value="预备党员申请" />
        </el-select>
        <el-select v-model="searchForm.status" placeholder="审核状态" class="!w-36" clearable>
          <el-option label="待审核" value="待审核" />
          <el-option label="已通过" value="已通过" />
          <el-option label="已拒绝" value="已拒绝" />
        </el-select>
        <el-select v-model="reviewFilter" placeholder="筛选" class="!w-40" clearable>
          <el-option label="未审核" value="未审核" />
          <el-option label="已审核" value="已审核" />
        </el-select>
        <el-button type="primary" @click="handleSearch">
          <el-icon class="mr-1"><Search /></el-icon> 搜索
        </el-button>
        <el-button @click="handleReset">
          <el-icon class="mr-1"><Refresh /></el-icon> 重置
        </el-button>
      </div>
    </div>

    <!-- Table -->
    <div class="page-card">
      <el-table :data="applicationList" v-loading="loading" stripe class="w-full">
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="studentId" label="学号" width="140" />
        <el-table-column prop="type" label="申请类型" width="140" />
        <el-table-column prop="submittedAt" label="提交时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewer" label="审核人" width="120" />
        <el-table-column prop="reviewTime" label="审核时间" width="180" />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status === '待审核'" size="small" type="success" @click="handleApprove(row)">通过</el-button>
            <el-button v-if="row.status === '待审核'" size="small" type="danger" @click="handleReject(row)">拒绝</el-button>
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

    <!-- View Dialog -->
    <el-dialog v-model="viewDialogVisible" title="申请详情" width="800px">
      <div v-if="currentApplication" class="space-y-5">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ currentApplication.name }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentApplication.studentId }}</el-descriptions-item>
          <el-descriptions-item label="申请类型">{{ currentApplication.type }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ currentApplication.submittedAt }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentApplication.status)">{{ currentApplication.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审核人">{{ currentApplication.reviewer || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ currentApplication.reviewTime || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div>
          <h4 class="font-semibold text-[var(--color-text-primary)] mb-3">申请材料</h4>
          <div v-if="currentApplication.detail">
            <div v-if="currentApplication.type.includes('积极分子')">
              <div class="whitespace-pre-wrap mb-2 text-[var(--color-text-secondary)]">{{ currentApplication.detail.details }}</div>
              <div v-if="currentApplication.detail.attachments">
                <el-image v-for="(u,i) in tryParseArray(currentApplication.detail.attachments)" :key="i" :src="u" class="w-24 h-24 mr-2" fit="cover" :preview-src-list="tryParseArray(currentApplication.detail.attachments)" />
              </div>
            </div>
            <div v-else-if="currentApplication.type.includes('入党申请')">
              <div class="whitespace-pre-wrap text-[var(--color-text-secondary)]">{{ currentApplication.detail.details }}</div>
              <div v-if="currentApplication.detail.attachments?.length" class="mt-2">
                <el-image v-for="(u,i) in currentApplication.detail.attachments" :key="i" :src="u" class="w-24 h-24 mr-2" fit="cover" :preview-src-list="currentApplication.detail.attachments" />
              </div>
            </div>
            <div v-else>
              <div class="whitespace-pre-wrap text-[var(--color-text-secondary)]">考察报告：{{ currentApplication.detail.evaluationReport }}</div>
              <div v-if="currentApplication.detail.attachments?.length" class="mt-2">
                <el-image v-for="(u,i) in currentApplication.detail.attachments" :key="i" :src="u" class="w-24 h-24 mr-2" fit="cover" :preview-src-list="currentApplication.detail.attachments" />
              </div>
              <div class="mt-2 text-[var(--color-text-secondary)]">预备期：{{ currentApplication.detail.probationStart }} ~ {{ currentApplication.detail.probationEnd }}</div>
            </div>
          </div>
          <el-empty v-else description="暂无材料" />
        </div>
      </div>
    </el-dialog>

    <!-- Review Dialog -->
    <el-dialog v-model="reviewDialogVisible" :title="reviewTitle" width="500px">
      <el-form ref="reviewFormRef" :model="reviewForm" :rules="reviewRules" label-width="100px">
        <el-form-item label="审核结果" prop="result">
          <el-radio-group v-model="reviewForm.result">
            <el-radio label="通过">通过</el-radio>
            <el-radio label="拒绝">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见" prop="remark">
          <el-input v-model="reviewForm.remark" type="textarea" :rows="4" placeholder="请输入审核意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReviewSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const viewDialogVisible = ref(false)
const reviewDialogVisible = ref(false)
const submitLoading = ref(false)
const reviewFormRef = ref()
const currentApplication = ref(null)

const searchForm = reactive({ keyword: '', type: '', status: '' })
const reviewFilter = ref('')
const reviewForm = reactive({ applicationId: null, typeKey: '', result: '通过', remark: '' })
const reviewRules = {
  result: [{ required: true, message: '请选择审核结果', trigger: 'change' }],
  remark: [{ required: true, message: '请输入审核意见', trigger: 'blur' }]
}

const tryParseArray = (v) => {
  if (!v) return []
  try { const arr = JSON.parse(v); return Array.isArray(arr) ? arr : [] } catch { return [] }
}

const applicationList = ref([])
const pagination = reactive({ current: 1, size: 10, total: 0 })
const reviewTitle = ref('审核申请')

const getStatusType = (status) => {
  if (status === '待审核') return 'warning'
  if (status === '已通过') return 'success'
  if (status === '已拒绝') return 'danger'
  return 'info'
}

const handleSearch = () => { pagination.current = 1; loadData() }
const handleReset = () => {
  searchForm.keyword = ''; searchForm.type = ''; searchForm.status = ''; reviewFilter.value = ''
  handleSearch()
}

const getTypeKey = (type) => type.includes('积极分子') ? 'positive' : (type.includes('预备') ? 'prepare' : 'party')

const handleView = async (row) => {
  try {
    const typeKey = getTypeKey(row.type)
    const res = await request.get(`/admin/applications/${typeKey}/${row.id}`)
    currentApplication.value = { ...row, detail: res.data, typeKey }
    viewDialogVisible.value = true
  } catch {}
}

const handleApprove = (row) => {
  reviewTitle.value = '审核申请 - 通过'
  reviewForm.applicationId = row.id
  reviewForm.typeKey = getTypeKey(row.type)
  reviewForm.result = '通过'
  reviewForm.remark = ''
  reviewDialogVisible.value = true
}

const handleReject = (row) => {
  reviewTitle.value = '审核申请 - 拒绝'
  reviewForm.applicationId = row.id
  reviewForm.typeKey = getTypeKey(row.type)
  reviewForm.result = '拒绝'
  reviewForm.remark = ''
  reviewDialogVisible.value = true
}

const handleReviewSubmit = async () => {
  if (!reviewFormRef.value) return
  try {
    await reviewFormRef.value.validate()
    submitLoading.value = true
    const approve = reviewForm.result === '通过'
    await request.post(`/admin/applications/review/${reviewForm.typeKey}/${reviewForm.applicationId}`, null, { params: { approve, comments: reviewForm.remark } })
    ElMessage.success('审核完成')
    reviewDialogVisible.value = false
    loadData()
  } catch {} finally {
    submitLoading.value = false
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/applications')
    let list = res.data || []
    if (searchForm.keyword) {
      const kw = searchForm.keyword.trim()
      list = list.filter(it => String(it.name || '').includes(kw) || String(it.studentId || '').includes(kw))
    }
    if (searchForm.type) list = list.filter(it => it.type === searchForm.type)
    if (searchForm.status) list = list.filter(it => it.status === searchForm.status)
    if (reviewFilter.value === '未审核') list = list.filter(it => it.status === '待审核')
    else if (reviewFilter.value === '已审核') list = list.filter(it => it.status !== '待审核')
    applicationList.value = list
    pagination.total = list.length
  } catch {} finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>
