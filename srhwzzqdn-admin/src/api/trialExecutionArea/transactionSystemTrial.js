import request from "@/utils/request";

const base_api = '/superBrain/trialExecution/transactionSystemTrial'

export const GetTransactionSystemTrialByConditionAndPage = (current, limit, queryDto) => {
  return request({
    url: base_api + '/getTransactionSystemTrialByConditionAndPage/' + current + '/' + limit,
    method: 'post',
    data: queryDto,
  })
}

export const SaveTransactionSystemTrial = data => {
  return request({
    url: base_api + '/saveTransactionSystemTrial',
    method: 'post',
    data: data,
  })
}

export const DeleteTransactionSystemTrialById = id => {
  return request({
    url: base_api + '/deleteTransactionSystemTrialById/' + id,
    method: 'delete',
  })
}

export const DeleteAllTransactionSystemTrialByIds = ids => {
  return request({
    url: base_api + '/deleteAllTransactionSystemTrialByIds',
    method: 'post',
    data: ids,
  })
}

// ====================交易规则====================
export const GetTransactionRuleList = () => {
  return request({
    url: base_api + '/getTransactionRuleList',
    method: 'get',
  })
}

export const SaveTransactionRule = data => {
  return request({
    url: base_api + '/saveTransactionRule',
    method: 'post',
    data: data,
  })
}

export const DeleteTransactionRuleById = id => {
  return request({
    url: base_api + '/deleteTransactionRuleById/' + id,
    method: 'delete',
  })
}

export const DeleteAllTransactionRuleByIds = ids => {
  return request({
    url: base_api + '/deleteAllTransactionRuleByIds',
    method: 'post',
    data: ids,
  })
}