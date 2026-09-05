<template>
  <div class="trade-record-div">
    <div class="search-div">
      <div class="search-header">
        <span class="search-title">查询条件</span>
      </div>
      <el-form label-width="80px" size="small">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="股票名称">
              <el-input v-model="tradeRecordQueryDto.stockName" style="width: 100%" clearable placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="股票代码">
              <el-input v-model="tradeRecordQueryDto.stockCode" style="width: 100%" clearable placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="买卖方向">
              <el-select v-model="tradeRecordQueryDto.tradeDirection" style="width: 100%" clearable multiple placeholder="请选择">
                <el-option v-for="item in tradeDirectionOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="心理状态">
              <el-select v-model="tradeRecordQueryDto.psychology" style="width: 100%" clearable multiple placeholder="请选择">
                <el-option v-for="item in tradePsychologyOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="符合计划">
              <el-select v-model="tradeRecordQueryDto.followPlan" style="width: 100%" clearable multiple placeholder="请选择">
                <el-option v-for="item in tradePlanMatchOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="执行评分">
              <el-select v-model="tradeRecordQueryDto.executeRating" style="width: 100%" clearable multiple placeholder="请选择">
                <el-option v-for="item in tradeExecuteRatingOptions" :key="item.value" :label="item.text" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="交易时间">
              <el-date-picker v-model="tradeRecordTimeArea" type="datetimerange" range-separator="至" start-placeholder="开始" end-placeholder="结束" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" :unlink-panels="true" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24" style="text-align: right;">
            <el-form-item label-width="10px">
              <el-button type="primary" size="small" @click="searchTradeRecordData"><el-icon><Search /></el-icon>搜索</el-button>
              <el-button size="small" @click="resetTradeRecordData"><el-icon><Refresh /></el-icon>重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>
    <div class="tools-div beautified-tools" style="text-align: right;">
      <el-button type="success" size="small" @click="addTradeRecord"><el-icon><DocumentAdd /></el-icon>添加交易记录</el-button>
      <el-button type="danger" size="small" @click="deleteTradeRecordAll" :disabled="tradeRecordSelectedRows.length === 0"><el-icon><Delete /></el-icon>批量删除</el-button>
    </div>
    <el-table :data="tradeRecordList" v-loading="tradeRecordLoading" style="width: 100%" height="400" border stripe size="small" @selection-change="handleTradeRecordSelectionChange">
      <el-table-column type="selection" width="40" align="center" />
      <el-table-column label="操作" align="center" fixed="left" width="240" #default="scope">
        <el-button type="info" size="small" @click="viewTradeRecordDetail(scope.row)"><el-icon><View /></el-icon>查看</el-button>
        <el-button type="primary" size="small" @click="editTradeRecord(scope.row)"><el-icon><Edit /></el-icon>编辑</el-button>
        <el-button type="danger" size="small" @click="deleteTradeRecord(scope.row)"><el-icon><Delete /></el-icon>删除</el-button>
      </el-table-column>
      <el-table-column prop="tradeDatetime" label="交易时间" align="center" width="160" />
      <el-table-column prop="stockName" label="股票名称" align="center" width="100" show-overflow-tooltip />
      <el-table-column prop="stockCode" label="股票代码" align="center" width="90" />
      <el-table-column prop="tradeDirection" label="方向" align="center" width="70">
        <template #default="scope">
          <el-tag :type="scope.row.tradeDirection === 1 || scope.row.tradeDirection === 3 ? 'danger' : 'success'" size="small">{{ getDisplayText(scope.row.tradeDirection, tradeDirectionOptions) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="tradePrice" label="成交价" align="center" width="80">
        <template #default="scope">{{ scope.row.tradePrice != null ? Number(scope.row.tradePrice).toFixed(2) : '-' }}</template>
      </el-table-column>
      <el-table-column prop="tradeQuantity" label="数量" align="center" width="70" />
      <el-table-column prop="psychology" label="心理状态" align="center" width="80">
        <template #default="scope">{{ getDisplayText(scope.row.psychology, tradePsychologyOptions) }}</template>
      </el-table-column>
      <el-table-column prop="followPlan" label="符合计划" align="center" width="80">
        <template #default="scope">{{ getDisplayText(scope.row.followPlan, tradePlanMatchOptions) }}</template>
      </el-table-column>
      <el-table-column prop="profitPct" label="盈亏%" align="center" width="80">
        <template #default="scope">
          <span :style="{color: scope.row.profitPct > 0 ? '#F56C6C' : scope.row.profitPct < 0 ? '#67C23A' : '#909399'}">{{ scope.row.profitPct != null ? Number(scope.row.profitPct).toFixed(2) + '%' : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="executeRating" label="执行评分" align="center" width="90">
        <template #default="scope">{{ getDisplayText(scope.row.executeRating, tradeExecuteRatingOptions) }}</template>
      </el-table-column>
      <el-table-column prop="tradeReason" label="交易理由" align="center" min-width="120" show-overflow-tooltip />
    </el-table>
    <el-pagination style="margin-top: 15px" v-model:current-page="tradeRecordPageParams.page" v-model:page-size="tradeRecordPageParams.limit" :page-sizes="PAGE_SIZES" @size-change="fetchTradeRecordData" @current-change="fetchTradeRecordData" layout="total, sizes, prev, pager, next" :total="tradeRecordTotal" />
  </div>

  <!-- 交易记录编辑对话框 -->
  <el-dialog v-model="tradeRecordDialogVisible" :title="tradeRecordDialogTitle" width="70%" class="custom-dialog enhanced-dialog" :close-on-click-modal="false">
    <el-form :model="tradeRecordFormData" ref="tradeRecordFormRef" label-width="110px" size="small">
      <el-divider content-position="left">交易基本信息</el-divider>
      <el-row :gutter="20">
        <el-col :span="8"><el-form-item label="交易时间" prop="tradeDatetime" :rules="[{required:true,message:'请选择',trigger:'change'}]"><el-date-picker v-model="tradeRecordFormData.tradeDatetime" type="datetime" style="width:100%" value-format="YYYY-MM-DD HH:mm:ss" /></el-form-item></el-col>
        <el-col :span="8"><el-form-item label="股票名称" prop="stockName" :rules="[{required:true,message:'请输入',trigger:'blur'}]"><el-input v-model="tradeRecordFormData.stockName" style="width:100%" /></el-form-item></el-col>
        <el-col :span="8"><el-form-item label="股票代码"><el-input v-model="tradeRecordFormData.stockCode" style="width:100%" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="买卖方向" prop="tradeDirection" :rules="[{required:true,message:'请选择',trigger:'change'}]"><el-select v-model="tradeRecordFormData.tradeDirection" style="width:100%" clearable><el-option v-for="item in tradeDirectionOptions" :key="item.value" :label="item.text" :value="item.value" /></el-select></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="成交价" prop="tradePrice" :rules="[{required:true,message:'请输入',trigger:'blur'}]"><el-input-number v-model="tradeRecordFormData.tradePrice" :precision="3" :min="0" style="width:100%" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="成交数量" prop="tradeQuantity" :rules="[{required:true,message:'请输入',trigger:'blur'}]"><el-input-number v-model="tradeRecordFormData.tradeQuantity" :min="0" style="width:100%" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="成交金额"><el-input-number v-model="tradeRecordFormData.tradeAmount" :precision="2" style="width:100%" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="交易时段"><el-select v-model="tradeRecordFormData.timeSlot" style="width:100%" clearable><el-option v-for="item in tradeTimeSlotOptions" :key="item.value" :label="item.text" :value="item.value" /></el-select></el-form-item></el-col>
      </el-row>
      <el-divider content-position="left">交易背景</el-divider>
      <el-row :gutter="20">
        <el-col :span="8"><el-form-item label="当时大盘状态"><el-select v-model="tradeRecordFormData.marketStatus" style="width:100%" clearable><el-option v-for="item in reviewMarketStatusOptions" :key="item.value" :label="item.text" :value="item.value" /></el-select></el-form-item></el-col>
        <el-col :span="8"><el-form-item label="个股位置"><el-select v-model="tradeRecordFormData.stockPosition" style="width:100%" clearable><el-option v-for="item in tradePositionOptions" :key="item.value" :label="item.text" :value="item.value" /></el-select></el-form-item></el-col>
        <el-col :span="8"><el-form-item label="个股涨跌%"><el-input-number v-model="tradeRecordFormData.stockChangePct" :precision="2" style="width:100%" /></el-form-item></el-col>
      </el-row>
      <el-divider content-position="left">心理状态</el-divider>
      <el-row :gutter="20">
        <el-col :span="8"><el-form-item label="心理状态"><el-select v-model="tradeRecordFormData.psychology" style="width:100%" clearable><el-option v-for="item in tradePsychologyOptions" :key="item.value" :label="item.text" :value="item.value" /></el-select></el-form-item></el-col>
        <el-col :span="8"><el-form-item label="情绪强度(1-5)"><el-rate v-model="tradeRecordFormData.emotionIntensity" :max="5" /></el-form-item></el-col>
        <el-col :span="8"><el-form-item label="是否符合计划"><el-select v-model="tradeRecordFormData.followPlan" style="width:100%" clearable><el-option v-for="item in tradePlanMatchOptions" :key="item.value" :label="item.text" :value="item.value" /></el-select></el-form-item></el-col>
      </el-row>
      <el-divider content-position="left">交易逻辑</el-divider>
      <el-row :gutter="20">
        <el-col :span="24"><el-form-item label="为何交易"><el-input v-model="tradeRecordFormData.tradeReason" type="textarea" :rows="2" style="width:100%" /></el-form-item></el-col>
        <el-col :span="8"><el-form-item label="预期收益%"><el-input-number v-model="tradeRecordFormData.expectedProfitPct" :precision="2" style="width:100%" /></el-form-item></el-col>
        <el-col :span="8"><el-form-item label="止损价"><el-input-number v-model="tradeRecordFormData.stopLossPrice" :precision="3" style="width:100%" /></el-form-item></el-col>
        <el-col :span="8"><el-form-item label="止盈价"><el-input-number v-model="tradeRecordFormData.takeProfitPrice" :precision="3" style="width:100%" /></el-form-item></el-col>
      </el-row>
      <el-divider content-position="left">结果与反思</el-divider>
      <el-row :gutter="20">
        <el-col :span="6"><el-form-item label="盈亏%"><el-input-number v-model="tradeRecordFormData.profitPct" :precision="2" style="width:100%" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="持仓时长(天)"><el-input-number v-model="tradeRecordFormData.holdingDuration" style="width:100%" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="执行评分"><el-select v-model="tradeRecordFormData.executeRating" style="width:100%" clearable><el-option v-for="item in tradeExecuteRatingOptions" :key="item.value" :label="item.text" :value="item.value" /></el-select></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="关联复盘日期"><el-date-picker v-model="tradeRecordFormData.reviewDate" type="date" style="width:100%" value-format="YYYY-MM-DD" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="反思总结"><el-input v-model="tradeRecordFormData.reflection" type="textarea" :rows="2" style="width:100%" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="教训"><el-input v-model="tradeRecordFormData.lesson" type="textarea" :rows="2" style="width:100%" /></el-form-item></el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button @click="tradeRecordDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitTradeRecord">提交</el-button>
    </template>
  </el-dialog>

  <!-- 交易记录详情查看对话框 -->
  <el-dialog v-model="tradeRecordDetailVisible" title="交易记录详情" width="65%" class="custom-dialog enhanced-dialog" :close-on-click-modal="true">
    <div class="pred-detail-container" v-if="tradeRecordDetailData">
      <div class="detail-section">
        <div class="detail-section-title">交易基本信息</div>
        <el-descriptions :column="3" border size="default" label-width="100px">
          <el-descriptions-item label="交易时间"><span class="detail-value highlight">{{ tradeRecordDetailData.tradeDatetime || '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="股票名称"><span class="detail-value highlight">{{ tradeRecordDetailData.stockName || '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="股票代码"><span class="detail-value">{{ tradeRecordDetailData.stockCode || '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="买卖方向"><el-tag :type="tradeRecordDetailData.tradeDirection === 1 || tradeRecordDetailData.tradeDirection === 3 ? 'danger' : 'success'" size="small">{{ getDisplayText(tradeRecordDetailData.tradeDirection, tradeDirectionOptions) }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="成交价"><span class="detail-value">{{ tradeRecordDetailData.tradePrice != null ? '¥ ' + Number(tradeRecordDetailData.tradePrice).toFixed(3) : '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="成交数量"><span class="detail-value">{{ tradeRecordDetailData.tradeQuantity || '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="成交金额"><span class="detail-value">{{ tradeRecordDetailData.tradeAmount != null ? '¥ ' + Number(tradeRecordDetailData.tradeAmount).toFixed(2) : '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="交易时段">{{ getDisplayText(tradeRecordDetailData.timeSlot, tradeTimeSlotOptions) || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="detail-section">
        <div class="detail-section-title">交易背景</div>
        <el-descriptions :column="3" border size="default" label-width="100px">
          <el-descriptions-item label="当时大盘状态">{{ getDisplayText(tradeRecordDetailData.marketStatus, reviewMarketStatusOptions) || '-' }}</el-descriptions-item>
          <el-descriptions-item label="个股位置">{{ getDisplayText(tradeRecordDetailData.stockPosition, tradePositionOptions) || '-' }}</el-descriptions-item>
          <el-descriptions-item label="个股涨跌%"><span class="detail-value" :style="{color: tradeRecordDetailData.stockChangePct > 0 ? '#F56C6C' : '#67C23A'}">{{ tradeRecordDetailData.stockChangePct != null ? Number(tradeRecordDetailData.stockChangePct).toFixed(2) + '%' : '-' }}</span></el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="detail-section">
        <div class="detail-section-title">心理状态</div>
        <el-descriptions :column="3" border size="default" label-width="100px">
          <el-descriptions-item label="心理状态"><el-tag size="small">{{ getDisplayText(tradeRecordDetailData.psychology, tradePsychologyOptions) }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="情绪强度"><el-rate v-model="tradeRecordDetailData.emotionIntensity" :max="5" disabled /></el-descriptions-item>
          <el-descriptions-item label="是否符合计划"><el-tag :type="tradeRecordDetailData.followPlan === 1 ? 'success' : 'danger'" size="small">{{ getDisplayText(tradeRecordDetailData.followPlan, tradePlanMatchOptions) }}</el-tag></el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="detail-section">
        <div class="detail-section-title">交易逻辑</div>
        <el-descriptions :column="3" border size="default" label-width="100px">
          <el-descriptions-item label="为何交易" :span="3"><div class="detail-text-block">{{ tradeRecordDetailData.tradeReason || '-' }}</div></el-descriptions-item>
          <el-descriptions-item label="预期收益%"><span class="detail-value">{{ tradeRecordDetailData.expectedProfitPct != null ? Number(tradeRecordDetailData.expectedProfitPct).toFixed(2) + '%' : '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="止损价"><span class="detail-value">{{ tradeRecordDetailData.stopLossPrice != null ? '¥ ' + Number(tradeRecordDetailData.stopLossPrice).toFixed(3) : '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="止盈价"><span class="detail-value">{{ tradeRecordDetailData.takeProfitPrice != null ? '¥ ' + Number(tradeRecordDetailData.takeProfitPrice).toFixed(3) : '-' }}</span></el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="detail-section">
        <div class="detail-section-title">结果与反思</div>
        <el-descriptions :column="3" border size="default" label-width="100px">
          <el-descriptions-item label="盈亏%"><span class="detail-value" :style="{color: tradeRecordDetailData.profitPct > 0 ? '#F56C6C' : '#67C23A'}">{{ tradeRecordDetailData.profitPct != null ? Number(tradeRecordDetailData.profitPct).toFixed(2) + '%' : '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="持仓时长"><span class="detail-value">{{ tradeRecordDetailData.holdingDuration != null ? tradeRecordDetailData.holdingDuration + ' 分钟' : '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="执行评分"><el-tag size="small">{{ getDisplayText(tradeRecordDetailData.executeRating, tradeExecuteRatingOptions) }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="关联复盘日期"><span class="detail-value">{{ tradeRecordDetailData.reviewDate || '-' }}</span></el-descriptions-item>
          <el-descriptions-item label="反思总结" :span="3"><div class="detail-text-block">{{ tradeRecordDetailData.reflection || '-' }}</div></el-descriptions-item>
          <el-descriptions-item label="教训" :span="3"><div class="detail-text-block">{{ tradeRecordDetailData.lesson || '-' }}</div></el-descriptions-item>
        </el-descriptions>
      </div>
    </div>
    <template #footer><el-button @click="tradeRecordDetailVisible = false">关闭</el-button></template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, DocumentAdd, Delete, Edit, View } from '@element-plus/icons-vue'
import { GetKeyAndValueByType } from "@/api/sysDict"
import { GetTradeRecordByConditionAndPage, SaveTradeRecord, DeleteTradeRecordById, DeleteAllTradeRecordByIds } from "@/api/trialExecutionArea/tradeRecord"
import { getDisplayText } from "@/utils/common"

// ==================== 通用常量 ====================
const PAGE_SIZES = [10, 20, 50, 100]

// ==================== 数据字典 ====================
const loadDict = async (tableName, optionsRef) => {
  const result = await GetKeyAndValueByType(tableName)
  optionsRef.value = result.data || []
}

const tradeDirectionOptions = ref([])
const tradeTimeSlotOptions = ref([])
const tradePositionOptions = ref([])
const tradePsychologyOptions = ref([])
const tradePlanMatchOptions = ref([])
const tradeExecuteRatingOptions = ref([])
const reviewMarketStatusOptions = ref([])

// ==================== 交易记录管理 ====================
const tradeRecordList = ref([])
const tradeRecordTotal = ref(0)
const tradeRecordPageParams = reactive({ page: 1, limit: 10 })
const tradeRecordTimeArea = ref([])
const tradeRecordSelectedRows = ref([])
const tradeRecordQueryDto = reactive({ stockName: '', stockCode: '', tradeDirection: [], psychology: [], tradeTimeStart: null, tradeTimeEnd: null, followPlan: [], executeRating: [] })
const handleTradeRecordSelectionChange = (selection) => { tradeRecordSelectedRows.value = selection }

const tradeRecordLoading = ref(false)
const fetchTradeRecordData = async () => {
  tradeRecordLoading.value = true
  try {
    const result = await GetTradeRecordByConditionAndPage(tradeRecordPageParams.page, tradeRecordPageParams.limit, tradeRecordQueryDto)
    if (result.code === 200) {
      tradeRecordList.value = result.data.list || []
      tradeRecordTotal.value = result.data.total || 0
    }
  } catch (e) { ElMessage.error("查询交易记录失败") }
  finally { tradeRecordLoading.value = false }
}
const searchTradeRecordData = () => {
  tradeRecordQueryDto.tradeTimeStart = tradeRecordTimeArea.value?.[0] || null
  tradeRecordQueryDto.tradeTimeEnd = tradeRecordTimeArea.value?.[1] || null
  tradeRecordPageParams.page = 1
  fetchTradeRecordData()
}
const resetTradeRecordData = () => {
  tradeRecordTimeArea.value = []
  Object.assign(tradeRecordQueryDto, { stockName: '', stockCode: '', tradeDirection: [], psychology: [], tradeTimeStart: null, tradeTimeEnd: null, followPlan: [], executeRating: [] })
  tradeRecordPageParams.page = 1
  fetchTradeRecordData()
}

const tradeRecordDialogVisible = ref(false)
const tradeRecordDialogTitle = ref('')
const tradeRecordFormRef = ref(null)
const defaultTradeRecordForm = () => ({
  id: null, tradeDatetime: new Date().toISOString().slice(0, 19).replace('T', ' '),
  stockName: '', stockCode: '', tradeDirection: null, tradePrice: null, tradeQuantity: null, tradeAmount: null, timeSlot: null,
  marketStatus: null, stockPosition: null, stockChangePct: null,
  psychology: null, emotionIntensity: 3, followPlan: null,
  tradeReason: '', expectedProfitPct: null, stopLossPrice: null, takeProfitPrice: null,
  profitPct: null, holdingDuration: null, executeRating: null, reflection: '', lesson: '',
  reviewDate: null, remark: ''
})
const tradeRecordFormData = reactive(defaultTradeRecordForm())

// 交易记录录入时，交易时间变更自动带出关联复盘日期
watch(() => tradeRecordFormData.tradeDatetime, (val) => {
  if (val && val.length >= 10) {
    tradeRecordFormData.reviewDate = val.substring(0, 10)
  }
})

const addTradeRecord = () => {
  Object.assign(tradeRecordFormData, defaultTradeRecordForm())
  tradeRecordDialogTitle.value = '添加交易记录'
  tradeRecordDialogVisible.value = true
}
const editTradeRecord = (row) => {
  Object.assign(tradeRecordFormData, defaultTradeRecordForm(), row)
  tradeRecordDialogTitle.value = '编辑交易记录'
  tradeRecordDialogVisible.value = true
}
const tradeRecordDetailVisible = ref(false)
const tradeRecordDetailData = ref(null)
const viewTradeRecordDetail = (row) => {
  tradeRecordDetailData.value = { ...row }
  tradeRecordDetailVisible.value = true
}
const submitTradeRecord = async () => {
  if (!tradeRecordFormRef.value) return
  await tradeRecordFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      const result = await SaveTradeRecord(tradeRecordFormData)
      if (result.code === 200) {
        ElMessage.success("保存成功")
        tradeRecordDialogVisible.value = false
        fetchTradeRecordData()
      } else { ElMessage.error(result.message || "保存失败") }
    } catch (e) { ElMessage.error("保存交易记录失败") }
  })
}
const deleteTradeRecord = (row) => {
  ElMessageBox.confirm("确认删除该交易记录？", "提示", { type: "warning" }).then(async () => {
    const result = await DeleteTradeRecordById(row.id)
    if (result.code === 200) { ElMessage.success("删除成功"); fetchTradeRecordData() }
  }).catch(() => {})
}
const deleteTradeRecordAll = () => {
  ElMessageBox.confirm("确认批量删除选中记录？", "提示", { type: "warning" }).then(async () => {
    const ids = tradeRecordSelectedRows.value.map(r => r.id)
    const result = await DeleteAllTradeRecordByIds(ids)
    if (result.code === 200) { ElMessage.success("删除成功"); fetchTradeRecordData() }
  }).catch(() => {})
}

//--------------------钩子函数-------------------------
onMounted(() => {
  Promise.all([
    loadDict('t_trial_trade_direction', tradeDirectionOptions),
    loadDict('t_trial_trade_time_slot', tradeTimeSlotOptions),
    loadDict('t_trial_trade_position', tradePositionOptions),
    loadDict('t_trial_trade_psychology', tradePsychologyOptions),
    loadDict('t_trial_trade_plan_match', tradePlanMatchOptions),
    loadDict('t_trial_trade_execute_rating', tradeExecuteRatingOptions),
    loadDict('t_trial_review_market_status', reviewMarketStatusOptions)
  ]).catch(err => console.error('字典加载失败:', err))

  fetchTradeRecordData()
})
</script>

<style scoped>
.trade-record-div {
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

.beautified-tools .el-button:active {
  transform: translateY(0) scale(0.98);
}

/* 表格内按钮美化 */
:deep(.el-table .el-button) {
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  letter-spacing: 0.3px;
  border: none;
}

:deep(.el-table .el-button--primary) {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.3);
}

:deep(.el-table .el-button--danger) {
  background: linear-gradient(135deg, #F56C6C 0%, #f78989 100%);
  box-shadow: 0 2px 6px rgba(245, 108, 108, 0.3);
}

:deep(.el-table .el-button--info) {
  background: linear-gradient(135deg, #909399 0%, #a6a9ad 100%);
  box-shadow: 0 2px 6px rgba(144, 147, 153, 0.3);
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

:deep(.enhanced-dialog .el-dialog__body) {
  padding: 28px !important;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%) !important;
  max-height: 60vh !important;
  overflow-y: auto !important;
}

:deep(.enhanced-dialog .el-dialog__footer) {
  padding: 20px 28px !important;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%) !important;
  border-top: 2px solid rgba(102, 126, 234, 0.1) !important;
}

/* 分页组件样式 */
:deep(.el-pagination) {
  justify-content: center;
}

/* 详情样式 */
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
</style>