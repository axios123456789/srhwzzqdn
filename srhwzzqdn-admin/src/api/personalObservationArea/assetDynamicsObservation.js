import request from "@/utils/request";

const base_api = '/superBrain/personalObservation/assetDynamicsObservation'

// 报表1：资产类型分组统计
export const GetAssetTypeStatistics = () => {
  return request({
    url: `${base_api}/getAssetTypeStatistics`,
    method: 'get',
  })
}

// 报表2：基金子类分析
export const GetFundSubTypeAnalysis = () => {
  return request({
    url: `${base_api}/getFundSubTypeAnalysis`,
    method: 'get',
  })
}

// 报表3：股票子类分析
export const GetStockSubTypeAnalysis = () => {
  return request({
    url: `${base_api}/getStockSubTypeAnalysis`,
    method: 'get',
  })
}

// 穿透明细：按资产类型查询（分页）
export const GetAssetDetailByType = (assetType, page, limit) => {
  return request({
    url: `${base_api}/getAssetDetailByType`,
    method: 'get',
    params: { assetType, page, limit }
  })
}

// 穿透明细：按子类+清仓状态查询（分页）
export const GetAssetDetailBySubTypeAndStatus = (assetType, assetSubType, isCleared, page, limit) => {
  return request({
    url: `${base_api}/getAssetDetailBySubTypeAndStatus`,
    method: 'get',
    params: { assetType, assetSubType, isCleared, page, limit }
  })
}

// 图表：资产类型金额扇形图
export const GetAssetTypeDistribution = () => {
  return request({
    url: `${base_api}/getAssetTypeDistribution`,
    method: 'get',
  })
}

// 图表：指定资产类型的子类金额扇形图
export const GetSubTypeDistribution = (assetType) => {
  return request({
    url: `${base_api}/getSubTypeDistribution`,
    method: 'get',
    params: { assetType }
  })
}

// 图表：已清仓资产收益条形图
export const GetClearedAssetProfit = () => {
  return request({
    url: `${base_api}/getClearedAssetProfit`,
    method: 'get',
  })
}
