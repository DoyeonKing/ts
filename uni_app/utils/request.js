import config from '../config/index.js'

const candidateBaseURLs = Array.isArray(config.baseURLCandidates) && config.baseURLCandidates.length
	? config.baseURLCandidates
	: [config.baseURL]

let activeBaseURL = config.baseURL || candidateBaseURLs[0]

const doRequest = (baseURL, options) => {
	return new Promise((resolve, reject) => {
		uni.request({
			url: baseURL + options.url,
			method: options.method || 'GET',
			data: options.data || {},
			timeout: options.timeout || 10000,
			header: {
				'Content-Type': 'application/json',
				...options.header
			},
			success: (res) => {
				if (res.statusCode === 200) {
					resolve(res.data)
				} else {
					reject({ type: 'http', response: res, baseURL })
				}
			},
			fail: (err) => {
				reject({ type: 'network', error: err, baseURL })
			}
		})
	})
}

const request = async (options) => {
	const trialBaseURLs = [...new Set([activeBaseURL, ...candidateBaseURLs].filter(Boolean))]
	let lastError = null

	for (const baseURL of trialBaseURLs) {
		try {
			const data = await doRequest(baseURL, options)
			activeBaseURL = baseURL
			return data
		} catch (err) {
			lastError = err
			// 服务已连通但返回非 200：不再切换地址，直接抛出
			if (err && err.type === 'http') {
				throw err.response
			}
		}
	}

	if (lastError && lastError.error) {
		throw lastError.error
	}
	throw lastError || new Error('请求失败')
}

export default request
