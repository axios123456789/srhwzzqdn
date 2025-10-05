package com.xk.srhwzzqdn.manager.system.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.system.mapper.MapperConfigurationMapper;
import com.xk.srhwzzqdn.manager.system.service.MapperConfigurationService;
import com.xk.srhwzzqdn.model.entity.system.MapperConfiguration;
import com.xk.srhwzzqdn.util.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MapperConfigurationServiceImpl implements MapperConfigurationService {
    @Autowired
    private MapperConfigurationMapper mapperConfigurationMapper;

    /**
     * 根据业务类型type分页查询映射配置列表
     * @param current
     * @param limit
     * @param type
     * @return
     */
    @Override
    public PageInfo<MapperConfiguration> getMapperConfigByConditionAndPage(Integer current, Integer limit, String type) {
        //开启分页
        PageHelper.startPage(current, limit);

        //条件查询映射配置列表
        List<MapperConfiguration> mapperConfigurationList = mapperConfigurationMapper.getMapperConfigurationByCondition(type);

        //设置分页参数
        PageInfo<MapperConfiguration> pageInfo = new PageInfo<>(mapperConfigurationList);

        return pageInfo;
    }

    /**
     * 保存映射配置
     * @param mapperConfiguration
     */
    @Override
    public void saveConfiguration(MapperConfiguration mapperConfiguration) {
        if (mapperConfiguration.getId() == null || "".equals(mapperConfiguration.getId())){//添加配置
            mapperConfiguration.setId(UUIDUtil.getUUID());
            mapperConfigurationMapper.addMapperConfiguration(mapperConfiguration);
        }else {//修改配置
            mapperConfigurationMapper.updateMapperConfiguration(mapperConfiguration);
        }
    }

    /**
     * 根据id删除映射配置
     * @param id
     */
    @Override
    public void deleteMapperConfigurationById(String id) {
        mapperConfigurationMapper.deleteMapperConfigurationById(id);
    }

    /**
     * 根据映射业务类型查询其中1条记录
     * @param type
     * @return
     */
    @Override
    public MapperConfiguration getMapperConfigByType(String type) {
        return mapperConfigurationMapper.getMapperConfigByType(type);
    }

    /**
     * 根据映射业务类型查询所有记录
     * @param type
     * @return
     */
    @Override
    public List<MapperConfiguration> getAllMapperConfigByType(String type) {
        return mapperConfigurationMapper.getAllMapperConfigByType(type);
    }
}
