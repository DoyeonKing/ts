// API配置
const config = {
	// 开发环境
	development: {
		baseURL: 'http://localhost:8080/api'
	},
	// 生产环境
	production: {
		baseURL: 'https://your-domain.com/api'
	}
}

// 获取当前环境
const env = process.env.NODE_ENV || 'development'

export default config[env]

