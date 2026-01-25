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

    <!--总体操作按钮一览-->
    <div class="tools-div beautified-tools">
      <el-button
        type="success"
        size="small"
        @click="addWorkMemory"
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
      :title="workMemory.id ? '工作记忆重塑' : '工作记忆接入'"
      width="80%"
      class="custom-dialog enhanced-dialog"
      :close-on-click-modal="false"
      :lock-scroll="false"
      align-center
      draggable
      :fullscreen="isFullscreen"
    >
      <div class="dialog-content">
        <el-form label-width="120px" class="scrollable-form">
          <el-form label-width="120px">
            <!--    第一行    -->
            <el-row>
              <el-col :span="12">
                <el-form-item label="工作开始时间">
                  <el-date-picker
                    v-model="workMemory.beginTime"
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
                    v-model="workMemory.endTime"
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
                <el-form-item label="业务类型">
                  <el-tree-select
                    v-model="workMemory.workBusinessType"
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
              <el-col :span="12">
                <el-form-item label="技术类型">
                  <el-tree-select
                    v-model="workMemory.workTechType"
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
            <!--    第四行    -->
            <el-row>
              <el-col :span="12">
                <el-form-item label="记忆地点">
                  <el-cascader
                    v-model="workMemory.workAddress"
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
                    v-model="workMemory.workAddressDetail"
                    style="width: 100%"
                    clearable
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <!--    第五行    -->
            <el-row>
              <el-col :span="24">
                <el-form-item label="工作内容">
                  <el-input
                    type="textarea"
                    style="width: 100%"
                    :rows="5"
                    placeholder="请输入工作记忆内容"
                    v-model="workMemory.workContent"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <!--   第六行     -->
            <el-row>
              <el-col :span="24">
                <el-form-item label="工作业务笔记">
                  <el-input
                    type="textarea"
                    style="width: 100%"
                    :rows="5"
                    placeholder="请输入工作业务笔记"
                    v-model="workMemory.workBusinessNode"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <!--   第七行     -->
            <el-row>
              <el-col :span="24">
                <el-form-item label="工作技术笔记">
                  <el-input
                    type="textarea"
                    style="width: 100%"
                    :rows="5"
                    placeholder="请输入工作技术笔记"
                    v-model="workMemory.workTechNode"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <!--    第八行    -->
            <el-row>
              <el-col :span="12">
                <el-form-item label="记忆来源">
                  <el-select
                    v-model="workMemory.memorySource"
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
              <el-col :span="10">
                <el-form-item label="工作文档">
                  <el-upload
                    class="document-uploader"
                    action="http://localhost:8400/superBrain/system/fileUpload"
                    :show-file-list="true"
                    :file-list="documentFileList"
                    :headers="headers"
                    accept=".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.md"
                    :on-success="handleDocumentSuccess"
                    :on-error="handleUploadError"
                    :on-remove="handleDocumentRemove"
                    :before-upload="addFileInfo"
                  >
                    <el-button type="primary" class="upload-button">
                      <el-icon><Upload /></el-icon>
                      点击上传文档
                    </el-button>
                    <template #tip>
                      <div class="upload-tip">
                        支持格式：PDF、Word、Excel、PPT、TXT、MD等文档格式
                      </div>
                    </template>
                  </el-upload>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-form>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitWorkMemory">提交</el-button>
        </span>
      </template>
    </el-dialog>

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
import { nextTick, onMounted, ref } from 'vue'
import {
  GetAdministrative,
  GetAllSysCode,
  GetKeyAndValueByType,
} from '@/api/sysDict'
import {
  GetWorkMemoryByConditionAndPage,
  SaveWorkMemory,
} from '@/api/memoryReception'
import { getTodayTimeRange } from '@/utils/common'
import { useApp } from '@/pinia/modules/app'
import { ElMessage } from 'element-plus'

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
    if (item.workDocument != null && item.workDocument != '') {
      item.workDocument = item.workDocument.split(',')
    } else {
      item.workDocument = []
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

//-------------------------------------保存原始记忆------------------------------------
//工作记忆数据存储
const workMemory = ref({})
//模特窗口控制
const dialogVisible = ref(false)

//手动录入按钮点击
const addWorkMemory = () => {
  //数据重置
  workMemory.value = {}
  workMemory.value.memorySource = 1
  if (workList.value.length > 0) {
    workMemory.value.beginTime = workList.value[0].endTime
  }
  //图片列表数据重置
  memoryImageList.value = []
  fileList.value = []
  //文档列表数据重置
  documentFileList.value = []
  workDocumentList.value = []

  // 重置全屏状态
  isFullscreen.value = false
  // 添加全屏按钮
  nextTick(() => {
    addFullscreenButton()
  })

  //打开模特窗口
  dialogVisible.value = true
}

//记忆编辑按钮点击
const editWorkMemory = row => {
  //数据设置
  workMemory.value = { ...row }
  //图片文件列表数据设置
  fileList.value = []
  memoryImageList.value = workMemory.value.memoryImages
  memoryImageList.value.forEach(url => {
    fileList.value.push({ url: url })
  })
  //文档文件列表数据设置
  documentFileList.value = []
  workDocumentList.value = workMemory.value.workDocument
  workDocumentList.value.forEach(url => {
    documentFileList.value.push({ url: url })
  })

  // 重置全屏状态
  isFullscreen.value = false
  // 添加全屏按钮
  nextTick(() => {
    addFullscreenButton()
  })

  //打开模态窗口
  dialogVisible.value = true
}

//点击手动录入和修改模态窗口中的提交按钮触发
const submitWorkMemory = async () => {
  //console.log("数据"+rowMemory.value.memoryImages.join(','))
  //基本验证
  if (
    workMemory.value.beginTime == undefined ||
    workMemory.value.beginTime == ''
  ) {
    ElMessage.warning('【工作开始时间】不能为空')
    return
  }
  if (workMemory.value.endTime == undefined || workMemory.value.endTime == '') {
    ElMessage.warning('【工作结束时间】不能为空')
    return
  }
  if (
    workMemory.value.workContent == undefined ||
    workMemory.value.workContent == ''
  ) {
    ElMessage.warning('【工作内容】不能为空')
    return
  }
  //数据处理
  if (
    workMemory.value.workAddress != null &&
    workMemory.value.workAddress != '' &&
    workMemory.value.workAddress != undefined
  ) {
    workMemory.value.workAddress = workMemory.value.workAddress.join(',')
  } else {
    workMemory.value.workAddress = null
  }
  if (memoryImageList.value != null && memoryImageList.value.length > 0) {
    workMemory.value.memoryImages = memoryImageList.value.join(',')
  } else {
    workMemory.value.memoryImages = null
  }
  if (workDocumentList.value != null && workDocumentList.value.length > 0) {
    workMemory.value.workDocument = workDocumentList.value.join(',')
  } else {
    workMemory.value.workDocument = null
  }
  const { code, message } = await SaveWorkMemory(workMemory.value)
  if (code === 200) {
    dialogVisible.value = false
    ElMessage.success(message)
    workFetchData()
  } else {
    ElMessage.error(message)
  }
}

//-------------模态窗口全屏处理----------------
// 全屏状态控制
const isFullscreen = ref(false)
// 切换全屏功能
const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
}
// 添加全屏按钮到标题栏
const addFullscreenButton = () => {
  const dialogHeader = document.querySelector(
    '.enhanced-dialog .el-dialog__header'
  )
  if (dialogHeader && !dialogHeader.querySelector('.fullscreen-btn')) {
    const fullscreenBtn = document.createElement('button')
    fullscreenBtn.className = 'el-dialog__headerbtn fullscreen-btn'
    fullscreenBtn.style.right = '50px'
    fullscreenBtn.innerHTML = `
      <i class="el-dialog__close">
        <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
          <path fill="currentColor" d="m160 96.064 192 192V160h64v192H160v-64h128L160 96.064zm0 831.872 192-192v128h64V672H160v64h128L160 927.936zm704-831.872-192 192V160h-64v192h256v-64H704L864 96.064zm0 831.872-192-192v128h-64V672h256v64H704L864 927.936z"/>
        </svg>
      </i>
    `
    fullscreenBtn.onclick = toggleFullscreen
    dialogHeader.appendChild(fullscreenBtn)
  }
}

//-----------------文件上传处理--------------------
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
//-------------文档上传----------------
// 文件列表（如果需要保存到表单数据中）
const documentFileList = ref([]) //文件回显
const workDocumentList = ref([]) //数据
// 上传成功处理
const handleDocumentSuccess = (response, file, fileList) => {
  // 如果后端返回成功（假设code为200表示成功）
  if (response.code === 200) {
    // 保存文件信息到列表
    workDocumentList.value.push(response.data)

    // 如果需要，可以在这里触发事件给父组件
    // emit('upload-success', response.data)

    ElMessage.success('文档上传成功')
  } else {
    // 后端返回了错误
    ElMessage.error(response.message || '上传失败')
  }
}
// 上传错误处理
const handleUploadError = (error, file, fileList) => {
  // 文件大小超过限制的错误通常是HTTP 413
  if (error.status === 413) {
    ElMessage.error('文件大小超过限制')
  } else {
    ElMessage.error('上传失败，请重试')
  }
}
// 上传前的处理（可选：添加额外信息）
const addFileInfo = file => {
  // 这里可以添加一些文件信息到FormData中
  // 但不需要做前端的大小验证，完全交给后端
  //console.log('开始上传文件:', file.name, '大小:', file.size)
  return true // 必须返回true才会继续上传
}
// 文档移除处理
const handleDocumentRemove = (file, fileList) => {
  // 从documentFileList中移除
  const index = workDocumentList.value.findIndex(
    doc => doc.name === file.name || doc.url === file.url
  )

  if (index !== -1) {
    workDocumentList.value.splice(index, 1)
    ElMessage.success('文档已移除')
  }
}

//---------------------------------------删除记忆------------------------------------
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

/******************************上传文档样式**************************/
.document-uploader {
  width: 100%;
}

.upload-button {
  width: 100%;
  height: 80px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  background-color: #fafafa;
  color: chartreuse;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.upload-button:hover {
  border-color: #409eff;
  background-color: #f0f7ff;
}

.upload-button .el-icon {
  font-size: 28px;
  margin-bottom: 8px;
  color: #409eff;
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  text-align: center;
}
/*******************************************************************/

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
