<template>
	<view class="container">
		<view v-if="loading" class="empty">加载中...</view>
		<view v-else>
			<view v-if="list.length === 0" class="empty">暂无申请记录</view>
			<view v-else>
				<view class="item" v-for="(it,i) in list" :key="i" @click="goDetail(it)">
					<view class="item-top">
						<view class="left">
							<text class="name">{{ it.name || '-' }}</text>
							<text class="sid">学号：{{ it.studentId || '-' }}</text>
						</view>
						<text class="status" :class="tagClass(it.status)">{{ it.status }}</text>
					</view>
					<view class="item-mid">
						<text class="type">{{ it.type }}</text>
					</view>
					<view class="item-bottom">
						<text class="time-text">提交：{{ formatTime(it.submittedAt) }}</text>
						<text class="time-text" v-if="it.reviewedAt">审核：{{ formatTime(it.reviewedAt) }}</text>
					</view>
					<view class="item-preview" v-if="it.details">
						<text class="preview-text">{{ truncate(it.details, 60) }}</text>
					</view>
					<view class="item-footer">
						<text class="view-detail">查看详情 ›</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { applicationAPI } from '@/utils/api.js'

const loading = ref(true)
const list = ref([])

const tagClass = (s) => {
	if (s === '已通过') return 'success'
	if (s === '待审核') return 'warning'
	if (s === '已拒绝') return 'danger'
	return 'info'
}

const formatTime = (t) => {
	if (!t) return '-'
	return String(t).replace('T', ' ').substring(0, 19)
}

const truncate = (str, len) => {
	if (!str) return ''
	return str.length > len ? str.substring(0, len) + '...' : str
}

const goDetail = (item) => {
	// 使用全局事件传递数据，兼容所有版本的微信开发者工具
	getApp().globalData = getApp().globalData || {}
	getApp().globalData.__historyDetail = item
	uni.navigateTo({
		url: '/pages/home/party/historyDetail'
	})
}

onMounted(async () => {
    try {
        const resRaw = await applicationAPI.getMyHistory()
        const res = Array.isArray(resRaw) ? resRaw[0] : resRaw
        if (res.statusCode === 200 && res.data?.code === 200) {
            list.value = res.data.data || []
        } else {
            console.error('获取申请记录失败:', res)
        }
    } catch (e) {
        console.error('获取申请记录异常:', e)
    }
    loading.value = false
})
</script>

<style scoped>
.container { padding: 20rpx; background: #f5f5f5; min-height: 100vh; }
.empty { text-align: center; color: #888; padding: 40rpx 0; }

.item {
	background: #fff;
	padding: 24rpx 28rpx;
	border-radius: 16rpx;
	box-shadow: 0 2rpx 12rpx rgba(0,0,0,.05);
	margin-bottom: 20rpx;
}

.item-top {
	display: flex;
	justify-content: space-between;
	align-items: flex-start;
	margin-bottom: 12rpx;
}

.left { display: flex; flex-direction: column; gap: 4rpx; }
.name { color: #333; font-weight: 600; font-size: 30rpx; }
.sid { color: #888; font-size: 24rpx; }

.item-mid {
	margin-bottom: 10rpx;
}

.type { color: #e74c3c; font-weight: 600; font-size: 28rpx; }

.item-bottom {
	display: flex;
	gap: 24rpx;
	margin-bottom: 10rpx;
}

.time-text { color: #999; font-size: 24rpx; }

.item-preview {
	background: #fafafa;
	border-radius: 8rpx;
	padding: 16rpx 20rpx;
	margin-bottom: 12rpx;
}

.preview-text {
	color: #666;
	font-size: 26rpx;
	line-height: 1.6;
}

.item-footer {
	display: flex;
	justify-content: flex-end;
	padding-top: 10rpx;
	border-top: 1rpx solid #f5f5f5;
}

.view-detail {
	color: #1677ff;
	font-size: 26rpx;
	font-weight: 500;
}

.status {
	padding: 6rpx 16rpx;
	border-radius: 999rpx;
	font-size: 24rpx;
	font-weight: 600;
	flex-shrink: 0;
}

.success { background: #e6fffb; color: #08979c; }
.warning { background: #fff7e6; color: #d46b08; }
.danger { background: #fff1f0; color: #a8071a; }
.info { background: #f5f5f5; color: #666; }
</style>
