<template>
	<view class="page">
		<view class="order-card">
			<text class="title">订单信息</text>
			<text class="perf-name">{{ performanceName }}</text>
			<text class="perf-time">{{ dateTime }}</text>
			<text class="perf-seats">座位：{{ seatsDisplay }}</text>
			<view class="row">
				<text class="label">票数</text>
				<text class="value">{{ seatCount }} 张</text>
			</view>
			<view class="row total-row">
				<text class="label">合计</text>
				<text class="value price">¥{{ amount }}</text>
			</view>
		</view>
		<view class="pay-methods">
			<text class="section-title">支付方式</text>
			<view class="method-item" @click="payMethod='wechat'" :class="{ active: payMethod==='wechat' }">
				<text class="method-icon">💬</text>
				<text class="method-name">微信支付</text>
				<text v-if="payMethod==='wechat'" class="check">✓</text>
			</view>
			<view class="method-item" @click="payMethod='alipay'" :class="{ active: payMethod==='alipay' }">
				<text class="method-icon">💳</text>
				<text class="method-name">支付宝</text>
				<text v-if="payMethod==='alipay'" class="check">✓</text>
			</view>
		</view>
		<view class="btn-pay" @click="submitPay">确认支付 ¥{{ amount }}</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			performanceId: '',
			scheduleId: '',
			seats: '',
			seatsDisplay: '',
			seatCount: 0,
			amount: 0,
			performanceName: '',
			dateTime: '',
			payMethod: 'wechat'
		}
	},
	onLoad(opts) {
		this.performanceId = opts.performanceId || ''
		this.scheduleId = opts.scheduleId || ''
		this.seats = opts.seats ? decodeURIComponent(opts.seats) : ''
		this.seatsDisplay = this.seats || '-'
		this.seatCount = this.seats ? this.seats.split(',').length : 0
		this.amount = opts.amount || 0
		this.performanceName = opts.performanceName ? decodeURIComponent(opts.performanceName) : '演出'
		this.dateTime = opts.dateTime ? decodeURIComponent(opts.dateTime) : ''
	},
	methods: {
		submitPay() {
			uni.showLoading({ title: '支付中...' })
			setTimeout(() => {
				uni.hideLoading()
				uni.showToast({ title: '支付成功', icon: 'success' })
				setTimeout(() => {
					uni.navigateBack({ delta: 3 })
				}, 1500)
			}, 800)
		}
	}
}
</script>

<style scoped>
.page { padding: 24rpx; padding-bottom: 48rpx; min-height: 100vh; background: #f5f5f5; }
.order-card { background: #fff; border-radius: 20rpx; padding: 32rpx; margin-bottom: 24rpx; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06); }
.title { font-size: 30rpx; font-weight: bold; color: #333; display: block; margin-bottom: 20rpx; }
.perf-name { font-size: 32rpx; font-weight: bold; color: #333; display: block; margin-bottom: 12rpx; }
.perf-time { font-size: 26rpx; color: #666; display: block; margin-bottom: 8rpx; }
.perf-seats { font-size: 26rpx; color: #666; display: block; margin-bottom: 20rpx; }
.row { display: flex; justify-content: space-between; font-size: 28rpx; margin-bottom: 12rpx; }
.total-row { margin-top: 16rpx; padding-top: 16rpx; border-top: 1rpx solid #eee; }
.value.price { font-size: 34rpx; font-weight: bold; color: #6366f1; }
.pay-methods { background: #fff; border-radius: 20rpx; padding: 28rpx; margin-bottom: 32rpx; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06); }
.section-title { font-size: 28rpx; font-weight: bold; color: #333; display: block; margin-bottom: 20rpx; }
.method-item { display: flex; align-items: center; padding: 24rpx 0; border-bottom: 1rpx solid #f0f0f0; }
.method-item:last-child { border-bottom: none; }
.method-item.active { color: #6366f1; }
.method-icon { font-size: 40rpx; margin-right: 20rpx; }
.method-name { flex: 1; font-size: 28rpx; }
.check { color: #6366f1; font-weight: bold; }
.btn-pay { background: #6366f1; color: #fff; text-align: center; padding: 28rpx; border-radius: 24rpx; font-size: 32rpx; font-weight: bold; }
</style>
