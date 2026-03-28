<template>
  <div class="container">

    <!-- 🔷 顶部统计 -->
    <div class="summary">
      <div class="summary-card total">
        <div class="label">总资产</div>
        <div class="value">¥ {{ formatMoney(totalAmount) }}</div>
      </div>
      <div class="summary-card" v-for="(item, key) in assetTypeSummary" :key="key">
        <div class="label">{{ getAssetTypeName(key) }}</div>
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
          <el-button
              type="success"
              size="small"
              @click="openDialog()"
          >
            <el-icon><DocumentAdd /></el-icon>
            新增
          </el-button>
        </el-row>
      </el-form>
    </div>

    <!-- 🔷 表格区 -->
    <div class="table-card">
      <div class="table-header">
        <span>资产列表</span>
      </div>

      <div class="table-wrapper">
        <el-table
            :data="tableData"
            border
            stripe
            height="100%"
        >
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
              {{ getDisplayText(scope.row.assetSubType, assetSubTypeItem) }}
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
        width="80%"
        :close-on-click-modal="false"
        :lock-scroll="false"
        align-center
        draggable
    >
      <el-form :model="form" label-width="120px">
        <!--    第一行    -->
        <el-row>
          <el-col :span="12">
            <el-form-item label="资产名称">
              <el-input v-model="form.assetName" />
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
            <el-form-item label="资产类型">
              <el-select v-model="form.assetType" @change="handleAssetTypeChange" style="width: 100%">
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
            <el-form-item label="资产状态">
              <el-select v-model="form.assetStatus" style="width: 100%">
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

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { GetAssetLedgerByConditionAndPage, SaveAssetLedger, DeleteAssetLedgerById } from "@/api/assetControl"
import { GetKeyAndValueByType } from "@/api/sysDict"
import { useApp } from "@/pinia/modules/app"

//--------------------钩子函数-------------------------
onMounted(() => {
  //1.加载数据字典
  getAssetTypeItem();
  getAssetStatusItem();

  //2.调用查询数据接口
  fetchData();
});

//----------------公共函数-------------------------
//通用方法：根据值和映射表获取中文文本
const getDisplayText = (value, mappingArray) => {
  if (!value && value !== 0) return '-'
  const foundItem = mappingArray.find(item => item.value === value)
  return foundItem ? foundItem.text : value
}

const formatMoney = (v) => Number(v || 0).toLocaleString()
//-------------------------------------------------

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
      tableData.value = result.data.records || []
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

const openDialog = async (row) => {
  dialogVisible.value = true
  if (row && row.id) {
    Object.assign(form, row)
    await getAssetSubTypeItem(row.assetType)
  } else {
    Object.assign(form, {
      assetName: "",
      assetCode: "",
      assetType: null,
      assetSubType: null,
      amount: 0,
      investAmount: 0,
      assetStatus: null,
      remark: ""
    })
    assetSubTypeItem.value = []
  }
}

const saveData = async () => {
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

</script>

<style scoped>
/* 页面背景 */
.container {
  height: 100vh;
  padding: 16px;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 总览 */
.summary {
  display: flex;
  gap: 12px;
}

.summary-card {
  flex: 1;
  padding: 14px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.summary-card.total {
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: #fff;
}

.value {
  font-size: 20px;
  font-weight: bold;
  margin-top: 6px;
}

/* 查询区（重点优化） */
.search-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.04);
}

/* 表格区 */
.table-card {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  padding: 0;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

/* 表头 */
.table-header {
  padding: 14px 20px;
  font-weight: 600;
  border-bottom: 1px solid #ebeef5;
}

/* 表格滚动 */
.table-wrapper {
  flex: 1;
  padding: 10px 20px 0;
  overflow: hidden;
}

/* 分页 */
.pagination {
  padding: 12px 20px;
  text-align: right;
  border-top: 1px solid #ebeef5;
}
</style>