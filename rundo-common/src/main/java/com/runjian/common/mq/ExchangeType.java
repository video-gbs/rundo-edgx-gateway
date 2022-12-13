package com.runjian.common.mq;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Miracle
 * @date 2022/5/24 9:36
 */
@Getter
@AllArgsConstructor
public enum ExchangeType {
    // 交换器类型
    TOPIC("TOPIC"),
    DIRECT("DIRECT"),
    FANOUT("FANOUT"),
    HEADERS("HEADERS"),

    UNKNOWN("UNKNOWN")
    ;


    private String type;

    /**
     * 通过msg获取交换器类型
     * @param msg
     * @return
     */
    public static ExchangeType getTypeByType(String type){
        type = type.toUpperCase();
        for (ExchangeType exchangeType : values()){
            if (exchangeType.type.equals(type)) {
                return exchangeType;
            }
        }
        return UNKNOWN;
    }
}
