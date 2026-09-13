import request from './request'

/** F1 注册 */
export function register(data) {
  return request.post('/api/auth/register', data)
}

/** F2 登录，返回 {token, userId, username, role} */
export function login(data) {
  return request.post('/api/auth/login', data)
}
