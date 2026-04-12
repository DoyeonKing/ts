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

		<view class="block performance-block">
			<view class="performance-head">
				<text class="block-title">正在上演</text>
				<text class="performance-status" :class="hasPerformances ? 'on' : 'off'">{{ hasPerformances ? '有在售场次' : '暂无在售场次' }}</text>
			</view>
			<view v-if="performanceLoading" class="performance-loading">正在查询演出安排...</view>
			<view v-else-if="hasPerformances" class="performance-list">
				<view class="performance-item" v-for="item in performances" :key="item.id" @click="goPerformance(item.id)">
					<view class="performance-main">
						<text class="performance-venue">{{ item.venueName }}</text>
						<text class="performance-time">{{ formatDateTime(item.startTime) }}</text>
						<text class="performance-city">{{ item.city }} · {{ formatPrice(item.minPrice, item.maxPrice) }}</text>
					</view>
					<text class="performance-arrow">›</text>
				</view>
			</view>
			<view v-else class="performance-empty">
				<text class="performance-empty-text">当前这个剧目还没有可跳转的演出场次。</text>
			</view>
		</view>

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
	</view>
</template>

<script>
import { getRecommendationsForPlay } from '../../api/recommendation.js'
import { getPerformancesByPlayId } from '../../api/performance.js'

const PLAY_DETAIL_MAP = {
	'哈姆雷特': { id: 1, name: '《哈姆雷特》', genre: '悲剧', rating: '★★★★★', tags: ['莎士比亚', '四大悲剧'], plot: '丹麦王子哈姆雷特在父王被害、叔父篡位、母亲改嫁的境遇下，为父复仇过程中陷入延宕与抉择，最终与仇人同归于尽的悲剧。', characters: [{ name: '哈姆雷特', desc: '丹麦王子，为父复仇' }, { name: '克劳狄斯', desc: '叔父，篡位者' }, { name: '奥菲利亚', desc: '哈姆雷特恋人' }], actors: [{ id: 7, name: '濮存昕', role: '哈姆雷特' }, { id: 8, name: '袁泉', role: '奥菲利亚' }] },
	'李尔王': { id: 2, name: '《李尔王》', genre: '悲剧', rating: '★★★★☆', tags: ['莎士比亚', '四大悲剧'], plot: '不列颠国王李尔将国土分给善于谄媚的长女、次女，驱逐诚实的三女，最终被前二者抛弃，流落荒野，酿成悲剧。', characters: [{ name: '李尔王', desc: '不列颠国王' }, { name: '考狄利娅', desc: '三女' }], actors: [{ id: 7, name: '濮存昕', role: '李尔王' }, { id: 9, name: '何冰', role: '葛罗斯特' }] },
	'罗密欧与朱丽叶': { id: 5, name: '《罗密欧与朱丽叶》', genre: '爱情悲剧', rating: '★★★★★', tags: ['莎士比亚', '爱情'], plot: '维罗纳城两大家族蒙太古与凯普莱特世仇之下，青年罗密欧与朱丽叶一见钟情、私定终身，最终因误会双双殉情，换来两族和解。', characters: [{ name: '罗密欧', desc: '蒙太古之子' }, { name: '朱丽叶', desc: '凯普莱特之女' }], actors: [] },
	'茶馆': { id: 7, name: '《茶馆》', genre: '话剧', rating: '★★★★★', tags: ['老舍', '经典', '话剧'], plot: '裕泰茶馆中的人来人往，映照出时代风云和普通人在历史洪流中的命运起伏。', characters: [{ name: '王利发', desc: '裕泰茶馆掌柜' }, { name: '常四爷', desc: '性格鲜明的老派人物' }], actors: [{ id: 7, name: '濮存昕', role: '常四爷' }, { id: 9, name: '何冰', role: '松二爷' }] },
	'雷雨': { id: 8, name: '《雷雨》', genre: '话剧', rating: '★★★★★', tags: ['曹禺', '现实主义'], plot: '一场雷雨夜中，数个家庭的秘密和纠葛集中爆发。', characters: [{ name: '周朴园', desc: '家长式权威人物' }, { name: '繁漪', desc: '情绪复杂、压抑反抗' }], actors: [{ id: 17, name: '冯远征', role: '周朴园' }] },
	'天下第一楼': { id: 9, name: '《天下第一楼》', genre: '京味话剧', rating: '★★★★☆', tags: ['京味', '群像'], plot: '围绕老字号饭庄的兴衰，铺开一幅京味众生相。', characters: [{ name: '卢孟实', desc: '掌柜，精明能干' }], actors: [{ id: 15, name: '何冰', role: '卢孟实' }] },
	'天鹅湖': { id: 10, name: '《天鹅湖》', genre: '芭蕾', rating: '★★★★☆', tags: ['柴可夫斯基', '古典芭蕾'], plot: '王子齐格弗里德与被魔王施咒的白天鹅公主奥杰塔相爱，在舞会上被黑天鹅欺骗，最终真爱战胜魔法，二人得以相守。', characters: [{ name: '奥杰塔/奥吉莉娅', desc: '白天鹅/黑天鹅' }, { name: '齐格弗里德', desc: '王子' }], actors: [] },
	'茶花女': { id: 11, name: '《茶花女》', genre: '歌剧', rating: '★★★★★', tags: ['威尔第', '小仲马'], plot: '巴黎名妓薇奥列塔与青年阿尔弗雷多相爱，却因身份与长辈阻挠分离，最终病逝，阿尔弗雷多追悔莫及。', characters: [{ name: '薇奥列塔', desc: '茶花女' }, { name: '阿尔弗雷多', desc: '青年' }], actors: [{ id: 18, name: '廖昌永', role: '主演' }, { id: 19, name: '和慧', role: '主演' }] },
	'歌剧魅影': { id: 12, name: '《歌剧魅影》', genre: '音乐剧', rating: '★★★★★', tags: ['音乐剧', '经典'], plot: '巴黎歌剧院地下的神秘魅影，将天赋少女克里斯汀卷入爱情、控制与救赎之中。', characters: [{ name: '魅影', desc: '神秘天才音乐家' }, { name: '克里斯汀', desc: '年轻女高音' }], actors: [] },
	'悲惨世界': { id: 13, name: '《悲惨世界》', genre: '音乐剧', rating: '★★★★★', tags: ['音乐剧', '史诗'], plot: '在法国社会巨变的背景下，冉阿让、沙威和一群年轻人共同写下理想与牺牲。', characters: [{ name: '冉阿让', desc: '背负过去却不断自我救赎' }], actors: [] }
}

function normalizeName(name) {
	return decodeURIComponent(name || '').replace(/[《》]/g, '').trim()
}

function buildPlayDetail(opts = {}) {
	const rawId = opts.nodeId || opts.id || ''
	const numericId = Number(rawId)
	const byName = PLAY_DETAIL_MAP[normalizeName(opts.name)]
	if (byName) return { ...byName }
	const byId = Object.values(PLAY_DETAIL_MAP).find(item => item.id === numericId)
	if (byId) return { ...byId }
	return {
		id: Number.isFinite(numericId) && numericId > 0 ? numericId : rawId,
		name: opts.name ? `《${normalizeName(opts.name)}》` : '剧目详情',
		genre: '-',
		rating: '★★★★★',
		tags: [],
		plot: '',
		characters: [],
		actors: []
	}
}

export default {
	data() {
		return {
			detail: {},
			recommendations: { plays: [], actors: [], others: [] },
			performances: [],
			performanceLoading: false
		}
	},
	computed: {
		hasPerformances() {
			return this.performances.length > 0
		}
	},
	onLoad(opts) {
		this.detail = buildPlayDetail(opts)
		this.loadRecommendations(this.detail.id)
		this.loadPerformances(this.detail.id)
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
		async loadPerformances(playId) {
			this.performanceLoading = true
			try {
				const res = await getPerformancesByPlayId(playId)
				const data = res.data || res
				this.performances = Array.isArray(data) ? data : []
			} catch (e) {
				this.performances = []
			} finally {
				this.performanceLoading = false
			}
		},
		formatDateTime(value) {
			if (!value) return '-'
			return String(value).replace('T', ' ').slice(0, 16)
		},
		formatPrice(minPrice, maxPrice) {
			if (minPrice == null && maxPrice == null) return '票价待定'
			if (minPrice == null) return `最高¥${maxPrice}`
			if (maxPrice == null || Number(minPrice) === Number(maxPrice)) return `¥${minPrice}起`
			return `¥${minPrice}-¥${maxPrice}`
		},
		goPerformance(id) {
			uni.navigateTo({ url: `/pages/performance-detail/performance-detail?id=${id}` }).catch(() => {})
		},
		goActor(id) {
			uni.navigateTo({ url: `/pages/actor-detail/actor-detail?nodeId=${id}` }).catch(() => {})
		},
		goPlay(id) {
			uni.navigateTo({ url: `/pages/play-detail/play-detail?nodeId=${id}` }).catch(() => {})
		}
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
.char-list, .actor-list, .performance-list { display: flex; flex-direction: column; gap: 16rpx; }
.char-item, .actor-item { padding-bottom: 16rpx; border-bottom: 1rpx solid #f0f0f0; }
.char-item:last-child, .actor-item:last-child { border-bottom: none; padding-bottom: 0; }
.char-name, .actor-name { font-size: 28rpx; font-weight: 500; color: #333; display: block; }
.char-desc, .actor-role { font-size: 26rpx; color: #666; display: block; margin-top: 4rpx; }
.performance-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.performance-status { font-size: 24rpx; padding: 8rpx 16rpx; border-radius: 999rpx; }
.performance-status.on { color: #0f766e; background: rgba(16,185,129,0.12); }
.performance-status.off { color: #999; background: #f3f4f6; }
.performance-loading, .performance-empty-text { font-size: 26rpx; color: #888; }
.performance-item { display: flex; justify-content: space-between; align-items: center; padding: 20rpx; background: #f8f8f8; border-radius: 16rpx; }
.performance-main { flex: 1; min-width: 0; }
.performance-venue { font-size: 28rpx; font-weight: 600; color: #333; display: block; }
.performance-time, .performance-city { font-size: 24rpx; color: #666; display: block; margin-top: 8rpx; }
.performance-arrow { font-size: 34rpx; color: #999; margin-left: 16rpx; }
.rec-plays, .rec-actors { margin-bottom: 20rpx; }
.rec-plays:last-child, .rec-actors:last-child { margin-bottom: 0; }
.rec-label { font-size: 26rpx; color: #6366f1; display: block; margin-bottom: 12rpx; }
.rec-list { display: flex; flex-direction: column; gap: 12rpx; }
.rec-item { padding: 16rpx; background: #f8f8f8; border-radius: 12rpx; }
.rec-name { font-size: 28rpx; font-weight: 500; color: #333; display: block; }
.rec-reason { font-size: 24rpx; color: #888; display: block; margin-top: 4rpx; }
</style>
