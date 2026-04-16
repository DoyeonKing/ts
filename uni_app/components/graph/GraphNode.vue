<template>
<view class="node" :class="{ selected, faded }" :style="nodeStyle" @touchstart.stop="$emit('touchstart', $event, node)" @touchmove.stop="$emit('touchmove', $event, node)" @touchend.stop="$emit('touchend', $event, node)">
  <text class="node-text">{{ shortName }}</text>
</view>
</template>

<script>
export default {
  name: 'GraphNode',
  options: {
    virtualHost: true
  },
  props: {
    node: { type: Object, required: true },
    stageX: { type: Number, default: 0 },
    stageY: { type: Number, default: 0 },
    color: { type: String, default: '#999' },
    selected: { type: Boolean, default: false },
    faded: { type: Boolean, default: false }
  },
  computed: {
    nodeStyle() {
      const r = this.node.r || 24
      const size = r * 2
      return {
        left: `${this.node.x + this.stageX - r}px`,
        top: `${this.node.y + this.stageY - r}px`,
        width: `${size}px`,
        height: `${size}px`,
        background: this.color
      }
    },
    shortName() {
      const text = String((this.node && this.node.name) || '')
      return text.length > 4 ? text.slice(0, 3) + '..' : text
    }
  }
}
</script>

<style scoped>
.node{position:absolute;border-radius:999rpx;display:flex;align-items:center;justify-content:center;border:2rpx solid rgba(255,255,255,.92);box-shadow:0 0 24rpx rgba(255,255,255,.22);z-index:6;transition:opacity .16s ease,transform .16s ease}.selected{transform:scale(1.08);box-shadow:0 0 30rpx rgba(255,248,231,.5);z-index:8}.faded{opacity:.34}.node-text{font-size:18rpx;color:#fffef7;font-weight:700}
</style>
