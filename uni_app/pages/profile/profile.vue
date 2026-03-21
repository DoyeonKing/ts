<template>
	<view class="page">
		<view class="tabs">
			<view v-for="(t, i) in tabs" :key="i" :class="['tab', currentTab === i ? 'active' : '']" @click="currentTab = i">
				<text>{{ t }}</text>
			</view>
		</view>
		<view class="content">
			<!-- 个人 -->
			<view v-show="currentTab === 0" class="tab-content">
				<view class="user-card">
					<view class="user-avatar">👤</view>
					<text class="user-name">剧场达人</text>
					<text class="user-desc">热爱戏剧艺术，分享观剧体验</text>
					<view class="user-stats">
						<view class="stat"><text class="stat-num">25</text><text class="stat-label">观剧次数</text></view>
						<view class="stat"><text class="stat-num">12</text><text class="stat-label">发布日志</text></view>
						<view class="stat"><text class="stat-num">8</text><text class="stat-label">AI生成</text></view>
						<view class="stat"><text class="stat-num">15</text><text class="stat-label">盘票记录</text></view>
					</view>
				</view>
				<view class="menu-title">📱 功能菜单</view>
				<view v-for="(m, i) in menuItems" :key="i" class="menu-item" @click="onMenuClick(m)">
					<view class="menu-icon">{{ m.icon }}</view>
					<view class="menu-info">
						<text class="menu-title-text">{{ m.title }}</text>
						<text class="menu-desc">{{ m.desc }}</text>
					</view>
					<text class="menu-arrow">›</text>
				</view>
			</view>
			<!-- 日志 -->
			<view v-show="currentTab === 1" class="tab-content">
				<view class="sub-title">剧场日志</view>
				<view v-for="(log, i) in logs" :key="i" class="log-card" @click="goLogDetail(log.id)">
					<view class="log-head">
						<text class="log-play">{{ log.playName }}</text>
						<text class="log-rating">{{ log.rating }}</text>
					</view>
					<text class="log-time">观剧时间: {{ log.date }}</text>
					<text class="log-venue">剧场: {{ log.venue }}</text>
					<text class="log-content">{{ log.content }}</text>
				</view>
			</view>
			<!-- AI生成 -->
			<view v-show="currentTab === 2" class="tab-content">
				<view class="sub-title">AI生成</view>
				<view v-for="(item, i) in aiList" :key="i" class="ai-card" @click="goAIDetail(item.id)">
					<text class="ai-title">{{ item.title }}</text>
					<text class="ai-time">生成时间: {{ item.date }}</text>
					<text class="ai-preview">{{ item.preview }}</text>
				</view>
			</view>
			<!-- 盘票 -->
			<view v-show="currentTab === 3" class="tab-content">
				<view class="sub-title">盘票记录</view>
				<view class="ticket-tip">求票、转票、交易记录</view>
				<view v-for="(t, i) in ticketList" :key="i" class="ticket-card" @click="goTicketDetail(t.id)">
					<text class="ticket-title">{{ t.showName }}</text>
					<text class="ticket-type">{{ t.type }} · {{ t.date }}</text>
					<text class="ticket-status" :class="t.status">{{ t.statusText }}</text>
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
			tabs: ['个人', '日志', 'AI生成', '盘票'],
			menuItems: [
				{ icon: '📋', title: '我的订单', desc: '查看购票订单' },
				{ icon: '❤', title: '我的收藏', desc: '收藏的演出和剧目' },
				{ icon: '🕐', title: '观看历史', desc: '历史观剧记录' },
				{ icon: '⭐', title: '积分中心', desc: '积分兑换和奖励' },
				{ icon: '💬', title: '客服中心', desc: '联系客服' },
				{ icon: '📝', title: '意见反馈', desc: '提交建议和反馈' }
			],
			logs: [
				{ id: 1, playName: '《哈姆雷特》', rating: '★★★★☆', date: '2024年1月1日', venue: '国家大剧院', content: '这是一次非常精彩的观剧体验，演员的表演非常到位...' },
				{ id: 2, playName: '《天鹅湖》', rating: '★★★★★', date: '2024年1月5日', venue: '北京舞蹈学院', content: '芭蕾舞美轮美奂，强烈推荐...' }
			],
			aiList: [
				{ id: 1, title: 'AI生成 观后感', date: '2024年1月1日', preview: '观剧后的AI生成内容预览...' },
				{ id: 2, title: 'AI生成 海报', date: '2024年1月3日', preview: '文创海报设计...' }
			],
			ticketList: [
				{ id: 1, showName: '《哈姆雷特》', type: '求票', date: '2024-03-01', status: 'pending', statusText: '进行中' },
				{ id: 2, showName: '《天鹅湖》', type: '转票', date: '2024-03-05', status: 'done', statusText: '已完成' }
			]
		}
	},
	methods: {
		onMenuClick(m) {},
		goLogDetail(id) { uni.navigateTo({ url: `/pages/log-detail/log-detail?id=${id}` }).catch(() => {}) },
		goAIDetail(id) { uni.navigateTo({ url: `/pages/ai-generation/ai-generation?id=${id}` }).catch(() => {}) },
		goTicketDetail(id) { uni.navigateTo({ url: `/pages/ticket-exchange/ticket-exchange` }).catch(() => {}) }
	}
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f5f5; }
.tabs { display: flex; background: #fff; }
.tab { flex: 1; text-align: center; padding: 28rpx 0; font-size: 28rpx; color: #666; }
.tab.active { color: #6366f1; font-weight: bold; border-bottom: 4rpx solid #6366f1; }
.content { padding: 24rpx; padding-bottom: 40rpx; }
.tab-content { padding-bottom: 24rpx; }
.user-card { background: linear-gradient(180deg, rgba(99,102,241,0.2) 0%, #fff 100%); border-radius: 32rpx; padding: 48rpx; text-align: center; margin-bottom: 32rpx; box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.06); }
.user-avatar { width: 160rpx; height: 160rpx; margin: 0 auto 32rpx; background: rgba(99,102,241,0.2); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 80rpx; }
.user-name { font-size: 40rpx; font-weight: bold; color: #333; display: block; }
.user-desc { font-size: 28rpx; color: #666; display: block; margin-top: 12rpx; }
.user-stats { display: flex; justify-content: space-around; margin-top: 40rpx; }
.stat { display: flex; flex-direction: column; align-items: center; }
.stat-num { font-size: 36rpx; font-weight: bold; color: #6366f1; }
.stat-label { font-size: 24rpx; color: #888; margin-top: 8rpx; }
.menu-title { font-size: 32rpx; font-weight: bold; margin-bottom: 24rpx; color: #333; }
.menu-item { display: flex; align-items: center; background: #fff; border-radius: 20rpx; padding: 28rpx; margin-bottom: 20rpx; box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05); }
.menu-icon { width: 72rpx; height: 72rpx; background: rgba(99,102,241,0.15); border-radius: 16rpx; display: flex; align-items: center; justify-content: center; font-size: 36rpx; margin-right: 24rpx; }
.menu-info { flex: 1; }
.menu-title-text { font-size: 30rpx; font-weight: 500; color: #333; display: block; }
.menu-desc { font-size: 26rpx; color: #888; display: block; margin-top: 6rpx; }
.menu-arrow { font-size: 36rpx; color: #999; }
.sub-title { font-size: 36rpx; font-weight: bold; margin-bottom: 24rpx; color: #333; }
.log-card, .ai-card, .ticket-card { background: #fff; border-radius: 20rpx; padding: 28rpx; margin-bottom: 20rpx; box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05); }
.log-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.log-play { font-size: 30rpx; font-weight: bold; color: #333; }
.log-rating { font-size: 26rpx; color: #6366f1; }
.log-time, .log-venue { font-size: 26rpx; color: #666; display: block; margin-top: 8rpx; }
.log-content { font-size: 26rpx; color: #888; display: block; margin-top: 12rpx; }
.ai-title { font-size: 30rpx; font-weight: bold; display: block; }
.ai-time { font-size: 26rpx; color: #666; display: block; margin-top: 8rpx; }
.ai-preview { font-size: 26rpx; color: #888; display: block; margin-top: 8rpx; }
.ticket-tip { font-size: 26rpx; color: #888; margin-bottom: 24rpx; }
.ticket-title { font-size: 30rpx; font-weight: bold; display: block; }
.ticket-type { font-size: 26rpx; color: #666; display: block; margin-top: 8rpx; }
.ticket-status { font-size: 26rpx; display: block; margin-top: 8rpx; }
.ticket-status.pending { color: #f59e0b; }
.ticket-status.done { color: #22c55e; }
</style>
