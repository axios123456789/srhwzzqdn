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
          <el-col :span="6">
            <el-form-item label="消费类型">
              <el-select
                  v-model="lifeQueryDto.consumeType"
                  multiple
                  placeholder="请选择"
                  style="width: 100%"
                  clearable
              >
                <el-option
                    v-for="item in consumeTypeItem"
                    :key="item.value"
                    :label="item.text"
                    :value="item.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="记忆状态">
              <el-select
                  v-model="lifeQueryDto.memoryStatus"
                  multiple
                  placeholder="请选择"
                  style="width: 100%"
                  clearable
              >
                <el-option
                    v-for="item in memoryStatusItem"
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

    <!--  手动录入和记忆修改模态窗口 -->
    <el-dialog
        v-model="dialogVisible"
        :title="lifeMemory.id ? '生活记忆重塑' : '生活记忆接入'"
        width="80%"
        class="custom-dialog enhanced-dialog"
        :close-on-click-modal="false"
        :lock-scroll="false"
        align-center
        draggable
        :fullscreen="isFullscreen"
        @open="initFullscreen"
    >
      <div class="dialog-content">
        <el-form label-width="120px" class="scrollable-form">
          <el-form label-width="120px">
            <!--    第一行    -->
            <el-row>
              <el-col :span="12">
                <el-form-item label="工作开始时间">
                  <el-date-picker
                      v-model="lifeMemory.beginTime"
                      type="datetime"
                      style="width: 100%"
                      placeholder="选择日期时间"
                      :editable="false"
                      :value-format="'YYYY-MM-DD HH:mm:ss'"
                  ></el-date-picker>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="工作结束时间">
                  <el-date-picker
                      v-model="lifeMemory.endTime"
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
                <el-form-item label="生活记忆类型">
                  <el-select
                      v-model="lifeMemory.lifeType"
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
              <el-col :span="12">
                <el-form-item label="记忆来源">
                  <el-select
                      v-model="lifeMemory.memorySource"
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
                    <img w-full :src="dialogImageUrl" alt="" />
                  </el-dialog>
                </el-form-item>
              </el-col>
            </el-row>
            <!--   第四行     -->
            <el-row>
              <el-col :span="12">
                <el-form-item label="消费类型">
                  <el-select
                      v-model="lifeMemory.consumeType"
                      placeholder="请选择"
                      style="width: 100%"
                      clearable
                  >
                    <el-option
                        v-for="item in consumeTypeItem"
                        :key="item.value"
                        :label="item.text"
                        :value="item.value"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <!--     消费金额输入，使用计数器方式         -->
              <el-col :span="12">
                <el-form-item label="消费金额">
                  <el-input-number
                      v-model="lifeMemory.lifeConsume"
                      controls-position="right"
                      :min="0"
                      :max="10000"
                      :precision="2"
                      style="width: 100%"
                  ></el-input-number>
                </el-form-item>
              </el-col>
            </el-row>
            <!--    第五行    -->
            <el-row>
              <el-col :span="12">
                <el-form-item label="记忆地点">
                  <el-cascader
                      v-model="lifeMemory.memoryPlace"
                      :options="formattedAddressOptions"
                      clearable
                      placeholder="请选择记忆地点"
                      style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="详细地点">
                  <el-input
                      v-model="lifeMemory.memoryPlaceDetail"
                      style="width: 100%"
                      clearable
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <!--    第5.1行        -->
            <el-row>
              <el-col :span="12">
                <el-form-item label="记忆状态">
                  <el-select
                      v-model="lifeMemory.memoryStatus"
                      placeholder="请选择"
                      style="width: 100%"
                      clearable
                  >
                    <el-option
                        v-for="item in memoryStatusItem"
                        :key="item.value"
                        :label="item.text"
                        :value="item.value"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <!--    第六行    -->
            <el-row>
              <el-col :span="24">
                <el-form-item label="生活记忆内容">
                  <el-input
                      type="textarea"
                      style="width: 100%"
                      :rows="5"
                      placeholder="请输入生活记忆内容"
                      v-model="lifeMemory.lifeContent"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-form>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitLifeMemory">提交</el-button>
        </span>
      </template>
    </el-dialog>

    <!--  导出对话框  -->
    <ExportDialog
        v-model="exportDialogVisible"
        v-model:export-scope="exportScope"
        v-model:export-file-name="exportFileName"
        v-model:selected-columns="selectedColumns"
        :available-columns="exportColumns"
        :export-loading="exportLoading"
        :current-count="lifeList.length"
        :total-count="lifeTotal"
        @confirm="handleExport"
        @closed="resetExport"
    />

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
              @click="lifeInvoice(scope.row)"
              v-if="scope.row.memoryStatus == 1"
              class="beautified-associative-btn"
          >
            <el-icon><Connection /></el-icon>
            生活记账
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
          prop="consumeType"
          label="消费类型"
          width="120"
          #default="scope"
      >
        {{ getDisplayText(scope.row.consumeType, consumeTypeItem) }}
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
import {GetLifeMemoryByConditionAndPage, SaveLifeMemory, DeleteLifeMemoryById, DeleteAllLifeMemoryByIds} from "@/api/memoryReception";
import {useApp} from "@/pinia/modules/app";
import {useFullscreenDialog} from "@/hooks/useFullscreenDialog";
import {ElMessage, ElMessageBox} from "element-plus";
import {useExport} from "@/components/Export/hooks/useExport";
import ExportDialog from '@/components/Export/ExportDialog.vue';

//--------------------钩子函数-------------------------
onMounted(() => {
  //1.加载数据字典
  getFormattedAddressOptions() //行政区划
  getLifeMemorySourceItem();
  getLifeMemoryTypeItem();
  getConsumeTypeItem();
  getMemoryStatusItem();

  //2.查询条件设置默认时间为今天
  const [startOfDay, endOfDay] = getTodayTimeRange()
  beginTimeArea.value[0] = startOfDay
  beginTimeArea.value[1] = endOfDay
  lifeQueryDto.value.beginTime = startOfDay
  lifeQueryDto.value.endTime = endOfDay

  //3.调用查询数据接口
  lifeFetchData()
});

//------------------------公共方法---------------------------
// 通用方法：根据值和映射表获取中文文本
const getDisplayText = (value, mappingArray) => {
  if (!value) return '-'
  const foundItem = mappingArray.find(item => item.value === value)
  return foundItem ? foundItem.text : value
}

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
//消费类型
const consumeTypeItem = ref([]);
const getConsumeTypeItem = async () => {
  const { data } = await GetKeyAndValueByType('t_life_consume_type')
  consumeTypeItem.value = data
}
//生活记忆状态
const memoryStatusItem = ref([]);
const getMemoryStatusItem = async () => {
  const { data } = await GetKeyAndValueByType('t_life_memory_status')
  memoryStatusItem.value = data
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

//-----------------------------------------保存生活记忆-------------------------------------------------
//工作记忆数据存储
const lifeMemory = ref({})
//模特窗口控制
const dialogVisible = ref(false)
// 在需要全屏的组件中使用 Hook
const { isFullscreen, initFullscreen } = useFullscreenDialog()

//手动录入生活记忆按钮点击事件
const addLifeMemory = () => {
  //数据重置
  lifeMemory.value = {}
  lifeMemory.value.memorySource = 1
  lifeMemory.value.memoryStatus = 1
  if (lifeList.value.length > 0) {
    lifeMemory.value.beginTime = lifeList.value[0].endTime
  }
  //图片列表数据重置
  memoryImageList.value = []
  fileList.value = []

  //打开模态窗口
  dialogVisible.value = true;
}

//修改按钮点击事件
const editLifeMemory = (row) => {
  //数据设置
  lifeMemory.value = { ...row }
  //图片文件列表数据设置
  fileList.value = []
  memoryImageList.value = lifeMemory.value.memoryImages
  memoryImageList.value.forEach(url => {
    fileList.value.push({ url: url })
  })

  //打开模态窗口
  dialogVisible.value = true
}

//点击手动录入和修改模态窗口中的提交按钮触发
const submitLifeMemory = async () => {
  //console.log("数据"+rowMemory.value.memoryImages.join(','))
  //基本验证
  if (
      lifeMemory.value.beginTime == undefined ||
      lifeMemory.value.beginTime == ''
  ) {
    ElMessage.warning('【记忆开始时间】不能为空')
    return
  }
  if (lifeMemory.value.endTime == undefined || lifeMemory.value.endTime == '') {
    ElMessage.warning('【记忆结束时间】不能为空')
    return
  }
  if (
      lifeMemory.value.lifeContent == undefined ||
      lifeMemory.value.lifeContent == ''
  ) {
    ElMessage.warning('【生活记忆内容】不能为空')
    return
  }
  //数据处理
  if (
      lifeMemory.value.memoryPlace != null &&
      lifeMemory.value.memoryPlace != '' &&
      lifeMemory.value.memoryPlace != undefined
  ) {
    lifeMemory.value.memoryPlace = lifeMemory.value.memoryPlace.join(',')
  } else {
    lifeMemory.value.memoryPlace = null
  }
  if (memoryImageList.value != null && memoryImageList.value.length > 0) {
    lifeMemory.value.memoryImages = memoryImageList.value.join(',')
  } else {
    lifeMemory.value.memoryImages = null
  }
  const { code, message } = await SaveLifeMemory(lifeMemory.value)
  if (code === 200) {
    dialogVisible.value = false
    ElMessage.success(message)
    lifeFetchData()
  } else {
    ElMessage.error(message)
  }
}

//--------------------文件上传处理------------------------
const headers = {
  token: useApp().authorization.token, // 从pinia中获取token，在进行文件上传的时候将token设置到请求头中
}
//-----------上传记忆轮播图图片--------
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
const handlePictureCardPreview = file => {
  dialogImageUrl.value = file.url
  dialogVisibleHandle.value = true
}

//---------------------------------------删除记忆------------------------------------
//点击删除生活记忆按钮后触发
const deleteLifeMemoryById = row => {
  ElMessageBox.confirm('确定要从外置大脑清除这条生活记忆吗?', 'Warning', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    const { code, message } = await DeleteLifeMemoryById(row.id)
    if (code === 200) {
      ElMessage.success(message)
      lifeFetchData()
    } else {
      ElMessage.error(message)
    }
  })
}

//--------------------------------------------------批量删除记忆功能-------------------------------------------------
// 选中的行数据
const selectedRows = ref([])
// 获取表格引用
const multipleTable = ref(null)

// 处理选中行变化
const handleSelectionChange = selection => {
  selectedRows.value = selection
}
// 批量删除函数
const deleteSelectAll = async () => {
  if (!selectedRows.value || selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要删除的生活记忆记录')
    return
  }

  await ElMessageBox.confirm(
    `确定要批量删除选中的 ${selectedRows.value.length} 条生活记忆记录吗？此操作不可恢复！`,
    '警告',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'batch-delete-confirm-btn',
      cancelButtonClass: 'batch-delete-cancel-btn',
    }
  )

  // 获取所有选中记录的ID
  const selectedIds = selectedRows.value.map(row => row.id)

  // 调用批量删除的API
  const { code, message } = await DeleteAllLifeMemoryByIds(selectedIds)
  if (code === 200) {
    // 刷新数据
    lifeFetchData()

    // 清空选中状态
    if (multipleTable.value) {
      multipleTable.value.clearSelection()
    }
    selectedRows.value = []

    ElMessage.success(message)
  } else {
    ElMessage.error(message)
  }
}

//-----------------------------------------一键导出功能实现---------------------------------------
//-----------------------------------导出功能配置-----------------------------------
// 可导出的列配置
const exportColumns = [
  { key: 'memoryNo', label: '记忆编号', width: 20 },
  { key: 'beginTime', label: '记忆开始时间', width: 20 },
  { key: 'endTime', label: '记忆结束时间', width: 20 },
  { key: 'lifeType', label: '生活记忆类型', width: 12 },
  { key: 'consumeType', label: '消费类型', width: 12 },
  { key: 'memorySource', label: '记忆来源', width: 12 },
  { key: 'lifeContent', label: '生活记忆内容', width: 40 },
  { key: 'lifeConsume', label: '生活消费', width: 12 },
  { key: 'memoryOwnerName', label: '记忆所属人', width: 12 },
  { key: 'updateTime', label: '修改时间', width: 20 },
  { key: 'updateBy', label: '修改者', width: 12 },
]

// 数据格式化函数
const lifeDataFormatter = (item, key, value) => {
  switch (key) {
    case 'lifeType':
      return getDisplayText(value, lifeMemoryTypeItem.value)
    case 'consumeType':
      return getDisplayText(value, consumeTypeItem.value)
    case 'memorySource':
      return getDisplayText(value, lifeMemorySourceItem.value)
    default:
      return value
  }
}

// 获取全部数据的函数
const fetchAllLifeData = async () => {
  const { data } = await GetLifeMemoryByConditionAndPage(
      1,
      10000,
      lifeQueryDto.value
  )

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

  return data.list
}

// 使用导出Hook
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
  fetchAllData: fetchAllLifeData,
  dataFormatter: lifeDataFormatter,
  defaultFileName: '生活记忆数据',
  sheetName: '生活记忆数据'
})

// 包装显示导出对话框的方法
const showExportDialog = () => {
  showExport(lifeList.value, lifeTotal.value)
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

/* ***************************添加或修改模态窗口样式优化 - 添加全屏功能 ***********************/
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
  transition: all 0.3s ease;
}

/* 全屏状态下的样式调整 */
:deep(.enhanced-dialog.is-fullscreen) {
  max-height: 100vh !important;
  width: 100vw !important;
  margin: 0 !important;
  border-radius: 0 !important;
}

:deep(.enhanced-dialog.is-fullscreen .el-dialog) {
  width: 100% !important;
  height: 100vh !important;
  max-height: 100vh !important;
  border-radius: 0 !important;
  margin: 0 !important;
}

:deep(.enhanced-dialog.is-fullscreen .el-dialog__body) {
  max-height: calc(100vh - 120px) !important;
  flex: 1;
}

:deep(.enhanced-dialog.is-fullscreen .scrollable-form) {
  max-height: calc(100vh - 180px) !important;
}

/* 重点调整：标题栏高度优化 - 添加全屏按钮 */
:deep(.enhanced-dialog .el-dialog__header) {
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
  margin: 0;
  padding: 12px 60px 12px 20px; /* 右侧增加内边距为全屏按钮留空间 */
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

/* 全屏按钮样式 */
:deep(.enhanced-dialog .el-dialog__headerbtn.fullscreen-btn) {
  position: absolute;
  top: 50% !important;
  right: 50px; /* 放在关闭按钮左侧 */
  transform: translateY(-50%);
  margin-top: 0;
  height: 24px;
  width: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
  color: white;
}

:deep(.enhanced-dialog .el-dialog__headerbtn.fullscreen-btn:hover) {
  background: rgba(255, 255, 255, 0.1);
}

:deep(.enhanced-dialog .el-dialog__headerbtn.fullscreen-btn .el-dialog__close) {
  color: white;
  font-size: 16px;
  font-weight: normal;
  display: flex;
  align-items: center;
  justify-content: center;
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
/*****************************************************************/
</style>
