<template>
	<view class="page">
		<view class="header-card">
			<text class="perf-name">{{ performanceName }}</text>
			<text class="perf-meta">{{ dateTime }} · {{ venue }}</text>
			<text class="selected-info">已选座位：{{ selectedSeats.length }} 个</text>
			<text class="seats-text" v-if="selectedSeats.length">座位：{{ selectedSeats.join('、') }}</text>
		</view>
		<view class="stage-bar">舞台</view>
		<scroll-view scroll-x scroll-y class="seat-area" :scroll-top="scrollTop">
			<view class="seat-grid">
				<view class="seat-row" v-for="(row, rowIndex) in seatRows" :key="rowIndex">
					<text class="row-label">{{ rowLabel(rowIndex) }}</text>
					<view class="row-seats">
						<view
							v-for="col in 20"
							:key="col"
							:class="['seat', seatState(rowIndex, col)]"
							@click="toggleSeat(rowIndex, col)"
						>
							<text class="seat-num">{{ col }}</text>
						</view>
					</view>
				</view>
			</view>
		</scroll-view>
		<view class="legend">
			<view class="legend-item"><view class="dot available"></view><text>可选</text></view>
			<view class="legend-item"><view class="dot selected"></view><text>已选</text></view>
			<view class="legend-item"><view class="dot sold"></view><text>已售</text></view>
		</view>
		<view class="bottom-bar">
			<view class="total">合计 ¥{{ totalPrice }}</view>
			<view class="btn-confirm" :class="{ disabled: selectedSeats.length === 0 }" @click="confirm">确认选座</view>
		</view>
	</view>
</template>

<script>
const ROW_LABELS = 'ABCDEFGHIJ'
const SOLD_SEATS = ['A1', 'A2', 'B5', 'C10', 'D15', 'E8', 'F12', 'G3', 'H7', 'I14', 'J20']
const PRICE_PER_SEAT = 180

export default {
	data() {
		return {
			performanceId: '',
			scheduleId: '',
			performanceName: '',
			dateTime: '',
			venue: '',
			selectedSeats: [],
			seatRows: 10,
			scrollTop: 0
		}
	},
	computed: {
		totalPrice() {
			return this.selectedSeats.length * PRICE_PER_SEAT
		}
	},
	onLoad(opts) {
		this.performanceId = opts.performanceId || opts.id || '1'
		this.scheduleId = opts.scheduleId || '1'
		this.performanceName = opts.performanceName ? decodeURIComponent(opts.performanceName) : '《哈姆雷特》'
		this.dateTime = opts.dateTime ? decodeURIComponent(opts.dateTime) : '2024-03-01 19:30'
		this.venue = opts.venue ? decodeURIComponent(opts.venue) : '国家大剧院 歌剧厅'
	},
	methods: {
		rowLabel(rowIndex) {
			return ROW_LABELS[rowIndex] || ''
		},
		seatId(rowIndex, col) {
			return ROW_LABELS[rowIndex] + col
		},
		seatState(rowIndex, col) {
			const id = this.seatId(rowIndex, col)
			if (this.selectedSeats.indexOf(id) >= 0) return 'selected'
			if (SOLD_SEATS.indexOf(id) >= 0) return 'sold'
			return 'available'
		},
		toggleSeat(rowIndex, col) {
			const id = this.seatId(rowIndex, col)
			if (SOLD_SEATS.indexOf(id) >= 0) return
			const i = this.selectedSeats.indexOf(id)
			if (i >= 0) {
				this.selectedSeats = this.selectedSeats.filter(s => s !== id)
			} else {
				this.selectedSeats = [...this.selectedSeats, id]
			}
		},
		confirm() {
			if (this.selectedSeats.length === 0) {
				uni.showToast({ title: '请先选择座位', icon: 'none' })
				return
			}
			const seats = this.selectedSeats.join(',')
			const amount = this.selectedSeats.length * PRICE_PER_SEAT
			uni.navigateTo({
				url: `/pages/payment/payment?performanceId=${this.performanceId}&scheduleId=${this.scheduleId}&seats=${encodeURIComponent(seats)}&amount=${amount}&performanceName=${encodeURIComponent(this.performanceName)}&dateTime=${encodeURIComponent(this.dateTime)}`
			})
		}
	}
}
</script>

<style scoped>
.page { padding: 24rpx; padding-bottom: 160rpx; min-height: 100vh; background: #f5f5f5; }
.header-card { background: rgba(99,102,241,0.12); border-radius: 16rpx; padding: 24rpx; margin-bottom: 24rpx; }
.perf-name { font-size: 32rpx; font-weight: bold; color: #333; display: block; margin-bottom: 8rpx; }
.perf-meta { font-size: 26rpx; color: #666; display: block; margin-bottom: 8rpx; }
.selected-info { font-size: 26rpx; color: #6366f1; display: block; }
.seats-text { font-size: 24rpx; color: #666; display: block; margin-top: 6rpx; }
.stage-bar { background: #8B4513; color: #fff; text-align: center; padding: 16rpx; border-radius: 8rpx; margin-bottom: 20rpx; font-size: 28rpx; font-weight: bold; }
.seat-area { max-height: 70vh; }
.seat-grid { padding: 0 0 24rpx; }
.seat-row { display: flex; align-items: center; margin-bottom: 12rpx; }
.row-label { width: 48rpx; font-size: 24rpx; font-weight: bold; color: #333; flex-shrink: 0; }
.row-seats { display: flex; flex-wrap: nowrap; gap: 8rpx; }
.seat { width: 48rpx; height: 48rpx; border-radius: 8rpx; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.seat-num { font-size: 20rpx; }
.seat.available { background: #fff; border: 2rpx solid #ddd; color: #333; }
.seat.selected { background: #6366f1; border: 2rpx solid #6366f1; color: #fff; }
.seat.sold { background: #bbb; border: 2rpx solid #999; color: #fff; }
.legend { display: flex; gap: 32rpx; margin-top: 24rpx; padding: 20rpx 0; }
.legend-item { display: flex; align-items: center; gap: 12rpx; font-size: 24rpx; color: #666; }
.dot { width: 28rpx; height: 28rpx; border-radius: 6rpx; }
.dot.available { background: #fff; border: 2rpx solid #ddd; }
.dot.selected { background: #6366f1; }
.dot.sold { background: #bbb; }
.bottom-bar { position: fixed; left: 0; right: 0; bottom: 0; display: flex; align-items: center; justify-content: space-between; padding: 24rpx 32rpx; background: #fff; box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.08); }
.total { font-size: 32rpx; font-weight: bold; color: #6366f1; }
.btn-confirm { background: #6366f1; color: #fff; padding: 24rpx 48rpx; border-radius: 24rpx; font-size: 30rpx; }
.btn-confirm.disabled { background: #ccc; color: #fff; }
</style>
