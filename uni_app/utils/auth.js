// 认证工具类
const auth = {
	// 保存token
	setToken(token) {
		uni.setStorageSync('token', token)
	},
	
	// 获取token
	getToken() {
		return uni.getStorageSync('token')
	},
	
	// 清除token
	clearToken() {
		uni.removeStorageSync('token')
	},
	
	// 检查是否已登录
	isLoggedIn() {
		return !!this.getToken()
	}
}

export default auth

