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
          <el-button @click="resetDateRange">
            <el-icon><Refresh /></el-icon>重置
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
import { ref, onMounted, onBeforeUnmount, nextTick, onActivated } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

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

// 处理日期变化
const handleDateChange = (val) => {
  console.log('日期范围:', val)
}

// 刷新数据
const refreshData = () => {
  console.log('刷新数据, 时间范围:', dateRange.value)
  // 这里可以根据时间范围重新加载图表数据
}

// 重置时间范围
const resetDateRange = () => {
  dateRange.value = []
  refreshData()
}

// 初始化收入VS支出图表
const initIncomeExpenseChart = () => {
  if (!incomeExpenseChart.value) return
  
  incomeExpenseChartInstance = echarts.init(incomeExpenseChart.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
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
      data: ['1月', '2月', '3月', '4月', '5月', '6月']
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
        data: [8000, 8500, 9000, 8800, 9200, 9500],
        itemStyle: {
          color: '#67c23a'
        }
      },
      {
        name: '支出',
        type: 'bar',
        data: [6000, 7000, 6500, 7500, 6800, 7200],
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
  
  const option = {
    tooltip: {
      trigger: 'axis'
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
      data: ['1日', '5日', '10日', '15日', '20日', '25日', '30日']
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
        data: [3000, 1500, 2000, 1800, 2200, 1500, 2500],
        itemStyle: {
          color: '#67c23a'
        }
      },
      {
        name: '支出',
        type: 'line',
        data: [2000, 1800, 2200, 1500, 2000, 1800, 1700],
        itemStyle: {
          color: '#f56c6c'
        }
      },
      {
        name: '结余',
        type: 'line',
        data: [1000, -300, -200, 300, 200, -300, 800],
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
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: ¥{c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'center'
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
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: 2500, name: '饮食' },
          { value: 2000, name: '房租' },
          { value: 800, name: '日用品' },
          { value: 1200, name: '娱乐' },
          { value: 500, name: '其他' }
        ]
      }
    ]
  }
  
  expenseStructureChartInstance.setOption(option)
}

// 初始化支出类型图表
const initExpenseTypeChart = () => {
  if (!expenseTypeChart.value) return
  
  expenseTypeChartInstance = echarts.init(expenseTypeChart.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: ¥{c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'center'
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
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: 4500, name: '必要' },
          { value: 1500, name: '需要' },
          { value: 1000, name: '想要' }
        ]
      }
    ]
  }
  
  expenseTypeChartInstance.setOption(option)
}

// 初始化收入来源图表
const initIncomeSourceChart = () => {
  if (!incomeSourceChart.value) return
  
  incomeSourceChartInstance = echarts.init(incomeSourceChart.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: ¥{c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'center'
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
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: 8000, name: '工资' },
          { value: 1000, name: '兼职' },
          { value: 500, name: '理财' }
        ]
      }
    ]
  }
  
  incomeSourceChartInstance.setOption(option)
}

// 初始化所有图表
const initAllCharts = () => {
  initIncomeExpenseChart()
  initTrendChart()
  initExpenseStructureChart()
  initExpenseTypeChart()
  initIncomeSourceChart()
  isInitialized = true
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
