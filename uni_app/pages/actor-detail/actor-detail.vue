<template>
	<view class="page">
		<view class="card profile" v-if="actor.id">
			<view class="avatar-wrap">
				<image v-if="actor.avatar" class="avatar" :src="actor.avatar" mode="aspectFill" />
				<text v-else class="avatar-placeholder">👤</text>
			</view>
			<text class="name">{{ actor.name }}</text>
			<text class="intro" v-if="actor.intro">{{ actor.intro }}</text>
			<view class="tags" v-if="actor.awards && actor.awards.length">
				<text class="tag" v-for="(a, i) in actor.awards" :key="i">{{ a }}</text>
			</view>
			<text class="specialty" v-if="actor.specialty">专长：{{ actor.specialty }}</text>
		</view>
		<view class="block">
			<text class="block-title">代表作品</text>
			<view class="work-list">
				<view class="work-item" v-for="(w, i) in (actor.worksList || [])" :key="i" @click="goPlay(w.id)">
					<text class="work-name">{{ w.name }}</text>
					<text class="work-role" v-if="w.role">饰 {{ w.role }}</text>
				</view>
			</view>
		</view>
		<view class="block">
			<text class="block-title">近期排班</text>
			<view class="schedule-list">
				<view class="schedule-item" v-for="(s, i) in (actor.schedules || [])" :key="i">
					<text class="schedule-show">{{ s.showName }}</text>
					<text class="schedule-info">{{ s.date }} {{ s.venue }}</text>
				</view>
			</view>
		</view>
		<!-- 智能推荐：根据当前演员推荐剧目与合作演员 -->
		<view class="block" v-if="recommendations.plays.length || recommendations.actors.length">
			<text class="block-title">猜你喜欢</text>
			<view class="rec-plays" v-if="recommendations.plays.length">
				<text class="rec-label">推荐剧目</text>
				<view class="rec-list">
					<view class="rec-item" v-for="(p, i) in recommendations.plays" :key="i" @click="goPlay(p.id)">
						<text class="rec-name">{{ p.name }}</text>
						<text class="rec-reason">{{ p.reason }}</text>
					</view>
				</view>
			</view>
			<view class="rec-actors" v-if="recommendations.actors.length">
				<text class="rec-label">合作演员</text>
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
import { getRecommendationsForActor } from '../../api/recommendation.js'

const ACTOR_DETAIL_MAP = {
	'濮存昕': { id: 7, name: '濮存昕', intro: '著名话剧演员，北京人艺。', awards: ['梅花奖', '文华奖'], specialty: '话剧、莎士比亚', worksList: [{ id: 1, name: '《哈姆雷特》', role: '哈姆雷特' }, { id: 2, name: '《李尔王》', role: '李尔王' }, { id: 6, name: '《茶馆》', role: '常四爷' }], schedules: [{ showName: '《哈姆雷特》', date: '2026-04-11 19:30', venue: '国家大剧院' }] },
	'袁泉': { id: 8, name: '袁泉', intro: '演员，话剧与影视。', awards: ['梅花奖', '金狮奖'], specialty: '话剧、音乐剧', worksList: [{ id: 1, name: '《哈姆雷特》', role: '奥菲利亚' }], schedules: [] },
	'何冰': { id: 9, name: '何冰', intro: '北京人艺演员。', awards: ['梅花奖'], specialty: '话剧、喜剧', worksList: [{ id: 2, name: '《李尔王》', role: '葛罗斯特' }, { id: 6, name: '《茶馆》', role: '松二爷' }], schedules: [{ showName: '《茶馆》', date: '2026-04-13 19:30', venue: '北京人民艺术剧院' }] }
}

function normalizeName(name) {
	return decodeURIComponent(name || '').replace(/[《》]/g, '').trim()
}

function buildActorDetail(opts = {}) {
	const rawId = opts.nodeId || opts.id || ''
	const numericId = Number(rawId)
	const byName = ACTOR_DETAIL_MAP[normalizeName(opts.name)]
	if (byName) {
		return { ...byName, id: Number.isFinite(numericId) && numericId > 0 ? numericId : byName.id }
	}
	const byId = Object.values(ACTOR_DETAIL_MAP).find(item => item.id === numericId)
	if (byId) return { ...byId }
	return {
		id: Number.isFinite(numericId) && numericId > 0 ? numericId : rawId,
		name: opts.name ? normalizeName(opts.name) : '演员',
		intro: '',
		awards: [],
		specialty: '',
		worksList: [],
		schedules: []
	}
}

export default {
	data() {
		return {
			actor: {},
			recommendations: { plays: [], actors: [], others: [] }
		}
	},
	onLoad(opts) {
		this.actor = buildActorDetail(opts)
		this.loadRecommendations(this.actor.id)
	},
	methods: {
		async loadRecommendations(actorId) {
			try {
				const res = await getRecommendationsForActor(actorId)
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
		goPlay(id) {
			uni.navigateTo({ url: `/pages/play-detail/play-detail?nodeId=${id}` }).catch(() => {})
		},
		goActor(id) {
			uni.navigateTo({ url: `/pages/actor-detail/actor-detail?nodeId=${id}` }).catch(() => {})
		}
	}
}
</script>

<style scoped>
.page { padding: 24rpx; padding-bottom: 48rpx; min-height: 100vh; background: #f5f5f5; }
.card.profile { background: #fff; border-radius: 24rpx; padding: 40rpx; text-align: center; margin-bottom: 24rpx; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06); }
.avatar-wrap { width: 160rpx; height: 160rpx; margin: 0 auto 24rpx; }
.avatar { width: 100%; height: 100%; border-radius: 50%; background: #eee; }
.avatar-placeholder { display: inline-block; width: 160rpx; height: 160rpx; line-height: 160rpx; border-radius: 50%; background: rgba(99,102,241,0.15); font-size: 80rpx; }
.name { font-size: 38rpx; font-weight: bold; color: #333; display: block; margin-bottom: 12rpx; }
.intro { font-size: 28rpx; color: #666; line-height: 1.5; display: block; margin-bottom: 16rpx; }
.tags { display: flex; flex-wrap: wrap; justify-content: center; gap: 12rpx; margin-bottom: 12rpx; }
.tag { font-size: 24rpx; color: #6366f1; background: rgba(99,102,241,0.1); padding: 8rpx 16rpx; border-radius: 10rpx; }
.specialty { font-size: 26rpx; color: #888; display: block; }
.block { background: #fff; border-radius: 20rpx; padding: 28rpx; margin-bottom: 24rpx; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06); }
.block-title { font-size: 30rpx; font-weight: bold; color: #333; display: block; margin-bottom: 20rpx; }
.work-list, .schedule-list { display: flex; flex-direction: column; gap: 16rpx; }
.work-item, .schedule-item { padding: 16rpx 0; border-bottom: 1rpx solid #f0f0f0; }
.work-item:last-child, .schedule-item:last-child { border-bottom: none; }
.work-name { font-size: 28rpx; color: #333; display: block; }
.work-role { font-size: 26rpx; color: #666; display: block; margin-top: 4rpx; }
.schedule-show { font-size: 28rpx; font-weight: 500; color: #333; display: block; }
.schedule-info { font-size: 26rpx; color: #666; display: block; margin-top: 4rpx; }
.rec-plays, .rec-actors { margin-bottom: 20rpx; }
.rec-plays:last-child, .rec-actors:last-child { margin-bottom: 0; }
.rec-label { font-size: 26rpx; color: #6366f1; display: block; margin-bottom: 12rpx; }
.rec-list { display: flex; flex-direction: column; gap: 12rpx; }
.rec-item { padding: 16rpx; background: #f8f8f8; border-radius: 12rpx; }
.rec-name { font-size: 28rpx; font-weight: 500; color: #333; display: block; }
.rec-reason { font-size: 24rpx; color: #888; display: block; margin-top: 4rpx; }
</style>
