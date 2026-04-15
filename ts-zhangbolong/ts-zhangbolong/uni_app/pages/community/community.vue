<template>
	<view class="page">
		<view class="tabs">
			<view v-for="(t, i) in tabs" :key="i" :class="['tab', currentTab === i ? 'active' : '']" @click="currentTab = i">
				<text>{{ t }}</text>
			</view>
		</view>
		<view class="content">
			<view v-for="(post, i) in posts" :key="i" class="post-card" @click="goPostDetail(post.id)">
				<view class="post-header">
					<image class="avatar" :src="post.userAvatar" mode="aspectFill" />
					<view class="post-meta">
						<text class="user-name">{{ post.userName }}</text>
						<text class="post-time">{{ post.postTime }}</text>
					</view>
				</view>
				<text class="post-title">{{ post.title }}</text>
				<text class="post-content">{{ post.content }}</text>
				<view v-if="post.images && post.images.length" class="post-images">
					<image v-for="(img, j) in post.images.slice(0,3)" :key="j" class="post-img" :src="img" mode="aspectFill" />
				</view>
				<view class="post-tags" v-if="post.tags && post.tags.length">
					<text v-for="(tag, k) in post.tags.slice(0,4)" :key="k" class="tag">{{ tag }}</text>
				</view>
				<view class="post-footer">
					<text class="stat">❤ {{ post.likeCount }}</text>
					<text class="stat">💬 {{ post.commentCount }}</text>
					<text class="stat">📍 {{ post.location }}</text>
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
			tabs: ['推荐', '关注', '同城', '热门'],
			posts: [
				{
					id: '1',
					userId: 'user1',
					userName: '剧场达人小王',
					userAvatar: 'https://picsum.photos/200/200?random=1',
					postTime: '2小时前',
					title: '今晚的《哈姆雷特》太震撼了！',
					content: '莎翁的经典之作，演员的表演真是绝了！特别是那段独白，听得我鸡皮疙瘩都起来了。强烈推荐大家去看！',
					images: ['https://picsum.photos/400/600?random=10', 'https://picsum.photos/400/500?random=11'],
					tags: ['哈姆雷特', '莎士比亚', '经典戏剧'],
					location: '国家大剧院',
					likeCount: 128,
					commentCount: 32
				},
				{
					id: '2',
					userId: 'user2',
					userName: '音乐剧爱好者',
					userAvatar: 'https://picsum.photos/200/200?random=2',
					postTime: '4小时前',
					title: '《歌剧魅影》幕后花絮',
					content: '有幸参观了《歌剧魅影》的排练现场，演员们真的太敬业了！',
					images: [],
					tags: ['歌剧魅影', '幕后', '音乐剧'],
					location: '上海大剧院',
					likeCount: 89,
					commentCount: 18
				},
				{
					id: '3',
					userId: 'user3',
					userName: '戏剧评论家',
					userAvatar: 'https://picsum.photos/200/200?random=3',
					postTime: '6小时前',
					title: '现代戏剧的新趋势',
					content: '近年来，沉浸式戏剧越来越受欢迎，观众不再是被动的观看者，而是故事的参与者。',
					images: ['https://picsum.photos/400/800?random=30'],
					tags: ['沉浸式戏剧', '现代戏剧'],
					location: '北京人艺',
					likeCount: 256,
					commentCount: 45
				}
			]
		}
	},
	methods: {
		goPostDetail(id) {
			uni.navigateTo({ url: `/pages/post-detail/post-detail?id=${id}` }).catch(() => {})
		}
	}
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f5f5; }
.tabs { display: flex; background: #fff; }
.tab { flex: 1; text-align: center; padding: 28rpx 0; font-size: 28rpx; color: #666; }
.tab.active { color: #6366f1; font-weight: bold; border-bottom: 4rpx solid #6366f1; }
.content { padding: 24rpx; padding-bottom: 40rpx; }
.post-card { background: #fff; border-radius: 20rpx; padding: 28rpx; margin-bottom: 24rpx; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06); }
.post-header { display: flex; align-items: center; margin-bottom: 20rpx; }
.avatar { width: 72rpx; height: 72rpx; border-radius: 50%; margin-right: 20rpx; background: #eee; }
.post-meta { flex: 1; }
.user-name { font-size: 28rpx; font-weight: bold; color: #333; display: block; }
.post-time { font-size: 24rpx; color: #999; display: block; margin-top: 4rpx; }
.post-title { font-size: 30rpx; font-weight: bold; color: #333; display: block; margin-bottom: 12rpx; }
.post-content { font-size: 28rpx; color: #666; line-height: 1.5; display: block; margin-bottom: 16rpx; }
.post-images { display: flex; gap: 12rpx; margin-bottom: 16rpx; }
.post-img { width: 200rpx; height: 200rpx; border-radius: 12rpx; background: #eee; }
.post-tags { margin-bottom: 16rpx; }
.tag { display: inline-block; font-size: 22rpx; color: #6366f1; background: rgba(99,102,241,0.1); padding: 8rpx 16rpx; border-radius: 8rpx; margin-right: 12rpx; margin-bottom: 8rpx; }
.post-footer { display: flex; gap: 32rpx; font-size: 24rpx; color: #999; }
</style>
