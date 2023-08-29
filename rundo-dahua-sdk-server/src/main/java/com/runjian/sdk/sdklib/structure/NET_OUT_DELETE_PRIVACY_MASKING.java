package com.runjian.sdk.sdklib.structure;

import com.runjian.sdk.sdklib.NetSDKLib;

/**
 * @author 251823
 * @description CLIENT_DeletePrivacyMasking 输出参数
 * @date 2022/07/21 17:20:27
 */
public class NET_OUT_DELETE_PRIVACY_MASKING extends NetSDKLib.SdkStructure {
	/**
	 * 结构体大小
	 */
	public int dwSize;

	public NET_OUT_DELETE_PRIVACY_MASKING() {
		this.dwSize = this.size();
	}
}