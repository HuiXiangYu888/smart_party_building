<template>
	<view class="container">
		<view class="header">支部信息</view>
		
		<!-- 加载状态 -->
		<view v-if="loading" class="loading">
			<text>正在加载支部信息...</text>
		</view>
		
		<!-- 错误状态 -->
		<view v-else-if="error" class="error">
			<text>{{ error }}</text>
			<button class="retry-btn" @click="retryLoad">重试</button>
		</view>
		
		<!-- 正常显示 -->
		<view v-else class="content">
			<view class="form-item">
				<text class="label">支部名称</text>
				<view class="input readonly">{{ form.name || '-' }}</view>
			</view>
			<view class="form-item">
				<text class="label">支部人数</text>
				<view class="input readonly">{{ form.memberCount ?? '-' }}</view>
			</view>
			<view class="form-item">
				<text class="label">负责人</text>
				<view class="input readonly">{{ form.leaderName || '-' }}</view>
			</view>
			<view class="form-item">
				<text class="label">负责人电话</text>
				<view class="input readonly">{{ form.leaderMobile || '-' }}</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { requestWithAuth } from '@/utils/auth.js'

const form = reactive({
	id: undefined,
	name: '',
	memberCount: 0,
	leaderId: undefined,
	leaderName: '',
	leaderMobile: ''
})

const loading = ref(false)
const error = ref('')

const fetchBranch = async (branchId) => {
	loading.value = true
	error.value = ''
	
	try {
		console.log('正在请求支部信息，支部ID:', branchId)
		const res = await requestWithAuth({ url: `/branches/${branchId}`, method: 'GET' })
		console.log('支部信息响应:', res)
		
		if (res.statusCode === 200 && res.data?.code === 200) {
			const d = res.data.data || {}
			form.id = d.id
			form.name = d.name
			form.memberCount = d.memberCount
			form.leaderId = d.leaderId
			form.leaderName = d.leaderName
			form.leaderMobile = d.leaderMobile
			
			console.log('支部信息加载成功:', form)
		} else {
			const errorMsg = res.data?.message || '加载失败'
			error.value = errorMsg
			uni.showToast({ title: errorMsg, icon: 'none' })
		}
	} catch (e) {
		console.error('请求支部信息失败:', e)
		const errorMsg = e.message || '网络错误'
		error.value = errorMsg
		uni.showToast({ title: errorMsg, icon: 'none' })
	} finally {
		loading.value = false
	}
}

const retryLoad = async () => {
  let defaultBranchId = 1
	try {
		const ui = JSON.parse(uni.getStorageSync('userInfo') || '{}')
		if (ui && ui.branchId) defaultBranchId = ui.branchId
	} catch {}
	await fetchBranch(defaultBranchId)
}

onMounted(async () => {
  let defaultBranchId = 1
	try {
		const ui = JSON.parse(uni.getStorageSync('userInfo') || '{}')
		if (ui && ui.branchId) defaultBranchId = ui.branchId
	} catch (e) {
		console.error('解析用户信息失败:', e)
	}
	await fetchBranch(defaultBranchId)
})
</script>

<style scoped>
.container { 
	padding: 24rpx; 
	background-color: #f5f5f5;
	min-height: 100vh;
}

.header { 
	text-align: center; 
	font-weight: bold; 
	margin-bottom: 20rpx; 
	font-size: 36rpx;
	color: #333;
}

.loading {
	text-align: center;
	padding: 40rpx;
	color: #666;
	font-size: 28rpx;
}

.error {
	text-align: center;
	padding: 40rpx;
	color: #e74c3c;
	font-size: 28rpx;
}

.retry-btn {
	margin-top: 20rpx;
	background: #c92127;
	color: #fff;
	border: none;
	border-radius: 8rpx;
	padding: 16rpx 32rpx;
	font-size: 28rpx;
}

.content {
	background: #fff;
	border-radius: 12rpx;
	padding: 24rpx;
}

.form-item { 
	margin-bottom: 24rpx; 
}

.label { 
	display: block; 
	margin-bottom: 12rpx; 
	color: #333; 
	font-size: 28rpx;
	font-weight: 500;
}

.input { 
	background: #f8f8f8; 
	padding: 16rpx; 
	border-radius: 8rpx; 
	border: 1rpx solid #eee; 
	font-size: 28rpx;
}

.readonly { 
	color: #666; 
}
</style>
