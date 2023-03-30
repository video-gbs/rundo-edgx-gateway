package com.runjian.media.dispatcher.service.impl;

import com.runjian.common.commonDto.Gb28181Media.BaseRtpServerDto;
import com.runjian.common.commonDto.StreamCloseDto;
import com.runjian.common.constant.*;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.UuidUtil;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.media.dispatcher.conf.mq.DispatcherSignInConf;
import com.runjian.media.dispatcher.dto.entity.OnlineStreamsEntity;
import com.runjian.media.dispatcher.mapper.OnlineStreamsMapper;
import com.runjian.media.dispatcher.service.IOnlineStreamsService;
import com.runjian.media.dispatcher.service.IRedisCatchStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * @author chenjialing
 */
@Service
@Slf4j
public class IOnlineStreamsServiceImpl implements IOnlineStreamsService {
    @Autowired
    OnlineStreamsMapper onlineStreamsMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    IRedisCatchStorageService redisCatchStorageService;

    @Autowired
    DispatcherSignInConf dispatcherSignInConf;

    @Autowired
    RabbitMqSender rabbitMqSender;


    @Override
    public int update(OnlineStreamsEntity onlineStreams) {
        String streamId = onlineStreams.getStreamId();
        OnlineStreamsEntity onlineStreamsEntity = onlineStreamsMapper.selectOne(streamId);
        if(!ObjectUtils.isEmpty(onlineStreamsEntity)){
            onlineStreamsEntity.setGatewaySerialnum(onlineStreams.getGatewaySerialnum());
            onlineStreamsEntity.setMediaServerId(onlineStreams.getMediaServerId());
            onlineStreamsEntity.setRecordState(onlineStreams.getRecordState());
            return onlineStreamsMapper.update(onlineStreamsEntity);

        }else {
            return onlineStreamsMapper.add(onlineStreams);

        }

    }

    @Override
    public int remove(String streamId) {
        return onlineStreamsMapper.deleteBystreamId(streamId);
    }

    @Override
    public int removeAll() {
        return onlineStreamsMapper.deleteAll();
    }

    @Override
    public int removeByStreamList(List<String> streamIdList) {
        return onlineStreamsMapper.deleteBystreamIdList(streamIdList);
    }

    @Async("taskExecutor")
    @Override
    public void streamChangeDeal(String streamId,Boolean regist) {

        if(!regist){
            Object selfStreamBye = RedisCommonUtil.get(redisTemplate, VideoManagerConstants.MEDIA_STREAM_BYE + BusinessSceneConstants.SCENE_SEM_KEY + streamId);

            //推拉流结束 过滤调度中心返回的bye指令 推流自动结束 通知网关和调度服务
            if(!ObjectUtils.isEmpty(selfStreamBye)){
                //自行关闭的流 不进行通知
                RedisCommonUtil.del(redisTemplate,VideoManagerConstants.MEDIA_STREAM_BYE + BusinessSceneConstants.SCENE_SEM_KEY + streamId);
            }else {
                //异常中断的流 非用户主动关闭，进行通知；  可能为设备推流到zlm的网络异常导致zlm判断收流失败了
                log.error(LogTemplate.ERROR_LOG_TEMPLATE, "zlm推流中断异常", "自行中断", streamId);
                StreamCloseDto streamCloseDto = new StreamCloseDto();
                streamCloseDto.setStreamId(streamId);
                streamCloseDto.setCanClose(false);
                CommonMqDto mqInfo = redisCatchStorageService.getMqInfo(GatewayMsgType.STREAM_CLOSE.getTypeName(), GatewayCacheConstants.DISPATCHER_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,null);

                mqInfo.setData(streamCloseDto);
                rabbitMqSender.sendMsgByExchange(dispatcherSignInConf.getMqExchange(), dispatcherSignInConf.getMqSetQueue(), UuidUtil.toUuid(),mqInfo,true);

            }
            //删除流的通知
            remove(streamId);
        }else {
            //更新流信息
            BaseRtpServerDto baseRtpServerDto = (BaseRtpServerDto)RedisCommonUtil.get(redisTemplate, VideoManagerConstants.MEDIA_RTP_SERVER_REQ+BusinessSceneConstants.SCENE_SEM_KEY+streamId);
            if(ObjectUtils.isEmpty(baseRtpServerDto)){
                //缓存不存在或则推流超时了
                log.error(LogTemplate.ERROR_LOG_TEMPLATE, "zlm推流注册异常", "非正常请求点播流", streamId);
            }else {
                //记录流信息
                OnlineStreamsEntity onlineStreamsEntity = new OnlineStreamsEntity();
                onlineStreamsEntity.setGatewaySerialnum(baseRtpServerDto.getGatewayId());
                onlineStreamsEntity.setMediaServerId(baseRtpServerDto.getMediaServerId());
                onlineStreamsEntity.setRecordState(baseRtpServerDto.getRecordState());
                onlineStreamsEntity.setStreamId(streamId);
                update(onlineStreamsEntity);

            }
        }
    }

    @Override
    public List<OnlineStreamsEntity> streamList(String mediaServerId) {


        return onlineStreamsMapper.selectStreamsByMediaServerId(mediaServerId);
    }

    @Override
    public List<OnlineStreamsEntity> streamListByStreamIds(List<String> streamLists) {
        //获取数据库中的数据
        return onlineStreamsMapper.selectStreamsByStreamIds(streamLists);
    }

    @Override
    public List<OnlineStreamsEntity> streamAll() {
        return onlineStreamsMapper.selectAll();
    }
}
