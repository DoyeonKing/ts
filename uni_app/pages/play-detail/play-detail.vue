<template>
	<view class="page">
		<view class="card head">
			<text class="title">{{ detail.name || '剧目详情' }}</text>
			<text class="meta">{{ detail.genre || '-' }} · {{ detail.rating || '★★★★★' }}</text>
			<view class="tags" v-if="detail.tags && detail.tags.length">
				<text class="tag" v-for="(t, i) in detail.tags" :key="i">{{ t }}</text>
			</view>
		</view>

		<view class="block">
			<text class="block-title">背景剧情</text>
			<text class="block-content">{{ detail.plot || detail.desc || '暂无剧情简介。' }}</text>
		</view>

		<view class="block" v-if="detail.characters && detail.characters.length">
			<text class="block-title">主要人物</text>
			<view class="char-list">
				<view class="char-item" v-for="(c, i) in detail.characters" :key="i">
					<text class="char-name">{{ c.name }}</text>
					<text class="char-desc">{{ c.desc }}</text>
				</view>
			</view>
		</view>

		<view class="block" v-if="detail.actors && detail.actors.length">
			<text class="block-title">相关演员</text>
			<view class="actor-list">
				<view class="actor-item" v-for="(a, i) in detail.actors" :key="i" @click="goActor(a.id)">
					<text class="actor-name">{{ a.name }}</text>
					<text class="actor-role" v-if="a.role">饰 {{ a.role }}</text>
				</view>
			</view>
		</view>

		<view class="block performance-block">
			<view class="performance-head">
				<text class="block-title">正在上演</text>
				<text class="performance-status" :class="hasPerformances ? 'on' : 'off'">{{ hasPerformances ? '有在售场次' : '暂无在售场次' }}</text>
			</view>
			<view v-if="performanceLoading" class="performance-loading">正在查询演出安排...</view>
			<view v-else-if="hasPerformances" class="performance-list">
				<view class="performance-item" v-for="item in performances" :key="item.id" @click="goPerformance(item.id)">
					<view class="performance-main">
						<text class="performance-venue">{{ item.venueName }}</text>
						<text class="performance-time">{{ formatDateTime(item.startTime) }}</text>
						<text class="performance-city">{{ item.city }} · {{ formatPrice(item.minPrice, item.maxPrice) }}</text>
					</view>
					<text class="performance-arrow">›</text>
				</view>
			</view>
			<view v-else class="performance-empty">
				<text class="performance-empty-text">当前这个剧目还没有可跳转的演出场次。</text>
			</view>
		</view>

		<view class="block" v-if="recommendations.plays.length || recommendations.actors.length">
			<text class="block-title">猜你喜欢</text>
			<view class="rec-plays" v-if="recommendations.plays.length">
				<text class="rec-label">相似剧目</text>
				<view class="rec-list">
					<view class="rec-item" v-for="(p, i) in recommendations.plays" :key="i" @click="goPlay(p.id, p.name)">
						<text class="rec-name">{{ p.name }}</text>
						<text class="rec-reason">{{ p.reason }}</text>
					</view>
				</view>
			</view>
			<view class="rec-actors" v-if="recommendations.actors.length">
				<text class="rec-label">相关演员</text>
				<view class="rec-list">
					<view class="rec-item" v-for="(a, i) in recommendations.actors" :key="i" @click="goActor(a.id)">
						<text class="rec-name">{{ a.name }}</text>
						<text class="rec-reason">{{ a.reason }}</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { getRecommendationsForPlay, lookupNodeId } from '../../api/recommendation.js'
import { getPerformancesByPlayId } from '../../api/performance.js'
import { getNodeDetail, getNodeNeighborhood } from '../../api/knowledgeGraph.js'

function normalizeName(name) {
	return decodeURIComponent(name || '').replace(/[《》]/g, '').trim()
}

function wrapPlayName(name) {
	const n = normalizeName(name)
	return n ? `《${n}》` : '剧目详情'
}

function parseExtraData(raw) {
	if (!raw) return {}
	if (typeof raw === 'object') return raw
	try {
		return JSON.parse(raw)
	} catch (e) {
		return {}
	}
}

function ratingFromScore(score) {
	const n = Number(score)
	if (!Number.isFinite(n)) return '★★★★★'
	if (n >= 9.5) return '★★★★★'
	if (n >= 8.8) return '★★★★☆'
	if (n >= 8.0) return '★★★☆☆'
	return '★★☆☆☆'
}

function buildFallbackDetail(opts = {}, playId = null) {
	return {
		id: playId || opts.nodeId || opts.id || '',
		name: wrapPlayName(opts.name),
		genre: '-',
		rating: '★★★★★',
		tags: [],
		plot: '',
		characters: [],
		actors: []
	}
}

export default {
	data() {
		return {
			detail: {},
			recommendations: { plays: [], actors: [], others: [] },
			performances: [],
			performanceLoading: false
		}
	},
	computed: {
		hasPerformances() {
			return this.performances.length > 0
		}
	},
	onLoad(opts) {
		this.initPageData(opts || {})
	},
	methods: {
		async initPageData(opts) {
			const resolvedId = await this.resolvePlayId(opts)
			if (!resolvedId) {
				this.detail = buildFallbackDetail(opts)
				this.recommendations = { plays: [], actors: [], others: [] }
				this.performances = []
				return
			}

			this.detail = buildFallbackDetail(opts, resolvedId)
			await this.loadPlayDetail(resolvedId, opts)
			this.loadRecommendations(resolvedId)
			this.loadPerformances(resolvedId)
		},
		async resolvePlayId(opts) {
			const name = normalizeName(opts.name)
			if (name) {
				try {
					const res = await lookupNodeId(name, 'PLAY')
					const data = res.data || res
					if (data && data.found && Number(data.nodeId) > 0) {
						return Number(data.nodeId)
					}
				} catch (e) {}
			}

			const rawId = opts.nodeId || opts.id
			const numericId = Number(rawId)
			if (Number.isFinite(numericId) && numericId > 0) {
				return numericId
			}

			return null
		},
		async loadPlayDetail(playId, opts) {
			try {
				const [detailRes, neighborhoodRes] = await Promise.all([
					getNodeDetail(playId),
					getNodeNeighborhood(playId)
				])

				const detailData = detailRes.data || detailRes
				const node = (detailData && detailData.node) || {}
				const extra = parseExtraData(node.extraData)

				const neighborhoodData = neighborhoodRes.data || neighborhoodRes
				const nodes = Array.isArray(neighborhoodData.nodes) ? neighborhoodData.nodes : []
				const edges = Array.isArray(neighborhoodData.edges) ? neighborhoodData.edges : []
				const nodeMap = new Map(nodes.map(item => [item.id, item]))

				const tags = edges
					.filter(edge => edge.source === playId && edge.relationType === 'HAS_TAG')
					.map(edge => nodeMap.get(edge.target))
					.filter(item => item && item.name)
					.map(item => item.name)

				const actors = edges
					.filter(edge => edge.target === playId && edge.relationType === 'PERFORMS_IN')
					.map(edge => nodeMap.get(edge.source))
					.filter(item => item && item.name)
					.map(item => ({ id: item.id, name: item.name, role: '' }))

				const name = node.name || normalizeName(opts.name)
				this.detail = {
					id: playId,
					name: wrapPlayName(name),
					genre: extra.genre || '-',
					rating: ratingFromScore(extra.rating),
					tags,
					plot: node.description || '',
					characters: Array.isArray(extra.characters) ? extra.characters : [],
					actors
				}
			} catch (e) {
				this.detail = buildFallbackDetail(opts, playId)
			}
		},
		async loadRecommendations(playId) {
			try {
				const res = await getRecommendationsForPlay(playId)
				const data = res.data || res
				this.recommendations = {
					plays: data.plays || [],
					actors: data.actors || [],
					others: data.others || []
				}
			} catch (e) {
				this.recommendations = { plays: [], actors: [], others: [] }
			}
		},
		async loadPerformances(playId) {
			this.performanceLoading = true
			try {
				const res = await getPerformancesByPlayId(playId)
				const data = res.data || res
				this.performances = Array.isArray(data) ? data : []
			} catch (e) {
				this.performances = []
			} finally {
				this.performanceLoading = false
			}
		},
		formatDateTime(value) {
			if (!value) return '-'
			return String(value).replace('T', ' ').slice(0, 16)
		},
		formatPrice(minPrice, maxPrice) {
			if (minPrice == null && maxPrice == null) return '票价待定'
			if (minPrice == null) return `最高¥${maxPrice}`
			if (maxPrice == null || Number(minPrice) === Number(maxPrice)) return `¥${minPrice}起`
			return `¥${minPrice}-¥${maxPrice}`
		},
		goPerformance(id) {
			uni.navigateTo({ url: `/pages/performance-detail/performance-detail?id=${id}` }).catch(() => {})
		},
		goActor(id) {
			uni.navigateTo({ url: `/pages/actor-detail/actor-detail?nodeId=${id}` }).catch(() => {})
		},
		goPlay(id, name) {
			const encodedName = encodeURIComponent((name || '').replace(/[《》]/g, ''))
			uni.navigateTo({ url: `/pages/play-detail/play-detail?nodeId=${id}&name=${encodedName}` }).catch(() => {})
		}
	}
}
</script>

<style scoped>
.page { padding: 24rpx; padding-bottom: 48rpx; min-height: 100vh; background: #f5f5f5; }
.card.head { background: #fff; border-radius: 24rpx; padding: 36rpx; margin-bottom: 24rpx; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06); }
.title { font-size: 38rpx; font-weight: bold; color: #333; display: block; margin-bottom: 12rpx; }
.meta { font-size: 28rpx; color: #6366f1; display: block; margin-bottom: 16rpx; }
.tags { display: flex; flex-wrap: wrap; gap: 12rpx; }
.tag { font-size: 24rpx; color: #666; background: #f0f0f0; padding: 8rpx 16rpx; border-radius: 8rpx; }
.block { background: #fff; border-radius: 20rpx; padding: 28rpx; margin-bottom: 24rpx; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06); }
.block-title { font-size: 30rpx; font-weight: bold; color: #333; display: block; margin-bottom: 16rpx; }
.block-content { font-size: 28rpx; color: #666; line-height: 1.6; display: block; }
.char-list, .actor-list, .performance-list { display: flex; flex-direction: column; gap: 16rpx; }
.char-item, .actor-item { padding-bottom: 16rpx; border-bottom: 1rpx solid #f0f0f0; }
.char-item:last-child, .actor-item:last-child { border-bottom: none; padding-bottom: 0; }
.char-name, .actor-name { font-size: 28rpx; font-weight: 500; color: #333; display: block; }
.char-desc, .actor-role { font-size: 26rpx; color: #666; display: block; margin-top: 4rpx; }
.performance-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.performance-status { font-size: 24rpx; padding: 8rpx 16rpx; border-radius: 999rpx; }
.performance-status.on { color: #0f766e; background: rgba(16,185,129,0.12); }
.performance-status.off { color: #999; background: #f3f4f6; }
.performance-loading, .performance-empty-text { font-size: 26rpx; color: #888; }
.performance-item { display: flex; justify-content: space-between; align-items: center; padding: 20rpx; background: #f8f8f8; border-radius: 16rpx; }
.performance-main { flex: 1; min-width: 0; }
.performance-venue { font-size: 28rpx; font-weight: 600; color: #333; display: block; }
.performance-time, .performance-city { font-size: 24rpx; color: #666; display: block; margin-top: 8rpx; }
.performance-arrow { font-size: 34rpx; color: #999; margin-left: 16rpx; }
.rec-plays, .rec-actors { margin-bottom: 20rpx; }
.rec-plays:last-child, .rec-actors:last-child { margin-bottom: 0; }
.rec-label { font-size: 26rpx; color: #6366f1; display: block; margin-bottom: 12rpx; }
.rec-list { display: flex; flex-direction: column; gap: 12rpx; }
.rec-item { padding: 16rpx; background: #f8f8f8; border-radius: 12rpx; }
.rec-name { font-size: 28rpx; font-weight: 500; color: #333; display: block; }
.rec-reason { font-size: 24rpx; color: #888; display: block; margin-top: 4rpx; }
</style>
