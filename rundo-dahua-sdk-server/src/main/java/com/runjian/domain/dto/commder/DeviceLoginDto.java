package com.runjian.domain.dto.commder;

import com.runjian.sdk.sdklib.NetSDKLib;
import lombok.Data;
import org.json.JSONObject;

/**
 * 设备注册返回的信息
 * @author chenjialing
 */
@Data
public class DeviceLoginDto {

    NetSDKLib.NET_DEVICEINFO_Ex deviceinfoV40;

    private long lUserId;

    private int errorCode = -1;


}
