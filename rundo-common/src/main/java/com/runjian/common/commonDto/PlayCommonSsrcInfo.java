package com.runjian.common.commonDto;

import lombok.Data;

/**
 * @author chenjialing
 */
@Data
public class PlayCommonSsrcInfo {

    private SsrcInfo ssrcInfo;
    /**
     * wan地址
     */
    private String  hostAddress;

    /**
     * 传输协议
     * UDP/TCP
     */
    private String transport;


}
