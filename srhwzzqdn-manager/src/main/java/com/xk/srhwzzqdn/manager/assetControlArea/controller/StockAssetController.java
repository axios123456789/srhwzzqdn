package com.xk.srhwzzqdn.manager.assetControlArea.controller;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.assetControlArea.service.StockAssetService;
import com.xk.srhwzzqdn.model.dto.assetControl.StockQueryDto;
import com.xk.srhwzzqdn.model.entity.assetControl.*;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/superBrain/assetControl/stockAsset")
public class StockAssetController {
    @Autowired
    private StockAssetService stockAssetService;

    private static final Logger logger = LoggerFactory.getLogger(StockAssetController.class);

    @GetMapping("/getStockAllDataByCode/{stockCode}")
    public Result getStockAllDataByCode(@PathVariable("stockCode") String stockCode) {
        try {
            String result = stockAssetService.getStockAllDataByCode(stockCode);
            if (result != null && result.contains("数据已存在")) {
                return Result.build(null, 400, "该股票数据已存在，请勿重复获取！");
            }
            if (result != null && result.contains("未找到")) {
                return Result.build(null, 404, "未找到该股票代码对应的数据，请检查代码是否正确！");
            }
            return Result.build(result, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("获取股票数据失败", e);
            return Result.build(null, 500, "获取股票数据失败：" + e.getMessage());
        }
    }

    @GetMapping("/refreshAllStockRealtime")
    public Result refreshAllStockRealtime() {
        try {
            String result = stockAssetService.refreshAllStockRealtime();
            return Result.build(result, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("批量刷新实时数据失败", e);
            return Result.build(null, 500, "刷新实时数据失败：" + e.getMessage());
        }
    }

    @GetMapping("/getStockBasicByCode/{stockCode}")
    public Result getStockBasicByCode(@PathVariable("stockCode") String stockCode) {
        try {
            StockBasic stockBasic = stockAssetService.getStockBasicByCode(stockCode);
            return Result.build(stockBasic, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("查询股票基本数据失败", e);
            return Result.build(null, 500, "查询失败");
        }
    }

    @PostMapping("/getStockListByCondition/{current}/{limit}")
    public Result getStockListByCondition(@PathVariable Integer current, @PathVariable Integer limit,
                                          @RequestBody StockQueryDto dto) {
        try {
            PageInfo<StockBasic> pageInfo = stockAssetService.getStockListByCondition(current, limit, dto);
            return Result.build(pageInfo, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("分页查询股票列表失败", e);
            return Result.build(null, 500, "查询失败");
        }
    }

    @GetMapping("/getStockKline/{stockCode}/{klineType}/{limit}")
    public Result getStockKline(@PathVariable String stockCode, @PathVariable Integer klineType, @PathVariable Integer limit) {
        try {
            List<StockKline> list = stockAssetService.getStockKline(stockCode, klineType, limit);
            return Result.build(list, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("获取K线数据失败", e);
            return Result.build(null, 500, "获取K线数据失败");
        }
    }

    @GetMapping("/getStockFinance/{stockCode}/{limit}")
    public Result getStockFinance(@PathVariable String stockCode, @PathVariable Integer limit) {
        try {
            List<StockFinance> list = stockAssetService.getStockFinance(stockCode, limit);
            return Result.build(list, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("获取财务数据失败", e);
            return Result.build(null, 500, "获取财务数据失败");
        }
    }

    @GetMapping("/getStockCapitalFlow/{stockCode}/{limit}")
    public Result getStockCapitalFlow(@PathVariable String stockCode, @PathVariable Integer limit) {
        try {
            List<StockCapitalFlow> list = stockAssetService.getStockCapitalFlow(stockCode, limit);
            return Result.build(list, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("获取资金流向数据失败", e);
            return Result.build(null, 500, "获取资金流向数据失败");
        }
    }

    @GetMapping("/getStockNews/{stockCode}/{limit}")
    public Result getStockNews(@PathVariable String stockCode, @PathVariable Integer limit) {
        try {
            List<StockNews> list = stockAssetService.getStockNews(stockCode, limit);
            return Result.build(list, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("获取消息面数据失败", e);
            return Result.build(null, 500, "获取消息面数据失败");
        }
    }

    @GetMapping("/getStockHolderNum/{stockCode}/{limit}")
    public Result getStockHolderNum(@PathVariable String stockCode, @PathVariable Integer limit) {
        try {
            List<StockHolderNum> list = stockAssetService.getStockHolderNum(stockCode, limit);
            return Result.build(list, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("获取股东人数数据失败", e);
            return Result.build(null, 500, "获取股东人数数据失败");
        }
    }

    @GetMapping("/refreshStockNews/{stockCode}")
    public Result refreshStockNews(@PathVariable String stockCode) {
        try {
            String result = stockAssetService.refreshStockNews(stockCode);
            return Result.build(result, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("刷新消息面数据失败", e);
            return Result.build(null, 500, "刷新消息面数据失败：" + e.getMessage());
        }
    }

    /**
     * 单只股票实时刷新：行情估值/K线/资金流/财务/消息
     */
    @GetMapping("/refreshStockRealtime/{stockCode}")
    public Result refreshStockRealtime(@PathVariable String stockCode) {
        try {
            String result = stockAssetService.refreshStockRealtime(stockCode);
            return Result.build(result, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("单只股票实时刷新失败", e);
            return Result.build(null, 500, "单只股票实时刷新失败：" + e.getMessage());
        }
    }

    /**
     * AI 综合分析：规则评分 + AI 多维度分析（基本面/技术面/资金面/消息面/板块联动）
     */
    @GetMapping("/analyzeStock/{stockCode}")
    public Result analyzeStock(@PathVariable String stockCode) {
        try {
            Map<String, Object> result = stockAssetService.analyzeStock(stockCode);
            return Result.build(result, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("AI综合分析失败", e);
            return Result.build(null, 500, "AI综合分析失败：" + e.getMessage());
        }
    }

    @PutMapping("/updateStockBasic")
    public Result updateStockBasic(@RequestBody StockBasic stockBasic) {
        try {
            stockAssetService.updateStockBasic(stockBasic);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("更新股票数据失败", e);
            return Result.build(null, 500, "更新失败");
        }
    }

    @DeleteMapping("/deleteStockDataByCode/{stockCode}")
    public Result deleteStockDataByCode(@PathVariable String stockCode) {
        try {
            stockAssetService.deleteStockDataByCode(stockCode);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("删除股票数据失败", e);
            return Result.build(null, 500, "删除失败");
        }
    }
}