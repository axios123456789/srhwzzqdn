package com.xk.srhwzzqdn.manager.trialExecutionArea.controller;

import com.xk.srhwzzqdn.manager.trialExecutionArea.service.ReviewReportService;
import com.xk.srhwzzqdn.model.dto.trialExecutionArea.ReviewReportDto;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import com.xk.srhwzzqdn.model.vo.trialExecutionArea.ReviewReportVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/superBrain/trialExecution/reviewAnalysis")
public class ReviewReportController {
    @Autowired
    private ReviewReportService reviewReportService;

    /**
     * 获取复盘分析报表（融合每日复盘+交易记录两表数据）
     */
    @PostMapping("/getReviewReport")
    public Result getReviewReport(@RequestBody ReviewReportDto dto) {
        ReviewReportVo vo = reviewReportService.getReviewReport(dto);
        return Result.build(vo, ResultCodeEnum.SUCCESS);
    }
}