package com.xk.srhwzzqdn.manager.assetControlArea.mapper;

import com.xk.srhwzzqdn.model.entity.assetControl.ShortTermExperience;
import com.xk.srhwzzqdn.model.entity.assetControl.ShortTermPickDaily;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface ShortTermPickMapper {

    /**
     * 删除指定交易日的全部推荐记录（重算当天先删后插；仅限当天，不触碰其他交易日）
     */
    void deleteByTradeDate(@Param("tradeDate") Date tradeDate);

    /**
     * 批量插入当日推荐记录
     */
    void batchInsertPicks(@Param("list") List<ShortTermPickDaily> list);

    /**
     * 查询指定交易日的推荐记录（当天同一时刻只存在一个phase版本，先删后插保证）
     */
    List<ShortTermPickDaily> selectByTradeDate(@Param("tradeDate") Date tradeDate);

    /**
     * 查询待复盘推荐：trade_date 在 [minDate, maxDate] 区间且尚未生成过经验的记录
     */
    List<ShortTermPickDaily> selectPicksForReview(@Param("minDate") Date minDate,
                                                  @Param("maxDate") Date maxDate);

    /**
     * 插入一条复盘经验
     */
    void insertExperience(ShortTermExperience experience);

    /**
     * 查询最近的复盘经验列表（按经验日期倒序）
     */
    List<ShortTermExperience> selectRecentExperience(@Param("limit") Integer limit);

    /**
     * 查询与指定推荐列表相关的全部经验（复盘AI输入）
     */
    List<ShortTermExperience> selectExperienceByDates(@Param("dates") List<Date> dates);

    /**
     * 按代码批量查询库内行业/概念（东财失败时新浪/腾讯兜底源无此字段，从t_stock_basic补齐）
     */
    List<java.util.Map<String, Object>> selectIndustryByCodes(@Param("codes") List<String> codes);

    /**
     * 批量补库行业/概念（东财正常时把当轮见到的代码/名称/行业/概念增量写入t_stock_basic，
     * 供东财封禁期fillIndustryFromDb兜底读取；存在则仅更新非空字段，不覆盖已有公司档案字段）
     */
    void upsertBasicIndustry(@Param("list") List<java.util.Map<String, Object>> list);
}
