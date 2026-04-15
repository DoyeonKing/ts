<template>
	<view class="page">
		<view class="hero-card">
			<text class="hero-badge">AI CREATIVE LAB</text>
			<text class="hero-title">AI 文创生成</text>
			<text class="hero-desc">先通过千问生成完整方案，再把所有模块合并成一条统一出图提示词后生成图片。</text>
		</view>

		<view class="input-card">
			<text class="section-label">创意描述</text>
			<textarea
				class="query-input"
				v-model="query"
				maxlength="200"
				placeholder="例如：我想做一个悲惨世界主题T恤，整体高级、克制、有剧场感"
			/>
			<view class="type-row">
				<view :class="['type-chip', creativeType==='merch'?'active':'']" @tap="creativeType='merch'">周边</view>
				<view :class="['type-chip', creativeType==='poster'?'active':'']" @tap="creativeType='poster'">海报</view>
			</view>
			<view class="action-row">
				<button class="ghost-btn" @tap="goRecommend">去 AI 推荐</button>
				<button class="primary-btn" @tap="submitPrompt">先生成方案</button>
			</view>
		</view>

		<view class="result-card" v-if="loading">
			<text class="loading-title">生成中...</text>
		</view>

		<view class="result-card" v-if="!loading && promptResult.analysis">
			<text class="result-title">需求分析</text>
			<text class="summary-text">{{ promptResult.analysis }}</text>
		</view>

		<view class="result-card" v-if="!loading && promptResult.designPrompt">
			<text class="result-title">平面设计图提示词</text>
			<text class="summary-text">{{ promptResult.designPrompt }}</text>
		</view>

		<view class="result-card" v-if="!loading && promptResult.productPrompt">
			<text class="result-title">成品展示图提示词</text>
			<text class="summary-text">{{ promptResult.productPrompt }}</text>
			<text class="status-text ok" v-if="promptResult.llmEnabled">当前已连接千问 API 生成</text>
			<text class="status-text warn" v-else>当前使用本地降级提示词：{{ promptResult.llmError || '未知原因' }}</text>
			<text class="status-text warn" v-if="!promptResult.llmEnabled && (promptResult.llmMissing || []).length">缺少配置：{{ (promptResult.llmMissing || []).join('，') }}</text>
		</view>

		<view class="result-card" v-if="!loading && promptResult.designAdvice">
			<text class="result-title">设计建议</text>
			<text class="summary-text">{{ promptResult.designAdvice }}</text>
		</view>

		<view class="result-card" v-if="!loading && promptResult.mergedImagePrompt">
			<text class="result-title">统一出图提示词</text>
			<text class="summary-text">{{ promptResult.mergedImagePrompt }}</text>
			<view class="action-row" style="margin-top: 12rpx;">
				<button class="primary-btn" @tap="submitImage">按统一提示词生成图片</button>
			</view>
		</view>

		<view class="result-card" v-if="!loading && !imageResult.imageUrl && imageResult.imageError">
			<text class="result-title">图片生成状态</text>
			<text class="status-text warn">{{ imageResult.imageError }}</text>
		</view>

		<view class="result-card" v-if="!loading && imageResult.imageUrl">
			<text class="result-title">生成图片</text>
			<image class="result-image" :src="imageResult.imageUrl" mode="widthFix" />
			<text class="status-text ok" v-if="!imageResult.mock">当前图片由 {{ imageResult.provider || 'dashscope' }} 生成</text>
			<text class="status-text warn" v-else>图片生成失败：{{ imageResult.imageError || '未知原因' }}</text>
		</view>

		<view class="result-card" v-if="!loading && (creativeItems || []).length">
			<text class="result-title">周边建议</text>
			<view class="creative-list">
				<view class="creative-item" v-for="(item, index) in creativeItems" :key="index">
					<view class="creative-top">
						<text class="creative-type">{{ item.type || '文创' }}</text>
						<text class="creative-name">{{ item.name || ('方案 ' + (index + 1)) }}</text>
					</view>
					<text class="creative-line">设计：{{ item.designIdea || '围绕剧场主题做图形延展。' }}</text>
					<text class="creative-line">理由：{{ item.reason || '兼顾传播与纪念属性。' }}</text>
					<text class="creative-slogan">Slogan：{{ item.slogan || '把这场戏带回生活' }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { generateCreativePrompt, generateCreativeImage } from '../../api/ai.js'

export default {
	data() {
		return {
			query: '',
			creativeType: 'merch',
			loading: false,
			promptResult: {},
			imageResult: {}
		}
	},
	computed: {
		creativeItems() {
			if (this.promptResult && this.promptResult.items && this.promptResult.items.length) return this.promptResult.items
			return []
		}
	},
	methods: {
		goRecommend() {
			uni.navigateTo({ url: '/pages/ai-generation/ai-generation' })
		},
		async submitPrompt() {
			if (!this.query.trim()) {
				uni.showToast({ title: '请输入文创需求', icon: 'none' })
				return
			}
			this.loading = true
			this.imageResult = {}
			try {
				const res = await generateCreativePrompt({ type: this.creativeType, style: this.query.trim() })
				this.promptResult = (res && res.data) ? res.data : {}
			} catch (e) {
				this.promptResult = {}
				uni.showToast({ title: '方案生成失败', icon: 'none' })
			} finally {
				this.loading = false
			}
		},
		async submitImage() {
			const prompt = (this.promptResult && this.promptResult.mergedImagePrompt) ? this.promptResult.mergedImagePrompt : ''
			if (!prompt) {
				uni.showToast({ title: '请先生成统一出图提示词', icon: 'none' })
				return
			}
			this.loading = true
			this.imageResult = {}
			try {
				const res = await generateCreativeImage({ prompt, type: this.creativeType })
				this.imageResult = (res && res.data) ? res.data : {}
			} catch (e) {
				this.imageResult = {}
				uni.showToast({ title: '图片生成失败', icon: 'none' })
			} finally {
				this.loading = false
			}
		}
	},
	onLoad(opts) {
		if (opts && opts.query) this.query = decodeURIComponent(opts.query)
	}
}
</script>

<style scoped>
.page { min-height: 100vh; padding: 24rpx; background: linear-gradient(180deg, #1a1024 0%, #221630 100%); }
.hero-card,.input-card,.result-card { background: rgba(25,16,34,0.82); border: 1rpx solid rgba(251,191,36,0.14); border-radius: 26rpx; padding: 26rpx; margin-bottom: 22rpx; }
.hero-badge { display: inline-block; padding: 8rpx 16rpx; border-radius: 999rpx; background: rgba(251,191,36,0.14); color: #fde68a; font-size: 20rpx; margin-bottom: 12rpx; }
.hero-title { display: block; font-size: 42rpx; color: #fff7ed; font-weight: 700; margin-bottom: 8rpx; }
.hero-desc { display: block; font-size: 25rpx; color: #f3e8ff; line-height: 1.7; }
.section-label,.result-title,.loading-title { color: #fff7ed; font-size: 30rpx; font-weight: 700; }
.query-input { width: 100%; min-height: 180rpx; margin-top: 16rpx; padding: 20rpx; border-radius: 16rpx; box-sizing: border-box; background: rgba(43,29,58,0.92); color: #fff7ed; }
.type-row { display: flex; gap: 12rpx; margin-top: 14rpx; }
.type-chip { flex: 1; text-align: center; padding: 16rpx 0; border-radius: 14rpx; color: #f3e8ff; background: rgba(217,70,239,0.1); border: 1rpx solid rgba(217,70,239,0.16); }
.type-chip.active { color: #1f1227; background: linear-gradient(135deg, #facc15 0%, #fb7185 52%, #c084fc 100%); }
.action-row { display: flex; gap: 14rpx; margin-top: 18rpx; }
button::after { border: none; }
.ghost-btn,.primary-btn { flex: 1; text-align: center; padding: 20rpx 0; border-radius: 16rpx; font-size: 24rpx; font-weight: 600; line-height: 1; }
.ghost-btn { color: #f5d0fe; background: rgba(217,70,239,0.08); border: 1rpx solid rgba(217,70,239,0.16); }
.primary-btn { color: #1f1227; background: linear-gradient(135deg, #facc15 0%, #fb7185 52%, #c084fc 100%); }
.summary-text { display: block; margin-top: 10rpx; color: #f3e8ff; font-size: 24rpx; line-height: 1.7; white-space: pre-wrap; }
.status-text { display: block; margin-top: 10rpx; font-size: 22rpx; line-height: 1.6; }
.status-text.ok { color: #bbf7d0; }
.status-text.warn { color: #fdba74; }
.result-image { width: 100%; border-radius: 16rpx; margin-top: 16rpx; }
.creative-list { display: flex; flex-direction: column; gap: 14rpx; margin-top: 14rpx; }
.creative-item { padding: 20rpx; border-radius: 18rpx; background: rgba(43,29,58,0.9); border: 1rpx solid rgba(251,191,36,0.16); }
.creative-top { display: flex; align-items: center; gap: 10rpx; margin-bottom: 8rpx; }
.creative-type { padding: 6rpx 12rpx; border-radius: 999rpx; font-size: 20rpx; color: #2a1605; background: #fde68a; }
.creative-name { font-size: 28rpx; color: #fff7ed; font-weight: 700; }
.creative-line,.creative-slogan { display: block; margin-top: 8rpx; font-size: 24rpx; line-height: 1.7; color: #f3e8ff; }
.creative-slogan { color: #fbcfe8; }
</style>
