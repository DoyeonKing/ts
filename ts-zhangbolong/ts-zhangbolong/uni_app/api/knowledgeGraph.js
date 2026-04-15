import request from '../utils/request.js'

export function getFullGraph() {
	return request({ url: '/knowledge-graph/full' })
}

export function getNodeNeighborhood(nodeId) {
	return request({ url: `/knowledge-graph/node/${nodeId}/neighborhood` })
}

export function getNodeDetail(nodeId) {
	return request({ url: `/knowledge-graph/node/${nodeId}` })
}

export function searchNodes(keyword) {
	return request({ url: '/knowledge-graph/search', data: { keyword } })
}

export function getGraphByTypes(types) {
	return request({ url: '/knowledge-graph/filter', data: { types } })
}

export function getNodesByType(type) {
	return request({ url: '/knowledge-graph/nodes', data: { type } })
}

export function initSeedData() {
	return request({ url: '/knowledge-graph/init', method: 'POST' })
}
