package com.xk.srhwzzqdn.manager.system.service;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.model.dto.system.SysDictDto;
import com.xk.srhwzzqdn.model.entity.system.SysAdministrative;
import com.xk.srhwzzqdn.model.entity.system.SysCode;
import com.xk.srhwzzqdn.model.entity.system.SysDict;

import java.util.List;
import java.util.Map;

public interface SysDictService {
    //通过类型获取下拉列表等键和值
    List<Map<String, Object>> getKeyAndValueByType(String type);

    //根据当前账号的权限查询对应的账号等级
    List<Map<String, Object>> getUserLevelByPower(String type);

    //条件分页查询数据字典
    PageInfo<SysDict> getSysDictList(Integer current, Integer limit, SysDictDto sysDictDto);

    //添加或修改数据字典
    void saveDict(SysDict sysDict);

    //删除数据字典
    void deleteDictById(String id);

    //获取中国行政区划码值对
    List<SysAdministrative> getAdministrative();

    //根据type获取t_sys_code键值对（仅只有一级数据的时候）
    List<SysCode> getSysCodeByType(String type);

    //条件查询code码表
    List<SysCode> getSysCodeList(SysDictDto sysDictDto);

    //添加sysCode数据字典
    void addSysCode(SysCode sysCode);

    //修改sysCode数据字典
    void updateSysCode(SysCode sysCode);

    //根据id删除码值
    void deleteSysCodeById(String id);
}
