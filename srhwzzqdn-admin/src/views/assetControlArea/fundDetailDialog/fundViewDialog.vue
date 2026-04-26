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
          <el-table :data="props.fundData.navList" border stripe size="small" max-height="300px">
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
import { computed, ref } from 'vue'
import { Coin, Close, FullScreen, Aim, Document, Discount, Wallet, TrendCharts, List, Money, DataAnalysis, Grid } from '@element-plus/icons-vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  fundData: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:visible'])

const isFullscreen = ref(false)

const dialogVisible = computed({
  get() { return props.visible },
  set(val) { emit('update:visible', val) }
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
