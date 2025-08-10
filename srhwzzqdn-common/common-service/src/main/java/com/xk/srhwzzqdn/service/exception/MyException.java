package com.xk.srhwzzqdn.service.exception;

import com.xk.srhwzzqdn.model.vo.common.ResultCodeEnum;
import lombok.Data;

//自定义异常
@Data
public class MyException extends RuntimeException{
    private Integer code;
    private String message;
    private ResultCodeEnum resultCodeEnum;

    public MyException (ResultCodeEnum resultCodeEnum){
        this.resultCodeEnum = resultCodeEnum;
        this.code = resultCodeEnum.getCode();
        this.message = resultCodeEnum.getMessage();
    }
}
