package com.xk.srhwzzqdn.manager.assetControlArea.service.impl;

import com.xk.srhwzzqdn.manager.assetControlArea.mapper.AssetTransactionMapper;
import com.xk.srhwzzqdn.manager.assetControlArea.service.AssetTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AssetTransactionServiceImpl implements AssetTransactionService {
    @Autowired
    private AssetTransactionMapper assetTransactionMapper;
}
