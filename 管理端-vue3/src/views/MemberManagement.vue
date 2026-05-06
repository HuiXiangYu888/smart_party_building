<template>
  <div class="member-management">
    <!-- 搜索和操作栏 -->
    <el-card class="search-card">
      <div class="search-bar">
        <div style="display:flex;align-items:center;gap:8px">
          <span>开放个人信息申请</span>
          <el-switch v-model="openApplication" @change="onToggleOpen" />
        </div>
        <el-input
          v-model="searchForm.keyword"
          placeholder="请输入姓名/手机号/学号搜索"
          style="width: 320px"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-select v-model="searchForm.politicalStatus" placeholder="政治面貌" style="width: 150px" clearable>
          <el-option v-for="opt in politicalOptions" :key="opt" :label="opt" :value="opt" />
        </el-select>

        <el-select v-model="searchForm.reviewStatus" placeholder="状态（全部/已通过/待审核/不通过）" style="width: 220px" clearable>
          <el-option label="已通过" value="已通过" />
          <el-option label="待审核" value="待审核" />
          <el-option label="不通过" value="不通过" />
        </el-select>

        <el-select v-model="sortOption" placeholder="排序" style="width: 220px" clearable>
          <el-option label="学号升序" value="sid_asc" />
          <el-option label="学号降序" value="sid_desc" />
          <el-option label="政治面貌升序(学号次序)" value="ps_asc" />
          <el-option label="政治面貌降序(学号次序)" value="ps_desc" />
        </el-select>
        
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
        
        <!-- 删除添加按钮，按需保留 -->
      </div>
    </el-card>
    
    <!-- 成员列表 -->
    <el-card class="table-card">
      <el-table
        :data="memberList"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="studentId" label="学号" width="140" />
        <el-table-column prop="mobile" label="手机号" width="140" />
        <el-table-column prop="idNumber" label="身份证号" width="180" />
        <el-table-column prop="politicalStatus" label="政治面貌" width="120" />
        <el-table-column prop="branchName" label="所属支部" width="150" />
        <el-table-column prop="reviewStatus" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.reviewStatus === '待审核' ? 'warning' : (row.reviewStatus==='已通过' ? 'success' : 'danger')">
              {{ row.reviewStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="success" @click="approve(row)">通过</el-button>
            <el-button size="small" type="danger" @click="reject(row)">打回</el-button>
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
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        
        <el-form-item label="政治面貌" prop="politicalStatus">
          <el-select v-model="form.politicalStatus" placeholder="请选择政治面貌" style="width: 100%">
            <el-option v-for="opt in politicalOptions" :key="opt" :label="opt" :value="opt" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="入党时间" prop="joinDate">
          <el-date-picker
            v-model="form.joinDate"
            type="date"
            placeholder="选择入党时间"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="所属支部" prop="branch">
          <el-input v-model="form.branch" placeholder="请输入所属支部" />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="正常">正常</el-radio>
            <el-radio label="停用">停用</el-radio>
          </el-radio-group>
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
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const politicalOptions = ref([])
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const openApplication = ref(false)

const searchForm = reactive({
  keyword: '',
  politicalStatus: '',
  reviewStatus: '',
  studentId: ''
})
const sortOption = ref('')

const form = reactive({
  id: null,
  name: '',
  phone: '',
  politicalStatus: '',
  joinDate: '',
  branch: '',
  status: '正常'
})

const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  politicalStatus: [
    { required: true, message: '请选择政治面貌', trigger: 'change' }
  ],
  joinDate: [
    { required: true, message: '请选择入党时间', trigger: 'change' }
  ],
  branch: [
    { required: true, message: '请输入所属支部', trigger: 'blur' }
  ]
}

const memberList = ref([])

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const dialogTitle = ref('添加成员')

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.politicalStatus = ''
  searchForm.reviewStatus = ''
  sortOption.value = ''
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '添加成员'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑成员'
  Object.assign(form, row)
  dialogVisible.value = true
}

const approve = async (row) => {
  try {
    await request.post(`/admin/members/review/${row.id}`, null, { params: { approve: true } })
    ElMessage.success('已通过')
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const reject = async (row) => {
  try {
    await request.post(`/admin/members/review/${row.id}`, null, { params: { approve: false } })
    ElMessage.success('已打回')
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

// 删除功能按需移除

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    // 这里调用添加/编辑API
    ElMessage.success(form.id ? '编辑成功' : '添加成功')
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
    name: '',
    phone: '',
    politicalStatus: '',
    joinDate: '',
    branch: '',
    status: '正常'
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

const loadData = async () => {
  loading.value = true
  try {
    const statusMap = { '已通过': 'APPROVED', '待审核': 'PENDING', '不通过': 'REJECTED' }
    const params = {}
    if (searchForm.reviewStatus) params.reviewStatus = statusMap[searchForm.reviewStatus] || ''
    // 关键字中如果是纯数字且长度>=4，则也传给 studentId，提升后端过滤效率
    const kwRaw = (searchForm.keyword || '').trim()
    const kwIsStudent = /^\d{4,}$/.test(kwRaw)
    if (kwIsStudent) params.studentId = kwRaw
    const res = await request.get('/admin/members', { params })
    let list = (res.data || [])
    // 动态生成政治面貌选项（按接口返回去重）
    const set = new Set(list.map(it => String(it.politicalStatus || '').trim()).filter(Boolean))
    politicalOptions.value = Array.from(set)
    // 关键词（姓名/手机号/学号/身份证）本地过滤
    if (kwRaw) {
      const kw = kwRaw
      list = list.filter(it =>
        String(it.name || '').includes(kw) ||
        String(it.mobile || '').includes(kw) ||
        String(it.studentId || '').includes(kw) ||
        String(it.idNumber || '').includes(kw)
      )
    }
    // 政治面貌本地过滤（严格匹配后端中文：正式党员/预备党员/积极分子/非党员）
    if (searchForm.politicalStatus) {
      list = list.filter(it => it.politicalStatus === searchForm.politicalStatus)
    }
    // 排序：政治面貌按原值字符串比较（localeCompare）
    const comparePolitical = (a, b) => String(a || '').localeCompare(String(b || ''))
    list.sort((a,b) => {
      const sidA = String(a.studentId || '')
      const sidB = String(b.studentId || '')
      switch (sortOption.value) {
        case 'sid_asc':
          return sidA.localeCompare(sidB)
        case 'sid_desc':
          return sidB.localeCompare(sidA)
        case 'ps_asc':
          return comparePolitical(a.politicalStatus, b.politicalStatus) || sidA.localeCompare(sidB)
        case 'ps_desc':
          return comparePolitical(b.politicalStatus, a.politicalStatus) || sidA.localeCompare(sidB)
        default:
          return 0
      }
    })
    memberList.value = list
    pagination.total = memberList.value.length
  } finally {
    loading.value = false
  }
}

const loadOpenApplication = async () => {
  try {
    const res = await request.get('/settings/open-application')
    openApplication.value = !!res.data.open
  } catch (e) {
    console.error(e)
  }
}

const onToggleOpen = async (val) => {
  try {
    await request.put('/settings/open-application', null, { params: { open: val } })
    ElMessage.success(val ? '已开启申请' : '已关闭申请')
  } catch (e) {
    ElMessage.error('操作失败')
    openApplication.value = !val
  }
}

onMounted(() => {
  loadData()
  loadOpenApplication()
})
</script>

<style scoped>
.member-management {
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
