package com.xk.srhwzzqdn.manager;

import com.xk.srhwzzqdn.manager.properties.MinioProperties;
import com.xk.srhwzzqdn.manager.properties.UserProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.xk.srhwzzqdn")
@EnableConfigurationProperties(value = {MinioProperties.class, UserProperties.class})
public class ManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ManagerApplication.class, args);
    }
}
