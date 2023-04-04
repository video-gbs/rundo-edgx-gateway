package com.runjian.common.commonDto.Gateway.req;

import com.runjian.common.constant.VideoManagerConstants;
import lombok.Data;

/**
 * @author chenjialing
 */
@Data
public class StreamMediaInfoReq {

    
//    String streamId;
    /**
     * 开始时间
     */
    String app = VideoManagerConstants.GB28181_APP;

}
