import request from '../utils/request.js'

/** 智能推荐对话：自然语言 -> 结构化推荐方案 */
export function chatWithAI(data) {
	return request({
		url: '/ai/chat',
		method: 'POST',
		data
	})
}

/** 垂直特征生成：自然语言 -> 专业特征与赏析 */
export function generateVerticalText(data) {
	return request({
		url: '/ai/generate-text',
		method: 'POST',
		data
	})
}
