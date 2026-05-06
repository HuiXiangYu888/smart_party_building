<template>
	<view class="container">
		<view class="header">
			<text class="title">功能测试页面</text>
			<text class="subtitle">用于验证登录和API功能</text>
		</view>
		
		<view class="section">
			<text class="section-title">登录状态</text>
			<view class="status-item">
				<text class="label">登录状态:</text>
				<text class="value" :class="{ 'success': isUserLoggedIn, 'error': !isUserLoggedIn }">
					{{ isUserLoggedIn ? '已登录' : '未登录' }}
				</text>
			</view>
			<view class="status-item" v-if="userInfo.username">
				<text class="label">用户名:</text>
				<text class="value">{{ userInfo.username }}</text>
			</view>
			<view class="status-item" v-if="userInfo.userType">
				<text class="label">用户类型:</text>
				<text class="value">{{ getUserTypeText() }}</text>
			</view>
		</view>
		
		<view class="section">
			<text class="section-title">功能测试</text>
			<view class="button-group">
				<button class="test-btn" @click="testLogin">测试登录</button>
				<button class="test-btn" @click="testLogout">测试登出</button>
				<button class="test-btn" @click="testRefreshToken">测试刷新令牌</button>
				<button class="test-btn" @click="testAPI">测试API调用</button>
			</view>
		</view>
		
		<view class="section">
			<text class="section-title">测试结果</text>
			<view class="result-area">
				<text class="result-text">{{ testResult }}</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getUserInfo, isLoggedIn, logout, refreshAccessToken } from '@/utils/auth.js'
import { userAPI } from '@/utils/api.js'

// 响应式数据
const userInfo = reactive({
	username: '',
	userType: ''
})

const testResult = ref('等待测试...')

// 计算属性
const isUserLoggedIn = computed(() => {
	return isLoggedIn()
})

// 获取用户类型文本
const getUserTypeText = () => {
	if (!userInfo.userType) return ''
	
	const typeMap = {
		'MEMBER': '正式党员',
		'ACTIVIST': '积极分子',
		'PREPARE': '预备党员',
		'ADMIN': '管理员',
		'SUPER_ADMIN': '超级管理员'
	}
	
	return typeMap[userInfo.userType] || userInfo.userType
}

// 测试登录
const testLogin = () => {
	testResult.value = '正在测试登录功能...'
	
	// 模拟登录测试
	setTimeout(() => {
		if (isUserLoggedIn.value) {
			testResult.value = '✅ 登录测试通过：用户已登录'
		} else {
			testResult.value = '❌ 登录测试失败：用户未登录，请先登录'
		}
	}, 1000)
}

// 测试登出
const testLogout = async () => {
	testResult.value = '正在测试登出功能...'
	
	try {
		await logout()
		testResult.value = '✅ 登出测试通过：已成功登出'
	} catch (error) {
		testResult.value = `❌ 登出测试失败：${error.message}`
	}
}

// 测试刷新令牌
const testRefreshToken = async () => {
	testResult.value = '正在测试令牌刷新功能...'
	
	try {
		await refreshAccessToken()
		testResult.value = '✅ 令牌刷新测试通过：令牌已刷新'
	} catch (error) {
		testResult.value = `❌ 令牌刷新测试失败：${error.message}`
	}
}

// 测试API调用
const testAPI = async () => {
	testResult.value = '正在测试API调用功能...'
	
	try {
		const response = await userAPI.getUserInfo()
		if (response.statusCode === 200) {
			testResult.value = '✅ API调用测试通过：成功获取用户信息'
		} else {
			testResult.value = `❌ API调用测试失败：状态码 ${response.statusCode}`
		}
	} catch (error) {
		testResult.value = `❌ API调用测试失败：${error.message}`
	}
}

// 获取用户信息的函数
const loadUserInfo = () => {
	const user = getUserInfo()
	if (user) {
		Object.assign(userInfo, user)
	} else {
		// 清除用户信息
		Object.assign(userInfo, {
			username: '',
			userType: ''
		})
	}
}

// 页面加载时获取用户信息
onMounted(() => {
	loadUserInfo()
})

// 页面显示时刷新用户信息
onShow(() => {
	loadUserInfo()
})
</script>

<style lang="scss" scoped>
.container {
	min-height: 100vh;
	background-color: #f5f5f5;
	padding: 32rpx;
}

.header {
	text-align: center;
	margin-bottom: 48rpx;
	
	.title {
		display: block;
		font-size: 48rpx;
		font-weight: 600;
		color: #333333;
		margin-bottom: 16rpx;
	}
	
	.subtitle {
		font-size: 28rpx;
		color: #666666;
	}
}

.section {
	background-color: #ffffff;
	border-radius: 16rpx;
	padding: 32rpx;
	margin-bottom: 32rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
	
	.section-title {
		display: block;
		font-size: 32rpx;
		font-weight: 600;
		color: #333333;
		margin-bottom: 24rpx;
	}
}

.status-item {
	display: flex;
	align-items: center;
	margin-bottom: 16rpx;
	
	.label {
		width: 160rpx;
		font-size: 28rpx;
		color: #666666;
	}
	
	.value {
		flex: 1;
		font-size: 28rpx;
		color: #333333;
		font-weight: 500;
		
		&.success {
			color: #52c41a;
		}
		
		&.error {
			color: #ff4d4f;
		}
	}
}

.button-group {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.test-btn {
	height: 88rpx;
	background-color: #c92127;
	color: #ffffff;
	font-size: 28rpx;
	font-weight: 500;
	border: none;
	border-radius: 12rpx;
	transition: all 0.2s ease;
	
	&:active {
		background-color: #a0181e;
		transform: scale(0.98);
	}
}

.result-area {
	background-color: #f8f9fa;
	border-radius: 12rpx;
	padding: 24rpx;
	min-height: 120rpx;
	
	.result-text {
		font-size: 28rpx;
		color: #333333;
		line-height: 1.6;
	}
}
</style>
