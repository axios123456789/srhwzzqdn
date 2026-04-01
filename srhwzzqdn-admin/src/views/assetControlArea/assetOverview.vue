<template>
  <div class="container">

    <!-- 🔷 顶部统计 -->
    <div class="summary">
      <div class="summary-card total">
        <div class="label">总资产</div>
        <div class="value">¥ {{ formatMoney(totalAmount) }}</div>
      </div>
      <div class="summary-card" v-for="(item, key) in assetTypeSummary" :key="key">
        <div class="label">{{ getAssetTypeName(Number(key)) }}</div>
        <div class="value">¥ {{ formatMoney(item) }}</div>
      </div>
    </div>

    <!-- 🔷 查询区 -->
    <div class="search-card">
      <el-form label-width="120px" size="small">
        <!--    第一行查询条件    -->
        <el-row>
          <el-col :span="6">
            <el-form-item label="资产名称">
              <el-input
                  v-model="query.assetName"
                  style="width: 100%"
                  clearable
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="资产编号">
              <el-input
                  v-model="query.assetNo"
                  style="width: 100%"
                  clearable
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="资产类型">
              <el-select
                  v-model="query.assetType"
                  placeholder="请选择"
                  style="width: 100%"
                  clearable
                  @change="handleQueryAssetTypeChange"
              >
                <el-option
                    v-for="item in assetTypeItem"
                    :key="item.value"
                    :label="item.text"
                    :value="item.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="资产子类">
              <el-select
                  v-model="query.assetSubType"
                  placeholder="请先选择资产类型"
                  style="width: 100%"
                  clearable
                  multiple
                  :disabled="!query.assetType"
              >
                <el-option
                    v-for="item in assetSubTypeItem"
                    :key="item.value"
                    :label="item.text"
                    :value="item.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <!--   第二行查询条件     -->
        <el-row>
          <el-col :span="6">
            <el-form-item label="资产金额(大于)">
              <el-input-number
                  v-model="query.amount"
                  :precision="2"
                  :step="1000"
                  style="width: 100%"
                  clearable
              ></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="资产状态">
              <el-select
                  v-model="query.assetStatus"
                  multiple
                  placeholder="请选择"
                  style="width: 100%"
                  clearable
              >
                <el-option
                    v-for="item in assetStatusItem"
                    :key="item.value"
                    :label="item.text"
                    :value="item.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row style="display:flex">
          <el-button
              type="primary"
              size="small"
              @click="handleSearch"
          >
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button
              size="small"
              @click="resetQuery"
          >
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-row>
      </el-form>
    </div>

    <!-- 🔷 表格区 -->
    <div class="table-card">
      <div class="table-header">
        <div class="header-left">
          <span>资产列表</span>
        </div>
        <div class="header-right">
          <el-button
              type="success"
              size="small"
              @click="openDialog()"
          >
            <el-icon><DocumentAdd /></el-icon>
            新增
          </el-button>
          <el-button
              type="info"
              size="small"
              @click="showExportDialog"
          >
            <el-icon><Download /></el-icon>
            一键导出
          </el-button>
          <el-button type="danger" @click="batchDelete" size="small">
            批量删除
          </el-button>
        </div>
      </div>

      <div class="table-wrapper">
        <el-table
            :data="tableData"
            border
            stripe
            height="500px"
            @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="assetName" label="资产名称" min-width="150" />
          <el-table-column prop="assetCode" label="资产官方标识" min-width="150" />
          <el-table-column prop="assetNo" label="资产编号" width="180" />
          <el-table-column prop="assetType" label="资产类型" width="100">
            <template #default="scope">
              {{ getDisplayText(scope.row.assetType, assetTypeItem) }}
            </template>
          </el-table-column>
          <el-table-column prop="assetSubType" label="资产子类" width="150">
            <template #default="scope">
              {{ scope.row._assetSubTypeText || scope.row.assetSubType || '-' }}
            </template>
          </el-table-column>

          <el-table-column label="资产金额" width="140" align="right">
            <template #default="scope">
              ¥ {{ formatMoney(scope.row.amount) }}
            </template>
          </el-table-column>

          <el-table-column label="投入金额" width="140" align="right">
            <template #default="scope">
              {{ scope.row.investAmount ? formatMoney(scope.row.investAmount) : "-" }}
            </template>
          </el-table-column>

          <el-table-column prop="assetStatus" label="资产状态" width="100">
            <template #default="scope">
              {{ getDisplayText(scope.row.assetStatus, assetStatusItem) }}
            </template>
          </el-table-column>

          <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
          <el-table-column prop="assetOwnerName" label="资产所属人" width="120" />
          <el-table-column prop="updateTime" label="更新时间" width="160" />
          <el-table-column prop="updateBy" label="更新人" width="120" />

          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button text size="small" @click="openDialog(scope.row)">编辑</el-button>
              <el-button text size="small" type="danger" @click="deleteRow(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
            background
            layout="total, sizes, prev, pager, next"
            :total="total"
            :page-size="pageParams.limit"
            :current-page="pageParams.page"
            :page-sizes="[10, 20, 50, 100]"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 🔷 弹窗 -->
    <el-dialog
        v-model="dialogVisible"
        :title="form.id ? '编辑资产台账' : '新增资产台账'"
        width="60%"
        :close-on-click-modal="false"
        :lock-scroll="false"
        align-center
        draggable
        :fullscreen="isFullscreen"
        @open="initFullscreen"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="120px">
        <!--    第一行    -->
        <el-row>
          <el-col :span="12">
            <el-form-item label="资产名称" prop="assetName">
              <el-input v-model="form.assetName" placeholder="请输入资产名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资产官方标识">
              <el-input v-model="form.assetCode" />
            </el-form-item>
          </el-col>
        </el-row>
        <!--   第二行     -->
        <el-row>
          <el-col :span="12">
            <el-form-item label="资产类型" prop="assetType">
              <el-select v-model="form.assetType" @change="handleAssetTypeChange" style="width: 100%" placeholder="请选择资产类型">
                <el-option
                    v-for="item in assetTypeItem"
                    :key="item.value"
                    :label="item.text"
                    :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资产子类">
              <el-select v-model="form.assetSubType" placeholder="请先选择资产类型" style="width: 100%" :disabled="!form.assetType">
                <el-option
                    v-for="item in assetSubTypeItem"
                    :key="item.value"
                    :label="item.text"
                    :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <!--    第三行    -->
        <el-row>
          <el-col :span="12">
            <el-form-item label="资产金额">
              <el-input-number v-model="form.amount" :precision="2" :step="1000" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="投入金额">
              <el-input-number v-model="form.investAmount" :precision="2" :step="1000" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <!--    第四行    -->
        <el-row>
          <el-col :span="12">
            <el-form-item label="资产状态" prop="assetStatus">
              <el-select v-model="form.assetStatus" style="width: 100%" placeholder="请选择资产状态">
                <el-option
                    v-for="item in assetStatusItem"
                    :key="item.value"
                    :label="item.text"
                    :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资产所属人">
              <el-input v-model="form.assetOwnerName" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <!--    第五行    -->
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input
                  type="textarea"
                  style="width: 100%"
                  :rows="3"
                  placeholder="请输入备注"
                  v-model="form.remark"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveData">保存</el-button>
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
        :current-count="tableData.length"
        :total-count="total"
        @confirm="handleExport"
        @closed="resetExport"
    />

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { GetAssetLedgerByConditionAndPage, SaveAssetLedger, DeleteAssetLedgerById, DeleteAllAssetLedgerByIds } from "@/api/assetControl"
import { GetKeyAndValueByType } from "@/api/sysDict"
import { useExport } from "@/components/Export/hooks/useExport"
import ExportDialog from '@/components/Export/ExportDialog.vue'
import {useFullscreenDialog} from "@/hooks/useFullscreenDialog";

// 在需要全屏的组件中使用 Hook
const { isFullscreen, initFullscreen } = useFullscreenDialog()

//--------------------钩子函数-------------------------
onMounted(() => {
  //1.加载数据字典
  getAssetTypeItem();
  getAssetStatusItem();

  //2.调用查询数据接口
  fetchData();
});

//----------------公共函数-----------------------------
//通用方法：根据值和映射表获取中文文本
const getDisplayText = (value, mappingArray) => {
  if (!value && value !== 0) return '-'
  const foundItem = mappingArray.find(item => item.value === value)
  return foundItem ? foundItem.text : value
}

const formatMoney = (v) => Number(v || 0).toLocaleString()
//--------------------------------------------------------

//---------------------------一键导出功能---------------------------------
// 导出列定义
const exportColumns = [
  { key: 'assetName', label: '资产名称', width: 20 },
  { key: 'assetCode', label: '资产官方标识', width: 20 },
  { key: 'assetNo', label: '资产编号', width: 25 },
  { key: 'assetType', label: '资产类型', width: 12 },
  { key: 'assetSubType', label: '资产子类', width: 15 },
  { key: 'amount', label: '资产金额', width: 15 },
  { key: 'investAmount', label: '投入金额', width: 15 },
  { key: 'assetStatus', label: '资产状态', width: 12 },
  { key: 'remark', label: '备注', width: 30 },
  { key: 'assetOwnerName', label: '资产所属人', width: 12 },
  { key: 'updateTime', label: '更新时间', width: 20 },
  { key: 'updateBy', label: '更新人', width: 12 },
]

// 数据格式化函数
const assetDataFormatter = (item, key, value) => {
  switch (key) {
    case 'assetType':
      return getDisplayText(value, assetTypeItem.value)
    case 'assetSubType':
      // 使用预处理的子类文本
      return item._assetSubTypeText || value || '-'
    case 'assetStatus':
      return getDisplayText(value, assetStatusItem.value)
    case 'amount':
    case 'investAmount':
      return value ? formatMoney(value) : '-'
    default:
      return value
  }
}

// 预加载所有资产子类数据字典并处理数据
const processAssetSubTypeText = async (dataList) => {
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

// 获取全部数据的函数
const getAllAssetLedgerData = async () => {
  try {
    const result = await GetAssetLedgerByConditionAndPage(1, 999999, query)
    if (result.code === 200) {
      const records = result.data.records || []
      // 预处理资产子类文本
      await processAssetSubTypeText(records)
      return records
    } else {
      ElMessage.error(result.message || "获取数据失败")
      return []
    }
  } catch (error) {
    ElMessage.error("获取数据失败")
    return []
  }
}

// 使用导出hook
const {
  exportDialogVisible,
  exportScope,
  exportFileName,
  exportLoading,
  selectedColumns,
  showExportDialog: showExport,
  handleExport,
  resetExport
} = useExport({
  availableColumns: exportColumns,
  dataFormatter: assetDataFormatter,
  fetchAllData: getAllAssetLedgerData,
  defaultFileName: '个人资产台账数据',
  sheetName: '个人资产台账数据'
})

// 打开导出对话框
const showExportDialog = async () => {
  // 预处理当前页数据的资产子类文本
  const currentData = JSON.parse(JSON.stringify(tableData.value))
  await processAssetSubTypeText(currentData)
  showExport(currentData, total.value)
}
//---------------------------------------------------------------

//-------------------------数据字典获取------------------------------
//数据字典
const assetTypeItem = ref([])
const assetSubTypeItem = ref([])
const assetStatusItem = ref([])

//获取资产类型数据字典
const getAssetTypeItem = async () => {
    const result = await GetKeyAndValueByType("asset_type")
    assetTypeItem.value = result.data
}

//获取资产子类数据字典
const getAssetSubTypeItem = async (assetTypeValue) => {
  try {
    if (!assetTypeValue) {
      assetSubTypeItem.value = []
      return
    }
    const type = `asset_type_${assetTypeValue}`
    const result = await GetKeyAndValueByType(type)
    if (result.code === 200) {
      assetSubTypeItem.value = result.data
    } else {
      assetSubTypeItem.value = []
    }
  } catch (error) {
    assetSubTypeItem.value = []
  }
}

//获取资产状态数据字典
const getAssetStatusItem = async () => {
    const result = await GetKeyAndValueByType("asset_status")
    assetStatusItem.value = result.data
}

//新增修改模态窗口资产类型改变时加载对应的子类
const handleAssetTypeChange = async (val) => {
  form.assetSubType = []
  await getAssetSubTypeItem(val)
}

//查询条件中资产类型改变时加载对应的子类
const handleQueryAssetTypeChange = async (val) => {
  query.assetSubType = []
  await getAssetSubTypeItem(val)
}

//获取资产类型名称
const getAssetTypeName = (value) => {
  return getDisplayText(value, assetTypeItem.value)
}
//---------------------------------------------------------------------

//-------------------------列表展示区-----------------------------------
const query = reactive({
  assetName: "",
  assetNo: "",
  assetType: null,
  assetSubType: [],
  amount: null,
  assetStatus: []
})

const pageParams = reactive({
  page: 1,
  limit: 10
})

const tableData = ref([])
const total = ref(0)
const assetTypeSummary = ref({})
const selectedRows = ref([])

const totalAmount = computed(() => {
  let sum = 0
  for (let key in assetTypeSummary.value) {
    sum += Number(assetTypeSummary.value[key] || 0)
  }
  return sum
})

const handleSearch = () => {
  pageParams.page = 1
  fetchData()
}

const resetQuery = () => {
  Object.assign(query, {
    assetName: "",
    assetNo: "",
    assetType: null,
    assetSubType: [],
    amount: null,
    assetStatus: []
  })
  assetSubTypeItem.value = []
  pageParams.page = 1
  fetchData()
}

const handleSizeChange = (size) => {
  pageParams.limit = size
  pageParams.page = 1
  fetchData()
}

const handleCurrentChange = (page) => {
  pageParams.page = page
  fetchData()
}

const fetchData = async () => {
  try {
    const result = await GetAssetLedgerByConditionAndPage(pageParams.page, pageParams.limit, query)
    if (result.code === 200) {
      const records = result.data.records || []
      // 预处理资产子类文本，使其在列表中正确显示
      await processAssetSubTypeText(records)
      tableData.value = records
      total.value = result.data.total || 0
      assetTypeSummary.value = result.data.assetTypeSummary || {}
    } else {
      ElMessage.error(result.message || "查询失败")
    }
  } catch (error) {
    ElMessage.error("查询失败")
  }
}

//------------------------资产台账修改/新增-------------------------
const dialogVisible = ref(false)
const form = reactive({})
const formRef = ref(null)

// 表单校验规则
const formRules = {
  assetName: [
    { required: true, message: '请输入资产名称', trigger: 'blur' }
  ],
  assetType: [
    { required: true, message: '请选择资产类型', trigger: 'change' }
  ],
  assetStatus: [
    { required: true, message: '请选择资产状态', trigger: 'change' }
  ]
}

const openDialog = async (row) => {
  dialogVisible.value = true
  // 清空表单校验
  if (formRef.value) {
    formRef.value.resetFields()
  }

  if (row && row.id) {
    // 编辑模式:复制整行数据(包含 id)
    Object.assign(form, row)
    await getAssetSubTypeItem(row.assetType)
  } else {
    // 新增模式:清空所有字段,包括 id
    Object.assign(form, {
      id: null,  // ✅ 关键修复:清空 id,确保是新增操作
      assetName: "",
      assetCode: "",
      assetType: null,
      assetSubType: null,
      amount: 0,
      investAmount: 0,
      assetStatus: 1, // 默认设置为"正常"
      remark: ""
    })
    assetSubTypeItem.value = []
  }
}

const saveData = async () => {
  // 表单校验
  if (!formRef.value) return
  
  try {
    const valid = await formRef.value.validate()
    if (!valid) return
  } catch (error) {
    return
  }
  
  try {
    const result = await SaveAssetLedger(form)
    if (result.code === 200) {
      ElMessage.success("保存成功")
      dialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(result.message || "保存失败")
    }
  } catch (error) {
    ElMessage.error("保存失败")
  }
}

//---------------------------删除资产台账-------------------------------
const deleteRow = async (id) => {
  try {
    await ElMessageBox.confirm("确定要删除该资产台账吗?", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning"
    })
    const result = await DeleteAssetLedgerById(id)
    if (result.code === 200) {
      ElMessage.success("删除成功")
      fetchData()
    } else {
      ElMessage.error(result.message || "删除失败")
    }
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error("删除失败")
    }
  }
}

// 处理表格选择变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 批量删除
const batchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要删除的记录')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 条记录吗?`,
      '批量删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const ids = selectedRows.value.map(row => row.id)
    const result = await DeleteAllAssetLedgerByIds(ids)
    if (result.code === 200) {
      ElMessage.success('批量删除成功')
      fetchData()
    } else {
      ElMessage.error(result.message || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

</script>

<style scoped>
/* 页面背景 - 金融风格 */
.container {
  height: 100vh;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8eef5 100%);
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 顶部统计卡片 - 资产台账风格 */
.summary {
  display: flex;
  gap: 16px;
}

.summary-card {
  flex: 1;
  padding: 20px 24px;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.summary-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.summary-card.total {
  background: linear-gradient(135deg, #2c5aa0 0%, #4a7bc7 100%);
  color: #fff;
  border: none;
  position: relative;
  overflow: hidden;
}

.summary-card.total::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  animation: shimmer 3s infinite;
}

@keyframes shimmer {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.summary-card .label {
  font-size: 14px;
  opacity: 0.9;
  font-weight: 500;
}

.summary-card .value {
  font-size: 24px;
  font-weight: 700;
  margin-top: 8px;
  font-family: 'Arial', sans-serif;
  letter-spacing: 0.5px;
}

/* 查询区 - 专业金融风格 */
.search-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px 28px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  border: 1px solid #e8eef5;
}

.search-card :deep(.el-form-item__label) {
  font-weight: 500;
  color: #2c3e50;
}

.search-card :deep(.el-input__wrapper),
.search-card :deep(.el-select__wrapper),
.search-card :deep(.el-input-number) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.search-card :deep(.el-input__wrapper:hover),
.search-card :deep(.el-select__wrapper:hover) {
  box-shadow: 0 0 0 1px #4a7bc7 inset;
}

/* 搜索按钮美化 */
.search-card :deep(.el-button--primary) {
  background: linear-gradient(135deg, #2c5aa0 0%, #4a7bc7 100%);
  border: none;
  border-radius: 8px;
  padding: 8px 20px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.search-card :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(44, 90, 160, 0.4);
}

.search-card :deep(.el-button--default) {
  border-radius: 8px;
  padding: 8px 20px;
  font-weight: 500;
  border: 1px solid #dcdfe6;
  transition: all 0.3s ease;
}

.search-card :deep(.el-button--default:hover) {
  border-color: #4a7bc7;
  color: #4a7bc7;
}

/* 表格区 - 资产台账专业风格 */
.table-card {
  flex: 1;
  background: #fff;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  padding: 0;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  border: 1px solid #e8eef5;
}

/* 表头 - 金融风格 */
.table-header {
  padding: 18px 24px;
  font-weight: 600;
  border-bottom: 2px solid #e8eef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(to right, #fafbfc, #fff);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  color: #2c3e50;
  font-weight: 600;
}

.header-right {
  display: flex;
  gap: 10px;
}

/* 操作按钮美化 */
.header-right :deep(.el-button--success) {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  border: none;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.header-right :deep(.el-button--success:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.4);
}

.header-right :deep(.el-button--info) {
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  border: none;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.header-right :deep(.el-button--info:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.4);
}

.header-right :deep(.el-button--danger) {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  border: none;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.header-right :deep(.el-button--danger:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.4);
}

/* 表格滚动 */
.table-wrapper {
  flex: 1;
  padding: 12px 24px 0;
  overflow: hidden;
}

/* 表格样式美化 */
.table-wrapper :deep(.el-table) {
  border-radius: 8px;
}

.table-wrapper :deep(.el-table th) {
  background: #fafbfc;
  color: #2c3e50;
  font-weight: 600;
  font-size: 14px;
}

.table-wrapper :deep(.el-table td) {
  font-size: 14px;
}

.table-wrapper :deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: #fafbfc;
}

.table-wrapper :deep(.el-table__body tr:hover > td) {
  background: #e6f7ff !important;
}

/* 金额列特殊样式 */
.table-wrapper :deep(.el-table td[data-label="资产金额"]),
.table-wrapper :deep(.el-table td[data-label="投入金额"]) {
  font-weight: 600;
  color: #2c5aa0;
  font-family: 'Arial', sans-serif;
}

/* 操作按钮 */
.table-wrapper :deep(.el-button--text) {
  font-weight: 500;
  transition: all 0.3s ease;
}

.table-wrapper :deep(.el-button--text:hover) {
  transform: scale(1.05);
}

/* 分页 - 金融风格 */
.pagination {
  padding: 16px 24px;
  text-align: right;
  border-top: 2px solid #e8eef5;
  background: #fafbfc;
  border-radius: 0 0 16px 16px;
}

.pagination :deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background: linear-gradient(135deg, #2c5aa0 0%, #4a7bc7 100%);
  border-radius: 6px;
}

.pagination :deep(.el-pagination.is-background .el-pager li:not(.is-disabled):hover) {
  color: #4a7bc7;
}

/* 弹窗美化 */
:deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  background: linear-gradient(135deg, #2c5aa0 0%, #4a7bc7 100%);
  padding: 20px 24px;
  margin: 0;
}

:deep(.el-dialog__title) {
  color: #fff;
  font-weight: 600;
  font-size: 18px;
}

:deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #fff;
  font-size: 20px;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #e8eef5;
  background: #fafbfc;
}

/* 表单项美化 */
:deep(.el-form-item__label) {
  font-weight: 500;
  color: #2c3e50;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper),
:deep(.el-textarea__inner) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover),
:deep(.el-select__wrapper:hover),
:deep(.el-textarea__inner:hover) {
  box-shadow: 0 0 0 1px #4a7bc7 inset;
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-select__wrapper.is-focus),
:deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px #4a7bc7 inset !important;
}

/* 保存按钮 */
:deep(.el-button--primary) {
  background: linear-gradient(135deg, #2c5aa0 0%, #4a7bc7 100%);
  border: none;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(44, 90, 160, 0.4);
}
</style>