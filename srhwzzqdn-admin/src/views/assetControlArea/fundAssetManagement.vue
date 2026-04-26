<template>
  <div class="fund-management-container">
    <!-- 标题栏 + 数据获取区 合并 -->
    <div class="top-bar">
      <div class="top-bar-left">
        <div class="page-title-icon">
          <el-icon :size="22"><Coin /></el-icon>
        </div>
        <span class="page-title">基金资产管理</span>
      </div>
      <div class="top-bar-right">
        <el-input
          v-model="fetchFundCode"
          placeholder="输入基金代码获取数据"
          style="width: 220px"
          clearable
          size="small"
          @keyup.enter="fetchFundData"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" size="small" @click="fetchFundData" :loading="fetchLoading">
          <el-icon><Download /></el-icon>
          数据获取
        </el-button>
      </div>
    </div>

    <!-- 条件查询区 -->
    <div class="search-section">
      <el-form label-width="120px" size="small">
        <el-row>
          <el-col :span="6">
            <el-form-item label="基金名称">
              <el-input v-model="query.fundName" style="width: 100%" placeholder="请输入基金名称" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="基金代码">
              <el-input v-model="query.fundCode" style="width: 100%" placeholder="请输入基金代码" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="基金类型">
              <el-select v-model="query.fundType" multiple placeholder="请选择" style="width: 100%" clearable>
                <el-option v-for="item in fundTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="资产规模(亿)">
              <el-input-number v-model="query.assetScaleGt" :precision="2" :step="1" :min="0" style="width: 100%" controls-position="right" placeholder="大于" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="6">
            <el-form-item label="运作方式">
              <el-select v-model="query.operationMode" placeholder="请选择" style="width: 100%" clearable>
                <el-option v-for="item in operationModeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="封闭期(天)">
              <el-input-number v-model="query.closedPeriodGt" :min="0" :step="1" style="width: 100%" controls-position="right" placeholder="大于" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row style="display: flex; gap: 10px; padding-left: 10px;">
          <el-button type="primary" size="small" @click="handleSearch" class="beautified-search-btn">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button size="small" @click="resetQuery" class="beautified-reset-btn">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-row>
      </el-form>
    </div>

    <!-- 操作按钮区 -->
    <div class="action-section">
      <el-button type="danger" size="small" @click="batchDelete" class="action-btn-danger">
        <el-icon><Delete /></el-icon>
        批量删除
      </el-button>
      <el-button type="info" size="small" @click="handleExport" class="action-btn-export">
        <el-icon><Download /></el-icon>
        一键导出
      </el-button>
    </div>

    <!-- 卡片列表区（独立滚动块） -->
    <div class="card-list-section">
      <div class="card-scroll-wrapper">
        <div class="card-grid">
          <div
            v-for="item in pageData"
            :key="item.id"
            class="fund-card"
            :class="{ 'card-selected': selectedIds.includes(item.id) }"
          >
            <div class="card-checkbox">
              <el-checkbox :model-value="selectedIds.includes(item.id)" @change="(val) => toggleSelect(item.id, val)" />
            </div>
            <div class="card-header">
              <div class="card-title-row">
                <span class="card-fund-name clickable" @click="openViewDialog(item)">{{ item.fundName }}</span>
                <el-tag :type="getFundTypeTagType(item.fundType)" size="small">{{ item.fundType || '未知' }}</el-tag>
              </div>
              <div class="card-code">{{ item.fundCode }}</div>
            </div>
            <div class="card-body">
              <div class="card-info-grid">
                <div class="info-cell">
                  <span class="info-label">成立日期</span>
                  <span class="info-value">{{ item.establishDate || '-' }}</span>
                </div>
                <div class="info-cell">
                  <span class="info-label">资产规模</span>
                  <span class="info-value">{{ item.assetScale ? item.assetScale + '亿' : '-' }}</span>
                </div>
                <div class="info-cell">
                  <span class="info-label">基金管理人</span>
                  <span class="info-value">{{ item.fundManager || '-' }}</span>
                </div>
                <div class="info-cell">
                  <span class="info-label">基金托管者</span>
                  <span class="info-value">{{ item.fundCustodian || '-' }}</span>
                </div>
                <div class="info-cell">
                  <span class="info-label">基金经理</span>
                  <span class="info-value">{{ item.fundManagerName || '-' }}</span>
                </div>
                <div class="info-cell">
                  <span class="info-label">任职日期</span>
                  <span class="info-value">{{ item.managerAppointDate || '-' }}</span>
                </div>
                <div class="info-cell">
                  <span class="info-label">运作方式</span>
                  <span class="info-value">{{ item.operationMode || '-' }}</span>
                </div>
                <div class="info-cell">
                  <span class="info-label">封闭期</span>
                  <span class="info-value">{{ item.closedPeriod ? item.closedPeriod + '天' : '-' }}</span>
                </div>
                <div class="info-cell">
                  <span class="info-label">申购费率</span>
                  <span class="info-value">{{ item.purchaseRate ? item.purchaseRate + '%' : '-' }}</span>
                </div>
                <div class="info-cell">
                  <span class="info-label">赎回费率</span>
                  <span class="info-value">{{ item.redemptionRate ? item.redemptionRate + '%' : '-' }}</span>
                </div>
                <div class="info-cell">
                  <span class="info-label">管理费</span>
                  <span class="info-value">{{ item.managementFee ? item.managementFee + '%' : '-' }}</span>
                </div>
                <div class="info-cell">
                  <span class="info-label">托管费</span>
                  <span class="info-value">{{ item.custodianFee ? item.custodianFee + '%' : '-' }}</span>
                </div>
                <div class="info-cell">
                  <span class="info-label">销售服务费</span>
                  <span class="info-value">{{ item.salesServiceFee ? item.salesServiceFee + '%' : '-' }}</span>
                </div>
              </div>
            </div>
            <div class="card-footer">
              <el-button type="primary" size="small" @click="openDetailDialog(item)" class="card-action-btn-edit">
                <el-icon><Edit /></el-icon>
                数据维护
              </el-button>
              <el-button type="danger" size="small" @click="deleteFund(item)" class="card-action-btn-delete">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="filterData.length === 0" class="empty-state">
          <el-empty description="暂无基金数据，请通过数据获取添加基金" />
        </div>
      </div>

      <!-- 分页区 -->
      <div class="pagination-section" v-if="filterData.length > 0">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :total="filterData.length"
          :page-size="pageParams.limit"
          :current-page="pageParams.page"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 数据获取结果模态窗口 -->
    <el-dialog
      v-model="fetchDialogVisible"
      title="基金数据获取结果"
      width="60%"
      :close-on-click-modal="false"
      class="fetch-result-dialog"
    >
      <el-form label-width="120px" size="small">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="基金名称">
              <el-input v-model="fetchResult.fundName" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="基金代码">
              <el-input v-model="fetchResult.fundCode" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="基金类型">
              <el-select v-model="fetchResult.fundType" style="width: 100%" clearable>
                <el-option v-for="item in fundTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成立日期">
              <el-date-picker v-model="fetchResult.establishDate" type="date" style="width: 100%" value-format="YYYY-MM-DD" :editable="false" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="资产规模(亿)">
              <el-input-number v-model="fetchResult.assetScale" :precision="2" :step="0.01" :min="0" style="width: 100%" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="基金管理人">
              <el-input v-model="fetchResult.fundManager" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="基金公司描述">
              <el-input v-model="fetchResult.fundCompanyDesc" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="基金托管者">
              <el-input v-model="fetchResult.fundCustodian" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="基金经理">
              <el-input v-model="fetchResult.fundManagerName" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经理任职日期">
              <el-date-picker v-model="fetchResult.managerAppointDate" type="date" style="width: 100%" value-format="YYYY-MM-DD" :editable="false" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="经理描述">
              <el-input v-model="fetchResult.managerDesc" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="运作方式">
              <el-select v-model="fetchResult.operationMode" style="width: 100%" clearable>
                <el-option v-for="item in operationModeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="基金封闭期(天)">
              <el-input-number v-model="fetchResult.closedPeriod" :min="0" style="width: 100%" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="申购费率(%)">
              <el-input-number v-model="fetchResult.purchaseRate" :precision="4" :step="0.001" :min="0" style="width: 100%" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="赎回费率(%)">
              <el-input-number v-model="fetchResult.redemptionRate" :precision="4" :step="0.001" :min="0" style="width: 100%" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="管理费(%)">
              <el-input-number v-model="fetchResult.managementFee" :precision="4" :step="0.001" :min="0" style="width: 100%" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="托管费(%)">
              <el-input-number v-model="fetchResult.custodianFee" :precision="4" :step="0.001" :min="0" style="width: 100%" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="销售服务费(%)">
              <el-input-number v-model="fetchResult.salesServiceFee" :precision="4" :step="0.001" :min="0" style="width: 100%" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="交易规则">
              <el-input v-model="fetchResult.tradeRule" type="textarea" :rows="2" placeholder="请输入交易规则" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="fetchDialogVisible = false" class="dialog-cancel-btn">取消</el-button>
        <el-button type="primary" @click="submitFetchResult" class="dialog-submit-btn">提交入库</el-button>
      </template>
    </el-dialog>

    <!-- 基金数据维护大模态窗口 -->
    <FundDetailDialog
      v-model:visible="detailDialogVisible"
      :fund-row-data="currentFundRow"
      @save="handleDetailSave"
    />

    <!-- 基金详情查看模态窗口 -->
    <FundViewDialog
      v-model:visible="viewDialogVisible"
      :fund-data="currentViewFund"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Coin, Search, Download, Refresh, Delete, Edit } from '@element-plus/icons-vue'
import FundDetailDialog from './fundDetailDialog/fundDetailDialog.vue'
import FundViewDialog from './fundDetailDialog/fundViewDialog.vue'

// ============ 选项常量 ============
const fundTypeOptions = [
  { label: '股票型', value: '股票型' },
  { label: '债券型', value: '债券型' },
  { label: '货币型', value: '货币型' },
  { label: '混合型', value: '混合型' },
  { label: '指数型', value: '指数型' }
]
const operationModeOptions = [
  { label: '开放式', value: '开放式' },
  { label: '封闭式', value: '封闭式' }
]

const getFundTypeTagType = (type) => {
  const map = { '股票型': 'danger', '债券型': 'success', '货币型': 'warning', '混合型': 'info', '指数型': '' }
  return map[type] || 'info'
}

// ============ 数据获取区 ============
const fetchFundCode = ref('')
const fetchLoading = ref(false)
const fetchDialogVisible = ref(false)
const fetchResult = reactive({
  fundName: '', fundCode: '', fundType: '', establishDate: '',
  assetScale: 0, fundManager: '', fundCompanyDesc: '', fundCustodian: '',
  fundManagerName: '', managerAppointDate: '', managerDesc: '',
  operationMode: '', closedPeriod: 0, purchaseRate: 0, redemptionRate: 0,
  managementFee: 0, custodianFee: 0, salesServiceFee: 0, tradeRule: ''
})

// 模拟基金数据获取（根据基金代码模拟接口返回）
const mockFundDatabase = {
  '460001': { fundName: '友邦华泰红利ETF', fundType: '指数型', establishDate: '2006-11-17', assetScale: 12.35, fundManager: '友邦华泰基金', fundCompanyDesc: '友邦华泰基金管理有限公司', fundCustodian: '中国工商银行', fundManagerName: '张娅', managerAppointDate: '2006-11-17', managerDesc: '具有多年指数基金管理经验', operationMode: '开放式', closedPeriod: 0, purchaseRate: 0.12, redemptionRate: 0.5, managementFee: 0.5, custodianFee: 0.1, salesServiceFee: 0, tradeRule: 'T+1确认，T+2可赎回' },
  '110011': { fundName: '易方达中小盘混合', fundType: '混合型', establishDate: '2008-06-19', assetScale: 35.67, fundManager: '易方达基金', fundCompanyDesc: '易方达基金管理有限公司', fundCustodian: '中国工商银行', fundManagerName: '张坤', managerAppointDate: '2012-09-28', managerDesc: '擅长价值投资', operationMode: '开放式', closedPeriod: 0, purchaseRate: 0.15, redemptionRate: 0.5, managementFee: 1.5, custodianFee: 0.25, salesServiceFee: 0, tradeRule: 'T+1确认，T+2可赎回' },
  '000001': { fundName: '华夏成长混合', fundType: '混合型', establishDate: '2004-12-30', assetScale: 8.92, fundManager: '华夏基金', fundCompanyDesc: '华夏基金管理有限公司', fundCustodian: '中国建设银行', fundManagerName: '巩怀志', managerAppointDate: '2017-01-06', managerDesc: '资深基金经理', operationMode: '开放式', closedPeriod: 0, purchaseRate: 0.15, redemptionRate: 0.5, managementFee: 1.5, custodianFee: 0.25, salesServiceFee: 0, tradeRule: 'T+1确认，T+2可赎回' },
  '519697': { fundName: '交银优势行业混合', fundType: '混合型', establishDate: '2009-01-16', assetScale: 22.18, fundManager: '交银施罗德基金', fundCompanyDesc: '交银施罗德基金管理有限公司', fundCustodian: '中国建设银行', fundManagerName: '何帅', managerAppointDate: '2015-09-16', managerDesc: '擅长行业轮动', operationMode: '开放式', closedPeriod: 0, purchaseRate: 0.15, redemptionRate: 0.5, managementFee: 1.5, custodianFee: 0.25, salesServiceFee: 0, tradeRule: 'T+1确认，T+2可赎回' },
  '003834': { fundName: '华夏能源革新股票', fundType: '股票型', establishDate: '2017-06-07', assetScale: 45.33, fundManager: '华夏基金', fundCompanyDesc: '华夏基金管理有限公司', fundCustodian: '中国建设银行', fundManagerName: '郑泽鸿', managerAppointDate: '2017-06-07', managerDesc: '新能源领域研究专家', operationMode: '开放式', closedPeriod: 0, purchaseRate: 0.15, redemptionRate: 0.5, managementFee: 1.5, custodianFee: 0.25, salesServiceFee: 0, tradeRule: 'T+1确认，T+2可赎回' },
  '161725': { fundName: '招商中证白酒指数', fundType: '指数型', establishDate: '2015-05-27', assetScale: 98.76, fundManager: '招商基金', fundCompanyDesc: '招商基金管理有限公司', fundCustodian: '中国银行', fundManagerName: '侯昊', managerAppointDate: '2015-05-27', managerDesc: '指数投资专家', operationMode: '开放式', closedPeriod: 0, purchaseRate: 0.1, redemptionRate: 0.5, managementFee: 1.0, custodianFee: 0.2, salesServiceFee: 0, tradeRule: 'T+1确认，T+2可赎回' },
  '511010': { fundName: '国泰上证5年期国债ETF', fundType: '债券型', establishDate: '2013-03-05', assetScale: 5.21, fundManager: '国泰基金', fundCompanyDesc: '国泰基金管理有限公司', fundCustodian: '中国银行', fundManagerName: '梁杏', managerAppointDate: '2013-03-05', managerDesc: '债券投资专家', operationMode: '开放式', closedPeriod: 0, purchaseRate: 0.08, redemptionRate: 0.1, managementFee: 0.3, custodianFee: 0.1, salesServiceFee: 0, tradeRule: 'T+1确认' },
  '000198': { fundName: '天弘增利短债债券', fundType: '债券型', establishDate: '2013-11-20', assetScale: 156.42, fundManager: '天弘基金', fundCompanyDesc: '天弘基金管理有限公司', fundCustodian: '中国工商银行', fundManagerName: '姜晓丽', managerAppointDate: '2013-11-20', managerDesc: '短债管理经验丰富', operationMode: '开放式', closedPeriod: 0, purchaseRate: 0, redemptionRate: 0, managementFee: 0.25, custodianFee: 0.08, salesServiceFee: 0.15, tradeRule: 'T+1确认' },
  '004137': { fundName: '博时黄金ETF联接C', fundType: '指数型', establishDate: '2016-11-02', assetScale: 18.55, fundManager: '博时基金', fundCompanyDesc: '博时基金管理有限公司', fundCustodian: '中国银行', fundManagerName: '王祥', managerAppointDate: '2016-11-02', managerDesc: '黄金投资专家', operationMode: '开放式', closedPeriod: 0, purchaseRate: 0, redemptionRate: 0, managementFee: 0.5, custodianFee: 0.1, salesServiceFee: 0.2, tradeRule: 'T+1确认' },
  '501057': { fundName: '华夏科创50ETF联接', fundType: '指数型', establishDate: '2021-01-26', assetScale: 67.89, fundManager: '华夏基金', fundCompanyDesc: '华夏基金管理有限公司', fundCustodian: '中国建设银行', fundManagerName: '荣膺', managerAppointDate: '2021-01-26', managerDesc: '科创板研究专家', operationMode: '开放式', closedPeriod: 0, purchaseRate: 0.12, redemptionRate: 0.5, managementFee: 0.5, custodianFee: 0.1, salesServiceFee: 0, tradeRule: 'T+1确认，T+2可赎回' }
}

const fetchFundData = async () => {
  if (!fetchFundCode.value.trim()) {
    ElMessage.warning('请输入基金代码')
    return
  }
  fetchLoading.value = true
  // 模拟接口调用延迟
  await new Promise(resolve => setTimeout(resolve, 800))
  const code = fetchFundCode.value.trim()
  const mockData = mockFundDatabase[code]
  fetchLoading.value = false

  if (!mockData) {
    ElMessage.warning('没有找到该基金数据，请检查基金代码是否正确')
    return
  }

  // 填充获取结果
  Object.assign(fetchResult, { ...mockData, fundCode: code })
  fetchDialogVisible.value = true
}

const submitFetchResult = () => {
  const newFund = {
    id: Date.now(),
    ...JSON.parse(JSON.stringify(fetchResult)),
    navList: [],
    holdShares: 0, availableShares: 0, frozenShares: 0,
    positionCost: 0, positionValue: 0,
    tradeList: [], dividendList: [], riskList: [], holdingList: []
  }
  fundList.value.push(newFund)
  fetchDialogVisible.value = false
  ElMessage.success('基金数据入库成功')
}

// ============ 查询条件 ============
const query = reactive({
  fundName: '', fundCode: '', fundType: [],
  assetScaleGt: null, operationMode: '', closedPeriodGt: null
})

// 实际用于筛选的条件(只有点击搜索按钮时才更新)
const activeQuery = reactive({
  fundName: '', fundCode: '', fundType: [],
  assetScaleGt: null, operationMode: '', closedPeriodGt: null
})

const resetQuery = () => {
  // 清空输入框的条件
  Object.assign(query, {
    fundName: '', fundCode: '', fundType: [],
    assetScaleGt: null, operationMode: '', closedPeriodGt: null
  })
  // 同时清空实际筛选条件
  Object.assign(activeQuery, {
    fundName: '', fundCode: '', fundType: [],
    assetScaleGt: null, operationMode: '', closedPeriodGt: null
  })
  pageParams.page = 1
}

// ============ 基金列表数据（造数据） ============
const fundList = ref([
  {
    id: 1, fundName: '易方达中小盘混合', fundCode: '110011', fundType: '混合型',
    establishDate: '2008-06-19', assetScale: 35.67, fundManager: '易方达基金',
    fundCompanyDesc: '易方达基金管理有限公司', fundCustodian: '中国工商银行',
    fundManagerName: '张坤', managerAppointDate: '2012-09-28', managerDesc: '擅长价值投资',
    operationMode: '开放式', closedPeriod: 0, purchaseRate: 0.15, redemptionRate: 0.5,
    managementFee: 1.5, custodianFee: 0.25, salesServiceFee: 0, tradeRule: 'T+1确认，T+2可赎回',
    navList: [
      { navDate: '2026-04-21', unitNav: 3.4521, accumulatedNav: 4.8732, dailyChangeRate: 1.23, estimateNav: 3.4680 },
      { navDate: '2026-04-18', unitNav: 3.4102, accumulatedNav: 4.8313, dailyChangeRate: -0.56, estimateNav: 3.3910 },
      { navDate: '2026-04-17', unitNav: 3.4294, accumulatedNav: 4.8505, dailyChangeRate: 0.89, estimateNav: 3.4400 },
      { navDate: '2026-04-16', unitNav: 3.3992, accumulatedNav: 4.8203, dailyChangeRate: -1.12, estimateNav: 3.3610 },
      { navDate: '2026-04-15', unitNav: 3.4377, accumulatedNav: 4.8588, dailyChangeRate: 0.34, estimateNav: 3.4490 }
    ],
    holdShares: 5000, availableShares: 5000, frozenShares: 0, positionCost: 15000, positionValue: 17260.5,
    tradeList: [
      { tradeDate: '2025-01-15', tradeType: '申购', tradeShares: 2000, tradeAmount: 6000, tradeNav: 3.0, tradeFee: 9, confirmDate: '2025-01-16' },
      { tradeDate: '2025-06-20', tradeType: '申购', tradeShares: 3000, tradeAmount: 9000, tradeNav: 3.0, tradeFee: 13.5, confirmDate: '2025-06-21' }
    ],
    dividendList: [
      { dividendDate: '2025-12-15', arrivalDate: '2025-12-18', dividendMethod: '现金分红', dividendAmount: 250 }
    ],
    riskList: [
      { timePeriod: '近1年', maxDrawdown: 18.5, drawdownRecoveryDays: 120, annualizedReturn: 12.3, sharpeRatio: 0.85, volatility: 22.1 },
      { timePeriod: '近3年', maxDrawdown: 32.1, drawdownRecoveryDays: 245, annualizedReturn: 15.6, sharpeRatio: 0.92, volatility: 24.8 }
    ],
    holdingList: [
      { holdingId: 'H001', fundCode: '110011', holdingDate: '2026-03-31', holdingType: '股票', holdingCode: '600519', holdingName: '贵州茅台', holdingQuantity: 1200, holdingValue: 2184000, navRatio: 9.52, industryClass: '半导体', dataSource: '天天基金' },
      { holdingId: 'H002', fundCode: '110011', holdingDate: '2026-03-31', holdingType: '股票', holdingCode: '000858', holdingName: '五粮液', holdingQuantity: 3500, holdingValue: 525000, navRatio: 2.29, industryClass: '通信装备', dataSource: '天天基金' }
    ]
  },
  {
    id: 2, fundName: '招商中证白酒指数', fundCode: '161725', fundType: '指数型',
    establishDate: '2015-05-27', assetScale: 98.76, fundManager: '招商基金',
    fundCompanyDesc: '招商基金管理有限公司', fundCustodian: '中国银行',
    fundManagerName: '侯昊', managerAppointDate: '2015-05-27', managerDesc: '指数投资专家',
    operationMode: '开放式', closedPeriod: 0, purchaseRate: 0.1, redemptionRate: 0.5,
    managementFee: 1.0, custodianFee: 0.2, salesServiceFee: 0, tradeRule: 'T+1确认，T+2可赎回',
    navList: [
      { navDate: '2026-04-21', unitNav: 1.5623, accumulatedNav: 2.1345, dailyChangeRate: 2.15, estimateNav: 1.5780 },
      { navDate: '2026-04-18', unitNav: 1.5294, accumulatedNav: 2.1016, dailyChangeRate: -0.78, estimateNav: 1.5175 }
    ],
    holdShares: 10000, availableShares: 10000, frozenShares: 0, positionCost: 14000, positionValue: 15623,
    tradeList: [
      { tradeDate: '2024-12-01', tradeType: '申购', tradeShares: 10000, tradeAmount: 14000, tradeNav: 1.4, tradeFee: 14, confirmDate: '2024-12-02' }
    ],
    dividendList: [],
    riskList: [
      { timePeriod: '近1年', maxDrawdown: 25.3, drawdownRecoveryDays: 180, annualizedReturn: 8.7, sharpeRatio: 0.62, volatility: 28.5 }
    ],
    holdingList: [
      { holdingId: 'H003', fundCode: '161725', holdingDate: '2026-03-31', holdingType: '股票', holdingCode: '600519', holdingName: '贵州茅台', holdingQuantity: 800, holdingValue: 1456000, navRatio: 9.32, industryClass: '半导体', dataSource: '支付宝' }
    ]
  },
  {
    id: 3, fundName: '天弘增利短债债券', fundCode: '000198', fundType: '债券型',
    establishDate: '2013-11-20', assetScale: 156.42, fundManager: '天弘基金',
    fundCompanyDesc: '天弘基金管理有限公司', fundCustodian: '中国工商银行',
    fundManagerName: '姜晓丽', managerAppointDate: '2013-11-20', managerDesc: '短债管理经验丰富',
    operationMode: '开放式', closedPeriod: 0, purchaseRate: 0, redemptionRate: 0,
    managementFee: 0.25, custodianFee: 0.08, salesServiceFee: 0.15, tradeRule: 'T+1确认',
    navList: [
      { navDate: '2026-04-21', unitNav: 1.0823, accumulatedNav: 1.2845, dailyChangeRate: 0.02, estimateNav: 1.0825 }
    ],
    holdShares: 20000, availableShares: 20000, frozenShares: 0, positionCost: 20000, positionValue: 21646,
    tradeList: [
      { tradeDate: '2025-03-10', tradeType: '申购', tradeShares: 20000, tradeAmount: 20000, tradeNav: 1.0, tradeFee: 0, confirmDate: '2025-03-11' }
    ],
    dividendList: [],
    riskList: [
      { timePeriod: '近1年', maxDrawdown: 0.5, drawdownRecoveryDays: 15, annualizedReturn: 3.2, sharpeRatio: 1.85, volatility: 1.2 }
    ],
    holdingList: []
  }
])

// ============ 筛选 & 分页 ============
const pageParams = reactive({ page: 1, limit: 10 })

const filterData = computed(() => {
  return fundList.value.filter(item => {
    if (activeQuery.fundName && !item.fundName.includes(activeQuery.fundName)) return false
    if (activeQuery.fundCode && !item.fundCode.includes(activeQuery.fundCode)) return false
    if (activeQuery.fundType.length > 0 && !activeQuery.fundType.includes(item.fundType)) return false
    if (activeQuery.assetScaleGt && item.assetScale < activeQuery.assetScaleGt) return false
    if (activeQuery.operationMode && item.operationMode !== activeQuery.operationMode) return false
    if (activeQuery.closedPeriodGt && item.closedPeriod < activeQuery.closedPeriodGt) return false
    return true
  })
})

const pageData = computed(() => {
  const s = (pageParams.page - 1) * pageParams.limit
  return filterData.value.slice(s, s + pageParams.limit)
})

const handleSearch = () => {
  // 点击搜索按钮时,将查询条件应用到实际筛选条件
  Object.assign(activeQuery, JSON.parse(JSON.stringify(query)))
  pageParams.page = 1
}

const handleSizeChange = (size) => { pageParams.limit = size; pageParams.page = 1 }
const handleCurrentChange = (page) => { pageParams.page = page }

// ============ 选择 & 批量删除 ============
const selectedIds = ref([])
const toggleSelect = (id, val) => {
  if (val) { if (!selectedIds.value.includes(id)) selectedIds.value.push(id) }
  else { selectedIds.value = selectedIds.value.filter(i => i !== id) }
}

const batchDelete = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要删除的基金')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条基金数据吗？`, '批量删除确认', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
    fundList.value = fundList.value.filter(item => !selectedIds.value.includes(item.id))
    selectedIds.value = []
    ElMessage.success('批量删除成功')
  } catch (e) { /* cancel */ }
}

// ============ 单条删除 ============
const deleteFund = async (item) => {
  try {
    await ElMessageBox.confirm(`确定要删除基金【${item.fundName}】吗？`, '删除确认', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
    fundList.value = fundList.value.filter(i => i.id !== item.id)
    selectedIds.value = selectedIds.value.filter(i => i !== item.id)
    ElMessage.success('删除成功')
  } catch (e) { /* cancel */ }
}

// ============ 一键导出 ============
const handleExport = () => {
  if (fundList.value.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }
  // 简单CSV导出
  const headers = ['基金名称', '基金代码', '基金类型', '成立日期', '资产规模(亿)', '基金管理人', '基金托管者', '基金经理', '运作方式', '申购费率(%)', '赎回费率(%)', '管理费(%)', '托管费(%)']
  const keys = ['fundName', 'fundCode', 'fundType', 'establishDate', 'assetScale', 'fundManager', 'fundCustodian', 'fundManagerName', 'operationMode', 'purchaseRate', 'redemptionRate', 'managementFee', 'custodianFee']
  let csv = '\uFEFF' + headers.join(',') + '\n'
  fundList.value.forEach(item => {
    csv += keys.map(k => `"${item[k] != null ? item[k] : ''}"`).join(',') + '\n'
  })
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = '基金资产数据.csv'
  link.click()
  URL.revokeObjectURL(link.href)
  ElMessage.success('导出成功')
}

// ============ 数据维护模态窗口 ============
const detailDialogVisible = ref(false)
const currentFundRow = ref({})

const openDetailDialog = (item) => {
  currentFundRow.value = JSON.parse(JSON.stringify(item))
  detailDialogVisible.value = true
}

const handleDetailSave = (saveData) => {
  const idx = fundList.value.findIndex(i => i.id === saveData.id)
  if (idx >= 0) {
    fundList.value[idx] = { ...saveData }
  }
  detailDialogVisible.value = false
}

// ============ 基金详情查看模态窗口 ============
const viewDialogVisible = ref(false)
const currentViewFund = ref({})

const openViewDialog = (item) => {
  currentViewFund.value = JSON.parse(JSON.stringify(item))
  viewDialogVisible.value = true
}
</script>

<style scoped>
.fund-management-container {
  width: 100%;
  min-height: 100vh;
  margin: 0;
  padding: 0;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8eef5 100%);
  display: flex;
  flex-direction: column;
  gap: 0;
  overflow: hidden;
  height: 100vh;
}

/* ====== 顶部标题栏 ====== */
.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  padding: 16px 28px;
  box-shadow: 0 4px 20px rgba(30, 60, 114, 0.3);
  flex-shrink: 0;
}

.top-bar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title-icon {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  backdrop-filter: blur(10px);
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 1px;
  font-family: 'Microsoft YaHei', sans-serif;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.top-bar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.top-bar-right :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: none;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.top-bar-right :deep(.el-input__wrapper:hover),
.top-bar-right :deep(.el-input__wrapper.is-focus) {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.5);
  box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.1);
}

.top-bar-right :deep(.el-input__inner) {
  color: #fff;
}

.top-bar-right :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.6);
}

.top-bar-right :deep(.el-input__prefix .el-icon) {
  color: rgba(255, 255, 255, 0.7);
}

.top-bar-right :deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 8px;
  color: #fff;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
  padding: 8px 20px;
}

.top-bar-right :deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.5);
}

/* ====== 查询区 ====== */
.search-section {
  background: #fff;
  padding: 20px 24px;
  border-bottom: 1px solid #e8ecf1;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.search-section :deep(.el-form-item__label) {
  font-weight: 600;
  color: #2c3e50;
}

.search-section :deep(.el-input__wrapper),
.search-section :deep(.el-select__wrapper),
.search-section :deep(.el-input-number) {
  border-radius: 6px;
  transition: all 0.3s ease;
}

.search-section :deep(.el-input__wrapper:hover),
.search-section :deep(.el-select__wrapper:hover) {
  box-shadow: 0 0 0 1px #409eff inset;
}

/* ====== 操作按钮区 ====== */
.action-section {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
  padding: 12px 24px;
  background: #fff;
  border-bottom: 1px solid #e8ecf1;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.03);
}

.action-btn-danger {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%) !important;
  border: none !important;
  border-radius: 8px !important;
  font-weight: 600 !important;
  color: #fff !important;
  box-shadow: 0 2px 8px rgba(240, 147, 251, 0.4) !important;
  transition: all 0.3s ease !important;
  padding: 8px 20px !important;
}

.action-btn-danger:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 4px 12px rgba(240, 147, 251, 0.5) !important;
}

.action-btn-export {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%) !important;
  border: none !important;
  border-radius: 8px !important;
  font-weight: 600 !important;
  color: #fff !important;
  box-shadow: 0 2px 8px rgba(79, 172, 254, 0.4) !important;
  transition: all 0.3s ease !important;
  padding: 8px 20px !important;
}

.action-btn-export:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 4px 12px rgba(79, 172, 254, 0.5) !important;
}

/* ====== 卡片列表区 ====== */
.card-list-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}

.card-scroll-wrapper {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
}

.card-scroll-wrapper::-webkit-scrollbar {
  width: 8px;
}

.card-scroll-wrapper::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.card-scroll-wrapper::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, #c0c4cc, #909399);
  border-radius: 4px;
}

.card-scroll-wrapper::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, #909399, #606266);
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

/* ====== 基金卡片 ====== */
.fund-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e8ecf1;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.fund-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.fund-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.15);
  border-color: #c0c4cc;
}

.fund-card:hover::before {
  opacity: 1;
}

.fund-card.card-selected {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
}

.fund-card.card-selected::before {
  opacity: 1;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
}

.card-checkbox {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 2;
}

.card-header {
  padding: 16px 20px 12px;
  border-bottom: 1px solid #f0f2f5;
  background: linear-gradient(135deg, #fafbfc 0%, #f5f7fa 100%);
}

.card-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}

.card-fund-name {
  font-size: 16px;
  font-weight: 700;
  color: #2c3e50;
  letter-spacing: 0.5px;
}

.card-fund-name.clickable {
  cursor: pointer;
  transition: all 0.3s ease;
  color: #1e3c72;
  text-decoration: underline;
  text-underline-offset: 3px;
  text-decoration-color: rgba(30, 60, 114, 0.4);
}

.card-fund-name.clickable:hover {
  color: #409eff;
  text-decoration-color: #409eff;
}

.card-code {
  font-size: 13px;
  color: #909399;
  font-family: 'Consolas', 'Monaco', monospace;
  letter-spacing: 0.5px;
}

.card-body {
  padding: 12px 20px;
}

.card-info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px 16px;
}

.info-cell {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

.info-label {
  font-size: 13px;
  color: #909399;
  white-space: nowrap;
}

.info-value {
  font-size: 14px;
  color: #303133;
  font-weight: 600;
  text-align: right;
}

.card-footer {
  padding: 10px 20px 14px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  border-top: 1px solid #f0f2f5;
  background: #fafbfc;
}

.card-action-btn-edit {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  border-radius: 6px !important;
  font-weight: 600 !important;
  color: #fff !important;
  box-shadow: 0 2px 6px rgba(102, 126, 234, 0.3) !important;
  transition: all 0.3s ease !important;
}

.card-action-btn-edit:hover {
  transform: translateY(-1px) !important;
  box-shadow: 0 3px 8px rgba(102, 126, 234, 0.4) !important;
}

.card-action-btn-delete {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%) !important;
  border: none !important;
  border-radius: 6px !important;
  font-weight: 600 !important;
  color: #fff !important;
  box-shadow: 0 2px 6px rgba(240, 147, 251, 0.3) !important;
  transition: all 0.3s ease !important;
}

.card-action-btn-delete:hover {
  transform: translateY(-1px) !important;
  box-shadow: 0 3px 8px rgba(240, 147, 251, 0.4) !important;
}

/* ====== 空状态 ====== */
.empty-state {
  padding: 80px 0;
  text-align: center;
}

/* ====== 分页 ====== */
.pagination-section {
  display: flex;
  justify-content: flex-end;
  padding: 12px 24px;
  border-top: 1px solid #e8ecf1;
  flex-shrink: 0;
  background: #fff;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.03);
}

.pagination-section :deep(.el-pagination) {
  gap: 8px;
}

.pagination-section :deep(.el-pagination button),
.pagination-section :deep(.el-pager li) {
  border-radius: 6px;
  transition: all 0.3s ease;
}

/* ====== 按钮美化 ====== */
.beautified-search-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  border-radius: 8px !important;
  font-weight: 600 !important;
  color: #fff !important;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4) !important;
  transition: all 0.3s ease !important;
  padding: 8px 20px !important;
}

.beautified-search-btn:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.5) !important;
}

.beautified-reset-btn {
  border-radius: 8px !important;
  font-weight: 600 !important;
  transition: all 0.3s ease !important;
  border: 1px solid #dcdfe6 !important;
}

.beautified-reset-btn:hover {
  transform: translateY(-2px) !important;
  background: #f5f7fa !important;
  border-color: #c0c4cc !important;
}

/* ====== 数据获取结果对话框 ====== */
.fetch-result-dialog :deep(.el-dialog) {
  border-radius: 16px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.fetch-result-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 24px;
  border-bottom: none;
}

.fetch-result-dialog :deep(.el-dialog__title) {
  color: #fff;
  font-weight: 700;
  font-size: 18px;
}

.fetch-result-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #fff;
}

.fetch-result-dialog :deep(.el-dialog__body) {
  padding: 24px;
  background: #fafbfc;
}

.fetch-result-dialog :deep(.el-form-item__label) {
  font-weight: 600;
  color: #2c3e50;
}

.fetch-result-dialog :deep(.el-input__wrapper),
.fetch-result-dialog :deep(.el-select__wrapper),
.fetch-result-dialog :deep(.el-input-number) {
  border-radius: 6px;
  transition: all 0.3s ease;
}

.fetch-result-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px;
  background: #fff;
  border-top: 1px solid #e8ecf1;
}

.dialog-cancel-btn {
  border-radius: 8px !important;
  font-weight: 600 !important;
  transition: all 0.3s ease !important;
  padding: 10px 24px !important;
}

.dialog-cancel-btn:hover {
  transform: translateY(-2px) !important;
}

.dialog-submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  border-radius: 8px !important;
  font-weight: 600 !important;
  color: #fff !important;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4) !important;
  transition: all 0.3s ease !important;
  padding: 10px 24px !important;
}

.dialog-submit-btn:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.5) !important;
}
</style>
