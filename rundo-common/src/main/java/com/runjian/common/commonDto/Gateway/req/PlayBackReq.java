package com.runjian.common.commonDto.Gateway.req;

import lombok.Data;

/**
 * 点播传参
 * @author chenjialing
 */
@Data
public class PlayBackReq extends PlayReq{

    /**
     * 开始时间
     */
    String startTime;
    /**
     * 结束时间
     */
    String endTime;
}
