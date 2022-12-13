package com.runjian.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Miracle
 * @date 2022/4/20 14:21
 */
@Getter
@AllArgsConstructor
public enum CommonEnum {

    DISABLE(0, "禁用"),
    ENABLE(1, "启用"),




    ;
    private Integer code;

    private String msg;
}
