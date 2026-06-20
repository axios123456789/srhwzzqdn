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
          <el-tag :type="getFundTypeTag()" size="small" effect="dark" style="margin-left: 10px;">{{ getDisplayText(props.fundData.fundType, fundTypeOptions) || '未知' }}</el-tag>
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
              <span class="value highlight">{{ props.fundData.assetScale != null ? props.fundData.assetScale + ' 亿' : '-' }}</span>
            </div>
            <div class="info-item">
              <label>基金管理人</label>
              <span class="value">{{ props.fundData.fundCompany || '-' }}</span>
            </div>
            <div class="info-item">
              <label>基金托管者</label>
              <span class="value">{{ props.fundData.custodian || '-' }}</span>
            </div>
            <div class="info-item">
              <label>基金经理</label>
              <span class="value">{{ props.fundData.fundManager || '-' }}</span>
            </div>
            <div class="info-item">
              <label>任职日期</label>
              <span class="value">{{ props.fundData.managerStartDate || '-' }}</span>
            </div>
            <div class="info-item">
              <label>运作方式</label>
              <span class="value">{{ getDisplayText(props.fundData.operationMode, operationModeOptions) }}</span>
            </div>
            <div class="info-item">
              <label>封闭期</label>
              <span class="value">{{ props.fundData.closedEndDays != null ? props.fundData.closedEndDays + ' 天' : '-' }}</span>
            </div>
            <!-- 新增收益率字段 -->
            <div class="info-item">
              <label>近1月收益率</label>
              <span class="value" :class="props.fundData.return1m >= 0 ? 'profit-text' : 'loss-text'">{{ props.fundData.return1m != null ? props.fundData.return1m + '%' : '-' }}</span>
            </div>
            <div class="info-item">
              <label>近3月收益率</label>
              <span class="value" :class="props.fundData.return3m >= 0 ? 'profit-text' : 'loss-text'">{{ props.fundData.return3m != null ? props.fundData.return3m + '%' : '-' }}</span>
            </div>
            <div class="info-item">
              <label>近6月收益率</label>
              <span class="value" :class="props.fundData.return6m >= 0 ? 'profit-text' : 'loss-text'">{{ props.fundData.return6m != null ? props.fundData.return6m + '%' : '-' }}</span>
            </div>
            <div class="info-item">
              <label>近1年收益率</label>
              <span class="value" :class="props.fundData.return1y >= 0 ? 'profit-text' : 'loss-text'">{{ props.fundData.return1y != null ? props.fundData.return1y + '%' : '-' }}</span>
            </div>
            <div class="info-item">
              <label>最新规模</label>
              <span class="value highlight">{{ props.fundData.latestScale != null ? props.fundData.latestScale + ' 亿' : '-' }}</span>
            </div>
            <!-- 新增占比字段 -->
            <div class="info-item">
              <label>股票占比</label>
              <span class="value">{{ props.fundData.stockRatio != null ? props.fundData.stockRatio + '%' : '-' }}</span>
            </div>
            <div class="info-item">
              <label>债券占比</label>
              <span class="value">{{ props.fundData.bondRatio != null ? props.fundData.bondRatio + '%' : '-' }}</span>
            </div>
            <div class="info-item">
              <label>现金占比</label>
              <span class="value">{{ props.fundData.cashRatio != null ? props.fundData.cashRatio + '%' : '-' }}</span>
            </div>
            <!-- 新增持有比例字段 -->
            <div class="info-item">
              <label>机构持有比例</label>
              <span class="value">{{ props.fundData.institutionRatio != null ? props.fundData.institutionRatio + '%' : '-' }}</span>
            </div>
            <div class="info-item">
              <label>个人持有比例</label>
              <span class="value">{{ props.fundData.individualRatio != null ? props.fundData.individualRatio + '%' : '-' }}</span>
            </div>
            <div class="info-item">
              <label>内部持有比例</label>
              <span class="value">{{ props.fundData.internalRatio != null ? props.fundData.internalRatio + '%' : '-' }}</span>
            </div>
          </div>
          <div class="info-full" v-if="props.fundData.netAssets">
            <label>净资产规模</label>
            <p class="desc-text">{{ props.fundData.netAssets }}</p>
          </div>
          <div class="info-full" v-if="props.fundData.fundCompanyDesc">
            <label>基金公司描述</label>
            <p class="desc-text">{{ props.fundData.fundCompanyDesc }}</p>
          </div>
          <div class="info-full" v-if="props.fundData.managerDesc">
            <label>经理描述</label>
            <p class="desc-text">{{ props.fundData.managerDesc }}</p>
          </div>
          <div class="info-full" v-if="props.fundData.scaleHistory">
            <label>规模历史</label>
            <p class="desc-text">{{ props.fundData.scaleHistory }}</p>
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
              <div class="fee-value">{{ props.fundData.purchaseFeeRate !== null && props.fundData.purchaseFeeRate !== undefined ? props.fundData.purchaseFeeRate + '%' : '-' }}</div>
            </div>
            <div class="fee-item">
              <div class="fee-label">赎回费率</div>
              <div class="fee-value">{{ props.fundData.redeemFeeRate !== null && props.fundData.redeemFeeRate !== undefined ? props.fundData.redeemFeeRate + '%' : '-' }}</div>
            </div>
            <div class="fee-item">
              <div class="fee-label">管理费</div>
              <div class="fee-value">{{ props.fundData.managementFeeRate !== null && props.fundData.managementFeeRate !== undefined ? props.fundData.managementFeeRate + '%' : '-' }}</div>
            </div>
            <div class="fee-item">
              <div class="fee-label">托管费</div>
              <div class="fee-value">{{ props.fundData.custodianFeeRate !== null && props.fundData.custodianFeeRate !== undefined ? props.fundData.custodianFeeRate + '%' : '-' }}</div>
            </div>
            <div class="fee-item">
              <div class="fee-label">销售服务费</div>
              <div class="fee-value">{{ props.fundData.salesServiceFeeRate !== null && props.fundData.salesServiceFeeRate !== undefined ? props.fundData.salesServiceFeeRate + '%' : '-' }}</div>
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
        <div class="card-body" v-if="holdingData">
          <div class="position-grid">
            <div class="position-item">
              <div class="position-label">持有份额</div>
              <div class="position-value">{{ formatNumber(holdingData.holdShares) }}</div>
            </div>
            <div class="position-item">
              <div class="position-label">可用份额</div>
              <div class="position-value">{{ formatNumber(holdingData.availableShares) }}</div>
            </div>
            <div class="position-item">
              <div class="position-label">冻结份额</div>
              <div class="position-value">{{ formatNumber(holdingData.frozenShares) }}</div>
            </div>
            <div class="position-item">
              <div class="position-label">持仓成本</div>
              <div class="position-value highlight">{{ formatCurrency(holdingData.positionCost) }}</div>
            </div>
            <div class="position-item">
              <div class="position-label">持仓市值</div>
              <div class="position-value highlight">{{ formatCurrency(holdingData.positionValue) }}</div>
            </div>
            <div class="position-item">
              <div class="position-label">持仓盈亏</div>
              <div class="position-value" :class="getProfitClass(holdingData.positionProfit)">{{ formatCurrency(holdingData.positionProfit) }}</div>
            </div>
            <div class="position-item">
              <div class="position-label">收益率</div>
              <div class="position-value" :class="getProfitClass(holdingData.positionProfitRate)">
                {{ holdingData.positionProfitRate ? holdingData.positionProfitRate.toFixed(2) + '%' : '0%' }}
              </div>
            </div>
          </div>
        </div>
        <div class="card-body" v-else>
          <el-empty description="暂无持仓信息数据" :image-size="100" />
        </div>
      </div>

      <!-- 业绩走势卡片 -->
      <div class="info-card">
        <div class="card-header">
          <el-icon><TrendCharts /></el-icon>
          <span>业绩走势</span>
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
            <!-- 右上角业绩显示 - 仿支付宝风格 -->
            <div class="performance-badge" v-if="performanceText">
              <span class="performance-value" :style="{ color: performanceColor }">{{ performanceText }}</span>
              <span class="performance-label">{{ performanceRangeLabel }}业绩</span>
            </div>
          </div>
          
          <!-- ECharts 图表 -->
          <div class="nav-chart-container" style="position:relative;">
            <div ref="chartRef" v-show="!chartLoading" style="width:100%;height:100%;"></div>
            <div v-if="chartLoading" style="width:100%;height:100%;display:flex;align-items:center;justify-content:center;">
              <el-icon class="is-loading" :size="24" color="#409EFF"><Loading /></el-icon>
              <span style="margin-left:8px;color:#999;font-size:13px;">加载中...</span>
            </div>
            <!-- 自定义tooltip - 仿支付宝风格 -->
            <div v-if="customTooltip.visible" class="custom-chart-tooltip" :style="customTooltip.positionStyle">
              <div class="tooltip-date">{{ customTooltip.date }}</div>
              <div class="tooltip-row">
                <span class="tooltip-label">业绩走势</span>
                <span class="tooltip-value" :style="{ color: customTooltip.trendColor }">{{ customTooltip.trendText }}</span>
              </div>
              <div class="tooltip-row">
                <span class="tooltip-label">单位净值</span>
                <span class="tooltip-value tooltip-value-dark">{{ customTooltip.unitNavText }}</span>
              </div>
              <div class="tooltip-row">
                <span class="tooltip-label">累计净值</span>
                <span class="tooltip-value tooltip-value-dark">{{ customTooltip.accumulatedNavText }}</span>
              </div>
              <div class="tooltip-row">
                <span class="tooltip-label">日涨跌幅</span>
                <span class="tooltip-value" :style="{ color: customTooltip.dailyChangeRateColor }">{{ customTooltip.dailyChangeRateText }}</span>
              </div>
            </div>
          </div>
          
          <!-- 净值数据表格 + 业绩分析 并排布局 -->
          <div class="nav-and-analysis-wrapper">
            <!-- 净值数据表格 -->
            <div class="nav-table-wrapper">
              <el-table 
                :data="getPaginatedNavData()" 
                border 
                stripe
                v-loading="navTableLoading" 
                size="small" 
                height="400px"
                style="width: 100%"
              >
                <el-table-column prop="navDate" label="净值日期" min-width="100" />
                <el-table-column prop="unitNav" label="单位净值" min-width="100" align="right" />
                <el-table-column prop="accumulatedNav" label="累计净值" min-width="100" align="right" />
                <el-table-column prop="dailyChangeRate" label="日涨跌幅(%)" min-width="110" align="right">
                  <template #default="{ row }">
                    <span :class="row.dailyChangeRate >= 0 ? 'profit-text' : 'loss-text'">
                      {{ row.dailyChangeRate >= 0 ? '+' : '' }}{{ row.dailyChangeRate }}%
                    </span>
                  </template>
                </el-table-column>
                <el-table-column prop="valuation" label="估值" min-width="100" align="right" />
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

            <!-- 业绩分析块 -->
            <div class="performance-analysis-wrapper">
              <div class="analysis-section-header">
                <span class="analysis-section-title">业绩分析</span>
                <el-date-picker
                  v-model="analysisDateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  size="small"
                  style="width: 240px;"
                  value-format="YYYY-MM-DD"
                  @change="handleAnalysisDateRangeChange"
                />
              </div>
              <!-- 业绩分析标签页 -->
              <div class="analysis-time-range-tabs">
                <div
                  v-for="option in analysisTimeRangeOptions"
                  :key="option.value"
                  :class="['analysis-time-tab', { active: analysisActiveTimeRange === option.value }]"
                  @click="handleAnalysisTimeRangeChange(option.value)"
                >
                  {{ option.label }}
                </div>
              </div>
              <!-- 业绩分析数据卡片 -->
              <div class="analysis-cards-container" v-loading="performanceAnalysisLoading">
                <template v-if="performanceAnalysisData">
                  <!-- 核心指标 -->
                  <div class="analysis-card-group">
                    <div class="analysis-card-group-title">核心指标</div>
                    <div class="analysis-cards-row">
                      <div class="analysis-metric-card">
                        <div class="metric-label">总涨幅</div>
                        <div class="metric-value" :class="performanceAnalysisData.totalIncrease >= 0 ? 'profit-text' : 'loss-text'">
                          {{ formatAnalysisValue(performanceAnalysisData.totalIncrease) }}
                        </div>
                      </div>
                      <div class="analysis-metric-card">
                        <div class="metric-label">平均涨跌</div>
                        <div class="metric-value" :class="performanceAnalysisData.averageIncrease >= 0 ? 'profit-text' : 'loss-text'">
                          {{ formatAnalysisValue(performanceAnalysisData.averageIncrease) }}
                        </div>
                      </div>
                      <div class="analysis-metric-card">
                        <div class="metric-label">最大日涨幅</div>
                        <div class="metric-value profit-text">{{ formatAnalysisValue(performanceAnalysisData.maxDayIncrease) }}</div>
                      </div>
                      <div class="analysis-metric-card">
                        <div class="metric-label">最大日跌幅</div>
                        <div class="metric-value loss-text">{{ formatAnalysisValue(performanceAnalysisData.maxDayDecrease) }}</div>
                      </div>
                    </div>
                  </div>
                  <!-- 涨跌统计 -->
                  <div class="analysis-card-group">
                    <div class="analysis-card-group-title">涨跌统计</div>
                    <div class="analysis-cards-row">
                      <div class="analysis-metric-card">
                        <div class="metric-label">上涨天数</div>
                        <div class="metric-value profit-text">{{ formatDays(performanceAnalysisData.increase) }}</div>
                      </div>
                      <div class="analysis-metric-card">
                        <div class="metric-label">下跌天数</div>
                        <div class="metric-value loss-text">{{ formatDays(performanceAnalysisData.decrease) }}</div>
                      </div>
                      <div class="analysis-metric-card">
                        <div class="metric-label">平盘天数</div>
                        <div class="metric-value">{{ formatDays(performanceAnalysisData.flat) }}</div>
                      </div>
                      <div class="analysis-metric-card">
                        <div class="metric-label">上涨合计</div>
                        <div class="metric-value profit-text">{{ formatAnalysisValue(performanceAnalysisData.increaseSum) }}</div>
                      </div>
                      <div class="analysis-metric-card">
                        <div class="metric-label">下跌合计</div>
                        <div class="metric-value loss-text">{{ formatAnalysisValue(performanceAnalysisData.decreaseSum) }}</div>
                      </div>
                    </div>
                  </div>
                  <!-- 极端行情 -->
                  <div class="analysis-card-group">
                    <div class="analysis-card-group-title">极端行情</div>
                    <div class="analysis-cards-row">
                      <div class="analysis-metric-card">
                        <div class="metric-label">超5%涨幅天数</div>
                        <div class="metric-value profit-text">{{ formatDays(performanceAnalysisData.overFiveDayIncrease) }}</div>
                      </div>
                      <div class="analysis-metric-card">
                        <div class="metric-label">超5%跌幅天数</div>
                        <div class="metric-value loss-text">{{ formatDays(performanceAnalysisData.overFiveDayDecrease) }}</div>
                      </div>
                      <div class="analysis-metric-card">
                        <div class="metric-label">超5%涨幅合计</div>
                        <div class="metric-value profit-text">{{ formatAnalysisValue(performanceAnalysisData.overFiveDayIncreaseSum) }}</div>
                      </div>
                      <div class="analysis-metric-card">
                        <div class="metric-label">超5%跌幅合计</div>
                        <div class="metric-value loss-text">{{ formatAnalysisValue(performanceAnalysisData.overFiveDayDecreaseSum) }}</div>
                      </div>
                      <div class="analysis-metric-card">
                        <div class="metric-label">超10%涨幅天数</div>
                        <div class="metric-value profit-text">{{ formatDays(performanceAnalysisData.overTenDayIncrease) }}</div>
                      </div>
                      <div class="analysis-metric-card">
                        <div class="metric-label">超10%跌幅天数</div>
                        <div class="metric-value loss-text">{{ formatDays(performanceAnalysisData.overTenDayDecrease) }}</div>
                      </div>
                      <div class="analysis-metric-card">
                        <div class="metric-label">超10%涨幅合计</div>
                        <div class="metric-value profit-text">{{ formatAnalysisValue(performanceAnalysisData.overTenDayIncreaseSum) }}</div>
                      </div>
                      <div class="analysis-metric-card">
                        <div class="metric-label">超10%跌幅合计</div>
                        <div class="metric-value loss-text">{{ formatAnalysisValue(performanceAnalysisData.overTenDayDecreaseSum) }}</div>
                      </div>
                    </div>
                  </div>
                </template>
                <el-empty v-else description="暂无业绩分析数据" :image-size="60" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 基金经理分析卡片 -->
      <div class="info-card">
        <div class="card-header">
          <el-icon><User /></el-icon>
          <span>基金经理分析</span>
        </div>
        <div class="card-body" v-if="managerAnalysisData">
          <!-- 基本信息 -->
          <div class="info-grid">
            <div class="info-item">
              <label>姓名</label>
              <span class="value">{{ managerAnalysisData.name || '-' }}</span>
            </div>
            <div class="info-item">
              <label>星级</label>
              <span class="value">
                <el-rate :model-value="managerAnalysisData.starLevel" :max="5" disabled show-score />
              </span>
            </div>
            <div class="info-item">
              <label>从业时间</label>
              <span class="value">{{ managerAnalysisData.workYears != null ? managerAnalysisData.workYears + ' 年' : '-' }}</span>
            </div>
            <div class="info-item">
              <label>管理规模</label>
              <span class="value highlight">{{ managerAnalysisData.manageScale != null ? managerAnalysisData.manageScale + ' 亿' : '-' }}</span>
            </div>
            <div class="info-item">
              <label>学历</label>
              <span class="value">{{ getDisplayText(managerAnalysisData.education, educationOptions) }}</span>
            </div>
            <div class="info-item">
              <label>个人持有</label>
              <span class="value" :class="managerAnalysisData.personalHold === 1 ? 'profit-text' : 'loss-text'">
                {{ managerAnalysisData.personalHold === 1 ? '是' : '否' }}
              </span>
            </div>
            <div class="info-item">
              <label>获奖记录</label>
              <span class="value">
                <el-tag v-if="managerAnalysisData.awards && getDisplayText(managerAnalysisData.awards, awardOptions) !== '-'"
                  :type="getAwardTagType(getDisplayText(managerAnalysisData.awards, awardOptions))" size="small">
                  {{ getDisplayText(managerAnalysisData.awards, awardOptions) }}
                </el-tag>
                <span v-else>{{ getDisplayText(managerAnalysisData.awards, awardOptions) }}</span>
              </span>
            </div>
          </div>
          <!-- 评分信息 -->
          <div class="score-grid">
            <div class="score-item">
              <div class="score-label">综合评分</div>
              <div class="score-value">{{ managerAnalysisData.overallScore || 0 }}</div>
            </div>
            <div class="score-item">
              <div class="score-label">选证能力</div>
              <div class="score-value">{{ managerAnalysisData.selectionScore || 0 }}</div>
            </div>
            <div class="score-item">
              <div class="score-label">收益率</div>
              <div class="score-value">{{ managerAnalysisData.returnScore || 0 }}</div>
            </div>
            <div class="score-item">
              <div class="score-label">抗风险</div>
              <div class="score-value">{{ managerAnalysisData.riskScore || 0 }}</div>
            </div>
            <div class="score-item">
              <div class="score-label">稳定性</div>
              <div class="score-value">{{ managerAnalysisData.stabilityScore || 0 }}</div>
            </div>
            <div class="score-item">
              <div class="score-label">择时能力</div>
              <div class="score-value">{{ managerAnalysisData.timingScore || 0 }}</div>
            </div>
          </div>
          <!-- 分析维度 -->
          <div class="analysis-grid">
            <div class="analysis-item">
              <label>持仓集中度</label>
              <el-tag :type="getAnalysisTagType(getDisplayText(managerAnalysisData.positionConcentration, levelOptions))" size="small">
                {{ getDisplayText(managerAnalysisData.positionConcentration, levelOptions) }}
              </el-tag>
            </div>
            <div class="analysis-item">
              <label>换手率</label>
              <el-tag :type="getAnalysisTagType(getDisplayText(managerAnalysisData.turnoverRate, levelOptions))" size="small">
                {{ getDisplayText(managerAnalysisData.turnoverRate, levelOptions) }}
              </el-tag>
            </div>
            <div class="analysis-item">
              <label>能力路径匹配度</label>
              <el-tag :type="getAnalysisTagType(getDisplayText(managerAnalysisData.abilityPathMatch, levelOptions))" size="small">
                {{ getDisplayText(managerAnalysisData.abilityPathMatch, levelOptions) }}
              </el-tag>
            </div>
            <div class="analysis-item">
              <label>规模驾驭能力</label>
              <el-tag :type="getAnalysisTagType(getDisplayText(managerAnalysisData.scaleControlAbility, levelOptions))" size="small">
                {{ getDisplayText(managerAnalysisData.scaleControlAbility, levelOptions) }}
              </el-tag>
            </div>
            <div class="analysis-item">
              <label>本基金精力集中度</label>
              <el-tag :type="getAnalysisTagType(getDisplayText(managerAnalysisData.focusLevel, levelOptions))" size="small">
                {{ getDisplayText(managerAnalysisData.focusLevel, levelOptions) }}
              </el-tag>
            </div>
          </div>
          <!-- 文本分析 -->
          <div class="info-full" v-if="managerAnalysisData.managerDesc">
            <label>经理描述</label>
            <p class="desc-text">{{ managerAnalysisData.managerDesc }}</p>
          </div>
          <div class="info-full" v-if="managerAnalysisData.positionConcentrationAnalysis">
            <label>持仓集中度分析</label>
            <p class="desc-text">{{ managerAnalysisData.positionConcentrationAnalysis }}</p>
          </div>
          <div class="info-full" v-if="managerAnalysisData.turnoverRateAnalysis">
            <label>换手率分析</label>
            <p class="desc-text">{{ managerAnalysisData.turnoverRateAnalysis }}</p>
          </div>
          <div class="info-full" v-if="managerAnalysisData.abilityCircleAnalysis">
            <label>能力圈与路径依赖分析</label>
            <p class="desc-text">{{ managerAnalysisData.abilityCircleAnalysis }}</p>
          </div>
          <div class="info-full" v-if="managerAnalysisData.scaleControlAnalysis">
            <label>规模驾驭能力分析</label>
            <p class="desc-text">{{ managerAnalysisData.scaleControlAnalysis }}</p>
          </div>
          <div class="info-full" v-if="managerAnalysisData.workBackground">
            <label>从业背景</label>
            <p class="desc-text">{{ managerAnalysisData.workBackground }}</p>
          </div>
          <div class="info-full" v-if="managerAnalysisData.productManageAnalysis">
            <label>产品管理情况分析</label>
            <p class="desc-text">{{ managerAnalysisData.productManageAnalysis }}</p>
          </div>
          <div class="info-full" v-if="managerAnalysisData.stabilityAnalysis">
            <label>稳定性分析</label>
            <p class="desc-text">{{ managerAnalysisData.stabilityAnalysis }}</p>
          </div>
          <div class="info-full" v-if="managerAnalysisData.personalHoldAnalysis">
            <label>个人持有情况</label>
            <p class="desc-text">{{ managerAnalysisData.personalHoldAnalysis }}</p>
          </div>
        </div>
        <div class="card-body" v-else>
          <el-empty description="暂无基金经理分析数据" :image-size="100" />
        </div>
      </div>

      <!-- 交易记录卡片 -->
      <div class="info-card">
        <div class="card-header">
          <el-icon><List /></el-icon>
          <span>交易记录</span>
        </div>
        <div class="card-body">
          <el-table :data="tradeData" border stripe size="small" max-height="300px" v-loading="tradeTableLoading">
            <el-table-column prop="tradeDate" label="交易日期" width="120" />
            <el-table-column prop="tradeType" label="交易类型" width="100">
              <template #default="{ row }">
                <el-tag :type="row.tradeType === 1 ? 'success' : 'danger'" size="small">
                  {{ getDisplayText(row.tradeType, transactionTypeOptions) }}
                </el-tag>
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
          
          <!-- 分页组件 -->
          <div class="nav-table-pagination" v-if="tradeTableTotal > 0">
            <el-pagination
              v-model:current-page="tradeTablePage"
              v-model:page-size="tradeTablePageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="tradeTableTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @current-change="fetchTradeData"
              @size-change="fetchTradeData"
            />
          </div>
        </div>
      </div>

      <!-- 分红记录卡片 -->
      <div class="info-card">
        <div class="card-header">
          <el-icon><Money /></el-icon>
          <span>分红记录</span>
        </div>
        <div class="card-body">
          <el-table :data="dividendData" border stripe size="small" max-height="300px" v-loading="dividendTableLoading">
            <el-table-column prop="dividendDate" label="分红时间" width="150" />
            <el-table-column prop="arrivalDate" label="到账时间" width="150" />
            <el-table-column prop="dividendType" label="分红方式" width="120">
              <template #default="{ row }">
                <el-tag type="warning" size="small">{{ row.dividendType === 1 ? '现金分红' : '红利再投' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="dividendRule" label="分红规则" width="120" />
            <el-table-column prop="dividendAmount" label="分红金额" width="150" align="right">
              <template #default="{ row }">
                <span class="profit-text">¥{{ row.dividendAmount }}</span>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 分页组件 -->
          <div class="nav-table-pagination" v-if="dividendTableTotal > 0">
            <el-pagination
              v-model:current-page="dividendTablePage"
              v-model:page-size="dividendTablePageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="dividendTableTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @current-change="fetchDividendData"
              @size-change="fetchDividendData"
            />
          </div>
        </div>
      </div>

      <!-- 持仓明细卡片 -->
      <div class="info-card">
        <div class="card-header">
          <el-icon><Grid /></el-icon>
          <span>持仓明细</span>
        </div>
        <div class="card-body">
          <el-table :data="portfolioData" border stripe size="small" max-height="300px" v-loading="portfolioTableLoading">
            <el-table-column prop="portfolioDate" label="持仓日期" width="120" />
            <el-table-column prop="positionType" label="持仓类型" width="100">
              <template #default="{ row }">
                <el-tag :type="getPositionTypeTagType(row.positionType)" size="small">
                  {{ getPositionTypeText(row.positionType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="positionCode" label="持仓代码" width="120" />
            <el-table-column prop="positionName" label="持仓名称" min-width="160" show-overflow-tooltip />
            <el-table-column prop="positionQuantity" label="持仓数量" width="120" align="right" />
            <el-table-column prop="positionMarketValue" label="持仓市值" width="120" align="right" />
            <el-table-column prop="netRatio" label="占净值比例(%)" width="130" align="right" />
            <el-table-column prop="industryType" label="行业分类" width="110">
              <template #default="{ row }">
                <el-tag :type="getIndustryTypeTagType(row.industryType)" size="small">
                  {{ getIndustryTypeText(row.industryType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="dataSource" label="数据来源" width="100">
              <template #default="{ row }">
                <el-tag :type="getDataSourceTagType(row.dataSource)" size="small">
                  {{ getDataSourceText(row.dataSource) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 分页组件 -->
          <div class="nav-table-pagination" v-if="portfolioTableTotal > 0">
            <el-pagination
              v-model:current-page="portfolioTablePage"
              v-model:page-size="portfolioTablePageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="portfolioTableTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @current-change="fetchPortfolioData"
              @size-change="fetchPortfolioData"
            />
          </div>
        </div>
      </div>

      <!-- 风险指标卡片 -->
      <div class="info-card">
        <div class="card-header">
          <el-icon><DataAnalysis /></el-icon>
          <span>风险指标</span>
        </div>
        <div class="card-body">
          <el-table :data="riskPerformanceData" border stripe size="small" max-height="300px">
            <el-table-column prop="periodType" label="时间周期" width="120">
              <template #default="{ row }">
                {{ getPeriodTypeText(row.periodType) }}
              </template>
            </el-table-column>
            <el-table-column prop="maxDrawDown" label="最大回撤(%)" width="130" align="right">
              <template #default="{ row }">
                <span class="loss-text">{{ row.maxDrawDown }}%</span>
              </template>
            </el-table-column>
            <el-table-column prop="recoveryDays" label="回撤修复天数" width="130" align="right" />
            <el-table-column prop="annualReturn" label="年化收益率(%)" width="140" align="right">
              <template #default="{ row }">
                <span :class="row.annualReturn >= 0 ? 'profit-text' : 'loss-text'">
                  {{ row.annualReturn >= 0 ? '+' : '' }}{{ row.annualReturn }}%
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="sharpeRatio" label="夏普比率" width="110" align="right" />
            <el-table-column prop="volatility" label="波动率(%)" width="110" align="right" />
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
import { computed, ref, watch, nextTick, onBeforeUnmount, reactive, onMounted } from 'vue'
import { Coin, Close, FullScreen, Aim, Document, Discount, Wallet, TrendCharts, List, Money, DataAnalysis, Grid, User, Loading } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { GetFundNavByConditionAndPage, GetNaveChartsByCondition, GetFundManagerAnalysisByCode, GetFundHoldingByCode, GetFundTransactionByConditionAndPage, GetFundDividendByConditionAndPage, GetFundRiskPerformanceByCode, GetFundPortfolioByConditionAndPage, GetPerformanceAnalysisByCondition } from "@/api/fundAsset"
import { GetKeyAndValueByType } from "@/api/sysDict"
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: { type: Boolean, default: false },
  fundData: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:visible'])

// ============ 数据字典选项 ============
// 基金类型选项
const fundTypeOptions = ref([])
// 运作方式选项
const operationModeOptions = ref([])
// 交易类型选项
const transactionTypeOptions = ref([])
// 持仓类型选项
const positionTypeOptions = ref([])
// 行业分类选项
const sectorTypeOptions = ref([])
// 数据来源选项
const dataSourceOptions = ref([])
// 学历选项
const educationOptions = ref([])
// 获奖记录选项
const awardOptions = ref([])
// 等级选项（持仓集中度、换手率等）
const levelOptions = ref([])

// 获取数据字典
const getDictOptions = async () => {
  try {
    // 基金类型
    const fundTypeResult = await GetKeyAndValueByType("t_fund_type")
    fundTypeOptions.value = fundTypeResult.data || []
    
    // 运作方式
    const operationModeResult = await GetKeyAndValueByType("t_fund_run_way")
    operationModeOptions.value = operationModeResult.data || []
    
    // 交易类型
    const transactionTypeResult = await GetKeyAndValueByType("t_fund_transaction_type")
    transactionTypeOptions.value = transactionTypeResult.data || []
    
    // 持仓类型
    const positionTypeResult = await GetKeyAndValueByType("t_fund_position_type")
    positionTypeOptions.value = positionTypeResult.data || []
    
    // 行业分类
    const sectorTypeResult = await GetKeyAndValueByType("t_fund_sector_type")
    sectorTypeOptions.value = sectorTypeResult.data || []
    
    // 数据来源
    const dataSourceResult = await GetKeyAndValueByType("t_fund_data_source")
    dataSourceOptions.value = dataSourceResult.data || []
    
    // 学历
    const educationResult = await GetKeyAndValueByType("t_fund_school_type")
    educationOptions.value = educationResult.data || []
    
    // 获奖记录
    const awardResult = await GetKeyAndValueByType("t_fund_award")
    awardOptions.value = awardResult.data || []
    
    // 等级（持仓集中度、换手率等）
    const levelResult = await GetKeyAndValueByType("t_fund_level")
    levelOptions.value = levelResult.data || []
  } catch (error) {
    console.error('获取数据字典失败:', error)
  }
}

// 通用方法：根据值和映射表获取中文文本
const getDisplayText = (value, mappingArray) => {
  if (!value && value !== 0) return '-'
  const foundItem = mappingArray.find(item => item.value === value)
  return foundItem ? foundItem.text : value
}

// 组件挂载时获取数据字典
onMounted(() => {
  getDictOptions()
})

const isFullscreen = ref(false)
const chartInstance = ref(null)
const chartRef = ref(null)
const activeTimeRange = ref('1month')
const chartLoading = ref(false)

// ============ 业绩分析相关 ============
const performanceAnalysisData = ref(null)
const performanceAnalysisLoading = ref(false)
const analysisActiveTimeRange = ref('1year') // 业绩分析默认近1年
const analysisDateRange = ref([]) // 自定义时间范围
const analysisTimeRangeOptions = [
  { label: '近1月', value: '1month' },
  { label: '近3月', value: '3month' },
  { label: '近1年', value: '1year' },
  { label: '近3年', value: '3year' },
  { label: '近5年', value: '5year' },
  { label: '近10年', value: '10year' },
  { label: '全部', value: 'all' }
]

// 业绩分析时间范围计算
const getAnalysisTimeRange = (range) => {
  const now = new Date()
  const endTime = now.toISOString().slice(0, 10)
  let startDate = new Date()
  switch (range) {
    case '1month': startDate.setDate(now.getDate() - 30); break
    case '3month': startDate.setMonth(now.getMonth() - 3); break
    case '1year': startDate.setFullYear(now.getFullYear() - 1); break
    case '3year': startDate.setFullYear(now.getFullYear() - 3); break
    case '5year': startDate.setFullYear(now.getFullYear() - 5); break
    case '10year': startDate.setFullYear(now.getFullYear() - 10); break
    case 'all': return { beginTime: '', endTime: '' }
    default: startDate.setFullYear(now.getFullYear() - 1)
  }
  const beginTime = startDate.toISOString().slice(0, 10)
  return { beginTime, endTime }
}

// 获取业绩分析数据
const fetchPerformanceAnalysisData = async () => {
  const fundCode = props.fundData?.fundCode
  if (!fundCode) return
  performanceAnalysisLoading.value = true
  try {
    let beginTime = ''
    let endTime = ''
    // 优先使用自定义时间范围
    if (analysisDateRange.value && analysisDateRange.value.length === 2) {
      beginTime = analysisDateRange.value[0]
      endTime = analysisDateRange.value[1]
    } else {
      const range = getAnalysisTimeRange(analysisActiveTimeRange.value)
      beginTime = range.beginTime
      endTime = range.endTime
    }
    const result = await GetPerformanceAnalysisByCondition({
      fundCode,
      beginTime: beginTime || null,
      endTime: endTime || null
    })
    if (result.code === 200) {
      performanceAnalysisData.value = result.data || null
    } else {
      ElMessage.error(result.message || '获取业绩分析数据失败')
      performanceAnalysisData.value = null
    }
  } catch (error) {
    console.error('获取业绩分析数据失败:', error)
    ElMessage.error('获取业绩分析数据失败')
    performanceAnalysisData.value = null
  } finally {
    performanceAnalysisLoading.value = false
  }
}

// 业绩分析标签页切换
const handleAnalysisTimeRangeChange = (range) => {
  analysisActiveTimeRange.value = range
  analysisDateRange.value = [] // 清空自定义时间范围
  fetchPerformanceAnalysisData()
}

// 业绩分析自定义时间范围变化
const handleAnalysisDateRangeChange = (val) => {
  if (val && val.length === 2) {
    analysisActiveTimeRange.value = '' // 清空标签页选中
  }
  fetchPerformanceAnalysisData()
}

// 格式化业绩分析数值
const formatAnalysisValue = (val, suffix = '%', decimal = 2) => {
  if (val == null) return '-'
  return val.toFixed(decimal) + suffix
}

// 格式化天数
const formatDays = (val) => {
  if (val == null) return '-'
  return val + '天'
}

// 右上角业绩显示相关
const performanceText = ref('')
const performanceColor = ref('#EE4B4B')
const performanceRangeLabel = ref('')

// 自定义tooltip数据
const customTooltip = reactive({
  visible: false,
  date: '',
  trendText: '',
  trendColor: '#EE4B4B',
  unitNavText: '',
  accumulatedNavText: '',
  dailyChangeRateText: '',
  dailyChangeRateColor: '#EE4B4B',
  positionStyle: {}
})

// echarts图表数据（从后端getNaveChartsByCondition接口获取）
const chartDataList = ref([])

// 根据时间范围计算beginTime和endTime
const getTimeRange = (range) => {
  const now = new Date()
  const endTime = now.toISOString().slice(0, 10)
  let startDate = new Date()
  switch (range) {
    case '1month': startDate.setMonth(now.getMonth() - 1); break
    case '3month': startDate.setMonth(now.getMonth() - 3); break
    case '6month': startDate.setMonth(now.getMonth() - 6); break
    case '1year': startDate.setFullYear(now.getFullYear() - 1); break
    case '3year': startDate.setFullYear(now.getFullYear() - 3); break
    case '5year': startDate.setFullYear(now.getFullYear() - 5); break
    case '10year': startDate.setFullYear(now.getFullYear() - 10); break
    case 'all': return { beginTime: '', endTime: '' }
    default: startDate.setMonth(now.getMonth() - 1)
  }
  const beginTime = startDate.toISOString().slice(0, 10)
  return { beginTime, endTime }
}

// 获取echarts图表数据
const fetchChartData = async () => {
  const fundCode = props.fundData?.fundCode
  if (!fundCode) return
  chartLoading.value = true
  try {
    const { beginTime, endTime } = getTimeRange(activeTimeRange.value)
    const result = await GetNaveChartsByCondition({
      fundCode,
      beginTime: beginTime || null,
      endTime: endTime || null
    })
    if (result.code === 200) {
      chartDataList.value = result.data || []
    } else {
      ElMessage.error(result.message || '获取图表数据失败')
      chartDataList.value = []
    }
  } catch (error) {
    console.error('获取图表数据失败:', error)
    ElMessage.error('获取图表数据失败')
    chartDataList.value = []
  } finally {
    chartLoading.value = false
  }
}

// 分页相关状态
const navTablePage = ref(1)
const navTablePageSize = ref(10)
const navTableTotal = ref(0)
const navTableLoading = ref(false)
const navDataList = ref([])

// 净值查询参数
const navQueryParams = reactive({
  fundCode: '',
  beginTime: '',
  endTime: ''
})

// 获取净值数据的方法
const fetchNavData = async () => {
  if (!navQueryParams.fundCode) {
    return
  }
  
  navTableLoading.value = true
  try {
    const result = await GetFundNavByConditionAndPage(
      navTablePage.value,
      navTablePageSize.value,
      {
        fundCode: navQueryParams.fundCode,
        beginTime: navQueryParams.beginTime || null,
        endTime: navQueryParams.endTime || null
      }
    )
    
    if (result.code === 200) {
      navDataList.value = result.data.list || []
      navTableTotal.value = result.data.total || 0
    } else {
      ElMessage.error(result.message || '获取净值数据失败')
    }
  } catch (error) {
    console.error('获取净值数据失败:', error)
    ElMessage.error('获取净值数据失败')
  } finally {
    navTableLoading.value = false
  }
}

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

// 获取分页后的净值数据 - 改为直接返回从后端获取的数据
const getPaginatedNavData = () => {
  return navDataList.value
}

// 处理分页变化
const handleNavTablePageChange = (page) => {
  navTablePage.value = page
  fetchNavData()
}

// 处理每页条数变化
const handleNavTableSizeChange = (size) => {
  navTablePageSize.value = size
  navTablePage.value = 1
  fetchNavData()
}

// 初始化图表 - 仿照支付宝业绩走势图风格
const initChart = () => {
  if (!chartRef.value) return

  // 隐藏自定义tooltip
  customTooltip.visible = false

  // 移除旧的事件监听
  if (chartRef.value._tooltipMoveHandler) {
    chartRef.value.removeEventListener('mousemove', chartRef.value._tooltipMoveHandler)
    chartRef.value._tooltipMoveHandler = null
  }
  if (chartRef.value._tooltipLeaveHandler) {
    chartRef.value.removeEventListener('mouseleave', chartRef.value._tooltipLeaveHandler)
    chartRef.value._tooltipLeaveHandler = null
  }

  if (chartInstance.value) {
    chartInstance.value.dispose()
  }

  chartInstance.value = echarts.init(chartRef.value)

  // 使用后端返回的chartDataList数据，按日期升序排列（时间从左往右）
  const sortedData = [...chartDataList.value].sort((a, b) => {
    return new Date(a.navDate) - new Date(b.navDate)
  })

  if (sortedData.length === 0) {
    chartInstance.value.setOption({
      title: {
        text: '暂无数据',
        left: 'center',
        top: 'center',
        textStyle: { color: '#999', fontSize: 14, fontWeight: 400 }
      }
    })
    return
  }
  
  // 准备图表数据
  const dates = sortedData.map(item => item.navDate)
  const salesTrendData = sortedData.map(item => item.salesTrend)
  const unitNavData = sortedData.map(item => item.unitNav)
  const accumulatedNavData = sortedData.map(item => item.accumulatedNav)
  const dailyChangeRateData = sortedData.map(item => item.dailyChangeRate)

  // 判断整体涨跌，决定主色调
  const lastTrend = salesTrendData[salesTrendData.length - 1] || 0
  const isRise = lastTrend >= 0
  const mainColor = isRise ? '#EE4B4B' : '#1DB068'  // 涨红跌绿
  const areaColorTop = isRise ? 'rgba(238, 75, 75, 0.25)' : 'rgba(29, 176, 104, 0.25)'
  const areaColorBottom = isRise ? 'rgba(238, 75, 75, 0.02)' : 'rgba(29, 176, 104, 0.02)'

  // 计算Y轴范围，确保0线可见
  const minTrend = Math.min(0, ...salesTrendData)
  const maxTrend = Math.max(0, ...salesTrendData)
  const yMin = minTrend < 0 ? Math.floor(minTrend / 5) * 5 - 5 : 0
  const yMax = Math.ceil(maxTrend / 5) * 5 + 5

  // 更新右上角业绩显示
  const currentPerformance = lastTrend
  const performanceSign = currentPerformance >= 0 ? '+' : ''
  performanceText.value = `${performanceSign}${currentPerformance.toFixed(2)}%`
  performanceColor.value = mainColor
  performanceRangeLabel.value = timeRangeOptions.find(o => o.value === activeTimeRange.value)?.label || ''

  const option = {
    tooltip: {
      show: false  // 禁用ECharts内置tooltip，使用Vue自定义tooltip
    },
    grid: {
      left: 50,
      right: 20,
      bottom: 30,
      top: 20
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLine: {
        show: true,
        lineStyle: {
          color: '#e8e8e8'
        }
      },
      axisLabel: {
        color: '#999',
        fontSize: 11,
        interval: 'auto'
      },
      axisTick: {
        show: false
      },
      axisPointer: {
        show: true,
        type: 'line',
        lineStyle: {
          color: '#ccc',
          width: 1,
          type: 'dashed'
        },
        label: {
          show: false
        }
      }
    },
    yAxis: {
      type: 'value',
      min: yMin,
      max: yMax,
      axisLine: {
        show: false
      },
      axisLabel: {
        color: '#999',
        fontSize: 11,
        formatter: (value) => value.toFixed(0) + '%'
      },
      splitLine: {
        lineStyle: {
          color: '#f0f0f0',
          type: 'solid'
        }
      },
      axisTick: {
        show: false
      }
    },
    series: [
      {
        name: '业绩走势',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 4,
        showSymbol: true,
        lineStyle: {
          width: 2,
          color: mainColor
        },
        itemStyle: {
          color: mainColor,
          borderWidth: 0,
          opacity: 0
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: areaColorTop },
            { offset: 1, color: areaColorBottom }
          ])
        },
        emphasis: {
          scale: 2,
          itemStyle: {
            color: mainColor,
            borderWidth: 2,
            borderColor: '#fff',
            shadowBlur: 6,
            shadowColor: mainColor,
            opacity: 1
          }
        },
        markLine: {
          silent: true,
          symbol: 'none',
          label: {
            show: false
          },
          lineStyle: {
            color: '#ccc',
            type: 'solid',
            width: 1
          },
          data: [
            { yAxis: 0 }
          ]
        },
        data: salesTrendData
      }
    ]
  }

  chartInstance.value.setOption(option)

  // 使用ECharts的convertFromPixel来将鼠标像素坐标转换为数据索引
  // 监听canvas原生mousemove事件，驱动Vue自定义tooltip
  const chartDom = chartRef.value
  const handleMouseMove = (e) => {
    if (!chartInstance.value) return
    const rect = chartDom.getBoundingClientRect()
    const x = e.clientX - rect.left
    const y = e.clientY - rect.top
    
    // 使用ECharts API将像素坐标转换为数据坐标
    const pointInGrid = chartInstance.value.convertFromPixel({ seriesIndex: 0 }, [x, y])
    if (!pointInGrid || pointInGrid.length === 0) {
      customTooltip.visible = false
      return
    }
    
    // 对于category轴，pointInGrid[0]是类别索引（浮点数）
    const categoryIndex = Math.round(pointInGrid[0])
    if (categoryIndex < 0 || categoryIndex >= dates.length) {
      customTooltip.visible = false
      return
    }
    
    // 检查鼠标是否在grid区域内
    const gridRect = chartInstance.value.getModel().getComponent('grid', 0).coordinateSystem.getRect()
    if (x < gridRect.x || x > gridRect.x + gridRect.width || y < gridRect.y || y > gridRect.y + gridRect.height) {
      customTooltip.visible = false
      return
    }
    
    const dataIndex = categoryIndex
    const date = dates[dataIndex]
    const trend = salesTrendData[dataIndex]
    const unitNav = unitNavData[dataIndex]
    const accumulatedNav = accumulatedNavData[dataIndex]
    const dailyChangeRate = dailyChangeRateData[dataIndex]
    
    // 安全取值
    const trendVal = (trend != null && !isNaN(trend)) ? trend : 0
    const unitNavVal = (unitNav != null && !isNaN(unitNav)) ? unitNav : null
    const accNavVal = (accumulatedNav != null && !isNaN(accumulatedNav)) ? accumulatedNav : null
    const dailyChangeRateVal = (dailyChangeRate != null && !isNaN(dailyChangeRate)) ? dailyChangeRate : null
    
    const trendColor = trendVal >= 0 ? '#EE4B4B' : '#1DB068'
    const trendSign = trendVal >= 0 ? '+' : ''
    
    // 更新tooltip数据
    customTooltip.date = date
    customTooltip.trendText = trendSign + trendVal.toFixed(2) + '%'
    customTooltip.trendColor = trendColor
    customTooltip.unitNavText = unitNavVal != null ? unitNavVal.toFixed(4) : '-'
    customTooltip.accumulatedNavText = accNavVal != null ? accNavVal.toFixed(4) : '-'
    
    // 日涨跌幅显示
    if (dailyChangeRateVal != null) {
      const dailyChangeRateColor = dailyChangeRateVal >= 0 ? '#EE4B4B' : '#1DB068'
      const dailyChangeRateSign = dailyChangeRateVal >= 0 ? '+' : ''
      customTooltip.dailyChangeRateText = dailyChangeRateSign + dailyChangeRateVal.toFixed(2) + '%'
      customTooltip.dailyChangeRateColor = dailyChangeRateColor
    } else {
      customTooltip.dailyChangeRateText = '-'
      customTooltip.dailyChangeRateColor = '#fff'
    }
    
    customTooltip.visible = true
    
    // 计算tooltip位置（相对于chart容器）
    const tooltipWidth = 140
    const tooltipHeight = 80
    const containerWidth = chartDom.offsetWidth
    let left = x + 15
    let top = y - tooltipHeight / 2
    // 防止右侧溢出
    if (left + tooltipWidth > containerWidth) {
      left = x - tooltipWidth - 15
    }
    // 防止顶部溢出
    if (top < 0) top = 5
    // 防止底部溢出
    if (top + tooltipHeight > chartDom.offsetHeight) {
      top = chartDom.offsetHeight - tooltipHeight - 5
    }
    customTooltip.positionStyle = {
      left: left + 'px',
      top: top + 'px'
    }
    
    // 使用dispatchAction显示axisPointer竖线和高亮数据点
    chartInstance.value.dispatchAction({
      type: 'highlight',
      seriesIndex: 0,
      dataIndex: dataIndex
    })
    chartInstance.value.dispatchAction({
      type: 'showTip',
      seriesIndex: 0,
      dataIndex: dataIndex
    })
  }
  
  const handleMouseLeave = () => {
    customTooltip.visible = false
    if (chartInstance.value) {
      chartInstance.value.dispatchAction({
        type: 'downplay',
        seriesIndex: 0
      })
      chartInstance.value.dispatchAction({
        type: 'hideTip'
      })
    }
  }
  
  // 添加事件监听
  chartDom.addEventListener('mousemove', handleMouseMove)
  chartDom.addEventListener('mouseleave', handleMouseLeave)
  chartDom._tooltipMoveHandler = handleMouseMove
  chartDom._tooltipLeaveHandler = handleMouseLeave
}

// 切换时间范围
const handleTimeRangeChange = (range) => {
  activeTimeRange.value = range
  navTablePage.value = 1 // 重置分页
  // 根据时间范围调用后端接口获取图表数据
  fetchChartData().then(() => {
    nextTick(() => {
      initChart()
    })
  })
}

// 监听窗口大小变化
const handleResize = () => {
  if (chartInstance.value) {
    chartInstance.value.resize()
  }
}

// 监听对话框显示状态
watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      nextTick(() => {
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

// 监听对话框打开，自动加载净值数据
watch(() => props.visible, (newVal) => {
  if (newVal && props.fundData?.fundCode) {
    // 设置基金代码
    navQueryParams.fundCode = props.fundData.fundCode
    
    // 重置时间范围（初始为空）
    navQueryParams.beginTime = ''
    navQueryParams.endTime = ''
    
    // 重置分页
    navTablePage.value = 1
    
    // 自动获取净值数据
    fetchNavData()
    
    // 自动获取echarts图表数据（根据当前选中的时间范围）
    fetchChartData().then(() => {
      nextTick(() => {
        initChart()
      })
    })
    
    // 自动获取基金经理分析数据
    fetchManagerAnalysisData()
    
    // 自动获取基金持仓数据
    fetchHoldingData()
    
    // 自动获取交易数据
    fetchTradeData()
    
    // 自动获取分红数据
    fetchDividendData()
    
    // 自动获取风险收益数据
    fetchRiskPerformanceData()
    
    // 自动获取业绩分析数据
    fetchPerformanceAnalysisData()
    
    // 自动获取持仓数据
    portfolioQueryParams.fundCode = props.fundData.fundCode
    portfolioQueryParams.beginTime = ''
    portfolioQueryParams.endTime = ''
    portfolioTablePage.value = 1
    fetchPortfolioData()
  }
})

// 基金经理分析数据
const managerAnalysisData = ref(null)

// 基金持仓数据
const holdingData = ref(null)

// 持仓数据
const portfolioData = ref([])
const portfolioTableLoading = ref(false)
const portfolioTableTotal = ref(0)
const portfolioTablePage = ref(1)
const portfolioTablePageSize = ref(10)

// 持仓数据查询参数
const portfolioQueryParams = reactive({
  fundCode: '',
  beginTime: '',
  endTime: ''
})

// 获取持仓数据的方法
const fetchPortfolioData = async () => {
  if (!portfolioQueryParams.fundCode) {
    return
  }
  
  portfolioTableLoading.value = true
  try {
    const result = await GetFundPortfolioByConditionAndPage(
      portfolioTablePage.value,
      portfolioTablePageSize.value,
      {
        fundCode: portfolioQueryParams.fundCode,
        beginTime: portfolioQueryParams.beginTime || null,
        endTime: portfolioQueryParams.endTime || null
      }
    )
    
    if (result.code === 200) {
      portfolioData.value = result.data.list || []
      portfolioTableTotal.value = result.data.total || 0
    } else {
      ElMessage.error(result.message || '获取持仓数据失败')
    }
  } catch (error) {
    console.error('获取持仓数据失败:', error)
    ElMessage.error('获取持仓数据失败')
  } finally {
    portfolioTableLoading.value = false
  }
}

// 交易数据
const tradeData = ref([])
const tradeTableLoading = ref(false)
const tradeTableTotal = ref(0)
const tradeTablePage = ref(1)
const tradeTablePageSize = ref(10)

// 交易数据查询参数
const tradeQueryParams = reactive({
  fundCode: '',
  beginTime: '',
  endTime: ''
})

// 分红数据
const dividendData = ref([])
const dividendTableLoading = ref(false)
const dividendTableTotal = ref(0)
const dividendTablePage = ref(1)
const dividendTablePageSize = ref(10)

// 分红数据查询参数
const dividendQueryParams = reactive({
  fundCode: '',
  beginTime: '',
  endTime: ''
})

// 风险收益数据
const riskPerformanceData = ref([])

// 获取基金经理分析数据的方法
const fetchManagerAnalysisData = async () => {
  const fundCode = props.fundData?.fundCode
  if (!fundCode) {
    return
  }
  
  try {
    const result = await GetFundManagerAnalysisByCode(fundCode)
    
    if (result.code === 200 && result.data) {
      // 后端字段映射到前端字段（后端为空时前端也为空，不设置默认值）
      const backendData = result.data
      managerAnalysisData.value = {
        name: backendData.managerName,
        starLevel: backendData.starRating,
        workYears: backendData.workTime,
        manageScale: backendData.manageScale,
        education: backendData.education,
        personalHold: backendData.isManagerHolding,
        awards: backendData.awardRecords,
        overallScore: backendData.totalScore,
        selectionScore: backendData.stockSelectScore,
        returnScore: backendData.returnScore,
        riskScore: backendData.riskControlScore,
        stabilityScore: backendData.stabilityScore,
        timingScore: backendData.timingScore,
        positionConcentration: backendData.holdingsConcentration,
        turnoverRate: backendData.turnoverRate,
        abilityPathMatch: backendData.backgroundMatch,
        scaleControlAbility: backendData.scaleCapability,
        focusLevel: backendData.focusOnThisFund,
        managerDesc: backendData.managerDesc,
        positionConcentrationAnalysis: backendData.concentrationRateAnalyse,
        turnoverRateAnalysis: backendData.turnoverRateAnalyse,
        abilityCircleAnalysis: backendData.capabilityPathAnalysis,
        scaleControlAnalysis: backendData.scaleAbilityAnalysis,
        workBackground: backendData.professionalBackground,
        productManageAnalysis: backendData.productManagementAnalysis,
        stabilityAnalysis: backendData.stabilityAnalysis,
        personalHoldAnalysis: backendData.personalHolding
      }
    } else {
      managerAnalysisData.value = null
    }
  } catch (error) {
    console.error('获取基金经理分析数据失败:', error)
    managerAnalysisData.value = null
  }
}

// 获取基金持仓数据的方法
const fetchHoldingData = async () => {
  const fundCode = props.fundData?.fundCode
  if (!fundCode) {
    return
  }
  
  try {
    const result = await GetFundHoldingByCode(fundCode)
    
    if (result.code === 200 && result.data) {
      // 后端字段映射到前端字段（后端为空时前端也为空，不设置默认值）
      const backendData = result.data
      holdingData.value = {
        holdShares: backendData.holdShares,
        availableShares: backendData.availableShares,
        frozenShares: backendData.frozenShares,
        positionCost: backendData.costAmount,
        positionValue: backendData.marketValue,
        positionProfit: backendData.profitLoss,
        positionProfitRate: backendData.profitLossRate
      }
    } else {
      holdingData.value = null
    }
  } catch (error) {
    console.error('获取基金持仓数据失败:', error)
    holdingData.value = null
  }
}

// 获取交易数据的方法
const fetchTradeData = async () => {
  const fundCode = props.fundData?.fundCode
  if (!fundCode) {
    return
  }
  
  tradeTableLoading.value = true
  try {
    const result = await GetFundTransactionByConditionAndPage(
      tradeTablePage.value,
      tradeTablePageSize.value,
      {
        fundCode: fundCode,
        beginTime: tradeQueryParams.beginTime || null,
        endTime: tradeQueryParams.endTime || null
      }
    )
    
    if (result.code === 200) {
      tradeData.value = result.data.list || []
      tradeTableTotal.value = result.data.total || 0
    } else {
      ElMessage.error(result.message || '获取交易数据失败')
    }
  } catch (error) {
    console.error('获取交易数据失败:', error)
    ElMessage.error('获取交易数据失败')
  } finally {
    tradeTableLoading.value = false
  }
}

// 获取分红数据的方法
const fetchDividendData = async () => {
  const fundCode = props.fundData?.fundCode
  if (!fundCode) {
    return
  }
  
  dividendTableLoading.value = true
  try {
    const result = await GetFundDividendByConditionAndPage(
      dividendTablePage.value,
      dividendTablePageSize.value,
      {
        fundCode: fundCode,
        beginTime: dividendQueryParams.beginTime || null,
        endTime: dividendQueryParams.endTime || null
      }
    )
    
    if (result.code === 200) {
      dividendData.value = result.data.list || []
      dividendTableTotal.value = result.data.total || 0
    } else {
      ElMessage.error(result.message || '获取分红数据失败')
    }
  } catch (error) {
    console.error('获取分红数据失败:', error)
    ElMessage.error('获取分红数据失败')
  } finally {
    dividendTableLoading.value = false
  }
}

// 获取风险收益数据的方法
const fetchRiskPerformanceData = async () => {
  const fundCode = props.fundData?.fundCode
  if (!fundCode) {
    return
  }
  
  try {
    const result = await GetFundRiskPerformanceByCode(fundCode)
    
    if (result.code === 200) {
      riskPerformanceData.value = result.data || []
    } else {
      ElMessage.error(result.message || '获取风险收益数据失败')
    }
  } catch (error) {
    console.error('获取风险收益数据失败:', error)
    ElMessage.error('获取风险收益数据失败')
  }
}

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

// 分析维度标签类型映射
const getAnalysisTagType = (value) => {
  const typeMap = {
    '极高': 'danger',
    '高': 'warning',
    '中': 'info',
    '低': 'success',
    '极低': ''
  }
  return typeMap[value] || 'info'
}

// 获奖记录标签类型映射
const getAwardTagType = (award) => {
  const typeMap = {
    '金牛奖': 'danger',
    '金基金奖': 'warning',
    '明星基金奖': 'success',
    '晨星奖': 'info',
    '英华奖': 'primary',
    '其他': ''
  }
  return typeMap[award] || 'info'
}

// 持仓类型显示方法
const getPositionTypeText = (v) => {
  const map = { 1: '股票', 2: '债券', 3: '基金', 4: '现金' }
  return map[v] || '-'
}

// 持仓类型标签颜色
const getPositionTypeTagType = (v) => {
  const map = { 1: 'danger', 2: 'success', 3: 'warning', 4: 'info' }
  return map[v] || 'info'
}

// 行业分类显示方法
const getIndustryTypeText = (v) => {
  const map = { 1: '通信装备', 2: '电池', 3: '半导体' }
  return map[v] || '-'
}

// 行业分类标签颜色
const getIndustryTypeTagType = (v) => {
  const map = { 1: 'danger', 2: 'success', 3: 'warning' }
  return map[v] || 'info'
}

// 数据来源显示方法
const getDataSourceText = (v) => {
  const map = { 1: '支付宝', 2: '天天基金' }
  return map[v] || '-'
}

// 数据来源标签颜色
const getDataSourceTagType = (v) => {
  const map = { 1: 'primary', 2: 'success' }
  return map[v] || 'info'
}

// 时间周期显示方法
const getPeriodTypeText = (v) => {
  const map = { 1: '近1年', 2: '近3年', 3: '近5年' }
  return map[v] || '-'
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
  overflow: visible;
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
  border-radius: 10px 10px 0 0;
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

/* ====== 时间范围选择器 - 仿支付宝风格 ====== */
.time-range-selector {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.time-range-tabs {
  display: flex;
  gap: 0;
  width: fit-content;
}

/* ====== 右上角业绩显示 - 仿支付宝风格 ====== */
.performance-badge {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}

.performance-value {
  font-size: 16px;
  font-weight: 700;
  font-family: 'DIN Alternate', 'Helvetica Neue', Arial, sans-serif;
  line-height: 1.2;
}

.performance-label {
  font-size: 11px;
  color: #999;
  font-weight: 400;
}

.time-range-tab {
  padding: 6px 14px;
  font-size: 13px;
  color: #666;
  background: #f5f5f5;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
  font-weight: 400;
  border-right: 1px solid #e8e8e8;
}

.time-range-tab:first-child {
  border-radius: 4px 0 0 4px;
}

.time-range-tab:last-child {
  border-radius: 0 4px 4px 0;
  border-right: none;
}

.time-range-tab:hover {
  color: #1677FF;
  background: #e6f4ff;
}

.time-range-tab.active {
  color: #fff;
  background: #1677FF;
  font-weight: 500;
}

/* ====== 图表容器 - 仿支付宝风格 ====== */
.nav-chart-container {
  width: 100%;
  height: 350px;
  margin-bottom: 16px;
}

/* ====== 自定义图表tooltip - 仿支付宝风格 ====== */
.custom-chart-tooltip {
  position: absolute;
  z-index: 100;
  min-width: 120px;
  padding: 6px 10px;
  background: rgba(0, 0, 0, 0.65);
  border-radius: 4px;
  pointer-events: none;
  font-size: 11px;
  line-height: 1.5;
}
.custom-chart-tooltip .tooltip-date {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 4px;
  padding-bottom: 3px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
}
.custom-chart-tooltip .tooltip-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2px;
}
.custom-chart-tooltip .tooltip-row:last-child {
  margin-bottom: 0;
}
.custom-chart-tooltip .tooltip-label {
  color: rgba(255, 255, 255, 0.7);
  font-size: 10px;
}
.custom-chart-tooltip .tooltip-value {
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  margin-left: 10px;
}
.custom-chart-tooltip .tooltip-value-dark {
  color: #fff;
}

/* ====== 净值表格包装器 ====== */
.nav-table-wrapper {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

/* ====== 净值表格分页 ====== */
.nav-table-pagination {
  margin-top: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 8px 0;
  background: #fafbfc;
  border-radius: 6px;
  flex-shrink: 0;
  overflow-x: auto;
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

/* ====== 基金经理分析样式 ====== */
.score-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  margin: 16px 0;
}

.score-item {
  text-align: center;
  padding: 12px 8px;
  background: linear-gradient(135deg, #e8f4f8 0%, #d1e8f0 100%);
  border-radius: 8px;
  border: 1px solid #b8dce8;
  transition: all 0.3s ease;
}

.score-item:hover {
  border-color: #1e3c72;
  box-shadow: 0 2px 8px rgba(30, 60, 114, 0.15);
  transform: translateY(-2px);
}

.score-label {
  font-size: 12px;
  color: #606266;
  margin-bottom: 6px;
}

.score-value {
  font-size: 20px;
  font-weight: 700;
  color: #1e3c72;
}

.analysis-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  margin: 16px 0;
}

.analysis-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  background: #fafbfc;
  border-radius: 6px;
  border: 1px solid #e8ecf1;
  transition: all 0.3s ease;
}

.analysis-item:hover {
  border-color: #1e3c72;
  background: #f5f7fa;
}

.analysis-item label {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
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

/* ====== 净值表格与业绩分析并排布局 ====== */
.nav-and-analysis-wrapper {
  display: flex;
  gap: 16px;
  margin-top: 16px;
}

.nav-and-analysis-wrapper .nav-table-wrapper {
  flex: 1;
  min-width: 0;
}

/* ====== 业绩分析块 ====== */
.performance-analysis-wrapper {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  border: 1px solid #e8ecf1;
  border-radius: 8px;
  padding: 10px 12px;
  background: #fafbfc;
  height: 452px;
  overflow: hidden;
}

.analysis-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.analysis-section-title {
  font-size: 13px;
  font-weight: 700;
  color: #2c3e50;
}

/* ====== 业绩分析标签页 ====== */
.analysis-time-range-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
  flex-shrink: 0;
}

.analysis-time-tab {
  padding: 3px 8px;
  font-size: 11px;
  color: #666;
  background: #f5f5f5;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
  font-weight: 400;
  border-right: 1px solid #e8e8e8;
}

.analysis-time-tab:first-child {
  border-radius: 4px 0 0 4px;
}

.analysis-time-tab:last-child {
  border-radius: 0 4px 4px 0;
  border-right: none;
}

.analysis-time-tab:hover {
  color: #1677FF;
  background: #e6f4ff;
}

.analysis-time-tab.active {
  color: #fff;
  background: #1677FF;
  font-weight: 500;
}

/* ====== 业绩分析数据卡片容器 ====== */
.analysis-cards-container {
  flex: 1;
  overflow-y: auto;
  padding-right: 4px;
}

.analysis-cards-container::-webkit-scrollbar {
  width: 4px;
}

.analysis-cards-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 2px;
}

.analysis-cards-container::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 2px;
}

/* ====== 业绩分析分组 ====== */
.analysis-card-group {
  margin-bottom: 8px;
}

.analysis-card-group:last-child {
  margin-bottom: 0;
}

.analysis-card-group-title {
  font-size: 11px;
  font-weight: 600;
  color: #909399;
  margin-bottom: 6px;
  padding-left: 6px;
  border-left: 2px solid #1e3c72;
}

.analysis-cards-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 6px;
}

/* ====== 业绩分析指标小卡片 ====== */
.analysis-metric-card {
  padding: 6px 8px;
  background: #fff;
  border-radius: 4px;
  border: 1px solid #e8ecf1;
  transition: all 0.2s ease;
}

.analysis-metric-card:hover {
  border-color: #1e3c72;
  box-shadow: 0 1px 4px rgba(30, 60, 114, 0.1);
}

.analysis-metric-card .metric-label {
  font-size: 10px;
  color: #909399;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.analysis-metric-card .metric-value {
  font-size: 14px;
  font-weight: 700;
  color: #303133;
  font-family: 'DIN Alternate', 'Helvetica Neue', Arial, sans-serif;
}
</style>
