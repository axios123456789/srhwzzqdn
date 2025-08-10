package com.xk.srhwzzqdn.manager.system.service;

import com.xk.srhwzzqdn.model.vo.system.ValidateCodeVo;

public interface ValidateCodeService {
    //生成图片验证码
    ValidateCodeVo generateValidateCode();
}
