package com.runjian.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BusinessSceneStatusEnum {
    ready(0),
    running(3),
    end(1),
    TimeOut(2),

    ;

    private final int code;
}


