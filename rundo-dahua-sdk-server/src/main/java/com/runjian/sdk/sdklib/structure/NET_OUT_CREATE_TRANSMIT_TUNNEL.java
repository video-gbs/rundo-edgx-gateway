package com.runjian.sdk.sdklib.structure;


import com.runjian.sdk.sdklib.NetSDKLib;

/** 
CLIENT_CreateTransmitTunnel 接口输出参数
*/
public class NET_OUT_CREATE_TRANSMIT_TUNNEL extends NetSDKLib.SdkStructure {
/** 
/<  结构体大小
*/
public			int					dwSize;
/** 
/<  对上侦听端口
*/
public			int					nPort;
/**
私有web代理访问协议 {@link com.runjian.sdk.sdklib.enumeration.EM_WEB_TUNNEL_PROTOCOL}
 */
public			int					emWebProtocol;
public NET_OUT_CREATE_TRANSMIT_TUNNEL(){
    this.dwSize=this.size();
}

}