package com.runjian.conf.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum  DeviceTypeEnum {
    //设备大类备是属于哪个产品线，0 保留，1-50 DVR，51-100 DVS，101-150 NVR，151-200 IPC，65534 其他，具体分类方法见《设备类型对应序列号和类型值.docx》
//    DVR	1
//    NVR	2
//    CVR	3
//    DVS	4
//    IPC	5
//设备类型 1-设备 2-NVR 3-DVR 4-CVR
    //云镜控制
    HIKVISION_DVR(3,"DVR"),
    HIKVISION_NVR(2,"NVR"),
    HIKVISION_DVS(4,"DVS"),
    HIKVISION_IPC(1,"IPC"),
    HIKVISION_OTHER(5,"OTHER"),
    HIKVISION_UNKNOWN(0,"UNKNOWN"),

            ;
    private final Integer code;
    private final String typeName;

//    public static DeviceTypeEnum getTypeByTypeId(Integer id){
//        for (DeviceTypeEnum videoType : values()){
//            if (videoType.code.equals(id)){
//                return videoType;
//            }
//        }
//        return null;
//    }
}
