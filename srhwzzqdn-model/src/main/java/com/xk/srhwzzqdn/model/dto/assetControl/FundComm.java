package com.xk.srhwzzqdn.model.dto.assetControl;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "基金公共条件查询类")
public class FundComm {
    private String fundCode;
    private String beginTime;
    private String endTime;
}
