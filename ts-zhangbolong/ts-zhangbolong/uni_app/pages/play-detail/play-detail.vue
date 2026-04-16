<template>
	<view class="page">
		<view class="card head">
			<text class="title">{{ detail.name || '剧目详情' }}</text>
			<text class="meta">{{ detail.genre || '-' }} · {{ detail.rating || '★★★★★' }}</text>
			<view class="tags" v-if="detail.tags && detail.tags.length">
				<text class="tag" v-for="(t, i) in detail.tags" :key="i">{{ t }}</text>
			</view>
		</view>
		<view class="block">
			<text class="block-title">背景剧情</text>
			<text class="block-content">{{ detail.plot || detail.desc || '暂无剧情简介。' }}</text>
		</view>
		<view class="block" v-if="detail.characters && detail.characters.length">
			<text class="block-title">主要人物</text>
			<view class="char-list">
				<view class="char-item" v-for="(c, i) in detail.characters" :key="i">
					<text class="char-name">{{ c.name }}</text>
					<text class="char-desc">{{ c.desc }}</text>
				</view>
			</view>
		</view>
		<view class="block" v-if="detail.actors && detail.actors.length">
			<text class="block-title">相关演员</text>
			<view class="actor-list">
				<view class="actor-item" v-for="(a, i) in detail.actors" :key="i" @click="goActor(a.id)">
					<text class="actor-name">{{ a.name }}</text>
					<text class="actor-role" v-if="a.role">饰 {{ a.role }}</text>
				</view>
			</view>
		</view>
		<!-- 智能推荐：根据当前剧目推荐相似剧目与相关演员 -->
		<view class="block" v-if="recommendations.plays.length || recommendations.actors.length">
			<text class="block-title">猜你喜欢</text>
			<view class="rec-plays" v-if="recommendations.plays.length">
				<text class="rec-label">相似剧目</text>
				<view class="rec-list">
					<view class="rec-item" v-for="(p, i) in recommendations.plays" :key="i" @click="goPlay(p.id)">
						<text class="rec-name">{{ p.name }}</text>
						<text class="rec-reason">{{ p.reason }}</text>
					</view>
				</view>
			</view>
			<view class="rec-actors" v-if="recommendations.actors.length">
				<text class="rec-label">相关演员</text>
				<view class="rec-list">
					<view class="rec-item" v-for="(a, i) in recommendations.actors" :key="i" @click="goActor(a.id)">
						<text class="rec-name">{{ a.name }}</text>
						<text class="rec-reason">{{ a.reason }}</text>
					</view>
				</view>
			</view>
		</view>
		<view class="btn" @click="goPerformances">查看演出与购票</view>
	</view>
</template>

<script>
import { getRecommendationsForPlay } from '../../api/recommendation.js'
const playMap = {
	'1': { id: 1, name: '《哈姆雷特》', genre: '悲剧', rating: '★★★★★', tags: ['莎士比亚', '四大悲剧'], plot: '丹麦王子哈姆雷特在父王被害、叔父篡位、母亲改嫁的境遇下，为父复仇过程中陷入延宕与抉择，最终与仇人同归于尽的悲剧。', characters: [{ name: '哈姆雷特', desc: '丹麦王子，为父复仇' }, { name: '克劳狄斯', desc: '叔父，篡位者' }, { name: '奥菲利亚', desc: '哈姆雷特恋人' }], actors: [{ id: 1, name: '濮存昕', role: '哈姆雷特' }] },
	'2': { id: 2, name: '《罗密欧与朱丽叶》', genre: '爱情悲剧', rating: '★★★★★', tags: ['莎士比亚', '爱情'], plot: '维罗纳城两大家族蒙太古与凯普莱特世仇之下，青年罗密欧与朱丽叶一见钟情、私定终身，最终因误会双双殉情，换来两族和解。', characters: [{ name: '罗密欧', desc: '蒙太古之子' }, { name: '朱丽叶', desc: '凯普莱特之女' }], actors: [] },
	'3': { id: 3, name: '《天鹅湖》', genre: '芭蕾', rating: '★★★★☆', tags: ['柴可夫斯基', '古典芭蕾'], plot: '王子齐格弗里德与被魔王施咒的白天鹅公主奥杰塔相爱，在舞会上被黑天鹅欺骗，最终真爱战胜魔法，二人得以相守。', characters: [{ name: '奥杰塔/奥吉莉娅', desc: '白天鹅/黑天鹅' }, { name: '齐格弗里德', desc: '王子' }], actors: [] },
	'4': { id: 4, name: '《茶花女》', genre: '歌剧', rating: '★★★★★', tags: ['威尔第', '小仲马'], plot: '巴黎名妓薇奥列塔与青年阿尔弗雷多相爱，却因身份与长辈阻挠分离，最终病逝，阿尔弗雷多追悔莫及。', characters: [{ name: '薇奥列塔', desc: '茶花女' }, { name: '阿尔弗雷多', desc: '青年' }], actors: [] },
	'5': { id: 5, name: '《李尔王》', genre: '悲剧', rating: '★★★★☆', tags: ['莎士比亚', '四大悲剧'], plot: '不列颠国王李尔将国土分给善于谄媚的长女、次女，驱逐诚实的三女，最终被前二者抛弃，流落荒野，酿成悲剧。', characters: [{ name: '李尔王', desc: '不列颠国王' }, { name: '考狄利娅', desc: '三女' }], actors: [{ id: 3, name: '何冰', role: '李尔王' }] }
}
export default {
	data() {
		return {
			detail: {},
			recommendations: { plays: [], actors: [], others: [] }
		}
	},
	onLoad(opts) {
		const id = opts.id || '1'
		this.detail = playMap[id] || { id, name: '剧目', genre: '-', rating: '★★★★★', plot: '', characters: [], actors: [] }
		this.loadRecommendations(this.detail.id)
	},
	methods: {
		async loadRecommendations(playId) {
			try {
				const res = await getRecommendationsForPlay(playId)
				const data = res.data || res
				this.recommendations = {
					plays: data.plays || [],
					actors: data.actors || [],
					others: data.others || []
				}
			} catch (e) {
				this.recommendations = { plays: [], actors: [], others: [] }
			}
		},
		goPerformances() { uni.switchTab({ url: '/pages/performances/performances' }) },
		goActor(id) { uni.navigateTo({ url: `/pages/actor-detail/actor-detail?id=${id}` }).catch(() => {}) },
		goPlay(id) { uni.navigateTo({ url: `/pages/play-detail/play-detail?id=${id}` }).catch(() => {}) }
	}
}
</script>

<style scoped>
.page { padding: 24rpx; padding-bottom: 48rpx; min-height: 100vh; background: #f5f5f5; }
.card.head { background: #fff; border-radius: 24rpx; padding: 36rpx; margin-bottom: 24rpx; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06); }
.title { font-size: 38rpx; font-weight: bold; color: #333; display: block; margin-bottom: 12rpx; }
.meta { font-size: 28rpx; color: #6366f1; display: block; margin-bottom: 16rpx; }
.tags { display: flex; flex-wrap: wrap; gap: 12rpx; }
.tag { font-size: 24rpx; color: #666; background: #f0f0f0; padding: 8rpx 16rpx; border-radius: 8rpx; }
.block { background: #fff; border-radius: 20rpx; padding: 28rpx; margin-bottom: 24rpx; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06); }
.block-title { font-size: 30rpx; font-weight: bold; color: #333; display: block; margin-bottom: 16rpx; }
.block-content { font-size: 28rpx; color: #666; line-height: 1.6; display: block; }
.char-list, .actor-list { display: flex; flex-direction: column; gap: 16rpx; }
.char-item, .actor-item { padding-bottom: 16rpx; border-bottom: 1rpx solid #f0f0f0; }
.char-item:last-child, .actor-item:last-child { border-bottom: none; padding-bottom: 0; }
.char-name, .actor-name { font-size: 28rpx; font-weight: 500; color: #333; display: block; }
.char-desc, .actor-role { font-size: 26rpx; color: #666; display: block; margin-top: 4rpx; }
.btn { background: #6366f1; color: #fff; text-align: center; padding: 28rpx; border-radius: 16rpx; font-size: 30rpx; }
.rec-plays, .rec-actors { margin-bottom: 20rpx; }
.rec-plays:last-child, .rec-actors:last-child { margin-bottom: 0; }
.rec-label { font-size: 26rpx; color: #6366f1; display: block; margin-bottom: 12rpx; }
.rec-list { display: flex; flex-direction: column; gap: 12rpx; }
.rec-item { padding: 16rpx; background: #f8f8f8; border-radius: 12rpx; }
.rec-name { font-size: 28rpx; font-weight: 500; color: #333; display: block; }
.rec-reason { font-size: 24rpx; color: #888; display: block; margin-top: 4rpx; }
</style>
