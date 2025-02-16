package com.runjian.sdk.sdklib.structure;


import com.runjian.sdk.sdklib.NetSDKLib;

/** 
* @author 291189
* @description  CLIENT_NASDirectoryGetInfo接口出参 
* @date 2022/09/22 17:28:03
*/
public class NET_OUT_NAS_DIRECTORY_GET_INFO extends NetSDKLib.SdkStructure {
/** 
此结构体大小,必须赋值
*/
public			int					dwSize;
/** 
剩余空间, 单位MB
*/
public			int					nFreeSpace;
/** 
总空间, 单位MB
*/
public			int					nTotalSpace;
/** 
NAS状态 {@link com.runjian.sdk.sdklib.enumeration.EM_NAS_STATE_TYPE}
*/
public			int					emState;

public NET_OUT_NAS_DIRECTORY_GET_INFO(){
		this.dwSize=this.size();
}
}