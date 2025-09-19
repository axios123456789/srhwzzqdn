package com.xk.srhwzzqdn.manager.memoryReceptionArea.service.impl;

import com.xk.srhwzzqdn.manager.memoryReceptionArea.mapper.RowMemoryAccessMapper;
import com.xk.srhwzzqdn.manager.memoryReceptionArea.service.RowMemoryAccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RowMemoryAccessServiceImpl implements RowMemoryAccessService {
    @Autowired
    private RowMemoryAccessMapper rowMemoryAccessMapper;
}
