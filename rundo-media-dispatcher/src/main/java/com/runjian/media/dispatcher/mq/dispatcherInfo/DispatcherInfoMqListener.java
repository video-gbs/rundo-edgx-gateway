package com.runjian.media.dispatcher.mq.dispatcherInfo;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.media.dispatcher.dto.resp.DispatcherSignInRsp;
import com.runjian.media.dispatcher.mq.event.signIn.DispatcherInfoSignInEvent;
import com.runjian.media.dispatcher.service.DispatcherInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * @author chenjialing
 */
@Slf4j
@Component
public class DispatcherInfoMqListener implements ChannelAwareMessageListener {

    @Value("${dispatcher-info.serialNum}")
    private String serialNum;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    DispatcherInfoService dispatcherInfoService;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        String msg = new String(message.getBody());
        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "网关消息监听服务", "接收到返回消息", msg);

        CommonMqDto commonMqDto = JSONObject.parseObject(msg, CommonMqDto.class);

        //判断是否本网关的信息
        if(!serialNum.equals(commonMqDto.getSerialNum())){
            log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "网关消息监听服务", "拒绝接收非本网关的信息", msg);
            channel.basicRecover(true);
            return;
        }
        try {

            //目前的消息，注册返回，心跳返回(code,data,msg),data(GatewayMqDto中判断消息的类型)
            Integer code = commonMqDto.getCode();
            if(!code.equals(BusinessErrorEnums.SUCCESS.getErrCode())){
                //注册信息失败，无法进行业务队列的监听于发送 todo
                log.error(LogTemplate.ERROR_LOG_TEMPLATE, "网关消息监听服务", "消息接收成功，上层服务创建队列失败", commonMqDto);
                return;
            }
            //判断是否是注册返回
            String msgType = commonMqDto.getMsgType();
            if(msgType.equals(GatewayMsgType.DISPATCH_SIGN_IN.getTypeName())){
                //注册返回 进行业务队列的动态监听
                DispatcherSignInRsp gatewaySignInRsp = JSONObject.toJavaObject((JSONObject) commonMqDto.getData(),DispatcherSignInRsp.class);
                gatewaySignInRsp.setSerialNum(serialNum);
                DispatcherInfoSignInEvent gatewayInfoSignInEvent = new DispatcherInfoSignInEvent(this);
                gatewayInfoSignInEvent.setGatewaySignInRsp(gatewaySignInRsp);
                applicationEventPublisher.publishEvent(gatewayInfoSignInEvent);

            }else if(msgType.equals(GatewayMsgType.DISPATCH_HEARTBEAT.getTypeName())) {
                //心跳todo  暂时不处理
            }else if(msgType.equals(GatewayMsgType.DISPATCH_RE_SIGN_IN.getTypeName())){
                //重新发送注册信息
                dispatcherInfoService.sendRegisterInfo();
            }


        }catch (Exception ex){
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "网关消息监听服务", "消息接收成功，处理失败", message, ex);
        }finally {
            // 手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

}
