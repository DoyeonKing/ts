import request from '../utils/request.js'

/** 根据剧目（图谱节点 ID）推荐：相似剧目、相关演员等 */
export function getRecommendationsForPlay(playId) {
	return request({ url: `/recommendations/play/${playId}` })
}

/** 根据演员（图谱节点 ID）推荐 */
export function getRecommendationsForActor(actorId) {
	return request({ url: `/recommendations/actor/${actorId}` })
}

/** 根据任意图谱节点 ID 推荐 */
export function getRecommendationsForNode(nodeId) {
	return request({ url: `/recommendations/node/${nodeId}` })
}

/** 根据名称查找图谱节点 ID，用于详情页未传 nodeId 时 */
export function lookupNodeId(name, type) {
	return request({ url: '/recommendations/lookup', data: { name, type } })
}
