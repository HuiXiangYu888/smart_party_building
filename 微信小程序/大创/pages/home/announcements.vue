<template>
	<view class="container">
		<view class="header">
			<text class="title">党建公告</text>
		</view>
		
		
		<!-- 公告列表 -->
		<view class="announcement-list">
			<view 
				class="announcement-item" 
				v-for="(item, index) in announcementList" 
				:key="item.id"
				@click="handleViewDetail(item)"
			>
				<view class="item-header">
					<text class="item-title">{{ item.title }}</text>
					<text class="item-time">{{ formatTime(item.createdAt) }}</text>
				</view>
			</view>
			
			<!-- 空状态 -->
			<view class="empty-state" v-if="announcementList.length === 0 && !loading">
				<text>暂无公告</text>
			</view>
		</view>
		
		<!-- 加载状态 -->
		<view class="loading" v-if="loading">
			<text>加载中...</text>
		</view>
		
		<!-- 加载更多 -->
		<view class="load-more" v-if="hasMore && !loading">
			<button class="load-more-btn" @click="loadMore">加载更多</button>
		</view>
	</view>
</template>

<script setup>
	import { ref, onMounted } from 'vue'
	import { onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
	import { requestWithAuth } from '@/utils/auth.js'

	const announcementList = ref([])
	const loading = ref(false)
	const hasMore = ref(true)
	const currentPage = ref(1)
	const pageSize = ref(10)

	// 格式化时间
	const formatTime = (timeStr) => {
		if (!timeStr) return ''
		try {
			const date = new Date(timeStr)
			const now = new Date()
			const diff = now - date
			
			// 小于1分钟
			if (diff < 60000) {
				return '刚刚'
			}
			// 小于1小时
			if (diff < 3600000) {
				return Math.floor(diff / 60000) + '分钟前'
			}
			// 小于1天
			if (diff < 86400000) {
				return Math.floor(diff / 3600000) + '小时前'
			}
			// 小于7天
			if (diff < 604800000) {
				return Math.floor(diff / 86400000) + '天前'
			}
			// 超过7天显示具体日期
			return date.toLocaleDateString('zh-CN')
		} catch {
			return timeStr
		}
	}

	// 去除HTML标签
	const stripHtmlTags = (html) => {
		if (!html) return ''
		return html.replace(/<[^>]+>/g, '').replace(/&nbsp;/ig, ' ')
	}

	// 查看详情
	const handleViewDetail = (item) => {
		uni.navigateTo({
			url: `/pages/home/announcement-detail?id=${item.id}&title=${encodeURIComponent(item.title)}`
		})
	}

	// 加载数据
	const loadData = async () => {
		if (loading.value) return
		
		loading.value = true
		try {
			const params = {
				page: currentPage.value,
				size: pageSize.value
			}
			
			const res = await requestWithAuth({ 
				url: '/announcements/user', 
				method: 'GET',
				data: params
			})
			
			if (res.statusCode === 200 && res.data?.code === 200) {
				const newData = res.data.data || []
				
				if (currentPage.value === 1) {
					announcementList.value = newData
				} else {
					announcementList.value.push(...newData)
				}
				
				// 判断是否还有更多数据
				hasMore.value = newData.length === pageSize.value
			} else if (res.statusCode === 401 || res.data?.code === 401) {
				uni.showToast({ title: '请先登录', icon: 'none' })
				setTimeout(() => uni.navigateTo({ url: '/pages/my/login/login' }), 600)
			}
		} catch (e) {
			console.error('加载公告失败:', e)
			uni.showToast({ title: '加载失败', icon: 'none' })
		} finally {
			loading.value = false
		}
	}

	// 加载更多
	const loadMore = () => {
		if (hasMore.value && !loading.value) {
			currentPage.value++
			loadData()
		}
	}

	// 下拉刷新
	const onRefresh = () => {
		currentPage.value = 1
		announcementList.value = []
		hasMore.value = true
		loadData()
	}

	onMounted(() => {
		loadData()
	})

	onShow(() => {
		// 页面显示时刷新数据
		onRefresh()
	})

	onPullDownRefresh(() => {
		onRefresh()
		setTimeout(() => {
			uni.stopPullDownRefresh()
		}, 1000)
	})

	onReachBottom(() => {
		loadMore()
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


	.announcement-list {
		.announcement-item {
			background: white;
			border-radius: 10rpx;
			padding: 30rpx;
			margin-bottom: 20rpx;
			box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.1);
			
			.item-header {
				display: flex;
				justify-content: space-between;
				align-items: flex-start;
				margin-bottom: 20rpx;
				
				.item-title {
					font-size: 32rpx;
					font-weight: bold;
					color: #333;
					flex: 1;
					margin-right: 20rpx;
				}
				
				.item-time {
					font-size: 24rpx;
					color: #999;
					white-space: nowrap;
				}
			}
			
			.item-content {
				.content-text {
					font-size: 28rpx;
					color: #666;
					line-height: 1.6;
				}
			}
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
		color: #999;
		font-size: 28rpx;
	}

	.load-more {
		text-align: center;
		padding: 40rpx 0;
		
		.load-more-btn {
			background: #007aff;
			color: white;
			border: none;
			border-radius: 30rpx;
			padding: 20rpx 60rpx;
			font-size: 28rpx;
		}
	}
</style>
