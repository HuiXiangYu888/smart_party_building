<template>
	<!-- 顶部用户信息 -->
	<view class="user-header" @click="handleUserClick">
		<image class="user-avatar" :src="userInfo.avatar || '/static/my/头像.png'" mode="aspectFill"></image>
		<view class="user-info">
			<text class="user-name">{{ userInfo.username || '点击登录' }}</text>
			<text class="user-type">{{ getUserTypeText() }}</text>
		</view>
		<view class="user-action">
			<text v-if="isUserLoggedIn" class="logout-btn" @click.stop="handleLogout">退出</text>
			<text v-else class="login-btn">登录</text>
		</view>
	</view>
	
	<!--横栏-->
	<view class="banner">
		<image src="/static/my/danghui.png"></image>
		<text class="banner-text">智慧党建系统</text>
	</view>
		
	<!-- 功能菜单 -->
	<view class="menu-list">
		<view class="menu-item" @click="goMyActivity">
			<image class="menu-icon" src="/static/my/我的活动.png"></image>
			<text class="menu-text">我的活动</text>
			<text class="menu-arrow">></text>
		</view>
		<view class="menu-item" @click="goPointsRank">
			<image class="menu-icon" src="/static/my/学习记录.png"></image>
			<text class="menu-text">积分排行</text>
			<text class="menu-arrow">></text>
		</view>
		<view class="menu-item" @click="goChangePassword">
			<image class="menu-icon" src="/static/my/我的活动.png"></image>
			<text class="menu-text">修改密码</text>
			<text class="menu-arrow">></text>
		</view>
	</view>
	    
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getUserInfo, isLoggedIn, logout, checkPermission } from '@/utils/auth.js'
import eventBus, { EVENTS } from '@/utils/eventBus.js'

// 响应式数据
const userInfo = reactive({
	username: '',
	userType: '',
	avatar: ''
})

// 计算属性
const isUserLoggedIn = computed(() => {
	return userInfo.username && userInfo.username.trim() !== ''
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

// 处理用户点击
const handleUserClick = () => {
	if (!isUserLoggedIn.value) {
		uni.navigateTo({
			url: '/pages/my/login/login',
			fail: (err) => {
				console.error('跳转登录页面失败:', err)
				uni.showToast({
					title: '页面跳转失败',
					icon: 'none'
				})
			}
		})
	}
}

// 处理登出
const handleLogout = () => {
	uni.showModal({
		title: '确认退出',
		content: '确定要退出登录吗？',
		success: (res) => {
			if (res.confirm) {
				logout()
				// 登出后立即刷新用户信息
				loadUserInfo()
				
				// 显示退出成功提示
				uni.showToast({
					title: '已退出登录',
					icon: 'success',
					duration: 1500
				})
				
				// 可以选择跳转到登录页面（可选）
				// setTimeout(() => {
				// 	uni.navigateTo({
				// 		url: '/pages/my/login/login'
				// 	})
				// }, 1500)
			}
		}
	})
}

function goMyActivity(){
	if (!isUserLoggedIn.value) {
		uni.showToast({
			title: '请先登录',
			icon: 'none'
		})
		return
	}
	
	uni.navigateTo({
		url:"/pages/my/myActivity"
	})
}

function goPointsRank(){
	if (!isUserLoggedIn.value) {
		uni.showToast({
			title: '请先登录',
			icon: 'none'
		})
		return
	}
	
	uni.navigateTo({
        url:"/pages/my/pointsRank"
	})
}

function goChangePassword(){
	if (!isUserLoggedIn.value) {
		uni.showToast({
			title: '请先登录',
			icon: 'none'
		})
		return
	}
	
	uni.navigateTo({
		url:"/pages/my/changePassword"
	})
}

function goMemberProfile(){
	if (!isUserLoggedIn.value) {
		uni.showToast({ title: '请先登录', icon: 'none' })
		return
	}
	uni.navigateTo({ url: '/pages/my/memberInfo' })
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
			userType: '',
			avatar: ''
		})
	}
}


// 事件监听器
let loginListener = null
let logoutListener = null
let userInfoUpdateListener = null

// 页面加载时获取用户信息
onMounted(() => {
	loadUserInfo()
	
	// 监听登录事件
	loginListener = (userData) => {
		console.log('收到登录事件，更新用户信息:', userData)
		Object.assign(userInfo, userData)
	}
	
	// 监听退出登录事件
	logoutListener = () => {
		console.log('收到退出登录事件，清除用户信息')
		Object.assign(userInfo, {
			username: '',
			userType: '',
			avatar: ''
		})
	}
	
	// 监听用户信息更新事件
	userInfoUpdateListener = (updatedInfo) => {
		console.log('收到用户信息更新事件:', updatedInfo)
		Object.assign(userInfo, updatedInfo)
	}
	
	// 注册事件监听器
	eventBus.on(EVENTS.USER_LOGIN, loginListener)
	eventBus.on(EVENTS.USER_LOGOUT, logoutListener)
	eventBus.on(EVENTS.USER_INFO_UPDATED, userInfoUpdateListener)
})

// 页面显示时刷新用户信息（解决登录后不显示姓名的问题）
onShow(() => {
	loadUserInfo()
})

// 页面卸载时移除事件监听器
onUnmounted(() => {
	if (loginListener) {
		eventBus.off(EVENTS.USER_LOGIN, loginListener)
	}
	if (logoutListener) {
		eventBus.off(EVENTS.USER_LOGOUT, logoutListener)
	}
	if (userInfoUpdateListener) {
		eventBus.off(EVENTS.USER_INFO_UPDATED, userInfoUpdateListener)
	}
})
</script>

<style lang="scss" scoped>
	.user-header {
		display: flex;
		align-items: center;
		padding: 40rpx 32rpx;
		background: linear-gradient(135deg, #c92127 0%, #e74c3c 100%);
		border-radius: 0 0 32rpx 32rpx;
		margin-bottom: 32rpx;
		
		.user-avatar {
			width: 120rpx;
			height: 120rpx;
			border-radius: 60rpx;
			margin-right: 24rpx;
			border: 4rpx solid rgba(255, 255, 255, 0.3);
			box-sizing: border-box;
		}
		
		.user-info {
			flex: 1;
			display: flex;
			flex-direction: column;
			
			.user-name {
				font-size: 36rpx;
				font-weight: 600;
				color: #ffffff;
				margin-bottom: 8rpx;
			}
			
			.user-type {
				font-size: 24rpx;
				color: rgba(255, 255, 255, 0.8);
			}
		}
		
		.user-action {
			.logout-btn, .login-btn {
				padding: 16rpx 32rpx;
				background-color: rgba(255, 255, 255, 0.2);
				color: #ffffff;
				font-size: 28rpx;
				border-radius: 32rpx;
				border: 2rpx solid rgba(255, 255, 255, 0.3);
			}
		}
	}
	
	.banner {
		display: flex;
		align-items: center;
		height: 120rpx;
		margin: 0 32rpx 32rpx;
		background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
		border-radius: 24rpx;
		padding: 0 32rpx;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
		
		image {
			width: 80rpx;
			height: 80rpx;
			margin-right: 24rpx;
		}
		
		.banner-text {
			font-size: 32rpx;
			font-weight: 600;
			color: #c92127;
		}
	}
	
	.menu-list {
		padding: 0 32rpx;
		
		.menu-item {
			display: flex;
			align-items: center;
			padding: 32rpx 0;
			border-bottom: 1rpx solid #f0f0f0;
			transition: all 0.2s ease;
			
			&:last-child {
				border-bottom: none;
			}
			
			&:active {
				background-color: #f8f9fa;
			}
			
			.menu-icon {
				width: 48rpx;
				height: 48rpx;
				margin-right: 24rpx;
			}
			
			.menu-text {
				flex: 1;
				font-size: 32rpx;
				color: #333333;
				font-weight: 500;
			}
			
			.menu-arrow {
				font-size: 32rpx;
				color: #c9cdd4;
				font-weight: 300;
			}
		}
	}
</style>
