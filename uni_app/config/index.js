// API配置
// 手动指定一个局域网 IP（例如你的 172 网段地址）
const LOCAL_NETWORK_IP = '172.20.10.3'

const PORT = 8080
const API_PREFIX = '/api'

const toBaseURL = (host) => `http://${host}:${PORT}${API_PREFIX}`

const buildDevBaseURLCandidates = () => {
	const hosts = []

	// H5 下优先尝试当前页面所在主机
	// #ifdef H5
	if (typeof window !== 'undefined' && window.location && window.location.hostname) {
		hosts.push(window.location.hostname)
	}
	// #endif

	hosts.push(LOCAL_NETWORK_IP)
	hosts.push('127.0.0.1')
	hosts.push('localhost')

	const uniqueHosts = [...new Set(hosts.filter(Boolean))]
	return uniqueHosts.map(toBaseURL)
}

const devCandidates = buildDevBaseURLCandidates()

const config = {
	development: {
		baseURL: devCandidates[0],
		baseURLCandidates: devCandidates
	},
	production: {
		baseURL: 'https://your-domain.com/api',
		baseURLCandidates: ['https://your-domain.com/api']
	}
}

const env = process.env.NODE_ENV || 'development'

export default config[env]
