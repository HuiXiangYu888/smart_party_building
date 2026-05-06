<template>
	<view class="container">
    <view class="header">积分排行</view>
		<view class="rank-list">
    <view class="rank-item" :class="getRankClass(idx)" v-for="(it, idx) in list" :key="it.userId">
				<text class="idx">{{ idx + 1 }}</text>
				<text class="name">{{ it.username || '-' }}</text>
				<text class="branch">{{ it.branchName || '-' }}</text>
				<text class="points">{{ it.totalPoints }}</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { requestWithAuth as request } from '@/utils/auth.js'

const list = ref([])
const loading = ref(false)

const loadData = async () => {
  if (loading.value) return
  loading.value = true
  try {
    console.log('🔍 开始加载积分排行')
    const res = await request({ url: '/points/rank', method: 'GET' })
    console.log('📊 积分排行响应:', res)
    
    if (res.statusCode === 200 && res.data?.code === 200) {
      list.value = (res.data.data || []).map(x => ({
        userId: x.userId,
        username: x.username,
        branchName: x.branchName,
        totalPoints: Number(x.totalPoints || 0)
      }))
      console.log('✅ 积分排行加载成功，记录数:', list.value.length)
    } else {
      console.error('❌ 积分排行加载失败:', res.data?.message)
      uni.showToast({ title: res.data?.message || '加载失败', icon: 'none' })
    }
  } catch (e) {
    console.error('❌ 积分排行加载异常:', e)
    uni.showToast({ title: '网络错误', icon: 'none' })
  } finally {
    loading.value = false
  }
}

onMounted(loadData)

const getRankClass = (idx) => {
  if (idx === 0) return 'first'
  if (idx === 1) return 'second'
  if (idx === 2) return 'third'
  return ''
}
</script>

<style scoped>
.container { padding: 16px; }
.header { font-size: 22px; font-weight: 800; text-align: center; margin: 12px 0 16px; }
.rank-list { background: #fff; border-radius: 8rpx; overflow: hidden; }
.rank-item { display: flex; align-items: center; padding: 12px 10px; border-bottom: 1px solid #f0f0f0; }
.rank-item:last-child { border-bottom: none; }
.idx { width: 40rpx; color: #999; }
.name { flex: 1; margin-left: 12rpx; color: #333; }
.branch { flex: 1; color: #666; }
.points { width: 120rpx; text-align: right; color: #c92127; }

/* 前三名荣誉边框 */
.rank-item.first { border-left: 8rpx solid #FFD700; background: #fffdf0; }
.rank-item.second { border-left: 8rpx solid #C0C0C0; background: #fcfcff; }
.rank-item.third { border-left: 8rpx solid #CD7F32; background: #fffaf6; }
</style>


