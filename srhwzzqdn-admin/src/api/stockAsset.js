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
  // AI 综合分析耗时较长，单独放宽超时
  return request({ url: `${base_api}/analyzeStock/${stockCode}`, method: 'get', timeout: 300000 });
};

export const UpdateStockBasic = (data) => {
  return request({ url: `${base_api}/updateStockBasic`, method: 'put', data });
};

export const DeleteStockDataByCode = (stockCode) => {
  return request({ url: `${base_api}/deleteStockDataByCode/${stockCode}`, method: 'delete' });
};