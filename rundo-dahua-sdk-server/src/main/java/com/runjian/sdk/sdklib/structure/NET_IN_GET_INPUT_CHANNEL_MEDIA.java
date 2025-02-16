package com.runjian.sdk.sdklib.structure;

import com.runjian.sdk.sdklib.NetSDKLib;

import static com.runjian.sdk.sdklib.NetSDKLib.MAX_PREVIEW_CHANNEL_NUM;

/**
 * 获取录播主机通道输入媒体介质入参 ，对应接口{@link NetSDKLib#CLIENT_GetInputChannelMedia}
 *
 * @author ： 47040
 * @since ： Created in 2020/9/28 16:00
 */
public class NET_IN_GET_INPUT_CHANNEL_MEDIA extends NetSDKLib.SdkStructure {
    /**
     * 结构体大小
     */
    public int dwSize;
    /**
     * 通道数量
     */
    public int nChannelNum;
    /**
     * 通道号
     */
    public int[] nChannel = new int[MAX_PREVIEW_CHANNEL_NUM];

    public NET_IN_GET_INPUT_CHANNEL_MEDIA() {
        dwSize = this.size();
    }
}
