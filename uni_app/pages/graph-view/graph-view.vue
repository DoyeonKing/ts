<template>
<view class="page">
  <view class="header">
    <text class="title">知识图谱全景</text>
    <text class="sub">第二步：加搜索与类型筛选，保持节点/连线/拖动稳定</text>
    <view class="filters">
      <view v-for="t in typeFilters" :key="t.key" class="pill" :style="pillStyle(t)" @click="toggleType(t.key)">{{ t.label }}</view>
    </view>
    <view class="search-row">
      <input class="search-input" v-model="searchKey" placeholder="搜索节点、人物、术语..." placeholder-style="color: rgba(255,240,228,.35);" @confirm="onSearch" />
      <view class="search-btn" @click="onSearch">搜索</view>
    </view>
  </view>

  <view class="tip">现在支持搜索、筛选、拖空白移动整图、拖节点改位置、点击节点看卡片</view>

  <view class="stage" @touchstart="onStageStart" @touchmove="onStageMove" @touchend="onStageEnd">
    <canvas type="2d" id="graphCanvas" class="graph-canvas" :style="{ width: canvasW + 'px', height: canvasH + 'px' }"></canvas>
    <GraphNode
      v-for="node in visibleNodes"
      :key="node.id"
      :node="node"
      :stageX="stageX"
      :stageY="stageY"
      :color="getColor(node.nodeType)"
      :selected="selectedNode && selectedNode.id === node.id"
      :faded="selectedNode && !relatedIds.has(node.id)"
      @touchstart="onNodeStart"
      @touchmove="onNodeMove"
      @touchend="onNodeEnd"
    />
  </view>

  <GraphDetailPanel
    :node="selectedNode"
    :color="selectedNode ? getColor(selectedNode.nodeType) : '#999'"
    :typeLabel="selectedNode ? getTypeLabel(selectedNode.nodeType) : ''"
    @recenter="recenter"
    @close="clearSelection"
  />

  <view class="mask" v-if="loading">
    <view class="ring"></view>
    <text class="mask-text">图谱加载中...</text>
  </view>
</view>
</template>

<script>
import GraphNode from '../../components/graph/GraphNode.vue'
import GraphDetailPanel from '../../components/graph/GraphDetailPanel.vue'
import { getFullGraph, getNodeNeighborhood, searchNodes } from '../../api/knowledgeGraph.js'

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

function touchPoint(touch) {
  return {
    x: touch.clientX != null ? touch.clientX : (touch.pageX != null ? touch.pageX : (touch.x || 0)),
    y: touch.clientY != null ? touch.clientY : (touch.pageY != null ? touch.pageY : (touch.y || 0))
  }
}

export default {
  components: {
    GraphNode,
    GraphDetailPanel
  },
  computed: {
    relatedIds() {
      const ids = new Set()
      if (!this.selectedNode) return ids
      ids.add(this.selectedNode.id)
      this.edges.forEach(edge => {
        if (edge.source === this.selectedNode.id || edge.target === this.selectedNode.id) {
          ids.add(edge.source)
          ids.add(edge.target)
        }
      })
      return ids
    }
  },
  data() {
    return {
      canvasW: 375,
      canvasH: 600,
      ctx: null,
      canvas: null,
      dpr: 1,
      loading: false,
      searchKey: '',
      focusNodeId: null,
      nodes: [],
      edges: [],
      visibleNodes: [],
      selectedNode: null,
      activeTypes: ['PLAY', 'ACTOR', 'TERMINOLOGY', 'TAG', 'VENUE'],
      typeFilters: [
        { key: 'PLAY', label: '剧目' },
        { key: 'ACTOR', label: '演员' },
        { key: 'TERMINOLOGY', label: '术语' },
        { key: 'TAG', label: '标签' },
        { key: 'VENUE', label: '场馆' }
      ],
      stageX: 0,
      stageY: 0,
      startX: 0,
      startY: 0,
      lastX: 0,
      lastY: 0,
      stageDragging: false,
      draggingNodeId: null,
      nodeMoved: false
    }
  },
  onLoad(options) {
    const sys = uni.getSystemInfoSync()
    this.canvasW = sys.windowWidth
    this.canvasH = sys.windowHeight - 140
    if (options && options.nodeId) this.focusNodeId = parseInt(options.nodeId)
  },
  onReady() {
    const q = uni.createSelectorQuery().in(this)
    q.select('#graphCanvas').fields({ node: true, size: true }).exec(res => {
      if (res && res[0] && res[0].node) {
        this.canvas = res[0].node
        this.ctx = this.canvas.getContext('2d')
        this.canvas.width = this.canvasW * this.dpr
        this.canvas.height = this.canvasH * this.dpr
        this.ctx.scale(this.dpr, this.dpr)
      } else {
        this.ctx = uni.createCanvasContext('graphCanvas', this)
      }
      this.loadData()
    })
  },
  methods: {
    getColor(type) {
      return TYPE_COLORS[type] || '#999'
    },
    getTypeLabel(type) {
      return TYPE_LABELS[type] || type
    },
    shortName(name) {
      const text = String(name || '')
      return text.length > 4 ? text.slice(0, 3) + '..' : text
    },
    radiusFor(node) {
      const base = node.nodeType === 'TAG' ? 20 : 24
      return Math.min(base + (node.linkCount || 1), 30)
    },
    nodeStyle(node) {
      const size = node.r * 2
      return {
        left: `${node.x + this.stageX - node.r}px`,
        top: `${node.y + this.stageY - node.r}px`,
        width: `${size}px`,
        height: `${size}px`,
        background: this.getColor(node.nodeType)
      }
    },
    async loadData() {
      this.loading = true
      try {
        const res = this.focusNodeId ? await getNodeNeighborhood(this.focusNodeId) : await getFullGraph()
        const data = res.data || res
        const visibleSet = new Set(this.activeTypes)
        this.nodes = (data.nodes || []).filter(node => visibleSet.has(node.nodeType))
        this.edges = data.edges || []
        this.buildLayout()
      } catch (e) {
        this.nodes = []
        this.visibleNodes = []
        this.edges = []
        uni.showToast({ title: '图谱加载失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    buildLayout() {
      const centerX = this.canvasW / 2
      const centerY = this.canvasH / 2
      const cols = Math.max(3, Math.ceil(Math.sqrt(this.nodes.length || 1)))
      const gapX = Math.min(100, Math.max(72, this.canvasW / (cols + 1)))
      const gapY = 90
      const startX = centerX - ((cols - 1) * gapX) / 2
      const rows = Math.max(1, Math.ceil(this.nodes.length / cols))
      const startY = centerY - ((rows - 1) * gapY) / 2

      this.visibleNodes = this.nodes.map((node, index) => {
        const col = index % cols
        const row = Math.floor(index / cols)
        const r = this.radiusFor(node)
        return {
          ...node,
          x: startX + col * gapX,
          y: startY + row * gapY,
          r
        }
      })

      this.edges = (this.edges || []).filter(edge => {
        const ids = new Set(this.visibleNodes.map(node => node.id))
        return ids.has(edge.source) && ids.has(edge.target)
      })

      if (this.focusNodeId) {
        const focus = this.visibleNodes.find(node => node.id === this.focusNodeId)
        if (focus) {
          const dx = centerX - focus.x
          const dy = centerY - focus.y
          this.visibleNodes.forEach(node => {
            node.x += dx
            node.y += dy
          })
        }
      }

      this.stageX = 0
      this.stageY = 0
      this.drawLines()
    },
    onStageStart(e) {
      const pos = touchPoint(e.touches[0])
      this.startX = pos.x
      this.startY = pos.y
      this.lastX = pos.x
      this.lastY = pos.y
      this.stageDragging = false
    },
    onStageMove(e) {
      if (this.draggingNodeId) return
      const pos = touchPoint(e.touches[0])
      const dx = pos.x - this.lastX
      const dy = pos.y - this.lastY
      if (Math.abs(pos.x - this.startX) > 6 || Math.abs(pos.y - this.startY) > 6) {
        this.stageDragging = true
      }
      if (this.stageDragging) {
        this.stageX += dx
        this.stageY += dy
        this.lastX = pos.x
        this.lastY = pos.y
        this.drawLines()
      }
    },
    onStageEnd() {
      if (!this.stageDragging && this.selectedNode) {
        this.clearSelection()
      }
      this.stageDragging = false
    },
    onNodeStart(e, node) {
      const pos = touchPoint(e.touches[0])
      node._sx = pos.x
      node._sy = pos.y
      node._lx = pos.x
      node._ly = pos.y
      this.draggingNodeId = node.id
      this.nodeMoved = false
    },
    onNodeMove(e, node) {
      const pos = touchPoint(e.touches[0])
      const dx = pos.x - node._lx
      const dy = pos.y - node._ly
      if (Math.abs(pos.x - node._sx) > 4 || Math.abs(pos.y - node._sy) > 4) {
        this.nodeMoved = true
      }
      node.x += dx
      node.y += dy
      node._lx = pos.x
      node._ly = pos.y
      this.drawLines()
    },
    onNodeEnd(e, node) {
      const pos = touchPoint(e.changedTouches[0] || e.touches[0])
      if (Math.abs(pos.x - node._sx) > 4 || Math.abs(pos.y - node._sy) > 4) {
        this.nodeMoved = true
      }
      if (!this.nodeMoved) {
        this.selectedNode = node
      }
      this.draggingNodeId = null
      this.nodeMoved = false
    },
    drawLines() {
      const ctx = this.ctx
      if (!ctx) return
      const map = new Map(this.visibleNodes.map(node => [node.id, node]))
      if (this.canvas) {
        ctx.clearRect(0, 0, this.canvasW, this.canvasH)
        const bg = ctx.createLinearGradient(0, 0, 0, this.canvasH)
        bg.addColorStop(0, '#0f0609')
        bg.addColorStop(0.45, '#17080d')
        bg.addColorStop(1, '#12060a')
        ctx.fillStyle = bg
        ctx.fillRect(0, 0, this.canvasW, this.canvasH)
      } else {
        ctx.setFillStyle('#10070b')
        ctx.fillRect(0, 0, this.canvasW, this.canvasH)
      }

      this.edges.forEach(edge => {
        const from = map.get(edge.source)
        const to = map.get(edge.target)
        if (!from || !to) return
        const active = !this.selectedNode || (this.relatedIds.has(edge.source) && this.relatedIds.has(edge.target))
        ctx.beginPath()
        ctx.moveTo(from.x + this.stageX, from.y + this.stageY)
        ctx.lineTo(to.x + this.stageX, to.y + this.stageY)
        if (this.canvas) {
          ctx.strokeStyle = active ? 'rgba(242,201,76,0.46)' : 'rgba(255,255,255,0.12)'
          ctx.lineWidth = active ? 1.8 : 1
        } else {
          ctx.setStrokeStyle(active ? 'rgba(242,201,76,0.46)' : 'rgba(255,255,255,0.12)')
          ctx.setLineWidth(active ? 1.8 : 1)
        }
        ctx.stroke()
      })

      if (!this.canvas) ctx.draw()
    },

    pillStyle(item) {
      const active = this.activeTypes.includes(item.key)
      return {
        borderColor: active ? this.getColor(item.key) : 'rgba(255,255,255,.14)',
        color: active ? '#fff7eb' : 'rgba(255,240,228,.72)',
        background: active ? this.getColor(item.key) : 'rgba(255,255,255,.04)'
      }
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
          this.selectedNode = this.visibleNodes.find(node => node.id === this.focusNodeId) || null
        } else {
          uni.showToast({ title: '未找到相关节点', icon: 'none' })
        }
      } catch (e) {
        uni.showToast({ title: '未找到相关节点', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    toggleType(key) {
      const index = this.activeTypes.indexOf(key)
      if (index >= 0) {
        if (this.activeTypes.length <= 1) return
        this.activeTypes.splice(index, 1)
      } else {
        this.activeTypes.push(key)
      }
      this.selectedNode = null
      this.focusNodeId = null
      this.loadData()
    },
    recenter(nodeId) {
      this.focusNodeId = nodeId
      this.loadData()
    },
    clearSelection() {
      this.selectedNode = null
    }
  }
}
</script>

<style scoped>
.page{position:relative;width:100vw;height:100vh;background:radial-gradient(circle at 78% 16%,rgba(242,201,76,.16),transparent 26%),radial-gradient(circle at 8% 10%,rgba(186,61,78,.18),transparent 28%),linear-gradient(180deg,#0f0609 0%,#17080d 34%,#12060a 100%);overflow:hidden;display:flex;flex-direction:column}
.header{padding:18rpx 18rpx 10rpx}.title{display:block;font-size:36rpx;font-weight:700;color:#fff4e8}.sub{display:block;font-size:22rpx;color:rgba(255,240,228,.8);margin-top:8rpx}.filters{display:flex;gap:12rpx;flex-wrap:wrap;margin-top:14rpx}.pill{font-size:22rpx;padding:8rpx 18rpx;border-radius:999rpx;border:2rpx solid rgba(255,255,255,.12)}.search-row{display:flex;gap:12rpx;margin-top:14rpx}.search-input{flex:1;height:60rpx;background:rgba(255,255,255,.06);border:1rpx solid rgba(255,255,255,.08);border-radius:30rpx;padding:0 24rpx;font-size:26rpx;color:#fff3ea}.search-btn{min-width:110rpx;height:60rpx;line-height:60rpx;text-align:center;background:linear-gradient(135deg,#f2c94c,#c17f39);color:#43111a;border-radius:30rpx;font-size:26rpx;font-weight:700}.tip{margin:0 18rpx 8rpx;padding:12rpx 18rpx;border-radius:16rpx;background:rgba(242,201,76,.14);border:1rpx solid rgba(242,201,76,.22);font-size:22rpx;color:#fbe7b4}.stage{position:relative;flex:1;overflow:hidden}.graph-canvas{position:absolute;inset:0;z-index:1}.mask{position:absolute;inset:0;background:rgba(10,3,5,.52);display:flex;flex-direction:column;align-items:center;justify-content:center;gap:20rpx;z-index:50}.ring{width:68rpx;height:68rpx;border-radius:50%;border:6rpx solid rgba(255,255,255,.14);border-top-color:#f2c94c;animation:spin .9s linear infinite}.mask-text{font-size:30rpx;color:rgba(255,240,228,.9)}@keyframes spin{from{transform:rotate(0)}to{transform:rotate(360deg)}}
</style>
