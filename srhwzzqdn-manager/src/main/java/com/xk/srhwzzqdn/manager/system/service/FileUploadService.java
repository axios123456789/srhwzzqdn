package com.xk.srhwzzqdn.manager.system.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    //文件上传
    String upload(MultipartFile file);
}
