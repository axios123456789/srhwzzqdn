import request from "@/utils/request";

const base_api = '/superBrain/assetControl/fundAsset' //基金资产管理基本路径

/**
 * 获取基金基本数据，基金经理基本数据，关联持仓数据获取
 * @param fundCode 基金代码
 * @returns 
 */
export const GetFundBaseDataByCode = (fundCode) => {
  return request({
    url: `${base_api}/getFundBaseDataByCode/${fundCode}`,
    method: 'get',
    timeout: 300000 // 设置5分钟超时，因为接口需要AI分析，耗时较长
  })
}

/**
 * 条件分页查询基金基本数据
 * @param current 当前页
 * @param limit 每页条数
 * @param data 查询条件对象
 * @returns
 */
export const GetFundBaseDataByConditionAndPage = (current, limit, data) => {
  return request({
    url: `${base_api}/getFundBaseDataByConditionAndPage/${current}/${limit}`,
    method: 'post',
    data: data
  })
}

/**
 * 条件分页查询基金净值数据
 * @param current 当前页
 * @param limit 每页条数
 * @param params 查询参数 { fundCode, beginTime, endTime }
 * @returns
 */
export const GetFundNavByConditionAndPage = (current, limit, params) => {
  return request({
    url: `${base_api}/getFundNavByConditionAndPage/${current}/${limit}`,
    method: 'get',
    params: params
  })
}

/**
 * 根据基金代码获取基金经理分析数据
 * @param fundCode 基金代码
 * @returns
 */
export const GetFundManagerAnalysisByCode = (fundCode) => {
  return request({
    url: `${base_api}/getFundManagerAnalysisByCode`,
    method: 'get',
    params: { fundCode }
  })
}

/**
 * 根据基金代码获取基金持仓数据
 * @param fundCode 基金代码
 * @returns
 */
export const GetFundHoldingByCode = (fundCode) => {
  return request({
    url: `${base_api}/getFundHoldingByCode`,
    method: 'get',
    params: { fundCode }
  })
}

/**
 * 条件分页查询基金交易与流水数据
 * @param current 当前页
 * @param limit 每页条数
 * @param params 查询参数 { fundCode, beginTime, endTime }
 * @returns
 */
export const GetFundTransactionByConditionAndPage = (current, limit, params) => {
  return request({
    url: `${base_api}/getFundTransactionByConditionAndPage/${current}/${limit}`,
    method: 'get',
    params: params
  })
}

/**
 * 条件分页查询基金分红数据
 * @param current 当前页
 * @param limit 每页条数
 * @param params 查询参数 { fundCode, beginTime, endTime }
 * @returns
 */
export const GetFundDividendByConditionAndPage = (current, limit, params) => {
  return request({
    url: `${base_api}/getFundDividendByConditionAndPage/${current}/${limit}`,
    method: 'get',
    params: params
  })
}

/**
 * 根据基金代码获取基金风险收益数据
 * @param fundCode 基金代码
 * @returns
 */
export const GetFundRiskPerformanceByCode = (fundCode) => {
  return request({
    url: `${base_api}/getFundRiskPerformanceByCode`,
    method: 'get',
    params: { fundCode }
  })
}

/**
 * 条件分页查询基金持仓数据
 * @param current 当前页
 * @param limit 每页条数
 * @param params 查询参数 { fundCode, beginTime, endTime }
 * @returns
 */
export const GetFundPortfolioByConditionAndPage = (current, limit, params) => {
  return request({
    url: `${base_api}/getFundPortfolioByConditionAndPage/${current}/${limit}`,
    method: 'get',
    params: params
  })
}

/**
 * 更新基金基本数据
 * @param data 基金基本数据对象
 * @returns
 */
export const UpdateFundBaseAsset = (data) => {
  return request({
    url: `${base_api}/updateFundBaseAsset`,
    method: 'post',
    data: data
  })
}

/**
 * 更新基金经理分析数据
 * @param data 基金经理分析数据对象
 * @returns
 */
export const UpdateFundManagerAnalysis = (data) => {
  return request({
    url: `${base_api}/updateFundManagerAnalysis`,
    method: 'post',
    data: data
  })
}

/**
 * 更新基金持仓数据
 * @param data 基金持仓数据对象
 * @returns
 */
export const UpdateFundHolding = (data) => {
  return request({
    url: `${base_api}/updateFundHolding`,
    method: 'post',
    data: data
  })
}
