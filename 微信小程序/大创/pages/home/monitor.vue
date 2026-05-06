<template>
	<view class="container">
		<view class="title">党员学习监督</view>
		<view class="table">
			<view class="table-header">
				<view class="table-cell">姓名</view>
				<view class="table-cell">学习次数</view>
				<view class="table-cell">学习时长</view>
			</view>
			<view class="table-body" v-for="(item, index) in monitor" :key="index">
				<view class="table-cell">{{item.name}}</view>
				<view class="table-cell">{{item.learningCount}}</view>
				<view class="table-cell">{{item.learningTime}} 秒</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	const monitor = [
		{
			name: '张三',
			learningCount: 5,
			learningTime: 1200
		},
		{
			name: '李四',
			learningCount: 3,
			learningTime: 800
		}
	];
</script>

<style lang="scss" scoped>
	.container {
		width: 100%;
		min-height: 100vh;
		background-color: #f5f5f5;
		display: flex;
		flex-direction: column;
		align-items: center;
		padding-top: 20rpx;
	}
	
	.title {
		font-size: 36rpx;
		color: #333;
		font-weight: bold;
		margin-bottom: 20rpx;
	}
	
	.table {
		width: 90%;
		border-collapse: collapse;
	}
	
	.table-header {
		display: flex;
		background-color: #c42127;
		color: white;
	}
	
	.table-cell {
		flex: 1;
		text-align: center;
		padding: 16rpx 0;
		font-size: 28rpx;
		border-right: 1px solid rgba(255, 255, 255, 0.3);
		&:last-child {
			border-right: none;
		}
	}
	
	.table-body {
		display: flex;
		background-color: white;
		margin-top: 10rpx;
		box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
	}
</style>