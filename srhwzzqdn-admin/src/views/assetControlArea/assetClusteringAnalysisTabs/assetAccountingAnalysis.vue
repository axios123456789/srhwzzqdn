<template>
  <div class="asset-accounting-analysis">
    <!-- 时间范围筛选 -->
    <div class="filter-section">
      <el-form :inline="true" size="small">
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            :unlink-panels="true"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="refreshData">
            <el-icon><Search /></el-icon>查询
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <!-- 收入VS支出 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>收入VS支出</span>
            </div>
          </template>
          <div ref="incomeExpenseChart" style="height: 300px;"></div>
        </el-card>
      </el-col>

      <!-- 收支趋势 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>收支趋势</span>
            </div>
          </template>
          <div ref="trendChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <!-- 支出结构 -->
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>支出结构</span>
            </div>
          </template>
          <div ref="expenseStructureChart" style="height: 280px;"></div>
        </el-card>
      </el-col>

      <!-- 支出类型 -->
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>支出类型</span>
            </div>
          </template>
          <div ref="expenseTypeChart" style="height: 280px;"></div>
        </el-card>
      </el-col>

      <!-- 收入来源 -->
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>收入来源</span>
            </div>
          </template>
          <div ref="incomeSourceChart" style="height: 280px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, onActivated } from 'vue'
import { Search } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import {
  GetTransactionAmountGroup,
  GetExpenseStructureGroup,
  GetSpendingTypeGroup,
  GetIncomeSourceGroup
} from '@/api/assetClusteringAnalysis'

// 时间范围
const dateRange = ref([])

// 图表实例
const incomeExpenseChart = ref(null)
const trendChart = ref(null)
const expenseStructureChart = ref(null)
const expenseTypeChart = ref(null)
const incomeSourceChart = ref(null)

let incomeExpenseChartInstance = null
let trendChartInstance = null
let expenseStructureChartInstance = null
let expenseTypeChartInstance = null
let incomeSourceChartInstance = null

// 标记是否已初始化
let isInitialized = false

// 数据存储
let transactionAmountData = []
let expenseStructureData = []
let expenseTypeData = []
let incomeSourceData = []

// 处理长文本显示（截断并添加省略号）
const formatLongText = (text, maxLength = 6) => {
  if (!text) return ''
  if (text.length > maxLength) {
    return text.substring(0, maxLength) + '...'
  }
  return text
}

// 获取默认时间范围（当前年份1月1日到当前时间后一天）
const getDefaultDateRange = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  
  // 开始时间：当前年份1月1日
  const startDate = `${year}-01-01`
  
  // 结束时间：当前时间后一天
  const tomorrow = new Date(now)
  tomorrow.setDate(tomorrow.getDate() + 1)
  const endYear = tomorrow.getFullYear()
  const endMonth = String(tomorrow.getMonth() + 1).padStart(2, '0')
  const endDay = String(tomorrow.getDate()).padStart(2, '0')
  const endDate = `${endYear}-${endMonth}-${endDay}`
  
  return [startDate, endDate]
}

// 处理日期变化
const handleDateChange = (val) => {
  console.log('日期范围:', val)
}

// 刷新数据
const refreshData = async () => {
  let beginTime, endTime
  
  if (dateRange.value && dateRange.value.length === 2) {
    beginTime = dateRange.value[0]
    endTime = dateRange.value[1]
  } else {
    const defaultRange = getDefaultDateRange()
    beginTime = defaultRange[0]
    endTime = defaultRange[1]
    dateRange.value = defaultRange
  }
  
  try {
    // 并行请求所有数据
    const [transactionRes, expenseStructureRes, expenseTypeRes, incomeSourceRes] = await Promise.all([
      GetTransactionAmountGroup(beginTime, endTime),
      GetExpenseStructureGroup(beginTime, endTime),
      GetSpendingTypeGroup(beginTime, endTime),
      GetIncomeSourceGroup(beginTime, endTime)
    ])
    
    // 处理收支金额数据
    if (transactionRes.code === 200 && transactionRes.data) {
      transactionAmountData = transactionRes.data
    }
    
    // 处理支出结构数据
    if (expenseStructureRes.code === 200 && expenseStructureRes.data) {
      expenseStructureData = expenseStructureRes.data
    }
    
    // 处理支出类型数据
    if (expenseTypeRes.code === 200 && expenseTypeRes.data) {
      expenseTypeData = expenseTypeRes.data
    }
    
    // 处理收入来源数据
    if (incomeSourceRes.code === 200 && incomeSourceRes.data) {
      incomeSourceData = incomeSourceRes.data
    }
    
    // 更新所有图表
    updateAllCharts()
    
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败，请稍后重试')
  }
}

// 初始化收入VS支出图表
const initIncomeExpenseChart = () => {
  if (!incomeExpenseChart.value) return
  
  incomeExpenseChartInstance = echarts.init(incomeExpenseChart.value)
  updateIncomeExpenseChart()
}

// 更新收入VS支出图表
const updateIncomeExpenseChart = () => {
  if (!incomeExpenseChartInstance || !transactionAmountData.length) return
  
  const times = transactionAmountData.map(item => item.timeName)
  const incomes = transactionAmountData.map(item => item.invoiceAmount || 0)
  const expenses = transactionAmountData.map(item => item.spendingAmount || 0)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params) => {
        const originalTime = times[params[0].dataIndex]
        let result = originalTime + '<br/>'
        params.forEach(param => {
          result += `${param.marker}${param.seriesName}: ¥${param.value.toLocaleString()}<br/>`
        })
        return result
      }
    },
    legend: {
      data: ['收入', '支出'],
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
      data: times.map(t => formatLongText(t, 8)),
      axisLabel: {
        interval: 0,
        rotate: times.length > 10 ? 45 : 0
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '¥{value}'
      }
    },
    series: [
      {
        name: '收入',
        type: 'bar',
        data: incomes,
        itemStyle: {
          color: '#67c23a'
        }
      },
      {
        name: '支出',
        type: 'bar',
        data: expenses,
        itemStyle: {
          color: '#f56c6c'
        }
      }
    ]
  }
  
  incomeExpenseChartInstance.setOption(option)
}

// 初始化收支趋势图表
const initTrendChart = () => {
  if (!trendChart.value) return
  
  trendChartInstance = echarts.init(trendChart.value)
  updateTrendChart()
}

// 更新收支趋势图表
const updateTrendChart = () => {
  if (!trendChartInstance || !transactionAmountData.length) return
  
  const times = transactionAmountData.map(item => item.timeName)
  const incomes = transactionAmountData.map(item => item.invoiceAmount || 0)
  const expenses = transactionAmountData.map(item => item.spendingAmount || 0)
  const balances = transactionAmountData.map(item => item.balanceAmount || 0)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const originalTime = times[params[0].dataIndex]
        let result = originalTime + '<br/>'
        params.forEach(param => {
          result += `${param.marker}${param.seriesName}: ¥${param.value.toLocaleString()}<br/>`
        })
        return result
      }
    },
    legend: {
      data: ['收入', '支出', '结余'],
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
      boundaryGap: false,
      data: times.map(t => formatLongText(t, 8)),
      axisLabel: {
        interval: 0,
        rotate: times.length > 10 ? 45 : 0
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '¥{value}'
      }
    },
    series: [
      {
        name: '收入',
        type: 'line',
        data: incomes,
        itemStyle: {
          color: '#67c23a'
        }
      },
      {
        name: '支出',
        type: 'line',
        data: expenses,
        itemStyle: {
          color: '#f56c6c'
        }
      },
      {
        name: '结余',
        type: 'line',
        data: balances,
        itemStyle: {
          color: '#409eff'
        }
      }
    ]
  }
  
  trendChartInstance.setOption(option)
}

// 初始化支出结构图表
const initExpenseStructureChart = () => {
  if (!expenseStructureChart.value) return
  
  expenseStructureChartInstance = echarts.init(expenseStructureChart.value)
  updateExpenseStructureChart()
}

// 更新支出结构图表
const updateExpenseStructureChart = () => {
  if (!expenseStructureChartInstance || !expenseStructureData.length) return
  
  const data = expenseStructureData.map(item => ({
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
      top: 'center',
      formatter: (name) => formatLongText(name, 6),
      tooltip: {
        show: true,
        formatter: (params) => params.name
      }
    },
    series: [
      {
        name: '支出结构',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['60%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '16',
            fontWeight: 'bold',
            formatter: (params) => params.name
          }
        },
        labelLine: {
          show: false
        },
        data: data
      }
    ]
  }
  
  expenseStructureChartInstance.setOption(option)
}

// 初始化支出类型图表
const initExpenseTypeChart = () => {
  if (!expenseTypeChart.value) return
  
  expenseTypeChartInstance = echarts.init(expenseTypeChart.value)
  updateExpenseTypeChart()
}

// 更新支出类型图表
const updateExpenseTypeChart = () => {
  if (!expenseTypeChartInstance || !expenseTypeData.length) return
  
  const data = expenseTypeData.map(item => ({
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
      top: 'center',
      formatter: (name) => formatLongText(name, 6),
      tooltip: {
        show: true,
        formatter: (params) => params.name
      }
    },
    series: [
      {
        name: '支出类型',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['60%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '16',
            fontWeight: 'bold',
            formatter: (params) => params.name
          }
        },
        labelLine: {
          show: false
        },
        data: data
      }
    ]
  }
  
  expenseTypeChartInstance.setOption(option)
}

// 初始化收入来源图表
const initIncomeSourceChart = () => {
  if (!incomeSourceChart.value) return
  
  incomeSourceChartInstance = echarts.init(incomeSourceChart.value)
  updateIncomeSourceChart()
}

// 更新收入来源图表
const updateIncomeSourceChart = () => {
  if (!incomeSourceChartInstance || !incomeSourceData.length) return
  
  const data = incomeSourceData.map(item => ({
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
      top: 'center',
      formatter: (name) => formatLongText(name, 6),
      tooltip: {
        show: true,
        formatter: (params) => params.name
      }
    },
    series: [
      {
        name: '收入来源',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['60%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '16',
            fontWeight: 'bold',
            formatter: (params) => params.name
          }
        },
        labelLine: {
          show: false
        },
        data: data
      }
    ]
  }
  
  incomeSourceChartInstance.setOption(option)
}

// 更新所有图表
const updateAllCharts = () => {
  updateIncomeExpenseChart()
  updateTrendChart()
  updateExpenseStructureChart()
  updateExpenseTypeChart()
  updateIncomeSourceChart()
}

// 初始化所有图表
const initAllCharts = () => {
  initIncomeExpenseChart()
  initTrendChart()
  initExpenseStructureChart()
  initExpenseTypeChart()
  initIncomeSourceChart()
  isInitialized = true
  
  // 初始化后立即加载数据
  refreshData()
}

// 窗口大小改变时重新渲染图表
const handleResize = () => {
  incomeExpenseChartInstance?.resize()
  trendChartInstance?.resize()
  expenseStructureChartInstance?.resize()
  expenseTypeChartInstance?.resize()
  incomeSourceChartInstance?.resize()
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
    refreshData()
  }
})

onBeforeUnmount(() => {
  // 销毁图表实例
  incomeExpenseChartInstance?.dispose()
  trendChartInstance?.dispose()
  expenseStructureChartInstance?.dispose()
  expenseTypeChartInstance?.dispose()
  incomeSourceChartInstance?.dispose()
  
  // 移除事件监听
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.asset-accounting-analysis {
  padding: 0;
}

.filter-section {
  margin-bottom: 20px;
  padding: 15px 20px;
  background: #f5f7fa;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
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
