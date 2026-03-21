<template>
	<view class="page">
		<view class="header">
			<text class="title">行话科普</text>
			<text class="desc">剧场、演出行业常用术语解释，帮你听懂行内话</text>
		</view>
		<view class="search-bar">
			<input class="search-input" v-model="keyword" placeholder="搜索术语（如 SD、返场、卡司）" @input="onSearch" />
		</view>
		<view class="list" v-if="filteredList.length">
			<view class="card" v-for="(t, i) in filteredList" :key="t.id || i" @click="goGraph(t.id)">
				<view class="card-head">
					<text class="term-name">{{ t.name }}</text>
					<text class="card-arrow">›</text>
				</view>
				<text class="term-desc" v-if="t.description">{{ t.description }}</text>
				<view class="card-footer">
					<text class="link-graph">在图谱中查看关系</text>
				</view>
			</view>
		</view>
		<view class="empty" v-else-if="!loading">
			<text class="empty-text">暂无术语数据</text>
			<text class="empty-hint">请先在后端执行「知识图谱 → 初始化种子数据」</text>
		</view>
		<view class="loading-wrap" v-if="loading">
			<text class="loading-text">加载中...</text>
		</view>
	</view>
</template>

<script>
import { getNodesByType } from '../../api/knowledgeGraph.js'

export default {
	data() {
		return {
			keyword: '',
			list: [],
			loading: false
		}
	},
	computed: {
		filteredList() {
			const k = (this.keyword || '').trim().toLowerCase()
			if (!k) return this.list
			return this.list.filter(t => (t.name && t.name.toLowerCase().includes(k)) || (t.description && t.description.toLowerCase().includes(k)))
		}
	},
	onLoad() {
		this.loadTerminology()
	},
	methods: {
		async loadTerminology() {
			this.loading = true
			try {
				const res = await getNodesByType('TERMINOLOGY')
				const data = (res && res.data) ? res.data : res
				this.list = Array.isArray(data) ? data : []
			} catch (e) {
				this.list = []
			}
			this.loading = false
		},
		onSearch() {
			// 由 computed filteredList 自动过滤
		},
		goGraph(nodeId) {
			uni.navigateTo({ url: `/pages/graph-view/graph-view?nodeId=${nodeId}` }).catch(() => {})
		}
	}
}
</script>

<style scoped>
.page { padding: 24rpx; padding-bottom: 48rpx; min-height: 100vh; background: #f5f5f5; }
.header { margin-bottom: 24rpx; }
.title { font-size: 38rpx; font-weight: bold; color: #333; display: block; margin-bottom: 12rpx; }
.desc { font-size: 26rpx; color: #666; line-height: 1.5; display: block; }
.search-bar { margin-bottom: 24rpx; }
.search-input { width: 100%; height: 72rpx; background: #fff; border-radius: 36rpx; padding: 0 28rpx; font-size: 28rpx; box-sizing: border-box; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06); }
.list { display: flex; flex-direction: column; gap: 20rpx; }
.card { background: #fff; border-radius: 20rpx; padding: 28rpx; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06); }
.card-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12rpx; }
.term-name { font-size: 32rpx; font-weight: bold; color: #10b981; }
.card-arrow { font-size: 32rpx; color: #999; }
.term-desc { font-size: 28rpx; color: #666; line-height: 1.6; display: block; }
.card-footer { margin-top: 16rpx; padding-top: 16rpx; border-top: 1rpx solid #f0f0f0; }
.link-graph { font-size: 24rpx; color: #6366f1; }
.empty { text-align: center; padding: 80rpx 40rpx; }
.empty-text { font-size: 30rpx; color: #999; display: block; }
.empty-hint { font-size: 24rpx; color: #bbb; display: block; margin-top: 16rpx; }
.loading-wrap { text-align: center; padding: 40rpx; }
.loading-text { font-size: 28rpx; color: #999; }
</style>
