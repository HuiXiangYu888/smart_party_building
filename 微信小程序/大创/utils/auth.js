/**
 * JWT令牌管理工具类
 * 用于处理用户登录状态、令牌存储、刷新等功能
 */

import eventBus, { EVENTS } from './eventBus.js'

// 存储键名常量
const STORAGE_KEYS = {
	ACCESS_TOKEN: 'accessToken',
	REFRESH_TOKEN: 'refreshToken',
	USER_INFO: 'userInfo',
	TOKEN_EXPIRE_TIME: 'tokenExpireTime'
}

// 基础配置
const CONFIG = {
	BASE_URL: 'http://127.0.0.1:8000',
	TOKEN_REFRESH_THRESHOLD: 5 * 60 * 1000, // 5分钟前开始刷新
	REQUEST_TIMEOUT: 10000
}

/**
 * 获取存储的访问令牌
 */
export const getAccessToken = () => {
	return uni.getStorageSync(STORAGE_KEYS.ACCESS_TOKEN)
}

/**
 * 获取存储的刷新令牌
 */
export const getRefreshToken = () => {
	return uni.getStorageSync(STORAGE_KEYS.REFRESH_TOKEN)
}

/**
 * 获取用户信息
 */
export const getUserInfo = () => {
	const userInfo = uni.getStorageSync(STORAGE_KEYS.USER_INFO)
	return userInfo ? JSON.parse(userInfo) : null
}

/**
 * 更新用户信息
 */
export const updateUserInfo = (newUserInfo) => {
	const currentUserInfo = getUserInfo()
	if (currentUserInfo) {
		const updatedUserInfo = { ...currentUserInfo, ...newUserInfo }
		uni.setStorageSync(STORAGE_KEYS.USER_INFO, JSON.stringify(updatedUserInfo))
		return updatedUserInfo
	}
	return null
}

/**
 * 保存登录信息
 */
export const saveLoginInfo = (loginData) => {
	const { accessToken, refreshToken, userId, username, userType, expiresIn } = loginData
	
	// 计算令牌过期时间
	const expireTime = Date.now() + expiresIn
	
	// 保存到本地存储
	uni.setStorageSync(STORAGE_KEYS.ACCESS_TOKEN, accessToken)
	uni.setStorageSync(STORAGE_KEYS.REFRESH_TOKEN, refreshToken)
	uni.setStorageSync(STORAGE_KEYS.USER_INFO, JSON.stringify({
		userId,
		username,
		userType
	}))
	uni.setStorageSync(STORAGE_KEYS.TOKEN_EXPIRE_TIME, expireTime)
	
	// 触发登录事件，通知所有页面更新状态
	eventBus.emit(EVENTS.USER_LOGIN, {
		userId,
		username,
		userType
	})
}

/**
 * 清除登录信息
 */
export const clearLoginInfo = () => {
	// 清除本地存储
	uni.removeStorageSync(STORAGE_KEYS.ACCESS_TOKEN)
	uni.removeStorageSync(STORAGE_KEYS.REFRESH_TOKEN)
	uni.removeStorageSync(STORAGE_KEYS.USER_INFO)
	uni.removeStorageSync(STORAGE_KEYS.TOKEN_EXPIRE_TIME)
	
	// 触发退出登录事件，通知所有页面更新状态
	eventBus.emit(EVENTS.USER_LOGOUT, null)
}

/**
 * 检查是否已登录
 */
export const isLoggedIn = () => {
	const token = getAccessToken()
	const expireTime = uni.getStorageSync(STORAGE_KEYS.TOKEN_EXPIRE_TIME)
	
	if (!token || !expireTime) {
		return false
	}
	
	// 检查令牌是否过期
	return Date.now() < expireTime
}

/**
 * 检查令牌是否需要刷新
 */
export const needRefreshToken = () => {
	const expireTime = uni.getStorageSync(STORAGE_KEYS.TOKEN_EXPIRE_TIME)
	if (!expireTime) {
		return false
	}
	
	// 如果距离过期时间小于阈值，则需要刷新
	return (expireTime - Date.now()) < CONFIG.TOKEN_REFRESH_THRESHOLD
}

/**
 * 刷新访问令牌
 */
export const refreshAccessToken = async () => {
	const refreshToken = getRefreshToken()
	if (!refreshToken) {
		throw new Error('刷新令牌不存在')
	}
	
	try {
		const response = await uni.request({
			url: `${CONFIG.BASE_URL}/auth/refresh`,
			method: 'POST',
			header: {
				'Content-Type': 'application/json'
			},
			data: {
				refreshToken
			},
			timeout: CONFIG.REQUEST_TIMEOUT
		})
		
		if (response.statusCode === 200 && response.data.code === 200) {
			const newAccessToken = response.data.data
			
			// 更新访问令牌
			uni.setStorageSync(STORAGE_KEYS.ACCESS_TOKEN, newAccessToken)
			
			return newAccessToken
		} else {
			throw new Error(response.data.message || '令牌刷新失败')
		}
	} catch (error) {
		console.error('刷新令牌失败:', error)
		// 刷新失败，清除登录信息
		clearLoginInfo()
		throw error
	}
}

/**
 * 用户登出
 */
export const logout = async () => {
	const accessToken = getAccessToken()
	const userId = getUserInfo()?.userId
	
	try {
		// 调用后端登出接口（失败也不影响本地清理）
		const requestOptions = {
			url: `${CONFIG.BASE_URL}/auth/logout`,
			method: 'POST',
			header: {
				'Content-Type': 'application/json'
			},
			timeout: CONFIG.REQUEST_TIMEOUT
		}
		
		// 如果有有效的token，则添加到请求头中
		if (accessToken) {
			requestOptions.header['Authorization'] = `Bearer ${accessToken}`
		}
		
		// 如果有用户ID，则添加到请求体中
		if (userId) {
			requestOptions.data = { userId }
		}
		
		await uni.request(requestOptions)
		console.log('登出请求成功')
	} catch (e) {
		console.warn('登出请求失败，但继续清理本地数据:', e)
	} finally {
		// 无论后端请求是否成功，都清理本地登录信息
		clearLoginInfo()
		console.log('本地登录信息已清理')
		// 跳转至登录页，禁止返回
		uni.reLaunch({ url: '/pages/my/login/login' })
	}
}

/**
 * 携带令牌的通用请求，自动在接近过期时刷新
 */
export const requestWithAuth = async (options) => {
	let token = getAccessToken()
	if (!token) throw new Error('未登录')
	if (needRefreshToken()) {
		token = await refreshAccessToken()
	}
	const { url, method = 'GET', data = {}, header = {}, timeout = CONFIG.REQUEST_TIMEOUT } = options || {}
	return await uni.request({
		url: url.startsWith('http') ? url : `${CONFIG.BASE_URL}${url}`,
		method,
		data,
		timeout,
		header: {
			'Content-Type': 'application/json',
			'Authorization': `Bearer ${token}`,
			...header
		}
	})
}

/**
 * 提供给其他模块读取 BASE_URL，避免多处硬编码导致不一致
 */
export const getBaseUrl = () => CONFIG.BASE_URL