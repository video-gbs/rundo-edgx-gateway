package com.runjian.domain.req;

import lombok.Data;

@Data
public class AlarmforeignReq {
    /**
     * 通道号
     */
    public int  channelId;

    /**
     * 事件名称
     */
    public String  eventName;

    /**
     * 事件编号
     * 2绊线入侵
     * 3区域入侵
     */
    public int  eventCode;

    /**
     * 表示入侵方向, 0-进入, 1-离开,2-出现,3-消失
     */
    public int  bDirection;







}
