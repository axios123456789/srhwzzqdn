<template>
  <div class="review-analysis-div">
    <div class="review-time-filter">
      <el-radio-group v-model="reviewReportRange" @change="fetchReviewReportData">
        <el-radio-button :label="7">近7天</el-radio-button>
        <el-radio-button :label="30">近30天</el-radio-button>
        <el-radio-button :label="90">近90天</el-radio-button>
        <el-radio-button :label="0">全部</el-radio-button>
      </el-radio-group>
      <el-button type="primary" plain :loading="aiReportLoading" @click="aiGenerateReport" style="margin-left: 16px;">
        <span v-if="!aiReportLoading">🤖 生成 AI 分析报告</span>
        <span v-else>AI 生成中...</span>
      </el-button>
    </div>
    <!-- KPI卡片 -->
    <el-row :gutter="12" class="review-kpi-row">
      <el-col :span="6">
        <div class="review-kpi-card"><div class="kpi-label">总复盘天数</div><div class="kpi-value">{{ reviewReportData.reviewKpi?.totalReviewDays || 0 }}</div></div>
      </el-col>
      <el-col :span="6">
        <div class="review-kpi-card"><div class="kpi-label">盈利/亏损天数</div><div class="kpi-value"><span style="color:#F56C6C">{{ reviewReportData.reviewKpi?.profitDays || 0 }}</span> / <span style="color:#67C23A">{{ reviewReportData.reviewKpi?.lossDays || 0 }}</span></div></div>
      </el-col>
      <el-col :span="6">
        <div class="review-kpi-card"><div class="kpi-label">最高连续盈利</div><div class="kpi-value">{{ reviewReportData.reviewKpi?.maxContinuousProfitDays || 0 }} 天</div></div>
      </el-col>
      <el-col :span="6">
        <div class="review-kpi-card"><div class="kpi-label">平均每日盈亏</div><div class="kpi-value" :style="{color: (reviewReportData.reviewKpi?.avgDailyProfitPct || 0) > 0 ? '#F56C6C' : '#67C23A'}">{{ (reviewReportData.reviewKpi?.avgDailyProfitPct || 0).toFixed(2) }}%</div></div>
      </el-col>
    </el-row>
    <el-row :gutter="12" class="review-kpi-row">
      <el-col :span="6">
        <div class="review-kpi-card"><div class="kpi-label">总交易笔数</div><div class="kpi-value">{{ reviewReportData.tradeKpi?.totalTradeCount || 0 }}</div></div>
      </el-col>
      <el-col :span="6">
        <div class="review-kpi-card"><div class="kpi-label">胜率</div><div class="kpi-value" :style="{color: (reviewReportData.tradeKpi?.winRate || 0) > 50 ? '#F56C6C' : '#E6A23C'}">{{ (reviewReportData.tradeKpi?.winRate || 0).toFixed(1) }}%</div></div>
      </el-col>
      <el-col :span="6">
        <div class="review-kpi-card"><div class="kpi-label">平均盈亏</div><div class="kpi-value" :style="{color: (reviewReportData.tradeKpi?.avgProfitPct || 0) > 0 ? '#F56C6C' : '#67C23A'}">{{ (reviewReportData.tradeKpi?.avgProfitPct || 0).toFixed(2) }}%</div></div>
      </el-col>
      <el-col :span="6">
        <div class="review-kpi-card"><div class="kpi-label">最大盈/亏</div><div class="kpi-value"><span style="color:#F56C6C">{{ (reviewReportData.tradeKpi?.maxProfitPct || 0).toFixed(1) }}</span> / <span style="color:#67C23A">{{ (reviewReportData.tradeKpi?.maxLossPct || 0).toFixed(1) }}</span></div></div>
      </el-col>
    </el-row>
    <!-- 图表网格 -->
    <el-row :gutter="12">
      <el-col :span="12"><div class="review-chart-card"><div class="chart-title">情绪温度趋势</div><div ref="emotionChartRef" class="chart-box"></div></div></el-col>
      <el-col :span="12"><div class="review-chart-card"><div class="chart-title">市场状态分布</div><div ref="marketStatusChartRef" class="chart-box"></div></div></el-col>
      <el-col :span="12"><div class="review-chart-card"><div class="chart-title">涨停/跌停/连板趋势</div><div ref="limitChartRef" class="chart-box"></div></div></el-col>
      <el-col :span="12"><div class="review-chart-card"><div class="chart-title">主线板块频次排行</div><div ref="sectorChartRef" class="chart-box"></div></div></el-col>
      <el-col :span="12"><div class="review-chart-card"><div class="chart-title">适配体系分布</div><div ref="adaptSystemChartRef" class="chart-box"></div></div></el-col>
      <el-col :span="12"><div class="review-chart-card"><div class="chart-title">操作自评趋势</div><div ref="selfRatingChartRef" class="chart-box"></div></div></el-col>
      <el-col :span="12"><div class="review-chart-card"><div class="chart-title">当日盈亏趋势</div><div ref="dailyProfitChartRef" class="chart-box"></div></div></el-col>
      <el-col :span="12"><div class="review-chart-card"><div class="chart-title">北向资金趋势</div><div ref="northChartRef" class="chart-box"></div></div></el-col>
      <el-col :span="12"><div class="review-chart-card"><div class="chart-title">心理状态分布</div><div ref="psychologyChartRef" class="chart-box"></div></div></el-col>
      <el-col :span="12"><div class="review-chart-card"><div class="chart-title">心理状态与盈亏关系</div><div ref="psychologyProfitChartRef" class="chart-box"></div></div></el-col>
      <el-col :span="12"><div class="review-chart-card"><div class="chart-title">交易时段分布</div><div ref="timeSlotChartRef" class="chart-box"></div></div></el-col>
      <el-col :span="12"><div class="review-chart-card"><div class="chart-title">买卖方向统计</div><div ref="directionChartRef" class="chart-box"></div></div></el-col>
      <el-col :span="12"><div class="review-chart-card"><div class="chart-title">执行评分趋势</div><div ref="executeRatingChartRef" class="chart-box"></div></div></el-col>
      <el-col :span="12"><div class="review-chart-card"><div class="chart-title">是否符合计划统计</div><div ref="planMatchChartRef" class="chart-box"></div></div></el-col>
      <el-col :span="12"><div class="review-chart-card"><div class="chart-title">个股交易频次Top10</div><div ref="stockChartRef" class="chart-box"></div></div></el-col>
      <el-col :span="12"><div class="review-chart-card"><div class="chart-title">每日盈亏vs交易笔数</div><div ref="profitVsCountChartRef" class="chart-box"></div></div></el-col>
      <el-col :span="24"><div class="review-chart-card"><div class="chart-title">情绪温度vs次日交易胜率</div><div ref="emotionVsWinChartRef" class="chart-box"></div></div></el-col>
    </el-row>
  </div>

  <!-- AI分析报告弹窗 -->
  <el-dialog v-model="aiReportDialogVisible" title="🤖 AI 交易诊断报告" width="70%" class="custom-dialog" :close-on-click-modal="false">
    <div class="ai-report-meta" v-if="aiReportContent">
      <span>📊 数据范围：最近 {{ reviewReportRange }} 天</span>
      <span>🕐 生成时间：{{ aiReportGenerateTime }}</span>
    </div>
    <div v-if="aiReportContent" class="ai-report-content" v-html="renderMarkdown(aiReportContent)"></div>
    <el-empty v-else description="暂无报告内容" />
    <template #footer>
      <el-button @click="aiReportDialogVisible = false">关闭</el-button>
      <el-button type="primary" @click="copyReport">复制报告</el-button>
      <el-button type="success" @click="downloadReport"><el-icon><Download /></el-icon> 下载Markdown</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { GetKeyAndValueByType } from "@/api/sysDict"
import { GetReviewReport, AiGenerateReviewReport } from "@/api/trialExecutionArea/reviewAnalysis"
import * as echarts from 'echarts'
import { getDisplayText } from "@/utils/common"

// ==================== 数据字典 ====================
const loadDict = async (tableName, optionsRef) => {
  const result = await GetKeyAndValueByType(tableName)
  optionsRef.value = result.data || []
}

const reviewEmotionTempOptions = ref([])
const reviewMarketStatusOptions = ref([])
const reviewSectorOptions = ref([])
const reviewAdaptSystemOptions = ref([])
const reviewSelfRatingOptions = ref([])
const tradePsychologyOptions = ref([])
const tradeTimeSlotOptions = ref([])
const tradeDirectionOptions = ref([])
const tradeExecuteRatingOptions = ref([])
const tradePlanMatchOptions = ref([])

// 情绪温度标签类型
const getEmotionTagType = (val) => {
  const map = { 1: 'info', 2: 'primary', 3: 'warning', 4: 'danger', 5: 'danger' }
  return map[val] || 'info'
}

// ==================== 复盘分析管理 ====================
const reviewReportRange = ref(30)
const reviewReportData = ref({})
// 图表ref
const emotionChartRef = ref(null), marketStatusChartRef = ref(null), limitChartRef = ref(null), sectorChartRef = ref(null)
const adaptSystemChartRef = ref(null), selfRatingChartRef = ref(null), dailyProfitChartRef = ref(null), northChartRef = ref(null)
const psychologyChartRef = ref(null), psychologyProfitChartRef = ref(null), timeSlotChartRef = ref(null), directionChartRef = ref(null)
const executeRatingChartRef = ref(null), planMatchChartRef = ref(null), stockChartRef = ref(null), profitVsCountChartRef = ref(null)
const emotionVsWinChartRef = ref(null)

const reviewReportLoading = ref(false)
const fetchReviewReportData = async () => {
  const dto = {}
  if (reviewReportRange.value > 0) {
    const end = new Date()
    const start = new Date(end.getTime() - reviewReportRange.value * 24 * 3600 * 1000)
    dto.startTime = start.toISOString().slice(0, 10)
    dto.endTime = end.toISOString().slice(0, 10)
  }
  reviewReportLoading.value = true
  try {
    const result = await GetReviewReport(dto)
    if (result.code === 200) {
      reviewReportData.value = result.data || {}
      nextTick(() => renderReviewCharts())
    }
  } catch (e) { ElMessage.error("查询复盘分析失败") }
  finally { reviewReportLoading.value = false }
}

// AI分析报告
const aiReportLoading = ref(false)
const aiReportDialogVisible = ref(false)
const aiReportContent = ref('')
const aiReportGenerateTime = ref('')
const aiGenerateReport = async () => {
  aiReportLoading.value = true
  try {
    const dto = {}
    if (reviewReportRange.value > 0) {
      const end = new Date()
      const start = new Date(end.getTime() - reviewReportRange.value * 24 * 3600 * 1000)
      dto.startTime = start.toISOString().slice(0, 10)
      dto.endTime = end.toISOString().slice(0, 10)
    }
    const result = await AiGenerateReviewReport(dto)
    if (result.code === 200 && result.data?.report) {
      aiReportContent.value = result.data.report
      aiReportGenerateTime.value = new Date().toLocaleString('zh-CN')
      aiReportDialogVisible.value = true
      ElMessage.success("AI分析报告已生成")
    } else {
      ElMessage.error(result.message || "AI生成失败")
    }
  } catch (e) {
    ElMessage.error("AI生成失败：" + e.message)
  } finally {
    aiReportLoading.value = false
  }
}
// Markdown渲染（支持标题/加粗/列表/引用/代码/分割线/行内代码）
const renderMarkdown = (md) => {
  if (!md) return ''
  const escapeHtml = (s) => s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  // 先按行处理块级元素
  const lines = md.split('\n')
  let html = ''
  let inUl = false, inOl = false, inQuote = false, inCode = false
  const closeLists = () => { if (inUl) { html += '</ul>'; inUl = false } if (inOl) { html += '</ol>'; inOl = false } }
  const closeQuote = () => { if (inQuote) { html += '</blockquote>'; inQuote = false } }
  for (let i = 0; i < lines.length; i++) {
    let line = lines[i]
    // 代码块
    if (line.trim().startsWith('```')) { closeLists(); closeQuote(); if (inCode) { html += '</code></pre>'; inCode = false } else { html += '<pre><code>'; inCode = true } continue }
    if (inCode) { html += escapeHtml(line) + '\n'; continue }
    // 标题
    let m = line.match(/^### (.+)$/); if (m) { closeLists(); closeQuote(); html += `<h3>${inline(m[1])}</h3>`; continue }
    m = line.match(/^## (.+)$/); if (m) { closeLists(); closeQuote(); html += `<h2>${inline(m[1])}</h2>`; continue }
    m = line.match(/^# (.+)$/); if (m) { closeLists(); closeQuote(); html += `<h1>${inline(m[1])}</h1>`; continue }
    // 分割线
    if (/^---+\s*$/.test(line)) { closeLists(); closeQuote(); html += '<hr/>'; continue }
    // 引用
    m = line.match(/^> (.+)$/); if (m) { closeLists(); if (!inQuote) { html += '<blockquote>'; inQuote = true } html += `<p>${inline(m[1])}</p>`; continue }
    // 无序列表
    m = line.match(/^- (.+)$/); if (m) { closeQuote(); if (inOl) { html += '</ol>'; inOl = false } if (!inUl) { html += '<ul>'; inUl = true } html += `<li>${inline(m[1])}</li>`; continue }
    // 有序列表
    m = line.match(/^\d+\. (.+)$/); if (m) { closeQuote(); if (inUl) { html += '</ul>'; inUl = false } if (!inOl) { html += '<ol>'; inOl = true } html += `<li>${inline(m[1])}</li>`; continue }
    // 空行
    if (line.trim() === '') { closeLists(); closeQuote(); continue }
    // 普通段落
    closeLists(); closeQuote(); html += `<p>${inline(line)}</p>`
  }
  closeLists(); closeQuote(); if (inCode) html += '</code></pre>'
  function inline(s) {
    return escapeHtml(s).replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>').replace(/`(.+?)`/g, '<code>$1</code>')
  }
  return html
}
const copyReport = () => {
  navigator.clipboard.writeText(aiReportContent.value).then(() => {
    ElMessage.success("已复制到剪贴板")
  }).catch(() => ElMessage.error("复制失败"))
}
const downloadReport = () => {
  if (!aiReportContent.value) return
  const header = `# AI 交易诊断报告\n\n> 数据范围：最近 ${reviewReportRange.value} 天\n> 生成时间：${aiReportGenerateTime.value}\n\n---\n\n`
  const blob = new Blob([header + aiReportContent.value], { type: 'text/markdown;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `AI交易诊断报告_${new Date().toISOString().slice(0, 10)}.md`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success("报告已下载")
}

// 通用图表初始化
const initChart = (domRef, option) => {
  if (!domRef.value) return null
  const existing = echarts.getInstanceByDom(domRef.value)
  if (existing) existing.dispose()
  const inst = echarts.init(domRef.value, 'dark')
  inst.setOption(option)
  return inst
}

// 字典翻译辅助
const dictText = (code, options) => getDisplayText(code, options)
const buildPieData = (stats, options) => (stats || []).map(s => ({ name: dictText(s.code, options), value: s.count }))

const renderReviewCharts = () => {
  const d = reviewReportData.value
  // 1.情绪温度趋势
  initChart(emotionChartRef, {
    tooltip: { trigger: 'axis', formatter: (p) => p[0].axisValue + '<br/>' + p.map(i => i.marker + i.seriesName + ': ' + dictText(i.value, reviewEmotionTempOptions.value)).join('<br/>') },
    xAxis: { type: 'category', data: (d.emotionTrends || []).map(i => i.date) },
    yAxis: { type: 'value', min: 0, max: 5, axisLabel: { formatter: (v) => dictText(v, reviewEmotionTempOptions.value) || v } },
    series: [{ name: '情绪温度', type: 'line', data: (d.emotionTrends || []).map(i => i.score), smooth: true, markLine: { data: [{ yAxis: 2.5 }] } }]
  })
  // 2.市场状态分布
  initChart(marketStatusChartRef, { tooltip: { trigger: 'item' }, legend: { bottom: 0 }, series: [{ type: 'pie', radius: ['40%','70%'], data: buildPieData(d.marketStatusStats, reviewMarketStatusOptions.value) }] })
  // 3.涨停跌停趋势
  initChart(limitChartRef, {
    tooltip: { trigger: 'axis' }, legend: { data: ['涨停','跌停','连板'], bottom: 0 },
    xAxis: { type: 'category', data: (d.limitTrends || []).map(i => i.date) }, yAxis: { type: 'value' },
    series: [
      { name: '涨停', type: 'bar', data: (d.limitTrends || []).map(i => i.limitUp) },
      { name: '跌停', type: 'bar', data: (d.limitTrends || []).map(i => i.limitDown) },
      { name: '连板', type: 'line', data: (d.limitTrends || []).map(i => i.continuousBoard), smooth: true }
    ]
  })
  // 4.主线板块频次
  initChart(sectorChartRef, {
    tooltip: { trigger: 'axis' }, grid: { left: '15%' },
    yAxis: { type: 'category', data: (d.sectorStats || []).map(i => dictText(i.code, reviewSectorOptions.value)) },
    xAxis: { type: 'value' }, series: [{ type: 'bar', data: (d.sectorStats || []).map(i => i.count) }]
  })
  // 5.适配体系分布
  initChart(adaptSystemChartRef, { tooltip: { trigger: 'item' }, legend: { bottom: 0 }, series: [{ type: 'pie', radius: '65%', data: buildPieData(d.adaptSystemStats, reviewAdaptSystemOptions.value) }] })
  // 6.操作自评趋势
  initChart(selfRatingChartRef, {
    tooltip: { trigger: 'axis', formatter: (p) => p[0].axisValue + '<br/>' + p.map(i => i.marker + i.seriesName + ': ' + dictText(i.value, reviewSelfRatingOptions.value)).join('<br/>') },
    xAxis: { type: 'category', data: (d.selfRatingTrends || []).map(i => i.date) },
    yAxis: { type: 'value', min: 0, max: 5, axisLabel: { formatter: (v) => dictText(v, reviewSelfRatingOptions.value) || v } },
    series: [{ name: '操作自评', type: 'line', data: (d.selfRatingTrends || []).map(i => i.rating), smooth: true }]
  })
  // 7.当日盈亏趋势
  initChart(dailyProfitChartRef, {
    tooltip: { trigger: 'axis' }, xAxis: { type: 'category', data: (d.dailyProfitTrends || []).map(i => i.date) }, yAxis: { type: 'value' },
    series: [{ type: 'bar', data: (d.dailyProfitTrends || []).map(i => ({ value: i.profit, itemStyle: { color: i.profit > 0 ? '#F56C6C' : '#67C23A' } })) }]
  })
  // 8.北向资金趋势
  initChart(northChartRef, {
    tooltip: { trigger: 'axis' }, xAxis: { type: 'category', data: (d.northTrends || []).map(i => i.date) }, yAxis: { type: 'value' },
    series: [{ type: 'line', data: (d.northTrends || []).map(i => i.amount), smooth: true, areaStyle: {} }]
  })
  // 9.心理状态分布
  initChart(psychologyChartRef, { tooltip: { trigger: 'item' }, legend: { bottom: 0 }, series: [{ type: 'pie', radius: '65%', data: buildPieData(d.psychologyStats, tradePsychologyOptions.value) }] })
  // 10.心理状态与盈亏关系
  initChart(psychologyProfitChartRef, {
    tooltip: { trigger: 'axis' }, xAxis: { type: 'category', data: (d.psychologyProfits || []).map(i => dictText(i.code, tradePsychologyOptions.value)), axisLabel: { rotate: 30 } }, yAxis: { type: 'value' },
    series: [{ type: 'bar', data: (d.psychologyProfits || []).map(i => ({ value: Number(i.avgProfit).toFixed(2), itemStyle: { color: i.avgProfit > 0 ? '#F56C6C' : '#67C23A' } })) }]
  })
  // 11.交易时段分布
  initChart(timeSlotChartRef, { tooltip: { trigger: 'item' }, legend: { bottom: 0 }, series: [{ type: 'pie', radius: '65%', data: buildPieData(d.timeSlotStats, tradeTimeSlotOptions.value) }] })
  // 12.买卖方向统计
  initChart(directionChartRef, {
    tooltip: { trigger: 'axis' }, xAxis: { type: 'category', data: (d.directionStats || []).map(i => dictText(i.code, tradeDirectionOptions.value)) }, yAxis: { type: 'value' },
    series: [{ type: 'bar', data: (d.directionStats || []).map(i => i.count) }]
  })
  // 13.执行评分趋势
  initChart(executeRatingChartRef, {
    tooltip: { trigger: 'axis', formatter: (p) => p[0].axisValue + '<br/>' + p.map(i => i.marker + i.seriesName + ': ' + dictText(i.value, tradeExecuteRatingOptions.value)).join('<br/>') },
    xAxis: { type: 'category', data: (d.executeRatingTrends || []).map(i => i.date) },
    yAxis: { type: 'value', min: 0, max: 5, axisLabel: { formatter: (v) => dictText(v, tradeExecuteRatingOptions.value) || v } },
    series: [{ name: '执行评分', type: 'line', data: (d.executeRatingTrends || []).map(i => Number(i.rating).toFixed(2)), smooth: true }]
  })
  // 14.是否符合计划统计
  initChart(planMatchChartRef, { tooltip: { trigger: 'item' }, legend: { bottom: 0 }, series: [{ type: 'pie', radius: '65%', data: buildPieData(d.planMatchStats, tradePlanMatchOptions.value) }] })
  // 15.个股交易频次Top10
  initChart(stockChartRef, {
    tooltip: { trigger: 'axis' }, grid: { left: '15%' },
    yAxis: { type: 'category', data: (d.stockStats || []).map(i => i.stockName) }, xAxis: { type: 'value' },
    series: [{ type: 'bar', data: (d.stockStats || []).map(i => i.count) }]
  })
  // 16.每日盈亏vs交易笔数
  initChart(profitVsCountChartRef, {
    tooltip: { trigger: 'axis' }, legend: { data: ['盈亏%','交易笔数'], bottom: 0 },
    xAxis: { type: 'category', data: (d.dailyProfitVsTradeCounts || []).map(i => i.date) },
    yAxis: [{ type: 'value', name: '盈亏%' }, { type: 'value', name: '笔数' }],
    series: [
      { name: '盈亏%', type: 'line', data: (d.dailyProfitVsTradeCounts || []).map(i => Number(i.profit).toFixed(2)), smooth: true },
      { name: '交易笔数', type: 'bar', yAxisIndex: 1, data: (d.dailyProfitVsTradeCounts || []).map(i => i.tradeCount) }
    ]
  })
  // 17.情绪温度vs次日交易胜率
  initChart(emotionVsWinChartRef, {
    tooltip: {
      trigger: 'axis',
      formatter: (p) => {
        let r = p[0].axisValue + '<br/>'
        p.forEach(i => {
          if (i.seriesName === '情绪温度') r += i.marker + i.seriesName + ': ' + dictText(i.value, reviewEmotionTempOptions.value) + '<br/>'
          else r += i.marker + i.seriesName + ': ' + i.value + '%<br/>'
        })
        return r
      }
    },
    legend: { data: ['情绪温度','次日胜率%'], bottom: 0 },
    xAxis: { type: 'category', data: (d.emotionVsWinRates || []).map(i => i.date) },
    yAxis: [
      { type: 'value', name: '情绪', min: 0, max: 5, axisLabel: { formatter: (v) => dictText(v, reviewEmotionTempOptions.value) || v } },
      { type: 'value', name: '胜率%', min: 0, max: 100 }
    ],
    series: [
      { name: '情绪温度', type: 'bar', data: (d.emotionVsWinRates || []).map(i => i.emotion) },
      { name: '次日胜率%', type: 'line', yAxisIndex: 1, data: (d.emotionVsWinRates || []).map(i => Number(i.winRate).toFixed(1)), smooth: true }
    ]
  })
}

// ==================== 窗口 resize ====================
const handleResize = () => {
  document.querySelectorAll('.chart-box').forEach(dom => {
    const inst = echarts.getInstanceByDom(dom)
    if (inst) inst.resize()
  })
}

window.addEventListener('resize', handleResize)

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  document.querySelectorAll('.chart-box').forEach(dom => {
    const inst = echarts.getInstanceByDom(dom)
    if (inst) inst.dispose()
  })
})

//--------------------钩子函数-------------------------
onMounted(() => {
  Promise.all([
    loadDict('t_trial_review_emotion_temp', reviewEmotionTempOptions),
    loadDict('t_trial_review_market_status', reviewMarketStatusOptions),
    loadDict('t_trial_review_sector', reviewSectorOptions),
    loadDict('t_trial_review_adapt_system', reviewAdaptSystemOptions),
    loadDict('t_trial_review_self_rating', reviewSelfRatingOptions),
    loadDict('t_trial_trade_psychology', tradePsychologyOptions),
    loadDict('t_trial_trade_time_slot', tradeTimeSlotOptions),
    loadDict('t_trial_trade_direction', tradeDirectionOptions),
    loadDict('t_trial_trade_execute_rating', tradeExecuteRatingOptions),
    loadDict('t_trial_trade_plan_match', tradePlanMatchOptions)
  ]).catch(err => console.error('字典加载失败:', err))

  fetchReviewReportData()
})
</script>

<style scoped>
.review-analysis-div {
  padding: 10px;
}

.review-time-filter {
  margin-bottom: 16px;
  text-align: center;
}

.ai-report-meta {
  display: flex;
  gap: 24px;
  padding: 8px 16px;
  margin-bottom: 4px;
  background: rgba(64, 158, 255, 0.08);
  border-radius: 4px;
  color: #909399;
  font-size: 13px;
}
.ai-report-content {
  max-height: 60vh;
  overflow-y: auto;
  padding: 12px 16px;
  line-height: 1.85;
  color: #e0e0e0;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 6px;
}

.ai-report-content h1 { font-size: 20px; color: #409EFF; margin: 18px 0 10px; border-bottom: 1px solid #3a3a5c; padding-bottom: 6px; }
.ai-report-content h2 { font-size: 17px; color: #67C23A; margin: 16px 0 8px; padding-left: 8px; border-left: 3px solid #67C23A; }
.ai-report-content h3 { font-size: 15px; color: #E6A23C; margin: 12px 0 6px; }
.ai-report-content p { margin: 8px 0; }
.ai-report-content ul, .ai-report-content ol { margin: 8px 0 8px 22px; }
.ai-report-content li { margin: 4px 0; }
.ai-report-content strong { color: #F56C6C; font-weight: 600; }
.ai-report-content code { background: #2d2d44; color: #E6A23C; padding: 1px 5px; border-radius: 3px; font-size: 13px; }
.ai-report-content pre { background: #1e1e2e; padding: 10px 12px; border-radius: 4px; overflow-x: auto; margin: 8px 0; }
.ai-report-content pre code { background: none; color: #e0e0e0; padding: 0; }
.ai-report-content blockquote { border-left: 3px solid #409EFF; padding: 4px 12px; margin: 8px 0; color: #b0b0b0; background: rgba(64, 158, 255, 0.05); border-radius: 0 4px 4px 0; }
.ai-report-content hr { border: none; border-top: 1px dashed #3a3a5c; margin: 14px 0; }

.review-kpi-row {
  margin-bottom: 12px;
}

.review-kpi-card {
  background: linear-gradient(135deg, #2c3e50, #1a1a2e);
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  border: 1px solid #3a3a5c;
}

.review-kpi-card .kpi-label {
  font-size: 13px;
  color: #b0b5bd;
  margin-bottom: 8px;
}

.review-kpi-card .kpi-value {
  font-size: 22px;
  font-weight: 700;
  color: #e0e0e0;
}

.review-chart-card {
  background: #1a1a2e;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
  border: 1px solid #2a2a4c;
}

.review-chart-card .chart-title {
  font-size: 14px;
  font-weight: 600;
  color: #e0e0e0;
  margin-bottom: 8px;
  padding-left: 8px;
  border-left: 3px solid #409EFF;
}

.review-chart-card .chart-box {
  width: 100%;
  height: 300px;
}

/* 对话框样式 */
:deep(.custom-dialog) {
  border-radius: 16px !important;
  overflow: hidden !important;
  box-shadow: 0 16px 48px rgba(102, 126, 234, 0.25) !important;
}

:deep(.el-dialog__body) {
  max-height: 60vh !important;
  overflow-y: auto !important;
}
</style>