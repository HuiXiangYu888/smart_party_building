<template>
	<view class="container">
		<!-- 顶部品牌区域 -->
		<view class="brand-section">
			<view class="logo-container">
				<image class="logo" src="/static/my/党旗.png" mode="widthFix"></image>
			</view>
			<view class="brand-text">
				<text class="app-title">智慧党建</text>
				<text class="app-subtitle">Smart Party Building</text>
			</view>
		</view>
		
		<!-- 底部白色卡片区域 -->
		<view class="login-card">
			<view class="welcome-header">
				<text class="welcome-title">欢迎登录</text>
				<text class="welcome-desc">请输入您的账号密码继续</text>
			</view>
			
			<view class="form-container">
				<view class="input-item">
					<input 
						class="input-field"
						placeholder="请输入学号或身份证号"
						placeholder-class="input-placeholder"
						v-model="loginForm.username"
						maxlength="20"
					/>
				</view>
				
				<view class="input-item">
					<input 
						class="input-field"
						type="password"
						placeholder="请输入密码"
						placeholder-class="input-placeholder"
						v-model="loginForm.password"
						maxlength="20"
					/>
				</view>
				
				<button 
					class="login-btn" 
					hover-class="login-btn-hover"
					@click="handleLogin"
					:disabled="isLoading || !isFormValid"
				>
					<text>{{ isLoading ? '登录中...' : '立即登录' }}</text>
				</button>
				
				<view class="action-links">
					<text class="link-text">忘记密码？</text>
					<text class="link-text">遇到问题</text>
				</view>
			</view>
			
			<view class="footer">
				<text class="footer-text">智慧党建系统 提供技术支持</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { saveLoginInfo, isLoggedIn } from '@/utils/auth.js'
import { userAPI } from '@/utils/api.js'

// 响应式数据
const isLoading = ref(false)
const loginForm = ref({
	username: '', // 用户名（学号或身份证号）
	password: ''
})

// 表单验证
const isFormValid = computed(() => {
	return loginForm.value.username.trim() && loginForm.value.password.trim()
})

// 登录处理
const handleLogin = async () => {
	if (isLoading.value || !isFormValid.value) return
	
	isLoading.value = true
	
	try {
		// 构建登录请求数据
		const loginData = {
			username: loginForm.value.username.trim(),
			password: loginForm.value.password
		}
		
		// 调用后端登录接口
		const response = await userAPI.login(loginData)
		
		if (response.statusCode === 200 && response.data.code === 200) {
			// 保存登录信息
			saveLoginInfo(response.data.data)
			
			uni.showToast({
				title: '登录成功',
				icon: 'success'
			})
			
			// 跳转到首页
			setTimeout(() => {
				uni.switchTab({
					url: '/pages/home/home'
				})
			}, 1500)
		} else {
			throw new Error(response.data.message || '登录失败')
		}
	} catch (error) {
		console.error('登录错误:', error)
		uni.showToast({
			title: error.message || '登录失败，请重试',
			icon: 'none'
		})
	} finally {
		isLoading.value = false
	}
}

// 页面加载时检查登录状态
onMounted(() => {
	if (isLoggedIn()) {
		// 已登录，跳转到首页
		uni.switchTab({
			url: '/pages/home/home'
		})
	}
})
</script>

<style lang="scss" scoped>
.container {
	min-height: 100vh;
	background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
	display: flex;
	flex-direction: column;
	position: relative;
}

.brand-section {
	height: 38vh;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding-bottom: 20rpx;
	
	.logo-container {
		width: 180rpx;
		background-color: #fff;
		border-radius: 16rpx;
		padding: 10rpx;
		box-sizing: border-box;
		box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.1);
		margin-bottom: 24rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}
	
	.logo {
		width: 100%;
		height: auto;
		display: block;
	}
	
	.brand-text {
		text-align: center;
		.app-title {
			display: block;
			font-size: 44rpx;
			font-weight: bold;
			color: #ffffff;
			letter-spacing: 4rpx;
			margin-bottom: 8rpx;
		}
		.app-subtitle {
			font-size: 24rpx;
			color: rgba(255, 255, 255, 0.8);
			letter-spacing: 2rpx;
		}
	}
}

.login-card {
	flex: 1;
	background-color: #ffffff;
	border-radius: 48rpx 48rpx 0 0;
	padding: 60rpx 50rpx;
	display: flex;
	flex-direction: column;
	box-shadow: 0 -10rpx 30rpx rgba(0,0,0,0.05);
	
	.welcome-header {
		margin-bottom: 60rpx;
		.welcome-title {
			display: block;
			font-size: 40rpx;
			font-weight: bold;
			color: #333333;
			margin-bottom: 12rpx;
		}
		.welcome-desc {
			font-size: 26rpx;
			color: #999999;
		}
	}
	
	.form-container {
		flex: 1;
		
		.input-item {
			display: flex;
			align-items: center;
			background-color: #f8f9fa;
			border-radius: 24rpx;
			height: 100rpx;
			padding: 0 32rpx;
			margin-bottom: 40rpx;
			border: 2rpx solid transparent;
			transition: all 0.3s;
			
			&:focus-within {
				border-color: rgba(231, 76, 60, 0.3);
				background-color: #ffffff;
			}
			
			.input-field {
				flex: 1;
				height: 100%;
				font-size: 30rpx;
				color: #333333;
			}
			
			.input-placeholder {
				color: #bbbbbb;
			}
		}
		
		.login-btn {
			background: linear-gradient(90deg, #e74c3c, #c0392b);
			color: #ffffff;
			border-radius: 50rpx;
			height: 96rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			font-size: 32rpx;
			font-weight: bold;
			margin-top: 60rpx;
			box-shadow: 0 10rpx 20rpx rgba(231, 76, 60, 0.3);
			border: none;
			
			&::after {
				border: none;
			}
			
			&[disabled] {
				background: #cccccc;
				box-shadow: none;
				color: #ffffff;
			}
		}
		
		.login-btn-hover {
			opacity: 0.9;
			transform: translateY(2rpx);
		}
		
		.action-links {
			display: flex;
			justify-content: space-between;
			margin-top: 40rpx;
			padding: 0 20rpx;
			
			.link-text {
				font-size: 26rpx;
				color: #666666;
			}
		}
	}
	
	.footer {
		text-align: center;
		padding-top: 40rpx;
		padding-bottom: env(safe-area-inset-bottom);
		
		.footer-text {
			font-size: 22rpx;
			color: #cccccc;
		}
	}
}
</style>
