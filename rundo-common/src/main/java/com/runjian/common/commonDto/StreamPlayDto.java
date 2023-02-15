package com.runjian.common.commonDto;

import lombok.Data;

import java.util.Map;

/**
 * @author chenjialing
 */
@Data
public class StreamPlayDto {
    /**
     * 流id
     */
    private String streamId;

    /**
     * 是否成功,默认false
     */
    private Boolean isSuccess =false;
}
