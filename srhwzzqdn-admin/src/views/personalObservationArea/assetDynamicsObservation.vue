<template>
  <div class="asset-dynamics-observation">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>资产动态观测</h1>
    </div>

    <!-- ==================== 报表1：资产类型分组统计 ==================== -->
    <div class="section-card">
      <div class="section-title">
        <span class="title-icon"></span>
        <span>资产类型分组统计</span>
      </div>
      <div class="section-body">
        <el-table :data="assetTypeStatisticsData" size="small" :row-class-name="summaryRowClass" class="report-table">
          <el-table-column prop="assetTypeName" label="资产类型" width="160" />
          <el-table-column label="资产金额" min-width="160" align="right">
            <template #default="scope">¥{{ formatMoney(scope.row.amount) }}</template>
          </el-table-column>
          <el-table-column label="投入金额" min-width="160" align="right">
            <template #default="scope">¥{{ formatMoney(scope.row.investAmount) }}</template>
          </el-table-column>
          <el-table-column label="资产数" width="120" align="center">
            <template #default="scope">
              <el-link type="primary" :underline="false" @click="openDrillByType(scope.row.assetType, scope.row.assetTypeName)">{{ scope.row.assetCount }}</el-link>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- ==================== 报表2：基金类型分析 ==================== -->
    <div class="section-card">
      <div class="section-title">
        <span class="title-icon fund-icon"></span>
        <span>基金类型分析</span>
      </div>
      <div class="section-body">
        <el-table :data="fundSubTypeData" size="small" :row-class-name="summaryRowClass" class="report-table">
          <el-table-column prop="assetSubTypeName" label="基金类型" width="160" />
          <el-table-column label="资产金额" min-width="130" align="right">
            <template #default="scope">¥{{ formatMoney(scope.row.amount) }}</template>
          </el-table-column>
          <el-table-column label="投入金额" min-width="130" align="right">
            <template #default="scope">¥{{ formatMoney(scope.row.investAmount) }}</template>
          </el-table-column>
          <el-table-column label="资产数" width="90" align="center">
            <template #default="scope">
              <el-link type="primary" :underline="false" @click="openDrillBySubType(3, scope.row.assetSubType, scope.row.assetSubTypeName, null)">{{ scope.row.assetCount }}</el-link>
            </template>
          </el-table-column>
          <el-table-column label="已清仓数" width="100" align="center">
            <template #default="scope">
              <el-link type="primary" :underline="false" @click="openDrillBySubType(3, scope.row.assetSubType, scope.row.assetSubTypeName, true)">{{ scope.row.clearedCount }}</el-link>
            </template>
          </el-table-column>
          <el-table-column label="未清仓数" width="100" align="center">
            <template #default="scope">
              <el-link type="primary" :underline="false" @click="openDrillBySubType(3, scope.row.assetSubType, scope.row.assetSubTypeName, false)">{{ scope.row.activeCount }}</el-link>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- ==================== 报表3：股票类型分析 ==================== -->
    <div class="section-card">
      <div class="section-title">
        <span class="title-icon stock-icon"></span>
        <span>股票类型分析</span>
      </div>
      <div class="section-body">
        <el-table :data="stockSubTypeData" size="small" :row-class-name="summaryRowClass" class="report-table">
          <el-table-column prop="assetSubTypeName" label="股票类型" width="160" />
          <el-table-column label="资产金额" min-width="130" align="right">
            <template #default="scope">¥{{ formatMoney(scope.row.amount) }}</template>
          </el-table-column>
          <el-table-column label="投入金额" min-width="130" align="right">
            <template #default="scope">¥{{ formatMoney(scope.row.investAmount) }}</template>
          </el-table-column>
          <el-table-column label="资产数" width="90" align="center">
            <template #default="scope">
              <el-link type="primary" :underline="false" @click="openDrillBySubType(4, scope.row.assetSubType, scope.row.assetSubTypeName, null)">{{ scope.row.assetCount }}</el-link>
            </template>
          </el-table-column>
          <el-table-column label="已清仓数" width="100" align="center">
            <template #default="scope">
              <el-link type="primary" :underline="false" @click="openDrillBySubType(4, scope.row.assetSubType, scope.row.assetSubTypeName, true)">{{ scope.row.clearedCount }}</el-link>
            </template>
          </el-table-column>
          <el-table-column label="未清仓数" width="100" align="center">
            <template #default="scope">
              <el-link type="primary" :underline="false" @click="openDrillBySubType(4, scope.row.assetSubType, scope.row.assetSubTypeName, false)">{{ scope.row.activeCount }}</el-link>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- ==================== ECharts 图表区域 ==================== -->
    <el-row :gutter="16" class="charts-row">
      <el-col :span="8">
        <div class="section-card">
          <div class="section-title">
            <span class="title-icon chart-icon"></span>
            <span>资产类型金额分布</span>
          </div>
          <div class="section-body chart-body">
            <div ref="assetTypeChartRef" style="height: 280px;"></div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="section-card">
          <div class="section-title">
            <span class="title-icon chart-icon"></span>
            <span>储蓄子类金额分布</span>
          </div>
          <div class="section-body chart-body">
            <div ref="savingSubTypeChartRef" style="height: 280px;"></div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="section-card">
          <div class="section-title">
            <span class="title-icon chart-icon"></span>
            <span>基金子类金额分布</span>
          </div>
          <div class="section-body chart-body">
            <div ref="fundSubTypeChartRef" style="height: 280px;"></div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="charts-row">
      <el-col :span="12">
        <div class="section-card">
          <div class="section-title">
            <span class="title-icon chart-icon"></span>
            <span>股票子类金额分布</span>
          </div>
          <div class="section-body chart-body">
            <div ref="stockSubTypeChartRef" style="height: 280px;"></div>
          </div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="section-card">
          <div class="section-title">
            <span class="title-icon chart-icon"></span>
            <span>已清仓资产收益</span>
          </div>
          <div class="section-body chart-body">
            <div ref="clearedProfitChartRef" style="height: 280px;"></div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- ==================== 穿透明细弹窗 ==================== -->
    <el-dialog v-model="drillDialogVisible" :title="drillDialogTitle" width="75%" :close-on-click-modal="false" draggable>
      <el-table :data="drillTableData" border stripe size="small" height="400">
        <el-table-column prop="assetName" label="资产名称" min-width="150" />
        <el-table-column prop="assetCode" label="资产官方标识" min-width="120" />
        <el-table-column prop="assetNo" label="资产编号" width="160" />
        <el-table-column label="资产金额" width="130" align="right">
          <template #default="scope">¥{{ formatMoney(scope.row.amount) }}</template>
        </el-table-column>
        <el-table-column label="投入金额" width="130" align="right">
          <template #default="scope">{{ scope.row.investAmount ? '¥' + formatMoney(scope.row.investAmount) : '-' }}</template>
        </el-table-column>
        <el-table-column prop="assetStatus" label="资产状态" width="100">
          <template #default="scope">{{ getAssetStatusText(scope.row.assetStatus) }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column prop="updateTime" label="更新时间" width="160" />
      </el-table>
      <div class="drill-pagination">
        <el-pagination background layout="total, sizes, prev, pager, next" :total="drillTotal"
          :page-size="drillPageParams.limit" :current-page="drillPageParams.page"
          :page-sizes="[10, 20, 50]" @size-change="handleDrillSizeChange" @current-change="handleDrillPageChange" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, onActivated } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import {
  GetAssetTypeStatistics,
  GetFundSubTypeAnalysis,
  GetStockSubTypeAnalysis,
  GetAssetDetailByType,
  GetAssetDetailBySubTypeAndStatus,
  GetAssetTypeDistribution,
  GetSubTypeDistribution,
  GetClearedAssetProfit
} from '@/api/personalObservationArea/assetDynamicsObservation'
import { GetKeyAndValueByType } from '@/api/sysDict'

// ==================== 数据字典 ====================
const assetStatusDict = ref([])

const loadDict = async () => {
  try {
    const res = await GetKeyAndValueByType('asset_status')
    if (res.code === 200 && res.data) assetStatusDict.value = res.data
  } catch (e) { console.error('加载资产状态字典失败', e) }
}

const getAssetStatusText = (status) => {
  const item = assetStatusDict.value.find(d => d.value === status)
  return item ? item.text : status
}

// ==================== 报表数据 ====================
const assetTypeStatisticsData = ref([])
const fundSubTypeData = ref([])
const stockSubTypeData = ref([])

// ==================== 图表 ====================
const assetTypeChartRef = ref(null)
const savingSubTypeChartRef = ref(null)
const fundSubTypeChartRef = ref(null)
const stockSubTypeChartRef = ref(null)
const clearedProfitChartRef = ref(null)

let assetTypeChartInstance = null
let savingSubTypeChartInstance = null
let fundSubTypeChartInstance = null
let stockSubTypeChartInstance = null
let clearedProfitChartInstance = null

let isInitialized = false

// ==================== 穿透明细 ====================
const drillDialogVisible = ref(false)
const drillDialogTitle = ref('')
const drillTableData = ref([])
const drillTotal = ref(0)
const drillPageParams = ref({ page: 1, limit: 10 })

let drillMode = ''
let drillAssetType = null
let drillAssetSubType = null
let drillIsCleared = null

// ==================== 工具函数 ====================
const formatMoney = (value) => {
  if (!value && value !== 0) return '0.00'
  return Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const formatLongText = (text, maxLength = 6) => {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

// 合计行样式：后端返回 assetType=-1 或 assetSubType=-1 的为合计行
const summaryRowClass = ({ row }) => {
  if (row.assetType === -1 || row.assetSubType === -1) return 'summary-row'
  return ''
}

// ==================== 穿透明细操作 ====================
const openDrillByType = (assetType, typeName) => {
  drillMode = 'type'
  drillAssetType = assetType
  drillAssetSubType = null
  drillIsCleared = null
  drillDialogTitle.value = `${typeName} - 资产明细`
  drillPageParams.value = { page: 1, limit: 10 }
  drillDialogVisible.value = true
  fetchDrillData()
}

const openDrillBySubType = (assetType, assetSubType, subTypeName, isCleared) => {
  drillMode = 'subType'
  drillAssetType = assetType
  drillAssetSubType = assetSubType
  drillIsCleared = isCleared
  const statusText = isCleared === true ? '已清仓' : isCleared === false ? '未清仓' : '全部'
  drillDialogTitle.value = `${subTypeName} - ${statusText}资产明细`
  drillPageParams.value = { page: 1, limit: 10 }
  drillDialogVisible.value = true
  fetchDrillData()
}

const fetchDrillData = async () => {
  try {
    const { page, limit } = drillPageParams.value
    let res
    if (drillMode === 'type') {
      res = await GetAssetDetailByType(drillAssetType, page, limit)
    } else {
      res = await GetAssetDetailBySubTypeAndStatus(drillAssetType, drillAssetSubType, drillIsCleared, page, limit)
    }
    if (res.code === 200 && res.data) {
      drillTableData.value = res.data.records || []
      drillTotal.value = res.data.total || 0
    }
  } catch (e) {
    console.error('获取穿透明细数据失败', e)
    ElMessage.error('获取明细数据失败')
  }
}

const handleDrillPageChange = (page) => {
  drillPageParams.value.page = page
  fetchDrillData()
}

const handleDrillSizeChange = (size) => {
  drillPageParams.value.limit = size
  drillPageParams.value.page = 1
  fetchDrillData()
}

// ==================== 数据加载 ====================
const fetchAllData = async () => {
  try {
    const [typeStatRes, fundRes, stockRes, typeDistRes, savingDistRes, fundDistRes, stockDistRes, clearedRes] = await Promise.all([
      GetAssetTypeStatistics(),
      GetFundSubTypeAnalysis(),
      GetStockSubTypeAnalysis(),
      GetAssetTypeDistribution(),
      GetSubTypeDistribution(1),
      GetSubTypeDistribution(3),
      GetSubTypeDistribution(4),
      GetClearedAssetProfit()
    ])

    if (typeStatRes.code === 200 && typeStatRes.data) assetTypeStatisticsData.value = typeStatRes.data
    if (fundRes.code === 200 && fundRes.data) fundSubTypeData.value = fundRes.data
    if (stockRes.code === 200 && stockRes.data) stockSubTypeData.value = stockRes.data

    updatePieChart(assetTypeChartInstance, typeDistRes)
    updatePieChart(savingSubTypeChartInstance, savingDistRes)
    updatePieChart(fundSubTypeChartInstance, fundDistRes)
    updatePieChart(stockSubTypeChartInstance, stockDistRes)
    updateClearedProfitChart(clearedRes)
  } catch (e) {
    console.error('获取数据失败', e)
    ElMessage.error('获取数据失败')
  }
}

// ==================== 图表 ====================
const initPieChart = (refObj) => {
  if (!refObj) return null
  return echarts.init(refObj)
}

const updatePieChart = (instance, res) => {
  if (!instance || !res || res.code !== 200 || !res.data || !res.data.length) return
  const data = res.data.map(item => ({ value: Number(item.value) || 0, name: item.text }))
  instance.setOption({
    tooltip: { trigger: 'item', formatter: (p) => `${p.name}: ¥${p.value.toLocaleString()} (${p.percent}%)` },
    legend: {
      orient: 'horizontal',
      bottom: 5,
      left: 'center',
      itemGap: 12,
      formatter: (n) => formatLongText(n, 6),
      tooltip: { show: true, formatter: (p) => p.name }
    },
    series: [{
      type: 'pie',
      radius: ['30%', '55%'],
      center: ['50%', '40%'],
      data,
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' } },
      label: {
        formatter: (p) => formatLongText(p.name, 4),
        fontSize: 11
      },
      labelLine: {
        length: 10,
        length2: 15,
        smooth: true
      },
      labelLayout: {
        hideOverlap: true
      }
    }]
  })
}

const updateClearedProfitChart = (res) => {
  if (!clearedProfitChartInstance || !res || res.code !== 200 || !res.data || !res.data.length) return
  const sorted = [...res.data].sort((a, b) => (a.value || 0) - (b.value || 0))
  const names = sorted.map(i => formatLongText(i.text, 6))
  const values = sorted.map(i => Number(i.value) || 0)

  clearedProfitChartInstance.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: (params) => `${sorted[params[0].dataIndex].text}<br/>收益: ¥${params[0].value.toLocaleString()}` },
    grid: { left: '3%', right: '4%', top: '8%', bottom: '8%', containLabel: true },
    xAxis: { type: 'category', data: names, axisLabel: { interval: 0, rotate: 30, fontSize: 10 } },
    yAxis: { type: 'value', axisLabel: { formatter: (v) => v >= 10000 ? `¥${(v / 10000).toFixed(0)}万` : v < -10000 ? `-¥${(Math.abs(v) / 10000).toFixed(0)}万` : `¥${v}` } },
    series: [{
      name: '清仓收益', type: 'bar', data: values,
      itemStyle: {
        color: (params) => params.value >= 0
          ? new echarts.graphic.LinearGradient(0, 1, 0, 0, [{ offset: 0, color: '#95d475' }, { offset: 1, color: '#67c23a' }])
          : new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#f89898' }, { offset: 1, color: '#f56c6c' }])
      }
    }]
  })
}

const initAllCharts = () => {
  assetTypeChartInstance = initPieChart(assetTypeChartRef.value)
  savingSubTypeChartInstance = initPieChart(savingSubTypeChartRef.value)
  fundSubTypeChartInstance = initPieChart(fundSubTypeChartRef.value)
  stockSubTypeChartInstance = initPieChart(stockSubTypeChartRef.value)
  clearedProfitChartInstance = echarts.init(clearedProfitChartRef.value)
  isInitialized = true
  fetchAllData()
}

const handleResize = () => {
  assetTypeChartInstance?.resize()
  savingSubTypeChartInstance?.resize()
  fundSubTypeChartInstance?.resize()
  stockSubTypeChartInstance?.resize()
  clearedProfitChartInstance?.resize()
}

onMounted(() => {
  loadDict()
  setTimeout(() => { initAllCharts() }, 100)
  window.addEventListener('resize', handleResize)
})

onActivated(() => {
  if (!isInitialized) {
    setTimeout(() => { initAllCharts() }, 100)
  } else {
    fetchAllData()
  }
})

onBeforeUnmount(() => {
  assetTypeChartInstance?.dispose()
  savingSubTypeChartInstance?.dispose()
  fundSubTypeChartInstance?.dispose()
  stockSubTypeChartInstance?.dispose()
  clearedProfitChartInstance?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.asset-dynamics-observation {
  padding: 0 16px 20px;
  background: #f0f2f5;
  min-height: 100vh;
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 16px 24px;
  margin: 0 -16px 16px;
  border-radius: 0 0 8px 8px;
}

.page-header h1 {
  margin: 0;
  color: #fff;
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 1px;
}

/* ==================== 通用区块卡片 ==================== */
.section-card {
  background: #fff;
  border-radius: 8px;
  margin-bottom: 16px;
  overflow: hidden;
  box-shadow: 0 1px 4px 0 rgba(0, 0, 0, 0.08);
  transition: box-shadow 0.3s;
}

.section-card:hover {
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.12);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 20px;
  font-size: 15px;
  font-weight: 700;
  color: #303133;
  background: #fafbfc;
  border-bottom: 1px solid #ebeef5;
}

.title-icon {
  display: inline-block;
  width: 4px;
  height: 16px;
  border-radius: 2px;
  background: #667eea;
}

.title-icon.fund-icon {
  background: #e6a23c;
}

.title-icon.stock-icon {
  background: #f56c6c;
}

.title-icon.chart-icon {
  background: #67c23a;
}

.section-body {
  padding: 12px 16px;
}

.section-body.chart-body {
  padding: 8px;
}

/* ==================== 报表表格样式 ==================== */
.report-table {
  width: 100%;
}

.report-table :deep(.el-table__header th) {
  background: #f5f7fa !important;
  color: #606266;
  font-weight: 600;
  font-size: 13px;
  padding: 10px 0;
  border-bottom: 2px solid #ebeef5;
}

.report-table :deep(.el-table__body td) {
  padding: 9px 0;
  font-size: 13px;
  color: #303133;
  border-bottom: 1px solid #f2f3f5;
}

.report-table :deep(.el-table__body tr:hover > td) {
  background: #ecf5ff !important;
}

/* 合计行样式 */
.report-table :deep(.el-table__body tr.summary-row > td) {
  background: #fafbfc !important;
  font-weight: 700;
  color: #303133;
  border-top: 2px solid #dcdfe6;
  border-bottom: none;
}

.report-table :deep(.el-table__body tr.summary-row:hover > td) {
  background: #ecf5ff !important;
}

/* ==================== 图表行 ==================== */
.charts-row {
  margin-bottom: 0;
}

/* ==================== 穿透明细弹窗 ==================== */
.drill-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
