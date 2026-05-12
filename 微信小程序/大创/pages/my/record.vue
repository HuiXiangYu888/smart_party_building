<template>
	<view class="container">
		<view class="page-title">学习记录</view>
		
		<!-- 总学习时长统计 -->
		<view class="total-stats">
			<view class="stats-item">
				<text class="stats-label">总学习时长</text>
				<text class="stats-value">{{ formatDuration(totalDuration) }}</text>
			</view>
		</view>
		
		<view class="head">
			<view>视频标题</view>
			<view>开始时间</view>
			<view>学习时长</view>
		</view>
		<view class="body">
			<view class="record-item" v-for="(item, index) in records" :key="index">
				<view class="title">{{ (item.title || item.videoTitle || '') || '未知视频' }}</view>
				<view class="time">{{ formatTime(item.startedAt) }}</view>
				<view class="duration">{{ formatDuration(item.duration) }}</view>
			</view>
			
			<!-- 空状态提示 -->
			<view class="empty-state" v-if="records.length === 0">
				<text>暂无学习记录</text>
			</view>
		</view>
		
		<!-- 加载状态 -->
		<view class="loading" v-if="loading">
			<text>加载中...</text>
		</view>
	</view>
</template>
	
<script setup>
	import { ref, onMounted } from 'vue'
	import { onShow } from '@dcloudio/uni-app'
	import { requestWithAuth } from '@/utils/auth.js'

	const records = ref([])
	const totalDuration = ref(0)
	const loading = ref(false)

	// 加载学习记录
	const loadStudyRecords = async () => {
		loading.value = true
		try {
			const res = await requestWithAuth({ url: '/study/records/user', method: 'GET' })
			if (res.statusCode === 200 && res.data?.code === 200) {
				records.value = res.data.data || []
			} else if (res.statusCode === 401 || res.data?.code === 401) {
				uni.showToast({ title: '请先登录', icon: 'none' })
				setTimeout(() => uni.navigateTo({ url: '/pages/my/login/login' }), 600)
			}
		} catch (e) {
			uni.showToast({ title: e?.message || '网络错误', icon: 'none' })
		} finally {
			loading.value = false
		}
	}

	// 加载总学习时长
	const loadTotalDuration = async () => {
		try {
			const res = await requestWithAuth({ url: '/study/records/user/total-duration', method: 'GET' })
			if (res.statusCode === 200 && res.data?.code === 200) {
				totalDuration.value = res.data.data || 0
			}
		} catch (e) {
			console.error('加载总学习时长失败:', e)
		}
	}

	// 格式化时间
	const formatTime = (timeStr) => {
		if (!timeStr) return ''
		try {
			const date = new Date(timeStr)
			return date.toLocaleString('zh-CN', {
				year: 'numeric',
				month: '2-digit',
				day: '2-digit',
				hour: '2-digit',
				minute: '2-digit'
			})
		} catch {
			return timeStr
		}
	}

	// 格式化时长
	const formatDuration = (minutes) => {
		if (!minutes || minutes === 0) return '0分钟'
		const hours = Math.floor(minutes / 60)
		const mins = minutes % 60
		if (hours > 0) {
			return `${hours}小时${mins}分钟`
		}
		return `${mins}分钟`
	}

	onMounted(() => {
		loadStudyRecords()
		loadTotalDuration()
	})

	onShow(() => {
		loadStudyRecords()
		loadTotalDuration()
	})
</script>

<style lang="scss" scoped>
	.container {
		width: 100%;
		min-height: 100vh;
		background-color: #f5f7fa;
		padding: 20rpx;
		box-sizing: border-box;
	}
	
	.page-title {
		font-size: 36rpx;
		font-weight: bold;
		color: #333;
		margin: 30rpx 0;
		text-align: center;
		position: relative;
		display: inline-block;
		left: 50%;
		transform: translateX(-50%);
	}
	
	.page-title::after {
		content: '';
		position: absolute;
		bottom: -10rpx;
		left: 50%;
		transform: translateX(-50%);
		width: 40rpx;
		height: 6rpx;
		background-color: #e74c3c;
		border-radius: 4rpx;
	}
	
	.total-stats {
		background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
		border-radius: 16rpx;
		padding: 40rpx 30rpx;
		margin-bottom: 30rpx;
		box-shadow: 0 8rpx 20rpx rgba(231, 76, 60, 0.3);
		position: relative;
		overflow: hidden;
		
		&::before {
			content: '';
			position: absolute;
			top: -20rpx;
			right: -20rpx;
			width: 150rpx;
			height: 150rpx;
			background: rgba(255, 255, 255, 0.1);
			border-radius: 50%;
		}
		
		.stats-item {
			display: flex;
			justify-content: space-between;
			align-items: center;
			position: relative;
			z-index: 1;
			
			.stats-label {
				font-size: 30rpx;
				color: rgba(255, 255, 255, 0.9);
			}
			
			.stats-value {
				font-size: 42rpx;
				font-weight: bold;
				color: #fff;
				text-shadow: 0 4rpx 8rpx rgba(0, 0, 0, 0.1);
			}
		}
	}
	
	.head {
		display: flex;
		justify-content: space-between;
		background-color: #fff5f5;
		padding: 24rpx 30rpx;
		border-radius: 12rpx;
		margin-bottom: 10rpx;
		border-bottom: 2rpx solid #ffe8e8;
		
		view {
			flex: 1;
			text-align: center;
			font-size: 28rpx;
			font-weight: bold;
			color: #e74c3c;
		}
	}
	
	.body {
		width: 100%;
		background: #fff;
		border-radius: 16rpx;
		overflow: hidden;
		box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
	}
	
	.record-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		background-color: #fff;
		padding: 30rpx;
		border-bottom: 1rpx solid #f0f0f0;
		transition: background-color 0.2s ease;
		
		&:active {
			background-color: #fafafa;
		}
		
		view {
			flex: 1;
			text-align: center;
			font-size: 26rpx;
			color: #333;
		}
		
		.title {
			font-weight: 500;
		}
		
		.time {
			color: #888;
			font-size: 24rpx;
		}
		
		.duration {
			color: #e74c3c;
			font-weight: bold;
			font-size: 28rpx;
		}
	}
	
	.empty-state {
		text-align: center;
		padding: 100rpx 0;
		color: #999;
		font-size: 28rpx;
	}
	
	.loading {
		text-align: center;
		padding: 40rpx 0;
		color: #666;
		font-size: 26rpx;
	}
</style>
