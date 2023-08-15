package com.runjian.sdk.sdklib.structure;


import com.runjian.sdk.sdklib.NetSDKLib;

/** 
* @author 291189
* @description  压板检测结果 
* @date 2022/06/28 19:44:56
*/
public class NET_PRESSING_PLATE_DETECT extends NetSDKLib.SdkStructure {
/** 
压板状态 {@link com.runjian.sdk.sdklib.enumeration.EM_PRESSING_PLATE_STATE}
*/
public			int					emPressingPlateState;
/** 
包围盒
*/
public NET_RECT stuBoundingBox=new NET_RECT();

public			NET_PRESSING_PLATE_DETECT(){
}
}