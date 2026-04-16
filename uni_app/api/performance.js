import request from '../utils/request.js'

export function getOnSalePerformances() {
	return request({ url: '/performances' })
}

export function getPerformancesByPlayId(playId) {
	return request({ url: `/performances/play/${playId}` })
}
