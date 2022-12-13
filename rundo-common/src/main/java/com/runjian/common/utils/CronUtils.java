package com.runjian.common.utils;


import java.time.LocalTime;

/**
 * @author Miracle
 * @date 2022/4/25 14:08
 */
public class CronUtils {


    public static String getCronByLocalTime(LocalTime localTime, Integer weekday){
        if (weekday.equals(7)){
            weekday = 1;
        }else {
            weekday = weekday + 1;
        }
        return localTime.getSecond() + " " +
                localTime.getMinute() + " " +
                localTime.getHour() + " " +
                "?" + " " +
                "*" + " " +
                weekday;
    }
}
