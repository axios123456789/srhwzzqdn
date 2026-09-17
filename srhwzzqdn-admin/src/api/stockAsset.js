import request from "@/utils/request";

const base_api = '/superBrain/assetControl/stockAsset';

export const GetStockAllDataByCode = (stockCode) => {
  return request({
    url: `${base_api}/getStockAllDataByCode/${stockCode}`,
    method: 'get',
    timeout: 900000
  });
};

export const RefreshAllStockRealtime = () => {
  return request({
    url: `${base_api}/refreshAllStockRealtime`,
    method: 'get',
    timeout: 900000
  });
};

export const GetStockBasicByCode = (stockCode) => {
  return request({ url: `${base_api}/getStockBasicByCode/${stockCode}`, method: 'get' });
};

export const GetStockListByCondition = (current, limit, data) => {
  return request({ url: `${base_api}/getStockListByCondition/${current}/${limit}`, method: 'post', data });
};

export const GetStockKline = (stockCode, klineType, limit) => {
  return request({ url: `${base_api}/getStockKline/${stockCode}/${klineType}/${limit}`, method: 'get' });
};

export const GetStockFinance = (stockCode, limit) => {
  return request({ url: `${base_api}/getStockFinance/${stockCode}/${limit}`, method: 'get' });
};

export const GetStockHolderNum = (stockCode, limit) => {
  return request({ url: `${base_api}/getStockHolderNum/${stockCode}/${limit}`, method: 'get' });
};

export const GetStockCapitalFlow = (stockCode, limit) => {
  return request({ url: `${base_api}/getStockCapitalFlow/${stockCode}/${limit}`, method: 'get' });
};

export const GetStockNews = (stockCode, limit) => {
  return request({ url: `${base_api}/getStockNews/${stockCode}/${limit}`, method: 'get' });
};

export const RefreshStockNews = (stockCode) => {
  return request({ url: `${base_api}/refreshStockNews/${stockCode}`, method: 'get', timeout: 120000 });
};

export const RefreshStockRealtime = (stockCode) => {
  return request({ url: `${base_api}/refreshStockRealtime/${stockCode}`, method: 'get', timeout: 120000 });
};

export const AnalyzeStock = (stockCode) => {
  // AI 综合分析耗时较长，单独放宽超时；与后端 ai.common.read-timeout(900000) 对齐，保证前端不会先于后端超时
  return request({ url: `${base_api}/analyzeStock/${stockCode}`, method: 'get', timeout: 900000 });
};

export const GetStockTrend = (stockCode, tradeDate) => {
  return request({ url: `${base_api}/getStockTrend/${stockCode}/${tradeDate}`, method: 'get', timeout: 120000 });
};

export const GetChipAnalysis = (stockCode) => {
  // 纯后端算法分析（不调AI），短超时即可
  return request({ url: `${base_api}/getChipAnalysis/${stockCode}`, method: 'get', timeout: 30000 });
};

export const GetFundamentalStocks = () => {
  // 基本面选股：全市场快照实时筛选（不入库），放宽超时以应对东方财富接口偶发波动重试
  return request({ url: `${base_api}/getFundamentalStocks`, method: 'get', timeout: 60000 });
};

export const UpdateStockBasic = (data) => {
  return request({ url: `${base_api}/updateStockBasic`, method: 'put', data });
};

export const DeleteStockDataByCode = (stockCode) => {
  return request({ url: `${base_api}/deleteStockDataByCode/${stockCode}`, method: 'delete' });
};

// ==================== 市场分析（市场分析标签页） ====================
const marketApi = '/superBrain/assetControl/marketAnalysis';

export const GetRealtimeAnalysis = () => {
  // 实时聚合：后端需串行请求约10个东财接口（含重试与限速间隔），放宽超时
  return request({ url: `${marketApi}/getRealtimeAnalysis`, method: 'get', timeout: 90000 });
};

export const AnalyzeMarketWithAi = () => {
  // AI深度分析耗时较长：与后端 ai.common.read-timeout(900000) 对齐
  return request({ url: `${marketApi}/analyzeWithAi`, method: 'get', timeout: 900000 });
};

export const GetTodayMarketAnalysis = () => {
  return request({ url: `${marketApi}/getTodayAnalysis`, method: 'get', timeout: 30000 });
};

export const GetMarketHistoryList = (limit) => {
  return request({ url: `${marketApi}/getHistoryList/${limit}`, method: 'get', timeout: 30000 });
};

export const GetMarketHistoryReview = () => {
  // AI历史复盘调用大模型，放宽超时
  return request({ url: `${marketApi}/getHistoryReview`, method: 'get', timeout: 300000 });
};