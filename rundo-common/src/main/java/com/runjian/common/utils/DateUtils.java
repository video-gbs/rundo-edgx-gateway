package com.runjian.common.utils;

import com.runjian.common.config.exception.BusinessException;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Locale;

/**
 * @author Miracle
 * @date 2019/7/9 14:39
 */
public class DateUtils {

    /**
     * 兼容不规范的iso8601时间格式
     */
    private static final String ISO8601_COMPATIBLE_PATTERN = "yyyy-M-d'T'H:m:s";

    /**
     * 用以输出标准的iso8601时间格式
     */
    private static final String ISO8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).withZone(ZoneId.systemDefault());

    public static final DateTimeFormatter DATE_TIME_HOUR_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH", Locale.getDefault()).withZone(ZoneId.systemDefault());

    /**
     * 内部统一时间格式
     */
    public static final String PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String PATTERN_SDK = "yyyyMMddHHmmss";

    public static final String zoneStr = "Asia/Shanghai";

    /**
     * 转换空格符号位地址栏%20
     */
    public static final DateTimeFormatter DATE_TIME_FORMATTER_URL = DateTimeFormatter.ofPattern("yyyy-MM-dd%20HH:mm:ss", Locale.getDefault()).withZone(ZoneId.systemDefault());


    public static final DateTimeFormatter DATE_TIME_T_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).withZone(ZoneId.systemDefault());

    public static final DateTimeFormatter formatterCompatibleISO8601 = DateTimeFormatter.ofPattern(ISO8601_COMPATIBLE_PATTERN, Locale.getDefault()).withZone(ZoneId.of(zoneStr));
    public static final DateTimeFormatter formatterISO8601 = DateTimeFormatter.ofPattern(ISO8601_PATTERN, Locale.getDefault()).withZone(ZoneId.of(zoneStr));
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN, Locale.getDefault()).withZone(ZoneId.of(zoneStr));
    public static final DateTimeFormatter formatterSdk = DateTimeFormatter.ofPattern(PATTERN_SDK, Locale.getDefault()).withZone(ZoneId.of(zoneStr));

    /**
     * 缺省的时间戳格式
     */
    public static final String DEFAULT_TIMESTAMP = "yyyyMMddHHmmss";


    public static Date localDateTime2Date(LocalDateTime localDateTime) {
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zdt = localDateTime.atZone(zoneId);
        return Date.from(zdt.toInstant());
    }

    public static LocalDateTime date2LocalDateTime(Date date) {
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        return instant.atZone(zoneId).toLocalDateTime();
    }

    /**
     * 时间处理，在原有的文字时间加减上特定秒数
     * @param time
     * @param second
     * @return
     * @throws BusinessException
     */
    public static String dealTime(LocalDateTime time, Integer second) {
        return DateUtils.DATE_TIME_FORMATTER.format(time.plusSeconds(second));
    }

    /**
     * 时间字符串转int时间戳 换取当前整点的
     * @param dateStr
     * @return
     */
    public static long dealDateHourTime(String dateStr,Integer second){
        LocalDateTime localDateTime = LocalDateTime.from(DateUtils.DATE_TIME_FORMATTER.parse(dateStr));
        String format = DateUtils.DATE_TIME_HOUR_FORMATTER.format(localDateTime);
        return dealStringToDateHour(format,second);

    }

    public static long dealStringToDateHour(String dateStr,Integer second){
        LocalDateTime localDateTime = LocalDateTime.from(DateUtils.DATE_TIME_HOUR_FORMATTER.parse(dateStr)).plusSeconds(second);
        return localDateTime.toEpochSecond(ZoneOffset.of("+8"));
    }


    /**
     * int时间戳转时间字符串
     * @param timestamp 秒级时间戳
     * @return
     */
    public static String TimeStampToString(long timestamp,Integer second){
        LocalDateTime localDateTime;
        if(second == null){
             localDateTime = Instant.ofEpochSecond(timestamp).atZone(ZoneOffset.ofHours(8)).toLocalDateTime();

        }else {
             localDateTime = Instant.ofEpochSecond(timestamp).atZone(ZoneOffset.ofHours(8)).toLocalDateTime().plusSeconds(second);

        }
        return DateUtils.DATE_TIME_FORMATTER.format(localDateTime);

    }

    /**
     * 时间字符串转int时间戳
     * @param dateStr
     * @return
     */
    public static long StringToTimeStamp(String dateStr){
        LocalDateTime localDateTime = LocalDateTime.from(DateUtils.DATE_TIME_FORMATTER.parse(dateStr));
        return localDateTime.toEpochSecond(ZoneOffset.of("+8"));

    }



    /**
     * 将日期类型转换成指定格式的日期字符串
     *
     * @param date   - Date日期对象
     * @param format - 日期格式字符串
     * @return - 指定日期类型格式的时间字符串
     */
    public static String convert(Date date, String format) {
        return convertDateToStr(date, format);
    }

    /**
     * 将日期类型转换成指定格式的日期字符串
     *
     * @param date       待转换的日期
     * @param dateFormat 日期格式字符串
     * @return String
     */
    public static String convertDateToStr(Date date, String dateFormat) {

        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(date);
    }
    public static String yyyy_MM_dd_HH_mm_ssToISO8601(String formatTime) {

        return formatterISO8601.format(formatter.parse(formatTime));
    }

    public static String ISO8601Toyyyy_MM_dd_HH_mm_ss(String formatTime) {
        return formatter.format(formatterCompatibleISO8601.parse(formatTime));

    }

    /**
     * yyyy_MM_dd_HH_mm_ss 转时间戳
     * @param formatTime
     * @return
     */
    public static String ISO8601ToyyyyMMddHHmmss(String formatTime) {
        return formatterSdk.format(formatter.parse(formatTime));

    }

    /**
     * yyyy_MM_dd_HH_mm_ss 转时间戳
     * @param formatTime
     * @return
     */
    public static long yyyy_MM_dd_HH_mm_ssToTimestamp(String formatTime) {
        TemporalAccessor temporalAccessor = formatter.parse(formatTime);
        Instant instant = Instant.from(temporalAccessor);
        return instant.getEpochSecond();
    }

    /**
     * 获取当前时间
     * @return
     */
    public static String getNow() {
        LocalDateTime nowDateTime = LocalDateTime.now();
        return formatter.format(nowDateTime);
    }

    /**
     * 未来过期时间
     * @param expire
     * @return
     */
    public static String getExpireNow(int expire) {
        LocalDateTime nowDateTime = LocalDateTime.now().plusSeconds(expire);
        return formatter.format(nowDateTime);
    }

    public static String getStringTimeExpireNow(String dateTime,int expire) {
        LocalDateTime localDateTime = LocalDateTime.from(DateUtils.DATE_TIME_FORMATTER.parse(dateTime));
        return formatter.format(localDateTime.plusSeconds(expire));
    }

    /**
     * 未来过期时间
     * @param expire
     * @return
     */
    public static long getExpireTimestamp(int expire) {
        return LocalDateTime.now().plusSeconds(expire).toInstant(ZoneOffset.of("+8")).toEpochMilli();

    }

    /**
     * 时间字符串转int时间戳
     * @param dateStr
     * @return
     */
    public static int stringToYear(String dateStr){
        LocalDateTime localDateTime = LocalDateTime.from(DateUtils.DATE_TIME_FORMATTER.parse(dateStr));
        return localDateTime.getYear();

    }
    /**
     * 时间字符串转int时间戳
     * @param dateStr
     * @return
     */
    public static int stringToMonth(String dateStr){
        LocalDateTime localDateTime = LocalDateTime.from(DateUtils.DATE_TIME_FORMATTER.parse(dateStr));
        return localDateTime.getMonthValue();

    }
    /**
     * 时间字符串转int时间戳
     * @param dateStr
     * @return
     */
    public static int stringToDay(String dateStr){
        LocalDateTime localDateTime = LocalDateTime.from(DateUtils.DATE_TIME_FORMATTER.parse(dateStr));
        return localDateTime.getDayOfMonth();

    }
    /**
     * 时间字符串转int时间戳
     * @param dateStr
     * @return
     */
    public static int stringToHour(String dateStr){
        LocalDateTime localDateTime = LocalDateTime.from(DateUtils.DATE_TIME_FORMATTER.parse(dateStr));
        return localDateTime.getHour();

    }
    /**
     * 时间字符串转int时间戳
     * @param dateStr
     * @return
     */
    public static int stringToMinuts(String dateStr){
        LocalDateTime localDateTime = LocalDateTime.from(DateUtils.DATE_TIME_FORMATTER.parse(dateStr));
        return localDateTime.getMinute();

    }

    /**
     * 时间字符串转int时间戳
     * @param dateStr
     * @return
     */
    public static int stringToSeconds(String dateStr){
        LocalDateTime localDateTime = LocalDateTime.from(DateUtils.DATE_TIME_FORMATTER.parse(dateStr));
        return localDateTime.getSecond();

    }

    /**
     * 格式校验
     * @param timeStr 时间字符串
     * @param dateTimeFormatter 待校验的格式
     * @return
     */
    public static boolean verification(String timeStr, DateTimeFormatter dateTimeFormatter) {
        try {
            LocalDate.parse(timeStr, dateTimeFormatter);
            return true;
        }catch (DateTimeParseException exception) {
            return false;
        }
    }

    public static String getNowForISO8601() {
        LocalDateTime nowDateTime = LocalDateTime.now();
        return formatterISO8601.format(nowDateTime);
    }
}
