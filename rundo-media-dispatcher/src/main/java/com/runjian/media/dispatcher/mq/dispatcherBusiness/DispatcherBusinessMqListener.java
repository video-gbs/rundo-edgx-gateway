package com.runjian.media.dispatcher.mq.dispatcherBusiness;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.media.dispatcher.zlm.service.ImediaServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 业务状态下队列监听--动态监听
 * @author chenjialing
 */
@Slf4j
@Component
public class DispatcherBusinessMqListener implements ChannelAwareMessageListener {

    @Autowired
    ImediaServerService imediaServerService;
    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        try {
            String msg = new String(message.getBody());
            log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "调度服务消息监听服务调度服务消息监听服务", "接收到返回消息", msg);

            JSONObject jsonMsg = JSONObject.parseObject(msg);

            //转化为接收的通用mq信息
            CommonMqDto commonMqDto = JSONObject.toJavaObject(jsonMsg, CommonMqDto.class);

            //目前的消息，注册返回，心跳返回(code,data,msg),data(GatewayMqDto中判断消息的类型)
            Integer code = commonMqDto.getCode();
            if(!code.equals(BusinessErrorEnums.SUCCESS.getErrCode())){
                //注册信息失败，无法进行业务队列的监听于发送 todo
                log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "调度服务消息监听服务", "消息接收成功，上层服务创建队列失败", jsonMsg, "");
                return;
            }
            JSONObject dataJson = (JSONObject) commonMqDto.getData();
            //实际的请求参数
            JSONObject dataMapJson = dataJson.getJSONObject("dataMap");
            String msgType = commonMqDto.getMsgType();
            //bye请求
            if(msgType.equals(GatewayMsgType.STREAM_PLAY_STOP.getTypeName())){
                //bye指令信息
                String streamId = dataJson.getString("streamId");
                imediaServerService.streamStop(streamId, commonMqDto.getMsgId());

            }else if(msgType.equals(GatewayMsgType.STREAM_CLOSE.getTypeName())){
                Boolean canClose = dataMapJson.getBoolean("result");
                String streamId = dataJson.getString("streamId");
                if(canClose){
                    //针对无人观看的处理 进行实际流的停止处理
                    imediaServerService.streamBye(streamId, commonMqDto.getMsgId());
                }

            }


        }catch (Exception ex){
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "调度服务消息监听服务", "消息接收成功，处理失败", message, ex);
        }finally {
            // 手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

}
