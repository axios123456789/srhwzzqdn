<template>
  <div class="daily-review-div">
    <!-- 查询条件 -->
    <div class="search-div">
      <div class="search-header">
        <span class="search-title">查询条件</span>
      </div>
      <el-form label-width="80px" size="small">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="复盘日期">
              <el-date-picker v-model="dailyReviewTimeArea" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" style="width: 100%" value-format="YYYY-MM-DD" :unlink-panels="true" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="市场状态">
              <el-select v-model="dailyReviewQueryDto.marketStatus" style="width: 100%" clearable multiple placeholder="请选择">
                <el-option v-for="item in reviewMarketStatusOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="情绪温度">
              <el-select v-model="dailyReviewQueryDto.emotionTemp" style="width: 100%" clearable multiple placeholder="请选择">
                <el-option v-for="item in reviewEmotionTempOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="适配体系">
              <el-select v-model="dailyReviewQueryDto.adaptSystem" style="width: 100%" clearable multiple placeholder="请选择">
                <el-option v-for="item in reviewAdaptSystemOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24" style="text-align: right;">
            <el-form-item label-width="10px">
              <el-button type="primary" size="small" @click="searchDailyReviewData"><el-icon><Search /></el-icon>搜索</el-button>
              <el-button size="small" @click="resetDailyReviewData"><el-icon><Refresh /></el-icon>重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>
    <div class="tools-div beautified-tools" style="text-align: right;">
      <el-button type="success" size="small" @click="addDailyReview">
        <el-icon><DocumentAdd /></el-icon>
        添加复盘
      </el-button>
      <el-button type="danger" size="small" @click="deleteDailyReviewAll" :disabled="dailyReviewSelectedRows.length === 0">
        <el-icon><Delete /></el-icon>
        批量删除
      </el-button>
    </div>
    <el-table :data="dailyReviewList" v-loading="dailyReviewLoading" style="width: 100%" height="500" border stripe size="small" @selection-change="handleDailyReviewSelectionChange">
      <el-table-column type="selection" width="40" align="center" />
      <el-table-column label="操作" align="center" fixed="left" width="240" #default="scope">
        <el-button type="info" size="small" @click="viewDailyReviewDetail(scope.row)"><el-icon><View /></el-icon>查看</el-button>
        <el-button type="primary" size="small" @click="editDailyReview(scope.row)"><el-icon><Edit /></el-icon>编辑</el-button>
        <el-button type="danger" size="small" @click="deleteDailyReview(scope.row)"><el-icon><Delete /></el-icon>删除</el-button>
      </el-table-column>
      <el-table-column prop="reviewDate" label="复盘日期" align="center" width="110" />
      <el-table-column prop="marketStatus" label="市场状态" align="center" width="100">
        <template #default="scope">{{ getDisplayText(scope.row.marketStatus, reviewMarketStatusOptions) }}</template>
      </el-table-column>
      <el-table-column prop="emotionTemp" label="情绪温度" align="center" width="90">
        <template #default="scope">
          <el-tag :type="getEmotionTagType(scope.row.emotionTemp)" size="small">{{ getDisplayText(scope.row.emotionTemp, reviewEmotionTempOptions) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="两市成交额(亿)" align="center" width="120">
        <template #default="scope">{{ scope.row.totalAmount != null ? Number(scope.row.totalAmount).toFixed(2) : '-' }}</template>
      </el-table-column>
      <el-table-column prop="limitUpCount" label="涨停" align="center" width="70" />
      <el-table-column prop="limitDownCount" label="跌停" align="center" width="70" />
      <el-table-column prop="mainSector1" label="主线板块1" align="center" width="100">
        <template #default="scope">{{ getDisplayText(scope.row.mainSector1, reviewSectorOptions) }}</template>
      </el-table-column>
      <el-table-column prop="leaderStockName" label="龙头股" align="center" width="100" show-overflow-tooltip />
      <el-table-column prop="adaptSystem" label="适配体系" align="center" width="100">
        <template #default="scope">{{ getDisplayText(scope.row.adaptSystem, reviewAdaptSystemOptions) }}</template>
      </el-table-column>
      <el-table-column prop="planPositionLimit" label="计划仓位%" align="center" width="90">
        <template #default="scope">{{ scope.row.planPositionLimit != null ? Number(scope.row.planPositionLimit).toFixed(1) + '%' : '-' }}</template>
      </el-table-column>
      <el-table-column prop="dailyProfitPct" label="当日盈亏%" align="center" width="90">
        <template #default="scope">
          <span :style="{color: scope.row.dailyProfitPct > 0 ? '#F56C6C' : scope.row.dailyProfitPct < 0 ? '#67C23A' : '#909399'}">
            {{ scope.row.dailyProfitPct != null ? Number(scope.row.dailyProfitPct).toFixed(2) + '%' : '-' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="operationSelfRating" label="操作自评" align="center" width="90">
        <template #default="scope">{{ getDisplayText(scope.row.operationSelfRating, reviewSelfRatingOptions) }}</template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top: 15px" v-model:current-page="dailyReviewPageParams.page" v-model:page-size="dailyReviewPageParams.limit" :page-sizes="PAGE_SIZES" @size-change="fetchDailyReviewData" @current-change="fetchDailyReviewData" layout="total, sizes, prev, pager, next" :total="dailyReviewTotal" />
  </div>

  <!-- 每日复盘编辑对话框 -->
  <el-dialog v-model="dailyReviewDialogVisible" :title="dailyReviewDialogTitle" width="80%" class="custom-dialog enhanced-dialog" :close-on-click-modal="false">
    <el-form :model="dailyReviewFormData" ref="dailyReviewFormRef" label-width="120px" size="small">
      <div class="ai-fill-bar">
        <el-button type="primary" plain size="small" @click="smartFillAll"><el-icon><MagicStick /></el-icon> 智能填充</el-button>
        <el-button type="success" plain size="small" :loading="marketDataLoading" @click="fetchMarketData"><el-icon><Download /></el-icon> 获取市场数据</el-button>
        <span class="ai-fill-tip">按复盘日期获取对应交易日市场数据自动填充涨跌/成交额/涨跌家数；智能填充根据基础数据推荐市场状态→情绪温度→适配体系→仓位/止损止盈</span>
      </div>
      <el-divider content-position="left">大盘环境</el-divider>
      <el-row :gutter="20">
        <el-col :span="8"><el-form-item label="复盘日期" prop="reviewDate" :rules="[{required:true,message:'请选择日期',trigger:'change'}]"><el-date-picker v-model="dailyReviewFormData.reviewDate" type="date" style="width:100%" value-format="YYYY-MM-DD" /></el-form-item></el-col>
        <el-col :span="8"><el-form-item label="市场状态" prop="marketStatus" :rules="[{required:true,message:'请选择',trigger:'change'}]"><div class="linkage-field"><el-select v-model="dailyReviewFormData.marketStatus" style="flex:1" clearable><el-option v-for="item in reviewMarketStatusOptions" :key="item.value" :label="item.text" :value="item.value" /></el-select><el-button text type="primary" size="small" class="recommend-btn" @click="recommendMarketStatus">💡</el-button></div></el-form-item></el-col>
        <el-col :span="8"><el-form-item label="两市成交额(亿)"><el-input-number v-model="dailyReviewFormData.totalAmount" :precision="2" style="width:100%" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="上证涨跌%"><el-input-number v-model="dailyReviewFormData.shChangePct" :precision="2" style="width:100%" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="深证涨跌%"><el-input-number v-model="dailyReviewFormData.szChangePct" :precision="2" style="width:100%" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="创业板涨跌%"><el-input-number v-model="dailyReviewFormData.cybChangePct" :precision="2" style="width:100%" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="上涨家数"><el-input-number v-model="dailyReviewFormData.riseCount" style="width:100%" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="下跌家数"><el-input-number v-model="dailyReviewFormData.fallCount" style="width:100%" /></el-form-item></el-col>
      </el-row>
      <el-divider content-position="left">情绪指标</el-divider>
      <el-row :gutter="20">
        <el-col :span="8"><el-form-item label="情绪温度" prop="emotionTemp" :rules="[{required:true,message:'请选择',trigger:'change'}]"><div class="linkage-field"><el-select v-model="dailyReviewFormData.emotionTemp" style="flex:1" clearable><el-option v-for="item in reviewEmotionTempOptions" :key="item.value" :label="item.text" :value="item.value" /></el-select><el-button text type="primary" size="small" class="recommend-btn" @click="recommendEmotionTemp">💡</el-button></div></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="涨停家数"><el-input-number v-model="dailyReviewFormData.limitUpCount" style="width:100%" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="跌停家数"><el-input-number v-model="dailyReviewFormData.limitDownCount" style="width:100%" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="连板家数"><el-input-number v-model="dailyReviewFormData.continuousBoardCount" style="width:100%" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="昨涨停溢价%"><el-input-number v-model="dailyReviewFormData.yesterdayPremiumPct" :precision="2" style="width:100%" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="北向资金(亿)"><el-input-number v-model="dailyReviewFormData.northFlowAmount" :precision="2" style="width:100%" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="炸板家数"><el-input-number v-model="dailyReviewFormData.brokenBoardCount" style="width:100%" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="炸板率%"><div class="linkage-field"><el-input-number v-model="dailyReviewFormData.brokenBoardRate" :precision="2" style="flex:1" /><el-button text type="primary" size="small" class="recommend-btn" @click="calcBrokenBoardRate">🔢</el-button></div></el-form-item></el-col>
      </el-row>
      <el-divider content-position="left">主线与龙头</el-divider>
      <el-row :gutter="20">
        <el-col :span="8"><el-form-item label="主线板块1"><el-select v-model="dailyReviewFormData.mainSector1" style="width:100%" clearable filterable><el-option v-for="item in reviewSectorOptions" :key="item.value" :label="item.text" :value="item.value" /></el-select></el-form-item></el-col>
        <el-col :span="8"><el-form-item label="主线板块2"><el-select v-model="dailyReviewFormData.mainSector2" style="width:100%" clearable filterable><el-option v-for="item in reviewSectorOptions" :key="item.value" :label="item.text" :value="item.value" /></el-select></el-form-item></el-col>
        <el-col :span="8"><el-form-item label="主线板块3"><el-select v-model="dailyReviewFormData.mainSector3" style="width:100%" clearable filterable><el-option v-for="item in reviewSectorOptions" :key="item.value" :label="item.text" :value="item.value" /></el-select></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="板块涨停数"><el-input-number v-model="dailyReviewFormData.sectorLimitUpCount" style="width:100%" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="龙头股名称"><el-input v-model="dailyReviewFormData.leaderStockName" style="width:100%" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="龙头股代码"><el-input v-model="dailyReviewFormData.leaderStockCode" style="width:100%" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="龙头涨停时间"><el-date-picker v-model="dailyReviewFormData.leaderLimitUpTime" type="datetime" style="width:100%" value-format="YYYY-MM-DD HH:mm:ss" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="封单金额(亿)"><el-input-number v-model="dailyReviewFormData.leaderSealAmount" :precision="2" style="width:100%" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="龙头连板数"><el-input-number v-model="dailyReviewFormData.leaderContinuousBoard" style="width:100%" /></el-form-item></el-col>
      </el-row>
      <el-divider content-position="left">次日策略</el-divider>
      <el-row :gutter="20">
        <el-col :span="8"><el-form-item label="适配体系" prop="adaptSystem" :rules="[{required:true,message:'请选择',trigger:'change'}]"><div class="linkage-field"><el-select v-model="dailyReviewFormData.adaptSystem" style="flex:1" clearable><el-option v-for="item in reviewAdaptSystemOptions" :key="item.value" :label="item.text" :value="item.value" /></el-select><el-button text type="primary" size="small" class="recommend-btn" @click="recommendAdaptSystem">💡</el-button></div></el-form-item></el-col>
        <el-col :span="8"><el-form-item label="计划仓位上限%"><div class="linkage-field"><el-slider v-model="dailyReviewFormData.planPositionLimit" :min="0" :max="100" show-input style="flex:1" /><el-button text type="primary" size="small" class="recommend-btn" @click="recommendPositionLimit">💡</el-button></div></el-form-item></el-col>
        <el-col :span="8"><el-form-item label="止损线%"><div class="linkage-field"><el-input-number v-model="dailyReviewFormData.stopLossPct" :precision="2" style="flex:1" /><el-button text type="primary" size="small" class="recommend-btn" @click="recommendStopLossTakeProfit">💡</el-button></div></el-form-item></el-col>
        <el-col :span="8"><el-form-item label="止盈线%"><div class="linkage-field"><el-input-number v-model="dailyReviewFormData.takeProfitPct" :precision="2" style="flex:1" /><el-button text type="primary" size="small" class="recommend-btn" @click="recommendStopLossTakeProfit">💡</el-button></div></el-form-item></el-col>
        <el-col :span="24"><el-form-item label="关注标的"><el-input v-model="dailyReviewFormData.watchTargets" style="width:100%" placeholder="多个用逗号分隔，如：万润股份002643、华电辽能600796" /></el-form-item></el-col>
        <el-col :span="24">
          <div style="margin-bottom: 8px;">
            <el-button type="primary" plain size="small" :loading="aiTargetLoading" @click="aiAnalyzeTargets">
              <span v-if="!aiTargetLoading">🤖 AI 分析标的（生成买入条件+风险预警）</span>
              <span v-else>AI 分析中...</span>
            </el-button>
          </div>
        </el-col>
        <el-col :span="12"><el-form-item label="买入条件"><el-input v-model="dailyReviewFormData.buyCondition" type="textarea" :rows="3" style="width:100%" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="风险预警"><el-input v-model="dailyReviewFormData.riskWarning" type="textarea" :rows="3" style="width:100%" /></el-form-item></el-col>
      </el-row>
      <el-divider content-position="left">持仓与自评</el-divider>
      <el-row :gutter="20">
        <el-col :span="8"><el-form-item label="操作自评"><div class="linkage-field"><el-select v-model="dailyReviewFormData.operationSelfRating" style="flex:1" clearable><el-option v-for="item in reviewSelfRatingOptions" :key="item.value" :label="item.text" :value="item.value" /></el-select><el-button text type="primary" size="small" class="recommend-btn" @click="recommendSelfRating">💡</el-button></div></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="当日盈亏%"><el-input-number v-model="dailyReviewFormData.dailyProfitPct" :precision="2" style="width:100%" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="持仓盈亏%"><el-input-number v-model="dailyReviewFormData.positionProfitPct" :precision="2" style="width:100%" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="交易次数"><el-input-number v-model="dailyReviewFormData.tradeCount" style="width:100%" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="盈利交易次数"><el-input-number v-model="dailyReviewFormData.winTradeCount" style="width:100%" /></el-form-item></el-col>
        <el-col :span="24"><el-form-item label="今日操作记录"><el-input v-model="dailyReviewFormData.todayOperation" type="textarea" :rows="2" style="width:100%" /></el-form-item></el-col>
      </el-row>
      <el-divider content-position="left">
        <span>总结反思</span>
        <el-button type="primary" plain size="small" :loading="aiReviewLoading" @click="aiGenerateReviewSummary" style="margin-left: 12px;">
          <span v-if="!aiReviewLoading">🤖 AI 生成总结</span>
          <span v-else>AI 生成中...</span>
        </el-button>
      </el-divider>
      <el-row :gutter="20">
        <el-col :span="12"><el-form-item label="经验总结"><el-input v-model="dailyReviewFormData.experience" type="textarea" :rows="2" style="width:100%" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="教训反思"><el-input v-model="dailyReviewFormData.lesson" type="textarea" :rows="2" style="width:100%" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="改进点"><el-input v-model="dailyReviewFormData.improvePoint" type="textarea" :rows="2" style="width:100%" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="明日关注重点"><el-input v-model="dailyReviewFormData.tomorrowFocus" type="textarea" :rows="2" style="width:100%" /></el-form-item></el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button @click="dailyReviewDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitDailyReview">提交</el-button>
    </template>
  </el-dialog>

  <!-- 每日复盘详情查看对话框 -->
  <el-dialog v-model="dailyReviewDetailVisible" title="复盘详情" width="70%" class="custom-dialog enhanced-dialog" :close-on-click-modal="true">
    <div class="pred-detail-container" v-if="dailyReviewDetailData">
      <div class="detail-section">
        <div class="detail-section-title">大盘环境</div>
        <el-descriptions :column="3" border size="default" label-width="100px">
          <el-descriptions-item label="复盘日期"><span class="detail-value highlight">{{ dailyReviewDetailData.reviewDate || '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="市场状态"><el-tag size="small">{{ getDisplayText(dailyReviewDetailData.marketStatus, reviewMarketStatusOptions) }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="两市成交额"><span class="detail-value">{{ dailyReviewDetailData.totalAmount != null ? Number(dailyReviewDetailData.totalAmount).toFixed(2) + ' 亿' : '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="上证涨跌%"><span class="detail-value" :style="{color: dailyReviewDetailData.shChangePct > 0 ? '#F56C6C' : '#67C23A'}">{{ dailyReviewDetailData.shChangePct != null ? Number(dailyReviewDetailData.shChangePct).toFixed(2) + '%' : '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="深证涨跌%"><span class="detail-value" :style="{color: dailyReviewDetailData.szChangePct > 0 ? '#F56C6C' : '#67C23A'}">{{ dailyReviewDetailData.szChangePct != null ? Number(dailyReviewDetailData.szChangePct).toFixed(2) + '%' : '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="创业板涨跌%"><span class="detail-value" :style="{color: dailyReviewDetailData.cybChangePct > 0 ? '#F56C6C' : '#67C23A'}">{{ dailyReviewDetailData.cybChangePct != null ? Number(dailyReviewDetailData.cybChangePct).toFixed(2) + '%' : '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="上涨家数"><span class="detail-value">{{ dailyReviewDetailData.riseCount || '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="下跌家数"><span class="detail-value">{{ dailyReviewDetailData.fallCount || '-' }}</span></el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="detail-section">
        <div class="detail-section-title">情绪指标</div>
        <el-descriptions :column="3" border size="default" label-width="100px">
          <el-descriptions-item label="情绪温度"><el-tag :type="getEmotionTagType(dailyReviewDetailData.emotionTemp)" size="small">{{ getDisplayText(dailyReviewDetailData.emotionTemp, reviewEmotionTempOptions) }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="涨停家数"><span class="detail-value">{{ dailyReviewDetailData.limitUpCount || '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="跌停家数"><span class="detail-value">{{ dailyReviewDetailData.limitDownCount || '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="连板家数"><span class="detail-value">{{ dailyReviewDetailData.continuousBoardCount || '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="昨涨停溢价%"><span class="detail-value">{{ dailyReviewDetailData.yesterdayPremiumPct != null ? Number(dailyReviewDetailData.yesterdayPremiumPct).toFixed(2) + '%' : '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="北向资金"><span class="detail-value">{{ dailyReviewDetailData.northFlowAmount != null ? Number(dailyReviewDetailData.northFlowAmount).toFixed(2) + ' 万' : '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="炸板家数"><span class="detail-value">{{ dailyReviewDetailData.brokenBoardCount || '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="炸板率%"><span class="detail-value">{{ dailyReviewDetailData.brokenBoardRate != null ? Number(dailyReviewDetailData.brokenBoardRate).toFixed(2) + '%' : '-' }}</span></el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="detail-section">
        <div class="detail-section-title">主线与龙头</div>
        <el-descriptions :column="3" border size="default" label-width="100px">
          <el-descriptions-item label="主线板块1">{{ getDisplayText(dailyReviewDetailData.mainSector1, reviewSectorOptions) || '-' }}</el-descriptions-item>
          <el-descriptions-item label="主线板块2">{{ getDisplayText(dailyReviewDetailData.mainSector2, reviewSectorOptions) || '-' }}</el-descriptions-item>
          <el-descriptions-item label="主线板块3">{{ getDisplayText(dailyReviewDetailData.mainSector3, reviewSectorOptions) || '-' }}</el-descriptions-item>
          <el-descriptions-item label="板块涨停数"><span class="detail-value">{{ dailyReviewDetailData.sectorLimitUpCount || '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="龙头股名称"><span class="detail-value highlight">{{ dailyReviewDetailData.leaderStockName || '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="龙头股代码"><span class="detail-value">{{ dailyReviewDetailData.leaderStockCode || '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="涨停时间"><span class="detail-value">{{ dailyReviewDetailData.leaderLimitUpTime || '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="封单金额"><span class="detail-value">{{ dailyReviewDetailData.leaderSealAmount != null ? Number(dailyReviewDetailData.leaderSealAmount).toFixed(2) + ' 万' : '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="龙头连板数"><span class="detail-value">{{ dailyReviewDetailData.leaderContinuousBoard || '-' }}</span></el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="detail-section">
        <div class="detail-section-title">次日策略</div>
        <el-descriptions :column="3" border size="default" label-width="100px">
          <el-descriptions-item label="适配体系"><el-tag size="small" type="warning">{{ getDisplayText(dailyReviewDetailData.adaptSystem, reviewAdaptSystemOptions) }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="计划仓位上限"><span class="detail-value">{{ dailyReviewDetailData.planPositionLimit != null ? dailyReviewDetailData.planPositionLimit + '%' : '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="止损线%"><span class="detail-value" style="color:#67C23A">{{ dailyReviewDetailData.stopLossPct != null ? Number(dailyReviewDetailData.stopLossPct).toFixed(2) + '%' : '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="止盈线%"><span class="detail-value" style="color:#F56C6C">{{ dailyReviewDetailData.takeProfitPct != null ? Number(dailyReviewDetailData.takeProfitPct).toFixed(2) + '%' : '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="关注标的" :span="2"><span class="detail-value">{{ dailyReviewDetailData.watchTargets || '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="买入条件" :span="3"><div class="detail-text-block">{{ dailyReviewDetailData.buyCondition || '-' }}</div></el-descriptions-item>
          <el-descriptions-item label="风险预警" :span="3"><div class="detail-text-block">{{ dailyReviewDetailData.riskWarning || '-' }}</div></el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="detail-section">
        <div class="detail-section-title">持仓与自评</div>
        <el-descriptions :column="3" border size="default" label-width="100px">
          <el-descriptions-item label="操作自评"><el-tag size="small">{{ getDisplayText(dailyReviewDetailData.operationSelfRating, reviewSelfRatingOptions) }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="当日盈亏%"><span class="detail-value" :style="{color: dailyReviewDetailData.dailyProfitPct > 0 ? '#F56C6C' : '#67C23A'}">{{ dailyReviewDetailData.dailyProfitPct != null ? Number(dailyReviewDetailData.dailyProfitPct).toFixed(2) + '%' : '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="持仓盈亏%"><span class="detail-value" :style="{color: dailyReviewDetailData.positionProfitPct > 0 ? '#F56C6C' : '#67C23A'}">{{ dailyReviewDetailData.positionProfitPct != null ? Number(dailyReviewDetailData.positionProfitPct).toFixed(2) + '%' : '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="交易次数"><span class="detail-value">{{ dailyReviewDetailData.tradeCount || '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="盈利交易次数"><span class="detail-value">{{ dailyReviewDetailData.winTradeCount || '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="今日操作记录" :span="3"><div class="detail-text-block">{{ dailyReviewDetailData.todayOperation || '-' }}</div></el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="detail-section">
        <div class="detail-section-title">总结反思</div>
        <el-descriptions :column="2" border size="default" label-width="100px">
          <el-descriptions-item label="经验总结"><div class="detail-text-block">{{ dailyReviewDetailData.experience || '-' }}</div></el-descriptions-item>
          <el-descriptions-item label="教训反思"><div class="detail-text-block">{{ dailyReviewDetailData.lesson || '-' }}</div></el-descriptions-item>
          <el-descriptions-item label="改进点"><div class="detail-text-block">{{ dailyReviewDetailData.improvePoint || '-' }}</div></el-descriptions-item>
          <el-descriptions-item label="明日关注重点"><div class="detail-text-block">{{ dailyReviewDetailData.tomorrowFocus || '-' }}</div></el-descriptions-item>
        </el-descriptions>
      </div>
    </div>
    <template #footer><el-button @click="dailyReviewDetailVisible = false">关闭</el-button></template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, DocumentAdd, Delete, Edit, View, MagicStick, Download } from '@element-plus/icons-vue'
import { GetKeyAndValueByType } from "@/api/sysDict"
import { GetDailyReviewByConditionAndPage, SaveDailyReview, DeleteDailyReviewById, DeleteAllDailyReviewByIds, AiGenerateDailyReview, AiAnalyzeTargets, FetchRealtimeMarketData } from "@/api/trialExecutionArea/dailyReview"
import { StatTradeByReviewDate } from "@/api/trialExecutionArea/tradeRecord"
import { getDisplayText } from "@/utils/common"

// ==================== 通用常量 ====================
const PAGE_SIZES = [10, 20, 50, 100]
const DEBOUNCE_DELAY = 300

// ==================== 数据字典 ====================
const loadDict = async (tableName, optionsRef) => {
  const result = await GetKeyAndValueByType(tableName)
  optionsRef.value = result.data || []
}

const reviewMarketStatusOptions = ref([])
const reviewEmotionTempOptions = ref([])
const reviewSectorOptions = ref([])
const reviewAdaptSystemOptions = ref([])
const reviewSelfRatingOptions = ref([])

// 情绪温度标签类型
const getEmotionTagType = (val) => {
  const map = { 1: 'info', 2: 'primary', 3: 'warning', 4: 'danger', 5: 'danger' }
  return map[val] || 'info'
}

// ==================== 每日复盘管理 ====================
const dailyReviewList = ref([])
const dailyReviewTotal = ref(0)
const dailyReviewPageParams = reactive({ page: 1, limit: 10 })
const dailyReviewTimeArea = ref([])
const dailyReviewSelectedRows = ref([])
const dailyReviewQueryDto = reactive({ reviewDateStart: null, reviewDateEnd: null, marketStatus: [], emotionTemp: [], adaptSystem: [] })
const handleDailyReviewSelectionChange = (selection) => { dailyReviewSelectedRows.value = selection }

const dailyReviewLoading = ref(false)
const fetchDailyReviewData = async () => {
  dailyReviewLoading.value = true
  try {
    const result = await GetDailyReviewByConditionAndPage(dailyReviewPageParams.page, dailyReviewPageParams.limit, dailyReviewQueryDto)
    if (result.code === 200) {
      dailyReviewList.value = result.data.list || []
      dailyReviewTotal.value = result.data.total || 0
    }
  } catch (e) { ElMessage.error("查询每日复盘失败") }
  finally { dailyReviewLoading.value = false }
}
const searchDailyReviewData = () => {
  dailyReviewQueryDto.reviewDateStart = dailyReviewTimeArea.value?.[0] || null
  dailyReviewQueryDto.reviewDateEnd = dailyReviewTimeArea.value?.[1] || null
  dailyReviewPageParams.page = 1
  fetchDailyReviewData()
}
const resetDailyReviewData = () => {
  dailyReviewTimeArea.value = []
  Object.assign(dailyReviewQueryDto, { reviewDateStart: null, reviewDateEnd: null, marketStatus: [], emotionTemp: [], adaptSystem: [] })
  dailyReviewPageParams.page = 1
  fetchDailyReviewData()
}

// ==================== 复盘表单联动推荐引擎 ====================
const calcBrokenBoardRate = () => {
  const broken = dailyReviewFormData.brokenBoardCount
  const limitUp = dailyReviewFormData.limitUpCount
  if (broken == null || limitUp == null || limitUp <= 0) {
    ElMessage.warning("请先填报：炸板家数和涨停家数")
    return
  }
  dailyReviewFormData.brokenBoardRate = Number((broken / limitUp * 100).toFixed(2))
  ElMessage.success("已计算炸板率：" + dailyReviewFormData.brokenBoardRate + "%")
}

const recommendMarketStatus = () => {
  const f = dailyReviewFormData
  const filled = [f.shChangePct, f.szChangePct, f.cybChangePct, f.limitUpCount, f.limitDownCount, f.riseCount, f.fallCount].filter(v => v != null).length
  if (filled < 4) { ElMessage.warning("请先填报：指数涨跌%、涨停/跌停家数、上涨/下跌家数（至少4项）"); return }
  const sh = f.shChangePct || 0, sz = f.szChangePct || 0, cyb = f.cybChangePct || 0
  const limitUp = f.limitUpCount || 0, limitDown = f.limitDownCount || 1
  const riseCount = f.riseCount || 0, fallCount = f.fallCount || 1
  const avgIdx = (sh + sz + cyb) / 3
  const limitRatio = limitUp / limitDown
  const riseFallRatio = riseCount / fallCount
  const allUp = sh > 0 && sz > 0 && cyb > 0
  const allDown = sh < 0 && sz < 0 && cyb < 0
  let result = 3
  if (allUp && avgIdx > 1 && riseFallRatio > 2 && limitRatio > 3) result = 5
  else if (avgIdx > 0.3 && riseFallRatio > 1.2 && limitRatio > 1.5) result = 4
  else if (Math.abs(avgIdx) < 0.5 && riseFallRatio > 0.8 && riseFallRatio < 1.2 && limitRatio > 0.5 && limitRatio < 2) result = 3
  else if (allDown && avgIdx < -1 && limitUp > 30) result = 1
  else if (allDown && limitUp > 20 && limitRatio > 0.8) result = 2
  else if (avgIdx < 0 && riseFallRatio > 0.8) result = 2
  else if (avgIdx > 0 && (limitDown > 20 || riseFallRatio < 0.5)) result = 1
  else if (avgIdx > 0.5) result = 4
  else if (avgIdx < -0.5) result = 2
  dailyReviewFormData.marketStatus = result
  ElMessage.success("已推荐市场状态：" + getDisplayText(result, reviewMarketStatusOptions.value))
}

const recommendEmotionTemp = () => {
  const f = dailyReviewFormData
  if (f.limitUpCount == null || f.limitDownCount == null) { ElMessage.warning("请先填报：涨停家数、跌停家数"); return }
  const limitUp = f.limitUpCount, limitDown = f.limitDownCount || 1
  const riseCount = f.riseCount || 0, fallCount = f.fallCount || 1
  const premium = f.yesterdayPremiumPct || 0
  const brokenRate = f.brokenBoardRate || 0
  const continuousBoard = f.continuousBoardCount || 0
  let score = 0
  if (limitUp > 80) score += 20; else if (limitUp > 50) score += 15; else if (limitUp > 35) score += 10; else if (limitUp > 20) score += 5
  const limitRatio = limitUp / limitDown
  if (limitRatio > 3) score += 20; else if (limitRatio > 2) score += 15; else if (limitRatio > 1) score += 10; else if (limitRatio > 0.5) score += 5
  const riseFallRatio = riseCount / fallCount
  if (riseFallRatio > 2) score += 20; else if (riseFallRatio > 1.5) score += 15; else if (riseFallRatio > 1) score += 10; else if (riseFallRatio > 0.5) score += 5
  if (brokenRate < 10) score += 15; else if (brokenRate < 20) score += 10; else if (brokenRate < 30) score += 5
  if (premium > 2) score += 15; else if (premium > 0) score += 10; else if (premium > -1) score += 3
  if (continuousBoard > 10) score += 10; else if (continuousBoard > 5) score += 7; else if (continuousBoard > 2) score += 4
  let result = 3
  if (score >= 80) result = 5; else if (score >= 60) result = 4; else if (score >= 40) result = 3; else if (score >= 20) result = 2; else result = 1
  dailyReviewFormData.emotionTemp = result
  ElMessage.success("已推荐情绪温度：" + getDisplayText(result, reviewEmotionTempOptions.value) + "（综合评分" + score + "）")
}

const recommendAdaptSystem = () => {
  const f = dailyReviewFormData
  if (f.emotionTemp == null) { ElMessage.warning("请先填报：情绪温度（可点击情绪温度旁💡按钮自动推荐）"); return }
  if (f.marketStatus == null) { ElMessage.warning("请先填报：市场状态（可点击市场状态旁💡按钮自动推荐）"); return }
  const emotion = f.emotionTemp, market = f.marketStatus
  const brokenRate = f.brokenBoardRate || 0, premium = f.yesterdayPremiumPct || 0
  const continuousBoard = f.continuousBoardCount || 0
  const northFlow = f.northFlowAmount || 0
  let result = 3
  if (emotion === 1) result = 5
  else if (emotion === 2) result = 3
  else if (emotion === 3) { if (market === 3) result = 2; else if (market === 4 || market === 5) result = 4; else result = 3 }
  else if (emotion === 4) { if (brokenRate > 30) result = 2; else if (premium < 0) result = 2; else if (continuousBoard < 3) result = 2; else result = 1 }
  else if (emotion === 5) { if (brokenRate > 25) result = 2; else if (northFlow < -100000) result = 2; else result = 1 }
  dailyReviewFormData.adaptSystem = result
  ElMessage.success("已推荐适配体系：" + getDisplayText(result, reviewAdaptSystemOptions.value))
}

const recommendPositionLimit = () => {
  const f = dailyReviewFormData
  if (f.emotionTemp == null) { ElMessage.warning("请先填报：情绪温度"); return }
  if (f.adaptSystem == null) { ElMessage.warning("请先选择：适配体系（可点击适配体系旁💡按钮自动推荐）"); return }
  const emotion = f.emotionTemp, adapt = f.adaptSystem
  let result = 50
  if (adapt === 5) result = 0
  else if (adapt === 3) result = emotion === 2 ? 30 : 40
  else if (adapt === 2) result = emotion === 3 ? 50 : 60
  else if (adapt === 1) result = emotion === 4 ? 70 : 80
  else if (adapt === 4) result = emotion === 3 ? 50 : 60
  dailyReviewFormData.planPositionLimit = result
  ElMessage.success("已推荐计划仓位上限：" + result + "%")
}

const recommendStopLossTakeProfit = () => {
  const f = dailyReviewFormData
  if (f.adaptSystem == null) { ElMessage.warning("请先选择：适配体系（可点击适配体系旁💡按钮自动推荐）"); return }
  const adapt = f.adaptSystem
  const map = { 1: [-3, 10], 2: [-5, 8], 3: [-7, 10], 4: [-10, 25], 5: [null, null] }
  const [stopLoss, takeProfit] = map[adapt] || [null, null]
  if (stopLoss == null) { ElMessage.info("当前适配体系为空仓，无需设置止损止盈"); return }
  dailyReviewFormData.stopLossPct = stopLoss
  dailyReviewFormData.takeProfitPct = takeProfit
  ElMessage.success("已推荐止损" + stopLoss + "% / 止盈" + takeProfit + "%")
}

const recommendSelfRating = () => {
  const f = dailyReviewFormData
  if (f.dailyProfitPct == null) { ElMessage.warning("请先填报：当日盈亏%"); return }
  const profit = f.dailyProfitPct
  const winRate = (f.tradeCount != null && f.tradeCount > 0 && f.winTradeCount != null) ? (f.winTradeCount / f.tradeCount * 100) : 50
  let result = 3
  if (profit > 2 && winRate > 60) result = 5
  else if (profit > 0 && winRate >= 50) result = 4
  else if (Math.abs(profit) <= 0.5) result = 3
  else if (profit < 0 && winRate < 40) result = 2
  else if (profit < -2) result = 1
  dailyReviewFormData.operationSelfRating = result
  ElMessage.success("已推荐操作自评：" + getDisplayText(result, reviewSelfRatingOptions.value))
}

const smartFillAll = () => {
  const f = dailyReviewFormData
  const hasBase = [f.shChangePct, f.szChangePct, f.cybChangePct, f.limitUpCount, f.limitDownCount, f.riseCount, f.fallCount].filter(v => v != null).length
  if (hasBase < 4) { ElMessage.warning("请先填报基础市场数据（指数涨跌、涨跌停家数、涨跌家数等，至少4项），再使用智能填充"); return }
  recommendMarketStatus()
  recommendEmotionTemp()
  recommendAdaptSystem()
  recommendPositionLimit()
  recommendStopLossTakeProfit()
  ElMessage.success("智能填充完成，请检查并调整推荐值")
}

const dailyReviewDialogVisible = ref(false)
const dailyReviewDialogTitle = ref('')
const dailyReviewFormRef = ref(null)
const defaultDailyReviewForm = () => ({
  id: null, reviewDate: new Date().toISOString().slice(0, 10),
  shChangePct: null, szChangePct: null, cybChangePct: null, totalAmount: null, riseCount: null, fallCount: null, marketStatus: null,
  limitUpCount: null, limitDownCount: null, continuousBoardCount: null, yesterdayPremiumPct: null, emotionTemp: null, northFlowAmount: null, brokenBoardCount: null, brokenBoardRate: null,
  mainSector1: null, mainSector2: null, mainSector3: null, sectorLimitUpCount: null, leaderStockName: '', leaderStockCode: '', leaderLimitUpTime: null, leaderSealAmount: null, leaderContinuousBoard: null,
  adaptSystem: null, planPositionLimit: 50, watchTargets: '', buyCondition: '', riskWarning: '', stopLossPct: null, takeProfitPct: null,
  todayOperation: '', operationSelfRating: null, dailyProfitPct: null, positionProfitPct: null, tradeCount: null, winTradeCount: null,
  experience: '', lesson: '', improvePoint: '', tomorrowFocus: '', remark: ''
})
const dailyReviewFormData = reactive(defaultDailyReviewForm())

const marketDataLoading = ref(false)
const fetchMarketData = async () => {
  if (!dailyReviewFormData.reviewDate) { ElMessage.warning("请先选择复盘日期"); return }
  marketDataLoading.value = true
  try {
    const result = await FetchRealtimeMarketData(dailyReviewFormData.reviewDate)
    if (result.code === 200 && result.data) {
      const d = result.data
      if (d.shChangePct != null) dailyReviewFormData.shChangePct = d.shChangePct
      if (d.szChangePct != null) dailyReviewFormData.szChangePct = d.szChangePct
      if (d.cybChangePct != null) dailyReviewFormData.cybChangePct = d.cybChangePct
      if (d.totalAmount != null) dailyReviewFormData.totalAmount = d.totalAmount
      if (d.riseCount != null) dailyReviewFormData.riseCount = d.riseCount
      if (d.fallCount != null) dailyReviewFormData.fallCount = d.fallCount
      if (d.limitUpCount != null) dailyReviewFormData.limitUpCount = d.limitUpCount
      if (d.limitDownCount != null) dailyReviewFormData.limitDownCount = d.limitDownCount
      if (d.actualDate && d.actualDate !== dailyReviewFormData.reviewDate) {
        ElMessage.warning("复盘日为非交易日，已填充最近交易日 " + d.actualDate + " 的市场数据")
      } else if (d.riseCount == null || d.fallCount == null) {
        ElMessage.success("已获取 " + dailyReviewFormData.reviewDate + " 市场数据并填充（上涨/下跌家数暂无历史数据源，请手动填写）")
      } else {
        ElMessage.success("已获取 " + dailyReviewFormData.reviewDate + " 市场数据并填充")
      }
    } else { ElMessage.error(result.message || "获取市场数据失败") }
  } catch (e) { ElMessage.error("获取市场数据失败：" + e.message) }
  finally { marketDataLoading.value = false }
}

const aiReviewLoading = ref(false)
const aiGenerateReviewSummary = async () => {
  aiReviewLoading.value = true
  try {
    const sectorParts = [dailyReviewFormData.mainSector1, dailyReviewFormData.mainSector2, dailyReviewFormData.mainSector3].filter(s => s != null).map(s => getDisplayText(s, reviewSectorOptions.value)).join('、')
    const payload = { ...dailyReviewFormData, marketStatusText: getDisplayText(dailyReviewFormData.marketStatus, reviewMarketStatusOptions.value), emotionTempText: getDisplayText(dailyReviewFormData.emotionTemp, reviewEmotionTempOptions.value), adaptSystemText: getDisplayText(dailyReviewFormData.adaptSystem, reviewAdaptSystemOptions.value), selfRatingText: getDisplayText(dailyReviewFormData.operationSelfRating, reviewSelfRatingOptions.value), sectorText: sectorParts || '未填写' }
    const result = await AiGenerateDailyReview(payload)
    if (result.code === 200 && result.data) {
      if (result.data.experience) dailyReviewFormData.experience = result.data.experience
      if (result.data.lesson) dailyReviewFormData.lesson = result.data.lesson
      if (result.data.improvePoint) dailyReviewFormData.improvePoint = result.data.improvePoint
      if (result.data.tomorrowFocus) dailyReviewFormData.tomorrowFocus = result.data.tomorrowFocus
      ElMessage.success("AI总结已生成，可编辑修改")
    } else { ElMessage.error(result.message || "AI生成失败") }
  } catch (e) { ElMessage.error("AI生成失败：" + e.message) }
  finally { aiReviewLoading.value = false }
}

const aiTargetLoading = ref(false)
const aiAnalyzeTargets = async () => {
  if (!dailyReviewFormData.watchTargets || dailyReviewFormData.watchTargets.trim() === '') { ElMessage.warning("请先填写关注标的"); return }
  aiTargetLoading.value = true
  try {
    const sectorParts = [dailyReviewFormData.mainSector1, dailyReviewFormData.mainSector2, dailyReviewFormData.mainSector3].filter(s => s != null).map(s => getDisplayText(s, reviewSectorOptions.value)).join('、')
    const payload = { ...dailyReviewFormData, marketStatusText: getDisplayText(dailyReviewFormData.marketStatus, reviewMarketStatusOptions.value), emotionTempText: getDisplayText(dailyReviewFormData.emotionTemp, reviewEmotionTempOptions.value), adaptSystemText: getDisplayText(dailyReviewFormData.adaptSystem, reviewAdaptSystemOptions.value), sectorText: sectorParts || '未填写' }
    const result = await AiAnalyzeTargets(payload)
    if (result.code === 200 && result.data) {
      if (result.data.buyCondition) dailyReviewFormData.buyCondition = result.data.buyCondition
      if (result.data.riskWarning) dailyReviewFormData.riskWarning = result.data.riskWarning
      ElMessage.success("AI分析已生成买入条件和风险预警，可编辑修改")
    } else { ElMessage.error(result.message || "AI分析失败") }
  } catch (e) { ElMessage.error("AI分析失败：" + e.message) }
  finally { aiTargetLoading.value = false }
}

const addDailyReview = () => {
  Object.assign(dailyReviewFormData, defaultDailyReviewForm())
  dailyReviewDialogTitle.value = '添加每日复盘'
  dailyReviewDialogVisible.value = true
}
const editDailyReview = (row) => {
  Object.assign(dailyReviewFormData, defaultDailyReviewForm(), row)
  dailyReviewDialogTitle.value = '编辑每日复盘'
  dailyReviewDialogVisible.value = true
}
const dailyReviewDetailVisible = ref(false)
const dailyReviewDetailData = ref(null)
const viewDailyReviewDetail = (row) => {
  dailyReviewDetailData.value = { ...row }
  dailyReviewDetailVisible.value = true
}
const submitDailyReview = async () => {
  if (!dailyReviewFormRef.value) return
  await dailyReviewFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      const result = await SaveDailyReview(dailyReviewFormData)
      if (result.code === 200) { ElMessage.success("保存成功"); dailyReviewDialogVisible.value = false; fetchDailyReviewData() }
      else { ElMessage.error(result.message || "保存失败") }
    } catch (e) { ElMessage.error("保存每日复盘失败") }
  })
}
const deleteDailyReview = (row) => {
  ElMessageBox.confirm("确认删除该复盘记录？", "提示", { type: "warning" }).then(async () => {
    const result = await DeleteDailyReviewById(row.id)
    if (result.code === 200) { ElMessage.success("删除成功"); fetchDailyReviewData() }
  }).catch(() => {})
}
const deleteDailyReviewAll = () => {
  ElMessageBox.confirm("确认批量删除选中记录？", "提示", { type: "warning" }).then(async () => {
    const ids = dailyReviewSelectedRows.value.map(r => r.id)
    const result = await DeleteAllDailyReviewByIds(ids)
    if (result.code === 200) { ElMessage.success("删除成功"); fetchDailyReviewData() }
  }).catch(() => {})
}

// ==================== 表单联动watch ====================
// 炸板率自动计算
watch([() => dailyReviewFormData.brokenBoardCount, () => dailyReviewFormData.limitUpCount], ([broken, limitUp]) => {
  if (broken != null && limitUp != null && limitUp > 0) {
    dailyReviewFormData.brokenBoardRate = Number((broken / limitUp * 100).toFixed(2))
  }
})
// 复盘日期变更时，自动查询当天交易汇总并回填（防抖）
let reviewDateDebounceTimer = null
watch(() => dailyReviewFormData.reviewDate, (val) => {
  if (!val) return
  if (reviewDateDebounceTimer) clearTimeout(reviewDateDebounceTimer)
  reviewDateDebounceTimer = setTimeout(async () => {
    try {
      const result = await StatTradeByReviewDate(val)
      if (result.code === 200 && result.data) {
        const d = result.data
        const tradeCount = Number(d.tradeCount || 0)
        if (tradeCount > 0) {
          dailyReviewFormData.tradeCount = tradeCount
          dailyReviewFormData.winTradeCount = Number(d.winTradeCount || 0)
          dailyReviewFormData.dailyProfitPct = Number(Number(d.totalProfitPct || 0).toFixed(2))
          ElMessage.success("已自动汇总" + val + "交易数据：" + tradeCount + "笔，盈利" + (d.winTradeCount || 0) + "笔")
        }
      }
    } catch (e) { console.error('自动汇总交易数据失败:', e) }
  }, DEBOUNCE_DELAY)
})

//--------------------钩子函数-------------------------
onMounted(() => {
  Promise.all([
    loadDict('t_trial_review_market_status', reviewMarketStatusOptions),
    loadDict('t_trial_review_emotion_temp', reviewEmotionTempOptions),
    loadDict('t_trial_review_sector', reviewSectorOptions),
    loadDict('t_trial_review_adapt_system', reviewAdaptSystemOptions),
    loadDict('t_trial_review_self_rating', reviewSelfRatingOptions)
  ]).catch(err => console.error('字典加载失败:', err))

  fetchDailyReviewData()
})
</script>

<style scoped>
.daily-review-div {
  padding: 10px;
}

/* 搜索表单样式 */
.search-div {
  margin-bottom: 10px;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: rgba(255, 255, 255, 0.8);
}

.search-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.search-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

/* 操作按钮区域 */
.tools-div {
  margin-bottom: 10px;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: rgba(255, 255, 255, 0.8);
}

.beautified-tools .el-button {
  border-radius: 8px;
  padding: 10px 20px;
  font-size: 13px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.beautified-tools .el-button--success { background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%); border: none; }
.beautified-tools .el-button--danger { background: linear-gradient(135deg, #F56C6C 0%, #f78989 100%); border: none; }

/* 表格内按钮美化 */
:deep(.el-table .el-button) {
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 600;
  border: none;
}

:deep(.el-table .el-button--primary) { background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%); }
:deep(.el-table .el-button--danger) { background: linear-gradient(135deg, #F56C6C 0%, #f78989 100%); }
:deep(.el-table .el-button--info) { background: linear-gradient(135deg, #909399 0%, #a6a9ad 100%); }

/* 对话框样式 */
:deep(.custom-dialog), :deep(.enhanced-dialog) {
  border-radius: 16px !important;
  overflow: hidden !important;
  box-shadow: 0 16px 48px rgba(102, 126, 234, 0.25) !important;
}

:deep(.enhanced-dialog .el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border-radius: 16px 16px 0 0 !important;
  padding: 24px 28px !important;
  margin: 0 !important;
  width: 100% !important;
  box-sizing: border-box !important;
}

:deep(.enhanced-dialog .el-dialog__title) { color: white !important; font-weight: 700 !important; font-size: 22px !important; }
:deep(.enhanced-dialog .el-dialog__headerbtn .el-dialog__close) { color: white !important; font-size: 22px !important; }
:deep(.enhanced-dialog .el-dialog__body) { padding: 28px !important; background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%) !important; max-height: 60vh !important; overflow-y: auto !important; }
:deep(.enhanced-dialog .el-dialog__footer) { padding: 20px 28px !important; background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%) !important; border-top: 2px solid rgba(102, 126, 234, 0.1) !important; }

/* 分页组件样式 */
:deep(.el-pagination) { justify-content: center; }

/* 详情样式 */
.detail-section { margin-bottom: 24px; }
.detail-section:last-child { margin-bottom: 0; }
.detail-section-title { font-size: 16px; font-weight: 700; color: #303133; margin-bottom: 12px; padding-left: 10px; border-left: 4px solid #667eea; line-height: 1; }
.detail-value { font-size: 14px; color: #606266; }
.detail-value.highlight { color: #667eea; font-weight: 600; font-family: 'Courier New', monospace; }
.detail-text-block { font-size: 14px; color: #303133; line-height: 1.8; white-space: pre-wrap; word-break: break-all; }

/* 复盘表单联动样式 */
.ai-fill-bar { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; padding: 8px 16px; background: linear-gradient(135deg, #e8f4ff, #f0f9ff); border: 1px solid #d4e8fc; border-radius: 8px; }
.ai-fill-tip { font-size: 12px; color: #909399; }
.linkage-field { display: flex; align-items: center; width: 100%; gap: 4px; }
.recommend-btn { flex-shrink: 0; padding: 2px 6px !important; font-size: 14px; line-height: 1; }
</style>