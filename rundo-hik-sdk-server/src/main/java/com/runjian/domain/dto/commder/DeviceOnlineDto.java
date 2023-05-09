package com.runjian.domain.dto.commder;

import com.runjian.entity.DeviceEntity;
import com.runjian.hik.sdklib.HCNetSDK;
import lombok.Data;

/**
 * 设备注册返回的信息
 * @author chenjialing
 */
@Data
public class DeviceOnlineDto {

    HCNetSDK.NET_DVR_DEVICEINFO_V40 deviceinfoV40;

    DeviceEntity deviceEntity;
}
