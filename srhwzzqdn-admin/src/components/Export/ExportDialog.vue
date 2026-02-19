<template>
  <el-dialog
      v-model="dialogVisible"
      title="导出数据"
      width="500px"
      class="export-dialog"
      :close-on-click-modal="false"
      @closed="handleClosed"
  >
    <div class="export-dialog-content">
      <el-form label-width="100px">
        <el-form-item label="导出范围">
          <el-radio-group v-model="localExportScope">
            <el-radio label="current">
              导出当前页 ({{ currentCount }} 条)
            </el-radio>
            <el-radio label="all">导出全部数据 ({{ totalCount }} 条)</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="文件名称">
          <el-input
              v-model="localExportFileName"
              placeholder="请输入导出文件名称"
              clearable
          />
        </el-form-item>

        <el-form-item label="包含列">
          <el-checkbox-group v-model="localSelectedColumns">
            <el-checkbox
                v-for="column in availableColumns"
                :key="column.key"
                :label="column.key"
            >
              {{ column.label }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
            type="primary"
            @click="handleConfirm"
            :loading="exportLoading"
        >
          {{ exportLoading ? '导出中...' : '开始导出' }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  modelValue: Boolean,
  exportScope: String,
  exportFileName: String,
  exportLoading: Boolean,
  selectedColumns: Array,
  availableColumns: Array,
  currentCount: {
    type: Number,
    default: 0
  },
  totalCount: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits([
  'update:modelValue',
  'update:exportScope',
  'update:exportFileName',
  'update:selectedColumns',
  'confirm',
  'closed'
])

// 对话框可见性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 本地导出范围
const localExportScope = computed({
  get: () => props.exportScope,
  set: (val) => emit('update:exportScope', val)
})

// 本地文件名
const localExportFileName = computed({
  get: () => props.exportFileName,
  set: (val) => emit('update:exportFileName', val)
})

// 本地选中列
const localSelectedColumns = computed({
  get: () => props.selectedColumns,
  set: (val) => emit('update:selectedColumns', val)
})

const handleConfirm = () => {
  emit('confirm')
}

const handleClosed = () => {
  emit('closed')
}
</script>

<style scoped>
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

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 20px;
  padding: 16px 20px;
  border-top: 1px solid #e8e8e8;
  background: #fafafa;
}

.dialog-footer .el-button {
  min-width: 100px;
  padding: 10px 24px;
}
</style>