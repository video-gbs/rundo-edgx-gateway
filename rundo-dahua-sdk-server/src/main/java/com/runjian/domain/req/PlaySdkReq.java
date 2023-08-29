package com.runjian.domain.req;

import lombok.Data;

/**
 * @author chenjialing
 */
@Data
public class PlaySdkReq {
    long lUserId;
    int channelNum;
    int dwStreamType;
    int dwLinkMode;
    String streamId;
}
