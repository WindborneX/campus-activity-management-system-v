import { defineStore } from 'pinia'
import { myRegistrations } from '../api/registrations'

/**
 * 当前登录学生的已报名活动 id 集合。
 * 没有单活动报名态接口（V1.0），统一由 GET /api/registrations/mine 推导。
 */
export const useMyRegStore = defineStore('myReg', {
  state: () => ({
    ids: new Set(),   // 已报名的 activityId 集合
    list: [],         // 原始列表（用于“我的报名”页）
    loaded: false
  }),
  actions: {
    /** 拉取并更新集合；学生登录后调用 */
    async fetch() {
      const data = await myRegistrations()
      this.list = Array.isArray(data) ? data : []
      this.ids = new Set(this.list.map((r) => r.activity?.id).filter((id) => id != null))
      this.loaded = true
    },
    clear() {
      this.ids.clear()
      this.list = []
      this.loaded = false
    },
    isRegistered(activityId) {
      return this.ids.has(activityId)
    }
  }
})
