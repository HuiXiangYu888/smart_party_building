<template>
	<div class="add-container">
	    <div class="header">添加党员</div>
	    <div class="input-group">
	        <div class="label">学号：</div>
	        <input v-model="newMember.studentId" type="text">
	    </div>
	    <div class="input-group">
	        <div class="label">姓名：</div>
	        <input v-model="newMember.name" type="text">
	    </div>
	    <div class="input-group">
	        <div class="label">密码：</div>
	        <input v-model="newMember.password" type="password">
	    </div>
	    <div class="input-group">
	        <div class="label">性别：</div>
			<input v-model="newMember.gender" type="text">
	    </div>
	    <div class="input-group">
	        <div class="label">入党时间：</div>
	        <input v-model="newMember.joinDate" type="text">
	    </div>
	    <div class="input-group">
	        <div class="label">党内职务：</div>
	        <input v-model="newMember.position" type="text">
	    </div>
	    <div class="input-group">
	        <div class="label">所属支部：</div>
	        <input v-model="newMember.branch" type="text">
	    </div>
	    <button @click="addMember" class="add-button">添加</button>    
	</div>
</template>

<script setup>
	import { ref } from 'vue';
	
	
	const newMember = ref({
	    studentId: '',
	    name: '',
	    password: '',
	    gender: '',
	    joinDate: '',
	    position: '',
	    branch: ''
	});
	
	const addMember = () => {
	  // 密码验证，可根据实际需求修改
		if (newMember.value.password === '123456') {
			const newPartyMember = {
				id: newMember.value.studentId,
				name: newMember.value.name,
				gender: newMember.value.gender,
				joinDate: newMember.value.joinDate,
				position: newMember.value.position,
				branch: newMember.value.branch,
				isEditing: false
			};
	    // 使用uni.navigateTo进行页面跳转并传递数据
			uni.navigateTo({
				url:`/pages/home/memberInfo?newMem=${encodeURIComponent(JSON.stringify(newPartyMember))}`
			});
			uni.showToast({
				title:"添加成功"
			})
		} 
		// 密码错误，添加失败
		else {
			uni.showToast({
				icon:'error',
				title:"添加失败"
			})
		}
	};
</script>

<style lang="scss" scoped>
	.add-container {
		max-width: 600px;
		margin: 0 auto;
		padding: 20px;
		background-color: #f9f9f9;
		border-radius: 5px;
		box-shadow: 0 0 5px rgba(0, 0, 0, 0.1);	
		.header{
			text-align: center;
			font-weight: bold;
		}
		.input-group{
			margin-bottom: 25rpx;	
			.label{
				margin-bottom: 10rpx;
			}
			input{
				background-color: #eee;
				height: 60rpx;
				line-height: 60rpx;
				padding-left: 20rpx;
			}
		}
	}
</style>
