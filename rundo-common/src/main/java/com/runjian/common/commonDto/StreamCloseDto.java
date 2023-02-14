package com.runjian.common.commonDto;

import lombok.Data;

@Data
public class StreamCloseDto {
    /**
     * 流id
     */
    private String streamId;

    /**
     * 其他数据
     */
    private Boolean canClose = false;
}
