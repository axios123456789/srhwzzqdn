<template>
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
              <el-date-picker v-model="predTimeArea" type="datetimerange" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" :unlink-panels="true" />
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
              <el-button type="primary" size="small" @click="searchPredData"><el-icon><Search /></el-icon>搜索</el-button>
              <el-button size="small" @click="resetPredData"><el-icon><Refresh /></el-icon>重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>
    <!-- 操作按钮区域 -->
    <div class="tools-div beautified-tools" style="text-align: right;">
      <el-button type="success" size="small" @click="addPrediction"><el-icon><DocumentAdd /></el-icon>添加预测</el-button>
      <el-button type="danger" size="small" @click="deletePredAll" :disabled="predSelectedRows.length === 0"><el-icon><Delete /></el-icon>批量删除</el-button>
    </div>
    <!-- 预测模拟数据表格 -->
    <el-table :data="predList" v-loading="predLoading" style="width: 100%" height="300" ref="predTable" @selection-change="handlePredSelectionChange" border stripe size="small">
      <el-table-column type="selection" width="40" align="center" />
      <el-table-column label="操作" align="center" fixed="left" width="280" #default="scope">
        <el-button type="info" size="small" @click="viewPredDetail(scope.row)"><el-icon><View /></el-icon>查看</el-button>
        <el-button type="primary" size="small" @click="editPrediction(scope.row)"><el-icon><Edit /></el-icon>编辑</el-button>
        <el-button type="danger" size="small" @click="deletePrediction(scope.row)"><el-icon><Delete /></el-icon>删除</el-button>
      </el-table-column>
      <el-table-column prop="stockName" label="股票名称" align="center" width="100" show-overflow-tooltip />
      <el-table-column prop="stockCode" label="股票代码" align="center" width="100" />
      <el-table-column prop="riseFallPrediction" label="涨跌预测" align="center" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.riseFallPrediction === 1 ? 'danger' : 'success'" size="small">{{ getDisplayText(scope.row.riseFallPrediction, riseFallOptions) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="riseFallResult" label="涨跌结果" align="center" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.riseFallResult" :type="scope.row.riseFallResult === 1 ? 'danger' : 'success'" size="small">{{ getDisplayText(scope.row.riseFallResult, riseFallOptions) }}</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="predictionTime" label="预测时间" align="center" width="160" />
      <el-table-column prop="basisType" label="依据类型" align="center" min-width="120">
        <template #default="scope">{{ getBasisTypeDisplay(scope.row.basisType) }}</template>
      </el-table-column>
      <el-table-column prop="predictionSource" label="预测源" align="center" width="90">
        <template #default="scope">
          <el-tag v-if="scope.row.predictionSource" :type="scope.row.predictionSource === 1 ? 'info' : 'success'" size="small">{{ getDisplayText(scope.row.predictionSource, predictionSourceOptions) }}</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="predictionResult" label="预测结果" align="center" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.predictionResult === 1 ? 'success' : 'danger'" size="small">{{ getDisplayText(scope.row.predictionResult, predictionResultOptions) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="predictionSituation" label="预测情况" align="center" width="100">
        <template #default="scope">{{ getDisplayText(scope.row.predictionSituation, predictionSituationOptions) }}</template>
      </el-table-column>
      <el-table-column prop="simulateOperation" label="模拟操作" align="center" width="90">
        <template #default="scope">
          <el-tag v-if="scope.row.simulateOperation" :type="scope.row.simulateOperation === 1 ? 'danger' : 'success'" size="small">{{ getDisplayText(scope.row.simulateOperation, simulateOperationOptions) }}</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="tradeShare" label="交易份额" align="center" width="80" />
      <el-table-column prop="currentPrice" label="当前股价" align="center" width="90">
        <template #default="scope">{{ scope.row.currentPrice != null ? Number(scope.row.currentPrice).toFixed(2) : '-' }}</template>
      </el-table-column>
      <el-table-column prop="handlingFee" label="手续费" align="center" width="80">
        <template #default="scope">{{ scope.row.handlingFee != null ? Number(scope.row.handlingFee).toFixed(2) : '-' }}</template>
      </el-table-column>
      <el-table-column prop="tradeStatus" label="交易状态" align="center" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.tradeStatus" :type="scope.row.tradeStatus === 1 ? 'success' : 'danger'" size="small">{{ getDisplayText(scope.row.tradeStatus, simulateTradeStatusOptions) }}</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="predictionContent" label="预测内容" align="center" min-width="120" show-overflow-tooltip />
      <el-table-column prop="predictionBasis" label="预测依据" align="center" min-width="120" show-overflow-tooltip />
      <el-table-column prop="actualContent" label="实际内容" align="center" min-width="120" show-overflow-tooltip />
      <el-table-column prop="resultAnalysis" label="结果分析" align="center" min-width="120" show-overflow-tooltip />
    </el-table>
    <!-- 分页组件 -->
    <el-pagination style="margin-top: 15px" v-model:current-page="predPageParams.page" v-model:page-size="predPageParams.limit" :page-sizes="PAGE_SIZES" @size-change="fetchPredData" @current-change="fetchPredData" layout="total, sizes, prev, pager, next" :total="predTotal" />
  </div>

  <!-- 预测详情查看对话框 -->
  <el-dialog v-model="predDetailVisible" title="预测详情" width="65%" class="custom-dialog enhanced-dialog" :close-on-click-modal="true">
    <div class="pred-detail-container" v-if="predDetailData">
      <div class="detail-section">
        <div class="detail-section-title">股票信息</div>
        <el-descriptions :column="2" border size="default" label-width="80px">
          <el-descriptions-item label="股票名称" :span="1"><span class="detail-value highlight">{{ predDetailData.stockName }}</span></el-descriptions-item>
          <el-descriptions-item label="股票代码" :span="1"><span class="detail-value">{{ predDetailData.stockCode }}</span></el-descriptions-item>
          <el-descriptions-item label="预测时间" :span="1"><span class="detail-value">{{ predDetailData.predictionTime || '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="依据类型" :span="1"><el-tag type="warning" size="small">{{ getBasisTypeDisplay(predDetailData.basisType) }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="预测源" :span="1">
            <el-tag v-if="predDetailData.predictionSource" :type="predDetailData.predictionSource === 1 ? 'info' : 'success'" size="small">{{ getDisplayText(predDetailData.predictionSource, predictionSourceOptions) }}</el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="detail-section">
        <div class="detail-section-title">预测与结果</div>
        <el-row :gutter="16">
          <el-col :span="6"><div class="stat-card"><div class="stat-value" :class="predDetailData.riseFallPrediction === 1 ? 'danger' : 'success'">{{ getDisplayText(predDetailData.riseFallPrediction, riseFallOptions) }}</div><div class="stat-label">涨跌预测</div></div></el-col>
          <el-col :span="6"><div class="stat-card"><div class="stat-value" :class="predDetailData.riseFallResult === 1 ? 'danger' : (predDetailData.riseFallResult === 2 ? 'success' : 'primary')">{{ predDetailData.riseFallResult ? getDisplayText(predDetailData.riseFallResult, riseFallOptions) : '-' }}</div><div class="stat-label">涨跌结果</div></div></el-col>
          <el-col :span="6"><div class="stat-card"><div class="stat-value" :class="predDetailData.predictionResult === 1 ? 'success' : 'danger'">{{ getDisplayText(predDetailData.predictionResult, predictionResultOptions) }}</div><div class="stat-label">预测结果</div></div></el-col>
          <el-col :span="6"><div class="stat-card"><div class="stat-value primary">{{ getDisplayText(predDetailData.predictionSituation, predictionSituationOptions) || '-' }}</div><div class="stat-label">预测情况</div></div></el-col>
        </el-row>
      </div>
      <div class="detail-section">
        <div class="detail-section-title">预测内容</div>
        <el-descriptions :column="1" border size="default" label-width="80px">
          <el-descriptions-item label="预测内容"><div class="detail-text-block">{{ predDetailData.predictionContent || '-' }}</div></el-descriptions-item>
          <el-descriptions-item label="预测依据"><div class="detail-text-block">{{ predDetailData.predictionBasis || '-' }}</div></el-descriptions-item>
          <el-descriptions-item label="实际内容"><div class="detail-text-block">{{ predDetailData.actualContent || '-' }}</div></el-descriptions-item>
          <el-descriptions-item label="结果分析"><div class="detail-text-block">{{ predDetailData.resultAnalysis || '-' }}</div></el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="detail-section">
        <div class="detail-section-title">模拟交易</div>
        <el-descriptions :column="3" border size="default" label-width="80px">
          <el-descriptions-item label="模拟操作" :span="1">
            <el-tag v-if="predDetailData.simulateOperation" :type="predDetailData.simulateOperation === 1 ? 'danger' : 'success'" size="small">{{ getDisplayText(predDetailData.simulateOperation, simulateOperationOptions) }}</el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="交易份额" :span="1"><span class="detail-value">{{ predDetailData.tradeShare != null ? predDetailData.tradeShare + ' 股' : '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="当前股价" :span="1"><span class="detail-value">{{ predDetailData.currentPrice != null ? '¥ ' + Number(predDetailData.currentPrice).toFixed(2) : '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="手续费" :span="1"><span class="detail-value">{{ predDetailData.handlingFee != null ? '¥ ' + Number(predDetailData.handlingFee).toFixed(2) : '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="交易状态" :span="1">
            <el-tag v-if="predDetailData.tradeStatus" :type="predDetailData.tradeStatus === 1 ? 'success' : 'danger'" size="small">{{ getDisplayText(predDetailData.tradeStatus, simulateTradeStatusOptions) }}</el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="交易金额" :span="1">
            <span class="detail-value highlight" v-if="predDetailData.tradeShare && predDetailData.currentPrice">¥ {{ (Number(predDetailData.tradeShare) * Number(predDetailData.currentPrice)).toFixed(2) }}</span>
            <span v-else>-</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </div>
    <template #footer><span class="dialog-footer"><el-button @click="predDetailVisible = false">关闭</el-button></span></template>
  </el-dialog>

  <!-- 预测模拟 添加/修改对话框 -->
  <el-dialog v-model="predDialogVisible" :title="predDialogTitle" width="65%" class="custom-dialog enhanced-dialog" :close-on-click-modal="false">
    <el-steps :active="predFormStep" align-center style="margin-bottom: 20px;">
      <el-step title="预测信息" />
      <el-step title="结果与模拟" />
    </el-steps>
    <el-form :model="predFormData" label-width="120px" :rules="predFormRules" ref="predFormRef">
      <div v-show="predFormStep === 0">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="股票名称" prop="stockName"><el-input v-model="predFormData.stockName" placeholder="请输入股票名称" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="股票代码" prop="stockCode"><div style="display: flex; align-items: center; gap: 8px;"><el-input v-model="predFormData.stockCode" placeholder="请输入股票代码" style="flex: 1;" /><el-button type="primary" plain size="small" :loading="aiPredictLoading" @click="aiPredict">🤖 智能预测</el-button></div></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="涨跌预测" prop="riseFallPrediction"><el-select v-model="predFormData.riseFallPrediction" style="width: 100%" placeholder="请选择涨跌预测"><el-option v-for="item in riseFallOptions" :key="item.value" :label="item.text" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="预测时间" prop="predictionTime"><el-date-picker v-model="predFormData.predictionTime" type="datetime" placeholder="请选择预测时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="依据类型" prop="basisType"><el-select v-model="predFormData.basisType" style="width: 100%" placeholder="请选择依据类型(可多选)" multiple clearable><el-option v-for="item in basisTypeOptions" :key="item.value" :label="item.text" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="预测源" prop="predictionSource"><el-select v-model="predFormData.predictionSource" style="width: 100%" placeholder="请选择预测源" clearable><el-option v-for="item in predictionSourceOptions" :key="item.value" :label="item.text" :value="item.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20"><el-col :span="24"><el-form-item label="预测内容" prop="predictionContent"><el-input v-model="predFormData.predictionContent" type="textarea" :rows="2" placeholder="请输入预测内容" /></el-form-item></el-col></el-row>
        <el-row :gutter="20"><el-col :span="24"><el-form-item label="预测依据" prop="predictionBasis"><el-input v-model="predFormData.predictionBasis" type="textarea" :rows="2" placeholder="请输入预测依据" /></el-form-item></el-col></el-row>
      </div>
      <div v-show="predFormStep === 1">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="涨跌结果" prop="riseFallResult"><el-select v-model="predFormData.riseFallResult" style="width: 100%" clearable placeholder="请选择涨跌结果"><el-option v-for="item in riseFallOptions" :key="item.value" :label="item.text" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="预测情况" prop="predictionSituation"><el-select v-model="predFormData.predictionSituation" style="width: 100%" clearable placeholder="请选择预测情况"><el-option v-for="item in predictionSituationOptions" :key="item.value" :label="item.text" :value="item.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="预测结果" prop="predictionResult"><el-select v-model="predFormData.predictionResult" style="width: 100%" clearable placeholder="请选择预测结果"><el-option v-for="item in predictionResultOptions" :key="item.value" :label="item.text" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="模拟操作" prop="simulateOperation"><el-select v-model="predFormData.simulateOperation" style="width: 100%" clearable placeholder="请选择模拟操作"><el-option v-for="item in simulateOperationOptions" :key="item.value" :label="item.text" :value="item.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="交易份额" prop="tradeShare"><el-input-number v-model="predFormData.tradeShare" :min="0" style="width: 100%" placeholder="交易份额" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="当前股价" prop="currentPrice"><el-input-number v-model="predFormData.currentPrice" :precision="2" :min="0" style="width: 100%" placeholder="当前股价" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="手续费" prop="handlingFee"><el-input-number v-model="predFormData.handlingFee" :precision="2" :min="0" style="width: 100%" placeholder="手续费" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20"><el-col :span="12"><el-form-item label="交易状态" prop="tradeStatus"><el-select v-model="predFormData.tradeStatus" style="width: 100%" clearable placeholder="请选择交易状态"><el-option v-for="item in simulateTradeStatusOptions" :key="item.value" :label="item.text" :value="item.value" /></el-select></el-form-item></el-col></el-row>
        <el-row :gutter="20"><el-col :span="24"><el-form-item label="实际内容" prop="actualContent"><el-input v-model="predFormData.actualContent" type="textarea" :rows="2" placeholder="请输入实际内容" /></el-form-item></el-col></el-row>
        <el-row :gutter="20"><el-col :span="24"><el-form-item label="结果分析" prop="resultAnalysis"><el-input v-model="predFormData.resultAnalysis" type="textarea" :rows="2" placeholder="请输入结果分析" /></el-form-item></el-col></el-row>
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
  <el-dialog v-model="ledgerDialogVisible" :title="ledgerDialogTitle" width="50%" class="custom-dialog enhanced-dialog" :close-on-click-modal="false">
    <el-form :model="ledgerFormData" label-width="120px" :rules="ledgerFormRules" ref="ledgerFormRef">
      <el-row :gutter="20">
        <el-col :span="12"><el-form-item label="资产名称" prop="assetName"><el-input v-model="ledgerFormData.assetName" placeholder="请输入资产名称" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="资产编号" prop="assetCode"><el-input v-model="ledgerFormData.assetCode" placeholder="请输入资产编号" /></el-form-item></el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12"><el-form-item label="资产类型" prop="assetType"><el-select v-model="ledgerFormData.assetType" style="width: 100%" placeholder="请选择资产类型"><el-option v-for="item in simulateAssetTypeOptions" :key="item.value" :label="item.text" :value="item.value" /></el-select></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="资产金额" prop="assetAmount"><el-input-number v-model="ledgerFormData.assetAmount" :precision="2" style="width: 100%" placeholder="资产金额" /></el-form-item></el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12"><el-form-item label="资产数量" prop="assetQuantity"><el-input-number v-model="ledgerFormData.assetQuantity" :min="0" style="width: 100%" placeholder="资产数量(股)" /></el-form-item></el-col>
      </el-row>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="ledgerDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitLedger">提交</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, DocumentAdd, Delete, Edit, View, Wallet } from '@element-plus/icons-vue'
import { GetKeyAndValueByType } from "@/api/sysDict"
import { GetPredictionByConditionAndPage, SavePrediction, DeletePredictionById, DeleteAllPredictionByIds, GetSimulateLedgerList, SaveSimulateLedger, DeleteSimulateLedgerById, DeleteAllSimulateLedger, AiPredict } from "@/api/trialExecutionArea/predictionSimulate"
import { getDisplayText } from "@/utils/common"

// ==================== 通用常量 ====================
const PAGE_SIZES = [10, 20, 50, 100]

// ==================== 数据字典 ====================
const loadDict = async (tableName, optionsRef) => {
  const result = await GetKeyAndValueByType(tableName)
  optionsRef.value = result.data || []
}

const riseFallOptions = ref([])
const basisTypeOptions = ref([])
const predictionSourceOptions = ref([])
const predictionSituationOptions = ref([])
const predictionResultOptions = ref([])
const simulateOperationOptions = ref([])
const simulateTradeStatusOptions = ref([])
const simulateAssetTypeOptions = ref([])

// 依据类型多值显示
const getBasisTypeDisplay = (basisType) => {
  if (!basisType) return '-'
  if (Array.isArray(basisType)) {
    return basisType.map(type => getDisplayText(Number(type), basisTypeOptions.value)).join(', ')
  }
  if (typeof basisType === 'string') {
    return basisType.split(',').filter(v => v).map(type => getDisplayText(Number(type), basisTypeOptions.value)).join(', ')
  }
  return getDisplayText(basisType, basisTypeOptions.value)
}

// 金额格式化
const formatMoney = (value) => {
  if (value === null || value === undefined) return '0.00'
  const num = Number(value)
  if (isNaN(num)) return '0.00'
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

// ==================== 预测模拟管理 ====================
const predList = ref([])
const predTotal = ref(0)
const predPageParams = reactive({ page: 1, limit: 10 })
const predTimeArea = ref([])
const predSelectedRows = ref([])
const predTable = ref(null)
const predQueryDto = reactive({ stockName: '', stockCode: '', riseFallPrediction: [], basisType: [], predictionTimeStart: null, predictionTimeEnd: null, predictionSituation: [], predictionResult: [], simulateOperation: [], tradeStatus: [] })

const predLoading = ref(false)
const fetchPredData = async () => {
  predLoading.value = true
  try {
    const result = await GetPredictionByConditionAndPage(predPageParams.page, predPageParams.limit, predQueryDto)
    if (result.code === 200) {
      const pageInfo = result.data || {}
      predList.value = pageInfo.list || []
      predTotal.value = pageInfo.total || 0
    } else { ElMessage.error(result.message || "查询失败") }
  } catch (error) { ElMessage.error("查询预测模拟数据失败") }
  finally { predLoading.value = false }
}

const searchPredData = () => {
  predQueryDto.predictionTimeStart = predTimeArea.value && predTimeArea.value.length > 0 ? predTimeArea.value[0] : null
  predQueryDto.predictionTimeEnd = predTimeArea.value && predTimeArea.value.length > 0 ? predTimeArea.value[1] : null
  predPageParams.page = 1
  fetchPredData()
}

const resetPredData = () => {
  predTimeArea.value = []
  Object.assign(predQueryDto, { stockName: '', stockCode: '', riseFallPrediction: [], basisType: [], predictionTimeStart: null, predictionTimeEnd: null, predictionSituation: [], predictionResult: [], simulateOperation: [], tradeStatus: [] })
  predPageParams.page = 1
  fetchPredData()
}

const handlePredSelectionChange = (selection) => { predSelectedRows.value = selection }

// 预测模拟添加/修改对话框
const predDialogVisible = ref(false)
const predDialogTitle = ref('添加预测')
const predFormRef = ref(null)
const predFormStep = ref(0)
const predFormData = reactive({ id: null, stockName: '', stockCode: '', riseFallPrediction: null, predictionTime: '', predictionContent: '', predictionBasis: '', basisType: [], predictionSource: null, riseFallResult: null, actualContent: '', resultAnalysis: '', predictionSituation: null, predictionResult: null, simulateOperation: null, tradeShare: null, currentPrice: null, handlingFee: null, tradeStatus: null })

const predFormRules = {
  stockName: [{ required: true, message: '请输入股票名称', trigger: 'blur' }],
  stockCode: [{ required: true, message: '请输入股票代码', trigger: 'blur' }],
  riseFallPrediction: [{ required: true, message: '请选择涨跌预测', trigger: 'change' }]
}

// AI智能预测
const aiPredictLoading = ref(false)
const aiPredict = async () => {
  if (!predFormData.stockName || !predFormData.stockCode) { ElMessage.warning("请先填写股票名称和股票代码"); return }
  aiPredictLoading.value = true
  try {
    const result = await AiPredict({ stockName: predFormData.stockName, stockCode: predFormData.stockCode })
    if (result.code === 200 && result.data) {
      if (result.data.riseFallPrediction) predFormData.riseFallPrediction = result.data.riseFallPrediction
      if (result.data.basisType) { predFormData.basisType = result.data.basisType.split(',').filter(v => v).map(v => Number(v)) }
      if (result.data.predictionContent) predFormData.predictionContent = result.data.predictionContent
      if (result.data.predictionBasis) predFormData.predictionBasis = result.data.predictionBasis
      const now = new Date()
      const tomorrow = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1)
      const pad = (n) => String(n).padStart(2, '0')
      predFormData.predictionTime = `${tomorrow.getFullYear()}-${pad(tomorrow.getMonth() + 1)}-${pad(tomorrow.getDate())} 15:00:00`
      predFormData.predictionSource = 2
      ElMessage.success("AI智能预测已生成，可编辑修改")
    } else { ElMessage.error(result.message || "AI预测失败") }
  } catch (e) { ElMessage.error("AI预测失败：" + e.message) }
  finally { aiPredictLoading.value = false }
}

const addPrediction = () => {
  predDialogTitle.value = '添加预测'
  predFormStep.value = 0
  if (predFormRef.value) { predFormRef.value.resetFields() }
  Object.assign(predFormData, { id: null, stockName: '', stockCode: '', riseFallPrediction: null, predictionTime: '', predictionContent: '', predictionBasis: '', basisType: [], predictionSource: null, riseFallResult: null, actualContent: '', resultAnalysis: '', predictionSituation: null, predictionResult: null, simulateOperation: null, tradeShare: null, currentPrice: null, handlingFee: null, tradeStatus: null })
  predDialogVisible.value = true
}

const editPrediction = (row) => {
  predDialogTitle.value = '编辑预测'
  predFormStep.value = 0
  if (predFormRef.value) { predFormRef.value.resetFields() }
  Object.assign(predFormData, row)
  if (predFormData.basisType && typeof predFormData.basisType === 'string') {
    predFormData.basisType = predFormData.basisType.split(',').filter(v => v).map(v => Number(v))
  } else if (!predFormData.basisType) { predFormData.basisType = [] }
  predDialogVisible.value = true
}

const submitPrediction = async () => {
  if (!predFormRef.value) return
  try { const valid = await predFormRef.value.validate(); if (!valid) return } catch (error) { return }
  try {
    const submitData = { ...predFormData }
    if (Array.isArray(submitData.basisType)) { submitData.basisType = submitData.basisType.join(',') }
    const result = await SavePrediction(submitData)
    if (result.code === 200) { ElMessage.success(predFormData.id ? '编辑成功' : '添加成功'); predDialogVisible.value = false; fetchPredData(); fetchLedgerData() }
    else { ElMessage.error(result.message || "保存失败") }
  } catch (error) { ElMessage.error('保存失败') }
}

const deletePrediction = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该预测记录吗？', '警告', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    const result = await DeletePredictionById(row.id)
    if (result.code === 200) { ElMessage.success('删除成功'); fetchPredData(); fetchLedgerData() }
    else { ElMessage.error(result.message || "删除失败") }
  } catch (error) { if (error !== 'cancel') { ElMessage.error('删除失败') } }
}

const deletePredAll = async () => {
  if (!predSelectedRows.value || predSelectedRows.value.length === 0) { ElMessage.warning('请先选择要删除的记录'); return }
  try {
    await ElMessageBox.confirm('确定要批量删除选中的 ' + predSelectedRows.value.length + ' 条记录吗？', '警告', { type: 'warning' })
    const ids = predSelectedRows.value.map(row => row.id)
    const result = await DeleteAllPredictionByIds(ids)
    if (result.code === 200) { ElMessage.success('批量删除成功'); fetchPredData(); fetchLedgerData(); predTable.value.clearSelection(); predSelectedRows.value = [] }
    else { ElMessage.error(result.message || '批量删除失败') }
  } catch (error) { if (error !== 'cancel') { ElMessage.error('批量删除失败') } }
}

// 预测详情查看
const predDetailVisible = ref(false)
const predDetailData = ref(null)
const viewPredDetail = (row) => { predDetailData.value = { ...row }; predDetailVisible.value = true }

// ==================== 模拟台账 ====================
const ledgerList = ref([])
const ledgerLoading = ref(false)
const fetchLedgerData = async () => {
  ledgerLoading.value = true
  try {
    const result = await GetSimulateLedgerList()
    if (result.code === 200) { ledgerList.value = result.data || [] }
  } catch (error) { console.error('台账数据加载失败:', error) }
  finally { ledgerLoading.value = false }
}

const initAccount = async () => {
  try {
    await ElMessageBox.confirm('确定要初始化模拟账户吗？将删除所有台账数据后创建一条模拟账户资产记录（初始金额10万）', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await DeleteAllSimulateLedger()
    await SaveSimulateLedger({ assetName: '模拟账户资产', assetCode: 'SIM_ACCOUNT', assetType: 1, assetAmount: 100000, assetQuantity: 0 })
    ElMessage.success('初始化成功')
    fetchLedgerData()
  } catch (error) { if (error !== 'cancel') { ElMessage.error('初始化失败') } }
}

const ledgerDialogVisible = ref(false)
const ledgerDialogTitle = ref('添加台账')
const ledgerFormRef = ref(null)
const ledgerFormData = reactive({ id: null, assetName: '', assetCode: '', assetType: null, assetAmount: null, assetQuantity: null })
const ledgerFormRules = {
  assetName: [{ required: true, message: '请输入资产名称', trigger: 'blur' }],
  assetType: [{ required: true, message: '请选择资产类型', trigger: 'change' }],
  assetAmount: [{ required: true, message: '请输入资产金额', trigger: 'blur' }]
}

const addLedger = () => {
  ledgerDialogTitle.value = '添加台账'
  if (ledgerFormRef.value) { ledgerFormRef.value.resetFields() }
  Object.assign(ledgerFormData, { id: null, assetName: '', assetCode: '', assetType: null, assetAmount: null, assetQuantity: null })
  ledgerDialogVisible.value = true
}

const editLedger = (row) => {
  ledgerDialogTitle.value = '编辑台账'
  if (ledgerFormRef.value) { ledgerFormRef.value.resetFields() }
  Object.assign(ledgerFormData, row)
  ledgerDialogVisible.value = true
}

const submitLedger = async () => {
  if (!ledgerFormRef.value) return
  try { const valid = await ledgerFormRef.value.validate(); if (!valid) return } catch (error) { return }
  try {
    const result = await SaveSimulateLedger(ledgerFormData)
    if (result.code === 200) { ElMessage.success(ledgerFormData.id ? '编辑成功' : '添加成功'); ledgerDialogVisible.value = false; fetchLedgerData() }
    else { ElMessage.error(result.message || "保存失败") }
  } catch (error) { ElMessage.error('保存失败') }
}

const deleteLedger = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该台账记录吗？', '警告', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    const result = await DeleteSimulateLedgerById(row.id)
    if (result.code === 200) { ElMessage.success('删除成功'); fetchLedgerData() }
    else { ElMessage.error(result.message || "删除失败") }
  } catch (error) { if (error !== 'cancel') { ElMessage.error('删除失败') } }
}

// 台账计算属性
const mainAccountAmount = computed(() => {
  const mainAccount = ledgerList.value.find(item => item.assetType === 1)
  return mainAccount ? Number(mainAccount.assetAmount || 0) : 0
})
const positionList = computed(() => { return ledgerList.value.filter(item => item.assetType !== 1 && (item.assetQuantity || 0) > 0) })
const positionAmount = computed(() => { return positionList.value.reduce((sum, item) => sum + Number(item.assetAmount || 0), 0) })
const positionCount = computed(() => { return positionList.value.length })
const totalAssetAmount = computed(() => { return mainAccountAmount.value + positionAmount.value })

//--------------------钩子函数-------------------------
onMounted(() => {
  Promise.all([
    loadDict('t_trial_prediction_rise_fall', riseFallOptions),
    loadDict('t_trial_prediction_basis_type', basisTypeOptions),
    loadDict('t_trial_prediction_source', predictionSourceOptions),
    loadDict('t_trial_prediction_situation', predictionSituationOptions),
    loadDict('t_trial_prediction_result', predictionResultOptions),
    loadDict('t_trial_simulate_operation', simulateOperationOptions),
    loadDict('t_trial_simulate_trade_status', simulateTradeStatusOptions),
    loadDict('t_trial_simulate_asset_type', simulateAssetTypeOptions)
  ]).catch(err => console.error('字典加载失败:', err))

  fetchLedgerData()
  fetchPredData()
})
</script>

<style scoped>
.prediction-div { padding: 15px; border: 1px solid #ebeef5; border-top: none; border-radius: 0 0 4px 4px; background-color: rgba(255, 255, 255, 0.8); }
.account-overview { display: flex; justify-content: space-between; align-items: center; padding: 16px 24px; background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%); border-radius: 8px; color: #fff; margin-bottom: 12px; box-shadow: 0 4px 12px rgba(15, 52, 96, 0.3); }
.account-overview-left { display: flex; align-items: center; gap: 16px; }
.account-icon { width: 52px; height: 52px; border-radius: 50%; background: linear-gradient(135deg, #e94560, #c23616); display: flex; align-items: center; justify-content: center; box-shadow: 0 2px 8px rgba(233, 69, 96, 0.4); }
.account-label { font-size: 13px; color: rgba(255, 255, 255, 0.7); margin-bottom: 4px; }
.account-total-amount { font-size: 28px; font-weight: 700; font-family: 'DIN Alternate', 'Helvetica Neue', monospace; letter-spacing: 1px; color: #e94560; text-shadow: 0 0 10px rgba(233, 69, 96, 0.3); }
.account-overview-right { display: flex; align-items: center; gap: 0; }
.account-stat-item { display: flex; flex-direction: column; align-items: center; padding: 0 20px; }
.account-stat-label { font-size: 12px; color: rgba(255, 255, 255, 0.6); margin-bottom: 4px; }
.account-stat-value { font-size: 16px; font-weight: 600; font-family: 'DIN Alternate', 'Helvetica Neue', monospace; }
.account-stat-value.available { color: #f5c542; }
.account-stat-value.position { color: #4ecdc4; }
.account-stat-value.count { color: #a8e6cf; }
.account-stat-divider { width: 1px; height: 36px; background: rgba(255, 255, 255, 0.15); }
.position-cards { display: flex; flex-wrap: wrap; gap: 12px; margin-bottom: 4px; }
.position-card { flex: 0 0 calc(25% - 9px); min-width: 220px; background: linear-gradient(145deg, #ffffff 0%, #f8f9fa 100%); border: 1px solid #e0e6ed; border-radius: 8px; padding: 12px 16px; transition: all 0.3s ease; box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06); }
.position-card:hover { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12); border-color: #409eff; }
.position-card-header { display: flex; align-items: center; margin-bottom: 10px; padding-bottom: 8px; border-bottom: 1px solid #f0f2f5; }
.position-stock-name { font-size: 15px; font-weight: 700; color: #303133; margin-right: 8px; }
.position-stock-code { font-size: 12px; color: #909399; font-family: 'Courier New', monospace; }
.position-card-actions { margin-left: auto; }
.position-card-body { display: flex; gap: 24px; }
.position-card-col { flex: 1; }
.position-card-label { font-size: 11px; color: #909399; margin-bottom: 4px; }
.position-card-value { font-size: 18px; font-weight: 600; color: #303133; font-family: 'DIN Alternate', 'Helvetica Neue', monospace; }
.position-card-value.highlight { color: #e6a23c; }
.position-card-unit { font-size: 12px; color: #909399; font-weight: 400; margin-left: 2px; }
.position-empty { padding: 20px; text-align: center; background: rgba(255, 255, 255, 0.6); border-radius: 8px; border: 1px dashed #dcdfe6; margin-bottom: 4px; }
.search-div { margin-bottom: 10px; padding: 15px; border: 1px solid #ebeef5; border-radius: 4px; background-color: rgba(255, 255, 255, 0.8); }
.search-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; padding-bottom: 10px; border-bottom: 1px solid #ebeef5; }
.search-title { font-size: 14px; font-weight: 600; color: #303133; }
.tools-div { margin-bottom: 10px; padding: 10px; border: 1px solid #ebeef5; border-radius: 4px; background-color: rgba(255, 255, 255, 0.8); }
.beautified-tools .el-button { border-radius: 8px; padding: 10px 20px; font-size: 13px; font-weight: 600; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }
.beautified-tools .el-button--success { background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%); border: none; }
.beautified-tools .el-button--danger { background: linear-gradient(135deg, #F56C6C 0%, #f78989 100%); border: none; }
:deep(.el-table .el-button) { border-radius: 6px; padding: 6px 12px; font-size: 12px; font-weight: 600; border: none; }
:deep(.el-table .el-button--primary) { background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%); }
:deep(.el-table .el-button--danger) { background: linear-gradient(135deg, #F56C6C 0%, #f78989 100%); }
:deep(.el-table .el-button--info) { background: linear-gradient(135deg, #909399 0%, #a6a9ad 100%); }
.dialog-footer .el-button { border-radius: 8px; padding: 10px 24px; font-size: 14px; font-weight: 600; }
.dialog-footer .el-button--primary { background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%); border: none; }
:deep(.custom-dialog), :deep(.enhanced-dialog) { border-radius: 16px !important; overflow: hidden !important; box-shadow: 0 16px 48px rgba(102, 126, 234, 0.25) !important; }
:deep(.enhanced-dialog .el-dialog__header) { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important; border-radius: 16px 16px 0 0 !important; padding: 24px 28px !important; margin: 0 !important; width: 100% !important; box-sizing: border-box !important; }
:deep(.enhanced-dialog .el-dialog__title) { color: white !important; font-weight: 700 !important; font-size: 22px !important; }
:deep(.enhanced-dialog .el-dialog__headerbtn .el-dialog__close) { color: white !important; font-size: 22px !important; }
:deep(.enhanced-dialog .el-dialog__body) { padding: 28px !important; background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%) !important; max-height: 60vh !important; overflow-y: auto !important; }
:deep(.enhanced-dialog .el-dialog__footer) { padding: 20px 28px !important; background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%) !important; border-top: 2px solid rgba(102, 126, 234, 0.1) !important; }
:deep(.el-pagination) { justify-content: center; }
.detail-section { margin-bottom: 24px; }
.detail-section:last-child { margin-bottom: 0; }
.detail-section-title { font-size: 16px; font-weight: 700; color: #303133; margin-bottom: 12px; padding-left: 10px; border-left: 4px solid #667eea; line-height: 1; }
.detail-value { font-size: 14px; color: #606266; }
.detail-value.highlight { color: #667eea; font-weight: 600; font-family: 'Courier New', monospace; }
.detail-text-block { font-size: 14px; color: #303133; line-height: 1.8; white-space: pre-wrap; word-break: break-all; }
.stat-card { background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%); border: 1px solid #ebeef5; border-radius: 12px; padding: 20px 16px; text-align: center; transition: all 0.3s ease; }
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08); }
.stat-value { font-size: 28px; font-weight: 700; line-height: 1.2; margin-bottom: 8px; }
.stat-value.primary { color: #409EFF; }
.stat-value.danger { color: #F56C6C; }
.stat-value.success { color: #67C23A; }
.stat-value.warning { color: #E6A23C; }
.stat-label { font-size: 13px; color: #909399; font-weight: 500; }
</style>