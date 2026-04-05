<template>
  <div class="learnMemoryAccessDT">
    <h1 style="margin-top: 10px; font-family: 方正姚体; color: black">
      &emsp;
      <el-icon><Notebook /></el-icon>
      学习记忆接入
    </h1>

    <!--  搜索表单  -->
    <div class="search-div">
      <el-form label-width="120px" size="small">
        <!--    第一行查询条件    -->
        <el-row>
          <el-col :span="6">
            <el-form-item label="记忆编号">
              <el-input
                  v-model="learnQueryDto.memoryNo"
                  style="width: 100%"
                  clearable
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="学习记忆来源">
              <el-select
                  v-model="learnQueryDto.memorySource"
                  multiple
                  placeholder="请选择"
                  style="width: 100%"
                  clearable
              >
                <el-option
                    v-for="item in learnMemorySourceItem"
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
            <el-form-item label="学习记忆类型">
              <el-tree-select
                  v-model="learnQueryDto.learnType"
                  :data="learnMemoryTypeItem"
                  :props="{ value: 'value', label: 'text', children: 'children' }"
                  placeholder="请选择"
                  style="width: 100%"
                  clearable
                  check-strictly
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row style="display:flex">
          <el-button
              type="primary"
              size="small"
              @click="searchLearnMemory"
              class="beautified-search-btn"
          >
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button
              size="small"
              @click="resetLearnMemory"
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
          @click="addLearnMemory"
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
        :title="learnMemory.id ? '学习记忆重塑' : '学习记忆接入'"
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
                <el-form-item label="学习开始时间">
                  <el-date-picker
                      v-model="learnMemory.beginTime"
                      type="datetime"
                      style="width: 100%"
                      placeholder="选择日期时间"
                      :editable="false"
                      :value-format="'YYYY-MM-DD HH:mm:ss'"
                  ></el-date-picker>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="学习结束时间">
                  <el-date-picker
                      v-model="learnMemory.endTime"
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
                <el-form-item label="学习记忆类型">
                  <el-tree-select
                      v-model="learnMemory.learnType"
                      :data="learnMemoryTypeItem"
                      :props="{ value: 'value', label: 'text', children: 'children' }"
                      placeholder="请选择学习记忆类型"
                      style="width: 100%"
                      clearable
                      check-strictly
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="学习记忆来源">
                  <el-select
                      v-model="learnMemory.memorySource"
                      placeholder="请选择学习记忆来源"
                      style="width: 100%"
                      clearable
                  >
                    <el-option
                        v-for="item in learnMemorySourceItem"
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
                <el-form-item label="学习记忆册">
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
                      v-model="learnMemory.memoryPlace"
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
                      v-model="learnMemory.memoryPlaceDetail"
                      style="width: 100%"
                      clearable
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <!--    第六行    -->
            <el-row>
              <el-col :span="24">
                <el-form-item label="学习内容">
                  <el-input
                      type="textarea"
                      style="width: 100%"
                      :rows="3"
                      placeholder="请输入学习内容"
                      v-model="learnMemory.learnContent"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <!--    第七行    -->
            <el-row>
              <el-col :span="24">
                <el-form-item label="学习笔记">
                  <el-input
                      type="textarea"
                      style="width: 100%"
                      :rows="3"
                      placeholder="请输入学习笔记"
                      v-model="learnMemory.learnNode"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <!--    第八行    -->
            <el-row>
              <el-col :span="10">
                <el-form-item label="学习文档">
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
                    :on-preview="handleDocumentPreview"
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
          <el-button type="primary" @click="submitLearnMemory">提交</el-button>
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
        :current-count="learnList.length"
        :total-count="learnTotal"
        @confirm="handleExport"
        @closed="resetExport"
    />

    <!-- 列表展示  -->
    <el-table
        :data="learnList"
        style="width: 100%"
        height="300"
        ref="multipleTable"
        @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="40"></el-table-column>
      <el-table-column label="操作" align="center" width="300" #default="scope">
        <div class="action-buttons">
          <el-button
              type="primary"
              size="small"
              @click="editLearnMemory(scope.row)"
              class="beautified-edit-btn"
          >
            <el-icon><Edit /></el-icon>
            记忆编辑
          </el-button>
          <el-button
              type="danger"
              size="small"
              @click="deleteLearnMemoryById(scope.row)"
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
      <el-table-column prop="memoryNo" label="学习记忆编号" width="180" />
      <el-table-column prop="beginTime" label="学习开始时间" width="180" />
      <el-table-column prop="endTime" label="学习结束时间" width="180" />
      <el-table-column prop="learnDuration" label="学习时长(h)" width="120" />
      <el-table-column
          prop="memorySource"
          label="学习记忆来源"
          width="120"
          #default="scope"
      >
        {{ getDisplayText(scope.row.memorySource, learnMemorySourceItem) }}
      </el-table-column>
      <el-table-column
          prop="learnType"
          label="学习记忆类型"
          width="150"
          #default="scope"
      >
        {{ getDisplayTextByTree(scope.row.learnType, learnMemoryTypeItem) }}
      </el-table-column>
      <el-table-column
          prop="memoryImages"
          label="学习记忆册"
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
      <el-table-column prop="learnContent" label="学习内容" width="300">
        <template #default="scope">
          <span :title="scope.row.learnContent">
            {{ scope.row.learnContent && scope.row.learnContent.length > 100 ? scope.row.learnContent.substring(0, 100) + '...' : scope.row.learnContent }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="learnNode" label="学习笔记" width="300">
        <template #default="scope">
          <span :title="scope.row.learnNode">
            {{ scope.row.learnNode && scope.row.learnNode.length > 100 ? scope.row.learnNode.substring(0, 100) + '...' : scope.row.learnNode }}
          </span>
        </template>
      </el-table-column>
      <el-table-column
        prop="learnDocument"
        label="学习文档"
        width="200"
        #default="scope"
      >
        {{ getDocumentNames(scope.row.learnDocument) }}
      </el-table-column>
<!--      <el-table-column-->
<!--          prop="memoryPlace"-->
<!--          label="记忆地点"-->
<!--          width="200"-->
<!--          #default="scope"-->
<!--      >-->
<!--        {{ getDisplayTextByTree(scope.row.memoryPlace, formattedAddressOptions) }}-->
<!--      </el-table-column>-->
      <el-table-column prop="rowMemoryNo" label="原始记忆编号" width="180" />
      <el-table-column prop="memoryOwnerName" label="记忆所属人" width="120" />
      <el-table-column prop="updateTime" label="修改时间" width="180" />
      <el-table-column prop="updateBy" label="修改者" width="120" />
    </el-table>

    <!--分页条-->
    <el-pagination
        style="margin-top: 30px"
        v-model:current-page="learnPageParams.page"
        v-model:page-size="learnPageParams.limit"
        :page-sizes="[10, 20, 50, 100]"
        @size-change="learnFetchData"
        @current-change="learnFetchData"
        layout="total, sizes, prev, pager, next"
        :total="learnTotal"
    />
  </div>
</template>

<script setup>
import {onMounted, ref} from "vue";
import {getTodayTimeRange} from "@/utils/common";
import {GetAdministrative, GetKeyAndValueByType, GetAllSysCode} from "@/api/sysDict";
import {
  GetLearnMemoryByConditionAndPage,
  SaveLearnMemory,
  DeleteLearnMemoryById,
  DeleteAllLearnMemoryByIds
} from "@/api/memoryReception";
import {useApp} from "@/pinia/modules/app";
import {ElMessage, ElMessageBox} from "element-plus";
import {useExport} from "@/components/Export/hooks/useExport";
import ExportDialog from '@/components/Export/ExportDialog.vue';

//--------------------钩子函数-------------------------
onMounted(() => {
  //1.加载数据字典
  getFormattedAddressOptions() //行政区划
  getLearnMemorySourceItem();
  getLearnMemoryTypeItem();

  //2.查询条件设置默认时间为今天
  const [startOfDay, endOfDay] = getTodayTimeRange()
  beginTimeArea.value[0] = startOfDay
  beginTimeArea.value[1] = endOfDay
  learnQueryDto.value.beginTime = startOfDay
  learnQueryDto.value.endTime = endOfDay

  //3.调用查询数据接口
  learnFetchData()
});

//------------------------公共方法---------------------------
// 通用方法：根据值和映射表获取中文文本
const getDisplayText = (value, mappingArray) => {
  if (!value) return '-'
  const foundItem = mappingArray.find(item => item.value === value)
  return foundItem ? foundItem.text : value
}

//通用方法：根据值和映射表获取中文文本值（树形）
const getDisplayTextByTree = (value, treeData) => {
  if (!value) return '-'

  const traverse = nodes => {
    for (const node of nodes) {
      if (node.value === value) {
        return node.text
      }
      if (node.children?.length) {
        const result = traverse(node.children)
        if (result) return result
      }
    }
    return null
  }

  const result = traverse(treeData)
  return result || value
}

// 获取文档名称列表
const getDocumentNames = documentPaths => {
  if (!documentPaths || documentPaths.length === 0) return '-'

  // 如果是字符串，分割成数组
  const paths = Array.isArray(documentPaths)
    ? documentPaths
    : documentPaths.split(',')

  // 从每个路径中提取文件名并清洗
  return paths
    .map(path => {
      // 从路径中提取文件名部分
      const fileName = path.substring(path.lastIndexOf('/') + 1)
      // 使用现有的cleanFileName函数清洗文件名
      return cleanFileName(fileName)
    })
    .join(', ')
}

//---------------------数据字典获取-------------------------
//学习记忆来源
const learnMemorySourceItem = ref([]);
const getLearnMemorySourceItem = async () => {
  const { data } = await GetKeyAndValueByType('t_learn_memory_source')
  learnMemorySourceItem.value = data
}
//学习记忆类型
const learnMemoryTypeItem = ref([]);
const getLearnMemoryTypeItem = async () => {
  const { data } = await GetAllSysCode('t_learn_memory_type')
  learnMemoryTypeItem.value = data
}
//获取中国统计用行政区划下拉列表
const formattedAddressOptions = ref([])
const getFormattedAddressOptions = async () => {
  const { data } = await GetAdministrative()
  formattedAddressOptions.value = data
}

//--------------------------------学习记忆列表展示----------------------------------
//定义学习记忆列表参数接收
const learnList = ref([]);
//分页条数封装
const learnTotal = ref(0);
//分页封装
const learnPageParams = ref({ page: 1, limit: 10 });
//条件查询参数存储
const learnQueryDto = ref({
  memoryNo: "",
  memorySource: "",
  beginTime: "",
  endTime: "",
  learnType: ""
});
//条件查询时间范围封装
const beginTimeArea = ref([]);

//发送请求，拿到学习记忆列表
const learnFetchData = async () => {
  const {data} = await GetLearnMemoryByConditionAndPage(learnPageParams.value.page, learnPageParams.value.limit, learnQueryDto.value);

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
    if (item.learnDocument != null && item.learnDocument != '') {
      item.learnDocument = item.learnDocument.split(',')
    } else {
      item.learnDocument = []
    }
  })

  learnTotal.value = data.total;
  learnList.value = data.list;
}

//搜索按钮点击触发事件
const searchLearnMemory = () => {
  //时间参数赋值
  learnQueryDto.value.beginTime = beginTimeArea.value[0]
  learnQueryDto.value.endTime = beginTimeArea.value[1]

  //调用查询接口
  learnFetchData()
}
//重置按钮点击触发事件
const resetLearnMemory = () => {
  //时间范围重置
  beginTimeArea.value = []
  //参数重置
  learnQueryDto.value = {}

  //调用查询接口
  learnFetchData()
}

//--------------------------------学习记忆新增和修改----------------------------------
//对话框显示控制
const dialogVisible = ref(false);
//图片预览对话框
const dialogVisibleHandle = ref(false);
const dialogImageUrl = ref('');
//学习记忆表单数据
const learnMemory = ref({
  id: null,
  memoryNo: '',
  beginTime: '',
  endTime: '',
  learnDuration: null,
  memoryPlace: [],
  memoryPlaceDetail: '',
  memoryImages: '',
  memorySource: null,
  rowMemoryNo: '',
  learnType: '',
  learnContent: '',
  learnNode: '',
  learnDocument: ''
});
//文件列表
const fileList = ref([]);
const documentFileList = ref([]);
const workDocumentList = ref([]);

//新增按钮
const addLearnMemory = () => {
  //数据重置
  learnMemory.value = {}
  learnMemory.value.memorySource = 1
  if (learnList.value.length > 0) {
    learnMemory.value.beginTime = learnList.value[0].endTime
  }
  //图片列表数据重置
  memoryImageList.value = []
  fileList.value = []
  //文档列表数据重置
  documentFileList.value = []
  workDocumentList.value = []

  //打开模态窗口
  dialogVisible.value = true;
}

//修改按钮点击事件
const editLearnMemory = (row) => {
  //数据设置
  learnMemory.value = { ...row }
  //图片文件列表数据设置
  fileList.value = []
  memoryImageList.value = learnMemory.value.memoryImages
  memoryImageList.value.forEach(url => {
    fileList.value.push({ url: url })
  })
  //文档文件列表数据设置
  documentFileList.value = []
  workDocumentList.value = Array.isArray(learnMemory.value.learnDocument)
    ? [...learnMemory.value.learnDocument]
    : []
  if (workDocumentList.value != null && workDocumentList.value != '') {
    workDocumentList.value.forEach(url => {
      // 从URL中提取文件名并清洗
      const fileName = url.substring(url.lastIndexOf('/') + 1)
      const cleanedFileName = cleanFileName(fileName)
      documentFileList.value.push({
        name: cleanedFileName,
        url: url,
      })
    })
  }

  //打开模态窗口
  dialogVisible.value = true
}

//点击手动录入和修改模态窗口中的提交按钮触发
const submitLearnMemory = async () => {
  //基本验证
  if (
      learnMemory.value.beginTime == undefined ||
      learnMemory.value.beginTime == ''
  ) {
    ElMessage.warning('【学习开始时间】不能为空')
    return
  }
  if (learnMemory.value.endTime == undefined || learnMemory.value.endTime == '') {
    ElMessage.warning('【学习结束时间】不能为空')
    return
  }
  if (
      learnMemory.value.learnContent == undefined ||
      learnMemory.value.learnContent == ''
  ) {
    ElMessage.warning('【学习内容】不能为空')
    return
  }
  //数据处理
  if (
      learnMemory.value.memoryPlace != null &&
      learnMemory.value.memoryPlace != '' &&
      learnMemory.value.memoryPlace != undefined
  ) {
    learnMemory.value.memoryPlace = learnMemory.value.memoryPlace.join(',')
  } else {
    learnMemory.value.memoryPlace = null
  }
  if (memoryImageList.value != null && memoryImageList.value.length > 0) {
    learnMemory.value.memoryImages = memoryImageList.value.join(',')
  } else {
    learnMemory.value.memoryImages = null
  }
  if (workDocumentList.value != null && workDocumentList.value.length > 0) {
    learnMemory.value.learnDocument = workDocumentList.value.join(',')
  } else {
    learnMemory.value.learnDocument = null
  }
  const { code, message } = await SaveLearnMemory(learnMemory.value)
  if (code === 200) {
    dialogVisible.value = false
    ElMessage.success(message)
    learnFetchData()
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
//上传成功触发
const handleSliderSuccess = (response, uploadFile) => {
  memoryImageList.value.push(response.data)
}
const handlePictureCardPreview = file => {
  dialogImageUrl.value = file.url
  dialogVisibleHandle.value = true
}
//-------------文档上传----------------
// 文档预览处理函数 - 简化为直接下载
const handleDocumentPreview = file => {
  // 直接下载文件
  if (file.url) {
    const link = document.createElement('a')
    link.href = file.url
    const fileNameFromUrl = file.url.substring(file.url.lastIndexOf('/') + 1)
    link.download = cleanFileName(fileNameFromUrl)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  }
}

// 上传成功处理
const handleDocumentSuccess = (response, file, fileList) => {
  if (response.code === 200) {
    const fileUrl = response.data
    workDocumentList.value.push(fileUrl)
    const cleanedName = cleanFileName(file.name)
    file.name = cleanedName
    ElMessage.success(`文档上传成功，文件名：${cleanedName}`)
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}
// 上传错误处理
const handleUploadError = (error, file, fileList) => {
  if (error.status === 413) {
    ElMessage.error('文件大小超过限制')
  } else {
    ElMessage.error('上传失败，请重试')
  }
}
// 上传前的处理（可选：添加额外信息）
const addFileInfo = file => {
  return true
}
// 文档移除处理
const handleDocumentRemove = (file, fileList) => {
  const urlToRemove = file.url
  const index = workDocumentList.value.findIndex(
    docUrl => docUrl === urlToRemove
  )

  if (index !== -1) {
    workDocumentList.value.splice(index, 1)
    ElMessage.success('文档已移除')
  } else {
    const altIndex = workDocumentList.value.findIndex(
      docUrl =>
        docUrl.includes(urlToRemove.split('/').pop()) ||
        urlToRemove.includes(docUrl.split('/').pop())
    )
    if (altIndex !== -1) {
      workDocumentList.value.splice(altIndex, 1)
      ElMessage.success('文档已移除')
    }
  }

  const fileIndex = documentFileList.value.findIndex(f => f.url === urlToRemove)
  if (fileIndex !== -1) {
    documentFileList.value.splice(fileIndex, 1)
  }
}

//-------------辅助函数（针对文档上传）----------------
// 文件名清洗函数 - 支持大小写的32位十六进制UUID
const cleanFileName = fileName => {
  if (!fileName) return '未知文件'

  const hex32Pattern = /^[a-fA-F0-9]{32}/
  if (hex32Pattern.test(fileName)) {
    return fileName.substring(32)
  }

  const underscoreIndex = fileName.indexOf('_')
  if (underscoreIndex > 0) {
    return fileName.substring(underscoreIndex + 1)
  }

  const timestampPattern = /^\d{8}(_)?/
  if (timestampPattern.test(fileName)) {
    const match = fileName.match(timestampPattern)
    if (match) {
      return fileName.substring(match[0].length)
    }
  }

  const uuidWithHyphenPattern = /^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}/
  if (uuidWithHyphenPattern.test(fileName)) {
    return fileName.substring(36)
  }

  return fileName
}

//---------------------------------------删除记忆------------------------------------
//点击删除学习记忆按钮后触发
const deleteLearnMemoryById = row => {
  ElMessageBox.confirm('确定要从外置大脑清除这条学习记忆吗?', 'Warning', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    const { code, message } = await DeleteLearnMemoryById(row.id)
    if (code === 200) {
      ElMessage.success(message)
      learnFetchData()
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
    ElMessage.warning('请先选择要删除的学习记忆记录')
    return
  }

  await ElMessageBox.confirm(
    `确定要批量删除选中的 ${selectedRows.value.length} 条学习记忆记录吗？此操作不可恢复！`,
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
  const { code, message } = await DeleteAllLearnMemoryByIds(selectedIds)
  if (code === 200) {
    // 刷新数据
    learnFetchData()

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
  { key: 'learnType', label: '学习记忆类型', width: 12 },
  { key: 'memoryNo', label: '记忆编号', width: 20 },
  { key: 'beginTime', label: '记忆开始时间', width: 20 },
  { key: 'endTime', label: '记忆结束时间', width: 20 },
  { key: 'learnDuration', label: '学习时长(h)', width: 12 },
  { key: 'memorySource', label: '记忆来源', width: 12 },
  { key: 'learnContent', label: '学习内容', width: 40 },
  { key: 'learnNode', label: '学习笔记', width: 40 },
  { key: 'learnDocument', label: '学习文档', width: 25 },
  { key: 'rowMemoryNo', label: '原始记忆编号', width: 20 },
  { key: 'memoryOwnerName', label: '记忆所属人', width: 12 },
  { key: 'updateTime', label: '修改时间', width: 20 },
  { key: 'updateBy', label: '修改者', width: 12 },
]

// 数据格式化函数
const learnDataFormatter = (item, key, value) => {
  switch (key) {
    case 'learnType':
      return getDisplayTextByTree(value, learnMemoryTypeItem.value)
    case 'memorySource':
      return getDisplayText(value, learnMemorySourceItem.value)
    case 'learnDocument':
      return getDocumentNames(value)
    default:
      return value
  }
}

// 获取全部数据的函数
const fetchAllLearnData = async () => {
  const { data } = await GetLearnMemoryByConditionAndPage(
      1,
      10000,
      learnQueryDto.value
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
    if (item.learnDocument != null && item.learnDocument != '') {
      item.learnDocument = item.learnDocument.split(',')
    } else {
      item.learnDocument = []
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
  fetchAllData: fetchAllLearnData,
  dataFormatter: learnDataFormatter,
  defaultFileName: '学习记忆数据',
  sheetName: '学习记忆数据'
})

// 包装显示导出对话框的方法
const showExportDialog = () => {
  showExport(learnList.value, learnTotal.value)
}
</script>

<style scoped>
/**********************基本背景及页面布局样式*******************/
.learnMemoryAccessDT {
  position: relative;
  width: 100%;
  min-height: 100vh;
  margin: 0;
  padding: 0;
  overflow: auto;
}

.learnMemoryAccessDT::before {
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

.learnMemoryAccessDT > * {
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

/* 重点调整：标题栏高度优化 */
:deep(.enhanced-dialog .el-dialog__header) {
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
  margin: 0;
  padding: 12px 60px 12px 20px;
  display: flex;
  align-items: center;
  min-height: 40px;
  position: relative;
}

:deep(.enhanced-dialog .el-dialog__title) {
  color: white;
  font-weight: 600;
  font-size: 16px;
  line-height: 1.2;
  flex: 1;
}

:deep(.enhanced-dialog .el-dialog__headerbtn) {
  position: absolute;
  top: 50% !important;
  right: 16px;
  transform: translateY(-50%);
  margin-top: 0;
  height: 24px;
  width: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
}

:deep(.enhanced-dialog .el-dialog__headerbtn .el-dialog__close) {
  color: white;
  font-size: 20px;
  font-weight: bold;
}

:deep(.enhanced-dialog .el-dialog__body) {
  padding: 0;
  max-height: calc(80vh - 100px) !important;
  overflow: hidden;
  display: flex;
  flex: 1;
}

.dialog-content {
  width: 100%;
  padding: 20px;
}

.scrollable-form {
  max-height: calc(80vh - 160px);
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
/****************************************************************/

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
</style>
