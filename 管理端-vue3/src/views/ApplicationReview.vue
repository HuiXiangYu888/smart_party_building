<template>
  <div class="application-review">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <div class="search-bar">
        <el-input
          v-model="searchForm.keyword"
          placeholder="请输入姓名/学号搜索"
          style="width: 300px"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-select v-model="searchForm.type" placeholder="申请类型" style="width: 180px" clearable>
          <el-option label="入党申请" value="入党申请" />
          <el-option label="积极分子申请" value="积极分子申请" />
          <el-option label="预备党员申请" value="预备党员申请" />
        </el-select>
        
        <el-select v-model="searchForm.status" placeholder="审核状态" style="width: 150px" clearable>
          <el-option label="待审核" value="待审核" />
          <el-option label="已通过" value="已通过" />
          <el-option label="已拒绝" value="已拒绝" />
        </el-select>

        <el-select v-model="reviewFilter" placeholder="筛选(已审核/未审核)" style="width: 180px" clearable>
          <el-option label="未审核" value="未审核" />
          <el-option label="已审核" value="已审核" />
        </el-select>
        
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>

        <!-- 两级审核不建议批量操作，如需保留可在后端另行实现安全策略 -->
      </div>
    </el-card>
    
    <!-- 申请列表 -->
    <el-card class="table-card">
      <el-table
        :data="applicationList"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="studentId" label="学号" width="140" />
        <el-table-column prop="type" label="申请类型" width="140" />
        <el-table-column prop="submittedAt" label="提交时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewer" label="审核人" width="120" />
        <el-table-column prop="reviewTime" label="审核时间" width="180" />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button 
              v-if="row.status === '待审核'"
              size="small" 
              type="success" 
              @click="handleApprove(row)"
            >
              通过
            </el-button>
            <el-button 
              v-if="row.status === '待审核'"
              size="small" 
              type="danger" 
              @click="handleReject(row)"
            >
              拒绝
            </el-button>
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
      title="申请详情"
      width="800px"
    >
      <div v-if="currentApplication" class="application-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ currentApplication.name }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentApplication.studentId }}</el-descriptions-item>
          <el-descriptions-item label="申请类型">{{ currentApplication.type }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ currentApplication.submittedAt }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentApplication.status)">
              {{ currentApplication.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审核人">{{ currentApplication.reviewer || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ currentApplication.reviewTime || '-' }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="detail-section">
          <h4>申请材料</h4>
          <div v-if="currentApplication.detail">
            <div v-if="currentApplication.type.includes('积极分子')">
              <div style="margin-bottom:8px; white-space:pre-wrap">{{ currentApplication.detail.details }}</div>
              <div v-if="currentApplication.detail.attachments">
                <el-image
                  v-for="(u,i) in tryParseArray(currentApplication.detail.attachments)"
                  :key="i"
                  :src="u"
                  style="width:100px;height:100px;margin-right:8px"
                  fit="cover"
                  :preview-src-list="tryParseArray(currentApplication.detail.attachments)"
                />
              </div>
            </div>
            <div v-else-if="currentApplication.type.includes('入党申请')">
              <div style="white-space:pre-wrap">{{ currentApplication.detail.details }}</div>
              <div v-if="currentApplication.detail.attachments && currentApplication.detail.attachments.length" style="margin-top:8px">
                <el-image
                  v-for="(u,i) in currentApplication.detail.attachments"
                  :key="i"
                  :src="u"
                  style="width:100px;height:100px;margin-right:8px"
                  fit="cover"
                  :preview-src-list="currentApplication.detail.attachments"
                />
              </div>
            </div>
            <div v-else>
              <div style="white-space:pre-wrap">考察报告：{{ currentApplication.detail.evaluationReport }}</div>
              <div v-if="currentApplication.detail.attachments && currentApplication.detail.attachments.length" style="margin-top:8px">
                <el-image
                  v-for="(u,i) in currentApplication.detail.attachments"
                  :key="i"
                  :src="u"
                  style="width:100px;height:100px;margin-right:8px"
                  fit="cover"
                  :preview-src-list="currentApplication.detail.attachments"
                />
              </div>
              <div style="margin-top:8px">预备期：{{ currentApplication.detail.probationStart }} ~ {{ currentApplication.detail.probationEnd }}</div>
            </div>
          </div>
          <el-empty v-else description="暂无材料" />
        </div>
      </div>
    </el-dialog>
    
    <!-- 审核对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      :title="reviewTitle"
      width="500px"
    >
      <el-form
        ref="reviewFormRef"
        :model="reviewForm"
        :rules="reviewRules"
        label-width="100px"
      >
        <el-form-item label="审核结果" prop="result">
          <el-radio-group v-model="reviewForm.result">
            <el-radio label="通过">通过</el-radio>
            <el-radio label="拒绝">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="审核意见" prop="remark">
          <el-input
            v-model="reviewForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入审核意见"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reviewDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleReviewSubmit" :loading="submitLoading">
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
import request from '@/utils/request'

const loading = ref(false)
const viewDialogVisible = ref(false)
const reviewDialogVisible = ref(false)
const submitLoading = ref(false)
const reviewFormRef = ref()
const currentApplication = ref(null)

const searchForm = reactive({
  keyword: '',
  type: '',
  status: ''
})
const reviewFilter = ref('') // 未审核/已审核

const reviewForm = reactive({
  applicationId: null,
  typeKey: '',
  result: '通过',
  remark: ''
})

const reviewRules = {
  result: [
    { required: true, message: '请选择审核结果', trigger: 'change' }
  ],
  remark: [
    { required: true, message: '请输入审核意见', trigger: 'blur' }
  ]
}

// 将 JSON 字符串安全解析为数组
const tryParseArray = (v) => {
  if (!v) return []
  try {
    const arr = JSON.parse(v)
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
}

const applicationList = ref([])

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const reviewTitle = ref('审核申请')

const getStatusType = (status) => {
  switch (status) {
    case '待审核':
      return 'warning'
    case '已通过':
      return 'success'
    case '已拒绝':
      return 'danger'
    default:
      return 'info'
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.type = ''
  searchForm.status = ''
  handleSearch()
}

const handleView = async (row) => {
  const typeKey = row.type.includes('积极分子') ? 'positive' : (row.type.includes('预备') ? 'prepare' : 'party')
  try {
    const res = await request.get(`/admin/applications/${typeKey}/${row.id}`)
    currentApplication.value = { ...row, detail: res.data, typeKey }
    viewDialogVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

const handleApprove = (row) => {
  reviewTitle.value = '审核申请 - 通过'
  reviewForm.applicationId = row.id
  reviewForm.typeKey = row.type.includes('积极分子') ? 'positive' : (row.type.includes('预备') ? 'prepare' : 'party')
  reviewForm.result = '通过'
  reviewForm.remark = ''
  reviewDialogVisible.value = true
}

const handleReject = (row) => {
  reviewTitle.value = '审核申请 - 拒绝'
  reviewForm.applicationId = row.id
  reviewForm.typeKey = row.type.includes('积极分子') ? 'positive' : (row.type.includes('预备') ? 'prepare' : 'party')
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
    await request.post(`/admin/applications/review/${reviewForm.typeKey}/${reviewForm.applicationId}`, null, { params: { approve, comments: reviewForm.remark }})
    ElMessage.success('审核完成')
    reviewDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
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
    const res = await request.get('/admin/applications')
    let list = res.data || []
    // 关键词筛选（姓名/学号）
    if (searchForm.keyword) {
      const kw = searchForm.keyword.trim()
      list = list.filter(it => String(it.name || '').includes(kw) || String(it.studentId || '').includes(kw))
    }
    // 类型筛选
    if (searchForm.type) {
      list = list.filter(it => it.type === searchForm.type)
    }
    // 状态筛选
    if (searchForm.status) {
      list = list.filter(it => it.status === searchForm.status)
    }
    // 已审核/未审核筛选（未审核=待审核；已审核=非待审核）
    if (reviewFilter.value === '未审核') {
      list = list.filter(it => it.status === '待审核')
    } else if (reviewFilter.value === '已审核') {
      list = list.filter(it => it.status !== '待审核')
    }
    applicationList.value = list
    pagination.total = applicationList.value.length
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const batchApprove = async () => {
  try {
    loading.value = true
    await request.post('/members/profile/review/batch', null, { params: { approve: true } })
    ElMessage.success('已通过所有待审核申请')
    await loadData()
  } catch (e) {
    console.error(e)
    ElMessage.error('操作失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.application-review {
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

.application-detail {
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
