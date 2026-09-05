<template>
  <div class="rule-div">
    <div class="rule-header">
      <div class="rule-actions">
        <el-button type="success" size="small" @click="addRule">
          <el-icon><DocumentAdd /></el-icon>
          添加规则
        </el-button>
        <el-button type="danger" size="small" @click="deleteRuleAll" :disabled="ruleSelectedRows.length === 0">
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
      </div>
    </div>
    <!-- 规则筛选条件 -->
    <div class="rule-search-div">
      <el-form label-width="80px" size="small" inline>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="规则类型">
              <el-select v-model="ruleQueryDto.ruleType" style="width: 100%" clearable placeholder="请选择规则类型">
                <el-option v-for="item in ruleTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="规则状态">
              <el-select v-model="ruleQueryDto.ruleStatus" style="width: 100%" clearable placeholder="请选择规则状态">
                <el-option v-for="item in ruleStatusOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12" style="text-align: right;">
            <el-form-item label-width="10px">
              <el-button type="primary" size="small" @click="searchRuleData">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button size="small" @click="resetRuleData">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>
    <el-table
      :data="filteredRuleList"
      v-loading="ruleLoading"
      style="width: 100%"
      :height="ruleTableHeight"
      ref="ruleTable"
      @selection-change="handleRuleSelectionChange"
      border
      stripe
      size="small"
    >
      <el-table-column type="selection" width="40" align="center" />
      <el-table-column label="操作" align="center" fixed="left" width="160" #default="scope">
        <el-button type="primary" size="small" @click="editRule(scope.row)">
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
        <el-button type="danger" size="small" @click="deleteRule(scope.row)">
          <el-icon><Delete /></el-icon>
          删除
        </el-button>
      </el-table-column>
      <el-table-column prop="ruleCode" label="规则编号" align="center" min-width="200">
        <template #default="scope">
          <el-link type="primary" :underline="false" @click="viewRuleDetail(scope.row)">{{ scope.row.ruleCode }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="ruleType" label="规则类型" align="center" min-width="100">
        <template #default="scope">
          {{ getDisplayText(scope.row.ruleType, ruleTypeOptions) }}
        </template>
      </el-table-column>
      <el-table-column prop="ruleContent" label="规则内容" align="center" min-width="160" show-overflow-tooltip />
      <el-table-column prop="ruleDetail" label="规则细节" align="center" min-width="160" show-overflow-tooltip />
      <el-table-column prop="ruleStatus" label="规则状态" align="center" min-width="80">
        <template #default="scope">
          <el-tag :type="scope.row.ruleStatus === 1 ? 'success' : 'info'" size="small">
            {{ getDisplayText(scope.row.ruleStatus, ruleStatusOptions) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="useCount" label="使用次数" align="center" min-width="80" />
      <el-table-column prop="violateCount" label="违反次数" align="center" min-width="80" />
      <el-table-column prop="complySuccessCount" label="遵守成功次数" align="center" min-width="110" />
      <el-table-column prop="violateSuccessCount" label="违反成功次数" align="center" min-width="110" />
      <el-table-column prop="violatePenalty" label="违反惩罚" align="center" min-width="120" show-overflow-tooltip />
      <el-table-column label="遵守成功率" align="center" min-width="100">
        <template #default="scope">
          <span :class="getRateClass(calcComplySuccessRate(scope.row))">
            {{ calcComplySuccessRate(scope.row) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="违反成功率" align="center" min-width="100">
        <template #default="scope">
          <span :class="getRateClass(calcViolateSuccessRate(scope.row))">
            {{ calcViolateSuccessRate(scope.row) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" align="center" min-width="60" />
    </el-table>
  </div>

  <!-- 交易规则 详情查看对话框 -->
  <el-dialog
    v-model="ruleDetailVisible"
    title="规则详情"
    width="55%"
    class="custom-dialog enhanced-dialog"
    :close-on-click-modal="true"
  >
    <div class="rule-detail-container" v-if="ruleDetailData">
      <!-- 基本信息区 -->
      <div class="detail-section">
        <div class="detail-section-title">基本信息</div>
        <el-descriptions :column="2" border size="default">
          <el-descriptions-item label="规则编号" :span="1">
            <span class="detail-value highlight">{{ ruleDetailData.ruleCode }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="规则类型" :span="1">
            <el-tag type="warning" size="small">{{ getDisplayText(ruleDetailData.ruleType, ruleTypeOptions) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="规则状态" :span="1">
            <el-tag :type="ruleDetailData.ruleStatus === 1 ? 'success' : 'info'" size="small">
              {{ getDisplayText(ruleDetailData.ruleStatus, ruleStatusOptions) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="排序" :span="1">
            <span class="detail-value">{{ ruleDetailData.sortOrder }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="1">
            <span class="detail-value">{{ ruleDetailData.createTime }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="更新时间" :span="1">
            <span class="detail-value">{{ ruleDetailData.updateTime || '-' }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <!-- 规则内容区 -->
      <div class="detail-section">
        <div class="detail-section-title">规则内容</div>
        <el-descriptions :column="1" border size="default">
          <el-descriptions-item label="规则内容">
            <div class="detail-text-block">{{ ruleDetailData.ruleContent || '-' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="规则细节">
            <div class="detail-text-block">{{ ruleDetailData.ruleDetail || '-' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="违反惩罚">
            <div class="detail-text-block penalty-text">{{ ruleDetailData.violatePenalty || '-' }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <!-- 统计数据区 -->
      <div class="detail-section">
        <div class="detail-section-title">统计数据</div>
        <el-row :gutter="16">
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-value primary">{{ ruleDetailData.useCount || 0 }}</div>
              <div class="stat-label">使用次数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-value danger">{{ ruleDetailData.violateCount || 0 }}</div>
              <div class="stat-label">违反次数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-value success">{{ ruleDetailData.complySuccessCount || 0 }}</div>
              <div class="stat-label">遵守成功次数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-value warning">{{ ruleDetailData.violateSuccessCount || 0 }}</div>
              <div class="stat-label">违反成功次数</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 16px;">
          <el-col :span="12">
            <div class="stat-card rate-card">
              <div class="stat-value" :class="getRateClass(calcComplySuccessRate(ruleDetailData))">{{ calcComplySuccessRate(ruleDetailData) }}</div>
              <div class="stat-label">遵守规则成功率</div>
              <div class="rate-formula">遵守成功次数 / 使用次数</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="stat-card rate-card">
              <div class="stat-value" :class="getRateClass(calcViolateSuccessRate(ruleDetailData))">{{ calcViolateSuccessRate(ruleDetailData) }}</div>
              <div class="stat-label">违反规则成功率</div>
              <div class="rate-formula">违反成功次数 / 违反次数</div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="ruleDetailVisible = false">关闭</el-button>
      </span>
    </template>
  </el-dialog>

  <!-- 交易规则 添加/修改对话框 -->
  <el-dialog
    v-model="ruleDialogVisible"
    :title="ruleDialogTitle"
    width="50%"
    class="custom-dialog enhanced-dialog"
    :close-on-click-modal="false"
  >
    <el-form :model="ruleFormData" label-width="140px" :rules="ruleFormRules" ref="ruleFormRef">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="规则编号" prop="ruleCode">
            <el-input v-model="ruleFormData.ruleCode" placeholder="自动生成" :disabled="true" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="规则类型" prop="ruleType">
            <el-select v-model="ruleFormData.ruleType" style="width: 100%" placeholder="请选择规则类型">
              <el-option v-for="item in ruleTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="规则状态" prop="ruleStatus">
            <el-select v-model="ruleFormData.ruleStatus" style="width: 100%" placeholder="请选择规则状态">
              <el-option v-for="item in ruleStatusOptions" :key="item.value" :label="item.text" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="排序" prop="sortOrder">
            <el-input-number v-model="ruleFormData.sortOrder" :min="0" style="width: 100%" placeholder="排序" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="规则内容" prop="ruleContent">
            <el-input v-model="ruleFormData.ruleContent" type="textarea" :rows="2" placeholder="请输入规则内容" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="规则细节" prop="ruleDetail">
            <el-input v-model="ruleFormData.ruleDetail" type="textarea" :rows="2" placeholder="请输入规则细节" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="违反惩罚" prop="violatePenalty">
            <el-input v-model="ruleFormData.violatePenalty" type="textarea" :rows="2" placeholder="请输入违反规则惩罚" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="使用次数" prop="useCount">
            <el-input-number v-model="ruleFormData.useCount" :min="0" style="width: 100%" :disabled="!ruleFormData.id" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="违反次数" prop="violateCount">
            <el-input-number v-model="ruleFormData.violateCount" :min="0" style="width: 100%" :disabled="!ruleFormData.id" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="遵守成功次数" prop="complySuccessCount">
            <el-input-number v-model="ruleFormData.complySuccessCount" :min="0" style="width: 100%" :disabled="!ruleFormData.id" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="违反成功次数" prop="violateSuccessCount">
            <el-input-number v-model="ruleFormData.violateSuccessCount" :min="0" style="width: 100%" :disabled="!ruleFormData.id" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="ruleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRule">提交</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, DocumentAdd, Delete, Edit } from '@element-plus/icons-vue'
import { GetKeyAndValueByType } from "@/api/sysDict"
import { GetTransactionRuleList, SaveTransactionRule, DeleteTransactionRuleById, DeleteAllTransactionRuleByIds } from "@/api/trialExecutionArea/transactionSystemTrial"
import { getDisplayText } from "@/utils/common"

// ==================== 规则表格动态高度 ====================
const ruleTableHeight = ref(320)

const calcRuleTableHeight = () => {
  nextTick(() => {
    const HEADER_H = 68, TABS_H = 44, TOOLBAR_H = 50, SEARCH_H = 56, PAGINATION_H = 30, PADDING = 10
    const occupiedHeight = HEADER_H + TABS_H + TOOLBAR_H + SEARCH_H + PAGINATION_H + PADDING
    const availableHeight = window.innerHeight - occupiedHeight
    ruleTableHeight.value = Math.max(availableHeight, 200)
  })
}

const handleResize = () => {
  calcRuleTableHeight()
}

window.addEventListener('resize', handleResize)

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})

// ==================== 数据字典 ====================
const loadDict = async (tableName, optionsRef) => {
  const result = await GetKeyAndValueByType(tableName)
  optionsRef.value = result.data || []
}

// 规则状态选项（特殊：补"作废"项）
const ruleStatusOptions = ref([])
const getRuleStatusItem = async () => {
  const result = await GetKeyAndValueByType("t_trial_rule_status")
  const data = result.data || []
  if (!data.find(item => item.value === 0)) {
    data.push({ value: 0, text: '作废' })
  }
  ruleStatusOptions.value = data
}
// 规则类型选项
const ruleTypeOptions = ref([])

// ==================== 交易规则 ====================
const ruleList = ref([])
const ruleTable = ref(null)
const ruleSelectedRows = ref([])

// 规则筛选条件
const ruleQueryDto = reactive({
  ruleType: null,
  ruleStatus: 1
})

// 实际用于过滤的条件（点击搜索后才更新）
const activeRuleQuery = reactive({
  ruleType: null,
  ruleStatus: 1
})

// 根据筛选条件过滤规则列表
const filteredRuleList = computed(() => {
  return ruleList.value.filter(item => {
    if (activeRuleQuery.ruleType !== null && activeRuleQuery.ruleType !== '' && item.ruleType !== activeRuleQuery.ruleType) {
      return false
    }
    if (activeRuleQuery.ruleStatus !== null && activeRuleQuery.ruleStatus !== '' && item.ruleStatus !== activeRuleQuery.ruleStatus) {
      return false
    }
    return true
  })
})

// 计算遵守规则成功率 = 遵守成功次数 / 使用次数
const calcComplySuccessRate = (row) => {
  const useCount = row.useCount || 0
  const complySuccessCount = row.complySuccessCount || 0
  if (useCount === 0) return '-'
  return (complySuccessCount / useCount * 100).toFixed(1) + '%'
}

// 计算违反规则成功率 = 违反成功次数 / 违反次数
const calcViolateSuccessRate = (row) => {
  const violateCount = row.violateCount || 0
  const violateSuccessCount = row.violateSuccessCount || 0
  if (violateCount === 0) return '-'
  return (violateSuccessCount / violateCount * 100).toFixed(1) + '%'
}

// 根据成功率返回样式类名
const getRateClass = (rateStr) => {
  if (rateStr === '-') return 'rate-text rate-none'
  const numVal = parseFloat(rateStr)
  if (numVal >= 60) return 'rate-text rate-high'
  if (numVal >= 30) return 'rate-text rate-medium'
  return 'rate-text rate-low'
}

// 规则搜索（点击搜索时才应用筛选条件）
const searchRuleData = () => {
  activeRuleQuery.ruleType = ruleQueryDto.ruleType
  activeRuleQuery.ruleStatus = ruleQueryDto.ruleStatus
}

// 规则重置
const resetRuleData = () => {
  ruleQueryDto.ruleType = null
  ruleQueryDto.ruleStatus = 1
  activeRuleQuery.ruleType = null
  activeRuleQuery.ruleStatus = 1
}

// 获取规则数据
const ruleLoading = ref(false)
const fetchRuleData = async () => {
  ruleLoading.value = true
  try {
    const result = await GetTransactionRuleList()
    ruleList.value = result.data || []
  } catch (error) {
    console.error('规则数据加载失败:', error)
  }
  finally { ruleLoading.value = false }
}

// 规则选择变化
const handleRuleSelectionChange = (selection) => {
  ruleSelectedRows.value = selection
}

// 规则详情查看
const ruleDetailVisible = ref(false)
const ruleDetailData = ref(null)
const viewRuleDetail = (row) => {
  ruleDetailData.value = { ...row }
  ruleDetailVisible.value = true
}

// 生成规则编号：RULE-年月日时分秒
const generateRuleCode = () => {
  const now = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  const dateStr = `${now.getFullYear()}${pad(now.getMonth() + 1)}${pad(now.getDate())}${pad(now.getHours())}${pad(now.getMinutes())}${pad(now.getSeconds())}`
  return `RULE${dateStr}`
}

// 添加规则
const ruleDialogVisible = ref(false)
const ruleDialogTitle = ref('添加规则')
const ruleFormRef = ref(null)
const ruleFormData = reactive({
  id: null,
  ruleCode: '',
  ruleType: '',
  ruleContent: '',
  ruleDetail: '',
  sortOrder: 0,
  ruleStatus: 1,
  violatePenalty: '',
  useCount: 0,
  violateCount: 0,
  complySuccessCount: 0,
  violateSuccessCount: 0
})

const ruleFormRules = {
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  ruleContent: [{ required: true, message: '请输入规则内容', trigger: 'blur' }]
}

const addRule = () => {
  ruleDialogTitle.value = '添加规则'
  if (ruleFormRef.value) {
    ruleFormRef.value.resetFields()
  }
  Object.assign(ruleFormData, {
    id: null,
    ruleCode: generateRuleCode(),
    ruleType: '',
    ruleContent: '',
    ruleDetail: '',
    sortOrder: 0,
    ruleStatus: 1,
    violatePenalty: '',
    useCount: 0,
    violateCount: 0,
    complySuccessCount: 0,
    violateSuccessCount: 0
  })
  ruleDialogVisible.value = true
}

const editRule = (row) => {
  ruleDialogTitle.value = '编辑规则'
  if (ruleFormRef.value) {
    ruleFormRef.value.resetFields()
  }
  Object.assign(ruleFormData, row)
  ruleDialogVisible.value = true
}

const submitRule = async () => {
  if (!ruleFormRef.value) return
  try {
    const valid = await ruleFormRef.value.validate()
    if (!valid) return
  } catch (error) {
    return
  }
  try {
    await SaveTransactionRule(ruleFormData)
    ElMessage.success(ruleFormData.id ? '编辑规则成功' : '添加规则成功')
    ruleDialogVisible.value = false
    fetchRuleData()
  } catch (error) {
    ElMessage.error(ruleFormData.id ? '编辑规则失败' : '添加规则失败')
  }
}

const deleteRule = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该规则吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await DeleteTransactionRuleById(row.id)
    ElMessage.success('删除规则成功')
    fetchRuleData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除规则失败')
    }
  }
}

const deleteRuleAll = async () => {
  if (!ruleSelectedRows.value || ruleSelectedRows.value.length === 0) {
    ElMessage.warning('请先选择要删除的规则')
    return
  }
  try {
    await ElMessageBox.confirm(
      '确定要批量删除选中的 ' + ruleSelectedRows.value.length + ' 条规则吗？',
      '警告',
      { type: 'warning' }
    )
    const ids = ruleSelectedRows.value.map(item => item.id)
    await DeleteAllTransactionRuleByIds(ids)
    ElMessage.success('批量删除规则成功')
    fetchRuleData()
    ruleTable.value.clearSelection()
    ruleSelectedRows.value = []
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除规则失败')
    }
  }
}

//--------------------钩子函数-------------------------
onMounted(() => {
  calcRuleTableHeight()

  Promise.all([
    getRuleStatusItem(),
    loadDict('t_trial_rule_type', ruleTypeOptions)
  ]).catch(err => console.error('字典加载失败:', err))

  fetchRuleData()
})
</script>

<style scoped>
/* 交易规则块 */
.rule-div {
  padding: 15px;
  border: 1px solid #ebeef5;
  border-top: none;
  border-radius: 0 0 4px 4px;
  background-color: rgba(255, 255, 255, 0.8);
}

.rule-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.rule-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.rule-actions {
  display: flex;
  gap: 8px;
}

.rule-search-div {
  margin-bottom: 10px;
}

/* 操作按钮美化 */
.beautified-tools .el-button {
  border-radius: 8px;
  padding: 10px 20px;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  letter-spacing: 0.5px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 表格内按钮美化 */
:deep(.el-table .el-button) {
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 600;
  border: none;
}

:deep(.el-table .el-button--primary) {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.3);
}

:deep(.el-table .el-button--danger) {
  background: linear-gradient(135deg, #F56C6C 0%, #f78989 100%);
  box-shadow: 0 2px 6px rgba(245, 108, 108, 0.3);
}

/* 对话框按钮美化 */
.dialog-footer .el-button {
  border-radius: 8px;
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 600;
}

.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

/* 对话框样式 */
:deep(.custom-dialog),
:deep(.enhanced-dialog) {
  border-radius: 16px !important;
  overflow: hidden !important;
  box-shadow: 0 16px 48px rgba(102, 126, 234, 0.25) !important;
}

:deep(.enhanced-dialog .el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border-radius: 16px 16px 0 0 !important;
  padding: 24px 28px !important;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3) !important;
  position: relative;
  margin: 0 !important;
  width: 100% !important;
  box-sizing: border-box !important;
}

:deep(.enhanced-dialog .el-dialog__title) {
  color: white !important;
  font-weight: 700 !important;
  font-size: 22px !important;
}

:deep(.enhanced-dialog .el-dialog__headerbtn .el-dialog__close) {
  color: white !important;
  font-size: 22px !important;
}

:deep(.enhanced-dialog .el-dialog__body) {
  padding: 28px !important;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%) !important;
  max-height: 60vh !important;
  overflow-y: auto !important;
}

:deep(.enhanced-dialog .el-dialog__footer) {
  padding: 20px 28px !important;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%) !important;
  border-top: 2px solid rgba(102, 126, 234, 0.1) !important;
}

/* 规则详情弹窗样式 */
.rule-detail-container {
  padding: 0 4px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.detail-section-title {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 12px;
  padding-left: 10px;
  border-left: 4px solid #667eea;
  line-height: 1;
}

.detail-value {
  font-size: 14px;
  color: #606266;
}

.detail-value.highlight {
  color: #667eea;
  font-weight: 600;
  font-family: 'Courier New', monospace;
}

.detail-text-block {
  font-size: 14px;
  color: #303133;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-all;
}

.detail-text-block.penalty-text {
  color: #e6a23c;
  font-weight: 500;
}

.stat-card {
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 20px 16px;
  text-align: center;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 8px;
}

.stat-value.primary { color: #409EFF; }
.stat-value.danger { color: #F56C6C; }
.stat-value.success { color: #67C23A; }
.stat-value.warning { color: #E6A23C; }

.stat-label {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

/* 成功率文字样式 */
.rate-text {
  font-weight: 600;
  font-size: 13px;
}

.rate-none { color: #c0c4cc; }
.rate-high { color: #67C23A; }
.rate-medium { color: #E6A23C; }
.rate-low { color: #F56C6C; }

/* 成功率卡片样式 */
.rate-card {
  position: relative;
  padding: 24px 16px;
}

.rate-card .rate-formula {
  font-size: 12px;
  color: #b0b5bd;
  margin-top: 6px;
  font-style: italic;
}
</style>