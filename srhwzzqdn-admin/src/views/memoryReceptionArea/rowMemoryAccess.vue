<template>
  <div class="rowMemoryAccessDT">
    <h1 style="margin-top: 10px; font-family: 方正姚体; color: black">&emsp;<el-icon><MagicStick /></el-icon> 原始记忆接入</h1>
    <!---搜索表单-->
    <div class="search-div">
      <el-form label-width="120px" size="small">
        <!--    第一行查询条件    -->
        <el-row>
          <el-col :span="6">
            <el-form-item label="关系人名称">
              <el-input
                  v-model="rowQueryDto.contact"
                  style="width: 100%"
                  placeholder="您想要查询和谁的记忆？"
                  clearable
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="关系人类型">
              <el-select
                  v-model="rowQueryDto.contactType"
                  multiple
                  placeholder="请选择"
                  style="width: 100%"
                  clearable
              >
                <el-option
                    v-for="item in contactTypeItem"
                    :key="item.value"
                    :label="item.text"
                    :value="item.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="记录时间">
              <el-date-picker
                  v-model="recordTime"
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
        <!--    第二行查询条件    -->
        <el-row>
          <el-col :span="6">
            <el-form-item label="原始记忆类型">
              <el-select
                  v-model="rowQueryDto.rowMemoryType"
                  multiple
                  placeholder="请选择"
                  style="width: 100%"
                  clearable
              >
                <el-option
                    v-for="item in rowMemoryTypeItem"
                    :key="item.value"
                    :label="item.text"
                    :value="item.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="原始记忆来源">
              <el-select
                  v-model="rowQueryDto.memorySource"
                  multiple
                  placeholder="请选择"
                  style="width: 100%"
                  clearable
              >
                <el-option
                    v-for="item in memorySourceItem"
                    :key="item.value"
                    :label="item.text"
                    :value="item.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="工作记忆联想状态">
              <el-select
                  v-model="rowQueryDto.workMemoryAssociativeStatus"
                  placeholder="请选择"
                  style="width: 100%"
                  clearable
              >
                <el-option
                    v-for="item in associativeStatusItem"
                    :key="item.value"
                    :label="item.text"
                    :value="item.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="生活记忆联想状态">
              <el-select
                  v-model="rowQueryDto.lifeMemoryAssociativeStatus"
                  placeholder="请选择"
                  style="width: 100%"
                  clearable
              >
                <el-option
                    v-for="item in associativeStatusItem"
                    :key="item.value"
                    :label="item.text"
                    :value="item.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <!--   第三行查询条件     -->
        <el-row>
          <el-col :span="6">
            <el-form-item label="娱乐记忆联想状态">
              <el-select
                  v-model="rowQueryDto.funMemoryAssociativeStatus"
                  placeholder="请选择"
                  style="width: 100%"
                  clearable
              >
                <el-option
                    v-for="item in associativeStatusItem"
                    :key="item.value"
                    :label="item.text"
                    :value="item.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="交际记忆联想状态">
              <el-select
                  v-model="rowQueryDto.communicateMemoryAssociativeStatus"
                  placeholder="请选择"
                  style="width: 100%"
                  clearable
              >
                <el-option
                    v-for="item in associativeStatusItem"
                    :key="item.value"
                    :label="item.text"
                    :value="item.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="学习记忆联想状态">
              <el-select
                  v-model="rowQueryDto.learnMemoryAssociativeStatus"
                  placeholder="请选择"
                  style="width: 100%"
                  clearable
              >
                <el-option
                    v-for="item in associativeStatusItem"
                    :key="item.value"
                    :label="item.text"
                    :value="item.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row style="display:flex">
          <el-button type="primary" size="small" @click="searchRowMemory">
            搜索
          </el-button>
          <el-button size="small" @click="resetRowMemory">重置</el-button>
        </el-row>
      </el-form>
    </div>


  </div>
</template>

<script setup>
import {onMounted, ref} from "vue";
import {GetKeyAndValueByType} from "@/api/sysDict";

//-----------------------------------------------原始记忆列表--------------------------------------------------
//列表展示数据模型
const rowList = ref([]);
//分页条数数据封装
const rowTotal = ref(0);
//分页封装
const rowPageParams = ref({page: 1, limit: 10});
//条件查询参数封装
const rowQueryDto = ref({
  contact: '',
  contactType: [],
  recordBeginTime: '',
  recordEndTime: '',
  rowMemoryType: [],
  memorySource: [],
  workMemoryAssociativeStatus: "",
  lifeMemoryAssociativeStatus: "",
  funMemoryAssociativeStatus: "",
  communicateMemoryAssociativeStatus: "",
  learnMemoryAssociativeStatus: ""
});
//记录开始结束时间参数封装
const recordTime = ref([]);
const contactTypeItem = ref([]); //封装关系人类型下拉列表参数
const rowMemoryTypeItem = ref([]); //原始记忆类型下拉列表参数封装
const memorySourceItem = ref([]); //原始记忆来源下拉列表参数封装
const associativeStatusItem = ref([]); //联想状态下拉列表参数封装

//钩子函数
onMounted(() => {
  //获取下拉列表项
  getRowMemoryTypeItem();
  getMemorySourceItem();
  getContactTypeItem();
  getAssociativeStatusItem();

  rowFetchData();
});
//发送请求，获取关系人类型下拉列表
const getContactTypeItem = async () => {
  const { data } = await GetKeyAndValueByType('t_ty_people_relation');
  contactTypeItem.value = data;
}
//发送请求，获取原始记忆类型下拉列表
const getRowMemoryTypeItem = async () => {
  const { data } = await GetKeyAndValueByType('t_row_memory_type');
  rowMemoryTypeItem.value = data;
}
//发送请求，获取原始记忆来源下拉列表
const getMemorySourceItem = async () => {
  const { data } = await GetKeyAndValueByType('t_row_memory_source');
  memorySourceItem.value = data;
}
//获取联想状态下拉列表
const getAssociativeStatusItem = async () => {
  const { data } = await GetKeyAndValueByType('t_memory_associative_status');
  associativeStatusItem.value = data;
}

//搜索按钮点击事件
const searchRowMemory = () => {
  rowQueryDto.value.recordBeginTime = recordTime.value[0];
  rowQueryDto.value.recordEndTime = recordTime.value[1];
  //刷新列表
  rowFetchData();
}
//重置按钮点击事件
const resetRowMemory = () => {
  rowQueryDto.value = {};
  recordTime.value = [];
  rowFetchData();
}

//发送请求，拿到原始记忆数据
const rowFetchData = async () => {
}

</script>

<style scoped>
.rowMemoryAccessDT {
  position: relative;
  width: 100%;
  min-height: 100vh;
  margin: 0;
  padding: 0;
  overflow: auto;
}

.rowMemoryAccessDT::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('src/assets/memory/green_img5.gif');
  background-size: cover;
  background-attachment: fixed;
  opacity: 0.5; /* 设置背景图片的透明度为50% */
  /*z-index: -1; !* 确保伪元素在内容下方 *!*/
}
.rowMemoryAccessDT > * {
  position: relative;
  z-index: 1; /* 确保内容在伪元素上方 */
}

.tools-div {
  margin: 10px 0;
  padding: 10px;
  /*border: 1px solid #ebeef5;*/
  border-radius: 3px;
  background-color: transparent;
}

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

.search-div {
  margin-bottom: 10px;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 3px;
  background-color: transparent;
}
</style>