<template>
  <div class="fund-management-container">
    <!-- 标题栏 + 数据获取区 合并 -->
    <div class="top-bar">
      <div class="top-bar-left">
        <div class="page-title-icon">
          <el-icon :size="22"><Coin /></el-icon>
        </div>
        <span class="page-title">基金资产管理</span>
      </div>
      <div class="top-bar-right">
        <el-input
          v-model="fetchFundCode"
          placeholder="输入基金代码获取数据"
          style="width: 220px"
          clearable
          size="small"
          @keyup.enter="fetchFundData"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" size="small" @click="fetchFundData" :loading="fetchLoading">
          <el-icon><Download /></el-icon>
          数据获取
        </el-button>
      </div>
    </div>

    <!-- 条件查询区 -->
    <div class="search-section">
      <el-form label-width="120px" size="small">
        <el-row>
          <el-col :span="6">
            <el-form-item label="基金名称">
              <el-input v-model="query.fundName" style="width: 100%" placeholder="请输入基金名称" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="基金代码">
              <el-input v-model="query.fundCode" style="width: 100%" placeholder="请输入基金代码" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="基金类型">
              <el-select v-model="query.fundType" multiple placeholder="请选择" style="width: 100%" clearable>
                <el-option v-for="item in fundTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="资产规模(亿)">
              <el-input-number v-model="query.assetScale" :precision="2" :step="1" :min="0" style="width: 100%" controls-position="right" placeholder="大于" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="6">
            <el-form-item label="运作方式">
              <el-select v-model="query.operationMode" placeholder="请选择" style="width: 100%" clearable>
                <el-option v-for="item in operationModeOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="封闭期(天)">
              <el-input-number v-model="query.closedEndDays" :min="0" :step="1" style="width: 100%" controls-position="right" placeholder="大于" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row style="display: flex; gap: 10px; padding-left: 10px;">
          <el-button type="primary" size="small" @click="handleSearch" class="beautified-search-btn">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button size="small" @click="resetQuery" class="beautified-reset-btn">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-row>
      </el-form>
    </div>

    <!-- 操作按钮区 -->
    <div class="action-section">
      <el-button type="danger" size="small" @click="batchDelete" class="action-btn-danger">
        <el-icon><Delete /></el-icon>
        批量删除
      </el-button>
      <el-button type="info" size="small" @click="showExportDialog" class="action-btn-export">
        <el-icon><Download /></el-icon>
        一键导出
      </el-button>
    </div>

    <!-- 卡片列表区（独立滚动块） -->
    <div class="card-list-section">
      <div class="card-scroll-wrapper" v-loading="loading">
        <div class="card-grid">
          <div
            v-for="item in fundList"
            :key="item.id"
            class="fund-card"
            :class="{ 'card-selected': selectedIds.includes(item.id) }"
          >
            <div class="card-checkbox">
              <el-checkbox :model-value="selectedIds.includes(item.id)" @change="(val) => toggleSelect(item.id, val)" />
            </div>
            <div class="card-header">
              <div class="card-title-row">
                <span class="card-fund-name clickable" @click="openViewDialog(item)">{{ item.fundName }}</span>
                <el-tag :type="getFundTypeTagType(getDisplayText(item.fundType, fundTypeOptions))" size="small">{{ getDisplayText(item.fundType, fundTypeOptions) || '未知' }}</el-tag>
              </div>
              <div class="card-code">{{ item.fundCode }}</div>
            </div>
            <div class="card-body">
              <div class="card-info-grid">
                <div class="info-cell">
                  <span class="info-label">成立日期</span>
                  <span class="info-value">{{ item.establishDate ?? '-' }}</span>
                </div>
                <div class="info-cell">
                  <span class="info-label">资产规模</span>
                  <span class="info-value">{{ item.assetScale != null ? item.assetScale + '亿' : '-' }}</span>
                </div>
                <div class="info-cell">
                  <span class="info-label">基金管理人</span>
                  <span class="info-value">{{ item.fundCompany ?? '-' }}</span>
                </div>
                <div class="info-cell">
                  <span class="info-label">基金托管者</span>
                  <span class="info-value">{{ item.custodian ?? '-' }}</span>
                </div>
                <div class="info-cell">
                  <span class="info-label">基金经理</span>
                  <span class="info-value">{{ item.fundManager ?? '-' }}</span>
                </div>
                <div class="info-cell">
                  <span class="info-label">任职日期</span>
                  <span class="info-value">{{ item.managerStartDate ?? '-' }}</span>
                </div>
                <div class="info-cell">
                  <span class="info-label">运作方式</span>
                  <span class="info-value">{{ getDisplayText(item.operationMode, operationModeOptions) }}</span>
                </div>
                <div class="info-cell">
                  <span class="info-label">封闭期</span>
                  <span class="info-value">{{ item.closedEndDays != null ? item.closedEndDays + '天' : '-' }}</span>
                </div>
                <div class="info-cell">
                  <span class="info-label">申购费率</span>
                  <span class="info-value">{{ item.purchaseFeeRate != null ? item.purchaseFeeRate + '%' : '-' }}</span>
                </div>
                <div class="info-cell">
                  <span class="info-label">赎回费率</span>
                  <span class="info-value">{{ item.redeemFeeRate != null ? item.redeemFeeRate + '%' : '-' }}</span>
                </div>
                <div class="info-cell">
                  <span class="info-label">管理费</span>
                  <span class="info-value">{{ item.managementFeeRate != null ? item.managementFeeRate + '%' : '-' }}</span>
                </div>
                <div class="info-cell">
                  <span class="info-label">托管费</span>
                  <span class="info-value">{{ item.custodianFeeRate != null ? item.custodianFeeRate + '%' : '-' }}</span>
                </div>
                <div class="info-cell">
                  <span class="info-label">销售服务费</span>
                  <span class="info-value">{{ item.salesServiceFeeRate != null ? item.salesServiceFeeRate + '%' : '-' }}</span>
                </div>
              </div>
            </div>
            <div class="card-footer">
              <el-button type="primary" size="small" @click="openDetailDialog(item)" class="card-action-btn-edit">
                <el-icon><Edit /></el-icon>
                数据维护
              </el-button>
              <el-button type="danger" size="small" @click="deleteFund(item)" class="card-action-btn-delete">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="fundList.length === 0 && !loading" class="empty-state">
          <el-empty description="暂无基金数据，请通过数据获取添加基金" />
        </div>
      </div>

      <!-- 分页区 -->
      <div class="pagination-section" v-if="total > 0">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :total="total"
          :page-size="pageParams.limit"
          :current-page="pageParams.page"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 数据获取结果模态窗口 -->
    <el-dialog
      v-model="fetchDialogVisible"
      title="基金数据获取结果"
      width="60%"
      :close-on-click-modal="false"
      class="fetch-result-dialog"
    >
      <el-form label-width="120px" size="small">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="基金名称">
              <el-input v-model="fetchResult.fundName" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="基金代码">
              <el-input v-model="fetchResult.fundCode" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="基金类型">
              <el-select v-model="fetchResult.fundType" style="width: 100%" clearable>
                <el-option v-for="item in fundTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成立日期">
              <el-date-picker v-model="fetchResult.establishDate" type="date" style="width: 100%" value-format="YYYY-MM-DD" :editable="false" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="资产规模(亿)">
              <el-input-number v-model="fetchResult.assetScale" :precision="2" :step="0.01" :min="0" style="width: 100%" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="基金管理人">
              <el-input v-model="fetchResult.fundCompany" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="基金公司描述">
              <el-input v-model="fetchResult.fundCompanyDesc" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="基金托管者">
              <el-input v-model="fetchResult.custodian" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="基金经理">
              <el-input v-model="fetchResult.fundManager" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经理任职日期">
              <el-date-picker v-model="fetchResult.managerStartDate" type="date" style="width: 100%" value-format="YYYY-MM-DD" :editable="false" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="经理描述">
              <el-input v-model="fetchResult.managerDesc" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="运作方式">
              <el-select v-model="fetchResult.operationMode" style="width: 100%" clearable>
                <el-option v-for="item in operationModeOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="基金封闭期(天)">
              <el-input-number v-model="fetchResult.closedEndDays" :min="0" style="width: 100%" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="申购费率(%)">
              <el-input-number v-model="fetchResult.purchaseFeeRate" :precision="4" :step="0.001" :min="0" style="width: 100%" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="赎回费率(%)">
              <el-input-number v-model="fetchResult.redeemFeeRate" :precision="4" :step="0.001" :min="0" style="width: 100%" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="管理费(%)">
              <el-input-number v-model="fetchResult.managementFeeRate" :precision="4" :step="0.001" :min="0" style="width: 100%" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="托管费(%)">
              <el-input-number v-model="fetchResult.custodianFeeRate" :precision="4" :step="0.001" :min="0" style="width: 100%" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="销售服务费(%)">
              <el-input-number v-model="fetchResult.salesServiceFeeRate" :precision="4" :step="0.001" :min="0" style="width: 100%" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="交易规则">
              <el-input v-model="fetchResult.tradeRule" type="textarea" :rows="2" placeholder="请输入交易规则" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="fetchDialogVisible = false" class="dialog-cancel-btn">取消</el-button>
        <el-button type="primary" @click="submitFetchResult" class="dialog-submit-btn">提交入库</el-button>
      </template>
    </el-dialog>

    <!-- 基金数据维护大模态窗口 -->
    <FundDetailDialog
      v-model:visible="detailDialogVisible"
      :fund-row-data="currentFundRow"
      @save="handleDetailSave"
    />

    <!-- 基金详情查看模态窗口 -->
    <FundViewDialog
      v-model:visible="viewDialogVisible"
      :fund-data="currentViewFund"
    />

    <!-- 导出对话框 -->
    <ExportDialog
        v-model="exportDialogVisible"
        v-model:export-scope="exportScope"
        v-model:export-file-name="exportFileName"
        v-model:selected-columns="selectedColumns"
        :available-columns="exportColumns"
        :export-loading="exportLoading"
        :current-count="fundList.length"
        :total-count="total"
        @confirm="handleExport"
        @closed="resetExport"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Coin, Search, Download, Refresh, Delete, Edit } from '@element-plus/icons-vue'
import FundDetailDialog from './fundDetailDialog/fundDetailDialog.vue'
import FundViewDialog from './fundDetailDialog/fundViewDialog.vue'
import { GetKeyAndValueByType } from "@/api/sysDict"
import { GetFundBaseDataByCode, GetFundBaseDataByConditionAndPage, GetFundNavByConditionAndPage } from "@/api/fundAsset"
import { useExport } from "@/components/Export/hooks/useExport"
import ExportDialog from '@/components/Export/ExportDialog.vue'

// ============ 数据字典选项 ============
// 基金类型选项
const fundTypeOptions = ref([])
// 获取基金类型
const getFundTypeItem = async () => {
  const result = await GetKeyAndValueByType("t_fund_type")
  fundTypeOptions.value = result.data
}

// 运作方式选项
const operationModeOptions = ref([])
// 获取运作方式
const getOperationModeItem = async () => {
  const result = await GetKeyAndValueByType("t_fund_run_way")
  operationModeOptions.value = result.data
}

// 交易类型选项
const transactionTypeOptions = ref([])
// 获取交易类型
const getTransactionTypeItem = async () => {
  const result = await GetKeyAndValueByType("t_fund_transaction_type")
  transactionTypeOptions.value = result.data
}

// 持仓类型选项
const positionTypeOptions = ref([])
// 获取持仓类型
const getPositionTypeItem = async () => {
  const result = await GetKeyAndValueByType("t_fund_position_type")
  positionTypeOptions.value = result.data
}

// 行业分类选项
const sectorTypeOptions = ref([])
// 获取行业分类
const getSectorTypeItem = async () => {
  const result = await GetKeyAndValueByType("t_fund_sector_type")
  sectorTypeOptions.value = result.data
}

// 数据来源选项
const dataSourceOptions = ref([])
// 获取数据来源
const getDataSourceItem = async () => {
  const result = await GetKeyAndValueByType("t_fund_data_source")
  dataSourceOptions.value = result.data
}

// 通用方法：根据值和映射表获取中文文本
const getDisplayText = (value, mappingArray) => {
  if (!value && value !== 0) return '-'
  const foundItem = mappingArray.find(item => item.value === value)
  return foundItem ? foundItem.text : value
}

const getFundTypeTagType = (type) => {
  const map = { '股票型': 'danger', '债券型': 'success', '货币型': 'warning', '混合型': 'info', '指数型': '' }
  return map[type] || 'info'
}

// ============ 数据获取区 ============
const fetchFundCode = ref('')
const fetchLoading = ref(false)
const fetchDialogVisible = ref(false)
const fetchResult = reactive({
  fundName: '',
  fundCode: '',
  fundType: null,
  establishDate: '',
  assetScale: 0,
  fundCompany: '',
  fundCompanyDesc: '',
  custodian: '',
  fundManager: '',
  managerStartDate: '',
  managerDesc: '',
  operationMode: null,
  closedEndDays: 0,
  purchaseFeeRate: 0,
  redeemFeeRate: 0,
  managementFeeRate: 0,
  custodianFeeRate: 0,
  salesServiceFeeRate: 0,
  tradeRule: ''
})

// 加载提示相关
let loadingMessageInstance = null
let secondStageTimer = null
let startTime = null

// 清理加载提示
const clearLoadingMessage = () => {
  if (loadingMessageInstance) {
    loadingMessageInstance.close()
    loadingMessageInstance = null
  }
  if (secondStageTimer) {
    clearTimeout(secondStageTimer)
    secondStageTimer = null
  }
}

// 组件卸载时清理
onUnmounted(() => {
  clearLoadingMessage()
})

const fetchFundData = async () => {
  if (!fetchFundCode.value.trim()) {
    ElMessage.warning('请输入基金代码')
    return
  }
  
  const code = fetchFundCode.value.trim()
  fetchLoading.value = true
  startTime = Date.now()
  
  // 立即显示第一阶段提示
  loadingMessageInstance = ElMessage({
    message: '正在从数据源获取基金信息，请稍候...',
    type: 'info',
    duration: 0, // 不自动关闭
    showClose: false
  })
  
  // 设置10秒后的二阶段提示
  secondStageTimer = setTimeout(() => {
    if (loadingMessageInstance) {
      loadingMessageInstance.close()
      loadingMessageInstance = ElMessage({
        message: '正在进行AI智能分析，补充基金详细信息，预计需要1-2分钟，请耐心等待...',
        type: 'warning',
        duration: 0, // 不自动关闭
        showClose: false
      })
    }
  }, 10000)
  
  try {
    // 调用后端接口
    const response = await GetFundBaseDataByCode(code)
    
    // 清理加载提示
    clearLoadingMessage()
    fetchLoading.value = false
    
    // 根据后端返回的状态码做提示
    if (response.code === 200) {
      // 数据获取成功，重新加载数据
      fetchData()
      ElMessage.success('基金数据获取成功！')
    } else if (response.code === 400) {
      // 基金数据已存在
      ElMessage.warning(response.message || '该基金数据已存在，请勿重复获取')
    } else if (response.code === 404) {
      // 基金代码不存在
      ElMessage.error(response.message || '未找到该基金代码对应的数据')
    } else {
      // 其他错误情况
      ElMessage.error(response.message || '获取基金数据失败')
    }
  } catch (error) {
    // 清理加载提示
    clearLoadingMessage()
    fetchLoading.value = false
    console.error('获取基金数据失败:', error)
    ElMessage.error('获取基金数据失败，请稍后重试')
  }
}

const submitFetchResult = () => {
  const newFund = {
    id: Date.now(),
    ...JSON.parse(JSON.stringify(fetchResult)),
    navList: [],
    holdShares: 0, availableShares: 0, frozenShares: 0,
    positionCost: 0, positionValue: 0,
    tradeList: [], dividendList: [], riskList: [], holdingList: []
  }
  fundList.value.push(newFund)
  fetchDialogVisible.value = false
  ElMessage.success('基金数据入库成功')
}

// ============ 查询条件 ============
const query = reactive({
  fundName: '',
  fundCode: '',
  fundType: [],
  assetScale: null,
  operationMode: null,
  closedEndDays: null
})

const resetQuery = () => {
  Object.assign(query, {
    fundName: '',
    fundCode: '',
    fundType: [],
    assetScale: null,
    operationMode: null,
    closedEndDays: null
  })
  pageParams.page = 1
  fetchData()
}

// ============ 基金列表数据 ============
const fundList = ref([])
const total = ref(0)
const loading = ref(false)

// ============ 分页参数 ============
const pageParams = reactive({ page: 1, limit: 10 })

// ============ 获取数据 ============
const fetchData = async () => {
  loading.value = true
  try {
    // 构造查询参数，只传递有值的字段
    const queryParams = {}
    if (query.fundName) queryParams.fundName = query.fundName
    if (query.fundCode) queryParams.fundCode = query.fundCode
    if (query.fundType && query.fundType.length > 0) queryParams.fundType = query.fundType
    if (query.assetScale) queryParams.assetScale = query.assetScale
    if (query.operationMode) queryParams.operationMode = query.operationMode
    if (query.closedEndDays) queryParams.closedEndDays = query.closedEndDays

    const response = await GetFundBaseDataByConditionAndPage(
      pageParams.page,
      pageParams.limit,
      queryParams
    )

    if (response.code === 200) {
      fundList.value = response.data.list
      total.value = response.data.total
    } else {
      ElMessage.error(response.message || '获取基金数据失败')
    }
  } catch (error) {
    console.error('获取基金数据失败:', error)
    ElMessage.error('获取基金数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageParams.page = 1
  fetchData()
}

const handleSizeChange = (size) => {
  pageParams.limit = size
  pageParams.page = 1
  fetchData()
}

const handleCurrentChange = (page) => {
  pageParams.page = page
  fetchData()
}

// ============ 选择 & 批量删除 ============
const selectedIds = ref([])
const toggleSelect = (id, val) => {
  if (val) { if (!selectedIds.value.includes(id)) selectedIds.value.push(id) }
  else { selectedIds.value = selectedIds.value.filter(i => i !== id) }
}

const batchDelete = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要删除的基金')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条基金数据吗？`, '批量删除确认', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
    // TODO: 调用后端删除接口
    selectedIds.value = []
    fetchData()
    ElMessage.success('批量删除成功')
  } catch (e) { /* cancel */ }
}

// ============ 单条删除 ============
const deleteFund = async (item) => {
  try {
    await ElMessageBox.confirm(`确定要删除基金【${item.fundName}】吗？`, '删除确认', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
    // TODO: 调用后端删除接口
    selectedIds.value = selectedIds.value.filter(i => i !== item.id)
    fetchData()
    ElMessage.success('删除成功')
  } catch (e) { /* cancel */ }
}

// ============ 一键导出 ============
// 导出列配置
const exportColumns = [
  { key: 'fundName', label: '基金名称', width: 25 },
  { key: 'fundCode', label: '基金代码', width: 15 },
  { key: 'fundType', label: '基金类型', width: 12 },
  { key: 'establishDate', label: '成立日期', width: 15 },
  { key: 'assetScale', label: '资产规模(亿)', width: 15 },
  { key: 'fundCompany', label: '基金管理人', width: 20 },
  { key: 'custodian', label: '基金托管者', width: 20 },
  { key: 'fundManager', label: '基金经理', width: 15 },
  { key: 'managerStartDate', label: '任职日期', width: 15 },
  { key: 'operationMode', label: '运作方式', width: 12 },
  { key: 'closedEndDays', label: '封闭期(天)', width: 12 },
  { key: 'purchaseFeeRate', label: '申购费率(%)', width: 12 },
  { key: 'redeemFeeRate', label: '赎回费率(%)', width: 12 },
  { key: 'managementFeeRate', label: '管理费(%)', width: 12 },
  { key: 'custodianFeeRate', label: '托管费(%)', width: 12 },
  { key: 'salesServiceFeeRate', label: '销售服务费(%)', width: 15 }
]

// 数据格式化函数
const fundAssetDataFormatter = (item, key, value) => {
  switch (key) {
    case 'fundType':
      return getDisplayText(value, fundTypeOptions.value)
    case 'operationMode':
      return getDisplayText(value, operationModeOptions.value)
    default:
      return value
  }
}

// 获取全部数据的函数
const fetchAllFundAssetData = async () => {
  // 构造查询参数，只传递有值的字段
  const queryParams = {}
  if (query.fundName) queryParams.fundName = query.fundName
  if (query.fundCode) queryParams.fundCode = query.fundCode
  if (query.fundType && query.fundType.length > 0) queryParams.fundType = query.fundType
  if (query.assetScale) queryParams.assetScale = query.assetScale
  if (query.operationMode) queryParams.operationMode = query.operationMode
  if (query.closedEndDays) queryParams.closedEndDays = query.closedEndDays

  const response = await GetFundBaseDataByConditionAndPage(
    1,
    1000000,
    queryParams
  )

  if (response.code === 200) {
    return response.data.list || []
  } else {
    return []
  }
}

// 使用导出Hook
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
  fetchAllData: fetchAllFundAssetData,
  dataFormatter: fundAssetDataFormatter,
  defaultFileName: '基金资产数据',
  sheetName: '基金资产数据'
})

// 包装显示导出对话框的方法
const showExportDialog = () => {
  showExportDialogMethod(fundList.value, total.value)
}

// ============ 数据维护模态窗口 ============
const detailDialogVisible = ref(false)
const currentFundRow = ref({})

const openDetailDialog = (item) => {
  // 直接传递原始数据，后端返回什么就传递什么（包括0值）
  currentFundRow.value = item
  detailDialogVisible.value = true
}

const handleDetailSave = (saveData) => {
  // 保存成功后刷新列表
  fetchData()
  detailDialogVisible.value = false
}

// ============ 基金详情查看模态窗口 ============
const viewDialogVisible = ref(false)
const currentViewFund = ref({})

const openViewDialog = (item) => {
  currentViewFund.value = JSON.parse(JSON.stringify(item))
  viewDialogVisible.value = true
}

// ============ 钩子函数 ============
onMounted(() => {
  // 加载数据字典
  getFundTypeItem()
  getOperationModeItem()
  getTransactionTypeItem()
  getPositionTypeItem()
  getSectorTypeItem()
  getDataSourceItem()
  // 加载基金数据
  fetchData()
})
</script>

<style scoped>
.fund-management-container {
  width: 100%;
  min-height: 100vh;
  margin: 0;
  padding: 0;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8eef5 100%);
  display: flex;
  flex-direction: column;
  gap: 0;
  overflow: hidden;
  height: 100vh;
}

/* ====== 顶部标题栏 ====== */
.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  padding: 16px 28px;
  box-shadow: 0 4px 20px rgba(30, 60, 114, 0.3);
  flex-shrink: 0;
}

.top-bar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title-icon {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  backdrop-filter: blur(10px);
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 1px;
  font-family: 'Microsoft YaHei', sans-serif;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.top-bar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.top-bar-right :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: none;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.top-bar-right :deep(.el-input__wrapper:hover),
.top-bar-right :deep(.el-input__wrapper.is-focus) {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.5);
  box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.1);
}

.top-bar-right :deep(.el-input__inner) {
  color: #fff;
}

.top-bar-right :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.6);
}

.top-bar-right :deep(.el-input__prefix .el-icon) {
  color: rgba(255, 255, 255, 0.7);
}

.top-bar-right :deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 8px;
  color: #fff;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
  padding: 8px 20px;
}

.top-bar-right :deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.5);
}

/* ====== 查询区 ====== */
.search-section {
  background: #fff;
  padding: 20px 24px;
  border-bottom: 1px solid #e8ecf1;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.search-section :deep(.el-form-item__label) {
  font-weight: 600;
  color: #2c3e50;
}

.search-section :deep(.el-input__wrapper),
.search-section :deep(.el-select__wrapper),
.search-section :deep(.el-input-number) {
  border-radius: 6px;
  transition: all 0.3s ease;
}

.search-section :deep(.el-input__wrapper:hover),
.search-section :deep(.el-select__wrapper:hover) {
  box-shadow: 0 0 0 1px #409eff inset;
}

/* ====== 操作按钮区 ====== */
.action-section {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
  padding: 12px 24px;
  background: #fff;
  border-bottom: 1px solid #e8ecf1;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.03);
}

.action-btn-danger {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%) !important;
  border: none !important;
  border-radius: 8px !important;
  font-weight: 600 !important;
  color: #fff !important;
  box-shadow: 0 2px 8px rgba(240, 147, 251, 0.4) !important;
  transition: all 0.3s ease !important;
  padding: 8px 20px !important;
}

.action-btn-danger:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 4px 12px rgba(240, 147, 251, 0.5) !important;
}

.action-btn-export {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%) !important;
  border: none !important;
  border-radius: 8px !important;
  font-weight: 600 !important;
  color: #fff !important;
  box-shadow: 0 2px 8px rgba(79, 172, 254, 0.4) !important;
  transition: all 0.3s ease !important;
  padding: 8px 20px !important;
}

.action-btn-export:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 4px 12px rgba(79, 172, 254, 0.5) !important;
}

/* ====== 卡片列表区 ====== */
.card-list-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}

.card-scroll-wrapper {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
}

.card-scroll-wrapper::-webkit-scrollbar {
  width: 8px;
}

.card-scroll-wrapper::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.card-scroll-wrapper::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, #c0c4cc, #909399);
  border-radius: 4px;
}

.card-scroll-wrapper::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, #909399, #606266);
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

/* ====== 基金卡片 ====== */
.fund-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e8ecf1;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.fund-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.fund-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.15);
  border-color: #c0c4cc;
}

.fund-card:hover::before {
  opacity: 1;
}

.fund-card.card-selected {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
}

.fund-card.card-selected::before {
  opacity: 1;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
}

.card-checkbox {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 2;
}

.card-header {
  padding: 16px 20px 12px;
  border-bottom: 1px solid #f0f2f5;
  background: linear-gradient(135deg, #fafbfc 0%, #f5f7fa 100%);
}

.card-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}

.card-fund-name {
  font-size: 16px;
  font-weight: 700;
  color: #2c3e50;
  letter-spacing: 0.5px;
}

.card-fund-name.clickable {
  cursor: pointer;
  transition: all 0.3s ease;
  color: #1e3c72;
  text-decoration: underline;
  text-underline-offset: 3px;
  text-decoration-color: rgba(30, 60, 114, 0.4);
}

.card-fund-name.clickable:hover {
  color: #409eff;
  text-decoration-color: #409eff;
}

.card-code {
  font-size: 13px;
  color: #909399;
  font-family: 'Consolas', 'Monaco', monospace;
  letter-spacing: 0.5px;
}

.card-body {
  padding: 12px 20px;
}

.card-info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px 16px;
}

.info-cell {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

.info-label {
  font-size: 13px;
  color: #909399;
  white-space: nowrap;
}

.info-value {
  font-size: 14px;
  color: #303133;
  font-weight: 600;
  text-align: right;
}

.card-footer {
  padding: 10px 20px 14px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  border-top: 1px solid #f0f2f5;
  background: #fafbfc;
}

.card-action-btn-edit {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  border-radius: 6px !important;
  font-weight: 600 !important;
  color: #fff !important;
  box-shadow: 0 2px 6px rgba(102, 126, 234, 0.3) !important;
  transition: all 0.3s ease !important;
}

.card-action-btn-edit:hover {
  transform: translateY(-1px) !important;
  box-shadow: 0 3px 8px rgba(102, 126, 234, 0.4) !important;
}

.card-action-btn-delete {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%) !important;
  border: none !important;
  border-radius: 6px !important;
  font-weight: 600 !important;
  color: #fff !important;
  box-shadow: 0 2px 6px rgba(240, 147, 251, 0.3) !important;
  transition: all 0.3s ease !important;
}

.card-action-btn-delete:hover {
  transform: translateY(-1px) !important;
  box-shadow: 0 3px 8px rgba(240, 147, 251, 0.4) !important;
}

/* ====== 空状态 ====== */
.empty-state {
  padding: 80px 0;
  text-align: center;
}

/* ====== 分页 ====== */
.pagination-section {
  display: flex;
  justify-content: flex-end;
  padding: 12px 24px;
  border-top: 1px solid #e8ecf1;
  flex-shrink: 0;
  background: #fff;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.03);
}

.pagination-section :deep(.el-pagination) {
  gap: 8px;
}

.pagination-section :deep(.el-pagination button),
.pagination-section :deep(.el-pager li) {
  border-radius: 6px;
  transition: all 0.3s ease;
}

/* ====== 按钮美化 ====== */
.beautified-search-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  border-radius: 8px !important;
  font-weight: 600 !important;
  color: #fff !important;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4) !important;
  transition: all 0.3s ease !important;
  padding: 8px 20px !important;
}

.beautified-search-btn:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.5) !important;
}

.beautified-reset-btn {
  border-radius: 8px !important;
  font-weight: 600 !important;
  transition: all 0.3s ease !important;
  border: 1px solid #dcdfe6 !important;
}

.beautified-reset-btn:hover {
  transform: translateY(-2px) !important;
  background: #f5f7fa !important;
  border-color: #c0c4cc !important;
}

/* ====== 数据获取结果对话框 ====== */
.fetch-result-dialog :deep(.el-dialog) {
  border-radius: 16px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.fetch-result-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 24px;
  border-bottom: none;
}

.fetch-result-dialog :deep(.el-dialog__title) {
  color: #fff;
  font-weight: 700;
  font-size: 18px;
}

.fetch-result-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #fff;
}

.fetch-result-dialog :deep(.el-dialog__body) {
  padding: 24px;
  background: #fafbfc;
}

.fetch-result-dialog :deep(.el-form-item__label) {
  font-weight: 600;
  color: #2c3e50;
}

.fetch-result-dialog :deep(.el-input__wrapper),
.fetch-result-dialog :deep(.el-select__wrapper),
.fetch-result-dialog :deep(.el-input-number) {
  border-radius: 6px;
  transition: all 0.3s ease;
}

.fetch-result-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px;
  background: #fff;
  border-top: 1px solid #e8ecf1;
}

.dialog-cancel-btn {
  border-radius: 8px !important;
  font-weight: 600 !important;
  transition: all 0.3s ease !important;
  padding: 10px 24px !important;
}

.dialog-cancel-btn:hover {
  transform: translateY(-2px) !important;
}

.dialog-submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  border-radius: 8px !important;
  font-weight: 600 !important;
  color: #fff !important;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4) !important;
  transition: all 0.3s ease !important;
  padding: 10px 24px !important;
}

.dialog-submit-btn:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.5) !important;
}
</style>
