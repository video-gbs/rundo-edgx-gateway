package com.runjian.common.commonDto.Gb28181Media.req;

import lombok.Data;

/**
 * 点播传参
 * @author chenjialing
 */
@Data
public class MediaPlayBackReq extends MediaPlayReq {

    /**
     * 开始时间
     */
    String startTime;
    /**
     * 结束时间
     */
    String endTime;
}
