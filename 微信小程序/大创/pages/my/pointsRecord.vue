<template>
	<view class="container">
		<view class="page-title">积分明细</view>
		
		<!-- 总积分统计 -->
		<view class="total-stats">
			<view class="stats-item">
				<text class="stats-label">总积分</text>
				<text class="stats-value">{{ totalPoints }}</text>
			</view>
		</view>
		
		<!-- 积分明细列表 -->
		<view class="points-list">
			<view class="list-header">
				<view class="header-item">积分来源</view>
				<view class="header-item">积分变化</view>
				<view class="header-item">时间</view>
			</view>
			<view class="list-body">
				<view class="points-item" v-for="(item, index) in pointsList" :key="index">
					<view class="source">{{ item.description }}</view>
					<view class="points" :class="item.points > 0 ? 'positive' : 'negative'">
						{{ item.points > 0 ? '+' : '' }}{{ item.points }}
					</view>
					<view class="time">{{ formatTime(item.createdAt) }}</view>
				</view>
				
				<!-- 空状态提示 -->
				<view class="empty-state" v-if="pointsList.length === 0 && !loading">
					<text>暂无积分记录</text>
				</view>
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

	const pointsList = ref([])
	const totalPoints = ref(0)
	const loading = ref(false)

	// 加载积分明细
	const loadPointsList = async () => {
		loading.value = true
		try {
			console.log('🔍 开始加载积分明细')
			const res = await requestWithAuth({ url: '/points/user/list', method: 'GET' })
			console.log('📊 积分明细响应:', res)
			
			if (res.statusCode === 200 && res.data?.code === 200) {
				pointsList.value = res.data.data || []
				console.log('✅ 积分明细加载成功，记录数:', pointsList.value.length)
			} else if (res.statusCode === 401 || res.data?.code === 401) {
				uni.showToast({ title: '请先登录', icon: 'none' })
				setTimeout(() => uni.navigateTo({ url: '/pages/my/login/login' }), 600)
			} else {
				console.error('❌ 积分明细加载失败:', res.data?.message)
				uni.showToast({ title: res.data?.message || '加载失败', icon: 'none' })
			}
		} catch (e) {
			console.error('❌ 积分明细加载异常:', e)
			uni.showToast({ title: e?.message || '网络错误', icon: 'none' })
		} finally {
			loading.value = false
		}
	}

	// 加载总积分
	const loadTotalPoints = async () => {
		try {
			console.log('🔍 开始加载总积分')
			const res = await requestWithAuth({ url: '/points/user/summary', method: 'GET' })
			console.log('📊 总积分响应:', res)
			
			if (res.statusCode === 200 && res.data?.code === 200) {
				totalPoints.value = res.data.data?.total || 0
				console.log('✅ 总积分加载成功:', totalPoints.value)
			} else {
				console.error('❌ 总积分加载失败:', res.data?.message)
			}
		} catch (e) {
			console.error('❌ 总积分加载异常:', e)
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

	onMounted(() => {
		loadPointsList()
		loadTotalPoints()
	})

	onShow(() => {
		loadPointsList()
		loadTotalPoints()
	})
</script>

<style lang="scss" scoped>
	.container {
		padding: 20rpx;
		background-color: #f5f5f5;
		min-height: 100vh;
	}

	.page-title {
		font-size: 36rpx;
		font-weight: bold;
		text-align: center;
		margin-bottom: 30rpx;
		color: #333;
	}

	.total-stats {
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		border-radius: 16rpx;
		padding: 30rpx;
		margin-bottom: 30rpx;
		box-shadow: 0 8rpx 20rpx rgba(102, 126, 234, 0.3);
	}

	.stats-item {
		text-align: center;
	}

	.stats-label {
		display: block;
		font-size: 28rpx;
		color: rgba(255, 255, 255, 0.8);
		margin-bottom: 10rpx;
	}

	.stats-value {
		display: block;
		font-size: 48rpx;
		font-weight: bold;
		color: #fff;
	}

	.points-list {
		background: #fff;
		border-radius: 16rpx;
		overflow: hidden;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
	}

	.list-header {
		display: flex;
		background: #f8f9fa;
		padding: 20rpx;
		border-bottom: 1rpx solid #eee;
	}

	.header-item {
		flex: 1;
		font-size: 28rpx;
		font-weight: bold;
		color: #666;
		text-align: center;
	}

	.list-body {
		min-height: 200rpx;
	}

	.points-item {
		display: flex;
		align-items: center;
		padding: 24rpx 20rpx;
		border-bottom: 1rpx solid #f0f0f0;
	}

	.points-item:last-child {
		border-bottom: none;
	}

	.source {
		flex: 2;
		font-size: 28rpx;
		color: #333;
		margin-right: 20rpx;
	}

	.points {
		flex: 1;
		font-size: 28rpx;
		font-weight: bold;
		text-align: center;
	}

	.points.positive {
		color: #52c41a;
	}

	.points.negative {
		color: #ff4d4f;
	}

	.time {
		flex: 1.5;
		font-size: 24rpx;
		color: #999;
		text-align: right;
	}

	.empty-state {
		text-align: center;
		padding: 60rpx 20rpx;
		color: #999;
		font-size: 28rpx;
	}

	.loading {
		text-align: center;
		padding: 40rpx;
		color: #666;
		font-size: 28rpx;
	}
</style>
