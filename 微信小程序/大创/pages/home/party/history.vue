<template>
	<view class="container">
		<view v-if="loading" class="empty">加载中...</view>
		<view v-else>
			<view v-if="list.length === 0" class="empty">暂无申请记录</view>
			<view v-else>
				<view class="item" v-for="(it,i) in list" :key="i">
					<view class="left">
						<text class="name">{{ it.name || '-' }}</text>
						<text class="sid">学号：{{ it.studentId || '-' }}</text>
					</view>
					<view class="mid">
						<text class="type">{{ it.type }}</text>
						<text class="submit">提交：{{ it.submittedAt || '-' }}</text>
					</view>
					<text class="status" :class="tagClass(it.status)">{{ it.status }}</text>
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
.container { padding: 20rpx; }
.empty { text-align: center; color: #888; padding: 40rpx 0; }
.item { display:flex; align-items:center; justify-content: space-between; background:#fff; padding: 20rpx; border-radius: 12rpx; box-shadow: 0 2rpx 10rpx rgba(0,0,0,.05); margin-bottom: 16rpx; }
.left { display:flex; flex-direction:column; gap:6rpx; }
.name { color:#333; font-weight:600; }
.sid { color:#666; font-size: 24rpx; }
.mid { display:flex; flex-direction:column; gap:6rpx; align-items:flex-end; }
.submit { color:#999; font-size: 24rpx; }
.type { color:#333; font-weight:600; }
.time { color:#666; font-size: 24rpx; }
.status { padding: 6rpx 12rpx; border-radius: 999rpx; font-size: 24rpx; }
.success { background: #e6fffb; color: #08979c; }
.warning { background: #fff7e6; color: #d46b08; }
.danger { background: #fff1f0; color: #a8071a; }
.info { background: #f5f5f5; color: #666; }
</style>


