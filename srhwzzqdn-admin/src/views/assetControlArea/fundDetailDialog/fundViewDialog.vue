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
      <div class="info-card" v-if="holdingData">
        <div class="card-header">
          <el-icon><Wallet /></el-icon>
          <span>持仓信息</span>
        </div>
        <div class="card-body">
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
              v-loading="navTableLoading" 
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

      <!-- 基金经理分析卡片 -->
      <div class="info-card" v-if="managerAnalysisData">
        <div class="card-header">
          <el-icon><User /></el-icon>
          <span>基金经理分析</span>
        </div>
        <div class="card-body">
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
      </div>

      <!-- 交易记录卡片 -->
      <div class="info-card" v-if="tradeData && tradeData.length > 0">
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
      <div class="info-card" v-if="dividendData && dividendData.length > 0">
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

      <!-- 持仓数据卡片 -->
      <div class="info-card" v-if="portfolioData && portfolioData.length > 0">
        <div class="card-header">
          <el-icon><Grid /></el-icon>
          <span>持仓数据</span>
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
                <el-tag size="small">{{ getDisplayText(row.holdingType, positionTypeOptions) }}</el-tag>
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
            <el-table-column prop="industryClass" label="行业分类" width="100">
              <template #default="{ row }">
                {{ getDisplayText(row.industryClass, sectorTypeOptions) }}
              </template>
            </el-table-column>
            <el-table-column prop="dataSource" label="数据来源" width="100">
              <template #default="{ row }">
                {{ getDisplayText(row.dataSource, dataSourceOptions) }}
              </template>
            </el-table-column>
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
import { Coin, Close, FullScreen, Aim, Document, Discount, Wallet, TrendCharts, List, Money, DataAnalysis, Grid, User } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { GetFundNavByConditionAndPage, GetFundManagerAnalysisByCode, GetFundHoldingByCode, GetFundTransactionByConditionAndPage, GetFundDividendByConditionAndPage, GetFundRiskPerformanceByCode, GetFundPortfolioByConditionAndPage } from "@/api/fundAsset"
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
</style>
