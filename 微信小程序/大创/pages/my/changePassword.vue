<template>
	<view class="container">
		<view class="header">
			<text class="title">修改密码</text>
		</view>
		
		<view class="form-container">
			<view class="form-item">
				<text class="label">原密码</text>
				<input 
					class="input" 
					type="password" 
					v-model="formData.oldPassword" 
					placeholder="请输入原密码"
				/>
			</view>
			
			<view class="form-item">
				<text class="label">新密码</text>
				<input 
					class="input" 
					type="password" 
					v-model="formData.newPassword" 
					placeholder="请输入新密码"
				/>
			</view>
			
			<view class="form-item">
				<text class="label">确认密码</text>
				<input 
					class="input" 
					type="password" 
					v-model="formData.confirmPassword" 
					placeholder="请再次输入新密码"
				/>
			</view>
		</view>
		
		<view class="button-container">
			<button class="submit-btn" @click="handleSubmit">确认修改</button>
		</view>
	</view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { userAPI } from '@/utils/api.js'

const formData = reactive({
	oldPassword: '',
	newPassword: '',
	confirmPassword: ''
})

const handleSubmit = async () => {
	// 表单验证
	if (!formData.oldPassword) {
		uni.showToast({
			title: '请输入原密码',
			icon: 'none'
		})
		return
	}
	
	if (!formData.newPassword) {
		uni.showToast({
			title: '请输入新密码',
			icon: 'none'
		})
		return
	}
	
	if (formData.newPassword.length < 6) {
		uni.showToast({
			title: '新密码长度不能少于6位',
			icon: 'none'
		})
		return
	}
	
	if (formData.newPassword !== formData.confirmPassword) {
		uni.showToast({
			title: '两次输入的密码不一致',
			icon: 'none'
		})
		return
	}
	
	// 提交修改密码请求
	uni.showLoading({
		title: '修改中...'
	})
	
	try {
		const response = await userAPI.changePassword({
			oldPassword: formData.oldPassword,
			newPassword: formData.newPassword
		})
		
		uni.hideLoading()
		uni.showToast({
			title: '密码修改成功',
			icon: 'success'
		})
		
		// 清空表单
		formData.oldPassword = ''
		formData.newPassword = ''
		formData.confirmPassword = ''
		
		// 返回上一页
		setTimeout(() => {
			uni.navigateBack()
		}, 1500)
		
	} catch (error) {
		uni.hideLoading()
		uni.showToast({
			title: error.message || '修改失败',
			icon: 'none'
		})
	}
}
</script>

<style lang="scss" scoped>
.container {
	padding: 40rpx;
	background-color: #f5f5f5;
	min-height: 100vh;
}

.header {
	text-align: center;
	margin-bottom: 60rpx;
	
	.title {
		font-size: 36rpx;
		font-weight: bold;
		color: #333;
	}
}

.form-container {
	background-color: #fff;
	border-radius: 16rpx;
	padding: 40rpx;
	margin-bottom: 60rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.form-item {
	margin-bottom: 40rpx;
	
	&:last-child {
		margin-bottom: 0;
	}
	
	.label {
		display: block;
		font-size: 28rpx;
		color: #333;
		margin-bottom: 16rpx;
		font-weight: 500;
	}
	
	.input {
		width: 100%;
		height: 80rpx;
		border: 2rpx solid #e0e0e0;
		border-radius: 8rpx;
		padding: 0 24rpx;
		font-size: 28rpx;
		background-color: #fafafa;
		
		&:focus {
			border-color: #e74c3c;
			background-color: #fff;
		}
	}
}

.button-container {
	.submit-btn {
		width: 100%;
		height: 88rpx;
		background: linear-gradient(135deg, #c92127 0%, #e74c3c 100%);
		color: #fff;
		font-size: 32rpx;
		font-weight: 600;
		border-radius: 44rpx;
		border: none;
		box-shadow: 0 8rpx 16rpx rgba(231, 76, 60, 0.3);
		
		&:active {
			transform: translateY(2rpx);
			box-shadow: 0 4rpx 8rpx rgba(231, 76, 60, 0.3);
		}
	}
}
</style>
