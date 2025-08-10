package com.xk.srhwzzqdn.model.vo.common;

import lombok.Getter;

@Getter
public enum ResultCodeEnum {
    SUCCESS(200 , "操作成功") ,
    LOGIN_ERROR(201 , "用户名或者密码错误"),
    VALIDATECODE_ERROR(202 , "验证码错误") ,
    LOGIN_AUTH(208 , "用户未登录"),
    USER_NAME_IS_EXISTS(209 , "大脑账号已经存在"),
    SYSTEM_ERROR(9999 , "您的网络有问题请稍后重试"),
    NODE_ERROR( 217, "该节点下有子节点，不可以删除"),
    DATA_ERROR(204, "数据异常"),
    ACCOUNT_STOP( 216, "账号已被锁"),
    PASSWORD_MISMATCH(210, "两次输入的密码不一致"),
    VERIfICATION_PASSWORD_ERROR(211, "认证密码错误"),
    OLD_PASSWORD_ERROR(212, "旧密码输入错误"),
    NEW_WITH_OLD_PASSWORD_CAN_NOT_SAME(213, "新旧密码不能一样"),
    NOIMOORT_DATA(234, "无导出数据！");



    private Integer code ;      // 业务状态码
    private String message ;    // 响应消息

    private ResultCodeEnum(Integer code , String message) {
        this.code = code ;
        this.message = message ;
    }
}
