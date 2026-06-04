<template>
  <el-dialog
    v-model="dialogVisible"
    width="90%"
    :close-on-click-modal="false"
    :lock-scroll="false"
    align-center
    :show-close="false"
    class="fund-detail-dialog"
    destroy-on-close
    :fullscreen="isFullscreen"
  >
    <!-- 标题块 - 使用header插槽 -->
    <template #header>
      <div class="detail-header-bar">
        <div class="detail-header-left">
          <div class="detail-title-icon">
            <el-icon :size="18"><Coin /></el-icon>
          </div>
          <span class="header-title">基金数据维护 - {{ fundData.fundName || '未知基金' }}</span>
          <el-tag type="info" size="small" effect="dark" style="margin-left: 10px;">{{ fundData.fundCode }}</el-tag>
        </div>
        <div class="detail-header-right">
          <el-icon class="fullscreen-icon" @click="toggleFullscreen">
            <FullScreen v-if="!isFullscreen" />
            <aim v-else />
          </el-icon>
          <el-icon class="close-icon" @click="handleClose"><Close /></el-icon>
        </div>
      </div>
    </template>

    <!-- 按钮操作块 -->
    <div class="detail-actions">
      <el-button type="primary" size="small" @click="handleSave" :icon="Check">保存</el-button>
      <el-button size="small" @click="handleClose" :icon="Close">取消</el-button>
      <el-button type="warning" size="small" @click="handleFetchRealTimeData" :icon="Download" :loading="fetchLoading">实时数据</el-button>
    </div>

    <!-- 滚动内容区 -->
    <div class="detail-body">
      <!-- ====== 基金基本数据管理块 ====== -->
      <div class="section-block">
        <div class="section-title">
          <el-icon><Document /></el-icon>
          <span>基本数据</span>
        </div>
        <div class="section-content">
          <el-form label-width="120px" size="small">
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="基金名称">
                  <el-input v-model="fundData.fundName" placeholder="请输入基金名称" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="基金代码">
                  <el-input v-model="fundData.fundCode" placeholder="请输入基金代码" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="基金类型">
                  <el-select v-model="fundData.fundType" placeholder="请选择" style="width: 100%" clearable>
                    <el-option v-for="item in fundTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="成立日期">
                  <el-date-picker v-model="fundData.establishDate" type="date" style="width: 100%" placeholder="选择日期" value-format="YYYY-MM-DD" :editable="false" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="资产规模(亿)">
                  <el-input-number v-model="fundData.assetScale" :precision="2" :step="0.01" :min="0" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="基金管理人">
                  <el-input v-model="fundData.fundCompany" placeholder="请输入基金管理人" clearable />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="16">
                <el-form-item label="基金公司描述">
                  <el-input v-model="fundData.fundCompanyDesc" type="textarea" :rows="2" placeholder="请输入基金公司描述" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="基金托管者">
                  <el-input v-model="fundData.custodian" placeholder="请输入基金托管者" clearable />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="基金经理">
                  <el-input v-model="fundData.fundManager" placeholder="请输入基金经理" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="经理任职日期">
                  <el-date-picker v-model="fundData.managerStartDate" type="date" style="width: 100%" placeholder="选择日期" value-format="YYYY-MM-DD" :editable="false" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="经理描述">
                  <el-input v-model="fundData.managerDesc" placeholder="请输入经理描述" clearable />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="运作方式">
                  <el-select v-model="fundData.operationMode" placeholder="请选择" style="width: 100%" clearable>
                    <el-option v-for="item in operationModeOptions" :key="item.value" :label="item.text" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="基金封闭期(天)">
                  <el-input-number v-model="fundData.closedEndDays" :min="0" :step="1" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="申购费率(%)">
                  <el-input-number v-model="fundData.purchaseFeeRate" :precision="4" :step="0.001" :min="0" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="赎回费率(%)">
                  <el-input-number v-model="fundData.redeemFeeRate" :precision="4" :step="0.001" :min="0" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="管理费(%)">
                  <el-input-number v-model="fundData.managementFeeRate" :precision="4" :step="0.001" :min="0" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="托管费(%)">
                  <el-input-number v-model="fundData.custodianFeeRate" :precision="4" :step="0.001" :min="0" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="销售服务费(%)">
                  <el-input-number v-model="fundData.salesServiceFeeRate" :precision="4" :step="0.001" :min="0" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="16">
                <el-form-item label="交易规则">
                  <el-input v-model="fundData.tradeRule" type="textarea" :rows="2" placeholder="请输入交易规则" />
                </el-form-item>
              </el-col>
            </el-row>
            <!-- 新增收益率字段 -->
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="近1月收益率(%)">
                  <el-input-number v-model="fundData.return1m" :precision="2" :step="0.01" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="近3月收益率(%)">
                  <el-input-number v-model="fundData.return3m" :precision="2" :step="0.01" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="近6月收益率(%)">
                  <el-input-number v-model="fundData.return6m" :precision="2" :step="0.01" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="近1年收益率(%)">
                  <el-input-number v-model="fundData.return1y" :precision="2" :step="0.01" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="最新规模(亿)">
                  <el-input-number v-model="fundData.latestScale" :precision="2" :step="0.01" :min="0" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="24">
                <el-form-item label="净资产规模">
                  <el-input v-model="fundData.netAssets" type="textarea" :rows="3" placeholder="请输入净资产规模，例如：[1.0112, 4.4303, 5.9658, 21.6745] 亿元" />
                </el-form-item>
              </el-col>
            </el-row>
            <!-- 新增占比字段 -->
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="股票占比(%)">
                  <el-input-number v-model="fundData.stockRatio" :precision="2" :step="0.01" :min="0" :max="100" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="债券占比(%)">
                  <el-input-number v-model="fundData.bondRatio" :precision="2" :step="0.01" :min="0" :max="100" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="现金占比(%)">
                  <el-input-number v-model="fundData.cashRatio" :precision="2" :step="0.01" :min="0" :max="100" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
            </el-row>
            <!-- 新增持有比例字段 -->
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="机构持有比例(%)">
                  <el-input-number v-model="fundData.institutionRatio" :precision="2" :step="0.01" :min="0" :max="100" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="个人持有比例(%)">
                  <el-input-number v-model="fundData.individualRatio" :precision="2" :step="0.01" :min="0" :max="100" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="内部持有比例(%)">
                  <el-input-number v-model="fundData.internalRatio" :precision="2" :step="0.01" :min="0" :max="100" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="24">
                <el-form-item label="规模历史">
                  <el-input v-model="fundData.scaleHistory" type="textarea" :rows="3" placeholder="请输入规模历史记录" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>
      </div>

      <!-- ====== 净值与估值信息块 ====== -->
      <div class="section-block">
        <div class="section-title">
          <el-icon><TrendCharts /></el-icon>
          <span>净值与估值信息</span>
        </div>
        <div class="section-content">
          <!-- 查询与操作 -->
          <div class="sub-section-toolbar">
            <el-date-picker 
              v-model="navQuery.timeRange" 
              type="daterange" 
              range-separator="至" 
              start-placeholder="开始日期" 
              end-placeholder="结束日期" 
              value-format="YYYY-MM-DD" 
              :editable="false" 
              :unlink-panels="true" 
              size="small" 
              style="width: 280px; margin-right: 12px;" 
              clearable 
            />
            <el-button type="primary" size="small" @click="queryNavData" :icon="Search">查询</el-button>
            <el-button size="small" @click="resetNavQuery" :icon="Refresh">重置</el-button>
            <div style="flex:1"></div>
            <el-button type="success" size="small" @click="addNavRow" :icon="Plus">新增</el-button>
          </div>
          <el-table :data="navData" border stripe size="small" max-height="320px" v-loading="navTableLoading">
            <el-table-column prop="navDate" label="净值日期" width="120" />
            <el-table-column prop="unitNav" label="单位净值" width="110" align="right" />
            <el-table-column prop="accumulatedNav" label="累计净值" width="110" align="right" />
            <el-table-column prop="dailyChangeRate" label="日涨跌幅(%)" width="120" align="right">
              <template #default="{ row }">
                <span :class="row.dailyChangeRate >= 0 ? 'text-red' : 'text-green'">{{ row.dailyChangeRate }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="valuation" label="估值" width="110" align="right" />
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <el-button text size="small" @click="editNavRow(row)">修改</el-button>
                <el-button text size="small" type="danger" @click="deleteNavRow(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="sub-pagination">
            <el-pagination 
              background 
              layout="total, sizes, prev, pager, next" 
              :total="navTableTotal" 
              :page-size="navPage.limit" 
              :current-page="navPage.page" 
              :page-sizes="[10, 20, 50]" 
              @size-change="(s) => { navPage.limit = s; navPage.page = 1; fetchNavData() }" 
              @current-change="(p) => { navPage.page = p; fetchNavData() }" 
              size="small" 
            />
          </div>
        </div>
      </div>

      <!-- ====== 基金经理分析块 ====== -->
      <div class="section-block">
        <div class="section-title">
          <el-icon><User /></el-icon>
          <span>基金经理分析</span>
        </div>
        <div class="section-content">
          <el-form label-width="140px" size="small">
            <!-- 基本信息 -->
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="姓名">
                  <el-input v-model="managerAnalysis.managerName" placeholder="请输入基金经理姓名" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="星级">
                  <el-rate v-model="managerAnalysis.starRating" :max="5" allow-half show-score />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="从业时间(年)">
                  <el-input-number v-model="managerAnalysis.workTime" :precision="1" :step="0.5" :min="0" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="管理规模(亿)">
                  <el-input-number v-model="managerAnalysis.manageScale" :precision="2" :step="0.01" :min="0" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="学历">
                  <el-select v-model="managerAnalysis.education" placeholder="请选择" style="width: 100%" clearable>
                    <el-option v-for="item in schoolTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="个人持有">
                  <el-select v-model="managerAnalysis.isManagerHolding" placeholder="请选择" style="width: 100%" clearable>
                    <el-option label="是" :value="1" />
                    <el-option label="否" :value="0" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="获奖记录">
                  <el-select v-model="managerAnalysis.awardRecords" placeholder="请选择" style="width: 100%" clearable>
                    <el-option v-for="item in awardOptions" :key="item.value" :label="item.text" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <!-- 评分信息 -->
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="综合评分">
                  <el-input-number v-model="managerAnalysis.totalScore" :precision="2" :step="0.1" :min="0" :max="100" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="选证能力评分">
                  <el-input-number v-model="managerAnalysis.stockSelectScore" :precision="2" :step="0.1" :min="0" :max="100" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="收益率评分">
                  <el-input-number v-model="managerAnalysis.returnScore" :precision="2" :step="0.1" :min="0" :max="100" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="抗风险评分">
                  <el-input-number v-model="managerAnalysis.riskControlScore" :precision="2" :step="0.1" :min="0" :max="100" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="稳定性评分">
                  <el-input-number v-model="managerAnalysis.stabilityScore" :precision="2" :step="0.1" :min="0" :max="100" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="择时能力评分">
                  <el-input-number v-model="managerAnalysis.timingScore" :precision="2" :step="0.1" :min="0" :max="100" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
            </el-row>
            <!-- 分析维度下拉框 -->
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="持仓集中度">
                  <el-select v-model="managerAnalysis.holdingsConcentration" placeholder="请选择" style="width: 100%" clearable>
                    <el-option v-for="item in levelOptions" :key="item.value" :label="item.text" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="换手率">
                  <el-select v-model="managerAnalysis.turnoverRate" placeholder="请选择" style="width: 100%" clearable>
                    <el-option v-for="item in levelOptions" :key="item.value" :label="item.text" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="能力路径匹配度">
                  <el-select v-model="managerAnalysis.backgroundMatch" placeholder="请选择" style="width: 100%" clearable>
                    <el-option v-for="item in levelOptions" :key="item.value" :label="item.text" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="规模驾驭能力">
                  <el-select v-model="managerAnalysis.scaleCapability" placeholder="请选择" style="width: 100%" clearable>
                    <el-option v-for="item in levelOptions" :key="item.value" :label="item.text" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="本基金精力集中度">
                  <el-select v-model="managerAnalysis.focusOnThisFund" placeholder="请选择" style="width: 100%" clearable>
                    <el-option v-for="item in levelOptions" :key="item.value" :label="item.text" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <!-- 文本域分析 -->
            <el-row :gutter="16">
              <el-col :span="24">
                <el-form-item label="经理描述">
                  <el-input v-model="managerAnalysis.managerDesc" type="textarea" :rows="3" placeholder="请输入基金经理描述" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="24">
                <el-form-item label="持仓集中度分析">
                  <el-input v-model="managerAnalysis.concentrationRateAnalyse" type="textarea" :rows="3" placeholder="指基金前十大重仓股占股票投资市值的比例。如果这个比例很高（比如超过60%-70%），说明基金经理喜欢集中投资，依靠深度研究的少量个股来获取收益，净值波动可能会更；如果比例较低，说明更倾向于分散投资，通过组合管理来控制风险。" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="24">
                <el-form-item label="换手率分析">
                  <el-input v-model="managerAnalysis.turnoverRateAnalyse" type="textarea" :rows="3" placeholder="反映基金经理交易股票的频繁程度。低换手率通常意味着基金经理是选股型选手，倾向于长期持有；高换手率则可能偏向交易型选手，试图通过把握市场节奏来增厚收益。" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="24">
                <el-form-item label="能力圈与路径依赖分析">
                  <el-input v-model="managerAnalysis.capabilityPathAnalysis" type="textarea" :rows="3" placeholder="关注他的行业研究背景是否与现在管理的基金相匹配。例如，一个长期研究消费的基金经理去管理一只科技主题基金，就需要审慎评估。" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="24">
                <el-form-item label="规模驾驭能力分析">
                  <el-input v-model="managerAnalysis.scaleAbilityAnalysis" type="textarea" :rows="3" placeholder="留意基金规模是否在短期内急剧膨胀。许多优秀的策略在资金规模变大后可能会失效（即'规模是业绩的敌人'），需要观察基金经理是否有管理大规模资金的成功经验。" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="24">
                <el-form-item label="从业背景">
                  <el-input v-model="managerAnalysis.professionalBackground" type="textarea" :rows="3" placeholder="他的学历专业、研究员时期的行业覆盖范围、获得过的权威奖项（如'金牛奖'）等都是重要参考。" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="24">
                <el-form-item label="产品管理情况分析">
                  <el-input v-model="managerAnalysis.productManagementAnalysis" type="textarea" :rows="3" placeholder="看他是否'超负荷'工作。如果一个人名下管了十几只不同类型（如全市场选股、行业主题、量化对冲）的基金，精力难免被分散。" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="24">
                <el-form-item label="稳定性分析">
                  <el-input v-model="managerAnalysis.stabilityAnalysis" type="textarea" :rows="3" placeholder="如果他频繁跳槽，或者管理的基金频繁更换基金经理，这都是需要警惕的信号。" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="24">
                <el-form-item label="个人持有情况">
                  <el-input v-model="managerAnalysis.personalHolding" type="textarea" :rows="3" placeholder="基金经理本人是否也持有了自己管理的基金？这在基金的年报/半年报中有披露，是衡量信心的重要指标。" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>
      </div>

      <!-- ====== 份额与持仓信息块 ====== -->
      <div class="section-block">
        <div class="section-title">
          <el-icon><PieChart /></el-icon>
          <span>份额与持仓信息</span>
        </div>
        <div class="section-content">
          <el-form label-width="120px" size="small">
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="持有份额">
                  <el-input-number v-model="shareData.holdShares" :precision="2" :step="100" :min="0" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="可用份额">
                  <el-input-number v-model="shareData.availableShares" :precision="2" :step="100" :min="0" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="冻结份额">
                  <el-input-number v-model="shareData.frozenShares" :precision="2" :step="100" :min="0" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="持仓成本">
                  <el-input-number v-model="shareData.costAmount" :precision="2" :step="100" :min="0" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="持仓市值">
                  <el-input-number v-model="shareData.marketValue" :precision="2" :step="100" :min="0" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="持仓盈亏">
                  <el-input-number v-model="shareData.profitLoss" :precision="2" :step="100" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="持仓收益率(%)">
                  <el-input-number v-model="shareData.profitLossRate" :precision="2" :step="0.01" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>
      </div>

      <!-- ====== 交易与流水信息块 ====== -->
      <div class="section-block">
        <div class="section-title">
          <el-icon><List /></el-icon>
          <span>交易与流水信息</span>
        </div>
        <div class="section-content">
          <div class="sub-section-toolbar">
            <el-date-picker v-model="tradeQuery.timeRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" :editable="false" :unlink-panels="true" size="small" style="width: 280px; margin-right: 12px;" clearable />
            <el-button type="primary" size="small" @click="queryTradeData" :icon="Search">查询</el-button>
            <el-button size="small" @click="resetTradeQuery" :icon="Refresh">重置</el-button>
            <div style="flex:1"></div>
            <el-button type="success" size="small" @click="addTradeRow" :icon="Plus">新增</el-button>
          </div>
          <el-table :data="tradeData" border stripe size="small" max-height="280px" v-loading="tradeTableLoading">
            <el-table-column prop="tradeDate" label="交易日期" width="120" />
            <el-table-column prop="tradeType" label="交易类型" width="100">
              <template #default="{ row }">{{ getTradeTypeText(row.tradeType) }}</template>
            </el-table-column>
            <el-table-column prop="tradeShares" label="交易份额" width="110" align="right" />
            <el-table-column prop="tradeAmount" label="交易金额" width="110" align="right" />
            <el-table-column prop="tradeNav" label="交易净值" width="100" align="right" />
            <el-table-column prop="tradeFee" label="交易费用" width="100" align="right" />
            <el-table-column prop="confirmDate" label="确认日期" width="120" />
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <el-button text size="small" @click="editTradeRow(row)">修改</el-button>
                <el-button text size="small" type="danger" @click="deleteTradeRow(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="sub-pagination">
            <el-pagination background layout="total, sizes, prev, pager, next" :total="tradeTableTotal" :page-size="tradePage.limit" :current-page="tradePage.page" :page-sizes="[5, 10, 20]" @size-change="(s) => { tradePage.limit = s; tradePage.page = 1; fetchTradeData() }" @current-change="(p) => { tradePage.page = p; fetchTradeData() }" size="small" />
          </div>
        </div>
      </div>

      <!-- ====== 分红信息块 ====== -->
      <div class="section-block">
        <div class="section-title">
          <el-icon><Money /></el-icon>
          <span>分红信息</span>
        </div>
        <div class="section-content">
          <div class="sub-section-toolbar">
            <el-date-picker v-model="dividendQuery.timeRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" :editable="false" :unlink-panels="true" size="small" style="width: 280px; margin-right: 12px;" clearable />
            <el-button type="primary" size="small" @click="queryDividendData" :icon="Search">查询</el-button>
            <el-button size="small" @click="resetDividendQuery" :icon="Refresh">重置</el-button>
            <div style="flex:1"></div>
            <el-button type="success" size="small" @click="addDividendRow" :icon="Plus">新增</el-button>
          </div>
          <el-table :data="dividendData" border stripe size="small" max-height="280px" v-loading="dividendTableLoading">
            <el-table-column prop="dividendDate" label="分红时间" width="120" />
            <el-table-column prop="arrivalDate" label="到账时间" width="120" />
            <el-table-column prop="dividendType" label="分红方式" width="100">
              <template #default="{ row }">{{ getDividendTypeText(row.dividendType) }}</template>
            </el-table-column>
            <el-table-column prop="dividendRule" label="分红规则" width="120" />
            <el-table-column prop="dividendAmount" label="分红金额" width="120" align="right" />
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <el-button text size="small" @click="editDividendRow(row)">修改</el-button>
                <el-button text size="small" type="danger" @click="deleteDividendRow(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="sub-pagination">
            <el-pagination background layout="total, sizes, prev, pager, next" :total="dividendTableTotal" :page-size="dividendPage.limit" :current-page="dividendPage.page" :page-sizes="[5, 10, 20]" @size-change="(s) => { dividendPage.limit = s; dividendPage.page = 1; fetchDividendData() }" @current-change="(p) => { dividendPage.page = p; fetchDividendData() }" size="small" />
          </div>
        </div>
      </div>

      <!-- ====== 风险与绩效指标块 ====== -->
      <div class="section-block">
        <div class="section-title">
          <el-icon><DataAnalysis /></el-icon>
          <span>风险与绩效指标</span>
        </div>
        <div class="section-content">
          <div class="sub-section-toolbar">
            <el-button type="success" size="small" @click="addRiskRow" :icon="Plus">新增</el-button>
          </div>
          <el-table :data="riskData" border stripe size="small" max-height="280px">
            <el-table-column prop="periodType" label="时间标识" width="100">
              <template #default="{ row }">{{ getPeriodTypeText(row.periodType) }}</template>
            </el-table-column>
            <el-table-column prop="maxDrawDown" label="最大回撤(%)" width="120" align="right" />
            <el-table-column prop="recoveryDays" label="回撤修复天数" width="120" align="right" />
            <el-table-column prop="annualReturn" label="年化收益率(%)" width="130" align="right" />
            <el-table-column prop="sharpeRatio" label="夏普比率" width="100" align="right" />
            <el-table-column prop="volatility" label="波动率(%)" width="100" align="right" />
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <el-button text size="small" @click="editRiskRow(row)">修改</el-button>
                <el-button text size="small" type="danger" @click="deleteRiskRow(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- ====== 持仓信息块 ====== -->
      <div class="section-block">
        <div class="section-title">
          <el-icon><Grid /></el-icon>
          <span>持仓信息</span>
        </div>
        <div class="section-content">
          <div class="sub-section-toolbar">
            <el-date-picker v-model="holdingQuery.timeRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" :editable="false" :unlink-panels="true" size="small" style="width: 280px; margin-right: 12px;" clearable />
            <el-button type="primary" size="small" @click="queryHoldingData" :icon="Search">查询</el-button>
            <el-button size="small" @click="resetHoldingQuery" :icon="Refresh">重置</el-button>
            <div style="flex:1"></div>
            <el-button type="success" size="small" @click="addHoldingRow" :icon="Plus">新增</el-button>
          </div>
          <el-table :data="holdingData" border stripe size="small" max-height="320px" v-loading="holdingTableLoading">
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
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <el-button text size="small" @click="editHoldingRow(row)">修改</el-button>
                <el-button text size="small" type="danger" @click="deleteHoldingRow(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="sub-pagination">
            <el-pagination 
              background 
              layout="total, sizes, prev, pager, next" 
              :total="holdingTableTotal" 
              :page-size="holdingPage.limit" 
              :current-page="holdingPage.page" 
              :page-sizes="[10, 20, 50]" 
              @size-change="(s) => { holdingPage.limit = s; holdingPage.page = 1; fetchPortfolioData() }" 
              @current-change="(p) => { holdingPage.page = p; fetchPortfolioData() }" 
              size="small" 
            />
          </div>
        </div>
      </div>
    </div>

    <!-- ====== 子表格行编辑对话框 ====== -->
    <el-dialog
      v-model="subDialogVisible"
      :title="subDialogTitle"
      width="600px"
      append-to-body
      :close-on-click-modal="false"
      class="sub-edit-dialog"
    >
      <el-form label-width="120px" size="small">
        <!-- 净值编辑 -->
        <template v-if="subDialogType === 'nav'">
          <el-form-item label="净值日期">
            <el-date-picker v-model="subFormData.navDate" type="date" style="width: 100%" placeholder="选择日期" value-format="YYYY-MM-DD" :editable="false" />
          </el-form-item>
          <el-form-item label="单位净值">
            <el-input-number v-model="subFormData.unitNav" :precision="4" :step="0.0001" style="width: 100%" controls-position="right" />
          </el-form-item>
          <el-form-item label="累计净值">
            <el-input-number v-model="subFormData.accumulatedNav" :precision="4" :step="0.0001" style="width: 100%" controls-position="right" />
          </el-form-item>
          <el-form-item label="日涨跌幅(%)">
            <el-input-number v-model="subFormData.dailyChangeRate" :precision="2" :step="0.01" style="width: 100%" controls-position="right" />
          </el-form-item>
          <el-form-item label="估值">
            <el-input-number v-model="subFormData.valuation" :precision="4" :step="0.0001" style="width: 100%" controls-position="right" />
          </el-form-item>
        </template>
        <!-- 交易编辑 -->
        <template v-if="subDialogType === 'trade'">
          <el-form-item label="交易日期">
            <el-date-picker v-model="subFormData.tradeDate" type="date" style="width: 100%" placeholder="选择日期" value-format="YYYY-MM-DD" :editable="false" />
          </el-form-item>
          <el-form-item label="交易类型">
            <el-select v-model="subFormData.tradeType" style="width: 100%" placeholder="请选择">
              <el-option v-for="item in tradeTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="交易份额">
            <el-input-number v-model="subFormData.tradeShares" :precision="2" :step="100" :min="0" style="width: 100%" controls-position="right" />
          </el-form-item>
          <el-form-item label="交易金额">
            <el-input-number v-model="subFormData.tradeAmount" :precision="2" :step="100" :min="0" style="width: 100%" controls-position="right" />
          </el-form-item>
          <el-form-item label="交易净值">
            <el-input-number v-model="subFormData.tradeNav" :precision="4" :step="0.0001" :min="0" style="width: 100%" controls-position="right" />
          </el-form-item>
          <el-form-item label="交易费用">
            <el-input-number v-model="subFormData.tradeFee" :precision="2" :step="1" :min="0" style="width: 100%" controls-position="right" />
          </el-form-item>
          <el-form-item label="确认日期">
            <el-date-picker v-model="subFormData.confirmDate" type="date" style="width: 100%" placeholder="选择日期" value-format="YYYY-MM-DD" :editable="false" />
          </el-form-item>
        </template>
        <!-- 分红编辑 -->
        <template v-if="subDialogType === 'dividend'">
          <el-form-item label="分红时间">
            <el-date-picker v-model="subFormData.dividendDate" type="date" style="width: 100%" placeholder="选择日期" value-format="YYYY-MM-DD" :editable="false" />
          </el-form-item>
          <el-form-item label="到账时间">
            <el-date-picker v-model="subFormData.arrivalDate" type="date" style="width: 100%" placeholder="选择日期" value-format="YYYY-MM-DD" :editable="false" />
          </el-form-item>
          <el-form-item label="分红方式">
            <el-select v-model="subFormData.dividendType" style="width: 100%" placeholder="请选择">
              <el-option label="现金分红" :value="1" />
              <el-option label="红利再投" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="分红规则">
            <el-input v-model="subFormData.dividendRule" placeholder="请输入分红规则，如：10派0.01" clearable />
          </el-form-item>
          <el-form-item label="分红金额">
            <el-input-number v-model="subFormData.dividendAmount" :precision="2" :step="10" :min="0" style="width: 100%" controls-position="right" />
          </el-form-item>
        </template>
        <!-- 风险指标编辑 -->
        <template v-if="subDialogType === 'risk'">
          <el-form-item label="时间标识">
            <el-select v-model="subFormData.periodType" style="width: 100%" placeholder="请选择">
              <el-option label="近1年" :value="1" />
              <el-option label="近3年" :value="2" />
              <el-option label="近5年" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="最大回撤(%)">
            <el-input-number v-model="subFormData.maxDrawDown" :precision="2" :step="0.01" style="width: 100%" controls-position="right" />
          </el-form-item>
          <el-form-item label="回撤修复天数">
            <el-input-number v-model="subFormData.recoveryDays" :min="0" :step="1" style="width: 100%" controls-position="right" />
          </el-form-item>
          <el-form-item label="年化收益率(%)">
            <el-input-number v-model="subFormData.annualReturn" :precision="2" :step="0.01" style="width: 100%" controls-position="right" />
          </el-form-item>
          <el-form-item label="夏普比率">
            <el-input-number v-model="subFormData.sharpeRatio" :precision="4" :step="0.01" style="width: 100%" controls-position="right" />
          </el-form-item>
          <el-form-item label="波动率(%)">
            <el-input-number v-model="subFormData.volatility" :precision="2" :step="0.01" style="width: 100%" controls-position="right" />
          </el-form-item>
        </template>
        <!-- 持仓编辑 -->
        <template v-if="subDialogType === 'holding'">
          <el-form-item label="持仓日期">
            <el-date-picker v-model="subFormData.portfolioDate" type="date" style="width: 100%" placeholder="选择日期" value-format="YYYY-MM-DD" :editable="false" />
          </el-form-item>
          <el-form-item label="持仓类型">
            <el-select v-model="subFormData.positionType" style="width: 100%" placeholder="请选择">
              <el-option label="股票" :value="1" />
              <el-option label="债券" :value="2" />
              <el-option label="基金" :value="3" />
              <el-option label="现金" :value="4" />
            </el-select>
          </el-form-item>
          <el-form-item label="持仓代码">
            <el-input v-model="subFormData.positionCode" placeholder="请输入持仓代码" clearable />
          </el-form-item>
          <el-form-item label="持仓名称">
            <el-input v-model="subFormData.positionName" placeholder="请输入持仓名称" clearable />
          </el-form-item>
          <el-form-item label="持仓数量">
            <el-input-number v-model="subFormData.positionQuantity" :precision="2" :step="100" :min="0" style="width: 100%" controls-position="right" />
          </el-form-item>
          <el-form-item label="持仓市值">
            <el-input-number v-model="subFormData.positionMarketValue" :precision="2" :step="100" :min="0" style="width: 100%" controls-position="right" />
          </el-form-item>
          <el-form-item label="占净值比例(%)">
            <el-input-number v-model="subFormData.netRatio" :precision="2" :step="0.01" :min="0" style="width: 100%" controls-position="right" />
          </el-form-item>
          <el-form-item label="行业分类">
            <el-select v-model="subFormData.industryType" style="width: 100%" placeholder="请选择">
              <el-option label="通信装备" :value="1" />
              <el-option label="电池" :value="2" />
              <el-option label="半导体" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="数据来源">
            <el-select v-model="subFormData.dataSource" style="width: 100%" placeholder="请选择">
              <el-option label="支付宝" :value="1" />
              <el-option label="天天基金" :value="2" />
            </el-select>
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="subDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSubDialog">确定</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Close, Check, Search, Refresh, Plus, FullScreen, Aim, Download, User } from '@element-plus/icons-vue'
import {useFullscreenDialog} from "@/hooks/useFullscreenDialog";
import { GetKeyAndValueByType } from "@/api/sysDict"
import { GetFundNavByConditionAndPage, GetFundManagerAnalysisByCode, GetFundHoldingByCode, GetFundTransactionByConditionAndPage, GetFundDividendByConditionAndPage, GetFundRiskPerformanceByCode, GetFundPortfolioByConditionAndPage, AddFundNav, UpdateFundNav, DeleteFundNav, AddFundTransaction, UpdateFundTransaction, DeleteFundTransaction, AddFundDividend, UpdateFundDividend, DeleteFundDividend, AddFundRiskPerformance, UpdateFundRiskPerformance, DeleteFundRiskPerformance, AddFundPortfolio, UpdateFundPortfolio, DeleteFundPortfolio, AddFundImportData } from "@/api/fundAsset"

// 在需要全屏的组件中使用 Hook
const { isFullscreen, initFullscreen, toggleFullscreen } = useFullscreenDialog()

// ============ Props & Emit ============
const props = defineProps({
  visible: { type: Boolean, default: false },
  fundRowData: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:visible', 'save'])

const dialogVisible = computed({
  get() { return props.visible },
  set(val) { emit('update:visible', val) }
})

// ============ 数据字典选项 ============
// 基金类型选项
const fundTypeOptions = ref([])
const getFundTypeItem = async () => {
  const result = await GetKeyAndValueByType("t_fund_type")
  fundTypeOptions.value = result.data
}

// 运作方式选项
const operationModeOptions = ref([])
const getOperationModeItem = async () => {
  const result = await GetKeyAndValueByType("t_fund_run_way")
  operationModeOptions.value = result.data
}

// 交易类型选项
const tradeTypeOptions = ref([])
const getTransactionTypeItem = async () => {
  const result = await GetKeyAndValueByType("t_fund_transaction_type")
  tradeTypeOptions.value = result.data
}

// 持仓类型选项
const holdingTypeOptions = ref([])
const getPositionTypeItem = async () => {
  const result = await GetKeyAndValueByType("t_fund_position_type")
  holdingTypeOptions.value = result.data
}

// 行业分类选项
const industryClassOptions = ref([])
const getSectorTypeItem = async () => {
  const result = await GetKeyAndValueByType("t_fund_sector_type")
  industryClassOptions.value = result.data
}

// 数据来源选项
const dataSourceOptions = ref([])
const getDataSourceItem = async () => {
  const result = await GetKeyAndValueByType("t_fund_data_source")
  dataSourceOptions.value = result.data
}

// 等级选项(极低/低/中/高/极高)
const levelOptions = ref([])
const getLevelItem = async () => {
  const result = await GetKeyAndValueByType("t_fund_level")
  levelOptions.value = result.data
}

// 学历选项(全国前三/前十名校/985/211/其他)
const schoolTypeOptions = ref([])
const getSchoolTypeItem = async () => {
  const result = await GetKeyAndValueByType("t_fund_school_type")
  schoolTypeOptions.value = result.data
}

// 获奖记录选项(金牛奖/金基金奖/明星基金奖/晨星奖/英华奖/其他/无)
const awardOptions = ref([])
const getAwardItem = async () => {
  const result = await GetKeyAndValueByType("t_fund_award")
  awardOptions.value = result.data
}

// 其他选项（暂时保留硬编码，因为没有对应的字典类型）
const dividendMethodOptions = [
  { label: '现金分红', value: '现金分红' },
  { label: '红利再投', value: '红利再投' }
]

// 时间标识选项
const timePeriodOptions = ref([])
const getTimePeriodItem = async () => {
  const result = await GetKeyAndValueByType("t_fund_time_flag")
  timePeriodOptions.value = result.data
}

// ============ 文本映射 ============
const getTradeTypeText = (v) => {
  if (!v) return '-'
  const found = tradeTypeOptions.value.find(i => i.value === v)
  return found ? found.text : v
}

// 分红类型文本映射
const getDividendTypeText = (v) => {
  const map = { 1: '现金分红', 2: '红利再投' }
  return map[v] || '-'
}

// 时间标识文本映射
const getPeriodTypeText = (v) => {
  const map = { 1: '近1年', 2: '近3年', 3: '近5年' }
  return map[v] || '-'
}

const getHoldingTypeText = (v) => {
  if (!v) return '-'
  const found = holdingTypeOptions.value.find(i => i.value === v)
  return found ? found.text : v
}
const getIndustryClassText = (v) => {
  if (!v) return '-'
  const found = industryClassOptions.value.find(i => i.value === v)
  return found ? found.text : v
}
const getDataSourceText = (v) => {
  if (!v) return '-'
  const found = dataSourceOptions.value.find(i => i.value === v)
  return found ? found.text : v
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

// 数据来源标签颜色
const getDataSourceTagType = (v) => {
  const map = { 1: 'primary', 2: 'success' }
  return map[v] || 'info'
}

// ============ 基金基本数据 ============
const fundData = reactive({
  id: null, fundName: '', fundCode: '', fundType: '', establishDate: '',
  assetScale: null, fundCompany: '', fundCompanyDesc: '', custodian: '',
  fundManager: '', managerStartDate: '', managerDesc: '',
  operationMode: '', closedEndDays: null, purchaseFeeRate: null, redeemFeeRate: null,
  managementFeeRate: null, custodianFeeRate: null, salesServiceFeeRate: null, tradeRule: '',
  // 新增收益率字段
  return1m: null, return3m: null, return6m: null, return1y: null,
  // 新增规模字段
  netAssets: '', latestScale: null, scaleHistory: '',
  // 新增占比字段
  stockRatio: null, bondRatio: null, cashRatio: null,
  // 新增持有比例字段
  institutionRatio: null, individualRatio: null, internalRatio: null
})

// ============ 基金经理分析数据 ============
// 直接使用后端字段名，不做任何映射
const managerAnalysis = reactive({
  id: null,
  fundCode: '',
  managerName: '',
  starRating: null,
  workTime: null,
  manageScale: null,
  totalScore: null,
  stockSelectScore: null,
  returnScore: null,
  riskControlScore: null,
  stabilityScore: null,
  timingScore: null,
  managerDesc: '',
  concentrationRateAnalyse: '',
  turnoverRateAnalyse: '',
  capabilityPathAnalysis: '',
  scaleAbilityAnalysis: '',
  professionalBackground: '',
  productManagementAnalysis: '',
  stabilityAnalysis: '',
  personalHolding: '',
  holdingsConcentration: null,
  turnoverRate: null,
  backgroundMatch: null,
  scaleCapability: null,
  education: null,
  focusOnThisFund: null,
  isManagerHolding: null,
  awardRecords: null
})

// ============ 净值与估值 ============
const navData = ref([])
const navQuery = reactive({ timeRange: null })
const navPage = reactive({ page: 1, limit: 10 })
const navTableLoading = ref(false)
const navTableTotal = ref(0)

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
      navPage.page,
      navPage.limit,
      {
        fundCode: navQueryParams.fundCode,
        beginTime: navQueryParams.beginTime || null,
        endTime: navQueryParams.endTime || null
      }
    )
    
    if (result.code === 200) {
      navData.value = result.data.list || []
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

// 获取基金经理分析数据的方法
const fetchManagerAnalysisData = async () => {
  const fundCode = props.fundRowData?.fundCode
  if (!fundCode) {
    return
  }
  
  try {
    const result = await GetFundManagerAnalysisByCode(fundCode)
    
    if (result.code === 200 && result.data) {
      // 直接使用后端返回的数据，不做任何字段映射
      Object.assign(managerAnalysis, result.data)
    }
    // 如果没有数据或获取失败，保持managerAnalysis的初始状态
  } catch (error) {
    console.error('获取基金经理分析数据失败:', error)
    // 失败时保持初始状态
  }
}

// 获取基金持仓数据的方法
const fetchHoldingData = async () => {
  const fundCode = props.fundRowData?.fundCode
  if (!fundCode) {
    return
  }
  
  try {
    const result = await GetFundHoldingByCode(fundCode)
    
    if (result.code === 200 && result.data) {
      // 直接使用后端返回的数据，不做任何字段映射
      Object.assign(shareData, result.data)
    }
  } catch (error) {
    console.error('获取基金持仓数据失败:', error)
  }
}

// 净值数据已改为后端分页，navData 直接存储当前页数据

// 时间范围变化处理
const queryNavData = () => { 
  // 从时间选择器中获取时间值
  if (navQuery.timeRange && navQuery.timeRange.length === 2) {
    navQueryParams.beginTime = navQuery.timeRange[0]
    navQueryParams.endTime = navQuery.timeRange[1]
  } else {
    navQueryParams.beginTime = ''
    navQueryParams.endTime = ''
  }
  navPage.page = 1
  fetchNavData() 
}

const resetNavQuery = () => { 
  navQuery.timeRange = null
  navQueryParams.beginTime = ''
  navQueryParams.endTime = ''
  navPage.page = 1
  fetchNavData()
}

// ============ 份额与持仓 ============
// 直接使用后端字段名，不做任何映射
const shareData = reactive({
  id: null,
  fundCode: '',
  holdShares: null,
  availableShares: null,
  frozenShares: null,
  costAmount: null,
  marketValue: null,
  profitLoss: null,
  profitLossRate: null
})

// 监听持仓成本和市值变化,自动计算盈亏和收益率
watch(
  () => [shareData.costAmount, shareData.marketValue],
  () => {
    shareData.profitLoss = shareData.marketValue - shareData.costAmount
    shareData.profitLossRate = shareData.costAmount > 0 
      ? ((shareData.marketValue - shareData.costAmount) / shareData.costAmount * 100) 
      : 0
  },
  { immediate: true }
)

// ============ 交易与流水 ============
const tradeData = ref([])
const tradeQuery = reactive({ timeRange: null })
const tradePage = reactive({ page: 1, limit: 5 })

// 交易数据已改为后端分页，tradeData 直接存储当前页数据

// 交易数据查询参数
const tradeQueryParams = reactive({
  fundCode: '',
  beginTime: '',
  endTime: ''
})

// 交易数据加载状态和总数
const tradeTableLoading = ref(false)
const tradeTableTotal = ref(0)

// 获取交易数据的方法
const fetchTradeData = async () => {
  if (!tradeQueryParams.fundCode) {
    return
  }
  
  tradeTableLoading.value = true
  try {
    const result = await GetFundTransactionByConditionAndPage(
      tradePage.page,
      tradePage.limit,
      {
        fundCode: tradeQueryParams.fundCode,
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

const queryTradeData = () => { 
  // 从时间选择器中获取时间值
  if (tradeQuery.timeRange && tradeQuery.timeRange.length === 2) {
    tradeQueryParams.beginTime = tradeQuery.timeRange[0]
    tradeQueryParams.endTime = tradeQuery.timeRange[1]
  } else {
    tradeQueryParams.beginTime = ''
    tradeQueryParams.endTime = ''
  }
  tradePage.page = 1
  fetchTradeData()
}

const resetTradeQuery = () => { 
  tradeQuery.timeRange = null
  tradeQueryParams.beginTime = ''
  tradeQueryParams.endTime = ''
  tradePage.page = 1
  fetchTradeData()
}

// ============ 分红信息 ============
const dividendData = ref([])
const dividendQuery = reactive({ timeRange: null })
const dividendPage = reactive({ page: 1, limit: 5 })

// 分红数据已改为后端分页，dividendData 直接存储当前页数据

// 分红数据查询参数
const dividendQueryParams = reactive({
  fundCode: '',
  beginTime: '',
  endTime: ''
})

// 分红数据加载状态和总数
const dividendTableLoading = ref(false)
const dividendTableTotal = ref(0)

// 获取分红数据的方法
const fetchDividendData = async () => {
  if (!dividendQueryParams.fundCode) {
    return
  }
  
  dividendTableLoading.value = true
  try {
    const result = await GetFundDividendByConditionAndPage(
      dividendPage.page,
      dividendPage.limit,
      {
        fundCode: dividendQueryParams.fundCode,
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

const queryDividendData = () => { 
  // 从时间选择器中获取时间值
  if (dividendQuery.timeRange && dividendQuery.timeRange.length === 2) {
    dividendQueryParams.beginTime = dividendQuery.timeRange[0]
    dividendQueryParams.endTime = dividendQuery.timeRange[1]
  } else {
    dividendQueryParams.beginTime = ''
    dividendQueryParams.endTime = ''
  }
  dividendPage.page = 1
  fetchDividendData()
}

const resetDividendQuery = () => { 
  dividendQuery.timeRange = null
  dividendQueryParams.beginTime = ''
  dividendQueryParams.endTime = ''
  dividendPage.page = 1
  fetchDividendData()
}

// ============ 风险与绩效 ============
const riskData = ref([])

// 获取风险收益数据的方法
const fetchRiskPerformanceData = async () => {
  const fundCode = fundData.fundCode
  if (!fundCode) {
    return
  }
  
  try {
    const result = await GetFundRiskPerformanceByCode(fundCode)
    
    if (result.code === 200) {
      riskData.value = result.data || []
    } else {
      ElMessage.error(result.message || '获取风险收益数据失败')
    }
  } catch (error) {
    console.error('获取风险收益数据失败:', error)
    ElMessage.error('获取风险收益数据失败')
  }
}

// ============ 持仓信息 ============
const holdingData = ref([])
const holdingQuery = reactive({ timeRange: null })
const holdingPage = reactive({ page: 1, limit: 10 })

// 持仓数据已改为后端分页，holdingData 直接存储当前页数据

// 持仓数据查询参数
const holdingQueryParams = reactive({
  fundCode: '',
  beginTime: '',
  endTime: ''
})

// 持仓数据加载状态和总数
const holdingTableLoading = ref(false)
const holdingTableTotal = ref(0)

// 获取持仓数据的方法
const fetchPortfolioData = async () => {
  if (!holdingQueryParams.fundCode) {
    return
  }
  
  holdingTableLoading.value = true
  try {
    const result = await GetFundPortfolioByConditionAndPage(
      holdingPage.page,
      holdingPage.limit,
      {
        fundCode: holdingQueryParams.fundCode,
        beginTime: holdingQueryParams.beginTime || null,
        endTime: holdingQueryParams.endTime || null
      }
    )
    
    if (result.code === 200) {
      holdingData.value = result.data.list || []
      holdingTableTotal.value = result.data.total || 0
    } else {
      ElMessage.error(result.message || '获取持仓数据失败')
    }
  } catch (error) {
    console.error('获取持仓数据失败:', error)
    ElMessage.error('获取持仓数据失败')
  } finally {
    holdingTableLoading.value = false
  }
}

const queryHoldingData = () => { 
  // 从时间选择器中获取时间值
  if (holdingQuery.timeRange && holdingQuery.timeRange.length === 2) {
    holdingQueryParams.beginTime = holdingQuery.timeRange[0]
    holdingQueryParams.endTime = holdingQuery.timeRange[1]
  } else {
    holdingQueryParams.beginTime = ''
    holdingQueryParams.endTime = ''
  }
  holdingPage.page = 1
  fetchPortfolioData()
}

const resetHoldingQuery = () => { 
  holdingQuery.timeRange = null
  holdingQueryParams.beginTime = ''
  holdingQueryParams.endTime = ''
  holdingPage.page = 1
  fetchPortfolioData()
}

// ============ 子表格行编辑对话框 ============
const subDialogVisible = ref(false)
const subDialogType = ref('')
const subDialogTitle = ref('')
const subEditIndex = ref(-1)
const subFormData = reactive({})

const subDialogTitles = {
  nav: '净值与估值', trade: '交易与流水', dividend: '分红信息', risk: '风险与绩效指标', holding: '持仓信息'
}

const openSubDialog = (type, index, data) => {
  subDialogType.value = type
  subEditIndex.value = index
  subDialogTitle.value = (index >= 0 ? '编辑' : '新增') + subDialogTitles[type]
  Object.keys(subFormData).forEach(k => delete subFormData[k])
  Object.assign(subFormData, JSON.parse(JSON.stringify(data)))
  subDialogVisible.value = true
}

const confirmSubDialog = async () => {
  const type = subDialogType.value
  const idx = subEditIndex.value
  const data = JSON.parse(JSON.stringify(subFormData))

  if (type === 'nav') {
    data.fundCode = fundData.fundCode
    console.log('净值数据:', data)
    console.log('是否修改:', idx >= 0, '索引:', idx)
    try {
      if (idx >= 0) {
        console.log('更新净值，ID:', data.id)
        const result = await UpdateFundNav(data)
        if (result.code === 200) {
          ElMessage.success('修改成功')
          // 重新查询数据
          await fetchNavData()
        } else {
          ElMessage.error(result.message || '修改失败')
          return
        }
      } else {
        console.log('新增净值')
        const result = await AddFundNav(data)
        if (result.code === 200) {
          ElMessage.success('新增成功')
          // 重新查询数据
          await fetchNavData()
        } else {
          ElMessage.error(result.message || '新增失败')
          return
        }
      }
    } catch (error) {
      console.error('净值操作失败:', error)
      ElMessage.error('操作失败')
      return
    }
  } else if (type === 'trade') {
    data.fundCode = fundData.fundCode
    console.log('交易数据:', data)
    console.log('是否修改:', idx >= 0, '索引:', idx)
    try {
      if (idx >= 0) {
        console.log('更新交易，ID:', data.id)
        const result = await UpdateFundTransaction(data)
        if (result.code === 200) {
          ElMessage.success('修改成功')
          // 重新查询数据
          await fetchTradeData()
        } else {
          ElMessage.error(result.message || '修改失败')
          return
        }
      } else {
        console.log('新增交易')
        const result = await AddFundTransaction(data)
        if (result.code === 200) {
          ElMessage.success('新增成功')
          // 重新查询数据
          await fetchTradeData()
        } else {
          ElMessage.error(result.message || '新增失败')
          return
        }
      }
    } catch (error) {
      console.error('交易操作失败:', error)
      ElMessage.error('操作失败')
      return
    }
  } else if (type === 'dividend') {
    data.fundCode = fundData.fundCode
    console.log('分红数据:', data)
    console.log('是否修改:', idx >= 0, '索引:', idx)
    try {
      if (idx >= 0) {
        console.log('更新分红，ID:', data.id)
        const result = await UpdateFundDividend(data)
        if (result.code === 200) {
          ElMessage.success('修改成功')
          // 重新查询数据
          await fetchDividendData()
        } else {
          ElMessage.error(result.message || '修改失败')
          return
        }
      } else {
        console.log('新增分红')
        const result = await AddFundDividend(data)
        if (result.code === 200) {
          ElMessage.success('新增成功')
          // 重新查询数据
          await fetchDividendData()
        } else {
          ElMessage.error(result.message || '新增失败')
          return
        }
      }
    } catch (error) {
      console.error('分红操作失败:', error)
      ElMessage.error('操作失败')
      return
    }
  } else if (type === 'risk') {
    data.fundCode = fundData.fundCode
    console.log('风险绩效数据:', data)
    console.log('是否修改:', idx >= 0, '索引:', idx)
    try {
      if (idx >= 0) {
        console.log('更新风险绩效，ID:', data.id)
        const result = await UpdateFundRiskPerformance(data)
        if (result.code === 200) {
          ElMessage.success('修改成功')
          // 重新查询数据
          await fetchRiskPerformanceData()
        } else {
          ElMessage.error(result.message || '修改失败')
          return
        }
      } else {
        console.log('新增风险绩效')
        const result = await AddFundRiskPerformance(data)
        if (result.code === 200) {
          ElMessage.success('新增成功')
          // 重新查询数据
          await fetchRiskPerformanceData()
        } else {
          ElMessage.error(result.message || '新增失败')
          return
        }
      }
    } catch (error) {
      console.error('风险绩效操作失败:', error)
      ElMessage.error('操作失败')
      return
    }
  } else if (type === 'holding') {
    data.fundCode = fundData.fundCode
    console.log('持仓信息数据:', data)
    console.log('是否修改:', idx >= 0, '索引:', idx)
    try {
      if (idx >= 0) {
        console.log('更新持仓信息，ID:', data.id)
        const result = await UpdateFundPortfolio(data)
        if (result.code === 200) {
          ElMessage.success('修改成功')
          // 重新查询数据
          await fetchPortfolioData()
        } else {
          ElMessage.error(result.message || '修改失败')
          return
        }
      } else {
        console.log('新增持仓信息')
        const result = await AddFundPortfolio(data)
        if (result.code === 200) {
          ElMessage.success('新增成功')
          // 重新查询数据
          await fetchPortfolioData()
        } else {
          ElMessage.error(result.message || '新增失败')
          return
        }
      }
    } catch (error) {
      console.error('持仓信息操作失败:', error)
      ElMessage.error('操作失败')
      return
    }
  }

  subDialogVisible.value = false
}

// ============ 净值 CRUD ============
const addNavRow = () => openSubDialog('nav', -1, { navDate: '', unitNav: 0, accumulatedNav: 0, dailyChangeRate: 0, valuation: 0 })
const editNavRow = (row) => {
  const idx = navData.value.findIndex(item => item.id === row.id)
  openSubDialog('nav', idx >= 0 ? idx : -1, { ...row })
}
const deleteNavRow = async (row) => {
  console.log('删除净值数据:', row)
  console.log('row.id:', row.id, '类型:', typeof row.id)
  
  if (row.id) {
    try {
      console.log('调用删除接口，ID:', row.id)
      const result = await DeleteFundNav(row.id)
      console.log('删除接口返回:', result)
      
      if (result.code === 200) {
        const idx = navData.value.findIndex(item => item.id === row.id)
        if (idx >= 0) {
          navData.value.splice(idx, 1)
        }
        ElMessage.success('删除成功')
        // 重新查询数据
        await fetchNavData()
      } else {
        ElMessage.error(result.message || '删除失败')
      }
    } catch (error) {
      console.error('删除净值失败:', error)
      ElMessage.error('删除失败')
    }
  } else {
    console.log('无ID，直接从数组删除')
    const idx = navData.value.findIndex(item => item === row)
    if (idx >= 0) {
      navData.value.splice(idx, 1)
    }
    ElMessage.success('删除成功')
  }
}

// ============ 交易 CRUD ============
const addTradeRow = () => openSubDialog('trade', -1, { tradeDate: '', tradeType: 1, tradeShares: 0, tradeAmount: 0, tradeNav: 0, tradeFee: 0, confirmDate: '' })
const editTradeRow = (row) => {
  const idx = tradeData.value.findIndex(item => item.id === row.id)
  openSubDialog('trade', idx >= 0 ? idx : -1, { ...row })
}
const deleteTradeRow = async (row) => {
  console.log('删除交易数据:', row)
  console.log('row.id:', row.id, '类型:', typeof row.id)

  if (row.id) {
    try {
      console.log('调用删除接口，ID:', row.id)
      const result = await DeleteFundTransaction(row.id)
      console.log('删除接口返回:', result)

      if (result.code === 200) {
        const idx = tradeData.value.findIndex(item => item.id === row.id)
        if (idx >= 0) {
          tradeData.value.splice(idx, 1)
        }
        ElMessage.success('删除成功')
        // 重新查询数据
        await fetchTradeData()
      } else {
        ElMessage.error(result.message || '删除失败')
      }
    } catch (error) {
      console.error('删除交易失败:', error)
      ElMessage.error('删除失败')
    }
  } else {
    console.log('无ID，直接从数组删除')
    const idx = tradeData.value.findIndex(item => item === row)
    if (idx >= 0) {
      tradeData.value.splice(idx, 1)
    }
    ElMessage.success('删除成功')
  }
}

// ============ 分红 CRUD ============
const addDividendRow = () => openSubDialog('dividend', -1, { dividendDate: '', arrivalDate: '', dividendType: 1, dividendRule: '', dividendAmount: 0 })
const editDividendRow = (row) => {
  const idx = dividendData.value.findIndex(item => item.id === row.id)
  openSubDialog('dividend', idx >= 0 ? idx : -1, { ...row })
}
const deleteDividendRow = async (row) => {
  console.log('删除分红数据:', row)
  console.log('row.id:', row.id, '类型:', typeof row.id)

  if (row.id) {
    try {
      console.log('调用删除接口，ID:', row.id)
      const result = await DeleteFundDividend(row.id)
      console.log('删除接口返回:', result)

      if (result.code === 200) {
        const idx = dividendData.value.findIndex(item => item.id === row.id)
        if (idx >= 0) {
          dividendData.value.splice(idx, 1)
        }
        ElMessage.success('删除成功')
        // 重新查询数据
        await fetchDividendData()
      } else {
        ElMessage.error(result.message || '删除失败')
      }
    } catch (error) {
      console.error('删除分红失败:', error)
      ElMessage.error('删除失败')
    }
  } else {
    console.log('无ID，直接从数组删除')
    const idx = dividendData.value.findIndex(item => item === row)
    if (idx >= 0) {
      dividendData.value.splice(idx, 1)
    }
    ElMessage.success('删除成功')
  }
}

// ============ 风险指标 CRUD ============
const addRiskRow = () => openSubDialog('risk', -1, { periodType: 1, maxDrawDown: 0, recoveryDays: 0, annualReturn: 0, sharpeRatio: 0, volatility: 0 })
const editRiskRow = (row) => {
  const idx = riskData.value.findIndex(item => item.id === row.id)
  openSubDialog('risk', idx >= 0 ? idx : -1, { ...row })
}
const deleteRiskRow = async (row) => {
  console.log('删除风险绩效数据:', row)
  console.log('row.id:', row.id, '类型:', typeof row.id)

  if (row.id) {
    try {
      console.log('调用删除接口，ID:', row.id)
      const result = await DeleteFundRiskPerformance(row.id)
      console.log('删除接口返回:', result)

      if (result.code === 200) {
        const idx = riskData.value.findIndex(item => item.id === row.id)
        if (idx >= 0) {
          riskData.value.splice(idx, 1)
        }
        ElMessage.success('删除成功')
        // 重新查询数据
        await fetchRiskPerformanceData()
      } else {
        ElMessage.error(result.message || '删除失败')
      }
    } catch (error) {
      console.error('删除风险绩效失败:', error)
      ElMessage.error('删除失败')
    }
  } else {
    console.log('无ID，直接从数组删除')
    const idx = riskData.value.findIndex(item => item === row)
    if (idx >= 0) {
      riskData.value.splice(idx, 1)
    }
    ElMessage.success('删除成功')
  }
}

// ============ 持仓 CRUD ============
const addHoldingRow = () => openSubDialog('holding', -1, { portfolioDate: '', positionType: 1, positionCode: '', positionName: '', positionQuantity: 0, positionMarketValue: 0, netRatio: 0, industryType: 1, dataSource: 1 })
const editHoldingRow = (row) => {
  const idx = holdingData.value.findIndex(item => item.id === row.id)
  openSubDialog('holding', idx >= 0 ? idx : -1, { ...row })
}
const deleteHoldingRow = async (row) => {
  console.log('删除持仓信息数据:', row)
  console.log('row.id:', row.id, '类型:', typeof row.id)

  if (row.id) {
    try {
      console.log('调用删除接口，ID:', row.id)
      const result = await DeleteFundPortfolio(row.id)
      console.log('删除接口返回:', result)

      if (result.code === 200) {
        const idx = holdingData.value.findIndex(item => item.id === row.id)
        if (idx >= 0) {
          holdingData.value.splice(idx, 1)
        }
        ElMessage.success('删除成功')
        // 重新查询数据
        await fetchPortfolioData()
      } else {
        ElMessage.error(result.message || '删除失败')
      }
    } catch (error) {
      console.error('删除持仓信息失败:', error)
      ElMessage.error('删除失败')
    }
  } else {
    console.log('无ID，直接从数组删除')
    const idx = holdingData.value.findIndex(item => item === row)
    if (idx >= 0) {
      holdingData.value.splice(idx, 1)
    }
    ElMessage.success('删除成功')
  }
}

// ============ 初始化数据 ============
const initDialogData = () => {
  const row = props.fundRowData
  // 直接赋值，后端返回什么就填入什么（包括0）
  Object.assign(fundData, {
    id: row.id,
    fundName: row.fundName ?? '',
    fundCode: row.fundCode ?? '',
    fundType: row.fundType,
    establishDate: row.establishDate ?? '',
    assetScale: row.assetScale,
    fundCompany: row.fundCompany ?? '',
    fundCompanyDesc: row.fundCompanyDesc ?? '',
    custodian: row.custodian ?? '',
    fundManager: row.fundManager ?? '',
    managerStartDate: row.managerStartDate ?? '',
    managerDesc: row.managerDesc ?? '',
    operationMode: row.operationMode,
    closedEndDays: row.closedEndDays,
    purchaseFeeRate: row.purchaseFeeRate,
    redeemFeeRate: row.redeemFeeRate,
    managementFeeRate: row.managementFeeRate,
    custodianFeeRate: row.custodianFeeRate,
    salesServiceFeeRate: row.salesServiceFeeRate,
    tradeRule: row.tradeRule ?? '',
    // 新增字段初始化
    return1m: row.return1m,
    return3m: row.return3m,
    return6m: row.return6m,
    return1y: row.return1y,
    netAssets: row.netAssets ?? '',
    latestScale: row.latestScale,
    scaleHistory: row.scaleHistory ?? '',
    stockRatio: row.stockRatio,
    bondRatio: row.bondRatio,
    cashRatio: row.cashRatio,
    institutionRatio: row.institutionRatio,
    individualRatio: row.individualRatio,
    internalRatio: row.internalRatio
  })

  // 初始化基金经理分析数据 - 改为从后端接口获取
  fetchManagerAnalysisData()

  // 净值数据 - 改为从后端接口获取
  navQueryParams.fundCode = row.fundCode || ''
  navQueryParams.beginTime = ''
  navQueryParams.endTime = ''
  navQuery.timeRange = null
  navPage.page = 1
  navPage.limit = 10
  // 自动调用接口获取净值数据
  fetchNavData()

  // 份额数据 - 改为从后端接口获取
  fetchHoldingData()

  // 交易数据 - 改为从后端接口获取
  tradeQueryParams.fundCode = row.fundCode || ''
  tradeQueryParams.beginTime = ''
  tradeQueryParams.endTime = ''
  tradeQuery.timeRange = null
  tradePage.page = 1
  tradePage.limit = 5
  // 自动调用接口获取交易数据
  fetchTradeData()

  // 分红数据 - 改为从后端接口获取
  dividendQueryParams.fundCode = row.fundCode || ''
  dividendQueryParams.beginTime = ''
  dividendQueryParams.endTime = ''
  dividendQuery.timeRange = null
  dividendPage.page = 1
  dividendPage.limit = 5
  // 自动调用接口获取分红数据
  fetchDividendData()

  // 风险指标 - 改为从后端接口获取
  // 自动调用接口获取风险收益数据
  fetchRiskPerformanceData()

  // 持仓数据 - 改为从后端接口获取
  holdingQueryParams.fundCode = row.fundCode || ''
  holdingQueryParams.beginTime = ''
  holdingQueryParams.endTime = ''
  holdingQuery.timeRange = null
  holdingPage.page = 1
  holdingPage.limit = 10
  // 自动调用接口获取持仓数据
  fetchPortfolioData()
}

watch(() => props.visible, (val) => {
  if (val) initDialogData()
})

// ============ 保存 & 关闭 ============
const handleSave = () => {
  // 直接使用后端字段，不做任何映射
  const savePayload = {
    // 基金基本数据
    fundAsset: JSON.parse(JSON.stringify(fundData)),
    // 基金经理分析数据
    managerAnalysis: JSON.parse(JSON.stringify(managerAnalysis)),
    // 基金持仓数据（份额信息）
    fundHolding: JSON.parse(JSON.stringify(shareData)),
    // 其他数据保持不变
    navList: JSON.parse(JSON.stringify(navData.value)),
    tradeList: JSON.parse(JSON.stringify(tradeData.value)),
    dividendList: JSON.parse(JSON.stringify(dividendData.value)),
    riskList: JSON.parse(JSON.stringify(riskData.value)),
    holdingList: JSON.parse(JSON.stringify(holdingData.value))
  }
  emit('save', savePayload)
}

const handleClose = () => {
  dialogVisible.value = false
}

// ============ 获取实时数据 ============
const fetchLoading = ref(false)

const handleFetchRealTimeData = async () => {
  const fundCode = props.fundRowData?.fundCode
  if (!fundCode) {
    ElMessage.warning('缺少基金代码，无法获取数据')
    return
  }
  fetchLoading.value = true
  try {
    const result = await AddFundImportData(fundCode)
    if (result.code === 200) {
      ElMessage.success('实时数据获取成功')
    } else {
      ElMessage.error(result.message || '获取实时数据失败')
    }
  } catch (error) {
    ElMessage.error('获取实时数据失败')
  } finally {
    fetchLoading.value = false
  }
}

// ============ 钩子函数 ============
onMounted(() => {
  // 加载数据字典
  getFundTypeItem()
  getOperationModeItem()
  getTransactionTypeItem()
  getPositionTypeItem()
  getSectorTypeItem()
  getDataSourceItem()
  getTimePeriodItem()
  // 加载新增的数据字典
  getLevelItem()
  getSchoolTypeItem()
  getAwardItem()
})
</script>

<style scoped>
/* ====== 对话框整体容器 ====== */
.fund-detail-dialog :deep(.el-dialog) {
  height: 100%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.fund-detail-dialog :deep(.el-dialog__header) {
  padding: 0;
  margin: 0;
}

.fund-detail-dialog :deep(.el-dialog__body) {
  flex: 1;
  overflow-y: auto;
  padding: 0;
  background: #f5f7fa;
}

/* ====== 标题栏 ====== */
.detail-header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  padding: 16px 24px;
  box-shadow: 0 2px 12px rgba(30, 60, 114, 0.3);
}

.detail-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail-title-icon {
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

.header-title {
  font-size: 17px;
  font-weight: 600;
  color: #fff;
  letter-spacing: 0.3px;
}

.detail-header-right {
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

/* ====== 按钮操作块 ====== */
.detail-actions {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  gap: 12px;
  padding: 14px 24px;
  background: #fff;
  border-bottom: 1px solid #e8ecf1;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.detail-actions :deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 6px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
  padding: 8px 20px;
}

.detail-actions :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.5);
}

.detail-actions :deep(.el-button--default) {
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.3s ease;
  padding: 8px 20px;
  border-color: #dcdfe6;
}

.detail-actions :deep(.el-button--default:hover) {
  transform: translateY(-2px);
  border-color: #c0c4cc;
  background: #f5f7fa;
}

.detail-actions :deep(.el-button--warning) {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border: none;
  border-radius: 6px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(240, 147, 251, 0.4);
  transition: all 0.3s ease;
  padding: 8px 20px;
}

.detail-actions :deep(.el-button--warning:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(240, 147, 251, 0.5);
}

/* ====== 滚动内容区 ====== */
.detail-body {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 20px 24px;
}

/* ====== 分区块 ====== */
.section-block {
  background: #fff;
  border: 1px solid #e8ecf1;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.section-block:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  background: linear-gradient(135deg, #f8f9fc 0%, #eef1f5 100%);
  font-size: 15px;
  font-weight: 600;
  color: #2a5298;
  border-bottom: 1px solid #e8ecf1;
}

.section-title :deep(.el-icon) {
  font-size: 18px;
  color: #667eea;
}

.section-content {
  padding: 20px;
}

/* ====== 表单样式优化 ====== */
.section-content :deep(.el-form) {
  width: 100%;
}

.section-content :deep(.el-form-item) {
  margin-bottom: 18px;
}

.section-content :deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

/* ====== 子区块工具栏 ====== */
.sub-section-toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #fafbfc;
  border-radius: 6px;
  border: 1px solid #e8ecf1;
  flex-wrap: wrap;
  gap: 10px;
}

.sub-section-toolbar :deep(.el-date-editor) {
  border-radius: 6px;
}

.sub-section-toolbar :deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 6px;
  font-weight: 500;
  box-shadow: 0 2px 6px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}

.sub-section-toolbar :deep(.el-button--primary:hover) {
  transform: translateY(-1px);
  box-shadow: 0 3px 8px rgba(102, 126, 234, 0.4);
}

.sub-section-toolbar :deep(.el-button--default) {
  border-radius: 6px;
  transition: all 0.3s ease;
  border-color: #dcdfe6;
}

.sub-section-toolbar :deep(.el-button--default:hover) {
  transform: translateY(-1px);
  background: #f5f7fa;
}

.sub-section-toolbar :deep(.el-button--success) {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  border: none;
  border-radius: 6px;
  font-weight: 500;
  box-shadow: 0 2px 6px rgba(17, 153, 142, 0.3);
  transition: all 0.3s ease;
}

.sub-section-toolbar :deep(.el-button--success:hover) {
  transform: translateY(-1px);
  box-shadow: 0 3px 8px rgba(17, 153, 142, 0.4);
}

/* ====== 表格样式优化 ====== */
.section-content :deep(.el-table) {
  border-radius: 6px;
  overflow: hidden;
}

.section-content :deep(.el-table th) {
  background: #f8f9fc;
  color: #606266;
  font-weight: 600;
}

.section-content :deep(.el-table td) {
  padding: 10px 0;
}

/* ====== 子区块分页 ====== */
.sub-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #f0f2f5;
}

.sub-pagination :deep(.el-pagination) {
  gap: 8px;
}

/* ====== 涨跌颜色 ====== */
.text-red {
  color: #f56c6c;
  font-weight: 600;
}

.text-green {
  color: #67c23a;
  font-weight: 600;
}

/* ====== 子编辑对话框 ====== */
.sub-edit-dialog :deep(.el-dialog) {
  border-radius: 10px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
}

.sub-edit-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #f8f9fc 0%, #eef1f5 100%);
  border-bottom: 1px solid #e8ecf1;
  padding: 16px 20px;
}

.sub-edit-dialog :deep(.el-dialog__title) {
  font-weight: 600;
  color: #2a5298;
}

.sub-edit-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.sub-edit-dialog :deep(.el-form-item) {
  margin-bottom: 20px;
}
</style>
