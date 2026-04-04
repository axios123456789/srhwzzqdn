package com.xk.srhwzzqdn.manager.assetControlArea.controller;

import com.xk.srhwzzqdn.manager.assetControlArea.service.AssetTransactionService;
import com.xk.srhwzzqdn.model.dto.assetControl.AssetTransactionDto;
import com.xk.srhwzzqdn.model.entity.assetControl.AssetTransaction;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/superBrain/assetControl/assetAccounting")
public class AssetTransactionController {
    @Autowired
    private AssetTransactionService assetTransactionService;

    /**
     * 条件分页查询资产记账列表以及查询收支金额合计
     * @param current
     * @param limit
     * @param assetTransactionDto
     * @return
     */
    @RequestMapping("/getAssetTransactionListByConditionAndPage/{current}/{limit}")
    public Result getAssetTransactionListByConditionAndPage(@PathVariable("current") Integer current,
                                                            @PathVariable("limit") Integer limit,
                                                            @RequestBody AssetTransactionDto assetTransactionDto){
        Map<String, Object> assetTransactionListByConditionAndPage =
                assetTransactionService.getAssetTransactionListByConditionAndPage(current, limit, assetTransactionDto);
        return Result.build(assetTransactionListByConditionAndPage, ResultCodeEnum.SUCCESS);
    }

    /**
     * 保存资产记账
     * @param assetTransaction
     * @return
     */
    @PostMapping("/saveAssetAccounting")
    public Result saveAssetAccounting(@RequestBody AssetTransaction assetTransaction){
        try {
            assetTransactionService.saveAssetTransaction(assetTransaction);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "保存资产记账失败！");
        }
    }

    /**
     * 根据id删除资产记账
     * @param id
     * @return
     */
    @DeleteMapping("/deleteAssetAccountingById/{id}")
    public Result deleteAssetAccountingById(@PathVariable("id") Integer id){
        try {
            assetTransactionService.deleteAssetTransactionById(id);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "删除资产记账失败！");
        }
    }

    /**
     * 根据ids批量删除资产记账
     * @param ids
     * @return
     */
    @PostMapping("/deleteAllAssetAccountingByIds")
    public Result deleteAllAssetAccountingByIds(@RequestBody List<Integer> ids){
        try {
            assetTransactionService.deleteAllAssetTransactionByIds(ids);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "批量删除资产记账失败！");
        }
    }
}
