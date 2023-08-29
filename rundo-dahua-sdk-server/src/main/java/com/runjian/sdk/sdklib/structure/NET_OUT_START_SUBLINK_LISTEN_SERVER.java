package com.runjian.sdk.sdklib.structure;


import com.runjian.sdk.sdklib.NetSDKLib;

/** 
CLIENT_StartSubLinkListenServer 接口输出参数
*/
public class NET_OUT_START_SUBLINK_LISTEN_SERVER extends NetSDKLib.SdkStructure {
/** 
/<  结构体大小
*/
public			int					dwSize;

public NET_OUT_START_SUBLINK_LISTEN_SERVER(){
    this.dwSize=this.size();
}
}