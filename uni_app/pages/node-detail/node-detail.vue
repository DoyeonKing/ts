<template>
	<view class="page">
		<view class="head card">
			<text class="name">{{ detail.name || '节点详情' }}</text>
			<text class="type">{{ typeLabel }}</text>
			<text class="desc">{{ detail.description || '暂无节点描述。' }}</text>
		</view>

		<view class="card block" v-if="detail.meta && detail.meta.length">
			<text class="block-title">基础信息</text>
			<view class="meta-list">
				<view class="meta-item" v-for="(m, i) in detail.meta" :key="i">
					<text class="meta-key">{{ m.key }}</text>
					<text class="meta-val">{{ m.value }}</text>
				</view>
			</view>
		</view>

		<view class="card block" v-if="related.length">
			<text class="block-title">关联节点</text>
			<view class="rel-list">
				<view class="rel-item" v-for="(r, i) in related" :key="i" @click="goNode(r)">
					<text class="rel-name">{{ r.name }}</text>
					<text class="rel-meta">{{ getTypeLabel(r.nodeType) }} · {{ r.relationText || '关联' }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { getNodeDetail, getNodeNeighborhood } from '../../api/knowledgeGraph.js'

const TYPE_LABELS = {
	PLAY: '剧目',
	ACTOR: '演员',
	TERMINOLOGY: '术语',
	TAG: '标签',
	VENUE: '场馆'
}

export default {
	data() {
		return {
			detail: {},
			related: []
		}
	},
	computed: {
		typeLabel() {
			return this.getTypeLabel(this.detail.nodeType)
		}
	},
	onLoad(opts) {
		const nodeId = opts.nodeId || opts.id
		const name = decodeURIComponent(opts.name || '')
		const nodeType = opts.nodeType || ''
		this.detail = {
			id: nodeId,
			name: name || '节点详情',
			nodeType,
			description: '',
			meta: []
		}
		if (nodeId) {
			this.loadDetail(nodeId)
			this.loadRelated(nodeId)
		}
	},
	methods: {
		async loadDetail(nodeId) {
			try {
				const res = await getNodeDetail(nodeId)
				const data = res.data || res || {}
				const merged = { ...this.detail, ...data }
				merged.meta = this.buildMeta(merged)
				this.detail = merged
			} catch (e) {
				this.detail = { ...this.detail, meta: this.buildMeta(this.detail) }
			}
		},
		async loadRelated(nodeId) {
			try {
				const res = await getNodeNeighborhood(nodeId)
				const data = res.data || res || {}
				const nodes = Array.isArray(data.nodes) ? data.nodes : []
				const edges = Array.isArray(data.edges) ? data.edges : []
				const map = {}
				nodes.forEach(n => { map[n.id] = n })
				this.related = edges
					.map(e => {
						const targetId = e.source === this.detail.id ? e.target : e.source
						const target = map[targetId]
						if (!target) return null
						return {
							id: target.id,
							name: target.name,
							nodeType: target.nodeType,
							relationText: e.label || e.relationType || '关联'
						}
					})
					.filter(Boolean)
					.slice(0, 20)
			} catch (e) {
				this.related = []
			}
		},
		buildMeta(node) {
			const list = []
			if (node.id != null) list.push({ key: '节点ID', value: String(node.id) })
			if (node.nodeType) list.push({ key: '节点类型', value: this.getTypeLabel(node.nodeType) })
			if (node.linkCount != null) list.push({ key: '关联数量', value: String(node.linkCount) })
			return list
		},
		getTypeLabel(type) {
			return TYPE_LABELS[type] || type || '节点'
		},
		goNode(node) {
			if (!node || node.id == null) return
			const encodedName = encodeURIComponent(node.name || '')
			if (node.nodeType === 'PLAY') {
				uni.navigateTo({ url: `/pages/play-detail/play-detail?nodeId=${node.id}&name=${encodedName}` }).catch(() => {})
			} else if (node.nodeType === 'ACTOR') {
				uni.navigateTo({ url: `/pages/actor-detail/actor-detail?nodeId=${node.id}&name=${encodedName}` }).catch(() => {})
			} else if (node.nodeType === 'TERMINOLOGY') {
				uni.navigateTo({ url: `/pages/terminology/terminology?nodeId=${node.id}&name=${encodedName}` }).catch(() => {})
			} else {
				uni.navigateTo({ url: `/pages/node-detail/node-detail?nodeId=${node.id}&name=${encodedName}&nodeType=${node.nodeType || ''}` }).catch(() => {})
			}
		}
	}
}
</script>

<style scoped>
.page { padding: 24rpx; min-height: 100vh; background: #f6f6f8; }
.card { background: #fff; border-radius: 22rpx; box-shadow: 0 6rpx 18rpx rgba(0,0,0,0.06); }
.head { padding: 30rpx; margin-bottom: 20rpx; }
.name { font-size: 38rpx; font-weight: 700; color: #222; display: block; margin-bottom: 12rpx; }
.type { font-size: 24rpx; color: #6366f1; display: inline-block; background: rgba(99,102,241,0.12); padding: 8rpx 14rpx; border-radius: 999rpx; margin-bottom: 14rpx; }
.desc { font-size: 28rpx; color: #555; line-height: 1.7; display: block; }
.block { padding: 26rpx; margin-bottom: 20rpx; }
.block-title { font-size: 30rpx; font-weight: 700; color: #333; display: block; margin-bottom: 16rpx; }
.meta-list, .rel-list { display: flex; flex-direction: column; gap: 14rpx; }
.meta-item, .rel-item { padding: 18rpx; background: #f8f8fb; border-radius: 14rpx; }
.meta-key { display: block; font-size: 24rpx; color: #888; margin-bottom: 6rpx; }
.meta-val { display: block; font-size: 28rpx; color: #333; }
.rel-name { display: block; font-size: 28rpx; color: #222; font-weight: 600; }
.rel-meta { display: block; font-size: 24rpx; color: #777; margin-top: 6rpx; }
</style>
