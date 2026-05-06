<template>
	<div class="task-container">
		<div class="task" v-for="(task, index) in tasks" :key="index">
			<div class="title">
				<text class="title-text">{{ task.title }}</text>
			</div>
			<div class="meta">
				<view class="meta-item"><text class="label">积分</text><text class="value">+{{ task.points }}</text></view>
				<view class="meta-item"><text class="label">截止</text><text class="value">{{ formatDate(task.dueDate) }}</text></view>
			</div>
			<div class="content">{{ task.content }}</div>
			<div class="full-tip" v-if="isFull(task)">报名人数已满</div>
			<div class="actions">
				<button class="btn primary" :disabled="isFull(task)" v-if="!task.signed" @click="join(index)">报名</button>
				<button class="btn ghost" v-else :disabled="task.status==='PENDING_END'" @click="cancel(index)">取消报名</button>
			</div>
		</div>
		<!-- 空状态显示 -->
		<div class="empty-state" v-if="tasks.length === 0">
			暂无任务信息
		</div>
	</div>
</template>

<script setup>
	import { ref , onMounted} from "vue";
	import { onLoad , onShow} from '@dcloudio/uni-app';
	import { taskAPI } from "../../utils/api.js";
	
	const tasks = ref([]);
	const STORAGE_JOINED_KEY = 'joinedTaskIds'
	
	const formatDate = (str) => {
		if (!str) return ''
		try { return new Date(str).toLocaleString('zh-CN') } catch { return str }
	}
	
	// 检查并添加本地存储中的任务（兼容旧逻辑）
	const checkPendingTask = () => {
		const pendingTask = uni.getStorageSync("pendingTask");
		if (pendingTask) {
			try {
				const task = JSON.parse(pendingTask);
				const isDuplicate = tasks.value.some(item => item.id === task.id);
				if (!isDuplicate) tasks.value.push(task);
				uni.removeStorageSync("pendingTask");
			} catch (e) { console.error("解析任务失败:", e); }
		}
	};
	
	const loadTasks = async () => {
		try {
			let res = await taskAPI.getTaskPage({ page: 1, size: 50 });
			if (Array.isArray(res)) res = res[1];
			const payload = res?.data?.data || {};
			const { records = [] } = payload;
			const base = records
				.filter(it => it.status !== 'ENDED')
				.map(it => ({
				id: it.id,
				title: it.title,
				content: it.description,
				dueDate: it.dueDate,
				points: it.points,
				capacity: it.capacity,
				status: it.status,
				signed: false
			}));
			// 合并后端“我的报名”与本地缓存，提升切换时体验
			try {
				const myRes = await taskAPI.getMyTasks();
				const myList = myRes?.data?.data || [];
				const serverSet = new Set((myList || []).map(i => i.id || i.task_id));
				let localSet = new Set();
				try {
					const joined = uni.getStorageSync(STORAGE_JOINED_KEY) || '[]'
					localSet = new Set(JSON.parse(joined))
				} catch {}
				base.forEach(row => { if (serverSet.has(row.id) || localSet.has(row.id)) row.signed = true; });
			} catch {}
			tasks.value = base;
		} catch (e) {
			console.error('加载任务失败', e);
		}
	};

	const isFull = (task) => {
		if (!task) return false
		// capacity <= 0 视为停止报名
		if (typeof task.capacity === 'number') return task.capacity <= 0
		return false
	}
	
	onMounted(() => { checkPendingTask(); loadTasks(); });
	onShow(() => { checkPendingTask(); loadTasks(); });
	
	const join = async (index) => {
	  try {
		await taskAPI.userJoin(tasks.value[index].id);
		tasks.value[index].signed = true;
		// 本地持久化报名状态，避免页面切换后瞬时丢失
		try {
			const joined = uni.getStorageSync(STORAGE_JOINED_KEY) || '[]'
			const set = new Set(JSON.parse(joined))
			set.add(tasks.value[index].id)
			uni.setStorageSync(STORAGE_JOINED_KEY, JSON.stringify(Array.from(set)))
		} catch {}
		uni.showToast({ title: '报名成功', icon: 'success' })
	  } catch (e) {
		console.error('报名失败', e);
	  }
	};
	
	const cancel = async (index) => {
	  try {
		await taskAPI.userCancel(tasks.value[index].id);
		tasks.value[index].signed = false;
		try {
			const joined = uni.getStorageSync(STORAGE_JOINED_KEY) || '[]'
			const arr = JSON.parse(joined).filter(id => id !== tasks.value[index].id)
			uni.setStorageSync(STORAGE_JOINED_KEY, JSON.stringify(arr))
		} catch {}
		uni.showToast({ title: '已取消', icon: 'success' })
	  } catch (e) {
		console.error('取消失败', e);
	  }
	};
	
	onLoad(() => {});
</script>

<style lang="scss" scoped>
	.task-container { max-width: 800px; margin: 0 auto; padding: 20rpx; background-color: #f6f7fb; min-height: 100vh; }
	
	.task { background: #ffffff; border-radius: 14px; padding: 28rpx; margin-bottom: 20rpx; box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.06); border: 1rpx solid #f0f0f0; }
	
	.title { display: flex; align-items: center; gap: 10rpx; margin-bottom: 12rpx; }
	.flag { width: 28rpx; height: 28rpx; }
	.title-text { font-size: 30rpx; font-weight: 700; color: #262626; }
	.title:after { content: ""; flex: 1; margin-left: 10rpx; height: 2rpx; background: linear-gradient(90deg, #ffccc7, rgba(255,255,255,0)); }
	
	.meta { display: flex; gap: 20rpx; margin-bottom: 12rpx; }
	.meta .meta-item { display: flex; gap: 8rpx; align-items: center; padding: 6rpx 12rpx; background: #fafafa; border-radius: 999rpx; }
	.meta .label { color: #8c8c8c; font-size: 22rpx; }
	.meta .value { color: #cf1322; font-weight: 600; font-size: 24rpx; }
	
	.content { font-size: 28rpx; color: #595959; line-height: 1.8; margin-bottom: 18rpx; white-space: pre-line; }
	
	.actions { display: flex; gap: 16rpx; }
	.btn { flex: 1; padding: 16rpx 0; border-radius: 999rpx; border: none; font-size: 28rpx; }
	.btn.primary { background-color: #d4380d; color: #fff; }
	.btn.primary:active { background-color: #ad2102; }
	.btn.ghost { background-color: #ffffff; color: #8c8c8c; border: 2rpx solid #d9d9d9; }
	.btn.ghost:active { background-color: #fafafa; }
	
	.empty-state { text-align: center; padding: 80rpx 20rpx; color: #bfbfbf; font-size: 28rpx; background-color: #ffffff; border-radius: 14px; box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.04); }
	
	@media (max-width: 768px) {
		.task-container { padding: 16rpx; }
		.task { padding: 24rpx; }
	}
</style>
