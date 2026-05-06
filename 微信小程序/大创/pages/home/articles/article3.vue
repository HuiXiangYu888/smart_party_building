<template>
	<view class="container">
		<view class="header">{{ title || '视频播放' }}</view>
		<video
			id="videoPlayer"
			class="player"
			:src="src"
			controls
			:autoplay="currentTime === 0"
			show-center-play-btn
			@error="onVideoError"
			@timeupdate="onTimeUpdate"
			@loadedmetadata="onLoadedMetadata"
			@loadeddata="onLoadedData"
			@canplay="onCanplay"
			@play="onPlay"
			@pause="onPause"
			@ended="onEnded"
		></video>
		
		<!-- 学习进度提示 -->
		<view class="progress-info" v-if="showProgressInfo">
			<text>观看进度: {{ Math.round(watchPercentage) }}%</text>
			<text v-if="currentTime > 0" class="progress-time">当前时间: {{ Math.floor(currentTime/60) }}分{{ currentTime%60 }}秒</text>
			<text class="auto-save-tip">进度已自动保存</text>
		</view>
	</view>
</template>

<script setup>
	import { ref, onUnmounted } from 'vue'
	import { onLoad, onUnload } from '@dcloudio/uni-app'
	import { requestWithAuth } from '@/utils/auth.js'

	const src = ref('')
	const title = ref('')
	const resourceId = ref(null)
	const currentTime = ref(0)
	const totalDuration = ref(0)
	const watchPercentage = ref(0)
	const showProgressInfo = ref(false)
	
	// 学习记录相关
	const studyStartTime = ref(null)
	const studyDuration = ref(0)
	const isPlaying = ref(false)
	const isFirstPlay = ref(true)
	
	// 定时器
	let progressTimer = null
	let studyTimer = null

	const fetchSignedIfNeeded = async (rawSrc, id) => {
		if (rawSrc && rawSrc.includes('Signature=')) return rawSrc
		if (!id) return rawSrc
		try {
			const res = await requestWithAuth({ url: `/study/resources/${id}/signed-url`, method: 'GET' })
			if (res.statusCode === 200 && res.data?.code === 200) return res.data.data?.url || rawSrc
		} catch (e) {}
		return rawSrc
	}

	const onVideoError = (e) => {
		uni.showToast({ title: '视频加载失败', icon: 'none' })
		console.error('video error', e)
	}

	const onLoadedMetadata = (e) => {
		totalDuration.value = Math.floor(e.detail.duration)
		loadVideoProgress()
	}
	
	const onLoadedData = () => {
		// 视频数据加载完成
		console.log('视频数据加载完成，当前进度:', currentTime.value)
		if (currentTime.value > 0 && isFirstPlay.value) {
			// 延迟设置播放位置，确保视频完全准备好
			setTimeout(() => {
				const videoContext = uni.createVideoContext('videoPlayer')
				if (videoContext) {
					console.log('onLoadedData设置播放位置到:', currentTime.value)
					videoContext.seek(currentTime.value)
				}
			}, 1000)
		}
	}
	
	const onCanplay = () => {
		// 视频可以播放
		console.log('视频可以播放，当前进度:', currentTime.value)
		if (currentTime.value > 0 && isFirstPlay.value) {
			// 再次尝试设置播放位置
			setTimeout(() => {
				const videoContext = uni.createVideoContext('videoPlayer')
				if (videoContext) {
					console.log('onCanplay设置播放位置到:', currentTime.value)
					videoContext.seek(currentTime.value)
				}
			}, 500)
		}
	}

	const onPlay = () => {
		isPlaying.value = true
		studyStartTime.value = new Date()
		startStudyTimer()
		
		// 如果是第一次播放且有保存的进度，设置播放位置
		if (currentTime.value > 0 && isFirstPlay.value) {
			console.log('开始播放，准备设置播放位置到:', currentTime.value)
			// 立即设置播放位置
			const videoContext = uni.createVideoContext('videoPlayer')
			if (videoContext) {
				console.log('onPlay立即设置播放位置到:', currentTime.value)
				videoContext.seek(currentTime.value)
			}
			
			// 延迟再次设置，确保位置正确
			setTimeout(() => {
				const videoContext = uni.createVideoContext('videoPlayer')
				if (videoContext) {
					console.log('onPlay延迟设置播放位置到:', currentTime.value)
					videoContext.seek(currentTime.value)
				}
			}, 1000)
		}
		isFirstPlay.value = false
	}

	const onPause = () => {
		isPlaying.value = false
		stopStudyTimer()
		saveVideoProgress()
		
		// 如果学习时长少于60秒（1分钟），不保存学习记录
		if (studyDuration.value > 0 && studyDuration.value < 60) {
			console.log('学习时长少于1分钟，不保存学习记录:', studyDuration.value, '秒')
			studyDuration.value = 0 // 重置学习时长
		}
	}

	const onEnded = () => {
		isPlaying.value = false
		stopStudyTimer()
		saveVideoProgress()
		
		// 只有学习时长大于等于60秒（1分钟）才保存学习记录
		if (studyDuration.value >= 60) {
			saveStudyRecord()
		} else if (studyDuration.value > 0) {
			console.log('学习时长少于1分钟，不保存学习记录:', studyDuration.value, '秒')
			studyDuration.value = 0 // 重置学习时长
		}
	}

	const onTimeUpdate = (e) => {
		currentTime.value = Math.floor(e.detail.currentTime)
		watchPercentage.value = totalDuration.value > 0 ? (currentTime.value / totalDuration.value) * 100 : 0
		
		// 每60秒自动保存一次进度，确保进度不丢失
		if (Math.floor(currentTime.value) % 60 === 0 && currentTime.value > 0) {
			console.log('自动保存进度:', currentTime.value)
			saveVideoProgress()
		}
	}

	// 加载视频观看进度
	const loadVideoProgress = async () => {
		if (!resourceId.value) {
			console.log('没有resourceId，跳过加载进度')
			return
		}
		
		console.log('开始加载观看进度，resourceId:', resourceId.value)
		
		try {
			const res = await requestWithAuth({ 
				url: `/study/records/video/progress/${resourceId.value}`, 
				method: 'GET' 
			})
			console.log('加载进度响应:', res)
			
			if (res.statusCode === 200 && res.data?.code === 200) {
				const progress = res.data.data
				console.log('后端返回的进度数据:', progress)
				console.log('currentTime字段值:', progress.currentTime)
				
				// 使用 currentTime 字段
				const savedTime = progress.currentTime || 0
				console.log('解析出的保存时间:', savedTime)
				
				if (progress && savedTime > 0) {
					currentTime.value = savedTime
					watchPercentage.value = progress.watchPercentage || 0
					showProgressInfo.value = true
					console.log('加载到观看进度:', savedTime, '秒')
					
					// 如果有保存的进度，显示提示
					if (savedTime > 0) {
						uni.showToast({
							title: `从${Math.floor(savedTime/60)}分${savedTime%60}秒继续播放`,
							icon: 'none',
							duration: 2000
						})
					}
				} else {
					console.log('没有找到保存的进度或进度为0，savedTime:', savedTime)
					showProgressInfo.value = true
				}
			} else {
				console.log('加载进度失败:', res.data?.message || '未知错误')
				showProgressInfo.value = true
			}
		} catch (e) {
			console.error('加载观看进度失败:', e)
			showProgressInfo.value = true
		}
	}

	// 保存视频观看进度
	const saveVideoProgress = async () => {
		if (!resourceId.value || totalDuration.value === 0) {
			console.log('跳过保存进度: resourceId=', resourceId.value, 'totalDuration=', totalDuration.value)
			return
		}
		
		console.log('保存观看进度:', {
			resourceId: resourceId.value,
			currentTime: currentTime.value,
			totalDuration: totalDuration.value
		})
		
		try {
			const res = await requestWithAuth({
				url: '/study/records/video/progress',
				method: 'POST',
				data: {
					resourceId: resourceId.value,
					currentTime: currentTime.value,
					totalDuration: totalDuration.value
				}
			})
			console.log('保存进度响应:', res)
		} catch (e) {
			console.error('保存观看进度失败:', e)
		}
	}

	// 保存学习记录
	const saveStudyRecord = async () => {
		if (!resourceId.value || studyDuration.value === 0) return
		
		try {
			// 将秒转换为分钟，至少1分钟
			const durationInMinutes = Math.max(1, Math.ceil(studyDuration.value / 60))
			
			await requestWithAuth({
				url: '/study/records/log',
				method: 'POST',
				data: {
					resourceId: resourceId.value,
					duration: durationInMinutes
				}
			})
		} catch (e) {
			console.error('保存学习记录失败:', e)
		}
	}

	// 开始学习计时
	const startStudyTimer = () => {
		studyTimer = setInterval(() => {
			if (isPlaying.value) {
				studyDuration.value += 1
			}
		}, 1000)
	}

	// 停止学习计时
	const stopStudyTimer = () => {
		if (studyTimer) {
			clearInterval(studyTimer)
			studyTimer = null
		}
	}

	onLoad(async (opts) => {
		const raw = opts?.src ? decodeURIComponent(opts.src) : ''
		title.value = opts?.title ? decodeURIComponent(opts.title) : ''
		resourceId.value = opts?.id ? Number(opts.id) : null
		src.value = await fetchSignedIfNeeded(raw, resourceId.value)
		
		// 页面加载时立即尝试加载视频进度
		if (resourceId.value) {
			console.log('页面加载，开始加载视频进度')
			await loadVideoProgress()
		}
	})

	onUnload(() => {
		// 页面卸载时保存进度和学习记录
		if (isPlaying.value) {
			stopStudyTimer()
		}
		saveVideoProgress()
		
		// 只有学习时长大于等于60秒（1分钟）才保存学习记录
		if (studyDuration.value >= 60) {
			saveStudyRecord()
		} else if (studyDuration.value > 0) {
			console.log('学习时长少于1分钟，不保存学习记录:', studyDuration.value, '秒')
		}
	})

	onUnmounted(() => {
		// 组件卸载时清理定时器
		stopStudyTimer()
	})
</script>

<style lang="scss" scoped>
	.container {
		width: 100%;
		min-height: 100vh;
		background: #000;
		display: flex;
		flex-direction: column;
	}
	.header {
		color: #fff;
		padding: 20rpx;
		font-size: 30rpx;
	}
	.player {
		width: 100%;
		height: 420rpx;
		background: #000;
	}
	
	.progress-info {
		position: fixed;
		top: 100rpx;
		right: 20rpx;
		background: rgba(0, 0, 0, 0.7);
		color: #fff;
		padding: 10rpx 20rpx;
		border-radius: 20rpx;
		font-size: 24rpx;
		z-index: 1000;
		display: flex;
		flex-direction: column;
		align-items: flex-end;
		
		.progress-time {
			font-size: 20rpx;
			margin-top: 5rpx;
			opacity: 0.8;
		}
		
		.auto-save-tip {
			margin-top: 5rpx;
			font-size: 18rpx;
			color: #28a745;
			opacity: 0.8;
		}
	}
</style>
