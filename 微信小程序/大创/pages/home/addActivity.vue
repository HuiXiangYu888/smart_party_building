<template>
	<div class="add-activity-container">
	    <h2 class="page-title">添加活动</h2>
	    <div class="act-group">
	    	<label class="act-label">活动标题</label>
	    	<input
	    		type="text"
	    		v-model="activity.title"
				placeholder="请输入活动标题"
	    		class="act-input"
	    	/>
	    </div>
	    <div class="act-group">
	    	<label class="act-label">活动内容</label>
	    	<textarea
	    		v-model="activity.content"
				placeholder="请详细描述活动内容、规则、注意事项等..."
	    		class="act-textarea"
	    	></textarea>
	    </div>
	    
	    <button type="submit" class="submit-button" @click="addAct">存入草稿</button>
			
	    
	</div>
</template>

<script setup>
	import { ref } from 'vue';
	
	// 存储活动信息的响应式对象
	const activity = ref({
		title: '',
		content: ''
	});
	
	const addAct = () => {
		const newAct = {
			title:activity.value.title,
			content:activity.value.content
		};
		
		if(newAct.title.trim() !== '' && newAct.content.trim() !== ''){
			// 使用uni.navigateTo进行页面跳转并传递数据
			uni.navigateTo({
				url:`/pages/home/draft?newActivity=${encodeURIComponent(JSON.stringify(newAct))}`
			});
			uni.showToast({
				title:"添加成功"
			})
		} else {
			// 新增：提示用户填写必要信息
			uni.showToast({
				title: "请填写活动标题和内容",
				icon: "none"
			});
		}
	};
</script>

<style lang="scss" scoped>
	.add-activity-container {
		padding: 20px;
	}
	
	.page-title {
		margin-bottom: 20px;
		text-align: center;
		font-weight: bold;
	}
	
	.act-group {
		margin-bottom: 15px;
	}
	
	.act-label {
		display: block;
		margin-bottom: 5px;
		font-weight: bold;
	}
	
	.act-input {
		//width: 100%;
		padding: 8px;
		border: 1px solid #ccc;
		border-radius: 4px;
	}
	
	.act-textarea {
		//width: 100%;
		padding: 8px;
		border: 1px solid #ccc;
		border-radius: 4px;
	}
	
	.submit-button {
		
		background-color: #007bff;
		    color: white;
		    padding: 8px 16px; 
		    border: none;
		    border-radius: 6px; 
		    font-size: 13px; 
		    font-weight: 500;
		    box-shadow: 0 2px 4px rgba(0, 123, 255, 0.15);
		    
		    display: block;
		    margin: 20px auto 0; 
			margin-left: 60px;
			margin-right: 60px;
	}
</style>
