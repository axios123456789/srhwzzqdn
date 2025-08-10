package com.xk.srhwzzqdn.manager.system.mapper;

import com.xk.srhwzzqdn.model.dto.system.SysDictDto;
import com.xk.srhwzzqdn.model.entity.system.SysAdministrative;
import com.xk.srhwzzqdn.model.entity.system.SysCode;
import com.xk.srhwzzqdn.model.entity.system.SysDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SysDictMapper {
    //根据type查询所有
    List<SysDict> getCodesByType(String type);

    //根据当前账号的权限查询对应的账号等级
    List<SysDict> getUserLevelByPower(String type, Integer level);

    //条件查询数据字典
    List<SysDict> getSysDictList(SysDictDto sysDictDto);

    //添加数据字典
    void addDict(SysDict sysDict);

    //修改数据字典
    void updateDict(SysDict sysDict);

    //根据id删除数据字典
    @Update("update t_sys_dict set is_deleted = 1 where id = #{param1}")
    void deleteDictById(String id);

    //获取中国行政区划
    @Select("SELECT `value`, " +
                    "short_label AS shortLabel, " +
                    "label, pinyin, `rank`, " +
                    "parent_value AS parentValue, " +
                    "id " +
              "FROM t_sys_administrative")
    List<SysAdministrative> getAdministrativeList();

    //根据type获取对应的码值数据
    @Select("select id, value, text, sort_value as sortValue, parent_id as parentId, `status`, type from t_sys_code where type = #{param1}")
    List<SysCode> getSysCodeByType(String type);
}
