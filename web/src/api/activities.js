import request from './request'

/** F5 活动列表，stage: open|ongoing|finished|all（不传默认 open） */
export function listActivities(stage) {
  return request.get('/api/activities', { params: { stage } })
}

/** F6 活动详情 */
export function getActivity(id) {
  return request.get(`/api/activities/${id}`)
}

/** F4 教师查看自己的活动 */
export function myActivities() {
  return request.get('/api/activities/mine')
}

/** F3 创建活动 */
export function createActivity(data) {
  return request.post('/api/activities', data)
}

/** F4 修改活动 */
export function updateActivity(id, data) {
  return request.put(`/api/activities/${id}`, data)
}

/** F4 取消活动（软删除） */
export function cancelActivity(id) {
  return request.delete(`/api/activities/${id}`)
}

/** F9 查看活动报名名单 */
export function activityRegistrations(id) {
  return request.get(`/api/activities/${id}/registrations`)
}

