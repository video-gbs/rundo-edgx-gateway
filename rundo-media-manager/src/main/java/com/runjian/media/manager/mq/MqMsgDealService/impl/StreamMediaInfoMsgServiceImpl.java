//package com.runjian.media.manager.mq.MqMsgDealService.impl;
//
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.runjian.common.commonDto.Gb28181Media.resp.StreamAudioMediaInfoResp;
//import com.runjian.common.commonDto.Gb28181Media.resp.StreamMediaInfoResp;
//import com.runjian.common.commonDto.Gb28181Media.resp.StreamVideoMediaInfoResp;
//import com.runjian.common.config.exception.BusinessErrorEnums;
//import com.runjian.common.constant.GatewayCacheConstants;
//import com.runjian.common.constant.GatewayMsgType;
//import com.runjian.common.constant.VideoManagerConstants;
//import com.runjian.common.mq.RabbitMqSender;
//import com.runjian.common.mq.domain.CommonMqDto;
//import com.runjian.common.utils.UuidUtil;
//import com.runjian.media.manager.conf.mq.DispatcherSignInConf;
//import com.runjian.media.manager.dto.entity.OnlineStreamsEntity;
//import com.runjian.media.manager.mq.MqMsgDealService.IMqMsgDealServer;
//import com.runjian.media.manager.mq.MqMsgDealService.IMsgProcessorService;
//import com.runjian.media.manager.service.IOnlineStreamsService;
//import com.runjian.media.manager.service.IRedisCatchStorageService;
//import org.springframework.beans.factory.InitializingBean;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.util.ObjectUtils;
//
//@Component
//public class StreamMediaInfoMsgServiceImpl implements InitializingBean, IMsgProcessorService {
//
//    @Autowired
//    IMqMsgDealServer iMqMsgDealServer;
//
//
//    @Autowired
//    IOnlineStreamsService onlineStreamsService;
//
//
//    @Autowired
//    IRedisCatchStorageService redisCatchStorageService;
//
//    @Autowired
//    RabbitMqSender rabbitMqSender;
//
//    @Autowired
//    DispatcherSignInConf dispatcherSignInConf;
//
//    @Override
//    public void afterPropertiesSet() throws Exception {
//        iMqMsgDealServer.addRequestProcessor(GatewayMsgType.STREAM_MEDIA_INFO.getTypeName(),this);
//    }
//
//
////    {
////        "code" : 0,
////            "online" : true, # 是否在线
////        "readerCount" : 0, # 本协议观看人数
////        "totalReaderCount" : 0, # 观看总人数，包括hls/rtsp/rtmp/http-flv/ws-flv
////        "tracks" : [ # 轨道列表
////        {
////            "channels" : 1, # 音频通道数
////            "codec_id" : 2, # H264 = 0, H265 = 1, AAC = 2, G711A = 3, G711U = 4
////            "codec_id_name" : "CodecAAC", # 编码类型名称
////            "codec_type" : 1, # Video = 0, Audio = 1
////            "ready" : true, # 轨道是否准备就绪
////            "sample_bit" : 16, # 音频采样位数
////            "sample_rate" : 8000 # 音频采样率
////        },
////        {
////            "codec_id" : 0, # H264 = 0, H265 = 1, AAC = 2, G711A = 3, G711U = 4
////            "codec_id_name" : "CodecH264", # 编码类型名称
////            "codec_type" : 0, # Video = 0, Audio = 1
////            "fps" : 59,  # 视频fps
////            "height" : 720, # 视频高
////            "ready" : true,  # 轨道是否准备就绪
////            "width" : 1280 # 视频宽
////        }
////  ]
////    }
//
//    @Override
//    public void process(CommonMqDto commonMqDto) {
//        JSONObject dataJson = (JSONObject) commonMqDto.getData();
//        //实际的请求参数
//        String streamId = dataJson.getString("streamId");
//        JSONObject dataMapJson = dataJson.getJSONObject("dataMap");
//        String app = VideoManagerConstants.GB28181_APP;
//        String schema = VideoManagerConstants.GB28181_SCHEAM;
//        if(!ObjectUtils.isEmpty(dataMapJson)){
//            String app1 = dataJson.getString("app");
//            String schema1 = dataJson.getString("schema");
//            if(!ObjectUtils.isEmpty(app1)){
//                app = app1;
//            }
//            if(!ObjectUtils.isEmpty(schema1)){
//                schema = schema1;
//            }
//        }
//        CommonMqDto businessMqInfo = redisCatchStorageService.getMqInfo(GatewayMsgType.STREAM_MEDIA_INFO.getTypeName(), GatewayCacheConstants.DISPATCHER_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,commonMqDto.getMsgId());
//        String mqGetQueue = dispatcherSignInConf.getMqSetQueue();
//
//        //判断流属于哪个流媒体
//        OnlineStreamsEntity oneBystreamId = onlineStreamsService.getOneBystreamId(streamId);
//        if(ObjectUtils.isEmpty(oneBystreamId)){
//            //流信息不存在
//            businessMqInfo.setCode(BusinessErrorEnums.STREAM_NOT_FOUND.getErrCode());
//            businessMqInfo.setMsg(BusinessErrorEnums.STREAM_NOT_FOUND.getErrMsg());
//            businessMqInfo.setData(false);
//
//            rabbitMqSender.sendMsgByExchange(dispatcherSignInConf.getMqExchange(), mqGetQueue, UuidUtil.toUuid(),businessMqInfo,true);
//            return;
//        }
//        String mediaServerId = oneBystreamId.getMediaServerId();
//
//        MediaServerItem mediaServerItemOne = imediaServerService.getOne(mediaServerId);
//
//
//        JSONObject mediaInfo = zlmresTfulUtils.getMediaInfo(mediaServerItemOne, app, schema, streamId);
//        boolean mediaInfoFlag = mediaInfo.getInteger("code") == 0;
//        if(mediaInfoFlag){
//            StreamMediaInfoResp streamMediaInfoResp = JSONObject.toJavaObject(mediaInfo, StreamMediaInfoResp.class);
//            //数值转驼峰  jsonarray
//            JSONArray tracksArray = (JSONArray)streamMediaInfoResp.getTracks();
//
//            JSONArray objectsArr = new JSONArray();
//            StreamAudioMediaInfoResp streamAudioMediaInfoResp = new StreamAudioMediaInfoResp();
//            StreamVideoMediaInfoResp streamVideoMediaInfoResp = new StreamVideoMediaInfoResp();
//            for (Object trackOne : tracksArray) {
//                JSONObject trackJson = (JSONObject)trackOne;
//                if(trackJson.getInteger("codec_type") == 0){
//                    //视频
//                    streamVideoMediaInfoResp.setCodecName(trackJson.getString("codec_id_name"));
//                    streamVideoMediaInfoResp.setCodecType(0);
//                    streamVideoMediaInfoResp.setFps(trackJson.getInteger("fps"));
//                    streamVideoMediaInfoResp.setHeight(trackJson.getInteger("height"));
//                    streamVideoMediaInfoResp.setWidth(trackJson.getInteger("width"));
//                    streamVideoMediaInfoResp.setReady(trackJson.getBoolean("ready"));
//                    objectsArr.add(streamVideoMediaInfoResp);
//                }else {
//                    streamAudioMediaInfoResp.setChannels(trackJson.getInteger("channels"));
//                    streamAudioMediaInfoResp.setCodecName(trackJson.getString("codec_id_name"));
//                    streamAudioMediaInfoResp.setCodecType(1);
//                    streamAudioMediaInfoResp.setReady(trackJson.getBoolean("ready"));
//                    streamAudioMediaInfoResp.setSampleBit(trackJson.getInteger("sample_bit"));
//                    streamAudioMediaInfoResp.setSampleRate(trackJson.getInteger("sample_rate"));
//                    objectsArr.add(streamAudioMediaInfoResp);
//                }
//
//            }
//            streamMediaInfoResp.setTracks(objectsArr);
//            businessMqInfo.setData(streamMediaInfoResp);
//            rabbitMqSender.sendMsgByExchange(dispatcherSignInConf.getMqExchange(), mqGetQueue, UuidUtil.toUuid(),businessMqInfo,true);
//        }else {
//            //获取信息失败
//            businessMqInfo.setCode(BusinessErrorEnums.STREAM_NOT_FOUND.getErrCode());
//            businessMqInfo.setMsg(BusinessErrorEnums.STREAM_NOT_FOUND.getErrMsg());
//            businessMqInfo.setData(false);
//            rabbitMqSender.sendMsgByExchange(dispatcherSignInConf.getMqExchange(), mqGetQueue, UuidUtil.toUuid(),businessMqInfo,true);
//        }
//    }
//
//
//}
