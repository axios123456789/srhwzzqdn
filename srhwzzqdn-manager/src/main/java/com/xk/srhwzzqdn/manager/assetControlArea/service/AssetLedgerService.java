package com.xk.srhwzzqdn.manager.assetControlArea.service;

import com.xk.srhwzzqdn.model.dto.assetControl.AssetLedgerDto;
import com.xk.srhwzzqdn.model.entity.assetControl.AssetLedger;
import com.xk.srhwzzqdn.model.vo.assetControl.AssetLedgerPageVo;

import java.util.List;

public interface AssetLedgerService {
    //条件分页查询资产台账列表
    AssetLedgerPageVo getAssetLedgerByConditionAndPage(Integer current, Integer limit, AssetLedgerDto assetLedgerDto);

    //保存资产台账
    void saveAssetLedger(AssetLedger assetLedger);

    //根据id删除资产台账
    void deleteAssetLedgerById(Integer id);

    //根据ids批量删除资产台账
    void deleteAllAssetLedgerByIds(List<Integer> ids);
}
