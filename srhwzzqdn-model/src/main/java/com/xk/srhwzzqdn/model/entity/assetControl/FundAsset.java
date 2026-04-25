package com.xk.srhwzzqdn.model.entity.assetControl;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "基金资产台账实体类")
public class FundAsset {
    @Schema(description = "主键自增")
    private Integer id;

    @Schema(description = "基金代码")
    private String fundCode;

    @Schema(description = "基金名称")
    private String fundName;

    @Schema(description = "基金类型（1-股票型，2-债券型，3-货币型，4-混合型，5-指数型）")
    private Integer fundType;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Schema(description = "成立日期")
    private Date establishDate;

    @Schema(description = "资产规模（亿元）")
    private BigDecimal assetScale;

    @Schema(description = "基金管理人")
    private String fundCompany;

    @Schema(description = "基金公司描述")
    private String fundCompanyDesc;

    @Schema(description = "基金托管者")
    private String custodian;

    @Schema(description = "基金经理")
    private String fundManager;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Schema(description = "经理任职日期")
    private Date managerStartDate;

    @Schema(description = "经理描述")
    private String managerDesc;

    @Schema(description = "运作方式（1-开放式，2-封闭式）")
    private Integer operationMode;

    @Schema(description = "基金封闭期（天）")
    private Integer closedEndDays;

    @Schema(description = "申购费率（%）")
    private BigDecimal purchaseFeeRate;

    @Schema(description = "赎回费率（%）")
    private BigDecimal redeemFeeRate;

    @Schema(description = "管理费（%）")
    private BigDecimal managementFeeRate;

    @Schema(description = "托管费（%）")
    private BigDecimal custodianFeeRate;

    @Schema(description = "销售服务费（%）")
    private BigDecimal salesServiceFeeRate;

    @Schema(description = "交易规则")
    private String tradeRule;

    @Schema(description = "备注")
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "创建人")
    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "更新人")
    private String updateBy;
}
