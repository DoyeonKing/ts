<template>
	<view class="page">
		<view class="header">
			<text class="title">盘票社区</text>
			<text class="desc">求票、转票、交易记录统一管理，方便同好互换场次与座位</text>
		</view>
		<view class="tabs">
			<view :class="['tab', tabIndex===0?'active':'']" @click="tabIndex=0"><text>求票</text></view>
			<view :class="['tab', tabIndex===1?'active':'']" @click="tabIndex=1"><text>转票</text></view>
			<view :class="['tab', tabIndex===2?'active':'']" @click="tabIndex=2"><text>我的发布</text></view>
		</view>
		<view class="btn-publish" @click="publish">
			<text>{{ tabIndex <= 1 ? (tabIndex === 0 ? '发布求票' : '发布转票') : '查看全部' }}</text>
		</view>
		<view class="list">
			<view class="card" v-for="(t, i) in currentList" :key="i" @click="goDetail(t)">
				<view class="card-top">
					<text class="card-title">{{ t.showName }}</text>
					<text class="card-status" :class="t.status">{{ t.statusText }}</text>
				</view>
				<view class="card-row">
					<text class="label">场次</text>
					<text class="value">{{ t.date }} {{ t.time }}</text>
				</view>
				<view class="card-row">
					<text class="label">剧场</text>
					<text class="value">{{ t.venue }}</text>
				</view>
				<view class="card-row" v-if="t.seat">
					<text class="label">座位</text>
					<text class="value">{{ t.seat }}</text>
				</view>
				<view class="card-row">
					<text class="label">{{ tabIndex === 0 ? '期望价位' : '转票价格' }}</text>
					<text class="value price">{{ t.price }}</text>
				</view>
				<view class="card-footer">
					<text class="user">发布人 {{ t.userName }}</text>
					<text class="time">{{ t.publishTime }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			tabIndex: 0,
			qiupiao: [
				{ id: 1, showName: '《哈姆雷特》', date: '2024-03-01', time: '19:30', venue: '国家大剧院', seat: '', price: '面议', userName: '戏迷***', publishTime: '2小时前', status: 'pending', statusText: '求票中' },
				{ id: 2, showName: '《天鹅湖》', date: '2024-03-05', time: '19:00', venue: '北京舞蹈学院', seat: '', price: '¥200 以内', userName: '用户***', publishTime: '5小时前', status: 'pending', statusText: '求票中' }
			],
			zhuanpiao: [
				{ id: 3, showName: '《茶花女》', date: '2024-03-08', time: '19:30', venue: '中央歌剧院', seat: '2楼3排12座', price: '¥320', userName: '转***', publishTime: '1小时前', status: 'pending', statusText: '转票中' }
			],
			mine: [
				{ id: 1, showName: '《哈姆雷特》求票', date: '2024-03-01', time: '19:30', venue: '国家大剧院', seat: '', price: '面议', userName: '我', publishTime: '2小时前', status: 'pending', statusText: '进行中' }
			]
		}
	},
	computed: {
		currentList() {
			if (this.tabIndex === 0) return this.qiupiao
			if (this.tabIndex === 1) return this.zhuanpiao
			return this.mine
		}
	},
	methods: {
		publish() {
			if (this.tabIndex === 2) return
			uni.showToast({ title: this.tabIndex === 0 ? '发布求票' : '发布转票', icon: 'none' })
		},
		goDetail(t) {}
	}
}
</script>

<style scoped>
.page { padding: 24rpx; padding-bottom: 48rpx; min-height: 100vh; background: #f5f5f5; }
.header { margin-bottom: 28rpx; }
.title { font-size: 38rpx; font-weight: bold; color: #333; display: block; margin-bottom: 12rpx; }
.desc { font-size: 26rpx; color: #666; line-height: 1.5; display: block; }
.tabs { display: flex; gap: 20rpx; margin-bottom: 24rpx; }
.tab { flex: 1; text-align: center; padding: 20rpx 0; font-size: 28rpx; color: #666; background: #fff; border-radius: 16rpx; }
.tab.active { background: #6366f1; color: #fff; font-weight: 500; }
.btn-publish { background: #6366f1; color: #fff; text-align: center; padding: 24rpx; border-radius: 16rpx; margin-bottom: 24rpx; font-size: 28rpx; }
.list { display: flex; flex-direction: column; gap: 20rpx; }
.card { background: #fff; border-radius: 20rpx; padding: 28rpx; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06); }
.card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; }
.card-title { font-size: 32rpx; font-weight: bold; color: #333; }
.card-status { font-size: 24rpx; padding: 6rpx 16rpx; border-radius: 8rpx; }
.card-status.pending { background: rgba(99,102,241,0.15); color: #6366f1; }
.card-row { display: flex; margin-bottom: 12rpx; font-size: 26rpx; }
.label { color: #888; width: 140rpx; flex-shrink: 0; }
.value { color: #333; flex: 1; }
.value.price { color: #6366f1; font-weight: 500; }
.card-footer { margin-top: 20rpx; padding-top: 16rpx; border-top: 1rpx solid #eee; display: flex; justify-content: space-between; font-size: 24rpx; color: #999; }
</style>
