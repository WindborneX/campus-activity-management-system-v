<template>
  <div class="layout">
    <header class="topbar">
      <div class="topbar-inner">
        <router-link to="/activities" class="brand">
          <span class="brand-badge">校</span>
          <span>校园活动管理系统</span>
        </router-link>

        <nav class="nav">
          <router-link to="/activities" class="nav-item">活动浏览</router-link>
          <router-link v-if="auth.isStudent" to="/my-registrations" class="nav-item">我的报名</router-link>
          <router-link v-if="auth.isTeacher" to="/manage" class="nav-item">活动管理</router-link>
        </nav>

        <div class="user-area">
          <template v-if="auth.isLoggedIn">
            <el-tag :type="auth.isTeacher ? 'warning' : 'primary'" effect="plain" round>
              {{ auth.isTeacher ? '教师' : '学生' }}
            </el-tag>
            <span class="username">{{ auth.username }}</span>
            <el-button text type="primary" @click="handleLogout">退出</el-button>
          </template>
          <template v-else>
            <router-link to="/login"><el-button type="primary" round>登录 / 注册</el-button></router-link>
          </template>
        </div>
      </div>
    </header>

    <main class="page-container">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { useMyRegStore } from '../stores/myReg'

const auth = useAuthStore()
const myReg = useMyRegStore()
const router = useRouter()

onMounted(() => {
  // 学生登录后拉取已报名集合（教师无意义）
  if (auth.isLoggedIn && auth.isStudent) {
    myReg.fetch().catch(() => myReg.clear())
  }
})

function handleLogout() {
  ElMessageBox.confirm('确定退出登录吗？', '提示', {
    confirmButtonText: '退出',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    auth.logout()
    myReg.clear()
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.layout { min-height: 100vh; }

.topbar {
  background: #fff;
  border-bottom: 1px solid #e4e9f2;
  box-shadow: 0 1px 4px rgba(31, 56, 100, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}

.topbar-inner {
  max-width: 1080px;
  margin: 0 auto;
  height: 60px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 24px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 17px;
  font-weight: 700;
  color: var(--brand-deep);
}

.brand-badge {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: linear-gradient(135deg, var(--brand), var(--brand-deep));
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
}

.nav { display: flex; gap: 6px; flex: 1; }

.nav-item {
  padding: 6px 14px;
  border-radius: 8px;
  color: var(--ink-muted);
  font-size: 14.5px;
}

.nav-item:hover { background: var(--brand-light); color: var(--brand); }

.nav-item.router-link-active {
  background: var(--brand-light);
  color: var(--brand);
  font-weight: 600;
}

.user-area { display: flex; align-items: center; gap: 10px; }
.username { font-size: 14px; color: var(--ink); }
</style>
