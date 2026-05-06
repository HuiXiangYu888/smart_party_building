<template>
	<div class="draft-container">
	    
		<div v-for="(member, index) in draft" :key="index" class="draft">
			<div class="draft-title">{{ member.title }}</div>
			<div class="draft-content">{{ member.content }}</div>
			<div>
				<button class="delete-btn" @click="deleteDraft(index)">删除</button>
				<button class="publish-btn" @click="publish(index)">发布</button>
			</div>
		</div>
	</div>
</template>

<script setup>
	import { ref } from 'vue';
	import { onLoad } from '@dcloudio/uni-app';
	
	//假设原来有两个草稿
	const draft = ref([
		{
		  title: '2024年春季校园运动会',
		  content: '一、活动时间：2024年4月20日（周六）8:00-17:00；二、活动地点：学校田径场；三、参与对象：全体师生；四、比赛项目：1. 田径类：100米、400米、800米、跳远、跳高、铅球；五、注意事项：1. 参赛选手需提前30分钟到场签到；2. 自备运动装备，穿运动鞋参赛；3. 身体不适者禁止参赛，如有特殊情况需提前告知'
		},
		{
		  title: '2024年校园美食节',
		  content: '本周在东苑餐厅门口举办美食节，欢迎同学们品尝各色美味，请同学们合理消费，垃圾不乱扔，做到卫生消费'
		}
	]);
	
	onLoad((options) => {
		if (options.newActivity) {
			const newActivity = JSON.parse(decodeURIComponent(options.newActivity));
			draft.value.push(newActivity);
		}
	});
	
	const deleteDraft = (index) => {
	    draft.value.splice(index, 1);
	};
	
	const publish = (index) => {
		const publication = {
			id: Date.now(), // 用时间戳作为唯一ID
			title: draft.value[index].title,
			content: draft.value[index].content,
			signed: false,
		};
		
		// 从草稿中移除已发布项
		draft.value.splice(index, 1);
		
		// 存入本地存储（使用专门的键名，如 "pendingTask"）
		uni.setStorageSync("pendingTask", JSON.stringify(publication));
	
		//跳转到 tabBar 页面（task页面）
		uni.switchTab({
			url: "/pages/task/task",
			success: () => {
				uni.showToast({ title: "发布成功", icon: "success" });
			},
			fail: (err) => {
				console.error("跳转失败:", err);
				uni.showToast({ title: "发布失败", icon: "none" });
			},
		});
	};
</script>

<style lang="scss" scoped>
	$shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
	$border-radius: 10px;
	$spacing: 20px;
	$primary-color: #4096ff;
	$danger-color: #f53f3f;
	
	.draft-container {
	  max-width: 800px;
	  margin: 10px auto;
	  padding: 0 15px; 
	}
	
	.draft {
	  background: #fff;
	  border-radius: $border-radius;
	  box-shadow: $shadow;
	  padding: 10px;
	  padding-bottom: 20px;
	}
	
	.draft-title {
	  font-size: 15px;
	  font-weight: 600;
	  margin-bottom: 10px;
	  color: #1d2129;
	  border-bottom: 1px solid #f2f3f5;
	  position: relative;
	  
	  &:after {
	    content: '';
	    position: absolute;
	    left: 0;
	    bottom: -1px;
	    width: 60px;
	    height: 3px;
	    background-color: $primary-color; 
	  }
	}
	
	.draft-content {
	  line-height: 2; 
	  color: #4e5969;
	  margin-bottom: 14px;
	  font-size: 15px;
	  white-space: pre-line;
	  
	  
	  
	}
	
	.button-group {
	  display: flex;
	  gap: 15px; // 按钮间距
	  justify-content: flex-end; // 靠右对齐
	  padding-top: 15px;
	  border-top: 1px dashed #f2f3f5; // 分隔线
	}
	
	button {
	  padding: 5px 20px;
	  border-radius: 6px;
	  border: none;
	  cursor: pointer;
	  font-size: 14px;
	  font-weight: 500;	  
	  display: inline-flex;
	  align-items: center;
	  
	}
	
	.delete-btn {
	  background-color: #fff;
	  color: $danger-color;
	  border: 1px solid $danger-color;
	  margin-right: 30px;
	}
	
	.publish-btn {
	  background-color: $primary-color;
	  color: #fff;
	  
	}
	
	
	
</style>
