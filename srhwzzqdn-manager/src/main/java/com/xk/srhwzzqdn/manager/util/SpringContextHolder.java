package com.xk.srhwzzqdn.manager.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring 上下文持有器
 * 让静态工具类(如 StockQuoteUtil)能获取 Spring Bean
 */
@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextHolder.applicationContext = applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        if (applicationContext == null) return null;
        try {
            return applicationContext.getBean(clazz);
        } catch (Exception e) {
            return null;
        }
    }
}