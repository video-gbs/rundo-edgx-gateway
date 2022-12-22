package com.runjian.mq.gatewayInfo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * @author chenjialing
 */
@Slf4j
@Component
public class GatewayInfoMqListener implements ChannelAwareMessageListener {



    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        try {
            log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "网关消息监听服务", "接收到返回消息", message);
            String msg = new String(message.getBody());
            JSONObject jsonMsg = JSONObject.parseObject(msg);
            //目前的消息，注册返回，心跳返回(code,data,msg),data(GatewayMqDto中判断消息的类型)
            Integer code = jsonMsg.getInteger("code");
            if(!code.equals(BusinessErrorEnums.SUCCESS.getErrCode())){
                //注册信息失败，无法进行业务队列的监听于发送 todo
                log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "网关消息监听服务", "消息接收成功，上层服务创建队列失败", jsonMsg, "");
                return;
            }
            JSONObject data = jsonMsg.getJSONObject("data");
            if(data == null){
                log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "网关消息监听服务", "消息接收成功，上层服务返回数据异常", jsonMsg, "");
                return;
            }
            //判断是否是注册返回
            String msgType = jsonMsg.getString("msgType");
            if(msgType.equals(GatewayMsgType.GATEWAY_SIGN_IN.getTypeName())){
                //注册返回

                Boolean isFirstSignIn = data.getBoolean("isFirstSignIn");
                //监听队列
                String mqSetQueue = data.getString("mqSetQueue");
                //发送队列
                String mqGetQueue = data.getString("mqGetQueue");
                //交换机
                String mqExchange = data.getString("mqExchange");
                //消息类型
                String signType = data.getString("signType");
                GatewaySignInRsp gatewaySignInRsp = new GatewaySignInRsp();
                gatewaySignInRsp.setIsFirstSignIn(isFirstSignIn);
                gatewaySignInRsp.setMqExchange(mqExchange);
                gatewaySignInRsp.setMqGetQueue(mqGetQueue);
                gatewaySignInRsp.setMqSetQueue(mqSetQueue);
                gatewaySignInRsp.setSignType(signType);
                GatewayInfoSignInEvent gatewayInfoSignInEvent = new GatewayInfoSignInEvent(this);
                gatewayInfoSignInEvent.setGatewaySignInRsp(gatewaySignInRsp);
                applicationEventPublisher.publishEvent(gatewayInfoSignInEvent);

            }else {
                //心跳todo
            }



        }catch (Exception ex){
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "网关消息监听服务", "消息接收成功，处理失败", message, ex.getMessage());
        }finally {
            // 手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

}
