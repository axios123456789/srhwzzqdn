<template>
  <div class="asset-ledger-analysis">
    <!-- 净资产总览卡片 -->
    <el-row :gutter="20" class="overview-cards">
      <el-col :span="8">
        <el-card class="overview-card">
          <div class="card-content">
            <div class="card-title">总资产</div>
            <div class="card-amount">¥{{ formatMoney(overviewData.totalAssets) }}</div>
            <div class="card-trend" ref="totalAssetsChart" style="height: 60px;"></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="overview-card">
          <div class="card-content">
            <div class="card-title">投资资产</div>
            <div class="card-amount">¥{{ formatMoney(overviewData.investmentAssets) }}</div>
            <div class="card-trend" ref="investmentAssetsChart" style="height: 60px;"></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="overview-card">
          <div class="card-content">
            <div class="card-title">储蓄资产</div>
            <div class="card-amount">¥{{ formatMoney(overviewData.savingsAssets) }}</div>
            <div class="card-trend" ref="savingsAssetsChart" style="height: 60px;"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <!-- 资产类型占比 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>资产类型占比</span>
            </div>
          </template>
          <div ref="assetTypeChart" style="height: 300px;"></div>
        </el-card>
      </el-col>

      <!-- 资产状态分布 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>资产状态分布</span>
            </div>
          </template>
          <div ref="assetStatusChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <!-- 资产金额排行 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>资产金额排行</span>
            </div>
          </template>
          <div ref="assetRankChart" style="height: 350px;"></div>
        </el-card>
      </el-col>

      <!-- 投资收益分析 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>投资收益分析</span>
            </div>
          </template>
          <div ref="investmentReturnChart" style="height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, onActivated } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import {
  GetAssetStructure,
  GetAssetTypeGroup,
  GetAssetStatusGroup,
  GetAssetAmountRank,
  GetInvestmentReturnAnalysis
} from '@/api/assetClusteringAnalysis'

// 图表实例
const assetTypeChart = ref(null)
const assetStatusChart = ref(null)
const assetRankChart = ref(null)
const investmentReturnChart = ref(null)
const totalAssetsChart = ref(null)
const investmentAssetsChart = ref(null)
const savingsAssetsChart = ref(null)

let assetTypeChartInstance = null
let assetStatusChartInstance = null
let assetRankChartInstance = null
let investmentReturnChartInstance = null
let totalAssetsChartInstance = null
let investmentAssetsChartInstance = null
let savingsAssetsChartInstance = null

// 标记是否已初始化
let isInitialized = false

// 净资产总览数据
const overviewData = ref({
  totalAssets: 0,
  investmentAssets: 0,
  savingsAssets: 0
})

// 数据存储
let assetTypeData = []
let assetStatusData = []
let assetRankData = []
let investmentReturnData = []

// 格式化金额
const formatMoney = (value) => {
  if (!value && value !== 0) return '0.00'
  return Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

// 处理长文本显示（截断并添加省略号）
const formatLongText = (text, maxLength = 6) => {
  if (!text) return ''
  if (text.length > maxLength) {
    return text.substring(0, maxLength) + '...'
  }
  return text
}

// 获取所有数据
const fetchAllData = async () => {
  try {
    // 并行请求所有数据
    const [structureRes, typeRes, statusRes, rankRes, investmentRes] = await Promise.all([
      GetAssetStructure(),
      GetAssetTypeGroup(),
      GetAssetStatusGroup(),
      GetAssetAmountRank(),
      GetInvestmentReturnAnalysis()
    ])
    
    // 处理资产结构数据
    if (structureRes.code === 200 && structureRes.data) {
      overviewData.value = {
        totalAssets: structureRes.data.sumAsset || 0,
        investmentAssets: structureRes.data.investAsset || 0,
        savingsAssets: structureRes.data.savingAsset || 0
      }
    }
    
    // 处理资产类型数据
    if (typeRes.code === 200 && typeRes.data) {
      assetTypeData = typeRes.data
    }
    
    // 处理资产状态数据
    if (statusRes.code === 200 && statusRes.data) {
      assetStatusData = statusRes.data
    }
    
    // 处理资产金额排行数据
    if (rankRes.code === 200 && rankRes.data) {
      assetRankData = rankRes.data
    }
    
    // 处理投资收益分析数据
    if (investmentRes.code === 200 && investmentRes.data) {
      investmentReturnData = investmentRes.data
    }
    
    // 更新所有图表
    updateAllCharts()
    
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败，请稍后重试')
  }
}

// 初始化资产类型占比图表
const initAssetTypeChart = () => {
  if (!assetTypeChart.value) return
  
  assetTypeChartInstance = echarts.init(assetTypeChart.value)
  updateAssetTypeChart()
}

// 更新资产类型占比图表
const updateAssetTypeChart = () => {
  if (!assetTypeChartInstance || !assetTypeData.length) return
  
  const data = assetTypeData.map(item => ({
    value: item.value || 0,
    name: item.text
  }))
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        return `${params.seriesName}<br/>${params.marker}${params.name}: ¥${params.value.toLocaleString()} (${params.percent}%)`
      }
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      formatter: (name) => formatLongText(name, 6),
      tooltip: {
        show: true,
        formatter: (params) => params.name
      }
    },
    series: [
      {
        name: '资产类型',
        type: 'pie',
        radius: '50%',
        data: data,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        label: {
          formatter: (params) => formatLongText(params.name, 4)
        }
      }
    ]
  }
  
  assetTypeChartInstance.setOption(option)
}

// 初始化资产状态分布图表
const initAssetStatusChart = () => {
  if (!assetStatusChart.value) return
  
  assetStatusChartInstance = echarts.init(assetStatusChart.value)
  updateAssetStatusChart()
}

// 更新资产状态分布图表
const updateAssetStatusChart = () => {
  if (!assetStatusChartInstance || !assetStatusData.length) return
  
  const data = assetStatusData.map(item => ({
    value: item.value || 0,
    name: item.text
  }))
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        return `${params.seriesName}<br/>${params.marker}${params.name}: ${params.value}元 (${params.percent}%)`
      }
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      formatter: (name) => formatLongText(name, 6),
      tooltip: {
        show: true,
        formatter: (params) => params.name
      }
    },
    series: [
      {
        name: '资产状态',
        type: 'pie',
        radius: '50%',
        data: data,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        label: {
          formatter: (params) => formatLongText(params.name, 4)
        }
      }
    ]
  }
  
  assetStatusChartInstance.setOption(option)
}

// 初始化资产金额排行图表
const initAssetRankChart = () => {
  if (!assetRankChart.value) return
  
  assetRankChartInstance = echarts.init(assetRankChart.value)
  updateAssetRankChart()
}

// 更新资产金额排行图表
const updateAssetRankChart = () => {
  if (!assetRankChartInstance || !assetRankData.length) return
  
  // 按金额排序（从小到大，这样在横向柱状图中最大的在上面）
  const sortedData = [...assetRankData].sort((a, b) => (a.value || 0) - (b.value || 0))
  
  const names = sortedData.map(item => formatLongText(item.text, 8))
  const values = sortedData.map(item => item.value || 0)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params) => {
        const originalName = sortedData[params[0].dataIndex].text
        return `${originalName}<br/>金额: ¥${params[0].value.toLocaleString()}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      axisLabel: {
        formatter: (value) => {
          if (value >= 10000) {
            return `¥${(value / 10000).toFixed(0)}万`
          }
          return `¥${value}`
        }
      }
    },
    yAxis: {
      type: 'category',
      data: names,
      axisLabel: {
        interval: 0,
        formatter: (value) => value
      }
    },
    series: [
      {
        name: '资产金额',
        type: 'bar',
        data: values,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#237df2' }
          ])
        }
      }
    ]
  }
  
  assetRankChartInstance.setOption(option)
}

// 初始化投资收益分析图表
const initInvestmentReturnChart = () => {
  if (!investmentReturnChart.value) return
  
  investmentReturnChartInstance = echarts.init(investmentReturnChart.value)
  updateInvestmentReturnChart()
}

// 更新投资收益分析图表
const updateInvestmentReturnChart = () => {
  if (!investmentReturnChartInstance || !investmentReturnData.length) return
  
  const names = investmentReturnData.map(item => formatLongText(item.assetName, 6))
  const assetAmounts = investmentReturnData.map(item => item.assetAmount || 0)
  const investAmounts = investmentReturnData.map(item => item.investAmount || 0)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params) => {
        const originalName = investmentReturnData[params[0].dataIndex].assetName
        let result = originalName + '<br/>'
        params.forEach(param => {
          result += `${param.marker}${param.seriesName}: ¥${param.value.toLocaleString()}<br/>`
        })
        return result
      }
    },
    legend: {
      data: ['资产金额', '投入金额'],
      top: 'top'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: {
        interval: 0,
        rotate: names.length > 6 ? 30 : 0,
        formatter: (value) => value
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (value) => {
          if (value >= 10000) {
            return `¥${(value / 10000).toFixed(0)}万`
          }
          return `¥${value}`
        }
      }
    },
    series: [
      {
        name: '资产金额',
        type: 'bar',
        data: assetAmounts,
        itemStyle: {
          color: '#67c23a'
        }
      },
      {
        name: '投入金额',
        type: 'bar',
        data: investAmounts,
        itemStyle: {
          color: '#409eff'
        }
      }
    ]
  }
  
  investmentReturnChartInstance.setOption(option)
}

// 初始化趋势图
const initTrendChart = (chartRef, color) => {
  if (!chartRef.value) return null
  
  const chartInstance = echarts.init(chartRef.value)
  
  const option = {
    grid: {
      left: 0,
      right: 0,
      top: 5,
      bottom: 5
    },
    xAxis: {
      type: 'category',
      show: false,
      data: []
    },
    yAxis: {
      type: 'value',
      show: false
    },
    series: [
      {
        data: [],
        type: 'line',
        smooth: true,
        symbol: 'none',
        lineStyle: {
          color: color,
          width: 2
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: color + '40' },
            { offset: 1, color: color + '00' }
          ])
        }
      }
    ]
  }
  
  chartInstance.setOption(option)
  return chartInstance
}

// 更新趋势图
const updateTrendChart = (chartInstance, data) => {
  if (!chartInstance) return
  
  const option = {
    xAxis: {
      data: data.map((_, index) => index + 1)
    },
    series: [{
      data: data
    }]
  }
  
  chartInstance.setOption(option)
}

// 更新所有图表
const updateAllCharts = () => {
  updateAssetTypeChart()
  updateAssetStatusChart()
  updateAssetRankChart()
  updateInvestmentReturnChart()
  
  // 更新趋势图（使用模拟数据，因为后端没有提供历史趋势数据）
  const totalAssetsTrend = Array(6).fill(0).map(() => 
    overviewData.value.totalAssets * (0.8 + Math.random() * 0.2)
  )
  const investmentAssetsTrend = Array(6).fill(0).map(() => 
    overviewData.value.investmentAssets * (0.8 + Math.random() * 0.2)
  )
  const savingsAssetsTrend = Array(6).fill(0).map(() => 
    overviewData.value.savingsAssets * (0.8 + Math.random() * 0.2)
  )
  
  updateTrendChart(totalAssetsChartInstance, totalAssetsTrend)
  updateTrendChart(investmentAssetsChartInstance, investmentAssetsTrend)
  updateTrendChart(savingsAssetsChartInstance, savingsAssetsTrend)
}

// 初始化所有图表
const initAllCharts = () => {
  initAssetTypeChart()
  initAssetStatusChart()
  initAssetRankChart()
  initInvestmentReturnChart()
  
  // 初始化趋势图
  totalAssetsChartInstance = initTrendChart(totalAssetsChart, '#409eff')
  investmentAssetsChartInstance = initTrendChart(investmentAssetsChart, '#67c23a')
  savingsAssetsChartInstance = initTrendChart(savingsAssetsChart, '#e6a23c')
  
  isInitialized = true
  
  // 初始化后立即加载数据
  fetchAllData()
}

// 窗口大小改变时重新渲染图表
const handleResize = () => {
  assetTypeChartInstance?.resize()
  assetStatusChartInstance?.resize()
  assetRankChartInstance?.resize()
  investmentReturnChartInstance?.resize()
  totalAssetsChartInstance?.resize()
  investmentAssetsChartInstance?.resize()
  savingsAssetsChartInstance?.resize()
}

onMounted(() => {
  // 使用 setTimeout 确保 DOM 已完全渲染
  setTimeout(() => {
    initAllCharts()
  }, 100)
  
  // 监听窗口大小改变
  window.addEventListener('resize', handleResize)
})

// 当组件被激活时（标签页切换），重新初始化图表
onActivated(() => {
  if (!isInitialized) {
    setTimeout(() => {
      initAllCharts()
    }, 100)
  } else {
    // 如果已初始化，重新加载数据
    fetchAllData()
  }
})

onBeforeUnmount(() => {
  // 销毁图表实例
  assetTypeChartInstance?.dispose()
  assetStatusChartInstance?.dispose()
  assetRankChartInstance?.dispose()
  investmentReturnChartInstance?.dispose()
  totalAssetsChartInstance?.dispose()
  investmentAssetsChartInstance?.dispose()
  savingsAssetsChartInstance?.dispose()
  
  // 移除事件监听
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.asset-ledger-analysis {
  padding: 0;
}

.overview-cards {
  margin-bottom: 20px;
}

.overview-card {
  height: 150px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.overview-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.card-content {
  text-align: center;
}

.card-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.card-amount {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 100%;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.chart-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.card-header {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}
</style>
