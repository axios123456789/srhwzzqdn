<template>
  <div class="short-term-screening">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>
        <el-icon><Lightning /></el-icon>
        短线选股
      </h1>
      <div class="header-actions">
        <el-button type="warning" :loading="reviewing" @click="doReview">
          <el-icon><MagicStick /></el-icon>
          生成复盘经验
        </el-button>
        <el-button type="primary" :loading="loading" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新精选
        </el-button>
      </div>
    </div>

    <!-- 选股逻辑说明 -->
    <div class="rule-section">
      <el-alert type="info" :closable="false" show-icon>
        <template #title>
          三类精选各Top10：妖股梯队（连板高度+封板坚决度+涨停基因）｜低位潜伏（距60日低点≤28%+缩量整理+涨停基因+平台未启动）｜趋势延续（近5日有涨停+均线多头+非低位）。出货行为一票剔除。
        </template>
        <template #default>
          行为判定（0增量请求，基于封板质量/内外盘/主力资金/量能事实）：涨停封死+外盘占优+主力净流入=继续拉升；炸板未回封+内盘占优+主力净流出=出货嫌疑；主力净流入+缩量回调=吸筹；均线多头窄幅整理=强势整理。非交易日自动归上一个交易日收盘版；交易日盘中实时推荐（先删后插），收盘后推荐收盘版并长期留存用于复盘训练。
        </template>
      </el-alert>
    </div>

    <!-- 降级提示 -->
    <el-alert
      v-if="data && data.degraded && data.degradeReasons && data.degradeReasons.length"
      type="warning" :closable="false" show-icon class="degrade-alert"
    >
      <template #title>本轮数据降级（接口熔断，{{ (data.cacheTtlText) || '结果5分钟内缓存' }}）</template>
      <template #default>
        <div v-for="(r, i) in data.degradeReasons" :key="i">{{ r }}</div>
      </template>
    </el-alert>

    <!-- 归属信息与采集统计 -->
    <div class="summary-section" v-if="data">
      <el-tag size="large">归属交易日 {{ data.tradeDate }}</el-tag>
      <el-tag :type="data.phase === 'close' ? 'success' : (data.phase === 'pre' ? 'info' : 'warning')" size="large">{{ data.phaseText }}</el-tag>
      <el-tag v-if="data.fromDb" type="info" size="large">库内直读</el-tag>
      <el-tag v-if="data.fromCache" type="info" size="large">缓存命中</el-tag>
      <el-tag v-if="!data.saved && data.phase !== 'intraday'" type="danger" size="large">入库失败</el-tag>
      <template v-if="data.stats && data.stats.source !== 'db'">
        <el-tag type="danger" size="large">涨停 {{ data.stats.ztCount }} 家</el-tag>
        <el-tag type="warning" size="large">炸板 {{ data.stats.zbCount }} 家</el-tag>
        <el-tag type="success" size="large">快照候选 {{ data.stats.candidateCount }} 家</el-tag>
        <el-tag type="info" size="large">K线 {{ data.stats.klineFetched }} 只</el-tag>
      </template>
      <span class="screen-time" v-if="data.stats && data.stats.costMs">采集耗时 {{ Math.round(data.stats.costMs / 100) / 10 }}s</span>
    </div>

    <!-- 三类精选（子标签页） -->
    <div class="picks-section">
      <el-tabs v-model="activeGroup" class="group-tabs">
        <el-tab-pane v-for="g in groups" :key="g.type" :name="g.type">
          <template #label>
            <span class="group-label">
              {{ groupText(g.type) }}
              <el-badge :value="g.stocks.length" type="primary" class="group-badge" />
            </span>
          </template>

          <el-table
            :data="g.stocks"
            v-loading="loading"
            element-loading-text="正在采集涨停/炸板池、行情快照与K线数据（首算约1分钟内），请稍候..."
            border stripe style="width: 100%;"
            :row-class-name="behaviorRowClass"
          >
            <el-table-column type="expand">
              <template #default="{ row }">
                <div class="detail-box">
                  <div class="detail-line"><span class="detail-label">入选理由：</span>{{ row.priorityReason || '-' }}</div>
                  <div class="detail-line"><span class="detail-label">确认信号：</span>{{ row.confirmSignal || '-' }}</div>
                  <div class="detail-line"><span class="detail-label">行为依据：</span>{{ row.behaviorEvidence || '-' }}</div>
                  <div class="detail-line risk"><span class="detail-label">风险提示：</span>{{ row.riskNote || '-' }}</div>
                  <div class="detail-extra">
                    <el-tag v-if="row.ztTimeFirst" size="small">首封 {{ row.ztTimeFirst }}</el-tag>
                    <el-tag v-if="row.zhabanCount != null && row.zhabanCount > 0" type="warning" size="small">炸板 {{ row.zhabanCount }} 次</el-tag>
                    <el-tag v-if="row.ztCount60 != null" type="info" size="small">60日涨停 {{ row.ztCount60 }} 次</el-tag>
                    <el-tag v-if="row.sealRatio != null" type="info" size="small">封单/流通 {{ row.sealRatio }}%</el-tag>
                    <el-tag v-if="row.circCap != null" type="info" size="small">流通 {{ row.circCap }} 亿</el-tag>
                    <el-tag v-if="row.turnoverRate != null" type="info" size="small">换手 {{ row.turnoverRate }}%</el-tag>
                    <el-tag v-if="row.volumeRatio != null" type="info" size="small">量比 {{ row.volumeRatio }}</el-tag>
                    <el-tag v-if="row.outerInnerRatio != null" type="info" size="small">外/内盘 {{ row.outerInnerRatio }}</el-tag>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="优先" width="60" align="center">
              <template #default="{ row }">
                <span :class="row.priority === 1 ? 'rank-top' : ''">{{ row.priority }}</span>
              </template>
            </el-table-column>
            <el-table-column label="股票" width="130" align="center">
              <template #default="{ row }">
                <div class="stock-cell">
                  <span class="stock-name">{{ row.name }}</span>
                  <span class="stock-code">{{ row.code }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="行业" prop="industry" width="95" align="center">
              <template #default="{ row }">{{ row.industry || '-' }}</template>
            </el-table-column>
            <el-table-column label="现价" width="80" align="right">
              <template #default="{ row }">{{ row.price != null ? row.price : '-' }}</template>
            </el-table-column>
            <el-table-column label="涨跌幅%" width="85" align="right">
              <template #default="{ row }">
                <span :class="(row.changePct || 0) >= 0 ? 'price-up' : 'price-down'">
                  {{ row.changePct != null ? row.changePct : '-' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="连板" width="60" align="center" v-if="activeGroup === 'yaogu'">
              <template #default="{ row }">
                <el-tag v-if="row.lianban != null && row.lianban >= 2" type="danger" size="small">{{ row.lianban }}板</el-tag>
                <span v-else>{{ row.lianban != null ? row.lianban : '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="低位深度%" width="90" align="right" v-if="activeGroup === 'qianfu'">
              <template #default="{ row }">{{ row.lowDepth != null ? row.lowDepth : '-' }}</template>
            </el-table-column>
            <el-table-column label="缩量比" width="75" align="right" v-if="activeGroup !== 'yaogu'">
              <template #default="{ row }">{{ row.volShrink != null ? row.volShrink : '-' }}</template>
            </el-table-column>
            <el-table-column label="近5日%" width="75" align="right" v-if="activeGroup !== 'yaogu'">
              <template #default="{ row }">{{ row.gain5 != null ? row.gain5 : '-' }}</template>
            </el-table-column>
            <el-table-column label="近20日%" width="78" align="right" v-if="activeGroup !== 'yaogu'">
              <template #default="{ row }">{{ row.gain20 != null ? row.gain20 : '-' }}</template>
            </el-table-column>
            <el-table-column label="主力净流入(亿)" width="115" align="right">
              <template #default="{ row }">
                <span v-if="row.mainInflow != null" :class="row.mainInflow >= 0 ? 'price-up' : 'price-down'">
                  {{ row.mainInflow }}
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="净占比%" width="78" align="right">
              <template #default="{ row }">{{ row.mainInflowPct != null ? row.mainInflowPct : '-' }}</template>
            </el-table-column>
            <el-table-column label="行为判定" width="100" align="center">
              <template #default="{ row }">
                <el-tooltip :content="row.behaviorEvidence || '无依据'" placement="top" :disabled="!row.behaviorEvidence">
                  <el-tag :type="behaviorTagType(row.behavior)" :effect="row.behavior === 'distribute' ? 'dark' : 'light'" size="small">
                    {{ behaviorText(row.behavior) }}
                  </el-tag>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column label="评分" width="70" align="center">
              <template #default="{ row }">{{ row.score != null ? row.score : '-' }}</template>
            </el-table-column>
          </el-table>
          <div v-if="!loading && !g.stocks.length" class="empty-tip">
            本类暂无符合条件的精选（可能接口降级或当日无满足条件的标的）
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 复盘经验（AI数据记忆） -->
    <div class="exp-section">
      <div class="exp-header">
        <h2>复盘经验库</h2>
        <span class="exp-sub">推荐3个交易日后可复盘：T+3/T+5拉K线验证 → 事实归类 → AI总结经验入库，形成数据记忆</span>
      </div>
      <el-table :data="experiences" v-loading="expLoading" border stripe style="width: 100%;">
        <el-table-column label="经验日期" width="95" align="center">
          <template #default="{ row }">{{ fmtDate(row.expDate) }}</template>
        </el-table-column>
        <el-table-column label="推荐日" width="95" align="center">
          <template #default="{ row }">{{ fmtDate(row.tradeDate) }}</template>
        </el-table-column>
        <el-table-column label="股票" width="120" align="center">
          <template #default="{ row }">{{ row.stockName }}（{{ row.stockCode }}）</template>
        </el-table-column>
        <el-table-column label="推荐类型" width="90" align="center">
          <template #default="{ row }">{{ pickTypeText(row.pickType) }}</template>
        </el-table-column>
        <el-table-column label="经验类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="expTagType(row.expType)" size="small">{{ expTypeText(row.expType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="事实结果" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.resultBrief || '-' }}</template>
        </el-table-column>
        <el-table-column label="经验总结" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">{{ row.summary || '-' }}</template>
        </el-table-column>
        <el-table-column label="规则提示" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.ruleHint || '-' }}</template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Lightning, MagicStick } from '@element-plus/icons-vue'
import { GetShortTermStocks, RefreshShortTermStocks, GetShortTermExperience, ReviewShortTermPicks } from '@/api/stockAsset'
import { GetSysCodeByType } from '@/api/sysDict'

const loading = ref(false)
const reviewing = ref(false)
const expLoading = ref(false)
const data = ref(null)
const experiences = ref([])
const activeGroup = ref('yaogu')

// ===== 数据字典（不前端写死） =====
const pickTypeDict = ref([])
const behaviorDict = ref([])
const expTypeDict = ref([])
const dictText = (list, value) => {
  if (value == null) return '-'
  const hit = (list.value || []).find(d => d.value === value)
  return hit ? hit.text : value
}
const pickTypeText = v => dictText(pickTypeDict, v)
const behaviorText = v => dictText(behaviorDict, v)
const expTypeText = v => dictText(expTypeDict, v)

const loadDicts = async () => {
  try {
    const [a, b, c] = await Promise.all([
      GetSysCodeByType('t_short_term_pick_type'),
      GetSysCodeByType('t_short_term_behavior'),
      GetSysCodeByType('t_short_term_exp_type')
    ])
    pickTypeDict.value = a.data || []
    behaviorDict.value = b.data || []
    expTypeDict.value = c.data || []
  } catch (e) {
    console.warn('短线字典加载失败', e)
  }
}

// ===== 精选数据 =====
const groups = computed(() => {
  if (!data.value || !data.value.groups) {
    return [
      { type: 'yaogu', stocks: [] },
      { type: 'qianfu', stocks: [] },
      { type: 'qushi', stocks: [] }
    ]
  }
  return data.value.groups
})

const groupText = t => pickTypeText(t) || t

const loadData = async () => {
  loading.value = true
  try {
    const { data: res } = await GetShortTermStocks()
    data.value = res
    if (res && res.groups && res.groups.length && !groups.value.some(g => g.type === activeGroup.value)) {
      activeGroup.value = res.groups[0].type
    }
    if (res && res.saved === false && res.phase !== 'intraday') {
      ElMessage.warning('本轮推荐已生成但入库失败，请检查数据库')
    }
  } catch (e) {
    ElMessage.error('短线选股加载失败：' + (e && e.message ? e.message : '未知错误'))
  } finally {
    loading.value = false
  }
}

// 刷新精选按钮专用：不管什么时段直接实时采集+先删后入库
const refreshData = async () => {
  loading.value = true
  try {
    const { data: res } = await RefreshShortTermStocks()
    data.value = res
    if (res && res.groups && res.groups.length && !groups.value.some(g => g.type === activeGroup.value)) {
      activeGroup.value = res.groups[0].type
    }
    if (res && res.saved === false && res.phase !== 'intraday') {
      ElMessage.warning('本轮推荐已生成但入库失败，请检查数据库')
    }
  } catch (e) {
    ElMessage.error('刷新精选失败：' + (e && e.message ? e.message : '未知错误'))
  } finally {
    loading.value = false
  }
}

// ===== 复盘经验 =====
const loadExperience = async () => {
  expLoading.value = true
  try {
    const { data: res } = await GetShortTermExperience(50)
    experiences.value = (res && res.list) || []
  } catch (e) {
    console.warn('经验列表加载失败', e)
  } finally {
    expLoading.value = false
  }
}

const doReview = async () => {
  reviewing.value = true
  try {
    const { data: res } = await ReviewShortTermPicks()
    const n = res && res.reviewed != null ? res.reviewed : 0
    if (n > 0) {
      ElMessage.success(`复盘完成：生成 ${n} 条经验${res.aiUsed ? '（AI总结）' : '（规则模板兜底）'}`)
    } else {
      ElMessage.info((res && res.message) || '本轮无可复盘推荐')
    }
    await loadExperience()
  } catch (e) {
    ElMessage.error('复盘失败：' + (e && e.message ? e.message : '未知错误'))
  } finally {
    reviewing.value = false
  }
}

// ===== 展示辅助 =====
const behaviorTagType = b => {
  switch (b) {
    case 'pull_up': return 'danger'
    case 'absorb': return 'success'
    case 'organize': return 'warning'
    case 'distribute': return 'danger'
    default: return 'info'
  }
}
const expTagType = t => {
  switch (t) {
    case 'limit_up': return 'danger'
    case 'success': return 'success'
    case 'crash': return 'danger'
    case 'fake_out': return 'warning'
    default: return 'info'
  }
}
const behaviorRowClass = ({ row }) => (row.behavior === 'distribute' ? '' : '')
const fmtDate = d => (d ? String(d).slice(0, 10) : '-')

onMounted(() => {
  loadDicts()
  loadData()
  loadExperience()
})
</script>

<style scoped>
.short-term-screening {
  padding: 15px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.page-header h1 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.rule-section {
  margin-bottom: 12px;
}

.degrade-alert {
  margin-bottom: 12px;
}

.summary-section {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.screen-time {
  margin-left: auto;
  color: #909399;
  font-size: 12px;
}

.group-tabs {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 4px;
  padding: 0 10px 10px;
}

.group-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.stock-cell {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}

.stock-name {
  font-weight: 600;
}

.stock-code {
  font-size: 11px;
  color: #909399;
}

.price-up {
  color: #e74c3c;
}

.price-down {
  color: #27ae60;
}

.rank-top {
  color: #e74c3c;
  font-weight: 700;
}

.detail-box {
  padding: 8px 16px;
  line-height: 1.8;
}

.detail-label {
  color: #909399;
}

.detail-line.risk {
  color: #e67e22;
}

.detail-extra {
  margin-top: 6px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.empty-tip {
  text-align: center;
  color: #909399;
  padding: 20px 0;
}

.exp-section {
  margin-top: 16px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 4px;
  padding: 12px;
}

.exp-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 10px;
}

.exp-header h2 {
  font-size: 16px;
  margin: 0;
}

.exp-sub {
  font-size: 12px;
  color: #909399;
}
</style>
