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
                  <el-input v-model="fundData.fundManager" placeholder="请输入基金管理人" clearable />
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
                  <el-input v-model="fundData.fundCustodian" placeholder="请输入基金托管者" clearable />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="基金经理">
                  <el-input v-model="fundData.fundManagerName" placeholder="请输入基金经理" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="经理任职日期">
                  <el-date-picker v-model="fundData.managerAppointDate" type="date" style="width: 100%" placeholder="选择日期" value-format="YYYY-MM-DD" :editable="false" />
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
                  <el-input-number v-model="fundData.closedPeriod" :min="0" :step="1" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="申购费率(%)">
                  <el-input-number v-model="fundData.purchaseRate" :precision="4" :step="0.001" :min="0" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="赎回费率(%)">
                  <el-input-number v-model="fundData.redemptionRate" :precision="4" :step="0.001" :min="0" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="管理费(%)">
                  <el-input-number v-model="fundData.managementFee" :precision="4" :step="0.001" :min="0" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="托管费(%)">
                  <el-input-number v-model="fundData.custodianFee" :precision="4" :step="0.001" :min="0" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="销售服务费(%)">
                  <el-input-number v-model="fundData.salesServiceFee" :precision="4" :step="0.001" :min="0" style="width: 100%" controls-position="right" />
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
                  <el-input-number v-model="fundData.returnRate1Month" :precision="2" :step="0.01" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="近3月收益率(%)">
                  <el-input-number v-model="fundData.returnRate3Month" :precision="2" :step="0.01" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="近6月收益率(%)">
                  <el-input-number v-model="fundData.returnRate6Month" :precision="2" :step="0.01" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="近1年收益率(%)">
                  <el-input-number v-model="fundData.returnRate1Year" :precision="2" :step="0.01" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="净资产规模(亿)">
                  <el-input-number v-model="fundData.netAssetScale" :precision="2" :step="0.01" :min="0" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="最新规模(亿)">
                  <el-input-number v-model="fundData.latestScale" :precision="2" :step="0.01" :min="0" style="width: 100%" controls-position="right" />
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
                  <el-input-number v-model="fundData.institutionHoldRatio" :precision="2" :step="0.01" :min="0" :max="100" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="个人持有比例(%)">
                  <el-input-number v-model="fundData.personalHoldRatio" :precision="2" :step="0.01" :min="0" :max="100" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="内部持有比例(%)">
                  <el-input-number v-model="fundData.internalHoldRatio" :precision="2" :step="0.01" :min="0" :max="100" style="width: 100%" controls-position="right" />
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
            <el-date-picker v-model="navQuery.timeRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" :editable="false" :unlink-panels="true" size="small" style="width: 280px; margin-right: 12px;" clearable />
            <el-button type="primary" size="small" @click="queryNavData" :icon="Search">查询</el-button>
            <el-button size="small" @click="resetNavQuery" :icon="Refresh">重置</el-button>
            <div style="flex:1"></div>
            <el-button type="success" size="small" @click="addNavRow" :icon="Plus">新增</el-button>
          </div>
          <el-table :data="navPageData" border stripe size="small" max-height="320px">
            <el-table-column prop="navDate" label="净值日期" width="120" />
            <el-table-column prop="unitNav" label="单位净值" width="110" align="right" />
            <el-table-column prop="accumulatedNav" label="累计净值" width="110" align="right" />
            <el-table-column prop="dailyChangeRate" label="日涨跌幅(%)" width="120" align="right">
              <template #default="{ row }">
                <span :class="row.dailyChangeRate >= 0 ? 'text-red' : 'text-green'">{{ row.dailyChangeRate }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="estimateNav" label="估值" width="110" align="right" />
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ $index }">
                <el-button text size="small" @click="editNavRow($index)">修改</el-button>
                <el-button text size="small" type="danger" @click="deleteNavRow($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="sub-pagination">
            <el-pagination background layout="total, sizes, prev, pager, next" :total="navFilterData.length" :page-size="navPage.limit" :current-page="navPage.page" :page-sizes="[10, 20, 50]" @size-change="(s) => { navPage.limit = s; navPage.page = 1 }" @current-change="(p) => navPage.page = p" size="small" />
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
                  <el-input v-model="managerAnalysis.name" placeholder="请输入基金经理姓名" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="星级">
                  <el-rate v-model="managerAnalysis.starLevel" :max="5" allow-half show-score />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="从业时间(年)">
                  <el-input-number v-model="managerAnalysis.workYears" :precision="1" :step="0.5" :min="0" style="width: 100%" controls-position="right" />
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
                    <el-option label="全国前三" value="全国前三" />
                    <el-option label="前十名校" value="前十名校" />
                    <el-option label="985" value="985" />
                    <el-option label="211" value="211" />
                    <el-option label="其他" value="其他" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="个人持有">
                  <el-select v-model="managerAnalysis.personalHold" placeholder="请选择" style="width: 100%" clearable>
                    <el-option label="是" :value="1" />
                    <el-option label="否" :value="0" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <!-- 评分信息 -->
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="综合评分">
                  <el-input-number v-model="managerAnalysis.overallScore" :precision="2" :step="0.1" :min="0" :max="100" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="选证能力评分">
                  <el-input-number v-model="managerAnalysis.selectionScore" :precision="2" :step="0.1" :min="0" :max="100" style="width: 100%" controls-position="right" />
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
                  <el-input-number v-model="managerAnalysis.riskScore" :precision="2" :step="0.1" :min="0" :max="100" style="width: 100%" controls-position="right" />
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
                  <el-select v-model="managerAnalysis.positionConcentration" placeholder="请选择" style="width: 100%" clearable>
                    <el-option label="极低" value="极低" />
                    <el-option label="低" value="低" />
                    <el-option label="中" value="中" />
                    <el-option label="高" value="高" />
                    <el-option label="极高" value="极高" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="换手率">
                  <el-select v-model="managerAnalysis.turnoverRate" placeholder="请选择" style="width: 100%" clearable>
                    <el-option label="极低" value="极低" />
                    <el-option label="低" value="低" />
                    <el-option label="中" value="中" />
                    <el-option label="高" value="高" />
                    <el-option label="极高" value="极高" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="能力路径匹配度">
                  <el-select v-model="managerAnalysis.abilityPathMatch" placeholder="请选择" style="width: 100%" clearable>
                    <el-option label="极低" value="极低" />
                    <el-option label="低" value="低" />
                    <el-option label="中" value="中" />
                    <el-option label="高" value="高" />
                    <el-option label="极高" value="极高" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="规模驾驭能力">
                  <el-select v-model="managerAnalysis.scaleControlAbility" placeholder="请选择" style="width: 100%" clearable>
                    <el-option label="极低" value="极低" />
                    <el-option label="低" value="低" />
                    <el-option label="中" value="中" />
                    <el-option label="高" value="高" />
                    <el-option label="极高" value="极高" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="本基金精力集中度">
                  <el-select v-model="managerAnalysis.focusLevel" placeholder="请选择" style="width: 100%" clearable>
                    <el-option label="极低" value="极低" />
                    <el-option label="低" value="低" />
                    <el-option label="中" value="中" />
                    <el-option label="高" value="高" />
                    <el-option label="极高" value="极高" />
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
                  <el-input v-model="managerAnalysis.positionConcentrationAnalysis" type="textarea" :rows="3" placeholder="指基金前十大重仓股占股票投资市值的比例。如果这个比例很高（比如超过60%-70%），说明基金经理喜欢集中投资，依靠深度研究的少量个股来获取收益，净值波动可能会更；如果比例较低，说明更倾向于分散投资，通过组合管理来控制风险。" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="24">
                <el-form-item label="换手率分析">
                  <el-input v-model="managerAnalysis.turnoverRateAnalysis" type="textarea" :rows="3" placeholder="反映基金经理交易股票的频繁程度。低换手率通常意味着基金经理是选股型选手，倾向于长期持有；高换手率则可能偏向交易型选手，试图通过把握市场节奏来增厚收益。" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="24">
                <el-form-item label="能力圈与路径依赖分析">
                  <el-input v-model="managerAnalysis.abilityCircleAnalysis" type="textarea" :rows="3" placeholder="关注他的行业研究背景是否与现在管理的基金相匹配。例如，一个长期研究消费的基金经理去管理一只科技主题基金，就需要审慎评估。" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="24">
                <el-form-item label="规模驾驭能力分析">
                  <el-input v-model="managerAnalysis.scaleControlAnalysis" type="textarea" :rows="3" placeholder="留意基金规模是否在短期内急剧膨胀。许多优秀的策略在资金规模变大后可能会失效（即'规模是业绩的敌人'），需要观察基金经理是否有管理大规模资金的成功经验。" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="24">
                <el-form-item label="从业背景">
                  <el-input v-model="managerAnalysis.workBackground" type="textarea" :rows="3" placeholder="他的学历专业、研究员时期的行业覆盖范围、获得过的权威奖项（如'金牛奖'）等都是重要参考。" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="24">
                <el-form-item label="产品管理情况分析">
                  <el-input v-model="managerAnalysis.productManageAnalysis" type="textarea" :rows="3" placeholder="看他是否'超负荷'工作。如果一个人名下管了十几只不同类型（如全市场选股、行业主题、量化对冲）的基金，精力难免被分散。" />
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
                  <el-input v-model="managerAnalysis.personalHoldAnalysis" type="textarea" :rows="3" placeholder="基金经理本人是否也持有了自己管理的基金？这在基金的年报/半年报中有披露，是衡量信心的重要指标。" />
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
                  <el-input-number v-model="shareData.positionCost" :precision="2" :step="100" :min="0" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="持仓市值">
                  <el-input-number v-model="shareData.positionValue" :precision="2" :step="100" :min="0" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="持仓盈亏">
                  <el-input-number v-model="shareData.positionProfit" :precision="2" :step="100" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="持仓收益率(%)">
                  <el-input-number v-model="shareData.positionProfitRate" :precision="2" :step="0.01" style="width: 100%" controls-position="right" />
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
          <el-table :data="tradePageData" border stripe size="small" max-height="280px">
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
              <template #default="{ $index }">
                <el-button text size="small" @click="editTradeRow($index)">修改</el-button>
                <el-button text size="small" type="danger" @click="deleteTradeRow($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="sub-pagination">
            <el-pagination background layout="total, sizes, prev, pager, next" :total="tradeFilterData.length" :page-size="tradePage.limit" :current-page="tradePage.page" :page-sizes="[5, 10, 20]" @size-change="(s) => { tradePage.limit = s; tradePage.page = 1 }" @current-change="(p) => tradePage.page = p" size="small" />
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
          <el-table :data="dividendPageData" border stripe size="small" max-height="280px">
            <el-table-column prop="dividendDate" label="分红时间" width="120" />
            <el-table-column prop="arrivalDate" label="到账时间" width="120" />
            <el-table-column prop="dividendMethod" label="分红方式" width="100">
              <template #default="{ row }">{{ getDividendMethodText(row.dividendMethod) }}</template>
            </el-table-column>
            <el-table-column prop="dividendAmount" label="分红金额" width="120" align="right" />
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ $index }">
                <el-button text size="small" @click="editDividendRow($index)">修改</el-button>
                <el-button text size="small" type="danger" @click="deleteDividendRow($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="sub-pagination">
            <el-pagination background layout="total, sizes, prev, pager, next" :total="dividendFilterData.length" :page-size="dividendPage.limit" :current-page="dividendPage.page" :page-sizes="[5, 10, 20]" @size-change="(s) => { dividendPage.limit = s; dividendPage.page = 1 }" @current-change="(p) => dividendPage.page = p" size="small" />
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
            <el-table-column prop="timePeriod" label="时间标识" width="100">
              <template #default="{ row }">{{ getTimePeriodText(row.timePeriod) }}</template>
            </el-table-column>
            <el-table-column prop="maxDrawdown" label="最大回撤(%)" width="120" align="right" />
            <el-table-column prop="drawdownRecoveryDays" label="回撤修复天数" width="120" align="right" />
            <el-table-column prop="annualizedReturn" label="年化收益率(%)" width="130" align="right" />
            <el-table-column prop="sharpeRatio" label="夏普比率" width="100" align="right" />
            <el-table-column prop="volatility" label="波动率(%)" width="100" align="right" />
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ $index }">
                <el-button text size="small" @click="editRiskRow($index)">修改</el-button>
                <el-button text size="small" type="danger" @click="deleteRiskRow($index)">删除</el-button>
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
          <el-table :data="holdingPageData" border stripe size="small" max-height="320px">
            <el-table-column prop="holdingId" label="持仓ID" width="80" />
            <el-table-column prop="fundCode" label="基金代码" width="100" />
            <el-table-column prop="holdingDate" label="持仓日期" width="110" />
            <el-table-column prop="holdingType" label="持仓类型" width="90">
              <template #default="{ row }">{{ getHoldingTypeText(row.holdingType) }}</template>
            </el-table-column>
            <el-table-column prop="holdingCode" label="持仓代码" width="100" />
            <el-table-column prop="holdingName" label="持仓名称" min-width="140" show-overflow-tooltip />
            <el-table-column prop="holdingQuantity" label="持仓数量" width="100" align="right" />
            <el-table-column prop="holdingValue" label="持仓市值" width="110" align="right" />
            <el-table-column prop="navRatio" label="占净值比例(%)" width="120" align="right" />
            <el-table-column prop="industryClass" label="行业分类" width="100">
              <template #default="{ row }">{{ getIndustryClassText(row.industryClass) }}</template>
            </el-table-column>
            <el-table-column prop="dataSource" label="数据来源" width="90">
              <template #default="{ row }">{{ getDataSourceText(row.dataSource) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ $index }">
                <el-button text size="small" @click="editHoldingRow($index)">修改</el-button>
                <el-button text size="small" type="danger" @click="deleteHoldingRow($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="sub-pagination">
            <el-pagination background layout="total, sizes, prev, pager, next" :total="holdingFilterData.length" :page-size="holdingPage.limit" :current-page="holdingPage.page" :page-sizes="[10, 20, 50]" @size-change="(s) => { holdingPage.limit = s; holdingPage.page = 1 }" @current-change="(p) => holdingPage.page = p" size="small" />
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
            <el-input-number v-model="subFormData.estimateNav" :precision="4" :step="0.0001" style="width: 100%" controls-position="right" />
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
            <el-select v-model="subFormData.dividendMethod" style="width: 100%" placeholder="请选择">
              <el-option v-for="item in dividendMethodOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="分红金额">
            <el-input-number v-model="subFormData.dividendAmount" :precision="2" :step="10" :min="0" style="width: 100%" controls-position="right" />
          </el-form-item>
        </template>
        <!-- 风险指标编辑 -->
        <template v-if="subDialogType === 'risk'">
          <el-form-item label="时间标识">
            <el-select v-model="subFormData.timePeriod" style="width: 100%" placeholder="请选择">
              <el-option v-for="item in timePeriodOptions" :key="item.value" :label="item.text" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="最大回撤(%)">
            <el-input-number v-model="subFormData.maxDrawdown" :precision="2" :step="0.01" style="width: 100%" controls-position="right" />
          </el-form-item>
          <el-form-item label="回撤修复天数">
            <el-input-number v-model="subFormData.drawdownRecoveryDays" :min="0" :step="1" style="width: 100%" controls-position="right" />
          </el-form-item>
          <el-form-item label="年化收益率(%)">
            <el-input-number v-model="subFormData.annualizedReturn" :precision="2" :step="0.01" style="width: 100%" controls-position="right" />
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
          <el-form-item label="持仓ID">
            <el-input v-model="subFormData.holdingId" placeholder="自动生成" disabled />
          </el-form-item>
          <el-form-item label="基金代码">
            <el-input v-model="subFormData.fundCode" disabled />
          </el-form-item>
          <el-form-item label="持仓日期">
            <el-date-picker v-model="subFormData.holdingDate" type="date" style="width: 100%" placeholder="选择日期" value-format="YYYY-MM-DD" :editable="false" />
          </el-form-item>
          <el-form-item label="持仓类型">
            <el-select v-model="subFormData.holdingType" style="width: 100%" placeholder="请选择">
              <el-option v-for="item in holdingTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="持仓代码">
            <el-input v-model="subFormData.holdingCode" placeholder="请输入持仓代码" clearable />
          </el-form-item>
          <el-form-item label="持仓名称">
            <el-input v-model="subFormData.holdingName" placeholder="请输入持仓名称" clearable />
          </el-form-item>
          <el-form-item label="持仓数量">
            <el-input-number v-model="subFormData.holdingQuantity" :precision="2" :step="100" :min="0" style="width: 100%" controls-position="right" />
          </el-form-item>
          <el-form-item label="持仓市值">
            <el-input-number v-model="subFormData.holdingValue" :precision="2" :step="100" :min="0" style="width: 100%" controls-position="right" />
          </el-form-item>
          <el-form-item label="占净值比例(%)">
            <el-input-number v-model="subFormData.navRatio" :precision="2" :step="0.01" :min="0" style="width: 100%" controls-position="right" />
          </el-form-item>
          <el-form-item label="行业分类">
            <el-select v-model="subFormData.industryClass" style="width: 100%" placeholder="请选择">
              <el-option v-for="item in industryClassOptions" :key="item.value" :label="item.text" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="数据来源">
            <el-select v-model="subFormData.dataSource" style="width: 100%" placeholder="请选择">
              <el-option v-for="item in dataSourceOptions" :key="item.value" :label="item.text" :value="item.value" />
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
const getDividendMethodText = (v) => (dividendMethodOptions.find(i => i.value === v) || {}).label || v || '-'
const getTimePeriodText = (v) => {
  if (!v) return '-'
  const found = timePeriodOptions.value.find(i => i.value === v)
  return found ? found.text : v
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

// ============ 基金基本数据 ============
const fundData = reactive({
  id: null, fundName: '', fundCode: '', fundType: '', establishDate: '',
  assetScale: 0, fundManager: '', fundCompanyDesc: '', fundCustodian: '',
  fundManagerName: '', managerAppointDate: '', managerDesc: '',
  operationMode: '', closedPeriod: 0, purchaseRate: 0, redemptionRate: 0,
  managementFee: 0, custodianFee: 0, salesServiceFee: 0, tradeRule: '',
  // 新增收益率字段
  returnRate1Month: 0, returnRate3Month: 0, returnRate6Month: 0, returnRate1Year: 0,
  // 新增规模字段
  netAssetScale: 0, latestScale: 0, scaleHistory: '',
  // 新增占比字段
  stockRatio: 0, bondRatio: 0, cashRatio: 0,
  // 新增持有比例字段
  institutionHoldRatio: 0, personalHoldRatio: 0, internalHoldRatio: 0
})

// ============ 基金经理分析数据 ============
const managerAnalysis = reactive({
  name: '', starLevel: 0, workYears: 0, manageScale: 0,
  education: '', personalHold: null,
  overallScore: 0, selectionScore: 0, returnScore: 0,
  riskScore: 0, stabilityScore: 0, timingScore: 0,
  positionConcentration: '', turnoverRate: '', abilityPathMatch: '',
  scaleControlAbility: '', focusLevel: '',
  managerDesc: '', positionConcentrationAnalysis: '', turnoverRateAnalysis: '',
  abilityCircleAnalysis: '', scaleControlAnalysis: '', workBackground: '',
  productManageAnalysis: '', stabilityAnalysis: '', personalHoldAnalysis: ''
})

// ============ 净值与估值 ============
const navData = ref([])
const navQuery = reactive({ timeRange: null })
const navPage = reactive({ page: 1, limit: 10 })

const navFilterData = computed(() => {
  let list = [...navData.value]
  if (navQuery.timeRange && navQuery.timeRange.length === 2) {
    const [start, end] = navQuery.timeRange
    list = list.filter(r => r.navDate >= start && r.navDate <= end)
  }
  list.sort((a, b) => b.navDate.localeCompare(a.navDate))
  return list
})
const navPageData = computed(() => {
  const s = (navPage.page - 1) * navPage.limit
  return navFilterData.value.slice(s, s + navPage.limit)
})
const queryNavData = () => { navPage.page = 1 }
const resetNavQuery = () => { navQuery.timeRange = null; navPage.page = 1 }

// ============ 份额与持仓 ============
const shareData = reactive({
  holdShares: 0, availableShares: 0, frozenShares: 0,
  positionCost: 0, positionValue: 0,
  positionProfit: 0,
  positionProfitRate: 0
})

// 监听持仓成本和市值变化,自动计算盈亏和收益率
watch(
  () => [shareData.positionCost, shareData.positionValue],
  () => {
    shareData.positionProfit = shareData.positionValue - shareData.positionCost
    shareData.positionProfitRate = shareData.positionCost > 0 
      ? ((shareData.positionValue - shareData.positionCost) / shareData.positionCost * 100) 
      : 0
  },
  { immediate: true }
)

// ============ 交易与流水 ============
const tradeData = ref([])
const tradeQuery = reactive({ timeRange: null })
const tradePage = reactive({ page: 1, limit: 5 })

const tradeFilterData = computed(() => {
  let list = [...tradeData.value]
  if (tradeQuery.timeRange && tradeQuery.timeRange.length === 2) {
    const [start, end] = tradeQuery.timeRange
    list = list.filter(r => r.tradeDate >= start && r.tradeDate <= end)
  }
  list.sort((a, b) => b.tradeDate.localeCompare(a.tradeDate))
  return list
})
const tradePageData = computed(() => {
  const s = (tradePage.page - 1) * tradePage.limit
  return tradeFilterData.value.slice(s, s + tradePage.limit)
})
const queryTradeData = () => { tradePage.page = 1 }
const resetTradeQuery = () => { tradeQuery.timeRange = null; tradePage.page = 1 }

// ============ 分红信息 ============
const dividendData = ref([])
const dividendQuery = reactive({ timeRange: null })
const dividendPage = reactive({ page: 1, limit: 5 })

const dividendFilterData = computed(() => {
  let list = [...dividendData.value]
  if (dividendQuery.timeRange && dividendQuery.timeRange.length === 2) {
    const [start, end] = dividendQuery.timeRange
    list = list.filter(r => r.dividendDate >= start && r.dividendDate <= end)
  }
  list.sort((a, b) => b.dividendDate.localeCompare(a.dividendDate))
  return list
})
const dividendPageData = computed(() => {
  const s = (dividendPage.page - 1) * dividendPage.limit
  return dividendFilterData.value.slice(s, s + dividendPage.limit)
})
const queryDividendData = () => { dividendPage.page = 1 }
const resetDividendQuery = () => { dividendQuery.timeRange = null; dividendPage.page = 1 }

// ============ 风险与绩效 ============
const riskData = ref([])

// ============ 持仓信息 ============
const holdingData = ref([])
const holdingQuery = reactive({ timeRange: null })
const holdingPage = reactive({ page: 1, limit: 10 })

const holdingFilterData = computed(() => {
  let list = [...holdingData.value]
  if (holdingQuery.timeRange && holdingQuery.timeRange.length === 2) {
    const [start, end] = holdingQuery.timeRange
    list = list.filter(r => r.holdingDate >= start && r.holdingDate <= end)
  }
  list.sort((a, b) => b.holdingDate.localeCompare(a.holdingDate))
  return list
})
const holdingPageData = computed(() => {
  const s = (holdingPage.page - 1) * holdingPage.limit
  return holdingFilterData.value.slice(s, s + holdingPage.limit)
})
const queryHoldingData = () => { holdingPage.page = 1 }
const resetHoldingQuery = () => { holdingQuery.timeRange = null; holdingPage.page = 1 }

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

const confirmSubDialog = () => {
  const type = subDialogType.value
  const idx = subEditIndex.value
  const data = JSON.parse(JSON.stringify(subFormData))
  if (type === 'nav') {
    if (idx >= 0) Object.assign(navData.value[idx], data)
    else navData.value.push(data)
  } else if (type === 'trade') {
    if (idx >= 0) Object.assign(tradeData.value[idx], data)
    else tradeData.value.push(data)
  } else if (type === 'dividend') {
    if (idx >= 0) Object.assign(dividendData.value[idx], data)
    else dividendData.value.push(data)
  } else if (type === 'risk') {
    if (idx >= 0) Object.assign(riskData.value[idx], data)
    else riskData.value.push(data)
  } else if (type === 'holding') {
    if (idx >= 0) Object.assign(holdingData.value[idx], data)
    else holdingData.value.push(data)
  }
  subDialogVisible.value = false
  ElMessage.success(idx >= 0 ? '修改成功' : '新增成功')
}

// ============ 净值 CRUD ============
const addNavRow = () => openSubDialog('nav', -1, { navDate: '', unitNav: 0, accumulatedNav: 0, dailyChangeRate: 0, estimateNav: 0 })
const editNavRow = (idx) => {
  const realIdx = (navPage.page - 1) * navPage.limit + idx
  openSubDialog('nav', realIdx, navData.value[realIdx])
}
const deleteNavRow = (idx) => {
  const realIdx = (navPage.page - 1) * navPage.limit + idx
  navData.value.splice(realIdx, 1)
  ElMessage.success('删除成功')
}

// ============ 交易 CRUD ============
const addTradeRow = () => openSubDialog('trade', -1, { tradeDate: '', tradeType: '申购', tradeShares: 0, tradeAmount: 0, tradeNav: 0, tradeFee: 0, confirmDate: '' })
const editTradeRow = (idx) => {
  const realIdx = (tradePage.page - 1) * tradePage.limit + idx
  openSubDialog('trade', realIdx, tradeData.value[realIdx])
}
const deleteTradeRow = (idx) => {
  const realIdx = (tradePage.page - 1) * tradePage.limit + idx
  tradeData.value.splice(realIdx, 1)
  ElMessage.success('删除成功')
}

// ============ 分红 CRUD ============
const addDividendRow = () => openSubDialog('dividend', -1, { dividendDate: '', arrivalDate: '', dividendMethod: '现金分红', dividendAmount: 0 })
const editDividendRow = (idx) => {
  const realIdx = (dividendPage.page - 1) * dividendPage.limit + idx
  openSubDialog('dividend', realIdx, dividendData.value[realIdx])
}
const deleteDividendRow = (idx) => {
  const realIdx = (dividendPage.page - 1) * dividendPage.limit + idx
  dividendData.value.splice(realIdx, 1)
  ElMessage.success('删除成功')
}

// ============ 风险指标 CRUD ============
const addRiskRow = () => openSubDialog('risk', -1, { timePeriod: '近1年', maxDrawdown: 0, drawdownRecoveryDays: 0, annualizedReturn: 0, sharpeRatio: 0, volatility: 0 })
const editRiskRow = (idx) => openSubDialog('risk', idx, riskData.value[idx])
const deleteRiskRow = (idx) => {
  riskData.value.splice(idx, 1)
  ElMessage.success('删除成功')
}

// ============ 持仓 CRUD ============
const addHoldingRow = () => openSubDialog('holding', -1, { holdingId: 'H' + Date.now(), fundCode: fundData.fundCode, holdingDate: '', holdingType: '股票', holdingCode: '', holdingName: '', holdingQuantity: 0, holdingValue: 0, navRatio: 0, industryClass: '半导体', dataSource: '天天基金' })
const editHoldingRow = (idx) => {
  const realIdx = (holdingPage.page - 1) * holdingPage.limit + idx
  openSubDialog('holding', realIdx, holdingData.value[realIdx])
}
const deleteHoldingRow = (idx) => {
  const realIdx = (holdingPage.page - 1) * holdingPage.limit + idx
  holdingData.value.splice(realIdx, 1)
  ElMessage.success('删除成功')
}

// ============ 初始化数据 ============
const initDialogData = () => {
  const row = props.fundRowData
  Object.assign(fundData, {
    id: row.id || null, fundName: row.fundName || '', fundCode: row.fundCode || '',
    fundType: row.fundType || '', establishDate: row.establishDate || '',
    assetScale: row.assetScale || 0, fundManager: row.fundManager || '',
    fundCompanyDesc: row.fundCompanyDesc || '', fundCustodian: row.fundCustodian || '',
    fundManagerName: row.fundManagerName || '', managerAppointDate: row.managerAppointDate || '',
    managerDesc: row.managerDesc || '', operationMode: row.operationMode || '',
    closedPeriod: row.closedPeriod || 0, purchaseRate: row.purchaseRate || 0,
    redemptionRate: row.redemptionRate || 0, managementFee: row.managementFee || 0,
    custodianFee: row.custodianFee || 0, salesServiceFee: row.salesServiceFee || 0,
    tradeRule: row.tradeRule || '',
    // 新增字段初始化
    returnRate1Month: row.returnRate1Month || 0, returnRate3Month: row.returnRate3Month || 0,
    returnRate6Month: row.returnRate6Month || 0, returnRate1Year: row.returnRate1Year || 0,
    netAssetScale: row.netAssetScale || 0, latestScale: row.latestScale || 0,
    scaleHistory: row.scaleHistory || '',
    stockRatio: row.stockRatio || 0, bondRatio: row.bondRatio || 0, cashRatio: row.cashRatio || 0,
    institutionHoldRatio: row.institutionHoldRatio || 0, personalHoldRatio: row.personalHoldRatio || 0,
    internalHoldRatio: row.internalHoldRatio || 0
  })

  // 初始化基金经理分析数据
  if (row.managerAnalysis) {
    Object.assign(managerAnalysis, row.managerAnalysis)
  } else {
    Object.assign(managerAnalysis, {
      name: row.fundManagerName || '', starLevel: 0, workYears: 0, manageScale: 0,
      education: '', personalHold: null,
      overallScore: 0, selectionScore: 0, returnScore: 0,
      riskScore: 0, stabilityScore: 0, timingScore: 0,
      positionConcentration: '', turnoverRate: '', abilityPathMatch: '',
      scaleControlAbility: '', focusLevel: '',
      managerDesc: '', positionConcentrationAnalysis: '', turnoverRateAnalysis: '',
      abilityCircleAnalysis: '', scaleControlAnalysis: '', workBackground: '',
      productManageAnalysis: '', stabilityAnalysis: '', personalHoldAnalysis: ''
    })
  }

  // 净值数据
  navData.value = row.navList ? JSON.parse(JSON.stringify(row.navList)) : []
  navQuery.timeRange = null; navPage.page = 1; navPage.limit = 10

  // 份额数据
  Object.assign(shareData, {
    holdShares: row.holdShares || 0, availableShares: row.availableShares || 0,
    frozenShares: row.frozenShares || 0, positionCost: row.positionCost || 0,
    positionValue: row.positionValue || 0,
    positionProfit: row.positionProfit || 0,
    positionProfitRate: row.positionProfitRate || 0
  })

  // 交易数据
  tradeData.value = row.tradeList ? JSON.parse(JSON.stringify(row.tradeList)) : []
  tradeQuery.timeRange = null; tradePage.page = 1; tradePage.limit = 5

  // 分红数据
  dividendData.value = row.dividendList ? JSON.parse(JSON.stringify(row.dividendList)) : []
  dividendQuery.timeRange = null; dividendPage.page = 1; dividendPage.limit = 5

  // 风险指标
  riskData.value = row.riskList ? JSON.parse(JSON.stringify(row.riskList)) : []

  // 持仓数据
  holdingData.value = row.holdingList ? JSON.parse(JSON.stringify(row.holdingList)) : []
  holdingQuery.timeRange = null; holdingPage.page = 1; holdingPage.limit = 10
}

watch(() => props.visible, (val) => {
  if (val) initDialogData()
})

// ============ 保存 & 关闭 ============
const handleSave = () => {
  const savePayload = {
    ...JSON.parse(JSON.stringify(fundData)),
    managerAnalysis: JSON.parse(JSON.stringify(managerAnalysis)),
    navList: JSON.parse(JSON.stringify(navData.value)),
    holdShares: shareData.holdShares, availableShares: shareData.availableShares,
    frozenShares: shareData.frozenShares, positionCost: shareData.positionCost,
    positionValue: shareData.positionValue, positionProfit: shareData.positionProfit,
    positionProfitRate: shareData.positionProfitRate,
    tradeList: JSON.parse(JSON.stringify(tradeData.value)),
    dividendList: JSON.parse(JSON.stringify(dividendData.value)),
    riskList: JSON.parse(JSON.stringify(riskData.value)),
    holdingList: JSON.parse(JSON.stringify(holdingData.value))
  }
  emit('save', savePayload)
  ElMessage.success('保存成功')
}

const handleClose = () => {
  dialogVisible.value = false
}

// ============ 获取实时数据 ============
const fetchLoading = ref(false)

const handleFetchRealTimeData = async () => {
  fetchLoading.value = true
  try {
    // TODO: 调用API获取实时数据(最新净值、持仓等)
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('实时数据获取成功')
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
