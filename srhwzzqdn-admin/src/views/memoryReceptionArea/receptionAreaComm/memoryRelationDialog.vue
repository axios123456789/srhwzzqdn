<template>
  <el-dialog
    v-model="dialogVisible"
    title="记忆联想"
    width="600px"
    @close="handleClose"
  >
    <div>
      <h3>记忆联想功能</h3>
      <p>这里是记忆联想的模态窗口内容</p>
      <p>当前行数据ID: {{ rowData?.id || '无' }}</p>
      <p>当前行名称: {{ rowData?.name || '无' }}</p>
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  visible: Boolean,
  rowData: Object,
})

const emit = defineEmits(['update:visible'])

/**
 * 用代理变量，避免直接操作 props.visible
 */
const dialogVisible = computed({
  get: () => props.visible,
  set: val => emit('update:visible', val),
})

const handleClose = () => {
  emit('update:visible', false)
}
</script>

<style scoped>
.memory-relation-dialog {
  border-radius: 12px;
  overflow: hidden;
}

.memory-relation-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
  margin: 0;
  padding: 16px 20px;
}

.memory-relation-dialog :deep(.el-dialog__title) {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.dialog-content {
  padding: 20px;
}

.info-card,
.content-card,
.image-card {
  border-radius: 8px;
}

.card-header {
  font-weight: 600;
  color: #303133;
  font-size: 16px;
}

.content-section h4 {
  margin: 0 0 10px 0;
  color: #409eff;
  font-size: 14px;
  font-weight: 600;
}

.content-text {
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  border-left: 3px solid #409eff;
  min-height: 60px;
  line-height: 1.6;
}

.image-gallery {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.memory-image {
  width: 120px;
  height: 120px;
  border-radius: 6px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.memory-image:hover {
  transform: scale(1.05);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #ebeef5;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .dialog-content {
    padding: 15px;
  }

  .memory-image {
    width: 100px;
    height: 100px;
  }
}
</style>
