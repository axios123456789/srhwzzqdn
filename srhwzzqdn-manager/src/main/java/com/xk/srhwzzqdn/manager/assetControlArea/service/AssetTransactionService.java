package com.xk.srhwzzqdn.manager.assetControlArea.service;

import com.xk.srhwzzqdn.model.dto.assetControl.AssetTransactionDto;
import com.xk.srhwzzqdn.model.entity.assetControl.AssetTransaction;

import java.util.List;
import java.util.Map;

public interface AssetTransactionService {
    // 保存资产记账
    void saveAssetTransaction(AssetTransaction assetTransaction);

    // 根据id删除资产记账
    void deleteAssetTransactionById(Integer id);

    // 根据ids批量删除资产记账
    void deleteAllAssetTransactionByIds(List<Integer> ids);

    Map<String, Object> getAssetTransactionListByConditionAndPage(Integer current, Integer limit, AssetTransactionDto assetTransactionDto);
}
