package com.runjian.domain.dto.commder;

import com.runjian.entity.DeviceEntity;
import com.runjian.sdk.sdklib.NetSDKLib;
import lombok.Data;
import org.json.JSONObject;

/**
 * 设备注册返回的信息
 * @author chenjialing
 */
@Data
public class DeviceOnlineDto {

    NetSDKLib.NET_DEVICEINFO_Ex deviceinfoV40;

    DeviceEntity deviceEntity;
}
