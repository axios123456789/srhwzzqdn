package com.xk.srhwzzqdn.service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {
    @Bean
    public GroupedOpenApi userApi(){    //创建一个api接口的分组
        return GroupedOpenApi.builder()
                .group("user-api")  //分组名称
                .pathsToMatch("/api/**")    //接口请求路径规则
                .build();
    }

    @Bean
    public GroupedOpenApi srhwzzqdnApi(){
        return GroupedOpenApi.builder()
                .group("srhwzzqdn-api")
                .pathsToMatch("/srhwzzqdn/**")
                .build();
    }

    /***
     * @description 自定义接口信息
     */
    @Bean
    public OpenAPI customOpenAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("私人化外置增强大脑API接口文档")
                        .version("1.0")
                        .description("私人化外置增强大脑API接口文档")
                        .contact(new Contact().name("example")));
    }
}
