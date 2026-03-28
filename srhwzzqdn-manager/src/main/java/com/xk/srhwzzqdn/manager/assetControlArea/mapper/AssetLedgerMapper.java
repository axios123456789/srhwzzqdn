package com.xk.srhwzzqdn.manager.assetControlArea.mapper;

import com.xk.srhwzzqdn.model.dto.assetControl.AssetLedgerDto;
import com.xk.srhwzzqdn.model.entity.assetControl.AssetLedger;
import com.xk.srhwzzqdn.model.vo.assetControl.AssetTypeSummaryVO;
import com.xk.srhwzzqdn.model.vo.assetControl.AssetLedgerVo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AssetLedgerMapper {
    //条件查询资产台账列表
    List<AssetLedgerVo> getAssetLedgerByCondition(AssetLedgerDto assetLedgerDto);

    //根据资产类型分组统计金额
    List<AssetTypeSummaryVO> getAssetLedgerSummaryByType(AssetLedgerDto assetLedgerDto);

    //新增资产台账
    void addAssetLedger(AssetLedger assetLedger);

    //修改资产台账
    void updateAssetLedger(AssetLedger assetLedger);

    //根据 id 删除资产台账
    @Delete("delete from t_asset_ledger where id = #{param1}")
    void deleteAssetLedgerById(Integer id);

    //根据 ids 批量删除资产台账
    void deleteAllAssetLedgerByIds(List<Integer> ids);
}
