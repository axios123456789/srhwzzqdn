<template>
  <div class="stock-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>
        <el-icon><Histogram /></el-icon>
        股票资产管理
      </h1>
      <div class="fetch-area">
        <el-input
          v-model="stockCodeInput"
          placeholder="请输入股票代码（如：600519、000001）"
          style="width: 280px;"
          clearable
          @keyup.enter="fetchStockData"
        />
        <el-button
          type="primary"
          :loading="fetchLoading"
          @click="fetchStockData"
        >
          <el-icon><Search /></el-icon>
          数据获取
        </el-button>
        <el-button
          type="success"
          :loading="refreshLoading"
          @click="refreshAllData"
        >
          <el-icon><Refresh /></el-icon>
          实时数据
        </el-button>
      </div>
    </div>

    <!-- 条件查询区 -->
    <div class="query-section">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="股票名称">
          <el-input
            v-model="queryParams.stockName"
            placeholder="请输入"
            clearable
          />
        </el-form-item>
        <el-form-item label="股票代码">
          <el-input
            v-model="queryParams.stockCode"
            placeholder="请输入"
            clearable
          />
        </el-form-item>
        <el-form-item label="行业">
          <el-input
            v-model="queryParams.industry"
            placeholder="请输入"
            clearable
          />
        </el-form-item>
        <el-form-item label="市场">
          <el-select
            v-model="queryParams.market"
            placeholder="全部"
            clearable
            style="width: 120px;"
          >
            <el-option label="沪市" :value="1" />
            <el-option label="深市" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 股票列表 -->
    <div class="list-section">
      <el-table
        :data="stockList"
        border
        stripe
        highlight-current-row
        @row-click="handleRowClick"
        style="width: 100%;"
      >
        <el-table-column
          label="股票代码"
          prop="stockCode"
          width="100"
          align="center"
        />
        <el-table-column
          label="股票名称"
          prop="stockName"
          width="120"
          align="center"
        />
        <el-table-column label="市场" width="60" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.market === 1 ? 'danger' : 'primary'"
              size="small"
            >
              {{ row.market === 1 ? '沪' : '深' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最新价" width="100" align="right">
          <template #default="{ row }">
            <span :class="row.changePct >= 0 ? 'price-up' : 'price-down'">
              {{ formatPrice(row.lastPrice) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="涨跌幅" width="100" align="right">
          <template #default="{ row }">
            <span :class="row.changePct >= 0 ? 'price-up' : 'price-down'">
              {{ row.changePct >= 0 ? '+' : '' }}{{ formatPct(row.changePct) }}%
            </span>
          </template>
        </el-table-column>
        <el-table-column
          label="换手率"
          prop="turnoverRate"
          width="90"
          align="right"
        >
          <template #default="{ row }">
            {{ formatPct(row.turnoverRate) }}%
          </template>
        </el-table-column>
        <el-table-column label="总市值(亿)" width="120" align="right">
          <template #default="{ row }">
            {{ formatBigNum(row.totalMarketCap) }}
          </template>
        </el-table-column>
        <el-table-column label="PE(TTM)" width="90" align="right">
          <template #default="{ row }">{{ formatPrice(row.peTtm) }}</template>
        </el-table-column>
        <el-table-column label="PB" width="80" align="right">
          <template #default="{ row }">{{ formatPrice(row.pbRatio) }}</template>
        </el-table-column>
        <el-table-column
          label="行业"
          prop="industry"
          width="120"
          align="center"
          show-overflow-tooltip
        />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              link
              :loading="rowRefreshing === row.stockCode"
              @click.stop="handleRowRefresh(row)"
            >
              <el-icon v-if="rowRefreshing !== row.stockCode"><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button
              type="danger"
              size="small"
              link
              @click.stop="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-area">
        <el-pagination
          v-model:current-page="page.current"
          v-model:page-size="page.limit"
          :total="page.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </div>

    <!-- 详细数据展示区 -->
    <div v-if="selectedStock" class="detail-section">
      <!-- 基本面数据 + 实时行情 -->
      <div class="info-grid">
        <!-- 公司名片 -->
        <div class="info-card name-card">
          <div class="card-title">公司名片</div>
          <div class="name-row">
            <span class="stock-name">{{ selectedStock.stockName }}</span>
            <el-tag
              :type="selectedStock.market === 1 ? 'danger' : 'primary'"
              size="small"
            >
              {{ selectedStock.market === 1 ? '沪' : '深'
              }}{{ selectedStock.stockCode }}
            </el-tag>
          </div>
          <div class="industry-tag" v-if="selectedStock.industry">
            {{ selectedStock.industry }}
          </div>
          <div class="price-display">
            <span
              :class="
                selectedStock.changePct >= 0 ? 'price-up big' : 'price-down big'
              "
            >
              {{ formatPrice(selectedStock.lastPrice) }}
            </span>
            <span
              :class="selectedStock.changePct >= 0 ? 'price-up' : 'price-down'"
            >
              {{ selectedStock.changePct >= 0 ? '+' : ''
              }}{{ formatPct(selectedStock.changePct) }}% ({{
                selectedStock.changePct >= 0 ? '+' : ''
              }}{{ formatPrice(selectedStock.changeAmount) }})
            </span>
          </div>
          <div class="list-date" v-if="selectedStock.listDate">
            上市日期：{{ selectedStock.listDate }}
          </div>
        </div>

        <!-- 估值指标 -->
        <div class="info-card">
          <div class="card-title">估值指标</div>
          <div class="metric-grid">
            <div class="metric-item">
              <span class="label">PE(静)</span>
              <span class="value">
                {{ formatPrice(selectedStock.peStatic) }}
              </span>
            </div>
            <div class="metric-item">
              <span class="label">PE(动)</span>
              <span class="value">
                {{ formatPrice(selectedStock.peDynamic) }}
              </span>
            </div>
            <div class="metric-item">
              <span class="label">PE(TTM)</span>
              <span class="value">{{ formatPrice(selectedStock.peTtm) }}</span>
            </div>
            <div class="metric-item">
              <span class="label">PB</span>
              <span class="value">
                {{ formatPrice(selectedStock.pbRatio) }}
              </span>
            </div>
            <div class="metric-item">
              <span class="label">PS</span>
              <span class="value">
                {{ formatPrice(selectedStock.psRatio) }}
              </span>
            </div>
            <div class="metric-item">
              <span class="label">股息率</span>
              <span class="value">
                {{ formatPct(selectedStock.dividendYield) }}%
              </span>
            </div>
          </div>
        </div>

        <!-- 实时行情 -->
        <div class="info-card">
          <div class="card-title">实时行情</div>
          <div class="metric-grid">
            <div class="metric-item">
              <span class="label">换手率</span>
              <span class="value">
                {{ formatPct(selectedStock.turnoverRate) }}%
              </span>
            </div>
            <div class="metric-item">
              <span class="label">量比</span>
              <span class="value">
                {{ formatPrice(selectedStock.volumeRatio) }}
              </span>
            </div>
            <div class="metric-item">
              <span class="label">振幅</span>
              <span class="value">
                {{ formatPct(selectedStock.amplitude) }}%
              </span>
            </div>
            <div class="metric-item">
              <span class="label">成交量</span>
              <span class="value">
                {{ formatVolume(selectedStock.volume) }}
              </span>
            </div>
            <div class="metric-item">
              <span class="label">成交额</span>
              <span class="value">
                {{ formatBigNum(selectedStock.turnover) }}亿
              </span>
            </div>
          </div>
        </div>

        <!-- 市值规模 -->
        <div class="info-card">
          <div class="card-title">市值规模</div>
          <div class="metric-grid">
            <div class="metric-item">
              <span class="label">总市值</span>
              <span class="value">
                {{ formatBigNum(selectedStock.totalMarketCap) }}亿
              </span>
            </div>
            <div class="metric-item">
              <span class="label">流通市值</span>
              <span class="value">
                {{ formatBigNum(selectedStock.circMarketCap) }}亿
              </span>
            </div>
            <div class="metric-item">
              <span class="label">总股本</span>
              <span class="value">
                {{ formatBigNum(selectedStock.totalShares) }}万
              </span>
            </div>
            <div class="metric-item">
              <span class="label">流通股本</span>
              <span class="value">
                {{ formatBigNum(selectedStock.circShares) }}万
              </span>
            </div>
          </div>
        </div>

        <!-- 公司信息 -->
        <div
          class="info-card full-width"
          v-if="selectedStock.companyDesc || selectedStock.mainBusiness"
        >
          <div class="card-title">公司信息</div>
          <div class="company-info">
            <div class="info-row" v-if="selectedStock.legalRep">
              <span class="label">法人代表：</span>
              {{ selectedStock.legalRep }}
            </div>
            <div class="info-row" v-if="selectedStock.generalManager">
              <span class="label">总经理：</span>
              {{ selectedStock.generalManager }}
            </div>
            <div class="info-row" v-if="selectedStock.industry">
              <span class="label">所属行业：</span>
              {{ selectedStock.industry }}
            </div>
            <div class="info-row" v-if="selectedStock.companyDesc">
              <span class="label">公司简介：</span>
              {{ selectedStock.companyDesc }}
            </div>
            <div class="info-row" v-if="selectedStock.mainBusiness">
              <span class="label">主营业务：</span>
              {{ selectedStock.mainBusiness }}
            </div>
          </div>
        </div>
      </div>

      <!-- 分析页签 -->
      <el-tabs
        v-model="activeTab"
        class="detail-tabs"
        @tab-change="handleTabChange"
      >
        <!-- K线图 -->
        <el-tab-pane label="K线图" name="kline">
          <div class="chart-header">
            <span class="chart-title">K线图（滚轮/拖动缩放，通达信式）</span>
            <el-radio-group
              v-model="klineType"
              size="small"
              @change="loadKlineData"
            >
              <el-radio-button :label="1">日K</el-radio-button>
              <el-radio-button :label="2">周K</el-radio-button>
              <el-radio-button :label="3">月K</el-radio-button>
            </el-radio-group>
          </div>
          <div ref="klineChartRef" class="chart-container kline-height"></div>
        </el-tab-pane>

        <!-- 基本面 -->
        <el-tab-pane label="基本面" name="finance">
          <el-table
            v-if="financeData.length"
            :data="financeData"
            border
            stripe
            size="small"
            max-height="480"
          >
            <el-table-column
              prop="reportDate"
              label="报告期"
              width="100"
              fixed="left"
              align="center"
            />
            <el-table-column label="营收(亿)" width="95" align="right">
              <template #default="{ row }">
                {{ formatBigNum(row.revenue) }}
              </template>
            </el-table-column>
            <el-table-column label="营收同比" width="90" align="right">
              <template #default="{ row }">
                <span :class="pctClass(row.revenueYoy)">
                  {{ formatPct(row.revenueYoy) }}%
                </span>
              </template>
            </el-table-column>
            <el-table-column label="归母净利(亿)" width="110" align="right">
              <template #default="{ row }">
                {{ formatBigNum(row.netProfit) }}
              </template>
            </el-table-column>
            <el-table-column label="净利同比" width="90" align="right">
              <template #default="{ row }">
                <span :class="pctClass(row.netProfitYoy)">
                  {{ formatPct(row.netProfitYoy) }}%
                </span>
              </template>
            </el-table-column>
            <el-table-column label="扣非净利(亿)" width="110" align="right">
              <template #default="{ row }">
                {{ formatBigNum(row.deductNetProfit) }}
              </template>
            </el-table-column>
            <el-table-column label="EPS(元)" width="85" align="right">
              <template #default="{ row }">{{ formatPrice(row.eps) }}</template>
            </el-table-column>
            <el-table-column label="BPS(元)" width="85" align="right">
              <template #default="{ row }">{{ formatPrice(row.bps) }}</template>
            </el-table-column>
            <el-table-column label="ROE(%)" width="80" align="right">
              <template #default="{ row }">{{ formatPct(row.roe) }}</template>
            </el-table-column>
            <el-table-column label="毛利率(%)" width="90" align="right">
              <template #default="{ row }">
                {{ formatPct(row.grossMargin) }}
              </template>
            </el-table-column>
            <el-table-column label="净利率(%)" width="90" align="right">
              <template #default="{ row }">
                {{ formatPct(row.netMargin) }}
              </template>
            </el-table-column>
            <el-table-column label="资产负债率(%)" width="115" align="right">
              <template #default="{ row }">
                {{ formatPct(row.debtRatio) }}
              </template>
            </el-table-column>
            <el-table-column label="每股现金流(元)" width="120" align="right">
              <template #default="{ row }">
                {{ formatPrice(row.cashflowPerShare) }}
              </template>
            </el-table-column>
            <el-table-column label="流动比率" width="85" align="right">
              <template #default="{ row }">
                {{ formatPrice(row.currentRatio) }}
              </template>
            </el-table-column>
            <el-table-column label="速动比率" width="85" align="right">
              <template #default="{ row }">
                {{ formatPrice(row.quickRatio) }}
              </template>
            </el-table-column>
            <el-table-column label="ROIC(%)" width="85" align="right">
              <template #default="{ row }">{{ formatPct(row.roic) }}</template>
            </el-table-column>
          </el-table>
          <el-empty
            v-else
            description="暂无财务数据，请点击【实时数据】刷新补全"
          />

          <!-- 股东人数/筹码结构 -->
          <template v-if="holderData.length">
            <div class="chart-header holder-title">
              <span class="chart-title">
                股东人数 / 筹码结构（近{{ holderData.length }}期）
              </span>
              <el-tag size="small" type="warning" effect="plain">
                户数下降=筹码集中(主力吸筹)；户数上升=筹码分散(散户接盘)
              </el-tag>
            </div>
            <el-table :data="holderData" border stripe size="small" max-height="360">
              <el-table-column prop="endDate" label="截止日期" width="100" fixed="left" align="center" />
              <el-table-column label="股东户数" width="110" align="right">
                <template #default="{ row }">
                  {{ formatBigNum(row.holderNum) }}
                </template>
              </el-table-column>
              <el-table-column label="较上期变化" width="105" align="right">
                <template #default="{ row, $index }">
                  {{ holderChange(row, $index) }}
                </template>
              </el-table-column>
              <el-table-column label="变化率" width="90" align="right">
                <template #default="{ row }">
                  <span :class="pctClass(row.holderNumRatio)">
                    {{ formatPct(row.holderNumRatio) }}%
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="筹码信号" width="110" align="center">
                <template #default="{ row }">
                  <el-tag v-if="Number(row.holderNumRatio) < 0" size="small" type="danger" effect="plain">
                    集中·吸筹
                  </el-tag>
                  <el-tag v-else-if="Number(row.holderNumRatio) > 0" size="small" type="success" effect="plain">
                    分散·接盘
                  </el-tag>
                  <el-tag v-else size="small" type="info" effect="plain">持平</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="户均持股市值(万)" width="135" align="right">
                <template #default="{ row }">
                  {{ row.avgMarketCap ? formatBigNum(Number(row.avgMarketCap) / 10000) : '-' }}
                </template>
              </el-table-column>
              <el-table-column label="户均持股数(股)" width="125" align="right">
                <template #default="{ row }">
                  {{ formatBigNum(row.avgHoldNum) }}
                </template>
              </el-table-column>
              <el-table-column prop="noticeDate" label="公告日期" width="100" align="center" />
            </el-table>
          </template>
        </el-tab-pane>

        <!-- 资金面 -->
        <el-tab-pane label="资金面" name="flow">
          <template v-if="flowRaw.length">
            <div class="flow-analysis">
              <div class="flow-summary">
                <div class="metric-item">
                  <span class="label">今日主力净流入(万)</span>
                  <span class="value" :class="pctClass(flowAnalysis.today)">
                    {{ formatBigNum(flowAnalysis.today) }}
                  </span>
                </div>
                <div class="metric-item">
                  <span class="label">近5日主力(万)</span>
                  <span class="value" :class="pctClass(flowAnalysis.days5)">
                    {{ formatBigNum(flowAnalysis.days5) }}
                  </span>
                </div>
                <div class="metric-item">
                  <span class="label">近10日主力(万)</span>
                  <span class="value" :class="pctClass(flowAnalysis.days10)">
                    {{ formatBigNum(flowAnalysis.days10) }}
                  </span>
                </div>
                <div class="metric-item">
                  <span class="label">连续流入/流出</span>
                  <span class="value">{{ flowAnalysis.streakText }}</span>
                </div>
              </div>
              <div class="flow-conclusion">
                <el-tag :type="flowAnalysis.tagType" effect="dark" size="large">
                  {{ flowAnalysis.conclusion }}
                </el-tag>
              </div>
            </div>
            <div class="chart-header">
              <span class="chart-title">资金流向（近30日，单位：万元）</span>
            </div>
            <div
              ref="flowChartRef"
              class="chart-container"
              style="height: 320px;"
            ></div>
          </template>
          <el-empty
            v-else
            description="暂无资金流向数据，请点击【实时数据】刷新补全"
          />
        </el-tab-pane>

        <!-- 消息面 -->
        <el-tab-pane label="消息面" name="news">
          <div class="chart-header">
            <span class="chart-title">
              最新消息（实时抓取，历史消息按时间倒序排列）
            </span>
            <el-button
              type="primary"
              size="small"
              :loading="newsLoading"
              @click="refreshNews"
            >
              <el-icon><Refresh /></el-icon>
              刷新消息
            </el-button>
          </div>
          <template v-if="newsData.length">
            <!-- 最新消息区：取最近5条 -->
            <div class="news-latest">
              <div
                v-for="(n, i) in newsData.slice(0, 5)"
                :key="'latest-' + n.id"
                class="news-item latest"
              >
                <el-tag
                  :type="n.newsType === 2 ? 'warning' : 'danger'"
                  size="small"
                  effect="dark"
                >
                  {{ n.newsType === 2 ? '公告' : '最新' }}
                </el-tag>
                <a
                  :href="n.newsUrl"
                  target="_blank"
                  rel="noopener"
                  class="news-title"
                >
                  {{ n.title }}
                </a>
                <span class="news-meta">
                  {{ n.source }} · {{ n.publishTime }}
                </span>
              </div>
            </div>
            <!-- 历史消息时间线 -->
            <div class="news-history" v-if="newsData.length > 5">
              <div class="news-history-title">历史消息</div>
              <el-timeline>
                <el-timeline-item
                  v-for="n in newsData.slice(5)"
                  :key="n.id"
                  :timestamp="`${n.publishTime} · ${n.source}`"
                  :type="n.newsType === 2 ? 'warning' : 'primary'"
                >
                  <a
                    :href="n.newsUrl"
                    target="_blank"
                    rel="noopener"
                    class="news-title"
                  >
                    {{ n.title }}
                  </a>
                  <div v-if="n.summary" class="news-summary">
                    {{ n.summary }}
                  </div>
                </el-timeline-item>
              </el-timeline>
            </div>
          </template>
          <el-empty
            v-else
            :description="
              newsLoading
                ? '正在抓取最新消息...'
                : '暂无消息数据，点击【刷新消息】实时抓取'
            "
          />
        </el-tab-pane>

        <!-- 技术面 -->
        <el-tab-pane label="技术面" name="tech">
          <template v-if="techView">
            <div class="signal-area">
              <el-tag
                v-for="(s, i) in techView.signals"
                :key="i"
                :type="s.type"
                effect="plain"
              >
                {{ s.text }}
              </el-tag>
            </div>
            <div class="metric-grid tech-grid">
              <div class="metric-item">
                <span class="label">最新价</span>
                <span class="value">{{ formatPrice(techView.last) }}</span>
              </div>
              <div class="metric-item">
                <span class="label">MA5</span>
                <span class="value">{{ techView.ma5 ?? '-' }}</span>
              </div>
              <div class="metric-item">
                <span class="label">MA10</span>
                <span class="value">{{ techView.ma10 ?? '-' }}</span>
              </div>
              <div class="metric-item">
                <span class="label">MA20</span>
                <span class="value">{{ techView.ma20 ?? '-' }}</span>
              </div>
              <div class="metric-item">
                <span class="label">MA60</span>
                <span class="value">{{ techView.ma60 ?? '-' }}</span>
              </div>
              <div class="metric-item">
                <span class="label">MACD DIF</span>
                <span class="value">{{ techView.dif }}</span>
              </div>
              <div class="metric-item">
                <span class="label">MACD DEA</span>
                <span class="value">{{ techView.dea }}</span>
              </div>
              <div class="metric-item">
                <span class="label">MACD柱</span>
                <span class="value">{{ techView.hist }}</span>
              </div>
              <div class="metric-item">
                <span class="label">KDJ（K/D/J）</span>
                <span class="value">
                  {{ techView.k }} / {{ techView.d }} / {{ techView.j }}
                </span>
              </div>
              <div class="metric-item">
                <span class="label">RSI（6/12/24）</span>
                <span class="value">
                  {{ techView.rsi6 ?? '-' }} / {{ techView.rsi12 ?? '-' }} /
                  {{ techView.rsi24 ?? '-' }}
                </span>
              </div>
            </div>
          </template>
          <el-empty
            v-else
            description="暂无日K数据，无法计算技术指标，请点击【实时数据】刷新"
          />
        </el-tab-pane>

        <!-- AI 综合分析 -->
        <el-tab-pane label="AI综合分析" name="ai">
          <div class="ai-toolbar">
            <el-button
              type="danger"
              :loading="aiLoading"
              @click="runAiAnalysis"
            >
              <el-icon><MagicStick /></el-icon>
              {{ aiLoading ? 'AI分析中，约需1~10分钟，请耐心等待...' : '开始 AI 综合分析' }}
            </el-button>
            <el-button v-if="aiResult" type="primary" @click="exportReport">
              <el-icon><Download /></el-icon>
              导出分析报告
            </el-button>
            <el-button v-if="aiResult" type="success" @click="exportWordReport">
              <el-icon><Download /></el-icon>
              导出 Word 报告
            </el-button>
            <span v-if="aiTime" class="ai-time">分析完成于 {{ aiTime }}</span>
          </div>
          <template v-if="aiResult">
            <div class="score-panel">
              <div class="score-main">
                <div class="score-num">{{ aiResult.ruleScore.composite }}</div>
                <div class="score-label">系统综合评分</div>
                <el-tag
                  :type="scoreTagType(aiResult.ruleScore.composite)"
                  effect="dark"
                  size="large"
                >
                  {{ aiResult.ruleScore.valueLevel }}
                </el-tag>
              </div>
              <div class="score-items">
                <div class="score-item">
                  <div class="score-item-head">
                    <span>技术面</span>
                    <span :class="{ neg: aiResult.ruleScore.tech < 0 }">{{
                      aiResult.ruleScore.tech
                    }}</span>
                  </div>
                  <el-progress
                    :percentage="Math.max(0, aiResult.ruleScore.tech)"
                    color="#e6a23c"
                    :stroke-width="10"
                  />
                </div>
                <div class="score-item">
                  <div class="score-item-head">
                    <span>基本面</span>
                    <span :class="{ neg: aiResult.ruleScore.fund < 0 }">{{
                      aiResult.ruleScore.fund
                    }}</span>
                  </div>
                  <el-progress
                    :percentage="Math.max(0, aiResult.ruleScore.fund)"
                    color="#409eff"
                    :stroke-width="10"
                  />
                </div>
                <div class="score-item">
                  <div class="score-item-head">
                    <span>资金面</span>
                    <span :class="{ neg: aiResult.ruleScore.flow < 0 }">{{
                      aiResult.ruleScore.flow
                    }}</span>
                  </div>
                  <el-progress
                    :percentage="Math.max(0, aiResult.ruleScore.flow)"
                    color="#f56c6c"
                    :stroke-width="10"
                  />
                </div>
                <div class="score-item">
                  <div class="score-item-head">
                    <span>消息面</span>
                    <span :class="{ neg: aiResult.ruleScore.news < 0 }">{{
                      aiResult.ruleScore.news
                    }}</span>
                  </div>
                  <el-progress
                    :percentage="Math.max(0, aiResult.ruleScore.news)"
                    color="#67c23a"
                    :stroke-width="10"
                  />
                </div>
                <div class="position-desc">
                  {{ aiResult.ruleScore.positionDesc }}
                </div>
                <div
                  v-if="aiResult.sectorInfo && aiResult.sectorInfo.name"
                  class="sector-desc"
                >
                  所属板块「{{ aiResult.sectorInfo.name }}」今日涨跌
                  {{ aiResult.sectorInfo.changePct }}%
                  <template v-if="aiResult.sectorInfo.rank">
                    ，行业板块涨幅排名 {{ aiResult.sectorInfo.rank }} /
                    {{ aiResult.sectorInfo.total }}
                  </template>
                </div>
                <el-collapse class="score-detail-collapse">
                  <el-collapse-item
                    title="评分明细（各维度得分如何得出，逐项加减分）"
                    name="detail"
                  >
                    <div class="score-detail-grid">
                      <div
                        v-for="dim in scoreDetailDims"
                        :key="dim.name"
                        class="score-dim-card"
                      >
                        <div class="score-dim-head">
                          <span class="score-dim-name">{{ dim.name }}</span>
                          <span
                            class="score-dim-total"
                            :class="totalLevelCls(dim.total)"
                          >{{ dim.total }}</span>
                        </div>
                        <div class="score-dim-body">
                          <div
                            v-for="(row, ri) in dim.rows"
                            :key="ri"
                            class="score-row"
                            :class="row.type"
                          >
                            <span class="score-badge">{{ row.score }}</span>
                            <span class="score-text">{{ row.text }}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </el-collapse-item>
                </el-collapse>
              </div>
            </div>
            <div class="ai-report md-body" v-html="aiAnalysisHtml"></div>
            <div class="ai-disclaimer">
              以上分析由 AI
              基于实时数据自动生成，仅供参考，不构成投资建议，据此操作风险自负。
            </div>
          </template>
          <el-empty
            v-else
            description="点击【开始 AI 综合分析】：AI 将结合技术面/基本面/资金面/消息面/板块强度与股价位置，给出综合分析、投资评分与风险点"
          />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import {
  ref,
  reactive,
  computed,
  onMounted,
  onBeforeUnmount,
  nextTick,
} from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Histogram, Search, Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import {
  GetStockAllDataByCode,
  GetStockListByCondition,
  GetStockBasicByCode,
  GetStockKline,
  GetStockCapitalFlow,
  GetStockFinance,
  GetStockHolderNum,
  DeleteStockDataByCode,
  RefreshAllStockRealtime,
  GetStockNews,
  RefreshStockNews,
  RefreshStockRealtime,
  AnalyzeStock,
} from '@/api/stockAsset'

const stockCodeInput = ref('')
const fetchLoading = ref(false)
const refreshLoading = ref(false)
const stockList = ref([])
const selectedStock = ref(null)
const klineChartRef = ref(null)
const flowChartRef = ref(null)
const klineType = ref(1)
const activeTab = ref('kline')
const financeData = ref([])
const holderData = ref([])
const klineRaw = ref([])
const flowRaw = ref([])
const newsData = ref([])
const newsLoading = ref(false)
const aiLoading = ref(false)
const aiResult = ref(null)
const aiTime = ref('')
let klineChart = null
let flowChart = null

const queryParams = reactive({
  stockName: '',
  stockCode: '',
  industry: '',
  market: null,
})

const page = reactive({ current: 1, limit: 10, total: 0 })

const formatPrice = val => (val != null ? Number(val).toFixed(2) : '-')
const formatPct = val => (val != null ? Number(val).toFixed(2) : '-')
const formatBigNum = val => (val != null ? Number(val).toFixed(2) : '-')
const formatVolume = val => {
  if (val == null) return '-'
  if (val >= 100000000) return (val / 100000000).toFixed(2) + '亿'
  if (val >= 10000) return (val / 10000).toFixed(2) + '万'
  return val
}
const pctClass = val => (Number(val) >= 0 ? 'price-up' : 'price-down')
// 股东户数较上期变化：逻辑放script中，避免模板插值出现裸露的"<"导致vue模板解析错误
const holderChange = (row, index) => {
  const next = holderData.value[index + 1]
  if (index < holderData.value.length - 1 && next && next.holderNum != null) {
    return Number(row.holderNum) - Number(next.holderNum)
  }
  return '-'
}
const klineTypeName = { 1: '日K', 2: '周K', 3: '月K' }
// 与后端抓取上限一致，取全量历史（日K约1万根/周K约2千根/月K约600根）
const klineLimit = { 1: 10000, 2: 2000, 3: 600 }

const fetchStockData = async () => {
  if (!stockCodeInput.value) {
    ElMessage.warning('请输入股票代码')
    return
  }
  fetchLoading.value = true
  try {
    const res = await GetStockAllDataByCode(stockCodeInput.value.trim())
    if (res.code === 200) {
      ElMessage.success(res.data || '获取成功')
      fetchData()
    } else {
      ElMessage.error(res.message || '获取失败')
    }
  } catch (e) {
    ElMessage.error('获取失败：' + e.message)
  } finally {
    fetchLoading.value = false
  }
}

const refreshAllData = async () => {
  refreshLoading.value = true
  ElMessage.info('正在刷新所有股票的实时数据，请稍候...')
  try {
    const res = await RefreshAllStockRealtime()
    if (res.code === 200) {
      ElMessage.success(res.data || '刷新完成')
      fetchData()
      // 若详情区有选中的股票，同步刷新其数据
      if (selectedStock.value) {
        const code = selectedStock.value.stockCode
        const basic = await GetStockBasicByCode(code)
        if (basic.code === 200) selectedStock.value = basic.data
        loadKlineData()
        loadFinanceData()
        loadFlowData()
        loadNewsData()
      }
    } else {
      ElMessage.error(res.message || '刷新失败')
    }
  } catch (e) {
    ElMessage.error('刷新失败：' + e.message)
  } finally {
    refreshLoading.value = false
  }
}

const fetchData = async () => {
  try {
    const res = await GetStockListByCondition(
      page.current,
      page.limit,
      queryParams
    )
    if (res.code === 200) {
      stockList.value = res.data.list
      page.total = res.data.total
    }
  } catch (e) {
    ElMessage.error('查询失败')
  }
}

const handleSearch = () => {
  page.current = 1
  fetchData()
}

const resetQuery = () => {
  queryParams.stockName = ''
  queryParams.stockCode = ''
  queryParams.industry = ''
  queryParams.market = null
  page.current = 1
  fetchData()
}

const handleRowClick = async row => {
  try {
    const res = await GetStockBasicByCode(row.stockCode)
    if (res.code === 200) {
      selectedStock.value = res.data
      financeData.value = []
      holderData.value = []
      klineRaw.value = []
      flowRaw.value = []
      newsData.value = []
      aiResult.value = null
      aiTime.value = ''
      await nextTick()
      loadKlineData()
      loadFinanceData()
      loadFlowData()
    }
  } catch (e) {
    ElMessage.error('加载详情失败')
  }
}

const loadKlineData = async () => {
  if (!selectedStock.value) return
  try {
    const res = await GetStockKline(
      selectedStock.value.stockCode,
      klineType.value,
      klineLimit[klineType.value]
    )
    if (res.code === 200 && res.data && res.data.length > 0) {
      if (activeTab.value === 'kline') {
        renderKlineChart(res.data)
      } else if (klineType.value === 1) {
        // 技术面依赖日K数据，仅缓存不渲染
        klineRaw.value = [...res.data].sort(
          (a, b) => new Date(a.tradeDate) - new Date(b.tradeDate)
        )
      }
    } else {
      klineRaw.value = []
      klineChart && klineChart.clear()
      ElMessage.warning(
        `暂无${klineTypeName[klineType.value]}数据，请点击【实时数据】刷新补全`
      )
    }
  } catch (e) {
    console.error('K线数据加载失败', e)
  }
}

const loadFinanceData = async () => {
  if (!selectedStock.value) return
  try {
    const res = await GetStockFinance(selectedStock.value.stockCode, 100)
    if (res.code === 200) {
      financeData.value = res.data || []
    }
    const hres = await GetStockHolderNum(selectedStock.value.stockCode, 60)
    if (hres.code === 200) {
      holderData.value = hres.data || []
    }
  } catch (e) {
    console.error('财务数据加载失败', e)
  }
}

const loadFlowData = async () => {
  if (!selectedStock.value) return
  try {
    const res = await GetStockCapitalFlow(selectedStock.value.stockCode, 30)
    if (res.code === 200 && res.data && res.data.length > 0) {
      flowRaw.value = res.data
      if (activeTab.value === 'flow') {
        renderFlowChart(res.data)
      }
    } else {
      flowRaw.value = []
    }
  } catch (e) {
    console.error('资金流向数据加载失败', e)
  }
}

// 资金面分析：基于近30日资金流数据计算主力动向
const flowAnalysis = computed(() => {
  const data = [...flowRaw.value].sort(
    (a, b) => new Date(a.tradeDate) - new Date(b.tradeDate)
  )
  if (!data.length) {
    return {
      today: 0,
      days5: 0,
      days10: 0,
      streakText: '-',
      conclusion: '暂无数据',
      tagType: 'info',
    }
  }
  const main = data.map(d => Number(d.mainNetInflow || 0))
  const sum = arr => arr.reduce((s, v) => s + v, 0)
  const today = main[main.length - 1]
  const days5 = sum(main.slice(-5))
  const days10 = sum(main.slice(-10))
  // 从最新一天往前数连续同向天数
  let streak = 1
  for (let i = main.length - 1; i > 0; i--) {
    if (Math.sign(main[i]) === Math.sign(main[i - 1]) && main[i - 1] !== 0)
      streak++
    else break
  }
  const streakText =
    (main[main.length - 1] >= 0 ? '连续流入 ' : '连续流出 ') + streak + ' 天'
  let conclusion, tagType
  if (days5 > 0 && days10 > 0) {
    conclusion = '主力资金持续净流入，资金面偏多，关注增持动向'
    tagType = 'success'
  } else if (days5 > 0 && days10 <= 0) {
    conclusion = '主力资金近5日回流，短期资金面转暖'
    tagType = 'warning'
  } else if (days5 <= 0 && days10 > 0) {
    conclusion = '主力资金近5日流出，注意短线回撤风险'
    tagType = 'warning'
  } else {
    conclusion = '主力资金持续净流出，资金面偏空，宜观望等待企稳'
    tagType = 'danger'
  }
  return { today, days5, days10, streakText, conclusion, tagType }
})

const loadNewsData = async () => {
  if (!selectedStock.value) return
  try {
    const res = await GetStockNews(selectedStock.value.stockCode, 100)
    if (res.code === 200) {
      newsData.value = res.data || []
    }
  } catch (e) {
    console.error('消息面数据加载失败', e)
  }
}

// 实时抓取最新消息（新闻+公告，增量合并去重），完成后重新加载
const refreshNews = async () => {
  if (!selectedStock.value) return
  newsLoading.value = true
  try {
    const res = await RefreshStockNews(selectedStock.value.stockCode)
    if (res.code === 200) {
      ElMessage.success(res.data || '消息刷新完成')
      await loadNewsData()
    } else {
      ElMessage.error(res.message || '消息刷新失败')
    }
  } catch (e) {
    ElMessage.error('消息刷新失败：' + e.message)
  } finally {
    newsLoading.value = false
  }
}

const scoreTagType = s =>
  s >= 80 ? 'success' : s >= 70 ? 'primary' : s >= 60 ? 'warning' : 'danger'

// 单行刷新：只刷新该只股票的实时数据（行情/K线/资金流/财务/消息）
const rowRefreshing = ref('')
const handleRowRefresh = async row => {
  if (rowRefreshing.value) return
  rowRefreshing.value = row.stockCode
  try {
    const res = await RefreshStockRealtime(row.stockCode)
    if (res.code === 200) {
      ElMessage.success(res.data || '刷新成功')
      fetchData()
      // 当前详情正展示该股时，同步刷新详情各页签数据
      if (selectedStock.value && selectedStock.value.stockCode === row.stockCode) {
        loadKlineData()
        loadFinanceData()
        loadFlowData()
        loadNewsData()
      }
    } else {
      ElMessage.error(res.message || '刷新失败')
    }
  } catch (e) {
    ElMessage.error('刷新失败：' + (e.message || '请稍后重试'))
  } finally {
    rowRefreshing.value = ''
  }
}

// AI 综合分析：后端聚合各维度数据 + 板块情况，规则评分后调用公共 AI 接口
const runAiAnalysis = async () => {
  if (!selectedStock.value) return
  aiLoading.value = true
  try {
    const res = await AnalyzeStock(selectedStock.value.stockCode)
    if (res.code === 200 && res.data) {
      aiResult.value = res.data
      aiTime.value = new Date().toLocaleString()
      ElMessage.success('AI 综合分析完成')
    } else {
      ElMessage.error(res.message || 'AI 分析失败')
    }
  } catch (e) {
    ElMessage.error('AI 分析失败：' + (e.message || '请稍后重试'))
  } finally {
    aiLoading.value = false
  }
}

// 极简 Markdown 渲染：先转义再处理标题/加粗/列表，防注入
const mdToHtml = md => {
  if (!md) return ''
  const esc = s =>
    s
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
  const inline = s => s.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  const lines = esc(md).split(/\r?\n/)
  let html = ''
  let inList = false
  for (const raw of lines) {
    const line = raw.trim()
    const li = line.match(/^[-*]\s+(.*)/) || line.match(/^\d+\.\s+(.*)/)
    if (li) {
      if (!inList) {
        html += '<ul>'
        inList = true
      }
      html += `<li>${inline(li[1])}</li>`
      continue
    }
    if (inList) {
      html += '</ul>'
      inList = false
    }
    if (!line) continue
    const h = line.match(/^(#{1,6})\s+(.*)/)
    if (h) {
      const level = Math.min(h[1].length + 1, 6)
      html += `<h${level}>${inline(h[2])}</h${level}>`
    } else {
      html += `<p>${inline(line)}</p>`
    }
  }
  if (inList) html += '</ul>'
  return html
}

const aiAnalysisHtml = computed(() => mdToHtml(aiResult.value?.aiAnalysis))

// 解析评分明细：拆出各维度总分与逐条加减分（分数徽章+说明文本）
const parseScoreDetail = (items) => {
  let total = ''
  const rows = []
  for (const raw of items || []) {
    const s = String(raw)
    if (s.startsWith('=>')) {
      total = s.replace(/^=>\s*/, '')
      continue
    }
    const m = s.match(/^([+-]?\d+(?:\.\d+)?|×[\d.]+)\s*(.*)$/)
    if (m) {
      const sc = m[1]
      rows.push({
        score: sc,
        text: m[2],
        type: sc.startsWith('+') ? 'plus' : sc.startsWith('-') ? 'minus' : 'zero'
      })
    } else {
      rows.push({ score: '·', text: s, type: 'zero' })
    }
  }
  return { total, rows }
}

// 总分徽章配色：≥70看多(红)、<45看空(绿，含负分)、其余中性
const totalLevelCls = (total) => {
  const m = String(total).match(/-?\d+/)
  const num = m ? parseInt(m[0], 10) : NaN
  if (isNaN(num)) return ''
  return num >= 70 ? 'is-good' : num < 45 ? 'is-bad' : 'is-mid'
}

// 四个维度的评分明细（解析后供卡片渲染）
const scoreDetailDims = computed(() => {
  if (!aiResult.value || !aiResult.value.ruleScore) return []
  const rs = aiResult.value.ruleScore
  return [
    { name: '技术面', items: rs.techDetail },
    { name: '基本面', items: rs.fundDetail },
    { name: '资金筹码面', items: rs.flowDetail },
    { name: '消息面', items: rs.newsDetail }
  ].map((d) => ({ name: d.name, ...parseScoreDetail(d.items) }))
})

// 导出美观的 HTML 分析报告（浏览器打开即可打印为 PDF）
const exportReport = () => {
  if (!aiResult.value || !selectedStock.value) return
  const s = aiResult.value.ruleScoreLike
    ? null
    : aiResult.value.ruleScoreLike
    ? null
    : aiResult.value.ruleScore
  const stock = selectedStock.value
  const sec = aiResult.value.sectorInfo || {}
  const html = `<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8" />
<title>AI综合分析报告 - ${stock.stockName}（${stock.stockCode}）</title>
<style>
  body { font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif; max-width: 900px; margin: 0 auto; padding: 40px 32px; color: #303133; line-height: 1.8; }
  .report-header { text-align: center; border-bottom: 3px solid #c0392b; padding-bottom: 20px; margin-bottom: 24px; }
  .report-header h1 { margin: 0 0 8px; font-size: 26px; color: #c0392b; }
  .report-meta { color: #909399; font-size: 13px; }
  .stock-brief { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; margin-bottom: 24px; }
  .brief-item { background: #f8f9fa; border-radius: 8px; padding: 10px 14px; font-size: 13px; }
  .brief-item b { display: block; color: #909399; font-weight: normal; font-size: 12px; }
  .score-section { display: flex; gap: 24px; align-items: center; background: linear-gradient(135deg, #fdf6f5, #f8f9fa); border-radius: 12px; padding: 20px 24px; margin-bottom: 24px; }
  .score-big { text-align: center; }
  .score-big .num { font-size: 56px; font-weight: 700; color: #c0392b; line-height: 1.1; }
  .score-big .lab { font-size: 13px; color: #909399; }
  .score-sec-table { flex: 1; }
  table { width: 100%; border-collapse: collapse; font-size: 14px; }
  th, td { border: 1px solid #e4e7ed; padding: 8px 12px; text-align: left; }
  th { background: #f5f7fa; }
  .grade { display: inline-block; background: #c0392b; color: #fff; border-radius: 6px; padding: 4px 14px; font-weight: 600; }
  .md-body h1, .md-body h2 { border-left: 4px solid #c0392b; padding-left: 12px; font-size: 18px; margin: 26px 0 10px; }
  .md-body h3 { font-size: 16px; margin: 20px 0 8px; }
  .md-body ul { padding-left: 22px; margin: 8px 0; }
  .md-body li { margin: 4px 0; }
  .md-body p { margin: 8px 0; text-align: justify; }
  .md-body strong { color: #c0392b; }
  .disclaimer { margin-top: 32px; padding: 12px 16px; background: #fef0f0; color: #a94442; border-radius: 8px; font-size: 12px; }
  @media print { body { padding: 0; } .score-section { -webkit-print-color-adjust: exact; } }
</style>
</head>
<body>
  <div class="report-header">
    <h1>AI 综合分析报告</h1>
    <div class="report-meta">${stock.stockName}（${
    stock.stockCode
  }）· 所属行业：${stock.industry || '-'} · 生成时间：${aiTime.value}</div>
  </div>
  <div class="stock-brief">
    <div class="brief-item"><b>最新价</b>${stock.lastPrice ?? '-'}</div>
    <div class="brief-item"><b>涨跌幅</b>${stock.changePct ?? '-'}%</div>
    <div class="brief-item"><b>总市值</b>${stock.totalMarketCap ?? '-'} 亿</div>
    <div class="brief-item"><b>PE(TTM)</b>${stock.peTtm ?? '-'}</div>
    <div class="brief-item"><b>PB</b>${stock.pbRatio ?? '-'}</div>
    <div class="brief-item"><b>换手率</b>${stock.turnoverRate ?? '-'}%</div>
  </div>
  <div class="score-section">
    <div class="score-big"><div class="num">${
      s.composite
    }</div><div class="lab">系统综合评分</div><div style="margin-top:6px"><span class="grade">${
    s.valueLevel
  }</span></div></div>
    <div class="score-sec-table">
      <table>
        <tr><th>维度</th><th>评分</th><th>权重</th><th>说明</th></tr>
        <tr><td>技术面</td><td>${
          s.tech
        }</td><td>35%</td><td>均线/MACD/KDJ/RSI/量价</td></tr>
        <tr><td>基本面</td><td>${
          s.fund
        }</td><td>30%</td><td>成长性/ROE/估值</td></tr>
        <tr><td>资金面</td><td>${
          s.flow
        }</td><td>20%</td><td>主力资金流向</td></tr>
        <tr><td>消息面</td><td>${
          s.news
        }</td><td>15%</td><td>消息时效与公告密度</td></tr>
      </table>
      <p style="font-size:13px;color:#606266;margin:10px 0 0;">${
        s.positionDesc
      }</p>
      ${
        sec.name
          ? `<p style="font-size:13px;color:#606266;margin:4px 0 0;">板块情况：${
              sec.name
            } 今日涨跌 ${sec.changePct}%${
              sec.rank ? `，行业板块涨幅排名 ${sec.rank}/${sec.total}` : ''
            }</p>`
          : ''
      }
    </div>
  </div>
  <div class="md-body">${aiAnalysisHtml.value}</div>
  <div class="disclaimer">免责声明：本报告由 AI 基于公开实时数据自动生成，所有评分与分析仅供参考，不构成任何投资建议。市场有风险，投资需谨慎。</div>
</body>
</html>`
  const blob = new Blob([html], { type: 'text/html;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `AI分析报告_${stock.stockName}_${
    stock.stockCode
  }_${new Date().toISOString().slice(0, 10)}.html`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('分析报告已导出')
}

// 导出 Word 分析报告：采用 Word 兼容 HTML（.doc），Word/WPS 打开即为美观排版，无需额外依赖
const exportWordReport = () => {
  if (!aiResult.value || !selectedStock.value) return
  const s = aiResult.value.ruleScore || {}
  const stock = selectedStock.value
  const sec = aiResult.value.sectorInfo || {}
  const dims = scoreDetailDims.value

  // 维度总分徽章配色：≥70红(看多)、<45绿(看空，含负分)、其余橙(中性)
  const levelColor = v => {
    const n = Number(String(v).match(/-?\d+/))
    if (isNaN(n)) return '#e6a23c'
    return n >= 70 ? '#c0392b' : n < 45 ? '#27ae60' : '#e6a23c'
  }
  const scoreColor = t =>
    t === 'plus' ? '#c0392b' : t === 'minus' ? '#27ae60' : '#909399'

  // 评分明细表：每维度一张表（总分徽章 + 逐条加减分，红涨绿跌）
  const dimTableHtml = d => {
    const rows = (d.rows || [])
      .map(
        r => `<tr>
            <td width="72" align="center" style="border:1pt solid #ebeef5;padding:4pt 6pt;background-color:#fafafa;"><b style="color:${scoreColor(r.type)};font-size:10pt;">${r.score}</b></td>
            <td style="border:1pt solid #ebeef5;padding:4pt 8pt;font-size:10.5pt;">${r.text}</td>
          </tr>`
      )
      .join('')
    return `<table width="100%" style="border-collapse:collapse;margin:8pt 0 14pt;">
          <tr>
            <td colspan="2" style="border:1pt solid #ebeef5;padding:6pt 10pt;background-color:#f5f7fa;font-size:11pt;">
              <b style="color:#303133;">${d.name}评分构成</b>
              <span style="background-color:${levelColor(d.total)};color:#ffffff;padding:2pt 10pt;font-weight:bold;font-size:10pt;">${d.total || '暂无'}</span>
            </td>
          </tr>
          ${rows}
        </table>`
  }

  // AI 分析正文（复用 mdToHtml，保留标题/列表/加粗结构）
  const aiBody = mdToHtml(aiResult.value.aiAnalysis)

  const html = `<html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:w="urn:schemas-microsoft-com:office:word" xmlns="http://www.w3.org/TR/REC-html40">
<head>
<meta charset="utf-8" />
<title>AI综合分析报告 - ${stock.stockName}（${stock.stockCode}）</title>
<!--[if gte mso 9]><xml><w:WordDocument><w:View>Print</w:View><w:Zoom>100</w:Zoom></w:WordDocument></xml><![endif]-->
<style>
  @page WordSection1 { size:595.3pt 841.9pt; margin:57pt 57pt 57pt 57pt; }
  div.WordSection1 { page:WordSection1; }
  body { font-family:'Microsoft YaHei','SimHei',sans-serif; font-size:11pt; color:#303133; line-height:1.7; }
  p { margin:6pt 0; }
  table { border-collapse:collapse; width:100%; }
  td, th { border:1pt solid #dcdfe6; padding:5pt 8pt; font-size:10.5pt; }
  h1 { font-size:20pt; color:#c0392b; text-align:center; margin:0 0 6pt; }
  h2 { font-size:14pt; color:#1f3a5f; border-left:4pt solid #c0392b; padding-left:9pt; margin:18pt 0 8pt; }
  h3 { font-size:12pt; color:#303133; margin:12pt 0 6pt; }
  ul { margin:6pt 0; padding-left:22pt; }
  li { margin:3pt 0; }
  strong { color:#c0392b; }
</style>
</head>
<body>
<div class="WordSection1">
  <div style="text-align:center;border-bottom:3pt double #c0392b;padding-bottom:10pt;margin-bottom:14pt;">
    <h1>AI 综合分析报告</h1>
    <span style="color:#909399;font-size:10pt;">${stock.stockName}（${stock.stockCode}）· 所属行业：${stock.industry || '-'} · 生成时间：${aiTime.value}</span>
  </div>

  <h2>一、个股概览</h2>
  <table width="100%" style="border-collapse:collapse;">
    <tr>
      <td width="16%" style="background-color:#f5f7fa;">最新价</td><td width="17%">${stock.lastPrice ?? '-'}</td>
      <td width="16%" style="background-color:#f5f7fa;">涨跌幅</td><td width="17%">${stock.changePct ?? '-'}%</td>
      <td width="16%" style="background-color:#f5f7fa;">总市值</td><td width="18%">${stock.totalMarketCap ?? '-'} 亿</td>
    </tr>
    <tr>
      <td style="background-color:#f5f7fa;">PE(TTM)</td><td>${stock.peTtm ?? '-'}</td>
      <td style="background-color:#f5f7fa;">PB</td><td>${stock.pbRatio ?? '-'}</td>
      <td style="background-color:#f5f7fa;">换手率</td><td>${stock.turnoverRate ?? '-'}%</td>
    </tr>
  </table>

  <h2>二、系统规则评分</h2>
  <table width="100%" style="border-collapse:collapse;margin-bottom:8pt;">
    <tr>
      <td width="30%" align="center" rowspan="6" style="border:1pt solid #dcdfe6;background-color:#fdf6f5;">
        <span style="font-size:34pt;font-weight:bold;color:${levelColor(s.composite)};">${s.composite}</span><br />
        <span style="font-size:10pt;color:#909399;">系统综合评分</span><br />
        <span style="background-color:${levelColor(s.composite)};color:#ffffff;padding:2pt 10pt;font-weight:bold;font-size:10pt;">${s.valueLevel || ''}</span>
      </td>
      <th width="18%">维度</th><th width="16%">评分</th><th width="14%">权重</th><th>说明</th>
    </tr>
    <tr><td>技术面</td><td align="center"><b style="color:${levelColor(s.tech)};">${s.tech}</b></td><td align="center">35%</td><td>均线/MACD/KDJ/RSI/量价</td></tr>
    <tr><td>基本面</td><td align="center"><b style="color:${levelColor(s.fund)};">${s.fund}</b></td><td align="center">30%</td><td>成长性/ROE/估值</td></tr>
    <tr><td>资金面</td><td align="center"><b style="color:${levelColor(s.flow)};">${s.flow}</b></td><td align="center">20%</td><td>主力资金流向</td></tr>
    <tr><td>消息面</td><td align="center"><b style="color:${levelColor(s.news)};">${s.news}</b></td><td align="center">15%</td><td>消息时效与公告密度</td></tr>
    <tr><td colspan="3" style="background-color:#f5f7fa;">环境修正</td><td>${s.envDesc || '-'}</td></tr>
  </table>
  <p style="font-size:10.5pt;color:#606266;">${s.positionDesc || ''}</p>
  ${
    sec.name
      ? `<p style="font-size:10.5pt;color:#606266;">所属板块「${sec.name}」今日涨跌 ${sec.changePct}%${
          sec.rank ? `，行业板块涨幅排名 ${sec.rank}/${sec.total}` : ''
        }</p>`
      : ''
  }

  <h2>三、评分明细</h2>
  ${dims.map(d => dimTableHtml(d)).join('')}

  <h2>四、AI 综合分析</h2>
  <div>${aiBody}</div>

  <p style="margin-top:20pt;padding:9pt 12pt;background-color:#fef0f0;color:#a94442;font-size:9.5pt;border:1pt solid #f5dcdc;">
    免责声明：本报告由 AI 基于公开实时数据自动生成，所有评分与分析仅供参考，不构成任何投资建议。市场有风险，投资需谨慎。
  </p>
</div>
</body>
</html>`
  // \ufeff BOM 确保 Word 正确识别 UTF-8 中文
  const blob = new Blob(['\ufeff' + html], {
    type: 'application/msword;charset=utf-8'
  })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `AI分析报告_${stock.stockName}_${
    stock.stockCode
  }_${new Date().toISOString().slice(0, 10)}.doc`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('Word 报告已导出')
}

const handleTabChange = name => {
  if (!selectedStock.value) return
  nextTick(() => {
    if (name === 'kline') {
      loadKlineData()
    } else if (name === 'flow') {
      loadFlowData()
    } else if (name === 'news') {
      loadNewsData()
    }
  })
}

const calcMa = (closes, days) =>
  closes.map((_, i) => {
    if (i < days - 1) return null
    let s = 0
    for (let j = i - days + 1; j <= i; j++) s += closes[j]
    return Number((s / days).toFixed(2))
  })

const calcEma = (arr, n) => {
  const k = 2 / (n + 1)
  const out = []
  let prev = arr[0]
  arr.forEach((v, i) => {
    prev = i === 0 ? v : v * k + prev * (1 - k)
    out.push(prev)
  })
  return out
}

const renderKlineChart = data => {
  if (!klineChartRef.value) return
  // 选中行被删除后 v-if 会移除容器 DOM，旧实例绑定在已卸载节点上，需重建
  if (klineChart && klineChart.getDom() !== klineChartRef.value) {
    klineChart.dispose()
    klineChart = null
  }
  if (!klineChart) klineChart = echarts.init(klineChartRef.value)
  const sorted = [...data].sort(
    (a, b) => new Date(a.tradeDate) - new Date(b.tradeDate)
  )
  if (klineType.value === 1) klineRaw.value = sorted
  const dates = sorted.map(d => d.tradeDate)
  const ohlc = sorted.map(d => [
    Number(d.openPrice),
    Number(d.closePrice),
    Number(d.lowPrice),
    Number(d.highPrice),
  ])
  const closes = sorted.map(d => Number(d.closePrice))
  const volumes = sorted.map(d => Number(d.volume))
  const ups = sorted.map(d => Number(d.changePct ?? 0) >= 0)

  // MA 均线
  const maDefs = [
    { name: 'MA5', days: 5, color: '#ff9800' },
    { name: 'MA10', days: 10, color: '#2196f3' },
    { name: 'MA20', days: 20, color: '#9c27b0' },
    { name: 'MA60', days: 60, color: '#607d8b' },
  ]

  // MACD(12,26,9)，柱 = 2*(DIF-DEA)
  const ema12 = calcEma(closes, 12)
  const ema26 = calcEma(closes, 26)
  const dif = ema12.map((v, i) => Number((v - ema26[i]).toFixed(3)))
  const dea = calcEma(dif, 9).map(v => Number(v.toFixed(3)))
  const hist = dif.map((v, i) => Number((2 * (v - dea[i])).toFixed(3)))

  // 默认展示最近120根，可缩放查看全部
  const startPct = Math.max(0, 100 - (120 / sorted.length) * 100)

  klineChart.setOption(
    {
      animation: false,
      backgroundColor: '#fff',
      axisPointer: { link: [{ xAxisIndex: 'all' }] },
      // 通达信风格 tooltip：显示涨跌幅/涨跌额/振幅/换手率等完整行情信息
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'cross' },
        formatter: params => {
          const k = sorted[params[0]?.dataIndex]
          if (!k) return ''
          const pct = Number(k.changePct ?? 0)
          const pctColor = pct >= 0 ? '#f56c6c' : '#4caf50'
          const dot = c =>
            `<span style="display:inline-block;width:8px;height:8px;border-radius:50%;background:${
              typeof c === 'string' ? c : '#909399'
            };margin-right:4px;"></span>`
          let html = `<div style="font-weight:600;margin-bottom:4px;">${
            k.tradeDate
          }<span style="color:${pctColor};margin-left:8px;">${
            pct >= 0 ? '+' : ''
          }${pct.toFixed(2)}%</span></div>`
          html += `开盘：${k.openPrice ?? '-'}　收盘：${k.closePrice ??
            '-'}<br/>`
          html += `最高：${k.highPrice ?? '-'}　最低：${k.lowPrice ?? '-'}<br/>`
          html += `涨跌：${k.changeAmount ?? '-'}　振幅：${k.amplitude ??
            '-'}%　换手：${k.turnoverRate ?? '-'}%<br/>`
          params
            .filter(p => ['MA5', 'MA10', 'MA20', 'MA60'].includes(p.seriesName))
            .forEach(p => {
              html += `${dot(p.color)}${p.seriesName}：${
                p.value != null ? Number(p.value).toFixed(2) : '-'
              }　`
            })
          const vol = params.find(p => p.seriesName === '成交量')
          if (vol)
            html += `<br/>${dot(vol.color)}成交量：${Number(
              vol.value
            ).toLocaleString()}`
          const difP = params.find(p => p.seriesName === 'DIF')
          const deaP = params.find(p => p.seriesName === 'DEA')
          const macdP = params.find(p => p.seriesName === 'MACD')
          if (difP || deaP || macdP) {
            html += '<br/>'
            if (difP)
              html += `${dot(difP.color)}DIF：${
                difP.value != null ? Number(difP.value).toFixed(3) : '-'
              }　`
            if (deaP)
              html += `${dot(deaP.color)}DEA：${
                deaP.value != null ? Number(deaP.value).toFixed(3) : '-'
              }　`
            if (macdP)
              html += `${dot(macdP.color)}MACD：${
                macdP.value != null ? Number(macdP.value).toFixed(3) : '-'
              }`
          }
          return html
        },
      },
      legend: { data: ['MA5', 'MA10', 'MA20', 'MA60', 'DIF', 'DEA'], top: 0 },
      grid: [
        { left: 60, right: 70, top: 28, height: '50%' },
        { left: 60, right: 70, top: '62%', height: '12%' },
        { left: 60, right: 70, top: '78%', height: '14%' },
      ],
      xAxis: [
        {
          type: 'category',
          data: dates,
          scale: true,
          axisLine: { onZero: false },
          splitLine: { show: false },
        },
        {
          type: 'category',
          gridIndex: 1,
          data: dates,
          axisLabel: { show: false },
          axisTick: { show: false },
          axisLine: { onZero: false },
          splitLine: { show: false },
        },
        {
          type: 'category',
          gridIndex: 2,
          data: dates,
          axisLabel: { show: false },
          axisTick: { show: false },
          axisLine: { onZero: false },
          splitLine: { show: false },
        },
      ],
      yAxis: [
        { scale: true, splitArea: { show: true } },
        {
          gridIndex: 1,
          axisLabel: { show: false },
          splitLine: { show: false },
        },
        { gridIndex: 2, splitNumber: 3, splitLine: { show: false } },
      ],
      dataZoom: [
        { type: 'inside', xAxisIndex: [0, 1, 2], start: startPct, end: 100 },
        {
          type: 'slider',
          xAxisIndex: [0, 1, 2],
          top: '94%',
          height: 16,
          start: startPct,
          end: 100,
        },
      ],
      series: [
        {
          name: 'K线',
          type: 'candlestick',
          data: ohlc,
          itemStyle: {
            color: '#ec0000',
            color0: '#00da3c',
            borderColor: '#ec0000',
            borderColor0: '#00da3c',
          },
        },
        ...maDefs.map(m => ({
          name: m.name,
          type: 'line',
          data: calcMa(closes, m.days),
          showSymbol: false,
          lineStyle: { width: 1, color: m.color },
          itemStyle: { color: m.color },
        })),
        {
          name: '成交量',
          type: 'bar',
          xAxisIndex: 1,
          yAxisIndex: 1,
          data: volumes,
          itemStyle: { color: p => (ups[p.dataIndex] ? '#ec0000' : '#00da3c') },
        },
        {
          name: 'MACD',
          type: 'bar',
          xAxisIndex: 2,
          yAxisIndex: 2,
          data: hist,
          itemStyle: { color: p => (p.value >= 0 ? '#ec0000' : '#00da3c') },
        },
        {
          name: 'DIF',
          type: 'line',
          xAxisIndex: 2,
          yAxisIndex: 2,
          data: dif,
          showSymbol: false,
          lineStyle: { width: 1, color: '#ff9800' },
          itemStyle: { color: '#ff9800' },
        },
        {
          name: 'DEA',
          type: 'line',
          xAxisIndex: 2,
          yAxisIndex: 2,
          data: dea,
          showSymbol: false,
          lineStyle: { width: 1, color: '#2196f3' },
          itemStyle: { color: '#2196f3' },
        },
      ],
    },
    true
  )
  klineChart.resize()
}

const renderFlowChart = data => {
  if (!flowChartRef.value) return
  if (flowChart && flowChart.getDom() !== flowChartRef.value) {
    flowChart.dispose()
    flowChart = null
  }
  if (!flowChart) flowChart = echarts.init(flowChartRef.value)
  const sorted = [...data].sort(
    (a, b) => new Date(a.tradeDate) - new Date(b.tradeDate)
  )
  const dates = sorted.map(d => d.tradeDate)
  const val = fn => sorted.map(d => Number(fn(d) || 0))
  flowChart.setOption(
    {
      backgroundColor: '#fff',
      tooltip: { trigger: 'axis' },
      legend: {
        data: ['主力净流入', '超大单', '大单', '中单', '小单'],
        top: 0,
      },
      grid: { left: 60, right: 30, top: 36, bottom: 40 },
      xAxis: { type: 'category', data: dates },
      yAxis: { type: 'value', name: '万元' },
      series: [
        {
          name: '主力净流入',
          type: 'bar',
          data: val(d => d.mainNetInflow),
          itemStyle: { color: p => (p.value >= 0 ? '#ec0000' : '#00da3c') },
        },
        {
          name: '超大单',
          type: 'line',
          showSymbol: false,
          data: val(d => d.superLargeNet),
          lineStyle: { width: 1.5, color: '#f5222d' },
          itemStyle: { color: '#f5222d' },
        },
        {
          name: '大单',
          type: 'line',
          showSymbol: false,
          data: val(d => d.largeNet),
          lineStyle: { width: 1.5, color: '#fa8c16' },
          itemStyle: { color: '#fa8c16' },
        },
        {
          name: '中单',
          type: 'line',
          showSymbol: false,
          data: val(d => d.mediumNet),
          lineStyle: { width: 1.5, color: '#2196f3' },
          itemStyle: { color: '#2196f3' },
        },
        {
          name: '小单',
          type: 'line',
          showSymbol: false,
          data: val(d => d.smallNet),
          lineStyle: { width: 1.5, color: '#52c41a' },
          itemStyle: { color: '#52c41a' },
        },
      ],
    },
    true
  )
  flowChart.resize()
}

// 技术面：基于日K计算 MA/MACD/KDJ/RSI 与简单信号
const techView = computed(() => {
  const data = klineRaw.value
  if (!data || data.length < 30) return null
  const n = data.length
  const closes = data.map(d => Number(d.closePrice))
  const highs = data.map(d => Number(d.highPrice))
  const lows = data.map(d => Number(d.lowPrice))
  const last = closes[n - 1]

  const ma = days => {
    if (n < days) return null
    let s = 0
    for (let i = n - days; i < n; i++) s += closes[i]
    return Number((s / days).toFixed(2))
  }
  const ma5 = ma(5),
    ma10 = ma(10),
    ma20 = ma(20),
    ma60 = ma(60)

  const ema12 = calcEma(closes, 12)
  const ema26 = calcEma(closes, 26)
  const difArr = ema12.map((v, i) => v - ema26[i])
  const deaArr = calcEma(difArr, 9)
  const dif = Number(difArr[n - 1].toFixed(3))
  const dea = Number(deaArr[n - 1].toFixed(3))
  const hist = Number((2 * (difArr[n - 1] - deaArr[n - 1])).toFixed(3))
  const prevDiff = difArr[n - 2] - deaArr[n - 2]

  let k = 50,
    dd = 50
  for (let i = 0; i < n; i++) {
    const hh = Math.max(...highs.slice(Math.max(0, i - 8), i + 1))
    const ll = Math.min(...lows.slice(Math.max(0, i - 8), i + 1))
    const rsv = hh === ll ? 50 : ((closes[i] - ll) / (hh - ll)) * 100
    k = (2 / 3) * k + (1 / 3) * rsv
    dd = (2 / 3) * dd + (1 / 3) * k
  }
  const j = 3 * k - 2 * dd

  const rsi = days => {
    if (n <= days) return null
    let up = 0,
      dn = 0
    for (let i = n - days; i < n; i++) {
      const ch = closes[i] - closes[i - 1]
      if (ch >= 0) up += ch
      else dn -= ch
    }
    if (up + dn === 0) return 50
    return Number(((100 * up) / (up + dn)).toFixed(2))
  }
  const rsi6 = rsi(6),
    rsi12 = rsi(12),
    rsi24 = rsi(24)

  const signals = []
  if (ma5 && ma10 && ma20 && ma60) {
    if (ma5 > ma10 && ma10 > ma20 && ma20 > ma60)
      signals.push({ text: '均线多头排列', type: 'danger' })
    else if (ma5 < ma10 && ma10 < ma20 && ma20 < ma60)
      signals.push({ text: '均线空头排列', type: 'success' })
    if (last > ma5 && last > ma10 && last > ma20)
      signals.push({ text: '站上短期均线', type: 'danger' })
    else if (last < ma5 && last < ma10 && last < ma20)
      signals.push({ text: '跌破短期均线', type: 'success' })
  }
  if (dif > dea) {
    signals.push({
      text: prevDiff <= 0 ? 'MACD金叉' : 'MACD多头',
      type: prevDiff <= 0 ? 'danger' : 'info',
    })
  } else {
    signals.push({
      text: prevDiff >= 0 ? 'MACD死叉' : 'MACD空头',
      type: prevDiff >= 0 ? 'success' : 'info',
    })
  }
  if (k > 80) signals.push({ text: 'KDJ超买', type: 'warning' })
  else if (k < 20) signals.push({ text: 'KDJ超卖', type: 'warning' })
  if (rsi6 != null && rsi6 > 80)
    signals.push({ text: 'RSI6超买', type: 'warning' })
  else if (rsi6 != null && rsi6 < 20)
    signals.push({ text: 'RSI6超卖', type: 'warning' })

  return {
    last,
    ma5,
    ma10,
    ma20,
    ma60,
    dif,
    dea,
    hist,
    k: Number(k.toFixed(2)),
    d: Number(dd.toFixed(2)),
    j: Number(j.toFixed(2)),
    rsi6,
    rsi12,
    rsi24,
    signals,
  }
})

const handleDelete = row => {
  ElMessageBox.confirm(
    `确认删除 ${row.stockName}（${row.stockCode}）的全部数据？`,
    '提示',
    {
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        const res = await DeleteStockDataByCode(row.stockCode)
        if (res.code === 200) {
          ElMessage.success('删除成功')
          fetchData()
          if (selectedStock.value?.stockCode === row.stockCode) {
            selectedStock.value = null
            klineRaw.value = []
            financeData.value = []
            holderData.value = []
          }
        }
      } catch (e) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

onMounted(() => {
  fetchData()
})

onBeforeUnmount(() => {
  if (klineChart) klineChart.dispose()
  if (flowChart) flowChart.dispose()
})
</script>

<style scoped>
.stock-management {
  padding: 16px;
  background: #f5f7fa;
  min-height: 100vh;
}

.holder-title {
  margin: 16px 0 10px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 16px 24px;
  border-radius: 12px;
  margin-bottom: 16px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.page-header h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
}

.fetch-area {
  display: flex;
  gap: 12px;
  align-items: center;
}

.query-section {
  background: white;
  padding: 16px 20px;
  border-radius: 10px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.list-section {
  background: white;
  padding: 16px;
  border-radius: 10px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.pagination-area {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

.detail-section {
  margin-top: 16px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.info-card {
  background: white;
  border-radius: 10px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: transform 0.2s;
}

.info-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.full-width {
  grid-column: 1 / -1;
}

.name-card {
  grid-column: span 1;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #909399;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.name-card .card-title {
  color: rgba(255, 255, 255, 0.9);
  border-bottom-color: rgba(255, 255, 255, 0.2);
}

.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.stock-name {
  font-size: 20px;
  font-weight: 700;
}

.industry-tag {
  font-size: 12px;
  opacity: 0.8;
  margin-bottom: 12px;
}

.list-date {
  font-size: 12px;
  opacity: 0.85;
  margin-top: 8px;
}

.price-display {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.price-display .big {
  font-size: 28px;
  font-weight: 700;
}

.price-up {
  color: #ec0000;
}

.price-down {
  color: #00da3c;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.metric-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.metric-item .label {
  font-size: 12px;
  color: #909399;
}

.metric-item .value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.company-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  font-size: 13px;
  line-height: 1.6;
  color: #606266;
}

.info-row .label {
  color: #909399;
  font-weight: 600;
}

.detail-tabs {
  background: white;
  border-radius: 10px;
  padding: 8px 16px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 16px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart-container {
  width: 100%;
  height: 400px;
}

.kline-height {
  height: 540px;
}

.signal-area {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.tech-grid {
  grid-template-columns: repeat(4, 1fr);
}

/* 资金面分析 */
.flow-analysis {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  padding: 12px 16px;
  margin-bottom: 12px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.flow-summary {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  flex: 1;
  min-width: 480px;
}

.flow-conclusion {
  padding-left: 12px;
}

/* 消息面 */
.news-latest {
  margin-bottom: 16px;
}

.news-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 6px;
}

.news-item.latest {
  background: #fff7f0;
  margin-bottom: 6px;
}

.news-title {
  color: #303133;
  text-decoration: none;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.news-title:hover {
  color: #409eff;
  text-decoration: underline;
}

.news-meta {
  margin-left: auto;
  flex-shrink: 0;
  font-size: 12px;
  color: #909399;
}

.news-history-title {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 12px;
}

.news-summary {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* AI 综合分析 */
.ai-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.ai-time {
  font-size: 12px;
  color: #909399;
}

.score-panel {
  display: flex;
  gap: 32px;
  align-items: center;
  flex-wrap: wrap;
  background: linear-gradient(135deg, #fdf6f5, #f8fafc);
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  padding: 18px 24px;
  margin-bottom: 16px;
}

.score-main {
  text-align: center;
  min-width: 120px;
}

.score-num {
  font-size: 52px;
  font-weight: 700;
  color: #c0392b;
  line-height: 1.1;
}

.score-label {
  font-size: 13px;
  color: #909399;
  margin: 4px 0 8px;
}

.score-items {
  flex: 1;
  min-width: 320px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px 24px;
}

.score-item-head {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #606266;
  margin-bottom: 4px;
}

.score-item-head span.neg {
  color: #67c23a;
  font-weight: 600;
}

.position-desc,
.sector-desc {
  grid-column: 1 / -1;
  font-size: 13px;
  color: #606266;
  background: #fff;
  border-radius: 6px;
  padding: 6px 10px;
  border: 1px dashed #dcdfe6;
}

.score-detail-collapse {
  grid-column: 1 / -1;
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
  background: #fff;
  padding: 0 12px;
}

.score-detail-collapse :deep(.el-collapse-item__header) {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.score-detail-collapse :deep(.el-collapse-item__content) {
  padding-bottom: 14px;
}

.score-detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

@media (max-width: 1400px) {
  .score-detail-grid {
    grid-template-columns: 1fr;
  }
}

.score-dim-card {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #fafbfc;
  overflow: hidden;
}

.score-dim-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 7px 12px;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

.score-dim-name {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.score-dim-total {
  font-size: 12px;
  font-weight: 600;
  border-radius: 10px;
  padding: 1px 10px;
  color: #fff;
  background: #909399;
}

.score-dim-total.is-good {
  background: #f56c6c;
}

.score-dim-total.is-bad {
  background: #67c23a;
}

.score-dim-total.is-mid {
  background: #e6a23c;
}

.score-dim-body {
  padding: 8px 12px;
}

.score-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 12px;
  line-height: 20px;
  padding: 1px 0;
}

.score-badge {
  flex: 0 0 42px;
  text-align: right;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.score-row.plus .score-badge {
  color: #f56c6c;
}

.score-row.minus .score-badge {
  color: #67c23a;
}

.score-row.zero .score-badge {
  color: #c0c4cc;
}

.score-text {
  color: #606266;
}

.ai-report {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px 24px;
}

.ai-report :deep(h1),
.ai-report :deep(h2) {
  border-left: 4px solid #c0392b;
  padding-left: 10px;
  font-size: 17px;
  margin: 20px 0 10px;
}

.ai-report :deep(h3) {
  font-size: 15px;
  margin: 16px 0 8px;
}

.ai-report :deep(p) {
  margin: 8px 0;
  font-size: 14px;
  text-align: justify;
}

.ai-report :deep(ul) {
  padding-left: 22px;
  margin: 8px 0;
}

.ai-report :deep(li) {
  margin: 4px 0;
  font-size: 14px;
}

.ai-report :deep(strong) {
  color: #c0392b;
}

.ai-disclaimer {
  margin-top: 12px;
  padding: 10px 14px;
  background: #fef0f0;
  color: #a94442;
  border-radius: 8px;
  font-size: 12px;
}

:deep(.el-table) {
  cursor: pointer;
}

:deep(.el-table__row:hover > td) {
  background-color: #f0f7ff !important;
}
</style>
