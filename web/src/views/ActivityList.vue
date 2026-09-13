<template>
  <div>
    <h2 class="page-title">活动浏览</h2>

    <el-tabs v-model="stage" @tab-change="handleStageChange" class="stage-tabs">
      <el-tab-pane label="报名中" name="open" />
      <el-tab-pane label="进行中" name="ongoing" />
      <el-tab-pane label="已结束" name="finished" />
      <el-tab-pane label="全部" name="all" />
    </el-tabs>

    <div v-loading="loading" class="card-grid">
      <div v-for="a in list" :key="a.id" class="activity-card" @click="goDetail(a.id)">
        <div class="card-head">
          <span class="card-title">{{ a.title }}</span>
          <el-tag :type="stageType(a.stage)" effect="plain" round>{{ stageLabel(a.stage) }}</el-tag>
        </div>
        <div class="card-meta">
          <span class="meta-item">📍 {{ a.location }}</span>
          <span class="meta-item">🕒 {{ a.startTime }} ~ {{ a.endTime }}</span>
        </div>
        <div class="card-foot">
          <span class="capacity">
            已报 <b>{{ a.currentCount }}</b> / {{ a.maxParticipants }}
          </span>
          <el-progress
            :percentage="Math.round((a.currentCount / a.maxParticipants) * 100)"
            :show-text="false"
            :stroke-width="6"
            :color="progressColor(a)"
            style="width: 120px"
          />
        </div>
      </div>

      <el-empty v-if="!loading && list.length === 0" description="该阶段暂无活动" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listActivities } from '../api/activities'

const router = useRouter()
const stage = ref('open')
const list = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    list.value = await listActivities(stage.value)
  } finally {
    loading.value = false
  }
}

onMounted(load)

function handleStageChange() {
  load()
}

function goDetail(id) {
  router.push(`/activities/${id}`)
}

function stageLabel(s) {
  return { OPEN: '报名中', UPCOMING: '报名已截止', ONGOING: '进行中', FINISHED: '已结束', CANCELLED: '已取消' }[s] || s
}

function stageType(s) {
  return { OPEN: 'primary', UPCOMING: 'warning', ONGOING: 'success', FINISHED: 'info', CANCELLED: 'danger' }[s] || 'info'
}

function progressColor(a) {
  if (a.currentCount >= a.maxParticipants) return '#dc2626'
  if (a.currentCount * 100 / a.maxParticipants >= 80) return '#d97706'
  return '#2f5496'
}
</script>

<style scoped>
.stage-tabs { margin-bottom: 16px; }

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.activity-card {
  background: #fff;
  border: 1px solid #e4e9f2;
  border-radius: 12px;
  padding: 18px 20px;
  cursor: pointer;
  transition: box-shadow .2s, transform .2s, border-color .2s;
}

.activity-card:hover {
  box-shadow: 0 8px 24px rgba(31, 56, 100, 0.1);
  transform: translateY(-2px);
  border-color: #b8c8ea;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
}

.card-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--brand-deep);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  color: var(--ink-muted);
  font-size: 13px;
  margin-bottom: 14px;
}

.card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.capacity { font-size: 13px; color: var(--ink-muted); }
.capacity b { color: var(--brand); }
</style>
