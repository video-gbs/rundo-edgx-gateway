package com.runjian.service;

import com.runjian.common.commonDto.Gateway.req.RecordInfoReq;
import com.runjian.gb28181.bean.DeviceChannel;
import com.runjian.gb28181.bean.RecordInfo;

import java.util.List;

/**
 * @author chenjialing
 */
public interface IDeviceChannelService {



    /**
     * 录像列表
     * @param recordInfoReq
     */
    void recordInfo(RecordInfoReq recordInfoReq);

    /**
     * 通道删除
     * @param channelId
     * @param msgId
     */
    void channelHardDelete(String deviceId, String channelId, String msgId);

    /**
     * 软删除通道
     * @param deviceId
     * @param channelId
     * @param msgId
     */
    void channelSoftDelete(String deviceId, String channelId, String msgId);
    /*
    删除恢复
     */
    void channelDeleteRecover(String deviceId, String channelId, String msgId);

    /**
     * 语音对讲
     * @param deviceId
     * @param channelId
     * @param dispacherUrl
     * @param msgId
     */
    void channelTalk(String deviceId, String channelId,String dispacherUrl, String msgId);
}
