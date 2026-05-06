<template>
  <div class="space-y-5">
    <!-- Search Bar -->
    <div class="page-card">
      <div class="search-toolbar">
        <div class="flex items-center gap-2">
          <span class="text-sm text-[var(--color-text-secondary)]">开放个人信息申请</span>
          <el-switch v-model="openApplication" @change="onToggleOpen" />
        </div>
        <el-input
          v-model="searchForm.keyword"
          placeholder="请输入姓名/手机号/学号搜索"
          class="!w-72"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="searchForm.politicalStatus" placeholder="政治面貌" class="!w-36" clearable>
          <el-option v-for="opt in politicalOptions" :key="opt" :label="opt" :value="opt" />
        </el-select>
        <el-select v-model="searchForm.reviewStatus" placeholder="状态" class="!w-44" clearable>
          <el-option label="已通过" value="已通过" />
          <el-option label="待审核" value="待审核" />
          <el-option label="不通过" value="不通过" />
        </el-select>
        <el-select v-model="sortOption" placeholder="排序" class="!w-52" clearable>
          <el-option label="学号升序" value="sid_asc" />
          <el-option label="学号降序" value="sid_desc" />
          <el-option label="政治面貌升序(学号次序)" value="ps_asc" />
          <el-option label="政治面貌降序(学号次序)" value="ps_desc" />
        </el-select>
        <el-button type="primary" @click="handleSearch">
          <el-icon class="mr-1"><Search /></el-icon> 搜索
        </el-button>
        <el-button @click="handleReset">
          <el-icon class="mr-1"><Refresh /></el-icon> 重置
        </el-button>
      </div>
    </div>

    <!-- Member Table -->
    <div class="page-card">
      <el-table :data="memberList" v-loading="loading" stripe class="w-full">
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

      <div class="pagination-bar">
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
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const politicalOptions = ref([])
const openApplication = ref(false)

const searchForm = reactive({
  keyword: '',
  politicalStatus: '',
  reviewStatus: ''
})
const sortOption = ref('')

const memberList = ref([])
const pagination = reactive({ current: 1, size: 10, total: 0 })

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

const approve = async (row) => {
  try {
    await request.post(`/admin/members/review/${row.id}`, null, { params: { approve: true } })
    ElMessage.success('已通过')
    loadData()
  } catch {
    ElMessage.error('操作失败')
  }
}

const reject = async (row) => {
  try {
    await request.post(`/admin/members/review/${row.id}`, null, { params: { approve: false } })
    ElMessage.success('已打回')
    loadData()
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleSizeChange = (size) => { pagination.size = size; loadData() }
const handleCurrentChange = (current) => { pagination.current = current; loadData() }

const loadData = async () => {
  loading.value = true
  try {
    const statusMap = { '已通过': 'APPROVED', '待审核': 'PENDING', '不通过': 'REJECTED' }
    const params = {}
    if (searchForm.reviewStatus) params.reviewStatus = statusMap[searchForm.reviewStatus] || ''

    const kwRaw = (searchForm.keyword || '').trim()
    if (/^\d{4,}$/.test(kwRaw)) params.studentId = kwRaw

    const res = await request.get('/admin/members', { params })
    let list = res.data || []

    // Dynamic political status options
    const set = new Set(list.map(it => String(it.politicalStatus || '').trim()).filter(Boolean))
    politicalOptions.value = Array.from(set)

    // Client-side keyword filter
    if (kwRaw) {
      list = list.filter(it =>
        String(it.name || '').includes(kwRaw) ||
        String(it.mobile || '').includes(kwRaw) ||
        String(it.studentId || '').includes(kwRaw) ||
        String(it.idNumber || '').includes(kwRaw)
      )
    }

    // Political status filter
    if (searchForm.politicalStatus) {
      list = list.filter(it => it.politicalStatus === searchForm.politicalStatus)
    }

    // Sort
    const comparePolitical = (a, b) => String(a || '').localeCompare(String(b || ''))
    list.sort((a, b) => {
      const sidA = String(a.studentId || '')
      const sidB = String(b.studentId || '')
      switch (sortOption.value) {
        case 'sid_asc': return sidA.localeCompare(sidB)
        case 'sid_desc': return sidB.localeCompare(sidA)
        case 'ps_asc': return comparePolitical(a.politicalStatus, b.politicalStatus) || sidA.localeCompare(sidB)
        case 'ps_desc': return comparePolitical(b.politicalStatus, a.politicalStatus) || sidA.localeCompare(sidB)
        default: return 0
      }
    })

    memberList.value = list
    pagination.total = list.length
  } finally {
    loading.value = false
  }
}

const loadOpenApplication = async () => {
  try {
    const res = await request.get('/settings/open-application')
    openApplication.value = !!res.data.open
  } catch {}
}

const onToggleOpen = async (val) => {
  try {
    await request.put('/settings/open-application', null, { params: { open: val } })
    ElMessage.success(val ? '已开启申请' : '已关闭申请')
  } catch {
    ElMessage.error('操作失败')
    openApplication.value = !val
  }
}

onMounted(() => {
  loadData()
  loadOpenApplication()
})
</script>
