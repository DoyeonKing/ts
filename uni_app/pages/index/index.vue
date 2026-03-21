<template>
	<view class="page">
		<!-- 搜索栏与城市 -->
		<view class="search-card">
			<view class="search-row">
				<view class="search-box" @click="goSearch">
					<text class="icon-search">🔍</text>
					<text class="search-placeholder">搜索剧目、演员、剧场...</text>
				</view>
				<view class="city-btn" @click="showCityPicker = true">
					<text class="icon-location">📍</text>
					<text class="city-text">{{ currentCity }}</text>
					<text class="icon-arrow">▼</text>
				</view>
			</view>
		</view>

		<!-- 快速功能 -->
		<view class="section">
			<text class="section-title">⚡ 快速功能</text>
			<view class="quick-desc">多剧场多场次选座购票、求票转票、AI创作与知识查询</view>
			<view class="quick-row">
				<view class="quick-item" @click="goPage('/pages/performances/performances')">
					<text class="quick-icon">🛒</text>
					<text class="quick-text">购票</text>
					<text class="quick-hint">选座·支付</text>
				</view>
				<view class="quick-item" @click="goTicketExchange">
					<text class="quick-icon">🔄</text>
					<text class="quick-text">盘票</text>
					<text class="quick-hint">求票·转票</text>
				</view>
				<view class="quick-item" @click="goAIGeneration">
					<text class="quick-icon">✨</text>
					<text class="quick-text">AI生成</text>
					<text class="quick-hint">观后感·文创</text>
				</view>
			</view>
			<view class="quick-row">
				<view class="quick-item" @click="goKnowledgeGraph">
					<text class="quick-icon">📚</text>
					<text class="quick-text">知识图谱</text>
					<text class="quick-hint">剧目·问答</text>
				</view>
				<view class="quick-item" @click="goActorKnowledge">
					<text class="quick-icon">👤</text>
					<text class="quick-text">演员资料</text>
					<text class="quick-hint">作品·奖项</text>
				</view>
				<view class="quick-item" @click="goPlayKnowledge">
					<text class="quick-icon">🎭</text>
					<text class="quick-text">剧目信息</text>
					<text class="quick-hint">剧情·人物</text>
				</view>
			</view>
		</view>

		<!-- 推荐演出 -->
		<view class="section">
			<view class="section-head">
				<text class="section-title">🎭 推荐演出</text>
				<text class="section-more" @click="goPage('/pages/performances/performances')">查看更多 ›</text>
			</view>
			<scroll-view scroll-x class="performance-scroll" show-scrollbar="false">
				<view v-for="(item, i) in recommendPerformances" :key="i" class="perf-card" @click="goPerformanceDetail(item.id)">
					<view class="perf-content">
						<text class="perf-title">{{ item.title }}</text>
						<text class="perf-theater">{{ item.theater }}</text>
						<text class="perf-desc">{{ item.description }}</text>
						<view class="perf-footer">
							<text class="perf-price">{{ item.price }}</text>
							<view class="btn-buy">立即购票</view>
						</view>
					</view>
				</view>
			</scroll-view>
		</view>

		<!-- 热门剧目 -->
		<view class="section">
			<view class="section-head">
				<text class="section-title">🔥 热门剧目</text>
				<text class="section-more" @click="goPage('/pages/performances/performances')">查看更多 ›</text>
			</view>
			<view v-for="(play, i) in popularPlays" :key="i" class="play-card" @click="goPlayDetail(play.id)">
				<view class="play-icon">⭐</view>
				<view class="play-info">
					<text class="play-title">{{ play.title }}</text>
					<text class="play-genre">{{ play.genre }}</text>
					<text class="play-desc">{{ play.description }}</text>
				</view>
				<view class="play-right">
					<text class="play-rating">{{ play.rating }}</text>
					<text class="play-arrow">›</text>
				</view>
			</view>
		</view>

		<!-- 城市选择弹窗 -->
		<view v-if="showCityPicker" class="mask" @click="showCityPicker = false">
			<view class="city-dialog" @click.stop>
				<text class="dialog-title">选择城市</text>
				<scroll-view scroll-y class="city-list">
					<view v-for="c in cities" :key="c" class="city-item" @click="selectCity(c)">
						<text>{{ c }}</text>
						<text v-if="c === currentCity" class="check">✓</text>
					</view>
				</scroll-view>
				<view class="dialog-footer">
					<text class="btn-cancel" @click="showCityPicker = false">取消</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			currentCity: '北京',
			showCityPicker: false,
			cities: ['北京', '上海', '广州', '深圳', '杭州', '南京', '成都', '西安', '武汉', '天津'],
			recommendPerformances: [
				{ id: '1', title: '《哈姆雷特》', theater: '国家大剧院', price: '¥180起', description: '莎士比亚经典悲剧' },
				{ id: '2', title: '《天鹅湖》', theater: '北京舞蹈学院', price: '¥280起', description: '柴可夫斯基芭蕾舞剧' },
				{ id: '3', title: '《茶花女》', theater: '中央歌剧院', price: '¥380起', description: '威尔第歌剧经典' }
			],
			popularPlays: [
				{ id: '1', title: '《罗密欧与朱丽叶》', genre: '爱情悲剧', rating: '★★★★★', description: '莎士比亚经典爱情故事' },
				{ id: '2', title: '《李尔王》', genre: '悲剧', rating: '★★★★☆', description: '莎士比亚四大悲剧之一' },
				{ id: '3', title: '《麦克白》', genre: '悲剧', rating: '★★★★★', description: '权力与野心的经典之作' }
			]
		}
	},
	methods: {
		goSearch() {
			uni.navigateTo({ url: '/pages/search/search' }).catch(() => {})
		},
		goPage(url) {
			uni.switchTab({ url }).catch(() => uni.navigateTo({ url }))
		},
		goPerformanceDetail(id) {
			uni.navigateTo({ url: `/pages/performance-detail/performance-detail?id=${id}` }).catch(() => {})
		},
		goPlayDetail(id) {
			uni.navigateTo({ url: `/pages/play-detail/play-detail?id=${id}` }).catch(() => {})
		},
		goTicketExchange() {
			uni.navigateTo({ url: '/pages/ticket-exchange/ticket-exchange' }).catch(() => {})
		},
		goAIGeneration() {
			uni.navigateTo({ url: '/pages/ai-generation/ai-generation' }).catch(() => {})
		},
		goKnowledgeGraph() {
			uni.navigateTo({ url: '/pages/knowledge-graph/knowledge-graph' }).catch(() => {})
		},
		goActorKnowledge() {
			uni.navigateTo({ url: '/pages/actor-knowledge/actor-knowledge' }).catch(() => {})
		},
		goPlayKnowledge() {
			uni.navigateTo({ url: '/pages/play-knowledge/play-knowledge' }).catch(() => {})
		},
		selectCity(c) {
			this.currentCity = c
			this.showCityPicker = false
		}
	}
}
</script>

<style scoped>
.page { padding: 24rpx; padding-bottom: 40rpx; background: linear-gradient(180deg, rgba(99,102,241,0.08) 0%, #f5f5f5 100%); min-height: 100vh; }
.search-card { background: #fff; border-radius: 24rpx; padding: 24rpx; margin-bottom: 32rpx; box-shadow: 0 4rpx 20rpx rgba(99,102,241,0.1); }
.search-row { display: flex; align-items: center; gap: 24rpx; }
.search-box { flex: 1; display: flex; align-items: center; gap: 16rpx; background: #f0f0f0; border-radius: 24rpx; padding: 20rpx 24rpx; }
.icon-search { font-size: 32rpx; }
.search-placeholder { color: #999; font-size: 28rpx; }
.city-btn { display: flex; align-items: center; gap: 8rpx; background: rgba(99,102,241,0.15); border-radius: 20rpx; padding: 16rpx 20rpx; }
.icon-location { font-size: 28rpx; }
.city-text { font-size: 26rpx; font-weight: 500; color: #4338ca; }
.icon-arrow { font-size: 20rpx; color: #4338ca; }
.section { margin-bottom: 40rpx; }
.section-title { font-size: 36rpx; font-weight: bold; color: #333; display: block; margin-bottom: 24rpx; }
.section-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24rpx; }
.section-more { font-size: 26rpx; color: #6366f1; }
.quick-row { display: flex; justify-content: space-around; margin-bottom: 24rpx; }
.quick-item { width: 200rpx; height: 180rpx; background: #fff; border-radius: 24rpx; display: flex; flex-direction: column; align-items: center; justify-content: center; box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.06); }
.quick-icon { font-size: 48rpx; margin-bottom: 12rpx; }
.quick-text { font-size: 24rpx; color: #333; }
.quick-hint { font-size: 20rpx; color: #999; margin-top: 6rpx; }
.quick-desc { font-size: 24rpx; color: #888; margin-bottom: 20rpx; }
.performance-scroll { white-space: nowrap; margin: 0 -24rpx; }
.perf-card { display: inline-block; width: 520rpx; height: 320rpx; background: linear-gradient(180deg, rgba(99,102,241,0.15) 0%, #fff 100%); border-radius: 24rpx; margin-right: 24rpx; margin-left: 24rpx; padding: 28rpx; box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.06); }
.perf-content { display: flex; flex-direction: column; height: 100%; justify-content: space-between; }
.perf-title { font-size: 34rpx; font-weight: bold; color: #333; }
.perf-theater { font-size: 26rpx; color: #666; margin-top: 12rpx; }
.perf-desc { font-size: 24rpx; color: #888; margin-top: 8rpx; }
.perf-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 16rpx; }
.perf-price { font-size: 32rpx; font-weight: bold; color: #6366f1; }
.btn-buy { background: #6366f1; color: #fff; font-size: 24rpx; padding: 12rpx 20rpx; border-radius: 12rpx; }
.play-card { display: flex; align-items: center; background: #fff; border-radius: 20rpx; padding: 24rpx; margin-bottom: 20rpx; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06); }
.play-icon { width: 80rpx; height: 80rpx; background: rgba(99,102,241,0.15); border-radius: 16rpx; display: flex; align-items: center; justify-content: center; font-size: 40rpx; margin-right: 24rpx; }
.play-info { flex: 1; }
.play-title { font-size: 30rpx; font-weight: bold; color: #333; display: block; }
.play-genre { font-size: 24rpx; color: #6366f1; display: block; margin-top: 6rpx; }
.play-desc { font-size: 24rpx; color: #888; display: block; margin-top: 6rpx; }
.play-right { text-align: right; }
.play-rating { font-size: 26rpx; font-weight: bold; color: #6366f1; display: block; }
.play-arrow { font-size: 32rpx; color: #999; }
.mask { position: fixed; left: 0; right: 0; top: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 100; display: flex; align-items: flex-end; }
.city-dialog { width: 100%; max-height: 60vh; background: #fff; border-radius: 24rpx 24rpx 0 0; padding: 32rpx; }
.dialog-title { font-size: 34rpx; font-weight: bold; display: block; margin-bottom: 24rpx; }
.city-list { max-height: 400rpx; }
.city-item { display: flex; justify-content: space-between; padding: 24rpx 0; border-bottom: 1rpx solid #eee; }
.check { color: #6366f1; font-weight: bold; }
.btn-cancel { display: block; text-align: center; padding: 24rpx; color: #666; }
</style>
