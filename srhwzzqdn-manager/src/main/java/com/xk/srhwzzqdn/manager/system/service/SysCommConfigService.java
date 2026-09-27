package com.xk.srhwzzqdn.manager.system.service;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.model.dto.system.SysCommConfigQueryDto;
import com.xk.srhwzzqdn.model.entity.system.SysCommConfig;
import com.xk.srhwzzqdn.model.vo.system.InterfaceCompareVo;

import java.util.List;

public interface SysCommConfigService {

    PageInfo<SysCommConfig> findPage(Integer current, Integer limit, SysCommConfigQueryDto queryDto);

    List<SysCommConfig> findAllEnabled();

    SysCommConfig findById(String id);

    void save(SysCommConfig config);

    void update(SysCommConfig config);

    void deleteById(String id);

    List<InterfaceCompareVo> aiFetchLatest();
}