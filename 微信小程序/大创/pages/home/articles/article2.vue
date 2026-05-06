<template>
	<view class="container">	
		<view class="article">
			<view class="title">坚持高质量办理人大代表建议 努力做到民有所呼我有所应</view>
			<view class="content">
				<view class="paragraph">本报北京4月1日电 （记者张天培）中共中央政治局委员、全国人大常委会副委员长王晨1日在京出席十三届全国人大五次会议代表建议交办会并讲话。他强调，要以习近平新时代中国特色社会主义思想为指导，深入学习贯彻习近平总书记在全国两会上的重要讲话精神，不断发展全过程人民民主，坚持高质量办理人大代表建议，努力做到民有所呼、我有所应，以实际行动迎接党的二十大胜利召开。</view>
				<view class="paragraph">王晨指出，十三届全国人大五次会议期间，2900多名全国人大代表依法行使职权，提出对各方面工作的建议9203件，数量为5年来最高，展现了代表积极服务党和国家工作大局的履职担当。全国人大常委会办公厅会同有关方面，对代表提出的建议进行了认真梳理和分析，依法交由208家承办单位研究办理。办好代表建议既是承办单位的法定职责，也体现了对人民负责、受人民监督的宪法精神。要认真办理每一件代表建议，进一步推动解决问题、改进工作、完善制度，推动党中央各项决策部署落地见效。</view>
				<view class="paragraph">王晨强调，要把办理代表建议同解决人民群众急难愁盼问题结合起来，倾听人民呼声，回应人民期盼，让各项工作更好接地气、察民情、聚民智、惠民生。要坚持“内容高质量、办理高质量”“既要重结果、也要重过程”，不断提高代表建议办理工作水平。</view>
			
			</view>
		</view>
	</view>
</template>

<script setup>
	
</script>

<style lang="scss" scoped>
	.container {
		width: 100%;
		min-height: 100vh;
		background-color: #f5f5f5;
		display: flex;
		flex-direction: column;
	}
	
	// 文章内容样式
	.article {
		flex: 1;
		padding: 30rpx;
		background-color: white;
		margin: 20rpx;
		border-radius: 10rpx;
		box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.1);
		
		.title {
			font-size: 36rpx;
			color: #333;
			font-weight: bold;
			padding-bottom: 20rpx;
			border-bottom: 1px solid #eee;
			margin-bottom: 20rpx;
		}
		
		.content {
			.paragraph {
				font-size: 28rpx;
				color: #666;
				line-height: 48rpx;
				margin-bottom: 20rpx;
			}
		}
	}
</style>

