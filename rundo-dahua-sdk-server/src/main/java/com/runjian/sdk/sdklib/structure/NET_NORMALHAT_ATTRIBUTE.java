package com.runjian.sdk.sdklib.structure;


import com.runjian.sdk.sdklib.NetSDKLib;
import com.runjian.sdk.sdklib.enumeration.EM_COMPLIANCE_STATE;
import com.runjian.sdk.sdklib.enumeration.EM_WEARING_STATE;

/**
 * @author ： 260611
 * @description ： 普通帽子相关属性状态信息
 * @since ： Created in 2022/03/10 11:17
 */

public class NET_NORMALHAT_ATTRIBUTE extends NetSDKLib.SdkStructure {
    /**
     * 是否有戴普通帽,{@link EM_WEARING_STATE}
     */
    public int emHasHat;
    /**
     * 帽子检测结果,{@link EM_COMPLIANCE_STATE}
     */
    public int emHasLegalHat;
}