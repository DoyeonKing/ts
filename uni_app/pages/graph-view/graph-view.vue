<template>
	<view class="page">
		<view class="toolbar-shell">
			<view class="toolbar">
				<view class="toolbar-top">
					<view class="toolbar-title-wrap">
						<text class="toolbar-kicker">STAGE MAP</text>
						<text class="toolbar-title">知识图谱全景</text>
					</view>
					<view class="toolbar-badge">拖拽 · 搜索 · 聚焦</view>
				</view>
				<view class="filter-row">
					<view
						v-for="t in typeFilters" :key="t.key"
						class="filter-btn"
						:class="{ active: activeTypes.includes(t.key) }"
						:style="{ borderColor: activeTypes.includes(t.key) ? t.color : 'rgba(255,255,255,0.14)', color: activeTypes.includes(t.key) ? '#fff7eb' : 'rgba(255,240,228,0.72)', background: activeTypes.includes(t.key) ? t.color : 'rgba(255,255,255,0.04)' }"
						@click="toggleType(t.key)"
					>{{ t.label }}</view>
				</view>
				<view class="search-row">
					<input class="search-input" v-model="searchKey" placeholder="搜索节点、人物、术语..." placeholder-style="color: rgba(255,240,228,0.35);" @confirm="onSearch" />
					<view class="search-btn" @click="onSearch">搜索</view>
				</view>
			</view>
		</view>

		<view class="tap-guide">轻点节点会弹出操作菜单，可直接查看详情；节点密集时请点圆心附近</view>
		<canvas
			type="2d"
			id="graphCanvas"
			class="graph-canvas"
			:style="{ width: canvasW + 'px', height: canvasH + 'px' }"
			@touchstart="onTouchStart"
			@touchmove="onTouchMove"
			@touchend="onTouchEnd"
		></canvas>

		<!-- 图例 -->
		<view class="legend-shell">
			<view class="tap-hint">轻点圆形节点查看详情，拖动画布可移动视角</view>
			<view class="legend">
				<view class="legend-item" v-for="t in typeFilters" :key="t.key">
					<view class="legend-dot" :style="{ background: t.color, boxShadow: '0 0 12rpx ' + t.color }"></view>
					<text class="legend-text">{{ t.label }}</text>
				</view>
			</view>
		</view>

		<!-- 节点详情弹窗 -->
		<view class="detail-mask" v-if="selectedNode" @click="selectedNode = null">
			<view class="detail-card" @click.stop>
				<view class="detail-header">
					<view class="detail-dot" :style="{ background: getColor(selectedNode.nodeType) }"></view>
					<text class="detail-name">{{ selectedNode.name }}</text>
					<text class="detail-type">{{ getTypeLabel(selectedNode.nodeType) }}</text>
				</view>
				<text class="detail-desc" v-if="selectedNode.description">{{ selectedNode.description }}</text>
				<view class="detail-links" v-if="selectedNode.linkCount">
					<text class="detail-link-count">关联 {{ selectedNode.linkCount }} 个节点</text>
				</view>
				<!-- 智能推荐：猜你喜欢 -->
				<view class="detail-rec" v-if="nodeRecommendations.plays.length || nodeRecommendations.actors.length || nodeRecommendations.others.length">
					<text class="detail-rec-title">猜你喜欢</text>
					<view class="detail-rec-list">
						<view class="detail-rec-item" v-for="(r, i) in allRecommendations" :key="i" @tap="onRecommendClick(r)">
							<view class="detail-rec-text">
								<text class="detail-rec-name">{{ r.name }}</text>
								<text class="detail-rec-reason">{{ r.reason }}</text>
							</view>
							<text class="detail-rec-go">›</text>
						</view>
					</view>
				</view>
				<view class="detail-actions">
					<view class="detail-btn primary" v-if="hasNodeDetailPage(selectedNode)" @tap="goToNodeDetail(selectedNode)">进入{{ getTypeLabel(selectedNode.nodeType) }}详情</view>
					<view class="detail-btn" @tap="focusNode(selectedNode.id)">以此为中心展开</view>
					<view class="detail-btn secondary" @tap="selectedNode = null">关闭</view>
				</view>
			</view>
		</view>

		<!-- 加载状态 -->
		<view class="loading-mask" v-if="loading">
			<view class="loading-ring"></view>
			<text class="loading-text">舞台布景加载中...</text>
		</view>
	</view>
</template>

<script>
import { getFullGraph, getNodeNeighborhood, searchNodes } from '../../api/knowledgeGraph.js'
import { getRecommendationsForNode } from '../../api/recommendation.js'

const TYPE_COLORS = {
	PLAY: '#6366f1',
	ACTOR: '#f59e0b',
	TERMINOLOGY: '#10b981',
	TAG: '#8b5cf6',
	VENUE: '#ef4444'
}

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
			canvasW: 375,
			canvasH: 600,
			ctx: null,
			canvas: null,
			dpr: 1,

			nodes: [],
			edges: [],
			simNodes: [],
			simEdges: [],

			selectedNode: null,
			nodeRecommendations: { plays: [], actors: [], others: [] },
			recommendationCache: {},
			recommendationTimer: null,
			searchKey: '',
			loading: false,

			activeTypes: ['PLAY', 'ACTOR', 'TERMINOLOGY', 'TAG', 'VENUE'],
			typeFilters: [
				{ key: 'PLAY', label: '剧目', color: TYPE_COLORS.PLAY },
				{ key: 'ACTOR', label: '演员', color: TYPE_COLORS.ACTOR },
				{ key: 'TERMINOLOGY', label: '术语', color: TYPE_COLORS.TERMINOLOGY },
				{ key: 'TAG', label: '标签', color: TYPE_COLORS.TAG },
				{ key: 'VENUE', label: '场馆', color: TYPE_COLORS.VENUE }
			],

			// 视图变换
			offsetX: 0,
			offsetY: 0,
			scale: 1,

			// 触摸状态
			touchStartX: 0,
			touchStartY: 0,
			pressCandidateNode: null,
			pressStartedOnNode: false,
			draggingNode: null,
			isPanning: false,
			lastPanX: 0,
			lastPanY: 0,

			animFrameId: null,
			simRunning: false,
			simAlpha: 1,

			focusNodeId: null,
			canvasRect: null
		}
	},

	onLoad(opts) {
		const sysInfo = uni.getSystemInfoSync()
		this.canvasW = sysInfo.windowWidth
		this.canvasH = sysInfo.windowHeight - 160
		this.dpr = sysInfo.pixelRatio || 2
		this.offsetX = this.canvasW / 2
		this.offsetY = this.canvasH / 2

		if (opts && opts.nodeId) {
			this.focusNodeId = parseInt(opts.nodeId)
		}
	},

	onReady() {
		this.initCanvas()
	},

	onShow() {
		if (this.selectedNode) {
			this.selectedNode = null
			this.nodeRecommendations = { plays: [], actors: [], others: [] }
		}
		this.pressCandidateNode = null
		this.pressStartedOnNode = false
		this.draggingNode = null
		this.isPanning = false
		if (this.ctx) {
			this.$nextTick(() => this.render())
		}
	},

	onUnload() {
		this.simRunning = false
		if (this.recommendationTimer) {
			clearTimeout(this.recommendationTimer)
			this.recommendationTimer = null
		}
	},

	watch: {
		selectedNode(n) {
			if (this.recommendationTimer) {
				clearTimeout(this.recommendationTimer)
				this.recommendationTimer = null
			}
			if (n && n.id) {
				this.nodeRecommendations = this.recommendationCache[n.id] || { plays: [], actors: [], others: [] }
				this.recommendationTimer = setTimeout(() => {
					this.loadNodeRecommendations(n.id)
				}, 120)
			} else {
				this.nodeRecommendations = { plays: [], actors: [], others: [] }
			}
		}
	},

	computed: {
		allRecommendations() {
			const r = this.nodeRecommendations
			const list = [...(r.plays || []), ...(r.actors || []), ...(r.others || [])]
			return list.slice(0, 8)
		}
	},

	methods: {
		async loadNodeRecommendations(nodeId) {
			if (this.recommendationCache[nodeId]) {
				this.nodeRecommendations = this.recommendationCache[nodeId]
				return
			}
			try {
				const res = await getRecommendationsForNode(nodeId)
				const data = res.data || res
				const nextRecommendations = {
					plays: data.plays || [],
					actors: data.actors || [],
					others: data.others || []
				}
				this.recommendationCache = {
					...this.recommendationCache,
					[nodeId]: nextRecommendations
				}
				if (this.selectedNode && this.selectedNode.id === nodeId) {
					this.nodeRecommendations = nextRecommendations
				}
			} catch (e) {
				if (this.selectedNode && this.selectedNode.id === nodeId) {
					this.nodeRecommendations = { plays: [], actors: [], others: [] }
				}
			}
		},
		/** 进入当前选中节点的详情页（剧目/演员/术语等） */
		goToNodeDetail(node) {
			if (!node) return
			this.selectedNode = null
			const encodedName = encodeURIComponent(node.name || '')
			if (node.nodeType === 'PLAY') {
				uni.navigateTo({ url: `/pages/play-detail/play-detail?nodeId=${node.id}&name=${encodedName}` }).catch(() => {})
			} else if (node.nodeType === 'ACTOR') {
				uni.navigateTo({ url: `/pages/actor-detail/actor-detail?nodeId=${node.id}&name=${encodedName}` }).catch(() => {})
			} else if (node.nodeType === 'TERMINOLOGY') {
				uni.navigateTo({ url: `/pages/terminology/terminology?nodeId=${node.id}&name=${encodedName}` }).catch(() => {})
			} else {
				this.focusNode(node.id)
			}
		},
		/** 当前节点是否有独立详情页（用于显示/隐藏「进入详情」按钮） */
		hasNodeDetailPage(node) {
			if (!node) return false
			return node.nodeType === 'PLAY' || node.nodeType === 'ACTOR' || node.nodeType === 'TERMINOLOGY'
		},
		/** 点击「猜你喜欢」某一项：跳转详情或在图谱中展开 */
		onRecommendClick(r) {
			this.selectedNode = null
			const encodedName = encodeURIComponent(r.name || '')
			if (r.nodeType === 'PLAY') {
				uni.navigateTo({ url: `/pages/play-detail/play-detail?nodeId=${r.id}&name=${encodedName}` }).catch(() => {})
			} else if (r.nodeType === 'ACTOR') {
				uni.navigateTo({ url: `/pages/actor-detail/actor-detail?nodeId=${r.id}&name=${encodedName}` }).catch(() => {})
			} else if (r.nodeType === 'TERMINOLOGY') {
				uni.navigateTo({ url: `/pages/terminology/terminology?nodeId=${r.id}&name=${encodedName}` }).catch(() => {})
			} else {
				this.focusNode(r.id)
			}
		},
		initCanvas() {
			const query = uni.createSelectorQuery().in(this)
			query.select('#graphCanvas').fields({ node: true, size: true }).exec((res) => {
				if (!res || !res[0] || !res[0].node) {
					// 降级：使用旧版 canvas API
					this.initCanvasLegacy()
					return
				}
				const canvas = res[0].node
				const ctx = canvas.getContext('2d')
				canvas.width = this.canvasW * this.dpr
				canvas.height = this.canvasH * this.dpr
				ctx.scale(this.dpr, this.dpr)
				this.ctx = ctx
				this.canvas = canvas
				this.loadData()
				this.updateCanvasRect()
			})
		},
		updateCanvasRect() {
			const q = uni.createSelectorQuery().in(this)
			q.select('#graphCanvas').boundingClientRect(rect => {
				if (rect) this.canvasRect = { left: rect.left, top: rect.top }
			}).exec()
		},

		initCanvasLegacy() {
			const ctx = uni.createCanvasContext('graphCanvas', this)
			this.ctx = ctx
			this.canvas = null
			this.loadData()
			this.updateCanvasRect()
		},

		async loadData() {
			this.loading = true
			try {
				let res
				if (this.focusNodeId) {
					res = await getNodeNeighborhood(this.focusNodeId)
				} else {
					res = await getFullGraph()
				}
				const data = res.data || res
				this.nodes = data.nodes || []
				this.edges = data.edges || []
				this.initSimulation()
			} catch (e) {
				console.error('加载图谱失败', e)
				this.useMockData()
			}
			this.loading = false
		},

		useMockData() {
			this.nodes = [
				{ id: 1, name: '哈姆雷特', nodeType: 'PLAY', linkCount: 6, description: '莎士比亚四大悲剧之一' },
				{ id: 2, name: '李尔王', nodeType: 'PLAY', linkCount: 4, description: '莎士比亚四大悲剧' },
				{ id: 3, name: '罗密欧与朱丽叶', nodeType: 'PLAY', linkCount: 3 },
				{ id: 4, name: '天鹅湖', nodeType: 'PLAY', linkCount: 3 },
				{ id: 5, name: '茶花女', nodeType: 'PLAY', linkCount: 2 },
				{ id: 6, name: '茶馆', nodeType: 'PLAY', linkCount: 4 },
				{ id: 7, name: '濮存昕', nodeType: 'ACTOR', linkCount: 4 },
				{ id: 8, name: '袁泉', nodeType: 'ACTOR', linkCount: 2 },
				{ id: 9, name: '何冰', nodeType: 'ACTOR', linkCount: 3 },
				{ id: 10, name: '莎士比亚', nodeType: 'TAG', linkCount: 4 },
				{ id: 11, name: '悲剧', nodeType: 'TAG', linkCount: 3 },
				{ id: 12, name: '经典', nodeType: 'TAG', linkCount: 3 },
				{ id: 13, name: '独白', nodeType: 'TERMINOLOGY', linkCount: 2 },
				{ id: 14, name: '谢幕', nodeType: 'TERMINOLOGY', linkCount: 2 },
				{ id: 15, name: '国家大剧院', nodeType: 'VENUE', linkCount: 2 },
				{ id: 16, name: '北京人艺', nodeType: 'VENUE', linkCount: 3 }
			]
			this.edges = [
				{ source: 7, target: 1, relationType: 'PERFORMS_IN', label: '饰 哈姆雷特' },
				{ source: 7, target: 2, relationType: 'PERFORMS_IN', label: '饰 李尔王' },
				{ source: 7, target: 6, relationType: 'PERFORMS_IN', label: '饰 常四爷' },
				{ source: 8, target: 1, relationType: 'PERFORMS_IN', label: '饰 奥菲利亚' },
				{ source: 9, target: 2, relationType: 'PERFORMS_IN' },
				{ source: 9, target: 6, relationType: 'PERFORMS_IN' },
				{ source: 1, target: 10, relationType: 'HAS_TAG' },
				{ source: 1, target: 11, relationType: 'HAS_TAG' },
				{ source: 1, target: 12, relationType: 'HAS_TAG' },
				{ source: 2, target: 10, relationType: 'HAS_TAG' },
				{ source: 2, target: 11, relationType: 'HAS_TAG' },
				{ source: 3, target: 10, relationType: 'HAS_TAG' },
				{ source: 4, target: 12, relationType: 'HAS_TAG' },
				{ source: 6, target: 12, relationType: 'HAS_TAG' },
				{ source: 1, target: 13, relationType: 'HAS_TERMINOLOGY' },
				{ source: 1, target: 2, relationType: 'SIMILAR_TO' },
				{ source: 1, target: 3, relationType: 'SIMILAR_TO' },
				{ source: 1, target: 15, relationType: 'PERFORMED_AT' },
				{ source: 6, target: 16, relationType: 'PERFORMED_AT' },
				{ source: 7, target: 16, relationType: 'WORKS_AT' },
				{ source: 9, target: 16, relationType: 'WORKS_AT' },
				{ source: 13, target: 14, relationType: 'RELATED_TO' }
			]
			this.initSimulation()
		},

		initSimulation() {
			const visibleTypes = new Set(this.activeTypes)
			const filteredNodes = this.nodes.filter(n => visibleTypes.has(n.nodeType))
			const nodeIdSet = new Set(filteredNodes.map(n => n.id))

			const cx = 0, cy = 0
			this.simNodes = filteredNodes.map((n, i) => {
				const angle = (2 * Math.PI * i) / filteredNodes.length
				const r = Math.min(this.canvasW, this.canvasH) * 0.3
				return {
					...n,
					x: cx + r * Math.cos(angle) + (Math.random() - 0.5) * 30,
					y: cy + r * Math.sin(angle) + (Math.random() - 0.5) * 30,
					vx: 0,
					vy: 0,
					radius: this.getNodeRadius(n)
				}
			})

			this.simEdges = this.edges.filter(e => nodeIdSet.has(e.source) && nodeIdSet.has(e.target))
			this.simAlpha = 1
			this.startSimulation()
		},

		getNodeRadius(node) {
			const count = node.linkCount || 1
			const base = node.nodeType === 'TAG' ? 14 : 18
			return Math.min(base + count * 2, 36)
		},

		startSimulation() {
			this.simRunning = true
			const tick = () => {
				if (!this.simRunning) return
				this.simulationStep()
				this.render()
				if (this.simAlpha > 0.005) {
					if (this.canvas && this.canvas.requestAnimationFrame) {
						this.animFrameId = this.canvas.requestAnimationFrame(tick)
					} else {
						this.animFrameId = setTimeout(tick, 16)
					}
				} else {
					this.simRunning = false
					this.render()
				}
			}
			tick()
		},

		simulationStep() {
			const alpha = this.simAlpha
			const nodes = this.simNodes
			const edges = this.simEdges
			const nodeMap = {}
			nodes.forEach(n => { nodeMap[n.id] = n })

			// 斥力（节点之间互相排斥）
			const repulsion = 3000
			for (let i = 0; i < nodes.length; i++) {
				for (let j = i + 1; j < nodes.length; j++) {
					const a = nodes[i], b = nodes[j]
					let dx = b.x - a.x
					let dy = b.y - a.y
					let dist = Math.sqrt(dx * dx + dy * dy) || 1
					let force = repulsion / (dist * dist)
					let fx = (dx / dist) * force * alpha
					let fy = (dy / dist) * force * alpha
					a.vx -= fx
					a.vy -= fy
					b.vx += fx
					b.vy += fy
				}
			}

			// 引力（沿边吸引）
			const attraction = 0.06
			edges.forEach(e => {
				const a = nodeMap[e.source]
				const b = nodeMap[e.target]
				if (!a || !b) return
				let dx = b.x - a.x
				let dy = b.y - a.y
				let dist = Math.sqrt(dx * dx + dy * dy) || 1
				let force = (dist - 100) * attraction * alpha
				let fx = (dx / dist) * force
				let fy = (dy / dist) * force
				a.vx += fx
				a.vy += fy
				b.vx -= fx
				b.vy -= fy
			})

			// 中心引力
			const gravity = 0.02
			nodes.forEach(n => {
				n.vx -= n.x * gravity * alpha
				n.vy -= n.y * gravity * alpha
			})

			// 更新位置，阻尼
			const damping = 0.6
			nodes.forEach(n => {
				if (n === this.draggingNode) return
				n.vx *= damping
				n.vy *= damping
				n.x += n.vx
				n.y += n.vy
			})

			this.simAlpha *= 0.98
		},

		render() {
			const ctx = this.ctx
			if (!ctx) return
			const w = this.canvasW
			const h = this.canvasH
			const ox = this.offsetX
			const oy = this.offsetY
			const sc = this.scale
			const nodeMap = {}
			this.simNodes.forEach(n => { nodeMap[n.id] = n })
			const selectedId = this.selectedNode ? this.selectedNode.id : null
			const relatedNodeIds = new Set()
			const relatedEdgeIds = new Set()
			if (selectedId) {
				relatedNodeIds.add(selectedId)
				this.simEdges.forEach(e => {
					if (e.source === selectedId || e.target === selectedId) {
						relatedEdgeIds.add(e.id)
						relatedNodeIds.add(e.source)
						relatedNodeIds.add(e.target)
					}
				})
			}

			if (this.canvas) {
				ctx.clearRect(0, 0, w, h)
				const bgGradient = ctx.createLinearGradient(0, 0, 0, h)
				bgGradient.addColorStop(0, '#0f0609')
				bgGradient.addColorStop(0.45, '#1b0b11')
				bgGradient.addColorStop(1, '#090305')
				ctx.fillStyle = bgGradient
				ctx.fillRect(0, 0, w, h)
				const glow = ctx.createRadialGradient(w * 0.74, h * 0.18, 0, w * 0.74, h * 0.18, Math.max(w, h) * 0.45)
				glow.addColorStop(0, 'rgba(242, 201, 76, 0.14)')
				glow.addColorStop(1, 'rgba(242, 201, 76, 0)')
				ctx.fillStyle = glow
				ctx.fillRect(0, 0, w, h)
			} else {
				ctx.setFillStyle('#10070b')
				ctx.fillRect(0, 0, w, h)
			}

			this.simEdges.forEach(e => {
				const a = nodeMap[e.source]
				const b = nodeMap[e.target]
				if (!a || !b) return
				const x1 = a.x * sc + ox
				const y1 = a.y * sc + oy
				const x2 = b.x * sc + ox
				const y2 = b.y * sc + oy
				const isRelated = !selectedId || relatedEdgeIds.has(e.id)
				const edgeOpacity = selectedId ? (isRelated ? 0.72 : 0.12) : 0.3

				ctx.beginPath()
				ctx.moveTo(x1, y1)
				ctx.lineTo(x2, y2)
				if (this.canvas) {
					ctx.strokeStyle = isRelated ? `rgba(242, 201, 76, ${edgeOpacity})` : `rgba(238, 228, 229, ${edgeOpacity})`
					ctx.lineWidth = isRelated ? 1.6 : 1
					if (isRelated) {
						ctx.shadowBlur = 10
						ctx.shadowColor = 'rgba(242, 201, 76, 0.26)'
					}
				} else {
					ctx.setStrokeStyle(isRelated ? `rgba(242, 201, 76, ${edgeOpacity})` : `rgba(238, 228, 229, ${edgeOpacity})`)
					ctx.setLineWidth(isRelated ? 1.6 : 1)
				}
				ctx.stroke()
				if (this.canvas) {
					ctx.shadowBlur = 0
				}

				if (e.label && isRelated) {
					const mx = (x1 + x2) / 2
					const my = (y1 + y2) / 2
					if (this.canvas) {
						ctx.font = `${9 * sc}px sans-serif`
						ctx.fillStyle = 'rgba(255,240,228,0.64)'
						ctx.textAlign = 'center'
						ctx.textBaseline = 'middle'
					} else {
						ctx.setFontSize(9 * sc)
						ctx.setFillStyle('rgba(255,240,228,0.64)')
						ctx.setTextAlign('center')
						ctx.setTextBaseline('middle')
					}
					ctx.fillText(e.label, mx, my - 8 * sc)
				}
			})

			this.simNodes.forEach(n => {
				const x = n.x * sc + ox
				const y = n.y * sc + oy
				const r = n.radius * sc
				const color = TYPE_COLORS[n.nodeType] || '#999'
				const isSelected = selectedId === n.id
				const isRelated = !selectedId || relatedNodeIds.has(n.id)
				const nodeOpacity = selectedId ? (isRelated ? 1 : 0.28) : 1

				if (this.canvas) {
					ctx.save()
					ctx.globalAlpha = nodeOpacity
				}

				if (this.canvas) {
					ctx.beginPath()
					ctx.arc(x, y, r + (isSelected ? 16 * sc : 10 * sc), 0, Math.PI * 2)
					const outerGlow = ctx.createRadialGradient(x, y, r * 0.2, x, y, r + 18 * sc)
					outerGlow.addColorStop(0, isSelected ? 'rgba(255, 248, 231, 0.56)' : 'rgba(255,255,255,0.18)')
					outerGlow.addColorStop(1, 'rgba(255,255,255,0)')
					ctx.fillStyle = outerGlow
					ctx.fill()
				}

				ctx.beginPath()
				ctx.arc(x, y, r, 0, Math.PI * 2)
				if (this.canvas) {
					const nodeGradient = ctx.createLinearGradient(x - r, y - r, x + r, y + r)
					nodeGradient.addColorStop(0, '#fff2da')
					nodeGradient.addColorStop(0.22, color)
					nodeGradient.addColorStop(1, '#2f0c17')
					ctx.fillStyle = nodeGradient
					ctx.shadowBlur = isSelected ? 18 : 10
					ctx.shadowColor = color
				} else {
					ctx.setFillStyle(color)
				}
				ctx.fill()

				ctx.beginPath()
				ctx.arc(x, y, r, 0, Math.PI * 2)
				if (this.canvas) {
					ctx.strokeStyle = isSelected ? '#fff8e6' : 'rgba(255,255,255,0.84)'
					ctx.lineWidth = isSelected ? 2.8 * sc : 1.8 * sc
					ctx.shadowBlur = 0
				} else {
					ctx.setStrokeStyle('#fff')
					ctx.setLineWidth(2 * sc)
				}
				ctx.stroke()

				const fontSize = Math.max(10, Math.min(13, r * 0.7)) * sc
				if (this.canvas) {
					ctx.font = `bold ${fontSize}px sans-serif`
					ctx.fillStyle = '#fff9ef'
					ctx.textAlign = 'center'
					ctx.textBaseline = 'middle'
				} else {
					ctx.setFontSize(fontSize)
					ctx.setFillStyle('#fff')
					ctx.setTextAlign('center')
					ctx.setTextBaseline('middle')
				}

				let label = n.name
				if (label.length > 4) {
					label = label.substring(0, 3) + '..'
				}
				ctx.fillText(label, x, y)

				if (n.name.length > 4) {
					const subSize = 9 * sc
					if (this.canvas) {
						ctx.font = `${subSize}px sans-serif`
						ctx.fillStyle = isRelated ? 'rgba(255,240,228,0.78)' : 'rgba(255,240,228,0.34)'
					} else {
						ctx.setFontSize(subSize)
						ctx.setFillStyle('#d7c1c3')
					}
					ctx.fillText(n.name, x, y + r + 14 * sc)
				}

				if (this.canvas) {
					ctx.restore()
				}
			})

			if (!this.canvas) {
				ctx.draw()
			}
		},

		// ===== 触摸交互 =====

		openNodeActions(node) {
			if (!node) return
			this.selectedNode = node
			this.render()
			const items = []
			if (this.hasNodeDetailPage(node)) {
				items.push(`查看${this.getTypeLabel(node.nodeType)}详情`)
			}
			items.push('以此为中心展开')
			uni.showActionSheet({
				itemList: items,
				success: ({ tapIndex }) => {
					const detailIndex = this.hasNodeDetailPage(node) ? 0 : -1
					const focusIndex = this.hasNodeDetailPage(node) ? 1 : 0
					if (tapIndex === detailIndex) {
						this.goToNodeDetail(node)
					} else if (tapIndex === focusIndex) {
						this.selectedNode = null
						this.nodeRecommendations = { plays: [], actors: [], others: [] }
						this.render()
						this.focusNode(node.id)
					}
				},
				fail: () => {
					this.selectedNode = null
					this.nodeRecommendations = { plays: [], actors: [], others: [] }
					this.render()
				}
			})
		},

		getCanvasLocalXY(touch) {
			if (this.canvasRect && touch.clientX != null && touch.clientY != null)
				return { x: touch.clientX - this.canvasRect.left, y: touch.clientY - this.canvasRect.top }
			return { x: touch.x, y: touch.y }
		},

		onTouchStart(e) {
			const touch = e.touches[0]
			const local = this.getCanvasLocalXY(touch)
			this.touchStartX = local.x
			this.touchStartY = local.y
			this.lastPanX = local.x
			this.lastPanY = local.y
			this.pressCandidateNode = this.hitTestNode(local.x, local.y)
			this.pressStartedOnNode = !!this.pressCandidateNode
			this.draggingNode = null
			this.isPanning = !this.pressStartedOnNode
		},

		onTouchMove(e) {
			const touch = e.touches[0]
			const local = this.getCanvasLocalXY(touch)
			const moveX = local.x - this.touchStartX
			const moveY = local.y - this.touchStartY
			const movedFar = Math.abs(moveX) > 10 || Math.abs(moveY) > 10

			if (this.pressStartedOnNode) {
				// 手机上优先保证“轻点节点能打开详情”，节点不再直接拖拽
				return
			}

			if (movedFar) {
				this.isPanning = true
			}

			if (this.isPanning) {
				const dx = local.x - this.lastPanX
				const dy = local.y - this.lastPanY
				this.offsetX += dx
				this.offsetY += dy
				this.lastPanX = local.x
				this.lastPanY = local.y
				this.render()
			}
		},

		onTouchEnd(e) {
			const touch = e.changedTouches[0]
			const local = this.getCanvasLocalXY(touch)
			const dx = Math.abs(local.x - this.touchStartX)
			const dy = Math.abs(local.y - this.touchStartY)
			const tappedNode = this.pressCandidateNode && dx < 16 && dy < 16 ? this.pressCandidateNode : null

			if (tappedNode) {
				this.openNodeActions(tappedNode)
			} else if (dx < 10 && dy < 10 && this.selectedNode) {
				this.selectedNode = null
				this.render()
			}

			this.pressCandidateNode = null
			this.pressStartedOnNode = false
			this.draggingNode = null
			this.isPanning = false
		},

		hitTestNode(x, y) {
			const ox = this.offsetX
			const oy = this.offsetY
			const sc = this.scale
			let bestNode = null
			let bestDistance = Number.POSITIVE_INFINITY
			for (let i = this.simNodes.length - 1; i >= 0; i--) {
				const n = this.simNodes[i]
				const nx = n.x * sc + ox
				const ny = n.y * sc + oy
				const visualRadius = Math.max(n.radius * sc, 18)
				const hitRadius = Math.max(visualRadius + 18, 28)
				const dx = x - nx
				const dy = y - ny
				const distance = Math.sqrt(dx * dx + dy * dy)
				if (distance <= hitRadius && distance < bestDistance) {
					bestNode = n
					bestDistance = distance
				}
			}
			return bestNode
		},

		// ===== 功能操作 =====

		toggleType(key) {
			const idx = this.activeTypes.indexOf(key)
			if (idx >= 0) {
				if (this.activeTypes.length <= 1) return
				this.activeTypes.splice(idx, 1)
			} else {
				this.activeTypes.push(key)
			}
			this.initSimulation()
		},

		async onSearch() {
			if (!this.searchKey.trim()) {
				this.focusNodeId = null
				await this.loadData()
				return
			}
			this.loading = true
			try {
				const res = await searchNodes(this.searchKey.trim())
				const results = res.data || res
				if (results && results.length > 0) {
					this.focusNodeId = results[0].id
					await this.loadData()
				} else {
					uni.showToast({ title: '未找到相关节点', icon: 'none' })
				}
			} catch (e) {
				// 本地搜索 fallback
				const found = this.simNodes.find(n =>
					n.name.includes(this.searchKey.trim())
				)
				if (found) {
					this.selectedNode = found
					this.offsetX = this.canvasW / 2 - found.x * this.scale
					this.offsetY = this.canvasH / 2 - found.y * this.scale
					this.render()
				} else {
					uni.showToast({ title: '未找到相关节点', icon: 'none' })
				}
			}
			this.loading = false
		},

		async focusNode(nodeId) {
			this.selectedNode = null
			this.focusNodeId = nodeId
			this.offsetX = this.canvasW / 2
			this.offsetY = this.canvasH / 2
			this.scale = this.focusNodeId ? 1.15 : 1
			await this.loadData()
		},

		getColor(type) {
			return TYPE_COLORS[type] || '#999'
		},

		getTypeLabel(type) {
			return TYPE_LABELS[type] || type
		}
	}
}
</script>

<style scoped>
.page {
	position: relative;
	width: 100vw;
	height: 100vh;
	background:
		radial-gradient(circle at 78% 16%, rgba(242, 201, 76, 0.16), transparent 26%),
		radial-gradient(circle at 8% 10%, rgba(186, 61, 78, 0.18), transparent 28%),
		linear-gradient(180deg, #0f0609 0%, #17080d 34%, #12060a 100%);
	overflow: hidden;
	display: flex;
	flex-direction: column;
}

.toolbar-shell {
	padding: 18rpx 18rpx 10rpx;
	position: relative;
	z-index: 10;
}
.toolbar {
	padding: 20rpx 20rpx 18rpx;
	background: rgba(31, 11, 18, 0.78);
	border: 1rpx solid rgba(255, 244, 229, 0.1);
	box-shadow: 0 18rpx 36rpx rgba(0,0,0,0.28);
	border-radius: 26rpx;
	backdrop-filter: blur(18rpx);
}
.toolbar-top {
	display: flex;
	justify-content: space-between;
	align-items: flex-start;
	gap: 18rpx;
	margin-bottom: 18rpx;
}
.toolbar-title-wrap { flex: 1; }
.toolbar-kicker {
	display: block;
	font-size: 18rpx;
	letter-spacing: 3rpx;
	color: rgba(242, 201, 76, 0.84);
	margin-bottom: 8rpx;
}
.toolbar-title {
	display: block;
	font-size: 34rpx;
	font-weight: 700;
	color: #fff4e8;
}
.toolbar-badge {
	padding: 10rpx 18rpx;
	border-radius: 999rpx;
	background: rgba(255, 255, 255, 0.06);
	border: 1rpx solid rgba(255, 255, 255, 0.08);
	font-size: 22rpx;
	color: rgba(255, 240, 228, 0.82);
	white-space: nowrap;
}
.filter-row {
	display: flex;
	gap: 12rpx;
	flex-wrap: wrap;
	margin-bottom: 14rpx;
}
.filter-btn {
	font-size: 22rpx;
	padding: 8rpx 18rpx;
	border-radius: 999rpx;
	border: 2rpx solid rgba(255,255,255,0.12);
	transition: all 0.2s;
}
.filter-btn.active {
	color: #fff7eb;
	box-shadow: 0 8rpx 18rpx rgba(0,0,0,0.16);
}
.search-row {
	display: flex;
	gap: 12rpx;
	align-items: center;
}
.search-input {
	flex: 1;
	height: 60rpx;
	background: rgba(255,255,255,0.06);
	border: 1rpx solid rgba(255,255,255,0.08);
	border-radius: 30rpx;
	padding: 0 24rpx;
	font-size: 26rpx;
	color: #fff3ea;
}
.search-btn {
	min-width: 110rpx;
	height: 60rpx;
	line-height: 60rpx;
	text-align: center;
	background: linear-gradient(135deg, #f2c94c, #c17f39);
	color: #43111a;
	border-radius: 30rpx;
	font-size: 26rpx;
	font-weight: 700;
	box-shadow: 0 10rpx 22rpx rgba(193,127,57,0.24);
}

.tap-guide {
	position: relative;
	z-index: 8;
	margin: 0 18rpx 8rpx;
	padding: 12rpx 18rpx;
	border-radius: 16rpx;
	background: rgba(242, 201, 76, 0.14);
	border: 1rpx solid rgba(242, 201, 76, 0.22);
	font-size: 22rpx;
	color: #fbe7b4;
}

.graph-canvas {
	flex: 1;
	background: transparent;
}

 .legend-shell {
	position: absolute;
	left: 18rpx;
	bottom: 24rpx;
	z-index: 5;
}
.tap-hint {
	margin-bottom: 12rpx;
	padding: 12rpx 16rpx;
	background: rgba(19, 7, 11, 0.78);
	border: 1rpx solid rgba(255,255,255,0.08);
	border-radius: 14rpx;
	font-size: 22rpx;
	color: rgba(255,240,228,0.82);
	backdrop-filter: blur(12rpx);
}
.legend {
	display: flex;
	gap: 16rpx;
	flex-wrap: wrap;
	max-width: 86vw;
	background: rgba(19, 7, 11, 0.72);
	padding: 12rpx 18rpx;
	border-radius: 18rpx;
	border: 1rpx solid rgba(255,255,255,0.08);
	backdrop-filter: blur(14rpx);
}
.legend-item {
	display: flex;
	align-items: center;
	gap: 8rpx;
}
.legend-dot {
	width: 16rpx;
	height: 16rpx;
	border-radius: 50%;
}
.legend-text {
	font-size: 20rpx;
	color: rgba(255,240,228,0.8);
}

.detail-mask {
	position: fixed;
	top: 0; left: 0; right: 0; bottom: 0;
	background: rgba(0,0,0,0.4);
	display: flex;
	align-items: flex-end;
	justify-content: center;
	z-index: 100;
	backdrop-filter: blur(4rpx);
}
.detail-card {
	width: 92%;
	background: linear-gradient(180deg, rgba(255,248,241,0.98), rgba(255,238,224,0.96));
	border-radius: 30rpx 30rpx 0 0;
	padding: 40rpx 36rpx 60rpx;
	box-shadow: 0 -12rpx 40rpx rgba(0,0,0,0.18);
}
.detail-header {
	display: flex;
	align-items: center;
	gap: 16rpx;
	margin-bottom: 20rpx;
}
.detail-dot {
	width: 24rpx;
	height: 24rpx;
	border-radius: 50%;
	box-shadow: 0 0 14rpx currentColor;
}
.detail-name {
	font-size: 36rpx;
	font-weight: 700;
	color: #45131a;
}
.detail-type {
	font-size: 24rpx;
	color: #9a6d50;
	margin-left: auto;
}
.detail-desc {
	font-size: 28rpx;
	color: #6d4d4f;
	line-height: 1.7;
	display: block;
	margin-bottom: 16rpx;
}
.detail-links { margin-bottom: 24rpx; }
.detail-link-count {
	font-size: 26rpx;
	color: #8f2433;
	font-weight: 600;
}
.detail-actions {
	display: flex;
	gap: 20rpx;
}
.detail-btn {
	flex: 1;
	text-align: center;
	padding: 20rpx;
	border-radius: 18rpx;
	font-size: 28rpx;
	background: linear-gradient(135deg, #6e1826, #9f3044);
	color: #fff7eb;
	box-shadow: 0 12rpx 26rpx rgba(110,24,38,0.18);
}
.detail-btn.primary {
	background: linear-gradient(135deg, #4d1220, #7f2231);
}
.detail-btn.secondary {
	background: rgba(127, 34, 49, 0.08);
	color: #7f2231;
	box-shadow: none;
	border: 1rpx solid rgba(127,34,49,0.14);
}
.detail-rec {
	background: rgba(255,255,255,0.5);
	border-radius: 20rpx;
	padding: 20rpx;
	margin-bottom: 22rpx;
}
.detail-rec-title {
	display: block;
	font-size: 26rpx;
	font-weight: 700;
	color: #4d1220;
	margin-bottom: 12rpx;
}
.detail-rec-list {
	display: flex;
	flex-direction: column;
	gap: 12rpx;
	max-height: 260rpx;
	overflow-y: auto;
}
.detail-rec-item {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 16rpx 18rpx;
	border-radius: 16rpx;
	background: rgba(255,248,241,0.8);
}
.detail-rec-text { flex: 1; min-width: 0; }
.detail-rec-name {
	display: block;
	font-size: 26rpx;
	color: #4b1620;
	font-weight: 600;
	margin-bottom: 6rpx;
}
.detail-rec-reason {
	display: block;
	font-size: 22rpx;
	color: #8c6a64;
	line-height: 1.5;
}
.detail-rec-go {
	font-size: 30rpx;
	color: #9f3044;
	margin-left: 14rpx;
	flex-shrink: 0;
}

.loading-mask {
	position: absolute;
	top: 0; left: 0; right: 0; bottom: 0;
	background: rgba(10, 3, 5, 0.52);
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 20rpx;
	z-index: 50;
}
.loading-ring {
	width: 68rpx;
	height: 68rpx;
	border-radius: 50%;
	border: 6rpx solid rgba(255,255,255,0.14);
	border-top-color: #f2c94c;
	animation: spin 0.9s linear infinite;
}
.loading-text {
	font-size: 30rpx;
	color: rgba(255,240,228,0.9);
}
@keyframes spin {
	from { transform: rotate(0deg); }
	to { transform: rotate(360deg); }
}
</style>
