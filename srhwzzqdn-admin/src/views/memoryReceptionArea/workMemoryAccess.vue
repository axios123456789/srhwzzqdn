<template>
  <div class="workMemoryAccessDT">
    <h1 style="margin-top: 10px; font-family: 方正姚体; color: black">
      &emsp;
      <el-icon><briefcase /></el-icon>
      工作记忆接入
    </h1>
    <!--  搜索表单  -->
    <div class="search-div">
      <el-form label-width="120px" size="small">
        <!--    第一行查询条件    -->
        <el-row>
          <el-col :span="6">
            <el-form-item label="记忆编号">
              <el-input
                v-model="workMemoryQueryDto.memoryNo"
                style="width: 100%"
                clearable
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="记忆来源">
              <el-select
                v-model="workMemoryQueryDto.memorySource"
                multiple
                placeholder="请选择"
                style="width: 100%"
                clearable
              >
                <el-option
                  v-for="item in workMemorySourceItem"
                  :key="item.value"
                  :label="item.text"
                  :value="item.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="记忆时间">
              <el-date-picker
                v-model="beginTimeArea"
                type="datetimerange"
                range-separator="To"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                :editable="false"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>
        <!--   第二行查询条件     -->
        <el-row>
          <el-col :span="6">
            <el-form-item label="业务类型">
              <el-tree-select
                v-model="workMemoryQueryDto.workBusinessType"
                :data="workBusinessTypeItem"
                show-checkbox
                check-strictly
                node-key="value"
                :props="{
                  label: 'text',
                  value: 'value',
                  children: 'children',
                }"
                placeholder="请选择"
                style="width: 100%"
                clearable
              ></el-tree-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="技术类型">
              <el-tree-select
                v-model="workMemoryQueryDto.workTechType"
                :data="workTechTypeItem"
                show-checkbox
                check-strictly
                node-key="value"
                :props="{
                  label: 'text',
                  value: 'value',
                  children: 'children',
                }"
                placeholder="请选择"
                style="width: 100%"
                clearable
              ></el-tree-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row style="display:flex">
          <el-button
            type="primary"
            size="small"
            @click="searchWorkMemory"
            class="beautified-search-btn"
          >
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button
            size="small"
            @click="resetWorkMemory"
            class="beautified-reset-btn"
          >
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-row>
      </el-form>
    </div>

    <!-- 列表展示  -->
    <el-table
      :data="workList"
      style="width: 100%"
      height="300"
      ref="multipleTable"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="40"></el-table-column>
      <el-table-column label="操作" align="center" width="280" #default="scope">
        <div class="action-buttons">
          <el-button
            type="primary"
            size="small"
            @click="editWorkMemory(scope.row)"
            class="beautified-edit-btn"
          >
            <el-icon><Edit /></el-icon>
            记忆编辑
          </el-button>
          <el-button
            type="danger"
            size="small"
            @click="deleteWorkMemoryById(scope.row)"
            class="beautified-delete-btn"
          >
            <el-icon><Delete /></el-icon>
            记忆删除
          </el-button>
          <el-button
            type="warning"
            size="small"
            @click="memoryAssociative(scope.row)"
            class="beautified-associative-btn"
          >
            <el-icon><Connection /></el-icon>
            记忆联想
          </el-button>
        </div>
      </el-table-column>
      <el-table-column
        prop="workBusinessType"
        label="工作业务类型"
        width="120"
        #default="scope"
      >
        {{
          getDisplayTextByTree(scope.row.workBusinessType, workBusinessTypeItem)
        }}
      </el-table-column>
      <el-table-column
        prop="workTechType"
        label="工作技术类型"
        width="120"
        #default="scope"
      >
        {{ getDisplayTextByTree(scope.row.workTechType, workTechTypeItem) }}
      </el-table-column>
      <el-table-column prop="memoryNo" label="记忆编号" width="180" />
      <el-table-column prop="beginTime" label="工作开始时间" width="180" />
      <el-table-column prop="endTime" label="工作结束时间" width="180" />
      <el-table-column prop="workDuration" label="工作时长（h）" width="120" />
      <el-table-column
        prop="memorySource"
        label="记忆来源"
        width="120"
        #default="scope"
      >
        {{ getDisplayText(scope.row.memorySource, workMemorySourceItem) }}
      </el-table-column>
      <el-table-column
        prop="memoryImages"
        label="工作记录图"
        #default="scope"
        width="200"
      >
        <div style="float: left;">
          <img
            v-for="(item, index) in scope.row.memoryImages"
            :key="index"
            :src="item"
            width="50"
            height="50"
          />
        </div>
      </el-table-column>
      <el-table-column prop="workContent" label="工作内容" width="300" />
      <el-table-column
        prop="workBusinessNode"
        label="工作业务笔记"
        width="180"
      />
      <el-table-column prop="workTechNode" label="工作技术笔记" width="180" />
      <el-table-column prop="memoryOwnerName" label="记忆所属人" width="120" />
      <!--  工作文档    -->
      <el-table-column prop="updateTime" label="修改时间" width="180" />
      <el-table-column prop="updateBy" label="修改者" width="120" />
    </el-table>

    <!--分页条-->
    <el-pagination
      style="margin-top: 30px"
      v-model:current-page="workPageParams.page"
      v-model:page-size="workPageParams.limit"
      :page-sizes="[10, 20, 50, 100]"
      @size-change="workFetchData"
      @current-change="workFetchData"
      layout="total, sizes, prev, pager, next"
      :total="workTotal"
    />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import {
  GetAdministrative,
  GetAllSysCode,
  GetKeyAndValueByType,
} from '@/api/sysDict'
import { GetWorkMemoryByConditionAndPage } from '@/api/memoryReception'
import { getTodayTimeRange } from '@/utils/common'

//----------------钩子函数-------------------
onMounted(() => {
  //1.加载数据字典
  getWorkMemorySourceItem() //工作记忆来源
  getWorkBusinessTypeItem() //工作记忆业务类型
  getWorkTechTypeItem() //工作记忆技术类型
  getFormattedAddressOptions() //行政区划

  //2.查询条件设置默认时间为今天
  const [startOfDay, endOfDay] = getTodayTimeRange()
  beginTimeArea.value[0] = startOfDay
  beginTimeArea.value[1] = endOfDay
  workMemoryQueryDto.value.beginTime = startOfDay
  workMemoryQueryDto.value.endTime = endOfDay

  //3.调用查询数据接口
  workFetchData()
})

//-----------------------------------数据字典获取-----------------------------------
//获取记忆来源下拉列表
const workMemorySourceItem = ref([])
const getWorkMemorySourceItem = async () => {
  const { data } = await GetKeyAndValueByType('t_work_memory_source')
  workMemorySourceItem.value = data
}
//获取业务类型下拉列表
const workBusinessTypeItem = ref([])
const getWorkBusinessTypeItem = async () => {
  const { data } = await GetAllSysCode('t_work_memory_business_type')
  workBusinessTypeItem.value = data
}
//获取技术类型下拉列表
const workTechTypeItem = ref([])
const getWorkTechTypeItem = async () => {
  const { data } = await GetAllSysCode('t_work_memory_tech_type')
  workTechTypeItem.value = data
}
//获取中国统计用行政区划下拉列表
const formattedAddressOptions = ref([])
const getFormattedAddressOptions = async () => {
  const { data } = await GetAdministrative()
  formattedAddressOptions.value = data
}

//-----------------------------------公用函数--------------------------------------
// 通用方法：根据值和映射表获取中文文本
const getDisplayText = (value, mappingArray) => {
  if (!value) return '-'
  const foundItem = mappingArray.find(item => item.value === value)
  return foundItem ? foundItem.text : value
}
//通用方法：根据值和映射表获取中文文本（树形）
const getDisplayTextByTree = (value, treeData) => {
  if (!value) return '-'

  for (const item of treeData) {
    // 1️⃣ 先判断当前节点
    if (item.value === value) {
      return item.text
    }

    // 2️⃣ 再递归子节点
    if (item.children?.length) {
      const result = getDisplayText(value, item.children)
      if (result) return result
    }
  }
  return value
}

//-----------------------------------列表展示---------------------------------------
//列表数据
const workList = ref([])
//分页条数封装
const workTotal = ref(0)
//分页封装
const workPageParams = ref({ page: 1, limit: 10 })
//条件查询参数存储
const workMemoryQueryDto = ref({
  memoryNo: '',
  memorySource: '',
  beginTime: '',
  endTime: '',
  workBusinessType: '',
  workTechType: '',
})
//条件查询时间范围封装
const beginTimeArea = ref([])

//发送请求，拿到工作记忆数据
const workFetchData = async () => {
  const { data } = await GetWorkMemoryByConditionAndPage(
    workPageParams.value.page,
    workPageParams.value.limit,
    workMemoryQueryDto.value
  )
  //处理多张图片情况
  data.list.forEach(item => {
    if (item.memoryImages != null && item.memoryImages != '') {
      item.memoryImages = item.memoryImages.split(',')
    } else {
      item.memoryImages = []
    }
    if (item.workAddress != null && item.workAddress != '') {
      item.workAddress = item.workAddress.split(',')
    } else {
      item.workAddress = ''
    }
  })

  workTotal.value = data.total
  workList.value = data.list
}

//搜索按钮点击触发事件
const searchWorkMemory = () => {
  //时间参数赋值
  workMemoryQueryDto.value.beginTime = beginTimeArea.value[0]
  workMemoryQueryDto.value.endTime = beginTimeArea.value[1]

  //调用查询接口
  workFetchData()
}
//重置按钮点击触发事件
const resetWorkMemory = () => {
  //时间范围重置
  beginTimeArea.value = []
  //参数重置
  workMemoryQueryDto.value = {}

  //调用查询接口
  workFetchData()
}
</script>

<style scoped>
/**********************基本背景及页面布局样式*******************/
.workMemoryAccessDT {
  position: relative;
  width: 100%;
  min-height: 100vh;
  margin: 0;
  padding: 0;
  overflow: auto;
}

.workMemoryAccessDT::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('src/assets/memory/green_img5.gif');
  background-size: cover;
  background-attachment: fixed;
  opacity: 0.5;
}

.workMemoryAccessDT > * {
  position: relative;
  z-index: 1;
}
/**********************************************************/

/* **************************搜索表单容器样式 **********************/
.search-div {
  margin-bottom: 10px;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 3px;
  background-color: transparent;
}
/* 美化搜索按钮 - 保持原有大小和位置 */
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

.beautified-search-btn:active {
  transform: translateY(0);
  box-shadow: 0 1px 3px rgba(64, 158, 255, 0.3);
}
/* 美化重置按钮 - 保持原有大小和位置 */
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
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.beautified-reset-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
  border-color: #409eff;
  color: #409eff;
  background: #f5f7fa;
}

.beautified-reset-btn:active {
  transform: translateY(0);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}
/* 按钮图标样式 */
.beautified-search-btn .el-icon,
.beautified-reset-btn .el-icon {
  font-size: 12px;
  margin-right: 2px;
}
/****************************************************************/

/************************************列表样式***********************************/
/deep/ .el-table,
/deep/ .el-table__expanded-cell {
  background-color: transparent;
  color: #001528;
  border: 1px solid;
}
/deep/ .el-table th,
/deep/ .el-table tr,
/deep/ .el-table td {
  background-color: transparent;
  color: #001528;
  border: 1px solid;
}
/******************************************************************************/
</style>
