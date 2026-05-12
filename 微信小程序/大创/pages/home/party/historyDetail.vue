<template>
	<view class="detail-page">
		<!-- 状态卡片 -->
		<view class="status-card">
			<view class="status-header">
				<text class="type-label">{{ detail.type }}</text>
				<text class="status-tag" :class="tagClass(detail.status)">{{ detail.status }}</text>
			</view>
			<view class="info-row">
				<text class="info-label">申请人</text>
				<text class="info-value">{{ detail.name || '-' }}</text>
			</view>
			<view class="info-row">
				<text class="info-label">学号</text>
				<text class="info-value">{{ detail.studentId || '-' }}</text>
			</view>
			<view class="info-row">
				<text class="info-label">提交时间</text>
				<text class="info-value">{{ formatTime(detail.submittedAt) }}</text>
			</view>
			<view class="info-row" v-if="detail.reviewedAt">
				<text class="info-label">审核时间</text>
				<text class="info-value">{{ formatTime(detail.reviewedAt) }}</text>
			</view>
		</view>

		<!-- 申请内容 -->
		<view class="section-card">
			<view class="section-title">
				<view class="title-dot"></view>
				<text>申请内容</text>
			</view>
			<view class="section-body">
				<text class="content-text" v-if="detail.details">{{ detail.details }}</text>
				<text class="empty-text" v-else>无申请内容</text>
			</view>
		</view>

		<!-- 附件图片 -->
		<view class="section-card" v-if="detail.attachments && detail.attachments.length > 0">
			<view class="section-title">
				<view class="title-dot"></view>
				<text>附件（{{ detail.attachments.length }}张）</text>
			</view>
			<view class="section-body">
				<view class="attachment-grid">
					<image 
						v-for="(url, i) in detail.attachments" 
						:key="i" 
						:src="url" 
						mode="aspectFill" 
						class="attachment-img"
						@click="previewImage(i)"
					/>
				</view>
			</view>
		</view>

		<!-- 审核意见 -->
		<view class="section-card">
			<view class="section-title">
				<view class="title-dot" style="background-color:#fa8c16"></view>
				<text>审核意见</text>
			</view>
			<view class="section-body">
				<text class="content-text" v-if="detail.comments">{{ detail.comments }}</text>
				<text class="empty-text" v-else>{{ detail.status === '待审核' ? '等待审核中...' : '无审核意见' }}</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const detail = ref({})

const tagClass = (s) => {
	if (s === '已通过') return 'success'
	if (s === '待审核') return 'warning'
	if (s === '已拒绝') return 'danger'
	return 'info'
}

const formatTime = (t) => {
	if (!t) return '-'
	// 后端返回的可能是 "2026-05-12T19:12:34" 格式
	return String(t).replace('T', ' ').substring(0, 19)
}

const previewImage = (index) => {
	const urls = detail.value.attachments || []
	uni.previewImage({
		current: index,
		urls: urls
	})
}

onMounted(() => {
	// 从 globalData 中获取数据（兼容所有版本微信开发者工具）
	const app = getApp()
	if (app && app.globalData && app.globalData.__historyDetail) {
		detail.value = app.globalData.__historyDetail
		// 用完即清，防止脏数据
		delete app.globalData.__historyDetail
	}
})
</script>

<style scoped>
.detail-page {
	padding: 20rpx;
	background-color: #f5f5f5;
	min-height: 100vh;
}

.status-card {
	background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
	color: #fff;
	box-shadow: 0 4rpx 16rpx rgba(231, 76, 60, 0.3);
}

.status-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 24rpx;
	padding-bottom: 20rpx;
	border-bottom: 1rpx solid rgba(255,255,255,0.2);
}

.type-label {
	font-size: 34rpx;
	font-weight: 700;
}

.status-tag {
	padding: 8rpx 20rpx;
	border-radius: 999rpx;
	font-size: 24rpx;
	font-weight: 600;
}

.success { background: rgba(255,255,255,0.95); color: #08979c; }
.warning { background: rgba(255,255,255,0.95); color: #d46b08; }
.danger { background: rgba(255,255,255,0.95); color: #a8071a; }
.info { background: rgba(255,255,255,0.95); color: #666; }

.info-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 8rpx 0;
}

.info-label {
	font-size: 26rpx;
	color: rgba(255,255,255,0.75);
}

.info-value {
	font-size: 26rpx;
	color: #fff;
	font-weight: 500;
}

.section-card {
	background: #fff;
	border-radius: 16rpx;
	margin-bottom: 20rpx;
	overflow: hidden;
	box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
}

.section-title {
	display: flex;
	align-items: center;
	padding: 24rpx 30rpx;
	background-color: #fafafa;
	border-bottom: 1rpx solid #f0f0f0;
	font-size: 30rpx;
	font-weight: 600;
	color: #333;
}

.title-dot {
	width: 8rpx;
	height: 28rpx;
	background-color: #e74c3c;
	border-radius: 4rpx;
	margin-right: 14rpx;
}

.section-body {
	padding: 24rpx 30rpx;
}

.content-text {
	font-size: 28rpx;
	color: #333;
	line-height: 1.8;
	word-break: break-all;
}

.empty-text {
	font-size: 28rpx;
	color: #999;
}

.attachment-grid {
	display: flex;
	flex-wrap: wrap;
	gap: 16rpx;
}

.attachment-img {
	width: 200rpx;
	height: 200rpx;
	border-radius: 12rpx;
	background: #f2f3f5;
}
</style>
