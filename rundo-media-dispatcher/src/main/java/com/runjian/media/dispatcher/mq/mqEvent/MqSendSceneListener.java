package com.runjian.media.dispatcher.mq.mqEvent;


import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.StreamBusinessSceneResp;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.constant.StreamBusinessMsgType;
import com.runjian.media.dispatcher.mq.dispatcherBusiness.asyncSender.BusinessAsyncSender;
import com.runjian.media.dispatcher.service.IMediaPlayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 网关注册上线订阅
 * @author chenjialing
 */
@Component
@Slf4j
public class MqSendSceneListener implements ApplicationListener<MqSendSceneEvent> {



    @Autowired
    BusinessAsyncSender businessAsyncSender;

    @Autowired
    IMediaPlayService mediaPlayService;




    @Override
    public void onApplicationEvent(MqSendSceneEvent event) {
        StreamBusinessSceneResp mqSendSceneDto = event.getMqSendSceneDto();
        //点播流程相关，通知设备进行bye的异常执行
        if(mqSendSceneDto.getMsgType().equals(StreamBusinessMsgType.STREAM_LIVE_PLAY_START) || mqSendSceneDto.getMsgType().equals(StreamBusinessMsgType.STREAM_RECORD_PLAY_START)){
            if(mqSendSceneDto.getCode() != BusinessErrorEnums.SUCCESS.getErrCode()){
                //异常点播处理
                mediaPlayService.playBusinessErrorScene(mqSendSceneDto);

            }
        }

        businessAsyncSender.sendforAllScene(mqSendSceneDto);


    }

}
