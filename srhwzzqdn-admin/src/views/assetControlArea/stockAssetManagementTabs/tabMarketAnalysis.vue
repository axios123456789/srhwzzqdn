<template>
  <div class="market-analysis">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>
        <el-icon><TrendCharts /></el-icon>
        市场分析
      </h1>
      <div class="header-right">
        <el-tag v-if="data" type="warning" size="large" effect="dark">{{ data.marketPhase }}</el-tag>
        <el-tag v-if="data && data.quoteTime" type="info" size="large">行情时间 {{ data.quoteTime }}</el-tag>
        <el-switch v-model="autoRefresh" active-text="自动刷新" class="auto-switch" />
        <el-button :loading="rtLoading" @click="loadRealtime">
          <el-icon><Refresh /></el-icon>
          实时刷新
        </el-button>
        <el-button type="primary" :loading="aiLoading" @click="loadAi">
          <el-icon><MagicStick /></el-icon>
          AI深度分析
        </el-button>
      </div>
    </div>

    <!-- 功能说明 -->
    <div class="rule-section">
      <el-alert type="info" :closable="false" show-icon>
        <template #title>
          实时回答：市场在做什么/情绪如何、核心消息面、资金从哪流到哪、增量资金态度、主线板块与龙头（新手可买标注00/60开头）、主线是一日游还是长期。竞价阶段（9:15-9:25）自动取竞价撮合数据，盘中取实时数据；AI深度分析结果按交易日落库，同日重分析自动覆盖。
        </template>
      </el-alert>
    </div>

    <!-- 指数行情条 -->
    <div class="index-row" v-if="data">
      <div
        class="index-card"
        v-for="idx in data.indexes"
        :key="idx.code"
      >
        <div class="idx-name">{{ idx.name }}</div>
        <div class="idx-close" :class="pctClass(idx.changePct)">{{ fmtNum(idx.close) }}</div>
        <div class="idx-pct" :class="pctClass(idx.changePct)">
          {{ signedNum(idx.changePct) }}% ({{ signedNum(idx.changeAmt) }})
        </div>
        <div class="idx-amount">成交 {{ fmtNum(idx.amountYi) }} 亿</div>
      </div>
      <div class="index-card amount-card">
        <div class="idx-name">两市成交额</div>
        <div class="idx-close strong">{{ fmtNum(data.totalAmountYi) }} 亿</div>
        <div class="idx-pct" :class="amountClass">
          {{ amountCompareText }}
        </div>
        <div class="idx-amount">昨日全天 {{ fmtNum(data.prevAmountYi) }} 亿（{{ data.prevAmountDate || '-' }}）</div>
      </div>
    </div>
    <el-skeleton v-else :rows="3" animated class="loading-skeleton" />

    <!-- 情绪 / 涨跌分布 / 涨停梯队 -->
    <el-row :gutter="12" class="section-row" v-if="data">
      <el-col :span="8">
        <el-card shadow="hover" class="panel-card">
          <template #header>
            <span class="card-title">市场情绪温度计</span>
          </template>
          <div class="sentiment-wrap">
            <el-progress
              type="dashboard"
              :percentage="data.sentimentScore"
              :width="150"
              :stroke-width="14"
              :color="sentimentColors"
            >
              <template #default>
                <div class="sentiment-score">{{ data.sentimentScore }}</div>
                <div class="sentiment-level">{{ data.sentimentLevel }}</div>
              </template>
            </el-progress>
          </div>
          <div class="phase-note">{{ data.phaseNote }}</div>
          <div class="market-status">{{ data.marketStatus }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="panel-card">
          <template #header>
            <span class="card-title">涨跌分布与涨跌停</span>
          </template>
          <div class="updown-bar">
            <div class="up-part" :style="{ width: upPct + '%' }">{{ data.upCount }}</div>
            <div class="flat-part" :style="{ width: flatPct + '%' }">{{ data.flatCount }}</div>
            <div class="down-part" :style="{ width: downPct + '%' }">{{ data.downCount }}</div>
          </div>
          <div class="stat-chips">
            <div class="chip red">涨停 {{ data.limitUpCount }}</div>
            <div class="chip green">跌停 {{ data.limitDownCount }}</div>
            <div class="chip orange">炸板 {{ data.zhaBanCount }}（{{ data.zhaBanRate }}%）</div>
            <div class="chip purple">最高 {{ data.maxLianban }} 连板</div>
          </div>
          <div class="updown-tip" v-if="data.poolQdate">涨跌停池数据日期：{{ data.poolQdate }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="panel-card">
          <template #header>
            <span class="card-title">连板空间梯队</span>
          </template>
          <div class="tier-list">
            <div v-for="tier in data.lianbanTiers" :key="tier.lianban" class="tier-item">
              <el-tag type="danger" size="small" class="tier-tag">{{ tier.lianban }}板</el-tag>
              <span class="tier-count">×{{ tier.count }}</span>
              <span class="tier-stocks">{{ tier.stocks }}</span>
            </div>
            <el-empty v-if="!data.lianbanTiers || !data.lianbanTiers.length" description="暂无2板以上梯队" :image-size="50" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 资金流动 -->
    <el-card shadow="hover" class="flow-card" v-if="data">
      <template #header>
        <span class="card-title">资金流动（全市场，单位：亿）</span>
      </template>
      <div class="flow-row">
        <div class="flow-item" :class="flowClass(data.mainNetInflowYi)">
          <div class="flow-label">主力净流入</div>
          <div class="flow-value">{{ signedNum(data.mainNetInflowYi) }}</div>
          <div class="flow-sub">占成交 {{ data.mainNetPct }}%</div>
        </div>
        <div class="flow-item" :class="flowClass(data.ultraNetInflowYi)">
          <div class="flow-label">超大单（机构）</div>
          <div class="flow-value">{{ signedNum(data.ultraNetInflowYi) }}</div>
        </div>
        <div class="flow-item" :class="flowClass(data.bigNetInflowYi)">
          <div class="flow-label">大单（游资）</div>
          <div class="flow-value">{{ signedNum(data.bigNetInflowYi) }}</div>
        </div>
        <div class="flow-item" :class="flowClass(data.midNetInflowYi)">
          <div class="flow-label">中单（中户）</div>
          <div class="flow-value">{{ signedNum(data.midNetInflowYi) }}</div>
        </div>
        <div class="flow-item" :class="flowClass(data.smallNetInflowYi)">
          <div class="flow-label">小单（散户）</div>
          <div class="flow-value">{{ signedNum(data.smallNetInflowYi) }}</div>
        </div>
        <div class="flow-item margin-item">
          <div class="flow-label">两融余额（{{ data.marginDate || 'T-1' }}）</div>
          <div class="flow-value">{{ fmtNum(data.marginBalanceYi) }}</div>
          <div class="flow-sub" :class="data.marginNetBuyYi >= 0 ? 'price-up' : 'price-down'">
            融资净买入 {{ signedNum(data.marginNetBuyYi) }} 亿
          </div>
        </div>
      </div>
      <div class="flow-conclusion" v-if="moneyConclusion">
        <el-tag :type="data.mainNetInflowYi >= 0 ? 'danger' : 'success'" effect="dark" size="large">
          {{ moneyConclusion }}
        </el-tag>
      </div>
      <div class="north-note">{{ data.northNote }}</div>
    </el-card>

    <!-- 板块热力：主拉 vs 主弃 -->
    <el-row :gutter="12" class="section-row" v-if="data">
      <el-col :span="12">
        <el-card shadow="hover" class="panel-card">
          <template #header>
            <span class="card-title sector-hot">市场在拉（主力净流入 TOP10）</span>
          </template>
          <div class="sector-list">
            <div v-for="(s, i) in data.hotSectors.slice(0, 10)" :key="i" class="sector-item">
              <span class="sector-rank">{{ i + 1 }}</span>
              <el-tag size="small" :type="s.type === '概念' ? 'warning' : 'primary'">{{ s.type }}</el-tag>
              <span class="sector-name">{{ s.name }}</span>
              <span class="sector-pct price-up">{{ signedNum(s.changePct) }}%</span>
              <span class="sector-inflow price-up">+{{ fmtNum(s.mainInflowYi) }}亿</span>
              <span class="sector-leader">领涨 {{ s.leaderName }} {{ signedNum(s.leaderPct) }}%</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="panel-card">
          <template #header>
            <span class="card-title sector-cold">市场在弃（主力净流出 TOP10）</span>
          </template>
          <div class="sector-list">
            <div v-for="(s, i) in data.coldSectors.slice(0, 10)" :key="i" class="sector-item">
              <span class="sector-rank">{{ i + 1 }}</span>
              <el-tag size="small" :type="s.type === '概念' ? 'warning' : 'primary'">{{ s.type }}</el-tag>
              <span class="sector-name">{{ s.name }}</span>
              <span class="sector-pct" :class="pctClass(s.changePct)">{{ signedNum(s.changePct) }}%</span>
              <span class="sector-inflow price-down">{{ fmtNum(s.mainInflowYi) }}亿</span>
              <span class="sector-leader">领涨 {{ s.leaderName }} {{ signedNum(s.leaderPct) }}%</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 市场主线与龙头股 -->
    <el-card shadow="hover" class="mainline-card" v-if="data">
      <template #header>
        <span class="card-title">市场主线与龙头股</span>
      </template>
      <div class="mainline-conclusion">
        <div class="mainline-line">
          <span class="label">主线初判：</span>{{ data.mainLine }}
        </div>
        <div class="mainline-sustain">
          <el-tag :type="sustainTagType" size="large" effect="dark">{{ data.mainLineSustain }}</el-tag>
          <span class="sustain-reason" v-for="(r, i) in data.sustainReasons" :key="i">{{ r }}</span>
        </div>
        <div class="zt-aggregation" v-if="data.ztIndustryAggregation && data.ztIndustryAggregation.length">
          涨停聚集行业：
          <el-tag
            v-for="a in data.ztIndustryAggregation"
            :key="a.sector"
            type="danger"
            size="small"
            class="zt-ind-tag"
          >
            {{ a.sector }} ×{{ a.ztCount }}
          </el-tag>
        </div>
      </div>
      <el-table :data="data.leaders || []" border stripe size="small" class="leader-table">
        <el-table-column label="代码" prop="code" width="85" align="center" />
        <el-table-column label="名称" prop="name" width="110" align="center" />
        <el-table-column label="最新价" width="80" align="right">
          <template #default="{ row }">
            {{ row.price ? fmtNum(row.price) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="涨幅%" width="85" align="right">
          <template #default="{ row }">
            <span :class="pctClass(row.pct)">{{ signedNum(row.pct) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="连板" width="70" align="center">
          <template #default="{ row }">
            <span v-if="row.lianban >= 2" class="lianban-num">{{ row.lianban }}板</span>
            <span v-else-if="row.lianban === 1">首板</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="身份" prop="source" width="100" align="center" />
        <el-table-column label="所属板块" prop="sector" min-width="110" align="center" />
        <el-table-column label="几板" prop="ztStat" width="90" align="center">
          <template #default="{ row }">{{ row.ztStat || '-' }}</template>
        </el-table-column>
        <el-table-column label="新手可买" width="95" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.beginner" type="success" size="small">00/60主板</el-tag>
            <el-tag v-else type="info" size="small">建议观望</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div class="beginner-note">
        新手可买 = 代码 00/60 开头的沪深主板股（±10%涨跌停，无20cm风险）；30/68 开头为创业板/科创板，波动大不建议新手参与。仅为客观属性标注，不构成买入建议。
      </div>
    </el-card>

    <!-- 消息面 -->
    <el-card shadow="hover" class="news-card" v-if="data">
      <template #header>
        <span class="card-title">核心消息面（7×24财经快讯）</span>
      </template>
      <div class="news-list">
        <div v-for="(n, i) in data.news" :key="i" class="news-item">
          <span class="news-time">{{ n.time }}</span>
          <el-tooltip :content="n.summary" placement="top" :disabled="!n.summary">
            <span class="news-title">{{ n.title }}</span>
          </el-tooltip>
          <span class="news-codes" v-if="n.relatedCodes && n.relatedCodes.length">
            相关：{{ n.relatedCodes.join(' / ') }}
          </span>
        </div>
      </div>
    </el-card>

    <!-- AI 深度分析 -->
    <el-card shadow="hover" class="ai-card" v-if="aiVisible">
      <template #header>
        <div class="ai-header">
          <span class="card-title">AI 深度分析</span>
          <span class="ai-source">{{ aiSourceText }}</span>
        </div>
      </template>
      <div class="ai-section">
        <div class="ai-section-title">一、市场在做什么（情绪综合）</div>
        <div class="md-body" v-html="mdToHtml(ai.aiMarketStatus)"></div>
      </div>
      <div class="ai-section">
        <div class="ai-section-title">二、催动市场的核心消息面与逻辑</div>
        <div class="md-body" v-html="mdToHtml(ai.aiLogic)"></div>
      </div>
      <div class="ai-section">
        <div class="ai-section-title">三、资金流动：从哪流到哪、增量还是退场</div>
        <div class="md-body" v-html="mdToHtml(ai.aiMoneyFlow)"></div>
      </div>
      <div class="ai-section">
        <div class="ai-section-title">四、主线板块与持续性（一日游 vs 长期）</div>
        <div class="md-body" v-html="mdToHtml(ai.aiMainline)"></div>
      </div>
      <div class="ai-section">
        <div class="ai-section-title">五、明日展望</div>
        <div class="md-body" v-html="mdToHtml(ai.aiOutlook)"></div>
      </div>
      <div class="ai-section" v-if="ai.aiHistoryReview">
        <div class="ai-section-title">六、历史准确性复盘（规避过往误判）</div>
        <div class="md-body" v-html="mdToHtml(ai.aiHistoryReview)"></div>
      </div>
      <div class="ai-disclaimer">
        以上 AI 分析基于实时盘面数据自动生成，仅供参考，不构成投资建议，据此操作风险自负。
      </div>
    </el-card>

    <!-- 历史分析记录 -->
    <el-card shadow="hover" class="history-card">
      <template #header>
        <div class="ai-header">
          <span class="card-title">历史分析记录（按交易日落库）</span>
          <el-button size="small" type="primary" plain :loading="reviewLoading" @click="loadHistoryReview">
            <el-icon><Clock /></el-icon>
            AI历史准确性复盘
          </el-button>
        </div>
      </template>
      <el-table :data="historyList" border stripe size="small" v-loading="historyLoading">
        <el-table-column label="交易日" prop="marketDate" width="95" align="center" />
        <el-table-column label="分析时间" prop="analysisTime" width="80" align="center" />
        <el-table-column label="阶段" prop="marketPhase" width="85" align="center" />
        <el-table-column label="情绪" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="sentimentTagType(row.sentimentScore)" size="small">
              {{ row.sentimentScore }}分 {{ row.sentimentLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="上证%" width="75" align="right">
          <template #default="{ row }">
            <span :class="pctClass(row.shChangePct)">{{ signedNum(row.shChangePct) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="涨停" prop="limitUpCount" width="60" align="center" />
        <el-table-column label="主力净流入(亿)" width="115" align="right">
          <template #default="{ row }">
            <span :class="row.mainNetInflowYi >= 0 ? 'price-up' : 'price-down'">{{ signedNum(row.mainNetInflowYi) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="市场主线" prop="mainLine" min-width="200" show-overflow-tooltip />
        <el-table-column label="持续性初判" prop="mainLineSustainability" width="200" show-overflow-tooltip />
        <el-table-column label="结论" prop="marketStatus" min-width="240" show-overflow-tooltip />
      </el-table>
    </el-card>

    <!-- AI历史复盘弹窗 -->
    <el-dialog v-model="reviewDialogVisible" title="AI 历史准确性复盘（展望 vs 实际）" width="720px" top="6vh">
      <div v-if="reviewData && reviewData.hasData" class="md-body" v-html="mdToHtml(reviewData.review)"></div>
      <el-empty v-else :description="reviewData ? reviewData.message : '加载中...'" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { TrendCharts, Refresh, MagicStick, Clock } from '@element-plus/icons-vue'
import { GetRealtimeAnalysis, AnalyzeMarketWithAi, GetTodayMarketAnalysis, GetMarketHistoryList, GetMarketHistoryReview } from '@/api/stockAsset'

const data = ref(null)
const ai = ref(null)
const aiSource = ref('') // ai：实时分析生成；saved：当日已落库恢复
const rtLoading = ref(false)
const aiLoading = ref(false)
const autoRefresh = ref(false)
let autoTimer = null

const historyList = ref([])
const historyLoading = ref(false)
const reviewDialogVisible = ref(false)
const reviewLoading = ref(false)
const reviewData = ref(null)

// ---------- 数据加载 ----------
const loadRealtime = async () => {
  rtLoading.value = true
  try {
    const res = await GetRealtimeAnalysis()
    if (res.code === 200) {
      data.value = res.data
      // 保留已有的AI结果（若来自当日落库或本次会话），避免刷新覆盖
      if (!ai.value) {
        await restoreSavedAi()
      }
    } else {
      ElMessage.error(res.message || '获取实时市场分析失败')
    }
  } catch (e) {
    ElMessage.error('获取实时市场分析失败：' + (e.message || '网络异常'))
  } finally {
    rtLoading.value = false
  }
}

const restoreSavedAi = async () => {
  try {
    const res = await GetTodayMarketAnalysis()
    if (res.code === 200 && res.data) {
      const d = res.data
      const hasAi = d.aiMarketStatus || d.aiLogic || d.aiMainline
      if (hasAi) {
        ai.value = {
          aiMarketStatus: d.aiMarketStatus,
          aiLogic: d.aiLogic,
          aiMoneyFlow: d.aiMoneyFlow,
          aiMainline: d.aiMainline,
          aiOutlook: d.aiOutlook,
          aiHistoryReview: d.aiHistoryReview
        }
        aiSource.value = 'saved'
        historyList.value = await fetchHistory()
      }
    }
  } catch (e) {
    // 静默失败：落库恢复只是辅助
  }
}

const loadAi = async () => {
  aiLoading.value = true
  try {
    const res = await AnalyzeMarketWithAi()
    if (res.code === 200) {
      data.value = res.data
      ai.value = res.data.ai
      aiSource.value = res.data.saved ? 'ai-saved' : 'ai'
      ElMessage.success(res.data.saved ? 'AI深度分析完成并已按天落库' : 'AI深度分析完成')
      historyList.value = await fetchHistory()
    } else {
      ElMessage.error(res.message || 'AI深度分析失败')
    }
  } catch (e) {
    ElMessage.error('AI深度分析失败：' + (e.message || '网络异常，AI可能耗时较长请重试'))
  } finally {
    aiLoading.value = false
  }
}

const fetchHistory = async () => {
  try {
    const res = await GetMarketHistoryList(10)
    return res.code === 200 ? res.data : []
  } catch (e) {
    return []
  }
}

const loadHistoryReview = async () => {
  reviewLoading.value = true
  reviewDialogVisible.value = true
  try {
    const res = await GetMarketHistoryReview()
    if (res.code === 200) {
      reviewData.value = res.data
    } else {
      reviewData.value = { hasData: false, message: res.message || '加载失败' }
    }
  } catch (e) {
    reviewData.value = { hasData: false, message: '加载失败：' + (e.message || '网络异常') }
  } finally {
    reviewLoading.value = false
  }
}

onMounted(async () => {
  historyLoading.value = true
  try {
    historyList.value = await fetchHistory()
  } finally {
    historyLoading.value = false
  }
  await loadRealtime()
})

onBeforeUnmount(() => {
  if (autoTimer) clearInterval(autoTimer)
})

// 自动刷新开关（60秒，仅在交易阶段有效）
const TRADE_PHASES = ['集合竞价', '竞价撮合', '早盘', '盘中', '尾盘']
watch(autoRefresh, on => {
  if (autoTimer) { clearInterval(autoTimer); autoTimer = null }
  if (on) {
    autoTimer = setInterval(() => {
      if (data.value && TRADE_PHASES.includes(data.value.marketPhase) && !rtLoading.value && !aiLoading.value) {
        loadRealtime()
      }
    }, 60000)
  }
})

// ---------- 展示辅助 ----------
const aiVisible = computed(() => !!ai.value)
const aiSourceText = computed(() => {
  if (aiSource.value === 'saved') return '来源：当日已落库分析（点击AI深度分析可刷新）'
  if (aiSource.value === 'ai-saved') return '来源：AI实时分析，已按天落库'
  if (aiSource.value === 'ai') return '来源：AI实时分析'
  return ''
})

const upPct = computed(() => {
  if (!data.value) return 0
  const total = data.value.upCount + data.value.downCount + data.value.flatCount
  return total > 0 ? (data.value.upCount / total) * 100 : 0
})
const downPct = computed(() => {
  if (!data.value) return 0
  const total = data.value.upCount + data.value.downCount + data.value.flatCount
  return total > 0 ? (data.value.downCount / total) * 100 : 0
})
const flatPct = computed(() => Math.max(0, 100 - upPct.value - downPct.value))

const amountCompareText = computed(() => {
  if (!data.value) return ''
  const r = Number(data.value.amountRatioPct)
  if (!r || r <= 0) return '量能对比暂缺'
  const phase = data.value.marketPhase
  if (['已收盘', '非交易时段'].includes(phase)) {
    return r >= 100 ? `较昨日放量 ${fmtNum(r - 100)}%` : `较昨日缩量 ${fmtNum(100 - r)}%`
  }
  return `已达昨日全天 ${fmtNum(r)}%`
})
const amountClass = computed(() => {
  const r = Number(data.value?.amountRatioPct || 0)
  if (r >= 100) return Number(data.value?.shChangePct) >= 0 ? 'price-up' : 'price-down'
  return ''
})

const moneyConclusion = computed(() => {
  if (!data.value) return ''
  const main = Number(data.value.mainNetInflowYi || 0)
  const ultra = Number(data.value.ultraNetInflowYi || 0)
  const marginNet = Number(data.value.marginNetBuyYi || 0)
  const parts = []
  parts.push(main >= 0 ? `主力净流入 ${fmtNum(main)} 亿，机构资金进攻中` : `主力净流出 ${fmtNum(-main)} 亿，机构资金防守`)
  if (ultra >= 0) parts.push('超大单进场')
  else parts.push('超大单撤退')
  if (marginNet > 0) parts.push('杠杆资金加仓意愿强')
  else parts.push('杠杆资金谨慎')
  return parts.join('；')
})

const sustainTagType = computed(() => {
  const s = data.value?.mainLineSustain || ''
  if (s.includes('延续')) return 'danger'
  if (s.includes('一日游')) return 'warning'
  return 'info'
})

const sentimentColors = [
  { color: '#5470c6', percentage: 20 },
  { color: '#91cc75', percentage: 40 },
  { color: '#fac858', percentage: 60 },
  { color: '#ee6666', percentage: 80 },
  { color: '#d81e06', percentage: 100 }
]

const pctClass = v => Number(v) >= 0 ? 'price-up' : 'price-down'
const flowClass = v => Number(v) >= 0 ? 'flow-in' : 'flow-out'
const sentimentTagType = score => {
  if (score == null) return 'info'
  if (score < 20) return 'info'
  if (score < 40) return 'success'
  if (score < 60) return 'warning'
  if (score < 80) return 'danger'
  return 'danger'
}

const fmtNum = v => (v == null || v === '' ? '-' : Number(v).toLocaleString('zh-CN', { maximumFractionDigits: 2 }))
const signedNum = v => {
  if (v == null || v === '') return '-'
  const n = Number(v)
  return (n > 0 ? '+' : '') + n.toFixed(2)
}

// 极简 Markdown 渲染：先转义再处理标题/加粗/列表，防注入
const mdToHtml = md => {
  if (!md) return ''
  const esc = s =>
    s
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
  const inline = s => s.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  const lines = esc(md).split(/\r?\n/)
  let html = ''
  let inList = false
  for (const raw of lines) {
    const line = raw.trim()
    if (!line) continue
    if (/^#{1,4}\s/.test(line)) {
      if (inList) { html += '</ul>'; inList = false }
      html += '<h4>' + inline(line.replace(/^#{1,4}\s*/, '')) + '</h4>'
    } else if (/^[-*]\s/.test(line) || /^\d+\.\s/.test(line)) {
      if (!inList) { html += '<ul>'; inList = true }
      html += '<li>' + inline(line.replace(/^[-*]\s*/, '').replace(/^\d+\.\s*/, '')) + '</li>'
    } else {
      if (inList) { html += '</ul>'; inList = false }
      html += '<p>' + inline(line) + '</p>'
    }
  }
  if (inList) html += '</ul>'
  return html
}
</script>

<style scoped>
.market-analysis {
  padding: 12px 15px 30px;
  background: linear-gradient(180deg, #f5f7fa 0%, #eef1f6 100%);
  min-height: 100%;
}

/* 头部 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.page-header h1 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 22px;
  color: #1f2d3d;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}
.auto-switch {
  margin-right: 4px;
}
.loading-skeleton {
  padding: 10px 0;
}

/* 说明 */
.rule-section {
  margin-bottom: 12px;
}

/* 指数行情条 */
.index-row {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  margin-bottom: 12px;
}
.index-card {
  background: #fff;
  border-radius: 10px;
  padding: 14px 16px;
  box-shadow: 0 2px 8px rgba(31, 45, 61, 0.06);
  border-top: 3px solid #409eff;
}
.index-card.amount-card {
  border-top-color: #e6a23c;
}
.idx-name {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}
.idx-close {
  font-size: 24px;
  font-weight: 700;
  font-family: 'DIN Alternate', 'Helvetica Neue', sans-serif;
}
.idx-close.strong {
  font-size: 22px;
}
.idx-pct {
  font-size: 14px;
  margin-top: 2px;
}
.idx-amount {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.price-up { color: #e6262e; }
.price-down { color: #1cad62; }

/* 通用面板 */
.section-row {
  margin-bottom: 12px;
}
.panel-card {
  border-radius: 10px;
}
.card-title {
  font-weight: 700;
  font-size: 15px;
  color: #1f2d3d;
}
.card-title.sector-hot { color: #c0392b; }
.card-title.sector-cold { color: #16875a; }

/* 情绪温度计 */
.sentiment-wrap {
  display: flex;
  justify-content: center;
  padding: 6px 0;
}
.sentiment-score {
  font-size: 34px;
  font-weight: 800;
  color: #1f2d3d;
  line-height: 1.1;
}
.sentiment-level {
  font-size: 15px;
  color: #909399;
  margin-top: 2px;
}
.phase-note {
  text-align: center;
  color: #e6a23c;
  font-size: 12px;
  margin: 8px 0 6px;
}
.market-status {
  font-size: 13px;
  line-height: 1.7;
  color: #303133;
  background: #f8f9fb;
  border-radius: 8px;
  padding: 8px 10px;
}

/* 涨跌分布 */
.updown-bar {
  display: flex;
  height: 34px;
  border-radius: 8px;
  overflow: hidden;
  margin: 10px 0 14px;
  font-size: 12px;
  color: #fff;
  line-height: 34px;
  text-align: center;
}
.up-part { background: linear-gradient(90deg, #e6262e, #f0655f); min-width: 40px; }
.flat-part { background: #c0c4cc; min-width: 24px; }
.down-part { background: linear-gradient(90deg, #35c07f, #1cad62); min-width: 40px; }
.stat-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.chip {
  flex: 1 1 40%;
  text-align: center;
  border-radius: 8px;
  padding: 8px 4px;
  font-weight: 700;
  color: #fff;
  font-size: 13px;
}
.chip.red { background: linear-gradient(135deg, #e6262e, #f0655f); }
.chip.green { background: linear-gradient(135deg, #1cad62, #35c07f); }
.chip.orange { background: linear-gradient(135deg, #e6a23c, #f3c27a); }
.chip.purple { background: linear-gradient(135deg, #7c50e6, #a583f0); }
.updown-tip {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
}

/* 连板梯队 */
.tier-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.tier-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}
.tier-tag { flex-shrink: 0; }
.tier-count {
  color: #c0392b;
  font-weight: 700;
  flex-shrink: 0;
}
.tier-stocks {
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 资金流动 */
.flow-card {
  border-radius: 10px;
  margin-bottom: 12px;
}
.flow-row {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 10px;
}
.flow-item {
  border-radius: 10px;
  padding: 12px 10px;
  text-align: center;
  background: #f8f9fb;
}
.flow-item.flow-in { background: linear-gradient(180deg, #fff3f3, #ffe3e3); }
.flow-item.flow-out { background: linear-gradient(180deg, #f0fbf5, #dcf5e8); }
.flow-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}
.flow-value {
  font-size: 20px;
  font-weight: 800;
  font-family: 'DIN Alternate', 'Helvetica Neue', sans-serif;
  color: #1f2d3d;
}
.flow-sub {
  font-size: 12px;
  margin-top: 4px;
  color: #909399;
}
.flow-conclusion {
  margin-top: 12px;
  text-align: center;
}
.north-note {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
  text-align: center;
}

/* 板块热力 */
.sector-list {
  display: flex;
  flex-direction: column;
  gap: 7px;
}
.sector-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  padding: 3px 6px;
  border-radius: 6px;
  background: #fafbfc;
}
.sector-rank {
  width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  border-radius: 4px;
  background: #ebeef5;
  color: #606266;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}
.sector-name {
  font-weight: 600;
  width: 88px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.sector-pct { width: 62px; text-align: right; }
.sector-inflow { width: 82px; text-align: right; font-weight: 700; }
.sector-leader {
  flex: 1;
  color: #909399;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 主线与龙头 */
.mainline-card {
  border-radius: 10px;
  margin-bottom: 12px;
}
.mainline-conclusion {
  margin-bottom: 12px;
}
.mainline-line {
  font-size: 15px;
  font-weight: 700;
  color: #1f2d3d;
  margin-bottom: 8px;
}
.mainline-line .label { color: #c0392b; }
.mainline-sustain {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 8px;
}
.sustain-reason {
  font-size: 12px;
  color: #606266;
  background: #f4f4f5;
  border-radius: 4px;
  padding: 2px 8px;
}
.zt-aggregation {
  font-size: 13px;
  color: #606266;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
}
.zt-ind-tag { font-family: inherit; }
.leader-table { width: 100%; }
.lianban-num { color: #c0392b; font-weight: 700; }
.beginner-note {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

/* 消息面 */
.news-card {
  border-radius: 10px;
  margin-bottom: 12px;
}
.news-list {
  max-height: 320px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.news-item {
  display: flex;
  align-items: baseline;
  gap: 10px;
  font-size: 13px;
  padding: 4px 6px;
  border-radius: 6px;
}
.news-item:hover { background: #f8f9fb; }
.news-time {
  color: #909399;
  font-size: 12px;
  flex-shrink: 0;
}
.news-title {
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.news-codes {
  color: #409eff;
  font-size: 12px;
  flex-shrink: 0;
}

/* AI 分析 */
.ai-card {
  border-radius: 10px;
  margin-bottom: 12px;
}
.ai-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.ai-source {
  font-size: 12px;
  color: #909399;
}
.ai-section {
  margin-bottom: 16px;
}
.ai-section-title {
  font-size: 14px;
  font-weight: 700;
  color: #c0392b;
  border-left: 3px solid #e6262e;
  padding-left: 8px;
  margin-bottom: 8px;
}
.md-body {
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
}
.md-body :deep(h4) {
  margin: 10px 0 6px;
  font-size: 14px;
}
.md-body :deep(ul) {
  margin: 4px 0;
  padding-left: 20px;
}
.md-body :deep(p) {
  margin: 4px 0;
}
.ai-disclaimer {
  font-size: 12px;
  color: #909399;
  border-top: 1px dashed #dcdfe6;
  padding-top: 8px;
}

/* 历史 */
.history-card {
  border-radius: 10px;
}

/* 响应式 */
@media (max-width: 1400px) {
  .index-row { grid-template-columns: repeat(3, 1fr); }
  .flow-row { grid-template-columns: repeat(3, 1fr); }
}
</style>
