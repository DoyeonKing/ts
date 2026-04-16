<template>
	<view class="page">
		<view class="hero-card">
			<view class="hero-top">
				<text class="hero-badge">DUAL-ENGINE THEATER AI</text>
				<text class="hero-title">AI 智能剧场推荐引擎</text>
				<text class="hero-desc">输入一句自然语言需求，系统将自动完成意图解析、垂直特征生成、多路召回、RAG 上下文拼装与结构化推荐输出。</text>
			</view>
			<view class="chips">
				<text class="chip" v-for="(tip, index) in quickTips" :key="index" @click="fillExample(tip)">{{ tip }}</text>
			</view>
		</view>

		<view class="input-card">
			<text class="section-label">需求描述</text>
			<textarea
				class="query-input"
				v-model="query"
				maxlength="200"
				placeholder="例如：我想周末看一场演技张力强的话剧，预算300以内，最好在北京"
			/>
			<view class="toolbar">
				<view class="toolbar-left">
					<text class="helper-text">支持预算、时间、城市、剧种、风格等条件</text>
				</view>
				<view class="toolbar-right">
					<text class="count-text">{{ query.length }}/200</text>
				</view>
			</view>
			<view class="action-row">
				<view class="ghost-btn" @click="fillDemo">填入示例</view>
				<view class="primary-btn" @click="submitQuery">开始智能推荐</view>
			</view>
		</view>
		<view class="action-row secondary-actions">
			<view class="ghost-btn" @tap="submitAdvancedQuery">查看 LLM 推荐方案</view>
			<view class="ghost-btn" @tap="goCreativePage">AI 文创生成</view>
		</view>

		<view class="result-card" v-if="loading">
			<text class="loading-title">算法链路执行中</text>
			<view class="loading-steps">
				<view class="loading-item" v-for="(step, index) in loadingSteps" :key="index">
					<text class="loading-index">0{{ index + 1 }}</text>
					<text class="loading-text">{{ step }}</text>
				</view>
			</view>
		</view>

		<view class="result-card" v-if="submitted && (result.plans || []).length">
			<view class="result-head compact">
				<text class="result-title">融合排序分解</text>
				<text class="result-subtitle">先展示最核心的排序得分，再看具体推荐结果</text>
			</view>
			<view class="breakdown-grid top-breakdown" v-if="result.plans[0] && result.plans[0].scoreBreakdown">
				<view class="break-item" v-for="(value, key) in result.plans[0].scoreBreakdown" :key="key">
					<text class="break-key">{{ formatScoreKey(key) }}</text>
					<text class="break-value">{{ value }}</text>
				</view>
			</view>
		</view>

		<view class="result-card" v-if="submitted && result.intent">
			<view class="result-head">
				<text class="result-title">解析结果</text>
				<text class="result-subtitle">系统已将自然语言拆解为可执行约束</text>
			</view>
			<view class="intent-grid">
				<view class="intent-item">
					<text class="intent-label">预算上限</text>
					<text class="intent-value">{{ result.intent.maxPrice ? '¥' + result.intent.maxPrice : '未限定' }}</text>
				</view>
				<view class="intent-item">
					<text class="intent-label">城市</text>
					<text class="intent-value">{{ result.intent.city || '未限定' }}</text>
				</view>
				<view class="intent-item">
					<text class="intent-label">周末档期</text>
					<text class="intent-value">{{ result.intent.weekendOnly ? '是' : '否' }}</text>
				</view>
				<view class="intent-item">
					<text class="intent-label">演员偏好</text>
					<text class="intent-value">{{ result.intent.needActorFocus ? '重点考虑' : '普通' }}</text>
				</view>
			</view>
			<view class="tag-row" v-if="(result.intent.preferredTags || []).length">
				<text class="tag-title">偏好标签</text>
				<view class="tag-list">
					<text class="smart-tag" v-for="(tag, index) in result.intent.preferredTags" :key="index">{{ tag }}</text>
				</view>
			</view>
			<view class="tag-row" v-if="(result.intent.preferredTerminology || []).length">
				<text class="tag-title">术语特征</text>
				<view class="tag-list">
					<text class="smart-tag alt" v-for="(term, index) in result.intent.preferredTerminology" :key="index">{{ term }}</text>
				</view>
			</view>
		</view>

		<view class="result-card" v-if="submitted && result.verticalFeatures">
			<view class="result-head compact">
				<text class="result-title">垂直特征生成</text>
				<text class="result-subtitle">本地垂直引擎输出戏剧语义特征与专业赏析</text>
			</view>
			<view class="tag-row" v-if="(result.verticalFeatures.softTags || []).length">
				<text class="tag-title">软标签</text>
				<view class="tag-list">
					<text class="smart-tag gold" v-for="(item, index) in result.verticalFeatures.softTags" :key="index">{{ item }}</text>
				</view>
			</view>
			<view class="tag-row" v-if="(result.verticalFeatures.actorTraits || []).length">
				<text class="tag-title">演员特征</text>
				<view class="tag-list">
					<text class="smart-tag rose" v-for="(item, index) in result.verticalFeatures.actorTraits" :key="index">{{ item }}</text>
				</view>
			</view>
			<view class="tag-row" v-if="(result.verticalFeatures.reviewKeywords || []).length">
				<text class="tag-title">评论关键词</text>
				<view class="tag-list">
					<text class="smart-tag blue" v-for="(item, index) in result.verticalFeatures.reviewKeywords" :key="index">{{ item }}</text>
				</view>
			</view>
			<view class="analysis-box" v-if="result.verticalFeatures.generatedReview">
				<text class="analysis-title">专业赏析摘要</text>
				<text class="analysis-text">{{ result.verticalFeatures.generatedReview }}</text>
			</view>
			<view class="analysis-box" v-if="(result.verticalFeatures.analysisPoints || []).length">
				<text class="analysis-title">分析要点</text>
				<view class="bullet-list">
					<view class="bullet-item" v-for="(item, index) in result.verticalFeatures.analysisPoints" :key="index">
						<text class="bullet-dot"></text>
						<text class="bullet-text">{{ item }}</text>
					</view>
				</view>
			</view>
		</view>

		<view class="result-card" v-if="submitted && result.retrievalSummary">
			<view class="result-head compact">
				<text class="result-title">多路召回摘要</text>
				<text class="result-subtitle">知识图谱、偏好数据与票务约束共同参与排序</text>
			</view>
			<view class="intent-grid retrieval-grid">
				<view class="intent-item">
					<text class="intent-label">最终方案数</text>
					<text class="intent-value">{{ result.retrievalSummary.finalPlanCount || 0 }}</text>
				</view>
				<view class="intent-item">
					<text class="intent-label">召回模式</text>
					<text class="intent-value small">{{ result.retrievalSummary.retrievalMode || '智能多路召回' }}</text>
				</view>
				<view class="intent-item">
					<text class="intent-label">预算约束</text>
					<text class="intent-value">{{ result.retrievalSummary.hasBudgetConstraint ? '已启用' : '未启用' }}</text>
				</view>
				<view class="intent-item">
					<text class="intent-label">演员聚焦</text>
					<text class="intent-value">{{ result.retrievalSummary.actorFocus ? '已启用' : '未启用' }}</text>
				</view>
			</view>
		</view>

		<view class="result-card" v-if="submitted && result.algorithmMetrics">
			<view class="result-head compact">
				<text class="result-title">算法指标</text>
				<text class="result-subtitle">用于展示当前请求在意图识别、特征生成与事实约束上的完成度</text>
			</view>
			<view class="intent-grid metrics-grid">
				<view class="intent-item">
					<text class="intent-label">意图置信度</text>
					<text class="intent-value">{{ formatMetric(result.algorithmMetrics.intentConfidence) }}</text>
				</view>
				<view class="intent-item">
					<text class="intent-label">特征丰富度</text>
					<text class="intent-value">{{ formatMetric(result.algorithmMetrics.featureRichness) }}</text>
				</view>
				<view class="intent-item">
					<text class="intent-label">事实约束强度</text>
					<text class="intent-value">{{ formatMetric(result.algorithmMetrics.factConstraintLevel) }}</text>
				</view>
				<view class="intent-item">
					<text class="intent-label">方案多样性</text>
					<text class="intent-value">{{ result.algorithmMetrics.planDiversity || 0 }}</text>
				</view>
			</view>
			<view class="analysis-box">
				<text class="analysis-title">解释性状态</text>
				<text class="analysis-text">{{ result.algorithmMetrics.explainabilityReady ? '已生成可解释推荐结果，可直接用于答辩展示。' : '当前未生成可解释推荐方案，建议放宽约束后重试。' }}</text>
			</view>
		</view>

		<view class="result-card" v-if="submitted && result.algorithmTrace">
			<view class="result-head compact">
				<text class="result-title">算法追踪 Trace</text>
				<text class="result-subtitle">展示候选召回、场次过滤与最终保留数量，突出推荐过程的可解释性</text>
			</view>
			<view class="intent-grid metrics-grid">
				<view class="intent-item">
					<text class="intent-label">标签召回数</text>
					<text class="intent-value">{{ result.algorithmTrace.tagRecallHits || 0 }}</text>
				</view>
				<view class="intent-item">
					<text class="intent-label">术语召回数</text>
					<text class="intent-value">{{ result.algorithmTrace.termRecallHits || 0 }}</text>
				</view>
				<view class="intent-item">
					<text class="intent-label">偏好召回数</text>
					<text class="intent-value">{{ result.algorithmTrace.userRecallHits || 0 }}</text>
				</view>
				<view class="intent-item">
					<text class="intent-label">过滤前候选</text>
					<text class="intent-value">{{ result.algorithmTrace.candidateCountBeforeFilter || 0 }}</text>
				</view>
				<view class="intent-item">
					<text class="intent-label">可售场次数</text>
					<text class="intent-value">{{ result.algorithmTrace.availablePerformanceCount || 0 }}</text>
				</view>
				<view class="intent-item">
					<text class="intent-label">过滤淘汰数</text>
					<text class="intent-value">{{ result.algorithmTrace.filteredOutCount || 0 }}</text>
				</view>
			</view>
			<view class="analysis-box">
				<text class="analysis-title">Trace 解读</text>
				<text class="analysis-text">系统先依据标签、术语与用户偏好完成多路召回，再通过真实场次、票价和城市条件进行过滤，最后保留可售且得分更高的推荐方案。</text>
			</view>
		</view>

		<view class="result-card" v-if="submitted && (result.strategy || []).length">
			<view class="result-head compact">
				<text class="result-title">算法链路</text>
				<text class="result-subtitle">当前演示版本使用的推荐流程</text>
			</view>
			<view class="strategy-list">
				<view class="strategy-item" v-for="(item, index) in result.strategy" :key="index">
					<text class="strategy-no">{{ index + 1 }}</text>
					<text class="strategy-text">{{ item }}</text>
				</view>
			</view>
		</view>

		<view class="result-card" v-if="submitted && result.ragContext">
			<view class="result-head compact">
				<text class="result-title">RAG 上下文拼装</text>
				<text class="result-subtitle">将真实票务事实与垂直评论特征融合为受约束上下文</text>
			</view>
			<view class="analysis-box" v-if="result.ragContext.constraints">
				<text class="analysis-title">约束摘要</text>
				<text class="analysis-text">{{ result.ragContext.constraints }}</text>
			</view>
			<view class="analysis-box" v-if="result.ragContext.professionalReview">
				<text class="analysis-title">专业上下文</text>
				<text class="analysis-text">{{ result.ragContext.professionalReview }}</text>
			</view>
			<view class="fact-list" v-if="(result.ragContext.candidateFacts || []).length">
				<view class="fact-item" v-for="(fact, index) in result.ragContext.candidateFacts" :key="index">
					<view class="fact-top">
						<text class="fact-name">{{ fact.playName }}</text>
						<text class="fact-price">{{ fact.priceRange }}</text>
					</view>
					<text class="fact-meta">{{ fact.city }} · {{ fact.venue }} · {{ fact.showTime }}</text>
					<text class="fact-reason">{{ fact.reason || fact.analysis }}</text>
				</view>
			</view>
		</view>

		<view class="result-card" v-if="submitted && displayPlans.length">
			<view class="result-head compact">
				<text class="result-title">推荐方案</text>
				<text class="result-subtitle">默认直接展示多条可选方案，方便横向比较场次、票价与推荐依据</text>
			</view>
			<view class="result-overview-grid">
				<view class="break-item">
					<text class="break-key">首位推荐</text>
					<text class="break-value">{{ displayPlans[0] ? displayPlans[0].playName : '—' }}</text>
				</view>
				<view class="break-item">
					<text class="break-key">当前展示</text>
					<text class="break-value">{{ visiblePlans.length }} / {{ displayPlans.length }} 个方案</text>
				</view>
				<view class="break-item" v-if="candidatePlanCount">
					<text class="break-key">候选池规模</text>
					<text class="break-value">{{ candidatePlanCount }} 个候选</text>
				</view>
				<view class="break-item" v-if="result.retrievalSummary && result.retrievalSummary.retrievalMode">
					<text class="break-key">召回方式</text>
					<text class="break-value">{{ result.retrievalSummary.retrievalMode }}</text>
				</view>
			</view>
			<view class="plan-list">
				<view class="plan-item" v-for="(plan, index) in visiblePlans" :key="getPlanKey(plan, index)">
					<view class="plan-top">
						<view>
							<text class="plan-rank">{{ getPlanOrder(plan, index) === 0 ? '主推荐' : '备选 ' + getPlanOrder(plan, index) }}</text>
							<text class="plan-name">{{ plan.playName }}</text>
						</view>
						<view class="score-pill">匹配分 {{ plan.matchScore || '—' }}</view>
					</view>
					<text class="plan-desc">{{ plan.description || '该方案与当前需求有较高匹配度，可继续查看推荐依据。' }}</text>
					<view class="meta-grid">
						<view class="meta-item">
							<text class="meta-label">场馆</text>
							<text class="meta-value">{{ plan.venue || '待确认' }}</text>
						</view>
						<view class="meta-item">
							<text class="meta-label">时间</text>
							<text class="meta-value">{{ plan.showTime || '待确认' }}</text>
						</view>
						<view class="meta-item">
							<text class="meta-label">票价</text>
							<text class="meta-value highlight">{{ plan.priceRange || '待确认' }}</text>
						</view>
						<view class="meta-item">
							<text class="meta-label">城市</text>
							<text class="meta-value">{{ plan.city || '待确认' }}</text>
						</view>
					</view>
					<view class="reason-box">
						<text class="reason-title">推荐依据摘要</text>
						<text class="reason-text">{{ buildPlanReasonSummary(plan, getPlanOrder(plan, index)) }}</text>
					</view>
					<view class="analysis-box" v-if="plan.scoreBreakdown">
						<text class="analysis-title">分项得分</text>
						<view class="score-tag-row">
							<view class="score-tag" v-for="(value, key) in plan.scoreBreakdown" :key="key">
								<text class="score-tag-key">{{ formatScoreKey(key) }}</text>
								<text class="score-tag-value">{{ value }}</text>
							</view>
						</view>
					</view>
					<view class="analysis-toggle-row" v-if="plan.reason || plan.analysis || plan.description">
						<text class="analysis-toggle-btn" @tap="togglePlanExpand(plan, index)">{{ isPlanExpanded(plan, index) ? '收起分析依据' : '展开分析依据' }}</text>
					</view>
					<view class="analysis-box" v-if="(plan.reason || plan.analysis || plan.description) && isPlanExpanded(plan, index)">
						<text class="analysis-title">方案分析</text>
						<view class="bullet-list">
							<view class="bullet-item" v-for="(item, itemIndex) in getPlanHighlights(plan)" :key="itemIndex">
								<text class="bullet-dot"></text>
								<text class="bullet-text">{{ item }}</text>
							</view>
						</view>
					</view>
				</view>
			</view>
			<view class="more-row" v-if="hasMorePlans">
				<view class="ghost-btn mini" @tap="togglePlanList">{{ showAllPlans ? '收起更多方案' : '展开更多方案（+' + (displayPlans.length - defaultVisiblePlanCount) + '）' }}</view>
			</view>
		</view>

		<view class="result-card empty" v-if="!loading && submitted && !displayPlans.length">
			<text class="empty-title">暂无满足条件的演出</text>
			<text class="empty-desc">可以尝试放宽预算、取消周末限制，或改用“经典 / 爱情 / 话剧”等关键词重新提问。</text>
		</view>

		<view class="result-card" v-if="submitted && result.summary">
			<text class="summary-title">系统总结</text>
			<text class="summary-text">{{ result.summary }}</text>
		</view>
	</view>
</template>

<script>
import { chatWithAI, getAdvancedRecommendationPlan } from '../../api/ai.js'

export default {
	data() {
		return {
			query: '我想周末看一场演技张力强的话剧，预算300以内，最好在北京',
			loading: false,
			submitted: false,
			result: {},
			quickTips: [
				'我想看预算300以内的话剧',
				'周末想看一场北京的经典悲剧',
				'想看演员表现力强的剧目',
				'推荐一场适合第一次入坑的话剧'
			],
			loadingSteps: [
				'解析自然语言需求',
				'生成剧场垂直特征与专业赏析',
				'触发知识图谱与偏好多路召回',
				'执行预算 / 城市 / 时间过滤',
				'完成 RAG 上下文拼装与结构化输出'
			],
			defaultVisiblePlanCount: 5,
			showAllPlans: false,
			expandedPlanKeys: {}
		}
	},
	computed: {
		displayPlans() {
			const plans = Array.isArray(this.result.plans) ? this.result.plans : []
			const facts = this.result.ragContext && Array.isArray(this.result.ragContext.candidateFacts)
				? this.result.ragContext.candidateFacts
				: []
			const merged = [...plans]
			facts.forEach(fact => {
				if (!fact || !fact.playName) {
					return
				}
				const exists = merged.some(item => item && item.playName === fact.playName)
				if (!exists) {
					merged.push({
						playName: fact.playName,
						description: fact.reason || fact.analysis || '来自候选池的补充推荐方案，可作为备选继续比较。',
						venue: fact.venue,
						showTime: fact.showTime,
						priceRange: fact.priceRange,
						city: fact.city,
						reason: fact.reason,
						analysis: fact.analysis,
						matchScore: '候选'
					})
				}
			})
			return merged.slice(0, 5)
		},
		candidatePlanCount() {
			if (this.result.retrievalSummary && this.result.retrievalSummary.finalPlanCount) {
				return this.result.retrievalSummary.finalPlanCount
			}
			return this.displayPlans.length
		},
		hasMorePlans() {
			return this.displayPlans.length > this.defaultVisiblePlanCount
		},
		visiblePlans() {
			if (this.showAllPlans) {
				return this.displayPlans
			}
			return this.displayPlans.slice(0, this.defaultVisiblePlanCount)
		}
	},
	methods: {
		fillExample(text) {
			this.query = text
		},
		fillDemo() {
			this.query = '我想周末看一场演技张力强的话剧，预算300以内，最好在北京'
		},
		formatScoreKey(key) {
			const map = {
				graphScore: '图谱分',
				preferenceScore: '偏好分',
				ratingScore: '评分分',
				popularityScore: '热度分',
				ruleScore: '规则分'
			}
			return map[key] || key
		},
		formatMetric(value) {
			if (value === undefined || value === null) {
				return '0%'
			}
			if (typeof value === 'boolean') {
				return value ? '是' : '否'
			}
			if (typeof value === 'number' && value <= 1) {
				return Math.round(value * 100) + '%'
			}
			return value
		},
		getPlanOrder(plan, index) {
			const matchedIndex = this.displayPlans.findIndex(item => item && plan && item.playName === plan.playName)
			return matchedIndex >= 0 ? matchedIndex : index
		},
		getPlanKey(plan, index) {
			const order = this.getPlanOrder(plan, index)
			return (plan && plan.playName ? plan.playName : 'plan') + '-' + order
		},
		togglePlanList() {
			this.showAllPlans = !this.showAllPlans
		},
		togglePlanExpand(plan, index) {
			const key = this.getPlanKey(plan, index)
			this.$set(this.expandedPlanKeys, key, !this.expandedPlanKeys[key])
		},
		isPlanExpanded(plan, index) {
			const key = this.getPlanKey(plan, index)
			if (this.expandedPlanKeys[key] !== undefined) {
				return !!this.expandedPlanKeys[key]
			}
			return this.getPlanOrder(plan, index) === 0
		},
		getPlanHighlights(plan) {
			const rawList = [plan.reason, plan.analysis, plan.description]
			const list = rawList
				.filter(Boolean)
				.map(item => String(item).trim())
				.filter((item, index, arr) => arr.indexOf(item) === index)
			return list.length ? list.slice(0, 3) : ['该方案与当前约束整体匹配，可作为进一步决策候选。']
		},
		buildPlanReasonSummary(plan, index) {
			const summary = []
			if (index === 0) {
				summary.push('综合匹配度位于当前结果首位')
			}
			if (plan.city) {
				summary.push('覆盖城市：' + plan.city)
			}
			if (plan.priceRange) {
				summary.push('价格区间：' + plan.priceRange)
			}
			if (plan.showTime) {
				summary.push('可选场次：' + plan.showTime)
			}
			if (plan.reason) {
				summary.push(plan.reason)
			}
			return summary.slice(0, 4).join('；') || '系统基于你的预算、时间与剧目偏好生成了该推荐。'
		},
		async submitQuery() {
			if (!this.query.trim()) {
				uni.showToast({ title: '请输入推荐需求', icon: 'none' })
				return
			}
			this.loading = true
			this.submitted = true
			try {
				const res = await chatWithAI({ query: this.query.trim(), userId: 1 })
				this.result = res.data || {}
			} catch (error) {
				this.result = {}
				uni.showToast({ title: '推荐服务暂时不可用', icon: 'none' })
			} finally {
				this.loading = false
			}
		},
		async submitAdvancedQuery() {
			if (!this.query.trim()) {
				uni.showToast({ title: '请输入 LLM 推荐需求', icon: 'none' })
				return
			}
			this.loading = true
			this.submitted = true
			try {
				const res = await getAdvancedRecommendationPlan({ query: this.query.trim(), userId: 1 })
				this.result = res.data || {}
			} catch (error) {
				this.result = {}
				uni.showToast({ title: 'LLM 推荐服务暂不可用', icon: 'none' })
			} finally {
				this.loading = false
			}
		},
		goCreativePage() {
			const q = encodeURIComponent(this.query || '')
			uni.navigateTo({ url: `/pages/ai-creative/ai-creative?query=${q}` }).catch(() => {})
		}
	},
	onLoad() {
	}
}
</script>

<style scoped>
.page {
	min-height: 100vh;
	padding: 24rpx;
	padding-bottom: 56rpx;
	background:
		radial-gradient(circle at top left, rgba(251, 191, 36, 0.18), transparent 24%),
		radial-gradient(circle at top right, rgba(244, 114, 182, 0.16), transparent 24%),
		linear-gradient(180deg, #140f1c 0%, #151122 42%, #201626 100%);
}
.hero-card,
.input-card,
.result-card {
	background: rgba(20, 15, 28, 0.78);
	border: 1rpx solid rgba(240, 171, 252, 0.14);
	backdrop-filter: blur(16rpx);
	border-radius: 28rpx;
	padding: 28rpx;
	box-shadow: 0 18rpx 48rpx rgba(12, 8, 18, 0.38);
	margin-bottom: 24rpx;
}
.hero-badge {
	display: inline-block;
	padding: 8rpx 18rpx;
	border-radius: 999rpx;
	background: rgba(251, 191, 36, 0.14);
	color: #fde68a;
	font-size: 20rpx;
	letter-spacing: 2rpx;
	margin-bottom: 18rpx;
}
.hero-title {
	display: block;
	font-size: 44rpx;
	font-weight: 700;
	color: #fff7ed;
	margin-bottom: 14rpx;
}
.hero-desc {
	display: block;
	font-size: 26rpx;
	line-height: 1.7;
	color: #e9d5ff;
}
.chips {
	display: flex;
	flex-wrap: wrap;
	gap: 14rpx;
	margin-top: 24rpx;
}
.chip {
	padding: 12rpx 18rpx;
	border-radius: 999rpx;
	font-size: 22rpx;
	color: #fdf2f8;
	background: rgba(244, 114, 182, 0.14);
	border: 1rpx solid rgba(251, 113, 133, 0.2);
}
.section-label,
.result-title,
.summary-title,
.loading-title {
	display: block;
	font-size: 30rpx;
	font-weight: 700;
	color: #fff7ed;
}
.query-input {
	width: 100%;
	min-height: 220rpx;
	margin-top: 18rpx;
	padding: 24rpx;
	border-radius: 22rpx;
	background: rgba(39, 25, 46, 0.92);
	color: #fff7ed;
	font-size: 28rpx;
	box-sizing: border-box;
	border: 1rpx solid rgba(251, 191, 36, 0.18);
}
.toolbar {
	margin-top: 16rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
}
.helper-text,
.count-text,
.result-subtitle,
.empty-desc,
.summary-text {
	font-size: 22rpx;
	color: #c4b5fd;
	line-height: 1.6;
}
.action-row {
	display: flex;
	gap: 16rpx;
	margin-top: 24rpx;
}
.secondary-actions {
	margin-top: 16rpx;
}
.ghost-btn,
.primary-btn {
	flex: 1;
	text-align: center;
	padding: 24rpx 0;
	border-radius: 18rpx;
	font-size: 28rpx;
	font-weight: 600;
}
.ghost-btn {
	color: #f5d0fe;
	background: rgba(217, 70, 239, 0.08);
	border: 1rpx solid rgba(217, 70, 239, 0.16);
}
.primary-btn {
	color: #1f1227;
	background: linear-gradient(135deg, #facc15 0%, #fb7185 52%, #c084fc 100%);
}
.result-overview-grid {
	display: grid;
	grid-template-columns: repeat(2, minmax(0, 1fr));
	gap: 16rpx;
	margin-top: 18rpx;
}
.loading-steps,
.strategy-list,
.plan-list,
.fact-list {
	margin-top: 18rpx;
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}
.top-breakdown {
	margin-top: 18rpx;
}
.loading-item,
.strategy-item,
.fact-item {
	display: flex;
	align-items: center;
	gap: 18rpx;
	padding: 18rpx 20rpx;
	background: rgba(39, 25, 46, 0.72);
	border-radius: 18rpx;
}
.fact-item {
	display: block;
}
.loading-index,
.strategy-no {
	width: 44rpx;
	height: 44rpx;
	line-height: 44rpx;
	text-align: center;
	border-radius: 50%;
	background: rgba(251, 191, 36, 0.16);
	color: #fde68a;
	font-size: 22rpx;
	font-weight: 700;
}
.loading-text,
.strategy-text,
.intent-value,
.meta-value,
.reason-text,
.analysis-text,
.break-value,
.empty-title,
.fact-meta,
.fact-reason,
.bullet-text {
	color: #f3e8ff;
	font-size: 26rpx;
}
.intent-value.small {
	font-size: 22rpx;
	line-height: 1.6;
}
.result-head {
	margin-bottom: 18rpx;
}
.result-head.compact {
	margin-bottom: 12rpx;
}
.intent-grid,
.meta-grid,
.breakdown-grid {
	display: grid;
	grid-template-columns: repeat(2, minmax(0, 1fr));
	gap: 16rpx;
	margin-top: 18rpx;
}
.metrics-grid {
	margin-top: 18rpx;
}
.intent-item,
.meta-item,
.break-item {
	padding: 20rpx;
	border-radius: 20rpx;
	background: rgba(39, 25, 46, 0.76);
	border: 1rpx solid rgba(240, 171, 252, 0.1);
}
.intent-label,
.tag-title,
.meta-label,
.reason-title,
.analysis-title,
.breakdown-title,
.break-key {
	display: block;
	font-size: 22rpx;
	color: #c4b5fd;
	margin-bottom: 8rpx;
}
.tag-row {
	margin-top: 20rpx;
}
.tag-list {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
	margin-top: 12rpx;
}
.smart-tag {
	padding: 10rpx 16rpx;
	border-radius: 999rpx;
	font-size: 22rpx;
	color: #24151f;
	background: #fde68a;
}
.smart-tag.alt,
.smart-tag.blue {
	background: #bfdbfe;
}
.smart-tag.gold {
	background: #fde68a;
}
.smart-tag.rose {
	background: #fbcfe8;
}
.plan-item {
	padding: 24rpx;
	border-radius: 24rpx;
	background: linear-gradient(180deg, rgba(39, 25, 46, 0.96) 0%, rgba(24, 17, 34, 0.96) 100%);
	border: 1rpx solid rgba(251, 191, 36, 0.14);
}
.plan-top,
.fact-top {
	display: flex;
	justify-content: space-between;
	align-items: flex-start;
	gap: 20rpx;
}
.plan-rank {
	display: inline-block;
	padding: 8rpx 16rpx;
	border-radius: 999rpx;
	background: rgba(251, 191, 36, 0.14);
	color: #fde68a;
	font-size: 20rpx;
	margin-bottom: 12rpx;
}
.plan-name,
.fact-name {
	display: block;
	font-size: 34rpx;
	font-weight: 700;
	color: #fff7ed;
}
.fact-name {
	font-size: 28rpx;
}
.score-pill,
.fact-price {
	padding: 12rpx 18rpx;
	border-radius: 999rpx;
	background: rgba(244, 114, 182, 0.16);
	color: #fbcfe8;
	font-size: 22rpx;
}
.plan-desc {
	display: block;
	margin-top: 16rpx;
	font-size: 25rpx;
	line-height: 1.7;
	color: #f5d0fe;
}
.highlight {
	color: #fde68a;
	font-weight: 700;
}
.reason-box,
.analysis-box,
.breakdown-box {
	margin-top: 18rpx;
	padding: 18rpx;
	border-radius: 18rpx;
	background: rgba(18, 13, 26, 0.72);
	border: 1rpx solid rgba(240, 171, 252, 0.08);
}
.analysis-text,
.reason-text,
.summary-text,
.fact-reason {
	line-height: 1.7;
}
.analysis-toggle-row {
	margin-top: 14rpx;
}
.analysis-toggle-btn {
	display: inline-block;
	padding: 10rpx 16rpx;
	border-radius: 999rpx;
	font-size: 22rpx;
	color: #f5d0fe;
	background: rgba(217, 70, 239, 0.1);
	border: 1rpx solid rgba(217, 70, 239, 0.2);
}
.more-row {
	margin-top: 18rpx;
}
.ghost-btn.mini {
	width: 100%;
	padding: 18rpx 0;
	font-size: 24rpx;
}
.score-tag-row {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
	margin-top: 12rpx;
}
.score-tag {
	min-width: 140rpx;
	padding: 14rpx 16rpx;
	border-radius: 16rpx;
	background: rgba(39, 25, 46, 0.86);
	border: 1rpx solid rgba(250, 204, 21, 0.12);
}
.score-tag-key {
	display: block;
	font-size: 20rpx;
	color: #c4b5fd;
	margin-bottom: 8rpx;
}
.score-tag-value {
	font-size: 24rpx;
	color: #fff7ed;
	font-weight: 700;
}
.bullet-list {
	display: flex;
	flex-direction: column;
	gap: 12rpx;
}
.bullet-item {
	display: flex;
	align-items: flex-start;
	gap: 12rpx;
}
.bullet-dot {
	width: 12rpx;
	height: 12rpx;
	margin-top: 12rpx;
	border-radius: 50%;
	background: #fde68a;
	flex-shrink: 0;
}
.fact-meta {
	display: block;
	margin-top: 10rpx;
	font-size: 22rpx;
	color: #c4b5fd;
}
.fact-reason {
	display: block;
	margin-top: 10rpx;
	font-size: 24rpx;
}
.empty {
	text-align: center;
}
.empty-title {
	display: block;
	font-size: 30rpx;
	font-weight: 700;
	margin-bottom: 10rpx;
}
</style>
