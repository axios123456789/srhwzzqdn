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

  </div>
</template>

<script setup>
import {onMounted, ref} from "vue";
import {getTodayTimeRange} from "@/utils/common";
import {GetAdministrative, GetKeyAndValueByType} from "@/api/sysDict";

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
</style>
