<template>
	<view class="page">
		<view class="tabs">
			<view v-for="(t, i) in tabs" :key="i" :class="['tab', currentTab === i ? 'active' : '']" @click="currentTab = i">
				<text>{{ t }}</text>
			</view>
		</view>
		<view class="content">
			<!-- 演出列表 -->
			<view v-show="currentTab === 0" class="tab-content">
				<view v-for="(item, i) in performanceList" :key="i" class="perf-item" @click="goDetail(item.id)">
					<view class="perf-item-main">
						<text class="perf-item-title">{{ item.name }}</text>
						<text class="perf-item-venue">{{ item.venue }}</text>
						<text class="perf-item-time">{{ item.time }}</text>
						<text class="perf-item-price">{{ item.price }}</text>
					</view>
				</view>
			</view>
			<!-- 排班表 -->
			<view v-show="currentTab === 1" class="tab-content">
				<view class="schedule-tip">按剧场 / 剧目 / 演员查看排班</view>
				<view v-for="(s, i) in scheduleList" :key="i" class="schedule-card">
					<text class="schedule-title">{{ s.title }}</text>
					<text class="schedule-info">{{ s.theater }} · {{ s.date }}</text>
				</view>
			</view>
			<!-- SD信息 -->
			<view v-show="currentTab === 2" class="tab-content">
				<view class="sd-tip">查询演员当天是否 SD（Stage Door）</view>
				<view class="sd-card" v-for="(sd, i) in sdList" :key="i">
					<text class="sd-name">{{ sd.actor }}</text>
					<text class="sd-show">{{ sd.show }}</text>
					<text class="sd-date">{{ sd.date }}</text>
					<text class="sd-status" :class="sd.hasSD ? 'yes' : 'no'">{{ sd.hasSD ? '有 SD' : '无 SD' }}</text>
				</view>
			</view>
			<!-- 优惠信息 -->
			<view v-show="currentTab === 3" class="tab-content">
				<view class="discount-card" v-for="(d, i) in discounts" :key="i">
					<text class="discount-title">{{ d.title }}</text>
					<text class="discount-desc">{{ d.desc }}</text>
					<text class="discount-valid">有效期至 {{ d.valid }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			currentTab: 0,
			tabs: ['演出列表', '排班表', 'SD信息', '优惠信息'],
			performanceList: [
				{ id: 1, name: '《哈姆雷特》', venue: '国家大剧院', time: '2024年3月1日 19:30', price: '¥180起' },
				{ id: 2, name: '《天鹅湖》', venue: '北京舞蹈学院', time: '2024年3月2日 19:00', price: '¥280起' },
				{ id: 3, name: '《茶花女》', venue: '中央歌剧院', time: '2024年3月3日 19:30', price: '¥380起' },
				{ id: 4, name: '《罗密欧与朱丽叶》', venue: '北京人艺', time: '2024年3月5日 19:30', price: '¥220起' },
				{ id: 5, name: '《歌剧魅影》', venue: '上海大剧院', time: '2024年3月8日 19:30', price: '¥320起' }
			],
			scheduleList: [
				{ title: '国家大剧院 · 3月排班', theater: '国家大剧院', date: '2024-03-01 ~ 03-31' },
				{ title: '北京人艺 · 3月排班', theater: '北京人艺', date: '2024-03-01 ~ 03-31' }
			],
			sdList: [
				{ actor: '演员A', show: '《哈姆雷特》', date: '2024-03-01', hasSD: true },
				{ actor: '演员B', show: '《天鹅湖》', date: '2024-03-02', hasSD: false }
			],
			discounts: [
				{ title: '学生票 8折', desc: '凭学生证购票享8折优惠', valid: '2024-12-31' },
				{ title: '首演特惠', desc: '首演场次立减30元', valid: '2024-06-30' }
			]
		}
	},
	methods: {
		goDetail(id) {
			uni.navigateTo({ url: `/pages/performance-detail/performance-detail?id=${id}` }).catch(() => {})
		}
	}
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f5f5; }
.tabs { display: flex; background: #fff; padding: 0 24rpx; }
.tab { flex: 1; text-align: center; padding: 28rpx 0; font-size: 28rpx; color: #666; }
.tab.active { color: #6366f1; font-weight: bold; border-bottom: 4rpx solid #6366f1; }
.content { padding: 24rpx; }
.tab-content { padding-bottom: 40rpx; }
.perf-item { background: #fff; border-radius: 16rpx; padding: 28rpx; margin-bottom: 20rpx; box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05); }
.perf-item-title { font-size: 32rpx; font-weight: bold; color: #333; display: block; }
.perf-item-venue { font-size: 26rpx; color: #666; display: block; margin-top: 12rpx; }
.perf-item-time { font-size: 26rpx; color: #888; display: block; margin-top: 8rpx; }
.perf-item-price { font-size: 30rpx; font-weight: bold; color: #6366f1; display: block; margin-top: 12rpx; }
.schedule-tip, .sd-tip { font-size: 26rpx; color: #888; margin-bottom: 24rpx; }
.schedule-card, .sd-card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.schedule-title, .sd-name { font-size: 30rpx; font-weight: bold; display: block; }
.schedule-info, .sd-show, .sd-date { font-size: 26rpx; color: #666; display: block; margin-top: 8rpx; }
.sd-status { display: block; margin-top: 8rpx; font-size: 26rpx; }
.sd-status.yes { color: #22c55e; }
.sd-status.no { color: #999; }
.discount-card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; border-left: 6rpx solid #6366f1; }
.discount-title { font-size: 30rpx; font-weight: bold; display: block; }
.discount-desc { font-size: 26rpx; color: #666; display: block; margin-top: 8rpx; }
.discount-valid { font-size: 24rpx; color: #999; display: block; margin-top: 8rpx; }
</style>
