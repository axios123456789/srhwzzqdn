import request from "@/utils/request";

const base_api = '/superBrain/trialExecution/predictionSimulate'

// 条件分页查询预测模拟列表
export const GetPredictionByConditionAndPage = (current, limit, queryDto) => {
  return request({
    url: base_api + '/getPredictionByConditionAndPage/' + current + '/' + limit,
    method: 'post',
    data: queryDto,
  })
}

// 保存预测模拟记录
export const SavePrediction = data => {
  return request({
    url: base_api + '/savePrediction',
    method: 'post',
    data: data,
  })
}

// 根据id删除预测模拟记录
export const DeletePredictionById = id => {
  return request({
    url: base_api + '/deletePredictionById/' + id,
    method: 'delete',
  })
}

// 根据ids批量删除预测模拟记录
export const DeleteAllPredictionByIds = ids => {
  return request({
    url: base_api + '/deleteAllPredictionByIds',
    method: 'post',
    data: ids,
  })
}

// ====================模拟台账====================

// 查询模拟台账列表
export const GetSimulateLedgerList = () => {
  return request({
    url: base_api + '/getSimulateLedgerList',
    method: 'get',
  })
}

// 保存模拟台账
export const SaveSimulateLedger = data => {
  return request({
    url: base_api + '/saveSimulateLedger',
    method: 'post',
    data: data,
  })
}

// 根据id删除模拟台账
export const DeleteSimulateLedgerById = id => {
  return request({
    url: base_api + '/deleteSimulateLedgerById/' + id,
    method: 'delete',
  })
}

// 删除当前用户所有模拟台账
export const DeleteAllSimulateLedger = () => {
  return request({
    url: base_api + '/deleteAllSimulateLedger',
    method: 'delete',
  })
}

// ====================统计报表====================

// 获取预测统计报表
export const GetPredictionReport = data => {
  return request({
    url: base_api + '/getPredictionReport',
    method: 'post',
    data: data,
  })
}

// 穿透明细查询：根据报表条件+穿透维度分页查询预测记录明细
export const GetPredictionDetailByCondition = (current, limit, queryDto) => {
  return request({
    url: base_api + '/getPredictionDetailByCondition/' + current + '/' + limit,
    method: 'post',
    data: queryDto,
  })
}
