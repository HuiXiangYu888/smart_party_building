<template>
  <div class="space-y-5">
    <!-- Header -->
    <div class="page-card p-5">
      <div class="flex items-center gap-3 flex-wrap">
        <h2 class="text-lg font-semibold text-[var(--color-text-primary)]">{{ authStore.isSystemAdmin ? '账号管理' : '个人资料' }}</h2>
        <el-tag type="info">当前账号：{{ authStore.displayName }}</el-tag>
        <el-tag v-if="authStore.isSystemAdmin" type="danger">超级管理员</el-tag>
        <el-tag v-else type="warning">支部管理员</el-tag>
        <el-button v-if="authStore.isSystemAdmin" type="primary" @click="openCreate()">
          <el-icon class="mr-1"><Plus /></el-icon> 添加账号
        </el-button>
      </div>
    </div>

    <!-- Table -->
    <div class="page-card">
      <el-table :data="adminList" class="w-full">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="adminType" label="身份" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.adminType)">{{ getRoleLabel(row.adminType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="branchName" label="所属党支部" width="180">
          <template #default="{ row }">
            <span>{{ row.branchName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="140">
          <template #default="{ row }">
            <div v-if="authStore.isSystemAdmin" class="flex items-center gap-2">
              <el-switch :model-value="row.status === 'ACTIVE'" @change="onToggle(row)" />
              <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">{{ row.status === 'ACTIVE' ? '启用' : '禁用' }}</el-tag>
            </div>
            <el-tag v-else :type="row.status === 'ACTIVE' ? 'success' : 'danger'">{{ row.status === 'ACTIVE' ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="editAccount(row)">编辑</el-button>
            <el-popconfirm v-if="authStore.isSystemAdmin" title="确认删除该账号？" @confirm="onDelete(row)">
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Dialog -->
    <el-dialog v-model="showDialog" :title="dialogTitle" width="520px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="姓名">
          <el-input v-model="form.realName" :disabled="!authStore.isSystemAdmin" />
        </el-form-item>
        <el-form-item label="身份">
          <el-select v-model="form.adminType" class="w-full" :disabled="!authStore.isSystemAdmin">
            <el-option label="超级管理员" value="SYSTEM_ADMIN" />
            <el-option label="党支部管理员" value="BRANCH_ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isEditing" label="用户名">
          <el-input v-model="form.username" :disabled="!authStore.isSystemAdmin" />
        </el-form-item>
        <el-form-item v-if="!isEditing" label="手机号">
          <el-input v-model="form.mobile" :disabled="!authStore.isSystemAdmin" />
        </el-form-item>
        <el-form-item v-if="!isEditing" label="登录密码">
          <el-input v-model="form.password" type="password" placeholder="不填默认123456" :disabled="!authStore.isSystemAdmin" />
        </el-form-item>
        <el-form-item v-if="!isEditing" label="所属支部" :required="form.adminType === 'BRANCH_ADMIN'">
          <el-select v-model="form.branchId" class="w-full" :disabled="!authStore.isSystemAdmin">
            <el-option v-for="b in branchOptions" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="最近修改人" v-if="isEditing">
          <span class="text-[var(--color-text-secondary)]">{{ form.lastModifiedByName || '-' }}</span>
        </el-form-item>
        <el-form-item label="最近修改时间" v-if="isEditing">
          <span class="text-[var(--color-text-secondary)]">{{ form.lastModifiedAt || '-' }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button v-if="authStore.isSystemAdmin" type="primary" @click="saveAccount">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listAdmins, createAdmin, updateAdmin, updateAdminStatus, deleteAdmin } from '@/api/auth'
import request from '@/utils/request'
import { useAuthStore } from '@/stores/auth'

const showDialog = ref(false)
const dialogTitle = ref('添加账号')
const isEditing = ref(false)
const adminList = ref([])
const authStore = useAuthStore()

const form = reactive({
  id: undefined,
  username: '',
  realName: '',
  mobile: '',
  password: '',
  adminType: 'BRANCH_ADMIN',
  branchId: undefined,
  status: 'ACTIVE',
  lastModifiedByName: '',
  lastModifiedAt: ''
})

const getRoleType = (role) => ({ 'SYSTEM_ADMIN': 'danger', 'BRANCH_ADMIN': 'warning' }[role] || 'info')
const getRoleLabel = (role) => ({ 'SYSTEM_ADMIN': '超级管理员', 'BRANCH_ADMIN': '支部管理员' }[role] || role)

const openCreate = () => {
  dialogTitle.value = '添加账号'
  isEditing.value = false
  Object.assign(form, { id: undefined, username: '', realName: '', mobile: '', password: '', adminType: 'BRANCH_ADMIN', branchId: undefined, status: 'ACTIVE' })
  showDialog.value = true
}

const editAccount = (row) => {
  dialogTitle.value = authStore.isSystemAdmin ? '编辑账号' : '编辑个人资料'
  isEditing.value = true
  Object.assign(form, { id: row.id, realName: row.realName, adminType: row.adminType })
  showDialog.value = true
}

const saveAccount = async () => {
  try {
    if (isEditing.value) {
      await updateAdmin(form.id, { realName: form.realName, adminType: form.adminType })
      ElMessage.success('更新成功')
    } else {
      await createAdmin({ username: form.username, realName: form.realName, mobile: form.mobile, password: form.password, adminType: form.adminType, branchId: form.branchId })
      ElMessage.success('创建成功')
    }
    showDialog.value = false
    await fetchList()
  } catch {}
}

const onToggle = async (row) => {
  const target = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  try {
    await updateAdminStatus(row.id, target)
    row.status = target
    ElMessage.success('状态已更新')
  } catch {}
}

const onDelete = async (row) => {
  try {
    await deleteAdmin(row.id)
    ElMessage.success('删除成功')
    await fetchList()
  } catch {}
}

const fetchList = async () => {
  const res = await listAdmins()
  if (!authStore.isSystemAdmin) {
    adminList.value = res.data?.filter(admin => admin.id === authStore.userInfo?.id) || []
  } else {
    adminList.value = res.data || []
  }
}

const branchOptions = ref([])
const fetchBranches = async () => {
  try {
    const res = await request({ url: '/admin/accounts/branches', method: 'get' })
    branchOptions.value = res.data || []
  } catch {}
}

onMounted(() => {
  fetchList()
  fetchBranches()
})
</script>
