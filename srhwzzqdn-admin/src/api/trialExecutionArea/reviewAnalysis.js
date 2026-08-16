import request from "@/utils/request";

const base_api = '/superBrain/trialExecution/reviewAnalysis'

// 获取复盘分析报表（融合每日复盘+交易记录两表数据）
export const GetReviewReport = data => {
  return request({
    url: base_api + '/getReviewReport',
    method: 'post',
    data: data,
  })
}