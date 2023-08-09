package com.runjian.domain.dto.commder;

import com.runjian.hik.sdklib.HCNetSDK;
import lombok.Data;

/**
 * 设备注册返回的信息
 * @author chenjialing
 */
@Data
public class DeviceConfigDto {

    HCNetSDK.NET_DVR_DEVICECFG_V40 devicecfgV40;

    private int errorCode = 0;
}
