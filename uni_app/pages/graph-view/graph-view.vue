<template>
	<view class="page">
		<view class="toolbar-shell">
			<view class="toolbar">
				<view class="toolbar-top">
					<view class="toolbar-title-wrap">
						<text class="toolbar-kicker">STAGE MAP</text>
						<text class="toolbar-title">知识图谱全景</text>
					</view>
					<view class="toolbar-badge">3D旋转 · 点击聚焦</view>
				</view>
				<view class="filter-row">
					<view
					v-for="t in typeFilters"
					:key="t.key"
					class="filter-btn"
					:class="{ active: activeTypes.includes(t.key) }"
					:style="{ '--pill-color': t.color }"
					@tap="toggleType(t.key)"
				>{{ t.label }}</view>
				</view>
				<view class="search-row">
					<input class="search-input" v-model="searchKey" placeholder="搜索节点、人物、术语..." placeholder-style="color: rgba(255,240,228,.35);" @confirm="onSearch" />
					<view class="search-btn" @tap="onSearch">搜索</view>
				</view>
			</view>
		</view>
		<view class="tap-guide">单指拖动画布可旋转视角，轻点节点聚焦并打开详情</view>
		<canvas canvas-id="graphCanvas" id="graphCanvas" class="graph-canvas" :style="{ width: canvasW + 'px', height: canvasH + 'px' }" @touchstart="onTouchStart" @touchmove="onTouchMove" @touchend="onTouchEnd"></canvas>
		<view class="legend-shell">
			<view class="tap-hint">点击节点查看详情，轻点空白恢复旋转</view>
			<view class="legend">
				<view class="legend-item" v-for="t in typeFilters" :key="t.key">
					<view class="legend-dot" :style="{ background: t.color }"></view>
					<text class="legend-text">{{ t.label }}</text>
				</view>
			</view>
		</view>
		<cover-view class="detail-mask" v-if="selectedNode" @tap="clearSelection">
			<cover-view class="detail-card" @tap.stop>
				<cover-view class="detail-head">
					<cover-view class="detail-dot" :style="{ background: getColor(selectedNode.nodeType) }"></cover-view>
					<cover-view class="detail-name">{{ selectedNode.name }}</cover-view>
					<cover-view class="detail-type">{{ getTypeLabel(selectedNode.nodeType) }}</cover-view>
				</cover-view>
				<cover-view class="detail-desc">{{ selectedNode.description || '暂无简介' }}</cover-view>
				<cover-view class="detail-links">关联 {{ selectedNode.linkCount || 0 }} 个节点</cover-view>
				<cover-view class="detail-actions">
					<cover-view class="detail-btn primary" @tap="goDetail(selectedNode)">进入详情</cover-view>
					<cover-view class="detail-btn" @tap="recenter(selectedNode.id)">以此为中心</cover-view>
					<cover-view class="detail-btn secondary" @tap="clearSelection">关闭</cover-view>
				</cover-view>
			</cover-view>
		</cover-view>
		<view class="mask" v-if="loading"><view class="ring"></view><text class="mask-text">图谱加载中...</text></view>
	</view>
</template>

<script>
import { getFullGraph, getNodeNeighborhood, searchNodes } from '../../api/knowledgeGraph.js'
const TYPE_COLORS = { PLAY:'#6366f1', ACTOR:'#f59e0b', TERMINOLOGY:'#10b981', TAG:'#8b5cf6', VENUE:'#ef4444' }
const TYPE_LABELS = { PLAY:'剧目', ACTOR:'演员', TERMINOLOGY:'术语', TAG:'标签', VENUE:'场馆' }
const MAX_NODES = 54
const touchPoint = t => ({ x: t.clientX != null ? t.clientX : (t.pageX != null ? t.pageX : (t.x || 0)), y: t.clientY != null ? t.clientY : (t.pageY != null ? t.pageY : (t.y || 0)) })

export default {
	data() {
		return {
			canvasW: 375,
			canvasH: 600,
			ctx: null,
			loading: false,
			searchKey: '',
			focusNodeId: null,
			nodes: [],
			edges: [],
			renderNodes: [],
			selectedNode: null,
			activeTypes: ['PLAY', 'ACTOR', 'TERMINOLOGY', 'TAG', 'VENUE'],
			typeFilters: [
				{ key: 'PLAY', label: '剧目', color: TYPE_COLORS.PLAY },
				{ key: 'ACTOR', label: '演员', color: TYPE_COLORS.ACTOR },
				{ key: 'TERMINOLOGY', label: '术语', color: TYPE_COLORS.TERMINOLOGY },
				{ key: 'TAG', label: '标签', color: TYPE_COLORS.TAG },
				{ key: 'VENUE', label: '场馆', color: TYPE_COLORS.VENUE }
			],
			rotY: 0,
			rotX: -0.16,
			touchStartX: 0,
			touchStartY: 0,
			lastX: 0,
			lastY: 0,
			isPanning: false,
			canvasRect: null,
			timer: null,
			autoSpeed: 0.012
		}
	},
	onLoad(o) {
		const sys = uni.getSystemInfoSync()
		this.canvasW = sys.windowWidth
		this.canvasH = sys.windowHeight - 168
		if (o && o.nodeId) this.focusNodeId = parseInt(o.nodeId)
	},
	onReady() {
		this.ctx = uni.createCanvasContext('graphCanvas', this)
		this.updateCanvasRect()
		this.loadData()
		this.startRotate()
	},
	onUnload() {
		if (this.timer) clearInterval(this.timer)
	},
	methods: {
		updateCanvasRect() {
			uni.createSelectorQuery().in(this).select('#graphCanvas').boundingClientRect(r => {
				if (r) this.canvasRect = { left: r.left, top: r.top }
			}).exec()
		},
		getColor(t) { return TYPE_COLORS[t] || '#999' },
		getTypeLabel(t) { return TYPE_LABELS[t] || t },
		async loadData() {
			this.loading = true
			try {
				const res = this.focusNodeId ? await getNodeNeighborhood(this.focusNodeId) : await getFullGraph()
				const data = res.data || res
				this.nodes = (data.nodes || []).filter(n => this.activeTypes.includes(n.nodeType)).slice(0, MAX_NODES)
				const ids = new Set(this.nodes.map(n => n.id))
				this.edges = (data.edges || []).filter(e => ids.has(e.source) && ids.has(e.target))
				this.layoutNodes()
			} catch (e) {
				this.nodes = []
				this.edges = []
				this.renderNodes = []
				this.drawGraph()
				uni.showToast({ title: '图谱加载失败', icon: 'none' })
			} finally {
				this.loading = false
			}
		},
		layoutNodes() {
			const step = Math.PI * (3 - Math.sqrt(5))
			const radius = Math.min(this.canvasW, this.canvasH) * 0.31
			this.renderNodes = this.nodes.map((n, i) => {
				const phi = Math.acos(1 - 2 * ((i + 0.5) / Math.max(this.nodes.length, 1)))
				const theta = step * i
				const base = n.nodeType === 'TAG' ? 15 : n.nodeType === 'TERMINOLOGY' ? 18 : 21
				return {
					...n,
					r: Math.min(base + Math.min(n.linkCount || 0, 8) * 1.9, 36),
					x: radius * Math.sin(phi) * Math.cos(theta),
					y: radius * Math.cos(phi) * 0.9,
					z: radius * Math.sin(phi) * Math.sin(theta)
				}
			})
			this.drawGraph()
		},
		startRotate() {
			if (this.timer) clearInterval(this.timer)
			this.timer = setInterval(() => {
				if (this.selectedNode) return
				this.rotY += this.autoSpeed
				this.drawGraph()
			}, 32)
		},
		projectNode(n) {
			const sy = Math.sin(this.rotY), cy = Math.cos(this.rotY)
			const sx = Math.sin(this.rotX), cx = Math.cos(this.rotX)
			const x1 = n.x * cy - n.z * sy
			const z1 = n.x * sy + n.z * cy
			const y1 = n.y * cx - z1 * sx
			const z2 = n.y * sx + z1 * cx
			const p = 360 / (360 - z2)
			return { ...n, rx: x1 * p, ry: y1 * p, rz: z2, rr: n.r * (0.8 + p * 0.25) }
		},
		isRelated(id) {
			if (!this.selectedNode) return true
			if (id === this.selectedNode.id) return true
			return this.edges.some(e => (e.source === this.selectedNode.id && e.target === id) || (e.target === this.selectedNode.id && e.source === id))
		},
		drawGraph() {
			const ctx = this.ctx
			if (!ctx) return
			const w = this.canvasW, h = this.canvasH, ox = w / 2, oy = h / 2
			const nodes = this.renderNodes.map(n => this.projectNode(n)).sort((a, b) => a.rz - b.rz)
			const selectedId = this.selectedNode && this.selectedNode.id
			ctx.clearRect(0, 0, w, h)
			ctx.setFillStyle('#10070b')
			ctx.fillRect(0, 0, w, h)
			this.edges.forEach(e => {
				const a = nodes.find(n => n.id === e.source)
				const b = nodes.find(n => n.id === e.target)
				if (!a || !b) return
				const hi = !selectedId || e.source === selectedId || e.target === selectedId
				ctx.beginPath()
				ctx.moveTo(a.rx + ox, a.ry + oy)
				ctx.lineTo(b.rx + ox, b.ry + oy)
				ctx.setStrokeStyle(hi ? 'rgba(242,201,76,.42)' : 'rgba(255,255,255,.1)')
				ctx.setLineWidth(hi ? 1.5 : 1)
				ctx.stroke()
			})
			nodes.forEach(n => {
				const x = n.rx + ox, y = n.ry + oy
				const sel = selectedId === n.id
				const faded = selectedId && !this.isRelated(n.id)
				ctx.beginPath()
				ctx.arc(x, y, n.rr + 7, 0, Math.PI * 2)
				ctx.setFillStyle(sel ? 'rgba(255,248,231,.2)' : 'rgba(255,255,255,.08)')
				ctx.fill()
				ctx.beginPath()
				ctx.arc(x, y, n.rr, 0, Math.PI * 2)
				ctx.setFillStyle(this.getColor(n.nodeType))
				ctx.fill()
				ctx.setLineWidth(sel ? 3 : 1.4)
				ctx.setStrokeStyle(sel ? 'rgba(255,248,231,.98)' : 'rgba(255,255,255,.86)')
				ctx.stroke()
				if (faded) {
					ctx.beginPath()
					ctx.arc(x, y, n.rr, 0, Math.PI * 2)
					ctx.setFillStyle('rgba(17,7,11,.46)')
					ctx.fill()
				}
				const name = String(n.name || '')
				ctx.setFillStyle('#fff9ef')
				ctx.setFontSize(sel ? 12 : 10)
				ctx.setTextAlign('center')
				ctx.setTextBaseline('middle')
				ctx.fillText(name.length > 4 ? name.slice(0, 3) + '..' : name, x, y)
				if (name.length > 4) {
					ctx.setFontSize(9)
					ctx.setFillStyle('rgba(255,240,228,.72)')
					ctx.fillText(name, x, y + n.rr + 11)
				}
			})
			ctx.draw()
		},
		localPoint(t) {
			const p = touchPoint(t)
			return this.canvasRect ? { x: p.x - this.canvasRect.left, y: p.y - this.canvasRect.top } : p
		},
		hitNode(x, y) {
			const ox = this.canvasW / 2, oy = this.canvasH / 2
			let best = null, bestDist = Number.POSITIVE_INFINITY
			this.renderNodes.map(n => this.projectNode(n)).sort((a, b) => b.rz - a.rz).forEach(n => {
				const d = Math.hypot(x - (n.rx + ox), y - (n.ry + oy))
				if (d <= n.rr + 16 && d < bestDist) {
					best = n
					bestDist = d
				}
			})
			return best
		},
		onTouchStart(e) {
			const p = this.localPoint(e.touches[0])
			this.touchStartX = p.x
			this.touchStartY = p.y
			this.lastX = p.x
			this.lastY = p.y
			this.isPanning = false
		},
		onTouchMove(e) {
			const p = this.localPoint(e.touches[0])
			const dx = p.x - this.touchStartX, dy = p.y - this.touchStartY
			if (Math.abs(dx) > 8 || Math.abs(dy) > 8) this.isPanning = true
			if (!this.isPanning) return
			this.rotY += (p.x - this.lastX) * 0.008
			this.rotX += (p.y - this.lastY) * 0.005
			this.rotX = Math.max(-0.55, Math.min(0.55, this.rotX))
			this.autoSpeed = Math.max(-0.018, Math.min(0.018, (p.x - this.lastX) * 0.0005))
			this.lastX = p.x
			this.lastY = p.y
			this.drawGraph()
		},
		onTouchEnd(e) {
			const p = this.localPoint((e.changedTouches && e.changedTouches[0]) || e.touches[0])
			const dx = Math.abs(p.x - this.touchStartX), dy = Math.abs(p.y - this.touchStartY)
			if (!this.isPanning && dx < 16 && dy < 16) {
				const n = this.hitNode(p.x, p.y)
				if (n) {
					this.selectedNode = this.renderNodes.find(i => i.id === n.id) || n
					this.autoSpeed = 0
					this.drawGraph()
				} else {
					this.clearSelection()
				}
			} else if (!this.selectedNode && Math.abs(this.autoSpeed) < 0.006) {
				this.autoSpeed = this.autoSpeed >= 0 ? 0.006 : -0.006
			}
			this.isPanning = false
		},
		clearSelection() {
			this.selectedNode = null
			this.autoSpeed = 0.012
			this.drawGraph()
		},
		async onSearch() {
			if (!this.searchKey.trim()) {
				this.focusNodeId = null
				await this.loadData()
				return
			}
			try {
				const res = await searchNodes(this.searchKey.trim())
				const arr = res.data || res
				if (arr && arr.length) {
					this.focusNodeId = arr[0].id
					await this.loadData()
					this.selectedNode = this.renderNodes.find(n => n.id === this.focusNodeId) || null
					this.autoSpeed = 0
					this.drawGraph()
				} else {
					uni.showToast({ title: '未找到相关节点', icon: 'none' })
				}
			} catch (e) {
				uni.showToast({ title: '搜索失败', icon: 'none' })
			}
		},
		toggleType(k) {
			const idx = this.activeTypes.indexOf(k)
			if (idx >= 0) {
				if (this.activeTypes.length <= 1) return
				this.activeTypes.splice(idx, 1)
			} else {
				this.activeTypes.push(k)
			}
			this.focusNodeId = null
			this.selectedNode = null
			this.loadData()
		},
		recenter(id) {
			this.focusNode(id)
		},
		async focusNode(id) {
			this.focusNodeId = id
			this.selectedNode = null
			await this.loadData()
			this.selectedNode = this.renderNodes.find(n => n.id === id) || null
			this.autoSpeed = 0
			this.drawGraph()
		},
		goDetail(node) {
			const n = node || this.selectedNode
			if (!n || n.id == null) return
			const name = encodeURIComponent(n.name || '')
			if (n.nodeType === 'PLAY') uni.navigateTo({ url: `/pages/play-detail/play-detail?nodeId=${n.id}&name=${name}` }).catch(() => {})
			else if (n.nodeType === 'ACTOR') uni.navigateTo({ url: `/pages/actor-detail/actor-detail?nodeId=${n.id}&name=${name}` }).catch(() => {})
			else if (n.nodeType === 'TERMINOLOGY') uni.navigateTo({ url: `/pages/terminology/terminology?nodeId=${n.id}&name=${name}` }).catch(() => {})
			else uni.navigateTo({ url: `/pages/node-detail/node-detail?nodeId=${n.id}&name=${name}&nodeType=${n.nodeType || ''}` }).catch(() => {})
		}
	}
}
</script>

<style scoped>
.page{position:relative;width:100vw;height:100vh;background:radial-gradient(circle at 78% 16%,rgba(242,201,76,.16),transparent 26%),radial-gradient(circle at 8% 10%,rgba(186,61,78,.18),transparent 28%),linear-gradient(180deg,#0f0609 0%,#17080d 34%,#12060a 100%);overflow:hidden;display:flex;flex-direction:column}
.toolbar-shell{padding:18rpx 18rpx 10rpx;position:relative;z-index:10}
.toolbar{padding:20rpx 20rpx 18rpx;background:rgba(31,11,18,.78);border:1rpx solid rgba(255,244,229,.1);box-shadow:0 18rpx 36rpx rgba(0,0,0,.28);border-radius:26rpx;backdrop-filter:blur(18rpx)}
.toolbar-top{display:flex;justify-content:space-between;align-items:flex-start;gap:18rpx;margin-bottom:18rpx}
.toolbar-title-wrap{flex:1}
.toolbar-kicker{display:block;font-size:18rpx;letter-spacing:3rpx;color:rgba(242,201,76,.84);margin-bottom:8rpx}
.toolbar-title{display:block;font-size:34rpx;font-weight:700;color:#fff4e8}
.toolbar-badge{padding:10rpx 18rpx;border-radius:999rpx;background:rgba(255,255,255,.06);border:1rpx solid rgba(255,255,255,.08);font-size:22rpx;color:rgba(255,240,228,.82)}
.filter-row{display:flex;gap:12rpx;flex-wrap:wrap;margin-bottom:14rpx}
.filter-btn{font-size:22rpx;padding:8rpx 18rpx;border-radius:999rpx;border:2rpx solid rgba(255,255,255,.12);border-color:rgba(255,255,255,.14);color:rgba(255,240,228,.72);background:rgba(255,255,255,.04)}
.filter-btn.active{border-color:var(--pill-color);color:#fff7eb;background:var(--pill-color)}
.search-row{display:flex;gap:12rpx;align-items:center}
.search-input{flex:1;height:60rpx;background:rgba(255,255,255,.06);border:1rpx solid rgba(255,255,255,.08);border-radius:30rpx;padding:0 24rpx;font-size:26rpx;color:#fff3ea}
.search-btn{min-width:110rpx;height:60rpx;line-height:60rpx;text-align:center;background:linear-gradient(135deg,#f2c94c,#c17f39);color:#43111a;border-radius:30rpx;font-size:26rpx;font-weight:700;box-shadow:0 10rpx 22rpx rgba(193,127,57,.24)}
.tap-guide{position:relative;z-index:8;margin:0 18rpx 8rpx;padding:12rpx 18rpx;border-radius:16rpx;background:rgba(242,201,76,.14);border:1rpx solid rgba(242,201,76,.22);font-size:22rpx;color:#fbe7b4}
.graph-canvas{flex:1;background:transparent}
.legend-shell{position:absolute;left:18rpx;bottom:24rpx;z-index:5}
.tap-hint{margin-bottom:12rpx;padding:12rpx 16rpx;background:rgba(19,7,11,.78);border:1rpx solid rgba(255,255,255,.08);border-radius:14rpx;font-size:22rpx;color:rgba(255,240,228,.82);backdrop-filter:blur(12rpx)}
.legend{display:flex;gap:16rpx;flex-wrap:wrap;max-width:86vw;background:rgba(19,7,11,.72);padding:12rpx 18rpx;border-radius:18rpx;border:1rpx solid rgba(255,255,255,.08);backdrop-filter:blur(14rpx)}
.legend-item{display:flex;align-items:center;gap:8rpx}.legend-dot{width:16rpx;height:16rpx;border-radius:50%}.legend-text{font-size:20rpx;color:rgba(255,240,228,.8)}
.detail-mask{position:fixed;inset:0;background:rgba(0,0,0,.4);display:flex;align-items:flex-end;justify-content:center;z-index:100}
.detail-card{width:92%;background:linear-gradient(180deg,rgba(255,248,241,.98),rgba(255,238,224,.96));border-radius:30rpx 30rpx 0 0;padding:40rpx 36rpx 60rpx;box-shadow:0 -12rpx 40rpx rgba(0,0,0,.18)}
.detail-head{display:flex;align-items:center;gap:16rpx;margin-bottom:20rpx}.detail-dot{width:24rpx;height:24rpx;border-radius:50%}.detail-name{font-size:36rpx;font-weight:700;color:#45131a}.detail-type{font-size:24rpx;color:#9a6d50;margin-left:auto}.detail-desc{font-size:28rpx;color:#6d4d4f;line-height:1.7;display:block;margin-bottom:16rpx}.detail-links{font-size:26rpx;color:#8f2433;font-weight:600;display:block;margin-bottom:24rpx}.detail-actions{display:flex;gap:20rpx}.detail-btn{flex:1;text-align:center;padding:20rpx;border-radius:18rpx;font-size:28rpx;background:linear-gradient(135deg,#6e1826,#9f3044);color:#fff7eb}.detail-btn.primary{background:linear-gradient(135deg,#4d1220,#7f2231)}.detail-btn.secondary{background:rgba(127,34,49,.08);color:#7f2231;border:1rpx solid rgba(127,34,49,.14)}
.mask{position:absolute;inset:0;background:rgba(10,3,5,.52);display:flex;flex-direction:column;align-items:center;justify-content:center;gap:20rpx;z-index:50}.ring{width:68rpx;height:68rpx;border-radius:50%;border:6rpx solid rgba(255,255,255,.14);border-top-color:#f2c94c;animation:spin .9s linear infinite}.mask-text{font-size:30rpx;color:rgba(255,240,228,.9)}
@keyframes spin{from{transform:rotate(0)}to{transform:rotate(360deg)}}
</style>
