package com.xk.srhwzzqdn.manager.system.service;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.model.entity.system.MapperConfiguration;

import java.util.List;

public interface MapperConfigurationService {
    //根据业务类型type分页查询映射配置列表
    PageInfo<MapperConfiguration> getMapperConfigByConditionAndPage(Integer current, Integer limit, String type);

    //保存映射配置
    void saveConfiguration(MapperConfiguration mapperConfiguration);

    //根据id删除映射配置
    void deleteMapperConfigurationById(String id);

    //根据映射业务类型查询其中1条记录
    MapperConfiguration getMapperConfigByType(String type);

    //根据映射业务类型查询所有记录
    List<MapperConfiguration> getAllMapperConfigByType(String type);
}
