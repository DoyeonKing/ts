<template>
	<view class="page">
		<view class="header">
			<text class="title">{{ isDetailMode ? currentTerm.name || '术语详情' : '行话科普' }}</text>
			<text class="desc">{{ isDetailMode ? (currentTerm.description || '剧场、演出行业常用术语解释') : '剧场、演出行业常用术语解释，帮你听懂行内话' }}</text>
		</view>

		<view v-if="isDetailMode && currentTerm.id" class="focus-card">
			<view class="focus-head">
				<text class="focus-name">{{ currentTerm.name }}</text>
				<text class="focus-badge">术语节点</text>
			</view>
			<text class="focus-desc">{{ currentTerm.description || '暂无术语解释' }}</text>
			<view class="focus-actions">
				<view class="focus-btn primary" @click="goGraph(currentTerm.id)">在图谱中查看关系</view>
				<view class="focus-btn" @click="exitDetailMode">查看全部术语</view>
			</view>
		</view>

		<view class="search-bar">
			<input class="search-input" v-model="keyword" :placeholder="isDetailMode ? '继续搜索其他术语' : '搜索术语（如 SD、返场、卡司）'" @input="onSearch" />
		</view>

		<view class="list" v-if="filteredList.length">
			<view class="card" v-for="(t, i) in filteredList" :key="i" @click="openTerm(t)">
				<view class="card-head">
					<text class="term-name">{{ t.name }}</text>
					<text class="card-arrow">›</text>
				</view>
				<text class="term-desc" v-if="t.description">{{ t.description }}</text>
				<view class="card-footer">
					<text class="link-graph">{{ isDetailMode && currentTerm.id === t.id ? '当前术语' : '查看术语详情' }}</text>
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
			loading: false,
			targetNodeId: '',
			targetName: '',
			isDetailMode: false,
			currentTerm: {}
		}
	},
	computed: {
		filteredList() {
			const k = (this.keyword || '').trim().toLowerCase()
			let base = this.list
			if (this.isDetailMode && this.currentTerm && this.currentTerm.id) {
				base = [this.currentTerm, ...this.list.filter(t => t.id !== this.currentTerm.id)]
			}
			if (!k) return base
			return base.filter(t => (t.name && t.name.toLowerCase().includes(k)) || (t.description && t.description.toLowerCase().includes(k)))
		}
	},
	onLoad(opts) {
		this.targetNodeId = opts.nodeId || ''
		this.targetName = decodeURIComponent(opts.name || '')
		this.isDetailMode = !!(this.targetNodeId || this.targetName)
		if (this.targetName) {
			this.keyword = this.targetName
		}
		this.loadTerminology()
	},
	methods: {
		async loadTerminology() {
			this.loading = true
			try {
				const res = await getNodesByType('TERMINOLOGY')
				const data = (res && res.data) ? res.data : res
				this.list = Array.isArray(data) ? data : []
				this.applyTargetTerm()
			} catch (e) {
				this.list = []
				this.currentTerm = {}
			}
			this.loading = false
		},
		applyTargetTerm() {
			if (!this.isDetailMode) return
			const byId = this.list.find(t => String(t.id) === String(this.targetNodeId))
			const byName = this.list.find(t => (t.name || '') === this.targetName)
			this.currentTerm = byId || byName || {}
			if (!this.currentTerm.id && this.targetName) {
				this.currentTerm = { name: this.targetName, description: '暂无术语解释', id: this.targetNodeId || '' }
			}
		},
		onSearch() {
			// 由 computed filteredList 自动过滤
		},
		openTerm(term) {
			if (!term) return
			this.isDetailMode = true
			this.currentTerm = term
			this.targetNodeId = term.id
			this.targetName = term.name || ''
			this.keyword = term.name || ''
		},
		exitDetailMode() {
			this.isDetailMode = false
			this.currentTerm = {}
			this.targetNodeId = ''
			this.targetName = ''
			this.keyword = ''
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
.focus-card { background: linear-gradient(180deg, #f0fff8, #ffffff); border-radius: 24rpx; padding: 28rpx; margin-bottom: 24rpx; box-shadow: 0 8rpx 24rpx rgba(16,185,129,0.08); border: 1rpx solid rgba(16,185,129,0.14); }
.focus-head { display: flex; justify-content: space-between; align-items: center; gap: 16rpx; margin-bottom: 16rpx; }
.focus-name { font-size: 34rpx; font-weight: bold; color: #10b981; }
.focus-badge { font-size: 22rpx; color: #0f766e; background: rgba(16,185,129,0.12); padding: 8rpx 16rpx; border-radius: 999rpx; }
.focus-desc { font-size: 28rpx; color: #4b5563; line-height: 1.7; display: block; }
.focus-actions { display: flex; gap: 16rpx; margin-top: 24rpx; }
.focus-btn { flex: 1; text-align: center; height: 72rpx; line-height: 72rpx; border-radius: 36rpx; background: #eef2ff; color: #4f46e5; font-size: 26rpx; }
.focus-btn.primary { background: #10b981; color: #fff; }
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
