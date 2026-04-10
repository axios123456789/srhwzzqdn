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
  totalAssets: 100000,
  investmentAssets: 40000,
  savingsAssets: 60000
})

// 格式化金额
const formatMoney = (value) => {
  return value.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

// 初始化资产类型占比图表
const initAssetTypeChart = () => {
  if (!assetTypeChart.value) return
  
  assetTypeChartInstance = echarts.init(assetTypeChart.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: ¥{c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '资产类型',
        type: 'pie',
        radius: '50%',
        data: [
          { value: 30000, name: '储蓄' },
          { value: 20000, name: '基金' },
          { value: 15000, name: '股票' },
          { value: 35000, name: '保险' }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
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
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c}个 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '资产状态',
        type: 'pie',
        radius: '50%',
        data: [
          { value: 15, name: '正常' },
          { value: 2, name: '冻结' },
          { value: 3, name: '已赎回' },
          { value: 1, name: '已清仓' }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
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
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params) => {
        return `${params[0].name}<br/>金额: ¥${params[0].value.toLocaleString()}`
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
          return `¥${(value / 10000).toFixed(0)}万`
        }
      }
    },
    yAxis: {
      type: 'category',
      data: ['货币基金', '定期存款', '股票A', '基金B', '保险C', '活期存款']
    },
    series: [
      {
        name: '资产金额',
        type: 'bar',
        data: [5000, 15000, 18000, 22000, 25000, 30000],
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
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
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
      data: ['股票A', '基金B', '基金C', '保险D', '理财E']
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (value) => {
          return `¥${(value / 10000).toFixed(0)}万`
        }
      }
    },
    series: [
      {
        name: '资产金额',
        type: 'bar',
        data: [18000, 22000, 15000, 25000, 12000],
        itemStyle: {
          color: '#67c23a'
        }
      },
      {
        name: '投入金额',
        type: 'bar',
        data: [15000, 20000, 16000, 25000, 10000],
        itemStyle: {
          color: '#409eff'
        }
      }
    ]
  }
  
  investmentReturnChartInstance.setOption(option)
}

// 初始化趋势图
const initTrendChart = (chartRef, data, color) => {
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
      data: ['1月', '2月', '3月', '4月', '5月', '6月']
    },
    yAxis: {
      type: 'value',
      show: false
    },
    series: [
      {
        data: data,
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

// 初始化所有图表
const initAllCharts = () => {
  initAssetTypeChart()
  initAssetStatusChart()
  initAssetRankChart()
  initInvestmentReturnChart()
  
  // 初始化趋势图
  totalAssetsChartInstance = initTrendChart(totalAssetsChart, [80000, 85000, 90000, 95000, 98000, 100000], '#409eff')
  investmentAssetsChartInstance = initTrendChart(investmentAssetsChart, [30000, 32000, 35000, 38000, 39000, 40000], '#67c23a')
  savingsAssetsChartInstance = initTrendChart(savingsAssetsChart, [50000, 53000, 55000, 57000, 59000, 60000], '#e6a23c')
  
  isInitialized = true
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
