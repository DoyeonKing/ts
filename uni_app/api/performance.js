import request from '../utils/request.js'

export function getPerformancesByPlayId(playId) {
	return request({ url: `/performances/play/${playId}` })
}
