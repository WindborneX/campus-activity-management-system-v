import request from './request'

/** F7 报名活动 */
export function signup(activityId) {
  return request.post(`/api/activities/${activityId}/signup`)
}

/** F8 取消报名 */
export function cancelSignup(activityId) {
  return request.delete(`/api/activities/${activityId}/signup`)
}

/** 我的报名（学生） */
export function myRegistrations() {
  return request.get('/api/registrations/mine')
}

/** F9 查看活动报名名单（教师，仅发布者） */
export function activityRegistrations(activityId) {
  return request.get(`/api/activities/${activityId}/registrations`)
}
