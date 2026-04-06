package com.xk.srhwzzqdn.manager.assetControlArea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.assetControlArea.mapper.AssetLedgerMapper;
import com.xk.srhwzzqdn.manager.assetControlArea.service.AssetLedgerService;
import com.xk.srhwzzqdn.model.dto.assetControl.AssetLedgerDto;
import com.xk.srhwzzqdn.model.entity.assetControl.AssetLedger;
import com.xk.srhwzzqdn.model.vo.assetControl.AssetTypeSummaryVO;
import com.xk.srhwzzqdn.model.vo.assetControl.AssetLedgerPageVo;
import com.xk.srhwzzqdn.model.vo.assetControl.AssetLedgerVo;
import com.xk.srhwzzqdn.util.AuthContextUtil;
import com.xk.srhwzzqdn.util.GenerateNoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AssetLedgerServiceImpl implements AssetLedgerService {
    @Autowired
    private AssetLedgerMapper assetLedgerMapper;

    /**
     * 条件分页查询资产台账列表
     * @param current
     * @param limit
     * @param assetLedgerDto
     * @return
     */
    @Override
    public AssetLedgerPageVo getAssetLedgerByConditionAndPage(Integer current, Integer limit, AssetLedgerDto assetLedgerDto) {
        //1.开启分页
        PageHelper.startPage(current, limit);

        //设置所属人
        assetLedgerDto.setAssetOwner(AuthContextUtil.get().getId());

        //条件查询资产台账列表
        List<AssetLedgerVo> assetLedgerList = assetLedgerMapper.getAssetLedgerByCondition(assetLedgerDto);

        //创建分页对象
        PageInfo<AssetLedgerVo> assetLedgerPageInfo = new PageInfo<>(assetLedgerList);

        //查询资产类型分组统计金额
        List<AssetTypeSummaryVO> summaryList = assetLedgerMapper.getAssetLedgerSummaryByType(assetLedgerDto);
        
        //将 List<AssetTypeSummaryVO>转换为 Map<Integer, BigDecimal>
        Map<Integer, BigDecimal> assetTypeSummary = new HashMap<>();
        if (summaryList != null && !summaryList.isEmpty()) {
            for (AssetTypeSummaryVO item : summaryList) {
                Integer assetType = item.getAssetType();
                BigDecimal totalAmount = item.getTotalAmount();
                if (assetType != null && totalAmount != null) {
                    assetTypeSummary.put(assetType, totalAmount);
                }
            }
        }

        //构建返回VO
        AssetLedgerPageVo pageVo = new AssetLedgerPageVo();
        pageVo.setRecords(assetLedgerPageInfo.getList());
        pageVo.setTotal(assetLedgerPageInfo.getTotal());
        pageVo.setCurrent(current);
        pageVo.setSize(limit);
        pageVo.setAssetTypeSummary(assetTypeSummary);

        return pageVo;
    }

    /**
     * 保存资产台账
     * @param assetLedger
     */
    @Override
    public void saveAssetLedger(AssetLedger assetLedger) {
        if (assetLedger.getId() == null){//新增
            assetLedger.setAssetNo(GenerateNoUtil.generateNo("ASSET"));
            assetLedger.setAssetOwner(AuthContextUtil.get().getId());

            //新增资产台账
            assetLedgerMapper.addAssetLedger(assetLedger);
        }else {//修改
            assetLedger.setUpdateBy(AuthContextUtil.get().getUserName());

            //修改资产台账
            assetLedgerMapper.updateAssetLedger(assetLedger);
        }
    }

    /**
     * 根据id删除资产台账
     * @param id
     */
    @Override
    public void deleteAssetLedgerById(Integer id) {
        assetLedgerMapper.deleteAssetLedgerById(id);
    }

    /**
     * 根据ids批量删除资产台账
     * @param ids
     */
    @Override
    public void deleteAllAssetLedgerByIds(List<Integer> ids) {
        assetLedgerMapper.deleteAllAssetLedgerByIds(ids);
    }
}
