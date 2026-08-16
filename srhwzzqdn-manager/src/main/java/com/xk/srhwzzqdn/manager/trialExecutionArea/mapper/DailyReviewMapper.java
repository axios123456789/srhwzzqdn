package com.xk.srhwzzqdn.manager.trialExecutionArea.mapper;

import com.xk.srhwzzqdn.model.dto.trialExecutionArea.DailyReviewDto;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.DailyReview;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DailyReviewMapper {
    //条件查询每日复盘列表
    List<DailyReview> getDailyReviewByCondition(DailyReviewDto dto);

    //添加每日复盘
    void addDailyReview(DailyReview dailyReview);

    //修改每日复盘
    void updateDailyReview(DailyReview dailyReview);

    //根据id删除每日复盘
    @Delete("delete from t_trial_daily_review where id = #{param1}")
    void deleteDailyReviewById(Long id);

    //根据ids批量删除每日复盘
    void deleteAllDailyReviewByIds(List<Long> ids);

    //根据id查询每日复盘
    @Select("select * from t_trial_daily_review where id = #{param1}")
    DailyReview getDailyReviewById(Long id);

    //根据owner和复盘日期查询（用于同日期覆盖更新判断）
    @Select("select * from t_trial_daily_review where owner = #{param1} and review_date = #{param2} limit 1")
    DailyReview getDailyReviewByOwnerAndDate(String owner, String reviewDate);
}