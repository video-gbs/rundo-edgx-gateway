package com.runjian.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 网关的协议枚举
 * @author cjl
 * @date 2022/5/23 17:00
 */
@Getter
@ToString
@AllArgsConstructor
public enum GatewayProtocalEnum {
//    国标协议
    GB28181("GB28181"),
    HIK_SDK("HIK_SDK"),



    ;

    private final String typeName;

    public static GatewayProtocalEnum getTypeByTypeId(String id){
        id = id.toUpperCase();
        for (GatewayProtocalEnum videoType : values()){
            if (videoType.typeName.equals(id))
                return videoType;
        }
        return null;
    }
}