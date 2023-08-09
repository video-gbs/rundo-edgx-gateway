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
    GB28181("GB28181","gb28181"),
    HIK_SDK("HIK-DEVICE_NET_SDK_V6","HKSDK"),



    ;

    private final String typeName;
    private final String protocal;

    public static GatewayProtocalEnum getTypeByTypeId(String id){
        id = id.toUpperCase();
        for (GatewayProtocalEnum gatewayProtocalEnum : values()){
            if (gatewayProtocalEnum.typeName.equals(id))
                return gatewayProtocalEnum;
        }
        return null;
    }

    public static String getProtocalByTypeName(String typeName){
        typeName = typeName.toUpperCase();
        for (GatewayProtocalEnum gatewayProtocalEnum : values()){
            if (gatewayProtocalEnum.typeName.equals(typeName)){
                return gatewayProtocalEnum.getProtocal();

            }
        }
        return null;
    }
}