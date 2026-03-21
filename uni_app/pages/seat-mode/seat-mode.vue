<template>
	<view class="page">
		<view class="info-card">
			<text class="perf-name">{{ performanceName }}</text>
			<text class="perf-meta">{{ dateTime }} · {{ venue }}</text>
			<text class="perf-price">票价 {{ priceRange }}</text>
		</view>
		<text class="section-title">选择选座方式</text>
		<view class="mode-list">
			<view class="mode-item" @click="go2D">
				<view class="mode-icon">📐</view>
				<view class="mode-body">
					<text class="mode-name">2D 选座</text>
					<text class="mode-desc">平面座位图，按排按号选座，一目了然</text>
				</view>
				<text class="mode-arrow">›</text>
			</view>
			<view class="mode-item highlight" @click="go3D">
				<view class="mode-icon">🎭</view>
				<view class="mode-body">
					<text class="mode-name">3D 选座</text>
					<text class="mode-desc">沉浸式剧场视角，3D/2D/俯视切换，区域选座</text>
				</view>
				<text class="mode-arrow">›</text>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			performanceId: '',
			scheduleId: '1',
			performanceName: '《哈姆雷特》',
			dateTime: '2024-03-01 19:30',
			venue: '国家大剧院 歌剧厅',
			priceRange: '¥180-1280'
		}
	},
	onLoad(opts) {
		this.performanceId = opts.performanceId || opts.id || '1'
		this.scheduleId = opts.scheduleId || '1'
		this.performanceName = opts.performanceName || decodeURIComponent(opts.name || '') || '《哈姆雷特》'
		this.dateTime = opts.dateTime ? decodeURIComponent(opts.dateTime) : '2024-03-01 19:30'
		this.venue = opts.venue ? decodeURIComponent(opts.venue) : '国家大剧院 歌剧厅'
		this.priceRange = opts.priceRange || '¥180-1280'
	},
	methods: {
		go2D() {
			uni.navigateTo({
				url: `/pages/seat-selection-2d/seat-selection-2d?performanceId=${this.performanceId}&scheduleId=${this.scheduleId}&performanceName=${encodeURIComponent(this.performanceName)}&dateTime=${encodeURIComponent(this.dateTime)}&venue=${encodeURIComponent(this.venue)}`
			})
		},
		go3D() {
			const q = `performanceId=${this.performanceId}&scheduleId=${this.scheduleId}&performanceName=${encodeURIComponent(this.performanceName)}&dateTime=${encodeURIComponent(this.dateTime)}&venue=${encodeURIComponent(this.venue)}`
			uni.navigateTo({ url: `/pages/seat-selection-3d/seat-selection-3d?${q}` })
		}
	}
}
</script>

<style scoped>
.page { padding: 24rpx; min-height: 100vh; background: #f5f5f5; }
.info-card { background: linear-gradient(135deg, rgba(99,102,241,0.15) 0%, #fff 100%); border-radius: 20rpx; padding: 32rpx; margin-bottom: 32rpx; }
.perf-name { font-size: 36rpx; font-weight: bold; color: #333; display: block; margin-bottom: 12rpx; }
.perf-meta { font-size: 26rpx; color: #666; display: block; margin-bottom: 8rpx; }
.perf-price { font-size: 26rpx; color: #6366f1; display: block; }
.section-title { font-size: 30rpx; font-weight: bold; color: #333; display: block; margin-bottom: 24rpx; }
.mode-list { display: flex; flex-direction: column; gap: 20rpx; }
.mode-item { display: flex; align-items: center; background: #fff; border-radius: 20rpx; padding: 28rpx; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06); }
.mode-item.highlight { border: 2rpx solid rgba(99,102,241,0.4); }
.mode-icon { width: 88rpx; height: 88rpx; background: rgba(99,102,241,0.1); border-radius: 20rpx; display: flex; align-items: center; justify-content: center; font-size: 44rpx; margin-right: 24rpx; }
.mode-body { flex: 1; min-width: 0; }
.mode-name { font-size: 32rpx; font-weight: bold; color: #333; display: block; margin-bottom: 8rpx; }
.mode-desc { font-size: 24rpx; color: #888; line-height: 1.4; display: block; }
.mode-arrow { font-size: 36rpx; color: #999; }
</style>
