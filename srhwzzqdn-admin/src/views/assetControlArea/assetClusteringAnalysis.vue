<template>
  <div class="asset-clustering-analysis">
    <!-- 页面标题 -->
    <h1 style="margin-top: 10px; font-family: 方正姚体; color: black">
      <el-icon><PieChart /></el-icon>
      资产聚类分析
    </h1>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="analysis-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="资产台账分析" name="ledger">
        <AssetLedgerAnalysis v-if="activeTab === 'ledger'" />
      </el-tab-pane>
      <el-tab-pane label="资产记账分析" name="accounting">
        <AssetAccountingAnalysis v-if="activeTab === 'accounting'" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { PieChart } from '@element-plus/icons-vue'
import AssetLedgerAnalysis from './assetClusteringAnalysisTabs/assetLedgerAnalysis.vue'
import AssetAccountingAnalysis from './assetClusteringAnalysisTabs/assetAccountingAnalysis.vue'

// 当前激活的标签页
const activeTab = ref('ledger')

// 处理标签页切换
const handleTabChange = (tabName) => {
  console.log('切换到标签页:', tabName)
}
</script>

<style scoped>
.asset-clustering-analysis {
  padding: 20px;
}

.analysis-tabs {
  margin-top: 20px;
}

/* 自定义标签页样式 */
:deep(.el-tabs__header) {
  margin-bottom: 20px;
}

:deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 500;
}

:deep(.el-tabs__item.is-active) {
  color: #409eff;
}
</style>
