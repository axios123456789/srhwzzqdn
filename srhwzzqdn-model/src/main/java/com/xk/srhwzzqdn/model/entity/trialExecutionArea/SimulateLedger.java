package com.xk.srhwzzqdn.model.entity.trialExecutionArea;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "模拟台账实体类")
public class SimulateLedger {
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "模拟资产名称")
    private String assetName;

    @Schema(description = "资产编号")
    private String assetCode;

    @Schema(description = "资产类型：1-模拟账户资产 2-A股 3-ETF")
    private Integer assetType;

    @Schema(description = "资产金额")
    private BigDecimal assetAmount;

    @Schema(description = "资产数量（股）")
    private Integer assetQuantity;

    @Schema(description = "所属人")
    private String owner;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
