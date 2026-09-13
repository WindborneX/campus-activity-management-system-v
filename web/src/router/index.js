import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/login', name: 'login', component: () => import('../views/Login.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    children: [
      { path: '', redirect: '/activities' },
      { path: 'activities', name: 'activity-list', component: () => import('../views/ActivityList.vue'), meta: { public: true } },
      { path: 'activities/:id', name: 'activity-detail', component: () => import('../views/ActivityDetail.vue'), meta: { public: true } },
      { path: 'my-registrations', name: 'my-registrations', component: () => import('../views/MyRegistrations.vue'), meta: { roles: ['STUDENT'] } },
      { path: 'manage', name: 'manage', component: () => import('../views/Manage.vue'), meta: { roles: ['TEACHER'] } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/activities' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局守卫：未登录拦截 + 角色授权
router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.public) {
    // 已登录用户访问登录页，直接回各自首页
    if (to.name === 'login' && auth.isLoggedIn) {
      return auth.homeRoute()
    }
    return true
  }

  if (!auth.isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  if (to.meta.roles && !to.meta.roles.includes(auth.role)) {
    return auth.homeRoute()
  }
  return true
})

export default router
