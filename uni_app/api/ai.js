import request from '../utils/request.js'

/** 智能推荐对话：自然语言 -> 结构化推荐方案 */
export function chatWithAI(data) {
	return request({
		url: '/ai/chat',
		method: 'POST',
		data
	})
}

/** 独立高级剧场推荐：自然语言 -> 高级推荐方案 */
export function getAdvancedRecommendationPlan(data) {
	return request({
		url: '/advanced-recommendation/plan',
		method: 'POST',
		data
	})
}

/** 兼容旧页面调用 */
export function runSmartChat(query) {
	return getAdvancedRecommendationPlan({ query })
}

/** 垂直特征生成：自然语言 -> 专业特征与赏析 */
export function generateVerticalText(data) {
	return request({
		url: '/ai/generate-text',
		method: 'POST',
		data
	})
}

/** 文创建议生成（兼容返回 prompt/imageUrl/mock） */
export function generateCreative(data) {
	return request({
		url: '/ai/creative',
		method: 'POST',
		data
	})
}

/** 专门生成动态 prompt */
export function generateCreativePrompt(data) {
	return request({
		url: '/ai/creative-prompt',
		method: 'POST',
		data
	})
}

/** 根据 prompt 生成图片 */
export function generateCreativeImage(data) {
	return request({
		url: '/ai/creative-image',
		method: 'POST',
		data
	})
}
