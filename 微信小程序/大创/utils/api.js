/**
 * API服务类
 * 统一管理所有的API请求
 */

import { requestWithAuth as request } from './auth.js'

// 基础配置
const BASE_URL = 'http://127.0.0.1:8000'

export function toAbsoluteUrl(url) {
  if (!url) return ''
  if (/^https?:\/\//i.test(url)) return url
  const base = BASE_URL.replace(/\/+$/, '')
  const path = ('/' + String(url)).replace(/\\/g, '/').replace(/\/+/g, '/')
  return base + path
}

export function getNewsPage({ keyword = '', page = 1, size = 10, branchId } = {}) {
  return request({
    url: `${BASE_URL}/news/user`,
    method: 'GET',
    data: { keyword, page, size, branchId }
  })
}

export function getNewsDetail(id) {
  return request({
    url: `${BASE_URL}/news/${id}`,
    method: 'GET'
  })
}

/**
 * 用户相关API
 */
export const userAPI = {
	// 用户端登录
	login: (data) => {
		// 登录不应使用携带令牌的请求
		return uni.request({
			url: `${BASE_URL}/auth/user/login`,
			method: 'POST',
			header: { 'Content-Type': 'application/json' },
			data
		})
	},
	
	// 获取用户信息
	getUserInfo: () => {
		return request({
			url: `${BASE_URL}/user/info`,
			method: 'GET'
		})
	},
	
	// 更新用户信息
	updateUserInfo: (data) => {
		return request({
			url: `${BASE_URL}/user/info`,
			method: 'PUT',
			data
		})
	},
	
	// 修改密码
	changePassword: (data) => {
		return request({
			url: `${BASE_URL}/user/password`,
			method: 'PUT',
			data
		})
	}
}



/**
 * 认证相关API
 */
export const authAPI = {
	// 刷新令牌
	refreshToken: (data) => {
		return request({
			url: `${BASE_URL}/auth/refresh`,
			method: 'POST',
			data
		})
	},
	
	// 用户登出
	logout: (data) => {
		return request({
			url: `${BASE_URL}/auth/logout`,
			method: 'POST',
			data
		})
	},
	
	// 验证令牌
	validateToken: (token) => {
		return request({
			url: `${BASE_URL}/auth/validate`,
			method: 'GET',
			data: { token }
		})
	}
}

/**
 * 活动相关API
 */
export const activityAPI = {
	// 获取活动列表
	getActivityList: (params) => {
		return request({
			url: `${BASE_URL}/activities`,
			method: 'GET',
			data: params
		})
	},
	
	// 获取活动详情
	getActivityDetail: (id) => {
		return request({
			url: `${BASE_URL}/activities/${id}`,
			method: 'GET'
		})
	},
	
	// 参加活动
	joinActivity: (id) => {
		return request({
			url: `${BASE_URL}/activities/${id}/join`,
			method: 'POST'
		})
	},
	
	// 退出活动
	leaveActivity: (id) => {
		return request({
			url: `${BASE_URL}/activities/${id}/leave`,
			method: 'POST'
		})
	},
	
	// 获取我的活动
	getMyActivities: (params) => {
		return request({
			url: `${BASE_URL}/activities/my`,
			method: 'GET',
			data: params
		})
	}
}

/**
 * 任务相关API
 */
export const taskAPI = {
  // 获取任务分页（用户端也复用管理端分页）
  getTaskPage: (params) => {
    // 小程序公开分页
    return uni.request({
      url: `${BASE_URL}/tasks/user/page`,
      method: 'GET',
      data: params
    })
  },
  // 用户报名（鉴权）
  userJoin: (taskId) => request({ url: `${BASE_URL}/tasks/user/${taskId}/join`, method: 'POST' }),
  // 用户取消报名（鉴权）
  userCancel: (taskId) => request({ url: `${BASE_URL}/tasks/user/${taskId}/cancel`, method: 'POST' }),
  // 提交评价
  submitEvaluation: (taskId, data) => {
    return request({
      url: `${BASE_URL}/tasks/${taskId}/evaluations`,
      method: 'POST',
      data
    })
  },
  // 查看评价
  listEvaluations: (taskId) => {
    return request({
      url: `${BASE_URL}/tasks/${taskId}/evaluations`,
      method: 'GET'
    })
  },
  // 我的任务（报名记录）
  getMyTasks: () => request({ url: `${BASE_URL}/tasks/user/my`, method: 'GET' })
}

// 积分相关API
export const pointsAPI = {
  getMyPointsList: () => request({ url: `${BASE_URL}/points/user/list`, method: 'GET' }),
  getMyPointsSummary: () => request({ url: `${BASE_URL}/points/user/summary`, method: 'GET' })
}

/**
 * 党员相关API
 */
export const memberAPI = {
	// 获取党员列表
	getMemberList: (params) => {
		return request({
			url: `${BASE_URL}/members`,
			method: 'GET',
			data: params
		})
	},
	
	// 获取党员详情
	getMemberDetail: (id) => {
		return request({
			url: `${BASE_URL}/members/${id}`,
			method: 'GET'
		})
	},
	
	// 更新党员信息
	updateMember: (id, data) => {
		return request({
			url: `${BASE_URL}/members/${id}`,
			method: 'PUT',
			data
		})
	}
}

/**
 * 学习相关API
 */
export const studyAPI = {
	// 获取学习记录
	getStudyRecords: (params) => {
		return request({
			url: `${BASE_URL}/study/records`,
			method: 'GET',
			data: params
		})
	},
	
	// 添加学习记录
	addStudyRecord: (data) => {
		return request({
			url: `${BASE_URL}/study/records`,
			method: 'POST',
			data
		})
	},
	
	// 获取学习资料
	getStudyMaterials: (params) => {
		return request({
			url: `${BASE_URL}/study/materials`,
			method: 'GET',
			data: params
		})
	}
}

/**
 * 入党相关API
 */
export const partyAPI = {
	// 提交入党申请
	submitApplication: (data) => {
		return request({
			url: `${BASE_URL}/party/application`,
			method: 'POST',
			data
		})
	},
	
	// 获取入党申请状态
	getApplicationStatus: () => {
		return request({
			url: `${BASE_URL}/party/application/status`,
			method: 'GET'
		})
	},
	
	// 获取积极分子列表
	getActivistList: (params) => {
		return request({
			url: `${BASE_URL}/party/activists`,
			method: 'GET',
			data: params
		})
	},
	
	// 获取预备党员列表
	getPrepareList: (params) => {
		return request({
			url: `${BASE_URL}/party/prepare`,
			method: 'GET',
			data: params
		})
	},
	
	// 提交思想汇报
	submitReport: (data) => {
		return request({
			url: `${BASE_URL}/party/report`,
			method: 'POST',
			data
		})
	}
}

/**
 * 文件上传API
 */
export const uploadAPI = {
	// 上传文件（走后端 /files/upload，再转OSS）
	uploadFile: (filePath) => {
		return new Promise((resolve, reject) => {
			uni.uploadFile({
				url: `${BASE_URL}/files/upload`,
				filePath,
				name: 'file',
				header: {
					'Authorization': `Bearer ${uni.getStorageSync('accessToken') || ''}`
				},
				success: (res) => {
					try {
						const data = JSON.parse(res.data || '{}')
						if (data && data.code === 200) {
							resolve(data.data) // { url, objectKey }
						} else {
							reject(new Error(data.message || '上传失败'))
						}
					} catch (error) {
						reject(new Error('上传失败'))
					}
				},
				fail: (error) => {
					reject(error)
				}
			})
		})
	}
}

// 新版入党相关（匹配后端新接口）
export const applicationAPI = {
    applyPositive: (details, attachments) => request({
		url: `${BASE_URL}/user/applications/positive`,
		method: 'POST',
        data: { details, attachments }
	}),
	applyParty: (details, attachments) => request({
		url: `${BASE_URL}/user/applications/party`,
		method: 'POST',
		data: { details, attachments }
	}),
	applyPrepare: (evaluationReport, probationStart, probationEnd, attachments) => request({
		url: `${BASE_URL}/user/applications/prepare`,
		method: 'POST',
		data: { evaluationReport, probationStart, probationEnd, attachments }
    }),
    getMyHistory: () => request({ url: `${BASE_URL}/user/applications/history`, method: 'GET' })
}

export default {
	userAPI,
	authAPI,
	activityAPI,
	taskAPI,
	pointsAPI,
	memberAPI,
	studyAPI,
	partyAPI,
	uploadAPI,
	applicationAPI
}
