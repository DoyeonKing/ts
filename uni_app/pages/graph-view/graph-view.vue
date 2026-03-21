<template>
	<view class="page">
		<!-- 顶部工具栏 -->
		<view class="toolbar">
			<view class="filter-row">
				<view
					v-for="t in typeFilters" :key="t.key"
					class="filter-btn"
					:class="{ active: activeTypes.includes(t.key) }"
					:style="{ borderColor: t.color, color: activeTypes.includes(t.key) ? '#fff' : t.color, background: activeTypes.includes(t.key) ? t.color : 'transparent' }"
					@click="toggleType(t.key)"
				>{{ t.label }}</view>
			</view>
			<view class="search-row">
				<input class="search-input" v-model="searchKey" placeholder="搜索节点..." @confirm="onSearch" />
				<view class="search-btn" @click="onSearch">搜</view>
			</view>
		</view>

		<!-- Canvas 图谱 -->
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
		<view class="legend">
			<view class="legend-item" v-for="t in typeFilters" :key="t.key">
				<view class="legend-dot" :style="{ background: t.color }"></view>
				<text class="legend-text">{{ t.label }}</text>
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
			<text class="loading-text">加载图谱中...</text>
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

	onUnload() {
		this.simRunning = false
	},

	watch: {
		selectedNode(n) {
			if (n && n.id) this.loadNodeRecommendations(n.id)
			else this.nodeRecommendations = { plays: [], actors: [], others: [] }
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
			try {
				const res = await getRecommendationsForNode(nodeId)
				const data = res.data || res
				this.nodeRecommendations = {
					plays: data.plays || [],
					actors: data.actors || [],
					others: data.others || []
				}
			} catch (e) {
				this.nodeRecommendations = { plays: [], actors: [], others: [] }
			}
		},
		/** 进入当前选中节点的详情页（剧目/演员/术语等） */
		goToNodeDetail(node) {
			if (!node) return
			this.selectedNode = null
			if (node.nodeType === 'PLAY') {
				uni.navigateTo({ url: `/pages/play-detail/play-detail?id=${node.id}` }).catch(() => {})
			} else if (node.nodeType === 'ACTOR') {
				uni.navigateTo({ url: `/pages/actor-detail/actor-detail?id=${node.id}` }).catch(() => {})
			} else if (node.nodeType === 'TERMINOLOGY') {
				uni.navigateTo({ url: `/pages/terminology/terminology` }).catch(() => {})
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
			if (r.nodeType === 'PLAY') {
				uni.navigateTo({ url: `/pages/play-detail/play-detail?id=${r.id}` }).catch(() => {})
			} else if (r.nodeType === 'ACTOR') {
				uni.navigateTo({ url: `/pages/actor-detail/actor-detail?id=${r.id}` }).catch(() => {})
			} else if (r.nodeType === 'TERMINOLOGY') {
				uni.navigateTo({ url: `/pages/terminology/terminology` }).catch(() => {})
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

			if (this.canvas) {
				// 新版 canvas 2d
				ctx.clearRect(0, 0, w, h)
			} else {
				ctx.setFillStyle('#f5f5f5')
				ctx.fillRect(0, 0, w, h)
			}

			// 绘制边
			this.simEdges.forEach(e => {
				const a = nodeMap[e.source]
				const b = nodeMap[e.target]
				if (!a || !b) return
				const x1 = a.x * sc + ox
				const y1 = a.y * sc + oy
				const x2 = b.x * sc + ox
				const y2 = b.y * sc + oy

				ctx.beginPath()
				ctx.moveTo(x1, y1)
				ctx.lineTo(x2, y2)
				if (this.canvas) {
					ctx.strokeStyle = 'rgba(180,180,200,0.5)'
					ctx.lineWidth = 1
				} else {
					ctx.setStrokeStyle('rgba(180,180,200,0.5)')
					ctx.setLineWidth(1)
				}
				ctx.stroke()

				// 边上的标签
				if (e.label) {
					const mx = (x1 + x2) / 2
					const my = (y1 + y2) / 2
					if (this.canvas) {
						ctx.font = `${9 * sc}px sans-serif`
						ctx.fillStyle = '#999'
						ctx.textAlign = 'center'
						ctx.textBaseline = 'middle'
					} else {
						ctx.setFontSize(9 * sc)
						ctx.setFillStyle('#999')
						ctx.setTextAlign('center')
						ctx.setTextBaseline('middle')
					}
					ctx.fillText(e.label, mx, my - 6 * sc)
				}
			})

			// 绘制节点
			this.simNodes.forEach(n => {
				const x = n.x * sc + ox
				const y = n.y * sc + oy
				const r = n.radius * sc
				const color = TYPE_COLORS[n.nodeType] || '#999'

				// 选中高亮
				const isSelected = this.selectedNode && this.selectedNode.id === n.id

				// 外发光
				if (isSelected) {
					ctx.beginPath()
					ctx.arc(x, y, r + 4 * sc, 0, Math.PI * 2)
					if (this.canvas) {
						ctx.fillStyle = color + '40'
					} else {
						ctx.setFillStyle(color + '40')
					}
					ctx.fill()
				}

				// 圆形节点
				ctx.beginPath()
				ctx.arc(x, y, r, 0, Math.PI * 2)
				if (this.canvas) {
					ctx.fillStyle = color
				} else {
					ctx.setFillStyle(color)
				}
				ctx.fill()

				// 白色边框
				ctx.beginPath()
				ctx.arc(x, y, r, 0, Math.PI * 2)
				if (this.canvas) {
					ctx.strokeStyle = '#fff'
					ctx.lineWidth = 2 * sc
				} else {
					ctx.setStrokeStyle('#fff')
					ctx.setLineWidth(2 * sc)
				}
				ctx.stroke()

				// 节点文字
				const fontSize = Math.max(10, Math.min(13, r * 0.7)) * sc
				if (this.canvas) {
					ctx.font = `bold ${fontSize}px sans-serif`
					ctx.fillStyle = '#fff'
					ctx.textAlign = 'center'
					ctx.textBaseline = 'middle'
				} else {
					ctx.setFontSize(fontSize)
					ctx.setFillStyle('#fff')
					ctx.setTextAlign('center')
					ctx.setTextBaseline('middle')
				}

				// 长文本截断
				let label = n.name
				if (label.length > 4) {
					label = label.substring(0, 3) + '..'
				}
				ctx.fillText(label, x, y)

				// 节点下方完整名称
				if (n.name.length > 4) {
					const subSize = 9 * sc
					if (this.canvas) {
						ctx.font = `${subSize}px sans-serif`
						ctx.fillStyle = '#666'
					} else {
						ctx.setFontSize(subSize)
						ctx.setFillStyle('#666')
					}
					ctx.fillText(n.name, x, y + r + 12 * sc)
				}
			})

			// 旧版 canvas 需要 draw
			if (!this.canvas) {
				ctx.draw()
			}
		},

		// ===== 触摸交互 =====

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

			// 检测是否点中了节点
			const hit = this.hitTestNode(local.x, local.y)
			if (hit) {
				this.draggingNode = hit
			} else {
				this.draggingNode = null
				this.isPanning = true
			}
		},

		onTouchMove(e) {
			const touch = e.touches[0]
			const local = this.getCanvasLocalXY(touch)
			if (this.draggingNode) {
				this.draggingNode.x = (local.x - this.offsetX) / this.scale
				this.draggingNode.y = (local.y - this.offsetY) / this.scale
				this.draggingNode.vx = 0
				this.draggingNode.vy = 0
				this.render()
			} else if (this.isPanning) {
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

			// 点击（非拖拽）：放宽阈值，避免轻微滑动被当成拖拽
			if (dx < 12 && dy < 12) {
				const hit = this.hitTestNode(local.x, local.y)
				if (hit) {
					this.selectedNode = hit
					this.render()
				} else {
					if (this.selectedNode) {
						this.selectedNode = null
						this.render()
					}
				}
			}

			this.draggingNode = null
			this.isPanning = false
		},

		hitTestNode(x, y) {
			const ox = this.offsetX
			const oy = this.offsetY
			const sc = this.scale
			// 扩大可点击区域，方便小圆和手指点击
			const hitPadding = 24
			for (let i = this.simNodes.length - 1; i >= 0; i--) {
				const n = this.simNodes[i]
				const nx = n.x * sc + ox
				const ny = n.y * sc + oy
				const r = n.radius * sc + hitPadding
				const dx = x - nx
				const dy = y - ny
				if (dx * dx + dy * dy <= r * r) {
					return n
				}
			}
			return null
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
	background: #f5f5f5;
	overflow: hidden;
	display: flex;
	flex-direction: column;
}

.toolbar {
	padding: 16rpx 20rpx 12rpx;
	background: #fff;
	box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06);
	z-index: 10;
}
.filter-row {
	display: flex;
	gap: 12rpx;
	flex-wrap: wrap;
	margin-bottom: 12rpx;
}
.filter-btn {
	font-size: 22rpx;
	padding: 6rpx 18rpx;
	border-radius: 20rpx;
	border: 2rpx solid #ccc;
	transition: all 0.2s;
}
.filter-btn.active {
	color: #fff;
}
.search-row {
	display: flex;
	gap: 12rpx;
	align-items: center;
}
.search-input {
	flex: 1;
	height: 56rpx;
	background: #f5f5f5;
	border-radius: 28rpx;
	padding: 0 24rpx;
	font-size: 26rpx;
}
.search-btn {
	width: 72rpx;
	height: 56rpx;
	line-height: 56rpx;
	text-align: center;
	background: #6366f1;
	color: #fff;
	border-radius: 28rpx;
	font-size: 26rpx;
}

.graph-canvas {
	flex: 1;
	background: #f5f5f5;
}

.legend {
	position: absolute;
	bottom: 24rpx;
	left: 20rpx;
	display: flex;
	gap: 16rpx;
	background: rgba(255,255,255,0.92);
	padding: 10rpx 20rpx;
	border-radius: 16rpx;
	box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.1);
	z-index: 5;
}
.legend-item {
	display: flex;
	align-items: center;
	gap: 6rpx;
}
.legend-dot {
	width: 16rpx;
	height: 16rpx;
	border-radius: 50%;
}
.legend-text {
	font-size: 20rpx;
	color: #666;
}

/* 详情弹窗 */
.detail-mask {
	position: fixed;
	top: 0; left: 0; right: 0; bottom: 0;
	background: rgba(0,0,0,0.35);
	display: flex;
	align-items: flex-end;
	justify-content: center;
	z-index: 100;
}
.detail-card {
	width: 92%;
	background: #fff;
	border-radius: 28rpx 28rpx 0 0;
	padding: 40rpx 36rpx 60rpx;
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
}
.detail-name {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
}
.detail-type {
	font-size: 24rpx;
	color: #999;
	margin-left: auto;
}
.detail-desc {
	font-size: 28rpx;
	color: #666;
	line-height: 1.6;
	display: block;
	margin-bottom: 16rpx;
}
.detail-links {
	margin-bottom: 24rpx;
}
.detail-link-count {
	font-size: 26rpx;
	color: #6366f1;
}
.detail-actions {
	display: flex;
	gap: 20rpx;
}
.detail-btn {
	flex: 1;
	text-align: center;
	padding: 20rpx;
	border-radius: 16rpx;
	font-size: 28rpx;
	background: #6366f1;
	color: #fff;
}
.detail-btn.primary {
	background: #6366f1;
	color: #fff;
}
.detail-btn.secondary {
	background: #f0f0f0;
	color: #666;
}
.detail-rec { margin: 20rpx 0; padding-top: 20rpx; border-top: 1rpx solid #eee; }
.detail-rec-title { font-size: 26rpx; color: #6366f1; display: block; margin-bottom: 12rpx; }
.detail-rec-list { display: flex; flex-direction: column; gap: 10rpx; max-height: 260rpx; overflow-y: auto; }
.detail-rec-item { display: flex; align-items: center; padding: 16rpx; background: #f8f8f8; border-radius: 12rpx; }
.detail-rec-item:active { background: #eee; }
.detail-rec-text { flex: 1; min-width: 0; }
.detail-rec-name { font-size: 26rpx; font-weight: 500; color: #333; display: block; }
.detail-rec-reason { font-size: 22rpx; color: #888; display: block; margin-top: 4rpx; }
.detail-rec-go { font-size: 32rpx; color: #999; margin-left: 12rpx; flex-shrink: 0; }

/* 加载遮罩 */
.loading-mask {
	position: absolute;
	top: 0; left: 0; right: 0; bottom: 0;
	background: rgba(255,255,255,0.8);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 50;
}
.loading-text {
	font-size: 30rpx;
	color: #666;
}
</style>
