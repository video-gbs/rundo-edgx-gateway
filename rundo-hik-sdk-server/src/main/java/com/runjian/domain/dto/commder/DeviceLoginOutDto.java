package com.runjian.domain.dto.commder;

import com.runjian.hik.sdklib.HCNetSDK;
import lombok.Data;

/**
 * 设备注册返回的信息
 * @author chenjialing
 */
@Data
public class DeviceLoginOutDto {

    private boolean result = true;

    private int errorCode;


}
