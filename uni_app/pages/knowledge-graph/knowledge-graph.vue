<template>
	<view class="page theater-page">
		<view class="ambient ambient-left"></view>
		<view class="ambient ambient-right"></view>
		<view class="header">
			<text class="eyebrow">THEATER GRAPH</text>
			<text class="title">知识图谱</text>
			<text class="desc">把剧目、演员、术语与场馆串成一张可探索的舞台关系网，查资料也能像逛展一样顺手。</text>
		</view>
		<!-- 图谱可视化入口 -->
		<view class="graph-entry" @click="go('/pages/graph-view/graph-view')">
			<view class="graph-entry-glow"></view>
			<view class="graph-entry-left">
				<text class="graph-entry-kicker">沉浸浏览</text>
				<text class="graph-entry-title">图谱全景</text>
				<text class="graph-entry-desc">用节点连线查看剧目、演员、术语与场馆之间的关系，支持搜索、筛选与聚焦展开。</text>
			</view>
			<text class="graph-entry-arrow">↗</text>
		</view>
		<view class="modules">
			<view class="module" @click="go('/pages/play-knowledge/play-knowledge')">
				<view class="module-icon">🎭</view>
				<view class="module-body">
					<text class="module-title">剧目信息</text>
					<text class="module-desc">背景剧情、人物小传、评分、标签、相关演员与演出</text>
				</view>
				<text class="module-arrow">›</text>
			</view>
			<view class="module" @click="go('/pages/actor-knowledge/actor-knowledge')">
				<view class="module-icon">👤</view>
				<view class="module-body">
					<text class="module-title">演员资料</text>
					<text class="module-desc">详细资料、代表作品、奖项、专长、参演剧目与排班</text>
				</view>
				<text class="module-arrow">›</text>
			</view>
			<view class="module" @click="go('/pages/terminology/terminology')">
				<view class="module-icon">📖</view>
				<view class="module-body">
					<text class="module-title">术语科普</text>
					<text class="module-desc">剧场专业术语解释（SD、返场、卡司、末场等）</text>
				</view>
				<text class="module-arrow">›</text>
			</view>
			<view class="module" @click="goNews">
				<view class="module-icon">📰</view>
				<view class="module-body">
					<text class="module-title">新闻资讯</text>
					<text class="module-desc">行业动态、演出资讯、剧目上新与巡演信息</text>
				</view>
				<text class="module-arrow">›</text>
			</view>
			<view class="module" @click="goTimeline">
				<view class="module-icon">📅</view>
				<view class="module-body">
					<text class="module-title">时间线</text>
					<text class="module-desc">剧目与演员发展时间线、首演与重要场次记录</text>
				</view>
				<text class="module-arrow">›</text>
			</view>
			<view class="module highlight" @click="goQA">
				<view class="module-icon">💬</view>
				<view class="module-body">
					<text class="module-title">智能问答</text>
					<text class="module-desc">基于知识图谱的自然语言问答，如「哈姆雷特的主演是谁」</text>
				</view>
				<text class="module-arrow">›</text>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	methods: {
		go(url) { uni.navigateTo({ url }).catch(() => {}) },
		goNews() { uni.navigateTo({ url: '/pages/news/news' }).catch(() => {}) },
		goTimeline() { uni.navigateTo({ url: '/pages/timeline/timeline' }).catch(() => {}) },
		goQA() { uni.navigateTo({ url: '/pages/qa/qa' }).catch(() => {}) }
	}
}
</script>

<style scoped>
.page.theater-page {
	position: relative;
	padding: 28rpx 24rpx 56rpx;
	min-height: 100vh;
	background:
		radial-gradient(circle at top left, rgba(242, 201, 76, 0.16), transparent 34%),
		radial-gradient(circle at 90% 12%, rgba(167, 46, 63, 0.32), transparent 26%),
		linear-gradient(180deg, #12090d 0%, #1c0d12 28%, #2a1218 64%, #16090d 100%);
	overflow: hidden;
}
.ambient {
	position: absolute;
	border-radius: 999rpx;
	filter: blur(36rpx);
	opacity: 0.55;
	pointer-events: none;
}
.ambient-left {
	width: 220rpx;
	height: 220rpx;
	left: -70rpx;
	top: 100rpx;
	background: rgba(242, 201, 76, 0.18);
}
.ambient-right {
	width: 260rpx;
	height: 260rpx;
	right: -80rpx;
	top: 340rpx;
	background: rgba(201, 79, 97, 0.22);
}
.header {
	position: relative;
	z-index: 2;
	margin-bottom: 30rpx;
}
.eyebrow {
	display: inline-block;
	padding: 8rpx 18rpx;
	border: 1rpx solid rgba(242, 201, 76, 0.28);
	border-radius: 999rpx;
	font-size: 20rpx;
	letter-spacing: 3rpx;
	color: rgba(242, 201, 76, 0.88);
	background: rgba(255, 255, 255, 0.04);
	margin-bottom: 18rpx;
}
.title {
	font-size: 48rpx;
	font-weight: 700;
	color: #fff7e7;
	display: block;
	margin-bottom: 16rpx;
	text-shadow: 0 8rpx 18rpx rgba(0, 0, 0, 0.24);
}
.desc {
	font-size: 26rpx;
	color: rgba(255, 240, 218, 0.78);
	line-height: 1.7;
	display: block;
	max-width: 660rpx;
}
.modules {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
	position: relative;
	z-index: 2;
}
.module {
	display: flex;
	align-items: center;
	background: rgba(255, 248, 241, 0.92);
	border: 1rpx solid rgba(255, 255, 255, 0.44);
	backdrop-filter: blur(12rpx);
	border-radius: 24rpx;
	padding: 28rpx;
	box-shadow: 0 16rpx 36rpx rgba(10, 4, 7, 0.24);
}
.module-icon {
	width: 84rpx;
	height: 84rpx;
	background: linear-gradient(135deg, rgba(113, 24, 35, 0.12), rgba(242, 201, 76, 0.24));
	border-radius: 22rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 40rpx;
	margin-right: 24rpx;
	flex-shrink: 0;
	box-shadow: inset 0 0 0 1rpx rgba(113, 24, 35, 0.06);
}
.module-body { flex: 1; min-width: 0; }
.module-title {
	font-size: 30rpx;
	font-weight: 700;
	color: #4e1821;
	display: block;
	margin-bottom: 10rpx;
}
.module-desc {
	font-size: 24rpx;
	color: #7b5d5f;
	line-height: 1.5;
	display: block;
}
.module-arrow {
	font-size: 34rpx;
	color: #8d5f42;
	margin-left: 16rpx;
}
.module.highlight {
	background: linear-gradient(135deg, rgba(255, 248, 241, 0.96), rgba(255, 236, 212, 0.94));
	border-color: rgba(242, 201, 76, 0.38);
}
.graph-entry {
	position: relative;
	display: flex;
	align-items: center;
	background: linear-gradient(135deg, rgba(83, 17, 28, 0.98), rgba(120, 29, 43, 0.96) 58%, rgba(193, 141, 75, 0.9));
	border-radius: 28rpx;
	padding: 34rpx 30rpx;
	margin-bottom: 26rpx;
	box-shadow: 0 18rpx 44rpx rgba(7, 2, 4, 0.34);
	overflow: hidden;
	z-index: 2;
}
.graph-entry-glow {
	position: absolute;
	width: 240rpx;
	height: 240rpx;
	right: -40rpx;
	top: -36rpx;
	background: radial-gradient(circle, rgba(242, 201, 76, 0.38), transparent 70%);
	pointer-events: none;
}
.graph-entry-left { flex: 1; position: relative; z-index: 1; }
.graph-entry-kicker {
	font-size: 20rpx;
	letter-spacing: 3rpx;
	color: rgba(255, 226, 165, 0.84);
	display: block;
	margin-bottom: 10rpx;
}
.graph-entry-title {
	font-size: 38rpx;
	font-weight: 700;
	color: #fff7ea;
	display: block;
	margin-bottom: 10rpx;
}
.graph-entry-desc {
	font-size: 24rpx;
	color: rgba(255, 241, 229, 0.82);
	line-height: 1.6;
	display: block;
	max-width: 520rpx;
}
.graph-entry-arrow {
	width: 72rpx;
	height: 72rpx;
	line-height: 72rpx;
	text-align: center;
	font-size: 36rpx;
	color: #fff8e9;
	background: rgba(255, 255, 255, 0.12);
	border: 1rpx solid rgba(255, 255, 255, 0.18);
	border-radius: 20rpx;
	margin-left: 18rpx;
	position: relative;
	z-index: 1;
}
</style>
