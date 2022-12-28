package com.runjian.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BusinessSceneStatusEnum {
    ready("ready"),
    end("end"),

    ;

    private final String typeName;

    public static BusinessSceneStatusEnum getTypeByTypeId(String id){
        id = id.toUpperCase();
        for (BusinessSceneStatusEnum type : values()){
            if (type.typeName.equals(id))
                return type;
        }
        return null;
    }
}
