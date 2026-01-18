package com.xk.srhwzzqdn.util;

import io.swagger.v3.oas.annotations.media.Schema;

import java.text.SimpleDateFormat;
import java.util.Date;

@Schema(description = "生成各种编号工具类")
public class GenerateNoUtil {
    public static String generateNo(String begin){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String format_str = formatter.format(new Date());
        return begin + format_str;
    }
}
