<template>
	<view class="party-application-page">
		<view class="form-container">
			<view>
                <view class="form-item">
                    <label>申请类型</label>
                    <picker mode="selector" :range="typeOptions" @change="onTypeChange">
                        <view class="picker">{{ selectedType || '请选择申请类型' }}</view>
                    </picker>
                </view>
                <view class="form-item">
                    <label>申请内容</label>
                    <textarea v-model="formData.partyApplication" placeholder="请输入申请内容"></textarea>
                </view>
                <view class="form-item">
                    <label>附件（图片）</label>
                    <view class="upload-list">
                        <view class="upload-item" v-for="(u,i) in attachments" :key="i">
                            <image :src="u" mode="aspectFill" class="thumb" />
                            <text class="remove" @click="removeAttachment(i)">删除</text>
                        </view>
                        <button type="button" class="btn" @click="chooseImage">选择图片</button>
                    </view>
				</view>
				<button type="button" @click="onSubmit">提交</button>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { uploadAPI, applicationAPI } from '@/utils/api.js'

const formData = ref({
  partyApplication: ''
});

const attachments = ref([])
const submitting = ref(false)
const typeOptions = ['入党申请','积极分子申请','预备党员申请']
const selectedType = ref('')

const onTypeChange = (e) => {
    const idx = Number(e.detail.value)
    selectedType.value = typeOptions[idx]
}

const chooseImage = () => {
    uni.chooseImage({
        count: 6,
        success: async (res) => {
            for (const fp of res.tempFilePaths) {
                try {
                    const { url } = await uploadAPI.uploadFile(fp)
                    attachments.value.push(url)
                } catch (e) {
                    uni.showToast({ title: '上传失败', icon: 'none' })
                }
            }
        }
    })
}

const removeAttachment = (i) => {
    attachments.value.splice(i, 1)
}

const onSubmit = async () => {
    if (!selectedType.value) {
        uni.showToast({ title: '请选择申请类型', icon: 'none' })
        return
    }
    if (!formData.value.partyApplication) {
        uni.showToast({ title: '请填写申请内容', icon: 'none' })
        return
    }
    try {
        if (submitting.value) return
        submitting.value = true
        uni.showLoading({ title: '提交中...' })
        const details = `${formData.value.partyApplication}`
        let respRaw
        if (selectedType.value === '入党申请') {
            respRaw = await applicationAPI.applyParty(details, attachments.value)
        } else if (selectedType.value === '积极分子申请') {
            respRaw = await applicationAPI.applyPositive(details, attachments.value)
        } else {
            // 预备党员需要预备期，这里最小化提交空时间，后续可在UI中补充时间字段
            const today = new Date().toISOString().slice(0,10)
            respRaw = await applicationAPI.applyPrepare(details, today + 'T00:00:00', today + 'T00:00:00', attachments.value)
        }
        const resp = Array.isArray(respRaw) ? respRaw[0] : respRaw
        if (resp.statusCode === 200 && resp.data?.code === 200) {
            uni.showToast({ title: '已提交', icon: 'success' })
            setTimeout(() => uni.navigateBack(), 800)
        } else {
            const msg = (resp.data && (resp.data.message || resp.data.msg)) ? `${resp.data.message || resp.data.msg}` : `提交失败(${resp.statusCode})`
            console.error('提交失败:', resp)
            uni.showToast({ title: msg, icon: 'none' })
        }
    } catch (e) {
        console.error('提交异常:', e)
        uni.showToast({ title: (e?.message || '提交失败'), icon: 'none' })
    } finally {
        submitting.value = false
        uni.hideLoading()
    }
}

const openHistory = async () => {
    try {
        const [res] = await applicationAPI.getMyHistory()
        if (res.statusCode === 200 && res.data?.code === 200) {
            const list = res.data.data || []
            const lines = list.map(x => `${x.type} | ${x.reviewedAt || '-'} | ${x.status}`).join('\n')
            uni.showModal({ title: '审核记录', content: lines || '暂无记录', showCancel: false })
        } else {
            uni.showToast({ title: '获取失败', icon: 'none' })
        }
    } catch (e) {
        uni.showToast({ title: '获取失败', icon: 'none' })
    }
}

</script>

<style lang="scss" scoped>
	$primary-color: #1677ff;
	.party-application-page {
	  padding: 20rpx;
	  background-color: #f5f5f5;
	}
	
	.form-container {
	  background-color: #fff;
	  padding: 30rpx;
	  border-radius: 12rpx;
	  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
	}
	
	.form-item {
	  margin-bottom: 20rpx;
	}
	
	.form-item label {
	  display: block;
	  margin-bottom: 10rpx;
	  font-weight: bold;
	  color: #333;
	}
	
	.form-item input,
	.form-item textarea {
	  width: 100%;
	  border: 1rpx solid #ccc;
	  border-radius: 6rpx;
	  font-size: 28rpx;
	}
	
	.form-input {
	  height: 30rpx;
	}
	
	.form-item textarea{
		padding-top: 10rpx;
	}
	
	button {
	  width: 100%;
	  height: 44px;
	  background-color: $primary-color;
	  color: #fff;
	  border-radius: 22px;
	  font-size: 16px;
	  border: none;
	  outline: none;
	  box-shadow: 0 4px 10px rgba(22, 119, 255, 0.2);
	  
	  &:active {
	    background-color: #0958d9;
	    box-shadow: 0 2px 5px rgba(22, 119, 255, 0.3);
	  }
	}

	.picker { width:100%; background:#fff; border:1rpx solid #ccc; border-radius:6rpx; padding: 16rpx; color:#666; }
	.upload-list { display:flex; gap:12rpx; flex-wrap:wrap; margin-top: 10rpx; }
	.upload-item { position:relative; }
	.thumb { width:160rpx; height:160rpx; border-radius:8rpx; background:#f2f3f5; }
	.remove { position:absolute; right:4rpx; top:4rpx; background:rgba(0,0,0,.6); color:#fff; font-size:20rpx; padding:2rpx 6rpx; border-radius:4rpx; }
</style>