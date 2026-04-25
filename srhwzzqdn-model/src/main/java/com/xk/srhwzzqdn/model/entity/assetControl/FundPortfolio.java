package com.xk.srhwzzqdn.model.entity.assetControl;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "基金持仓信息实体类")
public class FundPortfolio {
    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "基金代码")
    private String fundCode;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Schema(description = "持仓日期")
    private Date portfolioDate;

    @Schema(description = "持仓类型（1-股票，2-债券，3-基金，4-现金）")
    private Integer positionType;

    @Schema(description = "持仓代码")
    private String positionCode;

    @Schema(description = "持仓名称")
    private String positionName;

    @Schema(description = "持仓数量")
    private BigDecimal positionQuantity;

    @Schema(description = "持仓市值")
    private BigDecimal positionMarketValue;

    @Schema(description = "占净值比例（%）")
    private BigDecimal netRatio;

    @Schema(description = "行业分类（1-通信装备，2-电池，3-半导体，可扩展）")
    private Integer industryType;

    @Schema(description = "数据来源（1-支付宝，2-天天基金）")
    private Integer dataSource;

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
