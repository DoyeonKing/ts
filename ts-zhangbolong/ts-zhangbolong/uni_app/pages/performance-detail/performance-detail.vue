<template>
	<view class="page">
		<view class="card">
			<text class="title">{{ detail.name || '演出详情' }}</text>
			<text class="venue">剧场：{{ detail.venue || '-' }}</text>
			<text class="time">时间：{{ detail.time || '-' }}</text>
			<text class="price">票价：{{ detail.price || '¥180起' }}</text>
			<view class="btn-buy" @click="goSeatMode">立即购票 · 选座</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			detail: {}
		}
	},
	onLoad(opts) {
		const id = opts.id || opts.performanceId || '1'
		const map = {
			'1': { name: '《哈姆雷特》', venue: '国家大剧院 歌剧厅', time: '2024年3月1日 19:30', price: '¥180-1280' },
			'2': { name: '《天鹅湖》', venue: '北京舞蹈学院', time: '2024年3月2日 19:00', price: '¥280-880' },
			'3': { name: '《茶花女》', venue: '中央歌剧院', time: '2024年3月3日 19:30', price: '¥380-1280' },
			'4': { name: '《罗密欧与朱丽叶》', venue: '北京人艺', time: '2024年3月5日 19:30', price: '¥220-680' },
			'5': { name: '《歌剧魅影》', venue: '上海大剧院', time: '2024年3月8日 19:30', price: '¥320-980' }
		}
		const base = map[id] || map['1']
		this.detail = { id, ...base }
	},
	methods: {
		goSeatMode() {
			const performanceId = this.detail.id
			const scheduleId = '1'
			const performanceName = this.detail.name
			const dateTime = this.detail.time
			const venue = this.detail.venue
			const priceRange = this.detail.price || '¥180-1280'
			uni.navigateTo({
				url: `/pages/seat-mode/seat-mode?performanceId=${performanceId}&scheduleId=${scheduleId}&performanceName=${encodeURIComponent(performanceName)}&dateTime=${encodeURIComponent(dateTime)}&venue=${encodeURIComponent(venue)}&priceRange=${encodeURIComponent(priceRange)}`
			})
		}
	}
}
</script>

<style scoped>
.page { padding: 24rpx; }
.card { background: #fff; border-radius: 24rpx; padding: 40rpx; }
.title { font-size: 36rpx; font-weight: bold; display: block; margin-bottom: 24rpx; }
.venue, .time, .price { font-size: 28rpx; color: #666; display: block; margin-bottom: 16rpx; }
.btn-buy { margin-top: 32rpx; background: #6366f1; color: #fff; text-align: center; padding: 28rpx; border-radius: 16rpx; font-size: 30rpx; }
</style>
