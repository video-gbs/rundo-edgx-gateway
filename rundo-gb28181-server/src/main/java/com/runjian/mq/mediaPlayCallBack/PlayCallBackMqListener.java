package com.runjian.mq.mediaPlayCallBack;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.domain.resp.GatewaySignInRsp;
import com.runjian.mq.event.signIn.GatewayInfoSignInEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * @author chenjialing
 */
@Slf4j
@Component
public class PlayCallBackMqListener implements ChannelAwareMessageListener {




    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        try {
            log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "流媒体调度服务消息监听服务", "接收到返回消息", message);
            String msg = new String(message.getBody());
            JSONObject jsonMsg = JSONObject.parseObject(msg);



        }catch (Exception ex){
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "流媒体调度服务监听服务", "消息接收成功，处理失败", message, ex.getMessage());
        }finally {
            // 手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

}
