package com.xk.srhwzzqdn.manager.system.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.system.mapper.SysDictMapper;
import com.xk.srhwzzqdn.manager.system.service.SysDictService;
import com.xk.srhwzzqdn.manager.util.AdministrativeHelper;
import com.xk.srhwzzqdn.model.dto.system.SysDictDto;
import com.xk.srhwzzqdn.model.entity.system.SysAdministrative;
import com.xk.srhwzzqdn.model.entity.system.SysCode;
import com.xk.srhwzzqdn.model.entity.system.SysDict;
import com.xk.srhwzzqdn.util.AuthContextUtil;
import com.xk.srhwzzqdn.util.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SysDictServiceImpl implements SysDictService {
    @Autowired
    private SysDictMapper sysDictMapper;

    /**
     * 通过类型获取下拉列表等键和值
     * @param type
     * @return
     */
    @Override
    public List<Map<String, Object>> getKeyAndValueByType(String type) {
        //根据type查询所有
        List<SysDict> list = sysDictMapper.getCodesByType(type);

        //封装返回数据
        List<Map<String, Object>> items = new ArrayList<>();

        //处理数据返回
        for (SysDict sysCode : list){
            //创建map对象，封装返回值
            Map<String, Object> map = new HashMap<>();
            map.put("value", sysCode.getCode());
            map.put("text", sysCode.getValue());
            items.add(map);
        }

        return items;
    }

    /**
     * 根据当前账号的权限查询对应的账号等级
     * @param type
     * @return
     */
    @Override
    public List<Map<String, Object>> getUserLevelByPower(String type) {
        //根据type查询对应权限的账号等级(只能是自己的下级权限)
        List<SysDict> list = sysDictMapper.getUserLevelByPower(type, AuthContextUtil.get().getLevel());

        //封装返回数据
        List<Map<String, Object>> items = new ArrayList<>();

        //处理数据返回
        for (SysDict sysCode : list){
            //创建map对象，封装返回值
            Map<String, Object> map = new HashMap<>();
            map.put("value", sysCode.getCode());
            map.put("text", sysCode.getValue());
            items.add(map);
        }

        return items;
    }

    /**
     * 条件分页查询数据字典
     * @param current
     * @param limit
     * @param sysDictDto
     * @return
     */
    @Override
    public PageInfo<SysDict> getSysDictList(Integer current, Integer limit, SysDictDto sysDictDto) {
        //设置分页参数
        PageHelper.startPage(current, limit);

        //条件查询所有数据
        List<SysDict> list = sysDictMapper.getSysDictList(sysDictDto);

        //封装pageInfo对象
        PageInfo<SysDict> pageInfo = new PageInfo<>(list);

        return pageInfo;
    }

    /**
     * 添加或修改数据字典
     * @param sysDict
     */
    @Override
    public void saveDict(SysDict sysDict) {
        if (sysDict.getId() == null || "".equals(sysDict.getId())){//添加
            sysDict.setId(UUIDUtil.getUUID());
            sysDict.setCreateBy(AuthContextUtil.get().getUserName());

            sysDictMapper.addDict(sysDict);
        }else {//修改
            sysDict.setUpdateBy(AuthContextUtil.get().getUserName());
            sysDictMapper.updateDict(sysDict);
        }
    }

    /**
     * 删除数据字典
     * @param id
     */
    @Override
    public void deleteDictById(String id) {
        sysDictMapper.deleteDictById(id);
    }

    /**
     * 获取中国行政区划码值对
     * @return
     */
    @Override
    public List<SysAdministrative> getAdministrative() {
        //1.获取所有地区数据
        List<SysAdministrative> sysAdministrativeList = sysDictMapper.getAdministrativeList();

        //2.处理为最终想要数据
        List<SysAdministrative> sysAdministrativeList1 = AdministrativeHelper.buildTree(sysAdministrativeList);

        //3.返回
        return sysAdministrativeList1;
    }

    /**
     * 根据type获取t_sys_code键值对（仅只有一级数据的时候）
     * @param type
     * @return
     */
    @Override
    public List<SysCode> getSysCodeByType(String type) {
        //1.根据type获取对应的码值数据
        List<SysCode> sysCodes = sysDictMapper.getSysCodeByType(type);

        //2.直接返回
        return sysCodes;
    }
}
