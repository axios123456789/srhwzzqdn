package com.xk.srhwzzqdn.manager.memoryReceptionArea.controller;

import com.xk.srhwzzqdn.manager.memoryReceptionArea.service.RowMemoryAccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/superBrain/memoryReception/rowMemoryAccess")
public class RowMemoryAccessController {
    @Autowired
    private RowMemoryAccessService rowMemoryAccessService;


}
