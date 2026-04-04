package com.xk.srhwzzqdn.manager.assetControlArea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.assetControlArea.mapper.AssetTransactionMapper;
import com.xk.srhwzzqdn.manager.assetControlArea.service.AssetTransactionService;
import com.xk.srhwzzqdn.model.dto.assetControl.AssetTransactionDto;
import com.xk.srhwzzqdn.model.entity.assetControl.AssetTransaction;
import com.xk.srhwzzqdn.model.vo.assetControl.AssetTransactionTypeGroupVo;
import com.xk.srhwzzqdn.model.vo.assetControl.AssetTransactionVo;
import com.xk.srhwzzqdn.util.AuthContextUtil;
import com.xk.srhwzzqdn.util.GenerateNoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * 条件分页查询资产记账列表以及查询收支金额合计
     * @param current
     * @param limit
     * @param assetTransactionDto
     * @return
     */
    @Override
    public Map<String, Object> getAssetTransactionListByConditionAndPage(Integer current, Integer limit, AssetTransactionDto assetTransactionDto) {
        //1.开启分页
        PageHelper.startPage(current, limit);

        //2.条件查询资产记账列表
        List<AssetTransactionVo> assetTransactionList = assetTransactionMapper.getAssetTransactionListByCondition(assetTransactionDto);

        //3.创建分页对象
        PageInfo<AssetTransactionVo> assetTransactionVoPageInfo = new PageInfo<>(assetTransactionList);

        //4.获取根据收支类型分类的收支金额合计
        List<AssetTransactionTypeGroupVo> assetTransactionTypeGroupVoList = assetTransactionMapper.getAssetTransactionTypeGroupListByCondition(assetTransactionDto);

        //5.创建用于返回的map集合
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("dataList", assetTransactionVoPageInfo);
        // 将分组统计结果放入返回Map中
        /*if (assetTransactionTypeGroupVoList != null && assetTransactionTypeGroupVoList.size() > 0) {
            assetTransactionTypeGroupVoList.forEach(vo ->
                dataMap.put(vo.getTransactionType(), vo.getTransactionAmount())
            );
        }*/
        dataMap.put("groupList", assetTransactionTypeGroupVoList);

        //6.返回
        return dataMap;
    }
}
