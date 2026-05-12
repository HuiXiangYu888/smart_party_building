<template>
	<view class="container">
		<view class="header">
			<view class="title-dot"></view>
			<text class="title-text">{{ title || '视频播放' }}</text>
		</view>
		<video
			id="videoPlayer"
			class="player"
			:src="src"
			controls
			:autoplay="savedTime === 0"
			show-center-play-btn
			:enable-play-gesture="false"
			:enable-progress-gesture="false"
			:play-btn-position="'center'"
			@error="onVideoError"
			@timeupdate="onTimeUpdate"
			@loadedmetadata="onLoadedMetadata"
			@play="onPlay"
			@pause="onPause"
			@ended="onEnded"
		></video>

		<!-- 视频信息卡片 -->
		<view class="info-card">
			<view class="info-row">
				<text class="info-label">观看进度</text>
				<view class="progress-bar-wrap">
					<view class="progress-bar-bg">
						<view class="progress-bar-fill" :style="{ width: Math.min(watchPercentage, 100) + '%' }"></view>
					</view>
					<text class="progress-percent">{{ Math.round(watchPercentage) }}%</text>
				</view>
			</view>
			<view class="info-row" v-if="totalDuration > 0">
				<text class="info-label">视频时长</text>
				<text class="info-value">{{ formatDuration(totalDuration) }}</text>
			</view>
			<view class="info-row">
				<text class="info-label">完成状态</text>
				<text class="info-value" :class="hasCompleted ? 'completed' : 'not-completed'">
					{{ hasCompleted ? '✓ 已完成观看' : '未完成' }}
				</text>
			</view>
		</view>

		<!-- 提示 -->
		<view class="tip-card">
			<text class="tip-icon">💡</text>
			<text class="tip-text">完整看完视频可获得1积分，视频不可快进，请认真观看学习</text>
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
	const savedTime = ref(0)
	const hasCompleted = ref(false)

	// 学习记录相关
	const studyDuration = ref(0)
	const isPlaying = ref(false)
	const isFirstPlay = ref(true)
	// 防快进：记录上一次合法的播放时间
	let lastValidTime = 0

	// 定时器
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

	const formatDuration = (seconds) => {
		if (!seconds) return '0:00'
		const m = Math.floor(seconds / 60)
		const s = Math.floor(seconds % 60)
		return `${m}:${s.toString().padStart(2, '0')}`
	}

	const onVideoError = (e) => {
		uni.showToast({ title: '视频加载失败', icon: 'none' })
		console.error('video error', e)
	}

	const onLoadedMetadata = (e) => {
		totalDuration.value = Math.floor(e.detail.duration)
		loadVideoProgress()
	}

	const onPlay = () => {
		isPlaying.value = true
		startStudyTimer()

		// 如果是第一次播放且有保存的进度，恢复播放位置
		if (savedTime.value > 0 && isFirstPlay.value) {
			const videoContext = uni.createVideoContext('videoPlayer')
			if (videoContext) {
				videoContext.seek(savedTime.value)
				lastValidTime = savedTime.value
			}
		}
		isFirstPlay.value = false
	}

	const onPause = () => {
		isPlaying.value = false
		stopStudyTimer()
		saveVideoProgress()
	}

	const onEnded = () => {
		isPlaying.value = false
		stopStudyTimer()
		watchPercentage.value = 100
		currentTime.value = totalDuration.value
		saveVideoProgress()

		// 视频完整看完 → 通知后端发放积分和学习次数
		if (!hasCompleted.value) {
			completeVideo()
		}
	}

	const onTimeUpdate = (e) => {
		const newTime = Math.floor(e.detail.currentTime)

		// 防快进逻辑：如果用户拖拽进度条向前跳超过3秒，自动拉回
		if (newTime > lastValidTime + 3) {
			const videoContext = uni.createVideoContext('videoPlayer')
			if (videoContext) {
				videoContext.seek(lastValidTime)
				uni.showToast({ title: '不允许快进哦~', icon: 'none', duration: 1500 })
			}
			return
		}

		// 更新合法时间（允许自然播放和小幅度回退）
		if (newTime > lastValidTime) {
			lastValidTime = newTime
		}

		currentTime.value = newTime
		watchPercentage.value = totalDuration.value > 0 ? (currentTime.value / totalDuration.value) * 100 : 0

		// 每30秒自动保存一次进度
		if (currentTime.value > 0 && currentTime.value % 30 === 0) {
			saveVideoProgress()
		}
	}

	// 加载视频观看进度
	const loadVideoProgress = async () => {
		if (!resourceId.value) return
		try {
			const res = await requestWithAuth({
				url: `/study/records/video/progress/${resourceId.value}`,
				method: 'GET'
			})
			if (res.statusCode === 200 && res.data?.code === 200) {
				const progress = res.data.data
				if (progress) {
					const st = progress.currentTime || 0
					savedTime.value = st
					lastValidTime = st
					currentTime.value = st
					watchPercentage.value = progress.watchPercentage || 0
					hasCompleted.value = progress.watchPercentage >= 95
					if (st > 0) {
						uni.showToast({
							title: `从${formatDuration(st)}继续播放`,
							icon: 'none',
							duration: 2000
						})
					}
				}
			}
		} catch (e) {
			console.error('加载观看进度失败:', e)
		}
	}

	// 保存视频观看进度
	const saveVideoProgress = async () => {
		if (!resourceId.value || totalDuration.value === 0) return
		try {
			await requestWithAuth({
				url: '/study/records/video/progress',
				method: 'POST',
				data: {
					resourceId: resourceId.value,
					currentTime: currentTime.value,
					totalDuration: totalDuration.value
				}
			})
		} catch (e) {
			console.error('保存观看进度失败:', e)
		}
	}

	// 视频看完 → 后端发积分+学习次数
	const completeVideo = async () => {
		if (!resourceId.value) return
		try {
			const res = await requestWithAuth({
				url: '/study/records/video/complete',
				method: 'POST',
				data: {
					resourceId: resourceId.value
				}
			})
			if (res.statusCode === 200 && res.data?.code === 200) {
				hasCompleted.value = true
				const msg = res.data.data || ''
				if (msg.includes('积分')) {
					uni.showToast({ title: '🎉 完成观看，+1积分', icon: 'none', duration: 2500 })
				} else {
					uni.showToast({ title: '✓ 观看完成', icon: 'none', duration: 2000 })
				}
			}
		} catch (e) {
			console.error('提交完成状态失败:', e)
		}
	}

	// 开始学习计时
	const startStudyTimer = () => {
		if (studyTimer) return
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
	})

	onUnload(() => {
		if (isPlaying.value) stopStudyTimer()
		saveVideoProgress()
	})

	onUnmounted(() => {
		stopStudyTimer()
	})
</script>

<style lang="scss" scoped>
	.container {
		width: 100%;
		min-height: 100vh;
		background: #f5f5f5;
		display: flex;
		flex-direction: column;
	}

	.header {
		display: flex;
		align-items: center;
		padding: 24rpx 30rpx;
		background: #fff;
		border-bottom: 1rpx solid #f0f0f0;
	}

	.title-dot {
		width: 8rpx;
		height: 32rpx;
		background-color: #e74c3c;
		border-radius: 4rpx;
		margin-right: 16rpx;
	}

	.title-text {
		font-size: 32rpx;
		font-weight: 600;
		color: #333;
	}

	.player {
		width: 100%;
		height: 420rpx;
		background: #1a1a2e;
	}

	.info-card {
		margin: 20rpx;
		background: #fff;
		border-radius: 16rpx;
		padding: 24rpx 30rpx;
		box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
	}

	.info-row {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 16rpx 0;
		border-bottom: 1rpx solid #f8f8f8;

		&:last-child {
			border-bottom: none;
		}
	}

	.info-label {
		font-size: 28rpx;
		color: #666;
	}

	.info-value {
		font-size: 28rpx;
		color: #333;
		font-weight: 500;
	}

	.completed {
		color: #08979c;
		background: #e6fffb;
		padding: 4rpx 16rpx;
		border-radius: 8rpx;
		font-size: 26rpx;
	}

	.not-completed {
		color: #999;
	}

	.progress-bar-wrap {
		display: flex;
		align-items: center;
		gap: 16rpx;
		flex: 1;
		margin-left: 30rpx;
	}

	.progress-bar-bg {
		flex: 1;
		height: 12rpx;
		background: #f0f0f0;
		border-radius: 6rpx;
		overflow: hidden;
	}

	.progress-bar-fill {
		height: 100%;
		background: linear-gradient(90deg, #e74c3c, #ff6b6b);
		border-radius: 6rpx;
		transition: width 0.3s ease;
	}

	.progress-percent {
		font-size: 26rpx;
		color: #e74c3c;
		font-weight: 600;
		width: 70rpx;
		text-align: right;
	}

	.tip-card {
		margin: 0 20rpx 20rpx;
		background: #fff9f0;
		border: 1rpx solid #ffe8cc;
		border-radius: 12rpx;
		padding: 20rpx 24rpx;
		display: flex;
		align-items: flex-start;
		gap: 12rpx;
	}

	.tip-icon {
		font-size: 28rpx;
		flex-shrink: 0;
	}

	.tip-text {
		font-size: 24rpx;
		color: #b37000;
		line-height: 1.6;
	}
</style>
