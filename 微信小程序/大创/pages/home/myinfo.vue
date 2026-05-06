<template>
	<view class="container">
	    <view class="header">个人信息</view>
	    <view class="member-item">
	        <view class="label">学号：</view>
	        <view v-if="!myinfo.isEditing" class="info-value">{{ myinfo.studentId }}</view>
	        <view v-else><input v-model="myinfo.studentId" type="text" class="input-edit" /></view>
	    </view>
	    <view class="member-item">
	        <view class="label">姓名：</view>
	        <view v-if="!myinfo.isEditing" class="info-value">{{ myinfo.username }}</view>
	        <view v-else><input v-model="myinfo.username" type="text" class="input-edit" /></view>
	    </view>
	    <view class="member-item">
	        <view class="label">身份证号：</view>
	        <view v-if="!myinfo.isEditing" class="info-value">{{ myinfo.idNumber }}</view>
	        <view v-else><input v-model="myinfo.idNumber" type="text" class="input-edit" /></view>
	    </view>
	    <view class="member-item">
	        <view class="label">手机号：</view>
	        <view v-if="!myinfo.isEditing" class="info-value">{{ myinfo.mobile }}</view>
	        <view v-else><input v-model="myinfo.mobile" type="text" class="input-edit" /></view>
	    </view>
	    <view class="member-item">
	        <view class="label">政治面貌：</view>
	        <view v-if="!myinfo.isEditing" class="info-value">{{ myinfo.politicalStatus || '-' }}</view>
	        <view v-else>
	            <picker mode="selector" :range="politicalStatusOptions" @change="onPoliticalChange">
	                <view class="input-edit">{{ myinfo.politicalStatus || '请选择' }}</view>
	            </picker>
	        </view>
	    </view>
	    <view class="member-item">
	        <view class="label">所属支部：</view>
	        <view v-if="!myinfo.isEditing" class="info-value">{{ myinfo.branchName || '-' }}</view>
	        <view v-else>
	            <picker mode="selector" :range="branches.map(b=>b.name)" @change="onBranchChange">
	                <view class="input-edit">{{ myinfo.branchName || '请选择' }}</view>
	            </picker>
	        </view>
	    </view>
	    <view class="member-item">
	        <view class="label">审核状态：</view>
	        <view class="info-value" :style="{color: myinfo.reviewStatus==='PENDING' ? '#e6a23c' : (myinfo.reviewStatus==='APPROVED' ? '#67c23a' : '#f56c6c')}">
	            {{ myinfo.reviewStatus==='PENDING' ? '审核中' : (myinfo.reviewStatus==='APPROVED' ? '已通过' : '不通过') }}
	        </view>
	    </view>
	    
	    <button @click="toggleEdit()" :disabled="!openApplication && !myinfo.isEditing">
	        {{ myinfo.isEditing ? '提 交 审 核' : (openApplication ? '编 辑' : '未开放，无法编辑') }}
	    </button>    
	</view>          
</template>

<script setup>
	import { reactive, ref, onMounted } from 'vue'
	import { requestWithAuth as request } from '@/utils/auth.js'
	
	const myinfo = reactive({
		isEditing: false,
		studentId: '',
		idNumber: '',
		username: '',
		mobile: '',
		politicalStatus: '',
		branchId: undefined,
		branchName: '',
		reviewStatus: 'NORMAL'
	})
	const branches = ref([])
	const openApplication = ref(false)
	const politicalStatusOptions = ['群众', '共青团员', '预备党员', '党员', '其他']
	
	const fetchBranches = async () => {
		try {
			const res = await request({ url: '/branches', method: 'GET' })
			if (res.statusCode === 200 && res.data?.code === 200) {
				branches.value = res.data.data || []
			}
		} catch (e) {
			console.error('获取支部列表失败:', e)
		}
		// 保底提供"测试支部"
		if (!branches.value || branches.value.length === 0) {
			branches.value = [{ id: 1, name: '测试支部' }]
		} else if (!branches.value.some(b => b.name === '测试支部')) {
			branches.value = [{ id: 1, name: '测试支部' }, ...branches.value]
		}
	}
	
	const fetchProfile = async () => {
		const res = await request({ url: '/members/profile', method: 'GET' })
		if (res.statusCode === 200 && res.data?.code === 200) {
			const d = res.data.data || {}
			myinfo.studentId = d.studentId || ''
			myinfo.idNumber = d.idNumber || ''
			myinfo.username = d.username || ''
			myinfo.mobile = d.mobile || ''
			myinfo.politicalStatus = d.politicalStatus || ''
			myinfo.branchId = d.branchId
			myinfo.reviewStatus = d.reviewStatus || 'NORMAL'
			const b = branches.value.find(x => x.id === myinfo.branchId)
			myinfo.branchName = b ? b.name : ''
		}
	}

	const fetchOpenApplication = async () => {
		try {
			const res = await request({ url: '/settings/open-application', method: 'GET' })
			if (res.statusCode === 200 && res.data?.code === 200) {
				openApplication.value = !!res.data.data?.open
			}
		} catch (e) {}
	}
	
	const onBranchChange = (e) => {
		const i = Number(e.detail.value)
		const b = branches.value[i]
		myinfo.branchId = b?.id
		myinfo.branchName = b?.name
	}

	const onPoliticalChange = (e) => {
		const i = Number(e.detail.value)
		myinfo.politicalStatus = politicalStatusOptions[i]
	}
	
	const toggleEdit = async () => {
		if (!myinfo.isEditing) {
			if (!openApplication.value) {
				uni.showToast({ title: '当前未开放申请，无法编辑', icon: 'none' })
				return
			}
			myinfo.isEditing = true;
			return;
		}
		const payload = {
			studentId: myinfo.studentId,
			idNumber: myinfo.idNumber,
			username: myinfo.username,
			mobile: myinfo.mobile,
			politicalStatus: myinfo.politicalStatus,
			branchId: myinfo.branchId
		}
		const res = await request({ url: '/members/profile', method: 'PUT', data: payload })
		if (res.statusCode === 200 && res.data?.code === 200) {
			uni.showToast({ title: '已保存并提交审核', icon: 'success' })
			myinfo.isEditing = false
			myinfo.reviewStatus = 'PENDING'
		} else {
			uni.showToast({ title: res.data?.message || '保存失败', icon: 'none' })
		}
	}
	
	onMounted(async () => {
		await fetchBranches()
		await fetchOpenApplication()
		await fetchProfile()
	})
</script>

<style scoped>
.container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 5px;
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.1);
}
.header {
  text-align: center;
  font-weight: bold;
}
.member-item {
  margin-bottom: 30rpx;
}
.member-item .label {
  margin-bottom: 20rpx;
}
.member-item .info-value,
.member-item .input-edit {
  background-color: #eee;
  height: 60rpx;
  line-height: 60rpx;
  padding-left: 20rpx;
}
</style>
