<template>
	<view class="page">
		<view class="header">
			<text class="title">AI 生成</text>
			<text class="desc">基于观剧体验与剧目信息，生成观后感、剧情分析、演员评价及文创设计</text>
		</view>
		<view class="section">
			<text class="section-title">选择生成类型</text>
			<view class="type-list">
				<view :class="['type-item', selectedType===t.key?'active':'']" v-for="t in types" :key="t.key" @click="selectedType=t.key">
					<text class="type-icon">{{ t.icon }}</text>
					<text class="type-name">{{ t.name }}</text>
					<text class="type-desc">{{ t.desc }}</text>
				</view>
			</view>
		</view>
		<view class="section">
			<text class="section-title">选择剧目（可选）</text>
			<picker mode="selector" :range="playOptions" range-key="name" @change="onPlayChange">
				<view class="picker">{{ selectedPlay ? selectedPlay.name : '请选择关联剧目' }}</view>
			</picker>
		</view>
		<view class="section">
			<text class="section-title">补充说明（可选）</text>
			<textarea class="textarea" v-model="userInput" placeholder="例如：重点写第三幕的观感、希望风格偏文艺..." maxlength="200" />
		</view>
		<view class="btn-generate" @click="generate">生成</view>
		<view class="section" v-if="resultText">
			<text class="section-title">生成结果</text>
			<view class="result-box">
				<text class="result-text">{{ resultText }}</text>
				<image v-if="resultImageUrl" class="result-image" :src="resultImageUrl" mode="widthFix" />
				<text v-if="resultMock" class="result-hint">（当前为后端演示文案，配置大模型密钥后为真实生成）</text>
			</view>
		</view>
		<view class="section">
			<text class="section-title">生成历史</text>
			<view class="history-list">
				<view class="history-item" v-for="(h, i) in historyList" :key="i" @click="viewHistory(h)">
					<text class="history-type">{{ h.typeName }}</text>
					<text class="history-play">{{ h.playName || '未关联剧目' }}</text>
					<text class="history-time">{{ h.time }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import apiConfig from '../../config/index.js'

export default {
	data() {
		return {
			resultText: '',
			resultImageUrl: '',
			resultMock: false,
			selectedType: 'review',
			userInput: '',
			selectedPlay: null,
			playOptions: [
				{ id: 1, name: '《哈姆雷特》' },
				{ id: 2, name: '《天鹅湖》' },
				{ id: 3, name: '《茶花女》' },
				{ id: 4, name: '《罗密欧与朱丽叶》' }
			],
			types: [
				{ key: 'review', icon: '📝', name: '观后感', desc: '根据观剧体验生成观后感' },
				{ key: 'plot', icon: '📖', name: '剧情分析', desc: '剧情结构、主题与人物分析' },
				{ key: 'actor', icon: '👤', name: '演员评价', desc: '对演员表演的点评与印象' },
				{ key: 'poster', icon: '🖼', name: '文创海报', desc: '生成剧目主题海报或周边设计' },
				{ key: 'merch', icon: '🎁', name: '周边设计', desc: '纪念品、明信片等文创创意' }
			],
			historyList: [
				{ typeName: '观后感', playName: '《哈姆雷特》', time: '2024-01-15 20:30' },
				{ typeName: '剧情分析', playName: '《天鹅湖》', time: '2024-01-10 18:00' }
			]
		}
	},
	methods: {
		onPlayChange(e) {
			const i = e.detail.value
			this.selectedPlay = this.playOptions[i] || null
		},
		generate() {
			const type = this.types.find(t => t.key === this.selectedType)
			const key = this.selectedType
			uni.showLoading({ title: '生成中...' })
			const base = apiConfig.baseURL.replace(/\/$/, '')
			const isCreative = key === 'poster' || key === 'merch'
			uni.request({
				url: isCreative ? `${base}/ai/creative` : `${base}/ai/generate-text`,
				method: 'POST',
				header: { 'Content-Type': 'application/json' },
				data: isCreative
					? {
						type: key,
						playId: this.selectedPlay ? this.selectedPlay.id : undefined,
						style: this.userInput || undefined
					}
					: {
						type: key,
						playId: this.selectedPlay ? this.selectedPlay.id : undefined,
						userInput: this.userInput || undefined
					},
				success: (res) => {
					const body = res.data
					if (body && String(body.code) === '200' && body.data) {
						if (isCreative) {
							this.resultText = body.data.prompt || '已生成文创图片'
							this.resultImageUrl = body.data.imageUrl || ''
						} else {
							this.resultText = body.data.text || ''
							this.resultImageUrl = ''
						}
						this.resultMock = !!body.data.mock
						this.historyList.unshift({
							typeName: type ? type.name : 'AI生成',
							playName: this.selectedPlay ? this.selectedPlay.name : null,
							time: new Date().toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
						})
						if (this.resultMock) {
							uni.showToast({ title: '已返回（演示文案）', icon: 'none' })
						} else {
							uni.showToast({ title: '生成完成', icon: 'success' })
						}
					} else {
						const msg = (body && body.msg) || '生成失败'
						uni.showToast({ title: msg, icon: 'none' })
					}
				},
				fail: () => {
					uni.showToast({ title: '网络错误，请确认后端已启动', icon: 'none' })
				},
				complete: () => {
					uni.hideLoading()
				}
			})
		},
		viewHistory(h) {
			uni.showToast({ title: '查看: ' + h.typeName, icon: 'none' })
		}
	}
}
</script>

<style scoped>
.page { padding: 24rpx; padding-bottom: 48rpx; min-height: 100vh; background: #f5f5f5; }
.header { margin-bottom: 32rpx; }
.title { font-size: 38rpx; font-weight: bold; color: #333; display: block; margin-bottom: 12rpx; }
.desc { font-size: 26rpx; color: #666; line-height: 1.5; display: block; }
.section { margin-bottom: 36rpx; }
.section-title { font-size: 28rpx; font-weight: bold; color: #333; display: block; margin-bottom: 20rpx; }
.type-list { display: flex; flex-direction: column; gap: 16rpx; }
.type-item { background: #fff; border-radius: 16rpx; padding: 24rpx; border: 2rpx solid transparent; }
.type-item.active { border-color: #6366f1; background: rgba(99,102,241,0.06); }
.type-icon { font-size: 36rpx; margin-right: 16rpx; }
.type-name { font-size: 30rpx; font-weight: bold; color: #333; }
.type-desc { font-size: 24rpx; color: #888; display: block; margin-top: 8rpx; margin-left: 52rpx; }
.picker { background: #fff; padding: 24rpx; border-radius: 16rpx; font-size: 28rpx; color: #333; }
.textarea { background: #fff; width: 100%; min-height: 160rpx; padding: 24rpx; border-radius: 16rpx; font-size: 28rpx; box-sizing: border-box; }
.btn-generate { background: #6366f1; color: #fff; text-align: center; padding: 28rpx; border-radius: 16rpx; font-size: 30rpx; font-weight: 500; margin-bottom: 32rpx; }
.history-list { display: flex; flex-direction: column; gap: 16rpx; }
.history-item { background: #fff; border-radius: 16rpx; padding: 24rpx; }
.history-type { font-size: 28rpx; font-weight: bold; color: #333; display: block; }
.history-play { font-size: 26rpx; color: #666; display: block; margin-top: 6rpx; }
.history-time { font-size: 24rpx; color: #999; display: block; margin-top: 6rpx; }
.result-box { background: #fff; border-radius: 16rpx; padding: 24rpx; }
.result-text { font-size: 28rpx; color: #333; line-height: 1.65; white-space: pre-wrap; }
.result-image { width: 100%; border-radius: 16rpx; margin-top: 20rpx; }
.result-hint { font-size: 24rpx; color: #999; display: block; margin-top: 16rpx; }
</style>
