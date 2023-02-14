package com.runjian.common.commonDto;

import lombok.Data;

import java.util.Map;

/**
 * @author chenjialing
 */
@Data
public class StreamRespDto {
    /**
     * 流id
     */
    private String streamId;

    /**
     * 其他数据
     */
    private Map<String, Object> dataMap;
}
