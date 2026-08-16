package com.xk.srhwzzqdn.manager.trialExecutionArea.controller;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.trialExecutionArea.service.DailyReviewService;
import com.xk.srhwzzqdn.model.dto.trialExecutionArea.DailyReviewDto;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.DailyReview;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/superBrain/trialExecution/dailyReview")
public class DailyReviewController {
    @Autowired
    private DailyReviewService dailyReviewService;

    /**
     * 条件分页查询每日复盘列表
     */
    @PostMapping("/getDailyReviewByConditionAndPage/{current}/{limit}")
    public Result getDailyReviewByConditionAndPage(@PathVariable("current") Integer current,
                                                    @PathVariable("limit") Integer limit,
                                                    @RequestBody DailyReviewDto dto) {
        PageInfo<DailyReview> pageInfo = dailyReviewService.getDailyReviewByConditionAndPage(current, limit, dto);
        return Result.build(pageInfo, ResultCodeEnum.SUCCESS);
    }

    /**
     * 保存每日复盘（同日期自动覆盖更新）
     */
    @PostMapping("/saveDailyReview")
    public Result saveDailyReview(@RequestBody DailyReview dailyReview) {
        try {
            dailyReviewService.saveDailyReview(dailyReview);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "保存每日复盘失败！");
        }
    }

    /**
     * 根据id删除每日复盘
     */
    @DeleteMapping("/deleteDailyReviewById/{id}")
    public Result deleteDailyReviewById(@PathVariable("id") Long id) {
        try {
            dailyReviewService.deleteDailyReviewById(id);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "删除每日复盘失败！");
        }
    }

    /**
     * 根据ids批量删除每日复盘
     */
    @PostMapping("/deleteAllDailyReviewByIds")
    public Result deleteAllDailyReviewByIds(@RequestBody List<Long> ids) {
        try {
            dailyReviewService.deleteAllDailyReviewByIds(ids);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "批量删除每日复盘失败！");
        }
    }
}