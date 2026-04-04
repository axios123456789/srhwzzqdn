package com.xk.srhwzzqdn.manager.assetControlArea.mapper;

import com.xk.srhwzzqdn.model.dto.assetControl.AssetTransactionDto;
import com.xk.srhwzzqdn.model.entity.assetControl.AssetTransaction;
import com.xk.srhwzzqdn.model.vo.assetControl.AssetTransactionTypeGroupVo;
import com.xk.srhwzzqdn.model.vo.assetControl.AssetTransactionVo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AssetTransactionMapper {
    // 根据id删除资产记账
    @Delete("delete from t_asset_transaction where id = #{param1}")
    void deleteAssetTransactionById(Integer id);

    // 根据ids批量删除资产记账
    void deleteAllAssetTransactionByIds(java.util.List<Integer> ids);

    // 新增资产记账
    void addAssetTransaction(AssetTransaction assetTransaction);

    // 修改资产记账
    void updateAssetTransaction(AssetTransaction assetTransaction);

    //条件查询资产记账列表
    List<AssetTransactionVo> getAssetTransactionListByCondition(AssetTransactionDto assetTransactionDto);

    //获取根据收支类型分类的收支金额合计
    List<AssetTransactionTypeGroupVo> getAssetTransactionTypeGroupListByCondition(AssetTransactionDto assetTransactionDto);
}
