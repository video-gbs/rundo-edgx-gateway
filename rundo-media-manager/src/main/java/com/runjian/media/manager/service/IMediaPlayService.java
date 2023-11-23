package com.runjian.media.manager.service;


import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gb28181Media.req.CustomPlayReq;
import com.runjian.common.commonDto.Gb28181Media.req.GatewayStreamNotify;
import com.runjian.common.commonDto.Gb28181Media.req.MediaPlayBackReq;
import com.runjian.common.commonDto.Gb28181Media.req.MediaPlayReq;
import com.runjian.common.commonDto.Gb28181Media.resp.StreamCheckListResp;
import com.runjian.common.commonDto.Gb28181Media.resp.StreamMediaInfoResp;
import com.runjian.common.commonDto.StreamInfo;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.config.response.GatewayBusinessSceneResp;
import com.runjian.common.config.response.StreamBusinessSceneResp;
import com.runjian.media.manager.dto.dto.hook.OnPublishDto;
import com.runjian.media.manager.dto.dto.hook.StreamChangeDto;
import com.runjian.media.manager.dto.entity.OnlineStreamsEntity;

import java.util.List;

/**
 * 点播处理
 * @author chenjialing
 */
public interface IMediaPlayService {


    /**
     * 点播
     *
     * @param mediaPlayReq
     */
    void play(MediaPlayReq mediaPlayReq);

    /**
     * 回放
     *
     * @param mediaPlayBackReq
     */
    void playBack(MediaPlayBackReq mediaPlayBackReq);

    /**
     * 自定义直播
     *
     * @param customPlayReq
     */
    StreamInfo playCustom(CustomPlayReq customPlayReq);

    /**
     * 点播通知
     *
     * @param gatewayStreamNotify
     */
    void streamNotifyServer(GatewayBusinessSceneResp gatewayStreamNotify);

    /**
     * sip成功，但是推流失败的处理
     */
    public void playBusinessErrorScene(StreamBusinessSceneResp businessSceneRespEnd);

    /**
     * 通知网关停止流
     *
     * @param streamId
     * @param msgId
     */
    void streamBye(String streamId, String msgId);


    /**
     * 停止通知+网关停止流的判断
     *
     * @param streamId
     * @param msgId
     */
    void streamStop(String streamId, String msgId);

    /**
     * 获取流列表
     *
     * @param streamCheckListResp
     * @return
     */
    List<OnlineStreamsEntity> streamListByStreamIds(StreamCheckListResp streamCheckListResp, String msgId);

    /**
     * 停止对应流媒体中全部的
     */
    void streamStopAll();


    /**
     * 停止对应流媒体中全部的流
     * @param mediaServerId
     */
    void streamMediaOffline(String mediaServerId);


    /**
     * 停止对应流媒体中全部的流
     * @param mediaServerId
     */
    void streamMediaOnline(String mediaServerId);


    /**
     * 流状态变化通知
     * @param req
     * @param regist
     */
    void streamChangeDeal(StreamChangeDto req, Boolean regist);

    /**
     * 自定义流鉴权
     * @param req
     */
    void streamPublish(OnPublishDto req);

    /**
     * 发送streamClose
     *
     * @param streamId
     * @param canClose
     * @return
     */
    Boolean streamCloseSend(String streamId, Boolean canClose);

    /**
     * 无人观看处理
     *
     * @param app
     * @param streamId
     * @return
     */
    Boolean onStreamNoneReader(String app, String streamId);

    /**
     * 单个流信息获取
     * @param streamId
     * @return
     */
    StreamMediaInfoResp streamMediaInfo(String streamId);

}
