package com.xk.srhwzzqdn.model.vo.assetControl;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "业绩走势响应实体类")
public class NavEChartsVo {
    @Schema(description = "净值日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date navDate;

    @Schema(description = "单位净值")
    private BigDecimal unitNav;

    @Schema(description = "累计净值")
    private BigDecimal accumulatedNav;

    @Schema(description = "日涨跌幅")
    private BigDecimal dailyChangeRate;

    @Schema(description = "业绩走势")
    private BigDecimal salesTrend;
}
