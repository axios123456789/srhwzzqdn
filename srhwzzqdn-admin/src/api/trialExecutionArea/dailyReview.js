import request from "@/utils/request";

const base_api = '/superBrain/trialExecution/dailyReview'

// 条件分页查询每日复盘列表
export const GetDailyReviewByConditionAndPage = (current, limit, queryDto) => {
  return request({
    url: base_api + '/getDailyReviewByConditionAndPage/' + current + '/' + limit,
    method: 'post',
    data: queryDto,
  })
}

// 保存每日复盘（同日期自动覆盖更新）
export const SaveDailyReview = data => {
  return request({
    url: base_api + '/saveDailyReview',
    method: 'post',
    data: data,
  })
}

// 根据id删除每日复盘
export const DeleteDailyReviewById = id => {
  return request({
    url: base_api + '/deleteDailyReviewById/' + id,
    method: 'delete',
  })
}

// 根据ids批量删除每日复盘
export const DeleteAllDailyReviewByIds = ids => {
  return request({
    url: base_api + '/deleteAllDailyReviewByIds',
    method: 'post',
    data: ids,
  })
}

// AI生成复盘总结
export const AiGenerateDailyReview = data => {
  return request({
    url: base_api + '/aiGenerate',
    method: 'post',
    data: data,
    timeout: 900000,
  })
}

// AI分析关注标的
export const AiAnalyzeTargets = data => {
  return request({
    url: base_api + '/aiAnalyzeTargets',
    method: 'post',
    data: data,
    timeout: 900000,
  })
}

// 获取大盘市场数据（3大指数+涨跌家数统计）
// 传入date（yyyy-MM-dd）则获取该日期对应交易日的数据，不传获取当天实时数据
export const FetchRealtimeMarketData = date => {
  const query = date ? '?date=' + date : ''
  return request({
    url: base_api + '/fetchRealtimeMarketData' + query,
    method: 'get',
  })
}