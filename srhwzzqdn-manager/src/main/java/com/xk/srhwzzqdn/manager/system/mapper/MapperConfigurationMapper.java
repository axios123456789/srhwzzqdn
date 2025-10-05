package com.xk.srhwzzqdn.manager.system.mapper;

import com.xk.srhwzzqdn.model.entity.system.MapperConfiguration;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MapperConfigurationMapper {
    //条件查询映射配置列表
    List<MapperConfiguration> getMapperConfigurationByCondition(String type);

    //添加映射配置
    void addMapperConfiguration(MapperConfiguration mapperConfiguration);

    //修改映射配置
    void updateMapperConfiguration(MapperConfiguration mapperConfiguration);

    //根据id删除映射配置
    @Delete("delete from t_mapper_configuration where id = #{param1}")
    void deleteMapperConfigurationById(String id);

    //根据映射业务类型查询其中1条记录
    MapperConfiguration getMapperConfigByType(String type);

    //根据映射业务类型查询所有记录
    List<MapperConfiguration> getAllMapperConfigByType(String type);
}
