<template>
  <div class="report-div">
    <!-- 报表筛选条件 -->
    <div class="search-div">
      <div class="search-header">
        <span class="search-title">统计条件</span>
      </div>
      <el-form label-width="100px" size="small">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="统计时间">
              <el-date-picker
                v-model="reportTimeArea"
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
          <el-col :span="6">
            <el-form-item label="依据类型">
              <el-select v-model="reportDto.basisType" style="width: 100%" clearable placeholder="请选择" multiple>
                <el-option v-for="item in basisTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="股票代码">
              <el-input v-model="reportDto.stockCode" style="width: 100%" clearable placeholder="请输入股票代码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24" style="text-align: right;">
            <el-form-item label-width="10px">
              <el-button type="primary" size="small" @click="fetchReportData">
                <el-icon><Search /></el-icon>
                查询
              </el-button>
              <el-button size="small" @click="resetReportData">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <!-- 概览卡片 -->
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-value primary drill-link" @click="drillDown({ type: 'total' })">{{ reportData.totalCount || 0 }}</div>
          <div class="stat-label">总预测次数</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-value success drill-link" @click="drillDown({ type: 'total', predictionResult: 1 })">{{ reportData.successCount || 0 }}</div>
          <div class="stat-label">预测成功次数</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-value danger drill-link" @click="drillDown({ type: 'total', predictionResult: 2 })">{{ reportData.failCount || 0 }}</div>
          <div class="stat-label">预测失败次数</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card rate-card">
          <div class="stat-value" :class="getRateClass((reportData.accuracyRate || 0) + '%')">{{ reportData.accuracyRate || '0.00' }}%</div>
          <div class="stat-label">预测准确率</div>
        </div>
      </el-col>
    </el-row>

    <!-- 依据类型准确率 + 预测情况分布 -->
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="12">
        <div class="report-section">
          <div class="detail-section-title">依据类型准确率</div>
          <el-table :data="reportData.basisTypeStats || []" border stripe size="small">
            <el-table-column prop="basisType" label="依据类型" align="center">
              <template #default="scope">
                {{ getBasisTypeDisplay(scope.row.basisType) }}
              </template>
            </el-table-column>
            <el-table-column prop="count" label="总次数" align="center">
              <template #default="scope">
                <el-link type="primary" :underline="false" @click="drillDown({ type: 'basisType', basisType: scope.row.basisType })">{{ scope.row.count }}</el-link>
              </template>
            </el-table-column>
            <el-table-column prop="successCount" label="成功次数" align="center">
              <template #default="scope">
                <el-link type="primary" :underline="false" @click="drillDown({ type: 'basisType', basisType: scope.row.basisType, predictionResult: 1 })">{{ scope.row.successCount }}</el-link>
              </template>
            </el-table-column>
            <el-table-column prop="successRate" label="成功率" align="center">
              <template #default="scope">
                <span :class="getRateClass(scope.row.successRate + '%')">{{ scope.row.successRate }}%</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="report-section">
          <div class="detail-section-title">预测情况分布</div>
          <el-table :data="reportData.situationStats || []" border stripe size="small">
            <el-table-column prop="predictionSituation" label="预测情况" align="center">
              <template #default="scope">
                {{ getDisplayText(scope.row.predictionSituation, predictionSituationOptions) }}
              </template>
            </el-table-column>
            <el-table-column prop="count" label="次数" align="center">
              <template #default="scope">
                <el-link type="primary" :underline="false" @click="drillDown({ type: 'situation', predictionSituation: scope.row.predictionSituation })">{{ scope.row.count }}</el-link>
              </template>
            </el-table-column>
            <el-table-column prop="percentage" label="占比" align="center">
              <template #default="scope">
                <el-progress :percentage="Number(scope.row.percentage)" :stroke-width="14" :text-inside="true" />
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>

    <!-- 预测源统计 -->
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="12">
        <div class="report-section">
          <div class="detail-section-title">预测源统计</div>
          <el-table :data="reportData.predictionSourceStats || []" border stripe size="small">
            <el-table-column prop="predictionSource" label="预测源" align="center">
              <template #default="scope">
                {{ getDisplayText(scope.row.predictionSource, predictionSourceOptions) }}
              </template>
            </el-table-column>
            <el-table-column prop="count" label="总次数" align="center">
              <template #default="scope">
                <el-link type="primary" :underline="false" @click="drillDown({ type: 'predictionSource', predictionSource: scope.row.predictionSource })">{{ scope.row.count }}</el-link>
              </template>
            </el-table-column>
            <el-table-column prop="successCount" label="成功次数" align="center">
              <template #default="scope">
                <el-link type="primary" :underline="false" @click="drillDown({ type: 'predictionSource', predictionSource: scope.row.predictionSource, predictionResult: 1 })">{{ scope.row.successCount }}</el-link>
              </template>
            </el-table-column>
            <el-table-column prop="successRate" label="成功率" align="center">
              <template #default="scope">
                <span :class="getRateClass(scope.row.successRate + '%')">{{ scope.row.successRate }}%</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>

      <el-col :span="12">
        <div class="report-section">
          <div class="detail-section-title">月度预测准确率趋势</div>
          <el-table :data="reportData.monthlyTrends || []" border stripe size="small">
            <el-table-column prop="month" label="月份" align="center" />
            <el-table-column prop="totalCount" label="总次数" align="center">
              <template #default="scope">
                <el-link type="primary" :underline="false" @click="drillDown({ type: 'month', month: scope.row.month })">{{ scope.row.totalCount }}</el-link>
              </template>
            </el-table-column>
            <el-table-column prop="successCount" label="成功次数" align="center">
              <template #default="scope">
                <el-link type="primary" :underline="false" @click="drillDown({ type: 'month', month: scope.row.month, predictionResult: 1 })">{{ scope.row.successCount }}</el-link>
              </template>
            </el-table-column>
            <el-table-column prop="successRate" label="成功率" align="center">
              <template #default="scope">
                <span :class="getRateClass(scope.row.successRate + '%')">{{ scope.row.successRate }}%</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>

    <!-- 模拟操作盈亏统计 -->
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="24">
        <div class="report-section">
          <div class="detail-section-title">模拟操作盈亏统计</div>
          <el-row :gutter="16" v-if="reportData.simulateTradeStat">
            <el-col :span="8">
              <div class="stat-card">
                <div class="stat-value primary drill-link" @click="drillDown({ type: 'trade', simulateOperation: 1 })">{{ reportData.simulateTradeStat.totalBuyCount || 0 }}</div>
                <div class="stat-label">买入次数</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-card">
                <div class="stat-value success drill-link" @click="drillDown({ type: 'trade', simulateOperation: 2 })">{{ reportData.simulateTradeStat.totalSellCount || 0 }}</div>
                <div class="stat-label">卖出次数</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-card">
                <div class="stat-value warning">{{ reportData.simulateTradeStat.totalHandlingFee || '0.00' }}</div>
                <div class="stat-label">总手续费</div>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="16" style="margin-top: 16px;" v-if="reportData.simulateTradeStat">
            <el-col :span="8">
              <div class="stat-card">
                <div class="stat-value primary">{{ reportData.simulateTradeStat.totalBuyAmount || '0.00' }}</div>
                <div class="stat-label">买入总额</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-card">
                <div class="stat-value success">{{ reportData.simulateTradeStat.totalSellAmount || '0.00' }}</div>
                <div class="stat-label">卖出总额</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-card">
                <div class="stat-value" :class="Number(reportData.simulateTradeStat.totalProfitLoss || 0) >= 0 ? 'success' : 'danger'">
                  {{ reportData.simulateTradeStat.totalProfitLoss || '0.00' }}
                </div>
                <div class="stat-label">模拟盈亏</div>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-col>
    </el-row>

    <!-- 股票预测统计 -->
    <div class="report-section">
      <div class="detail-section-title">股票预测统计</div>
      <el-table :data="reportData.stockStats || []" border stripe size="small">
        <el-table-column prop="stockName" label="股票名称" align="center" min-width="120" show-overflow-tooltip />
        <el-table-column prop="stockCode" label="股票代码" align="center" width="120" />
        <el-table-column prop="predictCount" label="预测次数" align="center" width="100">
          <template #default="scope">
            <el-link type="primary" :underline="false" @click="drillDown({ type: 'stock', detailStockCode: scope.row.stockCode })">{{ scope.row.predictCount }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="successCount" label="成功次数" align="center" width="100">
          <template #default="scope">
            <el-link type="primary" :underline="false" @click="drillDown({ type: 'stock', detailStockCode: scope.row.stockCode, predictionResult: 1 })">{{ scope.row.successCount }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="successRate" label="成功率" align="center" width="100">
          <template #default="scope">
            <span :class="getRateClass(scope.row.successRate + '%')">{{ scope.row.successRate }}%</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>

  <!-- 穿透明细对话框 -->
  <el-dialog
    v-model="drillDetailVisible"
    :title="drillDetailTitle"
    width="80%"
    class="custom-dialog enhanced-dialog"
    :close-on-click-modal="true"
    destroy-on-close
  >
    <el-table
      :data="drillDetailList"
      v-loading="drillLoading"
      style="width: 100%"
      height="450"
      border
      stripe
      size="small"
    >
      <el-table-column label="操作" align="center" fixed="left" width="70" #default="scope">
        <el-button type="info" size="small" @click="viewPredDetail(scope.row)">
          <el-icon><View /></el-icon>
          查看
        </el-button>
      </el-table-column>
      <el-table-column prop="stockName" label="股票名称" align="center" width="120" show-overflow-tooltip />
      <el-table-column prop="stockCode" label="股票代码" align="center" width="100" />
      <el-table-column prop="riseFallPrediction" label="涨跌预测" align="center" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.riseFallPrediction === 1 ? 'danger' : 'success'" size="small">
            {{ getDisplayText(scope.row.riseFallPrediction, riseFallOptions) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="riseFallResult" label="涨跌结果" align="center" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.riseFallResult" :type="scope.row.riseFallResult === 1 ? 'danger' : 'success'" size="small">
            {{ getDisplayText(scope.row.riseFallResult, riseFallOptions) }}
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="predictionTime" label="预测时间" align="center" width="160" />
      <el-table-column prop="basisType" label="依据类型" align="center" min-width="120">
        <template #default="scope">
          {{ getBasisTypeDisplay(scope.row.basisType) }}
        </template>
      </el-table-column>
      <el-table-column prop="predictionResult" label="预测结果" align="center" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.predictionResult === 1 ? 'success' : 'danger'" size="small">
            {{ getDisplayText(scope.row.predictionResult, predictionResultOptions) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="predictionSituation" label="预测情况" align="center" width="100">
        <template #default="scope">
          {{ getDisplayText(scope.row.predictionSituation, predictionSituationOptions) }}
        </template>
      </el-table-column>
      <el-table-column prop="simulateOperation" label="模拟操作" align="center" width="90">
        <template #default="scope">
          <el-tag v-if="scope.row.simulateOperation" :type="scope.row.simulateOperation === 1 ? 'danger' : 'success'" size="small">
            {{ getDisplayText(scope.row.simulateOperation, simulateOperationOptions) }}
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="tradeShare" label="交易股数" align="center" width="80" />
      <el-table-column prop="currentPrice" label="当前价" align="center" width="90">
        <template #default="scope">
          {{ scope.row.currentPrice != null ? Number(scope.row.currentPrice).toFixed(2) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="handlingFee" label="手续费" align="center" width="80">
        <template #default="scope">
          {{ scope.row.handlingFee != null ? Number(scope.row.handlingFee).toFixed(2) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="tradeStatus" label="交易状态" align="center" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.tradeStatus" :type="scope.row.tradeStatus === 1 ? 'success' : 'danger'" size="small">
            {{ getDisplayText(scope.row.tradeStatus, simulateTradeStatusOptions) }}
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="predictionContent" label="预测内容" align="center" min-width="120" show-overflow-tooltip />
      <el-table-column prop="actualContent" label="实际内容" align="center" min-width="120" show-overflow-tooltip />
      <el-table-column prop="resultAnalysis" label="结果分析" align="center" min-width="120" show-overflow-tooltip />
    </el-table>
    <el-pagination
      style="margin-top: 20px"
      v-model:current-page="drillPageParams.page"
      v-model:page-size="drillPageParams.limit"
      :page-sizes="PAGE_SIZES"
      @size-change="fetchDrillDetailData"
      @current-change="fetchDrillDetailData"
      layout="total, sizes, prev, pager, next"
      :total="drillDetailTotal"
    />
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, View } from '@element-plus/icons-vue'
import { GetKeyAndValueByType } from "@/api/sysDict"
import { GetPredictionReport, GetPredictionDetailByCondition } from "@/api/trialExecutionArea/predictionSimulate"
import { getDisplayText } from "@/utils/common"

// ==================== 通用常量 ====================
const PAGE_SIZES = [10, 20, 50, 100]

// ==================== 数据字典 ====================
const loadDict = async (tableName, optionsRef) => {
  const result = await GetKeyAndValueByType(tableName)
  optionsRef.value = result.data || []
}

const predictionSituationOptions = ref([])
const simulateOperationOptions = ref([])
const predictionSourceOptions = ref([])
const basisTypeOptions = ref([])
const riseFallOptions = ref([])
const predictionResultOptions = ref([])
const simulateTradeStatusOptions = ref([])

// 依据类型多值显示：将逗号分隔字符串转为文字显示
const getBasisTypeDisplay = (basisType) => {
  if (!basisType) return '-'
  if (Array.isArray(basisType)) {
    return basisType.map(type => getDisplayText(Number(type), basisTypeOptions.value)).join(', ')
  }
  if (typeof basisType === 'string') {
    return basisType.split(',').filter(v => v).map(type => getDisplayText(Number(type), basisTypeOptions.value)).join(', ')
  }
  return getDisplayText(basisType, basisTypeOptions.value)
}

// 根据成功率返回样式类名
const getRateClass = (rateStr) => {
  if (rateStr === '-') return 'rate-text rate-none'
  const numVal = parseFloat(rateStr)
  if (numVal >= 60) return 'rate-text rate-high'
  if (numVal >= 30) return 'rate-text rate-medium'
  return 'rate-text rate-low'
}

// ==================== 统计报表 ====================
const reportTimeArea = ref([])
const reportDto = reactive({
  startTime: null,
  endTime: null,
  basisType: [],
  stockCode: ''
})
const reportData = ref({})

const reportLoading = ref(false)
const fetchReportData = async () => {
  reportDto.startTime = reportTimeArea.value && reportTimeArea.value.length > 0 ? reportTimeArea.value[0] : null
  reportDto.endTime = reportTimeArea.value && reportTimeArea.value.length > 0 ? reportTimeArea.value[1] : null
  reportLoading.value = true
  try {
    const result = await GetPredictionReport(reportDto)
    if (result.code === 200) {
      reportData.value = result.data || {}
    } else {
      ElMessage.error(result.message || "查询报表失败")
    }
  } catch (error) {
    ElMessage.error("查询报表失败")
  }
  finally { reportLoading.value = false }
}

const resetReportData = () => {
  reportTimeArea.value = []
  Object.assign(reportDto, {
    startTime: null,
    endTime: null,
    basisType: [],
    stockCode: ''
  })
  fetchReportData()
}

// ==================== 穿透明细 ====================
const drillDetailVisible = ref(false)
const drillDetailTitle = ref('穿透明细')
const drillDetailList = ref([])
const drillDetailTotal = ref(0)
const drillPageParams = reactive({ page: 1, limit: 10 })
const drillQueryDto = reactive({
  startTime: null,
  endTime: null,
  basisType: [],
  stockCode: '',
  predictionResult: null,
  predictionSituation: null,
  simulateOperation: null,
  predictionSource: null,
  month: null,
  detailStockCode: null
})

// 穿透点击处理
const drillDown = (params) => {
  drillQueryDto.startTime = reportDto.startTime
  drillQueryDto.endTime = reportDto.endTime
  drillQueryDto.basisType = [...(reportDto.basisType || [])]
  drillQueryDto.stockCode = reportDto.stockCode
  drillQueryDto.predictionResult = params.predictionResult || null
  drillQueryDto.predictionSituation = params.predictionSituation || null
  drillQueryDto.simulateOperation = params.simulateOperation || null
  drillQueryDto.predictionSource = params.predictionSource || null
  drillQueryDto.month = params.month || null
  drillQueryDto.detailStockCode = params.detailStockCode || null

  if (params.type === 'basisType' && params.basisType) {
    drillQueryDto.basisType = typeof params.basisType === 'string'
      ? params.basisType.split(',').filter(v => v).map(v => Number(v))
      : [params.basisType]
  }

  const titleParts = ['穿透明细']
  if (params.type === 'basisType' && params.basisType) {
    titleParts.push('依据类型: ' + getBasisTypeDisplay(params.basisType))
  }
  if (params.type === 'situation' && params.predictionSituation) {
    titleParts.push('预测情况: ' + getDisplayText(params.predictionSituation, predictionSituationOptions.value))
  }
  if (params.type === 'month' && params.month) {
    titleParts.push('月份: ' + params.month)
  }
  if (params.type === 'trade' && params.simulateOperation) {
    titleParts.push('操作: ' + getDisplayText(params.simulateOperation, simulateOperationOptions.value))
  }
  if (params.type === 'predictionSource' && params.predictionSource) {
    titleParts.push('预测源: ' + getDisplayText(params.predictionSource, predictionSourceOptions.value))
  }
  if (params.type === 'stock' && params.detailStockCode) {
    titleParts.push('股票: ' + params.detailStockCode)
  }
  if (params.predictionResult) {
    titleParts.push(params.predictionResult === 1 ? '成功' : '失败')
  }
  drillDetailTitle.value = titleParts.join(' - ')

  drillPageParams.page = 1
  drillDetailVisible.value = true
  fetchDrillDetailData()
}

// 获取穿透明细数据
const drillLoading = ref(false)
const fetchDrillDetailData = async () => {
  drillLoading.value = true
  try {
    const result = await GetPredictionDetailByCondition(drillPageParams.page, drillPageParams.limit, drillQueryDto)
    if (result.code === 200) {
      const pageInfo = result.data || {}
      drillDetailList.value = pageInfo.list || []
      drillDetailTotal.value = pageInfo.total || 0
    } else {
      ElMessage.error(result.message || "查询穿透明细失败")
    }
  } catch (error) {
    ElMessage.error("查询穿透明细失败")
  }
  finally { drillLoading.value = false }
}

// 预测详情查看（穿透明细中的查看按钮）
const predDetailVisible = ref(false)
const predDetailData = ref(null)
const viewPredDetail = (row) => {
  predDetailData.value = { ...row }
  predDetailVisible.value = true
}

//--------------------钩子函数-------------------------
onMounted(() => {
  Promise.all([
    loadDict('t_trial_prediction_situation', predictionSituationOptions),
    loadDict('t_trial_simulate_operation', simulateOperationOptions),
    loadDict('t_trial_prediction_source', predictionSourceOptions),
    loadDict('t_trial_prediction_basis_type', basisTypeOptions),
    loadDict('t_trial_prediction_rise_fall', riseFallOptions),
    loadDict('t_trial_prediction_result', predictionResultOptions),
    loadDict('t_trial_simulate_trade_status', simulateTradeStatusOptions)
  ]).catch(err => console.error('字典加载失败:', err))

  fetchReportData()
})
</script>

<style scoped>
/* 统计报表块 */
.report-div {
  padding: 15px;
  border: 1px solid #ebeef5;
  border-top: none;
  border-radius: 0 0 4px 4px;
  background-color: rgba(255, 255, 255, 0.8);
}

.report-section {
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: rgba(255, 255, 255, 0.6);
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

/* 详情样式 */
.detail-section-title {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 12px;
  padding-left: 10px;
  border-left: 4px solid #667eea;
  line-height: 1;
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

/* 穿透链接样式 */
.drill-link {
  cursor: pointer;
  transition: all 0.2s ease;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.drill-link:hover {
  transform: scale(1.1);
  filter: brightness(1.2);
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

/* 表格内按钮美化 */
:deep(.el-table .el-button) {
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 600;
  border: none;
}

:deep(.el-table .el-button--info) {
  background: linear-gradient(135deg, #909399 0%, #a6a9ad 100%);
  box-shadow: 0 2px 6px rgba(144, 147, 153, 0.3);
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

/* 分页组件样式 */
:deep(.el-pagination) {
  justify-content: center;
}
</style>