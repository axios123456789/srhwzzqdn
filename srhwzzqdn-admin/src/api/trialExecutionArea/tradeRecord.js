import request from "@/utils/request";

const base_api = '/superBrain/trialExecution/tradeRecord'

// 条件分页查询交易记录列表
export const GetTradeRecordByConditionAndPage = (current, limit, queryDto) => {
  return request({
    url: base_api + '/getTradeRecordByConditionAndPage/' + current + '/' + limit,
    method: 'post',
    data: queryDto,
  })
}

// 保存交易记录
export const SaveTradeRecord = data => {
  return request({
    url: base_api + '/saveTradeRecord',
    method: 'post',
    data: data,
  })
}

// 根据id删除交易记录
export const DeleteTradeRecordById = id => {
  return request({
    url: base_api + '/deleteTradeRecordById/' + id,
    method: 'delete',
  })
}

// 根据ids批量删除交易记录
export const DeleteAllTradeRecordByIds = ids => {
  return request({
    url: base_api + '/deleteAllTradeRecordByIds',
    method: 'post',
    data: ids,
  })
}

// 根据复盘日期汇总当天交易数据
export const StatTradeByReviewDate = reviewDate => {
  return request({
    url: base_api + '/statTradeByReviewDate/' + reviewDate,
    method: 'get',
  })
}