package com.xk.srhwzzqdn.manager.assetControlArea.controller;

import com.xk.srhwzzqdn.manager.assetControlArea.service.AssetLedgerService;
import com.xk.srhwzzqdn.model.dto.assetControl.AssetLedgerDto;
import com.xk.srhwzzqdn.model.vo.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/superBrain/assetControl/assetLedger")
public class AssetLedgerController {
    @Autowired
    private AssetLedgerService assetLedgerService;

}
