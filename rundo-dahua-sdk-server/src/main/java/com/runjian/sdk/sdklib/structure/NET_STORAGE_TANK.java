package com.runjian.sdk.sdklib.structure;

import com.runjian.sdk.sdklib.NetSDKLib;

public class NET_STORAGE_TANK extends NetSDKLib.SdkStructure {
	public NET_STORAGE_TANK() {
		this.dwSize = this.size();
		
		//System.out.println("NET_STORAGE_TANK " + this.size());
	}
	
	public int                 dwSize;
    public int                 nLevel;                                     // 级别, 主机是第0级,其它下属级别类推
    public int                 nTankNo;                                    // 同一级扩展柜内的扩展口编号, 从0开始
    public int                 nSlot;                                      // 对应主柜上的板卡号, 从0开始编号
}
