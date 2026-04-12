<template>
	<view class="page">
		<view class="placeholder">
			<text class="icon">📰</text>
			<text class="title">新闻资讯</text>
			<text class="desc">行业动态、演出资讯、剧目上新与巡演信息</text>
			<text class="hint">敬请期待</text>
		</view>
	</view>
</template>

<script>
export default {}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f5f5; display: flex; align-items: center; justify-content: center; padding: 48rpx; }
.placeholder { text-align: center; }
.icon { font-size: 120rpx; display: block; margin-bottom: 32rpx; }
.title { font-size: 36rpx; font-weight: bold; color: #333; display: block; margin-bottom: 16rpx; }
.desc { font-size: 28rpx; color: #666; line-height: 1.5; display: block; margin-bottom: 32rpx; }
.hint { font-size: 26rpx; color: #999; }
</style>
