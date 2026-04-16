<template>
	<view class="page">
		<view class="header">
			<text class="title">演员资料</text>
			<text class="desc">演员详细资料、代表作品、奖项、专长及参演剧目与排班信息</text>
		</view>
		<view class="list">
			<view class="card" v-for="(a, i) in actors" :key="i" @click="goDetail(a.id)">
				<view class="avatar-wrap">
					<image v-if="a.avatar" class="avatar" :src="a.avatar" mode="aspectFill" />
					<text v-else class="avatar-placeholder">👤</text>
				</view>
				<view class="body">
					<text class="name">{{ a.name }}</text>
					<text class="works">代表作品：{{ a.works }}</text>
					<view class="tags" v-if="a.awards && a.awards.length">
						<text class="tag" v-for="(aw, j) in a.awards.slice(0,2)" :key="j">{{ aw }}</text>
					</view>
					<text class="specialty" v-if="a.specialty">专长：{{ a.specialty }}</text>
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
			actors: [
				{ id: 1, name: '濮存昕', avatar: '', works: '《哈姆雷特》《李尔王》《茶馆》', awards: ['梅花奖', '文华奖'], specialty: '话剧、莎士比亚' },
				{ id: 2, name: '袁泉', avatar: '', works: '《简·爱》《青蛇》《琥珀》', awards: ['梅花奖', '金狮奖'], specialty: '话剧、音乐剧' },
				{ id: 3, name: '何冰', avatar: '', works: '《窝头会馆》《喜剧的忧伤》', awards: ['梅花奖'], specialty: '话剧、喜剧' }
			]
		}
	},
	methods: {
		goDetail(id) {
			uni.navigateTo({ url: `/pages/actor-detail/actor-detail?id=${id}` }).catch(() => {})
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
.card { display: flex; align-items: center; background: #fff; border-radius: 20rpx; padding: 28rpx; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06); }
.avatar-wrap { position: relative; width: 120rpx; height: 120rpx; margin-right: 24rpx; flex-shrink: 0; }
.avatar { width: 100%; height: 100%; border-radius: 50%; background: #eee; }
.avatar-placeholder { width: 100%; height: 100%; border-radius: 50%; background: rgba(99,102,241,0.15); text-align: center; line-height: 120rpx; font-size: 56rpx; }
.body { flex: 1; min-width: 0; }
.name { font-size: 32rpx; font-weight: bold; color: #333; display: block; margin-bottom: 8rpx; }
.works { font-size: 26rpx; color: #666; display: block; margin-bottom: 8rpx; }
.tags { display: flex; flex-wrap: wrap; gap: 8rpx; margin-bottom: 6rpx; }
.tag { font-size: 22rpx; color: #6366f1; background: rgba(99,102,241,0.1); padding: 6rpx 14rpx; border-radius: 8rpx; }
.specialty { font-size: 24rpx; color: #888; display: block; }
.arrow { font-size: 36rpx; color: #999; margin-left: 16rpx; }
</style>
