package com.xk.srhwzzqdn.manager.assetControlArea.controller;

import com.xk.srhwzzqdn.manager.assetControlArea.service.AssetLedgerService;
import com.xk.srhwzzqdn.model.dto.assetControl.AssetLedgerDto;
import com.xk.srhwzzqdn.model.entity.assetControl.AssetLedger;
import com.xk.srhwzzqdn.model.vo.assetControl.AssetLedgerPageVo;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/superBrain/assetControl/assetLedger")
public class AssetLedgerController {
    @Autowired
    private AssetLedgerService assetLedgerService;

    /**
     * 条件分页查询资产台账列表
     * @param current
     * @param limit
     * @param assetLedgerDto
     * @return
     */
    @RequestMapping("/getAssetLedgerByConditionAndPage/{current}/{limit}")
    public Result getAssetLedgerByConditionAndPage(@PathVariable("current") Integer current,
                                                  @PathVariable("limit") Integer limit,
                                                  @RequestBody AssetLedgerDto assetLedgerDto){
        AssetLedgerPageVo assetLedgerPageVo = assetLedgerService.getAssetLedgerByConditionAndPage(current, limit, assetLedgerDto);
        return Result.build(assetLedgerPageVo, ResultCodeEnum.SUCCESS);
    }

    /**
     * 保存资产台账
     * @param assetLedger
     * @return
     */
    @PostMapping("/saveAssetLedger")
    public Result saveAssetLedger(@RequestBody AssetLedger assetLedger){
        try {
            assetLedgerService.saveAssetLedger(assetLedger);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "保存资产台账失败！");
        }
    }

    /**
     * 根据id删除资产台账
     * @param id
     * @return
     */
    @DeleteMapping("/deleteAssetLedgerById/{id}")
    public Result deleteAssetLedgerById(@PathVariable("id") Integer id){
        try {
            assetLedgerService.deleteAssetLedgerById(id);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "删除资产台账失败！");
        }
    }

    /**
     * 根据ids批量删除资产台账
     * @param ids
     * @return
     */
    @PostMapping("/deleteAllAssetLedgerByIds")
    public Result deleteAllAssetLedgerByIds(@RequestBody List<Integer> ids){
        try {
            assetLedgerService.deleteAllAssetLedgerByIds(ids);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "批量删除资产台账失败！");
        }
    }
}
