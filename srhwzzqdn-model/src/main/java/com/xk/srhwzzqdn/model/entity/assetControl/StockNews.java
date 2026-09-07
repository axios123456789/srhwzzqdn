package com.xk.srhwzzqdn.model.entity.assetControl;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "股票消息面实体类")
public class StockNews {
    @Schema(description = "主键自增")
    private Integer id;

    @Schema(description = "股票代码")
    private String stockCode;

    @Schema(description = "消息标题")
    private String title;

    @Schema(description = "消息摘要")
    private String summary;

    @Schema(description = "来源（媒体名称/公告栏目）")
    private String source;

    @Schema(description = "消息详情链接")
    private String newsUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    @Schema(description = "发布时间")
    private Date publishTime;

    @Schema(description = "类型：1-新闻 2-公告")
    private Integer newsType;

    @Schema(description = "创建时间")
    private Date createTime;
}
