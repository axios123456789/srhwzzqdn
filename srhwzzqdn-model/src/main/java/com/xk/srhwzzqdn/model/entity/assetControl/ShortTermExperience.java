package com.xk.srhwzzqdn.model.entity.assetControl;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 短线推荐复盘经验库实体（AI数据记忆）
 * <p>
 * T+3/T+5 拉K线验证推荐结果 → 事实归类（不启动/假强/崩车/按信号启动/晋级）→ AI总结经验入库，
 * 经验中的 rule_hint 会注入后续推荐与复盘，形成数据记忆闭环。
 */
@Data
@Schema(description = "短线推荐复盘经验库实体类")
public class ShortTermExperience {
    @Schema(description = "主键自增")
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Schema(description = "经验生成日期")
    private Date expDate;

    @Schema(description = "经验类型：no_start潜伏不启动/fake_out假强出货/crash推荐后崩车/success按信号启动/limit_up晋级成功（字典t_short_term_exp_type）")
    private String expType;

    @Schema(description = "关联推荐类型（字典t_short_term_pick_type）")
    private String pickType;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Schema(description = "原推荐日期")
    private Date tradeDate;

    @Schema(description = "股票代码")
    private String stockCode;

    @Schema(description = "股票名称")
    private String stockName;

    @Schema(description = "事实结果（推荐后实际走势：最高涨幅/最大回撤/是否再启动）")
    private String resultBrief;

    @Schema(description = "经验总结（AI生成，AI失败时规则模板兜底）")
    private String summary;

    @Schema(description = "修正规则提示（注入后续推荐与复盘的依据）")
    private String ruleHint;

    @Schema(description = "AI复盘分析原文")
    private String aiRaw;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "入库时间")
    private Date createdAt;
}
