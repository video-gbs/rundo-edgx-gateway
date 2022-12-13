package com.runjian.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Miracle
 * @date 2022/5/23 17:00
 */
@Getter
@ToString
@AllArgsConstructor
public enum VideoType{
    RECORD("RECORD"),
    ALARM("ALARM"),
    DOWNLOAD("DOWNLOAD");

    private final String typeName;

    public static VideoType getTypeByTypeId(String id){
        id = id.toUpperCase();
        for (VideoType videoType : values()){
            if (videoType.typeName.equals(id))
                return videoType;
        }
        return null;
    }
}