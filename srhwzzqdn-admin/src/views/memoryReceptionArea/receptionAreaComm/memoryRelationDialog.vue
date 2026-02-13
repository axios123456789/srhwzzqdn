<template>
  <el-dialog
    v-model="dialogVisible"
    title="✨ 记忆联想空间"
    width="1200px"
    class="memory-enhanced-dialog"
    :close-on-click-modal="false"
    align-center
    draggable
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
      
      <!-- 联想提示区域 -->
      <div class="association-tip">
        <div class="tip-icon">💡</div>
        <div class="tip-content">
          <h4 class="tip-title">智能联想提示</h4>
          <p class="tip-desc">当前记忆内容为基本记忆，您可以根据基础信息关联生活，工作，学习，娱乐等记忆！！！</p>
        </div>
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
import {computed, onMounted, ref} from 'vue'
import {GetAdministrative, GetKeyAndValueByType} from "@/api/sysDict";

// ------------------------------- 基础与父组件建立关系 ------------------------------------------
/* 接收父组件参数 */
const props = defineProps({
  visible: Boolean,
  rowData: Object
})

/* 向父组件发送事件 */
const emit = defineEmits(['update:visible'])

/* 代理 visible，不能直接改 props */
const dialogVisible = computed({
  get() {
    return props.visible
  },
  set(val) {
    emit('update:visible', val)
  }
})

//钩子函数
onMounted(() => {
  getContactTypeItem();
  getRowMemoryTypeItem();
  getMemorySourceItem();
  getAssociativeStatusItem();
  getFormattedAddressOptions();
})

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
// 点击提交按钮触发
const submit = () => {
  console.log('rowData:', getMemoryPlaceDisplay(props.rowData), props.rowData.memoryPlace)
  if (!props.rowData?.id) {
    alert('请选择有效的记忆记录')
    return
  }
  alert(`开始对记忆ID: ${props.rowData.id} 进行联想分析`)
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