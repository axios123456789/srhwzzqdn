package com.xk.srhwzzqdn.manager.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.system.mapper.SysPersonInfoMapper;
import com.xk.srhwzzqdn.manager.system.mapper.SysRoleMapper;
import com.xk.srhwzzqdn.manager.system.mapper.SysUserAndRoleRelationMapper;
import com.xk.srhwzzqdn.manager.system.mapper.SysUserMapper;
import com.xk.srhwzzqdn.manager.system.service.SysUserService;
import com.xk.srhwzzqdn.model.dto.system.*;
import com.xk.srhwzzqdn.model.entity.system.SysPersonInfo;
import com.xk.srhwzzqdn.model.entity.system.SysUser;
import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import com.xk.srhwzzqdn.model.vo.system.LoginVo;
import com.xk.srhwzzqdn.service.exception.MyException;
import com.xk.srhwzzqdn.util.AuthContextUtil;
import com.xk.srhwzzqdn.util.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SysUserServiceImpl implements SysUserService {
    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SysUserAndRoleRelationMapper sysUserAndRoleRelationMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysPersonInfoMapper sysPersonInfoMapper;

    @Override
    public LoginVo login(LoginDto loginDto) {
        //校验账号密码前先进行验证码校验
        //1.获取输入的验证码和存储到redis的key的名称
        String captcha = loginDto.getCaptcha();
        String key = loginDto.getCodeKey();

        //2.根据key查询redis中的验证码值
        String redisCode = (String) redisTemplate.opsForValue().get("user:validate" + key);

        //3.比较输入的验证码和redis存储的验证码是否一致

        //4.不一致提示错误信息
        if (StrUtil.isEmpty(redisCode) || !StrUtil.equalsIgnoreCase(redisCode, captcha)){
            throw new MyException(ResultCodeEnum.VALIDATECODE_ERROR);
        }

        //5.一致删除redis中的验证码
        redisTemplate.delete("user:validate" + key);

        //1.获取提交的登录账号
        String loginAccount = loginDto.getUserName();

        //2.根据账号查询用户信息
        SysUser sysUser = sysUserMapper.getSysUserByAccount(loginAccount);

        //3.如果查不到用户信息，返回错误提升
        if (sysUser == null){
            throw new MyException(ResultCodeEnum.LOGIN_ERROR);
        }

        //4.如果用户存在，比较密码是否一致（输入的密码进行md5进行加密）
        String database_pwd = sysUser.getPassword();
        String input_pwd = DigestUtils.md5DigestAsHex(loginDto.getPassword().getBytes());
        if (!input_pwd.equals(database_pwd)){
            throw new MyException(ResultCodeEnum.LOGIN_ERROR);
        }

        //判断用户是否被锁定，锁定则抛出异常
        if (sysUser.getStatus() == 0){
            throw new MyException(ResultCodeEnum.ACCOUNT_STOP);
        }

        //5.如果密码一致，登录成功，密码不一致则登录失败
        //6.登录成功，生成用户唯一标识token
        String token = UUID.randomUUID().toString().replaceAll("-", "");

        //7.把登录成功用户信息放到redis里面
        redisTemplate.opsForValue().set("user:login"+token,
                JSON.toJSONString(sysUser),
                3,
                TimeUnit.HOURS);

        //8.返回token对象
        LoginVo loginVo = new LoginVo();
        loginVo.setToken(token);
        return loginVo;
    }

    /**
     * 用户退出登录
     * @param token
     */
    @Override
    public void logout(String token) {
        redisTemplate.delete("user:login"+token);
    }

    /**
     * 查询系统用户列表
     * @param current
     * @param limit
     * @param sysUserDto
     * @return
     */
    @Override
    public PageInfo<SysUser> findByPage(Integer current, Integer limit, SysUserDto sysUserDto) {
        //设置分页参数
        PageHelper.startPage(current, limit);

        //设置当前账户等级
        sysUserDto.setCurrentUserLevel(AuthContextUtil.get().getLevel());

        //条件查询所有数据
        List<SysUser> list = sysUserMapper.findByPage(sysUserDto);

        //封装pageInfo对象
        PageInfo<SysUser> pageInfo = new PageInfo<>(list);

        return pageInfo;
    }

    /**
     * 保存用户
     * @param sysUser
     */
    @Override
    public void saveUser(SysUser sysUser) {
        if (sysUser.getId() == null || sysUser.getId() == ""){//添加
            //设置id
            sysUser.setId(UUIDUtil.getUUID());
            //对密码进行加密设置
            sysUser.setPassword(DigestUtils.md5DigestAsHex(sysUser.getPassword().getBytes()));
            //设置状态为正常
            sysUser.setStatus(1);
            //设置创建者
            sysUser.setCreateBy(AuthContextUtil.get().getUserName());

            //添加
            sysUserMapper.addUser(sysUser);
            //设置用户信息
            generatePersonInfo(sysUser);
            return;
        }
        //修改
        //设置修改者
        sysUser.setUpdateBy(AuthContextUtil.get().getUserName());
        sysUserMapper.updateUser(sysUser);
    }

    /**
     * 根据id删除用户
     * @param id
     */
    @Override
    public void deleteUserById(String id) {
        sysUserMapper.deleteUserById(id);
    }

    /**
     * 用户分配角色
     * @param distributeRoleDto
     */
    @Override
    @Transactional
    public void distributeRole(DistributeRoleDto distributeRoleDto) {
        //分配角色前先删除之前已经分配的角色
        sysUserAndRoleRelationMapper.deleteRoleByUserId(distributeRoleDto.getUserId());

        //处理数据
        List<Map<String, Object>> userAndRoleList = distributeRoleDto.getRoleIdList().stream().map(roleId -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", UUIDUtil.getUUID());
            map.put("userId", distributeRoleDto.getUserId());
            map.put("roleId", roleId);
            return map;
        }).collect(Collectors.toList());

        //分配角色
        sysUserAndRoleRelationMapper.addRoleByUserId(userAndRoleList);
    }

    /**
     * 判断是否注册了主脑账号
     * @return
     */
    @Override
    public boolean isRegisterMainBrain() {
        //获取主脑账号数
        int mainBrainCount = sysUserMapper.getMainBrainCount();

        if (mainBrainCount == 0){
            return false;
        }else {
            return true;
        }
    }

    /**
     * 外置大脑账号注册
     * @param registerDto
     */
    @Transactional
    @Override
    public void registerBrainAccount(RegisterDto registerDto) {
        //1.对前端传过来的两个密码进行对比，判断是否一致
        if (!registerDto.getPassword().equals(registerDto.getPassword2())){
            throw new MyException(ResultCodeEnum.PASSWORD_MISMATCH);
        }

        //2.判重机制，判断注册的账号是否存在，如果存在则提升错误信息
        SysUser sysUserByAccount = sysUserMapper.getSysUserByAccount(registerDto.getAccount());
        if (sysUserByAccount != null){
            throw new MyException(ResultCodeEnum.USER_NAME_IS_EXISTS);
        }

        //处理数据
        SysUser sysUser = new SysUser();
        sysUser.setId(UUIDUtil.getUUID());
        sysUser.setStatus(1);
        sysUser.setAccount(registerDto.getAccount());
        sysUser.setPassword(DigestUtils.md5DigestAsHex(registerDto.getPassword().getBytes()));
        sysUser.setUserName(registerDto.getUserName());
        sysUser.setLevel(registerDto.getLevel());
        sysUser.setAvatar(registerDto.getAvatar());
        sysUser.setDescription(registerDto.getDescription());

        //3.根据传过来的level判断是注册主脑账号还是普通注册
        if (registerDto.getLevel() == 1){//主脑账号注册
            //4.将前端传过来的密码与系统允许注册主脑账号密码对比
            if (registerDto.getVerificationPassword() != null && "8241fb7f5a9031a866e3bb5d25506e33".equals(DigestUtils.md5DigestAsHex(registerDto.getVerificationPassword().getBytes()))){
                //5.直接注册（分配主脑的角色）
                //查询主脑角色id
                String id = sysRoleMapper.getRoleIdByRoleCode("mainMaster");
                //处理数据
                List<Map<String, Object>> userAndRole = new ArrayList<>();
                Map<String, Object> map = new HashMap<>();
                map.put("id", UUIDUtil.getUUID());
                map.put("userId", sysUser.getId());
                map.put("roleId", id);
                userAndRole.add(map);
                //分配角色
                sysUserAndRoleRelationMapper.addRoleByUserId(userAndRole);
                //注册
                sysUserMapper.addUser(sysUser);
                //设置个人信息
                generatePersonInfo(sysUser);
            }else {
                throw new MyException(ResultCodeEnum.VERIfICATION_PASSWORD_ERROR);
            }
        }else {//普通注册
            //5.直接注册（分配普通大脑的角色）
            //查询普通大脑角色id
            String id = sysRoleMapper.getRoleIdByRoleCode("normerBrain");
            //处理数据
            List<Map<String, Object>> userAndRole = new ArrayList<>();
            Map<String, Object> map = new HashMap<>();
            map.put("id", UUIDUtil.getUUID());
            map.put("userId", sysUser.getId());
            map.put("roleId", id);
            userAndRole.add(map);
            //分配角色
            sysUserAndRoleRelationMapper.addRoleByUserId(userAndRole);
            //注册
            sysUserMapper.addUser(sysUser);
            //设置个人信息
            generatePersonInfo(sysUser);
        }
    }
    //自动生成个人信息记录
    private void generatePersonInfo(SysUser sysUser){
        //插入一条个人信息记录
        SysPersonInfo sysPersonInfo1 = new SysPersonInfo();
        sysPersonInfo1.setId(UUIDUtil.getUUID());
        sysPersonInfo1.setAccountId(sysUser.getId());
        sysPersonInfo1.setCreateBy(sysUser.getUserName());
        sysPersonInfoMapper.addPersonInfo(sysPersonInfo1);
    }

    /**
     * 修改密码
     * @param editPasswordDto
     */
    @Override
    public void editPassword(EditPasswordDto editPasswordDto, String token) {
        //1.判断旧密码是否正确
        if (!AuthContextUtil.get().getPassword().equals(DigestUtils.md5DigestAsHex(editPasswordDto.getOldPassword().getBytes()))){
            throw new MyException(ResultCodeEnum.OLD_PASSWORD_ERROR);
        }
        //2.判断两次输入的密码是否一致
        if (!editPasswordDto.getNewPassword().equals(editPasswordDto.getNewPassword2())){
            throw new MyException(ResultCodeEnum.PASSWORD_MISMATCH);
        }
        //3.新的密码不能和旧密码一样
        if (editPasswordDto.getOldPassword().equals(editPasswordDto.getNewPassword())){
            throw new MyException(ResultCodeEnum.NEW_WITH_OLD_PASSWORD_CAN_NOT_SAME);
        }
        //3.修改密码操作
        sysUserMapper.updatePassword(DigestUtils.md5DigestAsHex(editPasswordDto.getNewPassword().getBytes()), AuthContextUtil.get().getId());

        //清空token
        redisTemplate.delete("user:login"+token);
    }
}
