package com.runjian.sdk.sdklib.structure;


import com.runjian.sdk.sdklib.NetSDKLib;

/** 
* @author 291189
* @description  呼吸器检测结果 
* @date 2022/06/28 19:44:55
*/
public class NET_RESPIRATOR_DETECT extends NetSDKLib.SdkStructure {
/** 
呼吸器状态 {@link com.runjian.sdk.sdklib.enumeration.EM_RESPIRATOR_STATE}
*/
public			int					emRespiratorState;
/** 
包围盒
*/
public NET_RECT stuBoundingBox=new NET_RECT();

public NET_RESPIRATOR_DETECT(){
}
}