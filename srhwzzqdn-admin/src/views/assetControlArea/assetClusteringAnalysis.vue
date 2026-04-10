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
  padding: 0;
  margin: 0;
  min-height: calc(100vh - 80px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* 自定义标签页样式 */
:deep(.el-tabs__header) {
  margin-bottom: 0 !important;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%) !important;
  border-bottom: none !important;
  padding: 15px 30px 0 !important;
}

:deep(.el-tabs__nav-wrap) {
  display: flex !important;
  justify-content: flex-start !important;
  margin-bottom: 0 !important;
}

:deep(.el-tabs__nav-wrap)::after {
  display: none !important;
  height: 0 !important;
}

:deep(.el-tabs__nav-scroll) {
  padding: 0 !important;
  overflow: visible !important;
}

:deep(.el-tabs__nav) {
  border: none !important;
  display: flex !important;
  align-items: center !important;
  float: none !important;
}

:deep(.el-tabs__item) {
  font-size: 16px !important;
  font-weight: 500 !important;
  padding: 12px 24px !important;
  height: auto !important;
  line-height: 1.5 !important;
  color: #606266 !important;
  border: none !important;
  position: relative !important;
  transition: all 0.3s ease !important;
  margin-right: 8px !important;
  border-radius: 8px 8px 0 0 !important;
  background: transparent !important;
  text-align: center !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  float: none !important;
}

:deep(.el-tabs__item:hover) {
  color: #409eff !important;
  background: rgba(64, 158, 255, 0.05) !important;
}

:deep(.el-tabs__item.is-active) {
  color: #409eff !important;
  font-weight: 600 !important;
  background: white !important;
  box-shadow: 0 -2px 8px rgba(64, 158, 255, 0.1) !important;
}

:deep(.el-tabs__item.is-active)::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  border-radius: 0 0 3px 3px;
}

:deep(.el-tabs__active-bar) {
  display: none !important;
}

:deep(.el-tabs__content) {
  padding: 20px 30px !important;
}

/* 标签页内容区域 */
:deep(.el-tab-pane) {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
