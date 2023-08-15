package com.runjian.sdk.sdklib.structure;

import com.runjian.sdk.sdklib.NetSDKLib;

/**
 * 获取锁定状态出参 {@link NetSDKLib#CLIENT_OperateCourseCompositeChannel}
 *
 * @author ： 47040
 * @since ： Created in 2020/9/28 20:00
 */
public class NET_OUT_COURSECOMPOSITE_GET_LOCKINFO extends NetSDKLib.SdkStructure {
    /**
     * 结构体大小
     */
    public int dwSize;
    /**
     * 锁定状态 1 true, 0 false
     */
    public int bState;

    public NET_OUT_COURSECOMPOSITE_GET_LOCKINFO() {
        dwSize = this.size();
    }
}
