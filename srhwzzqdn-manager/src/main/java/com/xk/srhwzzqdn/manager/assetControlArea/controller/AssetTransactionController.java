package com.xk.srhwzzqdn.manager.assetControlArea.controller;

import com.xk.srhwzzqdn.manager.assetControlArea.service.AssetTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/superBrain/assetControl/assetTransaction")
public class AssetTransactionController {
    @Autowired
    private AssetTransactionService assetTransactionService;
}
