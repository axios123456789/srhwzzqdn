package com.xk.srhwzzqdn.model.entity.assetControl;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "基金持仓与份额实体类")
public class FundHolding {
    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "基金代码")
    private String fundCode;

    @Schema(description = "持有份额")
    private BigDecimal holdShares;

    @Schema(description = "可用份额（可赎回份额）")
    private BigDecimal availableShares;

    @Schema(description = "冻结份额")
    private BigDecimal frozenShares;

    @Schema(description = "持仓成本")
    private BigDecimal costAmount;

    @Schema(description = "持仓市值（份额 * 最新净值）")
    private BigDecimal marketValue;

    @Schema(description = "持仓盈亏（市值 - 成本）")
    private BigDecimal profitLoss;

    @Schema(description = "持仓收益率（盈亏/成本*100%）")
    private BigDecimal profitLossRate;

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

    private String owner;
}
