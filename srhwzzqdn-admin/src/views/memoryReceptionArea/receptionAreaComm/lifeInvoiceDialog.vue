<template>
  <el-dialog
    v-model="dialogVisible"
    title="💰 生活记账"
    width="900px"
    class="life-invoice-dialog"
    :close-on-click-modal="false"
    align-center
    draggable
    :fullscreen="isFullscreen"
    @open="initFullscreen"
  >
    <div class="dialog-body">
      <!-- 生活记忆信息卡片 -->
      <div class="memory-card">
        <div class="card-header">
          <div class="header-icon">🏠</div>
          <h3 class="card-title">生活记忆档案</h3>
        </div>
        
        <div class="card-content-wrapper">
          <div class="card-content">
            <div class="info-grid">
              <!-- 第一行：3列 -->
              <div class="info-item timeline">
                <div class="item-icon">⏰</div>
                <div class="item-content">
                  <div class="item-label">记忆时段</div>
                  <div class="item-value">
                    {{ getTimePeriodDisplay }}
                  </div>
                </div>
              </div>
              
              <div class="info-item type-item">
                <div class="item-icon">🏷️</div>
                <div class="item-content">
                  <div class="item-label">生活记忆类型</div>
                  <div class="item-value">
                    {{ getDisplayText(lifeMemoryData.lifeType, lifeMemoryTypeItem) }}
                  </div>
                </div>
              </div>
              
              <div class="info-item source-item">
                <div class="item-icon">📌</div>
                <div class="item-content">
                  <div class="item-label">记忆来源</div>
                  <div class="item-value">
                    {{ getDisplayText(lifeMemoryData.memorySource, lifeMemorySourceItem) }}
                  </div>
                </div>
              </div>
              
              <!-- 第二行：3列 -->
              <div class="info-item consume-type-item">
                <div class="item-icon">🛒</div>
                <div class="item-content">
                  <div class="item-label">消费类型</div>
                  <div class="item-value">
                    {{ getDisplayText(lifeMemoryData.consumeType, consumeTypeItem) }}
                  </div>
                </div>
              </div>
              
              <div class="info-item consume-amount-item">
                <div class="item-icon">💰</div>
                <div class="item-content">
                  <div class="item-label">消费金额</div>
                  <div class="item-value">
                    ¥{{ lifeMemoryData.lifeConsume || 0 }}
                  </div>
                </div>
              </div>
              
              <div class="info-item place-item">
                <div class="item-icon">📍</div>
                <div class="item-content">
                  <div class="item-label">记忆地点</div>
                  <div class="item-value">
                    {{ getMemoryPlaceDisplay(lifeMemoryData) }}
                  </div>
                </div>
              </div>
              
              <!-- 第三行：3列 -->
              <div class="info-item owner-item">
                <div class="item-icon">👑</div>
                <div class="item-content">
                  <div class="item-label">记忆所属人</div>
                  <div class="item-value">
                    {{ lifeMemoryData.memoryOwnerName || '-' }}
                  </div>
                </div>
              </div>
              
              <div class="info-item status-item">
                <div class="item-icon">📊</div>
                <div class="item-content">
                  <div class="item-label">记忆状态</div>
                  <div class="item-value">
                    {{ getDisplayText(lifeMemoryData.memoryStatus, memoryStatusItem) }}
                  </div>
                </div>
              </div>
              
              <div class="info-item id-item">
                <div class="item-icon">🔢</div>
                <div class="item-content">
                  <div class="item-label">记忆编号</div>
                  <div class="item-value">
                    {{ lifeMemoryData.memoryNo || '-' }}
                  </div>
                </div>
              </div>
              
              <!-- 长文本内容：单独行，高度自适应 -->
              <div class="info-item content-item long-text-item">
                <div class="item-icon">📝</div>
                <div class="item-content">
                  <div class="item-label">生活记忆内容</div>
                  <div class="item-value content-text">
                    {{ lifeMemoryData.lifeContent || '暂无记忆内容' }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!--  生活记账表单  -->
    <div class="memory-card">
      <div class="card-header">
        <div class="header-icon">💳</div>
        <h3 class="card-title">记账信息</h3>
      </div>

      <div class="card-content-wrapper">
        <div class="card-content">
          <div class="info-grid">
            <!-- 账单行为 -->
            <div class="info-item form-item">
              <div class="item-icon">✍️</div>
              <div class="item-content">
                <div class="item-label">账单行为</div>
                <div class="item-value">
                  <el-input
                    v-model="invoiceData.invoiceAction"
                    placeholder="请输入账单行为"
                    clearable
                  />
                </div>
              </div>
            </div>

            <!-- 记账时间（不可编辑，使用生活记忆结束时间） -->
            <div class="info-item form-item">
              <div class="item-icon">🕒</div>
              <div class="item-content">
                <div class="item-label">记账时间</div>
                <div class="item-value">
                  <el-input
                    v-model="invoiceData.recordTime"
                    disabled
                    placeholder="记账时间"
                  />
                </div>
              </div>
            </div>

            <!-- 金额（可编辑，默认使用生活消费） -->
            <div class="info-item form-item">
              <div class="item-icon">💰</div>
              <div class="item-content">
                <div class="item-label">金额</div>
                <div class="item-value">
                  <el-input-number
                    v-model="invoiceData.amount"
                    controls-position="right"
                    :min="0"
                    :max="100000"
                    :precision="2"
                    style="width: 100%"
                  />
                </div>
              </div>
            </div>

            <!-- 记账资产台账 -->
            <div class="info-item form-item">
              <div class="item-icon">🏦</div>
              <div class="item-content">
                <div class="item-label">记账资产台账</div>
                <div class="item-value">
                  <el-input
                    v-model="invoiceData.assetName"
                    placeholder="请选择资产台账"
                    readonly
                    @click="openAssetLedgerDialog"
                  >
                    <template #suffix>
                      <el-icon><ArrowDown /></el-icon>
                    </template>
                  </el-input>
                </div>
              </div>
            </div>

            <!-- 收支类型（可编辑，默认根据生活记忆中的消费类型） -->
            <div class="info-item form-item">
              <div class="item-icon">📊</div>
              <div class="item-content">
                <div class="item-label">收支类型</div>
                <div class="item-value">
                  <el-select
                    v-model="invoiceData.transactionType"
                    placeholder="请选择"
                    style="width: 100%"
                    clearable
                    @change="handleTransactionTypeChange"
                  >
                    <el-option
                      v-for="item in transactionTypeItem"
                      :key="item.value"
                      :label="item.text"
                      :value="item.value"
                    ></el-option>
                  </el-select>
                </div>
              </div>
            </div>

            <!-- 账单类型 -->
            <div class="info-item form-item">
              <div class="item-icon">📋</div>
              <div class="item-content">
                <div class="item-label">账单类型</div>
                <div class="item-value">
                  <el-select
                    v-model="invoiceData.invoiceType"
                    placeholder="请选择"
                    style="width: 100%"
                    clearable
                  >
                    <el-option
                      v-for="item in invoiceTypeItem"
                      :key="item.value"
                      :label="item.text"
                      :value="item.value"
                    ></el-option>
                  </el-select>
                </div>
              </div>
            </div>

            <!-- 支出类型（当收支类型为支出时显示） -->
            <div class="info-item form-item" v-if="invoiceData.transactionType === 2">
              <div class="item-icon">💸</div>
              <div class="item-content">
                <div class="item-label">支出类型</div>
                <div class="item-value">
                  <el-select
                    v-model="invoiceData.spendingType"
                    placeholder="请选择"
                    style="width: 100%"
                    clearable
                  >
                    <el-option
                      v-for="item in spendingTypeItem"
                      :key="item.value"
                      :label="item.text"
                      :value="item.value"
                    ></el-option>
                  </el-select>
                </div>
              </div>
            </div>

            <!-- 收益类型（当收支类型为收入时显示） -->
            <div class="info-item form-item" v-if="invoiceData.transactionType === 1">
              <div class="item-icon">📈</div>
              <div class="item-content">
                <div class="item-label">收益类型</div>
                <div class="item-value">
                  <el-select
                    v-model="invoiceData.incomeType"
                    placeholder="请选择"
                    style="width: 100%"
                    clearable
                  >
                    <el-option
                      v-for="item in incomeTypeItem"
                      :key="item.value"
                      :label="item.text"
                      :value="item.value"
                    ></el-option>
                  </el-select>
                </div>
              </div>
            </div>

            <!-- 结清状态 -->
            <div class="info-item form-item">
              <div class="item-icon">✅</div>
              <div class="item-content">
                <div class="item-label">结清状态</div>
                <div class="item-value">
                  <el-select
                    v-model="invoiceData.settlementStatus"
                    placeholder="请选择"
                    style="width: 100%"
                    clearable
                  >
                    <el-option
                      v-for="item in settlementStatusItem"
                      :key="item.value"
                      :label="item.text"
                      :value="item.value"
                    ></el-option>
                  </el-select>
                </div>
              </div>
            </div>

            <!-- 备注（可编辑，默认生活记忆内容） -->
            <div class="info-item content-item long-text-item">
              <div class="item-icon">📝</div>
              <div class="item-content">
                <div class="item-label">备注</div>
                <div class="item-value content-text">
                  <el-input
                    v-model="invoiceData.remark"
                    type="textarea"
                    :rows="4"
                    placeholder="请输入备注..."
                    resize="vertical"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 选择资产台账对话框 -->
    <el-dialog
      v-model="assetLedgerDialogVisible"
      title="选择资产台账"
      width="50%"
      :close-on-click-modal="false"
      append-to-body
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
              >
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button
                  size="small"
                  @click="resetAssetLedgerQuery"
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

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { GetKeyAndValueByType } from "@/api/sysDict"
import { GetAssetLedgerByConditionAndPage } from "@/api/assetControl"
import { SaveAssetAccounting } from "@/api/assetAccounting"
import {useFullscreenDialog} from "@/hooks/useFullscreenDialog";
import {UpdateLifeMemoryStatusById} from "@/api/memoryReception";

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  lifeMemoryData: {
    type: Object,
    default: () => ({})
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'submit'])

// 对话框可见性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 在需要全屏的组件中使用 Hook
const { isFullscreen, initFullscreen } = useFullscreenDialog()

// 记账表单数据
const invoiceData = reactive({
  lifeId: null,
  invoiceAction: '',
  recordTime: '',
  amount: 0,
  assetLedgerId: null,
  assetName: '',
  transactionType: '',
  invoiceType: '',
  spendingType: '',
  incomeType: '',
  settlementStatus: '',
  remark: '',
  dataSource: 2 // 数据来源默认为生活记忆转换
})

// 数据字典
const lifeMemoryTypeItem = ref([])
const lifeMemorySourceItem = ref([])
const consumeTypeItem = ref([])
const memoryStatusItem = ref([])
const transactionTypeItem = ref([])
const invoiceTypeItem = ref([])
const spendingTypeItem = ref([])
const incomeTypeItem = ref([])
const settlementStatusItem = ref([])
const assetLedgerTypeItem = ref([])
const assetLedgerSubTypeItem = ref([])
const assetLedgerStatusItem = ref([])

// 获取记忆时段显示
const getTimePeriodDisplay = computed(() => {
  if (props.lifeMemoryData.beginTime && props.lifeMemoryData.endTime) {
    return `${props.lifeMemoryData.beginTime} ~ ${props.lifeMemoryData.endTime}`
  }
  return '-'
})

// 获取地点显示
const getMemoryPlaceDisplay = (data) => {
  if (data.memoryPlace && Array.isArray(data.memoryPlace) && data.memoryPlace.length > 0) {
    return data.memoryPlace.join('/')
  }
  return data.memoryPlaceDetail || '-'
}

// 获取显示文本
const getDisplayText = (value, options) => {
  if (!value || !options || options.length === 0) return '-'
  const found = options.find(item => item.value == value)
  return found ? found.text : value
}

// 初始化数据
const initData = async () => {
  //表单重置
  invoiceData.invoiceAction = '';
  invoiceData.assetLedgerId = null;
  invoiceData.assetName = '';
  invoiceData.invoiceType = '';
  invoiceData.spendingType = '';
  invoiceData.incomeType = '';
  invoiceData.settlementStatus = '';

  // 设置默认值
  invoiceData.lifeId = props.lifeMemoryData.id || null
  invoiceData.recordTime = props.lifeMemoryData.endTime || ''
  invoiceData.amount = props.lifeMemoryData.lifeConsume || 0
  invoiceData.remark = props.lifeMemoryData.lifeContent || ''
  
  // 根据消费类型设置收支类型
  if (props.lifeMemoryData.consumeType) {
    // 假设消费类型1为收入，其他为支出
    invoiceData.transactionType = props.lifeMemoryData.consumeType === 1 ? 1 : 2
  }
  
  // 加载数据字典
  await loadDictData()
}

// 加载数据字典
const loadDictData = async () => {
  try {
    // 并行加载所有数据字典
    const [lifeType, lifeSource, consume, memoryStatus, transType, invType, spendType, incType, settleStatus, assetType, assetStatus] = await Promise.all([
      GetKeyAndValueByType("life_memory_type"),
      GetKeyAndValueByType("life_memory_source"),
      GetKeyAndValueByType("consume_type"),
      GetKeyAndValueByType("memory_status"),
      GetKeyAndValueByType("transaction_type"),
      GetKeyAndValueByType("invoice_type"),
      GetKeyAndValueByType("spending_type"),
      GetKeyAndValueByType("income_type"),
      GetKeyAndValueByType("settlement_status"),
      GetKeyAndValueByType("asset_type"),
      GetKeyAndValueByType("asset_status")
    ])
    
    lifeMemoryTypeItem.value = lifeType.data || []
    lifeMemorySourceItem.value = lifeSource.data || []
    consumeTypeItem.value = consume.data || []
    memoryStatusItem.value = memoryStatus.data || []
    transactionTypeItem.value = transType.data || []
    invoiceTypeItem.value = invType.data || []
    spendingTypeItem.value = spendType.data || []
    incomeTypeItem.value = incType.data || []
    settlementStatusItem.value = settleStatus.data || []
    assetLedgerTypeItem.value = assetType.data || []
    assetLedgerStatusItem.value = assetStatus.data || []
  } catch (error) {
    console.error('加载数据字典失败:', error)
    ElMessage.error('加载数据字典失败')
  }
}

// 收支类型改变事件
const handleTransactionTypeChange = (value) => {
  // 清空对应的支出类型或收益类型
  if (value === 1) {
    invoiceData.spendingType = ''
  } else if (value === 2) {
    invoiceData.incomeType = ''
  }
}

// ==================== 资产台账选择功能 ====================
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

// 打开资产台账选择对话框
const openAssetLedgerDialog = async () => {
  resetAssetLedgerQuery()
  assetLedgerDialogVisible.value = true
  await fetchAssetLedgerData()
}

// 获取资产台账子类数据字典
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

// 资产台账查询条件资产类型改变事件
const handleAssetLedgerQueryTypeChange = async (val) => {
  assetLedgerQuery.assetSubType = []
  await getAssetLedgerSubTypeItem(val)
}

// 预加载所有资产子类数据字典并处理数据
const processAssetLedgerSubTypeText = async (dataList) => {
  const allSubTypeData = {}
  const assetTypes = [...new Set(dataList.map(item => item.assetType).filter(Boolean))]

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
  invoiceData.assetLedgerId = row.id
  invoiceData.assetName = row.assetName
  assetLedgerDialogVisible.value = false
}

// 取消
const handleCancel = () => {
  dialogVisible.value = false
}

// 提交
const handleSubmit = async () => {
  // 验证必填字段
  if (!invoiceData.invoiceAction) {
    ElMessage.warning('请输入账单行为')
    return
  }
  if (!invoiceData.recordTime) {
    ElMessage.warning('记账时间不能为空')
    return
  }
  if (!invoiceData.amount || invoiceData.amount <= 0) {
    ElMessage.warning('请输入有效的金额')
    return
  }
  if (!invoiceData.assetLedgerId) {
    ElMessage.warning('请选择记账资产台账')
    return
  }
  if (!invoiceData.transactionType) {
    ElMessage.warning('请选择收支类型')
    return
  }
  if (!invoiceData.invoiceType) {
    ElMessage.warning('请选择账单类型')
    return
  }
  if (invoiceData.transactionType === 2 && !invoiceData.spendingType) {
    ElMessage.warning('请选择支出类型')
    return
  }
  if (invoiceData.transactionType === 1 && !invoiceData.incomeType) {
    ElMessage.warning('请选择收益类型')
    return
  }
  if (!invoiceData.settlementStatus) {
    ElMessage.warning('请选择结清状态')
    return
  }

  try {
    const result = await SaveAssetAccounting(invoiceData)
    if (result.code === 200) {
      ElMessage.success('记账成功')
      emit('submit')
      dialogVisible.value = false
    } else {
      ElMessage.error(result.message || '记账失败')
    }

    const {code, message} = await UpdateLifeMemoryStatusById(invoiceData.lifeId, 2);
    if (code === 200) {
      ElMessage.success('生活记忆状态更新成功')
    } else {
      ElMessage.error(message || '生活记忆状态更新失败')
    }
  } catch (error) {
    ElMessage.error('记账失败')
  }
}

// 监听对话框打开
const handleDialogOpen = async () => {
  await initData()
}

// 暴露方法
defineExpose({
  handleDialogOpen
})
</script>

<style scoped>
.dialog-body {
  margin-bottom: 20px;
}

.memory-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 15px 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  font-size: 24px;
}

.card-title {
  margin: 0;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
}

.card-content-wrapper {
  padding: 20px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px;
  border-radius: 6px;
  background-color: #f5f7fa;
  transition: all 0.3s;
}

.info-item:hover {
  background-color: #e6f7ff;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.item-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
  font-weight: 500;
}

.item-value {
  font-size: 14px;
  color: #303133;
  font-weight: 600;
  word-break: break-all;
}

.long-text-item {
  grid-column: 1 / span 3;
}

.content-text {
  line-height: 1.5;
  white-space: pre-wrap;
}

.form-item .item-value :deep(.el-input),
.form-item .item-value :deep(.el-input-number),
.form-item .item-value :deep(.el-select) {
  width: 100%;
}

.form-item .item-value :deep(.el-input__inner) {
  font-size: 14px;
  font-weight: 600;
}

.asset-ledger-search {
  margin-bottom: 15px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 6px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
