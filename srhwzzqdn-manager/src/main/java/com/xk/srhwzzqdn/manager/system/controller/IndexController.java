package com.xk.srhwzzqdn.manager.system.controller;

import com.xk.srhwzzqdn.manager.system.service.SysMenuService;
import com.xk.srhwzzqdn.manager.system.service.SysUserService;
import com.xk.srhwzzqdn.manager.system.service.ValidateCodeService;
import com.xk.srhwzzqdn.model.dto.system.EditPasswordDto;
import com.xk.srhwzzqdn.model.dto.system.LoginDto;
import com.xk.srhwzzqdn.model.dto.system.RegisterDto;
import com.xk.srhwzzqdn.model.vo.common.Result;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import com.xk.srhwzzqdn.model.vo.system.LoginVo;
import com.xk.srhwzzqdn.model.vo.system.SysMenuVo;
import com.xk.srhwzzqdn.model.vo.system.ValidateCodeVo;
import com.xk.srhwzzqdn.util.AuthContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户登录及准备工作
 */
@Tag(name = "用户接口")
@RestController
@RequestMapping(value = "/superBrain/system/index")
public class IndexController {
    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private ValidateCodeService validateCodeService;

    @Autowired
    private SysMenuService sysMenuService;

    /**
     * 用户登录
     * @param loginDto
     * @return
     */
    @Operation(summary = "登录的方法")
    @PostMapping("/login")
    public Result login(@RequestBody LoginDto loginDto){
        LoginVo loginVo = sysUserService.login(loginDto);
        return Result.build(loginVo, ResultCodeEnum.SUCCESS);
    }

    /**
     * 生成图片验证码
     * @return
     */
    @GetMapping("/generateValidateCode")
    public Result generateValidateCode(){
        ValidateCodeVo validateCodeVo = validateCodeService.generateValidateCode();
        return Result.build(validateCodeVo, ResultCodeEnum.SUCCESS);
    }

    /**
     * 获取当前登录用户信息
     * @param
     * @return
     */
    @GetMapping("/getUserInfo")
    public Result getUserInfo(){
        return Result.build(AuthContextUtil.get(), ResultCodeEnum.SUCCESS);
    }

    /**
     * 用户退出登录
     * @param token
     * @return
     */
    @GetMapping("/logout")
    public Result logout(@RequestHeader(name = "token") String token){
        sysUserService.logout(token);
        return Result.build(null, ResultCodeEnum.SUCCESS);
    }

    //查询用户可以操作菜单
    @GetMapping("/menus")
    public Result menus(){
        List<SysMenuVo> sysMenuVos = sysMenuService.findMenusByUserId();
        return Result.build(sysMenuVos, ResultCodeEnum.SUCCESS);
    }

    /**
     * 判断是否注册了主脑账号
     * @return
     */
    @GetMapping("/isRegisterMainBrain")
    public Result isRegisterMainBrain(){
        try {
            boolean flag = sysUserService.isRegisterMainBrain();
            return Result.build(flag, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            return Result.build(null, 500, "大脑稍微过载了哦！！！");
        }
    }

    /**
     * 注册外置大脑账号
     * @param registerDto
     * @return
     */
    @PostMapping("/registerBrainAccount")
    public Result registerBrainAccount(@RequestBody RegisterDto registerDto){
        sysUserService.registerBrainAccount(registerDto);
        return Result.build(null, ResultCodeEnum.SUCCESS);
    }

    /**
     * 修改密码接口
     * @param editPasswordDto
     * @return
     */
    @PostMapping("/editPassword")
    public Result editPassword(@RequestBody EditPasswordDto editPasswordDto, @RequestHeader(name = "token") String token){
        sysUserService.editPassword(editPasswordDto, token);
        return Result.build(null, ResultCodeEnum.SUCCESS);
    }
}
