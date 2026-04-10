<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>
        <el-icon><Notebook /></el-icon>
        资产记账管理
      </h1>
    </div>

    <!-- 统计模块 -->
    <div class="statistics-div">
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="stat-card total-card">
            <div class="stat-icon">
              <el-icon :size="40"><Wallet /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">总计</div>
              <div class="stat-value">{{ statistics.total.toFixed(2) }} 元</div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-card income-card">
            <div class="stat-icon">
              <el-icon :size="40"><TrendCharts /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">收入</div>
              <div class="stat-value">+{{ statistics.income.toFixed(2) }} 元</div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-card expense-card">
            <div class="stat-icon">
              <el-icon :size="40"><ShoppingCart /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">支出</div>
              <div class="stat-value">-{{ statistics.expense.toFixed(2) }} 元</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

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
            <el-form-item label="账单编号">
              <el-input v-model="queryDto.invoiceNo" style="width: 100%" clearable placeholder="请输入账单编号" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="记账账户">
              <el-input
                v-model="queryDto.assetLedgerName"
                style="width: 100%"
                clearable
                placeholder="请选择记账账户"
                readonly
                @click="openAssetLedgerDialog('query')"
              >
                <template #suffix>
                  <el-icon><ArrowDown /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="记账时间">
              <el-date-picker
                v-model="recordTimeArea"
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
                <el-form-item label="记账金额(>)">
                  <el-input-number v-model="queryDto.amount" style="width: 100%" :min="0" :precision="2" placeholder="请输入最小金额" />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="账单类型">
                  <el-select v-model="queryDto.invoiceType" style="width: 100%" clearable placeholder="请选择账单类型" multiple>
                    <el-option v-for="item in billTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="结清状态">
                  <el-select v-model="queryDto.settlementStatus" style="width: 100%" clearable placeholder="请选择结清状态">
                    <el-option v-for="item in settlementStatusOptions" :key="item.value" :label="item.text" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="数据来源">
                  <el-select v-model="queryDto.dataSource" style="width: 100%" clearable placeholder="请选择数据来源" multiple>
                    <el-option v-for="item in dataSourceOptions" :key="item.value" :label="item.text" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="6">
                <el-form-item label="收支类型">
                  <el-select v-model="queryDto.transactionType" style="width: 100%" clearable placeholder="请选择收支类型" @change="handleQueryTransactionTypeChange">
                    <el-option v-for="item in incomeExpenseTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6" v-if="queryDto.transactionType === 1">
                <el-form-item label="收益类型">
                  <el-select v-model="queryDto.incomeType" style="width: 100%" clearable placeholder="请选择收益类型" multiple>
                    <el-option v-for="item in incomeTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6" v-else-if="queryDto.transactionType === 2">
                <el-form-item label="支出类型">
                  <el-select v-model="queryDto.spendingType" style="width: 100%" clearable placeholder="请选择支出类型" multiple>
                    <el-option v-for="item in expenseTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
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
    </div>

    <!-- 操作按钮区域 -->
    <div class="tools-div beautified-tools" style="text-align: right;">
      <el-button type="success" size="small" @click="addRecord">
        <el-icon><DocumentAdd /></el-icon>
        添加记账
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
      <el-table-column label="操作" align="center" fixed="left" width="180" #default="scope">
        <el-button type="primary" size="small" @click="editRecord(scope.row)">
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
        <el-button type="danger" size="small" @click="deleteRecord(scope.row)">
          <el-icon><Delete /></el-icon>
          删除
        </el-button>
      </el-table-column>
      <el-table-column prop="invoiceNo" label="账单编号" align="center" width="150" show-overflow-tooltip />
      <el-table-column prop="invoiceAction" label="账单行为" align="center" width="120" show-overflow-tooltip />
      <el-table-column prop="recordTime" label="记账时间" align="center" width="160" />
      <el-table-column prop="amount" label="金额 (元)" align="center" width="120">
        <template #default="scope">
          <span :style="{ color: scope.row.transactionType === 1 ? '#67C23A' : '#F56C6C' }">
            {{ scope.row.transactionType === 1 ? '+' : '-' }}{{ scope.row.amount }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="assetName" label="记账账户" align="center" width="120" show-overflow-tooltip />
      <el-table-column prop="transactionType" label="收支类型" align="center" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.transactionType === 1 ? 'success' : 'danger'" size="small">
            {{ getDisplayText(scope.row.transactionType, incomeExpenseTypeOptions) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="invoiceType" label="账单类型" align="center" width="120">
        <template #default="scope">
          {{ getDisplayText(scope.row.invoiceType, billTypeOptions) }}
        </template>
      </el-table-column>
      <el-table-column prop="spendingType" label="支出类型" align="center" width="100">
        <template #default="scope">
          {{ getDisplayText(scope.row.spendingType, expenseTypeOptions) }}
        </template>
      </el-table-column>
      <el-table-column prop="settlementStatus" label="结清状态" align="center" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.settlementStatus === 1 ? 'success' : 'warning'" size="small">
            {{ getDisplayText(scope.row.settlementStatus, settlementStatusOptions) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="incomeType" label="收益类型" align="center" width="100">
        <template #default="scope">
          {{ getDisplayText(scope.row.incomeType, incomeTypeOptions) }}
        </template>
      </el-table-column>
      <el-table-column prop="updateBy" label="记账人" align="center" width="100" />
      <el-table-column prop="dataSource" label="数据来源" align="center" width="100">
        <template #default="scope">
          {{ getDisplayText(scope.row.dataSource, dataSourceOptions) }}
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" align="center" min-width="150" show-overflow-tooltip />
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

    <!-- 添加/修改对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="60%"
      class="custom-dialog enhanced-dialog"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" label-width="120px" :rules="formRules" ref="formRef">
        <el-row :gutter="20">
          <!-- 第一行 -->
          <el-col :span="12">
            <el-form-item label="账单行为" prop="invoiceAction">
              <el-input v-model="formData.invoiceAction" placeholder="请输入账单行为" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="记账时间" prop="recordTime">
              <el-date-picker
                v-model="formData.recordTime"
                type="datetime"
                placeholder="请选择记账时间"
                style="width: 100%"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <!-- 第二行 -->
          <el-col :span="12">
            <el-form-item label="金额" prop="amount">
              <el-input-number v-model="formData.amount" :precision="2" :min="0" style="width: 100%" placeholder="请输入金额" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="记账账户" prop="assetName">
              <el-input
                v-model="formData.assetName"
                placeholder="请选择记账账户"
                readonly
                @click="openAssetLedgerDialog('form')"
                style="cursor: pointer;"
              >
                <template #suffix>
                  <el-icon><ArrowDown /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <!-- 第四行：收支类型 -->
          <el-col :span="12">
            <el-form-item label="收支类型" prop="transactionType">
              <el-select v-model="formData.transactionType" style="width: 100%" placeholder="请选择收支类型" @change="handleIncomeExpenseChange">
                <el-option v-for="item in incomeExpenseTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="formData.transactionType === 1">
            <el-form-item label="收益类型" prop="incomeType">
              <el-select v-model="formData.incomeType" style="width: 100%" placeholder="请选择收益类型">
                <el-option v-for="item in incomeTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-else-if="formData.transactionType === 2">
            <el-form-item label="支出类型" prop="spendingType">
              <el-select v-model="formData.spendingType" style="width: 100%" placeholder="请选择支出类型">
                <el-option v-for="item in expenseTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <!-- 第五行 -->
          <el-col :span="12">
            <el-form-item label="账单类型" prop="invoiceType">
              <el-select v-model="formData.invoiceType" style="width: 100%" placeholder="请选择账单类型">
                <el-option v-for="item in billTypeOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结清状态" prop="settlementStatus">
              <el-select v-model="formData.settlementStatus" style="width: 100%" placeholder="请选择结清状态">
                <el-option v-for="item in settlementStatusOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <!-- 第六行 -->
          <el-col :span="12">
            <el-form-item label="数据来源" prop="dataSource">
              <el-select v-model="formData.dataSource" style="width: 100%" placeholder="请选择数据来源">
                <el-option v-for="item in dataSourceOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <!-- 第七行：备注 -->
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入备注信息" />
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

    <!-- 选择资产台账对话框 -->
    <el-dialog
      v-model="assetLedgerDialogVisible"
      title="选择资产台账"
      width="50%"
      class="asset-ledger-dialog"
      :close-on-click-modal="false"
    >
      <!-- 资产台账查询条件 -->
      <div class="asset-ledger-search">
        <el-form label-width="100px" size="small">
          <el-row :gutter="10">
            <el-col :span="12">
              <el-form-item label="资产名称">
                <el-input
                    v-model="assetLedgerQuery.assetName"
                    style="width: 100%"
                    clearable
                    placeholder="请输入资产名称"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="资产编号">
                <el-input
                    v-model="assetLedgerQuery.assetNo"
                    style="width: 100%"
                    clearable
                    placeholder="请输入资产编号"
                ></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="10">
            <el-col :span="12">
              <el-form-item label="资产类型">
                <el-select
                    v-model="assetLedgerQuery.assetType"
                    placeholder="请选择"
                    style="width: 100%"
                    clearable
                    @change="handleAssetLedgerQueryTypeChange"
                >
                  <el-option
                      v-for="item in assetLedgerTypeItem"
                      :key="item.value"
                      :label="item.text"
                      :value="item.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="资产子类">
                <el-select
                    v-model="assetLedgerQuery.assetSubType"
                    placeholder="请先选择资产类型"
                    style="width: 100%"
                    clearable
                    multiple
                    :disabled="!assetLedgerQuery.assetType"
                >
                  <el-option
                      v-for="item in assetLedgerSubTypeItem"
                      :key="item.value"
                      :label="item.text"
                      :value="item.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="10">
            <el-col :span="12">
              <el-form-item label="资产状态">
                <el-select
                    v-model="assetLedgerQuery.assetStatus"
                    multiple
                    placeholder="请选择"
                    style="width: 100%"
                    clearable
                >
                  <el-option
                      v-for="item in assetLedgerStatusItem"
                      :key="item.value"
                      :label="item.text"
                      :value="item.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24" style="text-align: right;">
              <el-button
                  type="primary"
                  size="small"
                  @click="handleAssetLedgerSearch"
                  class="beautified-search-btn"
              >
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button
                  size="small"
                  @click="resetAssetLedgerQuery"
                  class="beautified-reset-btn"
              >
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <!-- 资产台账列表 -->
      <el-table
        :data="assetLedgerList"
        border
        stripe
        height="280px"
        @row-click="selectAssetLedger"
        style="cursor: pointer; width: 100%;"
      >
        <el-table-column prop="assetName" label="资产名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="assetCode" label="资产官方标识" min-width="120" show-overflow-tooltip />
        <el-table-column prop="assetNo" label="资产编号" width="140" show-overflow-tooltip />
        <el-table-column prop="assetType" label="资产类型" width="90">
          <template #default="scope">
            {{ getDisplayText(scope.row.assetType, assetLedgerTypeItem) }}
          </template>
        </el-table-column>
        <el-table-column prop="assetSubType" label="资产子类" width="120" show-overflow-tooltip>
          <template #default="scope">
            {{ scope.row._assetSubTypeText || scope.row.assetSubType || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="assetStatus" label="资产状态" width="90">
          <template #default="scope">
            {{ getDisplayText(scope.row.assetStatus, assetLedgerStatusItem) }}
          </template>
        </el-table-column>
        <el-table-column prop="assetOwnerName" label="资产所属人" width="100" show-overflow-tooltip />
      </el-table>

      <!-- 分页 -->
      <div style="margin-top: 10px; text-align: right;">
        <el-pagination
            background
            layout="total, sizes, prev, pager, next"
            :total="assetLedgerTotal"
            :page-size="assetLedgerPageParams.limit"
            :current-page="assetLedgerPageParams.page"
            :page-sizes="[10, 20, 50, 100]"
            @size-change="handleAssetLedgerSizeChange"
            @current-change="handleAssetLedgerCurrentChange"
        />
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="assetLedgerDialogVisible = false">取消</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { GetKeyAndValueByType } from "@/api/sysDict"
import { GetAssetAccountingByConditionAndPage, SaveAssetAccounting, DeleteAssetAccountingById, DeleteAllAssetAccountingByIds } from "@/api/assetAccounting"
import { GetAssetLedgerByConditionAndPage } from "@/api/assetControl"
import { getTodayTimeRange } from "@/utils/common"
import { useExport } from "@/components/Export/hooks/useExport"
import ExportDialog from '@/components/Export/ExportDialog.vue'

//--------------------钩子函数-------------------------
onMounted(() => {
  //1.加载数据字典
  getTransactionTypeItem()
  getInvoiceTypeItem()
  getExpenseTypeItem()
  getSettlementStatusItem()
  getIncomeTypeItem()
  getDataSourceItem()

  //2.查询条件设置默认时间为今天
  const [startOfDay, endOfDay] = getTodayTimeRange()
  recordTimeArea.value[0] = startOfDay
  recordTimeArea.value[1] = endOfDay
  queryDto.recordTimeStart = startOfDay
  queryDto.recordTimeEnd = endOfDay

  //3.调用查询数据接口
  fetchData()
});

//------------------------公共方法---------------------------
// 通用方法：根据值和映射表获取中文文本
const getDisplayText = (value, mappingArray) => {
  if (!value && value !== 0) return '-'
  const foundItem = mappingArray.find(item => item.value === value)
  return foundItem ? foundItem.text : value
}

const formatMoney = (v) => Number(v || 0).toLocaleString()

// ==================== 数据字典 ====================
// 收支类型选项
const incomeExpenseTypeOptions = ref([])
// 获取收支类型
const getTransactionTypeItem = async () => {
  const result = await GetKeyAndValueByType("transaction_type")
  incomeExpenseTypeOptions.value = result.data
}

// 账单类型选项
const billTypeOptions = ref([])
// 获取账单类型
const getInvoiceTypeItem = async () => {
  const result = await GetKeyAndValueByType("invoice_type")
  billTypeOptions.value = result.data
}

// 支出类型选项
const expenseTypeOptions = ref([])
// 获取支出类型
const getExpenseTypeItem = async () => {
  const result = await GetKeyAndValueByType("spending_type")
  expenseTypeOptions.value = result.data
}

// 结清状态选项
const settlementStatusOptions = ref([])
// 获取结清状态
const getSettlementStatusItem = async () => {
  const result = await GetKeyAndValueByType("settlement_status")
  settlementStatusOptions.value = result.data
}

// 收益类型选项
const incomeTypeOptions = ref([])
// 获取收益类型
const getIncomeTypeItem = async () => {
  const result = await GetKeyAndValueByType("income_type")
  incomeTypeOptions.value = result.data
}

// 数据来源选项
const dataSourceOptions = ref([])
// 获取数据来源
const getDataSourceItem = async () => {
  const result = await GetKeyAndValueByType("asset_transaction_data_source")
  dataSourceOptions.value = result.data
}
//=========================================================

// ==================== 列表数据 ====================
const list = ref([])
const total = ref(0)
const pageParams = reactive({ page: 1, limit: 10 })
const searchExpanded = ref(false)
const recordTimeArea = ref([])
const queryDto = reactive({
  invoiceNo: '',
  recordTimeStart: null,
  recordTimeEnd: null,
  amount: null,
  assetLedgerId: null,
  assetLedgerName: '',
  transactionType: '',
  incomeType: [],
  spendingType: [],
  invoiceType: [],
  settlementStatus: '',
  dataSource: []
})

// ==================== 统计数据 ====================
const statistics = ref({
  total: 0,
  income: 0,
  expense: 0
})

// 获取数据
const fetchData = async () => {
  try {
    const result = await GetAssetAccountingByConditionAndPage(pageParams.page, pageParams.limit, queryDto)
    if (result.code === 200) {
      // 后端返回的数据格式：{ dataList: PageInfo, groupList: [{ transactionType, transactionAmount }] }
      const pageInfo = result.data.dataList || {}

      list.value = pageInfo.list || []
      total.value = pageInfo.total || 0

      // 处理分组统计数据
      const groupList = result.data.groupList || []
      let income = 0
      let expense = 0

      if (groupList.length > 0) {
        groupList.forEach(item => {
          const amount = Number(item.transactionAmount || 0)
          if (item.transactionType === '收入') {
            income += amount
          } else if (item.transactionType === '支出') {
            expense += amount
          }
        })
      }

      statistics.value = {
        total: income - expense,
        income: income,
        expense: expense
      }
    } else {
      ElMessage.error(result.message || "查询失败")
    }
  } catch (error) {
    ElMessage.error("查询失败")
  }
}

// 搜索
const searchData = () => {
  // 时间参数赋值
  queryDto.recordTimeStart = recordTimeArea.value[0]
  queryDto.recordTimeEnd = recordTimeArea.value[1]
  
  pageParams.page = 1
  fetchData()
}

// 切换搜索条件展开/收起
const toggleSearchExpand = () => {
  searchExpanded.value = !searchExpanded.value
}

// 查询条件收支类型改变事件
const handleQueryTransactionTypeChange = (value) => {
  if (value === 1) {
    queryDto.spendingType = []
  } else if (value === 2) {
    queryDto.incomeType = []
  }
}

// 重置
const resetData = () => {
  // 时间范围重置
  recordTimeArea.value = []
  
  // 参数重置
  Object.assign(queryDto, {
    invoiceNo: '',
    recordTimeStart: null,
    recordTimeEnd: null,
    amount: null,
    assetLedgerId: null,
    assetLedgerName: '',
    transactionType: '',
    incomeType: [],
    spendingType: [],
    invoiceType: [],
    settlementStatus: '',
    dataSource: []
  })
  pageParams.page = 1
  fetchData()
}

// ==================== 添加/修改 ====================
const dialogVisible = ref(false)
const dialogTitle = ref('添加记账')
const formRef = ref(null)
const formData = reactive({
  id: null,
  invoiceNo: '',
  invoiceAction: '',
  recordTime: '',
  amount: 0,
  assetName: '',
  assetLedgerId: null, // 资产台账id
  transactionType: '',
  invoiceType: '',
  spendingType: '',
  settlementStatus: '',
  incomeType: '',
  updateBy: '',
  dataSource: '',
  remark: ''
})

// 表单验证规则
const formRules = {
  invoiceAction: [{ required: true, message: '请输入账单行为', trigger: 'blur' }],
  recordTime: [{ required: true, message: '请选择记账时间', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  assetName: [{ required: true, message: '请选择记账账户', trigger: 'change' }],
  transactionType: [{ required: true, message: '请选择收支类型', trigger: 'change' }],
  invoiceType: [{ required: true, message: '请选择账单类型', trigger: 'change' }],
  settlementStatus: [{ required: true, message: '请选择结清状态', trigger: 'change' }]
}

// 添加记录
const addRecord = () => {
  dialogTitle.value = '添加记账'
  // 清空表单校验
  if (formRef.value) {
    formRef.value.resetFields()
  }

  Object.assign(formData, {
    id: null,
    invoiceNo: '',
    invoiceAction: '',
    recordTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
    amount: 0,
    assetName: '',
    assetLedgerId: null,
    transactionType: '',
    invoiceType: '',
    spendingType: '',
    settlementStatus: '',
    incomeType: '',
    updateBy: '',
    dataSource: 1,
    remark: ''
  })
  dialogVisible.value = true
}

// 编辑记录
const editRecord = (row) => {
  dialogTitle.value = '编辑记账'
  // 清空表单校验
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 收支类型改变事件
const handleIncomeExpenseChange = (value) => {
  if (value === 1) {
    formData.spendingType = ''
  } else if (value === 2) {
    formData.incomeType = ''
  }
}

// 提交表单
const submit = async () => {
  if (!formRef.value) return
  
  try {
    const valid = await formRef.value.validate()
    if (!valid) return
  } catch (error) {
    return
  }
  
  try {
    const result = await SaveAssetAccounting(formData)
    if (result.code === 200) {
      ElMessage.success(formData.id ? '编辑成功' : '添加成功')
      dialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(result.message || "保存失败")
    }
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// ==================== 删除 ====================
const deleteRecord = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该记账记录吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    
    const result = await DeleteAssetAccountingById(row.id)
    if (result.code === 200) {
      ElMessage.success('删除成功')
      fetchData()
    } else {
      ElMessage.error(result.message || "删除失败")
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// ==================== 批量删除 ====================
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
      `确定要批量删除选中的 ${selectedRows.value.length} 条记录吗？`,
      '警告',
      { type: 'warning' }
    )
    
    const ids = selectedRows.value.map(row => row.id)
    const result = await DeleteAllAssetAccountingByIds(ids)
    if (result.code === 200) {
      ElMessage.success('批量删除成功')
      fetchData()
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

// ==================== 导出功能 ====================
// 导出列配置
const exportColumns = [
  { key: 'invoiceNo', label: '账单编号', width: 20 },
  { key: 'invoiceAction', label: '账单行为', width: 15 },
  { key: 'recordTime', label: '记账时间', width: 20 },
  { key: 'amount', label: '金额', width: 15 },
  { key: 'assetName', label: '记账账户', width: 15 },
  { key: 'transactionType', label: '收支类型', width: 12 },
  { key: 'invoiceType', label: '账单类型', width: 15 },
  { key: 'spendingType', label: '支出类型', width: 12 },
  { key: 'settlementStatus', label: '结清状态', width: 12 },
  { key: 'incomeType', label: '收益类型', width: 12 },
  { key: 'updateBy', label: '记账人', width: 12 },
  { key: 'dataSource', label: '数据来源', width: 15 },
  { key: 'remark', label: '备注', width: 30 }
]

// 数据格式化函数
const assetAccountingDataFormatter = (item, key, value) => {
  switch (key) {
    case 'transactionType':
      return getDisplayText(value, incomeExpenseTypeOptions.value)
    case 'invoiceType':
      return getDisplayText(value, billTypeOptions.value)
    case 'spendingType':
      return getDisplayText(value, expenseTypeOptions.value)
    case 'incomeType':
      return getDisplayText(value, incomeTypeOptions.value)
    case 'settlementStatus':
      return getDisplayText(value, settlementStatusOptions.value)
    case 'dataSource':
      return getDisplayText(value, dataSourceOptions.value)
    default:
      return value
  }
}

// 获取全部数据的函数
const fetchAllAssetAccountingData = async () => {
  const { data } = await GetAssetAccountingByConditionAndPage(
      1,
      1000000,
      queryDto
  )
  
  const pageInfo = data.dataList || {}
  return pageInfo.list || []
}

// 使用导出Hook
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
  fetchAllData: fetchAllAssetAccountingData,
  dataFormatter: assetAccountingDataFormatter,
  defaultFileName: '资产记账数据',
  sheetName: '资产记账数据'
})

// 包装显示导出对话框的方法
const showExportDialog = () => {
  showExportDialogMethod(list.value, total.value)
}

// ==================== 选择资产台账功能 ====================
// 资产台账选择对话框
const assetLedgerDialogVisible = ref(false)
const assetLedgerList = ref([])
const assetLedgerTotal = ref(0)
const assetLedgerPageParams = reactive({ page: 1, limit: 10 })
const assetLedgerQuery = reactive({
  assetName: '',
  assetNo: '',
  assetType: null,
  assetSubType: [],
  assetStatus: []
})

// 选择来源: 'query' 表示条件查询, 'form' 表示表单选择
const assetLedgerSelectSource = ref('form')

// 资产台账数据字典
const assetLedgerTypeItem = ref([])
const assetLedgerSubTypeItem = ref([])
const assetLedgerStatusItem = ref([])

// 获取资产台账数据字典
const getAssetLedgerTypeItem = async () => {
  const result = await GetKeyAndValueByType("asset_type")
  assetLedgerTypeItem.value = result.data
}

const getAssetLedgerSubTypeItem = async (assetTypeValue) => {
  try {
    if (!assetTypeValue) {
      assetLedgerSubTypeItem.value = []
      return
    }
    const type = `asset_type_${assetTypeValue}`
    const result = await GetKeyAndValueByType(type)
    if (result.code === 200) {
      assetLedgerSubTypeItem.value = result.data
    } else {
      assetLedgerSubTypeItem.value = []
    }
  } catch (error) {
    assetLedgerSubTypeItem.value = []
  }
}

const getAssetLedgerStatusItem = async () => {
  const result = await GetKeyAndValueByType("asset_status")
  assetLedgerStatusItem.value = result.data
}

// 打开资产台账选择对话框
const openAssetLedgerDialog = async (source) => {
  // 设置选择来源
  assetLedgerSelectSource.value = source
  
  // 重置查询条件
  resetAssetLedgerQuery()
  
  assetLedgerDialogVisible.value = true
  
  // 加载数据字典
  await getAssetLedgerTypeItem()
  await getAssetLedgerStatusItem()
  
  // 查询资产台账数据
  fetchAssetLedgerData()
}

// 预加载所有资产子类数据字典并处理数据
const processAssetLedgerSubTypeText = async (dataList) => {
  const allSubTypeData = {}
  // 获取所有不同的资产类型
  const assetTypes = [...new Set(dataList.map(item => item.assetType).filter(Boolean))]

  // 并行加载所有资产类型对应的子类数据
  await Promise.all(assetTypes.map(async (assetType) => {
    const type = `asset_type_${assetType}`
    try {
      const result = await GetKeyAndValueByType(type)
      if (result.code === 200 && result.data) {
        allSubTypeData[assetType] = result.data
      }
    } catch (error) {
      console.error(`加载资产类型${assetType}的子类数据失败:`, error)
    }
  }))

  // 为每条数据添加子类文本
  dataList.forEach(item => {
    if (item.assetSubType && item.assetType && allSubTypeData[item.assetType]) {
      const foundItem = allSubTypeData[item.assetType].find(subItem => subItem.value === item.assetSubType)
      item._assetSubTypeText = foundItem ? foundItem.text : item.assetSubType
    }
  })

  return dataList
}

// 查询资产台账数据
const fetchAssetLedgerData = async () => {
  try {
    const result = await GetAssetLedgerByConditionAndPage(
      assetLedgerPageParams.page,
      assetLedgerPageParams.limit,
      assetLedgerQuery
    )
    if (result.code === 200) {
      const records = result.data.records || []
      // 预处理资产子类文本
      await processAssetLedgerSubTypeText(records)
      assetLedgerList.value = records
      assetLedgerTotal.value = result.data.total || 0
    } else {
      ElMessage.error(result.message || "查询资产台账失败")
    }
  } catch (error) {
    ElMessage.error("查询资产台账失败")
  }
}

// 资产台账查询条件资产类型改变事件
const handleAssetLedgerQueryTypeChange = async (val) => {
  assetLedgerQuery.assetSubType = []
  await getAssetLedgerSubTypeItem(val)
}

// 资产台账搜索
const handleAssetLedgerSearch = () => {
  assetLedgerPageParams.page = 1
  fetchAssetLedgerData()
}

// 资产台账重置
const resetAssetLedgerQuery = () => {
  Object.assign(assetLedgerQuery, {
    assetName: '',
    assetNo: '',
    assetType: null,
    assetSubType: [],
    assetStatus: []
  })
  assetLedgerSubTypeItem.value = []
  assetLedgerPageParams.page = 1
}

// 资产台账分页
const handleAssetLedgerSizeChange = (size) => {
  assetLedgerPageParams.limit = size
  assetLedgerPageParams.page = 1
  fetchAssetLedgerData()
}

const handleAssetLedgerCurrentChange = (page) => {
  assetLedgerPageParams.page = page
  fetchAssetLedgerData()
}

// 选择资产台账
const selectAssetLedger = (row) => {
  // 根据选择来源设置不同的数据
  if (assetLedgerSelectSource.value === 'query') {
    // 条件查询选择:设置资产台账id和账户名称用于查询
    queryDto.assetLedgerId = row.id
    queryDto.assetLedgerName = row.assetName
  } else if (assetLedgerSelectSource.value === 'form') {
    // 表单选择:设置资产台账id和账户名称
    formData.assetLedgerId = row.id
    formData.assetName = row.assetName
  }

  // 关闭对话框
  assetLedgerDialogVisible.value = false
}

</script>

<style scoped>
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

/* 统计模块样式 */
.statistics-div {
  margin-bottom: 15px;
  padding: 15px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 8px;
  background: linear-gradient(135deg, #ffffff 0%, #f5f7fa 100%);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  margin-right: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  border-radius: 50%;
}

.total-card .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.income-card .stat-icon {
  background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%);
  color: white;
}

.expense-card .stat-icon {
  background: linear-gradient(135deg, #F56C6C 0%, #f78989 100%);
  color: white;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
  font-weight: 500;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  font-family: 'Arial', sans-serif;
}

.total-card .stat-value {
  color: #667eea;
}

.income-card .stat-value {
  color: #67C23A;
}

.expense-card .stat-value {
  color: #F56C6C;
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

/* 资产台账对话框按钮美化 */
.search-card .el-button {
  border-radius: 8px;
  padding: 10px 20px;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  letter-spacing: 0.5px;
}

.search-card .el-button--primary {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.search-card .el-button--primary:hover {
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
  background: linear-gradient(135deg, #337ecc 0%, #529ce3 100%);
}

.search-card .el-button--default {
  border: 2px solid #e4e7ed;
  background: white;
  color: #606266;
}

.search-card .el-button--default:hover {
  border-color: #409eff;
  color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.search-card .el-button:active {
  transform: translateY(0) scale(0.98);
}

/* 表格样式 - 参照资产台账 */
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

:deep(.enhanced-dialog .el-dialog__header::before) {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><circle cx="20" cy="20" r="2" fill="rgba(255,255,255,0.1)"/><circle cx="80" cy="80" r="2" fill="rgba(255,255,255,0.1)"/><circle cx="50" cy="50" r="3" fill="rgba(255,255,255,0.08)"/></svg>') repeat;
  opacity: 0.5;
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

/* 资产台账对话框样式 */
:deep(.asset-ledger-dialog) {
  border-radius: 16px !important;
  overflow: hidden !important;
  box-shadow: 0 16px 48px rgba(64, 158, 255, 0.25) !important;
}

:deep(.asset-ledger-dialog .el-dialog__header) {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%) !important;
  border-radius: 16px 16px 0 0 !important;
  padding: 20px 24px !important;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3) !important;
  margin: 0 !important;
  width: 100% !important;
  box-sizing: border-box !important;
}

:deep(.asset-ledger-dialog .el-dialog__title) {
  color: white !important;
  font-weight: 700 !important;
  font-size: 20px !important;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

:deep(.asset-ledger-dialog .el-dialog__headerbtn .el-dialog__close) {
  color: white !important;
  font-size: 20px !important;
  transition: all 0.3s ease;
}

:deep(.asset-ledger-dialog .el-dialog__headerbtn .el-dialog__close:hover) {
  transform: rotate(90deg) scale(1.1);
  color: rgba(255, 255, 255, 0.8) !important;
}

:deep(.asset-ledger-dialog .el-dialog__body) {
  padding: 16px 20px !important;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%) !important;
}

.asset-ledger-search {
  background: rgba(255, 255, 255, 0.8);
  padding: 12px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  margin-bottom: 12px;
}

:deep(.asset-ledger-dialog .el-table) {
  background-color: #ffffff;
}

:deep(.asset-ledger-dialog .el-table th) {
  background: #fafbfc;
  color: #2c3e50;
  font-weight: 600;
  font-size: 13px;
}

:deep(.asset-ledger-dialog .el-table td) {
  font-size: 13px;
  background-color: #ffffff;
}

:deep(.asset-ledger-dialog .el-table--striped .el-table__body tr.el-table__row--striped td) {
  background-color: #fafbfc;
}

:deep(.asset-ledger-dialog .el-table__body tr:hover > td) {
  background-color: #e6f7ff !important;
}

:deep(.asset-ledger-dialog .el-dialog__footer) {
  padding: 16px 24px !important;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%) !important;
  border-top: 2px solid rgba(64, 158, 255, 0.1) !important;
}

/* 分页组件样式 */
/deep/ .el-pagination {
  justify-content: center;
}

/deep/ .el-pagination button,
/deep/ .el-pagination .el-pager li {
  background-color: rgba(255, 255, 255, 0.9);
}
</style>
