package com.xk.srhwzzqdn.manager.assetControlArea.controller;

import com.xk.srhwzzqdn.manager.assetControlArea.service.ShortTermPickService;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 短线选股（纯新增功能，不影响原有选股/研判）：
 * 三类精选（妖股梯队/低位潜伏/趋势延续）各top10 + 行为判定 + 时段归库 + 经验闭环
 */
@RestController
@RequestMapping("/superBrain/assetControl/shortTerm")
public class ShortTermPickController {
    @Autowired
    private ShortTermPickService shortTermPickService;

    private static final Logger logger = LoggerFactory.getLogger(ShortTermPickController.class);

    /**
     * 短线精选推荐：候选池收敛漏斗 + 行为判定 + 三类top10 + 时段归库（非交易日归上交易日），
     * 含池/快照/K线采集约1分钟内完成，正常结果缓存30分钟，降级结果缓存5分钟
     */
    @GetMapping("/getShortTermStocks")
    public Result getShortTermStocks() {
        try {
            Map<String, Object> result = shortTermPickService.getShortTermStocks();
            return Result.build(result, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("短线选股失败", e);
            return Result.build(null, 500, "短线选股失败：" + e.getMessage());
        }
    }

    /**
     * 刷新精选（按钮专用）：不管什么时段直接实时采集+先删后入库，不走缓存/不查库直读
     */
    @GetMapping("/refreshShortTermStocks")
    public Result refreshShortTermStocks() {
        try {
            Map<String, Object> result = shortTermPickService.refreshShortTermStocks();
            return Result.build(result, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("刷新精选失败", e);
            return Result.build(null, 500, "刷新精选失败：" + e.getMessage());
        }
    }

    /**
     * 复盘经验列表（AI数据记忆）
     */
    @GetMapping("/getExperience/{limit}")
    public Result getExperience(@PathVariable Integer limit) {
        try {
            Map<String, Object> result = shortTermPickService.getExperience(limit);
            return Result.build(result, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("查询短线复盘经验失败", e);
            return Result.build(null, 500, "查询复盘经验失败：" + e.getMessage());
        }
    }

    /**
     * 手动触发复盘：对3~10天前尚未复盘的推荐拉K线验证（T+3/T+5），
     * 事实归类后AI总结经验入库（AI失败时规则模板兜底）
     */
    @GetMapping("/review")
    public Result review() {
        try {
            Map<String, Object> result = shortTermPickService.reviewShortTermPicks();
            return Result.build(result, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("短线复盘失败", e);
            return Result.build(null, 500, "短线复盘失败：" + e.getMessage());
        }
    }
}
