<template>
<view class="panel" v-if="node">
  <view class="panel-card" @click.stop>
    <view class="panel-head">
      <view class="dot" :style="{ background: color }"></view>
      <text class="panel-name">{{ node.name }}</text>
      <text class="panel-type">{{ typeLabel }}</text>
    </view>
    <text class="panel-desc" v-if="node.description">{{ node.description }}</text>
    <text class="panel-links" v-if="node.linkCount">关联 {{ node.linkCount }} 个节点</text>
    <view class="panel-actions">
      <view class="btn tertiary" @tap.stop="$emit('goDetail', node)">查看详情</view>
      <view class="btn primary" @tap.stop="$emit('recenter', node.id)">以此为中心展开</view>
      <view class="btn secondary" @tap.stop="$emit('close')">关闭</view>
    </view>
  </view>
</view>
</template>

<script>
export default {
  name: 'GraphDetailPanel',
  options: {
    virtualHost: true
  },
  props: {
    node: { type: Object, default: null },
    color: { type: String, default: '#999' },
    typeLabel: { type: String, default: '' }
  }
}
</script>

<style scoped>
.panel{position:absolute;left:0;right:0;bottom:0;display:flex;justify-content:center;z-index:100;pointer-events:none}.panel-card{width:92%;padding:40rpx 36rpx 60rpx;background:linear-gradient(180deg,rgba(255,248,241,.98),rgba(255,238,224,.96));border-radius:30rpx 30rpx 0 0;box-shadow:0 -12rpx 40rpx rgba(0,0,0,.18);pointer-events:auto}.panel-head{display:flex;align-items:center;gap:16rpx;margin-bottom:20rpx}.dot{width:24rpx;height:24rpx;border-radius:50%}.panel-name{font-size:36rpx;font-weight:700;color:#45131a}.panel-type{font-size:24rpx;color:#9a6d50;margin-left:auto}.panel-desc{font-size:28rpx;color:#6d4d4f;line-height:1.7;display:block;margin-bottom:16rpx}.panel-links{display:block;margin-bottom:24rpx;font-size:26rpx;color:#8f2433;font-weight:600}.panel-actions{display:flex;gap:16rpx}.btn{flex:1;text-align:center;padding:20rpx;border-radius:18rpx;font-size:26rpx}.tertiary{background:#efe7ff;color:#5b35c8}.primary{background:linear-gradient(135deg,#6e1826,#9f3044);color:#fff7eb}.secondary{background:rgba(127,34,49,.08);color:#7f2231;border:1rpx solid rgba(127,34,49,.14)}
</style>
