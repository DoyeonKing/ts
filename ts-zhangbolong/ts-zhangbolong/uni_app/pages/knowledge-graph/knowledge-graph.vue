<template>
	<view class="page">
		<view class="header">
			<text class="title">知识图谱</text>
			<text class="desc">基于剧目、演员、术语等实体与关系的结构化知识，支持查询与智能问答</text>
		</view>
		<!-- 图谱可视化入口 -->
		<view class="graph-entry" @click="go('/pages/graph-view/graph-view')">
			<view class="graph-entry-left">
				<text class="graph-entry-title">图谱全景</text>
				<text class="graph-entry-desc">以节点连线的方式浏览剧目、演员、术语之间的关系网络</text>
			</view>
			<text class="graph-entry-arrow">›</text>
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
		goNews() { uni.showToast({ title: '新闻资讯（开发中）', icon: 'none' }) },
		goTimeline() { uni.showToast({ title: '时间线（开发中）', icon: 'none' }) },
		goQA() { uni.showToast({ title: '智能问答（开发中）', icon: 'none' }) }
	}
}
</script>

<style scoped>
.page { padding: 24rpx 24rpx 48rpx; min-height: 100vh; background: #f5f5f5; }
.header { margin-bottom: 32rpx; }
.title { font-size: 40rpx; font-weight: bold; color: #333; display: block; margin-bottom: 16rpx; }
.desc { font-size: 26rpx; color: #666; line-height: 1.5; display: block; }
.modules { display: flex; flex-direction: column; gap: 20rpx; }
.module { display: flex; align-items: center; background: #fff; border-radius: 20rpx; padding: 28rpx; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06); }
.module-icon { width: 80rpx; height: 80rpx; background: rgba(99,102,241,0.1); border-radius: 20rpx; display: flex; align-items: center; justify-content: center; font-size: 40rpx; margin-right: 24rpx; flex-shrink: 0; }
.module-body { flex: 1; min-width: 0; }
.module-title { font-size: 30rpx; font-weight: bold; color: #333; display: block; margin-bottom: 8rpx; }
.module-desc { font-size: 24rpx; color: #888; line-height: 1.4; display: block; }
.module-arrow { font-size: 32rpx; color: #999; margin-left: 16rpx; }
.module.highlight { border: 2rpx solid rgba(99,102,241,0.3); }
.graph-entry { display: flex; align-items: center; background: linear-gradient(135deg, #6366f1, #818cf8); border-radius: 20rpx; padding: 32rpx 28rpx; margin-bottom: 24rpx; box-shadow: 0 8rpx 24rpx rgba(99,102,241,0.25); }
.graph-entry-left { flex: 1; }
.graph-entry-title { font-size: 34rpx; font-weight: bold; color: #fff; display: block; margin-bottom: 8rpx; }
.graph-entry-desc { font-size: 24rpx; color: rgba(255,255,255,0.85); line-height: 1.4; display: block; }
.graph-entry-arrow { font-size: 44rpx; color: rgba(255,255,255,0.7); margin-left: 16rpx; }
</style>
