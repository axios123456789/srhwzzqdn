package com.xk.srhwzzqdn.manager.trialExecutionArea.service;

import com.xk.srhwzzqdn.model.dto.trialExecutionArea.ReviewReportDto;
import com.xk.srhwzzqdn.model.vo.trialExecutionArea.ReviewReportVo;

public interface ReviewReportService {
    //获取复盘分析报表
    ReviewReportVo getReviewReport(ReviewReportDto dto);
}