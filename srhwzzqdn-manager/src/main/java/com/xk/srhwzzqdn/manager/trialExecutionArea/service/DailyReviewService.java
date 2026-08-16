package com.xk.srhwzzqdn.manager.trialExecutionArea.service;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.model.dto.trialExecutionArea.DailyReviewDto;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.DailyReview;

import java.util.List;

public interface DailyReviewService {
    //条件分页查询每日复盘列表
    PageInfo<DailyReview> getDailyReviewByConditionAndPage(Integer current, Integer limit, DailyReviewDto dto);

    //保存每日复盘（含同日期覆盖逻辑）
    void saveDailyReview(DailyReview dailyReview);

    //根据id删除每日复盘
    void deleteDailyReviewById(Long id);

    //根据ids批量删除每日复盘
    void deleteAllDailyReviewByIds(List<Long> ids);
}