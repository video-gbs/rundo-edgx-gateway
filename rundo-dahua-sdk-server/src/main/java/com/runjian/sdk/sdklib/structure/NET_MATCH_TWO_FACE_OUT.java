package com.runjian.sdk.sdklib.structure;

import com.runjian.sdk.sdklib.NetSDKLib;

/**
 * @author 47081
 * @version 1.0
 * @description
 * @date 2020/10/19
 */
public class NET_MATCH_TWO_FACE_OUT extends NetSDKLib.SdkStructure {
    public int dwSize;
    /**
     * 两张图片的相似度,范围0~100
     */
    public int nSimilarity;

    public NET_MATCH_TWO_FACE_OUT() {
        this.dwSize = this.size();
    }
}
