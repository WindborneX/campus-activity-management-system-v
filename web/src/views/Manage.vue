<template>
  <div>
    <div class="page-head">
      <h2 class="page-title" style="margin:0">活动管理</h2>
      <el-button type="primary" round @click="openCreate">+ 发布活动</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="title" label="活动名称" min-width="180" />
      <el-table-column label="阶段" width="110">
        <template #default="{ row }">
          <el-tag :type="stageType(row.stage)" effect="plain" round size="small">{{ stageLabel(row.stage) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="报名" width="110">
        <template #default="{ row }">{{ row.currentCount }} / {{ row.maxParticipants }}</template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="170" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" :disabled="row.stage === 'CANCELLED'" @click="openEdit(row)">编辑</el-button>
          <el-button text type="primary" @click="showRegistrations(row)">名单</el-button>
          <el-button text type="danger" :disabled="row.stage === 'CANCELLED'" @click="handleCancel(row)">取消活动</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && list.length === 0" description="你还没有发布任何活动" />

    <!-- 创建 / 编辑 弹窗 -->
    <el-dialog v-model="dialogVisible" :title="mode === 'create' ? '发布活动' : '编辑活动'" width="560px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="活动标题" prop="title">
          <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="请输入活动标题" />
        </el-form-item>
        <el-form-item label="活动简介" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="2000" show-word-limit placeholder="请输入活动简介" />
        </el-form-item>
        <el-form-item label="活动地点" prop="location">
          <el-input v-model="form.location" maxlength="100" placeholder="请输入活动地点" />
        </el-form-item>
        <el-form-item label="报名截止" prop="signupDeadline">
          <el-date-picker v-model="form.signupDeadline" type="datetime" placeholder="选择报名截止时间" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择活动开始时间" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择活动结束时间" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="人数上限" prop="maxParticipants">
          <el-input-number v-model="form.maxParticipants" :min="1" :max="10000" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确认</el-button>
      </template>
    </el-dialog>

    <!-- 报名名单弹窗 -->
    <el-dialog v-model="regDialogVisible" title="报名名单" width="560px">
      <el-table :data="regList" v-loading="regLoading" stripe size="small">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="registeredAt" label="报名时间" width="180" />
      </el-table>
      <el-empty v-if="!regLoading && regList.length === 0" description="暂无学生报名" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  myActivities, createActivity, updateActivity, cancelActivity, activityRegistrations
} from '../api/activities'

const list = ref([])
const loading = ref(false)
const submitting = ref(false)

// 弹窗
const dialogVisible = ref(false)
const mode = ref('create') // create | edit
const editingId = ref(null)
const formRef = ref()
const form = reactive({
  title: '', description: '', location: '',
  signupDeadline: '', startTime: '', endTime: '',
  maxParticipants: 1
})

const rules = {
  title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
  location: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
  signupDeadline: [{ required: true, message: '请选择报名截止时间', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  maxParticipants: [{ required: true, message: '请输入人数上限', trigger: 'blur' }]
}

// 名单
const regDialogVisible = ref(false)
const regList = ref([])
const regLoading = ref(false)

async function load() {
  loading.value = true
  try {
    list.value = await myActivities()
  } finally {
    loading.value = false
  }
}

onMounted(load)

function resetForm() {
  form.title = ''; form.description = ''; form.location = ''
  form.signupDeadline = ''; form.startTime = ''; form.endTime = ''
  form.maxParticipants = 1
  editingId.value = null
  formRef.value?.clearValidate()
}

function openCreate() {
  mode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  mode.value = 'edit'
  editingId.value = row.id
  form.title = row.title
  form.description = row.description || ''
  form.location = row.location
  form.signupDeadline = row.signupDeadline
  form.startTime = row.startTime
  form.endTime = row.endTime
  form.maxParticipants = row.maxParticipants
  dialogVisible.value = true
}

/** 前端校验时间先后，后端再兜底 */
function validateTime() {
  if (form.signupDeadline >= form.startTime) {
    ElMessage.error('报名截止时间必须早于活动开始时间')
    return false
  }
  if (form.startTime >= form.endTime) {
    ElMessage.error('活动开始时间必须早于结束时间')
    return false
  }
  return true
}

async function submit() {
  await formRef.value.validate()
  if (!validateTime()) return
  submitting.value = true
  try {
    if (mode.value === 'create') {
      await createActivity({ ...form })
      ElMessage.success('发布成功')
    } else {
      await updateActivity(editingId.value, { ...form })
      ElMessage.success('修改成功')
    }
    dialogVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

async function handleCancel(row) {
  try {
    await ElMessageBox.confirm(
      `确定取消活动「${row.title}」吗？取消后无法恢复，且学生不能再报名。`,
      '取消活动',
      { confirmButtonText: '取消活动', cancelButtonText: '再想想', type: 'warning' }
    )
    await cancelActivity(row.id)
    ElMessage.success('活动已取消')
    load()
  } catch (e) {
    if (e !== 'cancel') { /* 拦截器已提示 */ }
  }
}

async function showRegistrations(row) {
  regDialogVisible.value = true
  regLoading.value = true
  try {
    regList.value = await activityRegistrations(row.id)
  } finally {
    regLoading.value = false
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
.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}
</style>
