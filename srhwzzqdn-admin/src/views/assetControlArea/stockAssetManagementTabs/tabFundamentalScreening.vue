<template>
  <div class="fundamental-screening">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>
        <el-icon><DataAnalysis /></el-icon>
        基本面选股
      </h1>
      <el-button type="primary" :loading="loading" @click="loadData">
        <el-icon><Refresh /></el-icon>
        重新选股
      </el-button>
    </div>

    <!-- 筛选标准说明 -->
    <div class="rule-section">
      <el-alert type="info" :closable="false" show-icon>
        <template #title>
          价值选股 = 基本面(78) + 技术面(12) + 题材(10)，满分100。新手可买范围：仅沪深主板股（00/60开头，不含创业板/科创板/北交所）且股价≤100元。基本面门槛：剔除ST/退市后须同时满足【净利润为正、加权ROE≥{{ screenData ? screenData.roeFloor : 15 }}%（候选不足时逐级放宽）、净利同比≥15%、营收未负增长、总市值≥50亿、PE(TTM) 0~60、非金融资产负债率≤70%】
        </template>
        <template #default>
          技术面（60日K线量价）：底部信号「地量见地价/金针探底」、底部放量突破「缩量整理+巨量破平台」加分，顶部预警「天量见天价/大阴吞阳/金针探顶」扣分，双重顶部预警直接剔除；题材：命中真实故事概念（存储/算力/机器人/核聚变/创新药等方向）得分，概念正处当日热度榜前20再加成。技术面与题材仅对基本面前35名生效，确保不选纯题材垃圾股。
        </template>
      </el-alert>
    </div>

    <!-- 选股汇总 -->
    <div class="summary-section" v-if="screenData">
      <el-tag size="large">ROE头部池 {{ screenData.totalScanned }} 家</el-tag>
      <el-tag type="success" size="large">基本面入围 {{ screenData.matchedCount }} 家</el-tag>
      <el-tag type="warning" size="large">技术面+题材精选 {{ screenData.stocks.length }} 家</el-tag>
      <el-tag v-if="screenData.reportDate" type="info" size="large">报告期 {{ screenData.reportDate }}</el-tag>
      <span class="screen-time">选股时间：{{ screenData.screenTime }}</span>
    </div>

    <!-- 入选股票列表 -->
    <div class="list-section">
      <el-table
        :data="screenData ? screenData.stocks : []"
        v-loading="loading"
        element-loading-text="正在调用东方财富接口筛选全市场基本面数据，请稍候..."
        border
        stripe
        style="width: 100%;"
      >
        <!-- 展开行：基本面好在哪 -->
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="highlight-box">
              <div class="highlight-title">
                {{ row.stockName }}（{{ row.stockCode }}）基本面好在哪：
              </div>
              <ul class="highlight-list">
                <li v-for="(h, idx) in row.highlights" :key="idx">{{ h }}</li>
              </ul>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="排名" width="60" align="center">
          <template #default="{ $index }">
            <span :class="$index === 0 ? 'rank-top' : ''">{{ $index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="股票代码" prop="stockCode" width="95" align="center" />
        <el-table-column label="股票名称" prop="stockName" width="110" align="center" />
        <el-table-column label="行业" prop="industry" width="100" align="center" />
        <el-table-column label="题材" width="150" align="center">
          <template #default="{ row }">
            <template v-if="row.concepts && row.concepts.length">
              <el-tag type="warning" size="small" class="concept-tag">
                {{ row.concepts[0] }}
              </el-tag>
              <el-tooltip v-if="row.concepts.length > 1" :content="row.concepts.join('、')" placement="top">
                <el-tag size="small" class="concept-tag">+{{ row.concepts.length - 1 }}</el-tag>
              </el-tooltip>
            </template>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="技术面" width="150" align="center">
          <template #default="{ row }">
            <div class="tech-cell">
              <el-tag
                v-for="sig in (row.techSignals || [])"
                :key="sig"
                type="success"
                size="small"
                class="tech-tag"
              >
                {{ sig }}
              </el-tag>
              <el-tag v-if="row.breakout" type="danger" size="small" class="tech-tag">放量突破</el-tag>
              <el-tag
                v-for="warn in (row.topWarnings || [])"
                :key="warn"
                type="danger"
                size="small"
                effect="dark"
                class="tech-tag"
              >
                {{ warn }}
              </el-tag>
              <span v-if="!(row.techSignals || []).length && !(row.topWarnings || []).length && !row.breakout">-</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="最新价" width="90" align="right">
          <template #default="{ row }">
            {{ formatPrice(row.lastPrice) }}
          </template>
        </el-table-column>
        <el-table-column label="涨跌幅%" width="90" align="right">
          <template #default="{ row }">
            <span :class="row.changePct >= 0 ? 'price-up' : 'price-down'">
              {{ formatPct(row.changePct) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="年初涨幅%" width="95" align="right">
          <template #default="{ row }">
            <span :class="row.yearChangePct >= 0 ? 'price-up' : 'price-down'">
              {{ formatNum(row.yearChangePct) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="总市值(亿)" width="100" align="right">
          <template #default="{ row }">
            {{ formatNum(row.totalMarketCap) }}
          </template>
        </el-table-column>
        <el-table-column label="ROE%" width="85" align="right">
          <template #default="{ row }">
            <span class="metric-strong">{{ formatNum(row.roe) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="净利同比%" width="95" align="right">
          <template #default="{ row }">
            <span :class="row.netProfitGrowth >= 0 ? 'price-up' : 'price-down'">
              {{ formatNum(row.netProfitGrowth) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="营收同比%" width="95" align="right">
          <template #default="{ row }">
            <span :class="row.revenueGrowth >= 0 ? 'price-up' : 'price-down'">
              {{ formatNum(row.revenueGrowth) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="毛利率%" width="85" align="right">
          <template #default="{ row }">
            {{ row.grossMargin > 0 ? formatNum(row.grossMargin) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="净利率%" width="85" align="right">
          <template #default="{ row }">
            {{ formatNum(row.netMargin) }}
          </template>
        </el-table-column>
        <el-table-column label="负债率%" width="85" align="right">
          <template #default="{ row }">
            {{ formatNum(row.debtRatio) }}
          </template>
        </el-table-column>
        <el-table-column label="净现比" width="80" align="right">
          <template #default="{ row }">
            <span :class="row.ocfNpRatio != null && row.ocfNpRatio >= 1 ? 'metric-strong' : ''">
              {{ formatNum(row.ocfNpRatio) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="PE(TTM)" width="85" align="right">
          <template #default="{ row }">
            {{ formatNum(row.peTtm) }}
          </template>
        </el-table-column>
        <el-table-column label="行业中位PE" width="100" align="right">
          <template #default="{ row }">
            {{ formatNum(row.industryPeMedian) }}
          </template>
        </el-table-column>
        <el-table-column label="市净率" width="80" align="right">
          <template #default="{ row }">
            {{ formatNum(row.pb) }}
          </template>
        </el-table-column>
        <el-table-column label="综合得分" width="95" align="right">
          <template #default="{ row }">
            <el-tooltip
              :content="`基本面 ${formatScore(row.fundScore)} + 技术面 ${formatScore(row.techScore)} + 题材 ${formatScore(row.themeScore)}`"
              placement="top"
            >
              <span class="score-value">{{ formatScore(row.score) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="核心亮点" min-width="240">
          <template #default="{ row }">
            <el-tooltip
              v-if="row.highlights && row.highlights.length"
              :content="row.highlights.join('；')"
              placement="top"
            >
              <span class="first-highlight">{{ row.highlights[0] }}</span>
            </el-tooltip>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 免责声明 -->
    <div class="disclaimer" v-if="screenData">
      以上选股结果由系统基于东方财富公开财务快照数据自动计算生成，仅供研究参考，不构成任何投资建议。
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { DataAnalysis, Refresh } from '@element-plus/icons-vue'
import { GetFundamentalStocks } from '@/api/stockAsset'

const loading = ref(false)
const screenData = ref(null)

const formatPrice = val => (val != null ? Number(val).toFixed(2) : '-')
const formatPct = val => (val != null ? Number(val).toFixed(2) : '-')
const formatNum = val => (val != null ? Number(val).toFixed(2) : '-')
const formatScore = val => (val != null ? Number(val).toFixed(1) : '-')

const loadData = async () => {
  loading.value = true
  try {
    const res = await GetFundamentalStocks()
    if (res.code === 200) {
      screenData.value = res.data
    } else {
      ElMessage.error(res.message || '基本面选股失败')
    }
  } catch (e) {
    ElMessage.error('基本面选股失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 父组件以 v-if 惰性渲染，每次进入该标签页都会重新挂载并自动选股
onMounted(() => {
  loadData()
})
</script>

<style scoped>
/* 页面标题头部 */
.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 30px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.page-header h1 {
  margin: 0;
  font-family: 方正姚体, sans-serif;
  color: white;
  font-size: 24px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-header h1 .el-icon {
  font-size: 28px;
}

/* 筛选标准说明 */
.rule-section {
  padding: 12px 15px 0;
}

/* 选股汇总 */
.summary-section {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 15px;
}

.screen-time {
  color: #909399;
  font-size: 13px;
  margin-left: auto;
}

/* 列表区 */
.list-section {
  padding: 0 15px;
}

.rank-top {
  color: #e6a23c;
  font-weight: 700;
}

.metric-strong {
  font-weight: 600;
  color: #409eff;
}

.score-value {
  font-weight: 700;
  color: #f56c6c;
}

.first-highlight {
  color: #67c23a;
  cursor: default;
}

/* 题材与技术面标签 */
.concept-tag,
.tech-tag {
  margin: 2px 3px 2px 0;
}

.tech-cell {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
}

/* 展开行亮点说明 */
.highlight-box {
  padding: 8px 16px 8px 40px;
}

.highlight-title {
  font-weight: 600;
  margin-bottom: 8px;
  color: #303133;
}

.highlight-list {
  margin: 0;
  padding-left: 18px;
}

.highlight-list li {
  line-height: 24px;
  color: #606266;
}

/* 免责声明 */
.disclaimer {
  padding: 12px 15px 20px;
  color: #c0c4cc;
  font-size: 12px;
  text-align: center;
}
</style>
