<template>
  <div class="page-container">
    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="main-tabs">
      <el-tab-pane label="股票分析" name="analysis">
        <TabStockAnalysis v-if="activeTab === 'analysis'" />
      </el-tab-pane>
      <el-tab-pane label="基本面选股" name="fundamental">
        <TabFundamentalScreening v-if="activeTab === 'fundamental'" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import TabStockAnalysis from './stockAssetManagementTabs/tabStockAnalysis.vue'
import TabFundamentalScreening from './stockAssetManagementTabs/tabFundamentalScreening.vue'

const activeTab = ref('analysis')
</script>

<style scoped>
/* 页面容器 */
.page-container {
  position: relative;
  width: 100%;
  min-height: 100vh;
  margin: 0;
  padding: 0;
  overflow: auto;
}

/* 标签页样式 */
.main-tabs {
  margin: 0 15px;
}

:deep(.main-tabs .el-tabs__header) {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 4px 4px 0 0;
  padding: 0 15px;
  margin-bottom: 0;
}

:deep(.main-tabs .el-tabs__item) {
  font-size: 15px;
  font-weight: 600;
  height: 44px;
  line-height: 44px;
}

:deep(.main-tabs .el-tabs__content) {
  padding: 0;
}
</style>
