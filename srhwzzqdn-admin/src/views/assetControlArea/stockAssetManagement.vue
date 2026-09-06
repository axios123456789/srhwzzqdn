<template>
  <div class="stock-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1><el-icon><Histogram /></el-icon> 股票资产管理</h1>
      <div class="fetch-area">
        <el-input v-model="stockCodeInput" placeholder="请输入股票代码（如：600519、000001）" style="width: 280px;" clearable @keyup.enter="fetchStockData" />
        <el-button type="primary" :loading="fetchLoading" @click="fetchStockData">
          <el-icon><Search /></el-icon> 数据获取
        </el-button>
        <el-button type="success" :loading="refreshLoading" @click="refreshAllData">
          <el-icon><Refresh /></el-icon> 实时数据
        </el-button>
      </div>
    </div>

    <!-- 条件查询区 -->
    <div class="query-section">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="股票名称">
          <el-input v-model="queryParams.stockName" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="股票代码">
          <el-input v-model="queryParams.stockCode" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="行业">
          <el-input v-model="queryParams.industry" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="市场">
          <el-select v-model="queryParams.market" placeholder="全部" clearable style="width: 120px;">
            <el-option label="沪市" :value="1" />
            <el-option label="深市" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon> 查询</el-button>
          <el-button @click="resetQuery"><el-icon><Refresh /></el-icon> 重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 股票列表 -->
    <div class="list-section">
      <el-table :data="stockList" border stripe highlight-current-row @row-click="handleRowClick" style="width: 100%;">
        <el-table-column label="股票代码" prop="stockCode" width="100" align="center" />
        <el-table-column label="股票名称" prop="stockName" width="120" align="center" />
        <el-table-column label="市场" width="60" align="center">
          <template #default="{ row }">
            <el-tag :type="row.market === 1 ? 'danger' : 'primary'" size="small">{{ row.market === 1 ? '沪' : '深' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最新价" width="100" align="right">
          <template #default="{ row }">
            <span :class="row.changePct >= 0 ? 'price-up' : 'price-down'">{{ formatPrice(row.lastPrice) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="涨跌幅" width="100" align="right">
          <template #default="{ row }">
            <span :class="row.changePct >= 0 ? 'price-up' : 'price-down'">{{ row.changePct >= 0 ? '+' : '' }}{{ formatPct(row.changePct) }}%</span>
          </template>
        </el-table-column>
        <el-table-column label="换手率" prop="turnoverRate" width="90" align="right">
          <template #default="{ row }">{{ formatPct(row.turnoverRate) }}%</template>
        </el-table-column>
        <el-table-column label="总市值(亿)" width="120" align="right">
          <template #default="{ row }">{{ formatBigNum(row.totalMarketCap) }}</template>
        </el-table-column>
        <el-table-column label="PE(TTM)" width="90" align="right">
          <template #default="{ row }">{{ formatPrice(row.peTtm) }}</template>
        </el-table-column>
        <el-table-column label="PB" width="80" align="right">
          <template #default="{ row }">{{ formatPrice(row.pbRatio) }}</template>
        </el-table-column>
        <el-table-column label="行业" prop="industry" width="120" align="center" show-overflow-tooltip />
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" size="small" link @click.stop="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-area">
        <el-pagination v-model:current-page="page.current" v-model:page-size="page.limit"
          :total="page.total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData" @current-change="fetchData" />
      </div>
    </div>

    <!-- 详细数据展示区 -->
    <div v-if="selectedStock" class="detail-section">
      <!-- 基本面数据 + 实时行情 -->
      <div class="info-grid">
        <!-- 公司名片 -->
        <div class="info-card name-card">
          <div class="card-title">公司名片</div>
          <div class="name-row">
            <span class="stock-name">{{ selectedStock.stockName }}</span>
            <el-tag :type="selectedStock.market === 1 ? 'danger' : 'primary'" size="small">{{ selectedStock.market === 1 ? '沪' : '深' }}{{ selectedStock.stockCode }}</el-tag>
          </div>
          <div class="industry-tag" v-if="selectedStock.industry">{{ selectedStock.industry }}</div>
          <div class="price-display">
            <span :class="selectedStock.changePct >= 0 ? 'price-up big' : 'price-down big'">{{ formatPrice(selectedStock.lastPrice) }}</span>
            <span :class="selectedStock.changePct >= 0 ? 'price-up' : 'price-down'">
              {{ selectedStock.changePct >= 0 ? '+' : '' }}{{ formatPct(selectedStock.changePct) }}%
              ({{ selectedStock.changePct >= 0 ? '+' : '' }}{{ formatPrice(selectedStock.changeAmount) }})
            </span>
          </div>
          <div class="list-date" v-if="selectedStock.listDate">上市日期：{{ selectedStock.listDate }}</div>
        </div>

        <!-- 估值指标 -->
        <div class="info-card">
          <div class="card-title">估值指标</div>
          <div class="metric-grid">
            <div class="metric-item"><span class="label">PE(静)</span><span class="value">{{ formatPrice(selectedStock.peStatic) }}</span></div>
            <div class="metric-item"><span class="label">PE(动)</span><span class="value">{{ formatPrice(selectedStock.peDynamic) }}</span></div>
            <div class="metric-item"><span class="label">PE(TTM)</span><span class="value">{{ formatPrice(selectedStock.peTtm) }}</span></div>
            <div class="metric-item"><span class="label">PB</span><span class="value">{{ formatPrice(selectedStock.pbRatio) }}</span></div>
            <div class="metric-item"><span class="label">PS</span><span class="value">{{ formatPrice(selectedStock.psRatio) }}</span></div>
            <div class="metric-item"><span class="label">股息率</span><span class="value">{{ formatPct(selectedStock.dividendYield) }}%</span></div>
          </div>
        </div>

        <!-- 实时行情 -->
        <div class="info-card">
          <div class="card-title">实时行情</div>
          <div class="metric-grid">
            <div class="metric-item"><span class="label">换手率</span><span class="value">{{ formatPct(selectedStock.turnoverRate) }}%</span></div>
            <div class="metric-item"><span class="label">量比</span><span class="value">{{ formatPrice(selectedStock.volumeRatio) }}</span></div>
            <div class="metric-item"><span class="label">振幅</span><span class="value">{{ formatPct(selectedStock.amplitude) }}%</span></div>
            <div class="metric-item"><span class="label">成交量</span><span class="value">{{ formatVolume(selectedStock.volume) }}</span></div>
            <div class="metric-item"><span class="label">成交额</span><span class="value">{{ formatBigNum(selectedStock.turnover) }}亿</span></div>
          </div>
        </div>

        <!-- 市值规模 -->
        <div class="info-card">
          <div class="card-title">市值规模</div>
          <div class="metric-grid">
            <div class="metric-item"><span class="label">总市值</span><span class="value">{{ formatBigNum(selectedStock.totalMarketCap) }}亿</span></div>
            <div class="metric-item"><span class="label">流通市值</span><span class="value">{{ formatBigNum(selectedStock.circMarketCap) }}亿</span></div>
            <div class="metric-item"><span class="label">总股本</span><span class="value">{{ formatBigNum(selectedStock.totalShares) }}万</span></div>
            <div class="metric-item"><span class="label">流通股本</span><span class="value">{{ formatBigNum(selectedStock.circShares) }}万</span></div>
            <div class="metric-item"><span class="label">内盘</span><span class="value">{{ formatVolume(selectedStock.innerVolume) }}</span></div>
            <div class="metric-item"><span class="label">外盘</span><span class="value">{{ formatVolume(selectedStock.outerVolume) }}</span></div>
          </div>
        </div>

        <!-- 公司信息 -->
        <div class="info-card full-width" v-if="selectedStock.companyDesc || selectedStock.mainBusiness">
          <div class="card-title">公司信息</div>
          <div class="company-info">
            <div class="info-row" v-if="selectedStock.legalRep"><span class="label">法人代表：</span>{{ selectedStock.legalRep }}</div>
            <div class="info-row" v-if="selectedStock.generalManager"><span class="label">总经理：</span>{{ selectedStock.generalManager }}</div>
            <div class="info-row" v-if="selectedStock.industry"><span class="label">所属行业：</span>{{ selectedStock.industry }}</div>
            <div class="info-row" v-if="selectedStock.companyDesc"><span class="label">公司简介：</span>{{ selectedStock.companyDesc }}</div>
            <div class="info-row" v-if="selectedStock.mainBusiness"><span class="label">主营业务：</span>{{ selectedStock.mainBusiness }}</div>
          </div>
        </div>
      </div>

      <!-- 分析页签 -->
      <el-tabs v-model="activeTab" class="detail-tabs" @tab-change="handleTabChange">
        <!-- K线图 -->
        <el-tab-pane label="K线图" name="kline">
          <div class="chart-header">
            <span class="chart-title">K线图（滚轮/拖动缩放，通达信式）</span>
            <el-radio-group v-model="klineType" size="small" @change="loadKlineData">
              <el-radio-button :label="1">日K</el-radio-button>
              <el-radio-button :label="2">周K</el-radio-button>
              <el-radio-button :label="3">月K</el-radio-button>
            </el-radio-group>
          </div>
          <div ref="klineChartRef" class="chart-container kline-height"></div>
        </el-tab-pane>

        <!-- 基本面 -->
        <el-tab-pane label="基本面" name="finance">
          <el-table v-if="financeData.length" :data="financeData" border stripe size="small" max-height="480">
            <el-table-column prop="reportDate" label="报告期" width="100" fixed="left" align="center" />
            <el-table-column label="营收(亿)" width="95" align="right">
              <template #default="{ row }">{{ formatBigNum(row.revenue) }}</template>
            </el-table-column>
            <el-table-column label="营收同比" width="90" align="right">
              <template #default="{ row }">
                <span :class="pctClass(row.revenueYoy)">{{ formatPct(row.revenueYoy) }}%</span>
              </template>
            </el-table-column>
            <el-table-column label="归母净利(亿)" width="110" align="right">
              <template #default="{ row }">{{ formatBigNum(row.netProfit) }}</template>
            </el-table-column>
            <el-table-column label="净利同比" width="90" align="right">
              <template #default="{ row }">
                <span :class="pctClass(row.netProfitYoy)">{{ formatPct(row.netProfitYoy) }}%</span>
              </template>
            </el-table-column>
            <el-table-column label="扣非净利(亿)" width="110" align="right">
              <template #default="{ row }">{{ formatBigNum(row.deductNetProfit) }}</template>
            </el-table-column>
            <el-table-column label="EPS(元)" width="85" align="right">
              <template #default="{ row }">{{ formatPrice(row.eps) }}</template>
            </el-table-column>
            <el-table-column label="BPS(元)" width="85" align="right">
              <template #default="{ row }">{{ formatPrice(row.bps) }}</template>
            </el-table-column>
            <el-table-column label="ROE(%)" width="80" align="right">
              <template #default="{ row }">{{ formatPct(row.roe) }}</template>
            </el-table-column>
            <el-table-column label="毛利率(%)" width="90" align="right">
              <template #default="{ row }">{{ formatPct(row.grossMargin) }}</template>
            </el-table-column>
            <el-table-column label="净利率(%)" width="90" align="right">
              <template #default="{ row }">{{ formatPct(row.netMargin) }}</template>
            </el-table-column>
            <el-table-column label="资产负债率(%)" width="115" align="right">
              <template #default="{ row }">{{ formatPct(row.debtRatio) }}</template>
            </el-table-column>
            <el-table-column label="每股现金流(元)" width="120" align="right">
              <template #default="{ row }">{{ formatPrice(row.cashflowPerShare) }}</template>
            </el-table-column>
            <el-table-column label="流动比率" width="85" align="right">
              <template #default="{ row }">{{ formatPrice(row.currentRatio) }}</template>
            </el-table-column>
            <el-table-column label="速动比率" width="85" align="right">
              <template #default="{ row }">{{ formatPrice(row.quickRatio) }}</template>
            </el-table-column>
            <el-table-column label="ROIC(%)" width="85" align="right">
              <template #default="{ row }">{{ formatPct(row.roic) }}</template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无财务数据，请点击【实时数据】刷新补全" />
        </el-tab-pane>

        <!-- 资金面 -->
        <el-tab-pane label="资金面" name="flow">
          <div class="chart-header">
            <span class="chart-title">资金流向（近30日，单位：万元）</span>
          </div>
          <div ref="flowChartRef" class="chart-container" style="height: 340px;"></div>
        </el-tab-pane>

        <!-- 技术面 -->
        <el-tab-pane label="技术面" name="tech">
          <template v-if="techView">
            <div class="signal-area">
              <el-tag v-for="(s, i) in techView.signals" :key="i" :type="s.type" effect="plain">{{ s.text }}</el-tag>
            </div>
            <div class="metric-grid tech-grid">
              <div class="metric-item"><span class="label">最新价</span><span class="value">{{ formatPrice(techView.last) }}</span></div>
              <div class="metric-item"><span class="label">MA5</span><span class="value">{{ techView.ma5 ?? '-' }}</span></div>
              <div class="metric-item"><span class="label">MA10</span><span class="value">{{ techView.ma10 ?? '-' }}</span></div>
              <div class="metric-item"><span class="label">MA20</span><span class="value">{{ techView.ma20 ?? '-' }}</span></div>
              <div class="metric-item"><span class="label">MA60</span><span class="value">{{ techView.ma60 ?? '-' }}</span></div>
              <div class="metric-item"><span class="label">MACD DIF</span><span class="value">{{ techView.dif }}</span></div>
              <div class="metric-item"><span class="label">MACD DEA</span><span class="value">{{ techView.dea }}</span></div>
              <div class="metric-item"><span class="label">MACD柱</span><span class="value">{{ techView.hist }}</span></div>
              <div class="metric-item"><span class="label">KDJ（K/D/J）</span><span class="value">{{ techView.k }} / {{ techView.d }} / {{ techView.j }}</span></div>
              <div class="metric-item"><span class="label">RSI（6/12/24）</span><span class="value">{{ techView.rsi6 ?? '-' }} / {{ techView.rsi12 ?? '-' }} / {{ techView.rsi24 ?? '-' }}</span></div>
            </div>
          </template>
          <el-empty v-else description="暂无日K数据，无法计算技术指标，请点击【实时数据】刷新" />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Histogram, Search, Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import {
  GetStockAllDataByCode, GetStockListByCondition, GetStockBasicByCode,
  GetStockKline, GetStockCapitalFlow, GetStockFinance, DeleteStockDataByCode,
  RefreshAllStockRealtime
} from '@/api/stockAsset'

const stockCodeInput = ref('')
const fetchLoading = ref(false)
const refreshLoading = ref(false)
const stockList = ref([])
const selectedStock = ref(null)
const klineChartRef = ref(null)
const flowChartRef = ref(null)
const klineType = ref(1)
const activeTab = ref('kline')
const financeData = ref([])
const klineRaw = ref([])
let klineChart = null
let flowChart = null

const queryParams = reactive({
  stockName: '', stockCode: '', industry: '', market: null
})

const page = reactive({ current: 1, limit: 10, total: 0 })

const formatPrice = (val) => val != null ? Number(val).toFixed(2) : '-'
const formatPct = (val) => val != null ? Number(val).toFixed(2) : '-'
const formatBigNum = (val) => val != null ? Number(val).toFixed(2) : '-'
const formatVolume = (val) => {
  if (val == null) return '-'
  if (val >= 100000000) return (val / 100000000).toFixed(2) + '亿'
  if (val >= 10000) return (val / 10000).toFixed(2) + '万'
  return val
}
const pctClass = (val) => Number(val) >= 0 ? 'price-up' : 'price-down'
const klineTypeName = { 1: '日K', 2: '周K', 3: '月K' }
// 与后端抓取上限一致，取全量历史（日K约1万根/周K约2千根/月K约600根）
const klineLimit = { 1: 10000, 2: 2000, 3: 600 }

const fetchStockData = async () => {
  if (!stockCodeInput.value) {
    ElMessage.warning('请输入股票代码')
    return
  }
  fetchLoading.value = true
  try {
    const res = await GetStockAllDataByCode(stockCodeInput.value.trim())
    if (res.code === 200) {
      ElMessage.success(res.data || '获取成功')
      fetchData()
    } else {
      ElMessage.error(res.message || '获取失败')
    }
  } catch (e) {
    ElMessage.error('获取失败：' + e.message)
  } finally {
    fetchLoading.value = false
  }
}

const refreshAllData = async () => {
  refreshLoading.value = true
  ElMessage.info('正在刷新所有股票的实时数据，请稍候...')
  try {
    const res = await RefreshAllStockRealtime()
    if (res.code === 200) {
      ElMessage.success(res.data || '刷新完成')
      fetchData()
      // 若详情区有选中的股票，同步刷新其数据
      if (selectedStock.value) {
        const code = selectedStock.value.stockCode
        const basic = await GetStockBasicByCode(code)
        if (basic.code === 200) selectedStock.value = basic.data
        loadKlineData()
        loadFinanceData()
        loadFlowData()
      }
    } else {
      ElMessage.error(res.message || '刷新失败')
    }
  } catch (e) {
    ElMessage.error('刷新失败：' + e.message)
  } finally {
    refreshLoading.value = false
  }
}

const fetchData = async () => {
  try {
    const res = await GetStockListByCondition(page.current, page.limit, queryParams)
    if (res.code === 200) {
      stockList.value = res.data.list
      page.total = res.data.total
    }
  } catch (e) {
    ElMessage.error('查询失败')
  }
}

const handleSearch = () => {
  page.current = 1
  fetchData()
}

const resetQuery = () => {
  queryParams.stockName = ''
  queryParams.stockCode = ''
  queryParams.industry = ''
  queryParams.market = null
  page.current = 1
  fetchData()
}

const handleRowClick = async (row) => {
  try {
    const res = await GetStockBasicByCode(row.stockCode)
    if (res.code === 200) {
      selectedStock.value = res.data
      financeData.value = []
      klineRaw.value = []
      await nextTick()
      loadKlineData()
      loadFinanceData()
      loadFlowData()
    }
  } catch (e) {
    ElMessage.error('加载详情失败')
  }
}

const loadKlineData = async () => {
  if (!selectedStock.value) return
  try {
    const res = await GetStockKline(selectedStock.value.stockCode, klineType.value, klineLimit[klineType.value])
    if (res.code === 200 && res.data && res.data.length > 0) {
      if (activeTab.value === 'kline') {
        renderKlineChart(res.data)
      } else if (klineType.value === 1) {
        // 技术面依赖日K数据，仅缓存不渲染
        klineRaw.value = [...res.data].sort((a, b) => new Date(a.tradeDate) - new Date(b.tradeDate))
      }
    } else {
      klineRaw.value = []
      klineChart && klineChart.clear()
      ElMessage.warning(`暂无${klineTypeName[klineType.value]}数据，请点击【实时数据】刷新补全`)
    }
  } catch (e) {
    console.error('K线数据加载失败', e)
  }
}

const loadFinanceData = async () => {
  if (!selectedStock.value) return
  try {
    const res = await GetStockFinance(selectedStock.value.stockCode, 12)
    if (res.code === 200) {
      financeData.value = res.data || []
    }
  } catch (e) {
    console.error('财务数据加载失败', e)
  }
}

const loadFlowData = async () => {
  if (!selectedStock.value) return
  try {
    const res = await GetStockCapitalFlow(selectedStock.value.stockCode, 30)
    if (res.code === 200 && res.data && res.data.length > 0) {
      if (activeTab.value === 'flow') {
        renderFlowChart(res.data)
      }
    }
  } catch (e) {
    console.error('资金流向数据加载失败', e)
  }
}

const handleTabChange = (name) => {
  if (!selectedStock.value) return
  nextTick(() => {
    if (name === 'kline') {
      loadKlineData()
    } else if (name === 'flow') {
      loadFlowData()
    }
  })
}

const calcMa = (closes, days) => closes.map((_, i) => {
  if (i < days - 1) return null
  let s = 0
  for (let j = i - days + 1; j <= i; j++) s += closes[j]
  return Number((s / days).toFixed(2))
})

const calcEma = (arr, n) => {
  const k = 2 / (n + 1)
  const out = []
  let prev = arr[0]
  arr.forEach((v, i) => {
    prev = i === 0 ? v : v * k + prev * (1 - k)
    out.push(prev)
  })
  return out
}

const renderKlineChart = (data) => {
  if (!klineChartRef.value) return
  // 选中行被删除后 v-if 会移除容器 DOM，旧实例绑定在已卸载节点上，需重建
  if (klineChart && klineChart.getDom() !== klineChartRef.value) {
    klineChart.dispose()
    klineChart = null
  }
  if (!klineChart) klineChart = echarts.init(klineChartRef.value)
  const sorted = [...data].sort((a, b) => new Date(a.tradeDate) - new Date(b.tradeDate))
  if (klineType.value === 1) klineRaw.value = sorted
  const dates = sorted.map(d => d.tradeDate)
  const ohlc = sorted.map(d => [Number(d.openPrice), Number(d.closePrice), Number(d.lowPrice), Number(d.highPrice)])
  const closes = sorted.map(d => Number(d.closePrice))
  const volumes = sorted.map(d => Number(d.volume))
  const ups = sorted.map(d => Number(d.changePct ?? 0) >= 0)

  // MA 均线
  const maDefs = [
    { name: 'MA5', days: 5, color: '#ff9800' },
    { name: 'MA10', days: 10, color: '#2196f3' },
    { name: 'MA20', days: 20, color: '#9c27b0' },
    { name: 'MA60', days: 60, color: '#607d8b' }
  ]

  // MACD(12,26,9)，柱 = 2*(DIF-DEA)
  const ema12 = calcEma(closes, 12)
  const ema26 = calcEma(closes, 26)
  const dif = ema12.map((v, i) => Number((v - ema26[i]).toFixed(3)))
  const dea = calcEma(dif, 9).map(v => Number(v.toFixed(3)))
  const hist = dif.map((v, i) => Number((2 * (v - dea[i])).toFixed(3)))

  // 默认展示最近120根，可缩放查看全部
  const startPct = Math.max(0, 100 - (120 / sorted.length) * 100)

  klineChart.setOption({
    animation: false,
    backgroundColor: '#fff',
    axisPointer: { link: [{ xAxisIndex: 'all' }] },
    tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
    legend: { data: ['MA5', 'MA10', 'MA20', 'MA60', 'DIF', 'DEA'], top: 0 },
    grid: [
      { left: 60, right: 70, top: 28, height: '50%' },
      { left: 60, right: 70, top: '62%', height: '12%' },
      { left: 60, right: 70, top: '78%', height: '14%' }
    ],
    xAxis: [
      { type: 'category', data: dates, scale: true, axisLine: { onZero: false }, splitLine: { show: false } },
      { type: 'category', gridIndex: 1, data: dates, axisLabel: { show: false }, axisTick: { show: false }, axisLine: { onZero: false }, splitLine: { show: false } },
      { type: 'category', gridIndex: 2, data: dates, axisLabel: { show: false }, axisTick: { show: false }, axisLine: { onZero: false }, splitLine: { show: false } }
    ],
    yAxis: [
      { scale: true, splitArea: { show: true } },
      { gridIndex: 1, axisLabel: { show: false }, splitLine: { show: false } },
      { gridIndex: 2, splitNumber: 3, splitLine: { show: false } }
    ],
    dataZoom: [
      { type: 'inside', xAxisIndex: [0, 1, 2], start: startPct, end: 100 },
      { type: 'slider', xAxisIndex: [0, 1, 2], top: '94%', height: 16, start: startPct, end: 100 }
    ],
    series: [
      {
        name: 'K线', type: 'candlestick', data: ohlc,
        itemStyle: { color: '#ec0000', color0: '#00da3c', borderColor: '#ec0000', borderColor0: '#00da3c' }
      },
      ...maDefs.map(m => ({
        name: m.name, type: 'line', data: calcMa(closes, m.days), showSymbol: false,
        lineStyle: { width: 1, color: m.color }, itemStyle: { color: m.color }
      })),
      {
        name: '成交量', type: 'bar', xAxisIndex: 1, yAxisIndex: 1, data: volumes,
        itemStyle: { color: (p) => ups[p.dataIndex] ? '#ec0000' : '#00da3c' }
      },
      {
        name: 'MACD', type: 'bar', xAxisIndex: 2, yAxisIndex: 2, data: hist,
        itemStyle: { color: (p) => p.value >= 0 ? '#ec0000' : '#00da3c' }
      },
      { name: 'DIF', type: 'line', xAxisIndex: 2, yAxisIndex: 2, data: dif, showSymbol: false, lineStyle: { width: 1, color: '#ff9800' }, itemStyle: { color: '#ff9800' } },
      { name: 'DEA', type: 'line', xAxisIndex: 2, yAxisIndex: 2, data: dea, showSymbol: false, lineStyle: { width: 1, color: '#2196f3' }, itemStyle: { color: '#2196f3' } }
    ]
  }, true)
  klineChart.resize()
}

const renderFlowChart = (data) => {
  if (!flowChartRef.value) return
  if (flowChart && flowChart.getDom() !== flowChartRef.value) {
    flowChart.dispose()
    flowChart = null
  }
  if (!flowChart) flowChart = echarts.init(flowChartRef.value)
  const sorted = [...data].sort((a, b) => new Date(a.tradeDate) - new Date(b.tradeDate))
  const dates = sorted.map(d => d.tradeDate)
  const val = (fn) => sorted.map(d => Number(fn(d) || 0))
  flowChart.setOption({
    backgroundColor: '#fff',
    tooltip: { trigger: 'axis' },
    legend: { data: ['主力净流入', '超大单', '大单', '中单', '小单'], top: 0 },
    grid: { left: 60, right: 30, top: 36, bottom: 40 },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', name: '万元' },
    series: [
      { name: '主力净流入', type: 'bar', data: val(d => d.mainNetInflow), itemStyle: { color: (p) => p.value >= 0 ? '#ec0000' : '#00da3c' } },
      { name: '超大单', type: 'line', showSymbol: false, data: val(d => d.superLargeNet), lineStyle: { width: 1.5, color: '#f5222d' }, itemStyle: { color: '#f5222d' } },
      { name: '大单', type: 'line', showSymbol: false, data: val(d => d.largeNet), lineStyle: { width: 1.5, color: '#fa8c16' }, itemStyle: { color: '#fa8c16' } },
      { name: '中单', type: 'line', showSymbol: false, data: val(d => d.mediumNet), lineStyle: { width: 1.5, color: '#2196f3' }, itemStyle: { color: '#2196f3' } },
      { name: '小单', type: 'line', showSymbol: false, data: val(d => d.smallNet), lineStyle: { width: 1.5, color: '#52c41a' }, itemStyle: { color: '#52c41a' } }
    ]
  }, true)
  flowChart.resize()
}

// 技术面：基于日K计算 MA/MACD/KDJ/RSI 与简单信号
const techView = computed(() => {
  const data = klineRaw.value
  if (!data || data.length < 30) return null
  const n = data.length
  const closes = data.map(d => Number(d.closePrice))
  const highs = data.map(d => Number(d.highPrice))
  const lows = data.map(d => Number(d.lowPrice))
  const last = closes[n - 1]

  const ma = (days) => {
    if (n < days) return null
    let s = 0
    for (let i = n - days; i < n; i++) s += closes[i]
    return Number((s / days).toFixed(2))
  }
  const ma5 = ma(5), ma10 = ma(10), ma20 = ma(20), ma60 = ma(60)

  const ema12 = calcEma(closes, 12)
  const ema26 = calcEma(closes, 26)
  const difArr = ema12.map((v, i) => v - ema26[i])
  const deaArr = calcEma(difArr, 9)
  const dif = Number(difArr[n - 1].toFixed(3))
  const dea = Number(deaArr[n - 1].toFixed(3))
  const hist = Number((2 * (difArr[n - 1] - deaArr[n - 1])).toFixed(3))
  const prevDiff = difArr[n - 2] - deaArr[n - 2]

  let k = 50, dd = 50
  for (let i = 0; i < n; i++) {
    const hh = Math.max(...highs.slice(Math.max(0, i - 8), i + 1))
    const ll = Math.min(...lows.slice(Math.max(0, i - 8), i + 1))
    const rsv = hh === ll ? 50 : ((closes[i] - ll) / (hh - ll)) * 100
    k = (2 / 3) * k + (1 / 3) * rsv
    dd = (2 / 3) * dd + (1 / 3) * k
  }
  const j = 3 * k - 2 * dd

  const rsi = (days) => {
    if (n <= days) return null
    let up = 0, dn = 0
    for (let i = n - days; i < n; i++) {
      const ch = closes[i] - closes[i - 1]
      if (ch >= 0) up += ch
      else dn -= ch
    }
    if (up + dn === 0) return 50
    return Number(((100 * up) / (up + dn)).toFixed(2))
  }
  const rsi6 = rsi(6), rsi12 = rsi(12), rsi24 = rsi(24)

  const signals = []
  if (ma5 && ma10 && ma20 && ma60) {
    if (ma5 > ma10 && ma10 > ma20 && ma20 > ma60) signals.push({ text: '均线多头排列', type: 'danger' })
    else if (ma5 < ma10 && ma10 < ma20 && ma20 < ma60) signals.push({ text: '均线空头排列', type: 'success' })
    if (last > ma5 && last > ma10 && last > ma20) signals.push({ text: '站上短期均线', type: 'danger' })
    else if (last < ma5 && last < ma10 && last < ma20) signals.push({ text: '跌破短期均线', type: 'success' })
  }
  if (dif > dea) {
    signals.push({ text: prevDiff <= 0 ? 'MACD金叉' : 'MACD多头', type: prevDiff <= 0 ? 'danger' : 'info' })
  } else {
    signals.push({ text: prevDiff >= 0 ? 'MACD死叉' : 'MACD空头', type: prevDiff >= 0 ? 'success' : 'info' })
  }
  if (k > 80) signals.push({ text: 'KDJ超买', type: 'warning' })
  else if (k < 20) signals.push({ text: 'KDJ超卖', type: 'warning' })
  if (rsi6 != null && rsi6 > 80) signals.push({ text: 'RSI6超买', type: 'warning' })
  else if (rsi6 != null && rsi6 < 20) signals.push({ text: 'RSI6超卖', type: 'warning' })

  return {
    last, ma5, ma10, ma20, ma60, dif, dea, hist,
    k: Number(k.toFixed(2)), d: Number(dd.toFixed(2)), j: Number(j.toFixed(2)),
    rsi6, rsi12, rsi24, signals
  }
})

const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除 ${row.stockName}（${row.stockCode}）的全部数据？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await DeleteStockDataByCode(row.stockCode)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchData()
        if (selectedStock.value?.stockCode === row.stockCode) {
          selectedStock.value = null
          klineRaw.value = []
          financeData.value = []
        }
      }
    } catch (e) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchData()
})


onBeforeUnmount(() => {
  if (klineChart) klineChart.dispose()
  if (flowChart) flowChart.dispose()
})
</script>

<style scoped>
.stock-management {
  padding: 16px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 16px 24px;
  border-radius: 12px;
  margin-bottom: 16px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.page-header h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
}

.fetch-area {
  display: flex;
  gap: 12px;
  align-items: center;
}

.query-section {
  background: white;
  padding: 16px 20px;
  border-radius: 10px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.list-section {
  background: white;
  padding: 16px;
  border-radius: 10px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.pagination-area {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

.detail-section {
  margin-top: 16px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.info-card {
  background: white;
  border-radius: 10px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: transform 0.2s;
}

.info-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.full-width {
  grid-column: 1 / -1;
}

.name-card {
  grid-column: span 1;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #909399;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.name-card .card-title {
  color: rgba(255, 255, 255, 0.9);
  border-bottom-color: rgba(255, 255, 255, 0.2);
}

.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.stock-name {
  font-size: 20px;
  font-weight: 700;
}

.industry-tag {
  font-size: 12px;
  opacity: 0.8;
  margin-bottom: 12px;
}

.list-date {
  font-size: 12px;
  opacity: 0.85;
  margin-top: 8px;
}

.price-display {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.price-display .big {
  font-size: 28px;
  font-weight: 700;
}

.price-up {
  color: #ec0000;
}

.price-down {
  color: #00da3c;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.metric-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.metric-item .label {
  font-size: 12px;
  color: #909399;
}

.metric-item .value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.company-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  font-size: 13px;
  line-height: 1.6;
  color: #606266;
}

.info-row .label {
  color: #909399;
  font-weight: 600;
}

.detail-tabs {
  background: white;
  border-radius: 10px;
  padding: 8px 16px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 16px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart-container {
  width: 100%;
  height: 400px;
}

.kline-height {
  height: 540px;
}

.signal-area {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.tech-grid {
  grid-template-columns: repeat(4, 1fr);
}

:deep(.el-table) {
  cursor: pointer;
}

:deep(.el-table__row:hover > td) {
  background-color: #f0f7ff !important;
}
</style>
