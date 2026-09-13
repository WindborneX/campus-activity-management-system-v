import { defineStore } from 'pinia'

const STORAGE_KEY = 'campus_auth'

function load() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY)) || null
  } catch {
    return null
  }
}

/** 登录态：token + 用户信息，手动持久化到 localStorage（不引入额外插件） */
export const useAuthStore = defineStore('auth', {
  state: () => ({
    auth: load()
  }),
  getters: {
    isLoggedIn: (s) => !!s.auth?.token,
    token: (s) => s.auth?.token || '',
    userId: (s) => s.auth?.userId ?? null,
    username: (s) => s.auth?.username || '',
    role: (s) => s.auth?.role || '',
    isTeacher: (s) => s.auth?.role === 'TEACHER',
    isStudent: (s) => s.auth?.role === 'STUDENT'
  },
  actions: {
    setAuth(auth) {
      this.auth = auth
      localStorage.setItem(STORAGE_KEY, JSON.stringify(auth))
    },
    logout() {
      this.auth = null
      localStorage.removeItem(STORAGE_KEY)
    },
    /** 登录后按角色跳转的首页 */
    homeRoute() {
      return this.isTeacher ? '/manage' : '/activities'
    }
  }
})
