<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-aside">
        <h1>校园活动<br />管理系统</h1>
        <p>发布 · 浏览 · 报名，一站完成</p>
        <ul>
          <li>教师在线发布与管理活动</li>
          <li>学生实时浏览、一键报名</li>
          <li>报名情况随时可查</li>
        </ul>
      </div>

      <div class="login-panel">
        <el-tabs v-model="mode" stretch class="login-tabs">
          <el-tab-pane label="登录" name="login" />
          <el-tab-pane label="注册" name="register" />
        </el-tabs>

        <!-- 登录 -->
        <el-form v-if="mode === 'login'" ref="loginFormRef" :model="loginForm" :rules="loginRules"
                 label-position="top" size="large" @keyup.enter="handleLogin">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="loginForm.username" placeholder="请输入用户名" clearable />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password />
          </el-form-item>
          <el-button type="primary" class="submit-btn" :loading="loading" @click="handleLogin">登 录</el-button>
          <p class="switch-tip">还没有账号？<el-link type="primary" @click="mode = 'register'">立即注册</el-link></p>
        </el-form>

        <!-- 注册 -->
        <el-form v-else ref="registerFormRef" :model="registerForm" :rules="registerRules"
                 label-position="top" size="large" @keyup.enter="handleRegister">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="registerForm.username" placeholder="3~50 个字符" clearable />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="registerForm.password" type="password" placeholder="6~64 个字符" show-password />
          </el-form-item>
          <el-form-item label="身份" prop="role">
            <el-radio-group v-model="registerForm.role">
              <el-radio-button value="STUDENT">学生</el-radio-button>
              <el-radio-button value="TEACHER">教师</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-button type="primary" class="submit-btn" :loading="loading" @click="handleRegister">注 册</el-button>
          <p class="switch-tip">已有账号？<el-link type="primary" @click="mode = 'login'">返回登录</el-link></p>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '../api/auth'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const mode = ref('login')
const loading = ref(false)
const loginFormRef = ref()
const registerFormRef = ref()

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', password: '', role: 'STUDENT' })

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度需在 3~50 之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 64, message: '密码长度需在 6~64 之间', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择身份', trigger: 'change' }]
}

async function handleLogin() {
  await loginFormRef.value.validate()
  loading.value = true
  try {
    const data = await login({ ...loginForm })
    auth.setAuth(data)
    ElMessage.success('登录成功')
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : null
    router.push(redirect || auth.homeRoute())
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  await registerFormRef.value.validate()
  loading.value = true
  try {
    await register({ ...registerForm })
    ElMessage.success('注册成功，请登录')
    // 后端注册不签发 token，回到登录表单并带出用户名
    loginForm.username = registerForm.username
    loginForm.password = ''
    mode.value = 'login'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(135deg, #eaf0fa 0%, #f5f7fb 55%, #dfe8f8 100%);
}

.login-card {
  display: flex;
  width: 860px;
  max-width: 100%;
  min-height: 480px;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 18px 48px rgba(31, 56, 100, 0.14);
}

.login-aside {
  flex: 1;
  background: linear-gradient(150deg, var(--brand-deep), var(--brand));
  color: #fff;
  padding: 48px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-aside h1 {
  font-size: 30px;
  line-height: 1.35;
  margin: 0 0 14px;
  letter-spacing: 2px;
}

.login-aside p { opacity: 0.85; margin: 0 0 28px; font-size: 14px; }

.login-aside ul {
  margin: 0;
  padding-left: 18px;
  font-size: 14px;
  line-height: 2.2;
  opacity: 0.92;
}

.login-panel {
  flex: 1.15;
  padding: 38px 44px 32px;
}

.submit-btn { width: 100%; margin-top: 6px; }

.switch-tip { text-align: center; font-size: 13px; color: var(--ink-muted); margin: 16px 0 0; }

@media (max-width: 720px) {
  .login-card { flex-direction: column; }
  .login-aside { padding: 32px; }
}
</style>
