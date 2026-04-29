<template>
  <el-dialog
    v-model="dialogVisible"
    width="90%"
    :close-on-click-modal="false"
    :show-close="false"
    :lock-scroll="false"
    align-center
    class="fund-view-dialog"
    destroy-on-close
    :fullscreen="isFullscreen"
  >
    <!-- 标题栏 - 参照数据维护窗口风格 -->
    <template #header>
      <div class="view-header-bar">
        <div class="view-header-left">
          <div class="view-title-icon">
            <el-icon :size="18"><Coin /></el-icon>
          </div>
          <span class="view-header-title">基金详情查看 - {{ props.fundData.fundName || '未知基金' }}</span>
          <el-tag :type="getFundTypeTag()" size="small" effect="dark" style="margin-left: 10px;">{{ props.fundData.fundType || '未知' }}</el-tag>
          <el-tag type="info" size="small" effect="dark" style="margin-left: 6px;">{{ props.fundData.fundCode }}</el-tag>
        </div>
        <div class="view-header-right">
          <el-icon class="fullscreen-icon" @click="isFullscreen = !isFullscreen">
            <FullScreen v-if="!isFullscreen" />
            <Aim v-else />
          </el-icon>
          <el-icon class="close-icon" @click="dialogVisible = false"><Close /></el-icon>
        </div>
      </div>
    </template>

    <!-- 内容区域 -->
    <div class="view-content">
      <!-- 基本信息卡片 -->
      <div class="info-card">
        <div class="card-header">
          <el-icon><Document /></el-icon>
          <span>基本信息</span>
        </div>
        <div class="card-body">
          <div class="info-grid">
            <div class="info-item">
              <label>成立日期</label>
              <span class="value">{{ props.fundData.establishDate || '-' }}</span>
            </div>
            <div class="info-item">
              <label>资产规模</label>
              <span class="value highlight">{{ props.fundData.assetScale ? props.fundData.assetScale + ' 亿' : '-' }}</span>
            </div>
            <div class="info-item">
              <label>基金管理人</label>
              <span class="value">{{ props.fundData.fundManager || '-' }}</span>
            </div>
            <div class="info-item">
              <label>基金托管者</label>
              <span class="value">{{ props.fundData.fundCustodian || '-' }}</span>
            </div>
            <div class="info-item">
              <label>基金经理</label>
              <span class="value">{{ props.fundData.fundManagerName || '-' }}</span>
            </div>
            <div class="info-item">
              <label>任职日期</label>
              <span class="value">{{ props.fundData.managerAppointDate || '-' }}</span>
            </div>
            <div class="info-item">
              <label>运作方式</label>
              <span class="value">{{ props.fundData.operationMode || '-' }}</span>
            </div>
            <div class="info-item">
              <label>封闭期</label>
              <span class="value">{{ props.fundData.closedPeriod ? props.fundData.closedPeriod + ' 天' : '-' }}</span>
            </div>
          </div>
          <div class="info-full" v-if="props.fundData.fundCompanyDesc">
            <label>基金公司描述</label>
            <p class="desc-text">{{ props.fundData.fundCompanyDesc }}</p>
          </div>
          <div class="info-full" v-if="props.fundData.managerDesc">
            <label>经理描述</label>
            <p class="desc-text">{{ props.fundData.managerDesc }}</p>
          </div>
        </div>
      </div>

      <!-- 费率信息卡片 -->
      <div class="info-card">
        <div class="card-header">
          <el-icon><Discount /></el-icon>
          <span>费率信息</span>
        </div>
        <div class="card-body">
          <div class="fee-grid">
            <div class="fee-item">
              <div class="fee-label">申购费率</div>
              <div class="fee-value">{{ props.fundData.purchaseRate ? props.fundData.purchaseRate + '%' : '0%' }}</div>
            </div>
            <div class="fee-item">
              <div class="fee-label">赎回费率</div>
              <div class="fee-value">{{ props.fundData.redemptionRate ? props.fundData.redemptionRate + '%' : '0%' }}</div>
            </div>
            <div class="fee-item">
              <div class="fee-label">管理费</div>
              <div class="fee-value">{{ props.fundData.managementFee ? props.fundData.managementFee + '%' : '0%' }}</div>
            </div>
            <div class="fee-item">
              <div class="fee-label">托管费</div>
              <div class="fee-value">{{ props.fundData.custodianFee ? props.fundData.custodianFee + '%' : '0%' }}</div>
            </div>
            <div class="fee-item">
              <div class="fee-label">销售服务费</div>
              <div class="fee-value">{{ props.fundData.salesServiceFee ? props.fundData.salesServiceFee + '%' : '0%' }}</div>
            </div>
          </div>
          <div class="info-full" v-if="props.fundData.tradeRule">
            <label>交易规则</label>
            <p class="desc-text">{{ props.fundData.tradeRule }}</p>
          </div>
        </div>
      </div>

      <!-- 持仓信息卡片 -->
      <div class="info-card">
        <div class="card-header">
          <el-icon><Wallet /></el-icon>
          <span>持仓信息</span>
        </div>
        <div class="card-body">
          <div class="position-grid">
            <div class="position-item">
              <div class="position-label">持有份额</div>
              <div class="position-value">{{ formatNumber(props.fundData.holdShares) }}</div>
            </div>
            <div class="position-item">
              <div class="position-label">可用份额</div>
              <div class="position-value">{{ formatNumber(props.fundData.availableShares) }}</div>
            </div>
            <div class="position-item">
              <div class="position-label">冻结份额</div>
              <div class="position-value">{{ formatNumber(props.fundData.frozenShares) }}</div>
            </div>
            <div class="position-item">
              <div class="position-label">持仓成本</div>
              <div class="position-value highlight">{{ formatCurrency(props.fundData.positionCost) }}</div>
            </div>
            <div class="position-item">
              <div class="position-label">持仓市值</div>
              <div class="position-value highlight">{{ formatCurrency(props.fundData.positionValue) }}</div>
            </div>
            <div class="position-item">
              <div class="position-label">持仓盈亏</div>
              <div class="position-value" :class="getProfitClass(props.fundData.positionProfit)">{{ formatCurrency(props.fundData.positionProfit) }}</div>
            </div>
            <div class="position-item">
              <div class="position-label">收益率</div>
              <div class="position-value" :class="getProfitClass(props.fundData.positionProfitRate)">
                {{ props.fundData.positionProfitRate ? props.fundData.positionProfitRate.toFixed(2) + '%' : '0%' }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 净值走势卡片 -->
      <div class="info-card" v-if="props.fundData.navList && props.fundData.navList.length > 0">
        <div class="card-header">
          <el-icon><TrendCharts /></el-icon>
          <span>净值走势</span>
        </div>
        <div class="card-body">
          <!-- 时间范围选择器 -->
          <div class="time-range-selector">
            <div class="time-range-tabs">
              <div
                v-for="option in timeRangeOptions"
                :key="option.value"
                :class="['time-range-tab', { active: activeTimeRange === option.value }]"
                @click="handleTimeRangeChange(option.value)"
              >
                {{ option.label }}
              </div>
            </div>
          </div>
          
          <!-- ECharts 图表 -->
          <div ref="chartRef" class="nav-chart-container"></div>
          
          <!-- 净值数据表格 -->
          <div class="nav-table-wrapper">
            <el-table 
              :data="getPaginatedNavData()" 
              border 
              stripe 
              size="small" 
              height="400px"
              style="width: 100%"
            >
              <el-table-column prop="navDate" label="净值日期" width="120" />
              <el-table-column prop="unitNav" label="单位净值" width="120" align="right" />
              <el-table-column prop="accumulatedNav" label="累计净值" width="120" align="right" />
              <el-table-column prop="dailyChangeRate" label="日涨跌幅(%)" width="130" align="right">
                <template #default="{ row }">
                  <span :class="row.dailyChangeRate >= 0 ? 'profit-text' : 'loss-text'">
                    {{ row.dailyChangeRate >= 0 ? '+' : '' }}{{ row.dailyChangeRate }}%
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="estimateNav" label="估值" width="120" align="right" />
            </el-table>
            
            <!-- 分页组件 -->
            <div class="nav-table-pagination">
              <el-pagination
                v-model:current-page="navTablePage"
                v-model:page-size="navTablePageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="navTableTotal"
                layout="total, sizes, prev, pager, next, jumper"
                @current-change="handleNavTablePageChange"
                @size-change="handleNavTableSizeChange"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- 交易记录卡片 -->
      <div class="info-card" v-if="props.fundData.tradeList && props.fundData.tradeList.length > 0">
        <div class="card-header">
          <el-icon><List /></el-icon>
          <span>交易记录</span>
        </div>
        <div class="card-body">
          <el-table :data="props.fundData.tradeList" border stripe size="small" max-height="300px">
            <el-table-column prop="tradeDate" label="交易日期" width="120" />
            <el-table-column prop="tradeType" label="交易类型" width="100">
              <template #default="{ row }">
                <el-tag :type="row.tradeType === '申购' ? 'success' : 'danger'" size="small">{{ row.tradeType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="tradeShares" label="交易份额" width="120" align="right" />
            <el-table-column prop="tradeAmount" label="交易金额" width="120" align="right">
              <template #default="{ row }">¥{{ row.tradeAmount }}</template>
            </el-table-column>
            <el-table-column prop="tradeNav" label="交易净值" width="100" align="right" />
            <el-table-column prop="tradeFee" label="交易费用" width="100" align="right">
              <template #default="{ row }">¥{{ row.tradeFee }}</template>
            </el-table-column>
            <el-table-column prop="confirmDate" label="确认日期" width="120" />
          </el-table>
        </div>
      </div>

      <!-- 分红记录卡片 -->
      <div class="info-card" v-if="props.fundData.dividendList && props.fundData.dividendList.length > 0">
        <div class="card-header">
          <el-icon><Money /></el-icon>
          <span>分红记录</span>
        </div>
        <div class="card-body">
          <el-table :data="props.fundData.dividendList" border stripe size="small" max-height="300px">
            <el-table-column prop="dividendDate" label="分红时间" width="150" />
            <el-table-column prop="arrivalDate" label="到账时间" width="150" />
            <el-table-column prop="dividendMethod" label="分红方式" width="120">
              <template #default="{ row }">
                <el-tag type="warning" size="small">{{ row.dividendMethod }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="dividendAmount" label="分红金额" width="150" align="right">
              <template #default="{ row }">
                <span class="profit-text">¥{{ row.dividendAmount }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 风险指标卡片 -->
      <div class="info-card" v-if="props.fundData.riskList && props.fundData.riskList.length > 0">
        <div class="card-header">
          <el-icon><DataAnalysis /></el-icon>
          <span>风险指标</span>
        </div>
        <div class="card-body">
          <el-table :data="props.fundData.riskList" border stripe size="small" max-height="300px">
            <el-table-column prop="timePeriod" label="时间周期" width="120" />
            <el-table-column prop="maxDrawdown" label="最大回撤(%)" width="130" align="right">
              <template #default="{ row }">
                <span class="loss-text">{{ row.maxDrawdown }}%</span>
              </template>
            </el-table-column>
            <el-table-column prop="drawdownRecoveryDays" label="回撤修复天数" width="130" align="right" />
            <el-table-column prop="annualizedReturn" label="年化收益率(%)" width="140" align="right">
              <template #default="{ row }">
                <span :class="row.annualizedReturn >= 0 ? 'profit-text' : 'loss-text'">
                  {{ row.annualizedReturn >= 0 ? '+' : '' }}{{ row.annualizedReturn }}%
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="sharpeRatio" label="夏普比率" width="110" align="right" />
            <el-table-column prop="volatility" label="波动率(%)" width="110" align="right" />
          </el-table>
        </div>
      </div>

      <!-- 持仓明细卡片 -->
      <div class="info-card" v-if="props.fundData.holdingList && props.fundData.holdingList.length > 0">
        <div class="card-header">
          <el-icon><Grid /></el-icon>
          <span>持仓明细</span>
        </div>
        <div class="card-body">
          <el-table :data="props.fundData.holdingList" border stripe size="small" max-height="300px">
            <el-table-column prop="holdingDate" label="持仓日期" width="110" />
            <el-table-column prop="holdingType" label="持仓类型" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ row.holdingType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="holdingCode" label="持仓代码" width="100" />
            <el-table-column prop="holdingName" label="持仓名称" min-width="150" show-overflow-tooltip />
            <el-table-column prop="holdingQuantity" label="持仓数量" width="120" align="right" />
            <el-table-column prop="holdingValue" label="持仓市值" width="130" align="right">
              <template #default="{ row }">¥{{ formatNumber(row.holdingValue) }}</template>
            </el-table-column>
            <el-table-column prop="navRatio" label="占净值比例(%)" width="130" align="right">
              <template #default="{ row }">{{ row.navRatio }}%</template>
            </el-table-column>
            <el-table-column prop="industryClass" label="行业分类" width="100" />
            <el-table-column prop="dataSource" label="数据来源" width="100" />
          </el-table>
        </div>
      </div>
    </div>

    <!-- 底部按钮 -->
    <template #footer>
      <el-button @click="dialogVisible = false" class="close-button">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch, nextTick, onBeforeUnmount } from 'vue'
import { Coin, Close, FullScreen, Aim, Document, Discount, Wallet, TrendCharts, List, Money, DataAnalysis, Grid } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const props = defineProps({
  visible: { type: Boolean, default: false },
  fundData: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:visible'])

const isFullscreen = ref(false)
const chartInstance = ref(null)
const chartRef = ref(null)
const activeTimeRange = ref('1month')

// 分页相关状态
const navTablePage = ref(1)
const navTablePageSize = ref(10)
const navTableTotal = ref(0)

// 时间范围选项
const timeRangeOptions = [
  { label: '近1月', value: '1month' },
  { label: '近3月', value: '3month' },
  { label: '近6月', value: '6month' },
  { label: '近1年', value: '1year' },
  { label: '近3年', value: '3year' },
  { label: '近5年', value: '5year' },
  { label: '近10年', value: '10year' },
  { label: '全部', value: 'all' }
]

const dialogVisible = computed({
  get() { return props.visible },
  set(val) { emit('update:visible', val) }
})

// 根据时间范围过滤数据
const getFilteredNavData = () => {
  if (!props.fundData.navList || props.fundData.navList.length === 0) {
    return []
  }

  const now = new Date()
  let startDate = new Date()

  switch (activeTimeRange.value) {
    case '1month':
      startDate.setMonth(now.getMonth() - 1)
      break
    case '3month':
      startDate.setMonth(now.getMonth() - 3)
      break
    case '6month':
      startDate.setMonth(now.getMonth() - 6)
      break
    case '1year':
      startDate.setFullYear(now.getFullYear() - 1)
      break
    case '3year':
      startDate.setFullYear(now.getFullYear() - 3)
      break
    case '5year':
      startDate.setFullYear(now.getFullYear() - 5)
      break
    case '10year':
      startDate.setFullYear(now.getFullYear() - 10)
      break
    case 'all':
      return props.fundData.navList
    default:
      startDate.setMonth(now.getMonth() - 1)
  }

  return props.fundData.navList.filter(item => {
    const itemDate = new Date(item.navDate)
    return itemDate >= startDate && itemDate <= now
  })
}

// 获取分页后的净值数据
const getPaginatedNavData = () => {
  const filteredData = getFilteredNavData()
  navTableTotal.value = filteredData.length
  
  const start = (navTablePage.value - 1) * navTablePageSize.value
  const end = start + navTablePageSize.value
  
  return filteredData.slice(start, end)
}

// 处理分页变化
const handleNavTablePageChange = (page) => {
  navTablePage.value = page
}

// 处理每页条数变化
const handleNavTableSizeChange = (size) => {
  navTablePageSize.value = size
  navTablePage.value = 1
}

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return

  if (chartInstance.value) {
    chartInstance.value.dispose()
  }

  chartInstance.value = echarts.init(chartRef.value)

  const filteredData = getFilteredNavData()
  
  // 准备图表数据
  const dates = filteredData.map(item => item.navDate)
  const unitNavs = filteredData.map(item => item.unitNav)
  const accumulatedNavs = filteredData.map(item => item.accumulatedNav)
  
  // 计算业绩走势数据（第一天为0，后一天为前一天加涨跌幅）
  const performanceData = []
  if (filteredData.length > 0) {
    performanceData.push(0) // 第一天为0
    for (let i = 1; i < filteredData.length; i++) {
      const prevNav = filteredData[i - 1].unitNav
      const currNav = filteredData[i].unitNav
      const changeRate = prevNav > 0 ? ((currNav - prevNav) / prevNav) * 100 : 0
      performanceData.push(performanceData[i - 1] + changeRate)
    }
  }

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.98)',
      borderColor: '#dcdfe6',
      borderWidth: 1,
      padding: [14, 18],
      textStyle: {
        color: '#303133',
        fontSize: 14
      },
      axisPointer: {
        type: 'cross',
        crossStyle: {
          color: '#999'
        },
        lineStyle: {
          color: '#1e3c72',
          width: 1,
          type: 'dashed'
        }
      },
      formatter: (params) => {
        if (!params || params.length === 0) return ''
        
        let result = `<div style="font-weight: 600; margin-bottom: 10px; color: #303133; font-size: 14px; border-bottom: 1px solid #ebeef5; padding-bottom: 8px;">📅 ${params[0].axisValue}</div>`
        params.forEach(param => {
          if (param.value !== undefined && param.value !== null) {
            let color = '#ff6b6b'
            if (param.seriesName === '累计净值') {
              color = '#4ecdc4'
            } else if (param.seriesName === '业绩走势') {
              color = '#ffa726'
            }
            
            const valueStr = param.seriesName === '业绩走势' 
              ? `${param.value >= 0 ? '+' : ''}${param.value.toFixed(2)}%`
              : param.value.toFixed(4)
            
            result += `
              <div style="display: flex; align-items: center; margin: 8px 0; padding: 4px 0;">
                <span style="display: inline-block; width: 12px; height: 12px; border-radius: 50%; background: ${color}; margin-right: 10px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);"></span>
                <span style="color: #606266; font-size: 14px; font-weight: 500;">${param.seriesName}：</span>
                <span style="color: ${color}; font-size: 15px; font-weight: 600; margin-left: 4px;">${valueStr}</span>
              </div>
            `
          }
        })
        return result
      }
    },
    legend: {
      data: ['单位净值', '累计净值', '业绩走势'],
      top: 10,
      right: 20,
      textStyle: {
        color: '#606266',
        fontSize: 13
      },
      itemWidth: 20,
      itemHeight: 10,
      itemGap: 20
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: 60,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLine: {
        lineStyle: {
          color: '#e8ecf1'
        }
      },
      axisLabel: {
        color: '#909399',
        fontSize: 11,
        rotate: 45
      },
      axisTick: {
        show: false
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        show: false
      },
      axisLabel: {
        color: '#909399',
        fontSize: 12,
        formatter: (value) => value.toFixed(2)
      },
      splitLine: {
        lineStyle: {
          color: '#f0f2f5',
          type: 'dashed'
        }
      },
      axisTick: {
        show: false
      }
    },
    series: [
      {
        name: '单位净值',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        showSymbol: true,
        hoverAnimation: true,
        lineStyle: {
          width: 3,
          color: '#ff6b6b',
          shadowColor: 'rgba(255, 107, 107, 0.4)',
          shadowBlur: 8,
          shadowOffsetY: 3
        },
        itemStyle: {
          color: '#ff6b6b',
          borderWidth: 2,
          borderColor: '#fff'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255, 107, 107, 0.3)' },
            { offset: 0.5, color: 'rgba(255, 107, 107, 0.15)' },
            { offset: 1, color: 'rgba(255, 107, 107, 0.02)' }
          ])
        },
        emphasis: {
          focus: 'series',
          itemStyle: {
            color: '#ff6b6b',
            borderWidth: 3,
            borderColor: '#fff',
            shadowBlur: 15,
            shadowColor: 'rgba(255, 107, 107, 0.6)'
          },
          lineStyle: {
            width: 4
          }
        },
        data: unitNavs
      },
      {
        name: '累计净值',
        type: 'line',
        smooth: true,
        symbol: 'diamond',
        symbolSize: 8,
        showSymbol: true,
        hoverAnimation: true,
        lineStyle: {
          width: 3,
          color: '#4ecdc4',
          type: 'dashed',
          shadowColor: 'rgba(78, 205, 196, 0.4)',
          shadowBlur: 8,
          shadowOffsetY: 3
        },
        itemStyle: {
          color: '#4ecdc4',
          borderWidth: 2,
          borderColor: '#fff'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(78, 205, 196, 0.25)' },
            { offset: 0.5, color: 'rgba(78, 205, 196, 0.12)' },
            { offset: 1, color: 'rgba(78, 205, 196, 0.02)' }
          ])
        },
        emphasis: {
          focus: 'series',
          itemStyle: {
            color: '#4ecdc4',
            borderWidth: 3,
            borderColor: '#fff',
            shadowBlur: 15,
            shadowColor: 'rgba(78, 205, 196, 0.6)'
          },
          lineStyle: {
            width: 4
          }
        },
        data: accumulatedNavs
      },
      {
        name: '业绩走势',
        type: 'line',
        smooth: true,
        symbol: 'triangle',
        symbolSize: 8,
        showSymbol: true,
        hoverAnimation: true,
        lineStyle: {
          width: 3,
          color: '#ffa726',
          type: 'dotted',
          shadowColor: 'rgba(255, 167, 38, 0.4)',
          shadowBlur: 8,
          shadowOffsetY: 3
        },
        itemStyle: {
          color: '#ffa726',
          borderWidth: 2,
          borderColor: '#fff'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255, 167, 38, 0.2)' },
            { offset: 0.5, color: 'rgba(255, 167, 38, 0.1)' },
            { offset: 1, color: 'rgba(255, 167, 38, 0.02)' }
          ])
        },
        emphasis: {
          focus: 'series',
          itemStyle: {
            color: '#ffa726',
            borderWidth: 3,
            borderColor: '#fff',
            shadowBlur: 15,
            shadowColor: 'rgba(255, 167, 38, 0.6)'
          },
          lineStyle: {
            width: 4
          }
        },
        data: performanceData
      }
    ]
  }

  chartInstance.value.setOption(option)
}

// 切换时间范围
const handleTimeRangeChange = (range) => {
  activeTimeRange.value = range
  navTablePage.value = 1 // 重置分页
  nextTick(() => {
    initChart()
  })
}

// 监听窗口大小变化
const handleResize = () => {
  if (chartInstance.value) {
    chartInstance.value.resize()
  }
}

// 监听数据变化
watch(
  () => props.fundData.navList,
  () => {
    nextTick(() => {
      initChart()
    })
  },
  { deep: true }
)

// 监听对话框显示状态
watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      nextTick(() => {
        initChart()
        window.addEventListener('resize', handleResize)
      })
    } else {
      window.removeEventListener('resize', handleResize)
      if (chartInstance.value) {
        chartInstance.value.dispose()
        chartInstance.value = null
      }
    }
  }
)

// 组件卸载前清理
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance.value) {
    chartInstance.value.dispose()
    chartInstance.value = null
  }
})

const getFundTypeTag = () => {
  const typeMap = {
    '股票型': 'danger',
    '债券型': 'success',
    '货币型': 'warning',
    '混合型': 'info',
    '指数型': ''
  }
  return typeMap[props.fundData.fundType] || 'info'
}

const formatNumber = (num) => {
  if (!num && num !== 0) return '-'
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const formatCurrency = (num) => {
  if (!num && num !== 0) return '-'
  return '¥' + num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const getProfitClass = (value) => {
  if (!value) return ''
  return value >= 0 ? 'profit-text' : 'loss-text'
}
</script>

<style scoped>
.fund-view-dialog :deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.25);
}

.fund-view-dialog :deep(.el-dialog__header) {
  padding: 0;
  margin: 0;
}

.fund-view-dialog :deep(.el-dialog__body) {
  padding: 0;
  max-height: 70vh;
  overflow-y: auto;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8eef5 100%);
}

.fund-view-dialog :deep(.el-dialog__body::-webkit-scrollbar) {
  width: 6px;
}

.fund-view-dialog :deep(.el-dialog__body::-webkit-scrollbar-track) {
  background: #f1f1f1;
}

.fund-view-dialog :deep(.el-dialog__body::-webkit-scrollbar-thumb) {
  background: linear-gradient(180deg, #c0c4cc, #909399);
  border-radius: 3px;
}

.fund-view-dialog :deep(.el-dialog__footer) {
  padding: 14px 24px;
  background: #fff;
  border-top: 1px solid #e8ecf1;
  box-shadow: 0 -1px 3px rgba(0, 0, 0, 0.05);
}

/* ====== 标题栏 - 参照数据维护窗口 ====== */
.view-header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  padding: 16px 24px;
  box-shadow: 0 2px 12px rgba(30, 60, 114, 0.3);
}

.view-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.view-title-icon {
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  backdrop-filter: blur(10px);
}

.view-header-title {
  font-size: 17px;
  font-weight: 600;
  color: #fff;
  letter-spacing: 0.3px;
}

.view-header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.fullscreen-icon,
.close-icon {
  font-size: 22px;
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 6px;
  border-radius: 6px;
}

.fullscreen-icon:hover,
.close-icon:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.2);
  transform: scale(1.1);
}

/* ====== 内容区域 ====== */
.view-content {
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ====== 信息卡片 ====== */
.info-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.info-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  background: linear-gradient(135deg, #fafbfc 0%, #f5f7fa 100%);
  border-bottom: 1px solid #e8ecf1;
  font-size: 15px;
  font-weight: 700;
  color: #2c3e50;
}

.card-header :deep(.el-icon) {
  font-size: 18px;
  color: #1e3c72;
}

.card-body {
  padding: 18px 20px;
}

/* ====== 信息网格 ====== */
.info-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

/* ====== 时间范围选择器 ====== */
.time-range-selector {
  margin-bottom: 20px;
}

.time-range-tabs {
  display: flex;
  gap: 8px;
  padding: 4px;
  background: #f5f7fa;
  border-radius: 8px;
  width: fit-content;
}

.time-range-tab {
  padding: 8px 16px;
  font-size: 13px;
  color: #606266;
  background: transparent;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  user-select: none;
  font-weight: 500;
}

.time-range-tab:hover {
  color: #1e3c72;
  background: rgba(30, 60, 114, 0.08);
}

.time-range-tab.active {
  color: #fff;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  box-shadow: 0 2px 8px rgba(30, 60, 114, 0.3);
}

/* ====== 图表容器 ====== */
.nav-chart-container {
  width: 100%;
  height: 400px;
  margin-bottom: 20px;
  border-radius: 8px;
  background: #fff;
  padding: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

/* ====== 净值表格包装器 ====== */
.nav-table-wrapper {
  margin-top: 16px;
}

/* ====== 净值表格分页 ====== */
.nav-table-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 10px 0;
  background: #fafbfc;
  border-radius: 8px;
}

.info-item label {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

.info-item .value {
  font-size: 15px;
  color: #303133;
  font-weight: 600;
}

.info-item .value.highlight {
  color: #1e3c72;
  font-size: 17px;
}

.info-full {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f2f5;
}

.info-full label {
  display: block;
  font-size: 13px;
  color: #909399;
  font-weight: 600;
  margin-bottom: 8px;
}

.desc-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  margin: 0;
  padding: 10px 14px;
  background: #fafbfc;
  border-radius: 6px;
  border-left: 3px solid #1e3c72;
}

/* ====== 费率网格 ====== */
.fee-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
}

.fee-item {
  text-align: center;
  padding: 14px 10px;
  background: linear-gradient(135deg, #fafbfc 0%, #f5f7fa 100%);
  border-radius: 8px;
  border: 1px solid #e8ecf1;
  transition: all 0.3s ease;
}

.fee-item:hover {
  border-color: #1e3c72;
  box-shadow: 0 2px 8px rgba(30, 60, 114, 0.1);
}

.fee-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}

.fee-value {
  font-size: 18px;
  font-weight: 700;
  color: #1e3c72;
}

/* ====== 持仓网格 ====== */
.position-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

.position-item {
  text-align: center;
  padding: 16px 10px;
  background: linear-gradient(135deg, #fafbfc 0%, #f5f7fa 100%);
  border-radius: 8px;
  border: 1px solid #e8ecf1;
  transition: all 0.3s ease;
}

.position-item:hover {
  border-color: #1e3c72;
  box-shadow: 0 2px 8px rgba(30, 60, 114, 0.1);
}

.position-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.position-value {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
}

.position-value.highlight {
  color: #1e3c72;
  font-size: 18px;
}

/* ====== 涨跌颜色 ====== */
.profit-text {
  color: #f56c6c;
  font-weight: 600;
}

.loss-text {
  color: #67c23a;
  font-weight: 600;
}

/* ====== 表格样式 ====== */
.card-body :deep(.el-table) {
  border-radius: 6px;
  overflow: hidden;
}

.card-body :deep(.el-table th) {
  background: #fafbfc;
  color: #606266;
  font-weight: 600;
}

.card-body :deep(.el-table td) {
  padding: 10px 0;
}

/* ====== 关闭按钮 ====== */
.close-button {
  border-radius: 6px !important;
  font-weight: 600 !important;
  padding: 9px 28px !important;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%) !important;
  border: none !important;
  color: #fff !important;
  transition: all 0.3s ease !important;
}

.close-button:hover {
  transform: translateY(-1px) !important;
  box-shadow: 0 4px 12px rgba(30, 60, 114, 0.4) !important;
}
</style>
