<template>
  <el-dialog
    v-model="dialogVisible"
    title="✨ 记忆联想空间"
    width="1200px"
    class="memory-enhanced-dialog"
    :close-on-click-modal="false"
    align-center
    draggable
    :fullscreen="isFullscreen"
    @open="initFullscreen"
  >
    <div class="dialog-body">
      <!-- 记忆信息卡片 -->
      <div class="memory-card">
        <div class="card-header">
          <div class="header-icon">🧠</div>
          <h3 class="card-title">原始记忆档案</h3>
        </div>
        
        <div class="card-content-wrapper">
          <div class="card-content">
            <div class="info-grid">
              <!-- 第一行：3列 -->
              <div class="info-item timeline">
                <div class="item-icon">⏰</div>
                <div class="item-content">
                  <div class="item-label">记忆时段</div>
                  <div class="item-value">
                    {{ getTimePeriodDisplay }}
                  </div>
                </div>
              </div>
              
              <div class="info-item type-item">
                <div class="item-icon">🏷️</div>
                <div class="item-content">
                  <div class="item-label">记忆类型</div>
                  <div class="item-value">
                    {{ getDisplayText(rowData.rowMemoryType, rowMemoryTypeItem) }}
                  </div>
                </div>
              </div>
              
              <div class="info-item source-item">
                <div class="item-icon">📌</div>
                <div class="item-content">
                  <div class="item-label">记忆来源</div>
                  <div class="item-value">
                    {{ getDisplayText(rowData.memorySource, memorySourceItem) }}
                  </div>
                </div>
              </div>
              
              <!-- 第二行：3列 -->
              <div class="info-item contact-type-item">
                <div class="item-icon">👥</div>
                <div class="item-content">
                  <div class="item-label">关系人类型</div>
                  <div class="item-value">
                    {{ getDisplayText(rowData.contactType, contactTypeItem) }}
                  </div>
                </div>
              </div>
              
              <div class="info-item contact-item">
                <div class="item-icon">👤</div>
                <div class="item-content">
                  <div class="item-label">关系人名称</div>
                  <div class="item-value">
                    {{ rowData.contact || '-' }}
                  </div>
                </div>
              </div>
              
              <div class="info-item place-item">
                <div class="item-icon">📍</div>
                <div class="item-content">
                  <div class="item-label">记忆地点</div>
                  <div class="item-value">
                    {{ getMemoryPlaceDisplay(rowData) }}
                  </div>
                </div>
              </div>
              
              <!-- 第三行：3列 -->
              <div class="info-item owner-item">
                <div class="item-icon">👑</div>
                <div class="item-content">
                  <div class="item-label">记忆所属人</div>
                  <div class="item-value">
                    {{ rowData.memoryOwnerName || '-' }}
                  </div>
                </div>
              </div>
              
              <div class="info-item status-item">
                <div class="item-icon">📊</div>
                <div class="item-content">
                  <div class="item-label">联想状态</div>
                  <div class="item-value">
                    {{ getDisplayText(rowData.memoryAssociativeStatus, associativeStatusItem) }}
                  </div>
                </div>
              </div>
              
              <div class="info-item id-item">
                <div class="item-icon">🔢</div>
                <div class="item-content">
                  <div class="item-label">记忆编号</div>
                  <div class="item-value">
                    {{ rowData.memoryNo || '-' }}
                  </div>
                </div>
              </div>
              
              <!-- 第四行：记录人 -->
              <div class="info-item recorder-item" style="grid-column: 1 / span 3;">
                <div class="item-icon">✍️</div>
                <div class="item-content">
                  <div class="item-label">记录人</div>
                  <div class="item-value">
                    {{ rowData.recordBy || '-' }}
                  </div>
                </div>
              </div>
              
              <!-- 长文本内容：单独行，高度自适应 -->
              <div class="info-item content-item long-text-item">
                <div class="item-icon">📝</div>
                <div class="item-content">
                  <div class="item-label">记忆内容</div>
                  <div class="item-value content-text">
                    {{ rowData.rowMemoryContent || '暂无记忆内容' }}
                  </div>
                </div>
              </div>
              
              <div class="info-item reason-item long-text-item">
                <div class="item-icon">❓</div>
                <div class="item-content">
                  <div class="item-label">记忆原因</div>
                  <div class="item-value content-text">
                    {{ rowData.rowMemoryReason || '暂无记忆原因说明' }}
                  </div>
                </div>
              </div>
              
              <div class="info-item action-item long-text-item">
                <div class="item-icon">⚡</div>
                <div class="item-content">
                  <div class="item-label">记忆行为</div>
                  <div class="item-value content-text">
                    {{ rowData.rowMemoryAction || '暂无相关行为记录' }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!--  记忆联想  -->
    <div class="memory-card">
      <div class="card-header">
        <div class="header-icon">🔗</div>
        <h3 class="card-title">记忆联想</h3>
      </div>

      <div class="card-content-wrapper">
        <div class="card-content">
          <div class="info-grid">
            <!-- 联想开始时间 -->
            <div class="info-item time-item">
              <div class="item-icon">🕒</div>
              <div class="item-content">
                <div class="item-label">联想开始时间</div>
                <div class="item-value">
                  {{ associativeMemory.begin_time || '-' }}
                </div>
              </div>
            </div>

            <!--    联想结束时间        -->
            <div class="info-item time-item">
              <div class="item-icon">🕒</div>
              <div class="item-content">
                <div class="item-label">联想结束时间</div>
                <div class="item-value">
                  {{ associativeMemory.end_time || '-' }}
                </div>
              </div>
            </div>

            <!--     联想记忆类型       -->
            <div class="info-item time-item">
              <div class="item-icon">🏷️</div>
              <div class="item-content">
                <div class="item-label">联想记忆类型</div>
                <div class="item-value">
                  {{ getDisplayText(associativeMemory.rowMemoryType, rowMemoryTypeItem) }}
                </div>
              </div>
            </div>

            <!-- 联想内容展示 -->
            <div class="info-item content-item long-text-item">
              <div class="item-icon">💭</div>
              <div class="item-content">
                <div class="item-label">联想内容（可调整）</div>
                <div class="item-value content-text">
                  <el-input
                    v-model="associativeMemory.rowMemoryContent"
                    type="textarea"
                    :rows="4"
                    placeholder="请输入联想内容..."
                    resize="vertical"
                  />
                </div>
              </div>
            </div>
            
            <!--      联想记忆文档      -->
            <div class="info-item time-item">
              <div class="item-icon">📂</div>
              <div class="item-content">
                <div class="item-label">联想记忆文档</div>
                <div class="item-value">
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
                </div>
              </div>
            </div>

            <!--    ----------------------工作记忆联想数据块------------------        -->
            <div class="info-item time-item" v-if="associativeMemory.rowMemoryType == 1">
              <div class="item-icon">🏷️</div>
              <div class="item-content">
                <div class="item-label">工作业务类型</div>
                <div class="item-value">
                  <el-tree-select
                      v-model="associativeMemory.workBusinessType"
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
                </div>
              </div>
            </div>
            <div class="info-item time-item" v-if="associativeMemory.rowMemoryType == 1">
              <div class="item-icon">🏷️</div>
              <div class="item-content">
                <div class="item-label">工作技术类型</div>
                <div class="item-value">
                  <el-tree-select
                      v-model="associativeMemory.workTechType"
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
                </div>
              </div>
            </div>
            <!--     工作业务笔记       -->
            <div class="info-item content-item long-text-item" v-if="associativeMemory.rowMemoryType == 1">
              <div class="item-icon">✍️</div>
              <div class="item-content">
                <div class="item-label">工作业务笔记</div>
                <div class="item-value content-text">
                  <el-input
                      v-model="associativeMemory.workBusinessNode"
                      type="textarea"
                      :rows="4"
                      placeholder="请输入工作业务笔记..."
                      resize="vertical"
                  />
                </div>
              </div>
            </div>
            <!--     工作技术笔记       -->
            <div class="info-item content-item long-text-item" v-if="associativeMemory.rowMemoryType == 1">
              <div class="item-icon">✍️</div>
              <div class="item-content">
                <div class="item-label">工作技术笔记</div>
                <div class="item-value content-text">
                  <el-input
                      v-model="associativeMemory.workTechNode"
                      type="textarea"
                      :rows="4"
                      placeholder="请输入工作技术笔记..."
                      resize="vertical"
                  />
                </div>
              </div>
            </div>

            <!--    ----------------------生活记忆联想数据块------------------        -->

          </div>
        </div>
      </div>
    </div>

    <!-- 联想提示区域 -->
    <div class="association-tip">
      <div class="tip-icon">💡</div>
      <div class="tip-content">
        <h4 class="tip-title">智能联想提示</h4>
        <p class="tip-desc">当前原始记忆档案内容为基本记忆，您可以根据基础信息联想生活，工作，学习，娱乐等记忆！！！</p>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer enhanced">
        <el-button 
          class="cancel-btn" 
          @click="dialogVisible = false"
        >
          <span class="btn-icon">✕</span>
          取消
        </el-button>
        <el-button 
          type="primary" 
          class="submit-btn" 
          @click="submit"
        >
          <span class="btn-icon">⚡</span>
          开始联想
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue'
import {GetAdministrative, GetAllSysCode, GetKeyAndValueByType} from "@/api/sysDict";
import {ElMessage} from "element-plus";
import {useApp} from "@/pinia/modules/app";
import {MemoryAssociation} from "@/api/memoryReception";
import {useFullscreenDialog} from "@/hooks/useFullscreenDialog";

// 在需要全屏的组件中使用 Hook
const { isFullscreen, initFullscreen } = useFullscreenDialog()

// ------------------------------- 基础与父组件建立关系 ------------------------------------------
/* 接收父组件参数 */
const props = defineProps({
  visible: Boolean,
  rowData: Object
})

/* 向父组件发送事件 */
const emit = defineEmits(['update:visible', 'refresh'])

/* 代理 visible，不能直接改 props */
const dialogVisible = computed({
  get() {
    return props.visible
  },
  set(val) {
    emit('update:visible', val)
  }
})

//打开模态窗口时触发，监听父组件，模态窗口打开触发
watch(
    () => props.visible,
    (val) => {
      if (val) {
        //loadDetail()
        getContactTypeItem();
        getRowMemoryTypeItem();
        getMemorySourceItem();
        getAssociativeStatusItem();
        getFormattedAddressOptions();
        getWorkBusinessTypeItem();
        getWorkTechTypeItem();

        //前置操作
        //associativeMemory.value = [];
        //文档列表数据重置
        documentFileList.value = []
        documentList.value = []

        //将原始记忆赋值给associativeMemory
        associativeMemory.value.row_id = props.rowData.id;
        associativeMemory.value.begin_time = props.rowData.recordTime;
        associativeMemory.value.end_time = props.rowData.recordEndTime;
        associativeMemory.value.contactType = props.rowData.contactType;
        associativeMemory.value.contact = props.rowData.contact;
        associativeMemory.value.memoryPlace = props.rowData.memoryPlace.join(',');
        associativeMemory.value.memoryPlaceDetail = props.rowData.memoryPlaceDetail;
        associativeMemory.value.memoryPlaceShort = props.rowData.memoryPlaceShort;
        associativeMemory.value.rowMemoryType = props.rowData.rowMemoryType;
        associativeMemory.value.rowMemoryContent = props.rowData.rowMemoryContent;
        associativeMemory.value.rowMemoryReason = props.rowData.rowMemoryReason;
        associativeMemory.value.rowMemoryAction = props.rowData.rowMemoryAction;
        associativeMemory.value.memoryOwner = props.rowData.memoryOwner;
        associativeMemory.value.memorySource = props.rowData.memorySource;
        associativeMemory.value.memoryImages = props.rowData.memoryImages.join(',');
        associativeMemory.value.memoryNo = props.rowData.memoryNo;
        associativeMemory.value.workBusinessType = "";
        associativeMemory.value.workTechType = "";
        associativeMemory.value.workBusinessNode = "";
        associativeMemory.value.workTechNode = "";
      }
    }
)
//钩子函数
// onMounted(() => {
//   getContactTypeItem();
//   getRowMemoryTypeItem();
//   getMemorySourceItem();
//   getAssociativeStatusItem();
//   getFormattedAddressOptions();
// })

// ----------------------------------- 逻辑操作 -------------------------------------------------
//--------------------------基本信息展示处理----------------------------------
// 使用组件内部的映射数据（保持与原始记忆一致的映射关系）
const rowMemoryTypeItem = ref([]);
const memorySourceItem = ref([]);
const contactTypeItem = ref([]);
const associativeStatusItem = ref([]);

// 地址映射表（模拟原始记忆中的地址数据结构）
const formattedAddressOptions = ref([
  {
    value: '110000',
    label: '北京市',
    children: [
      {
        value: '110100',
        label: '市辖区',
        children: [
          { value: '110101', label: '东城区' },
          { value: '110105', label: '朝阳区' },
          { value: '110108', label: '海淀区' },
          { value: '110112', label: '通州区' },
          { value: '110113', label: '顺义区' },
          { value: '110114', label: '昌平区' },
          { value: '110115', label: '大兴区' }
        ]
      }
    ]
  }
])

//发送请求，获取关系人类型下拉列表
const getContactTypeItem = async () => {
  const { data } = await GetKeyAndValueByType('t_ty_people_relation')
  contactTypeItem.value = data
}
//发送请求，获取原始记忆类型下拉列表
const getRowMemoryTypeItem = async () => {
  const { data } = await GetKeyAndValueByType('t_row_memory_type')
  rowMemoryTypeItem.value = data
}
//发送请求，获取原始记忆来源下拉列表
const getMemorySourceItem = async () => {
  const { data } = await GetKeyAndValueByType('t_row_memory_source')
  memorySourceItem.value = data
}
//获取联想状态下拉列表
const getAssociativeStatusItem = async () => {
  const { data } = await GetKeyAndValueByType('t_memory_associative_status')
  associativeStatusItem.value = data
}
//获取中国行政区划码值对
const getFormattedAddressOptions = async () => {
  const { data } = await GetAdministrative()
  formattedAddressOptions.value = data
}

//------------工作记忆字典----------------
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
//---------------------------------------

// 通用方法：根据值和映射表获取中文文本
const getDisplayText = (value, mappingArray) => {
  if (!value) return '-'
  const foundItem = mappingArray.find(item => item.value === value)
  return foundItem ? foundItem.text : value
}

// 获取记忆地点显示文本
const getMemoryPlaceDisplay = (row) => {
  if (!row.memoryPlace && !row.memoryPlaceDetail) return '-'
  let placeText = ''
  
  // 处理记忆地点
  if (row.memoryPlace) {
    if (Array.isArray(row.memoryPlace)) {
      // 如果是数组，转换为中文
      placeText = row.memoryPlace
        .map(code => getAddressTextByCode(code))
        .filter(Boolean)
        .join('')
    } else if (typeof row.memoryPlace === 'string' && row.memoryPlace.includes(',')) {
      // 如果是逗号分隔的字符串，拆分后转换
      placeText = row.memoryPlace
        .split(',')
        .map(code => getAddressTextByCode(code.trim()))
        .filter(Boolean)
        .join('')
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
  if (!code) return ''

  // ✅ 关键：取 .value
  const options = formattedAddressOptions.value
  if (!options || !options.length) return ''

  const findLabel = (options, targetCode) => {
    for (const option of options) {
      if (option.value == targetCode) {
        return option.label
      }
      if (option.children?.length) {
        const found = findLabel(option.children, targetCode)
        if (found) return found
      }
    }
    return ''
  }

  return findLabel(options, code)
}

// 计算属性：获取记忆时段显示
const getTimePeriodDisplay = computed(() => {
  if (!props.rowData?.recordTime && !props.rowData?.recordEndTime) return '-'
  return `${props.rowData.recordTime || ''} - ${props.rowData.recordEndTime || ''}`
})
//-------------------------------------------------------------------------

//------------------联想操作------------------------
const associativeMemory = ref({
  row_id: "", //原始记忆id
  begin_time: "", //联想开始时间
  end_time: "", //联想结束时间
  contactType: "", //联想关系人类型
  contact: "", //联想关系人名称
  memoryPlace: "", //联想记忆地点
  memoryPlaceDetail: "", //联想记忆地点详情
  memoryPlaceShort: "", //联想地点简称
  rowMemoryType: "", //联想原始记忆类型
  rowMemoryContent: "", //联想原始记忆内容
  rowMemoryReason: "", //联想记忆原因
  rowMemoryAction: "", //联想记忆行为
  memoryOwner: "", //联想记忆人
  memorySource: "", //联想记忆来源
  memoryImages: "", //联想图片
  memoryNo: "", //联想记忆编号
  document: "", //联想文件
  workBusinessType: "", //工作业务类型
  workTechType: "", //工作技术类型
  workBusinessNode: "", //工作业务笔记
  workTechNode: "", //工作技术笔记
  //------------生活记忆--------------

}); //联想记忆，用于存储转换联想记忆参数

//-----------------------------------上传处理-----------------------------------
const headers = {
  token: useApp().authorization.token, // 从pinia中获取token，在进行文件上传的时候将token设置到请求头中
}
//-----------文档上传-------------
// 文件列表（如果需要保存到表单数据中）
const documentFileList = ref([]) //文件回显
const documentList = ref([]) //数据

// 文档预览处理函数 - 简化为直接下载
const handleDocumentPreview = file => {
  // 直接下载文件
  if (file.url) {
    const link = document.createElement('a')
    link.href = file.url
    // 总是从URL中提取文件名并清洗，确保下载时使用的是干净的文件名
    // 这样可以确保与回显时使用相同的清洗逻辑
    const fileNameFromUrl = file.url.substring(file.url.lastIndexOf('/') + 1)
    link.download = cleanFileName(fileNameFromUrl)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  }
}

// 上传成功处理
const handleDocumentSuccess = (response, file, fileList) => {
  // 如果后端返回成功（假设code为200表示成功）
  if (response.code === 200) {
    // 保存文件路径到列表
    const fileUrl = response.data // response.data 应该是文件的URL路径
    documentList.value.push(fileUrl)

    // 更新documentFileList以显示文件名（去除UUID部分）
    const cleanedName = cleanFileName(file.name)
    file.name = cleanedName

    // 如果需要，可以在这里触发事件给父组件
    // emit('upload-success', response.data)

    ElMessage.success(`文档上传成功，文件名：${cleanedName}`)
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
  // 记录文件大小以便后续使用
  //console.log('开始上传文件:', file.name, '大小:', formatFileSize(file.size))
  return true // 必须返回true才会继续上传
}
// 文档移除处理
const handleDocumentRemove = (file, fileList) => {
  // 从documentList中移除对应的URL
  const urlToRemove = file.url
  const index = documentList.value.findIndex(
      docUrl => docUrl === urlToRemove
  )

  if (index !== -1) {
    documentList.value.splice(index, 1)
    ElMessage.success('文档已移除')
  } else {
    // 尝试另一种比较方式，防止URL格式差异
    const altIndex = documentList.value.findIndex(
        docUrl =>
            docUrl.includes(urlToRemove.split('/').pop()) ||
            urlToRemove.includes(docUrl.split('/').pop())
    )
    if (altIndex !== -1) {
      documentList.value.splice(altIndex, 1)
      ElMessage.success('文档已移除')
    }
  }

  // 同时也要从documentFileList中移除（用于界面更新）
  const fileIndex = documentFileList.value.findIndex(f => f.url === urlToRemove)

  if (fileIndex !== -1) {
    documentFileList.value.splice(fileIndex, 1)
  }
}

//-------------辅助函数（针对文档上传）----------------
// 文件名清洗函数 - 支持大小写的32位十六进制UUID
const cleanFileName = fileName => {
  if (!fileName) return '未知文件'

  // 方法1: 移除32位十六进制UUID前缀（支持大小写）
  const hex32Pattern = /^[a-fA-F0-9]{32}/
  if (hex32Pattern.test(fileName)) {
    return fileName.substring(32)
  }

  // 方法2: 移除下划线分隔的UUID
  const underscoreIndex = fileName.indexOf('_')
  if (underscoreIndex > 0) {
    return fileName.substring(underscoreIndex + 1)
  }

  // 方法3: 移除8位数字时间戳前缀
  const timestampPattern = /^\d{8}(_)?/
  if (timestampPattern.test(fileName)) {
    const match = fileName.match(timestampPattern)
    if (match) {
      return fileName.substring(match[0].length)
    }
  }

  // 方法4: 移除32位UUID（带连字符格式）如 xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
  const uuidWithHyphenPattern = /^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}/
  if (uuidWithHyphenPattern.test(fileName)) {
    return fileName.substring(36)
  }

  // 方法5: 移除开头的任意32位十六进制字符（不区分大小写）
  const hex32AnyPattern = /^[a-fA-F0-9]{32}[._-]?/
  if (hex32AnyPattern.test(fileName)) {
    return fileName.replace(hex32AnyPattern, '')
  }

  return fileName
}

// 文件大小格式化函数
const formatFileSize = bytes => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// ----------------------------------- 提交处理 -------------------------------------------------
// 点击提交按钮触发
const submit = async () => {
  //数据校验
  if (associativeMemory.value.rowMemoryContent == undefined || associativeMemory.value.rowMemoryContent == ''){
    ElMessage.error('【记忆内容】不能为空')
  }

  //数据处理
  if (documentList.value != null && documentList.value.length > 0) {
    associativeMemory.value.document = documentList.value.join(',')
  } else {
    associativeMemory.value.document = null
  }

  //console.log(`开始对记忆ID: ${associativeMemory.value.row_id} 进行联想分析`)
  //提交数据
  const {code, message} = await MemoryAssociation(associativeMemory.value);
  //console.log(`开始对记忆ID2: ${associativeMemory.value} 进行联想分析`)
  if (code === 200){
    // 1️⃣ 通知父组件刷新
    emit('refresh')
    // 2️⃣ 关闭弹窗
    emit('update:visible', false)
    ElMessage.success(message);
  } else {
    ElMessage.error(message);
  }
}
</script>

<style scoped>
/* --------------------------------- 记忆联想对话框美化样式 ------------------------------------------- */

/* ================= 弹窗整体美化 ================= */
.memory-enhanced-dialog /deep/ .el-dialog {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

/* 渐变标题栏 */
.memory-enhanced-dialog /deep/ .el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 28px;
  border-bottom: none;
}

.memory-enhanced-dialog /deep/ .el-dialog__title {
  color: white;
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 1px;
}

/* 关闭按钮美化 */
.memory-enhanced-dialog /deep/ .el-dialog__headerbtn {
  top: 22px;
  right: 28px;
}

.memory-enhanced-dialog /deep/ .el-dialog__headerbtn .el-dialog__close {
  color: white;
  font-size: 22px;
  transition: all 0.3s ease;
}

.memory-enhanced-dialog /deep/ .el-dialog__headerbtn:hover .el-dialog__close {
  color: #ffd700;
  transform: rotate(90deg);
}

/* ================= 内容区域美化 ================= */
.dialog-body {
  padding: 30px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4edf9 100%);
  min-height: 300px;
}

/* 记忆卡片样式 */
.memory-card {
  background: white;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.08);
  margin-bottom: 24px;
  transition: all 0.3s ease;
}

.memory-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 35px rgba(0, 0, 0, 0.12);
}

/* 卡片头部 */
.card-header {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  padding: 20px 24px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  font-size: 24px;
}

.card-title {
  color: white;
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  flex: 1;
}

.header-badge {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

/* 卡片内容容器 - 添加滚动条 */
.card-content-wrapper {
  max-height: 400px;
  overflow-y: auto;
  padding: 0 24px 24px 24px;
}

/* 滚动条样式美化 */
.card-content-wrapper::-webkit-scrollbar {
  width: 8px;
}

.card-content-wrapper::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.card-content-wrapper::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.card-content-wrapper::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 卡片内容 */
.card-content {
  padding-top: 24px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

/* 长文本项目样式 */
.long-text-item {
  grid-column: 1 / -1;
}

/* 信息项美化 */
.info-item {
  display: flex;
  gap: 15px;
  padding: 16px;
  background: #f8f9ff;
  border-radius: 12px;
  border-left: 4px solid #4facfe;
  transition: all 0.3s ease;
}

.info-item:hover {
  background: #edf4ff;
  transform: translateX(5px);
}

.item-icon {
  font-size: 20px;
  align-self: flex-start;
}

.item-content {
  flex: 1;
}

.item-label {
  font-size: 13px;
  color: #666;
  font-weight: 500;
  margin-bottom: 6px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.item-value {
  font-size: 15px;
  color: #333;
  line-height: 1.5;
}

.content-text {
  background: white;
  padding: 14px;
  border-radius: 8px;
  border: 1px solid #e1e8f0;
  min-height: 60px;
  display: flex;
  align-items: flex-start;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
}

/* ================= 联想提示区域 ================= */
.association-tip {
  display: flex;
  gap: 15px;
  padding: 20px;
  background: linear-gradient(135deg, #fff5e6 0%, #ffe6cc 100%);
  border-radius: 12px;
  border: 1px solid #ffd7a3;
  align-items: center;
}

.tip-icon {
  font-size: 24px;
}

.tip-content {
  flex: 1;
}

.tip-title {
  margin: 0 0 6px 0;
  color: #e67a00;
  font-size: 16px;
  font-weight: 600;
}

.tip-desc {
  margin: 0;
  color: #b35c00;
  font-size: 14px;
  line-height: 1.4;
}

/* ================= 底部按钮区域美化 ================= */
.dialog-footer.enhanced {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  padding: 20px 30px;
  border-top: 1px solid #eef2f7;
  background: white;
}

/* 取消按钮 */
.cancel-btn {
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  color: #606266;
  padding: 12px 24px;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  background: #e4e7ed;
  border-color: #c0c4cc;
  transform: translateY(-2px);
}

/* 提交按钮 */
.submit-btn {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  border: none;
  color: white;
  padding: 12px 28px;
  border-radius: 10px;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(79, 172, 254, 0.3);
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(79, 172, 254, 0.4);
}

/* 按钮图标 */
.btn-icon {
  margin-right: 6px;
  font-size: 16px;
}

/* ================= 响应式设计 ================= */
@media (max-width: 768px) {
  .memory-enhanced-dialog /deep/ .el-dialog {
    width: 95% !important;
    margin: 20px auto;
  }
  
  .dialog-body {
    padding: 20px;
  }
  
  .card-header {
    flex-direction: column;
    text-align: center;
    gap: 10px;
  }
  
  .info-item {
    flex-direction: column;
    gap: 10px;
  }
  
  .dialog-footer.enhanced {
    flex-direction: column;
  }
  
  .cancel-btn, .submit-btn {
    width: 100%;
  }
}

/* ================= 动画效果 ================= */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.memory-card {
  animation: fadeInUp 0.6s ease-out;
}

.association-tip {
  animation: fadeInUp 0.8s ease-out;
}

.info-item {
  animation: fadeInUp 1s ease-out;
}

</style>