package com.xk.srhwzzqdn.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期转换工具类
 * 支持 yyyy-MM-dd 和 yyyy-MM-dd HH:mm:ss 两种格式与 java.util.Date 的转换
 */
public class DateConvertUtil {

    private static final Logger log = LoggerFactory.getLogger(DateConvertUtil.class);

    /** 日期格式：yyyy-MM-dd */
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    /** 日期时间格式：yyyy-MM-dd HH:mm:ss */
    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private DateConvertUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ==================== 日期格式（yyyy-MM-dd）====================

    /**
     * 将 yyyy-MM-dd 格式的字符串转换为 java.util.Date
     *
     * @param dateStr 日期字符串，格式：yyyy-MM-dd，如 "2026-03-01"
     * @return Date 对象，转换失败返回 null
     */
    public static Date toDate(String dateStr) {
        if (!StringUtils.hasText(dateStr)) {
            log.debug("日期字符串为空，返回 null");
            return null;
        }

        if (!dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
            log.warn("日期字符串格式不正确，应为 yyyy-MM-dd，实际为: {}", dateStr);
            return null;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
            sdf.setLenient(false);
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            log.error("日期解析失败: {}", dateStr, e);
            return null;
        }
    }

    /**
     * 将 java.util.Date 转换为 yyyy-MM-dd 格式字符串
     *
     * @param date Date 对象
     * @return yyyy-MM-dd 格式字符串，如 "2026-03-01"，date 为 null 时返回 null
     */
    public static String toStringDate(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        return sdf.format(date);
    }

    // ==================== 日期时间格式（yyyy-MM-dd HH:mm:ss）====================

    /**
     * 将 yyyy-MM-dd HH:mm:ss 格式的字符串转换为 java.util.Date
     *
     * @param dateTimeStr 日期时间字符串，格式：yyyy-MM-dd HH:mm:ss，如 "2025-03-01 22:00:59"
     * @return Date 对象，转换失败返回 null
     */
    public static Date toDateTime(String dateTimeStr) {
        if (!StringUtils.hasText(dateTimeStr)) {
            log.debug("日期时间字符串为空，返回 null");
            return null;
        }

        if (!dateTimeStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
            log.warn("日期时间字符串格式不正确，应为 yyyy-MM-dd HH:mm:ss，实际为: {}", dateTimeStr);
            return null;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_PATTERN);
            sdf.setLenient(false);
            return sdf.parse(dateTimeStr);
        } catch (ParseException e) {
            log.error("日期时间解析失败: {}", dateTimeStr, e);
            return null;
        }
    }

    /**
     * 将 java.util.Date 转换为 yyyy-MM-dd HH:mm:ss 格式字符串
     *
     * @param date Date 对象
     * @return yyyy-MM-dd HH:mm:ss 格式字符串，如 "2025-03-01 22:00:59"，date 为 null 时返回 null
     */
    public static String toStringDateTime(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_PATTERN);
        return sdf.format(date);
    }

    // ==================== 通用方法（根据格式自动选择）====================

    /**
     * 根据指定格式将字符串转换为 java.util.Date
     *
     * @param dateStr 日期字符串
     * @param pattern 格式，如 "yyyy-MM-dd" 或 "yyyy-MM-dd HH:mm:ss"
     * @return Date 对象，转换失败返回 null
     */
    public static Date parse(String dateStr, String pattern) {
        if (!StringUtils.hasText(dateStr) || !StringUtils.hasText(pattern)) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            sdf.setLenient(false);
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            log.error("日期解析失败: dateStr={}, pattern={}", dateStr, pattern, e);
            return null;
        }
    }

    /**
     * 根据指定格式将 java.util.Date 转换为字符串
     *
     * @param date    Date 对象
     * @param pattern 格式，如 "yyyy-MM-dd" 或 "yyyy-MM-dd HH:mm:ss"
     * @return 格式化的日期字符串，date 为 null 时返回 null
     */
    public static String format(Date date, String pattern) {
        if (date == null || !StringUtils.hasText(pattern)) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }
}
