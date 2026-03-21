// uni-app promisify适配器
if (typeof Promise !== 'undefined' && Promise.prototype.finally) {
	// 已支持
} else {
	// 添加finally支持
}

