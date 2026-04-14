import request from "@/utils/request";

const base_api = '/superBrain/assetControl/assetClusteringAnalysis' //资产聚类分析基本路径

// 获取资产结构数据（总资产、储蓄资产、投资资产）
export const GetAssetStructure = () => {
  return request({
    url: `${base_api}/getAssetStructure`,
    method: 'get',
  })
}

// 获取资产类型分组数据
export const GetAssetTypeGroup = () => {
  return request({
    url: `${base_api}/getAssetTypeGroup`,
    method: 'get',
  })
}

// 获取资产状态分布分组数据
export const GetAssetStatusGroup = () => {
  return request({
    url: `${base_api}/getAssetStatusGroup`,
    method: 'get',
  })
}

// 获取资产金额排行数据
export const GetAssetAmountRank = () => {
  return request({
    url: `${base_api}/getAssetAmountRank`,
    method: 'get',
  })
}

// 获取投资收益分析数据
export const GetInvestmentReturnAnalysis = () => {
  return request({
    url: `${base_api}/getInvestmentReturnAnalysis`,
    method: 'get',
  })
}

// 获取收支金额分组数据
export const GetTransactionAmountGroup = (beginTime, endTime) => {
  return request({
    url: `${base_api}/getTransactionAmountGroup/${beginTime}/${endTime}`,
    method: 'get',
  })
}

// 获取支出结构分组数据
export const GetExpenseStructureGroup = (beginTime, endTime) => {
  return request({
    url: `${base_api}/expenseStructureGroup/${beginTime}/${endTime}`,
    method: 'get',
  })
}

// 获取支出类型分组数据
export const GetSpendingTypeGroup = (beginTime, endTime) => {
  return request({
    url: `${base_api}/getSpendingTypeGroup/${beginTime}/${endTime}`,
    method: 'get',
  })
}

// 获取收入来源分组数据
export const GetIncomeSourceGroup = (beginTime, endTime) => {
  return request({
    url: `${base_api}/getIncomeSourceGroup/${beginTime}/${endTime}`,
    method: 'get',
  })
}
