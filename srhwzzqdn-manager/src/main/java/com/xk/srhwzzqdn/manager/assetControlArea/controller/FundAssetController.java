package com.xk.srhwzzqdn.manager.assetControlArea.controller;

import com.xk.srhwzzqdn.manager.assetControlArea.service.FundAssetService;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/superBrain/assetControl/fundAsset")
public class FundAssetController {
    @Autowired
    private FundAssetService fundAssetService;

    private static final Logger logger = LoggerFactory.getLogger(FundAssetController.class);

    /**
     * 获取基金基本数据
     * @param fundCode
     * @return
     */
    @RequestMapping("/getFundBaseData/{fundCode}")
    public Result getFundBaseData(@PathVariable("fundCode") String fundCode) {
        try {
            fundAssetService.getFundBaseData(fundCode);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("获取资产金额排名数据失败", e);
            return Result.build(null, 500, "获取基金基本数据失败！");
        }
    }
}
