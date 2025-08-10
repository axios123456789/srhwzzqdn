package com.xk.srhwzzqdn.manager.util;

import com.xk.srhwzzqdn.model.entity.system.SysAdministrative;

import java.util.ArrayList;
import java.util.List;

//封装树形中国行政区划数据
public class AdministrativeHelper {
    //递归实现封装过程
    public static List<SysAdministrative> buildTree(List<SysAdministrative> sysAdministrativeList){
        //创建空列表封装最后结果
        List<SysAdministrative> trees = new ArrayList<>();

        //遍历所有菜单
        for (SysAdministrative sysAdministrative : sysAdministrativeList){
            //找到最上层数据
            if ("-1".equals(sysAdministrative.getParentValue())){
                trees.add(findChildren(sysAdministrative, sysAdministrativeList));
            }
        }

        return trees;
    }

    /**
     * 递归查找下级行政区
     * @param sysAdministrative
     * @param sysAdministrativeList
     * @return
     */
    private static SysAdministrative findChildren(SysAdministrative sysAdministrative, List<SysAdministrative> sysAdministrativeList) {
        //初始化children
        sysAdministrative.setChildren(new ArrayList<>());

        //递归查询
        for (SysAdministrative item : sysAdministrativeList){
            if (sysAdministrative.getValue().equals(item.getParentValue())){
                sysAdministrative.getChildren().add(findChildren(item, sysAdministrativeList));
            }
        }

        return sysAdministrative;
    }
}
