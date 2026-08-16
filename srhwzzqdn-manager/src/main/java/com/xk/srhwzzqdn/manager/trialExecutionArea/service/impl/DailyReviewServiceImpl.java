package com.xk.srhwzzqdn.manager.trialExecutionArea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.trialExecutionArea.mapper.DailyReviewMapper;
import com.xk.srhwzzqdn.manager.trialExecutionArea.service.DailyReviewService;
import com.xk.srhwzzqdn.model.dto.trialExecutionArea.DailyReviewDto;
import com.xk.srhwzzqdn.model.entity.trialExecutionArea.DailyReview;
import com.xk.srhwzzqdn.util.AuthContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class DailyReviewServiceImpl implements DailyReviewService {
    @Autowired
    private DailyReviewMapper dailyReviewMapper;

    /**
     * 条件分页查询每日复盘列表
     */
    @Override
    public PageInfo<DailyReview> getDailyReviewByConditionAndPage(Integer current, Integer limit, DailyReviewDto dto) {
        PageHelper.startPage(current, limit);
        dto.setOwner(AuthContextUtil.get().getId());
        List<DailyReview> list = dailyReviewMapper.getDailyReviewByCondition(dto);
        return new PageInfo<>(list);
    }

    /**
     * 保存每日复盘（含同日期覆盖逻辑）
     * 同一用户同一日期只能有一条复盘，重复提交自动转为更新
     */
    @Override
    public void saveDailyReview(DailyReview dailyReview) {
        String owner = AuthContextUtil.get().getId();
        dailyReview.setOwner(owner);
        if (dailyReview.getId() == null) {
            //新增：检查同日期是否已有记录
            String dateStr = formatDate(dailyReview.getReviewDate());
            DailyReview exist = dailyReviewMapper.getDailyReviewByOwnerAndDate(owner, dateStr);
            if (exist != null) {
                //同日期已存在，走更新
                dailyReview.setId(exist.getId());
                dailyReviewMapper.updateDailyReview(dailyReview);
            } else {
                dailyReviewMapper.addDailyReview(dailyReview);
            }
        } else {
            dailyReviewMapper.updateDailyReview(dailyReview);
        }
    }

    /**
     * 根据id删除每日复盘
     */
    @Override
    public void deleteDailyReviewById(Long id) {
        dailyReviewMapper.deleteDailyReviewById(id);
    }

    /**
     * 根据ids批量删除每日复盘
     */
    @Override
    public void deleteAllDailyReviewByIds(List<Long> ids) {
        dailyReviewMapper.deleteAllDailyReviewByIds(ids);
    }

    /**
     * Date转yyyy-MM-dd字符串
     */
    private String formatDate(Date date) {
        if (date == null) return null;
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
}