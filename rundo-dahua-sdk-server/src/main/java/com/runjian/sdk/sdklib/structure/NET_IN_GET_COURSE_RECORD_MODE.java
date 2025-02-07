package com.runjian.sdk.sdklib.structure;

import com.runjian.sdk.sdklib.NetSDKLib;

/**
 * 获取录像模式入参，对应接口{@link NetSDKLib#CLIENT_GetCourseRecordMode}
 *
 * @author ： 47040
 * @since ： Created in 2020/9/28 15:48
 */
public class NET_IN_GET_COURSE_RECORD_MODE extends NetSDKLib.SdkStructure {
    /**
     * 结构体大小
     */
    public int dwSize;
    /**
     * 教室id号
     */
    public int nClassRoomID;

    public NET_IN_GET_COURSE_RECORD_MODE() {
        dwSize = this.size();
    }
}
