package com.runjian.timer.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Miracle
 * @date 2022/4/24 9:29
 */
@Getter
@AllArgsConstructor
public enum RestfulMethodType {

    GET("GET"),
    POST("POST"),
    DELETE("DELETE"),
    PUT("PUT"),
    ERROR_MSG(null);

    private final String msg;


    public static RestfulMethodType getTypeByMag(String msg){
        // 忽略大小写
        msg = msg.toUpperCase();
        for (RestfulMethodType methodType : RestfulMethodType.values()){
            if (methodType.msg.equals(msg)){
                return methodType;
            }
        }
        return ERROR_MSG;
    }

}
