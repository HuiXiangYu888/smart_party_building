<template>
    <div class="container">
        <div class="header">党员信息列表</div>
        <div v-for="(member, index) in partyMembers" :key="index" class="member-info">
			<div class="member-item">
			    <div class="label">学号：</div>
			    <div v-if="!member.isEditing" class="info-value">{{ member.id }}</div>
			    <div v-else><input v-model="member.id" type="text" class="input-edit"></div>
			</div>
            <div class="member-item">
                <div class="label">姓名：</div>
                <div v-if="!member.isEditing" class="info-value">{{ member.name }}</div>
                <div v-else><input v-model="member.name" type="text" class="input-edit"></div>
            </div>
            <div class="member-item">
                <div class="label">性别：</div>
                <div v-if="!member.isEditing" class="info-value">{{ member.gender }}</div>
                <div v-else><input v-model="member.gender" type="text" class="input-edit"></div>
            </div>
            <div class="member-item">
                <div class="label">入党时间：</div>
                <div v-if="!member.isEditing" class="info-value">{{ member.joinDate }}</div>
                <div v-else><input v-model="member.joinDate" type="text" class="input-edit"></div>
            </div>
            <div class="member-item">
                <div class="label">党内职务：</div>
                <div v-if="!member.isEditing" class="info-value">{{ member.position }}</div>
                <div v-else><input v-model="member.position" type="text" class="input-edit"></div>
            </div>
            <div class="member-item">
                <div class="label">所属支部：</div>
                <div v-if="!member.isEditing" class="info-value">{{ member.branch }}</div>
                <div v-else><input v-model="member.branch" type="text" class="input-edit"></div>
            </div>
        
            <div class="button-group">
                <button @click="toggleEdit(member)">
                    {{ member.isEditing ? '保 存' : '编 辑' }}
                </button>
                <button @click="deleteMember(index)">删 除</button>
            </div>
        </div>                   
    </div>
</template>

<script setup>
    import { ref } from 'vue';
	import { onLoad } from '@dcloudio/uni-app';

    const partyMembers = ref([
        {
			id:2023001,
            name: '张三',
            gender: '男',
            joinDate: '2020-01-01',
            position: '支部委员',
            branch: '第一支部',
            isEditing: false
        },
        {
			id:2023002,
            name: '李四',
            gender: '女',
            joinDate: '2021-05-10',
            position: '党小组组长',
            branch: '第二支部',
            isEditing: false
        }
    ]);

    const toggleEdit = (member) => {
        member.isEditing = !member.isEditing;
    };

    const deleteMember = (index) => {
        partyMembers.value.splice(index, 1);
    };
	
	onLoad((options) => {
		if (options.newMem) {
			const newMem = JSON.parse(decodeURIComponent(options.newMem));
			partyMembers.value.push(newMem);
		}
	});
</script>

<style scoped lang="scss">
	.container {
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
		.member-info{
			margin-bottom: 50rpx;
			border-bottom: 1rpx solid #424242;
			padding: 20rpx;
			.member-item{
				margin-bottom: 30rpx;	
				.label{
					margin-bottom: 20rpx;
				}
				.info-value,.input-edit{
					background-color: #eee;
					height: 60rpx;
					line-height: 60rpx;
					padding-left: 20rpx;
				}
			}
			.button-group{
				display: flex;
				button{
					width: 40%;	
				}
			}
		}
	}

</style>