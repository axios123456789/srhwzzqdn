<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>
        <el-icon><Goods /></el-icon>
        交易系统试验
      </h1>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="main-tabs">
      <!-- 交易规则标签页 -->
      <el-tab-pane label="交易规则" name="rule">
        <div class="rule-div">
          <div class="rule-header">
            <div class="rule-actions">
              <el-button type="success" size="small" @click="addRule">
                <el-icon><DocumentAdd /></el-icon>
                添加规则
              </el-button>
              <el-button type="danger" size="small" @click="deleteRuleAll" :disabled="ruleSelectedRows.length === 0">
                <el-icon><Delete /></el-icon>
                批量删除
              </el-button>
            </div>
          </div>
      <!-- 规则筛选条件 -->
      <div class="rule-search-div">
        <el-form label-width="80px" size="small" inline>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-form-item label="规则类型">
                <el-select v-model="ruleQueryDto.ruleType" style="width: 100%" clearable placeholder="请选择规则类型">
                  <el-option v-for="item in ruleTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="规则状态">
                <el-select v-model="ruleQueryDto.ruleStatus" style="width: 100%" clearable placeholder="请选择规则状态">
                  <el-option v-for="item in ruleStatusOptions" :key="item.value" :label="item.text" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12" style="text-align: right;">
              <el-form-item label-width="10px">
                <el-button type="primary" size="small" @click="searchRuleData">
                  <el-icon><Search /></el-icon>
                  搜索
                </el-button>
                <el-button size="small" @click="resetRuleData">
                  <el-icon><Refresh /></el-icon>
                  重置
                </el-button>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>
      <el-table
        :data="filteredRuleList"
        style="width: 100%"
        :height="ruleTableHeight"
        ref="ruleTable"
        @selection-change="handleRuleSelectionChange"
        border
        stripe
        size="small"
      >
        <el-table-column type="selection" width="40" align="center" />
        <el-table-column label="操作" align="center" fixed="left" width="160" #default="scope">
          <el-button type="primary" size="small" @click="editRule(scope.row)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button type="danger" size="small" @click="deleteRule(scope.row)">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </el-table-column>
        <el-table-column prop="ruleCode" label="规则编号" align="center" min-width="200">
          <template #default="scope">
            <el-link type="primary" :underline="false" @click="viewRuleDetail(scope.row)">{{ scope.row.ruleCode }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="ruleType" label="规则类型" align="center" min-width="100">
          <template #default="scope">
            {{ getDisplayText(scope.row.ruleType, ruleTypeOptions) }}
          </template>
        </el-table-column>
        <el-table-column prop="ruleContent" label="规则内容" align="center" min-width="160" show-overflow-tooltip />
        <el-table-column prop="ruleDetail" label="规则细节" align="center" min-width="160" show-overflow-tooltip />
        <el-table-column prop="ruleStatus" label="规则状态" align="center" min-width="80">
          <template #default="scope">
            <el-tag :type="scope.row.ruleStatus === 1 ? 'success' : 'info'" size="small">
              {{ getDisplayText(scope.row.ruleStatus, ruleStatusOptions) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="useCount" label="使用次数" align="center" min-width="80" />
        <el-table-column prop="violateCount" label="违反次数" align="center" min-width="80" />
        <el-table-column prop="complySuccessCount" label="遵守成功次数" align="center" min-width="110" />
        <el-table-column prop="violateSuccessCount" label="违反成功次数" align="center" min-width="110" />
        <el-table-column prop="violatePenalty" label="违反惩罚" align="center" min-width="120" show-overflow-tooltip />
        <el-table-column label="遵守成功率" align="center" min-width="100">
          <template #default="scope">
            <span :class="getRateClass(calcComplySuccessRate(scope.row))">
              {{ calcComplySuccessRate(scope.row) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="违反成功率" align="center" min-width="100">
          <template #default="scope">
            <span :class="getRateClass(calcViolateSuccessRate(scope.row))">
              {{ calcViolateSuccessRate(scope.row) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" align="center" min-width="60" />
      </el-table>
        </div>
      </el-tab-pane>

      <!-- 交易试验记录标签页 -->
      <el-tab-pane label="交易试验记录" name="trial">
        <div class="trial-div">
      <!-- 搜索表单区域 -->
      <div class="search-div">
        <div class="search-header">
          <span class="search-title">查询条件</span>
          <el-button
            type="text"
            size="small"
            @click="toggleSearchExpand"
            class="expand-btn"
          >
            <el-icon>
              <ArrowDown v-if="!searchExpanded" />
              <ArrowUp v-else />
            </el-icon>
            {{ searchExpanded ? '收起' : '展开' }}
          </el-button>
        </div>
        <el-form label-width="120px" size="small">
          <el-row>
            <el-col :span="6">
              <el-form-item label="交易对象">
                <el-input v-model="queryDto.targetName" style="width: 100%" clearable placeholder="请输入交易对象名称" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="交易类型">
                <el-select v-model="queryDto.tradeType" style="width: 100%" clearable placeholder="请选择交易类型" multiple>
                  <el-option v-for="item in tradeTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="计划时间">
                <el-date-picker
                  v-model="planTimeArea"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  style="width: 100%"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  :unlink-panels="true"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <transition name="el-zoom-in-top">
            <div v-show="searchExpanded">
              <el-row>
                <el-col :span="6">
                  <el-form-item label="计划类型">
                    <el-select v-model="queryDto.planType" style="width: 100%" clearable placeholder="请选择计划类型" multiple>
                      <el-option v-for="item in planTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="交易状态">
                    <el-select v-model="queryDto.tradeStatus" style="width: 100%" clearable placeholder="请选择交易状态" multiple>
                      <el-option v-for="item in tradeStatusOptions" :key="item.value" :label="item.text" :value="item.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="交易结果">
                    <el-select v-model="queryDto.tradeResult" style="width: 100%" clearable placeholder="请选择交易结果" multiple>
                      <el-option v-for="item in tradeResultOptions" :key="item.value" :label="item.text" :value="item.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="失败类型">
                    <el-select v-model="queryDto.tradeFailType" style="width: 100%" clearable placeholder="请选择失败类型" multiple>
                      <el-option v-for="item in tradeFailTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="6">
                  <el-form-item label="是否触发计划">
                    <el-select v-model="queryDto.isUsePlan" style="width: 100%" clearable placeholder="请选择">
                      <el-option v-for="item in isUsePlanOptions" :key="item.value" :label="item.text" :value="item.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </transition>
          <el-row>
            <el-col :span="24" style="text-align: right;">
              <el-form-item label-width="10px">
                <el-button type="primary" size="small" @click="searchData" class="beautified-search-btn">
                  <el-icon><Search /></el-icon>
                  搜索
                </el-button>
                <el-button size="small" @click="resetData" class="beautified-reset-btn">
                  <el-icon><Refresh /></el-icon>
                  重置
                </el-button>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>      <!-- 操作按钮区域 -->
      <div class="tools-div beautified-tools" style="text-align: right;">
        <el-button type="success" size="small" @click="addRecord">
          <el-icon><DocumentAdd /></el-icon>
          添加试验
        </el-button>
        <el-button type="danger" size="small" @click="deleteSelectAll">
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
        <el-button type="info" size="small" @click="showExportDialog">
          <el-icon><Download /></el-icon>
          一键导出
        </el-button>
      </div>

      <!-- 数据表格 -->
      <el-table
        :data="list"
        style="width: 100%"
        height="450"
        ref="multipleTable"
        @selection-change="handleSelectionChange"
        border
        stripe
      >
        <el-table-column type="selection" width="40" align="center" />
        <el-table-column label="操作" align="center" fixed="left" width="240" #default="scope">
          <el-button type="info" size="small" @click="viewTrialDetail(scope.row)">
            <el-icon><View /></el-icon>
            查看
          </el-button>
          <el-button type="primary" size="small" @click="editRecord(scope.row)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button type="danger" size="small" @click="deleteRecord(scope.row)">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </el-table-column>
        <el-table-column prop="targetName" label="交易对象" align="center" width="120" show-overflow-tooltip />
        <el-table-column prop="tradeType" label="交易类型" align="center" width="100">
          <template #default="scope">
            {{ getDisplayText(scope.row.tradeType, tradeTypeOptions) }}
          </template>
        </el-table-column>
        <el-table-column prop="planType" label="计划类型" align="center" width="100">
          <template #default="scope">
            {{ getDisplayText(scope.row.planType, planTypeOptions) }}
          </template>
        </el-table-column>
        <el-table-column prop="currentPrice" label="当前价" align="center" width="90">
          <template #default="scope">
            {{ scope.row.currentPrice != null ? Number(scope.row.currentPrice).toFixed(2) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="planPrice" label="计划价" align="center" width="90">
          <template #default="scope">
            {{ scope.row.planPrice != null ? Number(scope.row.planPrice).toFixed(2) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="actualPrice" label="成交价" align="center" width="90">
          <template #default="scope">
            <span style="color: #409EFF; font-weight: 600;">
              {{ scope.row.actualPrice != null ? Number(scope.row.actualPrice).toFixed(2) : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="tradeStatus" label="交易状态" align="center" width="100">
          <template #default="scope">
            <el-tag :type="getTradeStatusTagType(scope.row.tradeStatus)" size="small">
              {{ getDisplayText(scope.row.tradeStatus, tradeStatusOptions) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tradeResult" label="交易结果" align="center" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.tradeResult === 1 ? 'success' : 'danger'" size="small">
              {{ getDisplayText(scope.row.tradeResult, tradeResultOptions) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tradeFailType" label="失败类型" align="center" width="100">
          <template #default="scope">
            {{ getDisplayText(scope.row.tradeFailType, tradeFailTypeOptions) }}
          </template>
        </el-table-column>
        <el-table-column prop="isUsePlan" label="触发计划" align="center" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.isUsePlan === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.isUsePlan === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="planContent" label="计划内容" align="center" min-width="150" show-overflow-tooltip />
        <el-table-column prop="planStartTime" label="计划开始时间" align="center" width="160" />
        <el-table-column prop="resultReview" label="结果复盘" align="center" min-width="150" show-overflow-tooltip />
      </el-table>

      <!-- 分页组件 -->
      <el-pagination
        style="margin-top: 30px"
        v-model:current-page="pageParams.page"
        v-model:page-size="pageParams.limit"
        :page-sizes="[10, 20, 50, 100]"
        @size-change="fetchData"
        @current-change="fetchData"
        layout="total, sizes, prev, pager, next"
        :total="total"
      />
        </div>
      </el-tab-pane>

      <!-- 预测模拟标签页 -->
      <el-tab-pane label="预测模拟" name="prediction">
        <div class="prediction-div">
          <!-- 模拟账户总览 - 顶部横条 -->
          <div class="account-overview">
            <div class="account-overview-left">
              <div class="account-icon">
                <el-icon :size="28"><Wallet /></el-icon>
              </div>
              <div class="account-info">
                <div class="account-label">模拟账户总资产</div>
                <div class="account-total-amount">
                  ¥ {{ formatMoney(totalAssetAmount) }}
                </div>
              </div>
            </div>
            <div class="account-overview-right">
              <div class="account-stat-item">
                <div class="account-stat-label">可用资金</div>
                <div class="account-stat-value available">¥ {{ formatMoney(mainAccountAmount) }}</div>
              </div>
              <div class="account-stat-divider"></div>
              <div class="account-stat-item">
                <div class="account-stat-label">持仓市值</div>
                <div class="account-stat-value position">¥ {{ formatMoney(positionAmount) }}</div>
              </div>
              <div class="account-stat-divider"></div>
              <div class="account-stat-item">
                <div class="account-stat-label">持仓数量</div>
                <div class="account-stat-value count">{{ positionCount }} 只</div>
              </div>
              <div class="account-stat-divider"></div>
              <div class="account-stat-item">
                <el-button type="warning" size="small" @click="initAccount" plain>
                  <el-icon><Wallet /></el-icon>
                  初始化账户
                </el-button>
                <el-button type="success" size="small" @click="addLedger" plain>
                  <el-icon><DocumentAdd /></el-icon>
                  添加
                </el-button>
              </div>
            </div>
          </div>

          <!-- 持仓卡片区域 -->
          <div class="position-cards" v-if="positionList.length > 0">
            <div class="position-card" v-for="item in positionList" :key="item.id">
              <div class="position-card-header">
                <span class="position-stock-name">{{ item.assetName }}</span>
                <span class="position-stock-code">{{ item.assetCode }}</span>
                <el-tag size="small" type="info" style="margin-left: 6px;">{{ getDisplayText(item.assetType, simulateAssetTypeOptions) }}</el-tag>
                <div class="position-card-actions">
                  <el-button type="primary" link size="small" @click="editLedger(item)">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                  <el-button type="danger" link size="small" @click="deleteLedger(item)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
              <div class="position-card-body">
                <div class="position-card-col">
                  <div class="position-card-label">持仓数量</div>
                  <div class="position-card-value">{{ item.assetQuantity || 0 }}<span class="position-card-unit">股</span></div>
                </div>
                <div class="position-card-col">
                  <div class="position-card-label">持仓市值</div>
                  <div class="position-card-value highlight">¥ {{ formatMoney(item.assetAmount) }}</div>
                </div>
              </div>
            </div>
          </div>
          <div class="position-empty" v-else>
            <el-empty description="暂无持仓，请先初始化账户" :image-size="60" />
          </div>

          <!-- 预测模拟管理 - 搜索区域 -->
          <div class="search-div" style="margin-top: 15px;">
            <div class="search-header">
              <span class="search-title">预测记录查询</span>
            </div>
            <el-form label-width="100px" size="small">
              <el-row :gutter="20">
                <el-col :span="6">
                  <el-form-item label="股票名称">
                    <el-input v-model="predQueryDto.stockName" style="width: 100%" clearable placeholder="请输入股票名称" />
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="股票代码">
                    <el-input v-model="predQueryDto.stockCode" style="width: 100%" clearable placeholder="请输入股票代码" />
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="涨跌预测">
                    <el-select v-model="predQueryDto.riseFallPrediction" style="width: 100%" clearable placeholder="请选择" multiple>
                      <el-option v-for="item in riseFallOptions" :key="item.value" :label="item.text" :value="item.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="依据类型">
                    <el-select v-model="predQueryDto.basisType" style="width: 100%" clearable placeholder="请选择" multiple>
                      <el-option v-for="item in basisTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="预测时间">
                    <el-date-picker
                      v-model="predTimeArea"
                      type="datetimerange"
                      range-separator="至"
                      start-placeholder="开始时间"
                      end-placeholder="结束时间"
                      style="width: 100%"
                      value-format="YYYY-MM-DD HH:mm:ss"
                      :unlink-panels="true"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="预测结果">
                    <el-select v-model="predQueryDto.predictionResult" style="width: 100%" clearable placeholder="请选择" multiple>
                      <el-option v-for="item in predictionResultOptions" :key="item.value" :label="item.text" :value="item.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="交易状态">
                    <el-select v-model="predQueryDto.tradeStatus" style="width: 100%" clearable placeholder="请选择" multiple>
                      <el-option v-for="item in simulateTradeStatusOptions" :key="item.value" :label="item.text" :value="item.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="24" style="text-align: right;">
                  <el-form-item label-width="10px">
                    <el-button type="primary" size="small" @click="searchPredData">
                      <el-icon><Search /></el-icon>
                      搜索
                    </el-button>
                    <el-button size="small" @click="resetPredData">
                      <el-icon><Refresh /></el-icon>
                      重置
                    </el-button>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </div>
          <!-- 操作按钮区域 -->
          <div class="tools-div beautified-tools" style="text-align: right;">
            <el-button type="success" size="small" @click="addPrediction">
              <el-icon><DocumentAdd /></el-icon>
              添加预测
            </el-button>
            <el-button type="danger" size="small" @click="deletePredAll" :disabled="predSelectedRows.length === 0">
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
          </div>
          <!-- 预测模拟数据表格 -->
          <el-table
            :data="predList"
            style="width: 100%"
            height="300"
            ref="predTable"
            @selection-change="handlePredSelectionChange"
            border
            stripe
            size="small"
          >
            <el-table-column type="selection" width="40" align="center" />
            <el-table-column label="操作" align="center" fixed="left" width="160" #default="scope">
              <el-button type="primary" size="small" @click="editPrediction(scope.row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button type="danger" size="small" @click="deletePrediction(scope.row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </el-table-column>
            <el-table-column prop="stockName" label="股票名称" align="center" width="100" show-overflow-tooltip />
            <el-table-column prop="stockCode" label="股票代码" align="center" width="100" />
            <el-table-column prop="riseFallPrediction" label="涨跌预测" align="center" width="80">
              <template #default="scope">
                <el-tag :type="scope.row.riseFallPrediction === 1 ? 'danger' : 'success'" size="small">
                  {{ getDisplayText(scope.row.riseFallPrediction, riseFallOptions) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="predictionTime" label="预测时间" align="center" width="160" />
            <el-table-column prop="basisType" label="依据类型" align="center" width="100">
              <template #default="scope">
                {{ getDisplayText(scope.row.basisType, basisTypeOptions) }}
              </template>
            </el-table-column>
            <el-table-column prop="predictionResult" label="预测结果" align="center" width="80">
              <template #default="scope">
                <el-tag :type="scope.row.predictionResult === 1 ? 'success' : 'danger'" size="small">
                  {{ getDisplayText(scope.row.predictionResult, predictionResultOptions) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="predictionSituation" label="预测情况" align="center" width="100">
              <template #default="scope">
                {{ getDisplayText(scope.row.predictionSituation, predictionSituationOptions) }}
              </template>
            </el-table-column>
            <el-table-column prop="simulateOperation" label="模拟操作" align="center" width="90">
              <template #default="scope">
                {{ getDisplayText(scope.row.simulateOperation, simulateOperationOptions) }}
              </template>
            </el-table-column>
            <el-table-column prop="tradeShare" label="交易份额" align="center" width="80" />
            <el-table-column prop="currentPrice" label="当前股价" align="center" width="90">
              <template #default="scope">
                {{ scope.row.currentPrice != null ? Number(scope.row.currentPrice).toFixed(2) : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="handlingFee" label="手续费" align="center" width="80">
              <template #default="scope">
                {{ scope.row.handlingFee != null ? Number(scope.row.handlingFee).toFixed(2) : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="tradeStatus" label="交易状态" align="center" width="80">
              <template #default="scope">
                <el-tag :type="scope.row.tradeStatus === 1 ? 'success' : 'danger'" size="small">
                  {{ getDisplayText(scope.row.tradeStatus, simulateTradeStatusOptions) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="predictionContent" label="预测内容" align="center" min-width="120" show-overflow-tooltip />
            <el-table-column prop="predictionBasis" label="预测依据" align="center" min-width="120" show-overflow-tooltip />
          </el-table>
          <!-- 分页组件 -->
          <el-pagination
            style="margin-top: 15px"
            v-model:current-page="predPageParams.page"
            v-model:page-size="predPageParams.limit"
            :page-sizes="[10, 20, 50, 100]"
            @size-change="fetchPredData"
            @current-change="fetchPredData"
            layout="total, sizes, prev, pager, next"
            :total="predTotal"
          />
        </div>
      </el-tab-pane>

      <!-- 统计报表标签页 -->
      <el-tab-pane label="统计报表" name="report">
        <div class="report-div">
          <!-- 报表筛选条件 -->
          <div class="search-div">
            <div class="search-header">
              <span class="search-title">统计条件</span>
            </div>
            <el-form label-width="100px" size="small">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="统计时间">
                    <el-date-picker
                      v-model="reportTimeArea"
                      type="datetimerange"
                      range-separator="至"
                      start-placeholder="开始时间"
                      end-placeholder="结束时间"
                      style="width: 100%"
                      value-format="YYYY-MM-DD HH:mm:ss"
                      :unlink-panels="true"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="依据类型">
                    <el-select v-model="reportDto.basisType" style="width: 100%" clearable placeholder="请选择" multiple>
                      <el-option v-for="item in basisTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="股票代码">
                    <el-input v-model="reportDto.stockCode" style="width: 100%" clearable placeholder="请输入股票代码" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="24" style="text-align: right;">
                  <el-form-item label-width="10px">
                    <el-button type="primary" size="small" @click="fetchReportData">
                      <el-icon><Search /></el-icon>
                      查询
                    </el-button>
                    <el-button size="small" @click="resetReportData">
                      <el-icon><Refresh /></el-icon>
                      重置
                    </el-button>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </div>

          <!-- 概览卡片 -->
          <el-row :gutter="16" style="margin-bottom: 20px;">
            <el-col :span="6">
              <div class="stat-card">
                <div class="stat-value primary">{{ reportData.totalCount || 0 }}</div>
                <div class="stat-label">总预测次数</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card">
                <div class="stat-value success">{{ reportData.successCount || 0 }}</div>
                <div class="stat-label">预测成功次数</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card">
                <div class="stat-value danger">{{ reportData.failCount || 0 }}</div>
                <div class="stat-label">预测失败次数</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card rate-card">
                <div class="stat-value" :class="getRateClass((reportData.accuracyRate || 0) + '%')">{{ reportData.accuracyRate || '0.00' }}%</div>
                <div class="stat-label">预测准确率</div>
              </div>
            </el-col>
          </el-row>

          <!-- 依据类型准确率 + 预测情况分布 -->
          <el-row :gutter="16" style="margin-bottom: 20px;">
            <el-col :span="12">
              <div class="report-section">
                <div class="detail-section-title">依据类型准确率</div>
                <el-table :data="reportData.basisTypeStats || []" border stripe size="small">
                  <el-table-column prop="basisType" label="依据类型" align="center">
                    <template #default="scope">
                      {{ getDisplayText(scope.row.basisType, basisTypeOptions) }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="count" label="总次数" align="center" />
                  <el-table-column prop="successCount" label="成功次数" align="center" />
                  <el-table-column prop="successRate" label="成功率" align="center">
                    <template #default="scope">
                      <span :class="getRateClass(scope.row.successRate + '%')">{{ scope.row.successRate }}%</span>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="report-section">
                <div class="detail-section-title">预测情况分布</div>
                <el-table :data="reportData.situationStats || []" border stripe size="small">
                  <el-table-column prop="predictionSituation" label="预测情况" align="center">
                    <template #default="scope">
                      {{ getDisplayText(scope.row.predictionSituation, predictionSituationOptions) }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="count" label="次数" align="center" />
                  <el-table-column prop="percentage" label="占比" align="center">
                    <template #default="scope">
                      <el-progress :percentage="Number(scope.row.percentage)" :stroke-width="14" :text-inside="true" />
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-col>
          </el-row>

          <!-- 月度趋势 + 模拟操作统计 -->
          <el-row :gutter="16" style="margin-bottom: 20px;">
            <el-col :span="12">
              <div class="report-section">
                <div class="detail-section-title">月度预测准确率趋势</div>
                <el-table :data="reportData.monthlyTrends || []" border stripe size="small">
                  <el-table-column prop="month" label="月份" align="center" />
                  <el-table-column prop="totalCount" label="总次数" align="center" />
                  <el-table-column prop="successCount" label="成功次数" align="center" />
                  <el-table-column prop="successRate" label="成功率" align="center">
                    <template #default="scope">
                      <span :class="getRateClass(scope.row.successRate + '%')">{{ scope.row.successRate }}%</span>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="report-section">
                <div class="detail-section-title">模拟操作盈亏统计</div>
                <el-row :gutter="16" v-if="reportData.simulateTradeStat">
                  <el-col :span="8">
                    <div class="stat-card">
                      <div class="stat-value primary">{{ reportData.simulateTradeStat.totalBuyCount || 0 }}</div>
                      <div class="stat-label">买入次数</div>
                    </div>
                  </el-col>
                  <el-col :span="8">
                    <div class="stat-card">
                      <div class="stat-value success">{{ reportData.simulateTradeStat.totalSellCount || 0 }}</div>
                      <div class="stat-label">卖出次数</div>
                    </div>
                  </el-col>
                  <el-col :span="8">
                    <div class="stat-card">
                      <div class="stat-value warning">{{ reportData.simulateTradeStat.totalHandlingFee || '0.00' }}</div>
                      <div class="stat-label">总手续费</div>
                    </div>
                  </el-col>
                </el-row>
                <el-row :gutter="16" style="margin-top: 16px;" v-if="reportData.simulateTradeStat">
                  <el-col :span="8">
                    <div class="stat-card">
                      <div class="stat-value primary">{{ reportData.simulateTradeStat.totalBuyAmount || '0.00' }}</div>
                      <div class="stat-label">买入总额</div>
                    </div>
                  </el-col>
                  <el-col :span="8">
                    <div class="stat-card">
                      <div class="stat-value success">{{ reportData.simulateTradeStat.totalSellAmount || '0.00' }}</div>
                      <div class="stat-label">卖出总额</div>
                    </div>
                  </el-col>
                  <el-col :span="8">
                    <div class="stat-card">
                      <div class="stat-value" :class="Number(reportData.simulateTradeStat.totalProfitLoss || 0) >= 0 ? 'success' : 'danger'">
                        {{ reportData.simulateTradeStat.totalProfitLoss || '0.00' }}
                      </div>
                      <div class="stat-label">模拟盈亏</div>
                    </div>
                  </el-col>
                </el-row>
              </div>
            </el-col>
          </el-row>

          <!-- 股票预测统计 -->
          <div class="report-section">
            <div class="detail-section-title">股票预测统计</div>
            <el-table :data="reportData.stockStats || []" border stripe size="small">
              <el-table-column prop="stockName" label="股票名称" align="center" min-width="120" show-overflow-tooltip />
              <el-table-column prop="stockCode" label="股票代码" align="center" width="120" />
              <el-table-column prop="predictCount" label="预测次数" align="center" width="100" />
              <el-table-column prop="successCount" label="成功次数" align="center" width="100" />
              <el-table-column prop="successRate" label="成功率" align="center" width="100">
                <template #default="scope">
                  <span :class="getRateClass(scope.row.successRate + '%')">{{ scope.row.successRate }}%</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 交易规则 详情查看对话框 -->
    <el-dialog
      v-model="ruleDetailVisible"
      title="规则详情"
      width="55%"
      class="custom-dialog enhanced-dialog"
      :close-on-click-modal="true"
    >
      <div class="rule-detail-container" v-if="ruleDetailData">
        <!-- 基本信息区 -->
        <div class="detail-section">
          <div class="detail-section-title">基本信息</div>
          <el-descriptions :column="2" border size="default">
            <el-descriptions-item label="规则编号" :span="1">
              <span class="detail-value highlight">{{ ruleDetailData.ruleCode }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="规则类型" :span="1">
              <el-tag type="warning" size="small">{{ getDisplayText(ruleDetailData.ruleType, ruleTypeOptions) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="规则状态" :span="1">
              <el-tag :type="ruleDetailData.ruleStatus === 1 ? 'success' : 'info'" size="small">
                {{ getDisplayText(ruleDetailData.ruleStatus, ruleStatusOptions) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="排序" :span="1">
              <span class="detail-value">{{ ruleDetailData.sortOrder }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间" :span="1">
              <span class="detail-value">{{ ruleDetailData.createTime }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="更新时间" :span="1">
              <span class="detail-value">{{ ruleDetailData.updateTime || '-' }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <!-- 规则内容区 -->
        <div class="detail-section">
          <div class="detail-section-title">规则内容</div>
          <el-descriptions :column="1" border size="default">
            <el-descriptions-item label="规则内容">
              <div class="detail-text-block">{{ ruleDetailData.ruleContent || '-' }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="规则细节">
              <div class="detail-text-block">{{ ruleDetailData.ruleDetail || '-' }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="违反惩罚">
              <div class="detail-text-block penalty-text">{{ ruleDetailData.violatePenalty || '-' }}</div>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <!-- 统计数据区 -->
        <div class="detail-section">
          <div class="detail-section-title">统计数据</div>
          <el-row :gutter="16">
            <el-col :span="6">
              <div class="stat-card">
                <div class="stat-value primary">{{ ruleDetailData.useCount || 0 }}</div>
                <div class="stat-label">使用次数</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card">
                <div class="stat-value danger">{{ ruleDetailData.violateCount || 0 }}</div>
                <div class="stat-label">违反次数</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card">
                <div class="stat-value success">{{ ruleDetailData.complySuccessCount || 0 }}</div>
                <div class="stat-label">遵守成功次数</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card">
                <div class="stat-value warning">{{ ruleDetailData.violateSuccessCount || 0 }}</div>
                <div class="stat-label">违反成功次数</div>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="16" style="margin-top: 16px;">
            <el-col :span="12">
              <div class="stat-card rate-card">
                <div class="stat-value" :class="getRateClass(calcComplySuccessRate(ruleDetailData))">{{ calcComplySuccessRate(ruleDetailData) }}</div>
                <div class="stat-label">遵守规则成功率</div>
                <div class="rate-formula">遵守成功次数 / 使用次数</div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="stat-card rate-card">
                <div class="stat-value" :class="getRateClass(calcViolateSuccessRate(ruleDetailData))">{{ calcViolateSuccessRate(ruleDetailData) }}</div>
                <div class="stat-label">违反规则成功率</div>
                <div class="rate-formula">违反成功次数 / 违反次数</div>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="ruleDetailVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
    <!-- 交易规则 添加/修改对话框 -->
    <el-dialog
      v-model="ruleDialogVisible"
      :title="ruleDialogTitle"
      width="50%"
      class="custom-dialog enhanced-dialog"
      :close-on-click-modal="false"
    >
      <el-form :model="ruleFormData" label-width="140px" :rules="ruleFormRules" ref="ruleFormRef">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="规则编号" prop="ruleCode">
              <el-input v-model="ruleFormData.ruleCode" placeholder="自动生成" :disabled="true" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规则类型" prop="ruleType">
              <el-select v-model="ruleFormData.ruleType" style="width: 100%" placeholder="请选择规则类型">
                <el-option v-for="item in ruleTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="规则状态" prop="ruleStatus">
              <el-select v-model="ruleFormData.ruleStatus" style="width: 100%" placeholder="请选择规则状态">
                <el-option v-for="item in ruleStatusOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="ruleFormData.sortOrder" :min="0" style="width: 100%" placeholder="排序" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="规则内容" prop="ruleContent">
              <el-input v-model="ruleFormData.ruleContent" type="textarea" :rows="2" placeholder="请输入规则内容" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="规则细节" prop="ruleDetail">
              <el-input v-model="ruleFormData.ruleDetail" type="textarea" :rows="2" placeholder="请输入规则细节" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="违反惩罚" prop="violatePenalty">
              <el-input v-model="ruleFormData.violatePenalty" type="textarea" :rows="2" placeholder="请输入违反规则惩罚" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="使用次数" prop="useCount">
              <el-input-number v-model="ruleFormData.useCount" :min="0" style="width: 100%" :disabled="!ruleFormData.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="违反次数" prop="violateCount">
              <el-input-number v-model="ruleFormData.violateCount" :min="0" style="width: 100%" :disabled="!ruleFormData.id" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="遵守成功次数" prop="complySuccessCount">
              <el-input-number v-model="ruleFormData.complySuccessCount" :min="0" style="width: 100%" :disabled="!ruleFormData.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="违反成功次数" prop="violateSuccessCount">
              <el-input-number v-model="ruleFormData.violateSuccessCount" :min="0" style="width: 100%" :disabled="!ruleFormData.id" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="ruleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRule">提交</el-button>
        </span>
      </template>
    </el-dialog>
    <!-- 交易试验 添加/修改对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="60%"
      class="custom-dialog enhanced-dialog"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" label-width="120px" :rules="formRules" ref="formRef">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="交易对象" prop="targetName">
              <el-input v-model="formData.targetName" placeholder="请输入交易对象名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="交易类型" prop="tradeType">
              <el-select v-model="formData.tradeType" style="width: 100%" placeholder="请选择交易类型">
                <el-option v-for="item in tradeTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划类型" prop="planType">
              <el-select v-model="formData.planType" style="width: 100%" placeholder="请选择计划类型">
                <el-option v-for="item in planTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="交易状态" prop="tradeStatus">
              <el-select v-model="formData.tradeStatus" style="width: 100%" placeholder="请选择交易状态">
                <el-option v-for="item in tradeStatusOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划开始时间" prop="planStartTime">
              <el-date-picker v-model="formData.planStartTime" type="datetime" placeholder="请选择计划开始时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划结束时间" prop="planEndTime">
              <el-date-picker v-model="formData.planEndTime" type="datetime" placeholder="请选择计划结束时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="当前价" prop="currentPrice">
              <el-input-number v-model="formData.currentPrice" :precision="2" style="width: 100%" placeholder="当前价" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="计划价" prop="planPrice">
              <el-input-number v-model="formData.planPrice" :precision="2" style="width: 100%" placeholder="计划交易价" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="开盘价" prop="openPrice">
              <el-input-number v-model="formData.openPrice" :precision="2" style="width: 100%" placeholder="开盘价" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="成交价" prop="actualPrice">
              <el-input-number v-model="formData.actualPrice" :precision="2" style="width: 100%" placeholder="实际成交价" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="收盘价" prop="closePrice">
              <el-input-number v-model="formData.closePrice" :precision="2" style="width: 100%" placeholder="收盘价" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否触发计划" prop="isUsePlan">
              <el-select v-model="formData.isUsePlan" style="width: 100%" placeholder="请选择">
                <el-option v-for="item in isUsePlanOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="交易结果" prop="tradeResult">
              <el-select v-model="formData.tradeResult" style="width: 100%" placeholder="请选择交易结果">
                <el-option v-for="item in tradeResultOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="失败类型" prop="tradeFailType">
              <el-select v-model="formData.tradeFailType" style="width: 100%" clearable placeholder="请选择失败类型">
                <el-option v-for="item in tradeFailTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="遵守规则" prop="complyRuleIds">
              <el-select v-model="formData.complyRuleIds" style="width: 100%" multiple clearable placeholder="请选择遵守规则">
                <el-option v-for="item in ruleList" :key="item.id" :label="item.ruleCode + ' - ' + getDisplayText(item.ruleType, ruleTypeOptions) + ' - ' + item.ruleContent" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="违反规则" prop="violateRuleIds">
              <el-select v-model="formData.violateRuleIds" style="width: 100%" multiple clearable placeholder="请选择违反规则">
                <el-option v-for="item in ruleList" :key="item.id" :label="item.ruleCode + ' - ' + getDisplayText(item.ruleType, ruleTypeOptions) + ' - ' + item.ruleContent" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="计划内容" prop="planContent">
              <el-input v-model="formData.planContent" type="textarea" :rows="2" placeholder="请输入交易计划内容" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="结果复盘" prop="resultReview">
              <el-input v-model="formData.resultReview" type="textarea" :rows="2" placeholder="请输入结果复盘" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submit">提交</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 交易试验记录 详情查看对话框 -->
    <el-dialog
      v-model="trialDetailVisible"
      title="试验记录详情"
      width="60%"
      class="custom-dialog enhanced-dialog"
      :close-on-click-modal="true"
    >
      <div class="trial-detail-container" v-if="trialDetailData">
        <!-- 交易信息区 -->
        <div class="detail-section">
          <div class="detail-section-title">交易信息</div>
          <el-descriptions :column="2" border size="default">
            <el-descriptions-item label="交易对象" :span="1">
              <span class="detail-value highlight">{{ trialDetailData.targetName || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="交易类型" :span="1">
              <el-tag type="warning" size="small">{{ getDisplayText(trialDetailData.tradeType, tradeTypeOptions) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="计划类型" :span="1">
              <el-tag size="small">{{ getDisplayText(trialDetailData.planType, planTypeOptions) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="交易状态" :span="1">
              <el-tag :type="getTradeStatusTagType(trialDetailData.tradeStatus)" size="small">
                {{ getDisplayText(trialDetailData.tradeStatus, tradeStatusOptions) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="交易结果" :span="1">
              <el-tag :type="trialDetailData.tradeResult === 1 ? 'success' : 'danger'" size="small">
                {{ getDisplayText(trialDetailData.tradeResult, tradeResultOptions) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="失败类型" :span="1">
              <span class="detail-value">{{ getDisplayText(trialDetailData.tradeFailType, tradeFailTypeOptions) || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="是否触发计划" :span="1">
              <el-tag :type="trialDetailData.isUsePlan === 1 ? 'success' : 'info'" size="small">
                {{ trialDetailData.isUsePlan === 1 ? '是' : '否' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间" :span="1">
              <span class="detail-value">{{ trialDetailData.createTime || '-' }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <!-- 价格信息区 -->
        <div class="detail-section">
          <div class="detail-section-title">价格信息</div>
          <el-row :gutter="16">
            <el-col :span="4">
              <div class="stat-card">
                <div class="stat-value primary">{{ trialDetailData.currentPrice != null ? Number(trialDetailData.currentPrice).toFixed(2) : '-' }}</div>
                <div class="stat-label">当前价</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-card">
                <div class="stat-value primary">{{ trialDetailData.planPrice != null ? Number(trialDetailData.planPrice).toFixed(2) : '-' }}</div>
                <div class="stat-label">计划价</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-card">
                <div class="stat-value success">{{ trialDetailData.openPrice != null ? Number(trialDetailData.openPrice).toFixed(2) : '-' }}</div>
                <div class="stat-label">开盘价</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-card">
                <div class="stat-value warning">{{ trialDetailData.actualPrice != null ? Number(trialDetailData.actualPrice).toFixed(2) : '-' }}</div>
                <div class="stat-label">成交价</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-card">
                <div class="stat-value danger">{{ trialDetailData.closePrice != null ? Number(trialDetailData.closePrice).toFixed(2) : '-' }}</div>
                <div class="stat-label">收盘价</div>
              </div>
            </el-col>
          </el-row>
        </div>
        <!-- 计划信息区 -->
        <div class="detail-section">
          <div class="detail-section-title">计划信息</div>
          <el-descriptions :column="2" border size="default">
            <el-descriptions-item label="计划开始时间" :span="1">
              <span class="detail-value">{{ trialDetailData.planStartTime || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="计划结束时间" :span="1">
              <span class="detail-value">{{ trialDetailData.planEndTime || '-' }}</span>
            </el-descriptions-item>
          </el-descriptions>
          <el-descriptions :column="1" border size="default" style="margin-top: 0;">
            <el-descriptions-item label="计划内容">
              <div class="detail-text-block">{{ trialDetailData.planContent || '-' }}</div>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <!-- 规则关联区 -->
        <div class="detail-section">
          <div class="detail-section-title">规则关联</div>
          <el-descriptions :column="2" border size="default">
            <el-descriptions-item label="遵守规则">
              <div class="detail-text-block">{{ getRuleNames(trialDetailData.complyRuleIds) }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="违反规则">
              <div class="detail-text-block penalty-text">{{ getRuleNames(trialDetailData.violateRuleIds) }}</div>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <!-- 结果复盘区 -->
        <div class="detail-section">
          <div class="detail-section-title">结果复盘</div>
          <el-descriptions :column="1" border size="default">
            <el-descriptions-item label="结果复盘">
              <div class="detail-text-block">{{ trialDetailData.resultReview || '-' }}</div>
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="trialDetailVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 预测模拟 添加/修改对话框 -->
    <el-dialog
      v-model="predDialogVisible"
      :title="predDialogTitle"
      width="65%"
      class="custom-dialog enhanced-dialog"
      :close-on-click-modal="false"
    >
      <el-steps :active="predFormStep" align-center style="margin-bottom: 20px;">
        <el-step title="预测信息" />
        <el-step title="结果与模拟" />
      </el-steps>
      <el-form :model="predFormData" label-width="120px" :rules="predFormRules" ref="predFormRef">
        <!-- 步骤1：预测信息 -->
        <div v-show="predFormStep === 0">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="股票名称" prop="stockName">
                <el-input v-model="predFormData.stockName" placeholder="请输入股票名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="股票代码" prop="stockCode">
                <el-input v-model="predFormData.stockCode" placeholder="请输入股票代码" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="涨跌预测" prop="riseFallPrediction">
                <el-select v-model="predFormData.riseFallPrediction" style="width: 100%" placeholder="请选择涨跌预测">
                  <el-option v-for="item in riseFallOptions" :key="item.value" :label="item.text" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="预测时间" prop="predictionTime">
                <el-date-picker v-model="predFormData.predictionTime" type="datetime" placeholder="请选择预测时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="依据类型" prop="basisType">
                <el-select v-model="predFormData.basisType" style="width: 100%" placeholder="请选择依据类型">
                  <el-option v-for="item in basisTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="预测内容" prop="predictionContent">
                <el-input v-model="predFormData.predictionContent" type="textarea" :rows="2" placeholder="请输入预测内容" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="预测依据" prop="predictionBasis">
                <el-input v-model="predFormData.predictionBasis" type="textarea" :rows="2" placeholder="请输入预测依据" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        <!-- 步骤2：结果与模拟 -->
        <div v-show="predFormStep === 1">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="涨跌结果" prop="riseFallResult">
                <el-select v-model="predFormData.riseFallResult" style="width: 100%" clearable placeholder="请选择涨跌结果">
                  <el-option v-for="item in riseFallOptions" :key="item.value" :label="item.text" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="预测情况" prop="predictionSituation">
                <el-select v-model="predFormData.predictionSituation" style="width: 100%" clearable placeholder="请选择预测情况">
                  <el-option v-for="item in predictionSituationOptions" :key="item.value" :label="item.text" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="预测结果" prop="predictionResult">
                <el-select v-model="predFormData.predictionResult" style="width: 100%" clearable placeholder="请选择预测结果">
                  <el-option v-for="item in predictionResultOptions" :key="item.value" :label="item.text" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="模拟操作" prop="simulateOperation">
                <el-select v-model="predFormData.simulateOperation" style="width: 100%" clearable placeholder="请选择模拟操作">
                  <el-option v-for="item in simulateOperationOptions" :key="item.value" :label="item.text" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="交易份额" prop="tradeShare">
                <el-input-number v-model="predFormData.tradeShare" :min="0" style="width: 100%" placeholder="交易份额" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="当前股价" prop="currentPrice">
                <el-input-number v-model="predFormData.currentPrice" :precision="2" :min="0" style="width: 100%" placeholder="当前股价" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="手续费" prop="handlingFee">
                <el-input-number v-model="predFormData.handlingFee" :precision="2" :min="0" style="width: 100%" placeholder="手续费" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="交易状态" prop="tradeStatus">
                <el-select v-model="predFormData.tradeStatus" style="width: 100%" clearable placeholder="请选择交易状态">
                  <el-option v-for="item in simulateTradeStatusOptions" :key="item.value" :label="item.text" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="实际内容" prop="actualContent">
                <el-input v-model="predFormData.actualContent" type="textarea" :rows="2" placeholder="请输入实际内容" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="结果分析" prop="resultAnalysis">
                <el-input v-model="predFormData.resultAnalysis" type="textarea" :rows="2" placeholder="请输入结果分析" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button v-if="predFormStep > 0" @click="predFormStep--">上一步</el-button>
          <el-button v-if="predFormStep < 1" type="primary" @click="predFormStep++">下一步</el-button>
          <el-button @click="predDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPrediction">提交</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 模拟台账 添加/修改对话框 -->
    <el-dialog
      v-model="ledgerDialogVisible"
      :title="ledgerDialogTitle"
      width="50%"
      class="custom-dialog enhanced-dialog"
      :close-on-click-modal="false"
    >
      <el-form :model="ledgerFormData" label-width="120px" :rules="ledgerFormRules" ref="ledgerFormRef">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="资产名称" prop="assetName">
              <el-input v-model="ledgerFormData.assetName" placeholder="请输入资产名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资产编号" prop="assetCode">
              <el-input v-model="ledgerFormData.assetCode" placeholder="请输入资产编号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="资产类型" prop="assetType">
              <el-select v-model="ledgerFormData.assetType" style="width: 100%" placeholder="请选择资产类型">
                <el-option v-for="item in simulateAssetTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资产金额" prop="assetAmount">
              <el-input-number v-model="ledgerFormData.assetAmount" :precision="2" style="width: 100%" placeholder="资产金额" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="资产数量" prop="assetQuantity">
              <el-input-number v-model="ledgerFormData.assetQuantity" :min="0" style="width: 100%" placeholder="资产数量(股)" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="ledgerDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitLedger">提交</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 导出对话框 -->
    <ExportDialog
        v-model="exportDialogVisible"
        v-model:export-scope="exportScope"
        v-model:export-file-name="exportFileName"
        v-model:selected-columns="selectedColumns"
        :available-columns="exportColumns"
        :export-loading="exportLoading"
        :current-count="list.length"
        :total-count="total"
        @confirm="handleExport"
        @closed="resetExport"
    />
  </div>
</template><script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { GetKeyAndValueByType } from "@/api/sysDict"
import { GetTransactionSystemTrialByConditionAndPage, SaveTransactionSystemTrial, DeleteTransactionSystemTrialById, DeleteAllTransactionSystemTrialByIds, GetTransactionRuleList, SaveTransactionRule, DeleteTransactionRuleById, DeleteAllTransactionRuleByIds } from "@/api/trialExecutionArea/transactionSystemTrial"
import { GetPredictionByConditionAndPage, SavePrediction, DeletePredictionById, DeleteAllPredictionByIds, GetSimulateLedgerList, SaveSimulateLedger, DeleteSimulateLedgerById, GetPredictionReport } from "@/api/trialExecutionArea/predictionSimulate"
import { getDisplayText } from "@/utils/common"
import { useExport } from "@/components/Export/hooks/useExport"
import ExportDialog from '@/components/Export/ExportDialog.vue'

// ==================== 标签页 ====================
const activeTab = ref('rule')

// ==================== 规则表格动态高度 ====================
const ruleTableHeight = ref(320)

const calcRuleTableHeight = () => {
  nextTick(() => {
    const occupiedHeight = 68 + 44 + 50 + 56 + 30 + 10
    const availableHeight = window.innerHeight - occupiedHeight
    ruleTableHeight.value = Math.max(availableHeight, 200)
  })
}

const handleResize = () => {
  calcRuleTableHeight()
}

window.addEventListener('resize', handleResize)

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})

//--------------------钩子函数-------------------------
onMounted(() => {
  //0.计算规则表格高度
  calcRuleTableHeight()
  //1.加载数据字典
  getTradeTypeItem()
  getPlanTypeItem()
  getTradeStatusItem()
  getTradeResultItem()
  getTradeFailTypeItem()
  getIsUsePlanItem()
  getRuleStatusItem()
  getRuleTypeItem()

  //3.加载预测模拟数据字典
  getRiseFallItem()
  getBasisTypeItem()
  getPredictionSituationItem()
  getPredictionResultItem()
  getSimulateOperationItem()
  getSimulateTradeStatusItem()
  getSimulateAssetTypeItem()

  //2.调用查询数据接口
  fetchData()
  fetchRuleData()
  fetchLedgerData()
  fetchPredData()
  fetchReportData()
});

// ==================== 数据字典 ====================
// 交易类型选项
const tradeTypeOptions = ref([])
const getTradeTypeItem = async () => {
  const result = await GetKeyAndValueByType("t_trial_transaction_type")
  tradeTypeOptions.value = result.data || []
}

// 计划类型选项
const planTypeOptions = ref([])
const getPlanTypeItem = async () => {
  const result = await GetKeyAndValueByType("t_trial_plan_type")
  planTypeOptions.value = result.data || []
}

// 交易状态选项
const tradeStatusOptions = ref([])
const getTradeStatusItem = async () => {
  const result = await GetKeyAndValueByType("t_trial_transaction_status")
  tradeStatusOptions.value = result.data || []
}

// 交易结果选项
const tradeResultOptions = ref([])
const getTradeResultItem = async () => {
  const result = await GetKeyAndValueByType("t_trial_transaction_result")
  const data = result.data || []
  if (!data.find(item => item.value === 0)) {
    data.push({ value: 0, text: '失败' })
  }
  tradeResultOptions.value = data
}

// 交易失败类型选项
const tradeFailTypeOptions = ref([])
const getTradeFailTypeItem = async () => {
  const result = await GetKeyAndValueByType("t_trade_fail_type")
  tradeFailTypeOptions.value = result.data || []
}

// 是否触发计划选项（前端写死，不使用数据字典）
const isUsePlanOptions = ref([
  { value: 1, text: '是' },
  { value: 0, text: '否' }
])
const getIsUsePlanItem = () => {
  // 前端写死，无需请求字典
}

// 规则状态选项
const ruleStatusOptions = ref([])
const getRuleStatusItem = async () => {
  const result = await GetKeyAndValueByType("t_trial_rule_status")
  const data = result.data || []
  if (!data.find(item => item.value === 0)) {
    data.push({ value: 0, text: '作废' })
  }
  ruleStatusOptions.value = data
}

// 规则类型选项
const ruleTypeOptions = ref([])
const getRuleTypeItem = async () => {
  const result = await GetKeyAndValueByType("t_trial_rule_type")
  ruleTypeOptions.value = result.data || []
}

// ==================== 预测模拟数据字典 ====================
// 涨跌预测选项
const riseFallOptions = ref([])
const getRiseFallItem = async () => {
  const result = await GetKeyAndValueByType("t_trial_prediction_rise_fall")
  riseFallOptions.value = result.data || []
}

// 依据类型选项
const basisTypeOptions = ref([])
const getBasisTypeItem = async () => {
  const result = await GetKeyAndValueByType("t_trial_prediction_basis_type")
  basisTypeOptions.value = result.data || []
}

// 预测情况选项
const predictionSituationOptions = ref([])
const getPredictionSituationItem = async () => {
  const result = await GetKeyAndValueByType("t_trial_prediction_situation")
  predictionSituationOptions.value = result.data || []
}

// 预测结果选项
const predictionResultOptions = ref([])
const getPredictionResultItem = async () => {
  const result = await GetKeyAndValueByType("t_trial_prediction_result")
  predictionResultOptions.value = result.data || []
}

// 模拟操作选项
const simulateOperationOptions = ref([])
const getSimulateOperationItem = async () => {
  const result = await GetKeyAndValueByType("t_trial_simulate_operation")
  simulateOperationOptions.value = result.data || []
}

// 模拟交易状态选项
const simulateTradeStatusOptions = ref([])
const getSimulateTradeStatusItem = async () => {
  const result = await GetKeyAndValueByType("t_trial_simulate_trade_status")
  simulateTradeStatusOptions.value = result.data || []
}

// 台账资产类型选项
const simulateAssetTypeOptions = ref([])
const getSimulateAssetTypeItem = async () => {
  const result = await GetKeyAndValueByType("t_trial_simulate_asset_type")
  simulateAssetTypeOptions.value = result.data || []
}

//=========================================================
// ==================== 预测模拟管理 ====================
const predList = ref([])
const predTotal = ref(0)
const predPageParams = reactive({ page: 1, limit: 10 })
const predTimeArea = ref([])
const predSelectedRows = ref([])
const predTable = ref(null)
const predQueryDto = reactive({
  stockName: '',
  stockCode: '',
  riseFallPrediction: [],
  basisType: [],
  predictionTimeStart: null,
  predictionTimeEnd: null,
  predictionSituation: [],
  predictionResult: [],
  simulateOperation: [],
  tradeStatus: []
})

// 获取预测模拟数据
const fetchPredData = async () => {
  try {
    const result = await GetPredictionByConditionAndPage(predPageParams.page, predPageParams.limit, predQueryDto)
    if (result.code === 200) {
      const pageInfo = result.data || {}
      predList.value = pageInfo.list || []
      predTotal.value = pageInfo.total || 0
    } else {
      ElMessage.error(result.message || "查询失败")
    }
  } catch (error) {
    ElMessage.error("查询预测模拟数据失败")
  }
}

// 搜索预测模拟
const searchPredData = () => {
  predQueryDto.predictionTimeStart = predTimeArea.value && predTimeArea.value.length > 0 ? predTimeArea.value[0] : null
  predQueryDto.predictionTimeEnd = predTimeArea.value && predTimeArea.value.length > 0 ? predTimeArea.value[1] : null
  predPageParams.page = 1
  fetchPredData()
}

// 重置预测模拟搜索
const resetPredData = () => {
  predTimeArea.value = []
  Object.assign(predQueryDto, {
    stockName: '',
    stockCode: '',
    riseFallPrediction: [],
    basisType: [],
    predictionTimeStart: null,
    predictionTimeEnd: null,
    predictionSituation: [],
    predictionResult: [],
    simulateOperation: [],
    tradeStatus: []
  })
  predPageParams.page = 1
  fetchPredData()
}

// 预测模拟选择变化
const handlePredSelectionChange = (selection) => {
  predSelectedRows.value = selection
}

// 预测模拟添加/修改对话框
const predDialogVisible = ref(false)
const predDialogTitle = ref('添加预测')
const predFormRef = ref(null)
const predFormStep = ref(0)
const predFormData = reactive({
  id: null,
  stockName: '',
  stockCode: '',
  riseFallPrediction: null,
  predictionTime: '',
  predictionContent: '',
  predictionBasis: '',
  basisType: null,
  riseFallResult: null,
  actualContent: '',
  resultAnalysis: '',
  predictionSituation: null,
  predictionResult: null,
  simulateOperation: null,
  tradeShare: null,
  currentPrice: null,
  handlingFee: null,
  tradeStatus: null
})

const predFormRules = {
  stockName: [{ required: true, message: '请输入股票名称', trigger: 'blur' }],
  stockCode: [{ required: true, message: '请输入股票代码', trigger: 'blur' }],
  riseFallPrediction: [{ required: true, message: '请选择涨跌预测', trigger: 'change' }]
}

const addPrediction = () => {
  predDialogTitle.value = '添加预测'
  predFormStep.value = 0
  if (predFormRef.value) {
    predFormRef.value.resetFields()
  }
  Object.assign(predFormData, {
    id: null,
    stockName: '',
    stockCode: '',
    riseFallPrediction: null,
    predictionTime: '',
    predictionContent: '',
    predictionBasis: '',
    basisType: null,
    riseFallResult: null,
    actualContent: '',
    resultAnalysis: '',
    predictionSituation: null,
    predictionResult: null,
    simulateOperation: null,
    tradeShare: null,
    currentPrice: null,
    handlingFee: null,
    tradeStatus: null
  })
  predDialogVisible.value = true
}

const editPrediction = (row) => {
  predDialogTitle.value = '编辑预测'
  predFormStep.value = 0
  if (predFormRef.value) {
    predFormRef.value.resetFields()
  }
  Object.assign(predFormData, row)
  predDialogVisible.value = true
}

const submitPrediction = async () => {
  if (!predFormRef.value) return
  try {
    const valid = await predFormRef.value.validate()
    if (!valid) return
  } catch (error) {
    return
  }
  try {
    const result = await SavePrediction(predFormData)
    if (result.code === 200) {
      ElMessage.success(predFormData.id ? '编辑成功' : '添加成功')
      predDialogVisible.value = false
      fetchPredData()
      fetchLedgerData()
    } else {
      ElMessage.error(result.message || "保存失败")
    }
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const deletePrediction = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该预测记录吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    const result = await DeletePredictionById(row.id)
    if (result.code === 200) {
      ElMessage.success('删除成功')
      fetchPredData()
      fetchLedgerData()
    } else {
      ElMessage.error(result.message || "删除失败")
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const deletePredAll = async () => {
  if (!predSelectedRows.value || predSelectedRows.value.length === 0) {
    ElMessage.warning('请先选择要删除的记录')
    return
  }
  try {
    await ElMessageBox.confirm(
      '确定要批量删除选中的 ' + predSelectedRows.value.length + ' 条记录吗？',
      '警告',
      { type: 'warning' }
    )
    const ids = predSelectedRows.value.map(row => row.id)
    const result = await DeleteAllPredictionByIds(ids)
    if (result.code === 200) {
      ElMessage.success('批量删除成功')
      fetchPredData()
      fetchLedgerData()
      predTable.value.clearSelection()
      predSelectedRows.value = []
    } else {
      ElMessage.error(result.message || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

//=========================================================
// ==================== 模拟台账 ====================
const ledgerList = ref([])

const fetchLedgerData = async () => {
  try {
    const result = await GetSimulateLedgerList()
    if (result.code === 200) {
      ledgerList.value = result.data || []
    }
  } catch (error) {
    // 台账数据加载失败不影响页面
  }
}

// 初始化账户
const initAccount = async () => {
  try {
    await ElMessageBox.confirm('确定要初始化模拟账户吗？将创建一条模拟账户资产记录（初始金额10万）', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    })
    await SaveSimulateLedger({
      assetName: '模拟账户资产',
      assetCode: 'SIM_ACCOUNT',
      assetType: 1,
      assetAmount: 100000,
      assetQuantity: 0
    })
    ElMessage.success('初始化成功')
    fetchLedgerData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('初始化失败')
    }
  }
}

// 台账添加/修改对话框
const ledgerDialogVisible = ref(false)
const ledgerDialogTitle = ref('添加台账')
const ledgerFormRef = ref(null)
const ledgerFormData = reactive({
  id: null,
  assetName: '',
  assetCode: '',
  assetType: null,
  assetAmount: null,
  assetQuantity: null
})

const ledgerFormRules = {
  assetName: [{ required: true, message: '请输入资产名称', trigger: 'blur' }],
  assetType: [{ required: true, message: '请选择资产类型', trigger: 'change' }],
  assetAmount: [{ required: true, message: '请输入资产金额', trigger: 'blur' }]
}

const addLedger = () => {
  ledgerDialogTitle.value = '添加台账'
  if (ledgerFormRef.value) {
    ledgerFormRef.value.resetFields()
  }
  Object.assign(ledgerFormData, {
    id: null,
    assetName: '',
    assetCode: '',
    assetType: null,
    assetAmount: null,
    assetQuantity: null
  })
  ledgerDialogVisible.value = true
}

const editLedger = (row) => {
  ledgerDialogTitle.value = '编辑台账'
  if (ledgerFormRef.value) {
    ledgerFormRef.value.resetFields()
  }
  Object.assign(ledgerFormData, row)
  ledgerDialogVisible.value = true
}

const submitLedger = async () => {
  if (!ledgerFormRef.value) return
  try {
    const valid = await ledgerFormRef.value.validate()
    if (!valid) return
  } catch (error) {
    return
  }
  try {
    const result = await SaveSimulateLedger(ledgerFormData)
    if (result.code === 200) {
      ElMessage.success(ledgerFormData.id ? '编辑成功' : '添加成功')
      ledgerDialogVisible.value = false
      fetchLedgerData()
    } else {
      ElMessage.error(result.message || "保存失败")
    }
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const deleteLedger = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该台账记录吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    const result = await DeleteSimulateLedgerById(row.id)
    if (result.code === 200) {
      ElMessage.success('删除成功')
      fetchLedgerData()
    } else {
      ElMessage.error(result.message || "删除失败")
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 台账计算属性
const mainAccountAmount = computed(() => {
  const mainAccount = ledgerList.value.find(item => item.assetType === 1)
  return mainAccount ? Number(mainAccount.assetAmount || 0) : 0
})

const positionList = computed(() => {
  return ledgerList.value.filter(item => item.assetType !== 1)
})

const positionAmount = computed(() => {
  return positionList.value.reduce((sum, item) => sum + Number(item.assetAmount || 0), 0)
})

const positionCount = computed(() => {
  return positionList.value.length
})

const totalAssetAmount = computed(() => {
  return mainAccountAmount.value + positionAmount.value
})

// 金额格式化
const formatMoney = (value) => {
  if (value === null || value === undefined) return '0.00'
  const num = Number(value)
  if (isNaN(num)) return '0.00'
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

//=========================================================
// ==================== 统计报表 ====================
const reportTimeArea = ref([])
const reportDto = reactive({
  startTime: null,
  endTime: null,
  basisType: [],
  stockCode: ''
})
const reportData = ref({})

const fetchReportData = async () => {
  reportDto.startTime = reportTimeArea.value && reportTimeArea.value.length > 0 ? reportTimeArea.value[0] : null
  reportDto.endTime = reportTimeArea.value && reportTimeArea.value.length > 0 ? reportTimeArea.value[1] : null
  try {
    const result = await GetPredictionReport(reportDto)
    if (result.code === 200) {
      reportData.value = result.data || {}
    } else {
      ElMessage.error(result.message || "查询报表失败")
    }
  } catch (error) {
    ElMessage.error("查询报表失败")
  }
}

const resetReportData = () => {
  reportTimeArea.value = []
  Object.assign(reportDto, {
    startTime: null,
    endTime: null,
    basisType: [],
    stockCode: ''
  })
  fetchReportData()
}

//=========================================================
// ==================== 交易规则 ====================
const ruleList = ref([])
const ruleTable = ref(null)
const ruleSelectedRows = ref([])

// 规则筛选条件
const ruleQueryDto = reactive({
  ruleType: null,
  ruleStatus: 1
})

// 实际用于过滤的条件（点击搜索后才更新）
const activeRuleQuery = reactive({
  ruleType: null,
  ruleStatus: 1
})

// 根据筛选条件过滤规则列表
const filteredRuleList = computed(() => {
  return ruleList.value.filter(item => {
    if (activeRuleQuery.ruleType !== null && activeRuleQuery.ruleType !== '' && item.ruleType !== activeRuleQuery.ruleType) {
      return false
    }
    if (activeRuleQuery.ruleStatus !== null && activeRuleQuery.ruleStatus !== '' && item.ruleStatus !== activeRuleQuery.ruleStatus) {
      return false
    }
    return true
  })
})

// 计算遵守规则成功率 = 遵守成功次数 / 使用次数
const calcComplySuccessRate = (row) => {
  const useCount = row.useCount || 0
  const complySuccessCount = row.complySuccessCount || 0
  if (useCount === 0) return '-'
  return (complySuccessCount / useCount * 100).toFixed(1) + '%'
}

// 计算违反规则成功率 = 违反成功次数 / 违反次数
const calcViolateSuccessRate = (row) => {
  const violateCount = row.violateCount || 0
  const violateSuccessCount = row.violateSuccessCount || 0
  if (violateCount === 0) return '-'
  return (violateSuccessCount / violateCount * 100).toFixed(1) + '%'
}

// 根据成功率返回样式类名
const getRateClass = (rateStr) => {
  if (rateStr === '-') return 'rate-text rate-none'
  const numVal = parseFloat(rateStr)
  if (numVal >= 60) return 'rate-text rate-high'
  if (numVal >= 30) return 'rate-text rate-medium'
  return 'rate-text rate-low'
}

// 规则搜索（点击搜索时才应用筛选条件）
const searchRuleData = () => {
  activeRuleQuery.ruleType = ruleQueryDto.ruleType
  activeRuleQuery.ruleStatus = ruleQueryDto.ruleStatus
}

// 规则重置
const resetRuleData = () => {
  ruleQueryDto.ruleType = null
  ruleQueryDto.ruleStatus = 1
  activeRuleQuery.ruleType = null
  activeRuleQuery.ruleStatus = 1
}

// 获取规则数据
const fetchRuleData = async () => {
  try {
    const result = await GetTransactionRuleList()
    ruleList.value = result.data || []
  } catch (error) {
    // 规则数据加载失败不影响页面
  }
}

// 规则选择变化
const handleRuleSelectionChange = (selection) => {
  ruleSelectedRows.value = selection
}

// 规则详情查看
const ruleDetailVisible = ref(false)
const ruleDetailData = ref(null)
const viewRuleDetail = (row) => {
  ruleDetailData.value = { ...row }
  ruleDetailVisible.value = true
}

// 生成规则编号：RULE-年月日时分秒
const generateRuleCode = () => {
  const now = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  const dateStr = `${now.getFullYear()}${pad(now.getMonth() + 1)}${pad(now.getDate())}${pad(now.getHours())}${pad(now.getMinutes())}${pad(now.getSeconds())}`
  return `RULE${dateStr}`
}

// 添加规则
const ruleDialogVisible = ref(false)
const ruleDialogTitle = ref('添加规则')
const ruleFormRef = ref(null)
const ruleFormData = reactive({
  id: null,
  ruleCode: '',
  ruleType: '',
  ruleContent: '',
  ruleDetail: '',
  sortOrder: 0,
  ruleStatus: 1,
  violatePenalty: '',
  useCount: 0,
  violateCount: 0,
  complySuccessCount: 0,
  violateSuccessCount: 0
})

const ruleFormRules = {
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  ruleContent: [{ required: true, message: '请输入规则内容', trigger: 'blur' }]
}

const addRule = () => {
  ruleDialogTitle.value = '添加规则'
  if (ruleFormRef.value) {
    ruleFormRef.value.resetFields()
  }
  Object.assign(ruleFormData, {
    id: null,
    ruleCode: generateRuleCode(),
    ruleType: '',
    ruleContent: '',
    ruleDetail: '',
    sortOrder: 0,
    ruleStatus: 1,
    violatePenalty: '',
    useCount: 0,
    violateCount: 0,
    complySuccessCount: 0,
    violateSuccessCount: 0
  })
  ruleDialogVisible.value = true
}

const editRule = (row) => {
  ruleDialogTitle.value = '编辑规则'
  if (ruleFormRef.value) {
    ruleFormRef.value.resetFields()
  }
  Object.assign(ruleFormData, row)
  ruleDialogVisible.value = true
}

const submitRule = async () => {
  if (!ruleFormRef.value) return
  try {
    const valid = await ruleFormRef.value.validate()
    if (!valid) return
  } catch (error) {
    return
  }
  try {
    await SaveTransactionRule(ruleFormData)
    ElMessage.success(ruleFormData.id ? '编辑规则成功' : '添加规则成功')
    ruleDialogVisible.value = false
    fetchRuleData()
  } catch (error) {
    ElMessage.error(ruleFormData.id ? '编辑规则失败' : '添加规则失败')
  }
}

const deleteRule = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该规则吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await DeleteTransactionRuleById(row.id)
    ElMessage.success('删除规则成功')
    fetchRuleData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除规则失败')
    }
  }
}

const deleteRuleAll = async () => {
  if (!ruleSelectedRows.value || ruleSelectedRows.value.length === 0) {
    ElMessage.warning('请先选择要删除的规则')
    return
  }
  try {
    await ElMessageBox.confirm(
      '确定要批量删除选中的 ' + ruleSelectedRows.value.length + ' 条规则吗？',
      '警告',
      { type: 'warning' }
    )
    const ids = ruleSelectedRows.value.map(item => item.id)
    await DeleteAllTransactionRuleByIds(ids)
    ElMessage.success('批量删除规则成功')
    fetchRuleData()
    ruleTable.value.clearSelection()
    ruleSelectedRows.value = []
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除规则失败')
    }
  }
}
//=========================================================
// ==================== 交易试验记录 ====================
const list = ref([])
const total = ref(0)
const pageParams = reactive({ page: 1, limit: 10 })
const searchExpanded = ref(false)
const planTimeArea = ref([])
const queryDto = reactive({
  targetName: '',
  tradeType: [],
  planType: [],
  planStartTime: null,
  planEndTime: null,
  tradeStatus: [],
  tradeResult: [],
  tradeFailType: [],
  isUsePlan: null
})

// 获取数据
const fetchData = async () => {
  try {
    const result = await GetTransactionSystemTrialByConditionAndPage(pageParams.page, pageParams.limit, queryDto)
    if (result.code === 200) {
      const pageInfo = result.data || {}
      list.value = pageInfo.list || []
      total.value = pageInfo.total || 0
    } else {
      ElMessage.error(result.message || "查询失败")
    }
  } catch (error) {
    ElMessage.error("查询失败")
  }
}

// 搜索
const searchData = () => {
  queryDto.planStartTime = planTimeArea.value && planTimeArea.value.length > 0 ? planTimeArea.value[0] : null
  queryDto.planEndTime = planTimeArea.value && planTimeArea.value.length > 0 ? planTimeArea.value[1] : null
  pageParams.page = 1
  fetchData()
}

// 切换搜索条件展开/收起
const toggleSearchExpand = () => {
  searchExpanded.value = !searchExpanded.value
}

// 重置
const resetData = () => {
  planTimeArea.value = []
  Object.assign(queryDto, {
    targetName: '',
    tradeType: [],
    planType: [],
    planStartTime: null,
    planEndTime: null,
    tradeStatus: [],
    tradeResult: [],
    tradeFailType: [],
    isUsePlan: null
  })
  pageParams.page = 1
  fetchData()
}

// ==================== 添加/修改试验 ====================
const dialogVisible = ref(false)
const dialogTitle = ref('添加试验')
const formRef = ref(null)
const formData = reactive({
  id: null,
  tradeType: null,
  targetName: '',
  planType: null,
  planContent: '',
  planStartTime: '',
  planEndTime: '',
  currentPrice: null,
  planPrice: null,
  openPrice: null,
  actualPrice: null,
  closePrice: null,
  tradeStatus: null,
  complyRuleIds: [],
  violateRuleIds: [],
  tradeResult: null,
  tradeFailType: null,
  resultReview: '',
  isUsePlan: null
})

const formRules = {
  targetName: [{ required: true, message: '请输入交易对象名称', trigger: 'blur' }],
  tradeType: [{ required: true, message: '请选择交易类型', trigger: 'change' }],
  tradeStatus: [{ required: true, message: '请选择交易状态', trigger: 'change' }]
}

// 获取下一个交易日的开盘和收盘时间（A股：9:30-15:00）
const getNextTradingTime = () => {
  const now = new Date()
  let next = new Date(now)
  next.setDate(next.getDate() + 1)
  // 跳过周末
  const day = next.getDay()
  if (day === 0) next.setDate(next.getDate() + 1) // 周日->周一
  if (day === 6) next.setDate(next.getDate() + 2) // 周六->周一
  const pad = (n) => String(n).padStart(2, '0')
  const dateStr = `${next.getFullYear()}-${pad(next.getMonth() + 1)}-${pad(next.getDate())}`
  return {
    planStartTime: `${dateStr} 09:30:00`,
    planEndTime: `${dateStr} 15:00:00`
  }
}

const addRecord = () => {
  dialogTitle.value = '添加试验'
  if (formRef.value) {
    formRef.value.resetFields()
  }
  const nextTradingTime = getNextTradingTime()
  Object.assign(formData, {
    id: null,
    tradeType: null,
    targetName: '',
    planType: null,
    planContent: '',
    planStartTime: nextTradingTime.planStartTime,
    planEndTime: nextTradingTime.planEndTime,
    currentPrice: null,
    planPrice: null,
    openPrice: null,
    actualPrice: null,
    closePrice: null,
    tradeStatus: 1,
    complyRuleIds: [],
    violateRuleIds: [],
    tradeResult: null,
    tradeFailType: null,
    resultReview: '',
    isUsePlan: 0
  })
  dialogVisible.value = true
}

const editRecord = (row) => {
  dialogTitle.value = '编辑试验'
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(formData, row)
  // 将逗号分隔的字符串转为数组供多选下拉框使用
  formData.complyRuleIds = row.complyRuleIds ? row.complyRuleIds.split(',').map(Number) : []
  formData.violateRuleIds = row.violateRuleIds ? row.violateRuleIds.split(',').map(Number) : []
  dialogVisible.value = true
}

const submit = async () => {
  if (!formRef.value) return
  try {
    const valid = await formRef.value.validate()
    if (!valid) return
  } catch (error) {
    return
  }
  try {
    // 将数组转为逗号分隔字符串提交给后端
    const submitData = { ...formData }
    submitData.complyRuleIds = Array.isArray(formData.complyRuleIds) ? formData.complyRuleIds.join(',') : formData.complyRuleIds
    submitData.violateRuleIds = Array.isArray(formData.violateRuleIds) ? formData.violateRuleIds.join(',') : formData.violateRuleIds
    const result = await SaveTransactionSystemTrial(submitData)
    if (result.code === 200) {
      ElMessage.success(formData.id ? '编辑成功' : '添加成功')
      dialogVisible.value = false
      fetchData()
      fetchRuleData()
    } else {
      ElMessage.error(result.message || "保存失败")
    }
  } catch (error) {
    ElMessage.error('保存失败')
  }
}
// ==================== 删除试验 ====================
const deleteRecord = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该试验记录吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    const result = await DeleteTransactionSystemTrialById(row.id)
    if (result.code === 200) {
      ElMessage.success('删除成功')
      fetchData()
      fetchRuleData()
    } else {
      ElMessage.error(result.message || "删除失败")
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// ==================== 批量删除试验 ====================
const selectedRows = ref([])
const multipleTable = ref(null)

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const deleteSelectAll = async () => {
  if (!selectedRows.value || selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要删除的记录')
    return
  }
  try {
    await ElMessageBox.confirm(
      '确定要批量删除选中的 ' + selectedRows.value.length + ' 条记录吗？',
      '警告',
      { type: 'warning' }
    )
    const ids = selectedRows.value.map(row => row.id)
    const result = await DeleteAllTransactionSystemTrialByIds(ids)
    if (result.code === 200) {
      ElMessage.success('批量删除成功')
      fetchData()
      fetchRuleData()
      multipleTable.value.clearSelection()
      selectedRows.value = []
    } else {
      ElMessage.error(result.message || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

// ==================== 辅助方法 ====================
// 试验记录详情查看
const trialDetailVisible = ref(false)
const trialDetailData = ref(null)
const viewTrialDetail = (row) => {
  trialDetailData.value = { ...row }
  trialDetailVisible.value = true
}

// 根据规则id获取规则名称
const getRuleNames = (ruleIds) => {
  if (!ruleIds) return '-'
  const ids = String(ruleIds).split(',').map(id => Number(id.trim())).filter(id => !isNaN(id))
  if (ids.length === 0) return '-'
  const names = ids.map(id => {
    const rule = ruleList.value.find(r => r.id === id)
    return rule ? rule.ruleCode : `ID:${id}`
  })
  return names.join('、')
}

const getTradeStatusTagType = (status) => {
  switch (status) {
    case 1: return 'info'
    case 2: return 'warning'
    case 3: return 'success'
    default: return 'info'
  }
}
// ==================== 导出功能 ====================
const exportColumns = [
  { key: 'targetName', label: '交易对象', width: 20 },
  { key: 'tradeType', label: '交易类型', width: 15 },
  { key: 'planType', label: '计划类型', width: 15 },
  { key: 'currentPrice', label: '当前价', width: 12 },
  { key: 'planPrice', label: '计划价', width: 12 },
  { key: 'openPrice', label: '开盘价', width: 12 },
  { key: 'actualPrice', label: '成交价', width: 12 },
  { key: 'closePrice', label: '收盘价', width: 12 },
  { key: 'tradeStatus', label: '交易状态', width: 12 },
  { key: 'tradeResult', label: '交易结果', width: 12 },
  { key: 'tradeFailType', label: '失败类型', width: 12 },
  { key: 'isUsePlan', label: '触发计划', width: 10 },
  { key: 'planStartTime', label: '计划开始时间', width: 20 },
  { key: 'planEndTime', label: '计划结束时间', width: 20 },
  { key: 'planContent', label: '计划内容', width: 30 },
  { key: 'complyRuleIds', label: '遵守规则', width: 20 },
  { key: 'violateRuleIds', label: '违反规则', width: 20 },
  { key: 'resultReview', label: '结果复盘', width: 30 }
]

const dataFormatter = (item, key, value) => {
  switch (key) {
    case 'tradeType':
      return getDisplayText(value, tradeTypeOptions.value)
    case 'planType':
      return getDisplayText(value, planTypeOptions.value)
    case 'tradeStatus':
      return getDisplayText(value, tradeStatusOptions.value)
    case 'tradeResult':
      return getDisplayText(value, tradeResultOptions.value)
    case 'tradeFailType':
      return getDisplayText(value, tradeFailTypeOptions.value)
    case 'isUsePlan':
      return value === 1 ? '是' : '否'
    default:
      return value
  }
}

const fetchAllData = async () => {
  const { data } = await GetTransactionSystemTrialByConditionAndPage(1, 1000000, queryDto)
  return data.list || []
}

const {
  exportDialogVisible,
  exportScope,
  exportFileName,
  exportLoading,
  selectedColumns,
  showExportDialog: showExportDialogMethod,
  handleExport,
  resetExport
} = useExport({
  availableColumns: exportColumns,
  fetchAllData: fetchAllData,
  dataFormatter: dataFormatter,
  defaultFileName: '交易系统试验数据',
  sheetName: '交易系统试验数据'
})

const showExportDialog = () => {
  showExportDialogMethod(list.value, total.value)
}
</script><style scoped>
/* 页面容器 */
.page-container {
  position: relative;
  width: 100%;
  min-height: 100vh;
  margin: 0;
  padding: 0;
  overflow: auto;
}

.page-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('src/assets/memory/background.gif');
  background-size: cover;
  background-attachment: fixed;
  opacity: 0.5;
  z-index: 0;
}

.page-container > * {
  position: relative;
  z-index: 1;
}

/* 页面标题头部 */
.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 30px;
  margin-bottom: 0;
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

/* 标签页样式 */
.main-tabs {
  margin: 0 15px;
}

:deep(.main-tabs .el-tabs__header) {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 4px 4px 0 0;
  padding: 0 15px;
  margin-bottom: 0;
}

:deep(.main-tabs .el-tabs__item) {
  font-size: 15px;
  font-weight: 600;
  height: 44px;
  line-height: 44px;
}

:deep(.main-tabs .el-tabs__content) {
  padding: 0;
}

/* 交易规则块 */
.rule-div {
  padding: 15px;
  border: 1px solid #ebeef5;
  border-top: none;
  border-radius: 0 0 4px 4px;
  background-color: rgba(255, 255, 255, 0.8);
}

.rule-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.rule-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.rule-actions {
  display: flex;
  gap: 8px;
}

/* 交易试验记录块 */
.trial-div {
  padding: 15px;
  border: 1px solid #ebeef5;
  border-top: none;
  border-radius: 0 0 4px 4px;
  background-color: rgba(255, 255, 255, 0.8);
}

.trial-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.trial-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

/* 预测模拟块 */
.prediction-div {
  padding: 15px;
  border: 1px solid #ebeef5;
  border-top: none;
  border-radius: 0 0 4px 4px;
  background-color: rgba(255, 255, 255, 0.8);
}

/* 模拟账户总览横条 */
.account-overview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  border-radius: 8px;
  color: #fff;
  margin-bottom: 12px;
  box-shadow: 0 4px 12px rgba(15, 52, 96, 0.3);
}

.account-overview-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.account-icon {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e94560, #c23616);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(233, 69, 96, 0.4);
}

.account-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 4px;
}

.account-total-amount {
  font-size: 28px;
  font-weight: 700;
  font-family: 'DIN Alternate', 'Helvetica Neue', monospace;
  letter-spacing: 1px;
  color: #e94560;
  text-shadow: 0 0 10px rgba(233, 69, 96, 0.3);
}

.account-overview-right {
  display: flex;
  align-items: center;
  gap: 0;
}

.account-stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 20px;
}

.account-stat-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 4px;
}

.account-stat-value {
  font-size: 16px;
  font-weight: 600;
  font-family: 'DIN Alternate', 'Helvetica Neue', monospace;
}

.account-stat-value.available {
  color: #f5c542;
}

.account-stat-value.position {
  color: #4ecdc4;
}

.account-stat-value.count {
  color: #a8e6cf;
}

.account-stat-divider {
  width: 1px;
  height: 36px;
  background: rgba(255, 255, 255, 0.15);
}

/* 持仓卡片区域 */
.position-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 4px;
}

.position-card {
  flex: 0 0 calc(25% - 9px);
  min-width: 220px;
  background: linear-gradient(145deg, #ffffff 0%, #f8f9fa 100%);
  border: 1px solid #e0e6ed;
  border-radius: 8px;
  padding: 12px 16px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
}

.position-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
  border-color: #409eff;
}

.position-card-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f2f5;
}

.position-stock-name {
  font-size: 15px;
  font-weight: 700;
  color: #303133;
  margin-right: 8px;
}

.position-stock-code {
  font-size: 12px;
  color: #909399;
  font-family: 'Courier New', monospace;
}

.position-card-actions {
  margin-left: auto;
}

.position-card-body {
  display: flex;
  gap: 24px;
}

.position-card-col {
  flex: 1;
}

.position-card-label {
  font-size: 11px;
  color: #909399;
  margin-bottom: 4px;
}

.position-card-value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  font-family: 'DIN Alternate', 'Helvetica Neue', monospace;
}

.position-card-value.highlight {
  color: #e6a23c;
}

.position-card-unit {
  font-size: 12px;
  color: #909399;
  font-weight: 400;
  margin-left: 2px;
}

.position-empty {
  padding: 20px;
  text-align: center;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 8px;
  border: 1px dashed #dcdfe6;
  margin-bottom: 4px;
}

/* 统计报表块 */
.report-div {
  padding: 15px;
  border: 1px solid #ebeef5;
  border-top: none;
  border-radius: 0 0 4px 4px;
  background-color: rgba(255, 255, 255, 0.8);
}

.report-section {
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: rgba(255, 255, 255, 0.6);
}

.trial-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.trial-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
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

.expand-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #409eff;
  transition: all 0.3s ease;
}

.expand-btn:hover {
  color: #66b1ff;
}

.expand-btn .el-icon {
  transition: transform 0.3s ease;
}
/* 操作按钮区域 */
.tools-div {
  margin-bottom: 10px;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: rgba(255, 255, 255, 0.8);
}

/* 搜索按钮美化 */
.beautified-search-btn {
  border-radius: 4px;
  padding: 8px 15px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.2s ease;
  border: none;
  height: auto;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
  box-shadow: 0 1px 4px rgba(64, 158, 255, 0.3);
}

.beautified-search-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.4);
  background: linear-gradient(135deg, #337ecc 0%, #529ce3 100%);
}

/* 重置按钮美化 */
.beautified-reset-btn {
  border-radius: 4px;
  padding: 8px 15px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.2s ease;
  border: 1px solid #dcdfe6;
  height: auto;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: white;
  color: #606266;
}

.beautified-reset-btn:hover {
  transform: translateY(-1px);
  border-color: #409eff;
  color: #409eff;
}

/* 操作按钮区域按钮美化 */
.beautified-tools .el-button {
  border-radius: 8px;
  padding: 10px 20px;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  letter-spacing: 0.5px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.beautified-tools .el-button--success {
  background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%);
  border: none;
}

.beautified-tools .el-button--success:hover {
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 6px 16px rgba(103, 194, 58, 0.4);
  background: linear-gradient(135deg, #529b2e 0%, #73c050 100%);
}

.beautified-tools .el-button--danger {
  background: linear-gradient(135deg, #F56C6C 0%, #f78989 100%);
  border: none;
}

.beautified-tools .el-button--danger:hover {
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 6px 16px rgba(245, 108, 108, 0.4);
  background: linear-gradient(135deg, #d84646 0%, #f06b6b 100%);
}

.beautified-tools .el-button--info {
  background: linear-gradient(135deg, #909399 0%, #a6a9ad 100%);
  border: none;
}

.beautified-tools .el-button--info:hover {
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 6px 16px rgba(144, 147, 153, 0.4);
  background: linear-gradient(135deg, #73767a 0%, #8d9094 100%);
}

.beautified-tools .el-button:active {
  transform: translateY(0) scale(0.98);
}
/* 表格内按钮美化 */
/deep/ .el-table .el-button {
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  letter-spacing: 0.3px;
  border: none;
}

/deep/ .el-table .el-button--primary {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.3);
}

/deep/ .el-table .el-button--primary:hover {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 10px rgba(64, 158, 255, 0.4);
  background: linear-gradient(135deg, #337ecc 0%, #529ce3 100%);
}

/deep/ .el-table .el-button--danger {
  background: linear-gradient(135deg, #F56C6C 0%, #f78989 100%);
  box-shadow: 0 2px 6px rgba(245, 108, 108, 0.3);
}

/deep/ .el-table .el-button--danger:hover {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 10px rgba(245, 108, 108, 0.4);
  background: linear-gradient(135deg, #d84646 0%, #f06b6b 100%);
}

/deep/ .el-table .el-button--info {
  background: linear-gradient(135deg, #909399 0%, #a6a9ad 100%);
  box-shadow: 0 2px 6px rgba(144, 147, 153, 0.3);
}

/deep/ .el-table .el-button--info:hover {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 10px rgba(144, 147, 153, 0.4);
  background: linear-gradient(135deg, #73767a 0%, #8d9094 100%);
}

/deep/ .el-table .el-button:active {
  transform: translateY(0) scale(0.95);
}

/* 对话框按钮美化 */
.dialog-footer .el-button {
  border-radius: 8px;
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  letter-spacing: 0.5px;
}

.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.dialog-footer .el-button--primary:hover {
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
  background: linear-gradient(135deg, #337ecc 0%, #529ce3 100%);
}

.dialog-footer .el-button--default {
  border: 2px solid #e4e7ed;
  background: white;
  color: #606266;
}

.dialog-footer .el-button--default:hover {
  border-color: #409eff;
  color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.dialog-footer .el-button:active {
  transform: translateY(0) scale(0.98);
}

/* 表格样式 */
/deep/ .el-table {
  background-color: #ffffff;
}

/deep/ .el-table th {
  background: #fafbfc;
  color: #2c3e50;
  font-weight: 600;
  font-size: 14px;
}

/deep/ .el-table td {
  font-size: 14px;
  background-color: #ffffff;
}

/deep/ .el-table--striped .el-table__body tr.el-table__row--striped td {
  background-color: #fafbfc;
}

/deep/ .el-table__body tr:hover > td {
  background-color: #e6f7ff !important;
}
/* 对话框样式 */
:deep(.custom-dialog),
:deep(.enhanced-dialog) {
  border-radius: 16px !important;
  overflow: hidden !important;
  box-shadow: 0 16px 48px rgba(102, 126, 234, 0.25) !important;
}

:deep(.enhanced-dialog .el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border-radius: 16px 16px 0 0 !important;
  padding: 24px 28px !important;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3) !important;
  position: relative;
  margin: 0 !important;
  width: 100% !important;
  box-sizing: border-box !important;
}

:deep(.enhanced-dialog .el-dialog__title) {
  color: white !important;
  font-weight: 700 !important;
  font-size: 22px !important;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  position: relative;
  z-index: 1;
}

:deep(.enhanced-dialog .el-dialog__headerbtn .el-dialog__close) {
  color: white !important;
  font-size: 22px !important;
  transition: all 0.3s ease;
  position: relative;
  z-index: 1;
}

:deep(.enhanced-dialog .el-dialog__headerbtn .el-dialog__close:hover) {
  transform: rotate(90deg) scale(1.1);
  color: rgba(255, 255, 255, 0.8) !important;
}

:deep(.enhanced-dialog .el-dialog__body) {
  padding: 28px !important;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%) !important;
  max-height: 60vh !important;
  overflow-y: auto !important;
}

:deep(.enhanced-dialog .el-dialog__body::-webkit-scrollbar) {
  width: 8px;
}

:deep(.enhanced-dialog .el-dialog__body::-webkit-scrollbar-track) {
  background: #f1f1f1;
  border-radius: 4px;
}

:deep(.enhanced-dialog .el-dialog__body::-webkit-scrollbar-thumb) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px;
}

:deep(.enhanced-dialog .el-dialog__body::-webkit-scrollbar-thumb:hover) {
  background: linear-gradient(135deg, #5568d3 0%, #6a4190 100%);
}

:deep(.enhanced-dialog .el-dialog__footer) {
  padding: 20px 28px !important;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%) !important;
  border-top: 2px solid rgba(102, 126, 234, 0.1) !important;
}

/* 分页组件样式 */
/deep/ .el-pagination {
  justify-content: center;
}

/deep/ .el-pagination button,
/deep/ .el-pagination .el-pager li {
  background-color: rgba(255, 255, 255, 0.9);
}

/* 规则详情弹窗样式 */
.rule-detail-container {
  padding: 0 4px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.detail-section-title {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 12px;
  padding-left: 10px;
  border-left: 4px solid #667eea;
  line-height: 1;
}

.detail-value {
  font-size: 14px;
  color: #606266;
}

.detail-value.highlight {
  color: #667eea;
  font-weight: 600;
  font-family: 'Courier New', monospace;
}

.detail-text-block {
  font-size: 14px;
  color: #303133;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-all;
}

.detail-text-block.penalty-text {
  color: #e6a23c;
  font-weight: 500;
}

.stat-card {
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 20px 16px;
  text-align: center;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 8px;
}

.stat-value.primary {
  color: #409EFF;
}

.stat-value.danger {
  color: #F56C6C;
}

.stat-value.success {
  color: #67C23A;
}

.stat-value.warning {
  color: #E6A23C;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

/* 成功率文字样式 */
.rate-text {
  font-weight: 600;
  font-size: 13px;
}

.rate-none {
  color: #c0c4cc;
}

.rate-high {
  color: #67C23A;
}

.rate-medium {
  color: #E6A23C;
}

.rate-low {
  color: #F56C6C;
}

/* 成功率卡片样式 */
.rate-card {
  position: relative;
  padding: 24px 16px;
}

.rate-card .rate-formula {
  font-size: 12px;
  color: #b0b5bd;
  margin-top: 6px;
  font-style: italic;
}
</style>