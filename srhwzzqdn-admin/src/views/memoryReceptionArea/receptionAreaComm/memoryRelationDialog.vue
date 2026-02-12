<template>
  <el-dialog
      v-model="dialogVisible"
      title="记忆联想"
      width="700px"
  >
    <div>
      <p>当前行 ID：{{ rowData?.id }}</p>
      <p>当前行名称：{{ rowData?.name }}</p>
    </div>

    <template #footer>
      <el-button @click="dialogVisible = false">关闭</el-button>
      <el-button
          type="primary"
          @click="submit"
          style="
              background: #409EFF;
              border-color: #409EFF;
            "
      >
        提交
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'

//-------------------------------基础与父组件建立关系------------------------------------------
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

//-----------------------------------逻辑操作-------------------------------------------------
//点击提交按钮触发
const submit = () => {
  alert(props.rowData.id);
}
</script>

<style scoped>
/*---------------------------------模态窗口样式优化------------------------------------------- */

</style>

