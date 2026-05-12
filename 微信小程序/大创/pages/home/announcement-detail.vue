<template>
	<view class="container">
		<view class="header">
			<text class="title">{{ announcement.title || '公告详情' }}</text>
		</view>
		
		<view class="content" v-if="announcement.id">
			<!-- 公告信息 -->
			<view class="announcement-info">
				<view class="info-item">
					<text class="label">发布时间：</text>
					<text class="value">{{ formatTime(announcement.createdAt) }}</text>
				</view>
			</view>
			
			<!-- 公告内容 -->
			<view class="announcement-content">
				<rich-text class="content-text" :nodes="formatContent(announcement.content)"></rich-text>
			</view>
		</view>
		
		<!-- 加载状态 -->
		<view class="loading" v-if="loading">
			<text>加载中...</text>
		</view>
		
		<!-- 错误状态 -->
		<view class="error" v-if="error">
			<text>{{ error }}</text>
			<button class="retry-btn" @click="loadData">重试</button>
		</view>
	</view>
</template>

<script setup>
	import { ref, onMounted } from 'vue'
	import { onLoad } from '@dcloudio/uni-app'
	import { requestWithAuth } from '@/utils/auth.js'

	const announcement = ref({})
	const loading = ref(false)
	const error = ref('')

	// 格式化富文本内容
	const formatContent = (html) => {
		if (!html) return ''
		// 替换图片样式，保证自适应宽度，避免横向滚动
		return html.replace(/<img[^>]*>/gi, function(match) {
			return match.replace(/style\s*=\s*["'][^"']*["']/gi, '')
				.replace(/width\s*=\s*["'][^"']*["']/gi, '')
				.replace(/height\s*=\s*["'][^"']*["']/gi, '')
				.replace(/<img/gi, '<img style="max-width:100%; height:auto; display:block; margin:10px 0; border-radius:8px;"');
		})
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

	// 加载公告详情
	const loadData = async () => {
		if (loading.value) return
		
		loading.value = true
		error.value = ''
		
		try {
			const res = await requestWithAuth({ 
				url: `/announcements/${announcement.value.id}`, 
				method: 'GET'
			})
			
			if (res.statusCode === 200 && res.data?.code === 200) {
				announcement.value = res.data.data || {}
			} else if (res.statusCode === 404 || res.data?.code === 404) {
				error.value = '公告不存在'
			} else if (res.statusCode === 401 || res.data?.code === 401) {
				uni.showToast({ title: '请先登录', icon: 'none' })
				setTimeout(() => uni.navigateTo({ url: '/pages/my/login/login' }), 600)
			} else {
				error.value = '加载失败'
			}
		} catch (e) {
			console.error('加载公告详情失败:', e)
			error.value = '网络错误'
		} finally {
			loading.value = false
		}
	}

	onLoad((options) => {
		if (options.id) {
			announcement.value.id = parseInt(options.id)
			announcement.value.title = options.title ? decodeURIComponent(options.title) : '公告详情'
			loadData()
		} else {
			error.value = '参数错误'
		}
	})
</script>

<style lang="scss" scoped>
	.container {
		padding: 20rpx;
		background-color: #f5f5f5;
		min-height: 100vh;
	}

	.header {
		text-align: center;
		margin-bottom: 30rpx;
		
		.title {
			font-size: 36rpx;
			font-weight: bold;
			color: #333;
		}
	}

	.content {
		background: white;
		border-radius: 10rpx;
		padding: 30rpx;
		box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.1);
	}

	.announcement-info {
		border-bottom: 2rpx solid #f0f0f0;
		padding-bottom: 20rpx;
		margin-bottom: 30rpx;
		
		.info-item {
			display: flex;
			align-items: center;
			
			.label {
				font-size: 26rpx;
				color: #999;
				margin-right: 10rpx;
			}
			
			.value {
				font-size: 26rpx;
				color: #666;
			}
		}
	}

	.announcement-content {
		.content-text {
			font-size: 30rpx;
			color: #333;
			line-height: 1.8;
			white-space: pre-wrap;
		}
	}

	.loading {
		text-align: center;
		padding: 100rpx 0;
		color: #999;
		font-size: 28rpx;
	}

	.error {
		text-align: center;
		padding: 100rpx 0;
		
		text {
			display: block;
			color: #f56c6c;
			font-size: 28rpx;
			margin-bottom: 30rpx;
		}
		
		.retry-btn {
			background: #007aff;
			color: white;
			border: none;
			border-radius: 30rpx;
			padding: 20rpx 60rpx;
			font-size: 28rpx;
		}
	}
</style>
