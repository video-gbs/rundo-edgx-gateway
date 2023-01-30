package com.runjian.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 网关类型枚举
 * @author cjl
 * @date 2022/5/23 17:00
 */
@Getter
@ToString
@AllArgsConstructor
public enum GatewayTypeEnum {
    //    国标协议
    OTHER("OTHER"),



    ;

    private final String typeName;

    public static GatewayTypeEnum getTypeByTypeId(String id){
        id = id.toUpperCase();
        for (GatewayTypeEnum videoType : values()){
            if (videoType.typeName.equals(id))
                return videoType;
        }
        return null;
    }
}