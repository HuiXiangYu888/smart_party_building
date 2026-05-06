<template>
	<view class="second_navi">中共南京工业大学计算机科学与技术学院委员会</view>
	<view class="container">
		<!-- 概览模块（按需隐藏） -->
		<view class="module" v-if="false">
			<view class="module-title">
				<view class="title-line"></view>
				<text>我的概览</text>
			</view>
			<view class="module-content">
				<view class="overview">
					<view class="points-card" @click="goPointsRecord">
						<view class="points-title">总积分</view>
						<view class="points-value">{{ pointsTotal }}</view>
					</view>
					<view class="recent-card">
						<view class="recent-title">近期活动</view>
						<view v-if="recentTasks.length === 0" class="recent-empty">暂无</view>
						<view v-else class="recent-list">
							<view class="recent-item" v-for="it in recentTasks" :key="it.id" @click="goActivityList">
								<text class="recent-name">{{ it.title }}</text>
								<text class="recent-time">{{ formatTime(it.dueDate) }}</text>
							</view>
						</view>
					</view>
				</view>
			</view>
		</view>
	
		<!-- 信息公示模块 -->
		<view class="module">
			<view class="module-title">
				<view class="title-line"></view>
				<text>信息公示</text>
			</view>
			<view class="module-content">
				<view class="grid">
					<view class="grid-item" @click="goPartyNotice">
						<image src="/static/home/添加党员.png"></image>
						<text>党建公告</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 信息管理模块 -->
		<view class="module">
			<view class="module-title">
				<view class="title-line"></view>
				<text>信息管理</text>
			</view>
			<view class="module-content">
				<view class="grid">
					<view class="grid-item" @click="goPartyInfo">
						<image src="/static/home/个人信息.png"></image>
						<text>支部信息</text>
					</view>
					<view class="grid-item" @click="goMyInfo">
						<image src="/static/home/个人信息.png"></image>
						<text>个人信息</text>
					</view>
					<view class="grid-item" @click="goPartyApplication">
						<image src="/static/home/入党.png"></image>
						<text>入党信息</text>
					</view>
				</view>
			</view>
		</view>
		
		<!-- 党员学习模块 -->
		<view class="module">
			<view class="module-title">
				<view class="title-line"></view>
				<text>党员学习</text>
			</view>
			<view class="module-content">
				<view class="grid">
					<view class="grid-item" @click="goStudyResource">
						<image src="/static/home/添加活动.png"></image>
						<text>在线资源</text>
					</view>
					<view class="grid-item" @click="goActivityList">
						<image src="/static/home/活动草稿.png"></image>
						<text>党建活动</text>
					</view>
					<view class="grid-item" @click="goStudyRecord">
						<image src="/static/home/党员信息.png"></image>
						<text>学习记录</text>
					</view>
					<view class="grid-item" @click="goActivityRecord">
						<image src="/static/home/党员学习.png"></image>
						<text>活动记录</text>
					</view>
					<view class="grid-item" @click="goPointsRecord">
						<image src="/static/home/添加党员.png"></image>
						<text>积分明细</text>
					</view>
				</view>
			</view>
		</view>

	</view>
</template>

<script setup>
	import { ref, onMounted } from 'vue';
	import { onShow } from '@dcloudio/uni-app'
	import { taskAPI, pointsAPI } from '@/utils/api.js'
	
	// 概览数据
	const pointsTotal = ref(0)
	const recentTasks = ref([])
	
	const formatTime = (str) => {
		if (!str) return ''
		try { return new Date(str).toLocaleDateString('zh-CN') } catch { return str }
	}
	
	const loadOverview = async () => {
		try {
			const resSum = await pointsAPI.getMyPointsSummary()
			pointsTotal.value = resSum?.data?.data?.total || 0
		} catch {}
		try {
			// 首页显示所有状态的任务（包括已完结）
			const resAll = await taskAPI.getTaskPage({ page: 1, size: 10 })
			const payload = resAll?.data?.data || {}
			const { records = [] } = payload
			const allTasks = records.map(it => ({
				id: it.id,
				title: it.title,
				dueDate: it.dueDate,
				points: it.points,
				status: it.status
			}))
			recentTasks.value = allTasks.slice(0, 3)
		} catch {}
	}
	
	onMounted(loadOverview)
	onShow(loadOverview)

	// 信息公示模块
	function goPartyNotice(){
		uni.navigateTo({ url:"/pages/home/announcements" })
	}
	// 信息管理模块
	function goPartyInfo(){
		try{
			const token = uni.getStorageSync('accessToken')
			const expire = uni.getStorageSync('tokenExpireTime')
			const loggedIn = !!token && !!expire && Date.now() < Number(expire)
			if (!loggedIn) { uni.navigateTo({ url: '/pages/my/login/login' }); return }
			uni.navigateTo({ url: '/pages/home/branchInfo' })
		}catch(e){ uni.navigateTo({ url: '/pages/my/login/login' }) }
	}
	function goMyInfo(){ uni.navigateTo({ url:"/pages/home/myinfo" }) }
    function goPartyApplication(){ uni.navigateTo({ url:"/pages/home/party" }) }
	// 党员学习模块
	function goStudyResource(){ uni.navigateTo({ url:"/pages/home/memStudy" }) }
	function goActivityList(){ uni.switchTab({ url:"/pages/task/task" }) }
	function goStudyRecord(){ uni.navigateTo({ url:"/pages/my/record" }) }
	function goActivityRecord(){ uni.navigateTo({ url:"/pages/my/myActivity" }) }
	function goPointsRecord(){ uni.navigateTo({ url:"/pages/my/pointsRecord" }) }
</script>

<style lang="scss" scoped>
	.second_navi{ position: fixed; width: 100%; height: 40rpx; background-color: #e74c3c; text-align: center; color: white; font-size: 25rpx; z-index: 1000; }
	.container { padding: 20rpx; background-color: #f5f5f5; padding-top: 60rpx; }
	.module { margin-bottom: 30rpx; background-color: #fff; border-radius: 12rpx; overflow: hidden; box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05); }
	.module .module-title { display: flex; align-items: center; padding: 24rpx 30rpx; font-size: 32rpx; font-weight: bold; color: #333; background-color: #f8f8f8; border-bottom: 1rpx solid #eee; }
	.module .module-title .title-line { width: 6rpx; height: 30rpx; background-color: #e74c3c; margin-right: 16rpx; border-radius: 3rpx; }
	.module .module-content { padding: 20rpx; }

	/* 概览样式 */
	.overview { display: flex; gap: 20rpx; }
	.points-card { flex: 1; background: linear-gradient(160deg, #fff1f0, #ffffff); border: 1rpx solid #ffd6d6; border-radius: 12rpx; padding: 24rpx; box-shadow: 0 2rpx 8rpx rgba(231,76,60,0.08); }
	.points-title { font-size: 26rpx; color: #8c8c8c; margin-bottom: 8rpx; }
	.points-value { font-size: 48rpx; font-weight: 700; color: #cf1322; }
	.recent-card { flex: 2; background: #fff; border: 1rpx solid #f0f0f0; border-radius: 12rpx; padding: 16rpx 0; }
	.recent-title { font-size: 28rpx; color: #333; padding: 0 20rpx 10rpx; }
	.recent-empty { color: #999; font-size: 26rpx; padding: 12rpx 20rpx; }
	.recent-list { }
	.recent-item { display: flex; justify-content: space-between; padding: 14rpx 20rpx; border-top: 1rpx solid #f5f5f5; }
	.recent-name { max-width: 70%; color: #555; font-size: 26rpx; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
	.recent-time { color: #999; font-size: 24rpx; }

	.grid { display: flex; flex-wrap: wrap; }
	.grid .grid-item { width: 30%; margin: 10rpx 0; padding: 25rpx 0; display: flex; flex-direction: column; align-items: center; background-color: #f9f9f9; border-radius: 8rpx; margin-right: 20rpx; transition: all 0.2s ease; }
	.grid .grid-item:active { background-color: #f0f0f0; transform: scale(0.98); }
	.grid .grid-item image { width: 60rpx; height: 60rpx; margin-bottom: 15rpx; }
	.grid .grid-item text { font-size: 28rpx; color: #333; text-align: center; }
</style>
