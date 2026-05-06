<template>
  <view class="container">
    <view class="form-item">
      <text class="label">学号</text>
      <input class="input" v-model="form.studentId" placeholder="请输入学号" />
    </view>
    <view class="form-item">
      <text class="label">身份证号</text>
      <input class="input" v-model="form.idNumber" placeholder="请输入身份证号" />
    </view>
    <view class="form-item">
      <text class="label">姓名</text>
      <input class="input" v-model="form.username" placeholder="请输入姓名" />
    </view>
    <view class="form-item">
      <text class="label">手机号</text>
      <input class="input" v-model="form.mobile" placeholder="请输入手机号" />
    </view>
    <view class="form-item">
      <text class="label">政治面貌</text>
      <picker mode="selector" :range="politicalStatusOptions" @change="onPoliticalStatusChange">
        <view class="picker">{{ selectedPoliticalStatus || '请选择政治面貌' }}</view>
      </picker>
    </view>
    <view class="form-item">
      <text class="label">所属支部</text>
      <picker mode="selector" :range="branchNames" @change="onBranchChange">
        <view class="picker">{{ selectedBranchName || '请选择所属支部' }}</view>
      </picker>
    </view>
    <button class="save-btn" @click="save">保存</button>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { requestWithAuth as request, updateUserInfo } from '@/utils/auth.js'
import eventBus, { EVENTS } from '@/utils/eventBus.js'

const form = reactive({
  studentId: '',
  idNumber: '',
  username: '',
  mobile: '',
  politicalStatus: '',
  branchId: undefined
})

const branches = ref([])
const branchNames = ref([])
const selectedBranchName = ref('')

// 政治面貌选项
const politicalStatusOptions = ref(['群众', '共青团员', '预备党员', '党员', '其他'])
const selectedPoliticalStatus = ref('')

const fetchProfile = async () => {
  const [res] = await request({ url: '/members/profile', method: 'GET' })
  if (res.statusCode === 200 && res.data?.code === 200) {
    const d = res.data.data || {}
    form.studentId = d.studentId || ''
    form.idNumber = d.idNumber || ''
    form.username = d.username || ''
    form.mobile = d.mobile || ''
    form.politicalStatus = d.politicalStatus || ''
    form.branchId = d.branchId
    
    // 设置政治面貌显示
    if (form.politicalStatus) {
      selectedPoliticalStatus.value = form.politicalStatus
    }
    if (branches.value.length > 0 && form.branchId) {
      const b = branches.value.find(x => x.id === form.branchId)
      selectedBranchName.value = b ? b.name : ''
    }
  }
}

const fetchBranches = async () => {
  try {
    const [res] = await request({ url: '/branches', method: 'GET' })
    if (res.statusCode === 200 && res.data?.code === 200) {
      branches.value = res.data.data || []
    }
  } catch (e) {
    console.error('获取支部列表失败:', e)
  }
  // 提供测试支部选项
  if (!branches.value || branches.value.length === 0) {
    branches.value = [
      { id: 1, name: '测试支部' },
      { id: 2, name: '计算机学院第一党支部' },
      { id: 3, name: '计算机学院第二党支部' }
    ]
  }
  branchNames.value = branches.value.map(x => x.name)
}

const onPoliticalStatusChange = (e) => {
  const idx = Number(e.detail.value)
  form.politicalStatus = politicalStatusOptions.value[idx]
  selectedPoliticalStatus.value = politicalStatusOptions.value[idx]
}

const onBranchChange = (e) => {
  const idx = Number(e.detail.value)
  const b = branches.value[idx]
  form.branchId = b?.id
  selectedBranchName.value = b?.name
}

const save = async () => {
  const payload = {
    studentId: form.studentId,
    idNumber: form.idNumber,
    username: form.username,
    mobile: form.mobile,
    politicalStatus: form.politicalStatus,
    branchId: form.branchId
  }
  const [res] = await request({ url: '/members/profile', method: 'PUT', data: payload })
  if (res.statusCode === 200 && res.data?.code === 200) {
    // 更新本地用户信息
    updateUserInfo({ username: form.username })
    
    // 触发用户信息更新事件
    eventBus.emit(EVENTS.USER_INFO_UPDATED, { username: form.username })
    
    uni.showToast({ title: '保存成功', icon: 'success' })
  } else {
    uni.showToast({ title: res.data?.message || '保存失败', icon: 'none' })
  }
}

onMounted(async () => {
  await fetchBranches()
  await fetchProfile()
})
</script>

<style scoped>
.container { padding: 24rpx; }
.form-item { margin-bottom: 24rpx; }
.label { display: block; margin-bottom: 12rpx; color: #333; }
.input { background: #fff; padding: 16rpx; border-radius: 8rpx; border: 1rpx solid #eee; }
.picker { background: #fff; padding: 16rpx; border-radius: 8rpx; border: 1rpx solid #eee; color: #666; }
.save-btn { margin-top: 24rpx; background: #c92127; color: #fff; }
</style>

