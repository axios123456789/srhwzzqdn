<template>
  <div class="trial-div">
    <!-- 搜索表单区域 -->
    <div class="search-div">
      <div class="search-header">
        <span class="search-title">查询条件</span>
        <el-button
          type="text"
          size="small"
          @click="toggleSearchExpand"
          class="expand-btn"
        >
          <el-icon>
            <ArrowDown v-if="!searchExpanded" />
            <ArrowUp v-else />
          </el-icon>
          {{ searchExpanded ? '收起' : '展开' }}
        </el-button>
      </div>
      <el-form label-width="120px" size="small">
        <el-row>
          <el-col :span="6">
            <el-form-item label="交易对象">
              <el-input v-model="queryDto.targetName" style="width: 100%" clearable placeholder="请输入交易对象名称" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="交易类型">
              <el-select v-model="queryDto.tradeType" style="width: 100%" clearable placeholder="请选择交易类型" multiple>
                <el-option v-for="item in tradeTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划时间">
              <el-date-picker
                v-model="planTimeArea"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                style="width: 100%"
                value-format="YYYY-MM-DD HH:mm:ss"
                :unlink-panels="true"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <transition name="el-zoom-in-top">
          <div v-show="searchExpanded">
            <el-row>
              <el-col :span="6">
                <el-form-item label="计划类型">
                  <el-select v-model="queryDto.planType" style="width: 100%" clearable placeholder="请选择计划类型" multiple>
                    <el-option v-for="item in planTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="交易状态">
                  <el-select v-model="queryDto.tradeStatus" style="width: 100%" clearable placeholder="请选择交易状态" multiple>
                    <el-option v-for="item in tradeStatusOptions" :key="item.value" :label="item.text" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="交易结果">
                  <el-select v-model="queryDto.tradeResult" style="width: 100%" clearable placeholder="请选择交易结果" multiple>
                    <el-option v-for="item in tradeResultOptions" :key="item.value" :label="item.text" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="失败类型">
                  <el-select v-model="queryDto.tradeFailType" style="width: 100%" clearable placeholder="请选择失败类型" multiple>
                    <el-option v-for="item in tradeFailTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="6">
                <el-form-item label="是否触发计划">
                  <el-select v-model="queryDto.isUsePlan" style="width: 100%" clearable placeholder="请选择">
                    <el-option v-for="item in isUsePlanOptions" :key="item.value" :label="item.text" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </transition>
        <el-row>
          <el-col :span="24" style="text-align: right;">
            <el-form-item label-width="10px">
              <el-button type="primary" size="small" @click="searchData" class="beautified-search-btn">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button size="small" @click="resetData" class="beautified-reset-btn">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>
    <!-- 操作按钮区域 -->
    <div class="tools-div beautified-tools" style="text-align: right;">
      <el-button type="success" size="small" @click="addRecord">
        <el-icon><DocumentAdd /></el-icon>
        添加试验
      </el-button>
      <el-button type="danger" size="small" @click="deleteSelectAll">
        <el-icon><Delete /></el-icon>
        批量删除
      </el-button>
      <el-button type="info" size="small" @click="showExportDialog">
        <el-icon><Download /></el-icon>
        一键导出
      </el-button>
    </div>

    <!-- 数据表格 -->
    <el-table
      :data="list"
      v-loading="listLoading"
      style="width: 100%"
      height="450"
      ref="multipleTable"
      @selection-change="handleSelectionChange"
      border
      stripe
    >
      <el-table-column type="selection" width="40" align="center" />
      <el-table-column label="操作" align="center" fixed="left" width="240" #default="scope">
        <el-button type="info" size="small" @click="viewTrialDetail(scope.row)">
          <el-icon><View /></el-icon>
          查看
        </el-button>
        <el-button type="primary" size="small" @click="editRecord(scope.row)">
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
        <el-button type="danger" size="small" @click="deleteRecord(scope.row)">
          <el-icon><Delete /></el-icon>
          删除
        </el-button>
      </el-table-column>
      <el-table-column prop="targetName" label="交易对象" align="center" width="120" show-overflow-tooltip />
      <el-table-column prop="tradeType" label="交易类型" align="center" width="100">
        <template #default="scope">
          {{ getDisplayText(scope.row.tradeType, tradeTypeOptions) }}
        </template>
      </el-table-column>
      <el-table-column prop="planType" label="计划类型" align="center" width="100">
        <template #default="scope">
          {{ getDisplayText(scope.row.planType, planTypeOptions) }}
        </template>
      </el-table-column>
      <el-table-column prop="currentPrice" label="当前价" align="center" width="90">
        <template #default="scope">
          {{ scope.row.currentPrice != null ? Number(scope.row.currentPrice).toFixed(2) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="planPrice" label="计划价" align="center" width="90">
        <template #default="scope">
          {{ scope.row.planPrice != null ? Number(scope.row.planPrice).toFixed(2) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="actualPrice" label="成交价" align="center" width="90">
        <template #default="scope">
          <span style="color: #409EFF; font-weight: 600;">
            {{ scope.row.actualPrice != null ? Number(scope.row.actualPrice).toFixed(2) : '-' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="tradeStatus" label="交易状态" align="center" width="100">
        <template #default="scope">
          <el-tag :type="getTradeStatusTagType(scope.row.tradeStatus)" size="small">
            {{ getDisplayText(scope.row.tradeStatus, tradeStatusOptions) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="tradeResult" label="交易结果" align="center" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.tradeResult === 1 ? 'success' : 'danger'" size="small">
            {{ getDisplayText(scope.row.tradeResult, tradeResultOptions) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="tradeFailType" label="失败类型" align="center" width="100">
        <template #default="scope">
          {{ getDisplayText(scope.row.tradeFailType, tradeFailTypeOptions) }}
        </template>
      </el-table-column>
      <el-table-column prop="isUsePlan" label="触发计划" align="center" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.isUsePlan === 1 ? 'success' : 'info'" size="small">
            {{ scope.row.isUsePlan === 1 ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="planContent" label="计划内容" align="center" min-width="150" show-overflow-tooltip />
      <el-table-column prop="planStartTime" label="计划开始时间" align="center" width="160" />
      <el-table-column prop="resultReview" label="结果复盘" align="center" min-width="150" show-overflow-tooltip />
    </el-table>

    <!-- 分页组件 -->
    <el-pagination
      style="margin-top: 30px"
      v-model:current-page="pageParams.page"
      v-model:page-size="pageParams.limit"
      :page-sizes="PAGE_SIZES"
      @size-change="fetchData"
      @current-change="fetchData"
      layout="total, sizes, prev, pager, next"
      :total="total"
    />
  </div>

  <!-- 交易试验 添加/修改对话框 -->
  <el-dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="60%"
    class="custom-dialog enhanced-dialog"
    :close-on-click-modal="false"
  >
    <el-form :model="formData" label-width="120px" :rules="formRules" ref="formRef">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="交易对象" prop="targetName">
            <el-input v-model="formData.targetName" placeholder="请输入交易对象名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="交易类型" prop="tradeType">
            <el-select v-model="formData.tradeType" style="width: 100%" placeholder="请选择交易类型">
              <el-option v-for="item in tradeTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="计划类型" prop="planType">
            <el-select v-model="formData.planType" style="width: 100%" placeholder="请选择计划类型">
              <el-option v-for="item in planTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="交易状态" prop="tradeStatus">
            <el-select v-model="formData.tradeStatus" style="width: 100%" placeholder="请选择交易状态">
              <el-option v-for="item in tradeStatusOptions" :key="item.value" :label="item.text" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="计划开始时间" prop="planStartTime">
            <el-date-picker v-model="formData.planStartTime" type="datetime" placeholder="请选择计划开始时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="计划结束时间" prop="planEndTime">
            <el-date-picker v-model="formData.planEndTime" type="datetime" placeholder="请选择计划结束时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="当前价" prop="currentPrice">
            <el-input-number v-model="formData.currentPrice" :precision="2" style="width: 100%" placeholder="当前价" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="计划价" prop="planPrice">
            <el-input-number v-model="formData.planPrice" :precision="2" style="width: 100%" placeholder="计划交易价" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="开盘价" prop="openPrice">
            <el-input-number v-model="formData.openPrice" :precision="2" style="width: 100%" placeholder="开盘价" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="成交价" prop="actualPrice">
            <el-input-number v-model="formData.actualPrice" :precision="2" style="width: 100%" placeholder="实际成交价" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="收盘价" prop="closePrice">
            <el-input-number v-model="formData.closePrice" :precision="2" style="width: 100%" placeholder="收盘价" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="是否触发计划" prop="isUsePlan">
            <el-select v-model="formData.isUsePlan" style="width: 100%" placeholder="请选择">
              <el-option v-for="item in isUsePlanOptions" :key="item.value" :label="item.text" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="交易结果" prop="tradeResult">
            <el-select v-model="formData.tradeResult" style="width: 100%" placeholder="请选择交易结果">
              <el-option v-for="item in tradeResultOptions" :key="item.value" :label="item.text" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="失败类型" prop="tradeFailType">
            <el-select v-model="formData.tradeFailType" style="width: 100%" clearable placeholder="请选择失败类型">
              <el-option v-for="item in tradeFailTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="遵守规则" prop="complyRuleIds">
            <el-select v-model="formData.complyRuleIds" style="width: 100%" multiple clearable placeholder="请选择遵守规则">
              <el-option v-for="item in ruleList" :key="item.id" :label="item.ruleCode + ' - ' + getDisplayText(item.ruleType, ruleTypeOptions) + ' - ' + item.ruleContent" :value="item.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="违反规则" prop="violateRuleIds">
            <el-select v-model="formData.violateRuleIds" style="width: 100%" multiple clearable placeholder="请选择违反规则">
              <el-option v-for="item in ruleList" :key="item.id" :label="item.ruleCode + ' - ' + getDisplayText(item.ruleType, ruleTypeOptions) + ' - ' + item.ruleContent" :value="item.id" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="计划内容" prop="planContent">
            <el-input v-model="formData.planContent" type="textarea" :rows="2" placeholder="请输入交易计划内容" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="结果复盘" prop="resultReview">
            <el-input v-model="formData.resultReview" type="textarea" :rows="2" placeholder="请输入结果复盘" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">提交</el-button>
      </span>
    </template>
  </el-dialog>

  <!-- 交易试验记录 详情查看对话框 -->
  <el-dialog
    v-model="trialDetailVisible"
    title="试验记录详情"
    width="60%"
    class="custom-dialog enhanced-dialog"
    :close-on-click-modal="true"
  >
    <div class="trial-detail-container" v-if="trialDetailData">
      <!-- 交易信息区 -->
      <div class="detail-section">
        <div class="detail-section-title">交易信息</div>
        <el-descriptions :column="2" border size="default">
          <el-descriptions-item label="交易对象" :span="1">
            <span class="detail-value highlight">{{ trialDetailData.targetName || '-' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="交易类型" :span="1">
            <el-tag type="warning" size="small">{{ getDisplayText(trialDetailData.tradeType, tradeTypeOptions) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="计划类型" :span="1">
            <el-tag size="small">{{ getDisplayText(trialDetailData.planType, planTypeOptions) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="交易状态" :span="1">
            <el-tag :type="getTradeStatusTagType(trialDetailData.tradeStatus)" size="small">
              {{ getDisplayText(trialDetailData.tradeStatus, tradeStatusOptions) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="交易结果" :span="1">
            <el-tag :type="trialDetailData.tradeResult === 1 ? 'success' : 'danger'" size="small">
              {{ getDisplayText(trialDetailData.tradeResult, tradeResultOptions) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="失败类型" :span="1">
            <span class="detail-value">{{ getDisplayText(trialDetailData.tradeFailType, tradeFailTypeOptions) || '-' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="是否触发计划" :span="1">
            <el-tag :type="trialDetailData.isUsePlan === 1 ? 'success' : 'info'" size="small">
              {{ trialDetailData.isUsePlan === 1 ? '是' : '否' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="1">
            <span class="detail-value">{{ trialDetailData.createTime || '-' }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <!-- 价格信息区 -->
      <div class="detail-section">
        <div class="detail-section-title">价格信息</div>
        <el-row :gutter="16">
          <el-col :span="4">
            <div class="stat-card">
              <div class="stat-value primary">{{ trialDetailData.currentPrice != null ? Number(trialDetailData.currentPrice).toFixed(2) : '-' }}</div>
              <div class="stat-label">当前价</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-card">
              <div class="stat-value primary">{{ trialDetailData.planPrice != null ? Number(trialDetailData.planPrice).toFixed(2) : '-' }}</div>
              <div class="stat-label">计划价</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-card">
              <div class="stat-value success">{{ trialDetailData.openPrice != null ? Number(trialDetailData.openPrice).toFixed(2) : '-' }}</div>
              <div class="stat-label">开盘价</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-card">
              <div class="stat-value warning">{{ trialDetailData.actualPrice != null ? Number(trialDetailData.actualPrice).toFixed(2) : '-' }}</div>
              <div class="stat-label">成交价</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-card">
              <div class="stat-value danger">{{ trialDetailData.closePrice != null ? Number(trialDetailData.closePrice).toFixed(2) : '-' }}</div>
              <div class="stat-label">收盘价</div>
            </div>
          </el-col>
        </el-row>
      </div>
      <!-- 计划信息区 -->
      <div class="detail-section">
        <div class="detail-section-title">计划信息</div>
        <el-descriptions :column="2" border size="default">
          <el-descriptions-item label="计划开始时间" :span="1">
            <span class="detail-value">{{ trialDetailData.planStartTime || '-' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="计划结束时间" :span="1">
            <span class="detail-value">{{ trialDetailData.planEndTime || '-' }}</span>
          </el-descriptions-item>
        </el-descriptions>
        <el-descriptions :column="1" border size="default" style="margin-top: 0;">
          <el-descriptions-item label="计划内容">
            <div class="detail-text-block">{{ trialDetailData.planContent || '-' }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <!-- 规则关联区 -->
      <div class="detail-section">
        <div class="detail-section-title">规则关联</div>
        <el-descriptions :column="2" border size="default">
          <el-descriptions-item label="遵守规则">
            <div class="detail-text-block">{{ getRuleNames(trialDetailData.complyRuleIds) }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="违反规则">
            <div class="detail-text-block penalty-text">{{ getRuleNames(trialDetailData.violateRuleIds) }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <!-- 结果复盘区 -->
      <div class="detail-section">
        <div class="detail-section-title">结果复盘</div>
        <el-descriptions :column="1" border size="default">
          <el-descriptions-item label="结果复盘">
            <div class="detail-text-block">{{ trialDetailData.resultReview || '-' }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="trialDetailVisible = false">关闭</el-button>
      </span>
    </template>
  </el-dialog>

  <!-- 导出对话框 -->
  <ExportDialog
    v-model="exportDialogVisible"
    v-model:export-scope="exportScope"
    v-model:export-file-name="exportFileName"
    v-model:selected-columns="selectedColumns"
    :available-columns="exportColumns"
    :export-loading="exportLoading"
    :current-count="list.length"
    :total-count="total"
    @confirm="handleExport"
    @closed="resetExport"
  />
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, DocumentAdd, Delete, Download, View, Edit } from '@element-plus/icons-vue'
import { GetKeyAndValueByType } from "@/api/sysDict"
import { GetTransactionSystemTrialByConditionAndPage, SaveTransactionSystemTrial, DeleteTransactionSystemTrialById, DeleteAllTransactionSystemTrialByIds, GetTransactionRuleList } from "@/api/trialExecutionArea/transactionSystemTrial"
import { getDisplayText } from "@/utils/common"
import { useExport } from "@/components/Export/hooks/useExport"
import ExportDialog from '@/components/Export/ExportDialog.vue'

// ==================== 通用常量 ====================
const PAGE_SIZES = [10, 20, 50, 100]

// ==================== 数据字典 ====================
const loadDict = async (tableName, optionsRef) => {
  const result = await GetKeyAndValueByType(tableName)
  optionsRef.value = result.data || []
}

const tradeTypeOptions = ref([])
const planTypeOptions = ref([])
const tradeStatusOptions = ref([])
const tradeResultOptions = ref([])
const getTradeResultItem = async () => {
  const result = await GetKeyAndValueByType("t_trial_transaction_result")
  const data = result.data || []
  if (!data.find(item => item.value === 0)) {
    data.push({ value: 0, text: '失败' })
  }
  tradeResultOptions.value = data
}
const tradeFailTypeOptions = ref([])
const isUsePlanOptions = ref([
  { value: 1, text: '是' },
  { value: 0, text: '否' }
])
const ruleStatusOptions = ref([])
const getRuleStatusItem = async () => {
  const result = await GetKeyAndValueByType("t_trial_rule_status")
  const data = result.data || []
  if (!data.find(item => item.value === 0)) {
    data.push({ value: 0, text: '作废' })
  }
  ruleStatusOptions.value = data
}
const ruleTypeOptions = ref([])

// ==================== 交易规则列表（跨页依赖：getRuleNames 和表单规则选择） ====================
const ruleList = ref([])
const fetchRuleData = async () => {
  try {
    const result = await GetTransactionRuleList()
    ruleList.value = result.data || []
  } catch (error) {
    console.error('规则数据加载失败:', error)
  }
}

// ==================== 交易试验记录 ====================
const list = ref([])
const total = ref(0)
const pageParams = reactive({ page: 1, limit: 10 })
const searchExpanded = ref(false)
const planTimeArea = ref([])
const queryDto = reactive({
  targetName: '',
  tradeType: [],
  planType: [],
  planStartTime: null,
  planEndTime: null,
  tradeStatus: [],
  tradeResult: [],
  tradeFailType: [],
  isUsePlan: null
})

// 获取数据
const listLoading = ref(false)
const fetchData = async () => {
  listLoading.value = true
  try {
    const result = await GetTransactionSystemTrialByConditionAndPage(pageParams.page, pageParams.limit, queryDto)
    if (result.code === 200) {
      const pageInfo = result.data || {}
      list.value = pageInfo.list || []
      total.value = pageInfo.total || 0
    } else {
      ElMessage.error(result.message || "查询失败")
    }
  } catch (error) {
    ElMessage.error("查询失败")
  }
  finally { listLoading.value = false }
}

// 搜索
const searchData = () => {
  queryDto.planStartTime = planTimeArea.value && planTimeArea.value.length > 0 ? planTimeArea.value[0] : null
  queryDto.planEndTime = planTimeArea.value && planTimeArea.value.length > 0 ? planTimeArea.value[1] : null
  pageParams.page = 1
  fetchData()
}

// 切换搜索条件展开/收起
const toggleSearchExpand = () => {
  searchExpanded.value = !searchExpanded.value
}

// 重置
const resetData = () => {
  planTimeArea.value = []
  Object.assign(queryDto, {
    targetName: '',
    tradeType: [],
    planType: [],
    planStartTime: null,
    planEndTime: null,
    tradeStatus: [],
    tradeResult: [],
    tradeFailType: [],
    isUsePlan: null
  })
  pageParams.page = 1
  fetchData()
}

// ==================== 添加/修改试验 ====================
const dialogVisible = ref(false)
const dialogTitle = ref('添加试验')
const formRef = ref(null)
const formData = reactive({
  id: null,
  tradeType: null,
  targetName: '',
  planType: null,
  planContent: '',
  planStartTime: '',
  planEndTime: '',
  currentPrice: null,
  planPrice: null,
  openPrice: null,
  actualPrice: null,
  closePrice: null,
  tradeStatus: null,
  complyRuleIds: [],
  violateRuleIds: [],
  tradeResult: null,
  tradeFailType: null,
  resultReview: '',
  isUsePlan: null
})

const formRules = {
  targetName: [{ required: true, message: '请输入交易对象名称', trigger: 'blur' }],
  tradeType: [{ required: true, message: '请选择交易类型', trigger: 'change' }],
  tradeStatus: [{ required: true, message: '请选择交易状态', trigger: 'change' }]
}

// 获取下一个交易日的开盘和收盘时间（A股：9:30-15:00）
const getNextTradingTime = () => {
  const now = new Date()
  let next = new Date(now)
  next.setDate(next.getDate() + 1)
  // 跳过周末
  const day = next.getDay()
  if (day === 0) next.setDate(next.getDate() + 1) // 周日->周一
  if (day === 6) next.setDate(next.getDate() + 2) // 周六->周一
  const pad = (n) => String(n).padStart(2, '0')
  const dateStr = `${next.getFullYear()}-${pad(next.getMonth() + 1)}-${pad(next.getDate())}`
  return {
    planStartTime: `${dateStr} 09:30:00`,
    planEndTime: `${dateStr} 15:00:00`
  }
}

const addRecord = () => {
  dialogTitle.value = '添加试验'
  if (formRef.value) {
    formRef.value.resetFields()
  }
  const nextTradingTime = getNextTradingTime()
  Object.assign(formData, {
    id: null,
    tradeType: null,
    targetName: '',
    planType: null,
    planContent: '',
    planStartTime: nextTradingTime.planStartTime,
    planEndTime: nextTradingTime.planEndTime,
    currentPrice: null,
    planPrice: null,
    openPrice: null,
    actualPrice: null,
    closePrice: null,
    tradeStatus: 1,
    complyRuleIds: [],
    violateRuleIds: [],
    tradeResult: null,
    tradeFailType: null,
    resultReview: '',
    isUsePlan: 0
  })
  dialogVisible.value = true
}

const editRecord = (row) => {
  dialogTitle.value = '编辑试验'
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(formData, row)
  // 将逗号分隔的字符串转为数组供多选下拉框使用
  formData.complyRuleIds = row.complyRuleIds ? row.complyRuleIds.split(',').map(Number) : []
  formData.violateRuleIds = row.violateRuleIds ? row.violateRuleIds.split(',').map(Number) : []
  dialogVisible.value = true
}

const submit = async () => {
  if (!formRef.value) return
  try {
    const valid = await formRef.value.validate()
    if (!valid) return
  } catch (error) {
    return
  }
  try {
    // 将数组转为逗号分隔字符串提交给后端
    const submitData = { ...formData }
    submitData.complyRuleIds = Array.isArray(formData.complyRuleIds) ? formData.complyRuleIds.join(',') : formData.complyRuleIds
    submitData.violateRuleIds = Array.isArray(formData.violateRuleIds) ? formData.violateRuleIds.join(',') : formData.violateRuleIds
    const result = await SaveTransactionSystemTrial(submitData)
    if (result.code === 200) {
      ElMessage.success(formData.id ? '编辑成功' : '添加成功')
      dialogVisible.value = false
      fetchData()
      fetchRuleData()
    } else {
      ElMessage.error(result.message || "保存失败")
    }
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// ==================== 删除试验 ====================
const deleteRecord = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该试验记录吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    const result = await DeleteTransactionSystemTrialById(row.id)
    if (result.code === 200) {
      ElMessage.success('删除成功')
      fetchData()
      fetchRuleData()
    } else {
      ElMessage.error(result.message || "删除失败")
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// ==================== 批量删除试验 ====================
const selectedRows = ref([])
const multipleTable = ref(null)

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const deleteSelectAll = async () => {
  if (!selectedRows.value || selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要删除的记录')
    return
  }
  try {
    await ElMessageBox.confirm(
      '确定要批量删除选中的 ' + selectedRows.value.length + ' 条记录吗？',
      '警告',
      { type: 'warning' }
    )
    const ids = selectedRows.value.map(row => row.id)
    const result = await DeleteAllTransactionSystemTrialByIds(ids)
    if (result.code === 200) {
      ElMessage.success('批量删除成功')
      fetchData()
      fetchRuleData()
      multipleTable.value.clearSelection()
      selectedRows.value = []
    } else {
      ElMessage.error(result.message || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

// ==================== 辅助方法 ====================
// 试验记录详情查看
const trialDetailVisible = ref(false)
const trialDetailData = ref(null)
const viewTrialDetail = (row) => {
  trialDetailData.value = { ...row }
  trialDetailVisible.value = true
}

// 根据规则id获取规则名称
const getRuleNames = (ruleIds) => {
  if (!ruleIds) return '-'
  const ids = String(ruleIds).split(',').map(id => Number(id.trim())).filter(id => !isNaN(id))
  if (ids.length === 0) return '-'
  const names = ids.map(id => {
    const rule = ruleList.value.find(r => r.id === id)
    return rule ? rule.ruleCode : `ID:${id}`
  })
  return names.join('、')
}

const getTradeStatusTagType = (status) => {
  switch (status) {
    case 1: return 'info'
    case 2: return 'warning'
    case 3: return 'success'
    default: return 'info'
  }
}

// ==================== 导出功能 ====================
const exportColumns = [
  { key: 'targetName', label: '交易对象', width: 20 },
  { key: 'tradeType', label: '交易类型', width: 15 },
  { key: 'planType', label: '计划类型', width: 15 },
  { key: 'currentPrice', label: '当前价', width: 12 },
  { key: 'planPrice', label: '计划价', width: 12 },
  { key: 'openPrice', label: '开盘价', width: 12 },
  { key: 'actualPrice', label: '成交价', width: 12 },
  { key: 'closePrice', label: '收盘价', width: 12 },
  { key: 'tradeStatus', label: '交易状态', width: 12 },
  { key: 'tradeResult', label: '交易结果', width: 12 },
  { key: 'tradeFailType', label: '失败类型', width: 12 },
  { key: 'isUsePlan', label: '触发计划', width: 10 },
  { key: 'planStartTime', label: '计划开始时间', width: 20 },
  { key: 'planEndTime', label: '计划结束时间', width: 20 },
  { key: 'planContent', label: '计划内容', width: 30 },
  { key: 'complyRuleIds', label: '遵守规则', width: 20 },
  { key: 'violateRuleIds', label: '违反规则', width: 20 },
  { key: 'resultReview', label: '结果复盘', width: 30 }
]

const dataFormatter = (item, key, value) => {
  switch (key) {
    case 'tradeType':
      return getDisplayText(value, tradeTypeOptions.value)
    case 'planType':
      return getDisplayText(value, planTypeOptions.value)
    case 'tradeStatus':
      return getDisplayText(value, tradeStatusOptions.value)
    case 'tradeResult':
      return getDisplayText(value, tradeResultOptions.value)
    case 'tradeFailType':
      return getDisplayText(value, tradeFailTypeOptions.value)
    case 'isUsePlan':
      return value === 1 ? '是' : '否'
    default:
      return value
  }
}

const fetchAllData = async () => {
  const { data } = await GetTransactionSystemTrialByConditionAndPage(1, 1000000, queryDto)
  return data.list || []
}

const {
  exportDialogVisible,
  exportScope,
  exportFileName,
  exportLoading,
  selectedColumns,
  showExportDialog: showExportDialogMethod,
  handleExport,
  resetExport
} = useExport({
  availableColumns: exportColumns,
  fetchAllData: fetchAllData,
  dataFormatter: dataFormatter,
  defaultFileName: '交易系统试验数据',
  sheetName: '交易系统试验数据'
})

const showExportDialog = () => {
  showExportDialogMethod(list.value, total.value)
}

// ==================== 钩子函数 ====================
onMounted(() => {
  // 加载数据字典
  Promise.all([
    loadDict('t_trial_transaction_type', tradeTypeOptions),
    loadDict('t_trial_plan_type', planTypeOptions),
    loadDict('t_trial_transaction_status', tradeStatusOptions),
    getTradeResultItem(),
    loadDict('t_trade_fail_type', tradeFailTypeOptions),
    getRuleStatusItem(),
    loadDict('t_trial_rule_type', ruleTypeOptions)
  ]).catch(err => console.error('字典加载失败:', err))

  // 加载规则列表（供表单规则选择和详情getRuleNames使用）
  fetchRuleData()

  // 加载试验数据
  fetchData()
})
</script>

<style scoped>
/* 交易试验记录块 */
.trial-div {
  padding: 15px;
  border: 1px solid #ebeef5;
  border-top: none;
  border-radius: 0 0 4px 4px;
  background-color: rgba(255, 255, 255, 0.8);
}

/* 搜索表单样式 */
.search-div {
  margin-bottom: 10px;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: rgba(255, 255, 255, 0.8);
}

.search-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.search-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.expand-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #409eff;
  transition: all 0.3s ease;
}

.expand-btn:hover {
  color: #66b1ff;
}

.expand-btn .el-icon {
  transition: transform 0.3s ease;
}

/* 操作按钮区域 */
.tools-div {
  margin-bottom: 10px;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: rgba(255, 255, 255, 0.8);
}

/* 搜索按钮美化 */
.beautified-search-btn {
  border-radius: 4px;
  padding: 8px 15px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.2s ease;
  border: none;
  height: auto;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
  box-shadow: 0 1px 4px rgba(64, 158, 255, 0.3);
}

.beautified-search-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.4);
  background: linear-gradient(135deg, #337ecc 0%, #529ce3 100%);
}

/* 重置按钮美化 */
.beautified-reset-btn {
  border-radius: 4px;
  padding: 8px 15px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.2s ease;
  border: 1px solid #dcdfe6;
  height: auto;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: white;
  color: #606266;
}

.beautified-reset-btn:hover {
  transform: translateY(-1px);
  border-color: #409eff;
  color: #409eff;
}

/* 操作按钮区域按钮美化 */
.beautified-tools .el-button {
  border-radius: 8px;
  padding: 10px 20px;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  letter-spacing: 0.5px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.beautified-tools .el-button--success {
  background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%);
  border: none;
}

.beautified-tools .el-button--success:hover {
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 6px 16px rgba(103, 194, 58, 0.4);
  background: linear-gradient(135deg, #529b2e 0%, #73c050 100%);
}

.beautified-tools .el-button--danger {
  background: linear-gradient(135deg, #F56C6C 0%, #f78989 100%);
  border: none;
}

.beautified-tools .el-button--danger:hover {
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 6px 16px rgba(245, 108, 108, 0.4);
  background: linear-gradient(135deg, #d84646 0%, #f06b6b 100%);
}

.beautified-tools .el-button--info {
  background: linear-gradient(135deg, #909399 0%, #a6a9ad 100%);
  border: none;
}

.beautified-tools .el-button--info:hover {
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 6px 16px rgba(144, 147, 153, 0.4);
  background: linear-gradient(135deg, #73767a 0%, #8d9094 100%);
}

.beautified-tools .el-button:active {
  transform: translateY(0) scale(0.98);
}

/* 表格内按钮美化 */
:deep(.el-table .el-button) {
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  letter-spacing: 0.3px;
  border: none;
}

:deep(.el-table .el-button--primary) {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.3);
}

:deep(.el-table .el-button--primary:hover) {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 10px rgba(64, 158, 255, 0.4);
  background: linear-gradient(135deg, #337ecc 0%, #529ce3 100%);
}

:deep(.el-table .el-button--danger) {
  background: linear-gradient(135deg, #F56C6C 0%, #f78989 100%);
  box-shadow: 0 2px 6px rgba(245, 108, 108, 0.3);
}

:deep(.el-table .el-button--danger:hover) {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 10px rgba(245, 108, 108, 0.4);
  background: linear-gradient(135deg, #d84646 0%, #f06b6b 100%);
}

:deep(.el-table .el-button--info) {
  background: linear-gradient(135deg, #909399 0%, #a6a9ad 100%);
  box-shadow: 0 2px 6px rgba(144, 147, 153, 0.3);
}

:deep(.el-table .el-button--info:hover) {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 10px rgba(144, 147, 153, 0.4);
  background: linear-gradient(135deg, #73767a 0%, #8d9094 100%);
}

:deep(.el-table .el-button:active) {
  transform: translateY(0) scale(0.95);
}

/* 对话框按钮美化 */
.dialog-footer .el-button {
  border-radius: 8px;
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  letter-spacing: 0.5px;
}

.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.dialog-footer .el-button--primary:hover {
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
  background: linear-gradient(135deg, #337ecc 0%, #529ce3 100%);
}

.dialog-footer .el-button--default {
  border: 2px solid #e4e7ed;
  background: white;
  color: #606266;
}

.dialog-footer .el-button--default:hover {
  border-color: #409eff;
  color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.dialog-footer .el-button:active {
  transform: translateY(0) scale(0.98);
}

/* 表格样式 */
:deep(.el-table) {
  background-color: #ffffff;
}

:deep(.el-table th) {
  background: #fafbfc;
  color: #2c3e50;
  font-weight: 600;
  font-size: 14px;
}

:deep(.el-table td) {
  font-size: 14px;
  background-color: #ffffff;
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background-color: #fafbfc;
}

:deep(.el-table__body tr:hover > td) {
  background-color: #e6f7ff !important;
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
  letter-spacing: 0.5px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  position: relative;
  z-index: 1;
}

:deep(.enhanced-dialog .el-dialog__headerbtn .el-dialog__close) {
  color: white !important;
  font-size: 22px !important;
  transition: all 0.3s ease;
  position: relative;
  z-index: 1;
}

:deep(.enhanced-dialog .el-dialog__headerbtn .el-dialog__close:hover) {
  transform: rotate(90deg) scale(1.1);
  color: rgba(255, 255, 255, 0.8) !important;
}

:deep(.enhanced-dialog .el-dialog__body) {
  padding: 28px !important;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%) !important;
  max-height: 60vh !important;
  overflow-y: auto !important;
}

:deep(.enhanced-dialog .el-dialog__body::-webkit-scrollbar) {
  width: 8px;
}

:deep(.enhanced-dialog .el-dialog__body::-webkit-scrollbar-track) {
  background: #f1f1f1;
  border-radius: 4px;
}

:deep(.enhanced-dialog .el-dialog__body::-webkit-scrollbar-thumb) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px;
}

:deep(.enhanced-dialog .el-dialog__body::-webkit-scrollbar-thumb:hover) {
  background: linear-gradient(135deg, #5568d3 0%, #6a4190 100%);
}

:deep(.enhanced-dialog .el-dialog__footer) {
  padding: 20px 28px !important;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%) !important;
  border-top: 2px solid rgba(102, 126, 234, 0.1) !important;
}

/* 分页组件样式 */
:deep(.el-pagination) {
  justify-content: center;
}

:deep(.el-pagination button),
:deep(.el-pagination .el-pager li) {
  background-color: rgba(255, 255, 255, 0.9);
}

/* 详情弹窗样式 */
.trial-detail-container {
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
</style>