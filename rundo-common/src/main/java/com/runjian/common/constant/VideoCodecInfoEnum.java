package com.runjian.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 视频枚举
 * @author cjl
 * @date 2022/5/23 17:00
 */
@Getter
@ToString
@AllArgsConstructor
public enum VideoCodecInfoEnum {


    //云镜控制
    H264(0,"H264"),
    H265(1,"H265"),
    AAC(2,"AAC"),
    G711A(3,"G711A"),
    G711U(4,"G711U"),


    ;
    private final Integer code;
    private final String typeName;

    public static VideoCodecInfoEnum getTypeByTypeId(Integer id){
        for (VideoCodecInfoEnum videoType : values()){
            if (videoType.code.equals(id)){
                return videoType;
            }
        }
        return null;
    }
}