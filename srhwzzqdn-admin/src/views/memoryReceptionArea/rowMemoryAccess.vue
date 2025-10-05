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
            <el-form-item label="记忆联想状态">
              <el-select
                  v-model="rowQueryDto.memoryAssociativeStatus"
                  placeholder="请选择"
                  style="width: 100%"
                  multiple
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
          <el-button
              type="primary"
              size="small"
              @click="searchRowMemory"
              class="beautified-search-btn"
          >
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button
              size="small"
              @click="resetRowMemory"
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
          @click="addRowMemory"
          class="action-btn manual-btn"
      >
        <el-icon><DocumentAdd /></el-icon>
        手动录入
      </el-button>
      <el-button
          type="warning"
          size="small"
          @click="addRowMemory2"
          class="action-btn responsive-btn"
      >
        <el-icon><DataAnalysis /></el-icon>
        响应式录入
      </el-button>
      <el-button
          type="danger"
          size="small"
          @click="addRowMemory3"
          class="action-btn ai-btn"
      >
        <el-icon><Cpu /></el-icon>
        智能录入
      </el-button>
    </div>

    <!--  手动录入和修改模态窗口  -->
    <el-dialog
        v-model="dialogVisible"
        :title="rowMemory.id ? '原始记忆重塑' : '原始记忆接入'"
        width="80%"
        class="custom-dialog enhanced-dialog"
        :close-on-click-modal="false"
        :lock-scroll="false"
        align-center
        draggable
    >
      <div class="dialog-content">
        <el-form label-width="120px" class="scrollable-form">
          <el-form label-width="120px">
            <!--    第一行    -->
            <el-row>
              <el-col :span="12">
                <el-form-item label="记忆起始时间">
                  <el-date-picker
                      v-model="rowMemory.recordTime"
                      type="datetime"
                      style="width: 100%"
                      placeholder="选择日期时间"
                      :editable="false"
                      :value-format="'YYYY-MM-DD HH:mm:ss'"
                  ></el-date-picker>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="记忆终止时间">
                  <el-date-picker
                      v-model="rowMemory.recordEndTime"
                      type="datetime"
                      style="width: 100%"
                      placeholder="选择日期时间"
                      :editable="false"
                      :value-format="'YYYY-MM-DD HH:mm:ss'"
                  ></el-date-picker>
                </el-form-item>
              </el-col>
            </el-row>
            <!--   第二行     -->
            <el-row>
              <el-col :span="12">
                <el-form-item label="关系人名称">
                  <el-input
                      v-model="rowMemory.contact"
                      style="width: 100%"
                      placeholder="输入与谁发生的记忆"
                      clearable
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="关系人类型">
                  <el-select
                      v-model="rowMemory.contactType"
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
            </el-row>
            <!--   第三行    -->
            <el-row>
              <el-col :span="24">
                <el-form-item label="记忆册">
                  <el-upload
                      v-model:file-list="fileList"
                      action="http://localhost:8400/superBrain/system/fileUpload"
                      list-type="picture-card"
                      multiple
                      :on-preview="handlePictureCardPreview"
                      :on-success="handleSliderSuccess"
                      :on-remove="handleRemove"
                      :headers="headers"
                  >
                    <el-icon>
                      <Plus />
                    </el-icon>
                  </el-upload>
                  <el-dialog v-model="dialogVisibleHandle">
                    <img w-full :src="dialogImageUrl" alt="">
                  </el-dialog>
                </el-form-item>
              </el-col>
            </el-row>
            <!--    第四行    -->
            <el-row>
              <el-col :span="12">
                <el-form-item label="记忆地点简称">
                  <el-select
                      v-model="rowMemory.memoryPlaceShort"
                      placeholder="请选择"
                      style="width: 100%"
                      @change="placeShortChange"
                      clearable
                  >
                    <el-option
                        v-for="item in placeShortItem"
                        :key="item.value"
                        :label="item.text"
                        :value="item.value"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="记忆地点">
                  <el-cascader v-model="rowMemory.memoryPlace"
                               :options="formattedAddressOptions"
                               clearable
                               placeholder="请选择记忆地点"
                               style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <!--    第五行    -->
            <el-row>
              <el-col :span="12">
                <el-form-item label="详细地点">
                  <el-input
                      v-model="rowMemory.memoryPlaceDetail"
                      style="width: 100%"
                      clearable
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="记忆类型">
                  <el-select
                      v-model="rowMemory.rowMemoryType"
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
            </el-row>
            <!--   第六行     -->
            <el-row>
              <el-col :span="24">
                <el-form-item label="记忆内容">
                  <el-input
                      type="textarea"
                      style="width: 100%"
                      :rows="5"
                      placeholder="请输入记忆内容（what）"
                      v-model="rowMemory.rowMemoryContent"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <!--   第七行     -->
            <el-row>
              <el-col :span="24">
                <el-form-item label="记忆原因">
                  <el-input
                      type="textarea"
                      style="width: 100%"
                      :rows="5"
                      placeholder="请输入产生该记忆原因（why）"
                      v-model="rowMemory.rowMemoryReason"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <!--    第八行    -->
            <el-row>
              <el-col :span="24">
                <el-form-item label="记忆行为">
                  <el-input
                      type="textarea"
                      style="width: 100%"
                      :rows="5"
                      placeholder="请输入针对该记忆的行为（how）"
                      v-model="rowMemory.rowMemoryAction"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <!--   第九行     -->
            <el-row>
              <el-col :span="12">
                <el-form-item label="记忆来源">
                  <el-select
                      v-model="rowMemory.memorySource"
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
              <el-col :span="12">
                <el-form-item label="记忆联想状态">
                  <el-select
                      v-model="rowMemory.memoryAssociativeStatus"
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
          </el-form>
        </el-form>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submit">提交</el-button>
        </span>
      </template>
    </el-dialog>

    <!--  数据展示列表  -->
    <el-table :data="rowList"
              style="width: 100%" height="300"
              ref="multipleTable"
              @selection-change="handleSelectionChange">
      <el-table-column
          type="selection"
          width="40">
      </el-table-column>
      <el-table-column label="操作" align="center" width="280" #default="scope">
        <div class="action-buttons">
          <el-button
              type="primary"
              size="small"
              @click="editRowMemory(scope.row)"
              class="beautified-edit-btn"
          >
            <el-icon><Edit /></el-icon>
            记忆编辑
          </el-button>
          <el-button
              type="danger"
              size="small"
              @click="deleteRowMemoryById(scope.row)"
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
      <el-table-column prop="rowMemoryType" label="记忆类型" width="120" #default="scope">
        {{ getDisplayText(scope.row.rowMemoryType, rowMemoryTypeItem) }}
      </el-table-column>
      <el-table-column prop="recordTime" label="记录开始时间" width="180" />
      <el-table-column prop="recordEndTime" label="记录结束时间" width="180" />
      <el-table-column prop="contactType" label="关系人类型" width="120" #default="scope">
        {{ getDisplayText(scope.row.contactType, contactTypeItem) }}
      </el-table-column>
      <el-table-column prop="contact" label="关系人名称" width="120" />
      <el-table-column
          prop="memoryImages"
          label="轮播图"
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
      <el-table-column prop="memoryPlace" label="记忆地点" width="200" #default="scope">
        {{ getMemoryPlaceDisplay(scope.row) }}
      </el-table-column>
      <el-table-column prop="rowMemoryContent" label="记忆内容" width="300" />
      <el-table-column prop="rowMemoryReason" label="记忆发生原由" width="180" />
      <el-table-column prop="rowMemoryAction" label="记忆行为" width="180" />
      <el-table-column prop="memoryOwnerName" label="记忆所属人" width="120" />
      <el-table-column prop="memorySource" label="记忆来源" width="120" #default="scope">
        {{ getDisplayText(scope.row.memorySource, memorySourceItem) }}
      </el-table-column>
      <el-table-column prop="memoryAssociativeStatus" label="记忆联想状态" width="150" #default="scope">
        {{ getDisplayText(scope.row.memoryAssociativeStatus, associativeStatusItem) }}
      </el-table-column>
      <el-table-column prop="recordBy" label="记录人" width="120" />
      <el-table-column prop="updateTime" label="修改时间" width="180" />
      <el-table-column prop="updateBy" label="修改者" width="120" />
    </el-table>

    <!--分页条-->
    <el-pagination
        style="margin-top: 30px"
        v-model:current-page="rowPageParams.page"
        v-model:page-size="rowPageParams.limit"
        :page-sizes="[10, 20, 50, 100]"
        @size-change="rowFetchData"
        @current-change="rowFetchData"
        layout="total, sizes, prev, pager, next"
        :total="rowTotal"
    />
  </div>
</template>

<script setup>
import {onMounted, ref} from "vue";
import {GetAdministrative, GetKeyAndValueByType} from "@/api/sysDict";
import {DeleteRowMemoryById, GetRowMemoryByConditionAndPage, SaveRowMemory} from "@/api/memoryReception";
import {useApp} from "@/pinia/modules/app";
import {ElMessage, ElMessageBox} from "element-plus";
import {GetAllMapperConfigByType} from "@/api/mapperConfiguration";

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
  memoryAssociativeStatus: []
});
//记录开始结束时间参数封装
const recordTime = ref([]);
const contactTypeItem = ref([]); //封装关系人类型下拉列表参数
const rowMemoryTypeItem = ref([]); //原始记忆类型下拉列表参数封装
const memorySourceItem = ref([]); //原始记忆来源下拉列表参数封装
const associativeStatusItem = ref([]); //联想状态下拉列表参数封装
// 完整的省市区数据
const formattedAddressOptions = ref([]);
const placeShortItem = ref([]); //地点简称

// 通用方法：根据值和映射表获取中文文本
const getDisplayText = (value, mappingArray) => {
  if (!value) return '-'
  const foundItem = mappingArray.find(item => item.value === value)
  return foundItem ? foundItem.text : value
}
// -----获取记忆地点显示文本-------
const getMemoryPlaceDisplay = (row) => {
  if (!row.memoryPlace && !row.memoryPlaceDetail) return '-'
  let placeText = ''
  // 处理记忆地点
  if (row.memoryPlace) {
    if (Array.isArray(row.memoryPlace)) {
      // 如果是数组，转换为中文
      placeText = row.memoryPlace.map(code =>
          getAddressTextByCode(code)
      ).filter(Boolean).join('')
    } else if (typeof row.memoryPlace === 'string' && row.memoryPlace.includes(',')) {
      // 如果是逗号分隔的字符串，拆分后转换
      placeText = row.memoryPlace.split(',').map(code =>
          getAddressTextByCode(code.trim())
      ).filter(Boolean).join('')
    } else {
      // 单个代码或直接文本
      placeText = getAddressTextByCode(row.memoryPlace) || row.memoryPlace
    }
  }
  // 拼接详细地点
  if (row.memoryPlaceDetail) {
    placeText += row.memoryPlaceDetail
  }
  return placeText || '-'
}
// 根据代码获取地址文本
const getAddressTextByCode = (code) => {
  if (!code || !formattedAddressOptions.value.length) return ''
  const findLabel = (options, targetCode) => {
    for (const option of options) {
      if (option.value == targetCode) {
        return option.label
      }
      if (option.children && option.children.length > 0) {
        const found = findLabel(option.children, targetCode)
        if (found) return found
      }
    }
    return ''
  }
  return findLabel(formattedAddressOptions.value, code)
}

//------设置默认时间为当天0点到当天23：59：59
const getTodayTimeRange = () => {
  const now = new Date();
  const beginTime = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0);
  const endTime = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59);
  const beginTimeStr = beginTime.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  })
  const endTimeStr = endTime.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  })
  return [beginTimeStr, endTimeStr];
}

//钩子函数
onMounted(() => {
  //获取下拉列表项
  getRowMemoryTypeItem();
  getMemorySourceItem();
  getContactTypeItem();
  getAssociativeStatusItem();
  getFormattedAddressOptions();
  getPlaceShortItem();

  //设置默认时间
  const [startOfDay, endOfDay] = getTodayTimeRange();
  recordTime.value[0] = startOfDay
  recordTime.value[1] = endOfDay
  rowQueryDto.value.recordBeginTime = startOfDay;
  rowQueryDto.value.recordEndTime = endOfDay;

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
//获取中国行政区划码值对
const getFormattedAddressOptions = async () => {
  const {data} = await GetAdministrative();
  formattedAddressOptions.value = data;
}
//获取地点简称
const getPlaceShortItem = async () => {
  const { data } = await GetKeyAndValueByType('t_memory_place_short');
  placeShortItem.value = data;
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
  const {data} = await GetRowMemoryByConditionAndPage(rowPageParams.value.page, rowPageParams.value.limit, rowQueryDto.value)
  //处理多张图片情况
  data.list.forEach(item => {
    if (item.memoryImages != null && item.memoryImages != ""){
      item.memoryImages = item.memoryImages.split(',')
    }else {
      item.memoryImages = []
    }
    if (item.memoryPlace != null && item.memoryPlace != ""){
      item.memoryPlace = item.memoryPlace.split(",")
    }else {
      item.memoryPlace = []
    }
  })

  rowTotal.value = data.total
  rowList.value = data.list
}

//------------------------------------------------原始记忆录入编辑-----------------------------------------------------
const rowMemory = ref({}) //原始记忆手动录入数据
const dialogVisible = ref(false) //控制手动录入和修改原始记忆模态窗口开闭
const placeShortWithPlaceMapper = ref([]) //地点简称和地点映射数据

//点击手动录入时触发
const addRowMemory = () => {
  //获取地点映射数据
  getPlaceShortWithPlaceMapper();

  rowMemory.value = {};
  rowMemory.value.memorySource = 1;
  rowMemory.value.memoryAssociativeStatus = 1;
  if (rowList.value.length > 0){
    rowMemory.value.recordTime = rowList.value[0].recordEndTime;
  }
  memoryImageList.value = [];
  fileList.value = [];
  dialogVisible.value = true;
}
//点击修改原始记忆触发
const editRowMemory = (row) => {
  //获取地点映射数据
  getPlaceShortWithPlaceMapper();

  rowMemory.value = {...row};
  fileList.value = [];
  memoryImageList.value = rowMemory.value.memoryImages;
  memoryImageList.value.forEach(url => {
    fileList.value.push({ url: url })
  })
  dialogVisible.value = true;
}

//获取地点映射数据
const getPlaceShortWithPlaceMapper = async () => {
  const {data} = await GetAllMapperConfigByType("shortPlaceWithPlace");
  placeShortWithPlaceMapper.value = data;
}

//记忆地点简称值改变事件
const placeShortChange = () => {
  const resultData = placeShortWithPlaceMapper.value.filter(item => item.mapperFieldA1 === rowMemory.value.memoryPlaceShort);
  rowMemory.value.memoryPlace = resultData[0].mapperFieldB1.split(",");
  rowMemory.value.memoryPlaceDetail = resultData[0].mapperFieldB3;
}

//点击手动录入和修改模态窗口中的提交按钮触发
const submit = async () => {
  //console.log("数据"+rowMemory.value.memoryImages.join(','))
  //基本验证
  if (rowMemory.value.recordTime == undefined || rowMemory.value.recordTime == ""){
    ElMessage.warning("【记忆开始时间】不能为空");
    return;
  }
  if (rowMemory.value.recordEndTime == undefined || rowMemory.value.recordEndTime == ""){
    ElMessage.warning("【记忆结束时间】不能为空");
    return;
  }
  if (rowMemory.value.rowMemoryContent == undefined || rowMemory.value.rowMemoryContent == ""){
    ElMessage.warning("【记忆内容】不能为空");
    return;
  }
  //数据处理
  if (rowMemory.value.memoryPlace != null && rowMemory.value.memoryPlace != "" && rowMemory.value.memoryPlace != undefined){
    rowMemory.value.memoryPlace = rowMemory.value.memoryPlace.join(",");
  } else {
    rowMemory.value.memoryPlace = null;
  }
  if (memoryImageList.value != null && memoryImageList.value.length > 0){
    rowMemory.value.memoryImages = memoryImageList.value.join(",");
  }else {
    rowMemory.value.memoryImages = null;
  }
  const {code, message} = await SaveRowMemory(rowMemory.value);
  if (code === 200){
    dialogVisible.value = false;
    ElMessage.success(message);
    rowFetchData();
  }else {
    ElMessage.error(message);
  }
}

//-------------------------文件上传-----------------------
const headers = {
  token: useApp().authorization.token, // 从pinia中获取token，在进行文件上传的时候将token设置到请求头中
}
// 图像上传成功以后的事件处理函数
// const handleAvatarSuccess = (response, uploadFile) => {
//   rowMemory.value.memoryImages = response.data
// }
//---上传商品轮播图图片-----
const memoryImageList = ref([])
//移除图像触发
const handleRemove = (uploadFile, uploadFiles) => {
  memoryImageList.value.splice(memoryImageList.value.indexOf(uploadFile.url), 1)
}
const fileList = ref([])
//上传成功触发
const handleSliderSuccess = (response, uploadFile) => {
  memoryImageList.value.push(response.data)
}
const dialogVisibleHandle = ref(false)
const dialogImageUrl = ref()
const handlePictureCardPreview = (file) => {
  dialogImageUrl.value = file.url;
  dialogVisibleHandle.value = true;
}

//-------------------------------------记忆删除-----------------------------------
//点击删除角色按钮后触发
const deleteRowMemoryById = row => {
  ElMessageBox.confirm('确定要从外置大脑清除这条记忆吗?', 'Warning', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    const { code, message } = await DeleteRowMemoryById(row.id)
    if (code === 200) {
      ElMessage.success(message)
      rowFetchData();
    } else {
      ElMessage.error(message)
    }
  })
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
  opacity: 0.5;
}

.rowMemoryAccessDT > * {
  position: relative;
  z-index: 1;
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

.action-btn .el-icon {
  font-size: 16px;
  margin-right: 4px;
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

/* 原有的其他样式保持不变 */
.tools-div {
  margin: 10px 0;
  padding: 10px;
  border-radius: 3px;
  background-color: transparent;
  float: right;
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

/*头像部分样式*/
.avatar-uploader .avatar {
  width: 178px;
  height: 178px;
  display: block;
}
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}
.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}
.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
}

/* 添加或修改模态窗口样式优化 */
:deep(.enhanced-dialog) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.2);
  max-height: 80vh !important;
}

:deep(.enhanced-dialog .el-dialog) {
  display: flex;
  flex-direction: column;
  max-height: 80vh !important;
  min-height: 600px;
}

/* 重点调整：标题栏高度优化 */
:deep(.enhanced-dialog .el-dialog__header) {
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
  margin: 0;
  padding: 12px 20px; /* 减少上下内边距 */
  display: flex;
  align-items: center;
  min-height: 40px; /* 显著减少头部高度 */
  position: relative;
}

:deep(.enhanced-dialog .el-dialog__title) {
  color: white;
  font-weight: 600;
  font-size: 16px; /* 适当减小标题字体 */
  line-height: 1.2;
  flex: 1;
}

:deep(.enhanced-dialog .el-dialog__headerbtn) {
  position: absolute;
  top: 50% !important;
  right: 16px;
  transform: translateY(-50%);
  margin-top: 0;
  height: 24px; /* 减小关闭按钮尺寸 */
  width: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
}

:deep(.enhanced-dialog .el-dialog__headerbtn .el-dialog__close) {
  color: white;
  font-size: 20px; /* 减小关闭图标大小 */
  font-weight: bold;
}

:deep(.enhanced-dialog .el-dialog__body) {
  padding: 0;
  max-height: calc(80vh - 100px) !important; /* 相应调整内容区域高度 */
  overflow: hidden;
  display: flex;
  flex: 1;
}

.dialog-content {
  width: 100%;
  padding: 20px;
}

.scrollable-form {
  max-height: calc(80vh - 160px); /* 相应调整可滚动区域高度 */
  overflow-y: auto;
  padding-right: 12px;
}

/* 底部按钮区域优化 */
.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 20px;
  padding: 16px 20px;
  border-top: 1px solid #e8e8e8;
  background: #fafafa;
}

:deep(.enhanced-dialog .el-dialog__footer) {
  padding: 0;
}

/* 底部按钮样式优化 */
.dialog-footer .el-button {
  min-width: 100px;
  padding: 10px 24px;
}

/* 上传区域样式优化 */
:deep(.avatar-uploader) {
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  transition: all 0.3s ease;
  height: 178px;
  width: 178px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}

:deep(.avatar-uploader .avatar) {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

:deep(.avatar-uploader .el-upload) {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.avatar-uploader-icon) {
  font-size: 28px;
  color: #8c939d;
}

:deep(.avatar-uploader:hover) {
  border-color: #409eff;
  box-shadow: 0 0 8px rgba(64, 158, 255, 0.3);
}

/* 表单布局优化 */
:deep(.scrollable-form .el-form-item) {
  margin-bottom: 18px;
}

:deep(.scrollable-form .el-row) {
  margin-bottom: 10px;
}

/* 单选框组样式调整 */
:deep(.scrollable-form .el-radio-group) {
  display: flex;
  gap: 15px;
  align-items: center;
  height: 32px;
}

:deep(.scrollable-form .el-radio) {
  margin-right: 0;
}

/* 文本域样式调整 */
:deep(.scrollable-form .el-textarea .el-textarea__inner) {
  min-height: 100px;
  resize: vertical;
}

/* 响应式调整 */
@media (max-height: 700px) {
  :deep(.enhanced-dialog) {
    max-height: 90vh !important;
  }

  :deep(.enhanced-dialog .el-dialog__body) {
    max-height: calc(90vh - 100px) !important;
  }

  .scrollable-form {
    max-height: calc(90vh - 160px);
  }

  :deep(.avatar-uploader) {
    height: 150px;
    width: 150px;
  }
}

/* 动画效果优化 */
:deep(.enhanced-dialog .el-dialog) {
  animation: dialog-fade-in 0.3s ease;
}

@keyframes dialog-fade-in {
  0% {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 滚动条优化 */
.scrollable-form::-webkit-scrollbar {
  width: 6px;
}

.scrollable-form::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.scrollable-form::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.scrollable-form::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* ******************列表操作按钮容器 ************************/
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
</style>