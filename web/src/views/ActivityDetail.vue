<template>
  <div v-loading="loading">
    <div class="back-link" @click="router.push('/activities')">← 返回活动列表</div>

    <div v-if="activity" class="detail-card">
      <div class="detail-head">
        <h2 class="page-title" style="margin:0">{{ activity.title }}</h2>
        <el-tag :type="stageType(activity.stage)" effect="plain" round size="large">
          {{ stageLabel(activity.stage) }}
        </el-tag>
      </div>

      <el-row :gutter="20" class="info-grid">
        <el-col :span="12">
          <div class="info-item"><span class="label">📍 地点</span><span class="val">{{ activity.location }}</span></div>
          <div class="info-item"><span class="label">🕒 开始</span><span class="val">{{ activity.startTime }}</span></div>
          <div class="info-item"><span class="label">🕓 结束</span><span class="val">{{ activity.endTime }}</span></div>
        </el-col>
        <el-col :span="12">
          <div class="info-item"><span class="label">⏳ 报名截止</span><span class="val">{{ activity.signupDeadline }}</span></div>
          <div class="info-item"><span class="label">👥 人数</span><span class="val">{{ activity.currentCount }} / {{ activity.maxParticipants }}</span></div>
          <el-progress
            :percentage="Math.round((activity.currentCount / activity.maxParticipants) * 100)"
            :stroke-width="8"
            :color="activity.currentCount >= activity.maxParticipants ? '#dc2626' : '#2f5496'"
            style="margin-top:12px"
          />
        </el-col>
      </el-row>

      <div class="desc">
        <h4>活动介绍</h4>
        <p>{{ activity.description || '暂无介绍' }}</p>
      </div>

      <div class="action-bar">
        <!-- 游客 -->
        <el-button v-if="!auth.isLoggedIn" type="primary" round @click="toLogin">登录后报名</el-button>

        <!-- 教师 -->
        <el-button v-else-if="auth.isTeacher" disabled round>
          教师身份无法报名
        </el-button>

        <!-- 学生 -->
        <template v-else-if="auth.isStudent">
          <el-button v-if="registered" type="danger" round @click="doCancel">取消报名</el-button>
          <el-button
            v-else
            type="primary"
            round
            :disabled="!canSignup"
            @click="doSignup"
          >
            {{ canSignup ? '立即报名' : signupReason }}
          </el-button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getActivity } from '../api/activities'
import { signup, cancelSignup } from '../api/registrations'
import { useAuthStore } from '../stores/auth'
import { useMyRegStore } from '../stores/myReg'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const myReg = useMyRegStore()

const activity = ref(null)
const loading = ref(false)

const registered = computed(() => myReg.isRegistered(Number(route.params.id)))

// 可报名条件：活动 OPEN
const canSignup = computed(() => activity.value?.stage === 'OPEN')

const signupReason = computed(() => {
  const s = activity.value?.stage
  return { UPCOMING: '报名已截止', ONGOING: '活动进行中', FINISHED: '活动已结束', CANCELLED: '活动已取消' }[s] || '不可报名'
})

async function load() {
  loading.value = true
  try {
    activity.value = await getActivity(route.params.id)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  if (auth.isStudent && !myReg.loaded) {
    myReg.fetch().catch(() => myReg.clear())
  }
  load()
})

watch(() => route.params.id, load)

function toLogin() {
  router.push({ path: '/login', query: { redirect: route.fullPath } })
}

async function doSignup() {
  if (!canSignup.value) return
  try {
    await signup(activity.value.id)
    ElMessage.success('报名成功')
    await myReg.fetch()
    load()
  } catch (e) {
    // 统一拦截器已弹错误提示，此处静默吸收 Promise 避免 Vue warn
  }
}

async function doCancel() {
  try {
    await ElMessageBox.confirm('确定取消报名吗？', '提示', {
      confirmButtonText: '取消报名',
      cancelButtonText: '再想想',
      type: 'warning'
    })
    await cancelSignup(activity.value.id)
    ElMessage.success('已取消报名')
    await myReg.fetch()
    load()
  } catch (e) {
    if (e !== 'cancel') { /* 拦截器已提示 */ }
  }
}

function stageLabel(s) {
  return { OPEN: '报名中', UPCOMING: '报名已截止', ONGOING: '进行中', FINISHED: '已结束', CANCELLED: '已取消' }[s] || s
}
function stageType(s) {
  return { OPEN: 'primary', UPCOMING: 'warning', ONGOING: 'success', FINISHED: 'info', CANCELLED: 'danger' }[s] || 'info'
}
</script>

<style scoped>
.back-link {
  display: inline-block;
  margin-bottom: 16px;
  cursor: pointer;
  color: var(--brand);
  font-size: 14px;
}

.detail-card {
  background: #fff;
  border: 1px solid #e4e9f2;
  border-radius: 14px;
  padding: 28px 32px;
}

.detail-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 22px;
}

.info-grid { margin-bottom: 24px; }

.info-item {
  display: flex;
  gap: 10px;
  padding: 6px 0;
  font-size: 14px;
}
.label { color: var(--ink-muted); min-width: 80px; }
.val { color: var(--ink); }

.desc {
  border-top: 1px dashed #e4e9f2;
  padding-top: 20px;
  margin-bottom: 24px;
}
.desc h4 { margin: 0 0 10px; font-size: 15px; color: var(--brand-deep); }
.desc p { margin: 0; line-height: 1.8; color: var(--ink); white-space: pre-wrap; }

.action-bar {
  display: flex;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px dashed #e4e9f2;
}
</style>
