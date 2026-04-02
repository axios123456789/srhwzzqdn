<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <h1 style="margin-top: 10px; font-family: 方正姚体; color: black">
      <el-icon><Notebook /></el-icon>
      资产记账管理
    </h1>

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
              <el-input v-model="queryDto.billNo" style="width: 100%" clearable placeholder="请输入账单编号" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="记账账户">
              <el-input v-model="queryDto.accountName" style="width: 100%" clearable placeholder="请输入记账账户" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="记账时间">
              <el-date-picker
                v-model="queryDto.timeRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                style="width: 100%"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <transition name="el-zoom-in-top">
          <div v-show="searchExpanded">
            <el-row>
              <el-col :span="6">
                <el-form-item label="记账金额(>)">
                  <el-input-number v-model="queryDto.minAmount" style="width: 100%" :min="0" :precision="2" placeholder="请输入最小金额" />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="账单类型">
                  <el-select v-model="queryDto.billType" style="width: 100%" clearable placeholder="请选择账单类型">
                    <el-option v-for="item in billTypeOptions" :key="item.value" :label="item.name" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="结清状态">
                  <el-select v-model="queryDto.settlementStatus" style="width: 100%" clearable placeholder="请选择结清状态">
                    <el-option v-for="item in settlementStatusOptions" :key="item.value" :label="item.name" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="数据来源">
                  <el-select v-model="queryDto.dataSource" style="width: 100%" clearable placeholder="请选择数据来源">
                    <el-option v-for="item in dataSourceOptions" :key="item.value" :label="item.name" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="6">
                <el-form-item label="收支类型">
                  <el-select v-model="queryDto.incomeExpenseType" style="width: 100%" clearable placeholder="请选择收支类型" @change="handleQueryIncomeExpenseChange">
                    <el-option v-for="item in incomeExpenseTypeOptions" :key="item.value" :label="item.name" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6" v-if="queryDto.incomeExpenseType === 'income'">
                <el-form-item label="收益类型">
                  <el-select v-model="queryDto.incomeType" style="width: 100%" clearable placeholder="请选择收益类型">
                    <el-option v-for="item in incomeTypeOptions" :key="item.value" :label="item.name" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6" v-else-if="queryDto.incomeExpenseType === 'expense'">
                <el-form-item label="支出类型">
                  <el-select v-model="queryDto.expenseType" style="width: 100%" clearable placeholder="请选择支出类型">
                    <el-option v-for="item in expenseTypeOptions" :key="item.value" :label="item.name" :value="item.value" />
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
      <el-table-column label="操作" align="center" width="180" #default="scope">
        <el-button type="primary" size="small" @click="editRecord(scope.row)">
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
        <el-button type="danger" size="small" @click="deleteRecord(scope.row)">
          <el-icon><Delete /></el-icon>
          删除
        </el-button>
      </el-table-column>
      <el-table-column prop="billNo" label="账单编号" align="center" width="150" show-overflow-tooltip />
      <el-table-column prop="billAction" label="账单行为" align="center" width="120" show-overflow-tooltip />
      <el-table-column prop="bookingTime" label="记账时间" align="center" width="160" />
      <el-table-column prop="amount" label="金额(元)" align="center" width="120">
        <template #default="scope">
          <span :style="{ color: scope.row.incomeExpenseType === 'income' ? '#67C23A' : '#F56C6C' }">
            {{ scope.row.incomeExpenseType === 'income' ? '+' : '-' }}{{ scope.row.amount }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="accountName" label="记账账户" align="center" width="120" show-overflow-tooltip />
      <el-table-column prop="accountType" label="账户类型" align="center" width="100">
        <template #default="scope">
          {{ getDisplayText(scope.row.accountType, accountTypeOptions) }}
        </template>
      </el-table-column>
      <el-table-column prop="accountNo" label="账户编号" align="center" width="120" show-overflow-tooltip />
      <el-table-column prop="incomeExpenseType" label="收支类型" align="center" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.incomeExpenseType === 'income' ? 'success' : 'danger'" size="small">
            {{ getDisplayText(scope.row.incomeExpenseType, incomeExpenseTypeOptions) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="billType" label="账单类型" align="center" width="120">
        <template #default="scope">
          {{ getDisplayText(scope.row.billType, billTypeOptions) }}
        </template>
      </el-table-column>
      <el-table-column prop="expenseType" label="支出类型" align="center" width="100">
        <template #default="scope">
          {{ getDisplayText(scope.row.expenseType, expenseTypeOptions) }}
        </template>
      </el-table-column>
      <el-table-column prop="settlementStatus" label="结清状态" align="center" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.settlementStatus === 'settled' ? 'success' : 'warning'" size="small">
            {{ getDisplayText(scope.row.settlementStatus, settlementStatusOptions) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="incomeType" label="收益类型" align="center" width="100">
        <template #default="scope">
          {{ getDisplayText(scope.row.incomeType, incomeTypeOptions) }}
        </template>
      </el-table-column>
      <el-table-column prop="bookkeeper" label="记账人" align="center" width="100" />
      <el-table-column prop="dataSource" label="数据来源" align="center" width="100">
        <template #default="scope">
          {{ getDisplayText(scope.row.dataSource, dataSourceOptions) }}
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" align="center" min-width="150" show-overflow-tooltip />
      <el-table-column prop="modifier" label="修改人" align="center" width="100" />
      <el-table-column prop="modifyTime" label="修改时间" align="center" width="160" />
    </el-table>

    <!-- 分页组件 -->
    <el-pagination
      style="margin-top: 30px"
      v-model:current-page="pageParams.page"
      v-model:page-size="pageParams.limit"
      :page-sizes="[10, 20, 50, 100]"
      @size-change="fetchData"
      @current-change="fetchData"
      layout="total, sizes, prev, pager, next, jumper"
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
            <el-form-item label="账单行为" prop="billAction">
              <el-input v-model="formData.billAction" placeholder="请输入账单行为" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="记账时间" prop="bookingTime">
              <el-date-picker
                v-model="formData.bookingTime"
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
            <el-form-item label="记账账户" prop="accountName">
              <el-input v-model="formData.accountName" placeholder="请输入记账账户" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <!-- 第三行 -->
          <el-col :span="12">
            <el-form-item label="账户类型" prop="accountType">
              <el-select v-model="formData.accountType" style="width: 100%" placeholder="请选择账户类型">
                <el-option v-for="item in accountTypeOptions" :key="item.value" :label="item.name" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="账户编号" prop="accountNo">
              <el-input v-model="formData.accountNo" placeholder="请输入账户编号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <!-- 第四行：收支类型 -->
          <el-col :span="12">
            <el-form-item label="收支类型" prop="incomeExpenseType">
              <el-select v-model="formData.incomeExpenseType" style="width: 100%" placeholder="请选择收支类型" @change="handleIncomeExpenseChange">
                <el-option v-for="item in incomeExpenseTypeOptions" :key="item.value" :label="item.name" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="formData.incomeExpenseType === 'income'">
            <el-form-item label="收益类型" prop="incomeType">
              <el-select v-model="formData.incomeType" style="width: 100%" placeholder="请选择收益类型">
                <el-option v-for="item in incomeTypeOptions" :key="item.value" :label="item.name" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-else-if="formData.incomeExpenseType === 'expense'">
            <el-form-item label="支出类型" prop="expenseType">
              <el-select v-model="formData.expenseType" style="width: 100%" placeholder="请选择支出类型">
                <el-option v-for="item in expenseTypeOptions" :key="item.value" :label="item.name" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <!-- 第五行 -->
          <el-col :span="12">
            <el-form-item label="账单类型" prop="billType">
              <el-select v-model="formData.billType" style="width: 100%" placeholder="请选择账单类型">
                <el-option v-for="item in billTypeOptions" :key="item.value" :label="item.name" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结清状态" prop="settlementStatus">
              <el-select v-model="formData.settlementStatus" style="width: 100%" placeholder="请选择结清状态">
                <el-option v-for="item in settlementStatusOptions" :key="item.value" :label="item.name" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <!-- 第六行 -->
          <el-col :span="12">
            <el-form-item label="记账人" prop="bookkeeper">
              <el-input v-model="formData.bookkeeper" placeholder="请输入记账人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数据来源" prop="dataSource">
              <el-select v-model="formData.dataSource" style="width: 100%" placeholder="请选择数据来源">
                <el-option v-for="item in dataSourceOptions" :key="item.value" :label="item.name" :value="item.value" />
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
    <el-dialog
      v-model="exportDialogVisible"
      title="导出数据"
      width="600px"
    >
      <el-form label-width="100px">
        <el-form-item label="导出范围">
          <el-radio-group v-model="exportScope">
            <el-radio label="current">当前页数据</el-radio>
            <el-radio label="all">全部数据</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="文件名称">
          <el-input v-model="exportFileName" placeholder="请输入文件名称" />
        </el-form-item>
        <el-form-item label="导出列">
          <el-checkbox-group v-model="selectedColumns">
            <el-checkbox 
              v-for="col in exportColumns" 
              :key="col.key" 
              :label="col.key"
            >
              {{ col.label }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resetExport">重置</el-button>
          <el-button type="primary" @click="handleExport" :loading="exportLoading">
            导出
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDisplayText } from '@/utils/common'

// ==================== 钩子函数 ====================
onMounted(() => {
  // 初始化模拟数据
  initMockData()
  // 查询数据
  fetchData()
})

// ==================== 数据字典 ====================
// 收支类型选项
const incomeExpenseTypeOptions = ref([
  { name: '收入', value: 'income' },
  { name: '支出', value: 'expense' }
])

// 账单类型选项
const billTypeOptions = ref([
  { name: '挣钱软件佣金', value: 'app_commission' },
  { name: '兼职', value: 'part_time' },
  { name: '理财收益', value: 'financial_income' },
  { name: '饮食支出', value: 'food_expense' },
  { name: '房租', value: 'rent' },
  { name: '日用品开销', value: 'daily_necessities' },
  { name: '工资', value: 'salary' }
])

// 支出类型选项
const expenseTypeOptions = ref([
  { name: '必要', value: 'necessary' },
  { name: '需要', value: 'needed' },
  { name: '想要', value: 'wanted' }
])

// 结清状态选项
const settlementStatusOptions = ref([
  { name: '已结清', value: 'settled' },
  { name: '待结清', value: 'pending' }
])

// 收益类型选项
const incomeTypeOptions = ref([
  { name: '工资', value: 'salary' },
  { name: '佣金', value: 'commission' },
  { name: '兼职', value: 'part_time' },
  { name: '理财', value: 'financial' }
])

// 账户类型选项
const accountTypeOptions = ref([
  { name: '银行卡', value: 'bank_card' },
  { name: '支付宝', value: 'alipay' },
  { name: '微信', value: 'wechat' },
  { name: '现金', value: 'cash' },
  { name: '信用卡', value: 'credit_card' }
])

// 数据来源选项
const dataSourceOptions = ref([
  { name: '银行转账', value: 'bank_transfer' },
  { name: '微信支付', value: 'wechat_pay' },
  { name: '支付宝', value: 'alipay' },
  { name: '现金支付', value: 'cash' },
  { name: '信用卡', value: 'credit_card' },
  { name: '理财产品', value: 'financial_product' },
  { name: '购物返利', value: 'shopping_rebate' },
  { name: '微信收款', value: 'wechat_receive' }
])

// ==================== 模拟数据 ====================
const mockData = ref([])
const billNoCounter = ref(1000)

// 生成账单编号
const generateBillNo = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const counter = String(billNoCounter.value++).padStart(4, '0')
  return `ZC${year}${month}${day}${counter}`
}

// 初始化模拟数据
const initMockData = () => {
  const now = new Date()
  mockData.value = [
    {
      id: 1,
      billNo: 'ZC20260401001',
      billAction: '工资收入',
      bookingTime: '2026-04-01 09:00:00',
      amount: 15000.00,
      accountName: '工商银行',
      accountType: 'bank_card',
      accountNo: '6222021234567890123',
      incomeExpenseType: 'income',
      billType: 'salary',
      expenseType: '',
      settlementStatus: 'settled',
      incomeType: 'salary',
      bookkeeper: '张三',
      dataSource: 'bank_transfer',
      remark: '4月份工资',
      modifier: '张三',
      modifyTime: '2026-04-01 09:00:00'
    },
    {
      id: 2,
      billNo: 'ZC20260401002',
      billAction: '午餐消费',
      bookingTime: '2026-04-01 12:30:00',
      amount: 35.50,
      accountName: '微信',
      accountType: 'wechat',
      accountNo: 'wx_123456',
      incomeExpenseType: 'expense',
      billType: 'food_expense',
      expenseType: 'necessary',
      settlementStatus: 'settled',
      incomeType: '',
      bookkeeper: '张三',
      dataSource: 'wechat_pay',
      remark: '公司附近餐厅午餐',
      modifier: '张三',
      modifyTime: '2026-04-01 12:30:00'
    },
    {
      id: 3,
      billNo: 'ZC20260401003',
      billAction: '房租支付',
      bookingTime: '2026-04-01 10:00:00',
      amount: 3000.00,
      accountName: '支付宝',
      accountType: 'alipay',
      accountNo: 'alipay_789012',
      incomeExpenseType: 'expense',
      billType: 'rent',
      expenseType: 'necessary',
      settlementStatus: 'settled',
      incomeType: '',
      bookkeeper: '张三',
      dataSource: 'alipay',
      remark: '4月份房租',
      modifier: '张三',
      modifyTime: '2026-04-01 10:00:00'
    },
    {
      id: 4,
      billNo: 'ZC20260401004',
      billAction: '兼职收入',
      bookingTime: '2026-04-01 15:00:00',
      amount: 800.00,
      accountName: '微信',
      accountType: 'wechat',
      accountNo: 'wx_123456',
      incomeExpenseType: 'income',
      billType: 'part_time',
      expenseType: '',
      settlementStatus: 'settled',
      incomeType: 'part_time',
      bookkeeper: '张三',
      dataSource: 'wechat_receive',
      remark: '周末兼职项目收入',
      modifier: '张三',
      modifyTime: '2026-04-01 15:00:00'
    },
    {
      id: 5,
      billNo: 'ZC20260401005',
      billAction: '日用品采购',
      bookingTime: '2026-04-01 18:30:00',
      amount: 156.80,
      accountName: '支付宝',
      accountType: 'alipay',
      accountNo: 'alipay_789012',
      incomeExpenseType: 'expense',
      billType: 'daily_necessities',
      expenseType: 'needed',
      settlementStatus: 'settled',
      incomeType: '',
      bookkeeper: '张三',
      dataSource: 'alipay',
      remark: '洗发水、牙膏等日用品',
      modifier: '张三',
      modifyTime: '2026-04-01 18:30:00'
    },
    {
      id: 6,
      billNo: 'ZC20260401006',
      billAction: '理财收益',
      bookingTime: '2026-04-01 08:00:00',
      amount: 520.00,
      accountName: '工商银行',
      accountType: 'bank_card',
      accountNo: '6222021234567890123',
      incomeExpenseType: 'income',
      billType: 'financial_income',
      expenseType: '',
      settlementStatus: 'settled',
      incomeType: 'financial',
      bookkeeper: '张三',
      dataSource: 'financial_product',
      remark: '货币基金收益',
      modifier: '张三',
      modifyTime: '2026-04-01 08:00:00'
    },
    {
      id: 7,
      billNo: 'ZC20260401007',
      billAction: '软件佣金',
      bookingTime: '2026-04-01 20:00:00',
      amount: 28.50,
      accountName: '支付宝',
      accountType: 'alipay',
      accountNo: 'alipay_789012',
      incomeExpenseType: 'income',
      billType: 'app_commission',
      expenseType: '',
      settlementStatus: 'settled',
      incomeType: 'commission',
      bookkeeper: '张三',
      dataSource: 'shopping_rebate',
      remark: '淘宝联盟佣金',
      modifier: '张三',
      modifyTime: '2026-04-01 20:00:00'
    },
    {
      id: 8,
      billNo: 'ZC20260401008',
      billAction: '晚餐消费',
      bookingTime: '2026-04-01 19:00:00',
      amount: 68.00,
      accountName: '现金',
      accountType: 'cash',
      accountNo: '',
      incomeExpenseType: 'expense',
      billType: 'food_expense',
      expenseType: 'wanted',
      settlementStatus: 'settled',
      incomeType: '',
      bookkeeper: '张三',
      dataSource: 'cash',
      remark: '和朋友聚餐',
      modifier: '张三',
      modifyTime: '2026-04-01 19:00:00'
    },
    {
      id: 9,
      billNo: 'ZC20260401009',
      billAction: '信用卡还款',
      bookingTime: '2026-04-01 10:30:00',
      amount: 2500.00,
      accountName: '工商银行',
      accountType: 'bank_card',
      accountNo: '6222021234567890123',
      incomeExpenseType: 'expense',
      billType: 'food_expense',
      expenseType: 'necessary',
      settlementStatus: 'pending',
      incomeType: '',
      bookkeeper: '张三',
      dataSource: 'bank_transfer',
      remark: '信用卡账单还款',
      modifier: '张三',
      modifyTime: '2026-04-01 10:30:00'
    },
    {
      id: 10,
      billNo: 'ZC20260401010',
      billAction: '兼职收入',
      bookingTime: '2026-04-01 16:00:00',
      amount: 450.00,
      accountName: '微信',
      accountType: 'wechat',
      accountNo: 'wx_123456',
      incomeExpenseType: 'income',
      billType: 'part_time',
      expenseType: '',
      settlementStatus: 'pending',
      incomeType: 'part_time',
      bookkeeper: '张三',
      dataSource: 'wechat_receive',
      remark: '线上辅导兼职',
      modifier: '张三',
      modifyTime: '2026-04-01 16:00:00'
    }
  ]
  billNoCounter.value = 1011
}

// ==================== 列表数据 ====================
const list = ref([])
const total = ref(0)
const pageParams = ref({ page: 1, limit: 10 })
const searchExpanded = ref(false)
const queryDto = ref({
  billNo: '',
  accountName: '',
  timeRange: [],
  minAmount: null,
  incomeExpenseType: '',
  incomeType: '',
  expenseType: '',
  billType: '',
  settlementStatus: '',
  dataSource: ''
})

// ==================== 统计数据 ====================
const statistics = ref({
  total: 0,
  income: 0,
  expense: 0
})

// 计算统计数据
const calculateStatistics = (filteredData) => {
  let income = 0
  let expense = 0
  
  filteredData.forEach(item => {
    if (item.incomeExpenseType === 'income') {
      income += item.amount
    } else if (item.incomeExpenseType === 'expense') {
      expense += item.amount
    }
  })
  
  statistics.value = {
    total: income - expense,
    income: income,
    expense: expense
  }
}

// 获取数据（使用模拟数据）
const fetchData = () => {
  // 过滤数据
  let filteredData = [...mockData.value]

  // 按账单编号过滤
  if (queryDto.value.billNo) {
    filteredData = filteredData.filter(item =>
      item.billNo.includes(queryDto.value.billNo)
    )
  }

  // 按记账账户过滤
  if (queryDto.value.accountName) {
    filteredData = filteredData.filter(item =>
      item.accountName.includes(queryDto.value.accountName)
    )
  }

  // 按时间范围过滤
  if (queryDto.value.timeRange && queryDto.value.timeRange.length === 2) {
    const startDate = new Date(queryDto.value.timeRange[0])
    const endDate = new Date(queryDto.value.timeRange[1])

    filteredData = filteredData.filter(item => {
      const itemDate = new Date(item.bookingTime)
      return itemDate >= startDate && itemDate <= endDate
    })
  }

  // 按最小金额过滤
  if (queryDto.value.minAmount !== null && queryDto.value.minAmount !== undefined) {
    filteredData = filteredData.filter(item =>
      item.amount >= queryDto.value.minAmount
    )
  }

  // 按收支类型过滤
  if (queryDto.value.incomeExpenseType) {
    filteredData = filteredData.filter(item =>
      item.incomeExpenseType === queryDto.value.incomeExpenseType
    )
  }

  // 按收益类型过滤
  if (queryDto.value.incomeType) {
    filteredData = filteredData.filter(item =>
      item.incomeType === queryDto.value.incomeType
    )
  }

  // 按支出类型过滤
  if (queryDto.value.expenseType) {
    filteredData = filteredData.filter(item =>
      item.expenseType === queryDto.value.expenseType
    )
  }

  // 按账单类型过滤
  if (queryDto.value.billType) {
    filteredData = filteredData.filter(item =>
      item.billType === queryDto.value.billType
    )
  }

  // 按结清状态过滤
  if (queryDto.value.settlementStatus) {
    filteredData = filteredData.filter(item =>
      item.settlementStatus === queryDto.value.settlementStatus
    )
  }

  // 按数据来源过滤
  if (queryDto.value.dataSource) {
    filteredData = filteredData.filter(item =>
      item.dataSource === queryDto.value.dataSource
    )
  }

  // 计算总数
  total.value = filteredData.length

  // 计算统计数据
  calculateStatistics(filteredData)

  // 分页
  const start = (pageParams.value.page - 1) * pageParams.value.limit
  const end = start + pageParams.value.limit
  list.value = filteredData.slice(start, end)
}

// 搜索
const searchData = () => {
  pageParams.value.page = 1
  fetchData()
}

// 切换搜索条件展开/收起
const toggleSearchExpand = () => {
  searchExpanded.value = !searchExpanded.value
}

// 查询条件收支类型改变事件
const handleQueryIncomeExpenseChange = (value) => {
  if (value === 'income') {
    queryDto.value.expenseType = ''
  } else if (value === 'expense') {
    queryDto.value.incomeType = ''
  }
}

// 重置
const resetData = () => {
  queryDto.value = {
    billNo: '',
    accountName: '',
    timeRange: [],
    minAmount: null,
    incomeExpenseType: '',
    incomeType: '',
    expenseType: '',
    billType: '',
    settlementStatus: '',
    dataSource: ''
  }
  pageParams.value.page = 1
  fetchData()
}

// ==================== 添加/修改 ====================
const dialogVisible = ref(false)
const dialogTitle = ref('添加记账')
const formRef = ref(null)
const formData = ref({
  id: null,
  billNo: '',
  billAction: '',
  bookingTime: '',
  amount: 0,
  accountName: '',
  accountType: '',
  accountNo: '',
  incomeExpenseType: '',
  billType: '',
  expenseType: '',
  settlementStatus: '',
  incomeType: '',
  bookkeeper: '',
  dataSource: '',
  remark: '',
  modifier: '',
  modifyTime: ''
})

// 表单验证规则
const formRules = {
  billAction: [{ required: true, message: '请输入账单行为', trigger: 'blur' }],
  bookingTime: [{ required: true, message: '请选择记账时间', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  accountName: [{ required: true, message: '请输入记账账户', trigger: 'blur' }],
  incomeExpenseType: [{ required: true, message: '请选择收支类型', trigger: 'change' }],
  billType: [{ required: true, message: '请选择账单类型', trigger: 'change' }],
  settlementStatus: [{ required: true, message: '请选择结清状态', trigger: 'change' }],
  bookkeeper: [{ required: true, message: '请输入记账人', trigger: 'blur' }]
}

// 添加记录
const addRecord = () => {
  dialogTitle.value = '添加记账'
  formData.value = {
    id: null,
    billNo: '',
    billAction: '',
    bookingTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
    amount: 0,
    accountName: '',
    accountType: '',
    accountNo: '',
    incomeExpenseType: '',
    billType: '',
    expenseType: '',
    settlementStatus: 'pending',
    incomeType: '',
    bookkeeper: '',
    dataSource: '',
    remark: '',
    modifier: '',
    modifyTime: ''
  }
  dialogVisible.value = true
}

// 编辑记录
const editRecord = (row) => {
  dialogTitle.value = '编辑记账'
  formData.value = { ...row }
  dialogVisible.value = true
}

// 收支类型改变事件
const handleIncomeExpenseChange = (value) => {
  if (value === 'income') {
    formData.value.expenseType = ''
  } else if (value === 'expense') {
    formData.value.incomeType = ''
  }
}

// 提交表单
const submit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      const currentTime = new Date().toISOString().slice(0, 19).replace('T', ' ')

      // 模拟保存操作
      if (formData.value.id) {
        // 编辑：更新模拟数据
        const index = mockData.value.findIndex(item => item.id === formData.value.id)
        if (index !== -1) {
          mockData.value[index] = {
            ...formData.value,
            modifier: formData.value.bookkeeper,
            modifyTime: currentTime
          }
        }
        ElMessage.success('编辑成功')
      } else {
        // 新增：添加到模拟数据
        const newRecord = {
          ...formData.value,
          id: Date.now(),
          billNo: generateBillNo(),
          modifier: formData.value.bookkeeper,
          modifyTime: currentTime
        }
        mockData.value.unshift(newRecord)
        ElMessage.success('添加成功')
      }

      dialogVisible.value = false
      fetchData()
    } catch (error) {
      ElMessage.error('提交失败')
    }
  })
}

// ==================== 删除 ====================
const deleteRecord = (row) => {
  ElMessageBox.confirm('确定删除该记账记录吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    // 从模拟数据中删除
    const index = mockData.value.findIndex(item => item.id === row.id)
    if (index !== -1) {
      mockData.value.splice(index, 1)
      ElMessage.success('删除成功')
      fetchData()
    }
  }).catch(() => {
    // 取消删除
  })
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

  await ElMessageBox.confirm(
    `确定要批量删除选中的 ${selectedRows.value.length} 条记录吗？`,
    '警告',
    { type: 'warning' }
  )

  try {
    // 从模拟数据中批量删除
    const selectedIds = selectedRows.value.map(row => row.id)
    mockData.value = mockData.value.filter(item => !selectedIds.includes(item.id))
    
    fetchData()
    multipleTable.value.clearSelection()
    selectedRows.value = []
    ElMessage.success('批量删除成功')
  } catch (error) {
    ElMessage.error('批量删除失败')
  }
}

// ==================== 导出功能 ====================
const exportDialogVisible = ref(false)
const exportScope = ref('current')
const exportFileName = ref('资产记账数据')
const exportLoading = ref(false)
const selectedColumns = ref([])

const exportColumns = [
  { key: 'billNo', label: '账单编号', width: 20 },
  { key: 'billAction', label: '账单行为', width: 15 },
  { key: 'bookingTime', label: '记账时间', width: 20 },
  { key: 'amount', label: '金额', width: 15 },
  { key: 'accountName', label: '记账账户', width: 15 },
  { key: 'accountType', label: '账户类型', width: 12 },
  { key: 'accountNo', label: '账户编号', width: 15 },
  { key: 'incomeExpenseType', label: '收支类型', width: 12 },
  { key: 'billType', label: '账单类型', width: 15 },
  { key: 'expenseType', label: '支出类型', width: 12 },
  { key: 'settlementStatus', label: '结清状态', width: 12 },
  { key: 'incomeType', label: '收益类型', width: 12 },
  { key: 'bookkeeper', label: '记账人', width: 12 },
  { key: 'dataSource', label: '数据来源', width: 15 },
  { key: 'remark', label: '备注', width: 30 }
]

const showExportDialog = () => {
  selectedColumns.value = [...exportColumns]
  exportDialogVisible.value = true
}

const handleExport = () => {
  ElMessage.success('导出功能需要后端支持，当前为前端演示')
  exportDialogVisible.value = false
}

const resetExport = () => {
  selectedColumns.value = [...exportColumns]
  exportFileName.value = '资产记账数据'
  exportScope.value = 'current'
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

/* 表格透明样式 */
/deep/ .el-table,
/deep/ .el-table__expanded-cell {
  background-color: rgba(255, 255, 255, 0.9);
  color: #001528;
}

/deep/ .el-table th,
/deep/ .el-table tr,
/deep/ .el-table td {
  background-color: transparent;
  color: #001528;
}

/deep/ .el-table--striped .el-table__body tr.el-table__row--striped td {
  background-color: rgba(245, 247, 250, 0.5);
}

/* 对话框样式 */
.custom-dialog {
  border-radius: 8px;
}

.enhanced-dialog .el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px 8px 0 0;
  padding: 20px;
}

.enhanced-dialog .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.enhanced-dialog .el-dialog__headerbtn .el-dialog__close {
  color: white;
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
