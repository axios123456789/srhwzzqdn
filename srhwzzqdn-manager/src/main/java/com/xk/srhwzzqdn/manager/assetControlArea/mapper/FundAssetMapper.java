package com.xk.srhwzzqdn.manager.assetControlArea.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FundAssetMapper {
    //根据基金代码查询基金的数据是否已获取
    @Select("select count(*) from t_fund_asset where fund_code = #{param1}")
    int isExistByCode(String fundCode);
}
