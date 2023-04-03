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
public enum PresetOperationTypeEnum {
    //    国标协议
    PresetGet("PresetGet"),
    PresetSet("PresetSet"),
    PresetInvoke("PresetInvoke"),
    PresetDel("PresetDel"),



    ;

    private final String typeName;

    public static PresetOperationTypeEnum getTypeByTypeId(String id){
        id = id.toUpperCase();
        for (PresetOperationTypeEnum videoType : values()){
            if (videoType.typeName.equals(id))
                return videoType;
        }
        return null;
    }
}