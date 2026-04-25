package com.xk.srhwzzqdn.manager.assetControlArea.service.impl;

import com.xk.srhwzzqdn.manager.assetControlArea.mapper.FundAssetMapper;
import com.xk.srhwzzqdn.manager.assetControlArea.service.FundAssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FundAssetServiceImpl implements FundAssetService {
    @Autowired
    private FundAssetMapper fundAssetMapper;
}
