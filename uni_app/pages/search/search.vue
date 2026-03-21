<template>
	<view class="page">
		<view class="search-bar">
			<input class="input" v-model="keyword" placeholder="搜索剧目、演员、剧场..." focus />
			<text class="btn-search" @click="doSearch">搜索</text>
		</view>
		<view class="tip">输入关键词进行搜索，支持剧目、演员、剧场、话题、用户</view>
	</view>
</template>

<script>
export default {
	data() { return { keyword: '' } },
	methods: {
		doSearch() {
			if (!this.keyword.trim()) return
			// TODO: 调用搜索 API
			uni.showToast({ title: '搜索: ' + this.keyword, icon: 'none' })
		}
	}
}
</script>

<style scoped>
.page { padding: 24rpx; }
.search-bar { display: flex; align-items: center; background: #f0f0f0; border-radius: 24rpx; padding: 20rpx 24rpx; margin-bottom: 24rpx; }
.input { flex: 1; font-size: 28rpx; }
.btn-search { color: #6366f1; font-size: 28rpx; margin-left: 24rpx; }
.tip { font-size: 26rpx; color: #888; }
</style>
