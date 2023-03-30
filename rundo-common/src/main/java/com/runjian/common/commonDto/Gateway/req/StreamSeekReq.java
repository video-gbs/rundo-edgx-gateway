package com.runjian.common.commonDto.Gateway.req;

import lombok.Data;

/**
 * @author chenjialing
 */
@Data
public class StreamSeekReq {

    
    String streamId;
    /**
     * 开始时间
     */
    String currentTime;
    /**
     * 结束时间
     */
    String targetTime;
}
