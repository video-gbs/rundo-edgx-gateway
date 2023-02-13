package com.runjian.common.commonDto.Gb28181Media;

import lombok.Data;

import java.util.Map;

@Data
public class StreamPlayResult {
    /**
     * 流id
     */
    private String streamId;

    /**
     * 其他数据
     */
    private Boolean isSuccess = false;
}
