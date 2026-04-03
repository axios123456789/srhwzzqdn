package com.xk.srhwzzqdn.manager.assetControlArea.service.impl;

import com.xk.srhwzzqdn.manager.assetControlArea.mapper.AssetTransactionMapper;
import com.xk.srhwzzqdn.manager.assetControlArea.service.AssetTransactionService;
import com.xk.srhwzzqdn.model.entity.assetControl.AssetTransaction;
import com.xk.srhwzzqdn.util.AuthContextUtil;
import com.xk.srhwzzqdn.util.GenerateNoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class AssetTransactionServiceImpl implements AssetTransactionService {
    @Autowired
    private AssetTransactionMapper assetTransactionMapper;

    /**
     * 保存资产记账
     * @param assetTransaction
     */
    @Override
    public void saveAssetTransaction(AssetTransaction assetTransaction) {
        if (assetTransaction.getId() == null) { // 新增
            // 生成账单编号
            assetTransaction.setInvoiceNo(GenerateNoUtil.generateNo("INV"));
            // 设置账单所属人
            assetTransaction.setInvoiceOwner(AuthContextUtil.get().getId());
            // 设置更新人
            assetTransaction.setUpdateBy(AuthContextUtil.get().getUserName());

            // 新增资产记账
            assetTransactionMapper.addAssetTransaction(assetTransaction);
        } else { // 修改
            // 设置更新人
            assetTransaction.setUpdateBy(AuthContextUtil.get().getUserName());

            // 修改资产记账
            assetTransactionMapper.updateAssetTransaction(assetTransaction);
        }
    }

    /**
     * 根据id删除资产记账
     * @param id
     */
    @Override
    public void deleteAssetTransactionById(Integer id) {
        assetTransactionMapper.deleteAssetTransactionById(id);
    }

    /**
     * 根据ids批量删除资产记账
     * @param ids
     */
    @Override
    public void deleteAllAssetTransactionByIds(List<Integer> ids) {
        assetTransactionMapper.deleteAllAssetTransactionByIds(ids);
    }
}
