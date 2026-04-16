<template>
	<view class="page">
		<view class="info-card">
			<text class="perf-name">{{ performanceName }}</text>
			<text class="perf-meta">{{ dateTime }} · {{ venue }}</text>
			<text class="perf-price">座位参考区间 {{ priceRange }}</text>
			<text class="perf-hint">这里是剧场参考软件，用来展示选座和观演视角，不涉及支付或下单。</text>
		</view>
		<text class="section-title">3D 选座</text>
		<view class="mode-list">
			<view class="mode-item highlight" @click="go3D">
				<view class="mode-icon">🎭</view>
				<view class="mode-body">
					<text class="mode-name">3D 选座参考</text>
					<text class="mode-desc">更像真实剧场的沉浸式舞台空间，支持无遮挡 / 前方坐姿 / 前方高个视角模拟</text>
				</view>
				<text class="mode-arrow">›</text>
			</view>
		</view>
	</view>
</template>

<script>
function safeDecode(value) {
	if (!value) return ''
	try {
		return decodeURIComponent(value)
	} catch (e) {
		return value
	}
}

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
		this.performanceName = safeDecode(opts.performanceName) || safeDecode(opts.name) || '《哈姆雷特》'
		this.dateTime = safeDecode(opts.dateTime) || '2024-03-01 19:30'
		this.venue = safeDecode(opts.venue) || '国家大剧院 歌剧厅'
		this.priceRange = safeDecode(opts.priceRange) || '¥180-1280'
	},
	methods: {
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
.perf-price { font-size: 26rpx; color: #6366f1; display: block; margin-bottom: 8rpx; }
.perf-hint { font-size: 22rpx; color: #7c5a38; line-height: 1.6; display: block; }
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
