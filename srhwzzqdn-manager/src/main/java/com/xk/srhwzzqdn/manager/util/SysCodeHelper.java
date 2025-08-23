package com.xk.srhwzzqdn.manager.util;

import com.xk.srhwzzqdn.model.entity.system.SysCode;
import java.util.ArrayList;
import java.util.List;

//封装树形码值数据
public class SysCodeHelper {
    //递归实现封装过程
    public static List<SysCode> buildTree(List<SysCode> sysCodes){
        //创建集合用于封装最终需要形式的数据
        List<SysCode> trees = new ArrayList<>();

        //遍历所有菜单集合
        for (SysCode sysCode : sysCodes){
            //找到递归操作的入口，第一层菜单
            if ("0".equals(sysCode.getParentId())){
                trees.add(findChildren(sysCode, sysCodes));
            }
        }

        return trees;
    }

    //递归查找下层菜单
    private static SysCode findChildren(SysCode sysCode, List<SysCode> sysCodes) {
        //初始化children
        sysCode.setChildren(new ArrayList<>());

        //递归查询
        for (SysCode item : sysCodes){
            if (sysCode.getId().equals(item.getParentId())){
                sysCode.getChildren().add(findChildren(item, sysCodes));
            }
        }

        return sysCode;
    }
}
