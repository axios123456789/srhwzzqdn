package com.xk.srhwzzqdn.manager.system.mapper;

import com.xk.srhwzzqdn.model.dto.system.SysCommConfigQueryDto;
import com.xk.srhwzzqdn.model.entity.system.SysCommConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysCommConfigMapper {

    List<SysCommConfig> findList(SysCommConfigQueryDto queryDto);

    List<SysCommConfig> findAllEnabled();

    SysCommConfig findById(@Param("id") String id);

    void insert(SysCommConfig config);

    void update(SysCommConfig config);

    void deleteById(@Param("id") String id);

    @Select("select value from t_sys_comm_config where id = #{param1} and status = 1")
    String getEnabledValueById(String id);
}