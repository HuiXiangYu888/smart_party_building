<template>
	<view class="container">
		<view class="title">请选择你的身份</view>
		<view class="identity-list">
			<view class="identity-item" @click="handlePartyMember">党员</view>
			<view class="identity-item" @click="handleBranchAdmin">党支部管理员</view>
		</view>
	</view>
</template>

<script setup>
	
	const handlePartyMember = () => {
		uni.navigateTo({
			url:"/pages/my/login/login1"
		})
		
	}
	
	
	const handleBranchAdmin = () => {
		uni.navigateTo({
			url:"/pages/my/login/login2"
		})
	}
</script>

<style lang="scss" scoped>
	.container {
		min-height: 100vh;
		background-color: #f5f7fa;
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 50rpx 30rpx;
	}
	
	.title {
		font-size: 36rpx;
		font-weight: 600;
		color: #333;
		margin-bottom: 80rpx;
		padding-bottom: 20rpx;
		border-bottom: 2px solid #1E88E5;
	}
	
	.identity-list {
		width: 100%;
		max-width: 500rpx;
		display: flex;
		flex-direction: column;
		gap: 30rpx;
	}
	
	.identity-item {
		height: 120rpx;
		background-color: #fff;
		border-radius: 16rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 32rpx;
		color: #333;
		box-shadow: 0 4rpx 10rpx rgba(0, 0, 0, 0.05);
		transition: all 0.3s ease;
		border: 2px solid transparent;
		
		&:hover {
			background-color: #f0f7ff;
			border-color: #1E88E5;
		}
		
		&:active {
			transform: scale(0.98);
			box-shadow: 0 2rpx 5rpx rgba(0, 0, 0, 0.1);
		}
	}
</style>
