import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import router from '../router'

/**
 * 统一 Axios 实例。
 * baseURL 为空、所有接口走相对路径 /api：
 * 开发与演示都由 Nginx 同源转发到后端，浏览器不直连 8080。
 */
const request = axios.create({
  baseURL: '',
  timeout: 10000
})

// 请求拦截：自动带上 JWT
request.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

// 响应拦截：统一解包 Result{code,message,data}
request.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 200) {
        return body.data
      }
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message || '业务错误'))
    }
    return body
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message
    if (status === 401) {
      const auth = useAuthStore()
      auth.logout()
      ElMessage.error('登录已过期，请重新登录')
      router.push('/login')
    } else {
      ElMessage.error(message || `请求失败（${status || '网络错误'}）`)
    }
    return Promise.reject(error)
  }
)

export default request
