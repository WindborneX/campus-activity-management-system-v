<template>
  <div>
    <h2 class="page-title">我的报名</h2>

    <el-table :data="myReg.list" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="activity.title" label="活动名称" min-width="180" />
      <el-table-column prop="activity.location" label="地点" min-width="140" />
      <el-table-column label="时间" min-width="300">
        <template #default="{ row }">
          {{ row.activity.startTime }} ~ {{ row.activity.endTime }}
        </template>
      </el-table-column>
      <el-table-column prop="registeredAt" label="报名时间" min-width="170" />
      <el-table-column label="活动阶段" width="120">
        <template #default="{ row }">
          <el-tag :type="stageType(row.activity.stage)" effect="plain" round size="small">
            {{ stageLabel(row.activity.stage) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button
            v-if="row.activity.stage === 'OPEN'"
            type="danger"
            text
            @click="handleCancel(row)"
          >取消报名</el-button>
          <span v-else class="muted">不可取消</span>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && myReg.list.length === 0" description="你还没有报名任何活动" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { cancelSignup } from '../api/registrations'
import { useMyRegStore } from '../stores/myReg'

const myReg = useMyRegStore()
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    await myReg.fetch()
  } finally {
    loading.value = false
  }
}

onMounted(load)

async function handleCancel(row) {
  try {
    await ElMessageBox.confirm(`确定取消报名「${row.activity.title}」吗？`, '提示', {
      confirmButtonText: '取消报名',
      cancelButtonText: '再想想',
      type: 'warning'
    })
    await cancelSignup(row.activity.id)
    ElMessage.success('已取消报名')
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
