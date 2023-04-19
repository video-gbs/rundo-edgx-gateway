package com.runjian.common.commonDto.Gb28181Media.req;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    String startTime;
    /**
     * 结束时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    String endTime;
}
