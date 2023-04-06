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
public enum DragRoomTypeEnum {
    //    国标协议

    /**预置位
     * start
     */

//    /**预置位
//     * end
//     */
//    public static final int PTZ_LEFT = 2;
//    public static final int PTZ_RIGHT = 1;
//    public static final int PTZ_UP = 8;
//    public static final int PTZ_DOWN = 4;
//    public static final int PTZ_UPLEFT = 10;
//    public static final int PTZ_UPRIGHT = 9;
//    public static final int PTZ_DOWNLEFT = 6;
//    public static final int PTZ_DOWNRIGHT = 5;
//    public static final int PTZ_STOP = 0;
//
//
//    //倍率放大缩小
//    public static final int ZOOM_IN = 20;
//    public static final int ZOOM_OUT = 10;
//
//    //F1指令----start
//    public static final int IRIS_REDUCE = 48;
//    public static final int IRIS_GROW = 44;
//    public static final int FOCUS_NEAR = 42;
//    public static final int FOCUS_FAR = 41;
//    public static final int IRISE_AND_FOCUS_STOP = 40;
    //F1指令----stop


    //云镜控制
    DragZoomIn(1,"DragZoomIn"),
    DragZoomOut(2,"DragZoomOut"),


    ;
    private final Integer code;
    private final String typeName;

    public static DragRoomTypeEnum getTypeByTypeId(Integer id){
        for (DragRoomTypeEnum videoType : values()){
            if (videoType.code.equals(id)){
                return videoType;
            }
        }
        return null;
    }
}