<template>
  <div class="container">

    <!-- 🔷 顶部统计 -->
    <div class="summary">
      <div class="summary-card total">
        <div class="label">总资产</div>
        <div class="value">¥ {{ formatMoney(totalAmount) }}</div>
      </div>
      <div class="summary-card">储蓄 ¥ {{ formatMoney(summary.saving) }}</div>
      <div class="summary-card">基金 ¥ {{ formatMoney(summary.fund) }}</div>
      <div class="summary-card">股票 ¥ {{ formatMoney(summary.stock) }}</div>
      <div class="summary-card">保险 ¥ {{ formatMoney(summary.insurance) }}</div>
    </div>

    <!-- 🔷 查询区 -->
    <div class="search-card">
      <div class="search-left">
        <el-form :inline="true" :model="query">
          <el-form-item label="资产名称">
            <el-input v-model="query.name" placeholder="请输入名称" clearable />
          </el-form-item>

          <el-form-item label="类型">
            <el-select v-model="query.type" clearable placeholder="请选择">
              <el-option label="储蓄" value="储蓄" />
              <el-option label="基金" value="基金" />
              <el-option label="股票" value="股票" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <div class="search-right">
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <el-button type="success" @click="openDialog()">新增</el-button>
      </div>
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
          <el-table-column prop="name" label="资产名称" min-width="150" />
          <el-table-column prop="type" label="类型" width="100" />
          <el-table-column prop="subType" label="子类" min-width="150" />

          <el-table-column label="金额" width="140" align="right">
            <template #default="scope">
              ¥ {{ formatMoney(scope.row.amount) }}
            </template>
          </el-table-column>

          <el-table-column label="投入额" width="140" align="right">
            <template #default="scope">
              {{ scope.row.investAmount ? formatMoney(scope.row.investAmount) : "-" }}
            </template>
          </el-table-column>

          <el-table-column prop="status" label="状态" width="100" />
          <el-table-column prop="owner" label="所属人" width="100" />
          <el-table-column prop="updateTime" label="更新时间" width="160" />

          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button text size="small" @click="openDialog(scope.row)">编辑</el-button>
              <el-button text size="small" type="danger" @click="deleteRow(scope.$index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
            background
            layout="total, prev, pager, next"
            :total="50"
            :page-size="10"
        />
      </div>
    </div>

    <!-- 🔷 弹窗 -->
    <el-dialog v-model="dialogVisible" title="资产信息">
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>

        <el-form-item label="类型">
          <el-select v-model="form.type" @change="handleTypeChange">
            <el-option label="储蓄" value="储蓄" />
            <el-option label="基金" value="基金" />
          </el-select>
        </el-form-item>

        <el-form-item label="子类">
          <el-select v-model="form.subType">
            <el-option v-for="i in subTypeOptions" :key="i" :label="i" :value="i" />
          </el-select>
        </el-form-item>

        <el-form-item label="金额">
          <el-input v-model="form.amount" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveData">保存</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed } from "vue"

const query = reactive({ name: "", type: "" })

const tableData = ref([
  { name: "余额宝", type: "储蓄", subType: "支付宝", amount: 20000 }
])

const totalAmount = computed(() =>
    tableData.value.reduce((sum, i) => sum + Number(i.amount || 0), 0)
)

const summary = computed(() => ({
  saving: totalAmount.value,
  fund: 0,
  stock: 0,
  insurance: 0
}))

const dialogVisible = ref(false)
const form = reactive({})

const subTypeMap = {
  储蓄: ["银行卡", "微信"],
  基金: ["指数基金"]
}
const subTypeOptions = ref([])

const handleTypeChange = (val) => {
  subTypeOptions.value = subTypeMap[val] || []
}

const openDialog = (row) => {
  dialogVisible.value = true
  Object.assign(form, row || {})
}

const saveData = () => {
  tableData.value.push({ ...form })
  dialogVisible.value = false
}

const deleteRow = (i) => tableData.value.splice(i, 1)

const handleSearch = () => {}
const resetQuery = () => {}

const formatMoney = (v) => Number(v || 0).toLocaleString()
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 10px rgba(0,0,0,0.04);
}

.search-left {
  display: flex;
  align-items: center;
}

.search-right {
  display: flex;
  gap: 10px;
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