<template>
	<view class="container">
		<!-- 视频资源列表 -->
		<view class="feed">
			<view v-for="item in videoList" :key="item.id" class="card" @click="open(item)">
				<view class="meta">
					<view class="title-row">
						<view class="title">{{ item.title }}</view>
						<image v-if="item.coverUrl" class="cover-mini" :src="item.coverUrl" mode="aspectFill" />
					</view>
					<view class="sub">{{ mapType(item.type) }} · {{ formatTime(item.createdAt) }}</view>
				</view>
			</view>
			<view v-if="loadingVideo" class="loading">加载中...</view>
			<view v-else-if="!videoList.length" class="empty">暂无视频资源</view>
		</view>
	</view>
</template>

<script setup>
	import { ref, onMounted } from 'vue'
	import { requestWithAuth } from '@/utils/auth.js'
	import { onShow } from '@dcloudio/uni-app'

	const videoList = ref([])
	const loadingVideo = ref(false)

	const loadVideos = async () => {
		loadingVideo.value = true
		try {
			const res = await requestWithAuth({ url: '/study/resources?type=VIDEO', method: 'GET' })
			if (res.statusCode === 200 && res.data?.code === 200) {
				videoList.value = res.data.data || []
			} else if (res.statusCode === 401 || res.data?.code === 401) {
				uni.showToast({ title: '请先登录后再查看学习资源', icon: 'none' })
				setTimeout(() => uni.navigateTo({ url: '/pages/my/login/login' }), 600)
			}
		} catch (e) {
			uni.showToast({ title: e?.message || '网络错误', icon: 'none' })
		} finally {
			loadingVideo.value = false
		}
	}

	const mapType = (t) => ({ VIDEO: '视频' }[t] || t)
	const formatTime = (t) => {
		if (!t) return ''
		try { return new Date(t).toLocaleString() } catch { return t }
	}
	const open = async (item) => {
		// 只支持视频播放
		if (item.type === 'VIDEO') {
			uni.navigateTo({ 
				url: `/pages/home/articles/article3?src=${encodeURIComponent(item.url)}&title=${encodeURIComponent(item.title)}&id=${item.id}` 
			})
		}
	}

	onMounted(() => { loadVideos(); })
	onShow(() => { loadVideos(); })
</script>

<style lang="scss" scoped>
	.container {
		width: 100%;
		min-height: 100vh;
		background-color: #f5f5f5;
		display: flex;
		flex-direction: column;
	}

	
	// 今日头条风格列表
	.feed {
		padding: 20rpx;
		.card {
			display: flex;
			gap: 20rpx;
			padding: 20rpx;
			background: #fff;
			border-radius: 12rpx;
			margin-bottom: 16rpx;
			box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.06);
			.meta { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
			.title-row { display: flex; align-items: center; gap: 16rpx; }
			.title { font-size: 32rpx; color: #333; line-height: 46rpx; font-weight: 600; flex: 1; }
			.cover-mini { width: 180rpx; height: 110rpx; border-radius: 8rpx; background: #f0f0f0; }
			.sub { font-size: 24rpx; color: #888; }
		}
		.loading, .empty { text-align: center; color: #888; padding: 40rpx 0; }
	}
</style>
