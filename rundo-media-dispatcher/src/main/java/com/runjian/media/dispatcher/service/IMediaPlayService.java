package com.runjian.media.dispatcher.service;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gb28181Media.ZlmStreamDto;
import com.runjian.common.commonDto.Gb28181Media.req.*;
import com.runjian.common.commonDto.Gb28181Media.resp.StreamCheckListResp;
import com.runjian.common.commonDto.StreamInfo;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.config.response.GatewayBusinessSceneResp;
import com.runjian.common.config.response.StreamBusinessSceneResp;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.constant.VideoManagerConstants;
import com.runjian.media.dispatcher.dto.entity.OnlineStreamsEntity;
import com.runjian.media.dispatcher.zlm.dto.MediaServerItem;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
     * 录播下载
     * @param req
     */
    void playRecordDownload(MediaRecordDownloadReq req);

    /**
     * 图片下载
     * @param mediaPlayBackReq
     */
    void playPictureDownload(MediaPictureDownloadReq req);

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
     * 流状态变化处理
     *
     * @param json
     */
    void streamChangeDeal(JSONObject json);

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
     * webrtc推流返沪信息
     * @param webRtcTalkReq
     * @return
     */
    StreamInfo webRtcTalk(WebRTCTalkReq webRtcTalkReq);

}
