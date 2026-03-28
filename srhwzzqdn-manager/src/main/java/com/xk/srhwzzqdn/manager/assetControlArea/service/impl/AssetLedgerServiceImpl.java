package com.xk.srhwzzqdn.manager.assetControlArea.service.impl;

import com.xk.srhwzzqdn.manager.assetControlArea.mapper.AssetLedgerMapper;
import com.xk.srhwzzqdn.manager.assetControlArea.service.AssetLedgerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AssetLedgerServiceImpl implements AssetLedgerService {
    @Autowired
    private AssetLedgerMapper assetLedgerMapper;
}
