import request from './request'

/** F5 活动列表，stage: open|ongoing|finished|all（不传默认 open） */
export function listActivities(stage) {
  return request.get('/api/activities', { params: { stage } })
}

/** F6 活动详情 */
export function getActivity(id) {
  return request.get(`/api/activities/${id}`)
}
