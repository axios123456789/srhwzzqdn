package com.xk.srhwzzqdn.manager.assetControlArea.controller;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.assetControlArea.service.FundAssetService;
import com.xk.srhwzzqdn.model.dto.assetControl.FundBaseDateDto;
import com.xk.srhwzzqdn.model.dto.assetControl.FundComm;
import com.xk.srhwzzqdn.model.entity.assetControl.FundAsset;
import com.xk.srhwzzqdn.model.entity.assetControl.FundManagerAnalysis;
import com.xk.srhwzzqdn.model.entity.assetControl.FundNav;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

}
