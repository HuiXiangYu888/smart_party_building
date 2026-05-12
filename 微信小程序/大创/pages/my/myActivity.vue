<template>
	<div class="container">
		<div class="header">
			<div class="title-line"></div>
			<h1>活动记录</h1>
		</div>
		<div v-if="list.length === 0" class="empty">暂无活动记录</div>
		<div v-else>
			<div class="activity-card" v-for="(item, index) in list" :key="item.id || index">
				<div class="card-top">
					<h2 class="title">{{ item.title }}</h2>
					<div class="status-tag" :class="getStatusClass(item.status)">{{ translateStatus(item.status) }}</div>
				</div>
				<div class="meta-info">
					<div class="info-item">
						<text class="label">获得积分</text>
						<text class="value points">+{{ item.points }}</text>
					</div>
					<div class="info-item">
						<text class="label">截止日期</text>
						<text class="value">{{ formatDate(item.dueDate) }}</text>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup>
	import { ref, onMounted } from 'vue';
	import { onShow } from '@dcloudio/uni-app';
	import { taskAPI } from '../../utils/api.js'

	const list = ref([])

	const formatDate = (str) => {
		if (!str) return ''
		try { return new Date(str).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }) } catch { return str }
	}

	const translateStatus = (s) => {
		if (s === 'PUBLISHING') return '发布中'
		if (s === 'PENDING_END') return '待完结'
		if (s === 'ENDED') return '已完结'
		return s || ''
	}

	const getStatusClass = (s) => {
		if (s === 'PUBLISHING') return 'status-active'
		if (s === 'PENDING_END') return 'status-warning'
		if (s === 'ENDED') return 'status-ended'
		return 'status-default'
	}

	const loadMyTasks = async () => {
		try {
			const res = await taskAPI.getMyTasks()
			const raw = res?.data?.data || []
			list.value = raw.map(it => ({
				id: it.id,
				title: it.title,
				dueDate: it.due_date || it.dueDate,
				points: it.points,
				status: it.status,
				assignmentStatus: it.assignment_status || it.assignmentStatus
			}))
		} catch (e) { console.error('获取活动记录失败', e) }
	}

	onMounted(loadMyTasks)
	onShow(loadMyTasks)
</script>

<style lang="scss" scoped>
	.container { background-color: #f5f5f5; min-height: 100vh; padding: 20rpx; box-sizing: border-box; }
	
	.header {
		display: flex;
		align-items: center;
		margin-bottom: 30rpx;
		padding: 10rpx 0;
	}
	
	.title-line {
		width: 8rpx;
		height: 36rpx;
		background-color: #e74c3c;
		border-radius: 4rpx;
		margin-right: 16rpx;
	}
	
	.header h1 { font-size: 36rpx; color: #333; font-weight: bold; margin: 0; }
	
	.activity-card {
		background: #fff;
		border-radius: 16rpx;
		padding: 30rpx;
		margin-bottom: 24rpx;
		box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
		border-left: 6rpx solid #e74c3c;
		transition: transform 0.2s;
		
		&:active {
			transform: scale(0.98);
		}
	}
	
	.card-top {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		margin-bottom: 24rpx;
	}
	
	.title {
		color: #333;
		font-size: 32rpx;
		font-weight: 600;
		margin: 0;
		flex: 1;
		line-height: 1.4;
		padding-right: 20rpx;
	}
	
	.status-tag {
		padding: 6rpx 16rpx;
		border-radius: 8rpx;
		font-size: 24rpx;
		font-weight: 500;
		white-space: nowrap;
	}
	
	.status-active { background: #fff1f0; color: #e74c3c; border: 1rpx solid #ffa39e; }
	.status-warning { background: #fff7e6; color: #fa8c16; border: 1rpx solid #ffd591; }
	.status-ended { background: #f5f5f5; color: #888; border: 1rpx solid #d9d9d9; }
	.status-default { background: #e6f7ff; color: #1890ff; border: 1rpx solid #91d5ff; }
	
	.meta-info {
		display: flex;
		flex-direction: column;
		gap: 16rpx;
		background: #fafafa;
		padding: 20rpx;
		border-radius: 12rpx;
	}
	
	.info-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
	}
	
	.label {
		font-size: 26rpx;
		color: #666;
	}
	
	.value {
		font-size: 26rpx;
		color: #333;
		font-weight: 500;
	}
	
	.points {
		color: #e74c3c;
		font-size: 30rpx;
		font-weight: bold;
	}
	
	.empty {
		text-align: center;
		color: #999;
		padding: 100rpx 0;
		font-size: 28rpx;
	}
</style>
