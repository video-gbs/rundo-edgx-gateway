package com.runjian.common.commonDto;

import lombok.Data;

import java.util.Map;

/**
 * @author chenjialing
 */
@Data
public class StreamCloseDto {
    /**
     * 流id
     */
    private String streamId;

    /**
     * 是否可关闭，无人观看可关闭true,其他异常断流false;
     */
    private Boolean canClose;
}
