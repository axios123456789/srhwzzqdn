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
 * 获取业绩走势echarts数据
 * @param params 查询参数 { fundCode, beginTime, endTime }
 * @returns
 */
export const GetNaveChartsByCondition = (params) => {
  return request({
    url: `${base_api}/getNaveChartsByCondition`,
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

/**
 * 添加基金净值数据
 * @param data 基金净值数据对象
 * @returns
 */
export const AddFundNav = (data) => {
  return request({
    url: `${base_api}/addFundNav`,
    method: 'post',
    data: data
  })
}

/**
 * 更新基金净值数据
 * @param data 基金净值数据对象
 * @returns
 */
export const UpdateFundNav = (data) => {
  return request({
    url: `${base_api}/updateFundNav`,
    method: 'post',
    data: data
  })
}

/**
 * 删除基金净值数据
 * @param id 净值数据ID
 * @returns
 */
export const DeleteFundNav = (id) => {
  return request({
    url: `${base_api}/deleteFundNav/${id}`,
    method: 'delete'
  })
}

/**
 * 添加基金交易数据
 * @param data 基金交易数据对象
 * @returns
 */
export const AddFundTransaction = (data) => {
  return request({
    url: `${base_api}/addFundTransaction`,
    method: 'post',
    data: data
  })
}

/**
 * 更新基金交易数据
 * @param data 基金交易数据对象
 * @returns
 */
export const UpdateFundTransaction = (data) => {
  return request({
    url: `${base_api}/updateFundTransaction`,
    method: 'post',
    data: data
  })
}

/**
 * 删除基金交易数据
 * @param id 交易数据ID
 * @returns
 */
export const DeleteFundTransaction = (id) => {
  return request({
    url: `${base_api}/deleteFundTransaction/${id}`,
    method: 'delete'
  })
}

/**
 * 添加基金分红数据
 * @param data 基金分红数据对象
 * @returns
 */
export const AddFundDividend = (data) => {
  return request({
    url: `${base_api}/addFundDividend`,
    method: 'post',
    data: data
  })
}

/**
 * 更新基金分红数据
 * @param data 基金分红数据对象
 * @returns
 */
export const UpdateFundDividend = (data) => {
  return request({
    url: `${base_api}/updateFundDividend`,
    method: 'post',
    data: data
  })
}

/**
 * 删除基金分红数据
 * @param id 分红数据ID
 * @returns
 */
export const DeleteFundDividend = (id) => {
  return request({
    url: `${base_api}/deleteFundDividend/${id}`,
    method: 'delete'
  })
}

/**
 * 添加基金风险绩效数据
 * @param data 基金风险绩效数据对象
 * @returns
 */
export const AddFundRiskPerformance = (data) => {
  return request({
    url: `${base_api}/addFundRiskPerformance`,
    method: 'post',
    data: data
  })
}

/**
 * 更新基金风险绩效数据
 * @param data 基金风险绩效数据对象
 * @returns
 */
export const UpdateFundRiskPerformance = (data) => {
  return request({
    url: `${base_api}/updateFundRiskPerformance`,
    method: 'post',
    data: data
  })
}

/**
 * 删除基金风险绩效数据
 * @param id 风险绩效数据ID
 * @returns
 */
export const DeleteFundRiskPerformance = (id) => {
  return request({
    url: `${base_api}/deleteFundRiskPerformance/${id}`,
    method: 'delete'
  })
}

/**
 * 添加基金持仓信息数据
 * @param data 基金持仓信息数据对象
 * @returns
 */
export const AddFundPortfolio = (data) => {
  return request({
    url: `${base_api}/addFundPortfolio`,
    method: 'post',
    data: data
  })
}

/**
 * 更新基金持仓信息数据
 * @param data 基金持仓信息数据对象
 * @returns
 */
export const UpdateFundPortfolio = (data) => {
  return request({
    url: `${base_api}/updateFundPortfolio`,
    method: 'post',
    data: data
  })
}

/**
 * 删除基金持仓信息数据
 * @param id 持仓信息数据ID
 * @returns
 */
export const DeleteFundPortfolio = (id) => {
  return request({
    url: `${base_api}/deleteFundPortfolio/${id}`,
    method: 'delete'
  })
}

/**
 * 根据基金代码删除基金的全部数据
 * @param fundCode 基金代码
 * @returns
 */
export const DeleteFundDataByCode = (fundCode) => {
  return request({
    url: `${base_api}/deleteFundDataByCode/${fundCode}`,
    method: 'delete'
  })
}

/**
 * 根据基金代码批量删除基金数据
 * @param fundCodes 基金代码数组
 * @returns
 */
export const DeleteFundDataByCodes = (fundCodes) => {
  return request({
    url: `${base_api}/deleteFundDataByCodes`,
    method: 'post',
    data: fundCodes
  })
}

/**
 * 获取基金重要数据 -> 基金持仓数据插入数据库，基金净值数据插入数据库
 * @param fundCode 基金代码
 * @returns
 */
export const AddFundImportData = (fundCode) => {
  return request({
    url: `${base_api}/addFundImportData/${fundCode}`,
    method: 'post',
    timeout: 300000 // 设置5分钟超时，因为接口需要获取外部数据，耗时较长
  })
}
