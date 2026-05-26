package com.xk.srhwzzqdn.manager.assetControlArea.controller;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.assetControlArea.service.FundAssetService;
import com.xk.srhwzzqdn.model.dto.assetControl.FundBaseDateDto;
import com.xk.srhwzzqdn.model.dto.assetControl.FundComm;
import com.xk.srhwzzqdn.model.entity.assetControl.*;
import com.xk.srhwzzqdn.model.vo.assetControl.NavEChartsVo;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import org.apache.ibatis.annotations.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/superBrain/assetControl/fundAsset")
public class FundAssetController {
    @Autowired
    private FundAssetService fundAssetService;

    private static final Logger logger = LoggerFactory.getLogger(FundAssetController.class);

    /**
     * 获取基金测试数据（控制台打印）（该仅测试接口数据是否正常返回，不参与业务）
     * @param fundCode
     * @return
     */
    @RequestMapping("/getFundTestData/{fundCode}")
    public Result getFundTestData(@PathVariable("fundCode") String fundCode) {
        try {
            fundAssetService.getFundTestData(fundCode);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("获取资产金额排名数据失败", e);
            return Result.build(null, 500, "获取基金基本数据失败！");
        }
    }

    /**
     * 获取基金基本数据，基金经理基本数据，关联持仓数据获取
     * 前端基金资产管理 -> 首页 -> 数据获取按钮点击 -> 调用该接口
     * @param fundCode
     * @return
     */
    @RequestMapping("/getFundBaseDataByCode/{fundCode}")
    public Result getFundBaseDataByCode(@PathVariable("fundCode") String fundCode) {
        try {
            String fundData = fundAssetService.getFundBaseDataByCode(fundCode);
            
            // 判断返回的特殊消息
            if (fundData != null) {
                // 基金数据已存在的情况
                if (fundData.contains("基金数据已存在")) {
                    return Result.build(null, 400, "该基金数据已存在，请勿重复获取！");
                }
                // 基金代码不存在的情况
                if (fundData.contains("无") && fundData.contains("这个代码的基金")) {
                    return Result.build(null, 404, "未找到该基金代码对应的数据，请检查基金代码是否正确！");
                }
            }
            
            return Result.build(fundData, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("获取基金基本数据失败", e);
            return Result.build(null, 500, "获取基金基本数据失败！");
        }
    }

    /**
     * 根据基金代码查询基金基本数据（用于资产台账穿透查看）
     * @param fundCode 基金代码
     * @return 基金基本数据
     */
    @GetMapping("/getFundBaseDataByFundCode/{fundCode}")
    public Result getFundBaseDataByFundCode(@PathVariable("fundCode") String fundCode) {
        try {
            FundAsset fundAsset = fundAssetService.getFundBaseDataByFundCode(fundCode);
            if (fundAsset == null) {
                return Result.build(null, 404, "未找到该基金代码对应的数据");
            }
            return Result.build(fundAsset, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("根据基金代码查询基金基本数据失败", e);
            return Result.build(null, 500, "查询基金基本数据失败！");
        }
    }

    /**
     * 条件分页查询基金基本数据
     * @param current
     * @param limit
     * @param fundBaseDateDto
     * @return
     */
    @PostMapping("/getFundBaseDataByConditionAndPage/{current}/{limit}")
    public Result getFundBaseDataByConditionAndPage(@PathVariable("current") Integer current,
                                                    @PathVariable("limit") Integer limit,
                                                    @RequestBody FundBaseDateDto fundBaseDateDto){
        PageInfo<FundAsset> fundAssetPageInfo = fundAssetService.getFundBaseDataByConditionAndPage(current, limit, fundBaseDateDto);
        return Result.build(fundAssetPageInfo, ResultCodeEnum.SUCCESS);
    }

    /**
     * 条件分页查询基金净值数据
     * @param current
     * @param limit
     * @param fundComm
     * @return
     */
    @GetMapping("/getFundNavByConditionAndPage/{current}/{limit}")
    public Result getFundNavByConditionAndPage(@PathVariable("current") Integer current,
                                               @PathVariable("limit") Integer limit,
                                               FundComm fundComm){
        PageInfo<FundNav> fundNavPageInfo = fundAssetService.getFundNavByConditionAndPage(current, limit, fundComm);
        return Result.build(fundNavPageInfo, ResultCodeEnum.SUCCESS);
    }

    /**
     * 根据基金代码获取基金经理分析数据
     * @param fundCode
     * @return
     */
    @GetMapping("/getFundManagerAnalysisByCode")
    public Result getFundManagerAnalysisByCode(String fundCode) {
        try {
            FundManagerAnalysis fundManagerAnalysis = fundAssetService.getFundManagerAnalysisByCode(fundCode);
            return Result.build(fundManagerAnalysis, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("获取基金经理分析数据失败", e);
            return Result.build(null, 500, "获取基金经理分析数据失败！");
        }
    }

    /**
     * 根据基金代码获取基金持仓数据
     * @param fundCode
     * @return
     */
    @GetMapping("/getFundHoldingByCode")
    public Result getFundHoldingByCode(String fundCode) {
        try {
            FundHolding fundHolding = fundAssetService.getFundHoldingByCode(fundCode);
            return Result.build(fundHolding, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("获取基金持仓数据失败", e);
            return Result.build(null, 500, "获取基金持仓数据失败！");
        }
    }

    /**
     * 条件分页查询基金交易与流水数据
     * @param current
     * @param limit
     * @param fundComm
     * @return
     */
    @GetMapping("/getFundTransactionByConditionAndPage/{current}/{limit}")
    public Result getFundTransactionByConditionAndPage(@PathVariable("current") Integer current,
                                                      @PathVariable("limit") Integer limit,
                                                      FundComm fundComm) {
        PageInfo<FundTransaction> fundTransactionPageInfo = fundAssetService.getFundTransactionByConditionAndPage(current, limit, fundComm);
        return Result.build(fundTransactionPageInfo, ResultCodeEnum.SUCCESS);
    }

    /**
     * 条件分页查询查询基金分红数据
     * @param current
     * @param limit
     * @param fundComm
     * @return
     */
    @GetMapping("/getFundDividendByConditionAndPage/{current}/{limit}")
    public Result getFundDividendByConditionAndPage(@PathVariable("current") Integer current,
                                                   @PathVariable("limit") Integer limit,
                                                   FundComm fundComm) {
        PageInfo<FundDividend> fundDividendPageInfo = fundAssetService.getFundDividendByConditionAndPage(current, limit, fundComm);
        return Result.build(fundDividendPageInfo, ResultCodeEnum.SUCCESS);
    }

    /**
     * 根据基金代码获取基金风险收益数据
     * @param fundCode
     * @return
     */
    @GetMapping("/getFundRiskPerformanceByCode")
    public Result getFundRiskPerformance(String fundCode) {
        List<FundRiskPerformance> fundRiskPerformanceList = fundAssetService.getFundRiskPerformance(fundCode);
        return Result.build(fundRiskPerformanceList, ResultCodeEnum.SUCCESS);
    }

    /**
     * 条件分页获取基金持仓信息
     * @param current
     * @param limit
     * @param fundComm
     * @return
     */
    @GetMapping("/getFundPortfolioByConditionAndPage/{current}/{limit}")
    public Result getFundPortfolioByConditionAndPage(@PathVariable("current") Integer current,
                                                    @PathVariable("limit") Integer limit,
                                                    FundComm fundComm) {
        PageInfo<FundPortfolio> fundPortfolioPageInfo = fundAssetService.getFundPortfolioByConditionAndPage(current, limit, fundComm);
        return Result.build(fundPortfolioPageInfo, ResultCodeEnum.SUCCESS);
    }

    /**
     * 更新基金基本数据
     * @param fundAsset
     * @return
     */
    @PostMapping("/updateFundBaseAsset")
    public Result updateFundBaseAsset(@RequestBody FundAsset fundAsset) {
        try {
            fundAssetService.updateFundBaseAsset(fundAsset);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("更新基金基本数据失败", e);
            return Result.build(null, 500, "更新基金基本数据失败！");
        }
    }

    /**
     * 更新基金经理分析数据
     * @param fundManagerAnalysis
     * @return
     */
    @PostMapping("/updateFundManagerAnalysis")
    public Result updateFundManagerAnalysis(@RequestBody FundManagerAnalysis fundManagerAnalysis) {
        try {
            fundAssetService.updateFundManagerAnalysis(fundManagerAnalysis);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("更新基金经理分析数据失败", e);
            return Result.build(null, 500, "更新基金经理分析数据失败！");
        }
    }

    /**
     * 更新基金持仓数据
     * @param fundHolding
     * @return
     */
    @PostMapping("/updateFundHolding")
    public Result updateFundHolding(@RequestBody FundHolding fundHolding) {
        try {
            fundAssetService.updateFundHolding(fundHolding);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("更新基金持仓数据失败", e);
            return Result.build(null, 500, "更新基金持仓数据失败！");
        }
    }

    /**
     * 添加基金净值数据
     * @param fundNav
     * @return
     */
    @PostMapping("/addFundNav")
    public Result addFundNav(@RequestBody FundNav fundNav) {
        try {
            fundAssetService.addFundNav(fundNav);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("添加基金净值数据失败", e);
            return Result.build(null, 500, "添加基金净值数据失败！");
        }
    }

    /**
     * 更新基金净值数据
     * @param fundNav
     * @return
     */
    @PostMapping("/updateFundNav")
    public Result updateFundNav(@RequestBody FundNav fundNav) {
        try {
            fundAssetService.updateFundNav(fundNav);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("更新基金净值数据失败", e);
            return Result.build(null, 500, "更新基金净值数据失败！");
        }
    }

    /**
     * 根据id删除基金净值数据
     * @param id
     * @return
     */
    @DeleteMapping("/deleteFundNav/{id}")
    public Result deleteFundNav(@PathVariable("id") Long id) {
        try {
            fundAssetService.deleteFundNav(id);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("删除基金净值数据失败", e);
            return Result.build(null, 500, "删除基金净值数据失败！");
        }
    }

    /**
     * 添加基金交易数据
     * @param fundTransaction
     * @return
     */
    @PostMapping("/addFundTransaction")
    public Result addFundTransaction(@RequestBody FundTransaction fundTransaction) {
        try {
            fundAssetService.addFundTransaction(fundTransaction);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("添加基金交易数据失败", e);
            return Result.build(null, 500, "添加基金交易数据失败！");
        }
    }

    /**
     * 更新基金交易数据
     * @param fundTransaction
     * @return
     */
    @PostMapping("/updateFundTransaction")
    public Result updateFundTransaction(@RequestBody FundTransaction fundTransaction) {
        try {
            fundAssetService.updateFundTransaction(fundTransaction);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("更新基金交易数据失败", e);
            return Result.build(null, 500, "更新基金交易数据失败！");
        }
    }

    /**
     * 根据id删除基金交易数据
     * @param id
     * @return
     */
    @DeleteMapping("/deleteFundTransaction/{id}")
    public Result deleteFundTransaction(@PathVariable("id") Long id) {
        try {
            fundAssetService.deleteFundTransaction(id);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("删除基金交易数据失败", e);
            return Result.build(null, 500, "删除基金交易数据失败！");
        }
    }

    /**
     * 添加基金分红数据
     * @param fundDividend
     * @return
     */
    @PostMapping("/addFundDividend")
    public Result addFundDividend(@RequestBody FundDividend fundDividend) {
        try {
            fundAssetService.addFundDividend(fundDividend);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("添加基金分红数据失败", e);
            return Result.build(null, 500, "添加基金分红数据失败！");
        }
    }

    /**
     * 更新基金分红数据
     * @param fundDividend
     * @return
     */
    @PostMapping("/updateFundDividend")
    public Result updateFundDividend(@RequestBody FundDividend fundDividend) {
        try {
            fundAssetService.updateFundDividend(fundDividend);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("更新基金分红数据失败", e);
            return Result.build(null, 500, "更新基金分红数据失败！");
        }
    }

    /**
     * 根据id删除基金分红数据
     * @param id
     * @return
     */
    @DeleteMapping("/deleteFundDividend/{id}")
    public Result deleteFundDividend(@PathVariable("id") Long id) {
        try {
            fundAssetService.deleteFundDividend(id);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("删除基金分红数据失败", e);
            return Result.build(null, 500, "删除基金分红数据失败！");
        }
    }

    /**
     * 添加基金风险绩效数据
     * @param fundRiskPerformance
     * @return
     */
    @PostMapping("/addFundRiskPerformance")
    public Result addFundRiskPerformance(@RequestBody FundRiskPerformance fundRiskPerformance) {
        try {
            fundAssetService.addFundRiskPerformance(fundRiskPerformance);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("添加基金风险绩效数据失败", e);
            return Result.build(null, 500, "添加基金风险绩效数据失败！");
        }
    }

    /**
     * 更新基金风险绩效数据
     * @param fundRiskPerformance
     * @return
     */
    @PostMapping("/updateFundRiskPerformance")
    public Result updateFundRiskPerformance(@RequestBody FundRiskPerformance fundRiskPerformance) {
        try {
            fundAssetService.updateFundRiskPerformance(fundRiskPerformance);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("更新基金风险绩效数据失败", e);
            return Result.build(null, 500, "更新基金风险绩效数据失败！");
        }
    }

    /**
     * 根据id删除基金风险绩效数据
     * @param id
     * @return
     */
    @DeleteMapping("/deleteFundRiskPerformance/{id}")
    public Result deleteFundRiskPerformance(@PathVariable("id") Long id) {
        try {
            fundAssetService.deleteFundRiskPerformance(id);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("删除基金风险绩效数据失败", e);
            return Result.build(null, 500, "删除基金风险绩效数据失败！");
        }
    }

    /**
     * 添加基金持仓信息数据
     * @param fundPortfolio
     * @return
     */
    @PostMapping("/addFundPortfolio")
    public Result addFundPortfolio(@RequestBody FundPortfolio fundPortfolio) {
        try {
            fundAssetService.addFundPortfolio(fundPortfolio);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("添加基金持仓信息数据失败", e);
            return Result.build(null, 500, "添加基金持仓信息数据失败！");
        }
    }

    /**
     * 更新基金持仓信息数据
     * @param fundPortfolio
     * @return
     */
    @PostMapping("/updateFundPortfolio")
    public Result updateFundPortfolio(@RequestBody FundPortfolio fundPortfolio) {
        try {
            fundAssetService.updateFundPortfolio(fundPortfolio);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("更新基金持仓信息数据失败", e);
            return Result.build(null, 500, "更新基金持仓信息数据失败！");
        }
    }

    /**
     * 根据id删除基金持仓信息数据
     * @param id
     * @return
     */
    @DeleteMapping("/deleteFundPortfolio/{id}")
    public Result deleteFundPortfolio(@PathVariable("id") Long id) {
        try {
            fundAssetService.deleteFundPortfolio(id);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("删除基金持仓信息数据失败", e);
            return Result.build(null, 500, "删除基金持仓信息数据失败！");
        }
    }

    /**
     * 获取业绩走势echarts数据
     * @param fundComm
     * @return
     */
    @GetMapping("/getNaveChartsByCondition")
    public Result getNaveChartsByCondition(FundComm fundComm){
        List<NavEChartsVo> navEChartsVos = fundAssetService.getNaveChartsByCondition(fundComm);
        return Result.build(navEChartsVos, ResultCodeEnum.SUCCESS);
    }

    /**
     * 根据基金代码删除基金的全部数据
     * @param fund_code
     * @return
     */
    @DeleteMapping("/deleteFundDataByCode/{fund_code}")
    public Result deleteFundDataByCode(@PathVariable("fund_code") String fund_code){
        try {
            fundAssetService.deleteFundDataByCode(fund_code);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("删除基金数据失败", e);
            return Result.build(null, 500, "基金数据删除失败！");
        }
    }

    /**
     * 根据基金代码批量删除基金数据
     * @param fund_codes
     * @return
     */
    @PostMapping("/deleteFundDataByCodes")
    public Result deleteFundDataByCodes(@RequestBody List<String> fund_codes){
        try {
            fundAssetService.deleteFundDataByCodes(fund_codes);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("批量删除基金数据失败", e);
            return Result.build(null, 500, "批量基金数据删除失败！");
        }
    }

    /**
     * 获取基金重要数据 -> 基金持仓数据插入数据库，基金净值数据插入数据库
     * @param fundCode
     * @return
     */
    @PostMapping("/addFundImportData/{fundCode}")
    public Result addFundImportData(@PathVariable("fundCode") String fundCode){
        try {
            fundAssetService.addFundImportData(fundCode);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("获取基金重要数据失败", e);
            return Result.build(null, 500, "获取基金重要数据！");
        }
    }

}
