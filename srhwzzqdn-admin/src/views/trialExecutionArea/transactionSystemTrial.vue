<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>
        <el-icon><Goods /></el-icon>
        交易系统试验
      </h1>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="main-tabs">
      <el-tab-pane label="交易规则" name="rule">
        <TabRule v-if="activeTab === 'rule'" />
      </el-tab-pane>
      <el-tab-pane label="交易试验记录" name="trial">
        <TabTrial v-if="activeTab === 'trial'" />
      </el-tab-pane>
      <el-tab-pane label="预测模拟" name="prediction">
        <TabPrediction v-if="activeTab === 'prediction'" />
      </el-tab-pane>
      <el-tab-pane label="预测报表" name="report">
        <TabReport v-if="activeTab === 'report'" />
      </el-tab-pane>
      <el-tab-pane label="每日复盘" name="dailyReview">
        <TabDailyReview v-if="activeTab === 'dailyReview'" />
      </el-tab-pane>
      <el-tab-pane label="交易记录" name="tradeRecord">
        <TabTradeRecord v-if="activeTab === 'tradeRecord'" />
      </el-tab-pane>
      <el-tab-pane label="复盘分析" name="reviewAnalysis">
        <TabReviewAnalysis v-if="activeTab === 'reviewAnalysis'" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Goods } from '@element-plus/icons-vue'
import TabRule from './transactionSystemTrialTabs/tabRule.vue'
import TabTrial from './transactionSystemTrialTabs/tabTrial.vue'
import TabPrediction from './transactionSystemTrialTabs/tabPrediction.vue'
import TabReport from './transactionSystemTrialTabs/tabReport.vue'
import TabDailyReview from './transactionSystemTrialTabs/tabDailyReview.vue'
import TabTradeRecord from './transactionSystemTrialTabs/tabTradeRecord.vue'
import TabReviewAnalysis from './transactionSystemTrialTabs/tabReviewAnalysis.vue'

const activeTab = ref('rule')
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

.page-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('src/assets/memory/background.gif');
  background-size: cover;
  background-attachment: fixed;
  opacity: 0.5;
  z-index: 0;
}

.page-container > * {
  position: relative;
  z-index: 1;
}

/* 页面标题头部 */
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
