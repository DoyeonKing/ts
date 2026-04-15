<template>
	<view class="page">
		<view class="header">
			<text class="title">剧目信息</text>
			<text class="desc">背景剧情、人物小传、评分、标签及相关演员与演出场次</text>
		</view>
		<view class="list">
			<view class="card" v-for="(p, i) in plays" :key="i" @click="goDetail(p.id)">
				<view class="cover-wrap">
					<image v-if="p.cover" class="cover" :src="p.cover" mode="aspectFill" />
					<text v-else class="cover-placeholder">🎭</text>
				</view>
				<view class="body">
					<text class="name">{{ p.name }}</text>
					<text class="meta">{{ p.genre }} · {{ p.rating }}</text>
					<text class="synopsis">{{ p.synopsis }}</text>
				</view>
				<text class="arrow">›</text>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			plays: [
				{ id: 1, name: '《哈姆雷特》', genre: '悲剧', rating: '★★★★★', synopsis: '莎士比亚四大悲剧之一，丹麦王子复仇与人性抉择。', cover: '' },
				{ id: 2, name: '《罗密欧与朱丽叶》', genre: '爱情悲剧', rating: '★★★★★', synopsis: '莎士比亚经典爱情故事，两大家族下的禁忌之恋。', cover: '' },
				{ id: 3, name: '《天鹅湖》', genre: '芭蕾', rating: '★★★★☆', synopsis: '柴可夫斯基芭蕾舞剧，王子与天鹅公主的童话。', cover: '' },
				{ id: 4, name: '《茶花女》', genre: '歌剧', rating: '★★★★★', synopsis: '威尔第歌剧，巴黎名妓与青年贵族的爱情悲剧。', cover: '' },
				{ id: 5, name: '《李尔王》', genre: '悲剧', rating: '★★★★☆', synopsis: '莎士比亚四大悲剧，权力、亲情与背叛。', cover: '' }
			]
		}
	},
	methods: {
		goDetail(id) {
			uni.navigateTo({ url: `/pages/play-detail/play-detail?id=${id}` }).catch(() => {})
		}
	}
}
</script>

<style scoped>
.page { padding: 24rpx; padding-bottom: 48rpx; min-height: 100vh; background: #f5f5f5; }
.header { margin-bottom: 28rpx; }
.title { font-size: 38rpx; font-weight: bold; color: #333; display: block; margin-bottom: 12rpx; }
.desc { font-size: 26rpx; color: #666; line-height: 1.5; display: block; }
.list { display: flex; flex-direction: column; gap: 20rpx; }
.card { display: flex; align-items: center; background: #fff; border-radius: 20rpx; padding: 24rpx; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06); }
.cover-wrap { width: 140rpx; height: 180rpx; margin-right: 24rpx; flex-shrink: 0; border-radius: 12rpx; overflow: hidden; background: #eee; }
.cover { width: 100%; height: 100%; }
.cover-placeholder { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; font-size: 56rpx; }
.body { flex: 1; min-width: 0; }
.name { font-size: 32rpx; font-weight: bold; color: #333; display: block; margin-bottom: 8rpx; }
.meta { font-size: 26rpx; color: #6366f1; display: block; margin-bottom: 8rpx; }
.synopsis { font-size: 24rpx; color: #888; display: block; line-height: 1.4; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.arrow { font-size: 36rpx; color: #999; margin-left: 16rpx; }
</style>
