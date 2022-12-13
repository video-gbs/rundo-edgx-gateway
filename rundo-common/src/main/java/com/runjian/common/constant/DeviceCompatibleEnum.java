package com.runjian.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author chenjialing
 * @date 2022/11/29 14:21
 */
@Getter
@AllArgsConstructor
public enum DeviceCompatibleEnum {

    HUAWEI_NVR_800(0, "华为nvr800兼容"),




    ;
    private Integer type;

    private String msg;
}
