import request from '@/utils/request'

const BASE = '/tasks'

export const taskAPI = {
  page: (params) => request({ url: `${BASE}/page`, method: 'GET', params }),
  create: (data) => request({ url: `${BASE}`, method: 'POST', data }),
  update: (id, data) => request({ url: `${BASE}/${id}`, method: 'PUT', data }),
  remove: (id) => request({ url: `${BASE}/${id}`, method: 'DELETE' }),
  assignMembers: (taskId, userIds) => request({ url: `${BASE}/${taskId}/assignments`, method: 'POST', data: userIds }),
  listEvaluations: (taskId) => request({ url: `${BASE}/${taskId}/evaluations`, method: 'GET' }),
  listParticipants: (taskId) => request({ url: `${BASE}/${taskId}/participants`, method: 'GET' })
}

export default taskAPI


