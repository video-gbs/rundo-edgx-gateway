package com.runjian.mq.gatewayInfo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.runjian.common.commonDto.Gb28181Media.req.GatewayBindReq;
import com.runjian.common.commonDto.StreamInfo;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.mq.domain.GatewayMqDto;
import com.runjian.conf.mq.GatewaySignInConf;
import com.runjian.domain.resp.GatewaySignInRsp;
import com.runjian.gb28181.bean.Device;
import com.runjian.mq.event.signIn.GatewayInfoSignInEvent;
import com.runjian.service.IGatewayInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
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
public class GatewayInfoMqListener implements ChannelAwareMessageListener {

    @Value("${gateway-info.serialNum}")
    private String serialNum;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    IGatewayInfoService gatewayInfoService;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        String msg = new String(message.getBody());
        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "网关消息监听服务", "接收到返回消息", msg);

        GatewayMqDto gatewayMqDto = JSONObject.parseObject(msg,GatewayMqDto.class);

        //判断是否本网关的信息
        if(!serialNum.equals(gatewayMqDto.getSerialNum())){
            log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "网关消息监听服务", "拒绝接收非本网关的信息", msg);
            channel.basicRecover(true);
            return;
        }
        try {

            //目前的消息，注册返回，心跳返回(code,data,msg),data(GatewayMqDto中判断消息的类型)
            Integer code = gatewayMqDto.getCode();
            if(!code.equals(BusinessErrorEnums.SUCCESS.getErrCode())){
                //注册信息失败，无法进行业务队列的监听于发送 todo
                log.error(LogTemplate.ERROR_LOG_TEMPLATE, "网关消息监听服务", "消息接收成功，上层服务创建队列失败", gatewayMqDto);
                return;
            }
            //判断是否是注册返回
            String msgType = gatewayMqDto.getMsgType();
            if(msgType.equals(GatewayMsgType.GATEWAY_SIGN_IN.getTypeName())){
                //注册返回 进行业务队列的动态监听
                GatewaySignInRsp gatewaySignInRsp = JSONObject.toJavaObject((JSONObject)gatewayMqDto.getData(),GatewaySignInRsp.class);
                gatewaySignInRsp.setSerialNum(serialNum);
                GatewayInfoSignInEvent gatewayInfoSignInEvent = new GatewayInfoSignInEvent(this);
                gatewayInfoSignInEvent.setGatewaySignInRsp(gatewaySignInRsp);
                applicationEventPublisher.publishEvent(gatewayInfoSignInEvent);

            }else if(msgType.equals(GatewayMsgType.GATEWAY_HEARTBEAT.getTypeName())) {
                //心跳todo  暂时不处理
            }else if(msgType.equals(GatewayMsgType.GATEWAY_RE_SIGN_IN.getTypeName())){
                //重新发送注册信息
                gatewayInfoService.sendRegisterInfo();
            }


        }catch (Exception ex){
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "网关消息监听服务", "消息接收成功，处理失败", message, ex);
        }finally {
            // 手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

}
