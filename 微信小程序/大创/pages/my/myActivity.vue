<template>
	<div class="container">
		<div class="header">
			<h1>活动记录</h1>
		</div>
		<div v-if="list.length === 0" class="empty">暂无活动记录</div>
		<div v-else>
			<div class="activity" v-for="(item, index) in list" :key="item.id || index">
				<h2 class="title">{{ item.title }}</h2>
				<div class="meta">
					<span class="kv"><label>积分</label><b>+{{ item.points }}</b></span>
					<span class="kv"><label>截止</label><b>{{ formatDate(item.dueDate) }}</b></span>
				</div>
				<div class="status">{{ translateStatus(item.status) }}</div>
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
		try { return new Date(str).toLocaleString('zh-CN') } catch { return str }
	}

	const translateStatus = (s) => {
		if (s === 'PUBLISHING') return '发布中'
		if (s === 'PENDING_END') return '待完结'
		if (s === 'ENDED') return '已完结'
		return s || ''
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
	.container { max-width: 600px; margin: 1.5rem auto; padding: 0 0.8rem; }
	.header { padding: 0.5rem 0; }
	.header h1 { font-size: 1.2rem; color: #333; font-weight: 700; }
	.activity { background: #fff; border-radius: 10px; padding: 0.9rem; margin-bottom: 0.9rem; border: 1px solid #f0f0f0; box-shadow: 0 2px 12px rgba(0,0,0,0.04); }
	.title { color: #333; font-size: 1.1rem; margin: 0 0 0.6rem 0; font-weight: 500; }
	.meta { display: flex; gap: 12px; margin-bottom: 8px; }
	.kv { background: #fafafa; padding: 4px 8px; border-radius: 999px; font-size: 12px; color: #666; }
	.kv b { color: #cf1322; margin-left: 4px; font-weight: 600; }
	.status { display: inline-block; padding: 0.2rem 0.6rem; border-radius: 4px; font-size: 0.8rem; color: #fff; background: #52c41a; }
	.empty { text-align: center; color: #999; padding: 1rem 0; }
</style>
