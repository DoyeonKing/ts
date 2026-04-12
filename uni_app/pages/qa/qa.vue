<template>
	<view class="page">
		<view class="hero">
			<text class="badge">SMART RECOMMENDATION</text>
			<text class="title">智能剧场推荐</text>
			<text class="desc">输入你的观剧偏好，系统将基于知识图谱、用户偏好、票价与场次进行融合推荐。</text>
		</view>

		<view class="card composer">
			<textarea
				v-model="query"
				class="query-input"
				maxlength="120"
				placeholder="例如：我想周末看一场演技有爆发力的话剧，预算300内，最好在北京"
			/>
			<view class="quick-list">
				<view class="quick-item" v-for="item in presets" :key="item" @click="query = item">{{ item }}</view>
			</view>
			<view class="btn" @click="submit">开始推荐</view>
		</view>

		<view v-if="result" class="result-wrap">
			<view class="card summary-card">
				<text class="section-title">解析结果</text>
				<text class="summary-text">{{ result.summary }}</text>
				<view class="intent-tags">
					<text class="intent-tag">预算：{{ result.intent.maxPrice || '不限' }}</text>
					<text class="intent-tag">城市：{{ result.intent.city || '不限' }}</text>
					<text class="intent-tag">周末：{{ result.intent.weekendOnly ? '是' : '否' }}</text>
					<text class="intent-tag" v-for="tag in result.intent.preferredTags" :key="tag">{{ tag }}</text>
				</view>
			</view>

			<view class="card strategy-card">
				<text class="section-title">算法链路</text>
				<view class="strategy-list">
					<text class="strategy-item" v-for="(item, index) in result.strategy" :key="item">{{ index + 1 }}. {{ item }}</text>
				</view>
			</view>

			<view class="plans">
				<view class="plan-card" v-for="plan in result.plans" :key="plan.playId">
					<view class="plan-head">
						<view>
							<text class="plan-title">{{ plan.playName }}</text>
							<text class="plan-score">匹配度 {{ plan.matchScore }}</text>
						</view>
						<text class="price">{{ plan.priceRange }}</text>
					</view>
					<text class="plan-desc">{{ plan.description }}</text>
					<view class="meta-grid">
						<text class="meta-item">场馆：{{ plan.venue }}</text>
						<text class="meta-item">城市：{{ plan.city }}</text>
						<text class="meta-item">场次：{{ plan.showTime }}</text>
					</view>
					<view class="reason-box">
						<text class="reason-title">推荐理由</text>
						<text class="reason-text">{{ plan.reason }}</text>
					</view>
					<view class="analysis-box">
						<text class="reason-title">分析说明</text>
						<text class="reason-text">{{ plan.analysis }}</text>
					</view>
					<view class="score-box">
						<text class="reason-title">排序得分</text>
						<text class="score-item">图谱分：{{ plan.scoreBreakdown.graphScore }}</text>
						<text class="score-item">偏好分：{{ plan.scoreBreakdown.preferenceScore }}</text>
						<text class="score-item">口碑分：{{ plan.scoreBreakdown.ratingScore }}</text>
						<text class="score-item">热度分：{{ plan.scoreBreakdown.popularityScore }}</text>
						<text class="score-item">规则分：{{ plan.scoreBreakdown.ruleScore }}</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { runSmartChat } from '../../api/ai.js'

export default {
	data() {
		return {
			query: '我想周末看一场演技有爆发力的话剧，预算300内，最好在北京',
			result: null,
			presets: [
				'我想周末看一场演技有爆发力的话剧，预算300内，最好在北京',
				'我想看经典爱情题材的演出，预算500左右',
				'推荐适合第一次进剧场看的作品'
			]
		}
	},
	methods: {
		async submit() {
			if (!this.query.trim()) {
				uni.showToast({ title: '请输入需求描述', icon: 'none' })
				return
			}
			uni.showLoading({ title: '智能分析中...' })
			try {
				const res = await runSmartChat(this.query)
				this.result = res.data
			} catch (e) {
				uni.showToast({ title: '推荐生成失败', icon: 'none' })
			} finally {
				uni.hideLoading()
			}
		}
	},
	mounted() {
		this.submit()
	}
}
</script>

<style scoped>
.page { min-height: 100vh; background: linear-gradient(180deg, #0f172a 0%, #111827 42%, #eef2ff 42%, #f8fafc 100%); padding: 28rpx; }
.hero { padding: 16rpx 8rpx 24rpx; }
.badge { display: inline-block; color: #93c5fd; font-size: 22rpx; letter-spacing: 3rpx; margin-bottom: 16rpx; }
.title { display: block; color: #f8fafc; font-size: 44rpx; font-weight: 700; margin-bottom: 12rpx; }
.desc { display: block; color: #cbd5e1; font-size: 26rpx; line-height: 1.6; }
.card { background: rgba(255,255,255,0.95); border-radius: 28rpx; padding: 28rpx; box-shadow: 0 20rpx 40rpx rgba(15,23,42,0.08); margin-bottom: 24rpx; }
.query-input { width: 100%; min-height: 180rpx; background: #eef2ff; border-radius: 20rpx; padding: 22rpx; box-sizing: border-box; font-size: 28rpx; color: #0f172a; }
.quick-list { display: flex; flex-wrap: wrap; gap: 16rpx; margin-top: 20rpx; }
.quick-item { padding: 14rpx 20rpx; border-radius: 999rpx; background: #e0e7ff; color: #3730a3; font-size: 24rpx; }
.btn { margin-top: 24rpx; text-align: center; background: linear-gradient(135deg, #2563eb, #7c3aed); color: #fff; font-size: 30rpx; font-weight: 600; padding: 24rpx; border-radius: 20rpx; }
.section-title { display: block; font-size: 30rpx; color: #0f172a; font-weight: 700; margin-bottom: 16rpx; }
.summary-text { display: block; font-size: 26rpx; color: #334155; line-height: 1.7; }
.intent-tags { display: flex; flex-wrap: wrap; gap: 14rpx; margin-top: 18rpx; }
.intent-tag { background: #dbeafe; color: #1d4ed8; padding: 10rpx 18rpx; border-radius: 999rpx; font-size: 22rpx; }
.strategy-list { display: flex; flex-direction: column; gap: 12rpx; }
.strategy-item { color: #475569; font-size: 26rpx; }
.plan-card { background: #ffffff; border-radius: 28rpx; padding: 28rpx; margin-bottom: 22rpx; box-shadow: 0 12rpx 32rpx rgba(30,41,59,0.08); }
.plan-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 16rpx; }
.plan-title { display: block; font-size: 34rpx; font-weight: 700; color: #111827; }
.plan-score { display: block; font-size: 24rpx; color: #7c3aed; margin-top: 8rpx; }
.price { color: #dc2626; font-size: 28rpx; font-weight: 700; }
.plan-desc { display: block; color: #475569; font-size: 26rpx; line-height: 1.7; margin-top: 18rpx; }
.meta-grid { display: flex; flex-direction: column; gap: 10rpx; margin-top: 18rpx; }
.meta-item { color: #334155; font-size: 24rpx; }
.reason-box, .analysis-box, .score-box { background: #f8fafc; border-radius: 20rpx; padding: 20rpx; margin-top: 18rpx; }
.reason-title { display: block; color: #0f172a; font-size: 26rpx; font-weight: 700; margin-bottom: 12rpx; }
.reason-text, .score-item { display: block; color: #475569; font-size: 24rpx; line-height: 1.7; }
</style>
