package com.runjian.domain.dto.commder;

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
