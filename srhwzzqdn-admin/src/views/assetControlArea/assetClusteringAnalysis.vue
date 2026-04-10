<template>
  <div class="asset-clustering-analysis">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>
        <el-icon><PieChart /></el-icon>
        资产聚类分析
      </h1>
    </div>

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
  padding: 0;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 30px;
  margin-bottom: 0;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.page-header h1 {
  margin: 0;
  font-family: 方正姚体, sans-serif;
  color: white;
  font-size: 24px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-header h1 .el-icon {
  font-size: 28px;
}

.analysis-tabs {
  background: white;
  padding: 20px 30px;
  margin: 0;
  min-height: calc(100vh - 80px);
}

/* 自定义标签页样式 */
:deep(.el-tabs__header) {
  margin-bottom: 20px;
  border-bottom: 2px solid #e4e7ed;
}

:deep(.el-tabs__nav-wrap)::after {
  display: none;
}

:deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 500;
  padding: 0 20px;
  height: 45px;
  line-height: 45px;
}

:deep(.el-tabs__item.is-active) {
  color: #409eff;
  font-weight: 600;
}

:deep(.el-tabs__active-bar) {
  height: 3px;
}
</style>
