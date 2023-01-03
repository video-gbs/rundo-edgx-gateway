package com.runjian.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Random;

/**
 * @author Miracle
 * @date 2022/4/21 15:58
 */
public class ConstantUtils {

    /**
     * RESTFUL请求工具
     */
    public static final RestTemplate REST_TEMPLATE = new RestTemplate();

    /**
     * JSON处理工具
     */
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    /**
     * 随机数生成工具
     */
    public static final Random RANDOM_UTIL = new Random();

}
