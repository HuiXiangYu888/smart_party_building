import request from '@/utils/request'

// 管理员登录
export function login(data) {
  return request({
    url: '/auth/admin/login',
    method: 'post',
    data
  })
}

// 刷新token
export function refreshToken(data) {
  return request({
    url: '/auth/refresh',
    method: 'post',
    data
  })
}

// 退出登录
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

// 获取管理员信息
export function getAdminInfo() {
  return request({
    url: '/auth/profile',
    method: 'get'
  })
}

// 账号管理
export function listAdmins(params) {
  return request({
    url: '/admin/accounts',
    method: 'get',
    params
  })
}

export function createAdmin(data) {
  return request({
    url: '/admin/accounts',
    method: 'post',
    data
  })
}

export function updateAdmin(id, data) {
  return request({
    url: `/admin/accounts/${id}`,
    method: 'put',
    data
  })
}

export function updateAdminStatus(id, status) {
  return request({
    url: `/admin/accounts/${id}/status`,
    method: 'patch',
    params: { status }
  })
}

export function deleteAdmin(id) {
  return request({
    url: `/admin/accounts/${id}`,
    method: 'delete'
  })
}
