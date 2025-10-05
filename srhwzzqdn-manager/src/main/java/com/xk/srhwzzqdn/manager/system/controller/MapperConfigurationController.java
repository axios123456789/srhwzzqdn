package com.xk.srhwzzqdn.manager.system.controller;

import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.system.service.MapperConfigurationService;
import com.xk.srhwzzqdn.model.entity.system.MapperConfiguration;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/superBrain/system/mapperConfig")
public class MapperConfigurationController {
    @Autowired
    private MapperConfigurationService mapperConfigurationService;

    /**
     * 根据业务类型type分页查询映射配置列表
     * @param current
     * @param limit
     * @param type
     * @return
     */
    @GetMapping("/getMapperConfigByConditionAndPage/{current}/{limit}")
    public Result getMapperConfigByConditionAndPage(@PathVariable("current") Integer current,
                                                    @PathVariable("limit") Integer limit,
                                                    String type){
        PageInfo<MapperConfiguration> pageInfo = mapperConfigurationService.getMapperConfigByConditionAndPage(current, limit, type);
        return Result.build(pageInfo, ResultCodeEnum.SUCCESS);
    }

    /**
     * 保存映射配置
     * @param mapperConfiguration
     * @return
     */
    @PostMapping("/saveMapperConfig")
    public Result saveConfiguration(@RequestBody MapperConfiguration mapperConfiguration){
        try {
            mapperConfigurationService.saveConfiguration(mapperConfiguration);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "保存映射配置失败！");
        }
    }

    /**
     * 根据id删除映射配置
     * @param id
     * @return
     */
    @DeleteMapping("/deleteMapperConfigById/{id}")
    public Result deleteMapperConfigurationById(@PathVariable("id") String id){
        try {
            mapperConfigurationService.deleteMapperConfigurationById(id);
            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "删除映射配置失败！");
        }
    }

    /**
     * 根据映射业务类型查询其中1条记录
     * @param type
     * @return
     */
    @GetMapping("/getMapperConfigByType/{type}")
    public Result getMapperConfigByType(@PathVariable("type") String type){
        MapperConfiguration mapperConfiguration = mapperConfigurationService.getMapperConfigByType(type);
        return Result.build(mapperConfiguration, ResultCodeEnum.SUCCESS);
    }

    /**
     * 根据映射业务类型查询所有记录
     * @param type
     * @return
     */
    @GetMapping("/getAllMapperConfigByType/{type}")
    public Result getAllMapperConfigByType(@PathVariable("type") String type){
        List<MapperConfiguration> mapperConfigurations = mapperConfigurationService.getAllMapperConfigByType(type);
        return Result.build(mapperConfigurations, ResultCodeEnum.SUCCESS);
    }
}
