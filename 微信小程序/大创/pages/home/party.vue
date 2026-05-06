<template>
	<view class="container">
		<view class="module">
			<view class="module-title">
				<view class="title-line"></view>
				<text>入党</text>
			</view>
			<view class="module-content">
            <view class="grid">
                <view class="grid-item" @click="goHistory">
                    <image src="/static/party/入党申请.png" class="grid-icon"></image>
                    <text>申请记录</text>
                </view>
                <view class="grid-item" @click="goPartyApplication">
                    <image src="/static/party/入党申请.png" class="grid-icon"></image>
                    <text>申请提交</text>
                </view>
            </view>
			</view>
		</view>

		<!-- 入党阶段模块 -->
		<view class="module">
			<view class="module-title">
				<view class="title-line"></view>
				<text>入党阶段</text>
			</view>
			<view class="module-content">
				<view class="stage-card">
					<text class="stage-label">当前状态</text>
					<text class="stage-value" :class="stageClass(currentStage)">{{ currentStage }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
    import { ref, onMounted } from 'vue'
    import { applicationAPI } from '@/utils/api.js'

    const currentStage = ref('未递交入党申请')

    const computeStage = (records=[]) => {
      // 预备党员 > 入党积极分子 > 已递交入党申请 > 未递交入党申请
      const hasPrepareApproved = records.some(r => r.type?.includes('预备') && r.status === '已通过')
      if (hasPrepareApproved) return '预备党员'
      const hasPositiveApproved = records.some(r => r.type?.includes('积极分子') && r.status === '已通过')
      if (hasPositiveApproved) return '入党积极分子'
      const hasPartySubmitted = records.some(r => r.type?.includes('入党申请'))
      if (hasPartySubmitted) return '已递交入党申请'
      return '未递交入党申请'
    }

    const loadStage = async () => {
      try {
        const resRaw = await applicationAPI.getMyHistory()
        const res = Array.isArray(resRaw) ? resRaw[0] : resRaw
        if (res.statusCode === 200 && res.data?.code === 200) {
          const list = res.data.data || []
          currentStage.value = computeStage(list)
        } else {
          currentStage.value = '未递交入党申请'
        }
      } catch { currentStage.value = '未递交入党申请' }
    }

    onMounted(loadStage)

    function stageClass(s){
      if (s === '预备党员') return 'stage-success'
      if (s === '入党积极分子') return 'stage-info'
      if (s === '已递交入党申请') return 'stage-warning'
      return 'stage-default'
    }

    function goPartyApplication() {
      uni.navigateTo({ url: "/pages/home/party/partyApplication" })
      .then(() => {})
      .catch(err => {
        console.error('导航到申请提交失败:', err)
        uni.showToast({ title: '跳转失败，稍后重试', icon: 'none' })
      })
    }
    function goHistory() {
      uni.navigateTo({ url: "/pages/home/party/history" })
      .then(() => {})
      .catch(err => {
        console.error('导航到申请记录失败:', err)
        uni.showToast({ title: '跳转失败，稍后重试', icon: 'none' })
      })
    }
	
	function goActivist() {
	  uni.navigateTo({
	    url: "/pages/home/party/activist"
	  });
	}
	
	function goPrepare() {
	  uni.navigateTo({
	    url: "/pages/home/party/prepare"
	  });
	}
	
	function goFormal() {
	  uni.navigateTo({
	    url: "/pages/home/party/formal"
	  });
	}
</script>

<style lang="scss" scoped>
.container {
  padding: 20rpx;
  background-color: #f5f5f5;
}

.module {
  margin-bottom: 30rpx;
  background-color: #fff;
  border-radius: 12rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);

  .module-title {
    display: flex;
    align-items: center;
    padding: 24rpx 30rpx;
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
    background-color: #f8f8f8;
    border-bottom: 1rpx solid #eee;

    .title-line {
      width: 6rpx;
      height: 30rpx;
      background-color: #e74c3c;
      margin-right: 16rpx;
      border-radius: 3rpx;
    }
  }

  .module-content {
    padding: 20rpx;
  }
}

.grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;

  .grid-item {
    width: calc(50% - 20rpx); // 一行两个按钮，每个按钮占50%宽度减去间距
    margin-bottom: 20rpx;
    padding: 30rpx 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    background-color: #f9f9f9;
    border-radius: 12rpx;
    transition: all 0.3s ease;
    box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.05);

    .grid-icon {
      width: 70rpx;
      height: 70rpx;
      margin-bottom: 15rpx;
    }

    text {
      font-size: 30rpx;
      font-weight: 500;
      color: #333;
      text-align: center;
    }

    &:hover {
      transform: translateY(-5rpx);
      box-shadow: 0 6rpx 12rpx rgba(0, 0, 0, 0.1);
    }
  }
}

/* 入党阶段卡片样式 */
.stage-card { display:flex; align-items:center; justify-content: space-between; background:#fff; border:1rpx solid #f0f0f0; border-radius:12rpx; padding: 24rpx; }
.stage-label { color:#666; font-size: 28rpx; }
.stage-value { font-size: 30rpx; font-weight: 600; padding: 8rpx 14rpx; border-radius: 999rpx; }
.stage-success { background:#e6fffb; color:#08979c; }
.stage-info { background:#f0f5ff; color:#2f54eb; }
.stage-warning { background:#fff7e6; color:#d46b08; }
.stage-default { background:#f5f5f5; color:#666; }
</style>