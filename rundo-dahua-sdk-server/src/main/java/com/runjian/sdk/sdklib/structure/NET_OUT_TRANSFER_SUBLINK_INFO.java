package com.runjian.sdk.sdklib.structure;


import com.runjian.sdk.sdklib.NetSDKLib;

/** 
CLIENT_TransferSubLinkInfo 接口输出参数
*/
public class NET_OUT_TRANSFER_SUBLINK_INFO extends NetSDKLib.SdkStructure {
/** 
/< 结构体大小
*/
public			int					dwSize;

public NET_OUT_TRANSFER_SUBLINK_INFO(){
    this.dwSize=this.size();
}
}