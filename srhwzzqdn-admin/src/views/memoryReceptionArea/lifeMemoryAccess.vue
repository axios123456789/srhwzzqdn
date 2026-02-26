<template>
  <div class="lifeMemoryAccessDT">
    <h1 style="margin-top: 10px; font-family: 方正姚体; color: black">
      &emsp;
      <el-icon><house /></el-icon>
      生活记忆接入
    </h1>

    <!--  搜索表单  -->
    <div class="search-div">
      <el-form label-width="120px" size="small">
        <!--    第一行查询条件    -->
        <el-row>
          <el-col :span="6">
            <el-form-item label="记忆编号">
              <el-input
                  v-model="lifeQueryDto.memoryNo"
                  style="width: 100%"
                  clearable
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="记忆来源">
              <el-select
                  v-model="lifeQueryDto.memorySource"
                  multiple
                  placeholder="请选择"
                  style="width: 100%"
                  clearable
              >
                <el-option
                    v-for="item in lifeMemorySourceItem"
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
            <el-form-item label="生活记忆类型">
              <el-select
                  v-model="lifeQueryDto.lifeType"
                  multiple
                  placeholder="请选择"
                  style="width: 100%"
                  clearable
              >
                <el-option
                    v-for="item in lifeMemoryTypeItem"
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
              @click="searchLifeMemory"
              class="beautified-search-btn"
          >
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button
              size="small"
              @click="resetLifeMemory"
              class="beautified-reset-btn"
          >
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-row>
      </el-form>
    </div>

    <!--总体操作按钮一览-->
    <div class="tools-div beautified-tools">
      <el-button
          type="success"
          size="small"
          @click="addLifeMemory"
          class="action-btn manual-btn"
      >
        <el-icon><DocumentAdd /></el-icon>
        手动录入
      </el-button>
      <el-button
          type="danger"
          size="small"
          @click="deleteSelectAll"
          class="action-btn batch-delete-btn"
      >
        <el-icon><Delete /></el-icon>
        批量删除
      </el-button>
      <el-button
          type="info"
          size="small"
          @click="showExportDialog"
          class="action-btn export-btn"
      >
        <el-icon><Download /></el-icon>
        一键导出
      </el-button>
    </div>

    <!-- 列表展示  -->
    <el-table
        :data="lifeList"
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
              @click="editLifeMemory(scope.row)"
              class="beautified-edit-btn"
          >
            <el-icon><Edit /></el-icon>
            记忆编辑
          </el-button>
          <el-button
              type="danger"
              size="small"
              @click="deleteLifeMemoryById(scope.row)"
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
      <el-table-column prop="memoryNo" label="记忆编号" width="180" />
      <el-table-column prop="beginTime" label="记忆开始时间" width="180" />
      <el-table-column prop="endTime" label="记忆结束时间" width="180" />
      <el-table-column
          prop="lifeType"
          label="生活记忆类型"
          width="120"
          #default="scope"
      >
        {{ getDisplayText(scope.row.lifeType, lifeMemoryTypeItem) }}
      </el-table-column>
      <el-table-column
          prop="memorySource"
          label="记忆来源"
          width="120"
          #default="scope"
      >
        {{ getDisplayText(scope.row.memorySource, lifeMemorySourceItem) }}
      </el-table-column>
      <el-table-column
          prop="memoryImages"
          label="工作记录图"
          #default="scope"
          width="200"
      >
        <div style="float: left;">
          <template v-for="(item, index) in scope.row.memoryImages" :key="index">
            <img
                v-if="index < 3"
                :src="item"
                width="50"
                height="50"
                style="margin-right: 5px;"
            />
            <span v-else-if="index === 3">...</span>
          </template>
        </div>
      </el-table-column>
      <el-table-column prop="lifeContent" label="生活记忆内容" width="300">
        <template #default="scope">
          <span :title="scope.row.lifeContent">
            {{ scope.row.lifeContent && scope.row.lifeContent.length > 100 ? scope.row.lifeContent.substring(0, 100) + '...' : scope.row.lifeContent }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="lifeConsume" label="生活消费" width="120" />
      <el-table-column prop="memoryOwnerName" label="记忆所属人" width="120" />
      <el-table-column prop="updateTime" label="修改时间" width="180" />
      <el-table-column prop="updateBy" label="修改者" width="120" />
    </el-table>

    <!--分页条-->
    <el-pagination
        style="margin-top: 30px"
        v-model:current-page="lifePageParams.page"
        v-model:page-size="lifePageParams.limit"
        :page-sizes="[10, 20, 50, 100]"
        @size-change="lifeFetchData"
        @current-change="lifeFetchData"
        layout="total, sizes, prev, pager, next"
        :total="lifeTotal"
    />
  </div>
</template>

<script setup>
import {onMounted, ref} from "vue";
import {getTodayTimeRange} from "@/utils/common";
import {GetAdministrative, GetKeyAndValueByType} from "@/api/sysDict";
import {GetLifeMemoryByConditionAndPage} from "@/api/memoryReception";

//--------------------钩子函数-------------------------
onMounted(() => {
  //1.加载数据字典
  getFormattedAddressOptions() //行政区划
  getLifeMemorySourceItem();
  getLifeMemoryTypeItem();

  //2.查询条件设置默认时间为今天
  const [startOfDay, endOfDay] = getTodayTimeRange()
  beginTimeArea.value[0] = startOfDay
  beginTimeArea.value[1] = endOfDay
  lifeQueryDto.value.beginTime = startOfDay
  lifeQueryDto.value.endTime = endOfDay

  //3.调用查询数据接口
  lifeFetchData()
});

//---------------------数据字典获取-------------------------
//生活记忆来源
const lifeMemorySourceItem = ref([]);
const getLifeMemorySourceItem = async () => {
  const { data } = await GetKeyAndValueByType('t_life_memory_source')
  lifeMemorySourceItem.value = data
}
//生活记忆类型
const lifeMemoryTypeItem = ref([]);
const getLifeMemoryTypeItem = async () => {
  const { data } = await GetKeyAndValueByType('t_life_memory_type')
  lifeMemoryTypeItem.value = data
}
//获取中国统计用行政区划下拉列表
const formattedAddressOptions = ref([])
const getFormattedAddressOptions = async () => {
  const { data } = await GetAdministrative()
  formattedAddressOptions.value = data
}

//--------------------------------生活记忆列表展示----------------------------------
//定义生活记忆列表参数接收
const lifeList = ref([]);
//分页条数封装
const lifeTotal = ref(0);
//分页封装
const lifePageParams = ref({ page: 1, limit: 10 });
//条件查询参数存储
const lifeQueryDto = ref({
  memoryNo: "",
  lifeType: "",
  beginTime: "",
  endTime: "",
  memorySource: ""
});
//条件查询时间范围封装
const beginTimeArea = ref([]);

//发送请求，拿到生活记忆列表
const lifeFetchData = async () => {
  const {data} = await GetLifeMemoryByConditionAndPage(lifePageParams.value.page, lifePageParams.value.limit, lifeQueryDto.value);

  //数据处理
  data.list.forEach(item => {
    if (item.memoryImages != null && item.memoryImages != '') {
      item.memoryImages = item.memoryImages.split(',')
    } else {
      item.memoryImages = []
    }
    if (item.memoryPlace != null && item.memoryPlace != '') {
      item.memoryPlace = item.memoryPlace.split(',')
    } else {
      item.memoryPlace = ''
    }
  })

  lifeTotal.value = data.total;
  lifeList.value = data.list;
}

//搜索按钮点击触发事件
const searchLifeMemory = () => {
  //时间参数赋值
  lifeQueryDto.value.beginTime = beginTimeArea.value[0]
  lifeQueryDto.value.endTime = beginTimeArea.value[1]

  //调用查询接口
  lifeFetchData()
}
//重置按钮点击触发事件
const resetLifeMemory = () => {
  //时间范围重置
  beginTimeArea.value = []
  //参数重置
  lifeQueryDto.value = {}

  //调用查询接口
  lifeFetchData()
}
</script>

<style scoped>
/**********************基本背景及页面布局样式*******************/
.lifeMemoryAccessDT {
  position: relative;
  width: 100%;
  min-height: 100vh;
  margin: 0;
  padding: 0;
  overflow: auto;
}

.lifeMemoryAccessDT::before {
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

.lifeMemoryAccessDT > * {
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

/* *************************** 操作按钮图标样式及布局美化 **************************/
.beautified-search-btn .el-icon,
.beautified-reset-btn .el-icon {
  font-size: 12px;
  margin-right: 2px;
}

/* 美化工具按钮区域 */
.beautified-tools {
  margin: 20px 0;
  padding: 16px 24px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  float: right;
  clear: both;
}

.action-btn {
  border-radius: 12px;
  padding: 12px 20px;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
  border: none;
  height: 40px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
}

.action-btn:active {
  transform: translateY(-1px);
}

.manual-btn {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  color: white;
}

.manual-btn:hover {
  background: linear-gradient(135deg, #389e0d 0%, #52c41a 100%);
  box-shadow: 0 6px 20px rgba(82, 196, 26, 0.4);
}

.responsive-btn {
  background: linear-gradient(135deg, #fa8c16 0%, #ffa940 100%);
  color: white;
}

.responsive-btn:hover {
  background: linear-gradient(135deg, #d46b08 0%, #fa8c16 100%);
  box-shadow: 0 6px 20px rgba(250, 140, 22, 0.4);
}

.ai-btn {
  background: linear-gradient(135deg, #f5222d 0%, #ff4d4f 100%);
  color: white;
}

.ai-btn:hover {
  background: linear-gradient(135deg, #cf1322 0%, #f5222d 100%);
  box-shadow: 0 6px 20px rgba(245, 34, 45, 0.4);
}

/* 导出按钮样式 */
.export-btn {
  background: linear-gradient(135deg, #909399 0%, #a6a9ad 100%);
  color: white;
  border: none;
  border-radius: 12px;
  padding: 12px 20px;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
  height: 40px;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 12px rgba(144, 147, 153, 0.3);
}

.export-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(144, 147, 153, 0.4);
  background: linear-gradient(135deg, #7a7d81 0%, #8f9296 100%);
}

.export-btn:active {
  transform: translateY(-1px);
  box-shadow: 0 3px 10px rgba(144, 147, 153, 0.3);
}

.export-btn:disabled {
  background: linear-gradient(135deg, #d9d9d9 0%, #bfbfbf 100%);
  color: #8c8c8c;
  cursor: not-allowed;
  transform: none;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 批量删除按钮样式 */
.batch-delete-btn {
  background: linear-gradient(135deg, #f5222d 0%, #ff4d4f 100%);
  color: white;
  border: none;
  border-radius: 12px;
  padding: 12px 20px;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
  height: 40px;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 12px rgba(245, 34, 45, 0.3);
}

.batch-delete-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(245, 34, 45, 0.4);
  background: linear-gradient(135deg, #cf1322 0%, #f5222d 100%);
}

.batch-delete-btn:active {
  transform: translateY(-1px);
  box-shadow: 0 3px 10px rgba(245, 34, 45, 0.3);
}

.batch-delete-btn:disabled {
  background: linear-gradient(135deg, #d9d9d9 0%, #bfbfbf 100%);
  color: #8c8c8c;
  cursor: not-allowed;
  transform: none;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.action-btn .el-icon {
  font-size: 16px;
  margin-right: 4px;
}

/* 导出对话框样式 */
.export-dialog-content {
  padding: 20px 0;
}

.export-dialog-content .el-radio-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.export-dialog-content .el-checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 200px;
  overflow-y: auto;
  padding: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .beautified-tools {
    flex-direction: column;
    align-items: flex-end;
    gap: 12px;
    padding: 12px 16px;
  }

  .action-btn {
    width: 140px;
    justify-content: center;
  }
}
/*******************************************************************/

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

/* ****************** 列表操作按钮容器美化 ************************/
.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
}
/* 通用按钮样式 */
.beautified-edit-btn,
.beautified-delete-btn,
.beautified-associative-btn {
  border-radius: 8px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
  border: none;
  height: 28px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  min-width: 70px;
  justify-content: center;
}

.beautified-edit-btn:hover,
.beautified-delete-btn:hover,
.beautified-associative-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.beautified-edit-btn:active,
.beautified-delete-btn:active,
.beautified-associative-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 编辑按钮样式 */
.beautified-edit-btn {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
  border: 1px solid #409eff;
}

.beautified-edit-btn:hover {
  background: linear-gradient(135deg, #337ecc 0%, #529ce3 100%);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  border-color: #337ecc;
}

/* 删除按钮样式 */
.beautified-delete-btn {
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
  color: white;
  border: 1px solid #f56c6c;
}

.beautified-delete-btn:hover {
  background: linear-gradient(135deg, #d54c4c 0%, #e57373 100%);
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
  border-color: #d54c4c;
}

/* 联想按钮样式 */
.beautified-associative-btn {
  background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%);
  color: white;
  border: 1px solid #e6a23c;
}

.beautified-associative-btn:hover {
  background: linear-gradient(135deg, #cf9233 0%, #e2a952 100%);
  box-shadow: 0 4px 12px rgba(230, 162, 60, 0.3);
  border-color: #cf9233;
}

/* 按钮图标样式 */
.beautified-edit-btn .el-icon,
.beautified-delete-btn .el-icon,
.beautified-associative-btn .el-icon {
  font-size: 12px;
  margin-right: 2px;
}

/* 响应式调整 */
@media (max-width: 1366px) {
  .action-buttons {
    flex-direction: column;
    gap: 6px;
  }

  .beautified-edit-btn,
  .beautified-delete-btn,
  .beautified-associative-btn {
    min-width: 80px;
    padding: 5px 10px;
    font-size: 11px;
  }
}
/****************************************************************/
</style>
